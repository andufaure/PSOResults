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

avrgsu=`find $res_dir|grep out| avrgsum`
avrgsu=$(($avrgsu/$nb_res))

echo "Results"
echo "Errors:$nb_errors"
echo "Results:$nb_res"
echo "Best:$best"
echo "Worst:$worst"
echo "FinalItAvrg:$avrgsu"
