package com.graphhopper.reader.overture.names;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the structured {@code names} property of an Overture Map feature.
 * <p>
 * This class encapsulates the Overture naming schema, which is divided into three parts:
 * <ul>
 * <li><b>Primary:</b> The most commonly used, localized name for the feature.</li>
 * <li><b>Common:</b> A map of translations (localized names) keyed by BCP-47 language tags.</li>
 * <li><b>Rules:</b> A list of complex naming rules, including alternate variants (official, short, etc.)
 * and linear referencing (names that only apply to specific sections or sides of a road).</li>
 * </ul>
 *
 * @see <a href="https://docs.overturemaps.org/schema/reference/names/">Overture Maps Names Schema</a>
 */
public class OvertureNames {
    private final String primary;
    private final Map<Bcp47LanguageTag, String> common;
    private final List<OvertureNameRule> rules;

    /**
     * Creates an OvertureNames instance.
     *
     * @param primary the primary, default name of the feature
     * @param common  a map of language tags to localized names (can be null or empty)
     * @param rules   a list of specialized name rules (can be null or empty)
     */
    public OvertureNames(
            String primary, Map<Bcp47LanguageTag, String> common, List<OvertureNameRule> rules) {
        this.primary = primary;
        this.common = common;
        this.rules = rules;
    }

    /**
     * Gets the primary name of the feature.
     *
     * @return the primary name string
     */
    public String getPrimary() {
        return primary;
    }

    /**
     * Gets the map of common localized names.
     *
     * @return a map where keys are BCP-47 language tags and values are name strings
     */
    public Map<Bcp47LanguageTag, String> getCommon() {
        return common;
    }

    /**
     * Gets the list of naming rules.
     *
     * @return the list of {@link OvertureNameRule} objects
     */
    public List<OvertureNameRule> getRules() {
        return rules;
    }

    /**
     * Returns a string representation of the OvertureNames object.
     * <p>
     * The string includes the primary name, the map of common names, and the list of rules.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "OvertureNames{" + "primary='"
                + primary + '\'' + ", common="
                + common + ", rules="
                + rules + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OvertureNames that = (OvertureNames) o;
        return Objects.equals(primary, that.primary)
                && Objects.equals(common, that.common)
                && Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, common, rules);
    }
}
