package ghareeb.smsplus.encoding;

import android.content.Context;
import ghareeb.smsplus.database.entities.ReceivedMessage;

public interface IDecoder
{
	boolean receiveMessagePart(byte[] partPdu, Context context) throws InvalidCodeException;
	
	boolean isMessageReceived();
	
	ReceivedMessage getReceivedMessage();
}
