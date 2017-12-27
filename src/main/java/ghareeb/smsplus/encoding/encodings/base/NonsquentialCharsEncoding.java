package ghareeb.smsplus.encoding.encodings.base;

import ghareeb.smsplus.encoding.BitsHelper;
import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.encoding.InvalidCodeException;

import java.util.BitSet;

public class NonsquentialCharsEncoding implements IEncoding {

    private char[] charSet;

    public NonsquentialCharsEncoding(char... params) {
	charSet = params;
    }

    @Override
    public void encode(char toEncode, BitSet bitSet, int startIndex,
	    int charLength, int offset) throws InvalidCharException {
	if(!isRecognized(toEncode))
	    throw new InvalidCharException();
	int value = findCharIndex(toEncode);
	value += offset;
	BitsHelper.convertToBinary(value, bitSet, startIndex, charLength);

    }

    @Override
    public char decode(BitSet bitSet, int startIndex, int charLength, int offset)
	    throws InvalidCodeException {
	int value = BitsHelper.convertToDecimal(bitSet, startIndex, charLength);
	value -= offset;
	
	if(value < 0 || value >= charSet.length)
	    throw new InvalidCodeException();
	
	return charSet[value];
    }

    @Override
    public int getMinimumBitsCountPerChar() {
	int charsCount = getCharsCount();

	return (int) Math.ceil(Math.log(charsCount) / Math.log(2.0));
    }

    @Override
    public int getCharsCount() {

	return charSet.length;
    }

    @Override
    public boolean isRecognized(char character) {

	return findCharIndex(character) >= 0;
    }

    private int findCharIndex(char value) {
	for (int i = 0; i < charSet.length; i++)
	    if (charSet[i] == value)
		return i;

	return -1;
    }

}
