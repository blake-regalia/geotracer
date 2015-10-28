
// escape html characters for text node
var text = function(s_str) {
	return s_str.replace('<', '&lt;').replace('>', '&gt;');
};

// security type lookup - least significant byte
var H_SECURITY_LSB = {
	0x00: 'OPEN',
	0x01: 'WEP',
	0x02: 'WPA PSK',
	0x03: 'WPA2 PSK',
	0x04: 'BOTH PSK',
	0x05: 'WPA EAP',
	0x06: 'WPA2 EAP',
	0x07: 'BOTH EAP',
};

//
var security_type = function(n_bitmask) {
	return H_SECURITY_LSB[n_bitmask & 0x0f];
};




// setup map
var d_map = $('<div id="map"></div>').appendTo(document.body).get(0);
y_map = L.map('map', {
	layers: [L.Tiles.OSM],
	center: H_PLACE.ELLISON,
	zoom: 17,
});


// action bar
var q_action_bar = $('<div id="action-bar" class="side-bar"></div>').appendTo(document.body);

// granular bar
var q_granular_bar = $('<div id="granular-bar" class="side-bar"></div>').appendTo(document.body);

// marker group
var y_marker_group = L.featureGroup().addTo(y_map);

// path group
var y_path_group = L.featureGroup().addTo(y_map);



// load wireless access points from database
var load_waps = function() {
	pg.from('wap')
		.select('count=#*')
		.group('.ssid')
		.order('count',-1)
		.results(function(a_rows) {
			//
			var g_rows = '';

			//
			a_rows.forEach(function(h_row) {
				g_rows += ''
					+'<div class="row" data-ssid="'+btoa(h_row.ssid)+'">'
						+'<div class="count">'+h_row.count+'</div>'
						+'<div>'
							+text(h_row.ssid)
						+'</div>'
					+'</div>';
			});

			//
			var g_table = '<div class="table"><div class="body">'+g_rows+'</div></div>';

			//
			$(g_table).appendTo(q_action_bar.empty())
				.find('.row')
					.click(function() {
						var s_ssid = atob($(this).attr('data-ssid'));
						location_samples_where({
							wap: {
								ssid: s_ssid,
							}
						});
					});
		});
};


// displays distinct locations of wap samples for given wap criteria
var location_samples_where = function(h_where) {
	pg.from('wap_sample')
		.select('^.location::geojson')
		.where(h_where)
		.results(function(a_rows) {

			//
			y_marker_group.clearLayers();
			y_path_group.clearLayers();

			//
			a_rows.forEach(function(h_row) {

				// parse geojson
				var h_feature = JSON.parse(h_row.location);

				// construct feature
				L.geoJson(h_feature, {
					pointToLayer: function(_f, y_latlng) {
						return L.marker(y_latlng, {
							icon: L.Icons.Tick('#000')
						});
					},
				})
					.addTo(y_marker_group);
			});

			//
			y_map.fitBounds(y_marker_group.getBounds(), {
				padding: [160, 160],
			});
		});

	//
	var g_rows = '';

	// 
	pg.from('wap_sample')
		.select('count=#*')
		.where(h_where)
		.group('wap.bssid','wap.frequency','wap.security','wap.local_id')
		.order('count', -1)
		.each(function(h_row) {
			g_rows += ''
				+'<div class="row" data-local-id="'+h_row.local_id+'">'
					+'<div class="count">'+text(h_row.count)+'</div>'
					+'<div class="bssid">'+text(h_row.bssid)+'</div>'
					+'<div class="frequency">'+(h_row.frequency/1000).toFixed(2)+'</div>'
					+'<div class="security">'+security_type(h_row.security)+'</div>'
				+'</div>';
		}, function() {
			var g_headers = ''
				+'<div class="title">'
					+JSON.stringify(h_where.wap)
				+'</div>';

			$(g_headers).appendTo(q_granular_bar.empty());
			$('<div class="table"><div class="body">'+g_rows+'</div></div>').appendTo(q_granular_bar)
				.find('.row')
					.click(function() {
						var s_local_id = $(this).attr('data-local-id');
						show_wap_samples({
							wap: {
								local_id: s_local_id,
							}
						});
					});
		});
};


// load and display all samples from single wap
var show_wap_samples = function(h_where) {
	pg.from('wap_sample')
		.select('.rssi','.location::geojson','.scan_time','.trace_id')
		.where(h_where)
		.order('trace_id')
		.order('scan_time')
		.results(function(a_rows) {

			//
			var e_heat =d3.scale.linear()
					.domain(true? d3.minmax(a_rows,'rssi'): [-100,-20])
					.range(['blue','red']);

			//
			var i_prev_id = false;
			var a_prev = false;
			var x_prev_rssi = false;

			//
			// y_marker_group.clearLayers();
			y_path_group.clearLayers();

			//
			a_rows.forEach(function(h_row) {

				// parse geojson
				var h_feature = JSON.parse(h_row.location);
				var y_xy = h_feature.coordinates;
				var a_ll = [y_xy[1], y_xy[0]];

				// polyline
				if(i_prev_id !== false) {

					// from same trace
					if(h_row.trace_id == i_prev_id) {
						L.polyline([a_prev, a_ll], {
							color: e_heat((x_prev_rssi+h_row.rssi) * 0.5),
						}).addTo(y_path_group);
					}
				}
				i_prev_id = h_row.trace_id;

				// prepare date-time string
				var s_datetime = new Date(h_row.scan_time).toLocaleString();

				// dot
				L.marker(a_ll, {
					icon: L.Icons.Dot(e_heat(h_row.rssi)),
				})
					.addTo(y_path_group)
					.bindPopup('Trace Id: #'+h_row.trace_id+'&nbsp;&nbsp;&nbsp; RSSI: '+h_row.rssi+'dBm<br />'+s_datetime)
					.on('mouseover', function(){
						y_observation = this;
					})
					.on('mouseout', function() {
						y_observation = false;
					});

				// previous point
				a_prev = a_ll;
				x_prev_rssi = h_row.rssi;
			});

			// reduce opacity of markers
			y_marker_group.getLayers().forEach(function(y_geojson_layer) {
				y_geojson_layer.eachLayer(function(y_layer) {
					y_layer.setOpacity(0.2);
				});
			});
		});
};

// popup scan info
var y_observation;
var v_observation_timer;
y_map.on('mousemove', function() {
	if(y_observation) {
		var y_opener = y_observation;
		y_observation.openPopup();
		clearTimeout(v_observation_timer);
		v_observation_timer = setTimeout(function() {
			if(y_opener) {
				y_opener.closePopup();
				y_observation = false;
			}
		}, 2250);
	}
});


// initialize
(function() {
	load_waps();
})();
