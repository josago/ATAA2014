package ataa2014;

import java.util.ArrayList;
import java.util.List;

import com.mojang.mario.LevelScene;
import com.mojang.mario.sprites.*;

/**
 * This class reads the information contained within the original Infinite Mario classes and creates an easier-to-read scene representation of the current LevelScene.
 * @author josago
 */
public class SceneCustom
{
	public final static int BLOCK_SIZE = 16; // Size in pixels of the side of a world block.
	
	// Block types:
	
	public final static byte BLOCK_TYPE_EMPTY     = 0;
	public final static byte BLOCK_TYPE_SOLID     = 1;
	public final static byte BLOCK_TYPE_PLATFORM  = 2;
	public final static byte BLOCK_TYPE_BREAKABLE = 3;
	public final static byte BLOCK_TYPE_SURPRISE  = 4;
	public final static byte BLOCK_TYPE_USED      = 5;
	public final static byte BLOCK_TYPE_COIN      = 6;
	public final static byte BLOCK_TYPE_PIPE      = 7;
	
	private final static String[] BLOCK_ICONS = {" ", "W", "-", "B", "?", "X", "*", "P"};
	
	// Enemy types:
	
	public final static byte ENEMY_TYPE_FLOWER             = 0;
	public final static byte ENEMY_TYPE_GOOMBA             = 1;
	public final static byte ENEMY_TYPE_GOOMBA_WINGED      = 2;
	public final static byte ENEMY_TYPE_KOOPA_GREEN        = 3;
	public final static byte ENEMY_TYPE_KOOPA_GREEN_WINGED = 4;
	public final static byte ENEMY_TYPE_KOOPA_RED          = 5;
	public final static byte ENEMY_TYPE_SPIKY              = 6;
	
	public final List<Float>[] enemies_x, enemies_y; // Coordinates of visible enemies within the level.
	
	// Item types:
	
	public final static byte ITEM_TYPE_MUSHROOM   = 0;
	public final static byte ITEM_TYPE_FIREFLOWER = 1;
	public final static byte ITEM_TYPE_SHELL      = 2;
	
	public final List<Float>[] items_x, items_y; // Coordinates of visible enemies within the level.
	
	public final int min_y, max_y, min_x, max_x; // Viewport limits of the scene.
	
	public final float mario_x, mario_y; // Mario coordinates within the level.
	
	public final byte[][] blocks; // Viewport contents.
	
	@SuppressWarnings("unchecked")
	public SceneCustom(LevelScene scene)
	{
		// Viewport limits:
		
		min_y = 0;
		max_y = scene.level.height;
		min_x = (int) Math.floor(scene.xCam / BLOCK_SIZE);
		max_x = min_x + 320 / BLOCK_SIZE;
		
		// Mario coordinates:
		
		mario_x = scene.mario.x;
		mario_y = scene.mario.y;
		
		// Coordinates of visible enemies and items:
		
		enemies_x = new ArrayList[7];
		enemies_y = new ArrayList[7];
		
		items_x = new ArrayList[3];
		items_y = new ArrayList[3];
		
		for (int e = 0; e < 7; e++)
		{
			enemies_x[e] = new ArrayList<Float>();
			enemies_y[e] = new ArrayList<Float>();
		}
		
		for (int i = 0; i < 3; i++)
		{
			items_x[i] = new ArrayList<Float>();
			items_y[i] = new ArrayList<Float>();
		}
		
		for (Sprite s: scene.sprites)
		{
			if (s instanceof FlowerEnemy)
			{
				enemies_x[ENEMY_TYPE_FLOWER].add(s.x);
				enemies_y[ENEMY_TYPE_FLOWER].add(s.y);
			}
			else if (s instanceof Enemy)
			{
				switch (((Enemy) s).type)
				{
					case Enemy.ENEMY_GOOMBA:
						if (((Enemy) s).winged)
						{
							enemies_x[ENEMY_TYPE_GOOMBA_WINGED].add(s.x);
							enemies_y[ENEMY_TYPE_GOOMBA_WINGED].add(s.y);
						}
						else
						{
							enemies_x[ENEMY_TYPE_GOOMBA].add(s.x);
							enemies_y[ENEMY_TYPE_GOOMBA].add(s.y);
						}
							
						break;
					case Enemy.ENEMY_GREEN_KOOPA:
						if (((Enemy) s).winged)
						{
							enemies_x[ENEMY_TYPE_KOOPA_GREEN_WINGED].add(s.x);
							enemies_y[ENEMY_TYPE_KOOPA_GREEN_WINGED].add(s.y);
						}
						else
						{
							enemies_x[ENEMY_TYPE_KOOPA_GREEN].add(s.x);
							enemies_y[ENEMY_TYPE_KOOPA_GREEN].add(s.y);
						}
						
						break;
					case Enemy.ENEMY_RED_KOOPA:
						enemies_x[ENEMY_TYPE_KOOPA_RED].add(s.x);
						enemies_y[ENEMY_TYPE_KOOPA_RED].add(s.y);
						
						break;
					case Enemy.ENEMY_SPIKY:
						enemies_x[ENEMY_TYPE_SPIKY].add(s.x);
						enemies_y[ENEMY_TYPE_SPIKY].add(s.y);
						
						break;
				}
			}
			else if (s instanceof Mushroom)
			{
				items_x[ITEM_TYPE_MUSHROOM].add(s.x);
				items_y[ITEM_TYPE_MUSHROOM].add(s.y);
			}
			else if (s instanceof FireFlower)
			{
				items_x[ITEM_TYPE_FIREFLOWER].add(s.x);
				items_y[ITEM_TYPE_FIREFLOWER].add(s.y);
			}
			else if (s instanceof Shell)
			{
				items_x[ITEM_TYPE_SHELL].add(s.x);
				items_y[ITEM_TYPE_SHELL].add(s.y);
			}
		}
		
		// Viewport contents:
		
		blocks = new byte[max_x - min_x + 1][max_y - min_y + 1];
		
		for (int j = min_y; j <= max_y; j++) {
			for (int i = min_x; i <= max_x; i++) {
				byte block = scene.level.getBlock(i, j);

				if ((block <= -81 && block >= -88) || (block <= -97 && block >= -104) || (block <= -109 && block >= -120) || (block <= -125 && block >= -128) || block == -65 || block == -69 || block == -199 || block == 9)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_SOLID;     // Solid geometry & pit stairs.
				}
				else if ((block <= -122 && block >= -124) || block == -76)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_PLATFORM;  // Background platforms.
				}
				else if (block >= 16 && block <= 18)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_BREAKABLE; // Breakable blocks.
				}
				else if (block == 21 || block == 22)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_SURPRISE;  // Surprise blocks.
				}
				else if (block == 4)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_USED;      // Used blocks.
				}
				else if (block == 34)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_COIN;      // Coins.
				}
				else if (block == 10 || block == 11 || block == 26 || block == 27)
				{
					blocks[i - min_x][j - min_y] = BLOCK_TYPE_PIPE;      // Pipes.
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		String output = "";
		
		for (int y = 0; y < max_y - min_y + 1; y++)
		{
			for (int x = 0; x < max_x - min_x + 1; x++)
			{
				output += BLOCK_ICONS[blocks[x][y]];
			}
			
			output += "\n";
		}

		return output;
	}
}
