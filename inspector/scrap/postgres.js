
pg.from('wap_rssi')
	.where({
		wap: {
			ssid: 'Wireless',
			security: [
				pg('&',7,'<>',0),
				pg('&',11,'<>',0)
			],
		},
	});

pg.from('wap_rssi')
	.where({
		wap_id: 176,
		value: pg('>',-125)
	})


select * from "wap_rssi" where
	exists(
		select 1 from "wap" inner join "wap_rssi"."wap_id" = "wap.local_id"
	)

select * from "wap_rssi"
	inner join "wap" on "wap_rssi"."wap_id" = "wap"."local_id"
		where ("wap"."ssid" = 'Wireless')
		and (
			(("wap"."security" & 7) <> 0) or
			(("wap"."security" & 11) <> 0)
		);


Psql.define('wap_rssi.wap_id=wap.local_id');

pg.from()

function(s_field) {
	return '('+s_field+' & 7) <> 0';
}


pg.from('wap')
	.distinct('ssid')
	.order('ssid');




select count(*) as count, wap.bssid, wap.ssid from wap_sample inner join wap on wap.local_id=wap_sample.wap_id group by wap.bssid, wap.ssid order by count desc limit 5;


pg.from('wap')
	.join('wap_sample')
	.select('samples=#*','.bssid')
	.group('.local_id')
	.order('samples', -1)

select count(*) as "samples", "wap.bssid"
	from "wap" inner join "wap_sample" on "wap"."local_id"="wap_sample"."wap_id" 
	group by "wap"."local_id"
	order by "samples" desc;