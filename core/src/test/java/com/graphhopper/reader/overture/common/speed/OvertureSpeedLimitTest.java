package com.graphhopper.reader.overture.common.speed;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class OvertureSpeedLimitTest {
    private static final double DELTA = 0.0001;

    /**
     * Helper to create the OvertureSpeed object, reducing boilerplate in tests.
     */
    private OvertureSpeed createSpeed(Double value, String unitStr) {
        return new OvertureSpeed(value, SpeedUnit.fromString(unitStr));
    }
    /**
     * Helper factory method to create {@link OvertureSpeedLimit} instances for legacy tests.
     * <p>
     * This method isolates tests that focus solely on speed values/units from changes in the
     * {@link OvertureSpeedLimit} constructor (e.g., new fields like {@code between} or {@code when}).
     * It explicitly passes {@code null} for the geometry and scope fields.
     * </p>
     */
    private OvertureSpeedLimit createLimit(OvertureSpeed max, OvertureSpeed min, Boolean isVar) {
        return new OvertureSpeedLimit(max, min, isVar, null, null);
    }

    @Test
    void testStandardKmh() {
        OvertureSpeedLimit limit =
                createLimit(createSpeed(50.0, "km/h"), createSpeed(30.0, "km/h"), false);

        assertEquals(50.0, limit.getMaxSpeedKmh(), DELTA);
        assertEquals(30.0, limit.getMinSpeedKmh(), DELTA);
    }

    @Test
    void testStandardMph() {
        OvertureSpeedLimit limit = createLimit(createSpeed(10.0, "mph"), createSpeed(5.0, "mph"), true);

        assertEquals(16.0934, limit.getMaxSpeedKmh(), DELTA);
        assertEquals(8.0467, limit.getMinSpeedKmh(), DELTA);
        assertTrue(limit.isMaxSpeedVariable());
    }

    @Test
    void testMixedUnits() {
        OvertureSpeedLimit limit =
                createLimit(createSpeed(10.0, "mph"), createSpeed(30.0, "km/h"), false);

        assertEquals(16.0934, limit.getMaxSpeedKmh(), DELTA);
        assertEquals(30.0, limit.getMinSpeedKmh(), DELTA);
    }

    @Test
    void testMphVariations() {
        OvertureSpeedLimit limit =
                createLimit(createSpeed(10.0, "mph"), createSpeed(5.0, "mph"), false);

        assertEquals(16.0934, limit.getMaxSpeedKmh(), DELTA, "Failed to convert variant: " + "mph");
    }

    @ParameterizedTest(name = "Should ignore conversion with unknown unit: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"knots"})
    void testUnknownUnitIgnored(String unit) {
        OvertureSpeedLimit limit = createLimit(createSpeed(50.0, unit), createSpeed(5.0, unit), false);

        assertEquals(50.0, limit.getMaxSpeedKmh(), DELTA, "Failed to convert variant: " + unit);
    }

    @Test
    void testMissingSpeedDataHandling() {
        OvertureSpeedLimit nullSpeedObj = createLimit(null, null, false);
        OvertureSpeedLimit nullValueObj = createLimit(createSpeed(null, "km/h"), null, false);

        assertNull(nullSpeedObj.getMaxSpeedKmh());
        assertNull(nullSpeedObj.getMinSpeedKmh());
        assertNull(nullValueObj.getMaxSpeedKmh());
    }

    @Test
    void testFieldsStorage() {
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.2, 0.8);
        PropertyScopeContainer scope =
                new PropertyScopeContainer("Mo-Fr 08:00-18:00", null, null, null, null, null);
        OvertureSpeedLimit limit =
                new OvertureSpeedLimit(createSpeed(50.0, "km/h"), null, false, range, scope);

        assertEquals(range, limit.getBetween());
        assertEquals(scope, limit.getWhen());
    }

    @Test
    void testEqualsAndHashCode() {
        /// Base Object
        OvertureSpeedLimit base = new OvertureSpeedLimit(
                createSpeed(50.0, "km/h"),
                createSpeed(30.0, "km/h"),
                false,
                new LinearlyReferencedRange(2.0, 4.5),
                new PropertyScopeContainer("Mo-Fr 08:00-18:00", null, null, null, null, null));

        /// Exact Copy
        OvertureSpeedLimit copy = new OvertureSpeedLimit(
                createSpeed(50.0, "km/h"),
                createSpeed(30.0, "km/h"),
                false,
                new LinearlyReferencedRange(2.0, 4.5),
                new PropertyScopeContainer("Mo-Fr 08:00-18:00", null, null, null, null, null));
        assertEquals(base, copy, "Identical objects must be equal");
        assertEquals(base.hashCode(), copy.hashCode(), "HashCodes must match");

        /// Diff Speed
        OvertureSpeedLimit diffSpeed =
                createLimit(createSpeed(60.0, "km/h"), createSpeed(30.0, "km/h"), false);
        assertNotEquals(base, diffSpeed, "Must differ by speed");

        /// Diff Geometry (Between)
        LinearlyReferencedRange range = new LinearlyReferencedRange(0.0, 1.0);
        OvertureSpeedLimit diffGeo = new OvertureSpeedLimit(
                createSpeed(50.0, "km/h"), createSpeed(30.0, "km/h"), false, range, null);
        assertNotEquals(base, diffGeo, "Must differ by geometry (between)");

        /// 5. Diff Scope (When)
        PropertyScopeContainer scope =
                new PropertyScopeContainer("Sun 08:00-18:00", null, null, null, null, null);
        OvertureSpeedLimit diffScope = new OvertureSpeedLimit(
                createSpeed(50.0, "km/h"), createSpeed(30.0, "km/h"), false, null, scope);
        assertNotEquals(base, diffScope, "Must differ by scope (when)");

        /// Null checks
        assertNotEquals(null, base);
        assertNotEquals(new Object(), base);
    }

    @Test
    void testToStringContainsData() {
        OvertureSpeedLimit limit = createLimit(createSpeed(50.0, "mph"), null, true);
        String str = limit.toString();

        assertTrue(str.contains("50.0"));
        assertTrue(str.contains("mph"));
    }
}
