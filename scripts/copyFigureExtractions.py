#!/usr/bin/python
import os

basedir = "/opt/ppraczyk/splited_results/"
testname = "oracle_jrm_correct_run_2"
outdir = "/tmp/extracted"
f = open("/opt/ppraczyk/splited/figures.data.py")
dic = eval(f.read())

os.mkdir(outdir)
for recid in dic.keys():
    for pnum in dic[recid].keys():
        if dic[recid][pnum] != 0:
            dirname = "%s/%i_%i/%s/%i_%i.pdf.extracted" % (basedir, recid, pnum, testname, recid, pnum)
            print dirname
            os.popen("cp -Rd %s %s" % (dirname, outdir))

