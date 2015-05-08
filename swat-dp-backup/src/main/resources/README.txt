##########################################################################################
#                               S w a t 4 D P b a c k u p                                #
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

In order to run WAMT based tasks you have to add the CA certificates to the trustsore.jks keystore within this project.

Notes:
1) The default password of the cacerts keystore is "changeit".
2) You have to add the complete signer chain.
3) If you use separate certificate authorities per environment you have to add them all.

1.1 keytool commands
--------------------

$ keytool -importcert -trustcacerts -alias <you-name-it> -file <root-ca-cert-file> -keystore cacerts
$ keytool -importcert -trustcacerts -alias <you-name-it-intermediate> -file <subordinate-ca-cert-file> -keystore cacerts


