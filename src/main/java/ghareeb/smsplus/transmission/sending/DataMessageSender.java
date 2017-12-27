package ghareeb.smsplus.transmission.sending;

import ghareeb.smsplus.Receiver_MessagePartDelivered;
import ghareeb.smsplus.Receiver_MessagePartSent;
import ghareeb.smsplus.encoding.BitsHelper;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import ghareeb.smsplus.transmission.PortBasedTransmissionBase;

import java.util.ArrayList;
import java.util.BitSet;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class DataMessageSender extends PortBasedTransmissionBase implements ISender
{
	private SmsManager manager;

	public DataMessageSender()
	{
		manager = SmsManager.getDefault();
	}

	@Override
	public void send(BitSet messageBinary, String toAddress, long messageKey, int messageId, int partsCount, Context context)
	{
		final int PARTS_COUNT = partsCount;

		if (PARTS_COUNT == 1)
			sendSinglePartMessage(messageBinary, toAddress, messageKey, context);
		else
			sendMultiPartMessage(messageBinary, toAddress, PARTS_COUNT, messageKey, messageId, context);

	}

	private void sendSinglePartMessage(BitSet message, String toAddress, long messageKey, Context context)
	{
		BitSet temp = new BitSet();
		BitsHelper.copy(message, 0, temp, 1, message.length());
		byte[] data = BitsHelper.convertToByteArray(temp);
		sendMessagePart(data, toAddress, messageKey, context);
	}

	private void sendMultiPartMessage(BitSet message, String toAddress, final int PARTS_COUNT, long messageKey, int messageId,
			Context context)
	{
		BitSet result = new BitSet();

		for (int i = 0; i < PARTS_COUNT; i++)
		{
			processMessagePart(i, messageId, PARTS_COUNT, message, result);
		}

		ArrayList<byte[]> messageParts = getMessageParts(result, PARTS_COUNT);

		for (int i = 0; i < messageParts.size() ; i++)
		{
			sendMessagePart(messageParts.get(i), toAddress, messageKey, context);
		}
	}

	private void sendMessagePart(byte[] data, String toAddress, final long MESSAGE_KEY, Context context)
	{
		Intent partSentIntent = new Intent(context, Receiver_MessagePartSent.class);
		partSentIntent.putExtra(Receiver_MessagePartSent.KEY_MESSAGE_KEY, MESSAGE_KEY);
		partSentIntent.putExtra(Receiver_MessagePartSent.KEY_RECEPIENT, toAddress);
		partSentIntent.setAction(Receiver_MessagePartSent.ACTION_MESSAGE_PART_SENT);
		PendingIntent partSentPI = PendingIntent.getBroadcast(context, 0, partSentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent partDeliveredIntent = new Intent(context, Receiver_MessagePartDelivered.class);
		partDeliveredIntent.putExtra(Receiver_MessagePartDelivered.KEY_MESSAGE_KEY, MESSAGE_KEY);
		partDeliveredIntent.putExtra(Receiver_MessagePartDelivered.KEY_RECEPIENT, toAddress);
		partDeliveredIntent.setAction(Receiver_MessagePartDelivered.ACTION_MESSAGE_PART_DELIVERED);
		PendingIntent partDeliveredPI = PendingIntent.getBroadcast(context, 0, partDeliveredIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		String sc = SharedPreferencesHelper.getString(context, SharedPreferencesHelper.PREF_CALL_CENTER);

		if (sc == null || sc.equals(""))
			manager.sendDataMessage(toAddress, null, PORT, data, partSentPI, partDeliveredPI);
		else
			manager.sendDataMessage(toAddress, sc, PORT, data, partSentPI, partDeliveredPI);
	}

	private void processMessagePart(int partNumber, int messageId, int totalPartsCount, BitSet original, BitSet target)
	{
		final int DEST_START_INDEX = partNumber * (MESSAGE_BITS_COUNT - PORT_HEADER_BITS_COUNT);
		target.set(DEST_START_INDEX);
		BitsHelper.convertToBinary(messageId, target, DEST_START_INDEX + 1, 5);
		BitsHelper.convertToBinary(totalPartsCount, target, DEST_START_INDEX + 6, 5);
		BitsHelper.convertToBinary(partNumber, target, DEST_START_INDEX + 11, 5);

		final int COPY_LENGTH = (MESSAGE_BITS_COUNT - (PORT_HEADER_BITS_COUNT + MULTIPART_MESSAGE_HEADER_BITS_COUNT));

		if (COPY_LENGTH > 0)//Remember: the final part may be a dummy part present for counting consistency
		{
			final int SOURCE_COPY_START_INDEX = partNumber * COPY_LENGTH;
			final int DEST_COPY_START_INDEX = SOURCE_COPY_START_INDEX + (partNumber + 1) * MULTIPART_MESSAGE_HEADER_BITS_COUNT;

			BitsHelper.copy(original, SOURCE_COPY_START_INDEX, target, DEST_COPY_START_INDEX, COPY_LENGTH);
		}
	}

	private ArrayList<byte[]> getMessageParts(BitSet dataWithHeaders, final int PARTS_COUNT)
	{
		byte[] allData = BitsHelper.convertToByteArray(dataWithHeaders);
		byte[] current;
		final int BYTES_PER_PART = (MESSAGE_BITS_COUNT - PORT_HEADER_BITS_COUNT) / 8;
		ArrayList<byte[]> result = new ArrayList<byte[]>();

		for (int part = 0; part < PARTS_COUNT - 1; part++)
		{
			current = new byte[BYTES_PER_PART];
			result.add(current);

			for (int i = 0; i < current.length; i++)
			{
				current[i] = allData[i + part * current.length];
			}
		}

		final int FINAL_PART_START = BYTES_PER_PART * (PARTS_COUNT - 1);
		final int FINAL_PART_LENGTH = allData.length - FINAL_PART_START;
		current = new byte[FINAL_PART_LENGTH];
		result.add(current);

		for (int i = 0; i < current.length; i++)
		{
			current[i] = allData[i + FINAL_PART_START];
		}

		
		return result;
	}

	@Override
	public int getBitsCountPerMessageId()
	{
		return MESSAGE_ID_BITS_COUNT;
	}

	public int getSinglePartMessageHeaderLength()
	{
		return PORT_HEADER_BITS_COUNT + SINGLE_PART_MESSAGE_HEADER_BITS_COUNT;
	}

	public int getMultipartMessageHeaderLength()
	{
		return PORT_HEADER_BITS_COUNT + MULTIPART_MESSAGE_HEADER_BITS_COUNT;
	}
}
