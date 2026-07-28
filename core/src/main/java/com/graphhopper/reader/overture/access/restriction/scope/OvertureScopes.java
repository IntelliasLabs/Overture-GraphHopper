package com.graphhopper.reader.overture.access.restriction.scope;

import com.graphhopper.reader.overture.access.restriction.OvertureAccessRestriction;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for interpreting the Overture {@code when} scope.
 *
 * <p>Overture attaches every attribute to a list of scoped rules, where the scope may constrain the
 * direction of travel ({@code when.heading}), the time ({@code when.during}), the transport mode
 * ({@code when.mode}) and more. GraphHopper's edge model stores one value per encoded value per
 * direction, so the scope has to collapse at import time.
 *
 * <p>Only the directional axis is handled here, because that is the only one GraphHopper can
 * represent losslessly. {@code during}, {@code using}, {@code recognized} and {@code vehicle} are
 * parsed into the model but have no encoded-value counterpart yet.
 */
public final class OvertureScopes {

    private OvertureScopes() {}

    /**
     * A list of rules split by the direction they apply to. A rule with no heading appears in both.
     *
     * @param forward rules applying to the forward direction
     * @param backward rules applying to the backward direction
     */
    public record Directed<T>(List<T> forward, List<T> backward) {}

    /**
     * Splits scoped rules by the direction they constrain.
     *
     * <p>Replaces the identical forward/backward partition that was hand-rolled in each access
     * parser. A rule without a heading is unconstrained and therefore appears in both lists.
     *
     * @param rules the scoped rules; {@code null} or empty yields two empty lists
     * @param headingOf extracts the rule's heading, returning {@code null} when unconstrained
     * @return the rules that apply forward and backward
     */
    public static <T> Directed<T> byHeading(
            @Nullable List<T> rules, Function<T, TravelHeading> headingOf) {
        if (rules == null || rules.isEmpty()) return new Directed<>(List.of(), List.of());

        List<T> forward = new ArrayList<>(rules.size());
        List<T> backward = new ArrayList<>(rules.size());
        for (T rule : rules) {
            if (rule == null) continue;
            TravelHeading heading = headingOf.apply(rule);
            if (appliesTo(heading, TravelHeading.FORWARD)) forward.add(rule);
            if (appliesTo(heading, TravelHeading.BACKWARD)) backward.add(rule);
        }
        return new Directed<>(forward, backward);
    }

    /**
     * @param ruleHeading the heading a rule is scoped to, or {@code null} if unconstrained
     * @param direction the direction being evaluated
     * @return whether the rule applies to {@code direction}
     */
    public static boolean appliesTo(@Nullable TravelHeading ruleHeading, TravelHeading direction) {
        return ruleHeading == null || ruleHeading == direction;
    }

    /**
     * Reports whether a scope constrains nothing beyond the direction of travel.
     *
     * <p>GraphHopper stores one value per encoded value per direction, with no time or vehicle
     * dimension. A rule scoped by {@code during}, {@code mode}, {@code vehicle}, {@code using} or
     * {@code recognized} therefore cannot be represented and must not be folded into an
     * unconditional value — doing so would apply, say, a lorry-only or rush-hour-only speed limit to
     * every vehicle at all times.
     *
     * @param when the scope to inspect; {@code null} counts as unconstrained
     * @return whether the rule applies unconditionally, possibly restricted to one direction
     */
    public static boolean isDirectionOnly(@Nullable PropertyScopeContainer when) {
        if (when == null) return true;
        return !when.hasDuring()
                && !when.hasUsing()
                && !when.hasRecognized()
                && !when.hasMode()
                && !when.hasVehicle();
    }

    /**
     * Extracts the heading an access restriction is scoped to.
     *
     * <p>Deliberately a function here rather than a method on {@link OvertureAccessRestriction}: the
     * access-parser tests mock that class and stub only {@code hasWhen()} and {@code getWhen()}, so a
     * new method on it would be intercepted by the mock and silently return {@code null}, sending
     * every restriction to both directions.
     *
     * @param restriction the restriction to inspect
     * @return the scoped heading, or {@code null} if it applies to both directions
     */
    @Nullable public static TravelHeading headingOf(OvertureAccessRestriction restriction) {
        return restriction.hasWhen() ? restriction.getWhen().getHeading() : null;
    }
}
