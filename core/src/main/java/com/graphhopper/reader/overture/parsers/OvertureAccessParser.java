package com.graphhopper.reader.overture.parsers;

import com.graphhopper.reader.overture.access.restriction.AccessType;
import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
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
        if (restrictions.isEmpty()) {
            return true;
        }

        // Get all modes to check (mode + parent modes)
        Set<String> modesToCheck = getModesWithParents(mode);

        boolean generalAccessDenied = false;

        // Check if any restriction denies access for the mode or its parents
        for (OvertureAccessRestriction restriction : restrictions) {
            if (!restriction.hasAccessType() || !restriction.hasWhen()) {
                continue;
            }

            if (restriction.getAccessType() == AccessType.DENIED) {
                List<TravelMode> restrictedModes = restriction.getWhen().getMode();
                if (!restrictedModes.isEmpty()) {
                    for (TravelMode travelMode : restrictedModes) {
                        String restrictedMode = travelMode.toString();
                        if (modesToCheck.contains(restrictedMode)) {
                            return false;
                        }
                    }
                } else {

                    // If no specific modes are listed, this is a general access restriction
                    boolean hasSpecificConditions =
                            restriction.getWhen().hasVehicle() || restriction.getWhen().hasRecognized();
                    if (!hasSpecificConditions) generalAccessDenied = true;
                }
            }
        }

        return !generalAccessDenied;
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
