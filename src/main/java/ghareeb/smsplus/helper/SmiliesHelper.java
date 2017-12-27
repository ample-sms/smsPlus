package ghareeb.smsplus.helper;

import android.content.Context;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.widget.EditText;
import ghareeb.smsplus.R;
import ghareeb.smsplus.common.Smiley;

public class SmiliesHelper
{
	public static final Smiley SMILE = new Smiley(R.drawable.smiley_smile, '\u2300', ":)", ":-)");
	public static final Smiley FROWN = new Smiley(R.drawable.smiley_frown, '\u2301', ":(", ":-(", ":-[", ":[", ":c", ":-c",
			":-C", ":C");
	public static final Smiley YUCK = new Smiley(R.drawable.smiley_yuck, '\u2302', ":p", ":-p", ":P", ":-P");
	public static final Smiley WINK = new Smiley(R.drawable.smiley_wink, '\u2303', ";)", ";-)");
	public static final Smiley KISS = new Smiley(R.drawable.smiley_kiss, '\u2304', ":-*");
	public static final Smiley SLANT = new Smiley(R.drawable.smiley_slant, '\u2305', ":/", ":-/", ":-\\", ":-\\");
	public static final Smiley CRYING = new Smiley(R.drawable.smiley_crying, '\u2306', ":'(");
	public static final Smiley LAUGH_OPEN_EYES = new Smiley(R.drawable.smiley_laugh_open_eyes, '\u2307', ":D", ":-D", "xD",
			"x-D", "XD", "X-D");
	public static final Smiley THUMBS_UP = new Smiley(R.drawable.smiley_thumb_up, '\u2308');
	public static final Smiley GASP = new Smiley(R.drawable.smiley_gusp, '\u2309', ":o", ":-o", ":O", ":-O", "=o", "=-o", "=O",
			"=-O", "o_O");
	public static final Smiley AMBIVALENT = new Smiley(R.drawable.smiley_ambivalent, '\u230a', ":|", ":-|");
	public static final Smiley HEART = new Smiley(R.drawable.smiley_heart, '\u230b', "<3");
	public static final Smiley CONFUSED = new Smiley(R.drawable.smiley_confused, '\u230c', ":$", ":-$", ":Q", ":-Q", ":s", ":-s",
			":S", ":-S");
	public static final Smiley SLEEPY = new Smiley(R.drawable.smiley_sleepy, '\u230d');
	public static final Smiley ANGRY = new Smiley(R.drawable.smiley_angry, '\u230e', ":@", ":-@");
	public static final Smiley PARTY = new Smiley(R.drawable.smiley_party, '\u230f');
	public static final Smiley COOL = new Smiley(R.drawable.smiley_cool, '\u2310', "B)", "B-)");
	public static final Smiley SICK = new Smiley(R.drawable.smiley_sick, '\u2311', "X(", "X-(", "x(", "x-(");
	public static final Smiley DEVIL = new Smiley(R.drawable.smiley_devil, '\u2312');
	public static final Smiley SQUINT = new Smiley(R.drawable.smiley_squint, '\u2313');

	private static Smiley[] all =
	{ SMILE, FROWN, YUCK, WINK, KISS, THUMBS_UP, CRYING, SLANT, HEART, LAUGH_OPEN_EYES, GASP, AMBIVALENT, CONFUSED,
			SLEEPY, ANGRY, PARTY, COOL, SICK, DEVIL, SQUINT };
	private static final Factory spannableFactory = Factory.getInstance();

	public static CharSequence replaceAllPatternsWithImages(CharSequence original, float textSize, Context context)
	{
		if (original == null || original.length() == 0)
			return "";

		Spannable spannable = spannableFactory.newSpannable(original);

		for (int i = 0; i < all.length; i++)
		{
			all[i].replacePatternsWithImage(spannable, textSize, context);
		}

		return spannable;
	}

	public static void addSmileyAtPosition(EditText et, int position, Smiley smiley, Context context)
	{
		if (et.length() >= position)
		{
			String text = et.getText().toString();
			StringBuilder builder = new StringBuilder(text);
			builder.insert(position, smiley.PRIMARY_PATTERN);
			CharSequence result = replaceAllPatternsWithImages(builder.toString(), et.getTextSize(), context);
			et.setText(result);
			et.setSelection(position + 1);
		}
	}

	public static Smiley[] getAllSmilies()
	{
		return all;
	}

	public static CharSequence removeAllSmiliesPtterns(CharSequence original)
	{
		CharSequence result = original;

		for (Smiley current : all)
		{
			result = current.removePatterns(result);
		}

		return result;
	}

	public static int getCountOfSmiliesPatterns(CharSequence text)
	{
		char current;
		int counter = 0;
		
		for(int i = 0; i < text.length(); i++ )
		{
			current = text.charAt(i);
			
			for(int j = 0; j < all.length; j++)
			{
				if(all[j].PRIMARY_PATTERN == current)
				{
					counter++;
					break;
				}
			}
		}
		
		return counter;
	}
}
