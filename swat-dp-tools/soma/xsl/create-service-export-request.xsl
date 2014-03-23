<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="2.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management"
    xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:param name="domain" />
    <xsl:param name="serviceObjectClass" />
    <xsl:param name="serviceObjectName" />

    <xsl:template match="/">
        <xsl:element name="soapenv:Envelope">
            <xsl:element name="soapenv:Body">
                <xsl:element name="dp:request">
                    <xsl:attribute name="domain"><xsl:value-of select="$domain" /></xsl:attribute>

                    <xsl:element name="dp:do-export">
                        <xsl:attribute name="format"><xsl:text>ZIP</xsl:text></xsl:attribute>
                        <xsl:element name="dp:object">
                            <xsl:attribute name="class"><xsl:value-of select="$serviceObjectClass" /></xsl:attribute>
                            <xsl:attribute name="name"><xsl:value-of select="$serviceObjectName" /></xsl:attribute>
                            <xsl:attribute name="ref-objects"><xsl:text>true</xsl:text></xsl:attribute>
                            <xsl:attribute name="ref-files"><xsl:text>true</xsl:text></xsl:attribute>
                        </xsl:element>
                        <xsl:for-each select="import-objects/object">
                            <xsl:element name="dp:object">
                                <xsl:attribute name="class"><xsl:value-of select="@type" /></xsl:attribute>
                                <xsl:attribute name="name"><xsl:value-of select="@name" /></xsl:attribute>
                                <xsl:attribute name="ref-objects"><xsl:text>true</xsl:text></xsl:attribute>
                                <xsl:attribute name="ref-files"><xsl:text>true</xsl:text></xsl:attribute>
                            </xsl:element>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>