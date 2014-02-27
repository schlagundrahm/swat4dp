#!/bin/sh
# IBM DataPower L2 Support, csupport@us.ibm.com
#
# The script is provided as it is.
#
# The script will run 7 times, once every 5 mins and collect sample
# data to troubleshoot 100% CPU issues. 
# After script finishes, please send the file cli_output.*
# to the IBM DataPower support specialist.
# For more detail see the technote below --
# http://www-01.ibm.com/support/docview.wss?uid=swg21377610 

##************************************************************
## ****  Edit next 3 lines according to your environment *****
##************************************************************
## Hostname or ip address of the DataPower device 
DPHOST=$1

## The INFILE is created then used each time the SSH connection is made
INFILE=cli.txt

## The filename prefix these will have a date and time stamp added
OUTFILE=cli_output.

##************************************************************

cat << EOF > $INFILE
DP_PRIVLEDGED_USER_ID
PASSWORD
echo show clock
show clock
echo show load
show load
echo show cpu
show cpu
echo show throughput
show throughput
echo show tcp
show tcp
echo show int
show int
diag
echo show mem
show mem
echo show mem details
show mem details
echo show connections
show connections
echo show handles
show handles
echo show activity 50
show activity 50
EOF

pause

# Collecting load and memory data, 7 captures.
COUNT=7
echo "Data collect started at $DATE" > $OUTFILE
while [[ $COUNT -gt 0 ]]
do
	DATE=`date`
	echo " *********** Number = $COUNT  **************" >> $OUTFILE
	ssh  -T $DPHOST < $INFILE  >> $OUTFILE
	mv $OUTFILE $OUTFILE$(date +%Y%m%d-%H%M%S)
	echo "Created file: " $OUTFILE$(date +%Y%m%d-%H%M%S)
	sleep 300
	(( COUNT -= 1 ))
done

echo "Complete"