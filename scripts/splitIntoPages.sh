#!/bin/bash
#
# Splits all pdf files from a directory into pages
# usage: splitIntoPages.sh inputDir outputDir


for PDFFILE in `ls $1/*.pdf`; do
    NUMPAGES=`pdfinfo ${PDFFILE} | grep Pages: | cut -f 2- --delimiter=\ `
    FILENAME_FULL=`basename ${PDFFILE}`
    FILENAME=${FILENAME_FULL%%.pdf}
    for ((PAGENUM=1;PAGENUM<=$NUMPAGES;PAGENUM++)); do
	OUTPUT_FNAME=$2/${FILENAME}_${PAGENUM}.pdf
	echo Generating ${OUTPUT_FNAME}
	pdftk ${PDFFILE} cat ${PAGENUM} output ${OUTPUT_FNAME}
     done
done
