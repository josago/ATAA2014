package ataa2014;

import java.util.List;

import com.mojang.mario.LevelScene;

public class StateVersion1 implements State
{
	private static final int VECTOR_REPRESENTATION_LENGTH = 20;
	
	private static final int VECTOR_DX = 0;
	private static final int VECTOR_DY = 1;
	
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
	
	private final float[] v = new float[VECTOR_REPRESENTATION_LENGTH];
	
	/**
	 * Creates a state representation (version 1) from the description of a Mario level.
	 * @param scene A LevelScene object representing the current state of the game.
	 */
	public StateVersion1(LevelScene scene)
	{
		SceneCustom sc = new SceneCustom(scene);
		
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
		
		System.out.println(this);
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
			v[2 * pos_v + VECTOR_DX] = Float.POSITIVE_INFINITY;
			v[2 * pos_v + VECTOR_DY] = Float.POSITIVE_INFINITY;
		}
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
	
	@Override
	public float[] vectorRepresentation()
	{
		return v;
	}

	@Override
	public int vectorRepresentationLength()
	{
		return VECTOR_REPRESENTATION_LENGTH;
	}

}
