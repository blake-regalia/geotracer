/**
* class 
**/
(function(__namespace) {

	/**
	* private static:
	**/
	var __class = 'EventBrowser';

	// endpoint for fetching files
	var P_GET_FILES = P_DATA_SOURCE;


	// convert object class name to html class name
	var S_CLASSNAME = (function() {
		var r_upper = /[A-Z]/;
		var s_name = '';
		for(var i=0; i<__class.length; i++) {
			var s_char = __class[i];
			if(r_upper.test(s_char)) s_name += (s_name.length?'-':'')+s_char.toLowerCase();
			else s_name +=s_char
		}
		return s_name;
	})();


	var __construct = function(s_category, h_args) {
		
		/**
		* private:
		**/
		var j_root;
		var j_body;

		// construct body
		(function() {

			//
			var a_fields = [
				{
					title: 'device',
				},
				{
					title: 'created',
					filter: function() {

					},
				},
				{
					title: 'size',
				},
				{
					title: 'nickname',
					filter: function() {

					},
				},
			];

			//
			var r_created = /(\d{4}).(\d{2}).(\d{2}).(\d{2}).(\d{2}).(\d{2})/;

			//
			var f_create_row = function(h_row) {
				var s_device = h_row.device;
				var h_file = h_row.file;
				var m_dt = r_created.exec(h_file.name);
				var s_size = h_file.size+'b';
				var s_created = new Date(m_dt[1], (~~m_dt[2])-1, m_dt[3], m_dt[4], m_dt[5], m_dt[6]).format('yyyy mmm dd ddd / HH:mm:ss');
				return ''
					+'<div data-url="'+s_device+'/'+h_file.name+'">'
						+'<div>'+s_device+'</div>'
						+'<div>'+s_created+'</div>'
						+'<div>'+s_size+'</div>'
						+'<div>nickname</div>'
					+'</div>';
			};

			// construct fields
			var g_fields = '';
			for(var i=0; i<a_fields.length; i++) {
				var h_field = a_fields[i];
				g_fields += ''
					+'<div>'
						+h_field.title
					+'</div>';
			}

			// construct table
			var g_body = '';
			for(var si_device in H_DATA_FILES) {

				//
				var a_files = H_DATA_FILES[si_device];

				//
				for(var i=a_files.length-1; i>=0; i--) {
					g_body += f_create_row({
						device: si_device,
						file: a_files[i],
					});
				}
			}

			// create this primary element
			var g_root = ''
				+'<div class="'+S_CLASSNAME+'">'
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
			operator['select'] = function(f_okay) {
				j_body.children().click(function() {
					var p_data_url = $(this).attr('data-url');
					$.getJSON(P_GET_FILES+'/'+p_data_url, f_okay);
				});
			};

			operator[''] = function() {
				
			};


		// perform args
		(function() {
			for(var e in h_args) {
				if(operator[e]) operator[e].apply(operator, [h_args[e]]);
			}
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