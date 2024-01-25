#!/bin/sh

set -e

coordinatorCLIConf="${coordinatorCLIConf} --optimizer.distributedWindowChildThreshold=3"


if [ $# -eq 0 ]
then
    nesCoordinator $coordinatorCLIConf
else
    exec $@

fi
