#!/bin/sh
pidlist=`ps -ef|grep uncode-mq|grep -v "grep"|awk '{print $2}'`
if [ "$pidlist" = "" ]
   then
       echo "no moke pid alive!"
else
  echo "moke id list :$pidlist"
  kill -9 $pidlist
  echo "kill $pidlist"
  echo "moke stop success"
fi
