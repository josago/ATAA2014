package ataa2014;

public class ParamsATAA {

	//For the simulated human
	public static boolean useSimulatedHuman = false;	
	public static int in_between_time_feedback_request_human_simulator = 300; //1000 = 1 sec
	
	//For the experiments
	public static boolean ATAA_Exp_attempt1 = false;
	public static boolean ATAA_Exp = false;
	
	//Parameters that can be set and changed when running experiments
	// Options: 
	//"NeuralNet" 
	//"WekaModelPerActionModel"
	public static String model = "NeuralNet";
	//Options:
	// "FeatGen_Mario"
	// "StateRepresentation"
	public static String features = "FeatGen_Mario"; 
	public static String fileNameResults = "Results_ATAA_experiment";
			
}
