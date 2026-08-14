/**
 * Provides classes for reading and importing Overture Maps data into GraphHopper.
 * <p>The main responsibility of this package is to orchestrate the conversion
 * of Overture road segments into GraphHopper edges and nodes.</p>
 *
 * <h3>Key Sub-packages:</h3>
 * <ul>
 * <li><b>{@code road}</b> — Defines the core domain models, that representing the Overture Transportation schema.</li>
 * <li><b>{@code parser}</b> — Contains the low-level engine for handling Parquet and JSON
 * formats, including S3 streaming capabilities.</li>
 * <li><b>{@code parsers}</b> — A collection of specialized attribute parsers that map Overture
 * properties to GraphHopper's Encoded Values.</li>
 * <li><b>{@code aws}</b> — Infrastructure classes for direct interaction with AWS S3.</li>
 * </ul>
 *
 * <h3>Main Entry Point:</h3>
 * <ul>
 * <li>{@link com.graphhopper.reader.overture.OvertureReader} — The central orchestrator of the import process.
 * It detects the input format (Parquet/JSON) and coordinates the population of the {@link com.graphhopper.storage.BaseGraph}
 * by delegating attribute parsing to specialized classes.</li>
 * </ul>
 *
 * <h3>External Documentation:</h3>
 * <ul>
 * <li><a href="https://docs.overturemaps.org/schema/reference/transportation/segment/#schema">Overture Road Segment Schema</a> —
 * Primary source for input data structures.</li>
 * <li><a href="https://wiki.openstreetmap.org/wiki/Map_features#Highway">OSM Map Features</a> —
 * General reference for road attributes (access, surface, etc.) used by GraphHopper.</li>
 * </ul>
 */
package com.graphhopper.reader.overture;
