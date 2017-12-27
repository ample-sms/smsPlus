package ghareeb.smsplus.transmission.receiving;

import java.util.BitSet;

import android.content.Context;

public interface IReceiver
{
	long receiveMessagePart(byte[] partPdu, Context context);

	boolean isMessageReceived();
	
	String getSender();

	BitSet getEncodedMessageBody();
	
	int getMessageId();
}
