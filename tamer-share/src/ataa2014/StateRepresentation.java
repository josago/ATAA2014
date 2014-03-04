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
	//     obs.charArray    -> Contains the matrix-like tile representation of the current view (including Mario itself, rounded to the nearest integer). Each tile is represented by 2 chars: code and representation.
	//     obs.intArray     -> Array of integers having the format {sprite_1_type, sprite_1_winged?, sprite_2_type, sprite_2_winged?, ...}
	//     obs.doubleArray  -> Array of doubles having the format {sprite_1_X_position, sprite_1_Y_position, sprite_1_X_speed, sprite_1_Y_speed, sprite_2_X_position, ...}. All measures are given in block-sized units.

	// Tile information within obs.charArray as used in the TAMER framework:
	
	private final static char TILE_MARIO      = 'M';
	private final static char TILE_EXIT       = '!';
	private final static char TILE_OTHER      = ' '; // Can represent either empty, floor or platform tiles.
	private final static char TILE_COIN       = '$';
	private final static char TILE_BREAKABLE  = 'b';
	private final static char TILE_SURPRISE   = '?';
	private final static char TILE_PIPE       = '|';
	
	private final static char TILE_BLOCK_UPPER = 1;
	private final static char TILE_BLOCK_LOWER = 2;
	private final static char TILE_BLOCK_ALL   = 7;
	
	// Sprite information within obs.intArray as used in the TAMER framework:
	
	private final static int SPRITE_NOT_WINGED = 0;
	private final static int SPRITE_WINGED     = 1;
	
	private final static int SPRITE_MARIO_SMALL = 0;
	private final static int SPRITE_MARIO_BIG   = 10;
	private final static int SPRITE_MARIO_FIRE  = 11;
	
	private final static int SPRITE_ENEMY_RED_KOOPA   = 1;
	private final static int SPRITE_ENEMY_GREEN_KOOPA = 2;
	private final static int SPRITE_ENEMY_GOOMBA      = 3;
	private final static int SPRITE_ENEMY_SPIKY       = 4;
	private final static int SPRITE_ENEMY_FLOWER      = 5;
	
	private final static int SPRITE_MUSHROOM   = 6;
	private final static int SPRITE_FIREFLOWER = 7;
	private final static int SPRITE_FIREBALL   = 8;
	private final static int SPRITE_SHELL      = 9;
	
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
