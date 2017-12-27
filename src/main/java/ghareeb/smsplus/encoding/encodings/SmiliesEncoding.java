package ghareeb.smsplus.encoding.encodings;

import ghareeb.smsplus.encoding.encodings.base.CompositeEncoding;
import ghareeb.smsplus.encoding.encodings.base.NonsquentialCharsEncoding;
import ghareeb.smsplus.helper.SmiliesHelper;

public class SmiliesEncoding extends CompositeEncoding
{
	//18 chars
	public SmiliesEncoding()
	{
		NonsquentialCharsEncoding innerEncoding = new NonsquentialCharsEncoding(
				SmiliesHelper.AMBIVALENT.PRIMARY_PATTERN,
				SmiliesHelper.ANGRY.PRIMARY_PATTERN,
				SmiliesHelper.CONFUSED.PRIMARY_PATTERN,
				SmiliesHelper.COOL.PRIMARY_PATTERN,
				SmiliesHelper.CRYING.PRIMARY_PATTERN,
				SmiliesHelper.FROWN.PRIMARY_PATTERN,
				SmiliesHelper.GASP.PRIMARY_PATTERN,
				SmiliesHelper.HEART.PRIMARY_PATTERN,
				SmiliesHelper.SLEEPY.PRIMARY_PATTERN,
				SmiliesHelper.KISS.PRIMARY_PATTERN,
				SmiliesHelper.THUMBS_UP.PRIMARY_PATTERN,
				SmiliesHelper.LAUGH_OPEN_EYES.PRIMARY_PATTERN,
				SmiliesHelper.PARTY.PRIMARY_PATTERN,
				SmiliesHelper.SICK.PRIMARY_PATTERN,
				SmiliesHelper.SLANT.PRIMARY_PATTERN,
				SmiliesHelper.SMILE.PRIMARY_PATTERN,
				SmiliesHelper.WINK.PRIMARY_PATTERN,
				SmiliesHelper.YUCK.PRIMARY_PATTERN,
				SmiliesHelper.DEVIL.PRIMARY_PATTERN,
				SmiliesHelper.SQUINT.PRIMARY_PATTERN
				);
		
		addEncoding(innerEncoding);
		
	}

}
