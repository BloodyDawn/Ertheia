# !/bin/bash

while :;
do
	java -server -Dfile.encoding=UTF-8 -Xms128m -Xmx256m -cp ./libs/*:login.jar BootManager > log/stdout/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 10;
done