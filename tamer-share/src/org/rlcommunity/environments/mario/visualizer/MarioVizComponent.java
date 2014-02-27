package org.rlcommunity.environments.mario.visualizer;

import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;

import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class MarioVizComponent implements SelfUpdatingVizComponent, Observer {
	 private MarioVisualizer theVizualizer = null;
	 private VizComponentChangeListener theChangeListener;
	 boolean everDrawn = false;
	
	 public MarioVizComponent(MarioVisualizer theVizualizer) {
        this.theVizualizer = theVizualizer;
        theVizualizer.getTheGlueState().addObserver(this);
     }
	
	 public void render(Graphics2D g) {
		 
	 }
	 
	 public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
	        this.theChangeListener = theChangeListener;
	 }
	 
	 public void update(Observable o, Object arg) {
	        if (!everDrawn) {
	            theChangeListener.vizComponentChanged(this);
	        }

	 }
}