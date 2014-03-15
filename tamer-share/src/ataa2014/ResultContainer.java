package ataa2014;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ResultContainer {
	
	private int nrVars = 6; 
	private ArrayList<ArrayList<Integer>> vars;
	private int[][] vars_total;
	private PrintWriter printer;
	private boolean openFile = false;
	private int nrRuns;
	
	//Variables that are kept track of
	public static int nrLevelsFinished; //
	public static int timesDied; //
	public static int coins_received; //
	public static int enemies_killed; //
	public static int power_ups_received;//
	public static int distance_traveled;//
	
	public ResultContainer(){
		System.out.println("Current directory = " + System.getProperty("user.dir"));		
		resetVars();
		vars = new ArrayList<ArrayList<Integer>> () ;
		vars_total = null;
		nrRuns = 0;
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
		vars.get(5).add(distance_traveled);
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
			for(int i = 0; i< vars_total.length;i++)
			{
				for(int j = 0; j<vars_total[i].length;j++)
				{
					printer.print(vars_total[i][j]/(double)nrRuns);
					vars_total[i][j] = 0;
					if(j<vars_total[i].length-1)
						printer.print("\t");
				}
				printer.print("\n");				
			}
			close_all();
			resetVars();			
			openFile = false;
			nrRuns = 0;
		}
		else
		{
			System.err.println("Trying to write to file without an opened file!");
		}
	}
	
	public void run_finished()
	{
		if(vars_total == null)
		{
			vars_total = new int[vars.size()][vars.get(0).size()];
		}
		
			
		for(int i = 0; i<vars.size();i++)
		{
			for(int j = 0; j<vars.get(i).size();j++)
			{
				vars_total[i][j] += vars.get(i).get(j);
			}
			vars.get(i).clear();
		}
		nrRuns++;
		resetVars();
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
		distance_traveled = 0;
	}
	
	public void close_all()
	{
		printer.close();
	}
	
}
