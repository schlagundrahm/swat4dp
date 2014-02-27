@ECHO OFF

set ANT_HOME=c:\scdev\tools\ant\apache-ant-1.8.4
set DP_OP_HOME=z:\RetoHirt\DataPowerOperator

set DP_ENV=prod
set SSL_OPTS=-Djavax.net.ssl.trustStore=%DP_OP_HOME%/certs/%DP_ENV%-cacerts -Djavax.net.ssl.trustStorePassword=changeit

start %ANT_HOME%\bin\ant.bat %SSL_OPTS% -f %DP_OP_HOME%\build.xml bku_securebackup
