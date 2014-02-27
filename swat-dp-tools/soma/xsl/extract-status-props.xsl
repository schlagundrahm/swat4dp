<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:xalan="http://xml.apache.org/xslt" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="text" indent="no" encoding="UTF-8" />
    <xsl:strip-space elements="*" />

    <xsl:template match="/soapenv:Envelope/soapenv:Body/dp:response">
        <xsl:text>## DataPower Response Status&#xa;</xsl:text>
		<xsl:apply-templates />
	</xsl:template>
    
    <xsl:template match="dp:timestamp">
        <xsl:value-of select="concat('status.ts=',./text(),'&#xa;')" />
    </xsl:template>

	<xsl:template match="dp:import/import-results">
		<xsl:text># import-results&#xa;</xsl:text>
		<xsl:value-of select="concat('status.import.domain=',@domain,'&#xa;')" />
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="export-details">
		<xsl:text># export-details&#xa;</xsl:text>
        <xsl:apply-templates />		
	</xsl:template>
    
    <xsl:template match="export-details/domain">
        <xsl:value-of select="concat('status.export.domain=',.,'&#xa;')" />
    </xsl:template>
    
    <xsl:template match="export-details/user">
        <xsl:value-of select="concat('status.export.user=',.,'&#xa;')" />
    </xsl:template>
    
    <xsl:template match="export-details/comment">
        <xsl:value-of select="concat('status.export.comment=',.,'&#xa;')" />
    </xsl:template>
    
    <xsl:template match="export-details/description">
        <xsl:value-of select="concat('status.export.description=',.,'&#xa;')" />
    </xsl:template>
    
    <xsl:template match="export-details/current-date">
        <xsl:value-of select="concat('status.export.ts=',.,' ')" />
    </xsl:template>
    
    <xsl:template match="export-details/current-time">
        <xsl:value-of select="concat(.,'&#xa;')" />
    </xsl:template>
    
    <xsl:template match="imported-files">
        <xsl:text># imported-files&#xa;</xsl:text>
        <xsl:for-each select="file">
            <xsl:value-of select="concat('status.import.file.',position(),'=',@src,'&#xa;')" />
        </xsl:for-each>
    </xsl:template>

	<xsl:template match="file-copy-log">
		<xsl:text># file-copy-log&#xa;</xsl:text>
		<xsl:for-each select="file-result">
			<xsl:value-of select="concat('status.file-copy.',position(),'.name','=',@name,'&#xa;')" />
            <xsl:value-of select="concat('status.file-copy.',position(),'.result','=',@result,' [', reason/text(), ']&#xa;')" />
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="exec-script-results">
		<xsl:text># exec-script-results&#xa;</xsl:text>
		<xsl:for-each select="cfg-result">
			<xsl:value-of select="concat('status.object.',@class,'.',@name,'=',@status,'&#xa;')" />
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="error-log">
		<xsl:text># error-log&#xa;</xsl:text>
		<xsl:value-of select="concat('status.log.',log-event/@level,'=',log-event/text(),'&#xa;')" />
	</xsl:template>	

	<xsl:template match="dp:result">
		<xsl:text># dp:result&#xa;</xsl:text>
		<xsl:value-of select="concat('status.result=',./text(),'&#xa;')" />
		<!-- apply templates to element nodes only i.e. skip the dp:result text node -->
		<xsl:apply-templates select="child::*"/>
	</xsl:template>
	
	<xsl:template match="dp:file">
		<!-- do nothing i.e. skip file entries -->
	</xsl:template>
	
</xsl:stylesheet>
