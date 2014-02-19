package ataa2014;

import java.util.ArrayList;
import java.util.Random;

public class SimulatedFast extends SimulatedHuman {

	
	public SimulatedFast(){
		super();
	}
	
	
	@Override
	protected void addFeedback(){
		// Actions represented as the keys in the class Mario (under sprites)
				
		if (eventMemory.size() > SimulatedHuman.sizeMemeory){	
			eventMemory.remove(0);
			eventMemory.add(new ArrayList <SimulatedHuman.Event>());
		}
		
		if(stepsSinceLastFeedback < SimulatedHuman.minStepsBetweenFeedback)
		{
			double prob = r.nextDouble();
			if(prob < SimulatedHuman.pFeedback)
			{
				double dev = r.nextGaussian();
				dev = Math.abs(dev) * deviationDelay;
				double timeTillFeedback = minDelay + dev;
				
				// If Mario has moved to the left in the last x frames and there is no pit or enemy close to the right: negative
				
				// If Mario has been running to the right in the last x frames: positive
				
				// If 
			}
				
		}
	}

}
