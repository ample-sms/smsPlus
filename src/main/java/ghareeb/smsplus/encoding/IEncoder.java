package ghareeb.smsplus.encoding;

import android.content.Context;
import ghareeb.smsplus.common.CountInformation;
import ghareeb.smsplus.database.entities.SentMessage;

public interface IEncoder
{
	SentMessage send(String message, String toAddress, Context context) throws InvalidCharException;
	
	SentMessage send(String message, long threadId, String toAddress, Context context) throws InvalidCharException;
	
	CountInformation getCountInformation(String message) throws InvalidCharException;
}
