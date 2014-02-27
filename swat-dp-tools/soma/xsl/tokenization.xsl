<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xslt"
	xmlns:env="http://www.w3.org/2003/05/soap-envelope"
	xmlns:dp="http://www.datapower.com/schemas/management"
	xmlns:str="http://exslt.org/strings"
	exclude-result-prefixes="xalan str env">

	<xsl:output
		method="xml"
		indent="yes"
		xalan:indent-amount="4"
		omit-xml-declaration="yes" />
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


	<!-- System Settings -->
	<xsl:template match="SystemSettings">
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>

		<xsl:element name="{name()}">
			<xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
			<xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
			<xsl:copy-of select="@*" />
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='Description'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="string('device.summary')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='RefreshInterval'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ntp.refresh-interval')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='UserSummary'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ntp.summary')" />
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

	<!-- Host Aliases -->
	<xsl:template match="HostAlias">
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>
		<xsl:variable name="index">
			<xsl:value-of select="position() - (count(../preceding-sibling::*[name()!='HostAlias']) + 1)" />
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
					<xsl:value-of select="concat('host.alias.',$index,'.name')" />
				</xsl:with-param>
			</xsl:call-template>

			</xsl:attribute>
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='UserSummary'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('host.alias.',$index,'.summary')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='IPAddress'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('host.alias.',$index,'.ip')" />
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


	<!-- DNS Settings -->
	<xsl:template match="DNSNameService">
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>

		<xsl:element name="{name()}">
			<xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
			<xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
			<xsl:copy-of select="@*" />
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='SearchDomains'">
						<xsl:element name="{name()}">
							<xsl:for-each select="*">
								<xsl:choose>
									<xsl:when test="local-name(.)='SearchDomain'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('dns.search.domain.',count(../preceding-sibling::SearchDomains)+1)" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="." />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
					<xsl:when test="local-name(.)='NameServers'">
						<xsl:element name="{name()}">
							<xsl:for-each select="*">
								<xsl:choose>
									<xsl:when test="local-name(.)='IPAddress'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('dns.server.',count(../preceding-sibling::NameServers)+1)" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="." />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
					<xsl:when test="local-name(.)='StaticHosts'">
						<xsl:element name="{name()}">
							<xsl:for-each select="*">
								<xsl:choose>
									<xsl:when test="local-name(.)='Hostname'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('dns.static.host.',count(../preceding-sibling::StaticHosts)+1,'.name')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='IPAddress'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('dns.static.host.',count(../preceding-sibling::StaticHosts)+1,'.ip')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="." />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="." />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	
	<!-- NTP Settings -->
	<xsl:template match="NTPService">
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>

		<xsl:element name="{name()}">
			<xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
			<xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
			<xsl:copy-of select="@*" />
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='RemoteServer'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ntp.remote-server.',count(./preceding-sibling::RemoteServer)+1)" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='RefreshInterval'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ntp.refresh-interval')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='UserSummary'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ntp.summary')" />
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

	<!-- SNMP Settings -->
	<xsl:template match="SNMPSettings">
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>

		<xsl:element name="{name()}">
			<xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
			<xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
			<xsl:copy-of select="@*" />
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='UserSummary'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('snmp.summary')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='LocalAddress'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('snmp.local.alias')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='LocalPort'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('snmp.local.port')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Policies'">
						<xsl:element name="{name()}">
							<xsl:for-each select="*">
								<xsl:choose>
									<xsl:when test="local-name(.)='Community'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.policy',count(../preceding-sibling::Policies)+1,'.community')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='Domain'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.policy',count(../preceding-sibling::Policies)+1,'.domain')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='Mode'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.policy',count(../preceding-sibling::Policies)+1,'.mode')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='Host'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.policy',count(../preceding-sibling::Policies)+1,'.host')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="." />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
					<xsl:when test="local-name(.)='NameServers'">
						<xsl:element name="{name()}">
							<xsl:for-each select="*">
								<xsl:choose>
									<xsl:when test="local-name(.)='IPAddress'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('dns.server.',count(../preceding-sibling::NameServers)+1)" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="." />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
					<xsl:when test="local-name(.)='Targets'">
						<xsl:element name="{name()}">
							<xsl:for-each select="*">
								<xsl:choose>
									<xsl:when test="local-name(.)='Host'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.target.',count(../preceding-sibling::Targets)+1,'.host')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='Port'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.target',count(../preceding-sibling::Targets)+1,'.port)" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='Community'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.target',count(../preceding-sibling::Targets)+1,'.community')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='TrapVersion'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.target',count(../preceding-sibling::Targets)+1,'.version')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='SecurityName'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.target',count(../preceding-sibling::Targets)+1,'.security.name')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:when test="local-name(.)='SecurityLevel'">
										<xsl:call-template name="set-token">
											<xsl:with-param name="key">
												<xsl:value-of select="concat('snmp.target',count(../preceding-sibling::Targets)+1,'.security.level')" />
											</xsl:with-param>
										</xsl:call-template>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy-of select="." />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="." />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<!-- Ethernet Interfaces -->
	<xsl:template match="EthernetInterface">
		<xsl:variable name="index">
			<xsl:value-of select="position()" />
		</xsl:variable>
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>

		<xsl:element name="{name()}">
			<xsl:copy-of select="document('')/*/namespace::*[name()='env']" />
			<xsl:copy-of select="document('')/*/namespace::*[name()='dp']" />
			<xsl:copy-of select="@*" />
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='IPAddress'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ethernet.',$label,'.ip')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='DefaultGateway'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('ethernet.',$label,'.gw')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='StaticRoutes'">
						<xsl:call-template name="set-static-routes">
							<xsl:with-param name="int-prefix">
								<xsl:value-of select="concat('ethernet.',$label)" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='StandbyControls'">
						<xsl:call-template name="set-standby-controls">
							<xsl:with-param name="int-prefix">
								<xsl:value-of select="concat('ethernet.',$label)" />
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

	<!-- VLAN Interfaces -->
	<xsl:template match="VLANInterface">
		<xsl:variable name="label">
			<xsl:value-of select="@name" />
		</xsl:variable>
		<xsl:variable name="index">
			<xsl:value-of select="count(preceding-sibling::*[name()='VLANInterface']) + 1" />
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
					<xsl:value-of select="concat('vlan.',$index,'.name')" />
				</xsl:with-param>
			</xsl:call-template>

			</xsl:attribute>
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='IPAddress'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('vlan.',$index,'.ip')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='DefaultGateway'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('vlan.',$index,'.gw')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Identifier'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('vlan.',$index,'.identifier')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Interface'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat('vlan.',$index,'.interface')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='StaticRoutes'">
						<xsl:call-template name="set-static-routes">
							<xsl:with-param name="int-prefix">
								<xsl:value-of select="concat('vlan.',$index)" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='StandbyControls'">
						<xsl:call-template name="set-standby-controls">
							<xsl:with-param name="int-prefix">
								<xsl:value-of select="concat('vlan.',$index)" />
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
	<!-- Static Routes -->
	<xsl:template name="set-static-routes">
		<xsl:param name="int-prefix"></xsl:param>
		<xsl:variable name="index">
			<xsl:value-of select="count(preceding-sibling::*[name()='StaticRoutes']) + 1" />
		</xsl:variable>

		<xsl:element name="{name()}">

			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='Destination'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.staticroute.',$index,'.destination')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Gateway'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.staticroute.',$index,'.gateway')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Metric'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.staticroute.',$index,'.metric')" />
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

	<!-- Standby Group -->
	<xsl:template name="set-standby-controls">
		<xsl:param name="int-prefix"></xsl:param>
		<xsl:element name="{name()}">
			<xsl:for-each select="*">
				<xsl:choose>
					<xsl:when test="local-name(.)='Group'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.group')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='VirtualIP'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.vip')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Preempt'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.preempt')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='Priority'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.priority')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='AuthLow'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.auth.low')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='AuthHigh'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.auth.high')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='AuxVirtualIP'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.vip.aux')" />
							</xsl:with-param>
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="local-name(.)='SelfBalance'">
						<xsl:call-template name="set-token">
							<xsl:with-param name="key">
								<xsl:value-of select="concat($int-prefix,'.standby.selfbalancing')" />
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

</xsl:stylesheet>