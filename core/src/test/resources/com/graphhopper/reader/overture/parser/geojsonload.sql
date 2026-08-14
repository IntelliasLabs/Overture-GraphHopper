INSTALL
spatial; --noqa
LOAD
spatial; --noqa
INSTALL
httpfs;
LOAD
httpfs;


-- Create a local GeoParquet file.
COPY
(
SELECT *
FROM
    read_parquet('s3://overturemaps-us-west-2/release/2025-12-17.0/theme=transportation/type=segment/*.parquet')
WHERE subtype = 'road' LIMIT 50 )
    TO 'extract.parquet';

-- Convert GeoParquet to line-delimited GeoJSON (or any other GDAL format)
COPY
(
SELECT id,
       geometry,
       subtype,
       CAST(bbox AS JSON)                   as bbox,
       CAST(connectors AS JSON)             as connectors,
       CAST(routes AS JSON)                 as routes,
       extract.class,
       CAST(destinations AS JSON)           as destinations,
       CAST(prohibited_transitions AS JSON) as prohibited_transitions,
       CAST(road_surface AS JSON)           as road_surface,
       CAST(road_flags AS JSON)             as road_flags,
       CAST(speed_limits AS JSON)           as speed_limits,
       CAST(width_rules AS JSON)            as width_rules,
       extract.subclass,
       CAST(subclass_rules AS JSON)         as subclass_rules,
       CAST(access_restrictions as JSON)    as access_restrictions,
       CAST(level_rules AS JSON)            as level_rules,
       extract.theme,
       type,
       extract.version,
       CAST(sources AS JSON)                as sources,
       CAST("names" AS JSON)                as "names"

FROM 'extract.parquet' )
    TO 'extract.geojson'
WITH (FORMAT GDAL, DRIVER 'GeoJSON', LAYER_CREATION_OPTIONS 'ID_FIELD=id');
