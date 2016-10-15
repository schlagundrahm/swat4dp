<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:dp="http://www.datapower.com/schemas/management"
   xmlns:str="http://exslt.org/strings" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">

   <xsl:output method="xml" omit-xml-declaration="yes" />

   <xsl:param name="domain" />
   <xsl:param name="device" />

   <xsl:template match="/soapenv:Envelope/soapenv:Body/dp:response/dp:file">

      <xsl:variable name="filename" select="./@name" />
      <xsl:variable name="index" select="position()" />
      <xsl:message>
         <xsl:value-of select="concat('file[', $index,']=', $filename)" />
      </xsl:message>



      <xsl:variable name="location">
         <xsl:call-template name="get-file-location">
            <xsl:with-param name="input" select="$filename" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="targetfile" select="concat('../service-export/', $location, '.b64')" />

      <xsl:message>
         <xsl:value-of select="concat('target file=',$targetfile)" />
      </xsl:message>

      <xsl:result-document method="text" href="{$targetfile}" omit-xml-declaration="yes">
         <xsl:value-of select="./text()" />
      </xsl:result-document>

      <xsl:value-of select="concat('filepath=', $location)" />
      

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
            <xsl:value-of select="concat(substring-before($input, ':///'), substring-after($input, '://'))" />
         </xsl:when>
         <xsl:when test="contains($input, '://')">
            <xsl:value-of select="concat(substring-before($input, '://'), substring-after($input, ':/'))" />
         </xsl:when>
         <xsl:when test="contains($input, ':/')">
            <xsl:value-of select="concat(substring-before($input, ':/'), substring-after($input, ':'))" />
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$input" />
         </xsl:otherwise>
      </xsl:choose>

   </xsl:template>

</xsl:stylesheet>