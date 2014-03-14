package ataa2014;

public class ParamsATAA {

	//For the simulated human
	public static boolean useSimulatedHuman = true;	//Set to true when running an experiment
	public static int in_between_time_feedback_request_human_simulator = 300; //1000 = 1 sec. set to 300 when running an experiment
	
	//For the experiments
	public static boolean ATAA_Exp = true; //Set to true when running an experiment
	
	//Number of steps that a single evaluation consists of
	//For experiment: set to 3600 (20 minutes is more or less 1200 sec*3 steps/sec = 3600)  
	public static int nr_steps_per_evaluation = 500;
	//Number of steps that are in one bin for collecting results
	//For experiment: set to 200
	public static int nr_steps_for_episode = 50; //
	//For experiment: set to 6
	public static int nr_of_runs = 5;
	
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
	
	public static String[] modelOptions = {"NeuralNet"};
	//public static String[] modelOptions = {"WekaModelPerActionModel", "NeuralNet"};
	public static String[] featureGeneratorOptions = {"FeatGen_Mario", "StateRepresentation"};
			
}
