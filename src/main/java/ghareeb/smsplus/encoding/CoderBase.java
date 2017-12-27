package ghareeb.smsplus.encoding;

import ghareeb.smsplus.encoding.encodings.base.IEncoding;
import ghareeb.smsplus.helper.ClockHelper;

import java.util.BitSet;

/*||16-bit encoding preamble | 24-bit send date time | encoded message bits||*/
public class CoderBase
{
	protected IEncoding encoding;
	protected BitSet bitSet;
	public final static int BITS_PER_UNICODE_CHAR = 16;
	protected final static char NULL_CHAR = '\u0000';
	protected final static int UNICODE_PREAMBLE = 1;
	


	public int getBitsCountPerNonUnicodeChar()
	{
		if (encoding != null)
			return encoding.getMinimumBitsCountPerChar();

		return 0;
	}
	
	protected long getMillisecondsOfStartDateTime()
	{
		return ClockHelper.getMillisecondsOfStartDateTime();
	}

}
