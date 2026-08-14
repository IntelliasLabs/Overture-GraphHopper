package com.graphhopper.reader.overture.road.segment.destination;

/**
 * Indicates what special symbol/icon is present on a signpost, visible as road marking or similar.
 */
public enum OvertureDestinationSymbol {
    /** Motorway or high-speed road symbol. */
    MOTORWAY,
    /** Airport or aviation facility icon. */
    AIRPORT,
    /** Medical facility or hospital symbol. */
    HOSPITAL,
    /**
     * Center of a locality, city center or downtown, from centre in raw OSM value.
     */
    CENTER,
    /** Industrial zone or business park symbol. */
    INDUSTRIAL,
    /** Public or private parking area icon. */
    PARKING,
    /** Bus station or public transit stop symbol. */
    BUS,
    /** Railway or train station icon. */
    TRAIN_STATION,
    /** Highway rest area or service stop. */
    REST_AREA,
    /** Ferry terminal or maritime transport symbol. */
    FERRY,
    /** Motorroad (expressway for motor vehicles only) icon. */
    MOTORROAD,
    /** General fuel or gas station symbol. */
    FUEL,
    /** Scenic viewpoint or tourist attraction icon. */
    VIEWPOINT,
    /** Specifically marked diesel fuel station. */
    FUEL_DIESEL,

    /**
     * 'food', 'restaurant' in OSM.
     */
    FOOD,
    /** Lodging facilities such as hotels, motels, or hostels. */
    LODGING,
    /** Information point or tourist center. */
    INFO,
    /** Campground or caravan site symbol. */
    CAMP_SITE,
    /** Major road interchange or junction icon. */
    INTERCHANGE,

    /** 'toilets' in OSM. */
    RESTROOMS;

    /**
     * Case-insensitive mapping from string to Enum.
     *
     * @param value the string value from the Overture data.
     * @return the corresponding Enum constant.
     * @throws IllegalArgumentException if the value is null or unknown.
     */
    public static OvertureDestinationSymbol fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
