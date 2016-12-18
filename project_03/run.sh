#!/bin/sh

cp="-cp .:./bin/:./libs/jgroups-3.6.6.Final.jar"
cl="ch.unine.ds.project3.murenap.CoinExchanger"

run="java $cp $cl"

exec $run >> logs/a.log &
exec $run >> logs/b.log &
exec $run >> logs/c.log &
