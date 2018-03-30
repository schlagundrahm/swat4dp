#!/bin/bash
##############################
# Author:   psha@ch.ibm.com
# Modifed:  2016-12-21
# $Id$
##############################

set -o errexit
set -o pipefail
set -o nounset
# set -o xtrace

usage() # Tell them how it works.
{
    echo "This is an utility script for a DataPower environment."
    echo "This script retrieves the running DataPower configuration to determine the version."
    echo ""
    echo "Please note:"
    echo "1) Only WS-Proxy and MPGW services will be retrieved"
    echo "2) The version number needs to be within brackets in the UserSummary of each service"
    echo ""
    echo "Usage: $0 -i <hostname or ip> -d <domain>"
    echo ""
    echo "Options:"
    echo " -p : DataPower port [default = 5550]"
    echo " -u : DataPower username [default = \$USER]"
    echo " -x : DataPower password"
    echo " -s : Retrieve only service versions but no filesystem information"
    echo ""

    exit 1
}

if [ $# == 0 ]; then
    usage
fi

while getopts ':hsd:i:p:u:x:' option; do
  case "$option" in
    h) usage
       ;;
    s) servicesonly=true
       ;;
    d) domain=$OPTARG
       ;;
    i) dphost=$OPTARG
       ;;
    p) port=$OPTARG
       ;;
    u) username=$OPTARG
       ;;
    x) pwd=$OPTARG
       ;;
   \?) printf "illegal option: -%s\n" "$OPTARG" >&2
       echo ""
       usage >&2
       ;;
    :) printf "Option -%s requires an argument." $OPTARG >&2
       exit 1
       ;;
  esac
done

if [ -z ${dphost+x} ]; then
    echo "The -i <hostname> must be specified" >&2
    exit 1
fi

if [ -z ${domain+x} ]; then
    echo "The -d <domain> must be specified" >&2
    exit 1
fi

if [ -z ${username+x} ]; then
    username=$USERNAME
    echo "using current user $username"
fi

if [ -z ${port+x} ]; then
    port=5550
    echo "using predefined port 5550"
fi

if [ -z ${pwd+x} ]; then
    read -s -p "Enter DataPower Password: " pwd
    echo ""
fi


# Set magic variables for current file & dir
__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"



declare -r PROTO=https
declare -r URI3=/service/mgmt/3.0
declare -r URI=/service/mgmt/current

cDate=$(date +"%Y-%m-%d")
cTime=$(date +"%T")


xfwreq=$(cat << EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:man="http://www.datapower.com/schemas/management">
   <soapenv:Header/>
   <soapenv:Body>
      <man:request domain="${domain}">
         <man:get-config class="XMLFirewallService" name=""/>
      </man:request>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)

wspreq=$(cat << EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:man="http://www.datapower.com/schemas/management">
   <soapenv:Header/>
   <soapenv:Body>
      <man:request domain="${domain}">
         <man:get-config class="WSGateway" name=""/>
      </man:request>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)

mpgreq=$(cat << EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:man="http://www.datapower.com/schemas/management">
   <soapenv:Header/>
   <soapenv:Body>
      <man:request domain="${domain}">
         <man:get-config class="MultiProtocolGateway" name=""/>
      </man:request>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)

fsreq=$(cat << EOF
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:man="http://www.datapower.com/schemas/management">
   <soapenv:Header/>
   <soapenv:Body>
      <man:request domain="${domain}">
         <man:get-filestore location="local:" annotated="true" layout-only="false" no-subdirectories="false"/>
      </man:request>
   </soapenv:Body>
</soapenv:Envelope>
EOF
)



URL=${PROTO}://${dphost}:${port}${URI}
echo ""
echo "calling ${URL} with user ${username}"
echo ""
echo "DataPower ${domain} Service Versions on ${cDate} ${cTime}"
echo "============================================================"
echo ""
echo '-------------------------------------------------------------------------------------------------'
#echo "XML Firewalls"

#xfwres=$(curl -ks --header "Content-Type: text/xml;charset=UTF-8" --data-binary "${xfwreq}" -u ${username}:${pwd} ${URL})

#echo $xfwres > xmlfw-response.xml

#echo $xfwres | xmlstarlet sel -t -m "//XMLFirewallService" -n -v "substring-before(substring-after(UserSummary,'['),']')" -o " - " -v "@name"
#echo ""
#echo ""
#echo '-------------------------------------------------------------------------------------------------'
echo "Web Service Proxies"

wspres=$(curl -ks --header "Content-Type: text/xml;charset=UTF-8" --data-binary "${wspreq}" -u ${username}:${pwd} ${URL})

echo $wspres > wsp-response.xml
echo $wspres | xmlstarlet sel -t -m "//WSGateway" -n -v "substring-before(substring-after(UserSummary,'['),']')" -o " - " -v "@name"
echo ""
echo ""
echo '-------------------------------------------------------------------------------------------------'
echo "Multi-Protocol Gateways"

mpgres=$(curl -ks --header "Content-Type: text/xml;charset=UTF-8" --data-binary "${mpgreq}" -u ${username}:${pwd} ${URL})

echo $mpgres | xmlstarlet sel -t -m "//MultiProtocolGateway" -n -v "substring-before(substring-after(UserSummary,'['),']')" -o " - " -v "@name"
echo ""
echo ""
echo '-------------------------------------------------------------------------------------------------'
echo ""

if [ -z ${servicesonly+x} ]; then

echo "File Timestamps in directory local:///"
echo "======================================"

fsres=$(curl -ks --header "Content-Type: text/xml;charset=UTF-8" --data-binary "${fsreq}" -u ${username}:${pwd} ${URL})

echo $fsres | xmlstarlet sel -t -o "# of sub directories = " -v "count(//directory)" -n -o "# of files = " -v "count(//file)" -n -o "total size = " -v "sum(//size)"

echo $fsres | xmlstarlet sel -t -m "//directory" -n -n -v "@name" -n -o "-------------------------------------------------------" -m "file" -n -v "modified" -o " - " -v "@name" -o " - " -v "size"
echo ""

echo '-------------------------------------------------------------------------------------------------'
echo ""
fi
echo '-- DONE --'
