<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="2.0" 
    xmlns:xalan="http://xml.apache.org/xslt" 
    xmlns:dp="http://www.datapower.com/schemas/management"
    xmlns:str="http://exslt.org/strings" 
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" />
    <xsl:strip-space elements="*"/>

    <xsl:template match="/datapower-configuration/configuration/*[@name='']">
        <!-- do nothing i.e. remove element-->
        <xsl:message>
            <xsl:value-of select="concat('found element with empty name ', ./name())"/>
         </xsl:message>
    </xsl:template>
    
    <xsl:template match="/datapower-configuration/configuration//*[text()='@TO_BE_REMOVED@']">
        <!-- do nothing i.e. remove element-->
        <xsl:message>
            <xsl:value-of select="concat('found element with @TO_BE_REMOVED@ token ', ./name())" />
        </xsl:message>
    </xsl:template>
    
    <xsl:template match="/datapower-configuration/configuration//*[child::node()/text()='@PARENT_TO_BE_REMOVED@']">
        <!-- do nothing i.e. remove element-->
        <xsl:message>
            <xsl:value-of select="concat('found element with @PARENT_TO_BE_REMOVED@ token ', ./name())" />
        </xsl:message>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>