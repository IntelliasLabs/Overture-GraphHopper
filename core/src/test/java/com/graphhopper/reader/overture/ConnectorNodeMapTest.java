package com.graphhopper.reader.overture;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers {@link ConnectorNodeMap}.
 *
 * <p>Worth testing directly rather than only through an import: this map decides which segments share a
 * node, so a probing or parsing bug does not crash, it silently welds unrelated junctions together or
 * tears real ones apart - which is the exact failure the connector work set out to fix.
 */
class ConnectorNodeMapTest {

    @Test
    @DisplayName("A stored connector reads back; an unknown one reports NOT_FOUND")
    void storesAndReports() {
        ConnectorNodeMap map = new ConnectorNodeMap();
        String id = UUID.randomUUID().toString();

        assertEquals(ConnectorNodeMap.NOT_FOUND, map.get(id));

        map.put(id, 42);

        assertEquals(42, map.get(id));
        assertEquals(1, map.size());
        assertEquals(ConnectorNodeMap.NOT_FOUND, map.get(UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Agrees with a HashMap across a large random set, through many resizes")
    void agreesWithHashMapAtScale() {
        // The point is the open addressing: linear probing plus repeated rehashing is where an
        // off-by-one silently starts returning another connector's node.
        ConnectorNodeMap map = new ConnectorNodeMap();
        Map<String, Integer> reference = new HashMap<>();
        Random random = new Random(20260813L);

        for (int i = 0; i < 200_000; i++) {
            String id = new UUID(random.nextLong(), random.nextLong()).toString();
            reference.put(id, i);
            map.put(id, i);
        }

        assertEquals(reference.size(), map.size());
        reference.forEach((id, node) -> assertEquals(node, map.get(id), id));
    }

    @Test
    @DisplayName("Re-putting a connector overwrites rather than duplicating")
    void putOverwrites() {
        ConnectorNodeMap map = new ConnectorNodeMap();
        String id = UUID.randomUUID().toString();

        map.put(id, 1);
        map.put(id, 2);

        assertEquals(2, map.get(id));
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("Case and the all-zero UUID are handled, since both break naive parsing")
    void handlesCaseAndZeroUuid() {
        ConnectorNodeMap map = new ConnectorNodeMap();

        // An all-zero UUID collides with the free-slot marker, so it takes a separate path.
        String zero = "00000000-0000-0000-0000-000000000000";
        map.put(zero, 7);
        assertEquals(7, map.get(zero));

        // Upper and lower case hex must resolve to the same 128 bits.
        String lower = "34413a1f-d027-4c25-af96-031225672581";
        map.put(lower, 9);
        assertEquals(9, map.get(lower.toUpperCase()));
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("UUIDs differing only in their last nibble stay distinct")
    void doesNotConflateNearlyIdenticalIds() {
        // Both halves of the id must reach the key. Keying on the high 64 bits alone - or on a 64-bit
        // hash - is what would merge these two.
        ConnectorNodeMap map = new ConnectorNodeMap();
        String a = "34413a1f-d027-4c25-af96-031225672581";
        String b = "34413a1f-d027-4c25-af96-031225672582";

        map.put(a, 1);
        map.put(b, 2);

        assertEquals(1, map.get(a));
        assertEquals(2, map.get(b));
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("Ids that are not UUIDs still work, rather than colliding on a parse failure")
    void fallsBackForNonUuidIds() {
        // No Overture data seen so far has these. If some ever does, the wrong outcome is silently
        // treating every malformed id as the same key.
        ConnectorNodeMap map = new ConnectorNodeMap();

        map.put("not-a-uuid", 1);
        map.put("also/not/a/uuid", 2);
        map.put("34413a1f-d027-4c25-af96-03122567258", 3); // 35 chars
        map.put("34413a1f-d027-4c25-af96-0312256725zz", 4); // non-hex

        assertEquals(1, map.get("not-a-uuid"));
        assertEquals(2, map.get("also/not/a/uuid"));
        assertEquals(3, map.get("34413a1f-d027-4c25-af96-03122567258"));
        assertEquals(4, map.get("34413a1f-d027-4c25-af96-0312256725zz"));
        assertEquals(4, map.size());
    }
}
