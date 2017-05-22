#!/bin/bash

pid=`ps aux | grep sprinkler-1.0-SNAPSHOT.jar | grep -v grep | awk '{print $2}'`
kill -9 $pid