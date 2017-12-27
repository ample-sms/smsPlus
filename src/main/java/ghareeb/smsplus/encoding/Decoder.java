package ghareeb.smsplus.encoding;

import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.transmission.receiving.IReceiver;

import java.util.BitSet;

import android.content.Context;

public class Decoder extends CoderBase implements IDecoder
{
	private IReceiver receiver;
	private String messageBody;
	private long sendDateTimeMillis;
	private long key;
	
	public Decoder(IReceiver receiver)
	{
		this.receiver = receiver;
	}
	
	private void readSendDateTime()
	{
		long minutes = BitsHelper.convertToDecimal(bitSet, EncodingManager.IDENTIFIER_PREAMBLE_LENGTH, EncodingManager.SEND_DATE_TIME_PREMBLE_LENGTH);
		sendDateTimeMillis = (minutes * 60L * 1000L) + getMillisecondsOfStartDateTime();
	}

	private String decode(BitSet encoded) throws InvalidCodeException
	{
		bitSet = encoded;
		encoding = EncodingManager.generateEncodingFromIdentifierPreamble(bitSet);
		readSendDateTime();
		int position = EncodingManager.IDENTIFIER_PREAMBLE_LENGTH + EncodingManager.SEND_DATE_TIME_PREMBLE_LENGTH;
		char current = NULL_CHAR;
		StringBuilder builder = new StringBuilder();
		
		do
		{
			try
			{
				current = encoding.decode(bitSet, position, encoding.getMinimumBitsCountPerChar(), 0);

				position += encoding.getMinimumBitsCountPerChar();
			} catch (InvalidCodeException e)
			{
				position += encoding.getMinimumBitsCountPerChar();
				current = (char) BitsHelper.convertToDecimal(bitSet, position, BITS_PER_UNICODE_CHAR);
				position += BITS_PER_UNICODE_CHAR;
			}

			if (current != NULL_CHAR)
				builder.append(current);
		} while (current != NULL_CHAR);

		String result = builder.toString();

		return result;
	}

	@Override
	public boolean receiveMessagePart(byte[] partPdu, Context context) throws InvalidCodeException
	{
		//key of the received message not part
		key = receiver.receiveMessagePart(partPdu, context);	
		
		if(isMessageReceived())
		{
			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(context);
			BitSet encodedBody = receiver.getEncodedMessageBody();
			messageBody = decode(encodedBody);
			helper.ReceivedMessage_setMessageBodyAndSendTime(receiver.getMessageId(), messageBody, sendDateTimeMillis, true);
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean isMessageReceived()
	{
		return receiver.isMessageReceived();
	}

	@Override
	public ReceivedMessage getReceivedMessage()
	{
		ReceivedMessage result = new ReceivedMessage();
		result.setSender(receiver.getSender());
		result.setBody(messageBody);
		result.setKey(key);
		
		return result;
	}

}
