<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
	xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" 
	exclude-result-prefixes="dp str xalan">

	<xsl:output method="xml" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:template match="/datapower-configuration">
		<xsl:copy>
			<xsl:attribute name="version">
                <xsl:value-of select="@version" />
           </xsl:attribute>
		   <xsl:apply-templates />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="export-details">
		<xsl:copy>
			<xsl:copy-of select="*[position()&lt;8]" />
		</xsl:copy>
	</xsl:template>

	<xsl:template match="configuration">
		<configuration>
		<xsl:for-each select="node()">
			<xsl:sort select="name()" />
			<xsl:copy-of select="." />
		</xsl:for-each>
		</configuration>
		<xsl:apply-templates />
	</xsl:template>

	<!-- reorder files -->
	<xsl:template match="files">
		<files>
		<xsl:for-each select="file">
			<xsl:sort select="@name" />
			<xsl:copy-of select="." />
		</xsl:for-each>
		</files>
		<xsl:apply-templates />

	</xsl:template>


	<xsl:template match="*">
		<!-- xsl:copy-of select="." / -->
	</xsl:template>
</xsl:stylesheet>