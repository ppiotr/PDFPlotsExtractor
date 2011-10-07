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
            parts = line.split()
            results[parts[0][:-1]] = parts[1]
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

    results = {}

    start_time = time.time()

    execution = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    VmPeak = 0
    VmSize = 0
    retcode = None
    memusage = []
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
    execution.wait()
    end_time = time.time()

    stdout, stderr = execution.communicate(None)

    results["memusage"] = "\n".join(memusage)
    results["max_memusage"] = "%i" % (VmPeakT, )
    results["stdout"] = stdout
    results["stderr"] = stderr
    results["return_code"] = "%i" % (retcode, )
    results["execution_time"] = str(end_time - start_time)


    # optionally saving results
    if folder:
        if not os.path.exists(folder):
            os.mkdir(folder)

        for key in results:
            fd = open("%s/%s" % (folder, key), "w")
            fd.write(results[key])
            fd.close()
    return results


# executing prticular things

EXTRACTOR_EXECUTABLE = "./run.sh"

def print_status_message(status_file, total_num, successful_num, expected_num):
    msg = "Processed: %i records, successful: %i. Expected number of executions: %i" %(total_num, successful_num, expected_num)
    fd = open(status_file, "w")
    fd.write(msg)
    fd.close()

def get_record_path(test_folder, rec_id):
    return "%s/%s" % (test_folder, rec_id)

def retrieve_random_document(random_generator, test_folder):
    """Retrieve a random documetn and create a directory for it, returns record ID"""
    while True:
        try:
            rec_num = random_generator.randint(10000, 900000)
            fd = urllib2.urlopen("http://inspirebeta.net/record/%i" % (rec_num, ))
            content = fd.read()
            fd.close()
            regexp_res = re.findall("""<a href="([^"]*)">PDF</a>""", content)
            if regexp_res and len(regexp_res) > 0:
                url = regexp_res[0]
                fd = urllib2.urlopen(url)
                content = fd.read()
                fd.close()

                folder = get_record_path(test_folder, str(rec_num))
                os.mkdir(folder)

                fd = open("%s/fulltext.pdf" % ( folder, ), "w")
                fd.write(content)
                fd.close()

                return rec_num
        except:
            print "Error when retreiving PDF from record %i" %(rec_num,)
            pass


def extract_single_record(rec_id, test_folder):
    folder = get_record_path(test_folder, rec_id)
    fulltext_path = folder + "/fulltext.pdf"
    cmd = "%s %s" % (EXTRACTOR_EXECUTABLE, fulltext_path)
    results = execute_track([EXTRACTOR_EXECUTABLE, fulltext_path], folder)
    return results

def perform_single_test(random_generator, test_folder):
    # download random record from Inspire
    record_id = retrieve_random_document(random_generator, test_folder)
    result = extract_single_record(record_id, test_folder)

    return 0 if ("forced_exit" in result) else 1


if __name__ == "__main__":
     #temporary


    number_trials = int(sys.argv[1])
    test_folder = sys.argv[2]


    log_file = test_folder + "/log"
    status_file = test_folder + "/status"

    total_num = 0
    successful_num =0

    if not os.path.exists(test_folder):
        os.mkdir(test_folder)

    rnd = random.Random()

    print "STARTING Number of trials: %i results path: %s" % (number_trials, test_folder)
    for i in xrange(number_trials):
        successful_num += perform_single_test(rnd, test_folder)
        total_num += 1
        print "*",
        print_status_message(status_file, total_num, successful_num, number_trials)

    print "FINISHED PROCESSING"
