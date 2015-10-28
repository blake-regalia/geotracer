<?php

// error_reporting(E_ALL);

$u_device = $_GET['device'];
$u_file = $_GET['file'];

// assertions
$clean = preg_match('/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/', $u_device)
	&& preg_match('/^\d{4}.\d{2}.\d{2}.\d{2}.\d{2}.\d{2}.\d+\.uploadable$/', $u_file)
	&& $_FILES['data']['size'] < 10485760;

if(!$clean) die('');


$device = basename($u_device);
$file = basename($u_file, '.uploadable').'.bin';

$upload_dir = realpath(dirname(__FILE__).'/../data/input').'/'.$device;
$destination = $upload_dir.'/'.$file;

// file already exists, just pretend to accept it
if(file_exists($destination)) {
	echo filesize($_FILES['data']['tmp_name']);
}

// proceed
else {

	// directory needs to be created
	if(!file_exists($upload_dir)) {

		// mkdirs recursively
		if(!mkdir($upload_dir, 0777, true)) {
			die('Failed to mkdir("'.$upload_dir.'")');
		}
	}
	else if(!is_dir($upload_dir)) {
		die('Illegitimate upload directory');
	}

	// move to destination
	move_uploaded_file($_FILES['data']['tmp_name'], $destination) or die('Failed to upload file');

	// respond with filesize
	echo filesize($destination);

	// kick off decoders
	chdir(dirname(__FILE__));
	$exec_str = './process.sh '.escapeshellarg($device).' '.escapeshellarg($file)." >> ./log 2>&1 &";
	file_put_contents('attempt', $exec_str);
	exec($exec_str);
}

?>