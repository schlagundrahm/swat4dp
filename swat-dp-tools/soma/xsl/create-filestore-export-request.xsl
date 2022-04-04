<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:param name="domain" />

    <!-- get filestore snapshot
        <xsd:element name="get-filestore">
        <xsd:complexType>
        <xsd:attribute name="location" type="tns:filestore-location"/>
        <xsd:attribute name="annotated" type="xsd:boolean"/>
        <xsd:attribute name="layout-only" type="xsd:boolean"/>
        <xsd:attribute name="no-subdirectories" type="xsd:boolean"/>
        </xsd:complexType>
        </xsd:element>
    -->

    <xsl:template match="/">
        <xsl:element name="soapenv:Envelope">
            <xsl:element name="soapenv:Body">
                <xsl:element name="dp:request">
                    <xsl:attribute name="domain"><xsl:value-of select="$domain" /></xsl:attribute>

                    <xsl:element name="dp:get-filestore">
                        <xsl:attribute name="location"><xsl:value-of select="/export-files/filestore[1]/@location" /></xsl:attribute>
                        <xsl:attribute name="no-subdirectories"><xsl:value-of select="/export-files/filestore[1]/@no-subdirectories" /></xsl:attribute>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>