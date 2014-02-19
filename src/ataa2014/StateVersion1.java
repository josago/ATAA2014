package ataa2014;

import java.util.List;

import com.mojang.mario.LevelScene;

public class StateVersion1 extends State
{
	private static final int VECTOR_REPRESENTATION_LENGTH = 24;
	
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
	public static final byte ENTITY_MUSHROOM           = 7;
	public static final byte ENTITY_FIREFLOWER         = 8;
	public static final byte ENTITY_SHELL              = 9;
	public static final byte ENTITY_PIT                = 10;
	public static final byte ENTITY_STEP               = 11;
	
	private final float[] v = new float[VECTOR_REPRESENTATION_LENGTH];
	
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
		
		while (pointer_y < sc.max_y && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y] != SceneCustom.BLOCK_TYPE_SOLID)
		{
			pointer_y++;
		}
		
		// Search for a step both to the left and to the right:
		
		int[] step_r = searchStep(sc, +1, pointer_x, pointer_y); // Right.
		int[] step_l = searchStep(sc, -1, pointer_x, pointer_y); // Left.
		
		float dist_rx = step_r[VECTOR_DX] * SceneCustom.BLOCK_SIZE - sc.mario_x;
		float dist_lx = step_l[VECTOR_DX] * SceneCustom.BLOCK_SIZE - sc.mario_x;
		float dist_ry = step_r[VECTOR_DY] * SceneCustom.BLOCK_SIZE - sc.mario_y;
		float dist_ly = step_l[VECTOR_DY] * SceneCustom.BLOCK_SIZE - sc.mario_y;
		
		if (Math.sqrt(Math.pow(dist_lx, 2) + Math.pow(dist_ly, 2)) <= Math.sqrt(Math.pow(dist_rx, 2) + Math.pow(dist_ry, 2)))
		{
			if (step_l[VECTOR_DX] == -1)
			{
				v[2 * ENTITY_STEP + VECTOR_DX] = DISTANCE_FAR_AWAY;
				v[2 * ENTITY_STEP + VECTOR_DY] = 0;
			}
			else
			{
				v[2 * ENTITY_STEP + VECTOR_DX] = dist_lx;
				v[2 * ENTITY_STEP + VECTOR_DY] = dist_ly;
			}
		}
		else
		{
			v[2 * ENTITY_STEP + VECTOR_DX] = dist_rx;
			v[2 * ENTITY_STEP + VECTOR_DY] = dist_ry;
		}
		
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
	
	private int[] searchStep(SceneCustom sc, int diff_x, int pointer_x, int pointer_y) // BUG: Not working fine in all situations.
	{
		int diff_y;
		
		// Move the pointer horizontally until a step or the end of the scene is reached:
		
		while (pointer_x > sc.min_x && pointer_x < sc.max_x && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y] == sc.blocks[pointer_x - sc.min_x + diff_x][pointer_y - sc.min_y] && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y - 1] == sc.blocks[pointer_x - sc.min_x + diff_x][pointer_y - sc.min_y - 1])
		{
			pointer_x += diff_x;
		}
		
		if (pointer_x > sc.min_x && pointer_x < sc.max_x && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y] == sc.blocks[pointer_x - sc.min_x + diff_x][pointer_y - sc.min_y])
		{
			// Up-step:
			
			diff_y = -1;
			
			pointer_y--;
		}
		else if (pointer_x > sc.min_x && pointer_x < sc.max_x && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y - 1] == sc.blocks[pointer_x - sc.min_x + diff_x][pointer_y - sc.min_y - 1])
		{
			// Down-step:
			
			diff_y = +1;
		}
		else
		{
			// No step:
			
			return new int[]{-1, -1};
		}
		
		// Move the pointer vertically until the end of the step is reached:
		
		while (pointer_y > sc.min_y && pointer_y < sc.max_y && sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y] == sc.blocks[pointer_x - sc.min_x][pointer_y - sc.min_y + diff_y] && sc.blocks[pointer_x - sc.min_x + 1][pointer_y - sc.min_y] == sc.blocks[pointer_x - sc.min_x + 1][pointer_y - sc.min_y + diff_y])
		{
			pointer_y += diff_y;
		}
		
		// Final fixes to the pointer towards calculating a correct distance value afterwards:
		
		if (diff_y == +1 && diff_x == +1)
		{
			pointer_y++;
		}
		else if (diff_y == +1 && diff_x == -1)
		{
			pointer_y--;
		}
			
		if (diff_x == +1)
		{
			pointer_x++;
		}
		
		// Return the coordinates of the edge of the step:
		
		return new int[]{pointer_x, pointer_y};
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
	
	public float[] vectorRepresentation()
	{
		return v;
	}

	public static int vectorRepresentationLength()
	{
		return VECTOR_REPRESENTATION_LENGTH;
	}

}
