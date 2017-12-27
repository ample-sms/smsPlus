package ghareeb.smsplus.encoding.encodings;

import java.util.BitSet;

import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.encoding.InvalidCodeException;
import ghareeb.smsplus.encoding.encodings.base.IEncoding;

public class PaddingEncoding implements IEncoding
{

	private static final char NULL_CHAR = '\u0000';

	@Override
	public void encode(char toEncode, BitSet bitSet, int startIndex, int charLength, int offset) throws InvalidCharException
	{
		throw new InvalidCharException();

	}

	@Override
	public char decode(BitSet bitSet, int startIndex, int charLength, int offset) throws InvalidCodeException
	{

		for (int i = startIndex; i < startIndex + charLength; i++)
			if (bitSet.get(i))
				throw new InvalidCodeException();

		return NULL_CHAR;
	}

	@Override
	public int getMinimumBitsCountPerChar()
	{
		return 0;
	}

	@Override
	public int getCharsCount()
	{
		return 1;
	}

	@Override
	public boolean isRecognized(char character)
	{
		return false;
	}

}
