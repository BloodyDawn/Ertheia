@echo off

title L2WT Game Server
:start
echo %DATE% %TIME% Game server is running > gameserver_is_running.tmp
echo.
REM set PATH="type here your path to java jdk/jre (including bin folder)"
REM Default parameters for a basic server.

REM Protector
REM set JAVA_OPTS=%JAVA_OPTS% -Xbootclasspath/p:../crypt/crypt.jar

REM Non Heap memory
set JAVA_OPTS=%JAVA_OPTS% -XX:PermSize=256m
set JAVA_OPTS=%JAVA_OPTS% -XX:MaxPermSize=512m

REM Set heap min/max to same size for consistent results
set JAVA_OPTS=%JAVA_OPTS% -Xms2g
set JAVA_OPTS=%JAVA_OPTS% -Xmx3g

REM Garbage collection/Performance Options
REM set JAVA_OPTS=%JAVA_OPTS% -Xnoclassgc
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+AggressiveOpts
REM set JAVA_OPTS=%JAVA_OPTS% -XX:TargetSurvivorRatio=90
REM set JAVA_OPTS=%JAVA_OPTS% -XX:SurvivorRatio=16
REM set JAVA_OPTS=%JAVA_OPTS% -XX:MaxTenuringThreshold=12
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+UseParNewGC
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+UseConcMarkSweepGC
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSIncrementalMode
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSIncrementalPacing
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+CMSParallelRemarkEnabled
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+UseCompressedStrings
REM set JAVA_OPTS=%JAVA_OPTS% -XX:-UseLoopPredicate

REM The important setting in 64-bits with the Sun JVM is -XX:+UseCompressedOops as it saves memory and improves performance
REM set JAVA_OPTS=%JAVA_OPTS% -XX:UseSSE=3
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+UseFastAccessorMethods

REM Logging
REM set JAVA_OPTS=%JAVA_OPTS% -verbose:gc
REM set JAVA_OPTS=%JAVA_OPTS% -verbose:class
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGCTimeStamps
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+PrintGC
REM set JAVA_OPTS=%JAVA_OPTS% -XX:+TraceClassUnloading
REM set JAVA_OPTS=%JAVA_OPTS% -Xloggc:gc.log

REM ----------- Set Class Paths and Calls setenv.bat -----------------
SET OLDCLASSPATH=%CLASSPATH%
REM ------------------------------------------------------------------
java -server -Dfile.encoding=UTF-8 %JAVA_OPTS% -cp ./libs/*;./libs/game.jar BootManager

REM ======== Optimize memory settings =======
REM Minimal size with geodata is 1.5G, w/o geo 1G
REM Make sure -Xmn value is always 1/4 the size of -Xms and -Xmx.
REM -Xms<size>	set initial Java heap size
REM -Xmx<size>	set maximum Java heap size
REM -Xmn<size>	Size of young generation
REM ===============================
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo GameServer: Administrator Restarting.
echo.
goto start
:error
echo.
echo GameServer: Server terminated abnormally.
echo.
:end
echo.
echo GameServer: Server terminated.
echo.
del gameserver_is_running.tmp
pause