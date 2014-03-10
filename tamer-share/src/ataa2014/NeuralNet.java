package ataa2014;

import java.util.ArrayList;
import java.util.Arrays;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.TransferFunction;
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
	public static final double LEARNING_RATE = 0.01;
	
	private ArrayList<Double>   outputList;
	private ArrayList<double[]> sampleList;

	private MultiLayerPerceptron neuralNet;
	
	private final int num_inputs;
	private final int num_hidden;
	
	private class TransferFunctionCustom extends TransferFunction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public double getOutput(double net)
		{
			return 10 * Math.tanh(net / 2);
		}
		
		@Override
		public double getDerivative(double net)
		{
			return 5 * Math.pow((2 * Math.cosh(net / 2)) / (Math.cosh(net) + 1), 2);
		}
	}
	
	public NeuralNet(int num_inputs, int num_hidden)
	{
		System.out.println("Initializing neural network with " + num_inputs + " inputs, " + num_hidden + " hidden nodes and 1 output.");
		
		this.num_inputs = num_inputs;
		this.num_hidden = num_hidden;
		
		clearSamplesAndReset();
	}
	
	@Override
	public void addInstance(Sample sample)
	{
		//if (sample.label != 0.0)
		//{
			boolean found = false;
			
			for (int i = 0; i < sampleList.size(); i++)
			{
				if (Arrays.equals(sample.feats, sampleList.get(i)))
				{
					//outputList.set(i, (outputList.get(i) + sample.label) / 2); // Weighted average.
					outputList.set(i, sample.label); // Latest value.
					
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				sampleList.add(sample.feats);
				outputList.add(sample.label);
			}
			
			//System.err.println("NN adding instance " + sample.label + " to a list of size " + sampleList.size());
		//}
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
		for (Sample s: samples)
		{
			addInstance(s);
		}
		
	}

	@Override
	public void buildModel()
	{
		DataSet trainingSet = new DataSet(num_inputs, 1);	
		
		for (int i = 0; i < sampleList.size(); i++){
			trainingSet.addRow(new DataSetRow(sampleList.get(i), new double[]{outputList.get(i)}));
		}
	   
	    neuralNet.learnInNewThread(trainingSet);
	    
	    try
	    {
			Thread.sleep(1000 / 24);
		}
	    catch (InterruptedException e)
		{
	    	
		}
	    
	    neuralNet.stopLearning();
	}

	@Override
	public double predictLabel(double[] feats)
	{	
		neuralNet.setInput(feats);
		neuralNet.calculate();
		//System.out.println("NN output: " + neuralNet.getOutput()[0]);
		return neuralNet.getOutput()[0];
	}

	@Override
	public void clearSamplesAndReset()
	{
		outputList = new ArrayList <Double>();
		sampleList = new ArrayList <double[]>();
		
		neuralNet = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, num_inputs, num_hidden, 1);
		neuralNet.randomizeWeights();
		
		neuralNet.getLayerAt(2).getNeuronAt(0).setTransferFunction(new TransferFunctionCustom());
		
	    ((LMS) neuralNet.getLearningRule()).setMaxError(0.001);
	    ((LMS) neuralNet.getLearningRule()).setLearningRate(LEARNING_RATE);
	    ((LMS) neuralNet.getLearningRule()).setMaxIterations(Integer.MAX_VALUE);
	}
}