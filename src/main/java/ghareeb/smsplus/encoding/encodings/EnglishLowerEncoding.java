package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.CompositeEncoding;
import ghareeb.smsplus.encoding.encodings.base.SuccessiveCharsEncoding;

public class EnglishLowerEncoding extends CompositeEncoding{
    private final static char SMALL_A = 'a';
    private final static char SMALL_Z = 'z';
    
    private SuccessiveCharsEncoding smallLettersEnglish;
    
    public EnglishLowerEncoding()
    {
	smallLettersEnglish = new SuccessiveCharsEncoding(SMALL_A, SMALL_Z);
	addEncoding(smallLettersEnglish);
    }

}
