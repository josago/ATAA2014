package ataa2014;

import java.util.List;

import com.mojang.mario.LevelScene;

public class StateVersion1 extends State
{
	private static final int VECTOR_REPRESENTATION_LENGTH = 30;
	
	private static final int VECTOR_DX = 0; // Horizontal distance to an entity.
	private static final int VECTOR_DY = 1; // Vertical distance to an entity.
	private static final int VECTOR_LX = 1; // Horizontal length of an entity.
	// private static final int VECTOR_LY = 0; // Vertical length of an entity.
	
	public static final int DISTANCE_FAR_AWAY = 500; // Safe, "far away" distance when an entity is not visible.
	
	public static final byte ENTITY_FLOWER             = 0;
	public static final byte ENTITY_GOOMBA             = 1;
	public static final byte ENTITY_GOOMBA_WINGED      = 2;
	public static final byte ENTITY_KOOPA_GREEN        = 3;
	public static final byte ENTITY_KOOPA_GREEN_WINGED = 4;
	public static final byte ENTITY_KOOPA_RED          = 5;
	public static final byte ENTITY_SPIKY              = 6;
	public static final byte ENTITY_COIN               = 7;
	public static final byte ENTITY_SURPRISE           = 8;
	public static final byte ENTITY_MUSHROOM           = 9;
	public static final byte ENTITY_FIREFLOWER         = 10;
	public static final byte ENTITY_SHELL              = 11;
	public static final byte ENTITY_PIT                = 12;
	public static final byte ENTITY_STEP               = 13;
	public static final byte ENTITY_PLATFORM           = 14;
	
	private final double[] v = new double[VECTOR_REPRESENTATION_LENGTH];
	
	/**
	 * Creates a state representation (version 1) from the description of a Mario level.
	 * @param scene A LevelScene object representing the current state of the game.
	 */
	public StateVersion1(LevelScene scene)
	{
		SceneCustom sc = new SceneCustom(scene);
		
		// Straight-forward entities:
		
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_FLOWER],             sc.enemies_y[SceneCustom.ENEMY_TYPE_FLOWER],             ENTITY_FLOWER);
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_GOOMBA],             sc.enemies_y[SceneCustom.ENEMY_TYPE_GOOMBA],             ENTITY_GOOMBA);
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_GOOMBA_WINGED],      sc.enemies_y[SceneCustom.ENEMY_TYPE_GOOMBA_WINGED],      ENTITY_GOOMBA_WINGED);
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_KOOPA_GREEN],        sc.enemies_y[SceneCustom.ENEMY_TYPE_KOOPA_GREEN],        ENTITY_KOOPA_GREEN);
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_KOOPA_GREEN_WINGED], sc.enemies_y[SceneCustom.ENEMY_TYPE_KOOPA_GREEN_WINGED], ENTITY_KOOPA_GREEN_WINGED);
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_KOOPA_RED],          sc.enemies_y[SceneCustom.ENEMY_TYPE_KOOPA_RED],          ENTITY_KOOPA_RED);
		addClosestEntity(sc, sc.enemies_x[SceneCustom.ENEMY_TYPE_SPIKY],              sc.enemies_y[SceneCustom.ENEMY_TYPE_SPIKY],              ENTITY_SPIKY);
		
		addClosestEntity(sc, sc.items_x[SceneCustom.ITEM_TYPE_MUSHROOM],   sc.items_y[SceneCustom.ITEM_TYPE_MUSHROOM],   ENTITY_MUSHROOM);
		addClosestEntity(sc, sc.items_x[SceneCustom.ITEM_TYPE_FIREFLOWER], sc.items_y[SceneCustom.ITEM_TYPE_FIREFLOWER], ENTITY_FIREFLOWER);
		addClosestEntity(sc, sc.items_x[SceneCustom.ITEM_TYPE_SHELL],      sc.items_y[SceneCustom.ITEM_TYPE_SHELL],      ENTITY_SHELL);
		
		// Complicated entities:
		
		// Closest pit from Mario:
		
		int pit_l = sc.min_x;
		int pit_r = sc.min_x;
		
		for (int x = 0; x < sc.max_x - sc.min_x + 1; x++)
		{
			byte block = sc.blocks[x][sc.max_y - sc.min_y];
			
			if (block == SceneCustom.BLOCK_TYPE_SOLID && pit_l == pit_r)
			{
				pit_l++;
				pit_r++;
			}
			else if (block == SceneCustom.BLOCK_TYPE_EMPTY)
			{
				pit_r++;
			}
		}
		
		if (pit_l < pit_r)
		{
			if (sc.mario_x <= pit_l * SceneCustom.BLOCK_SIZE)
			{
				v[2 * ENTITY_PIT + VECTOR_DX] = pit_l * SceneCustom.BLOCK_SIZE - sc.mario_x;
			}
			else if (sc.mario_x >= pit_r * SceneCustom.BLOCK_SIZE)
			{
				v[2 * ENTITY_PIT + VECTOR_DX] = pit_r * SceneCustom.BLOCK_SIZE - sc.mario_x;
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
		
		v[2 * ENTITY_PIT + VECTOR_LX] = (pit_r - pit_l) * SceneCustom.BLOCK_SIZE;
		
		// Closest step from Mario:
		
		int pointer_x = (int) (Math.floor(sc.mario_x) / SceneCustom.BLOCK_SIZE);
		int pointer_y = Math.min((int) (Math.floor(sc.mario_y) / SceneCustom.BLOCK_SIZE), sc.max_y);
		
		// Pointer to ground level just below Mario:
		
		while (pointer_y < sc.max_y && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y] != SceneCustom.BLOCK_TYPE_SOLID) // BUG: If Mario is above the screen level!
		{
			pointer_y++;
		}
		
		// Closest step from Mario:
		
		float[] step_dist = closestStep(sc);

		v[2 * ENTITY_STEP + VECTOR_DX] = step_dist[VECTOR_DX];
		v[2 * ENTITY_STEP + VECTOR_DY] = step_dist[VECTOR_DY];
		
		// Closest platfrom from Mario:
		
		float[] platform_dist = closestBlock(sc, SceneCustom.BLOCK_TYPE_PLATFORM);
		
		if (Math.abs(platform_dist[VECTOR_DX]) < SceneCustom.BLOCK_SIZE)
		{
			v[2 * ENTITY_PLATFORM + VECTOR_DX] = 0;
		}
		else
		{
			v[2 * ENTITY_PLATFORM + VECTOR_DX] = platform_dist[VECTOR_DX];
		}
		
		v[2 * ENTITY_PLATFORM + VECTOR_DY] = platform_dist[VECTOR_DY];
		
		// Closest coin from Mario:
		
		float[] coin_dist = closestBlock(sc, SceneCustom.BLOCK_TYPE_COIN);
		
		v[2 * ENTITY_COIN + VECTOR_DX] = coin_dist[VECTOR_DX];
		v[2 * ENTITY_COIN + VECTOR_DY] = coin_dist[VECTOR_DY];
		
		// Closest surprise block from Mario:
		
		float[] surprise_dist = closestBlock(sc, SceneCustom.BLOCK_TYPE_SURPRISE);
		
		v[2 * ENTITY_SURPRISE + VECTOR_DX] = surprise_dist[VECTOR_DX];
		v[2 * ENTITY_SURPRISE + VECTOR_DY] = surprise_dist[VECTOR_DY];
		
		//System.out.println(this); // Temporal, just for checking whether the state representation really works.
	}
	
	private void addClosestEntity(SceneCustom sc, List<Float> pos_x, List<Float> pos_y, int pos_v)
	{
		int i_min = -1;
		
		for (int i = 0; i < pos_x.size(); i++)
		{
			float entity_x = pos_x.get(i);
			float entity_y = pos_y.get(i);
			
			if (i_min == -1 || Math.sqrt(Math.pow(v[2 * pos_v + VECTOR_DX], 2) + Math.pow(v[2 * pos_v + VECTOR_DY], 2)) > Math.sqrt(Math.pow(entity_x - sc.mario_x, 2) + Math.pow(entity_y - sc.mario_y, 2)))
			{
				i_min = i;
				
				v[2 * pos_v + VECTOR_DX] = entity_x - sc.mario_x;
				v[2 * pos_v + VECTOR_DY] = entity_y - sc.mario_y;
			}
		}
		
		if (i_min == -1)
		{
			v[2 * pos_v + VECTOR_DX] = DISTANCE_FAR_AWAY;
			v[2 * pos_v + VECTOR_DY] = 0;
		}
	}
	
	private float[] closestStep(SceneCustom sc)
	{
		float[] step_dist = new float[]{DISTANCE_FAR_AWAY, DISTANCE_FAR_AWAY};
		
		for (int x = sc.min_x; x < sc.max_x; x++)
		{
			for (int y = sc.min_y; y < sc.max_y; y++)
			{
				int step_x = - DISTANCE_FAR_AWAY;
				int step_y = - DISTANCE_FAR_AWAY;
				
				// Step template matching:
				
				// Outer corners:
				
				if (sc.blocks[x - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_EMPTY && sc.blocks[x + 1 - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_EMPTY)
				{
					if ((sc.blocks[x - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_SOLID || sc.blocks[x - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_PIPE) && sc.blocks[x + 1 - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_EMPTY)
					{
						step_x = x;
						step_y = y + 1;
					}
					else if (sc.blocks[x - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_EMPTY && (sc.blocks[x + 1 - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_SOLID || sc.blocks[x + 1 - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_PIPE))
					{
						step_x = x + 1;
						step_y = y + 1;
					}
				}
				
				// Inner corners:
				
				if ((sc.blocks[x - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_SOLID || sc.blocks[x - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_PIPE) && (sc.blocks[x + 1 - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_SOLID || sc.blocks[x + 1 - sc.min_x][y + 1 - sc.min_y] == SceneCustom.BLOCK_TYPE_PIPE))
				{
					if ((sc.blocks[x - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_SOLID || sc.blocks[x - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_PIPE) && sc.blocks[x + 1 - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_EMPTY)
					{
						step_x = x;
						step_y = y + 1;
					}
					else if (sc.blocks[x - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_EMPTY && (sc.blocks[x + 1 - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_SOLID || sc.blocks[x + 1 - sc.min_x][y - sc.min_y] == SceneCustom.BLOCK_TYPE_PIPE))
					{
						step_x = x + 1;
						step_y = y + 1;
					}
				}
				
				// Distance check:
				
				if (step_y * SceneCustom.BLOCK_SIZE - sc.mario_y != 1 && Math.sqrt(Math.pow(step_x * SceneCustom.BLOCK_SIZE - sc.mario_x, 2) + Math.pow(step_y * SceneCustom.BLOCK_SIZE - sc.mario_y, 2)) < Math.sqrt(Math.pow(step_dist[VECTOR_DX], 2) + Math.pow(step_dist[VECTOR_DY], 2)))
				{
					step_dist[VECTOR_DX] = step_x * SceneCustom.BLOCK_SIZE - sc.mario_x;
					step_dist[VECTOR_DY] = step_y * SceneCustom.BLOCK_SIZE - sc.mario_y - 1; // Small distance fix.
				}
			}
		}
		
		return step_dist;
	}
	
	private float[] closestBlock(SceneCustom sc, byte type_block)
	{
		float[] block_dist = new float[]{DISTANCE_FAR_AWAY, DISTANCE_FAR_AWAY};
		
		for (int x = sc.min_x; x <= sc.max_x; x++)
		{
			for (int y = sc.min_y; y <= sc.max_y; y++)
			{
				if (sc.blocks[x - sc.min_x][y - sc.min_y] == type_block)
				{
					if (Math.sqrt(Math.pow(x * SceneCustom.BLOCK_SIZE - sc.mario_x, 2) + Math.pow(y * SceneCustom.BLOCK_SIZE - sc.mario_y, 2)) < Math.sqrt(Math.pow(block_dist[VECTOR_DX], 2) + Math.pow(block_dist[VECTOR_DY], 2)))
					{
						block_dist[VECTOR_DX] = x * SceneCustom.BLOCK_SIZE - sc.mario_x;
						block_dist[VECTOR_DY] = y * SceneCustom.BLOCK_SIZE - sc.mario_y;
					}
				}
			}
		}
		
		return block_dist;
	}
	
	@Override
	public String toString()
	{
		String output = "(";
		
		for (int i = 0; i < VECTOR_REPRESENTATION_LENGTH; i++)
		{
			output += v[i];
			
			if (i < VECTOR_REPRESENTATION_LENGTH - 1)
			{
				output += ", ";
			}
			else
			{
				output += ")";
			}
		}
		
		return output;
	}
	
	public double[] vectorRepresentation()
	{
		return v;
	}

	public static int vectorRepresentationLength()
	{
		return VECTOR_REPRESENTATION_LENGTH;
	}

}
