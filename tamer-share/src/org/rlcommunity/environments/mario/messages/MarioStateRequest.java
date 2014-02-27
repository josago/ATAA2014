package org.rlcommunity.environments.mario.messages;

import org.rlcommunity.environments.puddleworld.messages.StateResponse;
import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;
import rlVizLib.messaging.environment.EnvironmentMessages;

public class MarioStateRequest extends EnvironmentMessages{
	
	public MarioStateRequest(GenericMessage theMessageObject){
		super(theMessageObject);
	}
	
	public static MarioStateResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnv.id(),
				MessageUser.kBenchmark.id(),
				EnvMessageType.kEnvCustom.id(),
				MessageValueType.kString.id(),
				"GETMARIOSTATE");

		String responseMessage=RLGlue.RL_env_message(theRequest);

		MarioStateResponse theResponse;
		try {
			theResponse = new MarioStateResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In ".getClass().getName()+", the response was not RL-Viz compatible");
			theResponse=null;
		}

		return theResponse;

	}
	
}