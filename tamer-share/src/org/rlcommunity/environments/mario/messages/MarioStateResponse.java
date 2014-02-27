package org.rlcommunity.environments.mario.messages;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.BinaryPayload;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

public class MarioStateResponse extends AbstractResponse {
	
	double mario_px;
	double mario_py;

	public MarioStateResponse(double mario_px, double mario_py) {
		this.mario_px=mario_px;
		this.mario_py=mario_py;
		
		
	}
	public MarioStateResponse(String responseMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse = new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayLoad();

		StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");

		mario_px=Double.parseDouble(stateTokenizer.nextToken());
		mario_py=Double.parseDouble(stateTokenizer.nextToken());
    }

	@Override
	public String toString() {
		String theResponse="MarioStateResponse: not implemented ";
		return theResponse;
	}

@Override
    public String makeStringResponse() {
        StringBuffer theResponseBuffer = new StringBuffer();
        theResponseBuffer.append("TO=");
        theResponseBuffer.append(MessageUser.kBenchmark.id());
        theResponseBuffer.append(" FROM=");
        theResponseBuffer.append(MessageUser.kEnv.id());
        theResponseBuffer.append(" CMD=");
        theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
        theResponseBuffer.append(" VALTYPE=");
        theResponseBuffer.append(MessageValueType.kStringList.id());
        theResponseBuffer.append(" VALS=");

        theResponseBuffer.append(mario_px);
		theResponseBuffer.append(":");
		theResponseBuffer.append(mario_py);

		return theResponseBuffer.toString();
    }

public double getPositionX() {
	return mario_px;
}

public double getPositionY() {
	return mario_py;
}

}