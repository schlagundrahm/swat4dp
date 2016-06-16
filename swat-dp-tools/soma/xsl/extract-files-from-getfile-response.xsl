<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:xalan="http://xml.apache.org/xslt"
   xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings" xmlns:saxon="http://saxon.sf.net/" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

   <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
   <xsl:strip-space elements="*" />

   <xsl:param name="domain" />
   <xsl:param name="device" />

   <xsl:template match="//fileresponse">

      <xsl:message>
         <xsl:value-of select="concat('files',./@file)" />
      </xsl:message>
      
         <xsl:variable name="index" select="position()" />
         <xsl:variable name="filename" select="./@file" />
         <xsl:variable name="location" select="./@location" />
         <xsl:variable name="targetfile" select="concat('get-file-', $index,'-',translate(translate($filename,':',''),'/','_'),'.xml')" />
        
         <xsl:message>
            <xsl:value-of select="concat('target file=',$targetfile)" />
         </xsl:message>

         <xsl:result-document method="text" href="{$request-filename}" omit-xml-declaration="yes">
            <xsl:value-of select="saxon:base64Binary-to-string(xs:base64Binary('RGFzc2Vs'), 'UTF8')" />
           
         </xsl:result-document>

   </xsl:template>

   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
   </xsl:template>

   <xsl:template name="get-file-name">
      <xsl:param name="input" />

      <xsl:choose>
         <xsl:when test="contains($input, '/')">
            <xsl:call-template name="get-file-name">
               <xsl:with-param name="input" select="substring-after($input, '/')" />
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$input" />
         </xsl:otherwise>
      </xsl:choose>

   </xsl:template>

   <xsl:template name="get-file-location">
      <xsl:param name="input" />

      <xsl:choose>
         <xsl:when test="contains($input, ':///')">
            <xsl:value-of select="substring-after($input, '://')" />
         </xsl:when>
         <xsl:when test="contains($input, '://')">
            <xsl:value-of select="substring-after($input, ':/')" />
         </xsl:when>
         <xsl:when test="contains($input, ':/')">
            <xsl:value-of select="substring-after($input, ':')" />
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$input" />
         </xsl:otherwise>
      </xsl:choose>

   </xsl:template>

</xsl:stylesheet>