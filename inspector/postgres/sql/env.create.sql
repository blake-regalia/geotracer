
-- temperature
create table temperature(
	trace_id integer references trace(local_id),
	time timestamp(3) with time zone,
	location geometry(point, 4326),
	celsius double precision
);

-- unique constraint
create unique index temperature_id on temperature(trace_id, time);

-- mono-field indexes
create index temperature_gix on temperature using gist(location);



-- luminosity
create table illuminance(
	trace_id integer references trace(local_id),
	time timestamp(3) with time zone,
	location geometry(point, 4326),
	lux double precision
);

-- unique constraint
create unique index illuminance_id on illuminance(trace_id, time);

-- mono-field indexes
create index illuminance_gix on illuminance using gist(location);



-- pressue
create table pressure(
	trace_id integer references trace(local_id),
	time timestamp(3) with time zone,
	location geometry(point, 4326),
	hectopascal double precision
);

-- unique constraint
create unique index pressure_id on pressure(trace_id, time);

-- mono-field indexes
create index pressure_gix on pressure using gist(location);

