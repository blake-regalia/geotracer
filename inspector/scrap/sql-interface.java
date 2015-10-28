

wap = Sql.core("wap")
	.field("bssid").value()
	.field("ssid").value()
	.field("frequency").value()
	.field("security").value();

wap.insert()

trace = Sql.core("trace")
	.field("device_uuid").value()
	.field("filename").value();

log.out(trace.insert(Sql.core()
		.field("began").value()
		.field("duration").value()
		.field("sensor").array(...)
	));


log.out(Sql.core("wap_rssi")
	.field("wap_id").using(wap, "local_id")
	.field("trace_id").using(trace, "local_id")
	.field("scan_time").value()
	.field("scan_duration").value()
	.field("location").geom("POINT(...)", 4326)
	.insert());




insert into "wap"
	("bssid", "ssid", "frequency", "security")
	values('','','','');

insert into "trace"
	("device_uuid", "filename")
	values('','');

insert into "wap_rssi"
	("wap_id","trace_id","scan_time","scan_duration","location")
	select
		"wap"."local_id",
		"trace"."local_id",
		'2015-01-20@12:11:55.32',
		'2.125',
		ST_GeomFromText('POINT(34.11732 -119.12234)', 4326)
	from "wap", "device"
	where "wap"."bssid"='...' and "wap"."ssid"='Wireless-Main' and "wap.frequency"='...' and "wap"."security"='...' and "device".