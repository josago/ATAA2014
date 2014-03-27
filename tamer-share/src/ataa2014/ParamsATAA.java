package ataa2014;

public class ParamsATAA {

	//For the simulated human
	public static boolean useSimulatedHuman = true;	//Set to true when running an experiment
	public static int in_between_time_feedback_request_human_simulator = 200; //1000 = 1 sec. set to 200 when running an experiment
	
	//=============================================
	//All parameters for when running an experiment
	//=============================================
	// Run the class ExperimentsATAA after setting all parameters
	
	//CHANGE THIS PARAMETER TO YOUR OWN NAME
	public static String personName = "LYDIA2";
	
	//Set to true when running an experiment
	public static boolean ATAA_Exp = true; 
	public static int nr_steps_credit = 4;
	
	//If the ATAA_Exp boolean is set to true the level difficulty below is used
	//to generate the levels
	//For the experiment set the level difficulty to 2
	public static int level_difficulty = 0;
	
	//Number of steps that a single evaluation consists of
	//For experiment: set to 400 
	public static int nr_steps_per_evaluation = 400;
	//Number of steps that are in one bin for collecting results
	//For experiment: set to 50
	public static int nr_steps_for_episode = 50; //
	//For experiment: set to 10
	public static int nr_of_runs = 3;
	
	//Parameters that can be set and changed when running experiments
	// Options: 
	//"NeuralNet" 
	//"WekaModelPerActionModel"
	// NeuralNetWorldModel
	public static String model = "NeuralNetWorldModel";
	
	//Options:
	// "FeatGen_Mario"
	// "StateRepresentation"
	public static String features = "StateRepresentation"; 
	public static String fileNameResults = "Results_ATAA_experiment.txt";
	
	//public static String[] modelOptions = {"NeuralNetWorldModel"};
	public static String[] modelOptions = {"NeuralNet"};
	public static String[] featureGeneratorOptions = {"StateRepresentation"};
	//public static String[] modelOptions = {"WekaModelPerActionModel"};
//	public static String[] featureGeneratorOptions = {"FeatGen_Mario"};
	
	//For the experiment the lower Options versions should be used
	//public static String[] modelOptions = {"NeuralNet", "WekaModelPerActionModel"};
	//public static String[] featureGeneratorOptions = {"FeatGen_Mario", "StateRepresentation"};
			
	//If this is set to false the levels never contain pits with stairs
	public static boolean includeStairs = false;
	
	//Options: e-greedy - greedy
	public static String selectionMethod = "e-greedy";
	public static double epsilon = 0.9;
	
	
	//===========================
	//Params only used when using a human trainer
	//===========================
	
	public static int nr_steps_per_comparison = 10;
	public static int nr_steps_stats = 5;
	public static String fileNameResultsHuman = "Provided models combinations.txt";
}
