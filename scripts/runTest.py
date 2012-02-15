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

if __name__ == "__main__":
    parameters = parse_input(sys.argv[1:])
    if not parameters:
        usage()
        sys.exit()

    if not os.path.exists(parameters["output_directory"]):
        create_directories(parameters["output_directory"])

    status_file = os.path.join(parameters["output_directory"], "status")

    stat_data = None

    for entry in get_input_files(parameters):
        print "Processing input file %s writing output to the directory %s" % (entry[0], entry[1])
        res = extract_file(entry[0], entry[1], parameters)
        stat_data = include_in_statistics(parameters, entry[2], res, stat_data)
    finalise_statistics(parameters, stat_data)



