package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.CompositeEncoding;
import ghareeb.smsplus.encoding.encodings.base.NonsquentialCharsEncoding;

//Not used anymore
public class SmileyPunctuationsEncoding extends CompositeEncoding
{
	private static final char AT = '@';
	private static final char AND = '&';
	private static final char PLUS = '+';
	private static final char MINUS = '-';
	private static final char TWO_DOTS = ':';
	private static final char LEFT_BRACE = '(';
	private static final char RIGHT_BRACE = ')';
	private static final char LATIN_SEMICOLON = ';';
	private static final char EQUALS = '=';
	private static final char ASTERICS = '*';
	private static final char OR = '|';
	private static final char EXCLAMATION_MARK = '!';
	private static final char DOLLAR_SIGN = '$';
	private static final char APOSTROF = '\'';
	private static final char LESS_THAN = '<';
	private static final char QUOTATION = '"';
	private static final char PERCENT = '%';
	private static final char DEVIDE = '/';

	public SmileyPunctuationsEncoding()
	{
		NonsquentialCharsEncoding encoding = new NonsquentialCharsEncoding(AT, AND, PLUS, MINUS, TWO_DOTS, LEFT_BRACE,
				RIGHT_BRACE, LATIN_SEMICOLON, EQUALS, ASTERICS, OR, EXCLAMATION_MARK, DOLLAR_SIGN, APOSTROF, LESS_THAN,
				QUOTATION, PERCENT, DEVIDE);
		addEncoding(encoding);
	}
}
