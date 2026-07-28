package com.graphhopper.reader.overture.road.segment.rule;

import static org.junit.jupiter.api.Assertions.*;

import com.graphhopper.reader.overture.LinearlyReferencedRange;
import com.graphhopper.reader.overture.access.restriction.PropertyScopeContainer;
import com.graphhopper.reader.overture.access.restriction.scope.containers.TravelHeading;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OvertureProhibitedTransitionTest {

    private List<OvertureTransitionSequenceItem> sequence;
    private TravelHeading heading;
    private PropertyScopeContainer when;
    private LinearlyReferencedRange between;

    @BeforeEach
    void setUp() {
        sequence = List.of(new OvertureTransitionSequenceItem("1", "1"));
        heading = TravelHeading.BACKWARD;
        when = new PropertyScopeContainer("10:00-12:00", TravelHeading.FORWARD);
        between = new LinearlyReferencedRange(0.1, 0.9);
    }

    @Test
    void testConstructorAndGetters() {
        OvertureProhibitedTransition transition =
                new OvertureProhibitedTransition(sequence, heading, when, between);

        assertAll(
                "Verify all fields are correctly assigned",
                () -> assertEquals(sequence, transition.getSequence()),
                () -> assertEquals(heading, transition.getFinalHeading()),
                () -> assertEquals(when, transition.getWhen()),
                () -> assertEquals(between, transition.getBetween()));
    }

    @Test
    void testEqualsAndHashCode() {
        OvertureProhibitedTransition t1 =
                new OvertureProhibitedTransition(sequence, heading, when, between);
        OvertureProhibitedTransition t2 =
                new OvertureProhibitedTransition(sequence, heading, when, between);

        assertAll(
                "Equality checks",
                () -> assertEquals(t1, t1, "Reflexive"),
                () -> assertEquals(t1, t2, "Symmetric"),
                () -> assertEquals(t1.hashCode(), t2.hashCode(), "HashCode consistency"),
                () -> assertNotEquals(null, t1, "Null check"),
                () -> assertNotEquals("not a transition", t1, "Type check"));
    }

    @Test
    void testInequalityAcrossAllFields() {
        OvertureProhibitedTransition base =
                new OvertureProhibitedTransition(sequence, heading, when, between);

        assertAll(
                "Field-by-field inequality",
                () -> assertNotEquals(
                        base, new OvertureProhibitedTransition(List.of(), heading, when, between)),
                () -> assertNotEquals(
                        base, new OvertureProhibitedTransition(sequence, TravelHeading.FORWARD, when, between)),
                () -> assertNotEquals(
                        base, new OvertureProhibitedTransition(sequence, heading, null, between)),
                () -> assertNotEquals(
                        base,
                        new OvertureProhibitedTransition(
                                sequence, heading, when, new LinearlyReferencedRange(0, 1))));
    }

    @Test
    void testToStringFormat() {
        OvertureProhibitedTransition transition =
                new OvertureProhibitedTransition(sequence, heading, when, between);
        String str = transition.toString();

        assertAll(
                "String content",
                () -> assertNotNull(str),
                () -> assertTrue(str.contains("OvertureProhibitedTransition")),
                () -> assertTrue(str.contains("finalHeading=" + heading)));
    }
}
