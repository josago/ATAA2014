package org.rlcommunity.environments.mario.visualizer;

import java.util.Vector;

import org.rlcommunity.environments.mario.messages.MarioStateRequest;
import org.rlcommunity.environments.mario.messages.MarioStateResponse;
import org.rlcommunity.environments.mario.viz.MarioComponent;
import org.rlcommunity.environments.puddleworld.visualizer.PuddleMapComponent;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.general.TinyGlue;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.agent.AgentValueForObsRequest;
import rlVizLib.messaging.agent.AgentValueForObsResponse;
import rlVizLib.messaging.environment.EnvObsForStateRequest;
import rlVizLib.messaging.environment.EnvObsForStateResponse;
import rlVizLib.messaging.environment.EnvRangeRequest;
import rlVizLib.messaging.environment.EnvRangeResponse;
import rlVizLib.visualization.AbstractVisualizer;
import rlVizLib.visualization.AgentOnValueFunctionVizComponent;
import rlVizLib.visualization.GenericScoreComponent;
import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.ValueFunctionVizComponent;
import rlVizLib.visualization.interfaces.AgentOnValueFunctionDataProvider;
import rlVizLib.visualization.interfaces.DynamicControlTarget;
import rlVizLib.visualization.interfaces.GlueStateProvider;
import rlVizLib.visualization.interfaces.ValueFunctionDataProvider;

public class MarioVisualizer extends AbstractVisualizer implements ValueFunctionDataProvider, AgentOnValueFunctionDataProvider, GlueStateProvider {
	private Vector<Double> mins = null;
    private Vector<Double> maxs = null;
    
    org.rlcommunity.environments.mario.messages.MarioStateResponse theCurrentState = null;

    private int lastStateUpdateTimeStep = -1;
    private int lastAgentValueUpdateTimeStep = -1;
    
    //Will have to find a way to easily generalize this and move it to vizlib
    TinyGlue glueState = null;
    //This is a little interface that will let us dump controls to a panel somewhere.
    DynamicControlTarget theControlTarget = null;
    private ValueFunctionVizComponent theValueFunction;
    private AgentOnValueFunctionVizComponent theAgentOnValueFunction;
    private boolean printedQueryError = false;
    
    public MarioVisualizer(TinyGlue glueState, DynamicControlTarget theControlTarget) {
        super();

        this.glueState = glueState;
        this.theControlTarget = theControlTarget;
        
        setupVizComponents();
        
    }
    
    
    
    protected void setupVizComponents() {
    	//theValueFunction = new ValueFunctionVizComponent(this, theControlTarget, this.glueState);
        //theAgentOnValueFunction = new AgentOnValueFunctionVizComponent(this, this.glueState);
    	
        SelfUpdatingVizComponent marioComponent = new MarioVizComponent(this);
        SelfUpdatingVizComponent scoreComponent = new GenericScoreComponent(this);

//        super.addVizComponentAtPositionWithSize(theValueFunction, 0, 0, 1.0, 1.0);
       super.addVizComponentAtPositionWithSize(marioComponent, 0, 0, 1.0, 1.0);
//        super.addVizComponentAtPositionWithSize(theAgentOnValueFunction, 0, 0,1.0,1.0);
       super.addVizComponentAtPositionWithSize(scoreComponent, 0, 0,1.0,1.0);
    }
    
    public synchronized void updateAgentState(boolean force) {
        //Only do this if we're on a new time step
        int currentTimeStep = glueState.getTotalSteps();

        if (theCurrentState == null || currentTimeStep != lastStateUpdateTimeStep || force) {
            theCurrentState = MarioStateRequest.Execute();
            lastStateUpdateTimeStep = currentTimeStep;
        }
    }
    
    public void updateEnvironmentVariableRanges() {
        //Get the Ranges (internalize this)
        EnvRangeResponse theERResponse = EnvRangeRequest.Execute();

        if (theERResponse == null) {
            System.err.println("Asked an Environment for Variable Ranges and didn't get back a parseable message.");
            Thread.dumpStack();
            System.exit(1);
        }

        mins = theERResponse.getMins();
        maxs = theERResponse.getMaxs();
    }
    
    public double getMaxValueForDim(int whichDimension) {
        if (maxs == null) {
            updateEnvironmentVariableRanges();
        }
        return maxs.get(whichDimension);
    }

    public double getMinValueForDim(int whichDimension) {
        if (mins == null) {
            updateEnvironmentVariableRanges();
        }
        return mins.get(whichDimension);
    }
    
    public Vector<Observation> getQueryObservations(Vector<Observation> theQueryStates) {
        EnvObsForStateResponse theObsForStateResponse = EnvObsForStateRequest.Execute(theQueryStates);

        if (theObsForStateResponse == null) {
            System.err.println("Asked an Environment for Query Observations and didn't get back a parseable message.");
            Thread.dumpStack();
            System.exit(1);
        }
        return theObsForStateResponse.getTheObservations();
    }
    
    AgentValueForObsResponse theValueResponse = null;
    
    public Vector<Double> queryAgentValues(Vector<Observation> theQueryObs) {
        int currentTimeStep = glueState.getTotalSteps();

        boolean needsUpdate = false;
        if (currentTimeStep != lastAgentValueUpdateTimeStep) {
            needsUpdate = true;
        }
        if (theValueResponse == null) {
            needsUpdate = true;
        } else if (theValueResponse.getTheValues().size() != theQueryObs.size()) {
            needsUpdate = true;
        }
        if (needsUpdate) {
            try {
                theValueResponse = AgentValueForObsRequest.Execute(theQueryObs);
                lastAgentValueUpdateTimeStep = currentTimeStep;
            } catch (NotAnRLVizMessageException e) {
                theValueResponse = null;
            }
        }

        if (theValueResponse == null) {
            if (!printedQueryError) {
                printedQueryError = true;
                System.err.println("In the Mountain Car Visualizer: Asked an Agent for Values and didn't get back a parseable message.  I'm not printing this again.");
                theValueFunction.setEnabled(false);
                theAgentOnValueFunction.setEnabled(false);
            }
            //Return NULL and make sure that gets handled
            return null;
        }

        return theValueResponse.getTheValues();
    }
    
    public double getCurrentStateInDimension(int whichDimension) {
        ensureStateExists();
        if (whichDimension == 0) {
            return theCurrentState.getPositionX();
        } else {
            return theCurrentState.getPositionY();
        }
    }
    private void ensureStateExists(){
        if(theCurrentState==null){
            updateAgentState(true);
        }
    }
    
    //This is the one required from RLVizLib, ours has a forcing parameter.  Should update the VizLib
    public void updateAgentState() {
        updateAgentState(false);
    }
    
    public TinyGlue getTheGlueState() {
        return glueState;
    }
}