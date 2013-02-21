#!/usr/bin/python

# retrieve samples from different decades rom 1950 ... 20 samples per decade

import urllib2
import re
import random
from invenio.bibdocfile import BibRecDocs
import os


NUM_PER_DECADE = 20

def retrieve_ids_by_query(url):
    """retrieve a number of recids from a given url"""
    final_set = set()
    for i in [100 * x + 1 for x in xrange(10)]:
        f =  urllib2.urlopen(url + "&jrec=%i" %(i,))
        c = f.read()
        f.close()
        final_set = final_set.union(set(map(lambda x: int(x), re.findall("([1-9][0-9]*) 001", c))))
    return final_set

def retrieve_from_period(from_year, to_year):
    return retrieve_ids_by_query("http://inspirehep.net/search?ln=en&ln=en&p=date+%%3C+%i+and+date+%%3E%%3D+%i&of=tm&action_search=Search&so=0&rm=&rg=100" % (to_year, from_year))

def retrieve_from_period_before(end_year):
    return retrieve_ids_by_query("http://inspirehep.net/search?ln=en&p=date+%%3C+%i&of=tm&action_search=Search" % (end_year, start))

def retrieve_random_sample(possible_ids, directory):
    """retrieves a sample document from a given set and return the id"""
    while len(possible_ids) > 0:
        recid = possible_ids.pop()
        brd = BibRecDocs(recid)

        pdf_bibdocfile = reduce( lambda x, y: y, filter(lambda bdf: bdf.format == ".pdf", brd.list_latest_files()), None)
        if pdf_bibdocfile:
            file_to_save = os.path.join(directory, "%i.pdf" % (recid, ))
            f = open(file_to_save, "w")
            f.write(pdf_bibdocfile.get_content())
            f.close()
            return recid
    return None # there were no more samples

def prepare_period(ids, directory):
    """Prepare a sample training set consisting of records from a decade"""
    global NUM_PER_DECADE
    if not os.path.exists(directory):
        os.mkdir(directory)
    for i in xrange(NUM_PER_DECADE):
        retrieve_random_sample(ids, directory)

if __name__ == "__main__":
    basedir = "sample_by_decade"
    if not os.path.exists(basedir):
        os.mkdir(basedir)
    print "Processing befiore 1950"
    prepare_period(retrieve_from_period_before(1950), os.path.join(basedir, "before1950"))
    for from_year, to_year in [(1950 + x*10, 1960 + x*10) for x in xrange(7)]:
        print "Processing between %i and %i" % (from_year, to_year)
        prepare_period(retrieve_from_period(from_year, to_year), os.path.join(basedir, "%ito%i" %(from_year, to_year)))

    print "DONE"

