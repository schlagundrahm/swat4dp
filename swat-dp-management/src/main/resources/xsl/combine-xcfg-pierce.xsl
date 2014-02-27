<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
	<xsl:output method="xml" indent="yes" omit-xml-declaration="yes" />
	<xsl:strip-space elements="datapower-configuration" />
	<xsl:param name="xcfgSelection" />
	<xsl:param name="filesConfig" />
	<xsl:variable name="xcfgSelectionVar" select="$xcfgSelection" />
	<xsl:variable name="filesConfigVar" select="$filesConfig" />
	<xsl:template match="/datapower-configuration">
		<xsl:copy>
			<xsl:attribute name="version">
                <xsl:value-of select="@version" />
           </xsl:attribute>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*/export-details">
		<xsl:copy-of select="."/>
	</xsl:template>
	<xsl:template match="*/configuration">
		<xsl:copy>
			<xsl:attribute name="domain">
                <xsl:value-of select="@domain" />
            </xsl:attribute>
			<xsl:text>&#xa;</xsl:text>
			<xsl:for-each select="collection($xcfgSelectionVar)">
				<xsl:variable name="curFile" select="document-uri(.)" />
				<xsl:variable select="document($curFile)" name="contents" />
				<xsl:copy-of select="$contents/datapower-configuration/configuration/*" />
				<xsl:text>&#xa;</xsl:text>
			</xsl:for-each>
			<xsl:apply-templates />
		</xsl:copy>
	</xsl:template>
	<xsl:template match="*/files">
		<xsl:variable name="contents" select="document($filesConfigVar)" />
		<xsl:copy-of select="$contents/files/*" />
	</xsl:template>
</xsl:stylesheet>