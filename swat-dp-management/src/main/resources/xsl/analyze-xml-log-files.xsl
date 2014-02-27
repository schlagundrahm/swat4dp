<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xslt">

	<xsl:output method="text" indent="yes"/>
	<xsl:strip-space elements="log"/>
	
	<xsl:param name="pattern" />
	<xsl:param name="logFiles" />
	
	<xsl:template match="/dummy">
		<xsl:message>Scanning directory <xsl:value-of select="$logFiles"/></xsl:message>
		<xsl:value-of select="'Date-Time ZULU;UTC;Class;Object;Transaction Type;Transaction ID;Client IP;Message'" />
		<xsl:text>&#xa;</xsl:text>
        <xsl:for-each select="collection($logFiles)">
				<xsl:variable name="curFile" select="document-uri(.)" />
				<xsl:message>Processing log: <xsl:value-of select="$curFile"/></xsl:message>
				<xsl:variable name="contents" select="document($curFile)" />
				<xsl:apply-templates select="$contents" />
		</xsl:for-each>
    </xsl:template>
	

	<!-- find all log entries containing the given pattern within the message field -->
	<!-- Return the result in a CSV format in order to be read by Excel -->
	<xsl:template match="/log/log-entry">
		<xsl:if test="matches(./message,$pattern)">
			<xsl:message>Match found: <xsl:value-of select="./message"/></xsl:message>
			<xsl:copy-of select="./date-time/text()"/>;<xsl:copy-of select="./time/@utc/string()"/>;<xsl:copy-of select="./class/text()"/>;<xsl:copy-of select="./object/text()"/>;<xsl:copy-of select="./transaction-type/text()"/>;<xsl:copy-of select="./transaction/text()"/>;<xsl:copy-of select="./client/text()"/>;<xsl:copy-of select="./message/text()"/><xsl:text>&#xa;</xsl:text>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>