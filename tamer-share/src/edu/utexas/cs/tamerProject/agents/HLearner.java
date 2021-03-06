package edu.utexas.cs.tamerProject.agents;

import java.util.ArrayList;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;


import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.SampleWithObsAct;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;


/**
 * HLearner interfaces between an agent, a CreditAssign object, and a RegressionModel 
 * object to learn the human reward model.
 * 
 * @author bradknox
 *
 */

public class HLearner {
	
	/** Written by Brad Knox
	 *
	 *
	 **/

	private RegressionModel model = null; 
	public CreditAssign credA = null;
	//


	public HLearner(RegressionModel model, CreditAssignParamVec credAssignParams){
		this.model = model;
		this.credA = new CreditAssign(credAssignParams);
	}

	public RegressionModel getModel(){
		return this.model;
	}
	
	public void reset(){
		System.err.println("\n\nWAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH \n reset all ownoooooooooo\n================\n");
		this.model.clearSamplesAndReset();
		this.clearHistory();
	}

	// Time of steps is recorded by the credid assigner (credA) to be able to reconstruct the
	// credit provided for different time steps.
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStep(Observation o, Action a, FeatGenerator featGen, double startTime){
		this.recordTimeStepEnd(startTime);
		this.recordTimeStepStart(o, a, featGen, startTime);
	}
	
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStepStart(Observation o, Action a, FeatGenerator featGen, double startTime){
		this.credA.recordTimeStepStart(o, a, featGen, startTime);
	}
	
	// each time step will have a endTime, which is the next step's startTime
	public void recordTimeStepEnd(double endTime){
		this.credA.recordTimeStepEnd(endTime);
	}
	
	public SampleWithObsAct[] processSamples(double currTime, boolean inTrainSess) {
		//System.out.print("\n\nHLearner processSamples()");
		SampleWithObsAct[] samples = this.credA.processSamplesAndRemoveFinished(currTime, inTrainSess);
		//if (samples.length > 0)
		//	System.out.println("Adding " + samples.length + " samples.");
		if (samples.length > 0)
			addSamplesAndBuild(samples);
		/*else{
			System.out.println("There are no samples to add");
		}*/
		return samples;
	}
	
	public void clearHistory(){
		System.err.println("\nHistory of credit assign is cleared\n");
		this.credA.clearHistory();
	}


	public void processHRew(ArrayList<HRew> hRewThisStep){
		//System.out.println("\n\nprocessHRew in HLearner");
		//System.out.println("Updating with human reward. Size: " + hRewThisStep.size());
		//Sample[] newSamples = new Sample[0];
		for (HRew hRew: hRewThisStep) {
			this.credA.processNewHReward(hRew.val, hRew.time);
		}
	}

	// The model receives samples and processes them
	private void addSamplesAndBuild(Sample[] samples) {
		this.model.addInstancesWReplacement(samples);
		this.model.buildModel();
	}

	

}