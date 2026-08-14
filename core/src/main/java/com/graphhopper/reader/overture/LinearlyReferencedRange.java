package com.graphhopper.reader.overture;

import java.util.Objects;

/**
 * Represents a normalized range along a linear geometry using linear referencing.
 * <p>
 * Values for {@code start} and {@code end} are expressed as a fraction of the total
 * geometry length, where 0.0 is the beginning and 1.0 is the end of the segment.
 * This is used to map attributes (like speed limits or surfaces) that only apply
 * to a specific portion of a road.
 * </p>
 */
public class LinearlyReferencedRange {
    private final double start;
    private final double end;

    /**
     * Creates a new range.
     * @param start position from 0.0 to 1.0
     * @param end position from 0.0 to 1.0
     */
    public LinearlyReferencedRange(double start, double end) {
        this.start = start;
        this.end = end;
    }

    /** Returns the start position of the range. */
    public double getStart() {
        return start;
    }

    /** Returns the end position of the range. */
    public double getEnd() {
        return end;
    }

    /**
     * Validates that the range is within [0, 1] bounds and start is before end.
     * @return {@code true} if the range is logically valid for linear referencing.
     */
    public boolean isValid() {
        return start < end && start >= 0 && end <= 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinearlyReferencedRange that)) return false;
        return Double.compare(start, that.start) == 0 && Double.compare(end, that.end) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "LinearlyReferencedRange{start=" + start + ", end=" + end + '}';
    }
}
