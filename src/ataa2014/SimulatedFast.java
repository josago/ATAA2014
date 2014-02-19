package ataa2014;

import java.util.ArrayList;

public class SimulatedFast implements SimulatedHuman {

	private ArrayList <SceneCustom> scenes;
	private ArrayList <State> states;
	
	public SimulatedFast(){
		scenes = new ArrayList <SceneCustom> ();
		states = new ArrayList <State> ();
	}

	@Override
	public float getFeedback(SceneCustom scene, State s) {
		scenes.add(scene);
		if (scenes.size() > SimulatedHuman.sizeMemeory){
			scenes.remove(0);
		}
		states.add(s);
		if (states.size() > SimulatedHuman.sizeMemeory){
			states.remove(0);
		}
		return feedbackFast(); 
	}

	private float feedbackFast() {
		// If Mario has moved to the left in the last x frames and there is no pit or enemy close to the right: negative
		
		// If Mario has been running to the right in the last x frames: positive
		
		// If 
		
		return 0;
	}

}
