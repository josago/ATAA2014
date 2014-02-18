package ataa2014;

import com.mojang.mario.LevelScene;

public class StateVersion1 implements State
{
	/**
	 * Creates a state representation (version 1) from the description of a Mario level.
	 * @param l
	 */
	public StateVersion1(LevelScene scene)
	{
		System.out.println(scene.mario.x + ", " + scene.mario.y);
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
