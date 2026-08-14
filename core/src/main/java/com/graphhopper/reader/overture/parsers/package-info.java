/**
 * Contains specialized parsers for mapping Overture road attributes to GraphHopper's internal
 * representation (Encoded Values).
 * <p>The parsers in this package follow a modular design, where each class is responsible
 * for a specific set of properties from the Overture Transportation schema.</p>
 * <h3>Key Parser Categories:</h3>
 * <ul>
 * <li><b>Access & Movement:</b> Handles directional access and average speeds for various
 * vehicle types (Car, Bike, Foot, Bus, etc.).</li>
 * <li><b>Road Geometry & Classification:</b> Maps {@code road_class}, {@code road_subclass},
 * {@code surface}, etc. to standard GraphHopper tags.</li>
 * <li><b>Contextual Data:</b> Extracts street names and temporal restrictions.</li>
 * </ul>
 * <h3>Usage Overview:</h3>
 * <p>These parsers are called sequentially during the {@code readGraph} phase
 * of the {@link com.graphhopper.reader.overture.OvertureReader} to populate edge attributes.</p>
 * <h3>External Documentation:</h3>
 * <ul>
 * <li><a href="https://docs.overturemaps.org/schema/reference/transportation/segment/#schema">Overture Road Segment Schema</a> —
 * Primary source for input data structures.</li>
 * <li><a href="https://wiki.openstreetmap.org/wiki/Map_features#Highway">OSM Map Features</a> —
 * General reference for road attributes (access, surface, etc.) used by GraphHopper.</li>
 * </ul>
 */
package com.graphhopper.reader.overture.parsers;
