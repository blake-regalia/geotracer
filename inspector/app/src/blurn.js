
// a toolbox for common data-handling operations by Blake Regalia

module.exports = (function() {
	
	var exports = function(){};

	// asynchronously iterates an indexed set in event-driven order & executes callback for each
	var iterate = exports['iterate'] = function(z_set, f_each, f_okay) {
		if(!z_set) return f_okay && f_okay();
		if(Array.isArray(z_set)) {
			var i=0, l=z_set.length || (f_okay && f_okay());
			var next = function() {
				if(i < l) {
					var k=i; i+=1;
					if(f_each.apply(f_each, [k, z_set[k], next, f_okay])) next();
				} else f_okay && f_okay();
			}; next();
		}
		else {
			var keys = Object.keys(z_set);
			var i=0, l=keys.length || (f_okay && f_okay());
			var next = function() {
				if(i < l) {
					var k=keys[i]; i+=1;
					if(f_each.apply(f_each, [k, z_set[k], next, f_okay])) next();
				} else f_okay && f_okay();
			}; next();
		}
	};

	// asynchronously iterates an indexed set in event-driven order & executes callback for each
	iterate['keyless'] = function(z_set, f_each, f_okay) {
		if(!z_set) return f_okay && f_okay();
		if(Array.isArray(z_set)) {
			var i=0, l=z_set.length || (f_okay && f_okay());
			var next = function() {
				if(i < l) {
					var k=i; i+=1;
					if(f_each.apply(f_each, [z_set[k], next, f_okay])) setImmediate(next);
				} else f_okay && f_okay();
			}; next();
		}
		else {
			var keys = Object.keys(z_set);
			var i=0, l=keys.length || (f_okay && f_okay());
			var next = function() {
				if(i < l) {
					var k=keys[i]; i+=1;
					if(f_each.apply(f_each, [z_set[k], next, f_okay])) setImmediate(next);
				} else f_okay && f_okay();
			}; next();
		}
	};

	iterate['count'] = function(h_how, f_each, f_okay) {
		var z_i = h_how.from;
		var z_high = h_how.to;
		var z_inc = h_how.by || 1;
		var next = function() {
			if(z_i <= z_high) {
				var k=z_i; z_i += z_inc;
				if(f_each.apply(f_each, [k, next])) next();
			} else f_okay && f_okay();
		}; next();
	};

	return exports;
})();