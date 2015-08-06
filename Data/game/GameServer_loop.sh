# !/bin/bash

# exit codes of GameServer:
#  0 normal shutdown
#  2 reboot attempt

err=1
until [ $err == 0 ];
do
	[ -d log/ ] || mkdir log/
	[ -d log/stdout/ ] || mkdir log/stdout/
	[ -f log/stdout/stdout.log ] && mv log/stdout/stdout.log "log/stdout/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
# For developers mostly (1. line gc logrotate, 2. line parameters for gc logging) :
#	[ -f log/gc.log ] && mv log/gc.log "log/gc/`date +%Y-%m-%d_%H-%M-%S`_gc.log"
#	-verbose:gc -Xloggc:log/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution
#	java -agentpath:./libyjpagent.so -server -Dfile.encoding=UTF-8 -Xms3g -Xmx6g -cp ./../lib/*:core.jar BootManager > log/stdout/stdout.log 2>&1
	java -server -Dfile.encoding=UTF-8 -Xmn256m -Xms2g -Xmx6g -cp ./libs/*:game.jar BootManager > log/stdout/stdout.log 2>&1

	err=$?
	sleep 10;
done