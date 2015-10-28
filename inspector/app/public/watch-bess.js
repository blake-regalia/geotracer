#!/usr/bin/env node

var fs = require('fs');
var child_process = require('child_process');
var s_cmd = 'node '+__dirname+'/compile-bess.js';

var h_options = {
	persistent: true,
	interval: 500,
};

var f_update = function() {
	child_process.exec(s_cmd, function(err) {
		if(err) console.error(err);
	});
};

var p_dir = __dirname+'/bess';

fs.readdir(p_dir, function(err, a_files) {
	for(var i=a_files.length-1; i>=0; i--) {
		var s_file = a_files[i];
		fs.watchFile(p_dir+'/'+s_file, h_options, f_update);
	}
});