##########################################################################################
#                                  S w a t 4 D P o p s                                   #
#                                 ---------------------                                  #
#     Schlag&rahm WebSphere Administration Toolkit for IBM DataPower SOA appliances      #
#                                                                                        #
#                                    Operator Module                                     #
#                                                                                        #
# Author: pshah@schlagundrahm.ch                                                         #
# Copyright (c) 2009-2013 schlag&rahm AG                                                 #
# All rights reserved.                                                                   #
#                                                                                        #
# http://www.schlagundrahm.ch                                                            #
#                                                                                        #
##########################################################################################


1) Customize the dpbuddy configuration
======================================

You have to change the user ID in the dp.properties file (dp.username=xyz12345678).
Please note that you have to modify the URLs in case you add or change your DataPower devices.

IMPORTANT:
If you have different user IDs for your environments (e.g. DEV,TEST,QUAL,PROD) you have to change this property manually.


2) Add internal CA certificates
================================

In order to run WAMT based tasks (e.g. securebackup) you have to add the CA certificates to the cacerts keystore of your Java runtime.
First you have to locate the Java runtime that will be executed by default.
Opening a command window (WindowsKey-R - cmd) and typing "echo %JAVA_HOME%" might help.
If your Java runtime is located at C:\Program Files\Java\jre6 you have to go to C:\Program Files\Java\jre6\lib\security.  

Notes:
1) The default password of the cacerts keystore is "changeit".
2) You have to add the complete signer chain.
3) If you use separate certificate authorities per environment you have to add them all.

2.1 keytool commands
--------------------

$ keytool -importcert -trustcacerts -alias <you-name-it> -file <root-ca-cert-file> -keystore cacerts
$ keytool -importcert -trustcacerts -alias <you-name-it-intermediate> -file <subordinate-ca-cert-file> -keystore cacerts


