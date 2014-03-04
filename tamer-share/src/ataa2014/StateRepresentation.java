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
	public StateRepresentation(int[][] theObsIntRanges,
			double[][] theObsDoubleRanges, int[][] theActIntRanges,
			double[][] theActDoubleRanges) {
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int[] getActionFeatIndices()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getNumFeatValsPerFeatI()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getSAFeats(Observation obs, Action act)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getSFeats(Observation obs)
	{
		// TODO Auto-generated method stub
		return null;
	}

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
