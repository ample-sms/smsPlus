package ghareeb.smsplus.encoding.encodings.base;

import ghareeb.smsplus.encoding.BitsHelper;
import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.encoding.InvalidCodeException;
import ghareeb.smsplus.encoding.encodings.base.IEncoding;

import java.util.BitSet;

public class SuccessiveCharsEncoding implements IEncoding
{

	protected final char startCharacter;
	protected final char endCharacter;

	public SuccessiveCharsEncoding(char startCharacter, char endCharacter)
	{
		this.startCharacter = startCharacter;
		this.endCharacter = endCharacter;
	}

	@Override
	public void encode(char toEncode, BitSet bitSet, int startIndex, int charLength, int offset) throws InvalidCharException
	{
		if (!isRecognized(toEncode))
			throw new InvalidCharException();

		int value = (toEncode - startCharacter) + offset;
		BitsHelper.convertToBinary(value, bitSet, startIndex, charLength);
	}

	@Override
	public char decode(BitSet bitSet, int startIndex, int charLength, int offset) throws InvalidCodeException
	{
		int value = BitsHelper.convertToDecimal(bitSet, startIndex, charLength);
		value = (value - offset) + startCharacter;

		if (value > endCharacter || value < startCharacter)
			throw new InvalidCodeException();

		return (char) value;
	}

	@Override
	public int getMinimumBitsCountPerChar()
	{
		int charsCount = getCharsCount();

		return (int) Math.ceil(Math.log(charsCount) / Math.log(2.0));
	}

	@Override
	public int getCharsCount()
	{
		return (endCharacter - startCharacter) + 1;
	}

	@Override
	public boolean isRecognized(char character)
	{
		return character >= startCharacter && character <= endCharacter;
	}

}
