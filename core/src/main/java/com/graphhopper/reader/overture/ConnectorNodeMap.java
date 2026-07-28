package com.graphhopper.reader.overture;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Overture connector ids to graph node ids, without keeping the id strings.
 *
 * <p>Replaces a {@code HashMap<String, Integer>}, which on a 33-million-segment extract held 44.2
 * million entries and cost roughly 6 GB - about 136 bytes per entry once the map node, the 36-character
 * string with its byte array, and a non-cacheable {@link Integer} are counted. That was the single
 * largest allocation in the import.
 *
 * <p>Overture connector ids are UUIDs, so the 36 characters carry exactly 128 bits. Storing them as two
 * {@code long}s in open-addressed arrays is both smaller and <em>exact</em>: about 20 bytes per slot,
 * roughly 1.3 GB at the same scale. Hashing to a single 64-bit key would have been smaller still, but
 * at 44 million keys the birthday bound gives a real chance of a collision per import, and a collision
 * here silently welds two unrelated junctions together - the precise failure this map exists to
 * prevent.
 *
 * <p>Ids that are not UUIDs fall back to a string map. No Overture data seen so far uses them, but the
 * fallback means malformed input degrades to the old behaviour instead of corrupting topology.
 *
 * <p>Not thread safe; the import is single-threaded.
 */
final class ConnectorNodeMap {

    /** Returned by {@link #get} when the connector has no node yet. */
    static final int NOT_FOUND = -1;

    private static final int DEFAULT_CAPACITY = 1 << 10;
    private static final float LOAD_FACTOR = 0.75f;

    /** A slot is free when both halves are zero, which is why the all-zero UUID is held separately. */
    private long[] keysHi;

    private long[] keysLo;
    private int[] values;

    private int mask;
    private int size;
    private int resizeAt;

    private boolean hasZeroKey;
    private int zeroKeyValue;

    /** Only allocated if a non-UUID id ever turns up. */
    private Map<String, Integer> malformedIds;

    ConnectorNodeMap() {
        allocate(DEFAULT_CAPACITY);
    }

    private void allocate(int capacity) {
        keysHi = new long[capacity];
        keysLo = new long[capacity];
        values = new int[capacity];
        mask = capacity - 1;
        resizeAt = (int) (capacity * LOAD_FACTOR);
    }

    /** @return how many connectors have been assigned a node */
    int size() {
        return size + (hasZeroKey ? 1 : 0) + (malformedIds == null ? 0 : malformedIds.size());
    }

    /**
     * @param connectorId the Overture connector id
     * @return the node id, or {@link #NOT_FOUND}
     */
    int get(String connectorId) {
        long hi = parseHi(connectorId);
        if (hi == PARSE_FAILED) {
            Integer v = malformedIds == null ? null : malformedIds.get(connectorId);
            return v == null ? NOT_FOUND : v;
        }
        long lo = parseLo(connectorId);

        if (hi == 0 && lo == 0) return hasZeroKey ? zeroKeyValue : NOT_FOUND;

        int slot = slotOf(hi, lo);
        return (keysHi[slot] == 0 && keysLo[slot] == 0) ? NOT_FOUND : values[slot];
    }

    /**
     * @param connectorId the Overture connector id
     * @param nodeId the node it resolves to
     */
    void put(String connectorId, int nodeId) {
        long hi = parseHi(connectorId);
        if (hi == PARSE_FAILED) {
            if (malformedIds == null) malformedIds = new HashMap<>();
            malformedIds.put(connectorId, nodeId);
            return;
        }
        long lo = parseLo(connectorId);

        if (hi == 0 && lo == 0) {
            hasZeroKey = true;
            zeroKeyValue = nodeId;
            return;
        }

        int slot = slotOf(hi, lo);
        if (keysHi[slot] == 0 && keysLo[slot] == 0) {
            keysHi[slot] = hi;
            keysLo[slot] = lo;
            values[slot] = nodeId;
            if (++size >= resizeAt) rehash();
        } else {
            values[slot] = nodeId;
        }
    }

    /** @return the slot holding this key, or the free slot where it belongs */
    private int slotOf(long hi, long lo) {
        int slot = hash(hi, lo) & mask;
        while (keysHi[slot] != 0 || keysLo[slot] != 0) {
            if (keysHi[slot] == hi && keysLo[slot] == lo) return slot;
            slot = (slot + 1) & mask;
        }
        return slot;
    }

    private void rehash() {
        long[] oldHi = keysHi;
        long[] oldLo = keysLo;
        int[] oldValues = values;

        allocate(oldHi.length << 1);
        size = 0;
        for (int i = 0; i < oldHi.length; i++) {
            if (oldHi[i] == 0 && oldLo[i] == 0) continue;
            int slot = slotOf(oldHi[i], oldLo[i]);
            keysHi[slot] = oldHi[i];
            keysLo[slot] = oldLo[i];
            values[slot] = oldValues[i];
            size++;
        }
    }

    /** A 64-bit mix of both halves; UUID bits are well distributed but the low bits alone are not. */
    private static int hash(long hi, long lo) {
        long h = hi * 0x9E3779B97F4A7C15L + lo;
        h ^= h >>> 32;
        h *= 0xD6E8FEB86659FD93L;
        h ^= h >>> 32;
        return (int) h;
    }

    // ---------------------------------------------------------------------------------------------
    // UUID parsing. Done by hand rather than through UUID.fromString, which allocates and splits, and
    // is called once per connector reference - 90 million times on the extract this was written for.
    // ---------------------------------------------------------------------------------------------

    /** Sentinel for "not a UUID"; a real high half of exactly this value falls back harmlessly. */
    private static final long PARSE_FAILED = 0xFFFF_FFFF_FFFF_FFFFL;

    private static final int[] DASHES = {8, 13, 18, 23};

    /**
     * @return the top 64 bits, or {@link #PARSE_FAILED}
     */
    private static long parseHi(String id) {
        if (!looksLikeUuid(id)) return PARSE_FAILED;
        long hi = accumulate(id, 0, 8, 0);
        if (hi == PARSE_FAILED) return PARSE_FAILED;
        hi = accumulate(id, 9, 13, hi);
        if (hi == PARSE_FAILED) return PARSE_FAILED;
        return accumulate(id, 14, 18, hi);
    }

    /**
     * @return the bottom 64 bits; only meaningful once {@link #parseHi} has succeeded
     */
    private static long parseLo(String id) {
        long lo = accumulate(id, 19, 23, 0);
        if (lo == PARSE_FAILED) return 0;
        lo = accumulate(id, 24, 36, lo);
        return lo == PARSE_FAILED ? 0 : lo;
    }

    /**
     * Folds the hex digits of {@code [from, to)} into {@code acc}.
     *
     * <p>Index ranges rather than an index array, because this runs once per connector reference - 90
     * million times on a continental extract - and an allocation per call would dominate it.
     *
     * @return the accumulated value, or {@link #PARSE_FAILED} on a non-hex character
     */
    private static long accumulate(String id, int from, int to, long acc) {
        for (int i = from; i < to; i++) {
            int nibble = nibble(id.charAt(i));
            if (nibble < 0) return PARSE_FAILED;
            acc = (acc << 4) | nibble;
        }
        return acc;
    }

    private static boolean looksLikeUuid(String id) {
        if (id == null || id.length() != 36) return false;
        for (int d : DASHES) if (id.charAt(d) != '-') return false;
        return true;
    }

    /** @return the hex value of {@code c}, or -1 */
    private static int nibble(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }
}
