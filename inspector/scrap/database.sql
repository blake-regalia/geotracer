
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
	began timestamp(3) without time zone,
	duration interval second(3),
	sensors text[]
);

-- uniquify 
create unique index trace_id on trace(device_id, filename);


-- wireless access point info
create table wap(
	local_id serial primary key,
	bssid macaddr,
	ssid text,
	frequency smallint,
	security integer
);

-- mono-field indexes
create index wap_bssid on wap(bssid);
create index wap_ssid on wap(ssid);
create index wap_frequency on wap(frequency);
create index wap_security on wap(security);

-- multi-field indexes
create index wap_id on wap(bssid, ssid);
create unique index wap_lookup on wap(bssid, ssid, security);


-- received signal strength indicator values for wireless access points
create table wap_rssi(
	wap_id integer references wap(local_id),
	trace_id integer references trace(local_id),
	scan_time timestamp(3) without time zone,
	scan_duration interval second(3),
	location geometry(point, 4326)
);


-- insert into other(wap_id, device_id, other) select wap.local_id, device.local_id, 'yes' from wap, device where wap.name='test' and device.value='blake';