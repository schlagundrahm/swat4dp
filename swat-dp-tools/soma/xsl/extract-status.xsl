<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="text" />

    <xsl:template match="/soapenv:Envelope/soapenv:Body/dp:response">
        <xsl:text>## DataPower Response Status&#xa;</xsl:text>
		<xsl:apply-templates />
	</xsl:template>
    
    <xsl:template match="dp:timestamp">
        <xsl:text>&#xa;</xsl:text>
        Date: <xsl:value-of select="./text()" />
    </xsl:template>

    <xsl:template match="dp:import/import-results">
        <xsl:text>&#xa;</xsl:text>
        Domain:
        <xsl:value-of select="@domain" />
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="file-copy-log">
        <xsl:for-each select="file-result">
            <xsl:text>&#xa;</xsl:text>
            <xsl:value-of select="@result" />
            -- File:
            <xsl:value-of select="@name" />
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="exec-script-results">
        <xsl:for-each select="cfg-result">
            <xsl:text>&#xa;</xsl:text>
            <xsl:value-of select="@status" />
            -- Object:
            <xsl:value-of select="@name" />
            (
            <xsl:value-of select="@class" />
            )
        </xsl:for-each>
    </xsl:template>
    
	<xsl:template match="dp:file">
		<!-- do nothing i.e. skip file entries -->
	</xsl:template>    
</xsl:stylesheet>