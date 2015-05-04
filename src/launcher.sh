#!/bin/bash


# Change this to your netid
netid=kxk133030

#
# Root directory of your project
#PROJDIR=$HOME/AOS_ROY/NodeDiscovery/
PROJDIR=$HOME/Project3

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#


CONFIG=$PROJDIR/topology.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR/bin

#
# Your main project class
#
PROG=Project3

#n=1
echo $CONFIG
cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | 
(
    #read i
    #echo $i
    while read line 
    do
        host=$( echo $line | cut -f2 -d" " | cut -f1 -d":" | cut -f2 -d" " )

	n=$( echo $line | cut -f1 -d" ")

        echo $host

	echo $n

	#echo "ssh $netid@$host java $BINDIR/$PROG $n " 
        ssh -l "$netid" "$host" "cd $BINDIR;java  -cp .:/$PROJDIR/commons-math3-3.4.1.jar $PROG $n $CONFIG" &
	#cd $BINDIR;java $PROG $n $CONFIG &
	#cd $BINDIR;java $PROG $n $CONFIG &
	#n=$(( n + 1 ))
    done
   
)


