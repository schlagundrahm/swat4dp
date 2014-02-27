########################################################
#                    DataPower SWAT                    #
#                   ----------------                   #
#     schlag&rahm WebSphere Administration Toolkit     #
#                                                      #
#                   Operator Module                    #
#                                                      #
# customized for Swisscard AECS AG                     #
#                                                      #
# Author: pshah@schlagundrahm.ch                       #
# Copyright (c) 2009-2012 schlag&rahm gmbh             #
# All rights reserved.                                 #
#                                                      #
# http://www.schlagundrahm.ch                          #
#                                                      #
########################################################


1) Customize the dpbuddy configuration
======================================

You have to change the user ID in the dp.properties file (dp.username=sce12345678).
Please note that you have to modify the URLs in case you add or change your DataPower devices.

IMPORTANT:
If you have different user IDs for the TST and PROD environment you have to change this property manually.


2) Add Swisscard CA certificates
================================

In order to run WAMT based tasks (e.g. securebackup) you have to add the CA certificates to the cacerts keystore of your Java runtime.
First you have to locate the Java runtime that will be executed by default.
Opening a command window (WindowsKey-R - cmd) and typing "echo %JAVA_HOME%" might help.
If your Java runtime is located at C:\Program Files\Java\jre6 you have to go to C:\Program Files\Java\jre6\lib\security.  

Note:
The default password of the cacerts keystore is "changeit".

2.1 TST environment
-------------------

$ keytool -importcert -trustcacerts -alias TestSwisscardRootCA -file TestSwisscardRootCA.crt -keystore cacerts
$ keytool -importcert -trustcacerts -alias TestSwisscardSubordinateSOAInternal -file TestSwisscardSubordinateSOAInternal.cacert.crt -keystore cacerts

2.2 PROD environment
--------------------

$ keytool -importcert -trustcacerts -alias SwisscardRootCA -file SwisscardRootCA.crt -keystore cacerts
$ keytool -importcert -trustcacerts -alias SwisscardSubordinateSOAInternal -file SwisscardSubordinateSOAInternal.cacert.crt -keystore cacerts


