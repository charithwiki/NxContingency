#!/bin/bash
graph=$1
MX=$2
COUNTER=1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
while [ $COUNTER -le $MX ]
do
     java -cp $DIR/../target/Nx-Contingency-1.0-jar-with-dependencies.jar edu.usc.hive.NxContingency $graph $COUNTER n 
     COUNTER=$((COUNTER+1))
done
