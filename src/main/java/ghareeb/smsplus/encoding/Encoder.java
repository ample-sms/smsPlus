package ghareeb.smsplus.encoding;

import ghareeb.smsplus.common.CountInformation;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;
import ghareeb.smsplus.transmission.PortBasedTransmissionBase;
import ghareeb.smsplus.transmission.sending.ISender;

import java.util.BitSet;
import java.util.GregorianCalendar;

import android.content.Context;

public class Encoder extends CoderBase implements IEncoder
{
	private int unicodeCharsCount;
	private int nonUnicodeCharsCount;
	private ISender sender;

	public Encoder(ISender sender)
	{
		this.sender = sender;
	}

	private void insertDateTime(BitSet bitSet)
	{
		//Get the time exactly as shown on device
		GregorianCalendar c = new GregorianCalendar();
		long millisSince1970 = c.getTimeInMillis() + c.get(GregorianCalendar.DST_OFFSET) + c.get(GregorianCalendar.ZONE_OFFSET);
		long milliSecondsFrom1970ToStart = getMillisecondsOfStartDateTime();
		long millisSiceStart = (millisSince1970 - milliSecondsFrom1970ToStart);
		int minutesSinceStart = (int) (millisSiceStart / (1000 * 60));
		BitsHelper.convertToBinary(minutesSinceStart, bitSet, EncodingManager.IDENTIFIER_PREAMBLE_LENGTH,
				EncodingManager.SEND_DATE_TIME_PREMBLE_LENGTH);
	}

	private void encode(String originalString) throws InvalidCharException
	{
		bitSet = new BitSet();
		encoding = EncodingManager.buildEncoding(originalString, bitSet);
		unicodeCharsCount = 0;
		nonUnicodeCharsCount = 0;
		int position = EncodingManager.IDENTIFIER_PREAMBLE_LENGTH + EncodingManager.SEND_DATE_TIME_PREMBLE_LENGTH;
		int bitsPerChar = encoding.getMinimumBitsCountPerChar();

		for (int i = 0; i < originalString.length(); i++)
		{
			try
			{
				encoding.encode(originalString.charAt(i), bitSet, position, bitsPerChar, 0);
				nonUnicodeCharsCount++;
				position += bitsPerChar;
			} catch (InvalidCharException e)
			{
				BitsHelper.convertToBinary(UNICODE_PREAMBLE, bitSet, position, bitsPerChar);
				position += bitsPerChar;
				nonUnicodeCharsCount++;
				BitsHelper.convertToBinary(originalString.charAt(i), bitSet, position, BITS_PER_UNICODE_CHAR);
				position += BITS_PER_UNICODE_CHAR;
				unicodeCharsCount++;
			}
		}

	}

	@Override
	public synchronized SentMessage send(String messageBody, long threadId, String toAddress, Context context)
			throws InvalidCharException
	{
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(context);
		SentMessage message = helper.SentMessage_insert(threadId, messageBody, sender.getBitsCountPerMessageId(), true);
		CountInformation countInformation = getCountInformation(messageBody);
		final int PARTS_COUNT = countInformation.getMessagePartsCount();
		insertDateTime(bitSet);
		helper.SentMessage_setPartsCount(message.getKey(), PARTS_COUNT, true);
		message.setRecepient(toAddress);
		sender.send(bitSet, toAddress, message.getKey(), message.getId(), PARTS_COUNT, context);

		return message;
	}

	// Inserts a contact and a thread if needed (may insert a thread only)
	// Fixes partially erratic toAddress
	@Override
	public synchronized SentMessage send(String messageBody, String toAddress, Context context) throws InvalidCharException
	{
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(context);
		ThreadEntity thread = helper.Thread_getThreadOfPhoneNumber(toAddress);

		return send(messageBody, thread.getId(), thread.getContact().getNumber().toString(), context);
	}

	@Override
	public synchronized CountInformation getCountInformation(String message) throws InvalidCharException
	{
		encode(message);

		if (message == null || message.equals(""))
			return new CountInformation(0, 0);

		int partsCount = 0;
		int remainingChars = 0;
		int messageNonUniChars = getEquivalentNonUnicodeCharsCountOfTheMessage();

		if (isMessageSinglePart())
		{
			int nonUniCharsOfSinglePart = getNonUnicodeCharsCountForSinglePartMessage();
			partsCount = 1;
			remainingChars = nonUniCharsOfSinglePart - messageNonUniChars;
		}
		else
		{
			int nonUniCharsOfFirstPart = getNonUnicodeCharsCountForFirstPartOfMultipartMessage();
			int nonUniCharsOfAnotherPart = getNonUnicodeCharsCountForOtherPartsOfMultipartMessage();

			messageNonUniChars -= nonUniCharsOfFirstPart;
			partsCount = 1 + (messageNonUniChars / nonUniCharsOfAnotherPart);
			messageNonUniChars %= nonUniCharsOfAnotherPart;

			if (messageNonUniChars > 0)
			{
				partsCount++;
				remainingChars = nonUniCharsOfAnotherPart - messageNonUniChars;
			}
			else
				remainingChars = 0;// chars fit exactly

		}

		CountInformation result = new CountInformation(remainingChars, partsCount);

		return result;
	}

	private int getNonUnicodeCharsCountForFirstPartOfMultipartMessage()
	{
		final int totalHeader = sender.getMultipartMessageHeaderLength() + EncodingManager.IDENTIFIER_PREAMBLE_LENGTH + EncodingManager.SEND_DATE_TIME_PREMBLE_LENGTH;
		final int bitsPerSMS = PortBasedTransmissionBase.MESSAGE_BITS_COUNT;
		final int availableBitsPerFirstPart = bitsPerSMS - totalHeader;
		final int nonUniCharsCountOfFirstPart = availableBitsPerFirstPart / encoding.getMinimumBitsCountPerChar();

		return nonUniCharsCountOfFirstPart;
	}

	private int getNonUnicodeCharsCountForOtherPartsOfMultipartMessage()
	{
		final int totalHeader = sender.getMultipartMessageHeaderLength();
		final int bitsPerSMS = PortBasedTransmissionBase.MESSAGE_BITS_COUNT;
		final int availableBitsPerFirstPart = bitsPerSMS - totalHeader;
		final int nonUniCharsCountOfAnotherPart = availableBitsPerFirstPart / encoding.getMinimumBitsCountPerChar();

		return nonUniCharsCountOfAnotherPart;
	}

	private int getNonUnicodeCharsCountForSinglePartMessage()
	{
		int headerLengthOfSinglePartMessage = sender.getSinglePartMessageHeaderLength()
				+ EncodingManager.IDENTIFIER_PREAMBLE_LENGTH + EncodingManager.SEND_DATE_TIME_PREMBLE_LENGTH;
		int availableBitsOfSinglePartMessage = PortBasedTransmissionBase.MESSAGE_BITS_COUNT - headerLengthOfSinglePartMessage;
		int bitsPerNonUnicodeChar = encoding.getMinimumBitsCountPerChar();
		int nonUnicodeCharsCountOfSinglePartMessage = availableBitsOfSinglePartMessage / bitsPerNonUnicodeChar;// Being
																												// pessimistic

		return nonUnicodeCharsCountOfSinglePartMessage;

	}

	// Here a single unicode char is treated as multiple non-unicode chars
	private int getEquivalentNonUnicodeCharsCountOfTheMessage()
	{
		double nonUniPerUni = (double) BITS_PER_UNICODE_CHAR / encoding.getMinimumBitsCountPerChar();
		double nonUniPerMessage = nonUnicodeCharsCount + (nonUniPerUni * unicodeCharsCount);
		int result = (int) Math.ceil(nonUniPerMessage);

		return result;
	}

	private boolean isMessageSinglePart()
	{
		int singlePartMessageNonUniCharsCount = getNonUnicodeCharsCountForSinglePartMessage();
		int nonUniCharsCountOfTheMessage = getEquivalentNonUnicodeCharsCountOfTheMessage();

		if (nonUniCharsCountOfTheMessage <= singlePartMessageNonUniCharsCount)
			return true;
		else
			return false;
	}

}
