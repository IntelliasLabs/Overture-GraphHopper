package com.graphhopper.reader.overture.parser.features;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccessRestrictionsFeatureTest {

    @Test
    @DisplayName("getName() returns enum name")
    void nameMatchesEnum() {
        for (AccessRestrictionsFeature f : AccessRestrictionsFeature.values()) {
            assertEquals(f.name(), f.getName());
        }
    }

    @Test
    @DisplayName("isRequired and isArray flags are correct")
    void flagsAreCorrect() {
        assertTrue(AccessRestrictionsFeature.ACCESS_TYPE.isRequired());
        assertFalse(AccessRestrictionsFeature.ACCESS_TYPE.isArray());

        assertFalse(AccessRestrictionsFeature.WHEN.isRequired());
        assertFalse(AccessRestrictionsFeature.WHEN.isArray());

        assertFalse(AccessRestrictionsFeature.BETWEEN.isRequired());
        assertTrue(AccessRestrictionsFeature.BETWEEN.isArray());
    }

    @Test
    @DisplayName("metadata methods return null by default")
    void metadataDefaults() {
        AccessRestrictionsFeature f = AccessRestrictionsFeature.ACCESS_TYPE;
        assertNull(f.getParentFeature());
        assertNull(f.getOtherName());
    }
}
