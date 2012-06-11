#!/bin/bash

function getNumberOfPages(){
    # return the number of pages, a PDF document contains
    local NUMPAGES=`pdfinfo $1 | grep Pages: | cut -f 2- --delimiter=\ `
    echo ${NUMPAGES}
}

OUTPUTFILE=$2

echo "{"

for PDFFILE in `ls $1/*.pdf`; do
    BASE=`basename ${PDFFILE}`
    NUMPAGES=`getNumberOfPages $PDFFILE`
    RECNUM=${BASE%%.pdf}
    echo "${RECNUM} : {"
    for ((PAGE=1;PAGE<=$NUMPAGES;PAGE++)); do
	echo "    ${PAGE} : 0,"
    done
    echo "}"
done

echo "}"