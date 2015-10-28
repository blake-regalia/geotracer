# Geotracer


## Android

### Requirements
Minimum SDK version: 18 (Android 4.3+)

### Installation
Push the [app-debug.apk file](android/build/app/build/outputs/apk) to your android device:
```shell
cd ./geotracer
adb install ./android/dist/geotracer.apk
```

**Warning:** at this time, the automatic upload feature is disabled on the distribution apk. You can either set the upload URL manually in the source code and compile the Android app yourself, or simply download the trace files onto your computer from the mobile device (stored under `/sdcard/geotracer-files/`).

## Decoder / Inspector

### Requirements
Must be run on a *nix machine (Linux / OS X / etc.).

Must have PostgreSQL 9.3 or higher installed.

Must have Node.js installed

### Installation
To setup the database and it's tables:
```shell
cd ./inspector/postgres
./setup
```
This can be used by the inspector's web app upload page

### Usage

The inspector's web app is for viewing data through a GUI; however it is incomplete at this time. You may try using it by starting the server:
```
cd ./inspector/app/src
node server.js
```
And then opening that URL with the app name as the path; for example:
 * [http://localhost:1551/map-wap](http://localhost:1551/map-wap)
 * [http://localhost:1551/btle-rssi](http://localhost:1551/btle-rssi)


To use the decoder manually:
```
Usage: android-geotracer-decoder(agd) [options] app
  app: can be one of the following
	btle-rssi
	map-wap
	map-env
  options:
    -a, --all-files
       process all files for given device
       Default: false
    -d, --device
       specifies the input device by prefix
    -f, --file
       specifies the input file
    -n, --nth-latest
       specifies the nth-latest input file for the given device
       Default: 1
    -o, --output
       set which output format to use
    -p, --path
       specifies path of input file including its parent directory
    -l, --stream-mode
       streams output to stdout
       Default: false
```

For example, to decode a certain trace file for a given device:
```shell
# in the data directory...
cd ./inspector/data

# extracts bluetooth-low-energy data from the trace file and outputs data in JSON format
./decode -d $DEVICE_DIR -f $TRACE_FILE btle-rssi -o json

# extracts wireless-acces-point data w/ location data and outputs data in SQL format
./decode -d $DEVICE_DIR -f $TRACE_FILE map-wap -o sql
```

