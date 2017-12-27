package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.CompositeEncoding;
import ghareeb.smsplus.encoding.encodings.base.SuccessiveCharsEncoding;

//Not used anymore
public class EnglishUpperEncoding extends CompositeEncoding
{
	private final static char CAPITAL_A = 'A';
	private final static char CAPITAL_Z = 'Z';

	private SuccessiveCharsEncoding capitalLettersEnglish;

	public EnglishUpperEncoding()
	{
		capitalLettersEnglish = new SuccessiveCharsEncoding(CAPITAL_A, CAPITAL_Z);
		addEncoding(capitalLettersEnglish);
	}
}
