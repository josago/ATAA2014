package org.rlcommunity.environments.mario.viz;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.util.Random;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;

import org.rlcommunity.environments.mario.viz.sprites.*;
import org.rlcommunity.environments.mario.sonar.FakeSoundEngine;
import org.rlcommunity.environments.mario.sonar.SonarSoundEngine;

import org.rlcommunity.environments.mario.FakeGraphics;
import org.rlcommunity.environments.mario.FakeImage;
import org.rlcommunity.environments.mario.GlueMario;

import ataa2014.ParamsATAA;
import ataa2014.SimulatedHuman;


public class MarioComponent extends JComponent implements Runnable, KeyListener, FocusListener
{
    private static final long serialVersionUID = 739318775993206607L;
    public static int TICKS_PER_SECOND = 24;

    private boolean running = false;
    private int width, height;
    private GraphicsConfiguration graphicsConfiguration;
    private Scene scene;
    private SonarSoundEngine sound;
//    private boolean focused = false;
    private boolean useScale2x = false;
    private MapScene mapScene;

    private Scale2x scale2x = new Scale2x(320, 240);
    
    private SimulatedHuman simHuman;

    public MarioComponent(int width, int height)
    {
        this.setFocusable(true);
        this.setEnabled(true);
        this.width = width;
        this.height = height;
        setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setRequestFocusEnabled(true);
		addKeyListener(this);

        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        sound = new FakeSoundEngine();
        try
        {
            if (org.rlcommunity.environments.mario.GlueMario.go_quiet) {
            	throw new LineUnavailableException("No sound when fast or dark");
            }
            else {
            	sound = new SonarSoundEngine(64);
            }
        }
        catch (LineUnavailableException e)
        {
            System.out.println(e);
        }

        setFocusable(true);
        
    }
    
    /**
     * Constructor for when using a simulated human to provide reinforcement in the human reinforcement
     * setting
     * @param width
     * @param height
     * @param h = Simulated human
     */
    public MarioComponent(int width, int height, SimulatedHuman h)
    {
        this.setFocusable(true);
        this.setEnabled(true);
        this.width = width;
        this.height = height;
        setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setRequestFocusEnabled(true);
		addKeyListener(this);

        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        sound = new FakeSoundEngine();
        try
        {
            if (org.rlcommunity.environments.mario.GlueMario.go_quiet) {
            	throw new LineUnavailableException("No sound when fast or dark");
            }
            else {
            	sound = new SonarSoundEngine(64);
            }
        }
        catch (LineUnavailableException e)
        {
            System.out.println(e);
        }

        setFocusable(true);
        
        simHuman = h;
    }

    private void toggleKey(int keyCode, boolean isPressed)
    {
        if (keyCode == KeyEvent.VK_LEFT)
        {
            scene.toggleKey(Mario.KEY_LEFT, isPressed);
        }
        if (keyCode == KeyEvent.VK_RIGHT)
        {
            scene.toggleKey(Mario.KEY_RIGHT, isPressed);
        }
        if (keyCode == KeyEvent.VK_DOWN)
        {
            scene.toggleKey(Mario.KEY_DOWN, isPressed);
        }
        if (keyCode == KeyEvent.VK_UP)
        {
            scene.toggleKey(Mario.KEY_UP, isPressed);
        }
        if (keyCode == KeyEvent.VK_CONTROL)
        {
            scene.toggleKey(Mario.KEY_SPEED, isPressed);
        }
        if (keyCode == KeyEvent.VK_SPACE)
        {
            scene.toggleKey(Mario.KEY_JUMP, isPressed);
        }
        if (isPressed && keyCode == KeyEvent.VK_F1)
        {
            useScale2x = !useScale2x;
        }
    }

    public void paint(Graphics g)
    {
    }

    public void update(Graphics g)
    {
    }

    public void start()
    {
        if (!running)
        {
            running = true;
            new Thread(this, "Game Thread").start();
        }
    }

    public void stop()
    {
    	System.out.println("Stop function called in MarioComponent");
        Art.stopMusic();
        running = false;
        sound.shutDown();
    }

    public int tick;
    public int lastTick;
    public double now;
    public double time;
    public double lastTime;
    
    public void run()
    {
    	
    	
    	if (!GlueMario.go_dark)
    		graphicsConfiguration = getGraphicsConfiguration();
    	else
    		graphicsConfiguration = null;

        //      scene = new LevelScene(graphicsConfiguration);
        mapScene = new MapScene(graphicsConfiguration, this, new Random().nextLong());
        scene = mapScene;
        scene.setSound(sound);

        VolatileImage image = null;
        Graphics g = null;
        Graphics og = null;
        
        if (!GlueMario.go_dark) {
        	Art.init(graphicsConfiguration, sound);

	        image = createVolatileImage(320, 240);
	        g = getGraphics();
	        og = image.getGraphics();
        }
        else {
        	image = new FakeImage();
        	g = new FakeGraphics();
        	og = new FakeGraphics();
        }

        lastTick = -1;
        //        double lastNow = 0;
        int renderedFrames = 0;
//        int fps = 0;

        //        double now = 0;
        //        double startTime = System.nanoTime() / 1000000000.0; 
        //        double timePerFrame = 0; 
        time = System.nanoTime() / 1000000000.0;
        now = 0;
        double start_time = time;
        double averagePassedTime = 0;

        addKeyListener(this);
        addFocusListener(this);

        boolean naiveTiming = true;
        

        if (org.rlcommunity.environments.mario.GlueMario.glue_running) {
        	org.rlcommunity.environments.mario.GlueMario.restartLevel();
        	if (org.rlcommunity.environments.mario.GlueMario.go_fast)
        		TICKS_PER_SECOND = 1000;
        }
        else
        	toTitle();

        while (running)
        {
            lastTime = time;
            time = System.nanoTime() / 1000000000.0;
            double passedTime = time - lastTime;

            if (passedTime < 0) naiveTiming = false; // Stop relying on nanotime if it starts skipping around in time (ie running backwards at least once). This sometimes happens on dual core amds.
            averagePassedTime = averagePassedTime * 0.9 + passedTime * 0.1;

            if (naiveTiming)
            {
                now = time-start_time;
            }
            else
            {
                now += averagePassedTime;
            }
            tick = (int) (now * TICKS_PER_SECOND);
            if (lastTick == -1) lastTick = tick;
            while (running && (lastTick < tick))
            {
                scene.tick();
                lastTick++;

                if (lastTick % TICKS_PER_SECOND == 0)
                {
//                    fps = renderedFrames;
                    renderedFrames = 0;
                }
                if (!org.rlcommunity.environments.mario.GlueMario.go_fast) {
                	break;
                }
            }

            float alpha = (float) (now * TICKS_PER_SECOND - tick);
            sound.clientTick(alpha);

//            int x = (int) (Math.sin(now) * 16 + 160);
//            int y = (int) (Math.cos(now) * 16 + 120);
            if (!GlueMario.go_dark) {
	            og.setColor(Color.WHITE);
	            og.fillRect(0, 0, 320, 240);
            }
            scene.render(og, alpha);

            if (!this.hasFocus() && tick/4%2==0)
            {
                String msg = "CLICK TO PLAY";

                if (!GlueMario.go_dark) {
	                drawString(og, msg, 160 - msg.length() * 4 + 1, 110 + 1, 0);
	                drawString(og, msg, 160 - msg.length() * 4, 110, 7);
                }
            }
            if (!GlueMario.go_dark) {
	            og.setColor(Color.BLACK);
	            
	            /*          drawString(og, "FPS: " + fps, 5, 5, 0);
	             drawString(og, "FPS: " + fps, 4, 4, 7);*/
	
	            if (width != 320 || height != 240)
	            {
	                if (useScale2x)
	                {
	                    g.drawImage(scale2x.scale(image), 0, 0, null);
	                }
	                else
	                {
	                    g.drawImage(image, 0, 0, 640, 480, null);
	                }
	            }
	            else
	            {
	                g.drawImage(image, 0, 0, null);
	            }
            }
        }
        
        System.out.println("Game thread past the while running loop");
        
        renderedFrames++;

        if (!org.rlcommunity.environments.mario.GlueMario.go_fast) {
            try
            {
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
            }
        }

        if (!GlueMario.go_dark)
        	Art.stopMusic();        
        
    }

    private void drawString(Graphics g, String text, int x, int y, int c)
    {
        char[] ch = text.toCharArray();
        for (int i = 0; i < ch.length; i++)
        {
            g.drawImage(Art.font[ch[i] - 32][c], x + i * 8, y, null);
        }
    }

    public void keyPressed(KeyEvent arg0)
    {
        toggleKey(arg0.getKeyCode(), true);
    }

    public void keyReleased(KeyEvent arg0)
    {
        toggleKey(arg0.getKeyCode(), false);
    }

    public void startLevel(long seed, int difficulty, int type)
    {    	
        scene = new LevelScene(graphicsConfiguration, this, seed, difficulty, type);
        
        if(ParamsATAA.useSimulatedHuman)
        	scene.addSimulatedHuman(simHuman);
        
        //System.out.println("LevelScene generated");
        scene.setSound(sound);
        scene.init();
    }

    public void levelFailed()
    {
        if (org.rlcommunity.environments.mario.GlueMario.glue_running) {
	        org.rlcommunity.environments.mario.GlueMario.reportLoss();
	        org.rlcommunity.environments.mario.GlueMario.restartLevel();
        }
        else {
            scene = mapScene;
            mapScene.startMusic();
        }
        Mario.lives--;
        if (Mario.lives == 0)
        {
            lose();
        }
    }

    public void keyTyped(KeyEvent arg0)
    {
    }

    public void focusGained(FocusEvent arg0)
    {
//        focused = true;
    }

    public void focusLost(FocusEvent arg0)
    {
//        focused = false;
    }

    public void levelWon()
    {
        scene = mapScene;
        mapScene.startMusic();
        if (org.rlcommunity.environments.mario.GlueMario.glue_running) {
	        org.rlcommunity.environments.mario.GlueMario.reportWin();
	        
	        if (!ParamsATAA.ATAA_Exp)
	        {
	        	org.rlcommunity.environments.mario.GlueMario.param.level_difficulty++; //guangliang added, increase difficulty to continue to next level
	        }
	        
	        org.rlcommunity.environments.mario.GlueMario.restartLevel();
        }
        else
        	mapScene.levelWon();
    }
    
    public void win()
    {
        scene = new WinScene(this);
        scene.setSound(sound);
        scene.init();
    }
    
    public void toTitle()
    {
        Mario.resetStatic();
        scene = new TitleScene(this, graphicsConfiguration);
        scene.setSound(sound);
        scene.init();
    }
    
    public void lose()
    {
        scene = new LoseScene(this);
        scene.setSound(sound);
        scene.init();
    }

    public void startGame()
    {
        scene = mapScene;
        mapScene.startMusic();
        mapScene.init();
   }
}