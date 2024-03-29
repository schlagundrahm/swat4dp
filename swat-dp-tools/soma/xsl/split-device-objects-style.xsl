<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
    <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*" />

    <xsl:param name="filterObjectTypes" />
    <xsl:param name="log-level" />
    <xsl:variable name="filterObjectTypesVar" select="$filterObjectTypes" />
    <xsl:variable name="filterObjectTypeList" select="tokenize($filterObjectTypesVar, ',')" />

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

    <xsl:template match="*">
        <xsl:if test="number($log-level) = 0">
            <xsl:message>
                <xsl:value-of
                    select="concat('trying to match ', local-name(), ' against ', string-join($filterObjectTypeList,','), ' ...')" />
            </xsl:message>
        </xsl:if>
        <xsl:if test="exists(index-of($filterObjectTypeList, local-name()))">
            <xsl:if test="number($log-level) &lt; 2">
                <xsl:message>
                    <xsl:value-of
                        select="concat('... found match of ', local-name(), ' within ', string-join($filterObjectTypeList,','))" />
                </xsl:message>
            </xsl:if>
            <xsl:copy-of select="." />
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>