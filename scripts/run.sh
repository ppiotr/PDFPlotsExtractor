#!/bin/bash

java -cp PDFPlotsExtractor.jar:libs/* invenio.pdf.cli.PlotsExtractorCli $1 $2
