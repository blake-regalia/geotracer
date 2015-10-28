
var y_map;

// doc ready
$(document).ready(function() {

	// setup map
	var d_map = $('<div id="map"></div>').prependTo(document.body).get(0);
	y_map = L.map(d_map, {
		layers: L.Tiles.OSM,
		center: H_PLACE.ELLISON,
		zoom: 17,
	});

});



var plot = function(z_go) {

	//
	var parse = function(a_rows) {

		// acquire icon function
		var h_method = this.acquire? this.acquire(a_rows): this.method;

		// how to handle each layer
		var h_how = {};

		// marker layer
		if(h_method.marker) {

			// ref icon handler
			var f_icon = h_method.marker;

			// construct how
			h_how = {
				pointToLayer: function(feature, latlng) {
					return L.marker(latlng, {
						icon: f_icon(h_row),
					});
				},
			};
		}

		// pairs
		else if(h_method.each_pair) {

		}

		// clear existing layers
		y_map.eachLayer(function(y_layer) {
			if(y_layer instanceof L.LayerGroup) y_map.removeLayer(y_layer);
		});

		// create layer group
		var y_dest = new L.LayerGroup();

		// store every vertex for bounding box calculation
		var a_ll = [];

		// iterate rows
		for(var i=0; i<a_rows.length; i++) {

			// ref row
			var h_row = a_rows[i];

			// parse geojson
			var h_geo = JSON.parse(h_row.location);

			// push vertex
			var a_crd = h_geo.coordinates;
			a_ll.push([a_crd[1], a_crd[0]]);

			// construct layer, add to group
			y_dest.addLayer(
				L.geoJson(h_geo, h_how)
			);
		}

		// add group to map
		y_map.addLayer(y_dest);

		// fit bounds
		y_map.fitBounds(a_ll);
	};

	// function given as arg
	if('function' == typeof z_go) {

		// return parse function
		return parse.bind({acquire: z_go});
	}

	// data given
	else {

		// default icon function: black tick
		return parse.apply({
			method: {
				marker: function(){
					return L.Icons.Tick('#00');
				},
			},
		}, [z_go]);
	}
};


// plot heat based on field name
var plot_heat = function(s_field) {

	// plot using acquire function
	return plot(function(a_rows) {

		// generate heat color gradient
		var e_heat =d3.scale.linear()
				.domain(d3.minmax(a_rows, s_field))
				.range(['blue','red']);

		// icon function
		return {
			marker: function(h_row) {
				return L.Icons.Tick(
					e_heat(h_row[s_field])
				);
			},
		};
	});
};






// // plot heat based on field name
// var plot_heat = function(s_field) {

// 	// plot using acquire function
// 	return plot(function(a_rows) {

// 		// generate heat color gradient
// 		var e_heat =d3.scale.linear()
// 				.domain(d3.minmax(a_rows, s_field))
// 				.range(['blue','red']);

// 		// icon function
// 		return function(h_row) {
// 			// console.log(h_row.)
// 			return L.Icons.Tick(
// 				e_heat(h_row[s_field])
// 			);
// 		};
// 	});
// };











