<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xslt"
   xmlns:env="http://www.w3.org/2003/05/soap-envelope" xmlns:dp="http://www.datapower.com/schemas/management" xmlns:str="http://exslt.org/strings"
   exclude-result-prefixes="xalan str env">

   <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="4" omit-xml-declaration="yes" />
   <xsl:strip-space elements="*" />

   <xsl:template match="/datapower-configuration">
      <xsl:copy>
         <xsl:attribute name="version">
				<xsl:value-of select="@version" />
			</xsl:attribute>
         <xsl:apply-templates />
      </xsl:copy>
   </xsl:template>

   <xsl:template match="configuration">
      <xsl:copy>
         <xsl:attribute name="domain">@domain.name@</xsl:attribute>
         <xsl:apply-templates />
      </xsl:copy>
   </xsl:template>

   <!-- identity template -->
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()" />
      </xsl:copy>
   </xsl:template>

   <xsl:template match="comment()|processing-instruction()">
      <xsl:copy />
   </xsl:template>
   
   
   <!-- Service Templates -->
   <!-- ================= -->
   
   <!-- XMLFW Service -->
   <xsl:template match="XMLFirewallService">
      <xsl:variable name="label">
         <xsl:value-of select="@name" />
      </xsl:variable>

      <xsl:element name="{name()}">
         <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
         <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
         <xsl:copy-of select="@*" />
         <xsl:for-each select="*">
            <xsl:choose>
               <xsl:when test="local-name(.)='mAdminState'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('service.state')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='LocalAddress'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('local.address')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='LocalPort'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('local.port')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RemoteAddress'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.address')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RemotePort'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.port')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='HTTPTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('http.timeout')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='HTTPPersistentTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('http.timeout.persistent')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RequestType'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('request.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='ResponseType'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('response.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='XMLManager'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('xml.manager')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='Type'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='SSLProfile'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('ssl.profile')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='DebugMode'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('debug.mode')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='DebugHistory'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('debug.history')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:copy-of select="." />
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </xsl:element>
   </xsl:template>


   <!-- MPGW Service -->
   <xsl:template match="MultiProtocolGateway">
      <xsl:variable name="label">
         <xsl:value-of select="@name" />
      </xsl:variable>

      <xsl:element name="{name()}">
         <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
         <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
         <xsl:copy-of select="@*[name()!='name']" />
         <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
               <xsl:with-param name="name">
                  <xsl:value-of select="string('name')" />
               </xsl:with-param>
               <xsl:with-param name="key">
                  <xsl:value-of select="string('service.object.name')" />
               </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
         <xsl:for-each select="*">
            <xsl:choose>
               <xsl:when test="local-name(.)='mAdminState'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('service.state')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='FrontProtocol'">
                  <xsl:call-template name="set-attribute-and-value-token">
                     <xsl:with-param name="attrname">
                        <xsl:value-of select="string('class')" />
                     </xsl:with-param>
                     <xsl:with-param name="attrkey">
                        <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.class')" />
                     </xsl:with-param>
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.name')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RemoteAddress'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.address')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RemotePort'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.port')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='FrontTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('frontside.timeout')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='FrontPersistentTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('frontside.timeout.persistent')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='BackTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backside.timeout')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='BackPersistentTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backside.timeout.persistent')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RequestType'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('request.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='ResponseType'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('response.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='XMLManager'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('xml.manager')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='Type'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='SSLProfile'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('ssl.profile')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='DebugMode'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('debug.mode')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='DebugHistory'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('debug.history')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:copy-of select="." />
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </xsl:element>
   </xsl:template>


   <!-- WSP Service -->
   <xsl:template match="WSGateway">
      <xsl:variable name="label">
         <xsl:value-of select="@name" />
      </xsl:variable>

      <xsl:element name="{name()}">
         <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
         <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
         <xsl:copy-of select="@*[name()!='name']" />
         <xsl:attribute name="name">
            <xsl:call-template name="set-attribute-token">
               <xsl:with-param name="name">
                  <xsl:value-of select="string('name')" />
               </xsl:with-param>
               <xsl:with-param name="key">
                  <xsl:value-of select="string('service.object.name')" />
               </xsl:with-param>
            </xsl:call-template>
         </xsl:attribute>
         <xsl:for-each select="*">
            <xsl:choose>
               <xsl:when test="local-name(.)='mAdminState'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('service.state')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='FrontProtocol'">
                  <xsl:call-template name="set-attribute-and-value-token">
                     <xsl:with-param name="attrname">
                        <xsl:value-of select="string('class')" />
                     </xsl:with-param>
                     <xsl:with-param name="attrkey">
                        <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.class')" />
                     </xsl:with-param>
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',count(./preceding-sibling::FrontProtocol)+1,'.name')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RemoteAddress'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.address')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RemotePort'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.port')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='FrontTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('frontside.timeout')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='FrontPersistentTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('frontside.timeout.persistent')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='BackTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backside.timeout')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='BackPersistentTimeout'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backside.timeout.persistent')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='RequestType'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('request.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='ResponseType'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('response.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='XMLManager'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('xml.manager')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='Type'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('backend.type')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='SSLProfile'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('ssl.profile')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='DebugMode'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('debug.mode')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='DebugHistory'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="string('debug.history')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:copy-of select="." />
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </xsl:element>
   </xsl:template>


   <!-- HTTP(S) Front Side Handler -->
   <xsl:template match="HTTPSSourceProtocolHandler|HTTPSourceProtocolHandler">
      <xsl:variable name="label">
         <xsl:value-of select="@name" />
      </xsl:variable>
      <xsl:variable name="index">
         <xsl:value-of
            select="position() - (count(../preceding-sibling::*[name()!='HTTPSSourceProtocolHandler' and name()!='HTTPSourceProtocolHandler']) + 0)" />
      </xsl:variable>
      
      <xsl:message>
         <xsl:value-of select="concat('fsh.',$index,'.class=',name())" />
      </xsl:message>

      <xsl:element name="{name()}">
         <xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
         <xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
         <xsl:copy-of select="@*[name()!='name']" />
         <xsl:attribute name="name">
			<xsl:call-template name="set-attribute-token">
				<xsl:with-param name="name">
					<xsl:value-of select="string('name')" />
				</xsl:with-param>
				<xsl:with-param name="key">
					<xsl:value-of select="concat('fsh.',$index,'.name')" />
				</xsl:with-param>
			</xsl:call-template>
         </xsl:attribute>
         <xsl:for-each select="*">
            <xsl:choose>
               <xsl:when test="local-name(.)='UserSummary'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',$index,'.summary')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='LocalAddress'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',$index,'.host')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='LocalPort'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',$index,'.port')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='SSLProxy'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',$index,'.ssl')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="local-name(.)='ACL'">
                  <xsl:call-template name="set-token">
                     <xsl:with-param name="key">
                        <xsl:value-of select="concat('fsh.',$index,'.acl')" />
                     </xsl:with-param>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:copy-of select="." />
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </xsl:element>
   </xsl:template>



   <!-- N A M E D T E M P L A T E S -->

   <!-- print tokens and corresponding values to console -->
   <xsl:template name="set-token">
      <xsl:param name="key"></xsl:param>
      <xsl:message>
         <xsl:value-of select="concat($key,'=',text())" />
      </xsl:message>
      <xsl:element name="{name()}">
         <xsl:copy-of select="@*" />
         <xsl:value-of select="concat('@',$key,'@')" />
      </xsl:element>
   </xsl:template>

   <xsl:template name="set-attribute-token">
      <xsl:param name="name"></xsl:param>
      <xsl:param name="key"></xsl:param>
      <xsl:message>
         <xsl:value-of select="concat($key,'=')" />
         <xsl:value-of select="@*[name()=$name]" />
      </xsl:message>
      <xsl:element name="{name()}">
         <xsl:value-of select="concat('@',$key,'@')" />
      </xsl:element>
   </xsl:template>

   <xsl:template name="set-attribute-and-value-token">
      <xsl:param name="attrname"></xsl:param>
      <xsl:param name="attrkey"></xsl:param>
      <xsl:param name="key"></xsl:param>
      <xsl:message>
         <xsl:value-of select="concat($key,'=',text())" />
      </xsl:message>
      <xsl:message>
         <xsl:value-of select="concat($attrkey,'=')" />
         <xsl:value-of select="@*[name()=$attrname]" />
      </xsl:message>
      <xsl:element name="{name()}">
         <xsl:attribute name="{$attrname}">
				<xsl:value-of select="concat('@',$attrkey,'@')" />
			</xsl:attribute>
         <xsl:value-of select="concat('@',$key,'@')" />
      </xsl:element>
   </xsl:template>

   <xsl:template name="print-read-only-token">
      <xsl:message>
         <xsl:value-of select="concat('#read-only# ',name(),'=',text())" />
      </xsl:message>
   </xsl:template>

</xsl:stylesheet>