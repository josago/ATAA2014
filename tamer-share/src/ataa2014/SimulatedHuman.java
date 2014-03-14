package ataa2014;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedHuman {
	 
	public static final int sizeMemeory = 5;
		
	
	public static enum Event {hurtByEnemy, wasShooting, gotPowerUp, killedEnemy, gotCoin, wasRunning, carryingSomething, nothing, threwSomething, diedInPit};
	
	public ArrayList <double[]> stateMemory;
	public ArrayList <SimulatedHuman.Event> eventMemory;
	public int stepsSinceLastFeedback;	
	public Random r;
	public ArrayList<double []> feedbackList; 
	
	protected double prob_feedback = 0.3;
	
	
	public SimulatedHuman(){
		stateMemory = new ArrayList <double[]> ();
		eventMemory = new ArrayList <SimulatedHuman.Event>();
		stepsSinceLastFeedback = 1000;
		
		r = new Random();
		feedbackList = new ArrayList<double []>();
	}

	public double getFeedback() {
		for(double[] elem: feedbackList){
			elem[0] -= 1.0;
		}
		
		addFeedback();
		
		double [] fb = feedbackList.get(0);
		if(fb[0] == 0.0){
			feedbackList.remove(0);
			//System.out.println("Feedback from human: " + fb[1]);
			return fb[1];
		}	
	
		return 0.0;		
	}
	
	protected void addFeedback(){
		
	}

		
	public void addInformation(double[] feats) {
		

		stateMemory.add(feats);
		if (stateMemory.size() > SimulatedHuman.sizeMemeory){			
			stateMemory.remove(0);
		}
		
		
	}

	
	public void receiveEvent(Event e) {
		eventMemory.add(e);	
	}
	
	
	//return a sum of directions in memory (dir: -1=left 0=still 1=right)
	protected int getdir() {
		int dir = 0;
		
		for (int i = 0; i < stateMemory.size(); i++)
			dir += stateMemory.get(i)[0];
		
	    return dir;
	    }
	
	//return a sum of all jumps in memory
	protected int getjumps() {
		int jumps = 0;
		
		for (int i = 0; i < stateMemory.size(); i++)
			jumps += stateMemory.get(i)[2];
		
	    return jumps;
	  }
	
	protected double[] getstep(int i){
		double step_x = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_STEP + StateRepresentation.VECTOR_DX];
		double step_y = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_STEP + StateRepresentation.VECTOR_DY];
		
		return new double[]{step_x, step_y};
	}
	
	protected boolean stuckAtStep(){		
		if(stateMemory.size()>1)
		{
			double[] step_0 = getstep(stateMemory.size()-1);
			double[] step_1 = getstep(stateMemory.size()-2);
			return step_0[0] == step_1[0] && step_0[1] == step_1[1];
		}
		return false;
	}
	
	protected double[] getPit(int i)
	{
		double pit_x = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_PIT + StateRepresentation.VECTOR_DX];
		double pit_y = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_PIT + StateRepresentation.VECTOR_DY];
		
		return new double[]{pit_x, pit_y};
	}
}
