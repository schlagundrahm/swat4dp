<?xml version="1.0" encoding="UTF-8"?>
<!-- Licensed Materials - Property of schlag&rahm -->
<!-- Copyright (c) 2010, 2013 schlag&rahm AG, Switzerland. All rights reserved. -->
<!-- Licensed Materials - Property of IBM -->
<!-- Copyright IBM Corporation 2013. All Rights Reserved. -->
<!-- US Government Users Restricted Rights - Use, duplication or disclosure -->
<!-- restricted by GSA ADP Schedule Contract with IBM Corp. -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:env="http://www.w3.org/2003/05/soap-envelope" xmlns:dp="http://www.datapower.com/schemas/management"
    xmlns:str="http://exslt.org/strings" exclude-result-prefixes="xalan str env">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />

    <xsl:template match="/datapower-configuration">
        <xsl:copy>
            <xsl:attribute name="version">
                <xsl:value-of select="@version" />
            </xsl:attribute>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="configuration">
        <xsl:copy>
            <xsl:attribute name="domain">@domain.name@</xsl:attribute>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="processing-instruction()">
        <xsl:copy />
    </xsl:template>


    <!-- Service Templates -->
    <!-- ================= -->

    <!-- XMLFW Service -->
    <xsl:template match="XMLFirewallService">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*" />
            <xsl:for-each select="*">
                <xsl:choose>
                    <xsl:when test="local-name(.)='mAdminState'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.state')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='UserSummary'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.summary')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='LocalAddress'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('local.address')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='LocalPort'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('local.port')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='RemoteAddress'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backend.address')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='RemotePort'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backend.port')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='HTTPTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('http.timeout')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='HTTPPersistentTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('http.timeout.persistent')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='RequestType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('request.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='ResponseType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('response.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='XMLManager'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('xml.manager')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='Type'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backend.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLProfile'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('ssl.profile')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugMode'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.mode')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugHistory'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.history')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>


    <!-- MPGW Service -->
    <xsl:template match="MultiProtocolGateway">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*[name()!='name']" />
            <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
               <xsl:with-param name="name">
                  <xsl:value-of select="string('name')" />
               </xsl:with-param>
               <xsl:with-param name="key">
                  <xsl:value-of select="string('service.object.name')" />
               </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
            <xsl:for-each select="*">
                <xsl:choose>
                    <xsl:when test="local-name(.)='mAdminState'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.state')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='UserSummary'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.summary')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontProtocol'">
                        <xsl:call-template name="set-attribute-and-value-token">
                            <xsl:with-param name="attrname">
                                <xsl:value-of select="string('class')" />
                            </xsl:with-param>
                            <xsl:with-param name="attrkey">
                                <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.class')" />
                            </xsl:with-param>
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.name')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='BackendUrl'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backend.url')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('frontside.timeout')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontPersistentTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('frontside.timeout.persistent')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='BackTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backside.timeout')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='BackPersistentTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backside.timeout.persistent')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='RequestType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('request.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='ResponseType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('response.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='XMLManager'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('xml.manager')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='Type'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backend.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLProfile'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('ssl.profile')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugMode'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.mode')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugHistory'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.history')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>


    <!-- WebTokenService -->
    <xsl:template match="WebTokenService">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*[name()!='name']" />
            <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
               <xsl:with-param name="name">
                  <xsl:value-of select="string('name')" />
               </xsl:with-param>
               <xsl:with-param name="key">
                  <xsl:value-of select="string('service.object.name')" />
               </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
            <xsl:for-each select="*">
                <xsl:choose>
                    <xsl:when test="local-name(.)='mAdminState'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.state')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='UserSummary'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.summary')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontProtocol'">
                        <xsl:call-template name="set-attribute-and-value-token">
                            <xsl:with-param name="attrname">
                                <xsl:value-of select="string('class')" />
                            </xsl:with-param>
                            <xsl:with-param name="attrkey">
                                <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.class')" />
                            </xsl:with-param>
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.name')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontSide'">
                        <xsl:element name="{name()}">
                            <xsl:for-each select="*">
                                <xsl:choose>
                                    <xsl:when test="local-name(.)='LocalAddress'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('fsh.',count(../preceding-sibling::FrontSide)+1,'.host')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='LocalPort'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('fsh.',count(../preceding-sibling::FrontSide)+1,'.port')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='UseSSL'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('fsh.',count(../preceding-sibling::FrontSide)+1,'.usessl')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='SSLServer' and ./text() != ''">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('fsh.',count(../preceding-sibling::FrontSide)+1,'.sslserver')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='SSLSNIServer' and ./text() != ''">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('fsh.',count(../preceding-sibling::FrontSide)+1,'.sslsniserver')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('frontside.timeout')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontPersistentTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('frontside.timeout.persistent')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='RequestType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('request.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='XMLManager'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('xml.manager')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugMode'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.mode')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugHistory'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.history')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>


    <!-- WSP Service -->
    <xsl:template match="WSGateway">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*[name()!='name']" />
            <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
               <xsl:with-param name="name">
                  <xsl:value-of select="string('name')" />
               </xsl:with-param>
               <xsl:with-param name="key">
                  <xsl:value-of select="string('service.object.name')" />
               </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
            <xsl:for-each select="*">
                <xsl:choose>
                    <xsl:when test="local-name(.)='mAdminState'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.state')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='UserSummary'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('service.summary')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontProtocol'">
                        <xsl:call-template name="set-attribute-and-value-token">
                            <xsl:with-param name="attrname">
                                <xsl:value-of select="string('class')" />
                            </xsl:with-param>
                            <xsl:with-param name="attrkey">
                                <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.class')" />
                            </xsl:with-param>
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.name')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLClient'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('ssl.client.profile')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='WSDLCachePolicy'">
                        <xsl:element name="{name()}">
                            <xsl:for-each select="*">
                                <xsl:choose>
                                    <xsl:when test="local-name(.)='Match'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsdl.cache.',count(../preceding-sibling::WSDLCachePolicy)+1,'.map')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='TTL'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsdl.cache.',count(../preceding-sibling::WSDLCachePolicy)+1,'.ttl')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:when>
                    <xsl:when test="local-name(.)='BaseWSDL'">
                        <xsl:element name="{name()}">
                            <xsl:for-each select="*">
                                <xsl:choose>
                                    <xsl:when test="local-name(.)='WSDLName'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsdl.',count(../preceding-sibling::BaseWSDL)+1,'.name')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='WSDLSourceLocation'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsdl.',count(../preceding-sibling::BaseWSDL)+1,'.location')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='PolicyAttachments'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsdl.',count(../preceding-sibling::BaseWSDL)+1,'.policyattachments')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('frontside.timeout')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='FrontPersistentTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('frontside.timeout.persistent')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='BackTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backside.timeout')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='BackPersistentTimeout'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backside.timeout.persistent')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='RequestType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('request.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='ResponseType'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('response.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='XMLManager'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('xml.manager')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='Type'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('backend.type')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLProfile'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('ssl.profile')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugMode'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.mode')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='DebugHistory'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="string('debug.history')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>


    <!-- WSP Endpoint Rewrite Policy -->
    <xsl:template match="WSEndpointRewritePolicy">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*[name()!='name']" />
            <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
               <xsl:with-param name="name">
                  <xsl:value-of select="string('name')" />
               </xsl:with-param>
               <xsl:with-param name="key">
                  <xsl:value-of select="string('service.object.name')" />
               </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
            <xsl:for-each select="*">
                <xsl:choose>
                    <!--
                        <ServicePortMatchRegexp>^{http://www.tgic.de/gsg/umd-registration-insurance/1.0}InsuranceRegistrationSOAPServicePort$</ServicePortMatchRegexp>
                        <LocalEndpointProtocol>default</LocalEndpointProtocol>
                        <LocalEndpointHostname>0.0.0.0</LocalEndpointHostname>
                        <LocalEndpointPort>0</LocalEndpointPort>
                        <LocalEndpointURI>/umd/registration/insurance/InsuranceRegistrationSOAPService</LocalEndpointURI>
                        <FrontProtocol class="HTTPSourceProtocolHandler">UmdWebServiceEndpoint</FrontProtocol>
                        <UseFrontProtocol>on</UseFrontProtocol>
                        <WSDLBindingProtocol>soap-11</WSDLBindingProtocol>
                        <FrontsidePortSuffix/>
                    -->
                    <xsl:when test="local-name(.)='WSEndpointLocalRewriteRule'">
                        <xsl:element name="{name()}">
                            <xsl:for-each select="*">
                                <xsl:choose>
                                    <xsl:when test="local-name(.)='ServicePortMatchRegexp'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.serviceportregexp')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='LocalEndpointProtocol'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.protocol')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='LocalEndpointHostname'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.hostname')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='LocalEndpointPort'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.port')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='LocalEndpointURI'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.uri')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='FrontProtocol'">
                                        <xsl:call-template name="set-attribute-and-value-token">
                                            <xsl:with-param name="attrname">
                                                <xsl:value-of select="string('class')" />
                                            </xsl:with-param>
                                            <xsl:with-param name="attrkey">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.fsh.class')" />
                                            </xsl:with-param>
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.fsh.name')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='UseFrontProtocol'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.usefrontprotocol')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='WSDLBindingProtocol'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.binding')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='FrontsidePortSuffix'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.suffix')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='LocalEndpointHostname'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.local.',count(../preceding-sibling::WSEndpointLocalRewriteRule)+1,'.hostname')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:when>
                    <!--
                        <WSEndpointRemoteRewriteRule>
                        <ServicePortMatchRegexp>^{http://www.tgic.de/gsg/umd-registration-insurance/1.0}InsuranceRegistrationSOAPServicePort$</ServicePortMatchRegexp>
                        <RemoteEndpointProtocol>http</RemoteEndpointProtocol>
                        <RemoteEndpointHostname>umd-app-e-01.ham.gdv.org</RemoteEndpointHostname>
                        <RemoteEndpointPort>9080</RemoteEndpointPort>
                        <RemoteEndpointURI>/umd/registration/insurance/InsuranceRegistrationSOAPService</RemoteEndpointURI>
                        <RemoteMQQM/>
                        <RemoteTibcoEMS/>
                        <RemoteWebSphereJMS/>
                        </WSEndpointRemoteRewriteRule>
                    -->
                    <xsl:when test="local-name(.)='WSEndpointRemoteRewriteRule'">
                        <xsl:element name="{name()}">
                            <xsl:for-each select="*">
                                <xsl:choose>
                                    <xsl:when test="local-name(.)='ServicePortMatchRegexp'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.remote.',count(../preceding-sibling::WSEndpointRemoteRewriteRule)+1,'.serviceportregexp')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='RemoteEndpointProtocol'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.remote.',count(../preceding-sibling::WSEndpointRemoteRewriteRule)+1,'.protocol')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='RemoteEndpointHostname'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.remote.',count(../preceding-sibling::WSEndpointRemoteRewriteRule)+1,'.hostname')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='RemoteEndpointPort'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.remote.',count(../preceding-sibling::WSEndpointRemoteRewriteRule)+1,'.port')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='RemoteEndpointURI'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.remote.',count(../preceding-sibling::WSEndpointRemoteRewriteRule)+1,'.uri')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:when>
                    <!-- <WSEndpointPublishRewriteRule>
                        <ServicePortMatchRegexp>^{http://egov.stzh.ch/egadrs/service/v2}DruckenServiceV2WSPort$</ServicePortMatchRegexp>
                        <PublishedEndpointProtocol>https</PublishedEndpointProtocol>
                        <PublishedEndpointHostname>service-test.intra.stzh.ch</PublishedEndpointHostname>
                        <PublishedEndpointPort>443</PublishedEndpointPort>
                        <PublishedEndpointURI>/egadrs-ws/DruckenServiceV2</PublishedEndpointURI>
                        </WSEndpointPublishRewriteRule>
                    -->
                    <xsl:when test="local-name(.)='WSEndpointPublishRewriteRule'">
                        <xsl:element name="{name()}">
                            <xsl:for-each select="*">
                                <xsl:choose>
                                    <xsl:when test="local-name(.)='ServicePortMatchRegexp'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.publish.',count(../preceding-sibling::WSEndpointPublishRewriteRule)+1,'.serviceportregexp')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='PublishedEndpointProtocol'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.publish.',count(../preceding-sibling::WSEndpointPublishRewriteRule)+1,'.protocol')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='PublishedEndpointHostname'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.publish.',count(../preceding-sibling::WSEndpointPublishRewriteRule)+1,'.hostname')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='PublishedEndpointPort'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.publish.',count(../preceding-sibling::WSEndpointPublishRewriteRule)+1,'.port')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:when test="local-name(.)='PublishedEndpointURI'">
                                        <xsl:call-template name="set-token">
                                            <xsl:with-param name="key">
                                                <xsl:value-of
                                                    select="concat('wsendpoint.publish.',count(../preceding-sibling::WSEndpointPublishRewriteRule)+1,'.uri')" />
                                            </xsl:with-param>
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:copy-of select="." />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:element>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- PolicyAttachments -->
    <xsl:template match="PolicyAttachments">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>
        <xsl:variable name="index">
            <xsl:value-of select="count(./preceding-sibling::PolicyAttachments) + 1" />
        </xsl:variable>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*[name()!='name']" />
            <xsl:attribute name="name">
         <xsl:call-template name="set-attribute-token">
            <xsl:with-param name="name">
               <xsl:value-of select="string('name')" />
            </xsl:with-param>
            <xsl:with-param name="key">
               <xsl:value-of select="concat('policyattachments.',$index,'.name')" />
            </xsl:with-param>
         </xsl:call-template>
         </xsl:attribute>
            <xsl:for-each select="*">
                <xsl:choose>
                    <xsl:when test="local-name(.)='EnforcementMode'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('policyattachments.',$index,'.mode')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <!-- HTTP(S) Front Side Handler -->
    <xsl:template match="HTTPSSourceProtocolHandler|HTTPSourceProtocolHandler">
        <xsl:variable name="label">
            <xsl:value-of select="@name" />
        </xsl:variable>
        <xsl:variable name="index">
            <xsl:value-of
                select="position() - (count(../preceding-sibling::*[name()!='HTTPSSourceProtocolHandler' and name()!='HTTPSourceProtocolHandler']) + 0)" />
        </xsl:variable>

        <xsl:message>
            <xsl:value-of select="concat('fsh.',$index,'.class=',name())" />
        </xsl:message>

        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
            <xsl:copy-of select="@*[name()!='name']" />
            <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
                <xsl:with-param name="name">
                    <xsl:value-of select="string('name')" />
                </xsl:with-param>
                <xsl:with-param name="key">
                    <xsl:value-of select="concat('fsh.',$index,'.name')" />
                </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
            <xsl:for-each select="*">
                <xsl:choose>
                    <xsl:when test="local-name(.)='UserSummary'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.summary')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='LocalAddress'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.host')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='LocalPort'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.port')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLProxy'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.ssl')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLServer'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.ssl')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='SSLSNIServer'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.ssl')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:when test="local-name(.)='ACL'">
                        <xsl:call-template name="set-token">
                            <xsl:with-param name="key">
                                <xsl:value-of select="concat('fsh.',$index,'.acl')" />
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="." />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>



    <!-- N A M E D T E M P L A T E S -->

    <!-- print tokens and corresponding values to console -->
    <xsl:template name="set-token">
        <xsl:param name="key"></xsl:param>
        <xsl:message>
            <xsl:value-of select="concat($key,'=',text())" />
        </xsl:message>
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*" />
            <xsl:value-of select="concat('@',$key,'@')" />
        </xsl:element>
    </xsl:template>

    <xsl:template name="set-attribute-token">
        <xsl:param name="name"></xsl:param>
        <xsl:param name="key"></xsl:param>
        <xsl:message>
            <xsl:value-of select="concat($key,'=')" />
            <xsl:value-of select="@*[name()=$name]" />
        </xsl:message>
        <xsl:element name="{name()}">
            <xsl:value-of select="concat('@',$key,'@')" />
        </xsl:element>
    </xsl:template>

    <xsl:template name="set-attribute-and-value-token">
        <xsl:param name="attrname"></xsl:param>
        <xsl:param name="attrkey"></xsl:param>
        <xsl:param name="key"></xsl:param>
        <xsl:message>
            <xsl:value-of select="concat($key,'=',text())" />
        </xsl:message>
        <xsl:message>
            <xsl:value-of select="concat($attrkey,'=')" />
            <xsl:value-of select="@*[name()=$attrname]" />
        </xsl:message>
        <xsl:element name="{name()}">
            <xsl:attribute name="{$attrname}">
                <xsl:value-of select="concat('@',$attrkey,'@')" />
            </xsl:attribute>
            <xsl:value-of select="concat('@',$key,'@')" />
        </xsl:element>
    </xsl:template>

    <xsl:template name="print-read-only-token">
        <xsl:message>
            <xsl:value-of select="concat('#read-only# ',name(),'=',text())" />
        </xsl:message>
    </xsl:template>

</xsl:stylesheet>