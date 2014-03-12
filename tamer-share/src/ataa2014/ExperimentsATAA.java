package ataa2014;

import edu.utexas.cs.tamerProject.applet.TamerApplet;

public class ExperimentsATAA {

	TamerApplet applet;
	
	public void run_experiment()
	{
		applet = new TamerApplet();
		System.out.println("Made applet object");
		applet.init();
		System.out.println("initialized applet");
		
		//applet.start();
				
		System.out.println("Applet started");
		
		try{ 
			Thread.sleep(4000); 
		} 
		catch(InterruptedException e) {
			System.out.println("Exception: " + e);
		} 
		System.out.println("destroy now");
		applet.stop();
		applet.destroy();
		System.out.println("DESTROYED WHAHAHA");
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExperimentsATAA exp = new ExperimentsATAA();
		exp.run_experiment();
		
	}

}
