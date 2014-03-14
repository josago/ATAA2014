package ataa2014;

import java.util.ArrayList;
import java.util.Random;
/*
 * The objective is to finish the level as fast as possible.
 * This simulation
 * Gives negative reward when mario: runs in the wrong direction for a time,
 * get stucked in front of a step.
 * positive reward when run in the right direction for a time
 * Get out of stuckenes  
 * 
 */
public class SimulatedFast extends SimulatedHuman {
	
	private static final int x = 0;
	private static final int y = 1;
	
	public SimulatedFast(){
		super();
	}	
	
	@Override
	protected void addFeedback(){
		// Actions represented as first three entries: 
		// 1st entry: -1 for going left, 1 for going right and 0 for nothing
		// 2nd entry: running or not. 3rd entry: jumping or not.
		//second element of feedback should be the actual feedback: -1 or 1
		double [] feedback = {1.0, 0.0};
		
		/*// Check if our interpretation of the actions is correct
		if(stateMemory.size()>0)
		{
			if(stateMemory.get(0)[1]==1)
			{
				System.out.println("Jump");
			}
			if(stateMemory.get(0)[2]==1)
			{
				System.out.println("Speed");
			}
			if(stateMemory.get(0)[0]==1)
			{
				System.out.println("To right");
			}
		}*/
		
			
		//If there are events you always respond to those
		if(eventMemory.size() > 0)
		{
			if(eventMemory.contains(Event.hurtByEnemy) || eventMemory.contains(Event.diedInPit)){
				System.out.println("DIEEEEEEEEEEEEEEEEED");
				feedback[1] = -1.0;
			}		
			else if(eventMemory.contains(Event.killedEnemy) || eventMemory.contains(Event.gotPowerUp) )
			{
				feedback[1] = 1.0;	
				System.out.println("KILLED ENEMY OR GOT POWERUP");
			}			
		}		
		else if(r.nextDouble() < prob_feedback)
		{			
			if(stateMemory.size() <= 4)
			{
				if(getdir(stateMemory) > 0 )
					feedback[1] = 1.0;
				else
					feedback[1] = -1.0;
					
			}
			else if(stateMemory.size() > 4)
			{		
				double[] pit = getPit(0);
				if(pit[0] < 2 && pit[0] > -2)
				{
					System.out.println("Pit is close");
				}
				
				int direction = getdir(stateMemory);
					
				// If Mario has been running to the right in the last x frames: positive
				if(direction < 3){
					feedback[1] = -1.0;
				}
				else if(direction >= 3){
					feedback[1] = 1.0;
				}
				
				double[] step = getstep(0); 
				
				
				if( (step[x] < 2 && step[x] > -2) && step[y] < 0 )
				{
					//System.out.println("Close to step up");
					
					
					//If stuck at step					
					if(stuckAtStep())
					{
						System.out.println("Getting stuck");
						feedback[1] = -1.0;
					}
					
					//If the last action was to jump towards the step
					else
					{
						boolean towardsStep = stateMemory.get(0)[1] > 0 && step[x] > 0;
						if(stateMemory.get(0)[1] == 1 && towardsStep)
						{						
							feedback[1] = 1.0;
							System.out.println("Close to step and get out of there");
						}
					}
		
				}
			}	
		}
		
		eventMemory.clear();
		
		feedbackList.add(feedback);
	}

}
