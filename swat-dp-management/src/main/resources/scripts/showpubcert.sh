#!/bin/bash
echo -e "\nUSER\nPASSWORD\n$1\nco\nshow file $2\nexit\nexit\n" | ssh $3 2>/dev/null
# | \
#sed -n '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/p'
# | \
#openssl x509 -noout -subject

