package ataa2014;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedFast extends SimulatedHuman {

	private final double pFeedback = 0.3;
	
	public SimulatedFast(){
		super();
	}
	
	
	@Override
	protected void addFeedback(){
		// Actions represented as the keys in the class Mario (under sprites)
		
		double [] feedback = {0.0, 0.0};
		
		if (eventMemory.size() > SimulatedHuman.sizeMemeory){	
			eventMemory.remove(0);
			eventMemory.add(new ArrayList <SimulatedHuman.Event>());
		}
		
		if(stepsSinceLastFeedback < SimulatedHuman.minStepsBetweenFeedback)
		{
			double prob = r.nextDouble();
			if(prob < pFeedback)
			{
				double dev = r.nextGaussian();
				dev = Math.abs(dev) * deviationDelay;
				double timeTillFeedback = minDelay + dev;
				
				// If Mario has moved to the left in the last x frames and there is no pit or enemy close to the right: negative
				
				// If Mario has been running to the right in the last x frames: positive
				
				// If 
			}
				
		}
		
		
		feedbackList.add(feedback);
	}

}
