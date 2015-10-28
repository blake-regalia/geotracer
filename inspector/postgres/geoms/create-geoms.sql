
-- array of points: 2d
create table t1(name text, traj geometry(point, 4326)[]);

-- multipoint: 2d
create table t2(name text, traj geometry(multipoint, 4326));

-- linestring: 2d
create table t3(name text, traj geometry(linestring, 4326));


-- array of points: 3dz
create table t1z(name text, traj geometry(pointZ, 4326)[]);

-- multipoint: 3dz
create table t2z(name text, traj geometry(multipointZ, 4326));

-- linestring: 3dz
create table t3z(name text, traj geometry(linestringZ, 4326));


-- array of points: 3dm
create table t1m(name text, traj geometry(pointM, 4326)[]);

-- multipoint: 3dm
create table t2m(name text, traj geometry(multipointM, 4326));

-- linestring: 3dm
create table t3m(name text, traj geometry(linestringM, 4326));