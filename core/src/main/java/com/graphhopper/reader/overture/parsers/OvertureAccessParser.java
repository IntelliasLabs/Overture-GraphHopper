package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.OvertureScopes;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelMode;
import java.util.List;
import java.util.Set;

/**
 * Parser for Overture access restrictions that determines whether a given travel mode
 * is allowed based on a list of access restrictions.
 */
public final class OvertureAccessParser {

    private OvertureAccessParser() {}
    /**
     * Checks whether access is allowed for the specified mode based on the given restrictions.
     * <p>
     * The method follows a hierarchical mode model:
     * <ul>
     *   <li>foot</li>
     *   <li>vehicle
     *     <ul>
     *       <li>bicycle</li>
     *       <li>motor_vehicle
     *         <ul>
     *           <li>motorcycle</li>
     *           <li>car</li>
     *           <li>hgv</li>
     *           <li>hov</li>
     *           <li>bus</li>
     *           <li>emergency</li>
     *           <li>truck</li>
     *         </ul>
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * <p>
     * If no restrictions are provided, access is allowed by default. If a restriction explicitly
     * denies access for the mode or any of its parent modes, access is denied.
     *
     * @param restrictions the list of access restrictions to evaluate, may be {@code null} or empty
     * @param mode         the travel mode to check (e.g., "car", "bicycle", "foot")
     * @return {@code true} if access is allowed, {@code false} if explicitly denied
     */
    public static boolean isAccessAllowed(List<OvertureAccessRestriction> restrictions, String mode) {
        // Default: accessible if no restrictions
        if (restrictions == null || restrictions.isEmpty()) {
            return true;
        }

        // Get all modes to check (mode + parent modes)
        Set<String> modesToCheck = getModesWithParents(mode);

        boolean denied = false;
        boolean allowedForThisMode = false;

        for (OvertureAccessRestriction restriction : restrictions) {
            if (restriction == null || !restriction.hasAccessType()) {
                continue;
            }
            PropertyScopeContainer when = restriction.getWhen();

            switch (restriction.getAccessType()) {
                case DENIED -> {
                    if (appliesToMode(when, modesToCheck) || isGeneralClosure(when)) denied = true;
                }
                    // An allow naming this mode lifts a broader denial, so "denied for vehicles but
                    // allowed for bicycles" leaves bicycles routable. Evaluated independently of list
                    // order, because Overture does not define an ordering over restrictions.
                case ALLOWED, DESIGNATED -> {
                    if (appliesToMode(when, modesToCheck)) allowedForThisMode = true;
                }
            }
        }

        return !denied || allowedForThisMode;
    }

    /**
     * Checks whether the mode may traverse the segment in at least one direction.
     *
     * <p>{@link #isAccessAllowed} evaluates a list that has already been narrowed to one direction.
     * Callers holding the unsplit list must use this method instead: a denial scoped to a single
     * heading — the ordinary way Overture encodes a oneway street — would otherwise look like a
     * closure of the whole segment.
     *
     * @param restrictions the unsplit list of access restrictions, may be {@code null} or empty
     * @param mode the travel mode to check
     * @return {@code true} if the mode is allowed in either direction
     */
    public static boolean isAccessAllowedEitherDirection(
            List<OvertureAccessRestriction> restrictions, String mode) {
        if (restrictions == null || restrictions.isEmpty()) return true;

        OvertureScopes.Directed<OvertureAccessRestriction> byHeading =
                OvertureScopes.byHeading(restrictions, OvertureScopes::headingOf);
        return isAccessAllowed(byHeading.forward(), mode)
                || isAccessAllowed(byHeading.backward(), mode);
    }

    /**
     * @param when the restriction's scope, possibly {@code null}
     * @param modesToCheck the queried mode together with its parent modes
     * @return whether the restriction names the queried mode or one of its parents
     */
    private static boolean appliesToMode(PropertyScopeContainer when, Set<String> modesToCheck) {
        if (when == null || !when.hasMode()) return false;
        for (TravelMode travelMode : when.getMode()) {
            if (modesToCheck.contains(travelMode.toString())) return true;
        }
        return false;
    }

    /**
     * Reports whether a denial closes the road for everyone rather than for a particular mode.
     *
     * <p>A restriction with no {@code when} clause at all is the plain "no access" case — a gated
     * service road or private drive. It used to be skipped entirely, which left such roads open to
     * bicycles and pedestrians; car traffic escaped the bug only because {@code
     * OvertureRoadSegment.isAccessible} implemented the same rule separately and got it right.
     *
     * <p>A denial qualified by {@code vehicle} (a dimensional limit such as height) or {@code
     * recognized} (permit holders) is not a general closure: it bars particular vehicles, not all
     * traffic.
     *
     * @param when the restriction's scope, possibly {@code null}
     * @return whether the denial applies to all traffic
     */
    private static boolean isGeneralClosure(PropertyScopeContainer when) {
        if (when == null) return true;
        return !when.hasMode() && !when.hasVehicle() && !when.hasRecognized();
    }

    /**
     * Checks whether access is explicitly allowed or designated for the specified mode.
     * <p>
     * Unlike {@link #isAccessAllowed}, this method returns true ONLY if there is an explicit
     * ALLOWED or DESIGNATED restriction for the mode. It returns false if access is merely
     * not denied (i.e., allowed by default).
     * <p>
     * This is useful for determining if a road has explicit bike infrastructure (like a designated
     * bike lane on a service road), which may allow higher speeds than default.
     *
     * @param restrictions the list of access restrictions to evaluate, may be {@code null} or empty
     * @param mode         the travel mode to check (e.g., "car", "bicycle", "foot")
     * @return {@code true} if access is explicitly ALLOWED or DESIGNATED for this mode
     */
    public static boolean isAccessDesignatedOrAllowed(
            List<OvertureAccessRestriction> restrictions, String mode) {
        if (restrictions.isEmpty()) {
            return false;
        }

        Set<String> modesToCheck = getModesWithParents(mode);

        for (OvertureAccessRestriction restriction : restrictions) {
            if (restriction == null || !restriction.hasAccessType()) {
                continue;
            }

            AccessType accessType = restriction.getAccessType();
            if (accessType != AccessType.ALLOWED && accessType != AccessType.DESIGNATED) {
                continue;
            }

            // Check if this restriction applies to the mode
            if (restriction.hasWhen() && restriction.getWhen().hasMode()) {
                List<TravelMode> restrictedModes = restriction.getWhen().getMode();
                for (TravelMode travelMode : restrictedModes) {
                    if (modesToCheck.contains(travelMode.toString())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns a set containing the mode and all its parent modes in the hierarchy.
     *
     * @param mode the travel mode
     * @return set of the mode and its parent modes
     */
    private static Set<String> getModesWithParents(String mode) {
        return switch (mode) {
            case "foot" -> Set.of("foot");
            case "bicycle" -> Set.of("bicycle", "vehicle");
            case "motorcycle", "car", "hgv", "hov", "bus", "emergency", "truck" -> Set.of(
                    mode, "motor_vehicle", "vehicle");
            case "motor_vehicle" -> Set.of("motor_vehicle", "vehicle");
            case "vehicle" -> Set.of("vehicle");
            default -> Set.of(mode);
        };
    }
}
