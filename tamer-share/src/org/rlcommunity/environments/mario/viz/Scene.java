package org.rlcommunity.environments.mario.viz;

import java.awt.Graphics;

import org.rlcommunity.environments.mario.sonar.SonarSoundEngine;
import org.rlcommunity.environments.mario.sonar.SoundListener;

import ataa2014.SimulatedHuman;


public abstract class Scene implements SoundListener
{
    public SonarSoundEngine sound;
    public static boolean[] keys = new boolean[16];
    
   //Added to use a simulated human
    protected SimulatedHuman human;
    protected boolean hasHuman;

    public void toggleKey(int key, boolean isPressed)
    {
        keys[key] = isPressed;
    }

    public final void setSound(SonarSoundEngine sound)
    {
        sound.setListener(this);
        this.sound = sound;
    }

    public abstract void init();

    public abstract void tick();

    public abstract void render(Graphics og, float alpha);
    
    public void addSimulatedHuman(SimulatedHuman h){
    	System.out.println("\nhuman added\n");
    	human = h;
    	hasHuman = true;
    }
}