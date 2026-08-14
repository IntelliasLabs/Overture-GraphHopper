package com.graphhopper.reader.overture.road.surface;

/**
 * Enumerates the road surface types used by the Overture road surface layer.
 */
public enum RoadSurfaceType {
    /** Unknown or unspecified surface type. */
    UNKNOWN,
    /** Generic paved surface of unspecified material. */
    PAVED,
    /** Generic unpaved surface of unspecified material. */
    UNPAVED,
    /** Gravel or crushed stone surface. */
    GRAVEL,
    /** Dirt or earthen surface. */
    DIRT,
    /** Paving stones or cobblestone-like surfacing. */
    PAVING_STONES,
    /** Metal surface, for example on some bridges. */
    METAL,
    /** Asphalt surface. */
    ASPHALT,
    /** Concrete surface. */
    CONCRETE;

    /**
     * Parses a {@link RoadSurfaceType} from a case-insensitive string.
     *
     * @param surface the string to parse, may be {@code null}
     * @return the matching {@link RoadSurfaceType}, or {@code null} if the input is {@code null}
     * or does not match any constant
     */
    public static RoadSurfaceType fromString(String surface) {
      if(surface == null)
          return null;
      try{
          return valueOf(surface.toUpperCase());
      }
      catch(IllegalArgumentException e){
          return null;
      }
    }

    /**
     * Returns the lower-case string representation of this surface type.
     *
     * @return the lower-case {@link #name()} of this surface type
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
