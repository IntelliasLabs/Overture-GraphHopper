package com.graphhopper.reader.overture.names;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import java.util.Objects;

/**
 * Represents a single naming rule in the Overture Maps schema.
 * <p>
 * Rules capture complex naming scenarios that cannot be represented in the simple {@code common} map.
 * This includes:
 * <ul>
 * <li><b>Variants:</b> specific name types like official, alternate, or short names.</li>
 * <li><b>Geometric Scoping:</b> names that apply only to a specific side ({@code side}) or
 * segment range ({@code between}) of a transportation feature.</li>
 * <li><b>Political Perspectives:</b> names that are disputed or only recognized by specific entities.</li>
 * </ul>
 */
public class OvertureNameRule implements HasBetweenProperty {
    private final Variant variant;
    private final Bcp47LanguageTag language;
    private final Perspectives perspectives;
    private final String value;
    private final LinearlyReferencedRange between;
    private final Side side;

    /**
     * Creates a new OvertureNameRule.
     *
     * @param variant      the variant type
     * @param language     the language tag
     * @param perspectives the political perspectives object
     * @param value        the name string
     * @param between      the linear reference range (nullable)
     * @param side         the side of the road (nullable)
     */
    public OvertureNameRule(
            Variant variant,
            Bcp47LanguageTag language,
            Perspectives perspectives,
            String value,
            LinearlyReferencedRange between,
            Side side) {
        this.variant = variant;
        this.language = language;
        this.perspectives = perspectives;
        this.value = value;
        this.between = between;
        this.side = side;
    }

    /**
     * Gets the variant type.
     *
     * @return the variant enum
     */
    public Variant getVariant() {
        return variant;
    }

    /**
     * Gets the language tag.
     *
     * @return the BCP-47 language object
     */
    public Bcp47LanguageTag getLanguage() {
        return language;
    }

    /**
     * Gets the political perspectives.
     *
     * @return the perspectives object containing accepted/disputed lists
     */
    public Perspectives getPerspectives() {
        return perspectives;
    }

    /**
     * Gets the name value.
     *
     * @return the name string
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the linear reference range.
     *
     * @return the range object, or null if not applicable
     */
    @Override
    public LinearlyReferencedRange getBetween() {
        return between;
    }

    /**
     * Gets the side of the road.
     *
     * @return the side enum, or null if not applicable
     */
    public Side getSide() {
        return side;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OvertureNameRule that)) return false;
        return variant == that.variant
                && Objects.equals(language, that.language)
                && Objects.equals(perspectives, that.perspectives)
                && Objects.equals(value, that.value)
                && Objects.equals(between, that.between)
                && side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variant, language, perspectives, value, between, side);
    }

    /**
     * Returns a string representation of the OvertureNameRule.
     *
     * @return a string describing the rule
     */
    @Override
    public String toString() {
        return "OvertureNameRule{variant=" + variant + ", language=" + language + ", perspectives="
                + perspectives + ", value='" + value + "', between="
                + between + ", side=" + side + '}';
    }
}
