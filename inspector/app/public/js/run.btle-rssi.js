var a_data;
$(document).ready(function() {

	var k_browser = new EventBrowser(SI_APP_KEY, {
		select: function(a_data) {
			f_app(a_data);
		},
	});

});

var T_MOVING_WINDOW = 2500;

var f_stat = function(a_data) {
	for(var i=0; i<a_data.length; i++) {
		var a_events = a_data[i].events;
		var prev_time = a_events[0].time;
		var c_avg = 0;
		for(j=1; j<a_events.length; j++) {
			c_avg += a_events[j].time - prev_time;
			prev_time = a_events[j].time;
		}
		console.log('Average for '+i+': '+(c_avg/(a_events.length-1)));
	}
}

var f_event_sort = function(h_a, h_b) {
	if(h_a.rssi < h_b.rssi) return -1;
	else if(h_a.rssi > h_b.rssi) return 1;
	return 0;
};
var f_reduce = function(a_data) {
	for(var j=0, l=a_data.length; j<l; j++) {
		var a_org = a_data[j].events;
		var a_set = [];

		var a_mw = [];
		for(var i=0, mi=a_org.length; i<mi; i++) {
			var h_evt = a_org[i];

			// remove any events older than 250 ms
			var t_cutoff = h_evt.time - T_MOVING_WINDOW;
			while(a_mw.length) {
				if(a_mw[0].time < t_cutoff) {
					a_mw.shift();
				}
				else {
					break;
				}
			}

			// while(a_mw.length > 5) {
			// 	a_mw.shift();
			// }

			// add this event to moving window
			a_mw.push(h_evt);
			a_sort = a_mw.slice(0);
			a_sort.sort(f_event_sort);

			// compute median of all events
			var n_median = a_sort[0];
			var n_mwe = a_sort.length;
			if(n_mwe % 2 == 1) {
				n_median = a_sort[(n_mwe-1)/2].rssi;
			}
			else {
			var n_half = n_mwe / 2;
				n_median = (a_sort[n_half-1].rssi+a_sort[n_half].rssi) / 2;
			}

			//
			a_set.push({
				time: h_evt.time,
				rssi: n_median,
			});
		}

		a_data[l+j] = {events: a_set};
	}
};


var f_event_time = function(h_event) {
	return h_event.time;
};

var f_event_rssi = function(h_event) {
	return h_event.rssi;
};


var f_app = function(a_data) {
	var j_vis = $('#vis').empty();
	var k_d3 = d3.select(j_vis.get(0));

	var WIDTH = j_vis.width();
	var HEIGHT = j_vis.height();
	var H_MARGIN = {
		top: 20,
		right: 20,
		bottom: 50,
		left: 50,
	};
	d_xrange = d3.scale.linear()
		.range([H_MARGIN.left, WIDTH-H_MARGIN.right])
		.domain([
			d3.min(a_data, function(h_device) {
				return d3.min(h_device.events, f_event_time);
			}),
			d3.max(a_data, function(h_device) {
				return d3.max(h_device.events, f_event_time);
			})
		]);

	d_yrange = d3.scale.linear()
		.range([HEIGHT-H_MARGIN.top, H_MARGIN.bottom])
		.domain([
			d3.min(a_data, function(h_device) {
				return d3.min(h_device.events, f_event_rssi);
			}),
			d3.max(a_data, function(h_device) {
				return d3.max(h_device.events, f_event_rssi);
			})
		]);

	d_xaxis = d3.svg.axis()
		.scale(d_xrange)
		.tickSize(500)
		.tickSubdivide(true);

	d_yaxis = d3.svg.axis()
		.scale(d_yrange)
		.tickSize(5)
		.orient('left')
		.tickSubdivide(true);

	k_d3.append('svg:g')
		.attr('class', 'x axis')
		.attr('transform', 'translate(0,'+(HEIGHT-H_MARGIN.bottom)+')')
		.call(d_xaxis);

	k_d3.append('svg:g')
		.attr('class', 'y axis')
		.attr('transform', 'translate('+(H_MARGIN.left)+',0)')
		.call(d_yaxis);

	var k_line = d3.svg.line()
		.x(function(d) {
			return d_xrange(d.time);
		})
		.y(function(d) {
			return d_yrange(d.rssi);
		})
		.interpolate('linear');

	var p = d3.scale.category10();
	for(var i=0, il=a_data.length; i<il; i++) {
		k_d3.append('svg:path')
			.attr('d', k_line(a_data[i].events))
			.attr('stroke', p(i))
			.attr('stroke-width', 1)
			.attr('stroke-opacity', (i<il/2)? 0.25: 1)
			.attr('fill', 'none');
	}
};