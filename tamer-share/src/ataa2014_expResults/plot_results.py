import numpy as np
import matplotlib.pyplot as plt
import os, re

NUM_STEPS_PER_SAMPLE   = 200

NUM_LEVELS_FINISHED    = 0
NUM_TIMES_DIED         = 1
NUM_COINS_EARNED       = 2
NUM_ENEMIES_KILLED     = 3
NUM_POWER_UPS_RECEIVED = 4

LABELS = ["Levels finished", "Times died", "Coins earned (x10)", "Enemies killed", "Power-ups received"]

def plot_all():
    i = 1
    
    for type_model in ("WekaModelPerActionModel", "NeuralNet"):
        for type_features in ("FeatGen_Mario", "StateRepresentation"):
            plt.subplot(2, 2, i)
            
            i += 1
            
            plot_results(type_model, type_features, show = False)
    
    plt.show()

def plot_results(type_model, type_features, show = True):
    results = load_experiment(type_model, type_features)

    plt.title(type_model + ", " + type_features)

    plt.xlabel("Game steps elapsed")
    plt.ylabel("Performance measures")
    
    for var in range(len(results)):
        xdata = np.arange(len(results[var])) * NUM_STEPS_PER_SAMPLE
        
        if var is NUM_COINS_EARNED:
            plt.plot(xdata, results[var] / 10, '-o', label = LABELS[var])
        else:
            plt.plot(xdata, results[var], '-o', label = LABELS[var])
    
    plt.legend()
    
    if show:
        plt.show()

def load_experiment(type_model, type_features):
    filenames = os.listdir('.')
    
    num_runs = 0
    results  = None
    
    for filename in filenames:
        if re.search(type_model + "_" + type_features, filename) is not None:
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

#plot_all()
plot_results("NeuralNet", "FeatGen_Mario")