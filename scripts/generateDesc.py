#!/usr/bin/python
import os
import subprocess

def get_pdfs(path):
    recids = []
    files = os.listdir(path)

    for file_name in files:
        if file_name[-4:].lower() == ".pdf":
            recids.append(int(file_name[:-4]))
    recids.sort()

    for recid in recids:
        pages_num = None
        pdf_file_name = os.path.join(path, "%i.pdf" % (recid, ))
        p = subprocess.Popen(["pdfinfo", pdf_file_name], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        p.wait()
        for line in p.stdout:
            if line.strip().startswith("Pages:"):
                pages_num = int(line[6:])

        if pages_num:
            print "%i : {" % (recid, )
            # load the PDF document and determine how many pages it has
            for page_num in xrange(1, pages_num+1):
                print "   %i : 0," % (page_num, )
            print "},"
        else:
            print "%i : \"PDF damaged\"" % (recid, )


get_pdfs("/home/piotr/pcfs/tests_1000_records")

print("DONE")
