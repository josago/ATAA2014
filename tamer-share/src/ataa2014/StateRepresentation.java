package ataa2014;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * Our custom state representation for the Mario environment, adapted to the TAMER framework.
 * @author josago
 */
public class StateRepresentation extends FeatGenerator
{
	// Important note about the format of the Observation objects for the Mario environment:
	//     obs.charArray    -> Contains the matrix-like tile representation of the current view (including Mario itself, rounded to the nearest integer).
	//     obs.intArray     -> Array of integers having the format {sprite_1_type, sprite_1_winged?, sprite_2_type, sprite_2_winged?, ...}
	//     obs.doubleArray  -> Array of doubles having the format {sprite_1_X_position, sprite_1_Y_position, sprite_1_X_speed, sprite_1_Y_speed, sprite_2_X_position, ...}. All measures are given in block-sized units.
	
	public StateRepresentation(int[][] theObsIntRanges, double[][] theObsDoubleRanges, int[][] theActIntRanges, double[][] theActDoubleRanges)
	{
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method returns a list of indices that indicate where the action features lie in the vectors returned by the other methods of this class.
	 */
	@Override
	public int[] getActionFeatIndices()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method returns a list of integers indicating how many different values each of the indices of the feature array can take.
	 * In TAMER this method seems to be implemented only for action features; it returns zero for the rest of indices.
	 */
	@Override
	public int[] getNumFeatValsPerFeatI()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method returns the feature vector that represents both the observation and the action given as parameters.
	 */
	@Override
	public double[] getSAFeats(Observation obs, Action act)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method returns the feature vector that represents the observation given as parameter.
	 */
	@Override
	public double[] getSFeats(Observation obs)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	// The next overriden methods are not implemented in the original TAMER class "FeatGen_Mario":

	@Override
	public double[] getMaxPossFeats()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getMinPossFeats()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getMaxPossSFeats()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getMinPossSFeats()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
