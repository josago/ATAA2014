package ataa2014;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedFast implements SimulatedHuman {

	private ArrayList <State> stateMemory;
	private ArrayList <ArrayList<SimulatedHuman.Event []>> eventMemory;
	private ArrayList <Integer> actionMemory;
	private int stepsSinceLastFeedback;
	private ArrayList <SimulatedHuman.Event> events;
	Random r;
	private ArrayList<double []> feedbackList; 
	
	public SimulatedFast(){
		stateMemory = new ArrayList <State> ();
		eventMemory = new ArrayList<ArrayList <SimulatedHuman.Event []>>();
		stepsSinceLastFeedback = 1000;
		events = new ArrayList <SimulatedHuman.Event>();
		r = new Random();
		feedbackList = new ArrayList<double []>();
	}

	@Override
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
	
	private void addFeedback(){
		// Actions represented as the keys in the class Mario (under sprites)
		if(stepsSinceLastFeedback < SimulatedHuman.minStepsBetweenFeedback)
		{
			double prob = r.nextDouble();
			if(prob < SimulatedHuman.pFeedback)
			{
				// If Mario has moved to the left in the last x frames and there is no pit or enemy close to the right: negative
				if(true){
					
				}
				// If Mario has been running to the right in the last x frames: positive
				
				// If 
			}
				
		}
	}

	// TODO: make sure this gets called somewhere by the controller or something
	@Override
	public void addInformation(State s, int action) {
		stateMemory.add(s);
		actionMemory.add(action);
		if (stateMemory.size() > SimulatedHuman.sizeMemeory){			
			stateMemory.remove(0);
			actionMemory.remove(0);
		}
		
	}

	@Override
	public void receiveEvent(Event e) {
		events.add(e);		
	}

}
