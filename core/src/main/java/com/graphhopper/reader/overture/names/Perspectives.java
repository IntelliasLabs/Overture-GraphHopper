package com.graphhopper.reader.overture.names;

import java.util.Objects;
import java.util.Set;

/**
 * Represents the political perspectives associated with a name rule.
 * <p>
 * This class defines whether a specific name is accepted or disputed by certain entities
 * (typically countries). This is crucial for handling disputed borders and politically sensitive naming.
 */
public class Perspectives {
    private final Mode mode;
    private final Set<String> countries;

    /**
     * Creates a new Perspectives instance.
     *
     * @param mode      the mode indicating if the countries accept or dispute the name
     * @param countries the set of country codes holding this view
     */
    public Perspectives(Mode mode, Set<String> countries) {
        this.mode = mode;
        this.countries = countries;
    }

    /**
     * Gets the perspective mode.
     *
     * @return the mode (ACCEPTED_BY or DISPUTED_BY)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Gets the set of countries holding this perspective.
     *
     * @return a set of country codes
     */
    public Set<String> getCountries() {
        return countries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Perspectives that)) return false;
        return getMode() == that.getMode() && Objects.equals(getCountries(), that.getCountries());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMode(), getCountries());
    }

    /**
     * Returns a string representation of the Perspectives.
     *
     * @return a string describing the perspectives
     */
    @Override
    public String toString() {
        return "Perspectives{mode=" + mode + ", countries=" + countries + '}';
    }
}
