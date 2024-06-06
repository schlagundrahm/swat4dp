<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
    <xsl:strip-space elements="*" />

    <xsl:param name="importFile" />
    <xsl:param name="log-level" />

    <xsl:template match="/datapower-configuration">
        <xsl:element name="datapower-configuration">
            <xsl:attribute name="version"><xsl:value-of select="@version" /></xsl:attribute>
            <xsl:element name="configuration">
                <xsl:attribute name="domain"><xsl:value-of select="configuration/@domain" /></xsl:attribute>

                <xsl:for-each select="configuration/*">
                    <xsl:variable name="currentType" select="name()" />
                    <xsl:variable name="currentName" select="@name" />
                    <xsl:variable name="currentObjectFound"
                        select="count(document($importFile)/import-objects/object[@type=$currentType and @name=$currentName])" />
                    <xsl:choose>
                        <xsl:when test="$currentObjectFound > 0">
                            <xsl:message>
                                <xsl:value-of select="concat('REMOVE - Object: (', $currentType, ')', $currentName)" />
                            </xsl:message>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:if test="number($log-level) &lt; 2">
                                <xsl:message>
                                    <xsl:value-of select="concat('COPY - Object: (', $currentType, ')', $currentName)" />
                                </xsl:message>
                            </xsl:if>
                            <xsl:copy-of select="." />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>