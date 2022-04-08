<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:param name="domain" />

    <xsl:template match="//dp:filestore">
        <xsl:element name="file-list">
            <xsl:attribute name="domain" select="$domain" />
            <xsl:apply-templates select="*" />
        </xsl:element>

    </xsl:template>

    <xsl:template match="//file">

        <xsl:variable name="index" select="position()" />
        <xsl:variable name="filename" select="@name" />
        <xsl:variable name="dirname" select="parent::node()/@name" />

        <xsl:element name="file">
            <xsl:attribute name="index" select="$index" />
            <xsl:value-of select="concat($dirname, '/', $filename)" />
        </xsl:element>

    </xsl:template>

    <xsl:template match="//dp:timestamp">
    </xsl:template>


    <!--
        <xsl:template match="@*|node()">
        </xsl:template>
    -->
</xsl:stylesheet>