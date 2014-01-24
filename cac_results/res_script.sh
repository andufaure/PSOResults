#!/bin/bash

res_dir=$1

[ "$res_dir" == "" ] && echo "$0 path" && exit 1

nb_errors=`find $res_dir|grep err|while read ; do cat $REPLY ; done|wc -l`
nb_res=`find $res_dir|grep out|wc -l`
best=`find $res_dir|grep out|while read ; do cat $REPLY | tail -n 23 | head -n 1 | cut -d":" -f 4 ; done|sort -n| head -n 1`
worst=`find $res_dir|grep out|while read ; do cat $REPLY | tail -n 23 | head -n 1 | cut -d":" -f 4 ; done|sort -n| tail -n 1`

function	avrgsum()
{
	avrgsum=0
	while read ; do 
			value=`cat $REPLY | tail -n 23|head -n 1 | cut -d":" -f 2`
			avrgsum=$(($avrgsum+$value))
	done
	echo $avrgsum
}

unset HISTOS
declare -Ax HISTOS

HISTOS["0"]=0
HISTOS["62"]=0
N=0
i=1
while [ $i -lt 61 ] ; do
    HISTOS[${i}]=0
    i=$(($i+5))
done

# $1 : fichier
function    histogramme()
{
    [ "$1" == "" ] && echo "$1 must be a valid file" && exit 1
    
    res_line=`cat $1|grep "Diff"|tail -n 1`
    
    diff_res=`echo $res_line|cut -d":" -f 4`
    it_res=`echo $res_line|cut -d":" -f 2`
    time_res=`echo $res_line|cut -d":" -f 6`
    
    #echo $res_line
    #echo $diff_res
    #echo $it_res
    #echo $time_res
    
    if [ $diff_res -eq 0 ] ; then
        val=${HISTOS[${i}]}
        HISTOS[0]=$(($val+1))
    elif [ $diff_res -lt 62 ] ; then
        i=1
        while [ $i -lt 61 ] ; do
            if [ $diff_res -ge $i ] && [ $diff_res -le $(($i+5)) ] ; then
                val=${HISTOS[${i}]}
                HISTOS[${i}]=$(($val+1))
                # echo "$i:${HISTOS[${i}]}"
                break
            fi
            i=$(($i+5))
        done
    else
        HISTOS[62]=$((${HISTOS[62]}+1))
    fi
    if [ $N -eq 99 ] ; then
        for elem in ${!HISTOS[*]} ; do
                echo "$elem ${HISTOS[$elem]}" ;
        done
    fi
}

avrgsu=`find $res_dir|grep out| avrgsum`
avrgsu=`echo "$avrgsu/$nb_res"|bc -l`

echo "Results"
echo "Errors:$nb_errors"
echo "Results:$nb_res"
echo "Best:$best"
echo "Worst:$worst"
echo "FinalItAvrg:$avrgsu"

find $res_dir|grep out|while read ; do histogramme $REPLY && N=$(($N+1)); done | sort -g



