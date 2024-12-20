#!/bin/sh

cd ./server
rmiregistry &
sleep 1
java FrontEnd &
sleep 1
java Replica 1 &
sleep 1
java Replica 2 &
sleep 1
java Replica 3 &
