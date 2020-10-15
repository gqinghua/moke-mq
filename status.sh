#!/bin/sh
pidlist=`ps -ef|grep uncode-mq|grep -v "grep"|awk '{print $2}'`
if [ "$pidlist" = "" ]
   then
       echo "moke stop"
else
  echo "moke running, id :$pidlist"
fi
