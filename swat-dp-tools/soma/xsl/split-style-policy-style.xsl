<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xalan="http://xml.apache.org/xslt" 
    exclude-result-prefixes="xalan">
    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />
    
    <xsl:param name="serviceName" />
    <xsl:param name="serviceObjectName" />

    <xsl:variable name="serviceNameVar" select="$serviceName" />

    <xsl:variable name="serviceObjectNameVar" select="$serviceObjectName" />
    <xsl:variable name="serviceObjectNameLengthVar" select="string-length($serviceObjectNameVar)" />

    <xsl:template match="/">
        <xsl:for-each select="datapower-configuration/configuration/StylePolicy/PolicyMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:result-document method="xml" href="{$serviceName}-30-{$index}-{substring($rulename,$serviceObjectNameLengthVar+2)}.xcfg" omit-xml-declaration="yes">
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
        <xsl:for-each select="datapower-configuration/configuration/WSStylePolicy/PolicyMaps/Rule">
            <xsl:variable name="index" select="99 + position()" />
            <xsl:variable name="rulename" select="text()" />
            <xsl:result-document method="xml" href="{$serviceName}-30-{$index}-{substring($rulename,$serviceObjectNameLengthVar+2)}.xcfg" omit-xml-declaration="yes">
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <xsl:element name="configuration">
                        <xsl:attribute name="domain"><xsl:value-of select="/datapower-configuration/configuration/@domain" /></xsl:attribute>
                        <xsl:for-each select="../../../WSStylePolicyRule[@name=$rulename]/Actions">
                            <xsl:variable name="actname" select="text()" />
                            <xsl:if test="../../StylePolicyAction[@name=$actname]/Type/text() = 'conditional'">
                                <xsl:for-each select="../../StylePolicyAction[@name=$actname]/Condition">
                                    <xsl:variable name="condname" select="ConditionAction/text()" />
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
            <xsl:result-document method="xml" href="{$serviceName}-30-waf-default-error.xcfg" omit-xml-declaration="yes">
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
            <xsl:result-document method="xml" href="{$serviceName}-30-waf-request-{$index}.xcfg" omit-xml-declaration="yes">
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
            <xsl:result-document method="xml" href="{$serviceName}-30-waf-response-{$index}.xcfg" omit-xml-declaration="yes">
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
            <xsl:result-document method="xml" href="{$serviceName}-30-waf-error-{$index}.xcfg" omit-xml-declaration="yes">
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