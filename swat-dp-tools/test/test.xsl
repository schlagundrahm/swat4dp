<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" omit-xml-declaration="yes" />

    <xsl:template match="/">
        Enter the template
        <xsl:apply-templates />
        Finished the template
    </xsl:template>

    <xsl:template match="request-uri">
        <xsl:variable name="url" select="./text()" />
        <xsl:variable name="req" select="@name" />
        <xsl:variable name="should" select="@should" />

        Checking variable:
        <xsl:value-of select="concat($url, ' ; ', $req)" />
        <xsl:text>&#xa;</xsl:text>

        <xsl:choose>
            <xsl:when test="matches($url, '^(/)([^/]*)(edu)')">
                DOES MATCH
                <xsl:choose>
                    <xsl:when test="$should = 'true'">
                        Request-Uri
                        <xsl:value-of select="./@name" />
                        is handled correctly.
                    </xsl:when>
                    <xsl:otherwise>
                        Request-Uri
                        <xsl:value-of select="./@name" />
                        is NOT HANDLED CORRECTLY.
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                DOES NOT MATCH
                <xsl:choose>
                    <xsl:when test="$should = 'false'">
                        Request-Uri
                        <xsl:value-of select="./@name" />
                        is handled correctly.
                    </xsl:when>
                    <xsl:otherwise>
                        Request-Uri
                        <xsl:value-of select="./@name" />
                        is NOT HANDLED CORRECTLY.
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

</xsl:stylesheet>