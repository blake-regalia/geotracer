/** native includes **/
var path = require('path');
var fs = require('fs');
var child_process = require('child_process');

/** third-party includes & setup **/
var express = require('express');
var body_parser = require('body-parser');
var multer = require('multer');
var pg = require('pg');

/** instances **/
var app = express();
var http_server = require('http').createServer(app);
var urlencoded = body_parser.urlencoded({extended:true});

/** user includes **/
// var ... = require(__dirname+'/...');


/** static fields **/
var N_PORT = 1551;
var P_SERVER_ROOT = path.join(__dirname,'/..');
var P_ROOT = path.join(P_SERVER_ROOT,'/..');

// path to data directory relative from root directory
var S_REQ_DATA = '/data';
var P_DATA_DIR = P_ROOT+'/data';
var P_TOOLS_DIR = P_DATA_DIR+'/tools';

// http interface
app.set('views', P_SERVER_ROOT+'/view');
app.set('view engine', 'jade');

// app index
app.get('/', function(req, res) {
	res.render('index', {
		socket_port: N_PORT,
	});
});

// :btle-rssi
f_app_ware(app, 'btle-rssi', 'Bluetooth Low Energy RSSI Visualization');
f_app_ware(app, 'map-wap', 'Wireless Access Point Mapper');
f_app_ware(app, 'gps-map', 'Wireless Access Point Mapper');
f_app_ware(app, 'play', 'Playground');


// database access
(function() {

	//
	var R_CONNECT = /^(?:(?:(?:(\w+):\/\/)?(\w+)@)?(\w+)\/)?(\w+)$/;

	//
	var h_connections = {};
	var h_defaults = {
		protocol: 'postgress',
		user: 'blake',
		host: 'localhost',
	};

	// executes query
	var query = function(y_client, s_query, res) {
		y_client.query(s_query, function(err, h_results) {

			// query failure
			if(err) return res.send({error:'query failed: '+err}) || res.end();

			// results!
			res.send(h_results);
			res.end();
		});
	};

	//
	app.post('/query', urlencoded, function(req, res) {

		// reference connection string
		var s_connect = req.body.connection;

		// connection alias exists
		if(h_connections[s_connect]) {

			// forward request to query handler
			query(h_connections[s_connect], req.body.query, res);
		}

		// connection has not been used
		else {

			// parse connection string
			var m_connect = R_CONNECT.exec(s_connect);
			if(!m_connect) return res.send({error:'invalid connection string: "'+s_connect+'"'}) || res.end();
			var s_connect_full = ''
					+(m_connect[1] || h_defaults.protocol)+'://'
					+(m_connect[2] || h_defaults.user)+'@'
					+(m_connect[3] || h_defaults.host)+'/'
					+m_connect[4];

			// attempt connect
			pg.connect(s_connect_full, function(err, y_client, f_release) {

				// connection failure
				if(err) return res.send({error:'database connection error / '+err}) || res.end();

				// connection success, store & key by alias/full
				h_connections[s_connect] = h_connections[s_connect_full] = y_client;

				// forward request to query handler
				query(y_client, req.body.query, res);
			});
		}
	});
})();


// static resource files
app.use(
	express.static(P_SERVER_ROOT+'/public')
);

// static resource files
app.use(S_REQ_DATA,
	express.static(P_DATA_DIR+'/output')
);

// allow for uploads
app.post('/upload',
	multer({
		dest: P_DATA_DIR+'/tmp',
		limits: {
			fieldSize: 10*1024*1024, // 10mb
		},
	}),
	function(req, res) {

		// assert query params
		var m_device = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/.exec(req.query.device);
		var m_file = /^(\d{4}\-\d{2}\-\d{2}_\d{2}\-\d{2}\-\d{2}_\d+)\.uploadable$/.exec(req.query.file);

		// query params are okay
		if(m_device && m_file) {

			// reference device and file param
			var s_device = m_device[0];
			var s_file = m_file[1]+'.bin';

			// reference data file
			var h_file = req.files.data;

			// upload exists
			if(h_file) {

				// prepare dest directory and path to dest file
				var p_dest_dir = P_DATA_DIR+'/input/'+s_device;
				var p_dest_file = p_dest_dir+'/'+s_file;

				// file already exists, pretend to accept it
				if(fs.existsSync(p_dest_file)) return res.send(h_file.size+'');

				// destination directory does not exist, create it
				if(!fs.existsSync(p_dest_dir)) fs.mkdirSync(p_dest_dir, 0755);

				// move temp file to destination
				return fs.rename(h_file.path, p_dest_file, function(err) {

					// success
					if(!err) {

						// send confirmation
						res.send(h_file.size+'');

						// kick off decoders
						child_process.execFile(P_TOOLS_DIR+'/process.sh',
							[s_device, s_file],
							{ cwd: P_TOOLS_DIR },
							function(err, stdout, stderr) {

								if(stderr) console.log(stderr);

								// append output to log
								fs.appendFile(P_TOOLS_DIR+'/log', stdout+(stderr? '\n'+stderr: ''), function() {

								});
							});
					}
					// failure
					else {
						console.log(err);

						// close connection
						res.end();
					}
				});
			}
		}

		// close connection
		res.end();
	});


function f_app_ware(app, si_key, s_title) {

	// add self to middleware
	app.get('/'+si_key, function(req, res) {

		// prepare files map
		var h_files = {};

		// set path to app's data
		var p_app_data = P_DATA_DIR+'/output/'+si_key;

		// data dir exists
		if(fs.existsSync(p_app_data)) {

			// read data dir
			var a_devices = fs.readdirSync(p_app_data);

			// for each directory within
			for(var i=a_devices.length-1; i>=0; i--) {

				// reference app type
				var si_device = a_devices[i];

				// reference dir path
				var p_device = p_app_data+'/'+si_device;

				// directory!
				if(fs.statSync(p_device).isDirectory()) {

					// examine all files in directory
					var a_files = fs.readdirSync(p_device).sort();
					var a_dir = [];
					for(var i=a_files.length-1; i>=0; i--) {
						var s_file = a_files[i];

						// push each one to device directory array
						a_dir.push({
							name: s_file,
							size: fs.statSync(p_device+'/'+s_file).size,
						});
					}

					// push to files hash
					h_files[si_device] = a_dir;
				}
			}
		}

		// render view
		res.render('general-app', {
			title: s_title,
			app_key: si_key,
			query: req.query,
			socket_port: N_PORT,
			data_url: S_REQ_DATA+'/'+si_key,
			data_files: h_files,
		});
	});
};


// http server port
http_server.listen(N_PORT, function() {
	console.log('listening on port '+N_PORT+'...');
});

