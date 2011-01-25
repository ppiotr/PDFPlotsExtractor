#!/bin/bash

java -cp PDFPlotsExtractor.jar:libs/* invenio.pdf.trainingset.EvaluateDocument $1 $2
