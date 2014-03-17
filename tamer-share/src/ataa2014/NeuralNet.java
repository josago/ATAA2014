package ataa2014;

import java.util.ArrayList;
import java.util.Arrays;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.*;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;

/**
 * This class implements a neural network to be used as a regression model to learn the H function within the TAMER framework.
 * @author josago
 */
public class NeuralNet extends RegressionModel
{
	public static final double LEARNING_RATE = 0.0001;
	public static final double MOMENTUM_RATE = 0.01;
	
	public static final int MAX_ITERATIONS = 1000;
	
	private ArrayList<Double>   outputList;
	private ArrayList<double[]> sampleList;

	private MultiLayerPerceptron neuralNet;
	
	private final int num_inputs;
	private final int num_hidden;
	
	private final boolean binary_labels;
	
	private class Sigmoid extends TransferFunction
	{
		private static final long serialVersionUID = 1L;
		
		private final double factor;
		
		public Sigmoid(double factor)
		{
			this.factor = factor;
		}

		@Override
		public double getOutput(double net)
		{
			double o = factor * (net / (3 * Math.sqrt(1 + Math.pow(net / 3, 2))));
				
			if (Double.isNaN(o))
			{
				if (net == Double.POSITIVE_INFINITY || net > 0)
				{
					return 1;
				}
				else
				{
					return -1;
				}
			}
			//System.out.println("Output: " + o);
			return o;
		}
		
		@Override
		public double getDerivative(double net)
		{
			double d = (9 * factor) / (Math.pow(9 + Math.pow(net, 2), 3.0 / 2.0));
			
			if (Double.isNaN(d))
			{
				return 0;
			}
			//System.out.println("Derivative: " + d);
			return d;
		}
	}
	
	public NeuralNet(int num_inputs, int num_hidden, boolean binary_labels)
	{
		//System.out.println("Initializing neural network with " + num_inputs + " inputs, " + num_hidden + " hidden nodes and 1 output.");
		
		this.num_inputs = num_inputs;
		this.num_hidden = num_hidden;
		
		this.binary_labels = binary_labels;
		
		clearSamplesAndReset();
	}
	
	@Override
	public void addInstance(Sample sample)
	{
		if (binary_labels && sample.label != 0)
		{
			sample.label = sample.label > 0 ? 1 : -1;
		}
		
		//if (sample.label != 0.0)
		//{
			boolean found = false;
			
			for (int i = 0; i < sampleList.size(); i++)
			{
				if (Arrays.equals(sample.feats, sampleList.get(i)))
				{
					outputList.set(i, (outputList.get(i) + sample.label) / 2); // Weighted average.
					//outputList.set(i, new Double(sample.label)); // Latest value.
					
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				sampleList.add(sample.feats);
				outputList.add(new Double(sample.label));
			}
			
			//System.err.println("Neural network added instance with label = " + sample.label + " to a list of size " + sampleList.size());
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
		
		for (int i = 0; i < sampleList.size(); i++)
		{
			trainingSet.addRow(new DataSetRow(sampleList.get(i), new double[]{outputList.get(i)}));
		}

		resetNetwork();
	    neuralNet.learnInNewThread(trainingSet);
	    
	    try
	    {
			Thread.sleep(1000 / 8);
		}
	    catch (InterruptedException e)
		{
	    	
		}
	    
	    neuralNet.stopLearning();
	}

	@Override
	public double predictLabel(double[] feats)
	{
		/*for (int i = 0; i < neuralNet.getWeights().length; i++)
		{
			System.out.print(feats[i] + ", ");
			System.out.print(neuralNet.getWeights()[i] + ", ");
		}
		
		System.out.println();*/
		
		neuralNet.setInput(feats);
		neuralNet.calculate();
		//System.out.println("Neural network output for the action (" + feats[0] + ", " + feats[1] + ", " + feats[2] + "): " + neuralNet.getOutput()[0]);
		return neuralNet.getOutput()[0];
	}

	@Override
	public void clearSamplesAndReset()
	{
		System.err.println("\nClear samples and reset in Neural Network\n");
		clearSamples();
		resetNetwork();
	}
	
	private void clearSamples()
	{
		outputList = new ArrayList<Double>();
		sampleList = new ArrayList<double[]>();
	}
	
	private void resetNetwork()
	{
		neuralNet = new MultiLayerPerceptron(num_inputs, num_hidden, 1);
		
		for (int i = 0; i < num_hidden; i++)
		{
			neuralNet.getLayerAt(1).getNeuronAt(i).setTransferFunction(new Sigmoid(1));
		}
		
		if (binary_labels)
		{
			neuralNet.getLayerAt(2).getNeuronAt(0).setTransferFunction(new Sigmoid(1));
		}
		else
		{
			neuralNet.getLayerAt(2).getNeuronAt(0).setTransferFunction(new Sigmoid(10));
		}
		
		MomentumBackpropagation mbp = new MomentumBackpropagation();
		
		mbp.setMaxError(0.01);
		mbp.setLearningRate(LEARNING_RATE);
		mbp.setMomentum(MOMENTUM_RATE);
		mbp.setMaxIterations(MAX_ITERATIONS);
		
		neuralNet.setLearningRule(mbp);
	}
	
	public void reset()
	{
		System.out.println("Reset neural network");
	}
}