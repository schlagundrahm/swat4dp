DataPower Operator Module
#########################


1) Customize the dpbuddy configuration
======================================

You have to change the user ID in the dp.properties file (dp.username=exshp).
Please note that you have to modify the URLs in case you add or change your DataPower devices.

IMPORTANT:
If you have different user IDs for the TEST, INT and PROD environment you have to change this property manually.


2) Add CA certificates
======================

In order to run WAMT based tasks (e.g. securebackup) you have to add the CA certificates to the cacerts keystore of your Java runtime.
First you have to locate the Java runtime that will be executed by default.
Opening a command window (WindowsKey-R - cmd) and typing "echo %JAVA_HOME%" might help.
If your Java runtime is located at C:\Program Files\Java\jre6 you have to go to C:\Program Files\Java\jre6\lib\security.  

Note:
The default password of the cacerts keystore is "changeit".


$ keytool -importcert -trustcacerts -alias SwissDefence-KM-RootCA-01 -file SwissDefence-KM-RootCA-01.crt -keystore cacerts
$ keytool -importcert -trustcacerts -alias SwissDefence-KM-ESubCA-02 -file SwissDefence-KM-ESubCA-02.crt -keystore cacerts

