
// leaflet helper
if(L) {

	// deafult icon image path
	var iconPath = L.Icon.Default.imagePath = '/res/';

	// custom icons
	(function() {

		// size map
		var h_size = {
			'sml': '12',
			'med': '18',
			'lrg': '24',
		};

		// helper function
		var icon = function(s_name, a_dim, a_anchor) {
			if(!a_anchor) a_anchor = [Math.floor(a_dim[0]*0.5), Math.floor(a_dim[1]*0.5)];
			return (function(s_size) {
				n_size = (s_size && h_size[s_size]) || '24';
				var p_icon = iconPath+this.name+'-'+n_size;
				return new L.Icon({
					iconUrl: p_icon+'.png',
					iconRetinaUrl: p_icon+'@2x.png',
					iconSize: this.dim,
					iconAnchor: this.anchor,
					color: '#ff0',
				});
			}).bind({name: s_name, dim: a_dim, anchor: a_anchor});
		};

		// icon table
		L.Icons = {
			Cross: icon('cross', [11,11]),
			Tick: function(s_color) {
				return new L.DivIcon({
					className: 'Ldi-cross med',
					html: '<div style="color:'+s_color+';">&#735;</div>',
				});
			},
			Dot: function(s_color) {
				return new L.DivIcon({
					className: 'Ldi-dot med',
					html: '<div style="color:'+s_color+';">&#8226;</div>',
					popupAnchor: [0, -3],
				});
			},
		};
	})();

	// tile layers
	L.Tiles = {
		OSM: L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
			maxNativeZoom: 19,
			maxZoom: 25,
			detectRetina: true,
		}),
	};

	// convert path to wkt
	L.Path.include({
		to_wkt: function() {

			// single point
			if(this._latlng) {
				var h_crd = this._latlng;
				return 'POINT('+h_crd.lng+' '+h_crd.lat+')';
			}

			// multi-point path
			else if(this._latlngs) {
				var s_wkt = '', s_suffix = '', b_close = false;
				if(this instanceof L.Polygon) {
					s_wkt = 'POLYGON('; s_suffix = ')';
					b_close = true;
				}
				else if(this instanceof L.Polyline) s_wkt = 'POLYLINE';
				else s_wkt = 'MULTIPOINT';
				s_wkt += '(';
				var a_crds = this._latlngs, s_close = '';
				for(var i=0; i<a_crds.length; i++) {
					if(i) s_wkt += ',';
					var h_crd = a_crds[i];
					var s_tmp = h_crd.lng+' '+h_crd.lat;
					if(!i && b_close) s_close = s_tmp;
					s_wkt += s_tmp;
				}
				return s_wkt+(b_close? ','+s_close: '')+')'+s_suffix;
			}
		},
	});


	// label plugin
	if(L.Label) {

		// fix the hiding issue
		L.Path.include({
			bindLabel: function(content, options) {
				if(!this.label || this.label.options !== options) {
					this.label = new L.Label(options, this);
				}

				this.label.setContent(content);

				if(!options.noHide && !this._showLabelAdded) {
					this
						.on('mouseover', this._showLabel, this)
						.on('mousemove', this._moveLabel, this)
						.on('mouseout remove', this._hideLabel, this);

					if(L.Browser.touch) {
						this.on('click', this._showLabel, this);
					}
					this._showLabelAdded = true;
				}

				return this;
			},

			showLabel: function() {
				this.label.setLatLng(this._latlng);
				this._map.showLabel(this.label);
			},
		});
	}
}


// gps locations
var H_PLACE = {
	HOME: [34.424233, -119.668866],
	ELLISON: [34.415357, -119.845238],
};


// pg helper
if(Postgres) {

	// config
	Postgres.config({
		mode: 'global',
		alias: 'pg',
	});

	// declare connection to database
	pg.connect('/query', 'geotracer');

	// define relations
	pg.define('wap_sample.wap_id=wap.local_id');
	pg.define('wap_sample.trace_id=trace.local_id');
	pg.define('trace.device_id=device.id');

	// define selectors
	pg.selector('geojson', 'ST_AsGeoJSON');
	pg.selector('text', 'ST_AsText');

}


// d3 helper
if(d3) {

	// min and max same time
	d3.minmax = function(a_col, z_filter) {
		var x_l = Infinity, x_h = -Infinity;
		if('function' == typeof z_filter) {
			for(var i=a_col.length-1; i>=0; i--) {
				var x_val = z_filter(a_col[i]);
				if(x_val < x_l) x_l = x_val;
				if(x_val > x_h) x_h = x_val;
			}
		}
		else if('string' == typeof z_filter) {
			for(var i=a_col.length-1; i>=0; i--) {
				var x_val = a_col[i][z_filter];
				if(x_val < x_l) x_l = x_val;
				if(x_val > x_h) x_h = x_val;
			}
		}
		return [x_l, x_h];
	};


	d3.range_2d = function(a_data, f_each) {
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
}