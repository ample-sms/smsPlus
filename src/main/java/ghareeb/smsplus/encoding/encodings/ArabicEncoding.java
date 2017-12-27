package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.CompositeEncoding;
import ghareeb.smsplus.encoding.encodings.base.NonsquentialCharsEncoding;
import ghareeb.smsplus.encoding.encodings.base.SuccessiveCharsEncoding;

public class ArabicEncoding extends CompositeEncoding
{
	//39 chars and if added to the basic encodings 44
	private static final char ARABIC_QUESTION_MARK = '\u061f';
	private static final char ARABIC_COMMA = '\u060c';
	//private static final char ARABIC_SEMICOLON = '\u061b';
	private static final char ARABIC_HAMZA = '\u0621';
	private static final char ARABIC_GHAIN = '\u063A';
	//private static final char ARABIC_TATWEEL = '\u0640';
	private static final char ARABIC_FAA = '\u0641';
	private static final char ARABIC_TANWEEN_NASB = '\u064b';

	private SuccessiveCharsEncoding arabicLower;
	private SuccessiveCharsEncoding arabicUpper;
	private NonsquentialCharsEncoding arabicQuestionMark;

	public ArabicEncoding()
	{
		arabicQuestionMark = new NonsquentialCharsEncoding(ARABIC_QUESTION_MARK, ARABIC_COMMA/*, ARABIC_SEMICOLON*/);
		arabicLower = new SuccessiveCharsEncoding(ARABIC_HAMZA, ARABIC_GHAIN);
		arabicUpper = new SuccessiveCharsEncoding(/*ARABIC_TATWEEL*/ARABIC_FAA, ARABIC_TANWEEN_NASB);
		addEncoding(arabicQuestionMark);
		addEncoding(arabicLower);
		addEncoding(arabicUpper);
	}
}
