#!/usr/bin/env node

var fs = require('fs');
var path = require('path');
var bess = require(__dirname+'/ext/bess/bess-parser.js');
var iterate_keyless = require(__dirname+'/../src/blurn.js').iterate.keyless;

var p_bess_dir = __dirname+'/bess';

try {
	fs.mkdirSync(__dirname+'/css');
} catch(e){}

// get all bess files
fs.readdir(p_bess_dir, function(err, a_files) {
	if(err) throw err;
	iterate_keyless(a_files, function(s_file, f_next) {

		// bess input file
		var p_src = p_bess_dir+'/'+s_file;
		var s_dst = path.basename(p_src, '.bess');

		// command-line args
		fs.readFile(p_src, {encoding:'utf-8'}, function(err, data) {
			var h_res = bess.parse(data);
			fs.writeFile(__dirname+'/css/'+s_dst+'.css', h_res.css, f_next);
		});
	});
});