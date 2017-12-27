package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.NonsquentialCharsEncoding;

public class BasicPunctuationEncoding extends NonsquentialCharsEncoding {
    private static final char FULL_STOP = '.';
    private static final char SPACE = ' ';
    private static final char NEW_LINE = '\n';
    
    public BasicPunctuationEncoding()
    {
	super(FULL_STOP, SPACE, NEW_LINE);
    }

}
