package org.rlcommunity.environments.mario;

/*
 * This implementation of Mario does not work with RL-Applet right now but may be easy to connect to it.
 * The implementation is by Michael Littman's group at Rutgers. I apologize to them for not 
 * having the full names and references and for changing the package name away from one that
 * identifies the code as being theirs; putting it here makes this codebase easier to navigate.
 */
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JFrame;

import org.rlcommunity.environments.mario.messages.MarioStateResponse;
import org.rlcommunity.environments.mario.viz.*;
import org.rlcommunity.environments.mario.viz.level.*;
import org.rlcommunity.environments.mario.viz.sprites.*;
import org.rlcommunity.environments.puddleworld.PuddleWorldState;
import org.rlcommunity.environments.puddleworld.messages.StateResponse;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.*;

import ataa2014.ParamsATAA;
import ataa2014.SimulatedHuman;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;
import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.interfaces.ProvidesEpisodeSummariesInterface;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;

public abstract class GlueMario implements EnvironmentInterface, ProvidesEpisodeSummariesInterface {
	public static boolean glue_running = false;
	public static boolean go_fast = true;//guangliang changed from true
	public static boolean go_dark = false;//guangliang changed from true
	public static boolean go_quiet = false;//guangliang changed from true
	
	public static int time_step_after_die = 3; //added by guangliang
	
	public static GlueMarioParameters param = new GlueMarioParameters();
	
	static JFrame frame;
	
	
	static int ticks_since_last_action;
	public static int trial_steps = 0;
	public static double trial_reward = 0;
	public static double run_reward = 0;
	public static double last_trial_reward = 0;
	
	static int action_direction = 0;
	static boolean action_jump = false;
	static boolean action_speed = false;
	
	static int last_action_direction = 0;//added by guangliang for adding extra time step after death to allow trainer enough time to give feedback
	static boolean last_action_jump = false;//added by guangliang
	static boolean last_action_speed = false;//added by guangliang

	static MarioComponent comp;

	static int mario_x;
	static int mario_y;

	static Object glue_block;
	
	static Reward_observation_terminal rot;
	static Reward_observation_terminal last_rot;//added by guangliang for adding extra time step after death to allow trainer enough time to give feedback
	
	protected rlVizLib.utilities.logging.EpisodeLogger theEpisodeLogger;
	
	protected SimulatedHuman simHuman;	

	public GlueMario() {
		setParameters(GlueMario.getDefaultParameters());
	}
	
	public GlueMario(ParameterHolder p) {
		setParameters(p);
	}
	
	/**
	 * Lydia:  Constructor for using a simulated human in the mario environment 
	 */
	public GlueMario(SimulatedHuman h)
	{
		simHuman = h;
		setParameters(GlueMario.getDefaultParameters());
	}
	
	
	
	public Vector<String> getEpisodeSummary(){
		return null;
	}

	public String getEpisodeSummary(long charToStartOn, int charsToSend) {
		return theEpisodeLogger.getLogSubString(charToStartOn, charsToSend);
	}
	
	public static void init(MarioComponent comp) {
		rot = new Reward_observation_terminal();
		rot.o = new Observation(0, 0, 0);
		
		if (!glue_running) return;
		GlueMario.comp = comp;
		Mario.lives = -1;
	}
	
	public static void reportWin() {
		rot.r = GlueMario.param.reward_goal;
		trial_reward += rot.r;
		run_reward += rot.r;
		
		rot.terminal = 1;
		Mario.lives--;
		synchronized (glue_block) {
			try {
				glue_block.notify();
				glue_block.wait();
			}
			catch (Exception e) {

			}
		}
	}
	
	public static void reportLoss() {
		rot.r = GlueMario.param.reward_death;
		trial_reward += rot.r;
		run_reward += rot.r;
		rot.terminal = 1;
		Mario.coins = 0;
		synchronized (glue_block) {
			try {
				glue_block.notify();
				glue_block.wait();
			}
			catch (Exception e) {

			}
		}
	}
	
	public static void restartLevel() {
		
		System.out.println("StartLevel called with level difficulty = " + param.level_difficulty);
		
		if (!glue_running) return;
		comp.startLevel(param.level_seed, param.level_difficulty, param.level_type);
		ticks_since_last_action = 0;
		trial_steps = 0;
		Mario.kills = 0;
		Mario.coins = 0;
		Mario.instance.setLarge(false, false);
		last_trial_reward = 0;
	}
	
	public void env_cleanup() {
		comp.stop();
		if (!go_dark)
			frame.dispose();
	}
	public String env_init() {
		theEpisodeLogger=new rlVizLib.utilities.logging.EpisodeLogger();
		
		launchMarioEnvironment();
		comp.start();
		GlueMario.run_reward = 0;
		
		
		//return param.getTaskSpec();
		return makeTaskSpec();
	}
	public String env_message(String theMessage) {
		EnvironmentMessages theMessageObject;
		//System.out.println("theMessage"+theMessage);
		try {
			theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
		} catch (Exception e) {
			System.err.println("Someone sent Mario a message thatwasn't RL-Viz compatible");
			return "I only respond to RL-Viz messages!";
		}

		if (theMessageObject.canHandleAutomatically(this)) {
			return theMessageObject.handleAutomatically(this);
		}
        
		//If it wasn't handled automatically, maybe its a custom Mario Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();
            //System.out.println("theCustomType"+theCustomType);
            if (theCustomType.equals("GETMARIOSTATE")) {
                //It is a request for the state
                double mariopx=mario_x;
                double mariopy=mario_y;
                MarioStateResponse theResponseObject = new MarioStateResponse(mariopx,mariopy);
                return theResponseObject.makeStringResponse();
            }

        }
        else if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvQueryVarRanges.id()) {
        
        	System.out.println("kEnvQueryVarRanges"+theMessageObject.getTheMessageType());
        }
        //System.out.println("Mario doesn't know how to handle message "+ theMessage);
        System.err.println("We need some code written in Env Message for Mario.. unknown request received: " + theMessage);
        Thread.dumpStack();
		return null;
	}
	public Observation env_start() {
		//System.err.println("env_start");
		GlueMario.action_direction =0;
		GlueMario.action_jump = false;
		GlueMario.action_speed = false;
		//Mario.instance.tickNoMove();
		synchronized (glue_block) {
			try {
				glue_block.notify();
				glue_block.wait();
			}
			catch (Exception e) {

			}
		}
		return rot.o;
	}
	public Reward_observation_terminal env_step(Action act) {
		trial_steps++;
//		System.out.println("terminal:"+rot.isTerminal());
		if (!rot.isTerminal())
		{	
		   GlueMario.action_direction = act.intArray[0];
		   GlueMario.action_jump = act.intArray[1] == 1;
		   GlueMario.action_speed = act.intArray[2] == 1;
		
//		   this.last_action_direction = GlueMario.action_direction;
//		   this.last_action_jump = GlueMario.action_jump;
//		   this.last_action_speed = GlueMario.action_speed;
//		}
//		else{
//			GlueMario.action_direction=this.last_action_direction; 
//			GlueMario.action_jump= this.last_action_jump;
//			GlueMario.action_speed= this.last_action_speed;
//			last_rot=rot;
//			last_rot.terminal=0;
//			this.time_step_after_die--;
//			System.out.println("step_after_death"+this.time_step_after_die);
			if (go_fast)
				if ((System.nanoTime() / 1000000000.0) - comp.time > .005)
					comp.lastTick = comp.tick;
			
			synchronized (glue_block) {
				try {
					glue_block.notify();
					glue_block.wait();
				}
				catch (Exception e) {

				}
			}
			//System.out.println("Observation"+rot.o.toString());
//			if (this.time_step_after_die>0)
//			   return last_rot;
//			else
//			   return rot;	
		}
//		System.out.println("action_direction:"+action_direction);
//		System.out.println("action_jump:"+action_jump);
//		System.out.println("action_speed:"+action_speed);
		
//		System.out.println("env_step");
		
		
		if (go_fast)
			if ((System.nanoTime() / 1000000000.0) - comp.time > .005)
				comp.lastTick = comp.tick;
		
		synchronized (glue_block) {
			try {
				glue_block.notify();
				glue_block.wait();
			}
			catch (Exception e) {

			}
		}
		//System.out.println("Observation"+rot.o.toString());
		return rot;
	}
	
	/**
	 * Note by josago: This method seems to populate the contents of the Observation object that will in turn be used to define a state representation.
	 * @param lscene A LevelScene object from the Mario environment.
	 */
	public static void levelCheckIn(LevelScene lscene) {
		if (!glue_running) return;
		ticks_since_last_action++;
		
		//ongoing action
		if (true) { //set to false to control mario from the environment window
			lscene.toggleKey(Mario.KEY_LEFT, action_direction == -1);
			lscene.toggleKey(Mario.KEY_RIGHT, action_direction == 1);
			if (ticks_since_last_action != 1) {
				lscene.toggleKey(Mario.KEY_JUMP, action_jump);
				lscene.toggleKey(Mario.KEY_SPEED, action_speed);
			}
			else {
				lscene.toggleKey(Mario.KEY_JUMP, false);
				lscene.toggleKey(Mario.KEY_SPEED, false);
			}
		}
		if (ticks_since_last_action < param.ticks_per_action)
			return;
		ticks_since_last_action = 0;
		
		//System.err.println("levelCheckIn");
		trial_reward = param.reward_step*(trial_steps-1);
		trial_reward += param.reward_coin*Mario.coins;
		trial_reward += param.reward_kill*Mario.kills;

		LevelRenderer lr = lscene.getLayer();
		
		Vector<Integer> o_ints = new Vector<Integer>();
		Vector<Double> o_doubles = new Vector<Double>();
		
		o_ints.add(lr.xCam/16);

		Vector<Integer> mario_ints = getSpriteInts(Mario.instance);
		Vector<Double> mario_doubles = getSpriteDoubles(Mario.instance);
		mario_x = (int)(mario_doubles.get(0).doubleValue());
		mario_y = 15-(int)(mario_doubles.get(1).doubleValue());
		o_ints.addAll(mario_ints);
		o_doubles.addAll(mario_doubles);
		
		for (Sprite s : lscene.getSprites()) {
			if (s instanceof Mario)
				continue;
			Vector<Integer> s_ints = getSpriteInts(s);
			if (s_ints != null) {
				Vector<Double> s_doubles = getSpriteDoubles(s);
				o_ints.addAll(s_ints);
				o_doubles.addAll(s_doubles);
			}
		}
		
		for (Sprite s : lscene.getSprites()) {
			s.last_step_x = s.x;
			s.last_step_y = s.y;
		}
		
		rot.o.intArray = new int[o_ints.size()];
		rot.o.doubleArray = new double[o_doubles.size()];
		rot.o.charArray = getBlocks(lr, lscene.getLevel());
		for (int i=0; i<rot.o.intArray.length; i++)
			rot.o.intArray[i] = o_ints.get(i).intValue();
		for (int i=0; i<rot.o.doubleArray.length; i++)
			rot.o.doubleArray[i] = o_doubles.get(i).doubleValue();
		rot.r = trial_reward-last_trial_reward;
		run_reward += rot.r;
		last_trial_reward = trial_reward;
		rot.terminal = 0;
		
		synchronized (glue_block) {
			try {
				glue_block.notify();
				glue_block.wait();
			}
			catch (Exception e) {

			}
		}			
	}
	
	/**
	 * Note by josago: This method probably needs re-writing so that it can parse all kinds of tiles from different types of Mario levels.
	 */
	static char[] getBlocks(LevelRenderer lr, Level level) {
		StringBuffer sb = new StringBuffer();
        for (int y = lr.yCam / 16; y <= (lr.yCam + lr.height) / 16; y++) {
        	for (int x = lr.xCam / 16; x <= (lr.xCam + lr.width) / 16; x++) {
                int b = lr.level.getBlock(x, y);
                if (mario_x == x && mario_y == y)
                	sb.append('M');
                else {
                	if (x == level.xExit)
                		sb.append('!');
                	else if (b == 0)
                		sb.append(' ');
                	else if (b == 34)
                		sb.append('$');
                	else if (b == 16 || b == 17)
                		sb.append('b');
                	else if (b == 21 || b == 22)
                		sb.append('?');
                	else if (b == 10 || b == 11 || b == 26 || b == 27)
                		sb.append('|');
                	else {
                        int code = 0;
                        if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_UPPER) > 0)
                        	code += 1;
                        if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_LOWER) > 0)
                        	code += 2;
                        if (((Level.TILE_BEHAVIORS[b & 0xff]) & Level.BIT_BLOCK_ALL) > 0)
                        	code = 7;
                        if (code != 0)
                        	sb.append(code);
                        else
                        	sb.append(' ');
                	}
                }
            }
        	sb.append('\n');
        }
        return sb.toString().toCharArray();
	}
	
	public static Vector<Integer> getSpriteInts(Sprite s) {
		int type = -1;
		boolean winged = false;
		if (s instanceof Mario) {
			type = 0;
			if (Mario.large)
				type = 10;
			if (Mario.fire)
				type = 11;
		}
		else if (s instanceof Enemy) {
			Enemy e = (Enemy)s;
			type = e.getType()+1;
			winged = e.getWinged();
		}
		else if (s instanceof Mushroom) {
			type = 6;
		}
		else if (s instanceof FireFlower) {
			type = 7;
		}
		else if (s instanceof Fireball) {
			type = 8;
		}
		else if (s instanceof Shell) {
			type = 9;
		}
		else
			return null;
		Vector<Integer> vi = new Vector<Integer>();
		vi.add(new Integer(type));
		vi.add(new Integer(winged?1:0));
		return vi;
	}
	
	public static Vector<Double> getSpriteDoubles(Sprite s) {
		Vector<Double> vd = new Vector<Double>();
		vd.add(new Double(s.getCurrentX()/16.0));
		vd.add(new Double(16.0-s.getCurrentY()/16.0));
		vd.add(new Double(param.ticks_per_action*s.getCurrentXs()/16));
		vd.add(new Double(param.ticks_per_action*-1*s.getCurrentYs()/16));
		return vd;
	}
	
	// Lydia: changed function from static to not static: only called from the non-static function 
	// env_init() in this class anyway
	public void launchMarioEnvironment() {
		glue_block = new Object();
		glue_running = true;
        MarioComponent mario; 
        if(ParamsATAA.useSimulatedHuman){
        	mario = new MarioComponent(640, 480, simHuman);
        }
        else{
        	mario = new MarioComponent(640, 480);
        }
        if (!go_dark) {
	        frame = new JFrame("RL-Glue Mario");
	        frame.setContentPane(mario);
	        frame.pack();
	        frame.setResizable(false);
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        
	        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        frame.setLocation((screenSize.width-frame.getWidth())/2, (screenSize.height-frame.getHeight())/2);
		    
	        frame.setVisible(!go_dark);
        }

        GlueMario.init(mario);
        //GlueMario.init();
	}
	

	
	public static void setParameters(ParameterHolder p) {
//		param.ticks_per_action = p.getIntegerParam("ticks per action");

		go_fast = p.getBooleanParam("fast");
		go_dark = p.getBooleanParam("dark");
		
		param.level_seed = p.getIntegerParam("level seed");
		param.level_type = p.getIntegerParam("level type");
		param.level_difficulty = p.getIntegerParam("level difficulty");
		
//		System.err.println("level params = "+param.level_seed+"/"+param.level_difficulty);
		if (p.isParamSet("instance")) {
			int instance = p.getIntegerParam("instance");
			switch (instance) {
			case 0:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 100;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 1;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 1:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 0;
				param.reward_death = 0;
				param.reward_step = -.01;
				param.reward_coin = 0;
				param.reward_kill = 1;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 2:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 10;
				param.reward_death = -100;
				param.reward_step = -.01;
				param.reward_coin = 0;
				param.reward_kill = 0;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 3:
				param.level_width = 150;
				param.max_trial_steps = 1000;
				param.reward_goal = 0;
				param.reward_death = 0;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 0;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 4:
				param.level_width = 150;
				param.max_trial_steps = 1000;
				param.reward_goal = 0;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 0;
				param.reward_kill = 0;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 5:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 100;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 1;
				param.speed_walk = 0.3;
				param.speed_run = 0.6;
				param.speed_jump = -1;
				param.speed_jump_sliding = -1;
				param.accel_gravity = .42;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 6:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 100;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 1;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 15;
				param.jump_time_sliding = -6;
				break;
			case 7:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 100;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 1;
				param.speed_walk = 1.2;
				param.speed_run = 2.4;
				param.speed_jump = -3.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .92;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 8:
				param.level_width = 620;
				param.max_trial_steps = 2000;
				param.reward_goal = 100;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 1;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .82;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			case 9:
				param.level_width = 320;
				param.max_trial_steps = 1000;
				param.reward_goal = 100;
				param.reward_death = -10;
				param.reward_step = -.01;
				param.reward_coin = 1;
				param.reward_kill = 1;
				param.speed_walk = 0.6;
				param.speed_run = 1.2;
				param.speed_jump = -1.9;
				param.speed_jump_sliding = -2;
				param.accel_gravity = .42;
				param.jump_time = 7;
				param.jump_time_sliding = -6;
				break;
			}
		}
		else {
			param.level_width = p.getIntegerParam("level width");
			param.max_trial_steps = p.getIntegerParam("max trial steps");;
			param.reward_goal = p.getDoubleParam("reward goal");
			param.reward_death = p.getDoubleParam("reward death");
			param.reward_step = p.getDoubleParam("reward step");
			param.reward_coin = p.getDoubleParam("reward coin");
			param.reward_kill = p.getDoubleParam("reward kill");
			param.speed_walk = p.getDoubleParam("speed walk");
			param.speed_run = p.getDoubleParam("speed run");
			param.speed_jump = p.getDoubleParam("speed jump");
			param.speed_jump_sliding = p.getDoubleParam("speed slidejump");
			param.accel_gravity = p.getDoubleParam("accel fall");
			param.jump_time = p.getIntegerParam("jump time");
			param.jump_time_sliding = p.getIntegerParam("slidejump time");
		}
	} 
	
	/**
	 * Settings for the levels that are actually used
	 * @return
	 */
	public static ParameterHolder getDefaultParameters() {
		ParameterHolder p = new ParameterHolder();

		p.addBooleanParam("fast", false);
		p.addBooleanParam("dark", false);
		
//		p.addIntegerParam("ticks per action", 5);
		
		p.addIntegerParam("level seed", 121);
		p.addIntegerParam("level type", 0);
		
		// Parameter level difficulty that is actually used		
		p.addIntegerParam("level difficulty", 2);
		
		p.addIntegerParam("instance", 0);
		/*
		p.addIntegerParam("level width", 320);

		p.addIntegerParam("max trial steps", 1000);

		p.addDoubleParam("reward goal", 100.0);
		p.addDoubleParam("reward death", -10.0);
		p.addDoubleParam("reward step", -0.01);
		p.addDoubleParam("reward coin", 1.0);
		p.addDoubleParam("reward kill", 1.0);

		p.addDoubleParam("speed walk", .6);
		p.addDoubleParam("speed run", 1.2);
		p.addDoubleParam("speed jump", -1.9);
		p.addDoubleParam("speed slidejump", -2.0);
		
		p.addDoubleParam("accel fall", .82);
		
		p.addIntegerParam("jump time", 7);
		p.addIntegerParam("slidejump time", -6);
		 */
		return p;
	}
	
    public static TaskSpecPayload getTaskSpecPayload(ParameterHolder p) {
    	setParameters(p);
        //String taskSpec = param.getTaskSpec();
    	String taskSpec = makeTaskSpec();
        return new TaskSpecPayload(taskSpec, false, "");
    }
//	
//	public static void main(String[] args) {
//        new EnvironmentLoader(new GlueMario(GlueMario.getDefaultParameters())).run();
//	}
	
	
	private static String makeTaskSpec() {
		
		TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
		
		theTaskSpecObject.setEpisodic();
        theTaskSpecObject.setDiscountFactor(1.0d);
        
     // FIRST add dicrete observation
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1));
		
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1));
		
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1));
		
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 3));
		
        theTaskSpecObject.addDiscreteObservation(new IntRange(0, 1));
		
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));
        
        theTaskSpecObject.addContinuousObservation(new DoubleRange(-5.0, 15.0));

        theTaskSpecObject.addDiscreteAction(new IntRange(-1, 1)); 
        
        theTaskSpecObject.addDiscreteAction(new IntRange(0, 1)); 
        
        theTaskSpecObject.addDiscreteAction(new IntRange(0, 1)); 
        
        theTaskSpecObject.setRewardRange(new DoubleRange(-1.0, 0.0));

        //This is a better way to tell the rows and cols
        theTaskSpecObject.setExtra("EnvName:Mario Revision:null");

        
        String taskSpecString = theTaskSpecObject.toTaskSpec();

        TaskSpec.checkTaskSpec(taskSpecString);
        return taskSpecString;
	}
}
