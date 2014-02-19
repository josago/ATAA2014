package ataa2014;

import java.util.ArrayList;

public class SimulatedFast implements SimulatedHuman {

	private ArrayList <SceneCustom> scenes;
	private ArrayList <State> states;
	private int stepsSinceLastFeedback;
	
	public SimulatedFast(){
		scenes = new ArrayList <SceneCustom> ();
		states = new ArrayList <State> ();
		stepsSinceLastFeedback = 1000;
	}

	@Override
	public float getFeedback() {
		
	// Actions represented as the keys in the class Mario (under sprites)
	
		// If Mario has moved to the left in the last x frames and there is no pit or enemy close to the right: negative
		
		// If Mario has been running to the right in the last x frames: positive
		
		// If 
		
		return 0;
	}
	

	@Override
	public void addInformation(SceneCustom scene, State s) {
		scenes.add(scene);
		if (scenes.size() > SimulatedHuman.sizeMemeory){
			scenes.remove(0);
			states.remove(0);
		}		
	}

	@Override
	public void receiveEvent(Event e) {
		// TODO Auto-generated method stub
		
	}

}
