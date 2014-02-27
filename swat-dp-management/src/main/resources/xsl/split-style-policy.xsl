<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
	xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
	<xsl:strip-space elements="datapower-configuration"/>
	<xsl:param name="serviceObjectName" />
	<xsl:variable name="serviceObjectNameVar" select="$serviceObjectName" />
	<xsl:variable name="serviceObjectNameLengthVar" select="string-length($serviceObjectNameVar)" />

	<xsl:template match="/">
        <xsl:for-each select="datapower-configuration/configuration/StylePolicy/PolicyMaps/Rule">
            <xsl:variable name="index" select="29 + position()" />
            <xsl:variable name="rulename" select="text()" />            
            <xsl:result-document method="xml" href="{$serviceObjectName}-{$index}-{substring($rulename,$serviceObjectNameLengthVar+2)}.xcfg" omit-xml-declaration="yes">
                <datapower-configuration version="3">
                    <xsl:copy-of select="../../../../export-details" />
                    <configuration domain="@domain.name@">
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
                    </configuration>
                </datapower-configuration>        
            </xsl:result-document>
        </xsl:for-each>
	</xsl:template>	

</xsl:stylesheet>