package com.graphhopper.reader.overture.road.segment.spliter;

import static com.graphhopper.reader.overture.road.segment.spliter.ListIfNotEmptyOtherNull.toListIfNotEmptyOtherNull;
import static com.graphhopper.reader.overture.road.segment.spliter.SegmentSplitterUtils.*;
import static java.lang.Math.abs;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.common.speed.OvertureSpeedLimit;
import com.graphhopper.reader.overture.names.OvertureNameRule;
import com.graphhopper.reader.overture.names.OvertureNames;
import com.graphhopper.reader.overture.road.flags.OvertureRoadFlags;
import com.graphhopper.reader.overture.road.segment.HasBetweenProperty;
import com.graphhopper.reader.overture.road.segment.OvertureConnector;
import com.graphhopper.reader.overture.road.segment.OvertureRoadProperties;
import com.graphhopper.reader.overture.road.segment.OvertureRoute;
import com.graphhopper.reader.overture.road.segment.OvertureSource;
import com.graphhopper.reader.overture.road.segment.destination.OvertureDestination;
import com.graphhopper.reader.overture.road.segment.rule.OvertureLevelRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureProhibitedTransition;
import com.graphhopper.reader.overture.road.segment.rule.OvertureSubclassRule;
import com.graphhopper.reader.overture.road.segment.rule.OvertureWidthRule;
import com.graphhopper.reader.overture.road.surface.OvertureRoadSurface;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Helping class to separate logic of processing subSegment from {@link com.graphhopper.reader.overture.road.segment.OvertureRoadSegment} in range
 */
class SubSegmentProcessor {

    private static final double EPS = 1e-9;

    /**
     * Method for processing extraction subLineString from the passed {@link LineString} between the start and end linearly-references
     * @param rootLineString the {@link LineString} from which extracts subLineString in range
     * @param startLr start linearly-referenced position
     * @param endLr end linearly-referenced position
     * @return the {@link LineString} in corresponding range
     */
    static LineString getSubLineString(LineString rootLineString,
                                       double startLr, double endLr) {
        if (rootLineString == null)
            return null;

        double length = SegmentSplitterUtils.calculateLength(rootLineString);
        double startLength = startLr * length;
        double endLength = endLr * length;

        double iterateLength = 0.0;
        List<Coordinate> subCoordinates = new LinkedList<>();
        for (int i = 1; i < rootLineString.getNumPoints(); i++) {
            Coordinate p1 = rootLineString.getCoordinateN(i - 1);
            Coordinate p2 = rootLineString.getCoordinateN(i);

            double segLen = haversineDistance(p1, p2);
            double nextLength = iterateLength + segLen;

            if (subCoordinates.isEmpty() && nextLength >= startLength - EPS && iterateLength <= startLength + EPS) {
                if (abs(iterateLength - startLength) < EPS)
                    addIfDistinct(subCoordinates, p1);
                else if (abs(nextLength - startLength) < EPS)
                    addIfDistinct(subCoordinates, p2);
                else
                    addIfDistinct(subCoordinates, findPointAtDistance(rootLineString, startLength).orElse(p2));
            }

            if (nextLength > startLength && nextLength < endLength) addIfDistinct(subCoordinates, p2);

            if (nextLength >= endLength - EPS) {
                if (abs(nextLength - endLength) < EPS) addIfDistinct(subCoordinates, p2);
                else
                    addIfDistinct(subCoordinates, findPointAtDistance(rootLineString, endLength).orElse(p2));
                break;
            }

            iterateLength = nextLength;
        }

        if (subCoordinates.size() < 2)
            return new LineString(new CoordinateArraySequence(0), rootLineString.getFactory());

        return new LineString(
                new CoordinateArraySequence(subCoordinates.toArray(Coordinate[]::new)),
                rootLineString.getFactory()
        );
    }

    /**
     * Method for processing extraction subProperties from the passed {@link OvertureRoadProperties} between the start and end linearly-references
     * @param rootOvertureRoadProperties the {@link OvertureRoadProperties} from which extracts subProperties in range
     * @param startLr start linearly-referenced position
     * @param endLr end linearly-referenced position
     * @return the {@link OvertureRoadProperties} in corresponding range
     */
    static OvertureRoadProperties getPropertiesBetween(OvertureRoadProperties rootOvertureRoadProperties,
                                                               double startLr, double endLr) {
        if (rootOvertureRoadProperties == null)
            return null;

        final Predicate<? super HasBetweenProperty> generalFilterBetweenProperty =
                p -> isNullOrInRange(p, startLr, endLr);

        final double recalcMultiplierToNewBetweenEnd = calculateMultiplier(startLr, endLr);

        final List<OvertureConnector> subConnectors = filterAndGetSubProperties(
                rootOvertureRoadProperties.getConnectors(),
                c -> c.getAt() >= startLr && c.getAt() <= endLr,
                c -> new OvertureConnector(
                        c.getConnectorId(),
                        recalculateAt(c.getAt(), startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureRoute> subRoutes = filterAndGetSubProperties(
                rootOvertureRoadProperties.getRoutes(), generalFilterBetweenProperty,
                r -> new OvertureRoute(
                        r.getName(), r.getNetwork(), r.getRef(),
                        r.getSymbol(), r.getWikidata(),
                        recalculateSubBetween(r, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureDestination> subDestinations = filterAndGetSubProperties(
                rootOvertureRoadProperties.getDestinations(),
                d ->
                        subConnectors.stream()
                                .anyMatch(c -> c.getConnectorId().equals(d.getFromConnectorId()) ||
                                        c.getConnectorId().equals(d.getToConnectorId())),
                Function.identity()
        );

        final List<OvertureProhibitedTransition> subProhibitedTransitions = filterAndGetSubProperties(
                rootOvertureRoadProperties.getProhibitedTransitions(), generalFilterBetweenProperty,
                pt -> new OvertureProhibitedTransition(
                        pt.getSequence(), pt.getFinalHeading(), pt.getWhen(),
                        recalculateSubBetween(pt, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureRoadSurface> subRoadSurfaces = filterAndGetSubProperties(
                rootOvertureRoadProperties.getSurfaces(), generalFilterBetweenProperty,
                rs -> new OvertureRoadSurface(
                        rs.getSurfaceType(),
                        recalculateSubBetween(rs, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureRoadFlags> subRoadFlags = filterAndGetSubProperties(
                rootOvertureRoadProperties.getFlags(), generalFilterBetweenProperty,
                rf -> new OvertureRoadFlags(
                        rf.isBridge(), rf.isTunnel(), rf.isUnderConstruction(),
                        rf.isAbandoned(), rf.isCovered(), rf.isIndoor(),
                        recalculateSubBetween(rf, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureSpeedLimit> subSpeedLimits = filterAndGetSubProperties(
                rootOvertureRoadProperties.getSpeedLimits(), generalFilterBetweenProperty,
                sl -> new OvertureSpeedLimit(
                        sl.getMaxSpeed(), sl.getMinSpeed(), sl.isMaxSpeedVariable(),
                        recalculateSubBetween(sl, startLr, endLr, recalcMultiplierToNewBetweenEnd),
                        sl.getWhen()
                )
        );

        final List<OvertureWidthRule> subWidthRule = filterAndGetSubProperties(
                rootOvertureRoadProperties.getWidthRules(), generalFilterBetweenProperty,
                wr -> new OvertureWidthRule(
                        wr.getValue(),
                        recalculateSubBetween(wr, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureSubclassRule> subclassRules = filterAndGetSubProperties(
                rootOvertureRoadProperties.getSubclassRules(), generalFilterBetweenProperty,
                scr -> new OvertureSubclassRule(
                        scr.getValue(),
                        recalculateSubBetween(scr, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureAccessRestriction> subAccessRestrictions = filterAndGetSubProperties(
                rootOvertureRoadProperties.getAccessRestrictions(), generalFilterBetweenProperty,
                ar -> new OvertureAccessRestriction(
                        ar.getAccessType(), ar.getWhen(),
                        recalculateSubBetween(ar, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureLevelRule> subLevelRules = filterAndGetSubProperties(
                rootOvertureRoadProperties.getLevelRules(), generalFilterBetweenProperty,
                sbl -> new OvertureLevelRule(
                        sbl.getValue(),
                        recalculateSubBetween(sbl, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        final List<OvertureSource> subSources = filterAndGetSubProperties(
                rootOvertureRoadProperties.getSources(), generalFilterBetweenProperty,
                s -> new OvertureSource(
                        s.getProperty(), s.getDataset(), s.getLicense(),
                        s.getRecordId(), s.getUpdateTime(), s.getConfidence(),
                        recalculateSubBetween(s, startLr, endLr, recalcMultiplierToNewBetweenEnd)
                )
        );

        OvertureNames rootOvertureNames = rootOvertureRoadProperties.getNames();
        final OvertureNames namesSubRules = rootOvertureNames == null ?
                null :
                new OvertureNames(
                        rootOvertureNames.getPrimary(), rootOvertureNames.getCommon(),
                filterAndGetSubProperties(
                        rootOvertureNames.getRules(), generalFilterBetweenProperty,
                        nr -> new OvertureNameRule(
                                nr.getVariant(), nr.getLanguage(), nr.getPerspectives(),
                                nr.getValue(),
                                recalculateSubBetween(nr, startLr, endLr, recalcMultiplierToNewBetweenEnd),
                                nr.getSide()
                        )
                )
        );

        return new OvertureRoadProperties(
                subConnectors, subRoutes,
                rootOvertureRoadProperties.getRoadClass(),
                subDestinations, subProhibitedTransitions,
                subRoadSurfaces, subRoadFlags,
                subSpeedLimits, subWidthRule,
                rootOvertureRoadProperties.getSubclass(),
                subclassRules, subAccessRestrictions,
                rootOvertureRoadProperties.getLevel(),
                subLevelRules,
                rootOvertureRoadProperties.getTheme(),
                rootOvertureRoadProperties.getType(),
                rootOvertureRoadProperties.getVersion(),
                subSources, namesSubRules,
                rootOvertureRoadProperties.getSubtype()
        );
    }

    private static double calculateMultiplier(double startLr, double endLr) {
        return 1.0 / (endLr - startLr);
    }

    private static <T extends HasBetweenProperty> boolean isNullOrInRange(T p, double startLr, double endLr) {
        if (p.getBetween() == null)
            return true;

        return p.getBetween().getStart() <= endLr &&
                p.getBetween().getEnd() >= startLr;
    }

    private static <T> List<T> filterAndGetSubProperties(List<T> elements,
                                                         Predicate<? super T> filterFunc,
                                                         Function<T, T> createSubPropFunc) {
        return elements.stream()
                .filter(filterFunc)
                .map(createSubPropFunc)
                .collect(toListIfNotEmptyOtherNull());
    }

    private static double recalculateAt(double at, double startLr, double endLr, double recalcMultiplierToNewBetweenEnd) {
        if (at == startLr)
            return 0.0;
        else if (at == endLr)
            return 1.0;

        return (at - startLr) * recalcMultiplierToNewBetweenEnd;
    }

    private static <T extends HasBetweenProperty> LinearlyReferencedRange recalculateSubBetween(T p, double startLr, double endLr,
                                                                                                double recalcMultiplierToNewBetweenEnd) {
        if (p.getBetween() == null)
            return null;

        double recalcStartLr = p.getBetween().getStart();
        double recalcEndLr = p.getBetween().getEnd();

        if (recalcStartLr < startLr && recalcEndLr > endLr)
            return new LinearlyReferencedRange(0.0, 1.0);

        if (recalcStartLr > startLr)
            recalcStartLr = (recalcStartLr - startLr) * recalcMultiplierToNewBetweenEnd;
        else if (recalcStartLr <= startLr)
            recalcStartLr = 0.0;

        if (recalcEndLr < endLr)
            recalcEndLr = (recalcEndLr - startLr) * recalcMultiplierToNewBetweenEnd;
        else if (recalcEndLr >= endLr)
            recalcEndLr = 1.0;

        return new LinearlyReferencedRange(recalcStartLr, recalcEndLr);
    }

    /**
     * Rounds the given coordinate to 7 decimal places and adds it to the list
     * only if it is distinct from the last element.
     *
     * @param list list of coordinates
     * @param c coordinate to be added to the list
     */
    private static void addIfDistinct(List<Coordinate> list, Coordinate c) {
        double roundedLat = Math.round(c.y * 1e7) / 1e7;
        double roundedLon = Math.round(c.x * 1e7) / 1e7;
        Coordinate rounded = new Coordinate(roundedLon, roundedLat);

        if (list.isEmpty()) {
            list.add(rounded);
        } else {
            Coordinate last = list.get(list.size() - 1);
            if (abs(last.x - rounded.x) > EPS || abs(last.y - rounded.y) > EPS) {
                list.add(rounded);
            }
        }
    }
}
