
var y_map;

// doc ready
$(document).ready(function() {

	// app
	f_app();

	// setup map
	var d_map = $('<div id="map"></div>').prependTo(document.body).get(0);
	y_map = L.map(d_map, {
		layers: L.Tiles.OSM,
		center: H_PLACE.ELLISON,
		zoom: 17,
	});

	// draw controls
	y_map.addControl(new L.Control.Draw({
		position: 'topright',
		draw: {
			circle: {
				shapeOptions: {
					fillOpacity: 0,
					color: 'blue',
					weight: 2,
				},
			}
		},
	}));

	// 
	y_map.on('draw:created', function(h_evt) {
		var y_layer = h_evt.layer;

		// we can query using this geometry
		if(y_layer.to_wkt) {

			// prepare value
			var s_test = pg.geom(y_layer.to_wkt());
			var f_geom;

			// circle
			if(y_layer instanceof L.Circle) {

				// create buffer
				f_geom = pg(
					pg.distance_spheroid(s_test, pg.WGS_84), '<', y_layer._mRadius
				);
			}
			// polygon
			else if(y_layer instanceof L.Polygon) {
				f_geom = pg.within(s_test);
			}
			else {
				return console.error('aborting query for '+h_evt.layerType);
			}

			//
			var e_heat = d3.scale.linear()
				.domain([-100, -20])
				.range(['blue','red']);

			// submit to what we want
			pg.from('wap_sample')
				.select('^.location::geojson')
				.where({
					location: f_geom,
					// scan_time: pg('>','2015-01-27 20:00:00'),
				}).exec(function(a_results) {

					// destination
					var y_dest = new L.LayerGroup();
					console.log(a_results);

					// iterate all rows
					for(var i=a_results.length-1; i>=0; i--) {
						var h_row = a_results[i];
						var h_geometry = JSON.parse(h_row.location);

						// convert feature to layer
						var y_feature = L.geoJson(h_geometry, {
							pointToLayer: function(feature, latlng) {
								return L.marker(latlng, {
									icon: L.Icons.Tick(
										e_heat(h_row.rssi)
									),
								});
							},
						});

						// add to destination
						y_dest.addLayer(y_feature);
					}

					// commit dest to map
					y_map.addLayer(y_dest);

				}).dump();
		}

		// keep this shape on the map
		y_layer.setStyle({
			opacity: 0.25,
		});
		y_map.addLayer(y_layer);
	});


	//
	// pg.from('wap_rssi')
	// 	.select('wap_rssi.*')
	// 	.where({
	// 		wap: {
	// 			ssid: 'Wireless',
	// 			location: pg.within(WKT_COUNTY_SANTA_BARBARA),
	// 		},
	// 	}).dump();

});



var plot = function(a_rows) {
	var a_ll = [];
	console.log(a_rows.length+' rows');
	for(var i=0; i<a_rows.length; i++) {
		var h_row = a_rows[i];
		var h_geo = JSON.parse(h_row.location);
		y_map.addLayer(L.geoJson(h_geo, {
			pointToLayer: function(feature, latlng) {
				a_ll.push(latlng);
				return L.marker(latlng, {
					icon: L.Icons.Tick('#000'),
				});
			},
		}));
	}
	y_map.fitBounds(a_ll);
};



var plot_rssi = function(a_rows) {

	//
	var e_heat =d3.scale.linear()
			.domain(true? d3.minmax(a_rows,'rssi'): [-100,-20])
			.range(['blue','red']);

	var a_ll = [];
	for(var i=0; i<a_rows.length; i++) {
		var h_row = a_rows[i];
		var h_geo = JSON.parse(h_row.location);
		y_map.addLayer(L.geoJson(h_geo, {
			pointToLayer: function(feature, latlng) {
				a_ll.push(latlng);
				return L.marker(latlng, {
					icon: L.Icons.Tick(e_heat(h_row.rssi)),
				});
			},
		}));
	}
	// y_map.fitBounds(a_ll);
};



var f_app = function() {

	// grab the top three sampled access points
	pg.from('wap')
		.join('wap_sample')
		.select('samples=#*')
		.group('.local_id')
		.order('samples',-1)
		.limit(4)
		.exec(function(a_res) {
			display_rssi({
				local_id: a_res[3].local_id,
			});
		});
};

var display_rssi = function(h_wap) {
	pg.from('wap_sample')
		.select('.location::geojson','.rssi')
		.where({
			wap: h_wap,
		})
		.exec(plot_rssi);
};












