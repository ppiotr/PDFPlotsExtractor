#!/bin/bash

#JAVA=/opt/jdk1.6.0_27/bin/java
JAVA=java
$JAVA -cp PDFPlotsExtractor.jar:libs/* -Djava.awt.headless=true invenio.pdf.cli.PlotsExtractorCli $1 $2 $3
