<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
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
    <xsl:template match="export-details">
        <xsl:copy>
            <xsl:copy-of select="*[position()&lt;8]" />
            <xsl:text>&#xa;</xsl:text>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="configuration">
        <xsl:copy>
            <xsl:attribute name="domain"><xsl:value-of select="@domain" /></xsl:attribute>
        </xsl:copy>
    </xsl:template>
    <!-- TODO create hashes when building the upload zip -->
    <xsl:template match="files">
        <xsl:copy>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>
    <xsl:template match="file[@location='local']">
        <xsl:copy>
            <xsl:attribute name="name">
                <xsl:value-of select="@name" />
           </xsl:attribute>
            <xsl:attribute name="src">
                <xsl:value-of select="@src" />
           </xsl:attribute>
            <xsl:attribute name="location">
                <xsl:value-of select="@location" />
           </xsl:attribute>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="*" />
</xsl:stylesheet>