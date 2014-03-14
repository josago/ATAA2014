package ataa2014;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;



public class InputPanel extends JPanel implements KeyListener {
    private char c = 'e';
    ExperimentsATAA exp;


    public enum Feedback {positive, negative, nothing};
    public Feedback f; 
    
    public InputPanel() {
    	f = Feedback.nothing;
        this.setPreferredSize(new Dimension(200, 200));
        addKeyListener(this);
        
    }
    
    public InputPanel(ExperimentsATAA exp) {
    	f = Feedback.nothing;
        this.setPreferredSize(new Dimension(200, 200));
        addKeyListener(this);
        this.exp = exp;
    }

    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    public void paintComponent(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
        if(f == Feedback.positive)
        {
        	g.setColor(Color.green);
        	g.drawString("POSITIVE", 35, 100);        	
        }
        else if (f == Feedback.negative){
        	g.setColor(Color.red);
        	g.drawString("NEGATIVE", 25, 100);
        	
        }/*
        else{
        	g.drawString("other key" , 20, 100);
        }*/
        
    }

    public void keyPressed(KeyEvent e) { }
    
    public void keyReleased(KeyEvent e) { }
    
    public void keyTyped(KeyEvent e) {
    	if(ParamsATAA.useSimulatedHuman)
    	{
	        c = e.getKeyChar();
	        //System.out.println("Pressed key = " + c);
	        int output = 0;
	        if (c == 'a') {
	        	output = -1;
	        	f = Feedback.negative;
	        }
	        else if (c == 'l'){
	        	output = 1;
	        	f = Feedback.positive;
	        }
	        else{        	
	        	f = Feedback.nothing;
	        }    
	        
	        repaint();        
	        //System.out.println("Output: " + output);  
	        
	        if(exp!=null)
	        {
	        	exp.setHumanReward(output);
	        }
    	}    	
    }    
    
    static InputPanel panel;

    public static void main(String[] s) {
        JFrame f = new JFrame();
        panel = new InputPanel();
        f.getContentPane().add(panel);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
        ActionListener painter = new ActionListener(){        	
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.f = InputPanel.Feedback.nothing;
				panel.repaint();
				//System.out.println("Timer repaint");
			}
			
        };
        
        int delay = 350;
        new Timer(delay, painter).start();
        
        
        
        Thread thread = new Thread("paintInput")
        {
            public void run()
            {
            	int i = 0;
                while (i<2){
                    //System.out.println("Hello World");
                    
                	System.out.println("Step " + i);
                    i++;
                    try
                    {
                        Thread.sleep(5000); // 1 second
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
            }            
        };
        thread.start();
        
        
    }
}