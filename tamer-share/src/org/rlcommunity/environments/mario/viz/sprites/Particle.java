package org.rlcommunity.environments.mario.viz.sprites;

import org.rlcommunity.environments.mario.viz.Art;

import ataa2014.SimulatedHuman;

public class Particle extends Sprite
{
    public int life;
    
    public Particle(int x, int y, float xa, float ya)
    {
        this(x, y, xa, ya, (int)(Math.random()*2), 0);
    }

    public Particle(int x, int y, float xa, float ya, int xPic, int yPic)
    {
        sheet = Art.particles;
        this.x = x;
        this.y = y;
        this.xa = xa;
        this.ya = ya;
        this.xPic = xPic;
        this.yPic = yPic;
        this.xPicO = 4;
        this.yPicO = 4;
        
        wPic = 8;
        hPic = 8;
        life = 10;
    }

    public SimulatedHuman.Event move()
    {
        if (life--<0) Sprite.spriteContext.removeSprite(this);
        x+=xa;
        y+=ya;
        ya*=0.95f;
        ya+=3;
        
        return SimulatedHuman.Event.nothing;
    }
}