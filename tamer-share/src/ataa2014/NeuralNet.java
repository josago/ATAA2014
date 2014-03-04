package ataa2014;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.LMS;
import org.neuroph.util.TransferFunctionType;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;

/**
 * This class implements a neural network to be used as a regression model to learn the H function within the TAMER framework.
 * @author josago
 */
public class NeuralNet extends RegressionModel
{
	private ArrayList <Double> outputList;
	private ArrayList <double[]> sampleList;
	
	private boolean newSamples;

	private NeuralNetwork neuralNet;
	
	private int num_inputs;
	private int num_hidden;
	
	private int maxIterations = 100;
	
	public NeuralNet(int num_inputs, int num_hidden)
	{
		
		this.num_inputs = num_inputs;
		this.num_hidden = num_hidden;
		
		outputList = new ArrayList <Double>();
		sampleList = new ArrayList <double[]>();
		
		neuralNet = new MultiLayerPerceptron(TransferFunctionType.TANH, num_inputs, num_hidden, 1);

	    ((LMS)neuralNet.getLearningRule()).setMaxError(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setLearningRate(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
	    
	}
	
	@Override
	public void addInstance(Sample sample)
	{	
		double[] attributes = sample.getAttributes(); // features + label as last element
		double[] features = new double[attributes.length - 1];
		
		for (int i = 0; i < attributes.length-1 ; i++){
			features[i] = attributes[i];
			
		}
		sampleList.add(features);
		outputList.add(attributes[attributes.length - 1]); 
		
		
	}

	@Override
	public void addInstances(Sample[] samples)
	{
		for (Sample s: samples)
		{
			addInstance(s);
		}
	}

	@Override
	public void addInstancesWReplacement(Sample[] samples)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void buildModel()
	{
		DataSet trainingSet = new DataSet(num_inputs, 1);	
		//System.out.println("data size:"+ sampleList.size() );
		for (int i = 0; i < sampleList.size(); i++){
			trainingSet.addRow(new DataSetRow(sampleList.get(i), new double[]{outputList.get(i)}));
		}
	   
	    neuralNet.learn(trainingSet);
	    
	    // return neuralNet.getOutput()[0];
		
	}

	@Override
	public double predictLabel(double[] feats)
	{	
		neuralNet.setInput(feats);
		neuralNet.calculate();
		return neuralNet.getOutput()[0];
	}

	@Override
	public void clearSamplesAndReset()
	{
		outputList = new ArrayList <Double>();
		sampleList = new ArrayList <double[]>();
		
		
		neuralNet = new MultiLayerPerceptron(TransferFunctionType.TANH, num_inputs, num_hidden, 1);

	    ((LMS)neuralNet.getLearningRule()).setMaxError(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setLearningRate(0.001);//0-1
	    ((LMS)neuralNet.getLearningRule()).setMaxIterations(maxIterations);//0-1
		
	}
}