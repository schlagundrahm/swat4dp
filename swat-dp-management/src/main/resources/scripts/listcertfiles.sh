#!/bin/bash
#echo -e "\nUSER\nPASSWROD\n$1\nco\ndir cert:\ndir sharedcert:\nexit\nexit\n" | ssh $2 2>/dev/null | \

echo -e "\nUSER\nPASSWORD\n$1\nco\ndir $2:\nexit\nexit\n" | ssh $3 2>/dev/null | \

awk -v domain="$1" -v dir="$2" -v host="$3" \
'BEGIN { print "Host;Domain;Cert File;Last Modified" } 
 { if ( NF == 7 && NR > 1 && $1~/\./ ) 
 	{ print host ";" domain ";" dir":///"$1 ";" $2" " $3" "$4" "$5" "$6 ";" system("./showcert.sh " domain " " dir":///"$1 " " host) } 
 } 
 END { print "STOP" }'
