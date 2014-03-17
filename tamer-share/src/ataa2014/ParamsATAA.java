package ataa2014;

public class ParamsATAA {

	//For the simulated human
	public static boolean useSimulatedHuman = true;	//Set to true when running an experiment
	public static int in_between_time_feedback_request_human_simulator = 100; //1000 = 1 sec. set to 300 when running an experiment
	
	//=============================================
	//All parameters for when running an experiment
	//=============================================
	// Run the class ExperimentsATAA after setting all parameters
	
	//Set to true when running an experiment
	public static boolean ATAA_Exp = true; 
	
	//If the ATAA_Exp boolean is set to true the level difficulty below is used
	//to generate the levels
	//For the experiment set the level difficulty to 2
	public static int level_difficulty = 2;
	
	//Number of steps that a single evaluation consists of
	//For experiment: set to 400 
	public static int nr_steps_per_evaluation = 400;
	//Number of steps that are in one bin for collecting results
	//For experiment: set to 50
	public static int nr_steps_for_episode = 50; //
	//For experiment: set to 10
	public static int nr_of_runs = 10;
	
	//Parameters that can be set and changed when running experiments
	// Options: 
	//"NeuralNet" 
	//"WekaModelPerActionModel"
	public static String model = "NeuralNet";
	//Options:
	// "FeatGen_Mario"
	// "StateRepresentation"
	public static String features = "FeatGen_Mario"; 
	public static String fileNameResults = "Results_ATAA_experiment.txt";
	
	//CHANGE THIS PARAMETER TO YOUR OWN NAME
	public static String personName = "Jorge6";
	
//	public static String[] modelOptions = {"NeuralNet"};
	//public static String[] modelOptions = {"WekaModelPerActionModel"};
//	public static String[] featureGeneratorOptions = {"FeatGen_Mario"};
	
	//For the experiment the lower Options versions should be used
	public static String[] modelOptions = {"NeuralNet", "WekaModelPerActionModel"};
	public static String[] featureGeneratorOptions = {"FeatGen_Mario", "StateRepresentation"};
			
	//If this is set to false the levels never contain pits with stairs
	public static boolean includeStairs = false;
	
	//===========================
	//Params only used when using a human trainer
	//===========================
	
	public static int nr_comparisons = 3;
	public static int nr_steps_per_comparison = 10;
	public static String fileNameResultsHuman = "Provided models combinations.txt";
}
