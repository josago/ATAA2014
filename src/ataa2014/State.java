package ataa2014;

public interface State
{
	/**
	 * Returns a vector representation of the state, suitable for being fed into a supervised learning algorithm.
	 */
	public float[] vectorRepresentation();
	
	/**
	 * Returns the length of the vector representation of the state.
	 * @return
	 */
	public int vectorRepresentationLength();
}
