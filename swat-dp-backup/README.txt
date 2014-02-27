########################################################
#                    S w a t 4 D P                     #
#                   ---------------                    #
#     schlag&rahm WebSphere Administration Toolkit     #
#                                                      #
#                Backup/Restore Module                 #
#                                                      #
# Author: pshah@schlagundrahm.ch                       #
# Copyright (c) 2009-2013 schlag&rahm AG               #
# All rights reserved.                                 #
#                                                      #
# http://www.schlagundrahm.ch                          #
#                                                      #
########################################################


Add JAR to your local Maven repository:
--------------------------------------

$ mvn.bat install:install-file -DgroupId=ant-contrib -DartifactId=ant-contrib -Dpackaging=jar -Dversion=1.0b3 -Dfile=ant-contrib-1.0b3.jar -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=javax.transaction -DartifactId=jta -Dpackaging=jar -Dversion=1.1 -Dfile=jta-1.1.jar -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=com.ibm.datapower -DartifactId=WAMT -Dpackaging=jar -Dversion=1.6.1.0 -Dfile=WAMT-1.6.1.0.jar -Djavadoc=WAMT-1.6.1.0_doc.zip -DgeneratePom=true
$ mvn.bat install:install-file -DgroupId=com.sardak.antform -DartifactId=antform -Dpackaging=jar -Dversion=2.0 -Dfile=antform.jar -Djavadoc=antform-doc.zip -DgeneratePom=true


Notes:
1) you have to add the CA certificates to cacerts in your Eclipse Java runtime

$ keytool -importcert -trustcacerts -alias <you-name-it> -file <root-ca-cert-file> -keystore cacerts


If you change the dependencies you have to regenerate the maven ant files (goal ant:ant)