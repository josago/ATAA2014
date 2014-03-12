package ataa2014;

import java.util.ArrayList;
import java.util.Random;
/*
 * The objective is to finish the level as fast as possible.
 * Gives negative reward when mario: runs in the wrong direction for a time,
 * get stucked in front of a step.
 * positive reward when run in the right direction for a time 
 * 
 */
public class SimulatedFast extends SimulatedHuman {

	private final double pFeedback = 0.3;
	private double step_x;
	private double step_y;
	
	public SimulatedFast(){
		super();
	}

	
	private int getdir(ArrayList<double[]> stateMemory) {
		int dir = 0;
		
		for (int i = 0; i < stateMemory.size(); i++)
			dir += stateMemory.get(i)[0];
		
	    return dir;
	    }
	
	
	private int getjumps(ArrayList<double[]> stateMemory) {
		int jumps = 0;
		
		for (int i = 0; i < stateMemory.size(); i++)
			jumps += stateMemory.get(i)[2];
		
	    return jumps;
	    }
	
	@Override
	protected void addFeedback(){
		// Actions represented as first three entries: 
		
		//secound element should be the feedback -1 or 1
		double [] feedback = {0.0, 0.0};
		
		double dev = r.nextGaussian();
		dev = Math.abs(dev) * deviationDelay;
		
		if(stateMemory.size() <= 4)
		{
			feedback[0] = 1;
			if(getdir(stateMemory) > 0 )
				feedback[1] = 1.0;
			else
				feedback[1] = -1.0;
				
		}
		
		
		else if(stateMemory.size() > 4)
		{
			feedback[0] = 1; // minDelay + dev;
			
			int jump = getjumps(stateMemory);
			int direction = getdir(stateMemory);
			
			double speed = stateMemory.get(0)[1] + stateMemory.get(1)[1] + stateMemory.get(2)[1] + stateMemory.get(3)[1] + stateMemory.get(4)[1];
			/*
			System.err.println("direction : " + direction);
			System.err.println("jump : " + jump);
			System.out.println(getjumps(stateMemory));
			System.err.println("speed : " + speed);
			*/
			// If Mario has been running to the right in the last x frames: positive
			if(direction < 3){
				System.err.println("LEFT :");
				feedback[1] = -1.0;
			}
			else if(direction >= 3){
				System.err.println("RIGHT :");
				feedback[1] = 1.0;
			}
			
			step_x = stateMemory.get(0)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_STEP + StateRepresentation.VECTOR_DX];
			step_y = stateMemory.get(0)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_STEP + StateRepresentation.VECTOR_DY];
			
			//if(step != 0)
			System.out.println(" STEEEP X :" + step_x);
			System.out.println(" STEEEP Y :" + step_y);
			
		}
		
		if(eventMemory.size() > 0)
		{
			if(eventMemory.get(0) == Event.hurtByEnemy){
				System.out.println("DIEEEEEEEEEEEEEEEEED");
				feedback[1] = -1.0;
			}		
			else if(eventMemory.get(0) == Event.killedEnemy || eventMemory.get(0) == Event.gotPowerUp )
			{
				feedback[1] = 1.0;	
				System.out.println("KILLED ENEMY HAHAHAHAHAHAHAH");
			}
		}
		
		eventMemory.clear();
		
		feedbackList.add(feedback);
	}

}
