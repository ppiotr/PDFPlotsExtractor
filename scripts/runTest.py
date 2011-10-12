#!/usr/bin/python
# Runs test on a given number of random records from Inspire

#usage: runTest.py number_of_trials status_file

import sys
import random
import urllib2
import re
import os
import subprocess
import time
from datetime import datetime
import getopt
from threading import Thread

def create_directories(path):
    """creates directory and if necessary, all intermediate directories as well"""
    print "Path: %s" %(path, )
    to_create = []
    c_path = path
    while not os.path.exists(c_path) and c_path.strip() != "":
        if c_path[-1] == "/":
            c_path = c_path[:-1]
        to_create.append(c_path)
        c_path = os.path.split(c_path)[0]

    to_create.reverse()
    print "To create: %s" %(str(to_create),)
    for path in to_create:
        os.mkdir(path)

# a general execution module


CFG_SLEEP_INTERVAL = 0.1
CFG_MAX_EXECUTION_TIME = 15 * 60 # 36000 # we kill task running more than one hour !

def get_current_timestamp():
    """returns a current timestamp up to 0.01 s"""
    return time.strftime("%Y%m%d%H%M%s") + str(datetime.now().microsecond)[:2]

def check_status(pid):
    """checks a totla memory usage of the process and return the value"""
    if not os.path.exists("/proc/%i" % (pid, )):
        return None
    results = {}
    try:
        fd = open("/proc/%i/status" % (pid, ), "r")
    except:
        return None
    if fd:
        for line in fd:
            try:
                parts = line.split()
                results[parts[0][:-1]] = parts[1]
            except:
                print "Unparsable line in /proce/%i/status file: %s" % (pid, line)
        return results
    return None

def check_stats(pid):
    """checks a totla memory usage of the process and return the value"""
    if not os.path.exists("/proc/%i" % (pid, )):
        return None
    results = {}
    try:
        fd = open("/proc/%i/stat" % (pid, ), "r")
    except:
        return None
    if fd:
        content = fd.read()
        fd.close()
        return content

    return None


def execute_track(args, folder = None):
    """
    Execute command and track the execution parameters writing them in the directory
    @param folder Optional parameter indicating the place where execution parameters should be saved
    @type folder string
    """
#    import rpdb2; rpdb2.start_embedded_debugger('password', fAllowRemote=False)
#    print "Executing task: %s folder: %s" % (str(args), folder)

    results = {}

    start_time = time.time()

    VmPeak = 0
    VmSize = 0
    retcode =  None
    memusage = []
    create_directories(folder)
#    print "Beginning execution"

    execution = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    # starting the execution of a reader thread
    def collect_output(fd, out_list):
        for line in fd:
            out_list.append(line)
        fd.close()


    stdout_list = []
    stderr_list = []

    th_stdout = Thread(target = collect_output, args=(execution.stdout, stdout_list ))
    th_stderr = Thread(target = collect_output, args=(execution.stderr, stderr_list))
    th_stderr.daemon = True
    th_stdout.daemon = True

    th_stdout.start()
    th_stderr.start()

    while retcode is None:

        stats = check_status(execution.pid)
        timestamp = get_current_timestamp()
        cur_time = time.time()
        if cur_time - start_time > CFG_MAX_EXECUTION_TIME:
            execution.terminate()
            results["forced_exit"] = "Stopped. Was working longer than %i seconds" % (CFG_MAX_EXECUTION_TIME, )

        if stats and "VmPeak" in stats and "VmSize" in stats:
            VmPeakT = int(stats["VmPeak"])
            VmSize = int(stats["VmSize"])
            if VmPeakT > VmPeak:
                VmPeak = VmPeakT # just in case !

            memusage.append("%s %i" % (timestamp, VmSize))

        time.sleep(CFG_SLEEP_INTERVAL)
        retcode = execution.poll()
 #       print "retcode: %s" % (str(retcode), )

    th_stderr.join()
    th_stdout.join()
    execution.wait()
    end_time = time.time()

#    stdout, stderr = execution.communicate(None)

    results["memusage"] = "\n".join(memusage)
    results["max_memusage"] = "%i" % (VmPeakT, )

#    results["stdout"] = stdout
#    results["stderr"] = stderr
    results["stdout"] = "\n".join(stdout_list)
    results["stderr"] = "\n".join(stderr_list)

    results["return_code"] = "%i" % (retcode, )
    results["execution_time"] = str(end_time - start_time)


    # optionally saving results
    if folder:
        # possibly creating all intermediate folders

        create_directories(folder)

        for key in results:
            fd = open(os.path.join(folder, key), "w")
            fd.write(results[key])
            fd.close()
    return results
# executing prticular things

EXTRACTOR_EXECUTABLE = "./run.sh"

def get_record_path(test_folder, rec_id):
    return os.path.join(test_folder, rec_id)

def retrieve_random_document(random_generator, test_folder):
    """Retrieve a random documetn and create a directory for it, returns record ID"""
    while True:
        rec_num = random_generator.randint(10000, 900000)
        file_name = os.path.join(test_folder, "%i.pdf" % (rec_num,) )
        if os.path.exists(file_name):
            continue

        try:
            fd = urllib2.urlopen("http://inspirebeta.net/record/%i" % (rec_num, ))
            content = fd.read()
            fd.close()
            regexp_res = re.findall("""<a href="([^"]*)">PDF</a>""", content)

            if regexp_res and len(regexp_res) > 0:
                url = regexp_res[0]
                fd = urllib2.urlopen(url)
                content = fd.read()
                fd.close()
                fd = open(file_name, "w")
                fd.write(content)
                fd.close()

                return (rec_num, file_name)
        except Exception, e:
            print "Error when retreiving PDF from record %i, %s" %(rec_num, str(e))
            pass



def extract_file(input_file, output_folder, parameters):
    #here we have the syntax of calling the proper extractor !
    #TODO: include parameters in the command line construction
    results = execute_track([EXTRACTOR_EXECUTABLE, input_file, output_folder], output_folder)
    return results

def extract_single_record(rec_id, test_folder):
    folder = get_record_path(test_folder, rec_id)
    fulltext_path = folder + "/fulltext.pdf"
    results = execute_track([EXTRACTOR_EXECUTABLE, fulltext_path], folder)
    return results

def perform_single_test(random_generator, test_folder):
    # download random record from Inspire
    record_id = retrieve_random_document(random_generator, test_folder)
    result = extract_single_record(record_id, test_folder)
    return 0 if ("forced_exit" in result) else 1



def usage():
    """prints the usage message of the program"""
    print """A tool for running and monitoring the performance of the PDF
extractor. It allows normal execution as well as running tests by downloading data
from the


Usage: runTests.py [options]

Accepted options
  -r number     --random=number  Runs in random mode. A number of random PDF's
                                 will be retrieved from Inspire. They will be
                                 saved in subdirectories of the output directory
                                 and the extraction will be performed
  -t test_name  --test test_name Specifies the name of the test
                                 (results will be written in subdirectories
                                 of this name)


  -f file_name  --file=file_name Process a specific file on the input

  -d directory  --directory=name Processes a specific directory of PDFs.
                                 If the directory resulted from running
                                 previous tests (tests_directory file
                                 is present), fulltext.pdf files from
                                 all subdirectories are processed.

  -o dir_name  --output=dir_name Writes output in a specified directory
  -h           --help            Prints this message


  -s           --svg             Generate the SVG file
  -p           --pages           Dump page images
  -a           --annotate        Dump pages annotated with the location of
                                 figures
  -z           --operations     Annotate pages with the location of all
                                 PDF operations

Examples:

   run.py -r 1000 -o some_test_dir

   Executes a random test on 1000 records taken from Inspire and write results
   in some_test_dir directory.

   run.py -t tests1 -d previous_test_run
"""


def parse_input(arguments):
    """ Determine starting options"""
    try:
        res = getopt.getopt(arguments, "r:t:f:d:o:hspaz" ,
                            ["random=", "test=", "file=", "directory=",
                             "output=", "help", "svg", "pages", "annotate",
                             "operations"])
    except:
        return None

    options = {}
    options["output_directory"] = "."
    options["test_name"] = ""
    options["svg"] = False
    options["dump_pages"] = False
    options["annotated_figures"] = False
    options["annotated_operations"] = False

    for option in res[0]:
        if option[0] in ("-r", "--random"):
            options["random"] = int(option[1])

        if option[0] in ("-t", "--test"):
            options["test_name"] = option[1]

        if option[0] in ("-f", "--file"):
            options["input_file"] = option[1]

        if option[0] in ("-d", "--directory"):
            options["input_directory"] = option[1]

        if option[0] in ("-o", "--output"):
            options["output_directory"] = option[1]

        if option[0] in ("-h", "--help"):
            return None

        if option[0] in ("-s", "--svg"):
            options["svg"] = True

        if option[0] in ("-p", "--pages"):
            options["dump_pages"] = True

        if option[0] in ("-a", "--annotate"):
            options["annotated_figures"] = True

        if option[0] in ("-z", "--operations"):
            options["annotated_operations"] = True

    return options


def get_input_files(options):
    """Produces next input file based on the configuration or none in the case,
       no more files should be processed
       yields paths to files that should be processed together with corresponding
       directoreis, wehre output should be placed
       """
    if "input_file" in options:
        pure_name = os.path.splitext(os.path.split(options["input_file"])[1])[0]
        yield (options["input_file"],
               os.path.join(options["output_directory"], pure_name,
                            options["test_name"]))

    if "input_directory" in options:
        for entry in os.listdir(options["input_directory"]):
            file_name, ext = os.path.splitext(entry)
            if ext.lower() == ".pdf":
                yield (os.path.join(options["input_directory"], entry),
                       os.path.join(options["output_directory"], file_name,
                                    options["test_name"]))
    if "random" in options:
        #generate random samples in the output directory
        random_generator = random.Random()
        for sample_num in xrange(0, options["random"]):
            #download a sample !
            recid, path = retrieve_random_document(random_generator,
                                                   options["output_directory"])
            yield (path, os.path.join(options["output_directory"],  str(recid),
                                      options["test_name"]))


if __name__ == "__main__":
    parameters = parse_input(sys.argv[1:])
    if not parameters:
        usage()
        sys.exit()

    if not os.path.exists(parameters["output_directory"]):
        create_directories(parameters["output_directory"])

    status_file = os.path.join(parameters["output_directory"], "status")

    for entry in get_input_files(parameters):
        print "Processing input file %s writing output to the directory %s" % (entry[0], entry[1])
        extract_file(entry[0], entry[1], parameters)

