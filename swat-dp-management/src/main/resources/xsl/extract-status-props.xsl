<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
    <xsl:output method="text" />
    <xsl:template match="/soapenv:Envelope/soapenv:Body/dp:response">
        status.ts=<xsl:value-of select="dp:timestamp" />
        <xsl:apply-templates />
    </xsl:template>
    <xsl:template match="dp:import/import-results">
        <xsl:apply-templates />
    </xsl:template>
    <xsl:template match="export-details/domain">
        <xsl:text>&#xa;</xsl:text>status.domain=<xsl:value-of select="." />
    </xsl:template>    
    <xsl:template match="file-copy-log">
        <xsl:for-each select="file-result">
            <xsl:text>&#xa;</xsl:text>status.file.<xsl:value-of select="@name" />=<xsl:value-of select="@result" />
        </xsl:for-each>
    </xsl:template>
    <xsl:template match="exec-script-results">
        <xsl:for-each select="cfg-result">
            <xsl:text>&#xa;</xsl:text>status.object.<xsl:value-of select="@class" />.<xsl:value-of select="@name" />=<xsl:value-of select="@status" />
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
