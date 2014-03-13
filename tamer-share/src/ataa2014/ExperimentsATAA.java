package ataa2014;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.Timer;

import org.rlcommunity.environments.mario.GeneralMario;
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
	
	public void init()
	{
		int[] initAction = {1, 0, 0};
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
			
			//Create reinforcement window
			ip = new InputPanel(this);			
			JFrame f = new JFrame();	        
	        f.getContentPane().add(ip);
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        f.pack();
	        f.setVisible(true);
	        
	        ActionListener painter = new ActionListener(){        	
				@Override
				public void actionPerformed(ActionEvent e) {
					ip.f = InputPanel.Feedback.nothing;
					ip.repaint();
					//System.out.println("Timer repaint");
				}
				
	        };
	        
	        int delay = 350;
	        new Timer(delay, painter).start();
		}
		glue = new LocalGlue(env, agent);
		glue.RL_init();	
		agent.setInTrainSess(true);
		System.out.println("In train sess after init vanuit RLglue: " + agent.getInTrainSess());
	}
	
	//TODO: Collect interesting shizzle and put to file and stuff
	//TODO: Make sure different combinations and shit can be runned and then results are processed
	public void run_experiment()
	{
		init();	
		System.out.println("========================\nINITIALIZED EVERYTHING FOR EXPERIMENT\n========================\n\n");
		ResultContainer results = new ResultContainer();		
		
		for(int i = 0; i<1000; i++)
		{
			// Make sure here the right reward is propagated (from this window definitely not yet, from simHuman: check)
			rew_obs = glue.RL_env_step(currentAction);
			if(!ParamsATAA.useSimulatedHuman){
				agent.addHRew(rewardHuman);
				rewardHuman = 0;
			}
			currentAction = glue.RL_agent_step(rew_obs.getReward(), rew_obs.getObservation());
		}
		System.exit(0);		
	}	
	
	/**
	 * Gets called by our own input panel if reward is added by a human
	 * @param i
	 */
	public void updateOnHumanReward(int i)
	{
		//System.out.println("Human feedback received: " + i);
		rewardHuman = i;
	}
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExperimentsATAA exp = new ExperimentsATAA();
		exp.run_experiment();
		
	}

}
