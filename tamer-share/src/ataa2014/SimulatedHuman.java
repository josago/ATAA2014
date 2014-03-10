package ataa2014;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedHuman {

	public static final double minDelay = 30;
	public static final double deviationDelay = 3; 
	public static final int sizeMemeory = 5;
		
	
	public static enum Event {hurtByEnemy, wasShooting, gotPowerUp, killedEnemy, gotCoin, wasRunning, carryingSomething, nothing, threwSomething};
	
	public ArrayList <double[]> stateMemory;
	public ArrayList <ArrayList<SimulatedHuman.Event>> eventMemory;
	public int stepsSinceLastFeedback;	
	public Random r;
	public ArrayList<double []> feedbackList; 
	
	
	public SimulatedHuman(){
		stateMemory = new ArrayList <double[]> ();
		eventMemory = new ArrayList<ArrayList <SimulatedHuman.Event>>();
		stepsSinceLastFeedback = 1000;
		for(int i = 0; i< SimulatedHuman.sizeMemeory; i++){
			eventMemory.add( new ArrayList <SimulatedHuman.Event>() );
		}
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
			return fb[1];
		}	
	
		return 0.0;		
	}
	
	protected void addFeedback(){
		
	}

	// TODO: make sure this gets called somewhere by the controller or something	
	public void addInformation(double[] feats) {
		//TODO: process features
		stateMemory.add(feats);
		if (stateMemory.size() > SimulatedHuman.sizeMemeory){			
			stateMemory.remove(0);
		}
		
	}

	
	public void receiveEvent(Event e) {
		eventMemory.get(eventMemory.size()-1).add(e);	
	}
	
	
}
