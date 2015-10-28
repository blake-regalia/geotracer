
// doc ready
$(document).ready(function() {

	// spawn event browser
	var k_browser = new EventBrowser(SI_APP_KEY, {
		select: function(h_data) {
			f_app(h_data);
		},
	});

	//
	pg.from('wap_sample')
		.select('wap_sample.*')
		.where({
			wap: {
				ssid: 'Wireless',
				// location: pg.within(WKT_COUNTY_SANTA_BARBARA),
			},
		}).results(function(a_rows) {
			console.log(a_rows);
		});

});

pg.geom([[-24,119]], 4326);


function f_app(h_data) {
	console.log(h_data);


	var h_device_groups = {};

	var a_gps = h_data.gps;
	var a_data = h_data.waps;

	new Table({
		data: a_data,
		class: 'data wap-infos',
		sort: function(h_a, h_b) {
			var s_a = h_a.security; var s_b = h_b.security;
			if(s_a == s_b) return 0;
			else if(s_a < s_b) return -1;
			else return 1;
		},
		select: function(h_row) {
			var x_rl = Infinity, x_rh = -Infinity;
			var a_points = [];
			var t_start = Infinity;
			var t_finish = -Infinity;

			y_map.fitBounds(
				get_2d_range(h_row.events, function(h_event) {
					if(h_event.rssi < x_rl) x_rl = h_event.rssi;
					if(h_event.rssi > x_rh) x_rh = h_event.rssi;
					if(h_event.time < t_start) t_start = h_event.time;
					if(h_event.time > t_finish) t_finish = h_event.time;
					var x_point = [h_event.latitude, h_event.longitude];
					a_points.push(x_point);
					return x_point;
				})
			);

			var e_heat = d3.scale.linear()
				.domain([x_rl, x_rh])
				.range(['blue','red']);

			var a_events = h_row.events;
			for(var i=a_events.length-1; i>=0; i--) {
				var h_event = a_events[i];
				var x_pct = Math.round(((h_event.time-t_start) / t_finish)*100);
				L.circle(a_points[i], 1, {
					color: e_heat(h_event.rssi),
				})
					// .bindLabel(x_pct+'%', {
					// 	noHide: true,
					// })
					.addTo(y_map)
					// .showLabel();
			}
		},
		fields: 'bssid/ssid/security/frequency/events'.split(/\//g),
	});

	var d_map = $('<div id="map"></div>').prependTo(document.body).get(0);

	var y_map = L.map(d_map, {
		layers: [
			L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
				maxNativeZoom: 19,
				maxZoom: 25,
			    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
			}),
		],
	});

	(function() {
		var l = a_gps.length-1;
		var e_time = d3.scale.linear()
			.domain([a_gps[0].time, a_gps[l].time])
			.range(['black','lightgreen']);
		for(var i=1; i<l; i++) {
			var h_a = a_gps[i-1], h_b = a_gps[i];
			var t_midpoint = (h_b.time + h_a.time) * 0.5;
			L.polyline([h_a.latlng, h_b.latlng], {
				color: e_time(t_midpoint),
				lineCap: 'butt',
				opacity: 0.8,
				weight: 1.5,
			}).addTo(y_map);
		}
	})();
};


function get_2d_range(a_data, f_each) {
	var x_al = Infinity, x_ah = -Infinity,
		x_bl = Infinity, x_bh = -Infinity;

	//
	if(a_data.length) {
		for(var i_row=a_data.length-1; i_row>=0; i_row--) {
			var a_ret = f_each.apply({}, [a_data[i_row]]);
			if(a_ret instanceof Array && a_ret.length == 2) {
				if(a_ret[0] instanceof Array) {
					do {
						var a_r0 = a_ret[0], a_r1 = a_ret[1];
						if(a_r0[0] < x_al) x_al = a_r0[0];
						if(a_r0[1] < x_bl) x_bl = a_r0[1];
						if(a_r1[0] > x_ah) x_ah = a_r1[0];
						if(a_r1[1] > x_bh) x_bh = a_r1[1];
					} while(i_row > 0 && (a_ret=f_each.apply({}, [a_data[--i_row]])));
				}
				else {
					do {
						var x_a = a_ret[0], x_b = a_ret[1];
						if(x_a < x_al) x_al = x_a;
						if(x_a > x_ah) x_ah = x_a;
						if(x_b < x_bl) x_bl = x_b;
						if(x_b > x_bh) x_bh = x_b;
					} while(i_row > 0 && (a_ret=f_each.apply({}, [a_data[--i_row]])));
				}
			}
		}
	}

	// 
	return [[x_al, x_bl], [x_ah, x_bh]];
};