<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:template match="/datapower-configuration">
        <xsl:element name="soapenv:Envelope">
            <xsl:element name="soapenv:Body">
                <xsl:element name="dp:request">
                    <xsl:attribute name="domain"><xsl:value-of select="configuration/@domain" /></xsl:attribute>

                    <xsl:element name="dp:del-config">
                        <xsl:for-each select="configuration/*">
                            <xsl:sort select="position()" data-type="number" order="descending" />
                            <xsl:variable name="currentType" select="name()" />
                            <xsl:variable name="currentName" select="@name" />
                            <xsl:choose>
                                <xsl:when test="@intrinsic='true'">
                                    <xsl:message>
                                        IGNORE - Intrinsic object found:
                                        <xsl:value-of select="$currentType" />
                                        /
                                        <xsl:value-of select="$currentName" />
                                    </xsl:message>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:element name="{$currentType}">
                                        <xsl:attribute name="name"><xsl:value-of select="$currentName" /></xsl:attribute>
                                    </xsl:element>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
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