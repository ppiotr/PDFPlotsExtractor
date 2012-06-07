import socket
import resourcesmgr
import os
import cPickle
import hashlib
import runTest


jar_file_path = "./PDFPlotsExtractor.jar"


def process_request(req):
    """process a single extraction request and return results"""

    print "recieved request: args: %s file: %s " %( str(req.params), str(req.inputFileName))

    # we start with argument substitution .... file names must be changed
    results_data, file_content = "", ""

    #results = execute_track([EXTRACTOR_EXECUTABLE, input_file, output_folder], output_folder)
    #make temporary directory

    temp_dir = tempfile.mkdtemp()
    results = runTest.extract_file(req.inputFileName, os.path.join(temp_dir, "results"), req.params)

    # preparing compressed version of the temp

    tarfile = os.path.join(temp_dir, "results.tgz")
    f = os.popen("tar -czf %s -C %s results" % (tarfile, temp_dir))
    f.read()
    f.close

    f = open(tarfile, "r")
    file_content = f.read()
    f.close()

    # removign temporary directory
    f = os.popen("rm -Rf %s" % (temp_dir, ))
    f.read()
    f.close()

    return {"data": results_data, "params" : req.params }, tarfile

def client():

client()

