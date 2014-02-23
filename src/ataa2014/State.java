package ataa2014;

/**
 * This class must be extended by any state representation to be integrated into the system, since it will be later used by all supervised learning algorithms.
 * @author josago
 */
public abstract class State
{
	/**
	 * Returns a vector representation of the state, suitable for being fed into a supervised learning algorithm.
	 */
	public abstract double[] vectorRepresentation();
	
	/**
	 * Returns the length of the vector representation of the state.
	 */
	public static int vectorRepresentationLength()
	{
		return 0;
	}
}
