package com.mojang.mario.sprites;

import java.awt.Graphics;
import java.awt.Image;

import ataa2014.SimulatedHuman;

import com.mojang.mario.level.SpriteTemplate;
import com.mojang.sonar.SoundSource;

public class Sprite implements SoundSource
{
    public static SpriteContext spriteContext;
    
    public float xOld, yOld, x, y, xa, ya;
    
    public int xPic, yPic;
    public int wPic = 32;
    public int hPic = 32;
    public int xPicO, yPicO;
    public boolean xFlipPic = false;
    public boolean yFlipPic = false;
    public Image[][] sheet;
    public boolean visible = true;
    
    public int layer = 1;

    public SpriteTemplate spriteTemplate;
    
    public SimulatedHuman.Event move()
    {
        x+=xa;
        y+=ya;
        return SimulatedHuman.Event.nothing;
    }
    
    public void render(Graphics og, float alpha)
    {
        if (!visible) return;
        
        int xPixel = (int)(xOld+(x-xOld)*alpha)-xPicO;
        int yPixel = (int)(yOld+(y-yOld)*alpha)-yPicO;

        og.drawImage(sheet[xPic][yPic], xPixel+(xFlipPic?wPic:0), yPixel+(yFlipPic?hPic:0), xFlipPic?-wPic:wPic, yFlipPic?-hPic:hPic, null);
    }
    
/*  private void blit(Graphics og, Image bitmap, int x0, int y0, int x1, int y1, int w, int h)
    {
        if (!xFlipPic)
        {
            if (!yFlipPic)
            {
                og.drawImage(bitmap, x0, y0, x0+w, y0+h, x1, y1, x1+w, y1+h, null);
            }
            else
            {
                og.drawImage(bitmap, x0, y0, x0+w, y0+h, x1, y1+h, x1+w, y1, null);
            }
        }
        else
        {
            if (!yFlipPic)
            {
                og.drawImage(bitmap, x0, y0, x0+w, y0+h, x1+w, y1, x1, y1+h, null);
            }
            else
            {
                og.drawImage(bitmap, x0, y0, x0+w, y0+h, x1+w, y1+h, x1, y1, null);
            }
        }
    }*/

    public final SimulatedHuman.Event tick()
    {
        xOld = x;
        yOld = y;
        SimulatedHuman.Event e = move();
        return e;
    }

    public final void tickNoMove()
    {
        xOld = x;
        yOld = y;
    }

    public float getX(float alpha)
    {
        return (xOld+(x-xOld)*alpha)-xPicO;
    }

    public float getY(float alpha)
    {
        return (yOld+(y-yOld)*alpha)-yPicO;
    }

    public SimulatedHuman.Event collideCheck()
    {
    	return SimulatedHuman.Event.nothing ;
    }

    public SimulatedHuman.Event bumpCheck(int xTile, int yTile)
    {
    	return SimulatedHuman.Event.nothing ;
    }

    public boolean shellCollideCheck(Shell shell)
    {
        return false;
    }

    public void release(Mario mario)
    {
    }

    public boolean fireballCollideCheck(Fireball fireball)
    {
        return false;
    }
}