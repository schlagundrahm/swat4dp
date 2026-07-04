<?xml version="1.0" encoding="UTF-8"?>
<!--
    Reformat xcfg files: sort attributes alphabetically and re-indent the document.
    Used by xcfg-reformat-macro after tokenization to produce stable, diff-friendly output.
-->
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xslt"
    exclude-result-prefixes="xalan">

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

</xsl:stylesheet>
