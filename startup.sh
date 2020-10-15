#!/bin/sh
java -jar -Xms512m -Xmx1024m lib/uncode-mq-1.0.0.jar -cfg conf/config.properties > logs/umq.log &
pidlist=`ps -ef|grep uncode-mq|grep -v "grep"|awk '{print $2}'`
if [ "$pidlist" = "" ]
   then
       echo "moke start faile!"
else
  echo "moke id list :$pidlist"
  echo "moke start success"
fi
