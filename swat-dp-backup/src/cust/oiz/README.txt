##########################################################################################
#                                  S w a t 4 D P o p s                                   #
#                                 ---------------------                                  #
#     schlag&rahm WebSphere Administration Toolkit for IBM DataPower SOA appliances      #
#                                                                                        #
#                                    Operator Module                                     #
#                                                                                        #
#                            customized for Stadt Zuerich OIZ                            #
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

You have to change the user ID in the dp.properties file (dp.username=oizXYZ).
Please note that you have to modify the URLs in case you add or change your DataPower devices.


2) Add Stadt Zuerich CA certificates
====================================

In order to run WAMT based tasks (e.g. securebackup) you have to add the CA certificates to the cacerts keystore of your Java runtime.
First you have to locate the Java runtime that will be executed by default.
Opening a command window (WindowsKey-R - cmd) and typing "echo %JAVA_HOME%" might help.
If your Java runtime is located at C:\Program Files\Java\jre6 you have to go to C:\Program Files\Java\jre6\lib\security.  

Note:
The default password of the cacerts keystore is "changeit".

$ keytool -importcert -trustcacerts -alias StadtZuerichRootCA -file StadtZuerichRootCA.crt -keystore cacerts
$ keytool -importcert -trustcacerts -alias StadtZuerichDeviceCA -file StadtZuerichDeviceCA.cacert.crt -keystore cacerts


