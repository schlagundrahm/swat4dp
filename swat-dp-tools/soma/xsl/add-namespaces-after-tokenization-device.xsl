<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xalan="http://xml.apache.org/xslt" 
    xmlns:dp="http://www.datapower.com/schemas/management" 
    xmlns:str="http://exslt.org/strings" 
    xmlns:env="http://www.w3.org/2003/05/soap-envelope">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />

    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@*">
                <xsl:sort select="name(.)" />
            </xsl:apply-templates>
            <xsl:apply-templates />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|comment()|processing-instruction()">
        <xsl:copy />
    </xsl:template>

    <xsl:template match="/datapower-configuration/configuration/*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="document('')/*/namespace::*[name()='env']"/>
            <xsl:copy-of select="document('')/*/namespace::*[name()='dp']"/>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>