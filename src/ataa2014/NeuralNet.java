package ataa2014;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.TransferFunctionType;

public class NeuralNet {
	public static void main(String[] args) {
		train2();
		// load the saved network
		//NeuralNetwork neuralNetwork = NeuralNetwork.createFromFile("and_perceptron.nnet");
		NeuralNetwork neuralNetwork = NeuralNetwork.createFromFile("multilayer_perceptron.nnet");
		// set network input
		neuralNetwork.setInput(0,0);
		// calculate network
		neuralNetwork.calculate();
		// get network output
		double[] networkOutput = neuralNetwork.getOutput();
		System.out.println(Math.round(networkOutput[0]));
	}
	
	public static void train2 () {
	    int maxIterations = 100000;
		
		NeuralNetwork neuralNet = new MultiLayerPerceptron(TransferFunctionType.TANH, 2, 2, 1);

	    ((LMS)neuralNet.getLearningRule()).setMaxError(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setLearningRate(0.7);//0-1
	    ((LMS)neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
	    
		DataSet trainingSet = new DataSet(2, 1);	
		
		trainingSet.addRow(new DataSetRow(new double[]{0, 0}, new double[]{0}));
		trainingSet.addRow(new DataSetRow(new double[]{0, 1}, new double[]{1}));
		trainingSet.addRow(new DataSetRow(new double[]{1, 0}, new double[]{1}));
		trainingSet.addRow(new DataSetRow(new double[]{1, 1}, new double[]{0}));
	    
	    neuralNet.learn(trainingSet);
	    
	    //DataSet testSet = new DataSet(maxIterations);
	    
	    //testSet.addRow(new double[]{4223.0D / daxmax, 4259.0D / daxmax, 4203.0D / daxmax, 3989.0D / daxmax});
	    
	    neuralNet.save("multilayer_perceptron.nnet");
	}
	
	public static void train () {
		// create new perceptron network
		NeuralNetwork neuralNetwork = new Perceptron(2, 1);
		// create training set
		DataSet trainingSet = new DataSet(2, 1);
		// add training data to training set (logical AND function)
		trainingSet.addRow(new DataSetRow(new double[]{0, 0}, new double[]{0}));
		trainingSet.addRow(new DataSetRow(new double[]{0, 1}, new double[]{0}));
		trainingSet.addRow(new DataSetRow(new double[]{1, 0}, new double[]{0}));
		trainingSet.addRow(new DataSetRow(new double[]{1, 1}, new double[]{1}));
		// learn the training set
		neuralNetwork.learn(trainingSet);
		// save the trained network into file
		neuralNetwork.save("and_perceptron.nnet");
	}
}
