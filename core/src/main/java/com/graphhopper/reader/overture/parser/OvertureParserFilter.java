package com.graphhopper.reader.overture.parser;

import com.graphhopper.reader.overture.parser.filters.*;
import java.util.Objects;

/**
 * Holds the collection of {@link com.graphhopper.reader.overture.parser.filters.OvertureFilter} instances
 * that are used by {@link OvertureExtractor} and {@link OvertureParser} to restrict which Overture
 * data is accepted during parsing.
 * <p>
 * Each filter manages a set of allowed values for a specific domain (road class, access type,
 * theme, travel mode, etc.) and is initialized with default values in the constructor.
 */
public class OvertureParserFilter {

    /**
     * The default, globally shared instance of the filter, initialized with
     * standard allowed values for all Overture features.
     */
    public static final OvertureParserFilter INSTANCE = new OvertureParserFilter();

    private RoadClassFilter roadClassFilter;
    private DestinationLabelTypeFilter destinationLabelTypeFilter;
    private DestinationSymbolFilter destinationSymbolFilter;
    private AccessTypeFilter accessTypeFilter;
    private ComparisonOperatorFilter comparisonOperatorFilter;
    private DimensionRestrictionFilter dimensionRestrictionFilter;
    private FeatureTypeFilter featureTypeFilter;
    private RecognizedStatusFilter recognizedStatusFilter;
    private RoadSubclassFilter roadSubclassFilter;
    private SideFilter sideFilter;
    private ThemeFilter themeFilter;
    private TravelHeadingFilter travelHeadingFilter;
    private TravelModeFilter travelModeFilter;
    private TravelReasonFilter travelReasonFilter;
    private UnitsFilter unitsFilter;
    private VariantFilter variantFilter;
    private SpeedUnitFilter speedUnitFilter;
    private SurfaceTypeFilter surfaceType;
    private ModeFilter modeFilter;

    // TODO: Add other filters as needed

    /**
     * Constructs a new filter registry and initializes all internal filters.
     * <p>
     * Each filter is instantiated and its {@code initializeAllowedValues()} method
     * is called to populate the default whitelists based on the Overture specification.
     * </p>
     */
    public OvertureParserFilter() {
        roadClassFilter = new RoadClassFilter();
        roadClassFilter.initializeAllowedValues();

        destinationLabelTypeFilter = new DestinationLabelTypeFilter();
        destinationLabelTypeFilter.initializeAllowedValues();

        destinationSymbolFilter = new DestinationSymbolFilter();
        destinationSymbolFilter.initializeAllowedValues();

        accessTypeFilter = new AccessTypeFilter();
        accessTypeFilter.initializeAllowedValues();

        comparisonOperatorFilter = new ComparisonOperatorFilter();
        comparisonOperatorFilter.initializeAllowedValues();

        dimensionRestrictionFilter = new DimensionRestrictionFilter();
        dimensionRestrictionFilter.initializeAllowedValues();

        featureTypeFilter = new FeatureTypeFilter();
        featureTypeFilter.initializeAllowedValues();

        recognizedStatusFilter = new RecognizedStatusFilter();
        recognizedStatusFilter.initializeAllowedValues();

        roadSubclassFilter = new RoadSubclassFilter();
        roadSubclassFilter.initializeAllowedValues();

        sideFilter = new SideFilter();
        sideFilter.initializeAllowedValues();

        themeFilter = new ThemeFilter();
        themeFilter.initializeAllowedValues();

        travelHeadingFilter = new TravelHeadingFilter();
        travelHeadingFilter.initializeAllowedValues();

        travelModeFilter = new TravelModeFilter();
        travelModeFilter.initializeAllowedValues();

        travelReasonFilter = new TravelReasonFilter();
        travelReasonFilter.initializeAllowedValues();

        unitsFilter = new UnitsFilter();
        unitsFilter.initializeAllowedValues();

        variantFilter = new VariantFilter();
        variantFilter.initializeAllowedValues();

        speedUnitFilter = new SpeedUnitFilter();
        speedUnitFilter.initializeAllowedValues();

        surfaceType = new SurfaceTypeFilter();
        surfaceType.initializeAllowedValues();

        modeFilter = new ModeFilter();
        modeFilter.initializeAllowedValues();
    }

    /**
     * Returns the filter controlling allowed road classes.
     */
    public RoadClassFilter getRoadClassFilter() {
        return roadClassFilter;
    }

    /**
     * Returns the filter for allowed destination label types.
     */
    public DestinationLabelTypeFilter getDestinationLabelTypeFilter() {
        return destinationLabelTypeFilter;
    }

    /**
     * Returns the filter for allowed destination symbols.
     */
    public DestinationSymbolFilter getDestinationSymbolFilter() {
        return destinationSymbolFilter;
    }

    /**
     * Returns the filter that controls allowed access types.
     */
    public AccessTypeFilter getAccessTypeFilter() {
        return accessTypeFilter;
    }

    /**
     * Returns the filter that controls allowed comparison operators used in rules.
     */
    public ComparisonOperatorFilter getComparisonOperatorFilter() {
        return comparisonOperatorFilter;
    }

    /**
     * Returns the filter that controls allowed dimension restriction units and values.
     */
    public DimensionRestrictionFilter getDimensionRestrictionFilter() {
        return dimensionRestrictionFilter;
    }

    /**
     * Returns the filter that restricts allowed feature types.
     */
    public FeatureTypeFilter getFeatureTypeFilter() {
        return featureTypeFilter;
    }

    /**
     * Returns the filter that indicates which recognition statuses are allowed.
     */
    public RecognizedStatusFilter getRecognizedStatusFilter() {
        return recognizedStatusFilter;
    }

    /**
     * Returns the filter that controls allowed road subclass values.
     */
    public RoadSubclassFilter getRoadSubclassFilter() {
        return roadSubclassFilter;
    }

    /**
     * Returns the filter that controls allowed side values (e.g. left/right).
     */
    public SideFilter getSideFilter() {
        return sideFilter;
    }

    /**
     * Returns the filter that restricts allowed theme values.
     */
    public ThemeFilter getThemeFilter() {
        return themeFilter;
    }

    /**
     * Returns the filter that restricts allowed travel headings.
     */
    public TravelHeadingFilter getTravelHeadingFilter() {
        return travelHeadingFilter;
    }

    /**
     * Returns the filter that restricts allowed travel modes.
     */
    public TravelModeFilter getTravelModeFilter() {
        return travelModeFilter;
    }

    /**
     * Returns the filter that restricts allowed travel reasons.
     */
    public TravelReasonFilter getTravelReasonFilter() {
        return travelReasonFilter;
    }

    /**
     * Returns the filter that restricts allowed units.
     */
    public UnitsFilter getUnitsFilter() {
        return unitsFilter;
    }

    /**
     * Returns the filter that restricts allowed variant values.
     */
    public VariantFilter getVariantFilter() {
        return variantFilter;
    }

    /**
     * Returns the filter that restricts allowed speed unit values.
     */
    public SpeedUnitFilter getSpeedUnitFilter() {
        return speedUnitFilter;
    }

    /**
     * Returns the filter that restricts allowed surface type values.
     */
    public SurfaceTypeFilter getSurfaceTypeFilter() {
        return surfaceType;
    }

    /**
     * Returns the filter that restricts allowed mode values.
     */
    public ModeFilter getModeFilter() {
        return modeFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OvertureParserFilter that = (OvertureParserFilter) o;
        return Objects.equals(roadClassFilter, that.roadClassFilter)
                && Objects.equals(destinationLabelTypeFilter, that.destinationLabelTypeFilter)
                && Objects.equals(destinationSymbolFilter, that.destinationSymbolFilter)
                && Objects.equals(accessTypeFilter, that.accessTypeFilter)
                && Objects.equals(comparisonOperatorFilter, that.comparisonOperatorFilter)
                && Objects.equals(dimensionRestrictionFilter, that.dimensionRestrictionFilter)
                && Objects.equals(featureTypeFilter, that.featureTypeFilter)
                && Objects.equals(recognizedStatusFilter, that.recognizedStatusFilter)
                && Objects.equals(roadSubclassFilter, that.roadSubclassFilter)
                && Objects.equals(sideFilter, that.sideFilter)
                && Objects.equals(themeFilter, that.themeFilter)
                && Objects.equals(travelHeadingFilter, that.travelHeadingFilter)
                && Objects.equals(travelModeFilter, that.travelModeFilter)
                && Objects.equals(travelReasonFilter, that.travelReasonFilter)
                && Objects.equals(unitsFilter, that.unitsFilter)
                && Objects.equals(variantFilter, that.variantFilter)
                && Objects.equals(speedUnitFilter, that.speedUnitFilter)
                && Objects.equals(surfaceType, that.surfaceType)
                && Objects.equals(modeFilter, that.modeFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                roadClassFilter,
                destinationLabelTypeFilter,
                destinationSymbolFilter,
                accessTypeFilter,
                comparisonOperatorFilter,
                dimensionRestrictionFilter,
                featureTypeFilter,
                recognizedStatusFilter,
                roadSubclassFilter,
                sideFilter,
                themeFilter,
                travelHeadingFilter,
                travelModeFilter,
                travelReasonFilter,
                unitsFilter,
                variantFilter,
                speedUnitFilter,
                surfaceType,
                modeFilter);
    }

    @Override
    public String toString() {
        return "OvertureParserFilter{" + "roadClassFilter="
                + roadClassFilter + ", destinationLabelTypeFilter="
                + destinationLabelTypeFilter + ", destinationSymbolFilter="
                + destinationSymbolFilter + ", accessTypeFilter="
                + accessTypeFilter + ", comparisonOperatorFilter="
                + comparisonOperatorFilter + ", dimensionRestrictionFilter="
                + dimensionRestrictionFilter + ", featureTypeFilter="
                + featureTypeFilter + ", recognizedStatusFilter="
                + recognizedStatusFilter + ", roadSubclassFilter="
                + roadSubclassFilter + ", sideFilter="
                + sideFilter + ", themeFilter="
                + themeFilter + ", travelHeadingFilter="
                + travelHeadingFilter + ", travelModeFilter="
                + travelModeFilter + ", travelReasonFilter="
                + travelReasonFilter + ", unitsFilter="
                + unitsFilter + ", variantFilter="
                + variantFilter + ", speedUnitFilter="
                + speedUnitFilter + ", surfaceType="
                + surfaceType + ", modeFilter="
                + modeFilter + '}';
    }
}
