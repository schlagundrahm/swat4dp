<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />
    
    <!--  unused in this style-sheet -->
    <xsl:param name="filterObjectTypes" />
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
            <xsl:attribute name="domain"><xsl:value-of select="@domain" /></xsl:attribute>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <!-- Filter out all objects, that have the attribute intrinsic assuming it's value is 'true' -->
    <xsl:template match="*[@intrinsic='true']">
        <xsl:copy-of select="." />
    </xsl:template>

    <xsl:template match="*">
    </xsl:template>
</xsl:stylesheet>