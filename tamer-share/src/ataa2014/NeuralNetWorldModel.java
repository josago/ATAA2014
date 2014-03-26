package ataa2014;

import java.util.ArrayList;
import java.util.Arrays;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;

/**
 * This class implements a neural network to be used as a regression model to learn the H function within the TAMER framework.
 * @author josago
 */
public class NeuralNetWorldModel extends RegressionModel
{
	public static final double LEARNING_RATE = 0.001;
	public static final double MOMENTUM_RATE = 0;
	
	public static final double MIN_ERROR      = 0.001;
	public static final int    MAX_ITERATIONS = Integer.MAX_VALUE;
	
	public static final int LOOKAHEAD_NEURONS     = 20;  // Number of hidden neurons to use for the world model.
	public static final int LOOKAHEAD_MAX_LEVELS  = 1;   // Maximum number of levels to lookahead into the future when planning an action.
	public static final int LOOKAHEAD_MIN_SAMPLES = 100; // Minimum number of samples needed before the world model is queried and effectively used.
	
	private ArrayList<Double>   outputList;
	private ArrayList<double[]> sampleList;

	private MultiLayerPerceptron neuralNetReward;
	private MultiLayerPerceptron neuralNetWorld;
	
	private final int num_inputs;
	private final int num_actions;
	private final int num_hidden;
	
	private class Linear extends TransferFunction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public double getOutput(double net)
		{
			return net;
		}
		
		@Override
		public double getDerivative(double net)
		{
			return 1;
		}
	}
	
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
	
	public NeuralNetWorldModel(int num_inputs, int num_actions, int num_hidden)
	{
		//System.out.println("Initializing neural network with " + num_inputs + " inputs, " + num_hidden + " hidden nodes and 1 output.");
		
		this.num_inputs  = num_inputs;
		this.num_actions = num_actions;
		this.num_hidden  = num_hidden;

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
		DataSet trainingSetReward = new DataSet(num_inputs, 1);
		DataSet trainingSetWorld  = new DataSet(num_inputs, num_inputs - num_actions);
		
		for (int i = 0; i < sampleList.size(); i++)
		{
			trainingSetReward.addRow(new DataSetRow(sampleList.get(i), new double[]{outputList.get(i)}));
			
			if (i < sampleList.size() - 1)
			{
				trainingSetWorld.addRow(new DataSetRow(sampleList.get(i), Arrays.copyOfRange(sampleList.get(i + 1), num_actions, num_inputs)));
			}
		}

		//resetNetworks();
		
		neuralNetReward.learnInNewThread(trainingSetReward);
		
		//long start = System.nanoTime();
		neuralNetWorld.learnInNewThread(trainingSetWorld);
		//neuralNetWorld.learn(trainingSetWorld);
		//System.out.println("N = " + sampleList.size() + ", t = " + (System.nanoTime() - start) / 1000000L);
	    
	    try
	    {
			Thread.sleep(1000 / 4);
		}
	    catch (InterruptedException e)
		{
	    	
		}
	    
	    neuralNetReward.stopLearning();
		neuralNetWorld.stopLearning();
		
		// Printing the weights:
		
		for (int i = 0; i < neuralNetWorld.getWeights().length; i++)
		{
			System.out.print(neuralNetWorld.getWeights()[i] + " ");
		}
		System.out.println();
	}

	@Override
	public double predictLabel(double[] feats)
	{
		if (sampleList.size() >= LOOKAHEAD_MIN_SAMPLES)
		{
			if (sampleList.size() == LOOKAHEAD_MIN_SAMPLES)
			{
				System.out.println("Querying look-ahead model from now on...");
			}
			
			return recursivePlan(feats, LOOKAHEAD_MAX_LEVELS);
		}
		else
		{
			neuralNetReward.setInput(feats);
			neuralNetReward.calculate();
	
			return neuralNetReward.getOutput()[0];
		}
	}
	
	private double recursivePlan(double[] feats, int h)
	{
		if (h == 0)
		{
			neuralNetReward.setInput(feats);
			neuralNetReward.calculate();

			return neuralNetReward.getOutput()[0];
		}
		else
		{
			neuralNetWorld.setInput(feats);
			neuralNetWorld.calculate();

			double[] feats_new = new double[num_inputs]; // Will include the new state + an action.
			System.arraycopy(neuralNetWorld.getOutput(), 0, feats_new, num_actions, num_inputs - num_actions);
			
			// Printing the output:
			
			for (int i = 0; i < feats_new.length; i++)
			{
				System.out.print(feats_new[i] + " ");
			}
			System.out.println();

			double Rmax = Double.NEGATIVE_INFINITY;
			
			// TODO: This is a very bad way of trying every possible action, but it is the easiest one for the time being:
				
			for (double[] action: new double[][]{{0, 0, 0}, {0, 0, 1}, {0, 1, 0}, {0, 1, 1}, {1, 0, 0}, {1, 0, 1}, {1, 1, 0}, {1, 1, 1}, {-1, 0, 0}, {-1, 0, 1}, {-1, 1, 0}, {-1, 1, 1}})
			{
				System.arraycopy(action, 0, feats_new, 0, num_actions);
				
				Rmax = Math.max(Rmax, recursivePlan(feats_new, h - 1));
			}

			return Rmax;
		}
	}

	@Override
	public void clearSamplesAndReset()
	{
		System.err.println("\nClear samples and reset in Neural Network\n");
		clearSamples();
		resetNetworks();
	}
	
	private void clearSamples()
	{
		outputList = new ArrayList<Double>();
		sampleList = new ArrayList<double[]>();
	}
	
	private void resetNetworks()
	{
		neuralNetReward = new MultiLayerPerceptron(num_inputs, num_hidden, 1);
		neuralNetWorld  = new MultiLayerPerceptron(num_inputs, num_inputs - num_actions); // TODO: Decide on the number of hidden units for this network.
		
		for (int i = 0; i < num_hidden; i++)
		{
			neuralNetReward.getLayerAt(1).getNeuronAt(i).setTransferFunction(new Sigmoid(1));
		}

		neuralNetReward.getLayerAt(2).getNeuronAt(0).setTransferFunction(new Sigmoid(10));
		
		/*for (int i = 0; i < LOOKAHEAD_NEURONS; i++)
		{
			neuralNetWorld.getLayerAt(1).getNeuronAt(i).setTransferFunction(new Sigmoid(1));
		}*/

		for (int i = 0; i < num_inputs - num_actions; i++)
		{
			neuralNetWorld.getLayerAt(1).getNeuronAt(i).setTransferFunction(new Linear());
		}
		
		MomentumBackpropagation mbp = new MomentumBackpropagation();
		
		mbp.setMaxError(MIN_ERROR);
		mbp.setLearningRate(LEARNING_RATE);
		mbp.setMomentum(MOMENTUM_RATE);
		mbp.setMaxIterations(MAX_ITERATIONS);
		
		neuralNetReward.setLearningRule(mbp);
		
		mbp = new MomentumBackpropagation();
		
		mbp.setMaxError(Integer.MAX_VALUE);
		mbp.setLearningRate(0.1);
		mbp.setMomentum(0.001);
		mbp.setMaxIterations(MAX_ITERATIONS);
		
		neuralNetWorld.setLearningRule(mbp);
	}
	
	public void reset()
	{
		System.out.println("Reset neural network");
	}
}