package ataa2014;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.TransferFunctionType;

import com.mojang.mario.LevelScene;

public class NeuralNet implements Runnable{
	private ArrayList <Double> outputList;
	private ArrayList <double[]> sampleList;
	
	private boolean newSamples;

	private NeuralNetwork neuralNet;
	
	public NeuralNet(){
		
		outputList = new ArrayList <Double>();
		sampleList = new ArrayList <double[]>();
		
		int maxIterations = 100;
		
		neuralNet = new MultiLayerPerceptron(TransferFunctionType.TANH, 40, 50, 1);

	    ((LMS)neuralNet.getLearningRule()).setMaxError(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setLearningRate(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
	}
	
	/*public static void main(String[] args) {
		train2();
		// load the saved network
		//NeuralNetwork neuralNetwork = NeuralNetwork.createFromFile("and_perceptron.nnet");
		//NeuralNetwork neuralNetwork = NeuralNetwork.createFromFile("multilayer_perceptron.nnet");
		// set network input
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

		neuralNet.setInput(0,0);
		// calculate network
		neuralNet.calculate();
		// get network output
		double[] networkOutput = neuralNet.getOutput();
		System.out.println(Math.round(networkOutput[0]));
	}
	
	/*public static void train2 () {
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
	}*/
	
	
	
	public double train(){
		
	    
		DataSet trainingSet = new DataSet(40, 1);	
		//System.out.println("data size:"+ sampleList.size() );
		for (int i = 0; i < sampleList.size(); i++){
			trainingSet.addRow(new DataSetRow(sampleList.get(i), new double[]{outputList.get(i)}));
		}
	   
	    neuralNet.learn(trainingSet);
	    
	    return neuralNet.getOutput()[0];
		
	}
	
	public void addSample(double[] features, double[] actions, double output){
		
		double [] sample = new double[features.length + actions.length];
		for (int i = 0; i < sample.length; i++){
			if (i < features.length) sample[i] = features[i];
			else sample[i] = actions[i - features.length];
			
		}
		sampleList.add(sample);
		outputList.add(output);
	}
	
	public double getOutput(double[] features, double[] actions){
		double [] sample = new double[features.length + actions.length];
		for (int i = 0; i < sample.length; i++){
			if (i < features.length) sample[i] = features[i];
			else sample[i] = actions[i - features.length];
		}
		
		neuralNet.setInput(sample);
		neuralNet.calculate();
		return neuralNet.getOutput()[0];
		
	}

	
	
	private int tempSize = 0;
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("sampleList: "+ sampleList.size()+ "tempSize" + tempSize);
		while (true){
			
		System.out.println(sampleList.size()+" "+tempSize);
		if (sampleList.size() > tempSize) {
			System.out.println("TRUE");
		
			tempSize = sampleList.size();
			train();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
			
	}
	/*public static void train () {
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
	*/

	
}
