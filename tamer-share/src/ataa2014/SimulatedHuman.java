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
	
	protected double prob_feedback = 0.6;
	protected final int x = 0;
	protected final int y = 1;
	
	
	public SimulatedHuman(){
		stateMemory = new ArrayList <double[]> ();
		eventMemory = new ArrayList <SimulatedHuman.Event>();
		stepsSinceLastFeedback = 1000;
		
		r = new Random();
		feedbackList = new ArrayList<double []>();
	}

	public double getFeedback() {
		addFeedback();		
		for(double[] elem: feedbackList){
			elem[0] -= 1.0;
		}
		
		
		
		double [] fb = feedbackList.get(0);
		if(fb[0] == 0.0){
			feedbackList.remove(0);
			if(fb[1] != 1.0 && fb[1] != -1.0 && fb[1] != 0.0)
				System.err.println("Wrong feedback value sim human");
			//System.out.println("Feedback from human: " + fb[1]);
			return fb[1];
		}	
	
		return 0.0;		
	}
	
	protected void addFeedback(){
		
	}

		
	public void addInformation(double[] feats) {
//		System.out.println("------------");
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
	
	protected boolean dirRight() {		
		for (int i = stateMemory.size()-1; i>1; i--)
			if(stateMemory.get(i)[0] == -1 || stateMemory.get(i)[0] == 0)				
				return false;		
	    return true;
	}
	
	protected boolean dirLeft() {
		for (int i = stateMemory.size()-1; i>1; i--)
		{
			if(stateMemory.get(i)[0] == 1)				
				return false;
		}
	    return true;
	}
	
	protected double[] getPowerUp()
	{
		double [] loc = new double[2];
		int i = stateMemory.size()-1;
		loc[0] = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_FIREFLOWER + StateRepresentation.VECTOR_DX];
		loc[1] = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_FIREFLOWER + StateRepresentation.VECTOR_DX];
		if(loc[0] == StateRepresentation.DISTANCE_FAR_AWAY)
		{
			loc[0] = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_MUSHROOM + StateRepresentation.VECTOR_DX];
			loc[1] = stateMemory.get(i)[StateRepresentation.FEATURES_ACTION + 2 * StateRepresentation.ENTITY_MUSHROOM + StateRepresentation.VECTOR_DX];			
		}
		return loc;
	}
	
	protected double[] getAction()
	{
		double [] ac = new double[3];
		int i = stateMemory.size()-1;
		ac[0] = stateMemory.get(i)[0];
		ac[1] = stateMemory.get(i)[1];
		ac[2]= stateMemory.get(i)[2];
		return ac;
	}
	
	
	protected boolean jumpedOverPit()
	{		
		if(stateMemory.size()>3)
		{
			double[] pit1 = getPit(stateMemory.size()-1);
			double[] pit2 = getPit(stateMemory.size()-2);
			double[] pit3 = getPit(stateMemory.size()-3);
			if(pit1[x] < 0 && (pit2[x] > 0 || pit3[x] > 0) )
			{
				System.out.println("Jumped over pit!!!");
				return true;				
			}
		}
		return false;
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
			//System.out.println(step_0[0] + " " + step_1[0]);
			if( Math.abs(step_0[0] - step_1[0]) < 0.1 && Math.abs(step_0[1] - step_1[1])<0.1)
				return true;
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
