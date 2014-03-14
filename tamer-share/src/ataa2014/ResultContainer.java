package ataa2014;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ResultContainer {
	
	private int nrVars = 5; 
	private ArrayList<ArrayList<Integer>> vars;
	private PrintWriter printer;
	private boolean openFile = false;
	
	//Variables that are kept track of
	public static int nrLevelsFinished; //
	public static int timesDied; //
	public static int coins_received; //
	public static int enemies_killed; //
	public static int power_ups_received;//
	
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
		File f = new File("src/ataa2014_expResults/" + ParamsATAA.fileNameResults);
		try {
			printer = new PrintWriter(f, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		openFile = true;
	}
	
	/**
	 * In the final file each row contains a variable
	 * The order is the the order to which they are added to the container (see processResults)
	 */
	public void writeToFile()
	{
		if(openFile)
		{
			//Write the containers to file	
			for(int i = 0; i< vars.size();i++)
			{
				for(int j = 0; j<vars.get(i).size();j++)
				{
					printer.print(vars.get(i).get(j));
					if(j<vars.get(i).size()-1)
						printer.print("\t");
				}
				printer.print("\n");
				vars.get(i).clear();
			}
			close_all();
			resetVars();			
			openFile = false;
		}
		else
		{
			System.err.println("Trying to write to file without an opened file!");
		}
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
		printer.close();
	}
	
}
