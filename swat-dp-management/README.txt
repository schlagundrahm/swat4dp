########################################################
#                    DataPower SWAT                    #
#                   ----------------                   #
#     schlag&rahm WebSphere Administration Toolkit     #
#                                                      #
# Author: pshah@schlagundrahm.ch                       #
# Copyright (c) 2009-2011 schlag&rahm gmbh             #
# All rights reserved.                                 #
#                                                      #
# http://www.schlagundrahm.ch                          #
#                                                      #
########################################################


Add JAR to you local Maven repository:
--------------------------------------

$ mvn.bat install:install-file -DgroupId=com.oopsconsultancy -DartifactId=xmltask -Dpackaging=jar -Dversion=1.16 -Dfile=xmltask-v1.16.jar -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=com.ibm.datapower -DartifactId=WAMT -Dpackaging=jar -Dversion=1.5.0.0 -Dfile=WAMT-1.5.0.0.jar -Djavadoc=WAMT-1.5.0.0_doc.zip -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=javax.transaction -DartifactId=jta -Dpackaging=jar -Dversion=1.0.1B -Dfile=jta-1.0.1B.jar -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=com.myarch -DartifactId=dpbuddy -Dpackaging=jar -Dversion=2.2.2 -Dfile=dpbuddy-2.2.jar -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=ant-contrib -DartifactId=ant-contrib -Dpackaging=jar -Dversion=1.0b3 -Dfile=ant-contrib-1.0b3.jar -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=com.sardak.antform -DartifactId=antform -Dpackaging=jar -Dversion=2.0 -Dfile=antform.jar -Djavadoc=antform-doc.zip -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=com.a7soft.examxml -DartifactId=JExamXML -Dpackaging=jar -Dversion=1.0.1 -Dfile=jexamxml.jar -Djavadoc=jexamxml-javadoc.zip -DgeneratePom=true

Notes:
1) you have to add the CA certificates (signing CA for the DataPower XML Manangement Interface SSL certificate) to cacerts in your Eclipse Java runtime

$ keytool -importcert -trustcacerts -alias SrsxRootCA -file srsx-root-ca.pem -keystore cacerts


If you change the dependencies you have to regenerate the maven ant files (goal ant:ant)

Additional JARs that have to be added to the ANT Global Entries:
----------------------------------------------------------------
For TrueZIP to work (use the same versions as defined in the pom.xml)
1) truezip-kernel-7.4.jar
2) truezip-file-7.4.jar
2) truezip-driver-file-7.4.jar
4) truezip-driver-zip-7.4.jar
5) commons-compress-1.3.jar


Adopt ANT build.properties:
---------------------------
- set the name of your user specific properties file:
	user.properties.file=settings/${user.name}.properties
- set the export directory:
	export.dir=exports



Adopt dp.properties for your DataPower environment:
---------------------------------------------------
- add your DataPower user name
	dp.username=${env.USERNAME}

- add domain and host information per environment
	<env-prefix>.dp.domain=<application-domain-name>
	dev.dp.xmlmgm.url.1=https://tstod0001.sctst.intra:8888
	dev.dp.xmlmgm.url.2=https://tstod0002.sctst.intra:8888
	

Eclipse shortcut properties:
----------------------------
C:\srdev\ide\eclipse\eclipse.exe -data "%WORKSPACES%\<your workspace dir>" -showlocation -nl en_US


Addon specific properties files in /src/main/resources:
-------------------------------------------------------
- WAMT.properties --> general WAMT settings
- WAMT.logging.properties --> Java Util Logger settings for WAMT


Addon specific files in the user's home directory:
--------------------------------------------------
- <user home>/WAMTRepository/WAMT.repository.xml --> WAMT device repository file (do not delete and do not check-in!)