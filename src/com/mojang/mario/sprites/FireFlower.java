package com.mojang.mario.sprites;

import ataa2014.SimulatedHuman;

import com.mojang.mario.Art;
import com.mojang.mario.LevelScene;


public class FireFlower extends Sprite
{
    private int width = 4;
    int height = 24;

    private LevelScene world;
    public int facing;

    public boolean avoidCliffs = false;
    private int life;

    public FireFlower(LevelScene world, int x, int y)
    {
        sheet = Art.items;

        this.x = x;
        this.y = y;
        this.world = world;
        xPicO = 8;
        yPicO = 15;

        xPic = 1;
        yPic = 0;
        height = 12;
        facing = 1;
        wPic  = hPic = 16;
        life = 0;
    }

    public SimulatedHuman.Event collideCheck()
    {
        float xMarioD = world.mario.x - x;
        float yMarioD = world.mario.y - y;
        float w = 16;
        if (xMarioD > -16 && xMarioD < 16)
        {
            if (yMarioD > -height && yMarioD < world.mario.height)
            {
                world.mario.getFlower();
                spriteContext.removeSprite(this);
                return SimulatedHuman.Event.gotPowerUp;
            }
        }
        return SimulatedHuman.Event.nothing;
    }

    public SimulatedHuman.Event move()
    {
        if (life<9)
        {
            layer = 0;
            y--;
            life++;            
        }
        return SimulatedHuman.Event.nothing;
    }
}