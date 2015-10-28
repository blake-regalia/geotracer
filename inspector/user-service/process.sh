#!/bin/bash

# cd to data dir
cd $(pwd -P)/../data

# for sake of log
echo "$1/$2::"

# visualizers
./decode -d $1 -f $2 btle-rssi -o json
./decode -d $1 -f $2 map-wap -o sql