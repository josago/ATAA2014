import numpy as np
import matplotlib.pyplot as plt
import os, re

NUM_STEPS_PER_SAMPLE   = 400

NUM_LEVELS_FINISHED    = 0
NUM_TIMES_DIED         = 1
NUM_COINS_EARNED       = 2
NUM_ENEMIES_KILLED     = 3
NUM_POWER_UPS_RECEIVED = 4
NUM_BLOCKS_TRAVELLED   = 5

LABELS = ["Levels finished", "Times died", "Coins earned (x5)", "Enemies killed", "Power-ups received", "Blocks travelled (x100)"]

def plot_one():
    filename = 'resultsATAA_NeuralNet_StateRepresentation_Lydia_longRun_learningrate0.1_simHuman2_lvl1.txt'
    results  = load_file(filename)
	
    plt.title('Is mario now freaking learning?')

    plt.xlabel("Game steps elapsed")
    plt.ylabel("Performance measures")

    plt.xticks(np.arange(0, 1 + (len(results[0]) - 1) * NUM_STEPS_PER_SAMPLE, NUM_STEPS_PER_SAMPLE))    
    plt.ylim(0, 7)
    
    for var in range(len(results)):
        xdata = np.arange(len(results[var])) * NUM_STEPS_PER_SAMPLE
        
        if var is NUM_COINS_EARNED:
            plt.plot(xdata, results[var] / 5, '-o', label = LABELS[var])
        elif var is NUM_BLOCKS_TRAVELLED:
            plt.plot(xdata, results[var] / (16 * 100), '-o', label = LABELS[var])
        else:
            plt.plot(xdata, results[var], '-o', label = LABELS[var])
    
    plt.legend(loc = 'upper center', bbox_to_anchor = (0.5, 1.00), ncol = 3, fancybox = True, shadow = True)
    
    plt.show()

def plot_all(user = None):
    i = 1
    
    for type_model in ("WekaModelPerActionModel", "NeuralNet"):
        for type_features in ("FeatGen_Mario", "StateRepresentation"):
            plt.subplot(2, 2, i)
            
            i += 1
            
            plot_results(type_model, type_features, user, show = False)
    
    plt.show()

def plot_results(type_model, type_features, user = None, show = True):
    results = load_experiment(type_model, type_features, user)

    plt.title(type_model + ", " + type_features)

    plt.xlabel("Game steps elapsed")
    plt.ylabel("Performance measures")

    plt.xticks(np.arange(0, 1 + (len(results[0]) - 1) * NUM_STEPS_PER_SAMPLE, NUM_STEPS_PER_SAMPLE))    
    plt.ylim(0, 7)
    
    for var in range(len(results)):
        xdata = np.arange(len(results[var])) * NUM_STEPS_PER_SAMPLE
        
        if var is NUM_COINS_EARNED:
            plt.plot(xdata, results[var] / 5, '-o', label = LABELS[var])
        elif var is NUM_BLOCKS_TRAVELLED:
            plt.plot(xdata, results[var] / (16 * 100), '-o', label = LABELS[var])
        else:
            plt.plot(xdata, results[var], '-o', label = LABELS[var])
    
    plt.legend(loc = 'upper center', bbox_to_anchor = (0.5, 1.00), ncol = 3, fancybox = True, shadow = True)
    
    if show:
        plt.show()

def load_experiment(type_model, type_features, user = None):
    filenames = os.listdir('.')
    
    num_runs = 0
    results  = None
    
    for filename in filenames:
        if (user is None and re.search(type_model + "_" + type_features, filename) is not None) or (user is not None and re.search(type_model + "_" + type_features + "_" + user, filename) is not None):
            num_runs += 1
            
            if results is None:
                results  = load_file(filename)
            else:
                results += load_file(filename)
            
    return results / num_runs

def load_file(filename):
    f = open(filename, 'r')
    
    data = []
    
    for line in f:
        numbers = map(float, re.split("\s+", line.rstrip()))
        
        data.append(numbers)
        
    return np.array(data)

# Code to be exectued:

plot_all()
#plot_one()
#plot_results("WekaModelPerActionModel", "FeatGen_Mario")
