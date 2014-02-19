package ataa2014;

public interface SimulatedHuman {

	public static final int minDelay = 200;
	public static final int deviationDelay = 100; 
	public static final int sizeMemeory = 5;
	public static final int minStepsBetweenFeedback = 15;
	public static final double pFeedback = 0.3;
	
	public double getFeedback();
	
	public void addInformation(State s, int action);
	
	public static enum Event {hurtByEnemy, wasShooting, gotPowerUp, killedEnemy, gotCoin, wasRunning, nothing};
	
	public void receiveEvent(Event e);
	
}
