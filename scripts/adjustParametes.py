#
#  An algorithm trying to adjust parameters of the extractor one by one (unlike a genetic algorithm)
#  We specify ranges and type (int, float) of all the parameters, order them in the importance order
#  Then we divide a parameter space into 1/10 slices and find optimal value ... subsequently we search
#  2 adjacent intervals dividing them in 10 pieces to find a more fine-grained maximum
#  As a quality function we use recall+precission

# Unlike in the genetic algorithm, we assume that parameters are independent.
# We set initial optimum to manually adjusted values and search for better results.
# Algorithm can be rerun afterwards on the results of previous execution

optimisable_parameters = [
    ["minimal_graphical_area_fraction", "float", 0, 1, 0.15],
    ["minimal_figure_width", "float", 0, 1, 0.15],
    ["minimal_column_width", "float", 0, 1, 0.25],
    ["minimal_figure_operations_number", "integer", 0, 1000, 10],
    ["minimal_vertical_separator_height", "float", 0, 1, 0.4],
    ["minimal_figure_graphical_operations_fraction", "float", 0, 1, 0.10],
    ["vertical_graphical_margin", "float", 0, 0.1, 0.02],
    ["vertical_emptiness_radius", "float", 0, 0.05, 0.005],
    ["maximum_non_breaking_fraction", "float", 0, 0.05, 0.005],
    ["vertical_plot_text_margin", "float", 0, 0.005, 0.00065],
    ["horizontal_text_margin", "float", 0, 0.5, 0.05],
    ["minimal_margin_width", "float", 0, 1, 0.3],
    ["colour_emptiness_threshold", "integer", 0, 255, 10],
    ["minimal_figure_height", "float", 0, 0.5, 0.1],
    ["horizontal_graphical_margin", "float", 0, 1, 0.1],
    ["minimal_aspect_ratio", "float", 0, 1, 0.1],
    ["horizontal_emptiness_radius", "float", 0, 0.1, 0.01],
    ["vertical_text_margin", "float", 0, 0.1, 0.005],
    ["horizontal_plot_text_margin", "float", 0, 0.1, 0.01],
    ["minimal_figure_graphical_operations_number", "integer", 0, 100, 4],
    ["maximal_inclusion_height", "float", 0, 1, 0.10]
]

# arguments outside of optimization - to be included in teh configuration
ignored_arguments = [("generate_debug_information", "false"),
                     ("generate_svg", "true"),
                     ("page_scale", "2"),
                     ("empty_pixel_colour_r","255"),
                     ("generate_plot_provenance","true"),
                     ("empty_pixel_colour_g","255"),
                     ("empty_pixel_colour_b","255")]


def calculateQuality(precission, recall):
    pass

def writeConfiguration(parameters, fileName):
    f = open(fileName, "w")
    for param in parameters:
        f.write("%s=%s\n" % (param[0], str(param[1])))
    f.close()

def runTest(parameters, prefix=""):
    """Executes a single test and return the evaluation measure - pair recall, precission"""
    return 0,0

def generateParameterValues(parameter):
    step = (float(parameter[3]) - float(parameter[2])) / 10
    result = [float(parameter[2]) + x*step for x in xrange(11)]
    if not float(parameter[4]) in result:
        result = result + [parameter[4]]
        result.sort()

    if parameter[1] == "integer":
        # in this case we remove duplicates and convert to integers
        result = [int(x) for x in result]
        new_result = []
        prev = None
        for element in result:
            if prev != element:
                new_result.append(element)
                prev = element
        result = new_result
    return result

def optimiseParameter(parameter, otherParameters, recDepth=0):
    if recDepth == 0:
        print "*Beginning the optimisation of a new parameter %s of the type %s and values in the interval [%s, %s]" % (parameter[0], parameter[1], str(parameter[2]), str(parameter[3]))
    opt_ind = None
    min_rec, min_prec = -1, -1

    possibleValues = generateParameterValues(parameter)
    for val_ind in xrange(len(possibleValues)):
        print ("***Testing the parameter value %s=%s" % (parameter[0], possibleValues[val_ind])),
        act_rec, act_prec = runTest(otherParameters + [(parameter[0], str(possibleValues[val_ind]))])
        print "  recall=%s, precission=%s" % (str(act_rec), str(act_prec))
        if act_rec + act_prec > min_rec + min_prec:
            opt_ind = val_ind
            min_rec = act_rec
            min_prec = act_prec

    opt_val = possibleValues[opt_ind]

    if recDepth < 1:
        if opt_ind == 0:
            new_min = possibleValues[0]
            new_max = possibleValues[1]
        elif opt_ind == len(possibleValues) -1:
            new_min =  possibleValues[-2]
            new_max = possibleValues[-1]
        else:
            new_min = possibleValues[opt_ind -1]
            new_max = possibleValues[opt_ind + 1]
        opt_val, min_rec, min_prec = optimiseParameter([parameter[0], parameter[1],new_min, new_max, possibleValues[opt_ind]], otherParameters, recDepth+1)

    if recDepth == 0:
        print "*finished optimising the parameter. The optimal value is %s=%s yielding recall=%s, precission=%s" % (parameter[0], opt_val, min_rec, min_prec)
    return opt_val, min_rec, min_prec

def transformToNormalParameters(params):
    """Takes a detailed list of parameters (containing types and intervals) and transforms
    into key, value pairs"""
    return map(lambda x: (x[0], x[4]), params)

def main():
    for pind in xrange(len(optimisable_parameters)):
        remaining = transformToNormalParameters(optimisable_parameters[:pind] + optimisable_parameters[pind+1:]) + ignored_arguments
        optimal_value, optimal_recall, optimal_precission = optimiseParameter(optimisable_parameters[pind], remaining)
        optimisable_parameters[pind][4] = optimal_value

    print "optimal solution found !"
    print str(transformToNormalParameters(optimisable_parameters))

if __name__=="__main__":
    main()
