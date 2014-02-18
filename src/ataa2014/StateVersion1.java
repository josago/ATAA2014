package ataa2014;

import com.mojang.mario.LevelScene;
import com.mojang.mario.level.Level;

public class StateVersion1 implements State
{
	/**
	 * Creates a state representation (version 1) from the description of a Mario level.
	 * @param scene A LevelScene object representing the current state of the game.
	 */
	public StateVersion1(LevelScene scene)
	{
		SceneCustom sc = new SceneCustom(scene);
		
		System.out.println(sc);
	}
	
	@Override
	public float[] vectorRepresentation()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int vectorRepresentationLength()
	{
		return 28;
	}

}
