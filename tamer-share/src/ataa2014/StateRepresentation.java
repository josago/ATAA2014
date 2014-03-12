package ataa2014;

import java.util.Arrays;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * Our custom state representation for the Mario environment, adapted to the TAMER framework.
 * @author josago
 */
public class StateRepresentation extends FeatGenerator
{
	public final static int FEATURES_ACTION = 3;  // Number of features generated from the Action objects.
	public final static int FEATURES_STATE  = 36; // Number of features generated from the Observation objects.
	
	public final static int VIEW_WIDTH  = 21; // Expressed in game blocks.
	public final static int VIEW_HEIGHT = 15; // Expressed in game blocks.
	
	public final static double DISTANCE_FAR_AWAY = Math.max(VIEW_WIDTH, VIEW_HEIGHT) * 1.5; // Distance used when an entity is not within the current view. Expressed in game blocks.
	
	// Indices of the different Mario environment entities within the arrays returned by this class (multiply them by 2 and add a VECTOR_* constant):
	
	public static final int ENTITY_FLOWER             = 0;
	public static final int ENTITY_GOOMBA             = 1;
	public static final int ENTITY_GOOMBA_WINGED      = 2;
	public static final int ENTITY_KOOPA_GREEN        = 3;
	public static final int ENTITY_KOOPA_GREEN_WINGED = 4;
	public static final int ENTITY_KOOPA_RED          = 5;
	public static final int ENTITY_SPIKY              = 6;
	public static final int ENTITY_COIN               = 7;
	public static final int ENTITY_SURPRISE           = 8;
	public static final int ENTITY_BREAKABLE          = 9;
	public static final int ENTITY_EXIT               = 10;
	public static final int ENTITY_MUSHROOM           = 11;
	public static final int ENTITY_FIREFLOWER         = 12;
	public static final int ENTITY_SHELL              = 13;
	public static final int ENTITY_PIT                = 14;
	public static final int ENTITY_STEP               = 15;
	public static final int ENTITY_PLATFORM           = 16;
	
	public static final int STATE_LARGE = 34; // Use this index directly.
	public static final int STATE_FIRE  = 35; // Use this index directly.
	
	public static final int VECTOR_DX = 0; // Horizontal distance to an entity.
	public static final int VECTOR_DY = 1; // Vertical distance to an entity.
	public static final int VECTOR_LX = 1; // Horizontal length of an entity.

	// Important note about the format of the Observation objects for the Mario environment:
	//     obs.charArray    -> Contains the matrix-like tile representation of the current view (including Mario itself, rounded to the nearest integer). Each tile is represented by 2 chars: code and representation.
	//     obs.intArray     -> Array of integers having the format {sprite_1_type, sprite_1_winged?, sprite_2_type, sprite_2_winged?, ...}
	//     obs.doubleArray  -> Array of doubles having the format {sprite_1_X_position, sprite_1_Y_position, sprite_1_X_speed, sprite_1_Y_speed, sprite_2_X_position, ...}. All measures are given in block-sized units.
	
	private final static int VECTOR_TYPE   = 0;
	private final static int VECTOR_WINGED = 1;
	private final static int VECTOR_X      = 0;
	private final static int VECTOR_Y      = 1;
	// private final static int VECTOR_SX     = 2;
	// private final static int VECTOR_SY     = 3;
	
	// Tile information within obs.charArray as used in the TAMER framework:
	
	private final static char TILE_MARIO      = 'M';
	private final static char TILE_EXIT       = '!';
	private final static char TILE_EMPTY      = ' ';
	private final static char TILE_COIN       = '$';
	private final static char TILE_BREAKABLE  = 'b';
	private final static char TILE_SURPRISE   = '?';
	private final static char TILE_PIPE       = '|';
	
	private final static char TILE_BLOCK_UPPER    = (int) 49;
	private final static char TILE_BLOCK_LOWER    = 2; // TODO: Possibly this does not work, but it is not used.
	private final static char TILE_BLOCK_ALL      = (int) 55;
	private final static char TILE_BLOCK_WHATEVER = (int) 0; // If case we don't care about the blocking properties of the tile.
	
	// Sprite information within obs.intArray as used in the TAMER framework:
	
	private final static int INT_XCAM = 0;
	
	// private final static int SPRITE_NOT_WINGED = 0;
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
	// private final static int SPRITE_FIREBALL   = 8; // We don't encode fire balls in our state representation.
	private final static int SPRITE_SHELL      = 9;
	
	private int      xCam;            // Horizontal position of the camera within the level, used to calculate relative distances between entities.
	private double[] lastMarioCoords; // Last known coordinates of Mario within the level.
	
	public StateRepresentation(int[][] theObsIntRanges, double[][] theObsDoubleRanges, int[][] theActIntRanges, double[][] theActDoubleRanges)
	{
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		
		// Note from josago: It seems we don't need to do anything else in here.
	}
	
	@Override
	public int getNumFeatures()
	{
		return FEATURES_ACTION + FEATURES_STATE;
	}

	/**
	 * This method returns a list of indices that indicate where the action features lie in the vectors returned by the other methods of this class.
	 */
	@Override
	public int[] getActionFeatIndices()
	{
		return new int[]{0, 1, 2};
	}

	/**
	 * This method returns a list of integers indicating how many different values each of the indices of the feature array can take.
	 * In TAMER this method seems to be implemented only for action features; it returns zero for the rest of indices.
	 */
	@Override
	public int[] getNumFeatValsPerFeatI()
	{
		int[] output = new int[FEATURES_ACTION + FEATURES_STATE];
		
		output[0] = 3;
		output[1] = 2;
		output[2] = 2;
		
		return output;
	}

	/**
	 * This method returns the feature vector that represents both the observation and the action given as parameters.
	 */
	@Override
	public double[] getSAFeats(Observation obs, Action act)
	{
		double[] AFeats = getAFeats(act);
		double[] SFeats = getSFeats(obs);
		
		double[] SAFeats = Arrays.copyOf(AFeats, AFeats.length + SFeats.length);
		
		for (int i = AFeats.length; i < SAFeats.length; i++)
		{
			SAFeats[i] = SFeats[i - AFeats.length];
		}
		
		return SAFeats;
	}

	/**
	 * This method returns the feature vector that represents the observation given as parameter.
	 */
	@Override
	public double[] getSFeats(Observation obs)
	{
		double[] v = new double[FEATURES_STATE];
		
		double[] coords_mario = coordsMario(v, obs.intArray, obs.doubleArray);
		
		if (coords_mario == null)
		{
			coords_mario = lastMarioCoords;
		}
		else
		{
			lastMarioCoords = coords_mario;
		}
		
		addClosestSprites(v,                obs.intArray, obs.doubleArray, coords_mario);
		addClosestPit(    v, obs.charArray,                                coords_mario);
		addClosestStep(   v, obs.charArray,                                coords_mario);
		
		addClosestBlock(v, ENTITY_EXIT,      obs.charArray, coords_mario, TILE_EXIT);
		addClosestBlock(v, ENTITY_COIN,      obs.charArray, coords_mario, TILE_COIN);
		addClosestBlock(v, ENTITY_BREAKABLE, obs.charArray, coords_mario, TILE_BREAKABLE);
		addClosestBlock(v, ENTITY_SURPRISE,  obs.charArray, coords_mario, TILE_SURPRISE);
		addClosestBlock(v, ENTITY_PLATFORM,  obs.charArray, coords_mario, TILE_BLOCK_UPPER);
		
		return v;
	}
	
	private double[] getAFeats(Action act)
	{
		double[] v = new double[FEATURES_ACTION];

		for (int i = 0; i < act.intArray.length; i++)
		{
			v[i] = act.intArray[i];
		}
		
		return v;
	}
	
	// The following overriden methods are not implemented in the original TAMER class FeatGen_Mario, so we won't be implementing them here either:

	@Override
	public double[] getMaxPossFeats()
	{
		return new double[0];
	}

	@Override
	public double[] getMinPossFeats()
	{
		return new double[0];
	}

	@Override
	public double[] getMaxPossSFeats()
	{
		return new double[0];
	}

	@Override
	public double[] getMinPossSFeats()
	{
		return new double[0];
	}
	
	// Useful support methods:
	
	/**
	 * This method returns the absolute coordinates of Mario within the current level and updates the feature vector with Mario's state.
	 * @param v A reference to the final feature vector to populate.
	 * @param intArray As contained within an Observation object.
	 * @param doubleArray As contained within an Observation object.
	 * @return An array having the format {coord_x, coord_y}.
	 */
	private double[] coordsMario(double[] v, int[] intArray, double[] doubleArray)
	{
		xCam = intArray[INT_XCAM]; // Horizontal position of the camera within the level, used to calculate relative distances between entities.
		
		if (intArray[1 + VECTOR_TYPE] == SPRITE_MARIO_SMALL || intArray[1 + VECTOR_TYPE] == SPRITE_MARIO_BIG || intArray[1 + VECTOR_TYPE] == SPRITE_MARIO_FIRE)
		{
			v[STATE_LARGE] = (intArray[1 + VECTOR_TYPE] == SPRITE_MARIO_BIG || intArray[1 + VECTOR_TYPE] == SPRITE_MARIO_FIRE) ? 1 : 0;
			v[STATE_FIRE]  =  intArray[1 + VECTOR_TYPE] == SPRITE_MARIO_FIRE ? 1 : 0;
			
			return new double[]{doubleArray[VECTOR_X], doubleArray[VECTOR_Y]};
		}
		
		return null; // Note from josago: In principle, this point should never be reached.
	}
	
	/**
	 * This method updates the distance from Mario to the closest block conforming to the given tile specification.
	 * @param v A reference to the final feature vector to populate.
	 * @param pos Position within v to update.
	 * @param charArray As contained within an Observation object.
	 * @param coords_mario As returned by the coordsMario() method.
	 * @param tile A TILE_* constant.
	 */
    private void addClosestBlock(double[] v, int pos, char[] charArray, double[] coords_mario, char tile)
    {
		for (int y = 0; y < VIEW_HEIGHT; y++)
		{
	    	for (int x = 0; x < VIEW_WIDTH; x++)
	    	{
    			int i = y * (VIEW_WIDTH + 1) + x; // + 1 in order to skip the '\n' chars.
                
    			if (charArray[i] == tile || (tile == TILE_BLOCK_WHATEVER && (charArray[i] == TILE_BLOCK_UPPER || charArray[i] == TILE_BLOCK_LOWER || charArray[i] == TILE_BLOCK_ALL)))
    			{
    				double dist_x = x - (coords_mario[VECTOR_X] - xCam);
    				double dist_y = (VIEW_HEIGHT - 1 - y) - coords_mario[VECTOR_Y];

    				if (Math.sqrt(Math.pow(dist_x, 2) + Math.pow(dist_y, 2)) < Math.sqrt(Math.pow(v[2 * pos + VECTOR_DX], 2) + Math.pow(v[2 * pos + VECTOR_DY], 2)))
                    {
    					v[2 * pos + VECTOR_DX] = dist_x;
    					v[2 * pos + VECTOR_DY] = dist_y;
                    }
                }
            }
        }
    }
    
    /**
     * This method updates the distance from Mario to the closest pit (hole you can fall in and die).
  	 * @param v A reference to the final feature vector to populate.
	 * @param charArray As contained within an Observation object.
	 * @param coords_mario As returned by the coordsMario() method.
     */
    private void addClosestPit(double[] v, char[] charArray, double[] coords_mario)
    {
		int pit_l = 0;
		int pit_r = 0;
		
		for (int x = 0; x < VIEW_WIDTH; x++)
		{
			char block = charArray[(VIEW_HEIGHT - 1) * (VIEW_WIDTH + 1) + x]; // + 1 in order to skip the '\n' chars.
			
			// TODO: This probably doesn't work with TAMER Observation objects.
			
			if (block == TILE_BLOCK_ALL && pit_l == pit_r)
			{
				pit_l++;
				pit_r++;
			}
			else if (block == TILE_EMPTY)
			{
				pit_r++;
			}
		}
		
		if (pit_l < pit_r)
		{
			if (coords_mario[VECTOR_X] <= pit_l)
			{
				v[2 * ENTITY_PIT + VECTOR_DX] = pit_l - coords_mario[VECTOR_X];
			}
			else if (coords_mario[VECTOR_X] >= pit_r)
			{
				v[2 * ENTITY_PIT + VECTOR_DX] = pit_r - coords_mario[VECTOR_X];
			}
			else
			{
				v[2 * ENTITY_PIT + VECTOR_DX] = 0;
			}
		}
		else
		{
			v[2 * ENTITY_PIT + VECTOR_DX] = DISTANCE_FAR_AWAY;
		}
		
		v[2 * ENTITY_PIT + VECTOR_LX] = pit_r - pit_l;
    }
    
    /**
     * This method updates the distance from Mario to the closest step. We consider a step to be a change in the height level of the map that can only be overcome by jumping.
     * @param v A reference to the final feature vector to populate.
	 * @param charArray As contained within an Observation object.
	 * @param coords_mario As returned by the coordsMario() method.
     */
	private void addClosestStep(double[] v, char[] charArray, double[] coords_mario)
	{
		for (int y = 0; y < VIEW_HEIGHT - 1; y++)
		{
	    	for (int x = 0; x < VIEW_WIDTH - 1; x++)
	    	{
	    		// Tile index naming convention:
	    		// A B
	    		// C D
	    		// Where A = (x, y)
	    		
    			int a =  y      * (VIEW_WIDTH + 1) + x;     // + 1 in order to skip the '\n' chars.
    			int b =  y      * (VIEW_WIDTH + 1) + x + 1;
    			int c = (y + 1) * (VIEW_WIDTH + 1) + x;
    			int d = (y + 1) * (VIEW_WIDTH + 1) + x + 1;
    			
				int step_x = - 1;
				int step_y = - 1;
				
				// Step template matching:
				
				// Outer corners:
				// 
				// X
				// or
				// 
				//  X
				
				if ((charArray[a] == TILE_EMPTY || charArray[a] == TILE_MARIO) && (charArray[b] == TILE_EMPTY || charArray[b] == TILE_MARIO))
				{
					if ((charArray[c] == TILE_BLOCK_ALL || charArray[c] == TILE_PIPE) && (charArray[d] == TILE_EMPTY || charArray[d] == TILE_MARIO))
					{
						step_x = x;
						step_y = y + 1;
					}
					else if ((charArray[c] == TILE_EMPTY || charArray[c] == TILE_MARIO) && (charArray[d] == TILE_BLOCK_ALL || charArray[d] == TILE_PIPE))
					{
						step_x = x + 1;
						step_y = y + 1;
					}
				}
				
				// Inner corners:
				// X
				//  X
				// or
				//  X
				// X 
				
				else if ((charArray[a] == TILE_BLOCK_ALL || charArray[a] == TILE_PIPE) && (charArray[b] == TILE_EMPTY || charArray[b] == TILE_MARIO) && (charArray[c] == TILE_EMPTY || charArray[c] == TILE_MARIO) && (charArray[d] == TILE_BLOCK_ALL || charArray[d] == TILE_PIPE))
				{
					step_x = x;
					step_y = y + 1;
				}
				else if ((charArray[a] == TILE_EMPTY || charArray[a] == TILE_MARIO) && (charArray[b] == TILE_BLOCK_ALL || charArray[b] == TILE_PIPE) && (charArray[c] == TILE_BLOCK_ALL || charArray[c] == TILE_PIPE) && (charArray[d] == TILE_EMPTY || charArray[d] == TILE_MARIO))
				{
					step_x = x + 1;
					step_y = y + 1;
				}
				
				if (step_x >= 0)
				{
					// Distance check (TODO: not sure if the first check within the "if" is correct):
					
					double dist_x = step_x - (coords_mario[VECTOR_X] - xCam);
					double dist_y = (VIEW_HEIGHT - 1 - step_y) - coords_mario[VECTOR_Y];
					
					if (Math.abs(dist_y) >= 1.07 && Math.sqrt(Math.pow(dist_x, 2) + Math.pow(dist_y, 2)) < Math.sqrt(Math.pow(v[2 * ENTITY_STEP + VECTOR_DX], 2) + Math.pow(v[2 * ENTITY_STEP + VECTOR_DY], 2)))
					{
						v[2 * ENTITY_STEP + VECTOR_DX] = dist_x;
						v[2 * ENTITY_STEP + VECTOR_DY] = dist_y; 
					}
				}
			}
		}
		//System.out.println("Step: " + v[2 * ENTITY_STEP + VECTOR_DX] + ", " + v[2 * ENTITY_STEP + VECTOR_DY]);
	}
	
	/**
	 * This method searches the closest sprites of each kind from Mario and adds these pieces of information to their corresponding place in a feature vector.
	 * @param v A reference to the final feature vector to populate.
	 * @param intArray As contained within an Observation object.
	 * @param doubleArray As contained within an Observation object.
	 * @param coords_mario As returned by the coordsMario() method.
	 */
	private void addClosestSprites(double[] v, int[] intArray, double[] doubleArray, double[] coords_mario)
	{
		// All entities will be placed far away by default:
		
		for (int i = 0; i < v.length / 2 - 1; i++) // - 1 to avoid overwriting Mario's state.
		{
			v[2 * i + VECTOR_DX] = DISTANCE_FAR_AWAY;
			v[2 * i + VECTOR_DY] = 0;
		}
		
		// Sprite distance update:
		
		for (int i = 0; i < (intArray.length - 3) / 2; i++)
		{
			int pos = -1;
			
			switch (intArray[3 + 2 * i + VECTOR_TYPE])
			{
				case SPRITE_ENEMY_RED_KOOPA:
					pos = ENTITY_KOOPA_RED;
				break;
				case SPRITE_ENEMY_GREEN_KOOPA:
					pos = intArray[3 + 2 * i + VECTOR_WINGED] == SPRITE_WINGED ? ENTITY_KOOPA_GREEN_WINGED : ENTITY_KOOPA_GREEN;
				break;
				case SPRITE_ENEMY_GOOMBA:
					pos = intArray[3 + 2 * i + VECTOR_WINGED] == SPRITE_WINGED ? ENTITY_GOOMBA_WINGED : ENTITY_GOOMBA;
				break;
				case SPRITE_ENEMY_SPIKY:
					pos = ENTITY_SPIKY;
				break;
				case SPRITE_ENEMY_FLOWER:
					pos = ENTITY_FLOWER;
				break;
				case SPRITE_MUSHROOM:
					pos = ENTITY_MUSHROOM;
				break;
				case SPRITE_FIREFLOWER:
					pos = ENTITY_FIREFLOWER;
				break;
				case SPRITE_SHELL:
					pos = ENTITY_SHELL;
				break;
			}
			
			if (pos != -1)
			{
				double dist_x = doubleArray[4 * (i + 1) + VECTOR_X] - coords_mario[VECTOR_X];
				double dist_y = doubleArray[4 * (i + 1) + VECTOR_Y] - coords_mario[VECTOR_Y];
				
				if (Math.sqrt(Math.pow(dist_x, 2) + Math.pow(dist_y, 2)) < Math.sqrt(Math.pow(v[2 * pos + VECTOR_DX], 2) + Math.pow(v[2 * pos + VECTOR_DY], 2)))
				{
					v[2 * pos + VECTOR_DX] = dist_x;
					v[2 * pos + VECTOR_DY] = dist_y;
				}
			}
		}
	}
}
