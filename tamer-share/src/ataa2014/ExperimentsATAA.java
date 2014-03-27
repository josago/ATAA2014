package ataa2014;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.rlcommunity.environments.mario.GeneralMario;
import org.rlcommunity.environments.mario.GlueMario;
import org.rlcommunity.environments.mario.viz.MarioComponent;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.LocalGlue;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.TamerApplet;
import edu.utexas.cs.tamerProject.applet.TamerPanel;

public class ExperimentsATAA {

	private GeneralAgent agent;
	private SimulatedHuman simHuman;
	private EnvironmentInterface env;
	private LocalGlue glue;
	private Action currentAction;
	private Reward_observation_terminal rew_obs;
	public static InputPanel ip;
	private int rewardHuman;
	private JFrame f;
	private Timer tim;
	private int [] seeds;
	
	public static boolean killFeedbackloop;
	public static boolean feedbackLoopGotKilled;
	public static int seed;
	
	public static int loopNr;

	
	private void modelParamsToFile(ArrayList<ArrayList<double[]>> param_results) {
		File f = new File("src/ataa2014_expResults/" + "ModelParamResults_" + ParamsATAA.personName);
		PrintWriter printer = null;
		try {
			printer = new PrintWriter(f, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		int nrSettings = param_results.size();
		int nrRuns = param_results.get(0).size();
		int nrStats = param_results.get(0).get(0).length;
		for(int i = 0; i<nrSettings;i++){			
			double[] sum = new double[nrStats];
			for(int j =0; j<nrRuns;j++){
				for(int k = 0; k<nrStats; k++)
				{
					sum[k] += param_results.get(i).get(j)[k];
				}
			}
			for(int k = 0; k<nrStats;k++)
			{				
				printer.print(sum[k]/nrRuns + " ");				
			}	
			printer.print("\n");
		}		
		printer.close();			
	}
	
	private void cleanUp() {
		tim.stop();
		f.dispose();
		GlueMario.frame.dispose();
		if(ParamsATAA.useSimulatedHuman)
		{
			feedbackLoopGotKilled = false;		
			killFeedbackloop = true;
			System.out.println("Killing feedbackloop!");
			while(!feedbackLoopGotKilled)
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Initialize the agent, feedback (real or simulated human), 
	 */
	public void init()
	{
		int[] initAction = {0, 0, 0};
		currentAction = new Action(3, 0);
		currentAction.intArray = initAction;
		killFeedbackloop = false;
		
		ParamsATAA.ATAA_Exp = true;
		if(ParamsATAA.useSimulatedHuman)
		{
			simHuman = new SimulatedFast();
			agent = new TamerAgent(simHuman);
			env = new GeneralMario(simHuman);
		}
		else
		{
			System.out.println("No simulated human used");
			rewardHuman = 0;
			agent = new TamerAgent();
			env = new GeneralMario();
		}
		
		// Create reinforcement window
		// Also displays the feedback from the simulated human
		ip = new InputPanel(this);			
		f = new JFrame();	        
        f.getContentPane().add(ip);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
        ActionListener painter = new ActionListener(){        	
			@Override
			public void actionPerformed(ActionEvent e) {
				ip.f = InputPanel.Feedback.nothing;
				ip.repaint();
			}
        };
        
        int delay = 350;
        tim  = new Timer(delay, painter);
        tim.start();
		
        //Initialize the glue 
		glue = new LocalGlue(env, agent);
		glue.RL_init();	
		agent.setInTrainSess(true);
		System.out.println("In train sess after init vanuit RLglue: " + agent.getInTrainSess());
				
	}
	
	//Different combinations of models and feature generators are tested and 
	//the results are collected and written to file
	//Set parameters for the experiment in ParamsATAA
	public void run_experiment()
	{
		loopNr = 0;
		ResultContainer results = new ResultContainer();
		initSeeds();
		
		//Each combination of features and models is an experimental setting
		for(String mod: ParamsATAA.modelOptions)
		{
			for(String feat: ParamsATAA.featureGeneratorOptions)
			{
				System.out.println("============\nCurrent experiment:\nmodel: " + mod + "\nfeature generator: " + feat+"\n==================");
				//Set parameters for this experimental setting
				ParamsATAA.fileNameResults = "resultsATAA_" + mod + "_" + feat + "_" + ParamsATAA.personName + ".txt";
				results.openFile();
				ParamsATAA.model = mod;
				ParamsATAA.features = feat;	
				for(int run = 0; run<ParamsATAA.nr_of_runs; run++)
				{
					// Manipulate the level seed
					seed = seeds[run];
					System.err.println("SEED SET TO: " + seeds[run]);
					
					loopNr++;
					System.out.println("\n\n====================\n====================\nRun number: " + run + "\n\n====================\n====================\n");
										
					init();
					if(ParamsATAA.nr_steps_per_evaluation%ParamsATAA.nr_steps_for_episode != 0)
					{
						System.err.println("Not all steps will be recorded!");
					}				
					
					//Run the number of steps for a evaluation
					for(int i = 1; i<ParamsATAA.nr_steps_per_evaluation+1; i++)
					{					
						//Make the agent and environment take a step
						rew_obs = glue.RL_env_step(currentAction);
						if(!ParamsATAA.useSimulatedHuman){
							agent.addHRew(rewardHuman);
							rewardHuman = 0;
						}					
						currentAction = glue.RL_agent_step(0.0, rew_obs.getObservation().duplicate());							
						
						//Process results if neccessary
						if(i%ParamsATAA.nr_steps_for_episode == 0 && i>1)
						{
							results.processResults();
						}
					}
					results.run_finished();
					cleanUp();
					System.out.println("Run finished and cleaned up");
				}				
				//Write results to file
				results.writeToFile();
				
			}
		}
	}	
	
	public void run_experiments_modelParams()
	{
		boolean keepMarioResults = false;
		
		ResultContainer results = new ResultContainer();
		int settingNr = 0;
		double[] learningRates = {0.01, 0.001, 0.0001};
		int[] trainingTimes = {4, 6, 8};
		boolean[] resetModel = {true, false};
		
		ParamsATAA.model = "NeuralNet";
		
		ArrayList<ArrayList<double[]>> param_results = new ArrayList<ArrayList<double[]>>();
		
		//Each combination of features and models is an experimental setting
		for(double lr : learningRates)
		{
			for(int tr: trainingTimes)
			{
				for(boolean res : resetModel)
				{			
					loopNr = 0;					
					NeuralNet.LEARNING_RATE = lr;
					NeuralNet.TRAIN_TIME = 1000/tr;
					NeuralNet.RESET_MODEL = res;
					param_results.add(new ArrayList<double[]>() );
					initSeeds();
					
					for(int run = 0; run<ParamsATAA.nr_of_runs; run++)
					{
						// Manipulate the level seed
						seed = seeds[run];
						System.err.println("SEED SET TO: " + seeds[run]);
						
						loopNr++;
						System.out.println("\n\n====================\n====================\nRun number: " + run + "\n\n====================\n====================\n");
											
						init();
						if(ParamsATAA.nr_steps_per_evaluation%ParamsATAA.nr_steps_for_episode != 0)
						{
							System.err.println("Not all steps will be recorded!");
						}				
						
						//Run the number of steps for a evaluation
						for(int i = 1; i<ParamsATAA.nr_steps_per_evaluation+1; i++)
						{					
							//Make the agent and environment take a step
							rew_obs = glue.RL_env_step(currentAction);
							if(!ParamsATAA.useSimulatedHuman){
								agent.addHRew(rewardHuman);
								rewardHuman = 0;
							}					
							currentAction = glue.RL_agent_step(0.0, rew_obs.getObservation().duplicate());							
							
							//Process results if neccessary
							if(i%ParamsATAA.nr_steps_for_episode == 0 && i>1)
							{
								results.processResults();
							}
						}
						results.run_finished();
						cleanUp();
						System.out.println("Run finished and cleaned up");
					}	
					//Adding param results
					param_results.get(settingNr).add(agent.model.getStats());
					//Write results to file
					ParamsATAA.fileNameResults = "resultsMarioParams_" + lr + "_" + tr + "_" + res + "_" + ParamsATAA.personName + ".txt";
					if(keepMarioResults)
					{
						results.openFile();
						results.writeToFile();
					}
					settingNr++;
				}
			}
		}
		modelParamsToFile(param_results);
	}
	
		//Different combinations of models and feature generators are tested and 
		//the results are collected and written to file
		//Set parameters for the experiment in ParamsATAA
		public void run_experiment_with_humans() throws FileNotFoundException, UnsupportedEncodingException
		{
			Random r = new Random();
			ParamsATAA.useSimulatedHuman = false;
			loopNr = 0;	
			Scanner reader = new Scanner(System.in);
			int nr_settings = ParamsATAA.modelOptions.length*ParamsATAA.featureGeneratorOptions.length;
			int[][] results = new int [nr_settings][nr_settings];
			int[] modelNrs = new int [2];
			int[] featureNrs = new int [2];
			boolean again = true;
			ResultContainer stats = new ResultContainer();
			
			int comparisonNr = 1;
			
			while(again)
			{				
				for(int j = 0; j<2;j++)
				{					
					modelNrs[j] = r.nextInt(ParamsATAA.modelOptions.length);
					featureNrs[j] = r.nextInt(ParamsATAA.featureGeneratorOptions.length);
					while(modelNrs[j]==modelNrs[1-j] && featureNrs[j]==featureNrs[1-j])
					{
						modelNrs[j] = r.nextInt(ParamsATAA.modelOptions.length);
						featureNrs[j] = r.nextInt(ParamsATAA.featureGeneratorOptions.length);
					}
					ParamsATAA.model = ParamsATAA.modelOptions[modelNrs[j]];
					ParamsATAA.features = ParamsATAA.featureGeneratorOptions[featureNrs[j]];
					seed = r.nextInt(500);
					init();					
					for(int step = 1; step<ParamsATAA.nr_steps_per_comparison+1;step++)
					{
						if(step%ParamsATAA.nr_steps_stats == 0)
						{
							stats.processResults();
						}
						rew_obs = glue.RL_env_step(currentAction);
						agent.addHRew(rewardHuman);
						rewardHuman = 0;
						currentAction = glue.RL_agent_step(0.0, rew_obs.getObservation().duplicate());					
					}
					
					cleanUp();
					
					ParamsATAA.fileNameResults = ParamsATAA.personName + "_" + ParamsATAA.model + "_" + ParamsATAA.features + ParamsATAA.fileNameResultsHuman + "_" +comparisonNr;
					comparisonNr++;
					stats.run_finished();
					stats.openFile();
					stats.writeToFile();
				}
				
				System.out.println("\n\n\n\n\n\n\n\n\nIn which setting did Mario perform better? Press 1 or 2\n");
				//get user input for a
				String a=reader.next();
				while( !(a.equals("1") || a.equals("2")) )
				{
					System.out.println("Wrong input\n In which setting did Mario perform better? Press 1 or 2\n");
					//get user input for a
					a=reader.next();
				}
				System.out.println("Thanks for comparing");
				int model_0 = featureNrs[0] + (modelNrs[0]*ParamsATAA.featureGeneratorOptions.length);
				int model_1 = featureNrs[1] + (modelNrs[1]*ParamsATAA.featureGeneratorOptions.length);
				if(a.equals("1")){
					results[model_0][model_1] ++;
				}
				else{
					results[model_1][model_0]++;
				}
				
				
				
				System.out.println("\nDo want to do another comparison? Press y or n\n");
				//get user input for a
				a=reader.next();
				while( !(a.equals("y") || a.equals("n")) )
				{
					System.out.println("Wrong input\n Do want to do another comparison? Press y or n\n");
					//get user input for a
					a=reader.next();
				}			
				
				if(a.equals("n")){
					again = false;
				}	
			}
			
			System.out.println("Thanks a lot mate!!!!");
			File f = new File("src/ataa2014_expResults/" + ParamsATAA.personName + "_" + ParamsATAA.fileNameResultsHuman);
			PrintWriter printer = new PrintWriter(f, "UTF-8");
			//Print results
			for(int i = 0; i<nr_settings;i++)
			{
				for(int j = 0; j<nr_settings;j++)
				{
					printer.write(results[i][j] + " ");
				}
				printer.write("\n");
			}
			printer.close();
			reader.close();
		}	
	
	private void initSeeds() {
		Random r = new Random();
		seeds = new int[ParamsATAA.nr_of_runs];
		for(int i =0; i<ParamsATAA.nr_of_runs;i++)
		{
			seeds[i] = r.nextInt(200) + 100;
		}		
	}

	public void setHumanReward(int i)
	{
		rewardHuman = i;
	}
	
	
	public static boolean marioDied = false;
	private int stepsSinceDeath = 0;
	private boolean showSamples = true;
	
	public void demo()
	{
		ParamsATAA.useSimulatedHuman = true;
		init();
		seed = 428;
		for(int step = 1; step < ParamsATAA.nr_steps_per_evaluation; step++)
		{		
			if(step%10 == 0)
				System.out.println("step "+step);
			rew_obs = glue.RL_env_step(currentAction);
			if(!ParamsATAA.useSimulatedHuman){
				agent.addHRew(rewardHuman);
				rewardHuman = 0;
			}					
			currentAction = glue.RL_agent_step(0.0, rew_obs.getObservation().duplicate());	
			/*if(marioDied)				
			{
				if(stepsSinceDeath == 0)
				{
					System.out.println("Mario died!!!!");
				}
				
				if(stepsSinceDeath==6)
					break;
				else
					stepsSinceDeath++;
			}	*/		
		}
		if(showSamples)
			agent.model.printSamples();
		cleanUp();
	}
		
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {		
		ExperimentsATAA exp = new ExperimentsATAA();
		//exp.testExperimentEnvironment();			
		//exp.run_experiment();
		//exp.run_experiment_with_humans();
		exp.demo();
		//exp.run_experiments_modelParams();
		System.exit(0);				
	}
	
	private void checkoutThreads()
	{
		Set<Thread> tset = Thread.getAllStackTraces().keySet();
		System.out.println("\n============\nAll running threads:\n=============");
		for(Thread t: tset)
		{
			System.out.println(t.getName());
			if(t.getName().equals("Game Thread"))
			{
				GlueMario.comp.stop();
				System.out.println("Game Thread stopped");
			}
			else if(t.getName().equals("simHuman feedback thread"))
			{
				t.interrupt();
			}
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		tset = Thread.getAllStackTraces().keySet();
		System.out.println("\n\nList threads again:\n");
		for(Thread t: tset)
		{
			System.out.println(t.getName());		
		}
	}

}
