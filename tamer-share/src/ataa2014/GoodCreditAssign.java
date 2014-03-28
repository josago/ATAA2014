package ataa2014;

import java.util.ArrayList;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;

public class GoodCreditAssign {
	
	private ArrayList<Sample> activeSamples;
	private int nrSteps;
	private RegressionModel model = null;
	
	public static boolean marioDied = false;
	private int stepsDied;
	
	public GoodCreditAssign(int nrSteps, RegressionModel m)
	{		
		activeSamples = new ArrayList<Sample>();
		this.nrSteps = nrSteps;
		model = m;
		stepsDied = 0;
	}
	
	public synchronized void process(Observation o, Action action, FeatGenerator featGen)
	{
//		System.out.println("=============NEW STEP PROCESSING==========");
		if(activeSamples.size() == nrSteps)
		{
			Sample s = activeSamples.get(0);
			activeSamples.remove(0);
//			System.out.println("=============\nSample to model:\n=============\n" + s.obs.toString() );
//			System.out.println("Action: " + s.feats[0] + " "+ s.feats[1] + " " + s.feats[2] + "\nSpeed: " + s.feats[StateRepresentation.STATE_SX] + "\nreward = " + s.label);
			model.addInstance(s);
			model.buildModel();			
		}
		
		//Add sample			
		activeSamples.add(new Sample(featGen.getFeats(o, action), 0.0, o));
		
		/*System.out.println("=============\nActive samples:\n=============\n");
		for(int i = 0; i<activeSamples.size();i++)
		{
			System.out.println("sample nr "+i + ":\n" +  activeSamples.get(i).obs.toString() + "\ncurrent reward = " + activeSamples.get(i).label);
		}*/
	}
	
	public synchronized void addHumanReward(double reward)
	{
		//System.out.println("Provided reward = " + reward);
		if(reward != 1.0 && reward != -1.0 && reward != 0.0)
			System.err.println("Wrong feedback value in credit assign");
		if(reward != 0.0)
		{
			double[] credit = credit();
			for(int i = 0; i<activeSamples.size();i++)
			{
				activeSamples.get(i).label+= credit[i] * reward;
				/*if(activeSamples.get(i).label%0.1 != 0.0 || activeSamples.get(i).label%-0.1 != 0.0 )
				{
					System.out.println("Wrong: in processing: " + activeSamples.get(i).label);
				}*/
			}
		}
					
	}
	
	//TODO: this is still kind of a dummy function
	public double[] credit()
	{
		//double[] credit = new double[nrSteps];		
		//Apply function
		
		
		double[] credit = {0.05, 0.1, 0.25, 0.6};
		
		return credit;		
	}
	
	
}
