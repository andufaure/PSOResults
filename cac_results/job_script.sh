#!/bin/bash

JAR_PATH="../../PSONB/dist/PSONB.jar"
INIT="../../instances/fazia/init.csv"
REF="../../instances/fazia/asm2.csv"

[ ! -e $JAR_PATH ] && echo "$JAR_PATH does not exist" 1>&2 && exit 1
[ ! -e $INIT ] && echo "$REF does not exist" 1>&2 && exit 1
[ ! -e $REF ] && echo "$INIT does not exist" 1>&2 && exit 1

[ "$2" == "" ] && echo "./job_script.sh START_REPLIC NB_REQUIRED_REPLICS" 1>&2 && exit 1

i=$1
ref_base=`basename $REF|sed s/\.csv//g`
mkdir -p ./$ref_base

while [ $i -lt $2 ] ; do
    out_file="./$ref_base/$ref_base_$i.out"
    err_file="./$ref_base/$ref_base_$i.err"
    java -jar $JAR_PATH $INIT $REF 1> $out_file 2> $err_file
    i=$(($i+1))
done 
