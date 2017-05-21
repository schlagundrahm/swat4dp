<?xml version="1.0" encoding="UTF-8"?>
<!-- Licensed Materials - Property of schlag&rahm -->
<!-- Copyright (c) 2010, 2013 schlag&rahm AG, Switzerland. All rights reserved. -->
<!-- Licensed Materials - Property of IBM -->
<!-- Copyright IBM Corporation 2013. All Rights Reserved. -->
<!-- US Government Users Restricted Rights - Use, duplication or disclosure -->
<!-- restricted by GSA ADP Schedule Contract with IBM Corp. -->
<xsl:stylesheet 
    version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xalan="http://xml.apache.org/xslt" 
    exclude-result-prefixes="xalan">
    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />
    
    <xsl:param name="resultDir" />
    <xsl:param name="serviceName" />
    <xsl:param name="serviceObjectName" />
    <xsl:param name="useIndexInRuleName" />
    <xsl:param name="log-level" />

    <xsl:variable name="dir">
        <xsl:value-of select="replace($resultDir, '%5C', '/')"/>
    </xsl:variable>
    

    <xsl:variable name="serviceNameVar" select="$serviceName" />

    <xsl:variable name="serviceObjectNameVar" select="$serviceObjectName" />
    <xsl:variable name="serviceObjectNameLengthVar" select="string-length($serviceObjectNameVar)" />

    <xsl:template match="/">
        <xsl:message>
            <xsl:value-of select="concat('result directory: ', $dir)" />
        </xsl:message>
        <!-- called  rules -->
        <xsl:for-each select="datapower-configuration/configuration/StylePolicyRule[Direction='rule']">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="@name" />
            <xsl:if test="number($log-level) &lt; 2" >
                <xsl:message>
                    <xsl:value-of select="concat('processing rule ', $rulename, ' ...')" />
                </xsl:message>
            </xsl:if>
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-29-', $index, '-', $rulename, '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-29-', $rulename, '.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:if test="number($log-level) &lt; 2" >
                <xsl:message>
                    <xsl:value-of select="concat('processing rule ', $rulename, ' [', $filename, '] ...')" />
                </xsl:message>
            </xsl:if>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' Called Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../export-details" />
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="./Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="number($log-level) &lt; 2" >
                                <xsl:message>
                                    <xsl:value-of select="concat('adding action ', $actname, ' ...')" />
                                </xsl:message>
                            </xsl:if>
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:if test="number($log-level) &lt; 2" >
                                        <xsl:message>
                                            <xsl:value-of select="concat('adding condition ', $condname, ' ...')" />
                                        </xsl:message>
                                    </xsl:if>
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="." />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
        <!-- MPGW/XMLFW Policies -->
        <xsl:for-each select="datapower-configuration/configuration/StylePolicy/PolicyMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:if test="number($log-level) &lt; 2" >
                <xsl:message>
                    <xsl:value-of select="concat('processing rule ', $rulename, ' ...')" />
                </xsl:message>
            </xsl:if>
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-30-', $index, '-', substring($rulename, $serviceObjectNameLengthVar+2), '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-30-', substring($rulename, $serviceObjectNameLengthVar+2), '.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:if test="number($log-level) &lt; 2" >
                <xsl:message>
                    <xsl:value-of select="concat('processing rule ', $rulename, ' [', $filename, '] ...')" />
                </xsl:message>
            </xsl:if>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' MPGW/XMLFW Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../../StylePolicyRule[@name=$rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="number($log-level) &lt; 2" >
                                <xsl:message>
                                    <xsl:value-of select="concat('adding action ', $actname, ' ...')" />
                                </xsl:message>
                            </xsl:if>
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:if test="number($log-level) &lt; 2" >
                                        <xsl:message>
                                            <xsl:value-of select="concat('adding condition ', $condname, ' ...')" />
                                        </xsl:message>
                                </xsl:if>
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="../../../StylePolicyRule[@name=$rulename]" />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
        <!-- WS-Proxy Policies -->
        <xsl:for-each select="datapower-configuration/configuration/WSStylePolicy/PolicyMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-30-', $index, '-', substring($rulename, $serviceObjectNameLengthVar+2), '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-30-', substring($rulename, $serviceObjectNameLengthVar+2), '.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:if test="number($log-level) &lt; 2" >
                <xsl:message>
                    <xsl:value-of select="concat('processing WSStylePolicy rule ', $rulename, ' [', $filename, '] ...')" />
                </xsl:message>
            </xsl:if>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' WSP Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../../WSStylePolicyRule[@name=$rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="number($log-level) &lt; 2" >
                                <xsl:message>
                                    <xsl:value-of select="concat('adding action ', $actname, ' ...')" />
                                </xsl:message>
                            </xsl:if>
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:if test="number($log-level) &lt; 2" >
                                        <xsl:message>
                                            <xsl:value-of select="concat('adding condition ', $condname, ' ...')" />
                                        </xsl:message>
                                    </xsl:if>
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="../../../WSStylePolicyRule[@name=$rulename]" />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
        <!-- WAF Default Error Policy of type Error Rule (StylePolicyRule) - there should be only one ErrorStyplePolicy -->
        <xsl:for-each select="datapower-configuration/configuration/WebAppErrorHandlingPolicy/ErrorStylePolicyRule[@class='StylePolicyRule']">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-30-waf-default-error-', $index, '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-30-waf-default-error.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' WAF Error Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../StylePolicyRule[@name=$rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="../../StylePolicyRule[@name=$rulename]" />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
        <!-- WAF Application Security Policy - Request Maps -->
        <xsl:for-each select="datapower-configuration/configuration/AppSecurityPolicy/RequestMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-30-waf-request-', $index, '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-30-waf-request.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' WAF Request Map Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:variable name="nonxml-rulename" select="/datapower-configuration/configuration/WebAppRequest[@name=$rulename]/NonXMLRule[@class='StylePolicyRule']/text()" />
                    <xsl:variable name="xml-rulename" select="/datapower-configuration/configuration/WebAppRequest[@name=$rulename]/XMLRule[@class='StylePolicyRule']/text()" />
                    <xsl:message>
                        <xsl:value-of select="concat('NonXMLRule = ', $nonxml-rulename)" />
                        <xsl:value-of select="concat('XMLRule = ', $xml-rulename)" />
                    </xsl:message>
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../../StylePolicyRule[@name=$nonxml-rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:for-each select="../../../StylePolicyRule[@name=$xml-rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:message><xsl:value-of select="string($actname)" /></xsl:message>
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="../../../StylePolicyRule[@name=$nonxml-rulename]" />
                        <xsl:copy-of select="../../../StylePolicyRule[@name=$xml-rulename]" />
                        <xsl:copy-of select="../../../WebAppRequest[@name=$rulename]" />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
        <!-- WAF Application Security Policy - Response Maps -->
        <xsl:for-each select="datapower-configuration/configuration/AppSecurityPolicy/ResponseMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-30-waf-response-', $index, '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-30-waf-response.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' WAF Response Map Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:variable name="nonxml-rulename" select="/datapower-configuration/configuration/WebAppResponse[@name=$rulename]/NonXMLRule[@class='StylePolicyRule']/text()" />
                    <xsl:variable name="xml-rulename" select="/datapower-configuration/configuration/WebAppResponse[@name=$rulename]/XMLRule[@class='StylePolicyRule']/text()" />
                    <xsl:message>
                        <xsl:value-of select="concat('NonXMLRule = ', $nonxml-rulename)" />
                        <xsl:value-of select="concat('XMLRule = ', $xml-rulename)" />
                    </xsl:message>
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../../StylePolicyRule[@name=$nonxml-rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:for-each select="../../../StylePolicyRule[@name=$xml-rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:message><xsl:value-of select="string($actname)" /></xsl:message>
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="../../../StylePolicyRule[@name=$nonxml-rulename]" />
                        <xsl:copy-of select="../../../StylePolicyRule[@name=$xml-rulename]" />
                        <xsl:copy-of select="../../../WebAppResponse[@name=$rulename]" />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
        <!-- WAF Application Security Policy - Error Maps -->
        <xsl:for-each select="datapower-configuration/configuration/AppSecurityPolicy/ErrorMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:variable name="filename">
                <xsl:choose>
                    <xsl:when test="$useIndexInRuleName = 'true'">
                        <xsl:value-of select="concat($serviceName, '-30-waf-error-', $index, '.xcfg')" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat($serviceName, '-30-waf-error.xcfg')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:result-document method="xml" href="file:////{$dir}/{$filename}" omit-xml-declaration="yes">
                <xsl:comment>
                    <xsl:value-of select="concat(' WAF Error Map Rule Index ', $index, ' ')" />
                </xsl:comment>
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../../StylePolicyRule[@name=$rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
                                    <xsl:copy-of select="../../StylePolicyAction[@name=$condname]" />
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:copy-of select="../../StylePolicyAction[@name=$actname]" />
                        </xsl:for-each>
                        <xsl:copy-of select="../../../StylePolicyRule[@name=$rulename]" />
                    </xsl:element>
                </datapower-configuration>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>