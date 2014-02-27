#!/bin/sh
# schlag&rahm AG

## ANT installation directory
ANT_HOME=\cygwin\d\srdev\tools\apache-ant-1.8.2

## swat-dp-backup home
DP_OP_HOME=..\


export SSL_OPTS=-Djavax.net.ssl.trustStore=$DP_OP_HOME/certs/vbs-cacerts -Djavax.net.ssl.trustStorePassword=changeit

start $ANT_HOME\bin\ant.bat $SSL_OPTS -f $DP_OP_HOME\build.xml securebackup -Ddp.hostname=apalg11sa.vpnvbs2.admin.ch -Ddp.port=5550
#start $ANT_HOME\bin\ant.bat $SSL_OPTS -f $DP_OP_HOME\build.xml securebackup -Ddp.hostname=apalg11sb.vpnvbs2.admin.ch -Ddp.port=5550
