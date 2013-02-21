#!/bin/bash

#JAVA=/opt/jdk1.6.0_27/bin/java
JAVA=java
$JAVA -cp ../target/PDFPlotsExtraction-1.0-SNAPSHOT.jar:../target/dependency/* -Djava.awt.headless=true invenio.pdf.cli.PlotsExtractorCli $1 $2 $3
