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
from threading import Thread, Semaphore
import socket
import SocketServer
import hashlib
import tempfile
import cPickle
from Queue import Queue
import base64



# Definitions for networked usage - distribution of the execution

latest_md5 = "" # md5 hash of the latest version of JAR file
latest_jar = "ABCD" * 10 + "F" # the content of the latest JAR file
current_controller = None
requests_queue = Queue()
results_queue = Queue()


def recv_bytes(sock, size):
    """Recieve exact number of bytes from a socket"""
    recvd = 0
    res = []
    chunk = 8192
    while recvd != size:
        if recvd + chunk > size:
            chunk = size - recvd
        part = sock.recv(chunk)
        recvd += len(part)
        res.append(part)
    return "".join(res)

def send_bytes(sock, buf):
    """Send exact number of bytes to a socket"""
    to_send = len(buf)
    sent = 0
    chunk = 8192

    while sent !=to_send:
        if sent + chunk > to_send:
            chunk = to_send - sent
        sent += sock.send(buf[sent:sent+chunk])

def send_data(request, file_content_raw):
    #transfer a file over a request object
    file_content = base64.b64encode(file_content_raw)
    chunk_size = 16000 # the size of single sending
    file_len = len(file_content)

    send_bytes(request, "%012i" % ( file_len))
    send_bytes(request, hashlib.md5(file_content).hexdigest())
    # now sending the file in chunks
    send_bytes(request, file_content)

def recieve_data(request):
    file_size = int(recv_bytes(request, 12))
    file_md5 = recv_bytes(request, 32)
    return base64.b64decode(recv_bytes(request, file_size))


class ProcessingResult(object):
    def __init__(self, original_params, results, file_content=None, file_name=None, tempdir=None):
        if file_content:
            if tempdir:
#                print "tempdir: " + str(tempdir)
                fd, self.fileName = tempfile.mkstemp(suffix=".tgz", dir=tempdir)
            else:
                fd, self.fileName = tempfile.mkstemp(suffix=".tgz")
            os.write(fd, file_content)
            os.close(fd)
        else:
            self.fileName = file_name
        self.results = results
        self.original_params = original_params

    @classmethod
    def read_from_socket(self, soc, tempdir=None):
        results_s = recieve_data(soc)
        params_s = recieve_data(soc)
        content = recieve_data(soc)
        return ProcessingResult(cPickle.loads(params_s), cPickle.loads(results_s), content, tempdir=tempdir)

    def send_over_socket(self, soc):
        send_data(soc, cPickle.dumps(self.results))
        send_data(soc, cPickle.dumps(self.original_params))
        f = open(self.fileName, "r")
        send_data(soc, f.read())
        f.close()

class ProcessingRequest(object):
    """Stores a request to extract data from a file ... contains the PDF file and the parameters of the extractor"""
    def __init__(self, params, file_name, input_content=None, input_file = None, folder=None, tempdir=None):
        self.params = params
        self.file_name = file_name

        if input_content:
            self.hash = hashlib.md5(input_content).hexdigest()
            if folder:
                self.inputFileName = os.path.join(folder, file_name)
                fd = open(self.inputFileName, "w")
            else:
                if tempdir:
                    fdl, self.inputFileName = tempfile.mkstemp(suffix=".pdf", dir=tempdir)
                else:
                    fdl, self.inputFileName = tempfile.mkstemp(suffix=".pdf")
                fd = os.fdopen(fdl, "w")
            fd.write(input_content)
            fd.close()
        else:
            self.inputFileName = input_file
            f = open(input_file, "r")
            self.hash = hashlib.md5(f.read()).hexdigest()
            f.close()


    def send_over_socket(self, soc):
        """Read data of a request and send it over a socket"""
        f = open(self.inputFileName, "r")
        send_data(soc, self.file_name)
        send_data(soc, f.read())
        send_data(soc, cPickle.dumps(self.params))


    @classmethod
    def read_from_socket(self, soc, folder=None, tempdir=None):
        """reads a request from a socket"""
        file_name = recieve_data(soc)
        file_data = recieve_data(soc)
        request_ser = recieve_data(soc)
        return ProcessingRequest(cPickle.loads(request_ser), file_name, input_content = file_data, folder = folder, tempdir=tempdir)

tasks_sem = Semaphore()
tasks_in_processing = {} # which tasks are being currently processed "hash of the file" -> (ProcessingRequest(), number of workers)

class Worker():
    # processign of a single worker
    def __init__(self, request, parameters):
        self.jar_md5 = "" # at the very beginning we will have to update JAR anyway
        self.request = request
        self.parameters = parameters

    def handle(self):
        while True:
            rq = None
            updated_stats = False

            try:
                rq = requests_queue.get_nowait()
            except:
                # there are no new tasks waiting ... we pick an existing task with the smallest count for execution
                tasks_sem.acquire()
                items = tasks_in_processing.items()
                items.sort(lambda x, y: x[1][1] - y[1][1])
                if items:
                    rq = items[0][1][0]
                    tasks_in_processing[items[0]] = (items[1][0], items[1][1])
                    updated_stats = True
                    print "Resubmitting already submitted task"
                tasks_sem.release()
            if not rq: # wait in the queue !
                rq = requests_queue.get()
            if not updated_stats:
                tasks_sem.acquire()
                tasks_in_processing[rq.hash] = (rq, 1)
                tasks_sem.release()


            self.update_jar_if_necessary()
            send_bytes(self.request, "CMD")
            rq.send_over_socket(self.request)
            tmpdir = None
            if "tempdir" in self.parameters:
                tmpdir = self.parameters["tempdir"]
            res = ProcessingResult.read_from_socket(self.request, tempdir = self.parameters["tempdir"])
            tasks_sem.acquire()
            if rq.hash in tasks_in_processing:
                del tasks_in_processing[rq.hash]
            else: # in this case the request has already returned.... not enqueueing any result
                res = None
            tasks_sem.release()

            if res:
                results_queue.put(res)


    def update_jar_if_necessary(self):
        """Update the jar archive"""
        if self.jar_md5 != latest_md5:
            print "updating JAR"
            send_bytes(self.request, "JAR")
            send_data(self.request, latest_jar)
            self.jar_md5 = hashlib.md5(latest_jar).hexdigest()
    def disconnect(self):
        print "Worker disconnected"

class Controller():
    def __init__(self, request, parameters):
        global current_controller
        self.request = request;
        self.parameters = parameters
        current_controller = self
        finish = False
        # clearing all the results from the queue

        while not finish:
            try:
                results_queue.get_nowait()
            except:
                finish=True
    def handle(self):
        global current_controller
        global latest_jar
        global latest_md5

       # recieve JAR, recieve requests (REQ + reqiest)* recieve END
        latest_jar = recieve_data(self.request)
        latest_md5 = recieve_data(self.request)

        #hashlib.md5(latest_jar).hexdigest()
        cmd = ""
        added_requests = 0
        while cmd != "END":
            cmd = recv_bytes(self.request, 3)
            if cmd == "REQ":
                tempdir = None
                if "tempdir" in self.parameters:
                    tempdir = self.parameters["tempdir"]
                req = ProcessingRequest.read_from_socket(self.request, tempdir=tempdir)
                requests_queue.put(req)
                added_requests += 1

        # now we wait for the same number of results

        print "Recieved a complete set of %i requests, now waiting for workers to finish processing\n\n\n" % (added_requests, )

        returned_results = 0
        while returned_results != added_requests:
            result = results_queue.get()
            result.send_over_socket(self.request)
            returned_results += 1
            print "Returned result %i of %i" % (returned_results, added_requests)

        current_controller = None

    def disconnect(self):
        global current_controller
        current_controller = None


class ClientRequestHandler(SocketServer.BaseRequestHandler ):
    def setup(self):
        client_type = recv_bytes(self.request, 1)
        self.algorithm = None
        if client_type == "W":
            print "Worker connected at " + str(self.client_address)
            self.algorithm = Worker(self.request, ClientRequestHandler.parameters)

        elif client_type == "C":
            print "Controller connected at " + str(self.client_address)
            # we can have only a single controller !
            if not current_controller:
                self.algorithm = Controller(self.request, ClientRequestHandler.parameters)
                send_bytes(self.request, "ACK")
            else:
                send_bytes(self.request, "RJC")
                print "Rejected controller request because there is already one connected controller"
        else:
            print "ERROR: unknown type of client connected"

    def handle(self):
        if self.algorithm:
            self.algorithm.handle()

    def finish(self):
        print self.client_address, 'disconnected!'
        if self.algorithm:
            self.algorithm.disconnect()
    @classmethod
    def setParameters(self, params):
        ClientRequestHandler.parameters = params

# The usual definitions useful for processing
#class Request

def create_directories(path):
    """creates directory and if necessary, all intermediate directories as well"""
    to_create = []
    c_path = path
    while not os.path.exists(c_path) and c_path.strip() != "":
        if c_path[-1] == "/":
            c_path = c_path[:-1]
        to_create.append(c_path)
        c_path = os.path.split(c_path)[0]

    to_create.reverse()
    for path in to_create:
        os.mkdir(path)
    return path

# a general execution module


CFG_SLEEP_INTERVAL = 0.1
CFG_MAX_EXECUTION_TIME = 15 * 60 # 36000 # we kill task running more than one hour !

jar_file_path = "./PDFPlotsExtractor.jar"

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
    print "Execution args: %s folder: %s" % (str(args), str(folder))
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

# this seems not to be used
#def extract_single_record(rec_id, test_folder):
#    folder = get_record_path(test_folder, rec_id)
#    fulltext_path = folder + "/fulltext.pdf"
#    results = execute_track([EXTRACTOR_EXECUTABLE, fulltext_path], folder)
#    return results

#def perform_single_test(random_generator, test_folder):
#    # download random record from Inspire
#    record_id = retrieve_random_document(random_generator, test_folder)
#    result = extract_single_record(record_id, test_folder)
#    return 0 if ("forced_exit" in result) else 1

def include_in_statistics(parameters, input_id, result, internal_data=None):
    """Helps building statistics incrementally"""

    # 1) Gathering number of executions with non-empty stderr and aggregating error messages (later we might want to aggregate by particular Java exception)


    res_dir = create_directories(os.path.join(parameters["output_directory"],
                                              "summary",
                                              parameters["test_name"]))
    stderr_dir = create_directories(os.path.join(res_dir, "stderr"))
    if internal_data is None:
        internal_data = {}
        internal_data["number_of_executions"] = 0
        internal_data["number_nonempty_stderr"] = 0
        internal_data["return_codes"] = {}
        internal_data["execution_times"] = {}
        internal_data["max_memory_usage"] = {}

    internal_data["number_of_executions"] += 1

    if result["stderr"].strip() != "":
        internal_data["number_nonempty_stderr"] += 1
        fd = open(os.path.join(stderr_dir, input_id), "w")
        fd.write(result["stderr"])
        fd.close()


    # 2) Build histogram of return codes
    code = result["return_code"]
    if not code in internal_data["return_codes"]:
        internal_data["return_codes"][code] = 0
    internal_data["return_codes"][code] += 1

    # 3) Build histogram of execution times (removing parts below a second)

    time = result["execution_time"].split(".")[0] #part before the first dot
    if not time in internal_data["execution_times"]:
        internal_data["execution_times"][time] = 0
    internal_data["execution_times"][time] += 1

    # 4) Build a histogram of memory usage

    mem_usage = str(int(int(result["max_memusage"]) / 1024)) # we count in megabytes
    if not mem_usage in internal_data["max_memory_usage"]:
        internal_data["max_memory_usage"][mem_usage] = 0
    internal_data["max_memory_usage"][mem_usage] += 1

    # 5) For every execution build plot of memory usage versus time
    #TODO ! this might be interesting if the memory usage of some process is too high

    return internal_data

def finalise_statistics(parameters, data):
    """Writing statistics generated in previous steps"""
    res_dir = create_directories(os.path.join(parameters["output_directory"],
                                              "summary", parameters["test_name"]))


    root_app_code = """
void createStderrPlot(){
  TH1I* histo = new TH1I("histo","Processes having empty error output", 2, 1, 3);
  histo->SetFillColor(8);
  histo->SetBarWidth(0.9);
  histo->SetBarOffset(0.05);
  histo->SetMinimum(0);
  histo->GetXaxis()->SetBinLabel(1, "empty stderr");
  histo->GetXaxis()->SetBinLabel(2, "non-empty stderr");
  histo->SetStats(0);
  histo->SetBinContent(1, data_nonempty);
  histo->SetBinContent(2, data_empty);

  histo->Draw("bar1 text");
}



void createHistogram(int num_values, int keys[], int values[], const char* name, const char* desc){
  // select minimal values
  int max = keys[0];
  for (int i=0; i < num_values; i++){
    int point = keys[i];
    if (point > max){
      max = point;
    }
  }

  // build the histogram
  TH1I* histo = new TH1I(name, desc, max, 0, max + 1);

  histo->SetFillColor(31);
  histo->SetBarWidth(0.9);
  histo->SetBarOffset(0.05);
  histo->SetMinimum(0);
  histo->SetStats(0);
  for (int i=0;i<num_values;i++){
    int key = keys[i];
    int val = values[i];
    histo->SetBinContent(key, val);
  }

  histo->Draw("bar1 text");
}


void createExecutionTimeHistogram(){
  createHistogram(data_executiontimes_num, data_executiontimes_keys, data_executiontimes_values, "histo_execution_times", "Execution times in seconds");
}

void createMemoryUsageHistogram(){
  createHistogram(data_memoryusage_num, data_memoryusage_keys, data_memoryusage_values, "histo_memory_usage", "Memory usage in megabytes");
}


void createExitCodeHistogram(){
  // build the histogram
  TH1I* histo = new TH1I("histo_exit_codes", "Exit codes", data_exitcodes_num, 0, data_exitcodes_num+1);

  histo->SetFillColor(8);
  histo->SetBarWidth(0.9);
  histo->SetBarOffset(0.05);
  histo->SetMinimum(0);
  histo->SetStats(0);
  // fixing labels
  for (int i=0; i < data_exitcodes_num; i++){
    char label[100];
    sprintf(label, "%i", data_exitcodes_keys[i]);
    histo->GetXaxis()->SetBinLabel(i + 1, label);
  }

  for (int i=0; i < data_exitcodes_num; i++){
    int val = data_exitcodes_values[i];
    histo->SetBinContent(i + 1, val);
  }

  histo->Draw("bar1 text");
}


void stats() {
   TCanvas *c1 = new TCanvas("c1","The FillRandom example",200,10,700,900);
   c1->SetFillColor(17);

   TPad* pad1 = new TPad("pad1","The pad with execution times", 0.01,0.50,0.49,0.99, 21);

   TPad* pad2 = new TPad("pad2","The pad with empty/nonempty stderr", 0.01,0.01,0.49,0.49, 21);

   TPad* pad3 = new TPad("pad3","The pad with return codes histogram", 0.50, 0.50, 0.99, 0.99, 21);

   TPad* pad4 = new TPad("pad4","The pad with the memory usage histogram", 0.50,0.01,0.99,0.49, 21);

   pad1->Draw();
   pad2->Draw();
   pad3->Draw();
   pad4->Draw();

   pad1->cd();
   createExecutionTimeHistogram();
   c1->Update();

   pad2->cd();
   pad2->GetFrame()->SetFillColor(42);
   pad2->GetFrame()->SetBorderMode(-1);
   pad2->GetFrame()->SetBorderSize(5);
   createStderrPlot();
   c1->Update();

   pad3->cd();

   createExitCodeHistogram();
   c1->Update();

   pad4->cd();
   createMemoryUsageHistogram();
   c1->Update();
}
"""
    fd = open(os.path.join(res_dir, "stats.C"), "w")

    # Writing stats of stderr
    fd.write("int data_nonempty = %i;\n" % (data["number_nonempty_stderr"], ))
    fd.write("int data_empty = %i;\n" % (data["number_of_executions"] - data["number_nonempty_stderr"], ))

    # Writing statistics of exit codes
    fd.write("/*exit codes histogram: %s\n*/\n" % (str(data["return_codes"]),))

    fd.write("int data_exitcodes_num = %i;\n" % (len(data["return_codes"]), ))
    keys_list = []
    vals_list = []
    for k in data["return_codes"]:
        keys_list.append(str(k))
        vals_list.append(str(data["return_codes"][k]))
    fd.write("int data_exitcodes_keys[] = {%s};\n" % (", ".join(keys_list),))
    fd.write("int data_exitcodes_values[] = {%s};\n" % (", ".join(vals_list),))

    #Writing statistics of memory usage
    fd.write("/*memory usage histogram (megabytes): %s\n*/\n" % (str(data["max_memory_usage"]),))
    fd.write("int data_memoryusage_num = %i;\n" % (len(data["max_memory_usage"]), ))
    keys_list = []
    vals_list = []
    for k in data["max_memory_usage"]:
        keys_list.append(str(k))
        vals_list.append(str(data["max_memory_usage"][k]))
    fd.write("int data_memoryusage_keys[] = {%s};\n" % (", ".join(keys_list),))
    fd.write("int data_memoryusage_values[] = {%s};\n" % (", ".join(vals_list),))

    #Writing statistics of execution time

    fd.write("/*execution time(seconds): %s\n*/\n" % (str(data["execution_times"]),))
    fd.write("int data_executiontimes_num = %i;\n" % (len(data["execution_times"]), ))
    keys_list = []
    vals_list = []
    for k in data["execution_times"]:
        keys_list.append(str(k))
        vals_list.append(str(data["execution_times"][k]))
    fd.write("int data_executiontimes_keys[] = {%s};\n" % (", ".join(keys_list),))
    fd.write("int data_executiontimes_values[] = {%s};\n" % (", ".join(vals_list),))

    fd.write(root_app_code)
    fd.close()


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
  -z           --operations      Annotate pages with the location of all
                                 PDF operations
  -e fname --description=fname   Specifies a file with description of data.
                                 Used to verify the correctness of the description.
  --temp=dir                     Specifies the temporary directory

Options allowing distributed execution on a cluster

  -c address:port --controller=address:port Act as a controller for a farm of
                                 extracting machines. A resource manager has to be working
                                 on the machine whose address is specified

  -m port --manager=port  Starts a resources manager at a given port
  -w address:port --worker=address:port Starts a worker connecting to manager at address:port
Examples:

   run.py -r 1000 -o some_test_dir

   Executes a random test on 1000 records taken from Inspire and write results
   in some_test_dir directory.

   run.py -t tests1 -d previous_test_run
"""


def parse_input(arguments):
    """ Determine starting options"""
    try:
        res = getopt.getopt(arguments, "r:t:f:d:o:e:c:m:w:hspaz" ,
                            ["random=", "test=", "file=", "directory=",
                             "output=", "description=","help", "svg", "pages", "annotate",
                             "operations", "controller=", "manager=", "worker=", "temp="])
    except:
        return None

    options = {}
    options["output_directory"] = "."
    options["test_name"] = ""
#    options["descriptions_file"] = None # The file with figures description (page by page number of figures)
    options["svg"] = False
    options["dump_pages"] = False
    options["annotated_figures"] = False
    options["annotated_operations"] = False

    for option in res[0]:
        if option[0] in ("--temp"):
            options["tempdir"] = option[1]
        if option[0] in ("-r", "--random"):
            options["random"] = int(option[1])

        if option[0] in ("-t", "--test"):
            options["test_name"] = option[1]

        if option[0] in ("-m", "--manager"):
            options["be_manager"] = int(option[1])

        if option[0] in ("-w", "--worker"):
            address = option[1].split(":")[0]
            port = int(option[1].split(":")[1])
            options["be_worker"] = {"address": address, "port" : port}

        if option[0] in ("-c", "--controller"):
            address = option[1].split(":")[0]
            port = int(option[1].split(":")[1])
            options["be_controller"] = {"address": address, "port" : port}

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

        if option[0] in ("-e", "--description"):
            options["descriptions_file"] = option[1]
            #parsing the description object and puttign it into options
            fd = open(options["descriptions_file"])
            con = fd.read()
            fd.close()
            obj = None
            try:
                obj = eval(con)
            except Exception, e:
                print "ERROR: The figures description file is incorrect. the contant should consist of a single Python expression constructing a dictionary"
                return None
            options["descriptions_object"] = obj

    if not "tempdir" in options:
        options["tempdir"] = None

    if (not ("be_manager" in options)) and (not ("be_worker" in options)):
        # preparing the review directory
        basedirname = os.path.join(options["output_directory"], "review")
        dirname = os.path.join(options["output_directory"], "review", options["test_name"])

        if not os.path.exists(options["output_directory"]):
            os.mkdir(options["output_directory"])

        if not os.path.exists(basedirname):
            os.mkdir(basedirname)

        if not os.path.exists(dirname):
            os.mkdir(dirname)

        os.mkdir(os.path.join(dirname, "all"))
        os.mkdir(os.path.join(dirname, "overdetected"))
        os.mkdir(os.path.join(dirname, "overdetectedmany")) #overdetected with more than one misdetected figure
        os.mkdir(os.path.join(dirname, "underdetected"))
        os.mkdir(os.path.join(dirname, "underdetectedmany")) #underedetected with more than one figure missing

        options["review_dir"] = dirname
        options["review_dir_all"] = os.path.join(dirname, "all")
        options["review_dir_overdetected"] = os.path.join(dirname, "overdetected")
        options["review_dir_overdetectedmany"] = os.path.join(dirname, "overdetectedmany")
        options["review_dir_underdetected"] = os.path.join(dirname, "underdetected")
        options["review_dir_underdetectedmany"] = os.path.join(dirname, "underdetectedmany")

    return options


def get_input_files(options):
    """Produces next input file based on the configuration or none in the case,
       no more files should be processed
       yields paths to files that should be processed together with corresponding
       directoreis, wehre output should be placed

       @return yields tuples describing subsequent output files: (input_file_path, output_directory, file_name)
       """
    if "descriptions_file" in options:
        # There is a descriptions file, we must produce output only described there
        obj = options["descriptions_object"]
        for recid in obj.keys():
            for pageid in obj[recid].keys():
                inputfile =  "%i_%i.pdf" % (recid, pageid)

                outbasedir = os.path.join(options["output_directory"], str(recid) + "_" +  str(pageid))
                outdir = os.path.join(options["output_directory"],  str(recid) + "_" +  str(pageid), options["test_name"])
                if not os.path.exists(outbasedir):
                    os.mkdir(outbasedir)

                if not os.path.exists(outdir):
                    os.mkdir(outdir)

                yield (os.path.join(options["input_directory"], inputfile),
                       outdir, inputfile )

    else:
        if "input_file" in options:
            pure_name = os.path.splitext(os.path.split(options["input_file"])[1])[0]
            yield (options["input_file"],
                   os.path.join(options["output_directory"], pure_name,
                                options["test_name"]), options["input_file"])

        if "input_directory" in options:
            for entry in os.listdir(options["input_directory"]):
                file_name, ext = os.path.splitext(entry)
                if ext.lower() == ".pdf":
                    yield (os.path.join(options["input_directory"], entry),
                           os.path.join(options["output_directory"], file_name,
                                        options["test_name"]), entry)
        if "random" in options:
        #generate random samples in the output directory
            random_generator = random.Random()
            for sample_num in xrange(0, options["random"]):
            #download a sample !
                recid, path = retrieve_random_document(random_generator,
                                                       options["output_directory"])
                yield (path, os.path.join(options["output_directory"],  str(recid),
                                          options["test_name"]), str(recid))

def read_number_of_figures_from_output_dir(directory):
    """read a number of extracted figures from a provided path
    @rtype int
    @returns Number of detected figures
    """
    files = os.listdir(directory)
    num_fig = 0
    for fname in files:
        if re.match("plot[0-9]+\\.png", fname):
            num_fig += 1
    return num_fig

def verify_results_correctness(options, current_file):
    """Verify that the results of the extraction are compliant
    with the description provided at the input

    @param current_file Describes the currently tested files
    @type current_file tuple of strings (input_file_path, output_directory, file_name)
    @returns (if_successful, extracted_number, expected_number)
    """
    retrieved_number = read_number_of_figures_from_output_dir(os.path.join(current_file[1], current_file[2] + ".extracted"))
    # reading recid and page number from the file name
    match_result = re.match("([0-9]+)_([0-9]+).pdf", current_file[2])
    if not match_result or len(match_result.groups()) != 2 or int(match_result.groups()[0]) == 0 or int(match_result.groups()[1]) == 0:
        # incorrect name of the entry !!!
        raise Exception("Requested verification of results with incorrect file name path:%s output dir: %s entry name: %s " % current_file)

    recid = int(match_result.groups()[0])
    pagenum = int(match_result.groups()[1])

    try:
        expected_pagenum = options["descriptions_object"][recid][pagenum]
    except Exception, e:
        print "Key not found recid=%i pagenum=%i dict=%s" % (recid, pagenum, str(options["descriptions_object"]))
        raise e



    return (retrieved_number == expected_pagenum, retrieved_number, expected_pagenum)


def prepare_for_review(options, current_file, detected, expected):
    """In the case, an incorrect number of figures has been read, we want to make the manual review process easy,
    we create a directory with the summary of expected and obtained results
    @param current_file tuple describing the currently processed file (path, output_dir, name)
    """

    def _prepare_directory_for_review(reviewdir):
        sourcedir = os.path.join(current_file[1], current_file[2] + ".extracted")
        os.symlink(sourcedir, os.path.join(reviewdir, os.path.basename(sourcedir)))

    if detected != expected:
        #log in all
        _prepare_directory_for_review(options["review_dir_all"])

    if detected > expected:
        #detected too many
        _prepare_directory_for_review(options["review_dir_overdetected"])
        if detected - expected > 1:
            # detected way too many
            _prepare_directory_for_review(options["review_dir_overdetectedmany"])
    if expected > detected:
        #detected not enough
        _prepare_directory_for_review(options["review_dir_underdetected"])
        if expected - detected > 1:
            # missed a lot of figures
            _prepare_directory_for_review(options["review_dir_underdetectedmany"])



def perform_processing_local(parameters, results, stat_data):
    """ the function that performs the actuall processing of the input data"""
    for entry in get_input_files(parameters):
        print "Processing input file %s writing output to the directory %s" % (entry[0], entry[1])
        res = extract_file(entry[0], entry[1], parameters)
        results.append((entry ,(parameters, entry[2], res, stat_data)))


def perform_processing_controller(parameters, results, stat_data):
    """Perfomrs extraction as a controller of the server farm"""
    print "Running processing as controller"

    def prepare_requests():
        res = []
        for entry in get_input_files(parameters):

            res.append(ProcessingRequest(entry, os.path.split(entry[0])[1], input_file = entry[0], tempdir=parameters["tempdir"]))
        return res

    def makedirs(d):
        if d[-1] == "/":
            d = d[:-1]

        if not os.path.exists(d):
            makedirs(os.path.split(d)[0])
            os.mkdir(d)

    def process_result(result):
        """consume a single ProcessingResult object ... uncompress to the output directory and """
        output_dir = result.original_params[1]
        #make sure that teh output dir is there
        makedirs(output_dir)


        f = os.popen("tar -zxf %s -C %s --strip-components=1" % ( result.fileName, output_dir))
        f.read()
        f.close()

        entry = result.original_params
        res = result.results
        results.append((entry ,(parameters, entry[2], res, stat_data)))

    HOST, PORT = parameters["be_controller"]["address"], parameters["be_controller"]["port"]
    # SOCK_STREAM == a TCP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #sock.setblocking(0)  # optional non-blocking
    sock.connect((HOST, PORT))
    send_bytes(sock, "C")
    if recv_bytes(sock, 3) != "ACK":
        print "Connection to the resources manager refused..."
        sock.close()
        return
    f = open(jar_file_path, "r")
    jar_content = f.read()
    f.close()
    send_data(sock, jar_content)
    send_data(sock, hashlib.md5(jar_content).hexdigest())
    requests = prepare_requests()

    for res in requests:
        send_bytes(sock, "REQ")
        res.send_over_socket(sock)

    send_bytes(sock, "END")

    # now waiting for the results

    for i in range(len(requests)):

        result = ProcessingResult.read_from_socket(sock, parameters["tempdir"])
        print "Recieved result in: %s" % (result.fileName, )
        process_result(result)

    sock.close()

def resources_manager_main(port, parameters):
    print "Starting the resources manager server"

    ClientRequestHandler.setParameters(parameters)
    server = SocketServer.ThreadingTCPServer(('', port), ClientRequestHandler)
    server.serve_forever()

def worker_main(host, port, parameters):
    def process_request(req, temp_dir):
        """process a single extraction request and return results"""

        print "recieved request: args: %s file: %s " %( str(req.params), str(req.inputFileName))

        results = extract_file(req.inputFileName, os.path.join(temp_dir, "results"), req.params)

        # preparing compressed version of the temp

        tarfile = os.path.join(temp_dir, "results.tgz")
        f = os.popen("tar -czf %s -C %s results" % (tarfile, temp_dir))
        f.read()
        f.close

        f = open(tarfile, "r")
        file_content = f.read()
        f.close()

        # removing temporary directory
#        f = os.popen("rm -Rf %s" % (temp_dir, ))
#        f.read()
#        f.close()
        return {"data": results, "params" : req.params }, tarfile

    # SOCK_STREAM == a TCP socket
    print "Started a worker connected to host %s at port %i" % (host, port)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #sock.setblocking(0)  # optional non-blocking
    sock.connect((host, port))
    send_bytes(sock, "W") # we are a worker

    while True:
        command = recv_bytes(sock, 3)  # limit reply to 16K
        if command == "JAR":
            jar_file = recieve_data(sock)

            f = open(jar_file_path, "w")
            f.write(jar_file)
            f.close()
            print "UPDATED JAR archive. md5: %s" % (hashlib.md5(jar_file).hexdigest(),)

        elif command == "CMD":
            print "Recieved processing request"
            print "Recieving the input PDF file"

            if "tempdir" in parameters:
                temp_dir = tempfile.mkdtemp(dir=parameters["tempdir"])
            else:
                temp_dir = tempfile.mkdtemp()

            request = ProcessingRequest.read_from_socket(sock, folder=temp_dir, tempdir=parameters["tempdir"])
            # now process command using the obtained data
            output_data, tarfile = process_request(request, temp_dir)

            # now send results file (compressed results folder)
            tempdir = None
            if "tempdir" in parameters:
                tempdir = parameters["tempdir"]

            res = ProcessingResult(output_data["params"], output_data["data"], file_name = tarfile, tempdir = tempdir)
            res.send_over_socket(sock)
            print "FINISHED PROCESSINGFILE"


    sock.close()
    return reply

def main():
    parameters = parse_input(sys.argv[1:])
    if not parameters:
        usage()
        sys.exit()

    if "be_manager" in parameters:
        resources_manager_main(parameters["be_manager"], parameters)
        return

    if "be_worker" in parameters:
        worker_main(parameters["be_worker"]["address"], parameters["be_worker"]["port"], parameters)
        return

    if not os.path.exists(parameters["output_directory"]):
        create_directories(parameters["output_directory"])

    status_file = os.path.join(parameters["output_directory"], "status")

    stat_data = None

    expected_figures = 0
    cdetected_figures = 0
    icdetected_figures = 0
    processed_pages = 0
    correct_pages = 0

    results = []

    # the code for collecting results using
    if "be_controller" in parameters:
        perform_processing_controller(parameters, results, stat_data)
    else:
        perform_processing_local(parameters, results, stat_data)

    for res in results:
        print str(res)
        stat_data = include_in_statistics(res[1][0], res[1][1], res[1][2], res[1][3])
        # If we have correct data specified, we should verify the result
        if "descriptions_object" in parameters:
            processed_pages += 1
            correctly_extracted,extracted_num, expected_num  = verify_results_correctness(parameters, res[0])
            print "Returned %s %i %i\n" % (str(correctly_extracted), extracted_num, expected_num)
            if correctly_extracted:
                correct_pages += 1

            expected_figures += expected_num

            df = (extracted_num - expected_num)

            if df > 0:
                icdetected_figures += df
                cdetected_figures += expected_num
            else:
                cdetected_figures += extracted_num
            prepare_for_review(parameters, res[0], extracted_num, expected_num)




    finalise_statistics(parameters, stat_data)

    if "descriptions_object" in parameters:
        print """Statistics about the correctness of the extraction:
    Processed pages: %i
    Correctly extracted pages: %i
    Expected figures: %i
    Correctly detected figures: %i
    Misdetected figures: %i""" % (processed_pages, correct_pages,  expected_figures, cdetected_figures, icdetected_figures)

if __name__ == "__main__":
    main()
