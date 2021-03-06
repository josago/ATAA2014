/*
Adapted by Brad Knox from RandomAgent.java by Brian Tanner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package edu.utexas.cs.tamerProject.agents.tamer;

import java.util.Arrays;
import java.util.Random;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import ataa2014.ExperimentsATAA;
import ataa2014.GoodCreditAssign;
import ataa2014.InputPanel;
import ataa2014.ParamsATAA;
import ataa2014.SimulatedHuman;
import ataa2014.StateRepresentation;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.CreditAssignParamVec;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.HLearner;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.SampleWithObsAct;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.trainInterface.TrainerListener;
import edu.utexas.cs.tamerProject.utils.Stopwatch;


/**
 * This class flexibly implements a TAMER agent, which learns from human 
 * reward, input by keyboard.  
 * 
 * @author bradknox
 *
 */
public class TamerAgent extends GeneralAgent implements AgentInterface {

	public HLearner hLearner;
	protected double lastStepStartTime;

	public TrainerListener trainerListener;

	/*
	 * Time of agent pause at end of episode in milliseconds, where
	 * agent simply waits to finish agent_end(), which TinyGlueExtended
	 * will wait for. This pause can be used to allow the trainer to 
	 * add reward or punishment at the ends of episodes. 
	 * 
	 *  *** When possible, create the pause in RunLocalExperiment
	 *  instead through its static variable PAUSE_DUR_AFTER_EP. ****
	 */
	public int EP_END_PAUSE = 2000; //2000; /
	private SampleWithObsAct[] lastLearningSamples;
	public static boolean verifyObsFitsEnvDesc = true;
	
	//Use a human simulator
	SimulatedHuman simHuman;
	StateRepresentation featureProcessorHuman;
	GoodCreditAssign assigner;
		
	public TamerAgent(SimulatedHuman h)
	{
		super();
		simHuman = h;			
	}
	
	public TamerAgent()
	{
		super();
	}
	
	public SampleWithObsAct[] getLastLearningSamples(){return this.lastLearningSamples;}
    
    
	
	// Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    public void agent_init(String taskSpec) {
    	GeneralAgent.agent_init(taskSpec, this);
    	
    	// Add simulated human to the regression model if 
    	if(ParamsATAA.useSimulatedHuman)
    	{
    		//System.out.println("Simulated human added to regression model");
    		model.addSimulatedHuman(simHuman);
    		featureProcessorHuman = new StateRepresentation(this.theObsIntRanges, this.theObsDoubleRanges, 
    				this.theActIntRanges, this.theActDoubleRanges);
    	}
		
		//// CREATE CreditAssignParamVec
		CreditAssignParamVec credAssignParams = new CreditAssignParamVec(this.params.distClass, 
														this.params.creditDelay, 
														this.params.windowSize,
														this.params.extrapolateFutureRew,
														this.params.delayWtedIndivRew,
														this.params.noUpdateWhenNoRew);
		
		//// INITIALIZE TAMER
		if(ParamsATAA.ATAA_Exp)
			this.assigner = new GoodCreditAssign(ParamsATAA.nr_steps_credit, this.model);
		else
			this.hLearner = new HLearner(this.model, credAssignParams);
		
		if(ParamsATAA.ATAA_Exp)
			this.actSelector = new ActionSelect(this.model, ParamsATAA.selectionMethod, 
					this.params.selectionParams, this.currObsAndAct.getAct().duplicate());
		else
			this.actSelector = new ActionSelect(this.model, this.params.selectionMethod, 
											this.params.selectionParams, this.currObsAndAct.getAct().duplicate());


		
		//LogTrainer.trainOnLog("/Users/bradknox/rl-library/data/cartpole_tamer/recTraj-wbknox-tamerOnly-1295030420.488000.log", this);
		if (enableGUI) { // TODO reduce 3 lines below to a single line, putting the 3 inside TrainerListener
			//Schedule a job for event dispatch thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					trainerListener = TrainerListener.createAndShowGUI(TamerAgent.this);
				}
			});
		}
		if (this.actSelector.getRewModel() == null)
			this.actSelector.setRewModel(this.model);
		this.endInitHelper();
		
		
		//The adding of the rewards provided by the simulated human
		if(ParamsATAA.useSimulatedHuman)
		{
			if(!this.inTrainSess)
				this.inTrainSess = true;
			
			final GoodCreditAssign balle = this.assigner;
		    Thread t = new Thread("simHuman feedback thread")
		    {		    	
		        public void run()  
		        {
		        	System.out.println("Human feedbackloop started");
		            while (!ExperimentsATAA.killFeedbackloop){		                
		            	double reward = simHuman.getFeedback();
		            	//System.out.println("Reward requested from simHuman = " + reward);
		            	balle.addHumanReward(reward);
		            	//Make sure the human feedback from the simulated human is 
		            	//visible in the feedback window
		            	if(ParamsATAA.ATAA_Exp)
		            	{
		            		if(reward != 0.0)
		            		{
		            			if(reward>0.0)
		            				ExperimentsATAA.ip.f = InputPanel.Feedback.positive;
		            			else
		            				ExperimentsATAA.ip.f = InputPanel.Feedback.negative;
		            			ExperimentsATAA.ip.repaint();
		            		}
		            	}
		            	//System.out.println("\n-----\nReward from simulated human added: " + reward + "\n----------");
		                try
		                {
		                    Thread.sleep(ParamsATAA.in_between_time_feedback_request_human_simulator);
		                } catch (Exception e)
		                {
		                    e.printStackTrace();
		                }
		            }
		            ExperimentsATAA.feedbackLoopGotKilled  = true;
		        }  
		    };
		    t.start();
		    System.out.println("Feedbackloop simulated human started!!!");
		}
	
    }
    
	// Called at the beginning of each episode (in RLViz, it's first called when "Start" is first clicked)
    public Action agent_start(Observation o, double time, Action predeterminedAct) {
    	//System.out.println("---------------------------start TAMER ep " + this.currEpNum);
    	// System.out.println("\n\n------------new episode-----------");
		this.startHelper();
		
		//this.hLearner.newEpisode();	 //// CLEAR HISTORY and do any other set up
		this.lastStepStartTime = -10000000; // should cause a big problem if it's used during the first time step (which shouldn't happen)
		
        return agent_step(0.0, o, time, predeterminedAct);
    }
    
    
    

    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct) {
    	
    	//Provide information to human simulater using last Obs and Act & Feature generator here
    	if(ParamsATAA.useSimulatedHuman)
    	{	    	
	    	if(lastObsAndAct.getAct() != null && lastObsAndAct.getObs() != null){
	    		//System.out.println("Updated simulated human with features and action");
	    		simHuman.addInformation(featureProcessorHuman.getFeats(lastObsAndAct.getObs(), lastObsAndAct.getAct()));

	    	}	    	
    	}
    	return agent_step(r, o, startTime, predeterminedAct, this.lastObsAndAct.getAct());
    }
    
    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct, Action tieBreakAction) {

    	if (verifyObsFitsEnvDesc)
    		this.checkObs(o);
    	this.stepStartTime = startTime;
		this.stepStartHelper(r, o); // this.stepStartTime (set in stepStartHelper()) ends last step and starts new step
		
		if(!ParamsATAA.ATAA_Exp)
    		this.hLearner.recordTimeStepEnd(startTime);
    	
    	/*
    	 * PROCESS PREVIOUS TIME STEP
    	 */
			
		if(!ParamsATAA.ATAA_Exp)	
		{
			processPrevTimeStep(this.stepStartTime);	
			this.lastLearningSamples = this.hLearner.processSamples(startTime, inTrainSess);
		}
		
		/*
		 *  GET ACTION
		 */
		this.currObsAndAct.setAct(predeterminedAct);
		if (this.currObsAndAct.actIsNull()) {
			this.currObsAndAct.setAct(this.actSelector.selectAction(o, tieBreakAction));
		}
    	
		this.lastStepStartTime = this.stepStartTime;

		this.stepEndHelper(r, o);
		if (this.isTopLevelAgent) // If not top level, TamerAgent's chosen action might not be the actual action. This must be called by the primary class.
			if(ParamsATAA.ATAA_Exp)
				this.assigner.process(o, this.currObsAndAct.getAct(), this.featGen);
			else
				this.hLearner.recordTimeStepStart(o, this.currObsAndAct.getAct(), this.featGen, startTime);
		
		return this.currObsAndAct.getAct();
    }

    
    
    
    public void agent_end(double r, double time) {
    	this.stepStartTime = time;
    	this.inTrainSess = true;
    	this.endHelper(r);
    	System.out.println("Agent end!");
		//// PROCESS PREVIOUS TIME STEP
		processPrevTimeStep(this.stepStartTime);
    	this.actSelector.anneal();
    	GeneralAgent.sleep(EP_END_PAUSE);
    }

    
    
    
	protected void processPrevTimeStep(double borderTime){
		if(!ParamsATAA.ATAA_Exp)
		{
			if (inTrainSess) //// UPDATE
				this.hLearner.processHRew(this.hRewThisStep);
	
			if (verbose)
				System.out.println("hRewThisStep: " + this.hRewThisStep.toString());
		}
	}
    

    public void agent_cleanup() {
        
    }




	
	private void getHandCodedHRew(){
		if ((this.lastObsAndAct.getObs().doubleArray[1] > 0 && this.lastObsAndAct.getAct().intArray[0] == 2) ||
			(this.lastObsAndAct.getObs().doubleArray[1] <= 0 && this.lastObsAndAct.getAct().intArray[0] == 0))
			this.addHRew(1.0);
//			this.hRewThisStep = 1.0;
		else
			this.addHRew(-1.0);
//			this.hRewThisStep = -1.0;
		System.out.println("\thRewThisStep: " + hRewThisStep.toString());
	}

	   public static void main(String[] args){
	    	TamerAgent agent = new TamerAgent();
	    	agent.processPreInitArgs(args);
	    	if (agent.glue) {
	        	AgentLoader L=new AgentLoader(agent);
	        	L.run();
	    	}
	    	else {
	    		agent.runSelf();
	    	}
	    }  
	    
		public void processPreInitArgs(String[] args) {
			System.out.println("\n[------Tamer process pre-init args------] " + Arrays.toString(args));
			super.processPreInitArgs(args);
			for (int i = 0; i < args.length; i++) {
	    		String argType = args[i];
	    		if (argType.equals("-tamerModel") && (i+1) < args.length){
	    			if (args[i+1].equals("linear")) {
	    				
	    				System.out.println("Setting model to linear model");
	    				
	    				this.params.featClass = "FeatGen_RBFs";
	    				this.params.modelClass = "IncGDLinearModel";
	    				
	    				// These fit the RBF class that was tested to give identical output with that of the python code
	    				this.params.featGenParams.put("basisFcnsPerDim", "40");
	    				this.params.featGenParams.put("relWidth", "0.08");
	    				this.params.featGenParams.put("biasFeatVal", "0.1");
	    				this.params.featGenParams.put("normMin", "-1");
	    				this.params.featGenParams.put("normMax", "1");
	   					
	    	
	   					// Learning params
	    				this.params.initModelWSamples = false;
	    				this.params.initWtsValue = 0.0;
	    				this.params.stepSize = 0.001; // matches python code
	    			}
	    			else if (args[i+1].equals("kNN")) {
	    				this.params.modelClass = "WekaModelPerActionModel";
	    				this.params.featClass = "FeatGen_NoChange";
	    				this.params.initModelWSamples = false; //// no biasing in MC for ALIHT paper and ICML workshop paper
	    				this.params.numBiasingSamples = 100;
	    				this.params.biasSampleWt = 0.1;
	    				this.params.wekaModelName = "IBk"; //// IBk for ALIHT paper and ICML workshop paper
	    			}
	    			
	    			if (args[i+1].equals("neural")) {
	    				System.out.println("Setting model to linear model");
	    				this.params.featClass = "FeatGen_RBFs";
	    				this.params.modelClass = "NeuralNet";
	    				
	    			}
	    			else {
	    				System.out.println("\nIllegal TamerAgent model type. Exiting.\n\n");
	    				System.exit(1);
	    			}
	    			
	    			System.out.println("agent model set to: " + args[i+1]);
	    		}
	    		else if (argType.equals("-credType") && (i+1) < args.length){
	    			if (args[i+1].equals("aggregate")) {
	    				this.params.delayWtedIndivRew = false;
	    				this.params.noUpdateWhenNoRew = false;
	    			}
	    			else if (args[i+1].equals("aggregRewOnly")) {
	    				this.params.delayWtedIndivRew = false;
	    				this.params.noUpdateWhenNoRew = true;    				
	    			}
	    			else if (args[i+1].equals("indivAlways")) {
	    				this.params.delayWtedIndivRew = true;
	    				this.params.noUpdateWhenNoRew = false;
	    			}
	    			else if (args[i+1].equals("indivRewOnly")) {
	    				this.params.delayWtedIndivRew = true;
	    				this.params.noUpdateWhenNoRew = true;    				
	    			}
	    			else{
	    				System.out.println("\nIllegal TamerAgent credit assignment type. Exiting.\n\n");
	    				System.exit(1);
	    			}
	    			System.out.println("agent.credType set to: " + args[i+1]);
	    		}
			}
		}
		
		
		//Process input from reward taken from the human feedback window
		public void receiveKeyInput(char c){
			if(!ParamsATAA.useSimulatedHuman || !ParamsATAA.ATAA_Exp){
				System.out.println("In receive shizzle where it shouldnt be");
				super.receiveKeyInput(c);
				//System.out.println("TamerAgent receives key: " + c);
				if (c == '/') {
					this.addHRew(1.0);
					System.out.println("reward");
				}
				else if (c == 'z') {
					this.addHRew(-1.0);
					System.out.println("punishment");
				}
				else if (c == '?') {
					this.addHRew(10.0);
				}
				else if (c == 'Z') {
					this.addHRew(-10.0);
				}
				else if (c == ' ' && this.allowUserToggledTraining) {
					this.toggleInTrainSess();
					this.hLearner.credA.setInTrainSess(Stopwatch.getComparableTimeInSec(), this.inTrainSess);
				}
				else if (c == 'S') {
					this.model.saveDataAsArff(this.envName, (int)Stopwatch.getWallTimeInSec(), "");
				}
			}	
	//			System.out.println("hRewList after key input: " + this.hRewList.toString());
		}
	    
		public void initRecords() {
			super.initRecords();
			System.err.println("initRecords calls hLearner.clearhistory()!!!!!");
			if (this.hLearner != null)
				this.hLearner.clearHistory();
			this.lastStepStartTime = -10000000;
		}
	  

	    public static ParameterHolder getDefaultParameters() {
	        ParameterHolder p = new ParameterHolder();
	        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());
	        return p;
	    }
	    
	    public void setTrainSess(boolean x)
		{
	    	this.inTrainSess = x;
		}	    
	    
	
}



/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "General TAMER Agent";
    }

    public String getShortName() {
        return "Tamer Agent";
    }

    public String getAuthors() {
        return "Brad Knox";
    }

    public String getInfoUrl() {
        return "http://www.cs.utexas.edu/~bradknox";
    }

    public String getDescription() {
        return "RL-Library Java Version of a general Tamer agent.";
	}   
    
}

