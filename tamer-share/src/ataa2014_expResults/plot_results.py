import numpy as np
import matplotlib.pyplot as plt
import os, re

PLOT_REL_SPEED        = True
NUM_STEPS_PER_SAMPLE  = 100
MAX_Y                 = 5.5
SPEED_MULT            = 50

NUM_LEVELS_FINISHED    = 0
NUM_TIMES_DIED         = 1
NUM_COINS_EARNED       = 2
NUM_ENEMIES_KILLED     = 3
NUM_POWER_UPS_RECEIVED = 4
NUM_BLOCKS_TRAVELLED   = 5

LABELS = ["Levels finished", "Times died", "Coins earned", "Enemies killed", "Power-ups received", "Blocks travelled (x" + str(SPEED_MULT) + ")"]

def plot_all(user = None):
    i = 1
    
    #for type_model in ("WekaModelPerActionModel", "NeuralNet", "NeuralNetWorldModel"):
        #for type_features in ("FeatGen_Mario", "StateRepresentation"):
        #plt.subplot(3, 2, i)
    for type_model in ("NeuralNet", "NeuralNetWorldModel"):
            plt.subplot(2, 1, i)
            
            i += 1
            
            plot_results(type_model, "StateRepresentation", user, show = False)
    
    plt.show()

def plot_results(type_model, type_features, user = None, show = True):
    results = load_experiment(type_model, type_features, user)

    plt.title(type_model + ", " + type_features)

    plt.xlabel("Game steps elapsed")
    plt.ylabel("Performance measures")

    plt.xticks(np.arange(0, 1 + len(results[0]) * NUM_STEPS_PER_SAMPLE, NUM_STEPS_PER_SAMPLE))  
    plt.xlim(NUM_STEPS_PER_SAMPLE, 1 + len(results[0]) * NUM_STEPS_PER_SAMPLE)
    plt.ylim(0, MAX_Y)
    
    for var in range(len(results)):
        xdata = np.arange(len(results[var])) * NUM_STEPS_PER_SAMPLE + NUM_STEPS_PER_SAMPLE
        
        if not PLOT_REL_SPEED:
            if var is NUM_BLOCKS_TRAVELLED:
                plt.plot(xdata, results[var] / (16 * SPEED_MULT), '-o', label = LABELS[var])
            elif var is not NUM_COINS_EARNED:
                plt.plot(xdata, results[var], '-o', label = LABELS[var])
        else:
            if var is NUM_BLOCKS_TRAVELLED:
                plt.plot(xdata, results[var] / (16 * SPEED_MULT), '-o', label = LABELS[var])
            elif var is not NUM_COINS_EARNED:
                plt.plot(xdata, (results[var] * 16 * SPEED_MULT) / results[NUM_BLOCKS_TRAVELLED], '-o', label = LABELS[var] + " / Blocks travelled")
    
    plt.legend(loc = 'upper center', bbox_to_anchor = (0.5, 1.00), ncol = 3, fancybox = True, shadow = True)
    
    if show:
        plt.show()

def load_experiment(type_model, type_features, user = None):
    filenames = os.listdir('.')
    
    num_runs = 0
    results  = None
    
    for filename in filenames:
        if (user is None and re.search(type_model + "_" + type_features, filename) is not None) or (user is not None and re.search(type_model + "_" + type_features + "_.*" + user, filename) is not None):
            num_runs += 1
            
            if results is None:
                results  = load_file(filename)
            else:
                results += load_file(filename)
            
    return results / num_runs

def load_experiment_modelParams(user = None):
    filenames = os.listdir('.')
    
    num_runs = 0
    results  = None
    
    for filename in filenames:
        if (user is None and re.search('ModelParamResults_', filename) is not None) or (user is not None and re.search('ModelParamResults_' + user, filename) is not None):
            num_runs += 1
            
            if results is None:
                results  = load_file(filename)
            else:
                results += load_file(filename)
            
    return results / num_runs    
    
def results_model_param(user = None):
    data = load_experiment_modelParams(user)
    if user == None:
        user = 'noUser'
    filename = 'accumulated_modelParamResults_' + user + '.txt'
    f = open(filename, 'w')
    for row in data:
        for value in row:
            f.write(str(value))
            f.write(' ')
        f.write('\n')
    f.close()   
    
def load_file(filename):
    f = open(filename, 'r')
    
    data = []
    
    for line in f:
        numbers = map(float, re.split("\s+", line.rstrip()))
        
        data.append(numbers)
        
    return np.array(data)

# Code to be exectued:

#plot_results("NeuralNet", "StateRepresentation", "day20")
plot_all("lvl_1")
#results_model_param('LYDIA')
