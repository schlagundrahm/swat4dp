##########################################################################################
#                                  S w a t 4 D P o p s                                   #
#                                 ---------------------                                  #
#     schlag&rahm WebSphere Administration Toolkit for IBM DataPower SOA appliances      #
#                                                                                        #
#                                    Operator Module                                     #
#                                                                                        #
#                                customized for VBS FUB                                  #
#                                                                                        #
# Author: pshah@schlagundrahm.ch                                                         #
# Copyright (c) 2009-2013 schlag&rahm AG                                                 #
# All rights reserved.                                                                   #
#                                                                                        #
# http://www.schlagundrahm.ch                                                            #
#                                                                                        #
##########################################################################################


1) Add Swiss Defense certificates
====================================

In order to run WAMT based tasks (e.g. securebackup) you have to add the CA certificates to the cacerts keystore of your Java runtime.
First you have to locate the Java runtime that will be executed by default.
Opening a command window (WindowsKey-R - cmd) and typing "echo %JAVA_HOME%" might help.
If your Java runtime is located at C:\Program Files\Java\jre6 you have to go to C:\Program Files\Java\jre6\lib\security.  

Note:
The default password of the cacerts keystore is "changeit".

$ keytool -importcert -trustcacerts -alias SwissDefenseRootCA -file SwissDefenseRootCA.crt -keystore cacerts
$ keytool -importcert -trustcacerts -alias foo -file foo.cacert.crt -keystore cacerts


