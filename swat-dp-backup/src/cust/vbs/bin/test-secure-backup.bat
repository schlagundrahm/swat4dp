@ECHO OFF

set ANT_HOME=<path to the ant installation>
set DP_OP_HOME=<home directory of the Swat4DPops installation>

set DP_ENV=test
set SSL_OPTS=-Djavax.net.ssl.trustStore=%DP_OP_HOME%/certs/vbs-cacerts -Djavax.net.ssl.trustStorePassword=changeit

start %ANT_HOME%\bin\ant.bat %SSL_OPTS% -f %DP_OP_HOME%\build.xml securebackup -Dsecurebackup.hostname=apalg11sa.vpnvbs2.admin.ch -Dsecurebackup.port=5550
start %ANT_HOME%\bin\ant.bat %SSL_OPTS% -f %DP_OP_HOME%\build.xml securebackup -Dsecurebackup.hostname=apalg11sb.vpnvbs2.admin.ch -Dsecurebackup.port=5550
