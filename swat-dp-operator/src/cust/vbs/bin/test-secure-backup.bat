@ECHO OFF

set ANT_HOME=c:\vbsdev\tools\ant\apache-ant-1.8.4
set DP_OP_HOME=c:\vbsdev\tools\DataPowerOperator

set DP_ENV=test
set SSL_OPTS=-Djavax.net.ssl.trustStore=%DP_OP_HOME%/certs/%DP_ENV%-cacerts -Djavax.net.ssl.trustStorePassword=changeit

start %ANT_HOME%\bin\ant.bat %SSL_OPTS% -f %DP_OP_HOME%\build.xml bku_securebackup -Dsecurebackup.hostname=apalg11sa.vpnvbs2.admin.ch
