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
	public static final double LEARNING_RATE_REWARD = 0.001;
	public static final double MOMENTUM_RATE_REWARD = 0;
	public static final double LEARNING_RATE_WORLD  = 0.0000001;
	public static final double MOMENTUM_RATE_WORLD  = 0;
	public static final int    MAX_ITERATIONS = Integer.MAX_VALUE;
	
	public static final int LOOKAHEAD_SKIP_SAMPLES = 5;
	public static final int LOOKAHEAD_MAX_LEVELS   = 2;   // Maximum number of levels to lookahead into the future when planning an action.
	public static final int LOOKAHEAD_MIN_SAMPLES  = 100; // Minimum number of samples needed before the world model is queried and effectively used.
	
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
			
			return d;
		}
	}
	
	public NeuralNetWorldModel(int num_inputs, int num_actions, int num_hidden)
	{
		this.num_inputs  = num_inputs;
		this.num_actions = num_actions;
		this.num_hidden  = num_hidden;

		clearSamplesAndReset();
	}
	
	@Override
	public void addInstance(Sample sample)
	{
		boolean found = false;
			
		for (int i = 0; i < sampleList.size(); i++)
		{
			if (Arrays.equals(sample.feats, sampleList.get(i)))
			{
				//outputList.set(i, (outputList.get(i) + sample.label) / 2); // Weighted average.
				outputList.set(i, new Double(sample.label)); // Latest value.
					
				found = true;
				break;
			}
		}
			
		if (!found)
		{
			ParamsATAA.epsilon *= 0.95;
			
			sampleList.add(sample.feats);
			outputList.add(new Double(sample.label));
		}
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
			
			if (i < sampleList.size() - 1 && i % LOOKAHEAD_SKIP_SAMPLES == 0)
			{
				trainingSetWorld.addRow(new DataSetRow(sampleList.get(i), Arrays.copyOfRange(sampleList.get(i + 1), num_actions, num_inputs)));
			}
		}

		//resetNetworks();
		
		neuralNetReward.learnInNewThread(trainingSetReward);
		
		if (sampleList.size() > 1)
		{
			neuralNetWorld.learnInNewThread(trainingSetWorld);
		}
	    
	    try
	    {
			Thread.sleep(1000 / 6);
		}
	    catch (InterruptedException e)
		{
	    	
		}
	    
	    neuralNetReward.stopLearning();
		neuralNetWorld.stopLearning();
		
		// Calculating the average error per sample for the neuralNetWorld:
		
		/*double error = 0;
		
		for (DataSetRow sample: trainingSetWorld.getRows())
		{
			neuralNetWorld.setInput(sample.getInput());
			neuralNetWorld.calculate();

			double[] output = neuralNetWorld.getOutput();
			
			for (int i = 0; i < output.length; i++)
			{
				error += Math.abs(output[i] - sample.getDesiredOutput()[i]) / output.length;
			}
		}
		
		error /= trainingSetWorld.size();
		
		System.out.println("Error: " + error);*/
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
		neuralNetReward.setInput(feats);
		neuralNetReward.calculate();

		double Sreturn = neuralNetReward.getOutput()[0];

		if (h > 0)
		{
			neuralNetWorld.setInput(feats);
			neuralNetWorld.calculate();

			double[] feats_new = new double[num_inputs]; // Will include the new state + an action.
			System.arraycopy(neuralNetWorld.getOutput(), 0, feats_new, num_actions, num_inputs - num_actions);
			
			double Rmax = Double.NEGATIVE_INFINITY;

			// TODO: This is a very bad way of trying every possible action, but it is the easiest one for the time being:
				
			for (double[] action: new double[][]{{0, 0, 0}, {0, 0, 1}, {0, 1, 0}, {0, 1, 1}, {1, 0, 0}, {1, 0, 1}, {1, 1, 0}, {1, 1, 1}, {-1, 0, 0}, {-1, 0, 1}, {-1, 1, 0}, {-1, 1, 1}})
			{
				System.arraycopy(action, 0, feats_new, 0, num_actions);
				
				Rmax = Math.max(Rmax, recursivePlan(feats_new, h - 1));
			}

			Sreturn += Rmax;
		}
		
		return Sreturn;
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
		neuralNetWorld  = new MultiLayerPerceptron(num_inputs, num_inputs - num_actions);
		
		for (int i = 0; i < num_hidden; i++)
		{
			neuralNetReward.getLayerAt(1).getNeuronAt(i).setTransferFunction(new Sigmoid(1));
		}

		neuralNetReward.getLayerAt(2).getNeuronAt(0).setTransferFunction(new Sigmoid(10));

		for (int i = 0; i < num_inputs - num_actions; i++)
		{
			neuralNetWorld.getLayerAt(1).getNeuronAt(i).setTransferFunction(new Linear());
		}
		
		MomentumBackpropagation mbp = new MomentumBackpropagation();
		
		mbp.setLearningRate(LEARNING_RATE_REWARD);
		mbp.setMomentum(MOMENTUM_RATE_REWARD);
		mbp.setMaxIterations(MAX_ITERATIONS);
		
		neuralNetReward.setLearningRule(mbp);
		
		mbp = new MomentumBackpropagation();
		
		mbp.setLearningRate(LEARNING_RATE_WORLD);
		mbp.setMomentum(MOMENTUM_RATE_WORLD);
		mbp.setMaxIterations(MAX_ITERATIONS);
		
		neuralNetWorld.setLearningRule(mbp);
	}
	
	public void reset()
	{
		System.out.println("Reset neural network");
	}
}