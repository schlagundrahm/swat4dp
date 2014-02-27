#!/bin/sh
# schlag&rahm AG

## ANT installation directory
ANT_HOME=\cygwin\d\srdev\tools\apache-ant-1.8.2

## swat-dp-backup home
DP_OP_HOME=..\


export SSL_OPTS=-Djavax.net.ssl.trustStore=$DP_OP_HOME/certs/%DP_ENV%-cacerts -Djavax.net.ssl.trustStorePassword=changeit

start $ANT_HOME\bin\ant.bat $SSL_OPTS -f $DP_OP_HOME\build.xml securebackup -Ddp.hostname=szhm2531-ilo.global.szh.loc -Ddp.port=25005
#start $ANT_HOME\bin\ant.bat $SSL_OPTS -f $DP_OP_HOME\build.xml securebackup -Ddp.hostname=szhm2532-ilo.global.szh.loc -Ddp.port=25005
