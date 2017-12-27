package ghareeb.smsplus.encoding.encodings.base;

import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.encoding.InvalidCodeException;

import java.util.BitSet;

public interface IEncoding {
    void encode(char toEncode, BitSet bitSet, int startIndex, int charLength, int offset) throws InvalidCharException;
    char decode(BitSet bitSet, int startIndex, int charLength, int offset) throws InvalidCodeException;
    int getMinimumBitsCountPerChar();
    int getCharsCount();
    boolean isRecognized(char character);

}
