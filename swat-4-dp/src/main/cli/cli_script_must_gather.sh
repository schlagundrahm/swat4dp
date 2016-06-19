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
## Hostname or ip address of the DataPower device 
DPHOST=$1
## User ID with privileged access
DPUSER=$2
## number of iterations
MAX_COUNT=$3
## sleep between iterations
SLEEP=$4

if [ $# -lt 2 ]
then
    echo "Error in $0 - Invalid Argument Count"
    echo "Syntax: $0 <dp_host> <userid> [<count>] [<sleep>]"
    exit
fi

if [ $# -lt 3 ]
then
    MAX_COUNT=7
    SLEEP=300
fi

if [ $# -lt 4 ]
then
    SLEEP=300
fi

##************************************************************

echo -n "Password: "
stty -echo
read DPPWD
stty echo
echo ""         # force a carriage return to be output

##************************************************************

## The INFILE is created then used each time the SSH connection is made
INFILE=cli_input.txt

## The filename prefix these will have a date and time stamp added
OUTFILE=${DPHOST}_cli_output.log

##************************************************************

cat << EOF > $INFILE
$DPUSER
$DPPWD
default
echo show clock
show clock
echo show version
show version
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
echo show int mode
show int mode
echo show network
show network
echo show route
show route
echo show netarp
show netarp
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
echo show task sysrecord
show task sysrecord
EOF

# Collecting load and memory data, 7 captures.
COUNT=1
DATE=`date`
echo "Data collection started at $DATE" > $OUTFILE
echo "=============================================================" >> $OUTFILE

while [[ $COUNT -lt $MAX_COUNT+1 ]]
do
	DATE=`date`
	echo " *********** Number = $COUNT of $MAX_COUNT  **************" >> $OUTFILE
	ssh  -T $DPHOST < $INFILE  >> $OUTFILE
	mv $OUTFILE ${OUTFILE}.$(date +%Y%m%d-%H%M%S)
	echo "Created file: " $OUTFILE.$(date +%Y%m%d-%H%M%S)
	sleep ${SLEEP}
	(( COUNT += 1 ))
done

rm $INFILE

echo "Complete"