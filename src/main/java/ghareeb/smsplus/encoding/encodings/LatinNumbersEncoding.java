package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.SuccessiveCharsEncoding;

public class LatinNumbersEncoding extends SuccessiveCharsEncoding{

    private static final char ZERO = '0';
    private static final char NINE = '9';
    
    
    public LatinNumbersEncoding()
    {
	super(ZERO, NINE);
    }

}
