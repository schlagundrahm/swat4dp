<?xml version="1.0" encoding="UTF-8"?>
<!-- -->
<!-- Author: Pierce Shah - pshah@schlagundrahm.ch -->
<!-- Based on the printPolicyRules.xsl stylesheet by John Rasmussen (rasmussj@us.ibm.com) -->
<!-- Run this against a service export.xml to produce a HTML report -->
<!-- -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xslt"
   xmlns:dp="http://www.datapower.com/extensions" extension-element-prefixes="dp">

   <xsl:param name="project" />
   <xsl:param name="service" />
   <xsl:param name="class" />
   <xsl:param name="env" />

   <xsl:output method="html" doctype-public="html" indent="yes" xalan:indent-amount="4" encoding="UTF-8" omit-xml-declaration="yes" />

   <xsl:variable name="timestamp" select="current-dateTime()"/>
   
   <xsl:variable name="vMainDoc" select="/" />

   <xsl:variable name="LF" select="'&#x0D;'" />

   <!-- origin -->
   <xsl:variable name="startX" select="150" />
   <xsl:variable name="startY" select="50" />

   <xsl:variable name="shapeR" select="10" />

   <!-- connectors -->
   <xsl:variable name="conL" select="100" />

   <!-- frontside coordinates -->
   <xsl:variable name="countF" select="count($vMainDoc//FrontProtocol[not(. = ../following-sibling::*/FrontProtocol)])" />
   <xsl:variable name="shapeFW" select="175" />
   <xsl:variable name="shapeFH" select="60" />
   <xsl:variable name="shapeFX" select="$startX + 50" />
   <xsl:variable name="deltaFY" select="10" />

   <!-- service coordinates -->
   <xsl:variable name="shapeSW" select="250" />
   <xsl:variable name="shapeSH" select="150" />
   <xsl:variable name="shapeSX" select="$shapeFX + $shapeFW + $conL" />

   <!-- backend coordinates -->
   <xsl:variable name="shapeBW" select="250" />
   <xsl:variable name="shapeBH" select="150" />
   <xsl:variable name="shapeBX" select="$shapeSX + $shapeSW + $conL" />

   <!-- text indent within shapes -->
   <xsl:variable name="shapeIX" select="5" />
   <xsl:variable name="shapeIY" select="5" />

   <!-- shape colors -->
   <xsl:variable name="colorBackend" select="'greenyellow'" />
   <xsl:variable name="colorFrontside" select="'deepskyblue'" />
   <xsl:variable name="colorService" select="'gold'" />



   <xsl:template match="/">
      <html>
         <head>
            <h1 align="center">
               <xsl:value-of select="string('*** Swat4DP - Technical Service Documentation ***')" />
            </h1>
            <h4 align="center">
               <xsl:value-of select="concat('created on: ', $timestamp)" />
            </h4>
            <h1 align="center">
               <xsl:value-of select="$project" />
            </h1>
            <h2 align="center">
               <xsl:value-of select="/datapower-configuration/export-details/comment" />
            </h2>
            <h2 align="center">
               <xsl:value-of select="/datapower-configuration/export-details/display-model" />
            </h2>
            <h2 align="center">
               <xsl:value-of select="/datapower-configuration/export-details/device-name" />
            </h2>
            <h2 align="center">
               <xsl:value-of
                  select="concat('Environment: ', $env, ' - Domain: ', /datapower-configuration/export-details/domain, ' - Service: ', $service, ' (', $class, ')')" />
            </h2>
            <h3 align="center">
               <xsl:value-of select="datapower-configuration/export-details/description" />
            </h3>
         </head>
         <body>
            <xsl:apply-templates select="//MultiProtocolGateway" />
            <xsl:apply-templates select="//WSGateway" />
            <xsl:apply-templates select="//WebAppFW" />
            <xsl:apply-templates select="//HTTPService" />
            <xsl:apply-templates select="//configuration/StylePolicy" />
            <xsl:apply-templates select="//configuration/WSStylePolicy" />
            <xsl:apply-templates select="//configuration/XPathRoutingMap" />
            <xsl:apply-templates select="//configuration/TCPProxyService" />
            <hr/>
            <p/>
            <xsl:apply-templates select="//configuration/AppSecurityPolicy" />
            <xsl:apply-templates select="//configuration/WSEndpointRewritePolicy" />
            <hr/>
            <p align="center">Serivce Files</p>
            <xsl:apply-templates select="//files" />
         </body>
      </html>
   </xsl:template>

   <!-- Service Diagrams -->
   <!-- **************** -->
   
   <!-- TCP Proxy -->
   <xsl:template match="TCPProxyService">

      <xsl:variable name="shapeSY">
         <xsl:choose>
            <xsl:when test="$countF * $shapeFH + ($countF - 1) * $deltaFY >= 150">
               <xsl:number value="$startY + ($countF * $shapeFH + ($countF - 1) * $deltaFY - 150) div 2" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:number value="$startY" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="conX" select="$shapeSX + $shapeSW" />
      <xsl:variable name="conY" select="$shapeSY + $shapeSH div 2" />

      <xsl:variable name="dArrow" select="6" />

      <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="100%" height="768">

         <desc>DataPower Service Diagram</desc>

         <defs>
            <marker id="markerSquare" markerWidth="7" markerHeight="7" refx="4" refy="4" orient="auto">
               <rect x="1" y="1" width="4" height="4" style="stroke: none; fill:#000000;" />
            </marker>

            <marker id="markerArrow" markerWidth="12" markerHeight="12" refx="2" refy="7" orient="auto">
               <path d="M2,2 L2,13 L8,7 L2,2" style="fill: #000000;" />
            </marker>

            <linearGradient id="sg1" x1="0%" y1="0%" x2="100%" y2="100%">
               <stop stop-color="{$colorService}" offset="0%" />
               <stop stop-color="white" offset="50%" />
               <stop stop-color="{$colorService}" offset="100%" />
            </linearGradient>

            <filter id="dS" width="120%" height="120%">
               <feOffset result="offOut" in="SourceGraphic" dx="5" dy="5" />
               <feGaussianBlur result="blurOut" in="offOut" stdDeviation="10" />
               <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
            </filter>

            <filter id="i1">
               <feDiffuseLighting result="diffOut" in="SourceGraphic" diffuseConstant="1" lighting-color="white">
                  <feDistantLight azimuth="45" elevation="45" />
               </feDiffuseLighting>
               <feComposite in="SourceGraphic" in2="diffOut" operator="arithmetic" k1="1" k2="0" k3="0" k4="0" />
            </filter>

         </defs>

         <rect x="{$shapeSX}" y="{$shapeSY}" width="{$shapeSW}" height="{$shapeSH}" rx="{$shapeR}" ry="{$shapeR}" fill="url(#sg1)"
            style="stroke: #000000;" />

         <foreignObject x="{$shapeSX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeSW - 10}" height="{$shapeSH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 14; font-weight: bold;">
               <xsl:value-of select="concat('Service: ', @name)" />
               <br />
            </div>
            <div class="summary" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 10;">
               <xsl:value-of select="concat('Summary: ', ./UserSummary)" />
               <br />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Local Address: ', ./LocalAddress, ':', ./LocalPort)" />
               <br />
               <xsl:value-of select="concat('Remote Address: ', ./RemoteAddres, ':', ./RemotePort)" />
            </div>
         </foreignObject>

         <line x1="{$conX}" y1="{$conY}" x2="{$conX + $conL - $dArrow}" y2="{$conY}"
            style="stroke: #333333; marker-start: url(#markerSquare); marker-end: url(#markerArrow);" />
         <text x="{$conX + 20}" y="{$conY + 20}" style="font-family: Arial; font-size: 8;">
            <xsl:value-of select="concat(./ResponseType,' / ', substring-before(./BackendUrl,'://'))" />
         </text>

         <xsl:if test="count(./SSLProxy) > 0">
            <xsl:apply-templates select="$vMainDoc//SSLProxyProfile[@name = string(./SSLProxy)]">
               <xsl:with-param name="startposX" select="$conX" />
               <xsl:with-param name="color" select="$colorBackend" />
            </xsl:apply-templates>
         </xsl:if>

         <xsl:variable name="shapeBY" select="$shapeSY + ($shapeSH - $shapeBH) div 2" />
         <rect x="{$shapeBX}" y="{$shapeBY}" width="{$shapeBW}" height="{$shapeBH}" rx="{$shapeR}" ry="{$shapeR}"
            style="stroke: #000000; fill: {$colorBackend};" filter="url(#dS)" />
         <foreignObject x="{$shapeBX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeBW - 10}" height="{$shapeBH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Backend Host: ', ./RemoteAddress, ':', ./RemotePort)" />
            </div>
         </foreignObject>

      </svg>

   </xsl:template>
   
   <!-- HTTP Service -->
   <xsl:template match="HTTPService">

      <xsl:variable name="shapeSY">
         <xsl:choose>
            <xsl:when test="$countF * $shapeFH + ($countF - 1) * $deltaFY >= 150">
               <xsl:number value="$startY + ($countF * $shapeFH + ($countF - 1) * $deltaFY - 150) div 2" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:number value="$startY" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="conX" select="$shapeSX + $shapeSW" />
      <xsl:variable name="conY" select="$shapeSY + $shapeSH div 2" />

      <xsl:variable name="dArrow" select="6" />

      <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="100%" height="768">

         <desc>DataPower Service Diagram</desc>

         <defs>

            <linearGradient id="sg1" x1="0%" y1="0%" x2="100%" y2="100%">
               <stop stop-color="{$colorService}" offset="0%" />
               <stop stop-color="white" offset="50%" />
               <stop stop-color="{$colorService}" offset="100%" />
            </linearGradient>

            <filter id="i1">
               <feDiffuseLighting result="diffOut" in="SourceGraphic" diffuseConstant="1" lighting-color="white">
                  <feDistantLight azimuth="45" elevation="45" />
               </feDiffuseLighting>
               <feComposite in="SourceGraphic" in2="diffOut" operator="arithmetic" k1="1" k2="0" k3="0" k4="0" />
            </filter>

         </defs>

         <rect x="{$shapeSX}" y="{$shapeSY}" width="{$shapeSW}" height="{$shapeSH}" rx="{$shapeR}" ry="{$shapeR}" fill="url(#sg1)"
            style="stroke: #000000;" />

         <foreignObject x="{$shapeSX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeSW - 10}" height="{$shapeSH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 14; font-weight: bold;">
               <xsl:value-of select="concat('Service: ', @name)" />
               <br />
            </div>
            <div class="summary" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 10;">
               <xsl:value-of select="concat('Summary: ', ./UserSummary)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Local Address: ', ./LocalAddress)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Local Port: ', ./LocalPort)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Base Dir: ', ./BaseDir)" />
            </div>
         </foreignObject>

      </svg>

   </xsl:template>
   
   <!-- MPGW / WSP -->
   <xsl:template match="MultiProtocolGateway|WSGateway">
      <xsl:variable name="vPolicyName" select="./StylePolicy[@class='StylePolicy']" />

      <xsl:variable name="shapeSY">
         <xsl:choose>
            <xsl:when test="$countF * $shapeFH + ($countF - 1) * $deltaFY >= 150">
               <xsl:number value="$startY + ($countF * $shapeFH + ($countF - 1) * $deltaFY - 150) div 2" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:number value="$startY" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="conX" select="$shapeSX + $shapeSW" />
      <xsl:variable name="conY" select="$shapeSY + $shapeSH div 2" />

      <xsl:variable name="dArrow" select="6" />

      <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="100%" height="768">

         <desc>DataPower Service Diagram</desc>

         <defs>
            <marker id="markerSquare" markerWidth="7" markerHeight="7" refx="4" refy="4" orient="auto">
               <rect x="1" y="1" width="4" height="4" style="stroke: none; fill:#000000;" />
            </marker>

            <marker id="markerArrow" markerWidth="12" markerHeight="12" refx="2" refy="7" orient="auto">
               <path d="M2,2 L2,13 L8,7 L2,2" style="fill: #000000;" />
            </marker>

            <linearGradient id="sg1" x1="0%" y1="0%" x2="100%" y2="100%">
               <stop stop-color="{$colorService}" offset="0%" />
               <stop stop-color="white" offset="50%" />
               <stop stop-color="{$colorService}" offset="100%" />
            </linearGradient>

            <filter id="dS" width="120%" height="120%">
               <feOffset result="offOut" in="SourceGraphic" dx="5" dy="5" />
               <feGaussianBlur result="blurOut" in="offOut" stdDeviation="10" />
               <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
            </filter>

            <filter id="i1">
               <feDiffuseLighting result="diffOut" in="SourceGraphic" diffuseConstant="1" lighting-color="white">
                  <feDistantLight azimuth="45" elevation="45" />
               </feDiffuseLighting>
               <feComposite in="SourceGraphic" in2="diffOut" operator="arithmetic" k1="1" k2="0" k3="0" k4="0" />
            </filter>

         </defs>


         <xsl:apply-templates select="//FrontProtocol[not(. = ../following-sibling::*/FrontProtocol)]" />

         <rect x="{$shapeSX}" y="{$shapeSY}" width="{$shapeSW}" height="{$shapeSH}" rx="{$shapeR}" ry="{$shapeR}" fill="url(#sg1)"
            style="stroke: #000000;" />

         <foreignObject x="{$shapeSX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeSW - 10}" height="{$shapeSH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 14; font-weight: bold;">
               <xsl:value-of select="concat('Service: ', @name)" />
               <br />
            </div>
            <div class="summary" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 10;">
               <xsl:value-of select="concat('Summary: ', ./UserSummary)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Request: ', ./RequestType)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Response: ', ./ResponseType)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Policy: ', ./StylePolicy)" />
            </div>
         </foreignObject>

         <line x1="{$conX}" y1="{$conY}" x2="{$conX + $conL - $dArrow}" y2="{$conY}"
            style="stroke: #333333; marker-start: url(#markerSquare); marker-end: url(#markerArrow);" />
         <text x="{$conX + 20}" y="{$conY + 20}" style="font-family: Arial; font-size: 8;">
            <xsl:value-of select="concat(./ResponseType,' / ', substring-before(./BackendUrl,'://'))" />
         </text>

         <xsl:if test="count(SSLProxy[@class='SSLProxyProfile']) > 0">
            <xsl:variable name="clientprofile" select="SSLProxy[@class='SSLProxyProfile']/text()" />
            <xsl:message>
               <xsl:value-of select="concat('SSL Client Profile: ', $clientprofile)" />
            </xsl:message>
            <xsl:apply-templates select="$vMainDoc//SSLProxyProfile[@name = $clientprofile]">
               <xsl:with-param name="startposX" select="$conX" />
               <xsl:with-param name="color" select="$colorBackend" />
            </xsl:apply-templates>
         </xsl:if>
         
         <xsl:if test="count(SSLClient[@class='SSLClientProfile']) > 0">
            <xsl:variable name="clientprofile" select="SSLClient[@class='SSLClientProfile']/text()" />
            <xsl:message>
               <xsl:value-of select="concat('SSL Client Profile: ', $clientprofile)" />
            </xsl:message>
            <xsl:apply-templates select="$vMainDoc//SSLClientProfile[@name = $clientprofile]">
               <xsl:with-param name="startposX" select="$conX" />
               <xsl:with-param name="color" select="$colorBackend" />
            </xsl:apply-templates>
         </xsl:if>

         <xsl:variable name="shapeBY" select="$shapeSY + ($shapeSH - $shapeBH) div 2" />
         <rect x="{$shapeBX}" y="{$shapeBY}" width="{$shapeBW}" height="{$shapeBH}" rx="{$shapeR}" ry="{$shapeR}"
            style="stroke: #000000; fill: {$colorBackend};" filter="url(#dS)" />
         <foreignObject x="{$shapeBX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeBW - 10}" height="{$shapeBH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat(substring-before(./Type,'-'), ' Backend: ', ./BackendUrl)" />
            </div>
         </foreignObject>

      </svg>

   </xsl:template>
   
   <!-- WAF -->
   <xsl:template match="WebAppFW">
      <xsl:variable name="vPolicyName" select="./StylePolicy[@class='StylePolicy']" />

      <xsl:variable name="shapeSY">
         <xsl:choose>
            <xsl:when test="$countF * $shapeFH + ($countF - 1) * $deltaFY >= 150">
               <xsl:number value="$startY + ($countF * $shapeFH + ($countF - 1) * $deltaFY - 150) div 2" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:number value="$startY" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="conX" select="$shapeSX + $shapeSW" />
      <xsl:variable name="conY" select="$shapeSY + $shapeSH div 2" />

      <xsl:variable name="dArrow" select="6" />

      <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="100%" height="768">

         <desc>DataPower Service Diagram</desc>

         <defs>
            <marker id="markerSquare" markerWidth="7" markerHeight="7" refx="4" refy="4" orient="auto">
               <rect x="1" y="1" width="4" height="4" style="stroke: none; fill:#000000;" />
            </marker>

            <marker id="markerArrow" markerWidth="12" markerHeight="12" refx="2" refy="7" orient="auto">
               <path d="M2,2 L2,13 L8,7 L2,2" style="fill: #000000;" />
            </marker>

            <linearGradient id="sg1" x1="0%" y1="0%" x2="100%" y2="100%">
               <stop stop-color="{$colorService}" offset="0%" />
               <stop stop-color="white" offset="50%" />
               <stop stop-color="{$colorService}" offset="100%" />
            </linearGradient>

            <filter id="dS" width="120%" height="120%">
               <feOffset result="offOut" in="SourceGraphic" dx="5" dy="5" />
               <feGaussianBlur result="blurOut" in="offOut" stdDeviation="10" />
               <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
            </filter>

            <filter id="i1">
               <feDiffuseLighting result="diffOut" in="SourceGraphic" diffuseConstant="1" lighting-color="white">
                  <feDistantLight azimuth="45" elevation="45" />
               </feDiffuseLighting>
               <feComposite in="SourceGraphic" in2="diffOut" operator="arithmetic" k1="1" k2="0" k3="0" k4="0" />
            </filter>

         </defs>

         <xsl:apply-templates select="//FrontSide[not(. = ../following-sibling::*/FrontSide)]" />

         <rect x="{$shapeSX}" y="{$shapeSY}" width="{$shapeSW}" height="{$shapeSH}" rx="{$shapeR}" ry="{$shapeR}" fill="url(#sg1)"
            style="stroke: #000000;" />

         <foreignObject x="{$shapeSX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeSW - 10}" height="{$shapeSH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 14; font-weight: bold;">
               <xsl:value-of select="concat('Service: ', @name)" />
               <br />
            </div>
            <div class="summary" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 10;">
               <xsl:value-of select="concat('Summary: ', ./UserSummary)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('XML Manager ', ./XMLManager)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Policy: ', ./StylePolicy)" />
            </div>
            <div class="details" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat('Error Policy: ', ./ErrorPolicy)" />
            </div>
         </foreignObject>

         <line x1="{$conX}" y1="{$conY}" x2="{$conX + $conL - $dArrow}" y2="{$conY}"
            style="stroke: #333333; marker-start: url(#markerSquare); marker-end: url(#markerArrow);" />
         <text x="{$conX + 20}" y="{$conY + 20}" style="font-family: Arial; font-size: 8;">
            <xsl:value-of select="concat(./ResponseType,' / ', substring-before(./BackendUrl,'://'))" />
         </text>

         <xsl:if test="count(SSLProxy[@class='SSLProxyProfile']) > 0">
            <xsl:variable name="clientprofile" select="SSLProxy[@class='SSLProxyProfile']/text()" />
            <xsl:message>
               <xsl:value-of select="concat('SSL Client Profile: ', $clientprofile)" />
            </xsl:message>
            <xsl:apply-templates select="$vMainDoc//SSLProxyProfile[@name = $clientprofile]">
               <xsl:with-param name="startposX" select="$conX" />
               <xsl:with-param name="color" select="$colorBackend" />
            </xsl:apply-templates>
         </xsl:if>
         
         <xsl:if test="count(SSLClient[@class='SSLClientProfile']) > 0">
            <xsl:variable name="clientprofile" select="SSLClient[@class='SSLClientProfile']/text()" />
            <xsl:message>
               <xsl:value-of select="concat('SSL Client Profile: ', $clientprofile)" />
            </xsl:message>
            <xsl:apply-templates select="$vMainDoc//SSLClientProfile[@name = $clientprofile]">
               <xsl:with-param name="startposX" select="$conX" />
               <xsl:with-param name="color" select="$colorBackend" />
            </xsl:apply-templates>
         </xsl:if>

         <xsl:variable name="shapeBY" select="$shapeSY + ($shapeSH - $shapeBH) div 2" />
         <rect x="{$shapeBX}" y="{$shapeBY}" width="{$shapeBW}" height="{$shapeBH}" rx="{$shapeR}" ry="{$shapeR}"
            style="stroke: #000000; fill: {$colorBackend};" filter="url(#dS)" />
         <foreignObject x="{$shapeBX + $shapeIX}" y="{$shapeSY + $shapeIY}" width="{$shapeBW - 10}" height="{$shapeBH -10}"
            style="font-family: Arial; font-size: 12;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat(substring-before(./Type,'-'), ' Backend: ', ./RemoteAddress, ':', ./RemotePort)" />
            </div>
         </foreignObject>

      </svg>

   </xsl:template>

   <!-- HTTP(S) Front Side Handlers -->
   <xsl:template match="FrontProtocol">
      <xsl:variable name="offsetFY">
         <xsl:choose>
            <xsl:when test="$countF * $shapeFH + ($countF - 1) * $deltaFY >= 150">
               <xsl:number value="0" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:number value="(150 - ($countF * $shapeFH + ($countF - 1) * $deltaFY)) div 2" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="shapeFY">
         <xsl:number value="$startY + $offsetFY + (position() - 1) * ($shapeFH + $deltaFY)" format="1" />
      </xsl:variable>

      <xsl:variable name="conX" select="$shapeFX + $shapeFW" />
      <xsl:variable name="conY" select="$shapeFY + $shapeFH div 2" />
      <xsl:variable name="conY2" select="$startY + $offsetFY + ($countF * $shapeFH + ($countF - 1) * $deltaFY) div 2" />

      <xsl:variable name="fshname" select="text()" />
      <xsl:variable name="fshtype" select="@class" />
      <xsl:variable name="fshnode" select="$vMainDoc//*[name() = $fshtype and @name = $fshname]" />

      <rect x="{$shapeFX}" y="{$shapeFY}" width="{$shapeFW}" height="{$shapeFH}" style="stroke: #000000; fill: {$colorFrontside};" />
      <line x1="{$conX}" y1="{$conY}" x2="{$conX + $conL}" y2="{$conY2}" style="stroke: #333333;" />

      <switch>
         <foreignObject x="{$shapeFX + $shapeIX}" y="{$shapeFY}" width="{$shapeFW - $shapeIX * 2}" height="{$shapeFH}"
            style="font-family: Arial; font-size: 10; text-anchor: start; dominant-baseline: hanging;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 12; font-weight: bold;">
               <xsl:value-of select="concat(position(), '. FSH: ', text())" />
            </div>
            <div class="detail" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of
                  select="concat(substring-before(@class,'SourceProtocolHandler'), ' - ', $fshnode/LocalAddress, ':', $fshnode/LocalPort)" />
            </div>
            <xsl:if test="$fshtype = 'HTTPSSourceProtocolHandler'">
               <div class="detail" xmlns="http://www.w3.org/1999/xhtml">
                  <xsl:choose>
                    <xsl:when test="count($fshnode/SSLProxy) > 0">
                        <xsl:value-of select="concat('SSL: ', $fshnode/SSLProxy)" />
                    </xsl:when>
                    <xsl:when test="count($fshnode/SSLServer) > 0">
                        <xsl:value-of select="concat('SSL: ', $fshnode/SSLServer)" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat('SSL: ', 'not found!')" />
                    </xsl:otherwise>
                  </xsl:choose>
               </div>
            </xsl:if>
         </foreignObject>
         <text x="{$shapeFX + $shapeIX}" y="{$shapeFY + $shapeIY}" style="font-family: Arial; font-size: 10;">
            <xsl:value-of select="concat('FSH: ', text())" />
         </text>
      </switch>

      <xsl:if test="$fshtype = 'HTTPSSourceProtocolHandler'">
         <xsl:apply-templates select="$vMainDoc//SSLProxyProfile[@name = $fshnode/SSLProxy]">
            <xsl:with-param name="startposX" select="$shapeFX" />
            <xsl:with-param name="color" select="$colorFrontside" />
         </xsl:apply-templates>
      </xsl:if>

   </xsl:template>
   
   <!-- HTTP(S) WAF Front Side Handlers -->
   <xsl:template match="FrontSide">
      <xsl:variable name="offsetFY">
         <xsl:choose>
            <xsl:when test="$countF * $shapeFH + ($countF - 1) * $deltaFY >= 150">
               <xsl:number value="0" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:number value="(150 - ($countF * $shapeFH + ($countF - 1) * $deltaFY)) div 2" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="shapeFY">
         <xsl:number value="$startY + $offsetFY + (position() - 1) * ($shapeFH + $deltaFY)" format="1" />
      </xsl:variable>

      <xsl:variable name="conX" select="$shapeFX + $shapeFW" />
      <xsl:variable name="conY" select="$shapeFY + $shapeFH div 2" />
      <xsl:variable name="conY2" select="$startY + $offsetFY + ($countF * $shapeFH + ($countF - 1) * $deltaFY) div 2" />

      <!--  xsl:variable name="fshname" select="./prceding-sibling  text()" / -->
      <xsl:variable name="fshtype" select="@class" />
      <xsl:variable name="fshnode" select="$vMainDoc//*[name() = $fshtype and @name = $fshname]" />

      <rect x="{$shapeFX}" y="{$shapeFY}" width="{$shapeFW}" height="{$shapeFH}" style="stroke: #000000; fill: {$colorFrontside};" />
      <line x1="{$conX}" y1="{$conY}" x2="{$conX + $conL}" y2="{$conY2}" style="stroke: #333333;" />

      <switch>
         <foreignObject x="{$shapeFX + $shapeIX}" y="{$shapeFY}" width="{$shapeFW - $shapeIX * 2}" height="{$shapeFH}"
            style="font-family: Arial; font-size: 10; text-anchor: start; dominant-baseline: hanging;">
            <div class="title" xmlns="http://www.w3.org/1999/xhtml" style="font-size: 12; font-weight: bold;">
               <xsl:value-of select="concat(position(), '. FSH: ', text())" />
            </div>
            <div class="detail" xmlns="http://www.w3.org/1999/xhtml">
               <xsl:value-of select="concat(substring-before(@class,'SourceProtocolHandler'), ' - ', $fshnode/LocalAddress, ':', $fshnode/LocalPort)" />
            </div>
            <xsl:if test="$fshtype = 'HTTPSSourceProtocolHandler'">
               <div class="detail" xmlns="http://www.w3.org/1999/xhtml">
                  <xsl:choose>
                    <xsl:when test="count($fshnode/SSLProxy) > 0">
                        <xsl:value-of select="concat('SSL: ', $fshnode/SSLProxy)" />
                    </xsl:when>
                    <xsl:when test="count($fshnode/SSLServer) > 0">
                        <xsl:value-of select="concat('SSL: ', $fshnode/SSLServer)" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat('SSL: ', 'not found!')" />
                    </xsl:otherwise>
                  </xsl:choose>
               </div>
            </xsl:if>
         </foreignObject>
         <text x="{$shapeFX + $shapeIX}" y="{$shapeFY + $shapeIY}" style="font-family: Arial; font-size: 10;">
            <xsl:value-of select="concat('FSH: ', text())" />
         </text>
      </switch>

      <xsl:if test="$fshtype = 'HTTPSSourceProtocolHandler'">
         <xsl:apply-templates select="$vMainDoc//SSLProxyProfile[@name = $fshnode/SSLProxy]">
            <xsl:with-param name="startposX" select="$shapeFX" />
            <xsl:with-param name="color" select="$colorFrontside" />
         </xsl:apply-templates>
         <xsl:apply-templates select="$vMainDoc//SSLServerProfile[@name = $fshnode/SSLServer]">
            <xsl:with-param name="startposX" select="$shapeFX" />
            <xsl:with-param name="color" select="$colorFrontside" />
         </xsl:apply-templates>
         <xsl:apply-templates select="$vMainDoc//SSLSNIServerProfile[@name = $fshnode/SSLServer]">
            <xsl:with-param name="startposX" select="$shapeFX" />
            <xsl:with-param name="color" select="$colorFrontside" />
         </xsl:apply-templates>
      </xsl:if>

   </xsl:template>

   <xsl:template match="SSLProxyProfile">
      <xsl:param name="color" />
      <xsl:param name="startposX" />

      <!-- <xsl:variable name="ypos"> <xsl:number value="330 + (position() - 1) * 60" format="1" /> </xsl:variable> <xsl:variable name="shapePX"> 
         <xsl:number value="50 + (position() - 1) * 400" format="1" /> </xsl:variable> -->

      <!-- SSL Profile -->
      <xsl:variable name="shapePW" select="250" />
      <xsl:variable name="shapePH" select="200" />
      <xsl:variable name="shapePX" select="$startposX" />
      <xsl:variable name="shapePY" select="$startY + $countF * $shapeFH + ($countF - 1) * $deltaFY + 50" />

      <xsl:variable name="shapeIY" select="10" />

      <xsl:variable name="deltaX" select="3" />
      <xsl:variable name="deltaY" select="10" />
      <xsl:variable name="deltaCredY" select="10" />

      <!-- Server Crypto Profile -->
      <xsl:variable name="shapeCPX" select="$shapePX + $deltaX" />
      <xsl:variable name="shapeCPH" select="($shapePH - 6 * $deltaY) div 2" />
      <xsl:variable name="shapeCPW" select="$shapePW - $deltaX * 2" />

      <xsl:variable name="shapeSCPY" select="$shapePY + 4 * $deltaY" />
      <xsl:variable name="shapeCCPY" select="$shapePY + 5 * $deltaY + $shapeCPH" />


      <xsl:variable name="shapeCredX" select="$shapeCPX + $deltaX" />
      <xsl:variable name="shapeCredH" select="($shapeCPH - 4 * $deltaCredY) div 2" />
      <xsl:variable name="shapeCredW" select="$shapeCPW - 2 * $deltaX" />

      <xsl:variable name="sslname" select="@name" />
      <xsl:variable name="ssldirection" select="./Direction" />
      <xsl:variable name="sslreverse" select="./ReverseCryptoProfile" />
      <xsl:variable name="sslforward" select="./ForwardCryptoProfile" />

      <xsl:variable name="cryptoreverse" select="$vMainDoc//CryptoProfile[@name = $sslreverse]" />
      <xsl:variable name="cryptoforward" select="$vMainDoc//CryptoProfile[@name = $sslforward]" />

      <!-- SSL Proxy Profile -->
      <rect x="{$shapePX}" y="{$shapePY}" width="{$shapePW}" height="{$shapePH}" style="stroke: #000000; fill: {$color};" />
      <text x="{$shapePX + $shapeIX}" y="{$shapePY + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <tspan x="{$shapePX + $shapeIX}">
            <xsl:value-of select="concat(position(), ' SSL Profile: ', $sslname)" />
         </tspan>
         <tspan x="{$shapePX + $shapeIX}" dy="10">
            <xsl:value-of select="concat('ClientAuth optional: ', ./ClientAuthOptional)" />
         </tspan>
         <tspan x="{$shapePX + $shapeIX}" dy="10">
            <xsl:value-of select="concat('ClientAuth always: ', ./ClientAuthAlwaysRequest)" />
         </tspan>

      </text>

      <!-- Server/Reverse Crypto Profile -->
      <rect x="{$shapeCPX}" y="{$shapeSCPY}" width="{$shapeCPW}" height="{$shapeCPH}" style="stroke: #000000; fill: #00ffaa;" />
      <text x="{$shapeCPX + $shapeIX}" y="{$shapeSCPY + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <xsl:value-of select="concat('Server Profile: ', $sslreverse/text())" />
      </text>
      <!-- Identification Credentials -->
      <rect x="{$shapeCredX}" y="{$shapeSCPY + 2 * $deltaCredY}" width="{$shapeCredW}" height="{$shapeCredH}" style="stroke: #000000; fill: #00ffaa;" />
      <text x="{$shapeCredX + $shapeIX}" y="{$shapeSCPY + 2 * $deltaCredY + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <xsl:value-of select="concat('IdCred: ', $cryptoreverse/IdentCredential)" />
      </text>

      <!-- Validation Credentials -->
      <rect x="{$shapeCredX}" y="{$shapeSCPY + 3 * $deltaCredY + $shapeCredH}" width="{$shapeCredW}" height="{$shapeCredH}"
         style="stroke: #000000; fill: #00ffaa;" />
      <text x="{$shapeCredX + $shapeIX}" y="{$shapeSCPY + 3 * $deltaCredY + $shapeCredH + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <xsl:value-of select="concat('ValCred: ', $cryptoreverse/ValCredential)" />
      </text>


      <!-- Client/Forward Crypto Profile -->
      <rect x="{$shapeCPX}" y="{$shapeCCPY}" width="{$shapeCPW}" height="{$shapeCPH}" style="stroke: #000000; fill: #00ffaa;" />
      <text x="{$shapeCPX + $shapeIX}" y="{$shapeCCPY + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <xsl:value-of select="concat('Client Profile: ', $sslforward/text())" />
      </text>
      <!-- Credentials -->

      <!-- Identification Credentials -->
      <rect x="{$shapeCredX}" y="{$shapeCCPY + 2 * $deltaCredY}" width="{$shapeCredW}" height="{$shapeCredH}" style="stroke: #000000; fill: #00ffaa;" />
      <text x="{$shapeCredX + $shapeIX}" y="{$shapeCCPY + 2 * $deltaCredY + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <xsl:value-of select="concat('IdCred: ', $cryptoforward/IdentCredential)" />
      </text>

      <!-- Validation Credentials -->
      <rect x="{$shapeCredX}" y="{$shapeCCPY + 3 * $deltaCredY + $shapeCredH}" width="{$shapeCredW}" height="{$shapeCredH}"
         style="stroke: #000000; fill: #00ffaa;" />
      <text x="{$shapeCredX + $shapeIX}" y="{$shapeCCPY + 3 * $deltaCredY + $shapeCredH + $shapeIY}" style="font-family: Arial; font-size: 10;">
         <xsl:value-of select="concat('ValCred: ', $cryptoforward/ValCredential)" />
      </text>

   </xsl:template>



   <!-- Service Policy -->
   <xsl:template match="StylePolicy|WSStylePolicy">
      <table border="2" align="center" width="80%" style="font-family: sans-serif; background-color: #dedfff;">
         <tr>
            <td colspan="2" align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
               Style Policy:
               <xsl:value-of select="@name" />
            </td>
         </tr>
         <xsl:apply-templates select="PolicyMaps" />
      </table>
   </xsl:template>
   
   <xsl:template match="AppSecurityPolicy">
      <table border="2" align="center" width="80%" style="font-family: sans-serif; background-color: #dedfff;">
         <tr>
            <td colspan="2" align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
               WAF Security Policy:
               <xsl:value-of select="@name" />
            </td>
         </tr>
         <xsl:apply-templates select="RequestMaps" />
         <xsl:apply-templates select="ResponseMaps" />
         <xsl:apply-templates select="ErrorMaps" />
      </table>
   </xsl:template>

   <!-- Policy Maps -->
   <xsl:template match="PolicyMaps">
      <xsl:variable name="Match" select="Match/text()" />
      <xsl:variable name="Rule" select="Rule/text()" />
      <xsl:variable name="StylePolicy" select="//StylePolicy//Rule[.=$Rule]" />
      <xsl:variable name="MatchXPath" select="//Matching[@name=$Match]/MatchRules/XPATHExpression" />
      <xsl:variable name="RuleDirection" select="//StylePolicyRule[@name=$Rule]/Direction" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position(), ' ', $RuleDirection)" />
         </td>
         <td>
            <pre>
               <table border="1" bgcolor="lightblue" width="80%">
                  <tbody>
                     <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Rule: ', $Rule)" />
                        </td>
                     </tr>
                     <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Match(', $Match,')')" />
                        </td>
                     </tr>
                     <xsl:apply-templates select="Rule" />
                  </tbody>
               </table>
            </pre>
         </td>
      </tr>
   </xsl:template>
   
   <!-- WAF Request Maps -->
   <xsl:template match="RequestMaps">
      <xsl:variable name="Match" select="Match/text()" />
      <xsl:variable name="Rule" select="Rule/text()" />
      <xsl:variable name="WebAppRequest" select="//WebAppRequest[@name=$Rule]" />
      <xsl:variable name="MatchXPath" select="//Matching[@name=$Match]/MatchRules/XPATHExpression" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position(), ' request-rule')" />
         </td>
         <td>
            <pre>
               <table border="1" bgcolor="lightblue" width="80%">
                  <tbody>
                      <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Match(', $Match,')')" />
                        </td>
                     </tr>
                     <xsl:for-each select="//Matching[@name=$Match]/*">
                     <tr>
                        <td align="left" colspan="5" bgcolor="lightgreen">
                           <xsl:call-template name="list-properties" />
                        </td>
                     </tr>
                     </xsl:for-each>
                     <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Rule: ', $Rule)" />
                        </td>
                     </tr>
                     <xsl:for-each select="//WebAppRequest[@name=$Rule]/*">
                     <tr>
                        <td align="left" colspan="5" bgcolor="lightgreen">
                           <xsl:call-template name="list-properties" />
                        </td>
                     </tr>
                     </xsl:for-each>
                  </tbody>
               </table>
            </pre>
         </td>
      </tr>
   </xsl:template>
   
   
   <!-- WAF Response Maps -->
   <xsl:template match="ResponseMaps">
      <xsl:variable name="Match" select="Match/text()" />
      <xsl:variable name="Rule" select="Rule/text()" />
      <xsl:variable name="WebAppResponse" select="//WebAppResponse[@name=$Rule]" />
      <xsl:variable name="MatchXPath" select="//Matching[@name=$Match]/MatchRules/XPATHExpression" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position(), ' response-rule')" />
         </td>
         <td>
            <pre>
               <table border="1" bgcolor="lightblue" width="80%">
                  <tbody>
                      <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Match(', $Match,')')" />
                        </td>
                     </tr>
                     <xsl:for-each select="//Matching[@name=$Match]/*">
                     <tr>
                        <td align="left" colspan="5" bgcolor="lightgreen">
                           <xsl:call-template name="list-properties" />
                        </td>
                     </tr>
                     </xsl:for-each>
                     <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Rule: ', $Rule)" />
                        </td>
                     </tr>
                     <xsl:for-each select="//WebAppResponse[@name=$Rule]/*">
                     <tr>
                        <td align="left" colspan="5" bgcolor="lightgreen">
                           <xsl:call-template name="list-properties" />
                        </td>
                     </tr>
                     </xsl:for-each>
                  </tbody>
               </table>
            </pre>
         </td>
      </tr>
   </xsl:template>
   
   
   
   
   <!-- WAF Error Maps -->
   <xsl:template match="ErrorMaps">
      <xsl:variable name="Match" select="Match/text()" />
      <xsl:variable name="Rule" select="Rule/text()" />
      <xsl:variable name="StylePolicy" select="//StylePolicy//Rule[.=$Rule]" />
      <xsl:variable name="MatchXPath" select="//Matching[@name=$Match]/MatchRules/XPATHExpression" />
      <xsl:variable name="RuleDirection" select="//StylePolicyRule[@name=$Rule]/Direction" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position(), ' ', $RuleDirection)" />
         </td>
         <td>
            <pre>
               <table border="1" bgcolor="lightblue" width="80%">
                  <tbody>
                     <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Rule: ', $Rule)" />
                        </td>
                     </tr>
                     <tr>
                        <td align="center" colspan="5">
                           <xsl:value-of select="concat('Match(', $Match,')')" />
                        </td>
                     </tr>
                     <xsl:apply-templates select="Rule" />
                  </tbody>
               </table>
            </pre>
         </td>
      </tr>
   </xsl:template>

   <!-- Policy Rules -->
   <xsl:template match="Rule">
      <xsl:variable name="Rule" select="text()" />
      <br />
      <xsl:apply-templates select="//StylePolicyRule[@name=$Rule]|//WSStylePolicyRule[@name=$Rule]" />
   </xsl:template>
   <!-- -->
   <xsl:template match="StylePolicyRule|WSStylePolicyRule">
      <tr>
         <th>Type</th>
         <th>Input</th>
         <th>Output</th>
         <th>Artifact</th>
      </tr>
      <xsl:apply-templates select="Actions" />
   </xsl:template>

   <!-- Policy Actions -->
   <xsl:template match="Actions">
      <xsl:variable name="StylePolicyAction" select="text()" />
      <xsl:apply-templates select="//StylePolicyAction[@name=$StylePolicyAction]" />
   </xsl:template>
   <!-- -->
   <xsl:template match="StylePolicyAction">
      <xsl:variable name="name" select="@name" />
      <xsl:variable name="bgColor">
         <xsl:choose>
            <xsl:when test="//ConditionAction[text()=$name]">
               <xsl:value-of select="lightgreen" />
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="lightblue" />
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="Type" select="Type/text()" />
      <xsl:variable name="Input" select="Input/text()" />
      <xsl:variable name="Output" select="Output/text()" />
      <xsl:variable name="artifcat">
         <xsl:choose>
            <xsl:when test="$Type='xform'">
               <xsl:value-of select="Transform" />
            </xsl:when>
            <xsl:when test="$Type='xformbin'">
               <xsl:choose>
                  <xsl:when test="TxMap">
                     <xsl:value-of select="TxMap" />
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="Transform" />
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:when>
            <xsl:when test="$Type='validate'">
               <xsl:value-of select="SchemaURL" />
            </xsl:when>
            <xsl:when test="$Type='on-error'">
               <xsl:value-of select="Rule" />
            </xsl:when>
            <xsl:when test="$Type='filter'">
               <xsl:value-of select="Transform" />
            </xsl:when>
            <xsl:when test="$Type='extract'">
               <xsl:choose>
                  <xsl:when test="Variable/text()">
                     <table border="1" bgcolor="lightblue">
                        <tbody>
                           <tr>
                              <td>
                                 <xsl:value-of select="concat('xPath(', XPath, ')')" />
                              </td>
                           </tr>
                           <tr bgcolor="lightgreen">
                              <td>
                                 <xsl:value-of select="concat(' Variable(', Variable, ')')" />
                              </td>
                           </tr>
                        </tbody>
                     </table>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="concat('xPath(', XPath, ')')" />
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:when>
            <xsl:when test="$Type='route-action'">
               <xsl:choose>
                  <xsl:when test="Transform">
                     <xsl:value-of select="Transform" />
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="concat(DynamicStylesheet, ' [', DynamicStylesheet/@class, ']')" />
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:when>
            <xsl:when test="$Type='fetch'">
               <xsl:value-of select="Destination" />
            </xsl:when>
         </xsl:choose>
      </xsl:variable>
      <xsl:element name="tr">
         <xsl:attribute name="bgcolor"><xsl:value-of select="$bgColor" /></xsl:attribute>
         <td>
            <xsl:value-of select="$Type" />
         </td>
         <td>
            <xsl:value-of select="$Input" />
         </td>
         <td>
            <xsl:choose>
               <xsl:when test="$Output">
                  <xsl:value-of select="$Output" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:if test="$Type='result'">
                     <xsl:value-of select="OutputType/text()" />
                  </xsl:if>
               </xsl:otherwise>
            </xsl:choose>
         </td>
         <td>
            <xsl:copy-of select="$artifcat" />
         </td>
      </xsl:element>
      <!-- -->
      <xsl:if test="$Type='conditional'">
         <xsl:for-each select="Condition">
            <tr>
               <td />
               <td colspan="3" align="center" bgcolor="lightgreen">
                  <xsl:value-of select="concat('when:(', Expression, ')')" />
               </td>
            </tr>
            <xsl:for-each select="ConditionAction">
               <xsl:variable name="StylePolicyAction" select="text()" />
               <xsl:apply-templates select="//StylePolicyAction[@name=$StylePolicyAction]" />
            </xsl:for-each>
         </xsl:for-each>
      </xsl:if>
      <!-- -->
   </xsl:template>

   <!-- XPath Routing Maps -->
   <xsl:template match="XPathRoutingMap">

      <table border="2" align="center" width="80%" style="font-family: sans-serif; background-color: #dedfff;">
         <tr>
            <td colspan="3" align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
               XPath Routing Map:
               <xsl:value-of select="@name" />
            </td>
         </tr>
         <tr>
            <td align="center" style="font-weight: bold; font-family: sans-serif;">
               XPath
            </td>
            <td align="center" style="font-weight: bold; font-family: sans-serif;">
               Host
            </td>
            <td align="center" style="font-weight: bold; font-family: sans-serif;">
               SSL
            </td>
         </tr>

         <xsl:apply-templates select="XPathRoutingRules" />

      </table>
   </xsl:template>

   <!-- XPath Routing Rules -->
   <xsl:template match="XPathRoutingRules">
      <tr>
         <td>
            <xsl:value-of select="XPath" />
         </td>
         <td>
            <xsl:value-of select="Host" />
            :
            <xsl:value-of select="Port" />
         </td>
         <td>
            <xsl:value-of select="SSL" />
         </td>
      </tr>
   </xsl:template>

   <!-- Web Service Endpoint Rewrite Policy -->
   <xsl:template match="WSEndpointRewritePolicy">
      <table border="2" align="center" width="80%" style="font-family: sans-serif; background-color: #dedfff;">
         <tr>
            <td align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
               Web Service Endpoint Rewrite Policy:<br/>
               <xsl:value-of select="@name" />
            </td>
         </tr>
         <tr>
            <td>
               <table border="1" align="center" width="100%" style="font-family: sans-serif; background-color: #dedfff;">
                  <tr>
                     <td colspan="2" align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
                        Local Endpoint Rewrite Rules (Frontside):
                     </td>
                  </tr>
                  <tbody>
                     <xsl:apply-templates select="WSEndpointLocalRewriteRule" />
                  </tbody>
               </table>
            </td>
         </tr>
         <tr>
            <td>
               <table border="1" align="center" width="100%" style="font-family: sans-serif; background-color: #dedfff;">
                  <tr>
                     <td colspan="2" align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
                        Remote Endpoint Rewrite Rules (Backside):
                     </td>
                  </tr>
                  <tbody>
                     <xsl:apply-templates select="WSEndpointRemoteRewriteRule" />
                  </tbody>
               </table>
            </td>
         </tr>
      </table>
   </xsl:template>

   <!-- Web Service Endpoint Local Rewrite Rule -->
   <!-- Policy Maps
      <WSEndpointLocalRewriteRule>
         <ServicePortMatchRegexp>^{http://vbs.admin.ch/pi/VBS/Bestellungen/RUAG}OrderChangeRUAG_AsyncOutPort$</ServicePortMatchRegexp> 
         <LocalEndpointProtocol>default</LocalEndpointProtocol> 
         <LocalEndpointHostname>0.0.0.0</LocalEndpointHostname> 
         <LocalEndpointPort>0</LocalEndpointPort> 
         <LocalEndpointURI>/services/zsn/ruag/orderchange</LocalEndpointURI> 
         <FrontProtocol class="HTTPSSourceProtocolHandler">wsext-sap-ruag</FrontProtocol> 
         <UseFrontProtocol>on</UseFrontProtocol> 
         <WSDLBindingProtocol>soap-11</WSDLBindingProtocol> 
         <FrontsidePortSuffix/> 
      </WSEndpointLocalRewriteRule> -->
   <xsl:template match="WSEndpointLocalRewriteRule">
      <xsl:variable name="URI" select="LocalEndpointURI/text()" />
      <xsl:variable name="useFSH" select="UseFrontProtocol/text()" />
      <xsl:variable name="FSHname" select="FrontProtocol/text()" />
      <xsl:variable name="FSHclass" select="FrontProtocol/@class" />
      <xsl:variable name="MatchRegexp" select="ServicePortMatchRegexp/text()" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position(), ' - ')" />
            <xsl:choose>
               <xsl:when test="$useFSH = 'on'">
                  <xsl:value-of select="concat($FSHname, ' (', $FSHclass, ')')" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="LocalEndpointProtocol" />
               </xsl:otherwise>
            </xsl:choose>
         </td>
         <td>
            <table colspan="2" border="1">
               <tbody>
                  <tr bgcolor="lightblue">
                     <td align="right">
                        <xsl:value-of select="string('URI: ')" />
                     </td>
                     <td align="center">
                        <xsl:value-of select="$URI" />
                     </td>
                  </tr>
                  <tr bgcolor="lightgreen">
                     <td align="right">
                        <xsl:value-of select="string('MatchRegexp: ')" />
                     </td>
                      <td align="center">
                        <xsl:value-of select="$MatchRegexp" />
                     </td>
                  </tr>
               </tbody>
            </table>
         </td>
      </tr>
   </xsl:template>
   
   <!-- Web Service Endpoint Remote Rewrite Rule -->
   <!-- Policy Maps
         <WSEndpointRemoteRewriteRule>
            <ServicePortMatchRegexp>^{http://vbs.admin.ch/pi/VBS/Bestellungen/RUAG}OrderChangeRUAG_AsyncOutPort$</ServicePortMatchRegexp>
            <RemoteEndpointProtocol>https</RemoteEndpointProtocol>
            <RemoteEndpointHostname>ixpjci20.op.intra2.admin.ch</RemoteEndpointHostname>
            <RemoteEndpointPort>52001</RemoteEndpointPort>
            <RemoteEndpointURI>/XISOAPAdapter/MessageServlet?senderParty=&amp;senderService=IBM_Datapower_ZSN&amp;receiverParty=&amp;receiverService=&amp;interface=OrderChangeRUAG_AsyncOut&amp;interfaceNamespace=http%3A%2F%2Fvbs.admin.ch%2Fpi%2FVBS%2FBestellungen%2FRUAG</RemoteEndpointURI>
            <RemoteMQQM/>
            <RemoteTibcoEMS/>
            <RemoteWebSphereJMS/>
         </WSEndpointRemoteRewriteRule>
   -->
   <xsl:template match="WSEndpointRemoteRewriteRule">
      <xsl:variable name="URI" select="RemoteEndpointURI/text()" />
      <xsl:variable name="Protocol" select="RemoteEndpointProtocol/text()" />
      <xsl:variable name="Hostname" select="RemoteEndpointHostname/text()" />
      <xsl:variable name="Port" select="RemoteEndpointPort/text()" />
      <xsl:variable name="MatchRegexp" select="ServicePortMatchRegexp/text()" />
      <xsl:variable name="URL" select="concat($Protocol, '://', $Hostname, ':', $Port, $URI)" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position(), ' - ')" />
            <xsl:choose>
               <xsl:when test="$Protocol = 'default'">
                  <xsl:value-of select="concat($Hostname, ':', $Port)" />
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="concat($Protocol, '://', $Hostname, ':', $Port)" />
               </xsl:otherwise>
            </xsl:choose>
         </td>
         <td>
            <table colspan="2" border="1">
               <tbody>
                  <tr bgcolor="lightblue">
                     <td align="right">
                        <xsl:value-of select="string('URI: ')" />
                     </td>
                     <td align="center">
                        <xsl:value-of select="$URI" />
                     </td>
                  </tr>
                  <tr bgcolor="lightgreen">
                     <td align="right">
                        <xsl:value-of select="string('MatchRegexp: ')" />
                     </td>
                      <td align="center">
                        <xsl:value-of select="$MatchRegexp" />
                     </td>
                  </tr>
               </tbody>
            </table>
         </td>
      </tr>
   </xsl:template>
   
   
   
   <!-- Files Section -->
   <xsl:template match="files">
      <table border="2" align="center" width="80%" style="font-family: sans-serif; background-color: #dedfff;">
         <thead align="center" style="font-weight: bold; font-family: sans-serif; background-color: #afb3d9;">
            <tr>
               <th colspan="3">Files</th>
            </tr>
            <tr>
               <th>Index</th>
               <th>File Name (on DataPower)</th>
               <th>Source Name (within ZIP artifact)</th>
            </tr>
         </thead>
         <tbody>
            <xsl:apply-templates select="file" />
         </tbody>
      </table>
   </xsl:template>
   
   <xsl:template match="file">
      <xsl:variable name="location" select="./@location" />
      <xsl:variable name="filename" select="@name" />
      <xsl:variable name="source" select="@src" />
      <tr>
         <td>
            <xsl:value-of select="concat('#', position())" />
         </td>
         <td>
            <xsl:value-of select="string($filename)" />
         </td>
         <td>
            <xsl:value-of select="string($source)" />
         </td>
      </tr>
   </xsl:template>
   
   <!--  Named Templates -->
   <xsl:template name="list-properties">
      <xsl:param name="level"></xsl:param>
      <div class="prop-list" xml:space="default">
         <xsl:choose>
            <xsl:when test="count(./*) > 0">
               <xsl:value-of select="concat(./name(), ': ')"/>
               <xsl:for-each select="*">
                  <xsl:choose>
                     <xsl:when test="./text() = ''">
                        <!-- do not print empty elements -->
                     </xsl:when>
                     <xsl:when test="not(. != '')">
                        <!-- do not print empty elements -->
                     </xsl:when>
                     <xsl:otherwise>
                        <div class="prop-sub-list" xml:space="default">
                           <xsl:value-of select="concat(./name(), ' = ', ./text())"/>
                        </div>
                     </xsl:otherwise>
                  </xsl:choose>
               </xsl:for-each>
            </xsl:when>
            <xsl:when test="./name() = 'mAdminState' or ./text() = ''">
               <!-- do not print empty elements -->
            </xsl:when>
            <xsl:when test="not(. != '')">
               <!-- do not print empty elements -->
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="concat(./name(), ' = ', ./text())"/>
            </xsl:otherwise>
         </xsl:choose>
      </div>
   </xsl:template>
</xsl:stylesheet>
