

-- wireless access point info
create table wap(
	local_id serial primary key,
	bssid macaddr,
	ssid text,
	frequency smallint,
	security integer
);

-- unique constraint
create unique index wap_lookup on wap(bssid, ssid, security);

-- mono-field indexes
create index wap_bssid on wap(bssid);
create index wap_ssid on wap(ssid);
create index wap_frequency on wap(frequency);
create index wap_security on wap(security);

-- multi-field indexes
create index wap_id on wap(bssid, ssid);



-- received signal strength indicator values for wireless access points
create table wap_sample(
	wap_id integer references wap(local_id),
	trace_id integer references trace(local_id),
	scan_time timestamp(3) with time zone,
	scan_duration interval second(3),
	location geometry(point, 4326),
	rssi integer
);

-- unique constraint
create unique index wap_sample_id on wap_sample(wap_id, trace_id, scan_time);

-- mono-field indexes
create index wap_sample_wap_id on wap_sample(wap_id);
create index wap_sample_gix on wap_sample using gist(location);

-- cluster
cluster wap_sample using wap_sample_wap_id;
analyze wap_sample;