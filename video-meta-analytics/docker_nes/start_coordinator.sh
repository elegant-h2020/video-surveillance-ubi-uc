#!/bin/bash

/opt/local/nebula-stream/nesCoordinator \
    --coordinatorIp=$coordinatorIp \
    --rpcPort=$coordinatorPort \
    --restIp=$restIp \
    --restPort=$restPort \
    --optimizer.distributedWindowChildThreshold=3 \
    --configPath=coordinator.yml \
> coordinator.log &
