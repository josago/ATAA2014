package ataa2014;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;



class InputPanel extends JPanel implements KeyListener {
    private char c = 'e';
    private NeuralNet neuralNet;


    public InputPanel() {
        this.setPreferredSize(new Dimension(500, 500));
        addKeyListener(this);
        
    }

    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    public void paintComponent(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g.drawString("the key that pressed is " + c, 250, 250);
    }

    public void keyPressed(KeyEvent e) { }
    public void keyReleased(KeyEvent e) { }
    public void keyTyped(KeyEvent e) {
        c = e.getKeyChar();
        int output = 0;
        if (c == 'k') output = -1;
        if (c == 'l') output = 1;
        
        System.out.println(output);
    	Random rndNumbers = new Random();

        int rndNumber = rndNumbers.nextInt(2);
        System.out.println("Number: " + rndNumber);
        
        repaint();
    }

    public static void main(String[] s) {
        JFrame f = new JFrame();
        f.getContentPane().add(new InputPanel());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        
        Thread thread = new Thread()
        {
            public void run()
            {
                while (true){
                    //System.out.println("Hello World");
                    
                	Random rndNumbers = new Random();

                    int rndNumber = rndNumbers.nextInt(2);
                    
                    if(rndNumber == 0)
                    	rndNumber = -1;
                    
                    System.out.println("Hello Random Number: " + rndNumber);
                    
                    try
                    {
                        Thread.sleep(100); // 1 second
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