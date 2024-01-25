#!/bin/bash

#TODO: use cnitdata instead of exdratest*

#Why I can use different 127.x.x.x IPs without having active interfaces? Here: https://networkengineering.stackexchange.com/a/44636

set -e

if [ $# -eq 0 ]
then

  nesWorker \
      --coordinatorPort=12346 \
      --localWorkerIp="127.0.0.2" \
      --physicalSources.type="CSVSource"  \
      --physicalSources.filePath=/opt/local/nebula-stream/transc_data_1.csv \
      --physicalSources.numberOfBuffersToProduce=900 \
      --physicalSources.sourceFrequency=45000 \
      --physicalSources.physicalSourceName=nes_worker1 \
      --physicalSources.logicalSourceName=Transc_stream \
      --physicalSources.skipHeader=true
else
    exec $@
fi


