CREATE TABLE IF NOT EXISTS tiles (
            zoom_level integer,
            tile_column integer,
            tile_row integer,
            tile_data blob);
CREATE TABLE IF NOT EXISTS metadata
        (name text, value text);
CREATE UNIQUE INDEX IF NOT EXISTS name on metadata (name);
CREATE UNIQUE INDEX IF NOT EXISTS tile_index on tiles
        (zoom_level, tile_column, tile_row);

PRAGMA synchronous=0;
PRAGMA locking_mode=EXCLUSIVE;
PRAGMA journal_mode=DELETE;

insert or ignore into metadata(name,value) values ('name','topo');
insert or ignore into metadata(name,value) values ('type','baselayer');
insert or ignore into metadata(name,value) values ('version','2');
insert or ignore into metadata(name,value) values ('description','geoportal');
insert or ignore into metadata(name,value) values ('format','jpg');