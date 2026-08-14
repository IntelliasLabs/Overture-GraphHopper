package com.graphhopper.reader.overture.road.segment;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestination;
import com.graphhopper.reader.overture.road.segment.rule.OvertureLevelRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureProhibitedTransition;
import com.graphhopper.reader.overture.road.segment.rule.OvertureSubclassRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureWidthRule;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the properties of an Overture Maps "segment" feature with the subtype "road".
 * <p>
 * This class corresponds to the {@code properties} object in the Overture schema, combining
 * common segment properties (like level and access restrictions) with road-specific properties
 * (like road class, surface, and speed limits).
 * </p>
 */
public class OvertureRoadProperties {
    /**
     * The segment subtype (road / rail /water).
     * This is included in properties so the subtype is a regular attribute
     * of the feature's metadata.
     */
    private final OvertureSegmentSubtype subtype;

    /**
     * List of connectors which this segment is physically connected to and their relative location.
     * Each connector is a possible routing decision point.
     */
    private final List<OvertureConnector> connectors;

    /**
     * The routes (e.g., highway systems, numbered routes) this segment belongs to.
     */
    private final List<OvertureRoute> routes;

    /**
     * Captures the kind of road and its position in the road network hierarchy (e.g., motorway, primary, residential).
     */
    private final OvertureRoadClass roadClass;

    /**
     * Describes objects that can be reached by following this segment, similar to information found on physical signposts.
     */
    private final List<OvertureDestination> destinations;

    /**
     * Rules preventing transitions from this segment to another segment (e.g., no right turn).
     */
    private final List<OvertureProhibitedTransition> prohibitedTransitions;

    /**
     * Physical surface of the road. May be specified as global values or scoped rules.
     */
    private final List<OvertureRoadSurface> surfaces;

    /**
     * Set of boolean attributes applicable to roads (e.g., is_bridge, is_tunnel).
     */
    private final List<OvertureRoadFlags> flags;

    /**
     * Rules governing speed limits on this road segment, including max/min speeds and variable speed corridors.
     */
    private final List<OvertureSpeedLimit> speedLimits;

    /**
     * Rules defining the edge-to-edge width of the road modeled by this segment, in meters.
     */
    private final List<OvertureWidthRule> widthRules;

    /**
     * Refines the expected usage of the segment (e.g., sidewalk, link, driveway). Must not overlap.
     */
    private final OvertureRoadSubclass subclass;

    /**
     * A set of subclass assignments scoped to specific ranges along the segment.
     */
    private final List<OvertureSubclassRule> subclassRules;

    /**
     * Rules governing access to this road segment (e.g., denied for trucks, allowed for pedestrians).
     */
    private final List<OvertureAccessRestriction> accessRestrictions;

    /**
     * The Z-order of the feature, where 0 is the visual ground level.
     * Used for stacking order (e.g., overpasses vs. underpasses).
     */
    private final int level;

    /**
     * Rules defining the Z-order (stacking order) applicable within specific scopes on the road segment.
     */
    private final List<OvertureLevelRule> levelRules;

    /**
     * The top-level Overture theme this feature belongs to (e.g., "transportation").
     */
    private final OvertureTheme theme;

    /**
     * The specific feature type within the theme (e.g., "segment").
     */
    private final OvertureFeatureType type;

    /**
     * Version number of the feature, incremented in each Overture release where the geometry or attributes changed.
     */
    private final int version;

    /**
     * Metadata about the sources used to derive specific properties of this feature (e.g., dataset, license).
     */
    private final List<OvertureSource> sources;

    /**
     * Container for the names associated with this feature (primary, common, etc.).
     */
    private final OvertureNames names;

    /**
     * Constructs road properties with a default {@link OvertureSegmentSubtype#ROAD} subtype.
     * <p>
     * This constructor provides backward compatibility for data parsers that do not
     * explicitly specify a segment subtype.
     * </p>
     */
    public OvertureRoadProperties(
            List<OvertureConnector> connectors,
            List<OvertureRoute> routes,
            OvertureRoadClass roadClass,
            List<OvertureDestination> destinations,
            List<OvertureProhibitedTransition> prohibitedTransitions,
            List<OvertureRoadSurface> surfaces,
            List<OvertureRoadFlags> flags,
            List<OvertureSpeedLimit> speedLimits,
            List<OvertureWidthRule> widthRules,
            OvertureRoadSubclass subclass,
            List<OvertureSubclassRule> subclassRules,
            List<OvertureAccessRestriction> accessRestrictions,
            int level,
            List<OvertureLevelRule> levelRules,
            OvertureTheme theme,
            OvertureFeatureType type,
            int version,
            List<OvertureSource> sources,
            OvertureNames names) {
        this(
                connectors,
                routes,
                roadClass,
                destinations,
                prohibitedTransitions,
                surfaces,
                flags,
                speedLimits,
                widthRules,
                subclass,
                subclassRules,
                accessRestrictions,
                level,
                levelRules,
                theme,
                type,
                version,
                sources,
                names,
                OvertureSegmentSubtype.ROAD);
    }

    /**
     * Extended constructor that allows explicitly setting the segment subtype.
     * Backwards compatible code may continue to use the original constructor; that
     * will default to {@link OvertureSegmentSubtype#ROAD}.
     */
    public OvertureRoadProperties(
            List<OvertureConnector> connectors,
            List<OvertureRoute> routes,
            OvertureRoadClass roadClass,
            List<OvertureDestination> destinations,
            List<OvertureProhibitedTransition> prohibitedTransitions,
            List<OvertureRoadSurface> surfaces,
            List<OvertureRoadFlags> flags,
            List<OvertureSpeedLimit> speedLimits,
            List<OvertureWidthRule> widthRules,
            OvertureRoadSubclass subclass,
            List<OvertureSubclassRule> subclassRules,
            List<OvertureAccessRestriction> accessRestrictions,
            int level,
            List<OvertureLevelRule> levelRules,
            OvertureTheme theme,
            OvertureFeatureType type,
            int version,
            List<OvertureSource> sources,
            OvertureNames names,
            OvertureSegmentSubtype subtype) {
        this.connectors = connectors;
        this.routes = routes;
        this.roadClass = roadClass;
        this.destinations = destinations;
        this.prohibitedTransitions = prohibitedTransitions;
        this.surfaces = surfaces;
        this.flags = flags;
        this.speedLimits = speedLimits;
        this.widthRules = widthRules;
        this.subclass = subclass;
        this.subclassRules = subclassRules;
        this.accessRestrictions = accessRestrictions;
        this.level = level;
        this.levelRules = levelRules;
        this.theme = theme;
        this.type = type;
        this.version = version;
        this.sources = sources;
        this.names = names;
        this.subtype = subtype == null ? OvertureSegmentSubtype.ROAD : subtype;
    }

    /**
     * Gets the list of connectors physically connected to this segment.
     *
     * @return a list of {@link OvertureConnector} objects.
     */
    public List<OvertureConnector> getConnectors() {
        return connectors;
    }

    /**
     * Gets the list of routes (highway systems, refs) this segment belongs to.
     *
     * @return a list of {@link OvertureRoute} objects.
     */
    public List<OvertureRoute> getRoutes() {
        return routes;
    }

    /**
     * Gets the classification of the road (e.g. motorway, primary).
     *
     * @return the {@link OvertureRoadClass}.
     */
    public OvertureRoadClass getRoadClass() {
        return roadClass;
    }

    /**
     * Gets the list of destination signs or observable writing reachable by following this segment.
     *
     * @return a list of {@link OvertureDestination} objects.
     */
    public List<OvertureDestination> getDestinations() {
        return destinations;
    }

    /**
     * Gets the rules preventing transitions from this segment to others.
     *
     * @return a list of {@link OvertureProhibitedTransition} objects.
     */
    public List<OvertureProhibitedTransition> getProhibitedTransitions() {
        return prohibitedTransitions;
    }

    /**
     * Gets the physical surface rules for the road.
     *
     * @return a list of {@link OvertureRoadSurface} objects.
     */
    public List<OvertureRoadSurface> getSurfaces() {
        return surfaces;
    }

    /**
     * Gets the boolean flags (attributes) for the road, such as whether it is a bridge or tunnel.
     *
     * @return a list of {@link OvertureRoadFlags} objects.
     */
    public List<OvertureRoadFlags> getFlags() {
        return flags;
    }

    /**
     * Gets the speed limit rules for this segment.
     *
     * @return a list of {@link OvertureSpeedLimit} objects.
     */
    public List<OvertureSpeedLimit> getSpeedLimits() {
        return speedLimits;
    }

    /**
     * Gets the rules defining the physical width of the road.
     *
     * @return a list of {@link OvertureWidthRule} objects.
     */
    public List<OvertureWidthRule> getWidthRules() {
        return widthRules;
    }

    /**
     * Gets the refined usage subclass of the segment (e.g. sidewalk, link).
     *
     * @return the {@link OvertureRoadSubclass}, or null if not defined.
     */
    public OvertureRoadSubclass getSubclass() {
        return subclass;
    }

    /**
     * Gets the subclass rules scoped to specific ranges along the segment.
     *
     * @return a list of {@link OvertureSubclassRule} objects.
     */
    public List<OvertureSubclassRule> getSubclassRules() {
        return subclassRules;
    }

    /**
     * Gets the access restriction rules (e.g., vehicle types allowed or denied).
     *
     * @return a list of {@link OvertureAccessRestriction} objects.
     */
    public List<OvertureAccessRestriction> getAccessRestrictions() {
        return accessRestrictions;
    }

    /**
     * Gets the global Z-order level of the feature.
     *
     * @return the level integer (default 0).
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the Z-order level rules scoped to specific ranges.
     *
     * @return a list of {@link OvertureLevelRule} objects.
     */
    public List<OvertureLevelRule> getLevelRules() {
        return levelRules;
    }

    /**
     * Gets the top-level Overture theme.
     *
     * @return the {@link OvertureTheme}.
     */
    public OvertureTheme getTheme() {
        return theme;
    }

    /**
     * Gets the specific feature type.
     *
     * @return the {@link OvertureFeatureType}.
     */
    public OvertureFeatureType getType() {
        return type;
    }

    /**
     * Gets the version number of the feature.
     *
     * @return the version integer.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Gets the metadata sources for the feature properties.
     *
     * @return a list of {@link OvertureSource} objects.
     */
    public List<OvertureSource> getSources() {
        return sources;
    }

    /**
     * Gets the names associated with the feature.
     *
     * @return the {@link OvertureNames} object.
     */
    public OvertureNames getNames() {
        return names;
    }

    /**
     * Gets the segment subtype stored inside the properties object.
     * @return the {@link OvertureSegmentSubtype}
     */
    public OvertureSegmentSubtype getSubtype() {
        return subtype;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OvertureRoadProperties that)) return false;
        return getLevel() == that.getLevel()
                && getVersion() == that.getVersion()
                && getSubtype() == that.getSubtype()
                && Objects.equals(getConnectors(), that.getConnectors())
                && Objects.equals(getRoutes(), that.getRoutes())
                && getRoadClass() == that.getRoadClass()
                && Objects.equals(getDestinations(), that.getDestinations())
                && Objects.equals(getProhibitedTransitions(), that.getProhibitedTransitions())
                && Objects.equals(getSurfaces(), that.getSurfaces())
                && Objects.equals(getFlags(), that.getFlags())
                && Objects.equals(getSpeedLimits(), that.getSpeedLimits())
                && Objects.equals(getWidthRules(), that.getWidthRules())
                && getSubclass() == that.getSubclass()
                && Objects.equals(getSubclassRules(), that.getSubclassRules())
                && Objects.equals(getAccessRestrictions(), that.getAccessRestrictions())
                && Objects.equals(getLevelRules(), that.getLevelRules())
                && getTheme() == that.getTheme()
                && getType() == that.getType()
                && Objects.equals(getSources(), that.getSources())
                && Objects.equals(getNames(), that.getNames());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getConnectors(),
                getRoutes(),
                getRoadClass(),
                getDestinations(),
                getProhibitedTransitions(),
                getSurfaces(),
                getFlags(),
                getSpeedLimits(),
                getWidthRules(),
                getSubclass(),
                getSubclassRules(),
                getAccessRestrictions(),
                getLevel(),
                getLevelRules(),
                getTheme(),
                getType(),
                getVersion(),
                getSubtype(),
                getSources(),
                getNames());
    }

    /**
     * Generates a string representation of the OvertureRoadProperties object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "OvertureRoadProperties{" + "connectors="
                + connectors + ", routes="
                + routes + ", roadClass="
                + roadClass + ", destinations="
                + destinations + ", prohibitedTransitions="
                + prohibitedTransitions + ", surfaces="
                + surfaces + ", flags="
                + flags + ", speedLimits="
                + speedLimits + ", widthRules="
                + widthRules + ", subclass="
                + subclass + ", subclassRules="
                + subclassRules + ", accessRestrictions="
                + accessRestrictions + ", level="
                + level + ", levelRules="
                + levelRules + ", theme="
                + theme + ", type="
                + type + ", version="
                + version + ", sources="
                + sources + ", subtype="
                + subtype + ", names="
                + names + '}';
    }
}
