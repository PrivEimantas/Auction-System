#!/bin/sh

cd ./server
rmiregistry &
sleep 2
java Server
