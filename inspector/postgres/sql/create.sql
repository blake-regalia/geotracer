
-- enable postgis
create extension postgis;

-- device info
create table device(
	id uuid primary key,
	info json
);


-- trace info
create table trace(
	local_id serial primary key,
	device_id uuid references device(id),
	filename text,
	began timestamp(3) with time zone,
	duration interval second(3),
	sensors text[]
);

-- uniquify 
create unique index trace_id on trace(device_id, filename);

