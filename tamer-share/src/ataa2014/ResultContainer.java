package ataa2014;

import java.util.ArrayList;

public class ResultContainer {
	
	private int nrVars = 5; 
	ArrayList<ArrayList<Integer>> vars;
	
	//Variables that are kept track of
	//TODO: get these updated in the process
	public static int nrLevelsFinished;
	public static int timesDied;
	public static int coins_received;
	public static int enemies_killed;
	public static int power_ups_received;
	
	public ResultContainer(){
		System.out.println("Current directory = " + System.getProperty("user.dir"));
		openFile();
		resetVars();
		
		vars = new ArrayList<ArrayList<Integer>> () ;
		for(int i = 0; i<nrVars;i++)
		{
			vars.add(new ArrayList<Integer> ());
		}
	}
	
	
	public void processResults()
	{
		// Put vars in their respective containers
		vars.get(0).add(nrLevelsFinished);
		vars.get(1).add(timesDied);
		vars.get(2).add(coins_received);
		vars.get(3).add(enemies_killed);
		vars.get(4).add(power_ups_received);
		resetVars();
	}
	
	public void openFile()
	{
		//Open file using name in ParamsATAA
		//Create buffered writer and shizzle
	}
	
	public void writeToFile()
	{
		//Write the containers to file		
	}
	
	/**
	 * Set all vars to initial values
	 */
	public void resetVars()
	{		
		nrLevelsFinished = 0;
		timesDied = 0;		
		coins_received = 0;
		enemies_killed = 0;
		power_ups_received = 0;
	}
	
	public void close_all()
	{
		//Close file and buffered writer and everything
	}
	
}
