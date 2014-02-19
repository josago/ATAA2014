package ataa2014;

public interface SimulatedHuman {

	public static final int minDelay = 200;
	public static final int deviationDelay = 100; 
	public static final int sizeMemeory = 5;
	public static final int minStepsBetweenFeedback = 15;
	public static final double pFeedback = 0.3;
	
	public float getFeedback();
	
	public void addInformation(SceneCustom scene, State s);
	
}
