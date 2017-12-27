package ghareeb.smsplus.transmission.receiving;

import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.encoding.BitsHelper;
import ghareeb.smsplus.transmission.PortBasedTransmissionBase;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

import android.content.Context;
import android.telephony.SmsMessage;
import android.util.Log;

public class Receiver extends PortBasedTransmissionBase implements IReceiver
{
	private static final int IS_MULTIPART_MASK = 0x1;
	private static final int MESSAGE_ID_MASK = 0x3e;
	private static final int PARTS_COUNT_MASK = 0x7c0;
	private static final int PART_ID_MASK = 0xf800;

	// helper attributes
	private BitSet dataWithHeaders;
	private int partHeader;
	private SmsPlusDatabaseHelper helper;

	// received part attributes
	private String sender;
	private int messageId;
	private int partsCount;
	private int partNumber;
	private byte[] partData;
	private boolean isMultipart;

	public int getMessageId()
	{
		return messageId;
	}
	
	private void readPdu(byte[] partPdu)
	{
		SmsMessage message;
		message = SmsMessage.createFromPdu(partPdu);
		sender = message.getOriginatingAddress();
		partData = message.getUserData();
		this.dataWithHeaders = BitsHelper.convertFromByteArray(partData);
		partHeader = getPartHeader();
		readIsMultipart();
		readMessageId();
		readPartsCount();
		readPartId();
	}

	private int getPartHeader()
	{
		int header = BitsHelper.convertToDecimal(dataWithHeaders, 0, MULTIPART_MESSAGE_HEADER_BITS_COUNT);

		return header;
	}

	private void readIsMultipart()
	{
		isMultipart = (partHeader & IS_MULTIPART_MASK) > 0;
	}

	private void readMessageId()
	{
		if (isMultipart)
			messageId = (partHeader & MESSAGE_ID_MASK) >> SINGLE_PART_MESSAGE_HEADER_BITS_COUNT;
		else
		{
			Random r = new Random();
			messageId = r.nextInt();
		}
	}

	private void readPartsCount()
	{
		if (isMultipart)
			partsCount = (partHeader & PARTS_COUNT_MASK) >> (SINGLE_PART_MESSAGE_HEADER_BITS_COUNT + PARTS_COUNT_BITS_COUNT);
		else
			partsCount = 1;
	}

	private void readPartId()
	{
		if (isMultipart)
			partNumber = (partHeader & PART_ID_MASK) >> (SINGLE_PART_MESSAGE_HEADER_BITS_COUNT + PARTS_COUNT_BITS_COUNT + PART_NUMBER_BITS_COUNT);
		else
			partNumber = 0;
	}

	private byte[] getReceivedMessageData(boolean close)
	{
		ArrayList<byte[]> partsData = helper.ReceivedMessagePart_getOrderedPartsData(messageId, close);
		ArrayList<Byte> temp = new ArrayList<Byte>();

		for (byte[] array : partsData)
		{
			for (Byte currentByte : array)
			{
				temp.add(currentByte);
			}
		}

		byte[] result = new byte[temp.size()];

		for (int i = 0; i < result.length; i++)
		{
			result[i] = temp.get(i);
		}

		return result;
	}

	private BitSet removeHeaders(BitSet messageBodyWithHeaders, final int PARTS_COUNT)
	{
		BitSet result = new BitSet();

		if (!isMultipart)
			BitsHelper.copy(messageBodyWithHeaders, SINGLE_PART_MESSAGE_HEADER_BITS_COUNT, result, 0,
					messageBodyWithHeaders.length() - SINGLE_PART_MESSAGE_HEADER_BITS_COUNT);
		else
		{
			int partLength, sourceCopyStart, destCopyStart;
			final int NORMAL_PART_LENGTH = MESSAGE_BITS_COUNT - (PORT_HEADER_BITS_COUNT + MULTIPART_MESSAGE_HEADER_BITS_COUNT);
			final int PART_LENGTH_WITH_HEADER = MESSAGE_BITS_COUNT - PORT_HEADER_BITS_COUNT;
			final int LAST_PART_LENGTH = messageBodyWithHeaders.length() % PART_LENGTH_WITH_HEADER
					- MULTIPART_MESSAGE_HEADER_BITS_COUNT;

			for (int i = 0; i < PARTS_COUNT; i++)
			{
				if (i < PARTS_COUNT - 1)
					partLength = NORMAL_PART_LENGTH;
				else
					partLength = LAST_PART_LENGTH;

				sourceCopyStart = i * PART_LENGTH_WITH_HEADER + MULTIPART_MESSAGE_HEADER_BITS_COUNT;
				destCopyStart = i * NORMAL_PART_LENGTH;

				BitsHelper.copy(messageBodyWithHeaders, sourceCopyStart, result, destCopyStart, partLength);
			}
		}

		return result;

	}

	@Override
	public long receiveMessagePart(byte[] partPdu, Context context)
	{
		readPdu(partPdu);
		helper = new SmsPlusDatabaseHelper(context);
		return helper.ReceivedMessagePart_receivePart(messageId, partNumber, partsCount, sender, partData, true);
	}

	@Override
	public boolean isMessageReceived()
	{
		if (helper != null)
			return helper.ReceivedMessage_isMessageReceived(messageId, true);

		Log.e("Receiver", "helper is not initialized");

		return false;
	}

	@Override
	public String getSender()
	{
		return sender;
	}

	@Override
	public BitSet getEncodedMessageBody()
	{

		if (helper != null)
		{
			byte[] messageDataWithHeaders = getReceivedMessageData(true);
			BitSet messageDataWithHeadersBS = BitsHelper.convertFromByteArray(messageDataWithHeaders);
			BitSet encodedMessageBody = removeHeaders(messageDataWithHeadersBS, partsCount);

			return encodedMessageBody;
		}

		Log.e("Receiver", "helper is not initialized");

		return null;
	}

}
