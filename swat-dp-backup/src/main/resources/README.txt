##########################################################################################
#                                  S w a t 4 D P o p s                                   #
#                                 ---------------------                                  #
#     Schlag&rahm WebSphere Administration Toolkit for IBM DataPower SOA appliances      #
#                                                                                        #
#                                 Backup/Restore Module                                  #
#                                                                                        #
# Author: pshah@schlagundrahm.ch                                                         #
# Copyright (c) 2009-2013 schlag&rahm AG                                                 #
# All rights reserved.                                                                   #
#                                                                                        #
# http://www.schlagundrahm.ch                                                            #
#                                                                                        #
##########################################################################################




1) Add internal CA certificates
================================

In order to run WAMT based tasks you have to add the CA certificates to the cacerts keystore of your Java runtime.
First you have to locate the Java runtime that will be executed by default.
Opening a command window (WindowsKey-R - cmd) and typing "echo %JAVA_HOME%" might help.
If your Java runtime is located at C:\Program Files\Java\jre6 you have to go to C:\Program Files\Java\jre6\lib\security.  

Notes:
1) The default password of the cacerts keystore is "changeit".
2) You have to add the complete signer chain.
3) If you use separate certificate authorities per environment you have to add them all.

1.1 keytool commands
--------------------

$ keytool -importcert -trustcacerts -alias <you-name-it> -file <root-ca-cert-file> -keystore cacerts
$ keytool -importcert -trustcacerts -alias <you-name-it-intermediate> -file <subordinate-ca-cert-file> -keystore cacerts


