#!/bin/bash


popcrun objmap ./torus2D 1 2
cat Excel.txt > ./results/torus2d.txt
rm Excel<.txt
for ((i=1; i <= 5; i++))
do
	for ((n=1; n <= 10; n++))
	do
		popcrun objmap ./torus2D $i $n
		tail -n +2 Excel.txt >> ./results/torus2d.txt	
		rm Excel<.txt
	done
done

