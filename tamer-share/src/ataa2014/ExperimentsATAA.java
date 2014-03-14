package ataa2014;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

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
	
	
	
	public void testExperimentEnvironment()
	{
		init();	
		System.out.println("========================\nINITIALIZED EVERYTHING FOR EXPERIMENT\n========================\n\n");
		ResultContainer results = new ResultContainer();		
		results.openFile();
		for(int i = 0; i<10; i++)
		{
			// Make sure here the right reward is propagated (from this window definitely not yet, from simHuman: check)
			rew_obs = glue.RL_env_step(currentAction);
			if(!ParamsATAA.useSimulatedHuman){
				agent.addHRew(rewardHuman);
				rewardHuman = 0;
			}
			currentAction = glue.RL_agent_step(rew_obs.getReward(), rew_obs.getObservation());
		}
		
		cleanUp();
		
		results.processResults();
		results.writeToFile();		
		System.out.println("END OF TEST ROUND\n\n\n\n\n\n\n");
		
	}
	
	private void cleanUp() {
		
		if(!ParamsATAA.useSimulatedHuman)
			f.dispose();
		GlueMario.frame.dispose();
	}

	/**
	 * Initialize the agent, feedback (real or simulated human), 
	 */
	public void init()
	{
		int[] initAction = {0, 0, 0};
		currentAction = new Action(3, 0);
		currentAction.intArray = initAction;
		
		ParamsATAA.ATAA_Exp = true;
		if(ParamsATAA.useSimulatedHuman)
		{
			simHuman = new SimulatedFast();
			agent = new TamerAgent(simHuman);
			env = new GeneralMario(simHuman);
		}
		else
		{
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
        Timer tim  = new Timer(delay, painter);
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
		ResultContainer results = new ResultContainer();
		
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
					System.out.println("Run number: " + run);
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
						
						currentAction = glue.RL_agent_step(rew_obs.getReward(), rew_obs.getObservation());
						
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
	
	public void setHumanReward(int i)
	{
		rewardHuman = i;
	}
		
	
	public static void main(String[] args) {		
		ExperimentsATAA exp = new ExperimentsATAA();
		//exp.testExperimentEnvironment();		
		exp.run_experiment();
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
