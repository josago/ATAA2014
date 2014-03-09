package org.rlcommunity.environments.mario;

import java.net.URL;
import java.util.Random;

import org.rlcommunity.environments.mario.visualizer.MarioVisualizer;
import org.rlcommunity.environments.puddleworld.PuddleWorld;
import org.rlcommunity.environments.puddleworld.PuddleWorldState;
import org.rlcommunity.environments.puddleworld.visualizer.PuddleWorldVisualizer;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.EnvironmentLoader;

import ataa2014.SimulatedHuman;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlVizLib.messaging.interfaces.HasImageInterface;
import rlVizLib.messaging.interfaces.getEnvMaxMinsInterface;
import rlVizLib.messaging.interfaces.getEnvObsForStateInterface;

public class GeneralMario extends GlueMario implements
HasAVisualizerInterface,
HasImageInterface {
	static final int numActions = 3;
	private Random randomGenerator = new Random();
	protected MarioState theState;
	
	
	public GeneralMario() {
		setParameters(GlueMario.getDefaultParameters());
    }
	
	public GeneralMario(ParameterHolder p) {
		setParameters(p);
	}
	
	public GeneralMario(SimulatedHuman h) {
		// set parameters is called in super
		super(h);		
	}

	
	
	
	/**
     * The value function will be drawn over the position and velocity.  This 
     * method provides the max values for those variables.
     * @param dimension
     * @return
     */
//    public double getMaxValueForQuerableVariable(int dimension) {
//        if (dimension == 0) {
//            return theState.worldRect.getMaxX();
//        } else {
//            return theState.worldRect.getMaxY();
//        }
//    }
	
	 /**
     * The value function will be drawn over the position and velocity.  This 
     * method provides the min values for those variables.
     * @param dimension
     * @return
     */
//    public double getMinValueForQuerableVariable(int dimension) {
//        if (dimension == 0) {
//            return theState.worldRect.getMinX();
//        } else {
//            return theState.worldRect.getMinY();
//        }
//    }
    
    

	/**
     * How many state variables are there (used for value function drawing)
     * @return
     */
    public int getNumVars() {
        return 2;
    }
    
    public String getVisualizerClassName() {
        return MarioVisualizer.class.getName();
    }
	
	/**
     * So we can draw a pretty image in the visualizer before we start
     * @return
     */
    public URL getImageURL() {
        URL imageURL = PuddleWorld.class.getResource("/images/mario.png");
        return imageURL;
    }
	
	/**
     * Given a state, return an observation.  This is trivial in mountain car
     * because the observation is the same as the internal state 
     * @param theState
     * @return
     */
    public Observation getObservationForState(Observation theState) {
        return theState;
    }
	
	 public static void main(String[] args) {
	        EnvironmentLoader L = new EnvironmentLoader(new GeneralMario());
	        L.run();
	    }
	 class DetailsProvider implements hasVersionDetails {

		    public String getName() {
		        return "Mario 1.1";
		    }

		    public String getShortName() {
		        return "Mario";
		    }

		    public String getAuthors() {
		        return "Brian Tanner, Leah Hackman, Matt Radkie, Andrew Butcher";
		    }

		    public String getInfoUrl() {
		        return "http://library.rl-community.org/mario";
		    }

		    public String getDescription() {
		        return "Mario problem from the reinforcement learning library.";
		    }
	 }
}