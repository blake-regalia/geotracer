/**
* class 
**/
(function(__namespace) {

	/**
	* private static:
	**/
	var __class = 'Table';

	var __construct = function(h_opt) {
		
		/**
		* private:
		**/
		var j_root;
		var j_body;
		var a_fields = [];
		var z_data;

		// required
		z_data = h_opt.data;
		(function() {
			if(!z_data) return exports.error('unacceptable data format');
		})();

		/**
		* public operator() ();
		**/
		var operator = function() {
			// return .apply(this, arguments);
		};
		
		
		/**
		* public:
		**/

			operator[''] = function() {
				
			};


		// apply args
		(function() {

			// construct fields
			var g_fields = '';
			(function() {
				var z_fields = h_opt.fields;

				// simple table mapping
				if(Array.isArray(z_fields)) {
					for(var i=0; i<as_fields.length; i++) {
						var s_field = as_fields[i];
						a_fields.push({
							title: s_field,
							key: s_field,
						});
					}
				}
				// hash
				else if(typeof z_fields === 'object') {
					for(var s_key in z_fields) {
						a_fields.push({
							title: z_fields[s_key],
							key: s_key,
						});
					}
				}
				else {
					return exports.error((typeof z_fields)+' is not a supported fields arguments');
				}

				// build fields
				for(var i=0; i<a_fields.length; i++) {
					g_fields += ''
						+'<div>'
							+a_fields[i].title
						+'</div>';
				}
			})();

			// construct body from data
			var g_body = '';
			(function() {
				var f_each = h_opt.each;

				// object
				if('object' == typeof z_data) {

					// array
					if(z_data instanceof Array) {

						// data wants to be sorted
						if('function' == typeof h_opt.sort) {
							z_data.sort(h_opt.sort);
						}

						// iterate every row of data
						for(var i_row=0; i_row<z_data.length; i_row++) {
							var h_row = z_data[i_row];
							var s_row_attrs = ' data-id="'+i_row+'"';

							// each row has data needs
							if(f_each) {

								// get needs of row
								h_needs = f_each.apply({}, [h_row]);

								// needs data attrs
								if(h_needs.data) {
									var h_needs_data = h_needs.data;
									for(var s_attr in h_needs_data) {
										s_row_attrs += ' data-'+s_attr+'="'+h_needs_data[s_attr]+'"';
									}
								}
							}

							// start of new row
							g_body += '<div'+s_row_attrs+'>';

							// iterate each field
							for(var i_field=0; i_field<a_fields.length; i_field++) {
								var h_field = a_fields[i_field];

								// prepare to store value of cell
								var z_value;

								// field refers to simple key of row
								if(h_field.key) {
									z_value = h_row[h_field.key];
								}
								// field uses get modifier
								else if(h_field.get) {
									var z_get = h_field.get;

									// depending on type of get argument
									switch(typeof z_get) {

										// function
										case 'function':
											z_value = z_get.apply({},[h_row]);
											break;

										// otherwise
										default:
											return exports.error((typeof z_get)+' is not a supported param value for the "get" option in data argument');
									}
								}

								// cast return type to string
								var s_value = '';
								if('object' == typeof z_value) {
									if(z_value instanceof Array) {
										s_value = z_value.length;
									}
									else {
										s_value = JSON.stringify(z_value);
									}
								}
								else {
									s_value = z_value+'';
								}

								// wrap cell value
								g_body += ''
									+'<div>'
										+s_value
									+'</div>';
							}

							// end of row
							g_body += '</div>';
						}
					}
				}
			})();

			// build primary element
			var g_root = ''
				+'<div class="'+h_opt.class+'">'
					+'<div class="table">'
						+'<div class="header">'
							+'<div>'
								+g_fields
							+'</div>'
						+'</div>'
						+'<div class="body">'
							+g_body
						+'</div>'
					+'</div>'
				+'</div>';

			// append it to the beginning of the document body
			j_root = $(g_root).prependTo(document.body);

			// set other private jquery/dom fields
			j_body = j_root.find('.body');

			// bind events
			(function() {
				if(h_opt.select) {
					var f_select = h_opt.select;
					j_body.on('click', '[data-id]', function() {
						var i_row = $(this).attr('data-id');
						f_select && f_select.apply({}, [z_data[i_row]]);
					});
				}
			})();

		})();
		
		
		return operator;	
	};


	/**
	* public static operator() ()
	**/
	var exports = __namespace[__class] = function() {
		if(this !== __namespace) {
			instance = __construct.apply(this, arguments);
			return instance;
		}
		else {
			if(arguments.length) {
				
			}
			return instance;
		}
	};

	
	/**
	* public static:
	**/
		
		//
		exports['toString'] = function() {
			return __class+'()';
		};
		
	// wrap the public static declarations in an iiaf
	(function() {

		// output a message to the console prefixed with this class's tag
		var debug = function(channel) {
			return function() {
				var args = Array.prototype.slice.call(arguments);
				args.unshift(__class+':');
				console[channel].apply(console, args);
			};
		};
		
		// open the various output channels
		exports['log'] = debug('log');
		exports['info'] = debug('info');
		exports['warn'] = debug('warn');
		exports['error'] = debug('error');
	})();
		

})(window);