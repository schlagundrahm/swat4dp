<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:param name="domain" />

    <xsl:template match="/datapower-configuration">
        <xsl:element name="import-objects">
            <xsl:attribute name="domain"><xsl:value-of select="$domain" /></xsl:attribute>

            <xsl:for-each select="configuration/*">
                <xsl:element name="object">
                    <xsl:attribute name="type"><xsl:value-of select="name()" /></xsl:attribute>
                    <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
                </xsl:element>
            </xsl:for-each>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>