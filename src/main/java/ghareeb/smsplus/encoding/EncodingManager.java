package ghareeb.smsplus.encoding;

import ghareeb.smsplus.encoding.encodings.ArabicEncoding;
import ghareeb.smsplus.encoding.encodings.BasicPunctuationEncoding;
import ghareeb.smsplus.encoding.encodings.DynamicEncoding;
import ghareeb.smsplus.encoding.encodings.EnglishLowerEncoding;
import ghareeb.smsplus.encoding.encodings.LatinNumbersEncoding;
import ghareeb.smsplus.encoding.encodings.PaddingEncoding;
import ghareeb.smsplus.encoding.encodings.SmiliesEncoding;
import ghareeb.smsplus.encoding.encodings.UnicodeEncoding;
import ghareeb.smsplus.encoding.encodings.base.CompositeEncoding;
import ghareeb.smsplus.encoding.encodings.base.IEncoding;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

//To add a new encoding
//1- create encoding class
//2- add IdentifierElement
//3- add string constant
//4- add an add statement in the static constructor
//5- add if statement in (getIdentifierOfEncoding) method
//6- add a case in (getEncodingByIdentifier) method
public class EncodingManager
{
	enum IdentifierElement
	{
		UNKNOWN(-1), ARABIC(0), ENGLISH_LOWER(1), SMILIES(2), LATIN_NUMBERS(3);

		public final int INDEX;

		private IdentifierElement(int value)
		{
			INDEX = value;
		}

		public static IdentifierElement getByIndex(int index)
		{
			IdentifierElement[] all = values();

			for (int i = 0; i < all.length; i++)
				if (all[i].INDEX == index)
					return all[i];

			return UNKNOWN;
		}
	}

	public static final int IDENTIFIER_PREAMBLE_LENGTH = 16;
	public static final int SEND_DATE_TIME_PREMBLE_LENGTH = 24;

	private static Hashtable<String, IEncoding> encodingTable = new Hashtable<String, IEncoding>();
	public static final String PADDING = "padding";
	public static final String UNICODE = "unicode";
	public static final String PUNCTUATION_BASIC = "punctB";
	
	public static final String ARABIC = "arabic";
	public static final String NUMBERS_LATIN = "numbersL";
	public static final String ENGLISH_LOWER = "english_lower";
	public static final String SMILIES = "smilies";

	static
	{
		encodingTable.put(ARABIC, new ArabicEncoding());
		encodingTable.put(PADDING, new PaddingEncoding());
		encodingTable.put(NUMBERS_LATIN, new LatinNumbersEncoding());
		encodingTable.put(PUNCTUATION_BASIC, new BasicPunctuationEncoding());
		encodingTable.put(ENGLISH_LOWER, new EnglishLowerEncoding());
		encodingTable.put(UNICODE, new UnicodeEncoding());
		encodingTable.put(SMILIES, new SmiliesEncoding());
	}

	public static IEncoding getEncoding(String encodingName)
	{
		return encodingTable.get(encodingName);
	}

	// Finds an encoding that is able to encode the specified character
	public static IEncoding getEncoding(char c)
	{
		List<IEncoding> all = Collections.list(encodingTable.elements());

		return findEncoding(c, all);
	}

	// generate an ordered Encoding and fills the identifier preamble at the
	// beginning the BitSet
	public static IEncoding buildEncoding(String text, BitSet toInsertIdentifierPreambleTo)
	{
		DynamicEncoding result = instantiateDynamicEncoding();
		List<IEncoding> added = new ArrayList<IEncoding>(result.getAllChildren());
		IEncoding[] encodings = new IEncoding[IDENTIFIER_PREAMBLE_LENGTH];
		IEncoding currentEncoding;
		char currentChar;

		for (int i = 0; i < text.length(); i++)
		{
			currentChar = text.charAt(i);

			if (findEncoding(currentChar, added) == null)
			{
				currentEncoding = getEncoding(currentChar);

				if (currentEncoding != null)
				{
					added.add(currentEncoding);
					encodings[getIdentifierOfEncoding(currentEncoding).INDEX] = currentEncoding;
				}
			}
		}

		for (int i = 0; i < encodings.length; i++)
			if (encodings[i] != null)
			{
				result.addEncoding(encodings[i]);
				toInsertIdentifierPreambleTo.set(i);
			}

		return result;
	}

	public static CompositeEncoding generateEncodingFromIdentifierPreamble(BitSet bitSet)
	{
		IdentifierElement currentElement;
		IEncoding currentEncoding = null;
		DynamicEncoding encoding = instantiateDynamicEncoding();

		for (int i = 0; i < IDENTIFIER_PREAMBLE_LENGTH; i++)
		{
			if (bitSet.get(i))
			{
				currentElement = IdentifierElement.getByIndex(i);
				currentEncoding = getEncodingByIdentifier(currentElement);

				if (currentEncoding != null)
					encoding.addEncoding(currentEncoding);
			}
		}

		return encoding;
	}

	private static DynamicEncoding instantiateDynamicEncoding()
	{
		DynamicEncoding result = new DynamicEncoding();
		result.addEncoding(getEncoding(PADDING));
		result.addEncoding(getEncoding(UNICODE));
		result.addEncoding(getEncoding(PUNCTUATION_BASIC));

		return result;
	}

	private static IEncoding findEncoding(char c, Iterable<IEncoding> all)
	{
		for (IEncoding enc : all)
		{
			if (enc.isRecognized(c))
				return enc;
		}

		return null;
	}

	private static IdentifierElement getIdentifierOfEncoding(IEncoding enc)
	{
		if (enc == getEncoding(ARABIC))
			return IdentifierElement.ARABIC;

		if (enc == getEncoding(ENGLISH_LOWER))
			return IdentifierElement.ENGLISH_LOWER;

		if(enc == getEncoding(SMILIES))
			return IdentifierElement.SMILIES;

		if (enc == getEncoding(NUMBERS_LATIN))
			return IdentifierElement.LATIN_NUMBERS;
		

		return IdentifierElement.UNKNOWN;
	}

	private static IEncoding getEncodingByIdentifier(IdentifierElement element)
	{
		IEncoding currentEncoding = null;

		switch (element)
		{
			case ARABIC:
				currentEncoding = getEncoding(ARABIC);
				break;
			case ENGLISH_LOWER:
				currentEncoding = getEncoding(ENGLISH_LOWER);
				break;
			case LATIN_NUMBERS:
				currentEncoding = getEncoding(NUMBERS_LATIN);
				break;
			case SMILIES:
				currentEncoding = getEncoding(SMILIES);
				break;
			default:
				currentEncoding = null;
				break;
		}

		return currentEncoding;
	}

}
