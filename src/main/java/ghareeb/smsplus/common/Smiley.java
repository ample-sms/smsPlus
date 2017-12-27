package ghareeb.smsplus.common;

import ghareeb.smsplus.helper.DimensionsHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

/**
 * Represents a smiley that can be shown within a text
 */
public class Smiley {
    /**
     * the ratio between the icon representing the smiley and the size of the text
     */
    private static final double SMILEY_TO_TEXT_SIZE_RATIO = 1.6;
    /**
     * <code>Spannable.Factory</code> instance used to generate the <code>Span</code> that will
     * contain the smiley and will cover a part of the text.
     */
    private static final Factory SPANNABLE_FACTORY = Factory.getInstance();
    /**
     * a single character that serves as a token to place the <code>Smiley</code> within texts.
     */
    public final char PRIMARY_PATTERN;
    /**
     * resource id of the icon of the smiley
     */
    public final int RESOURCE_ID;
    /**
     * a <code>RegularExpression</code> pattern instance used to locate the primary token and the
     * secondary patterns serving as tokens within a text.
     */
    private Pattern pattern;

    /**
     * Instantiates a new instance of the <code>Smiley</code> class
     *
     * @param resId          the resource id of the icon of the smiley
     * @param primaryPattern a single character that serves as a token to place the <code>Smiley</code>
     *                       within texts.
     * @param otherPatterns  possible multi-character patterns serving as tokens of the smiley within
     *                       a text
     */
    public Smiley(int resId, char primaryPattern, String... otherPatterns) {
        this.RESOURCE_ID = resId;
        this.PRIMARY_PATTERN = primaryPattern;
        //the pattern is comprised of the main and secondary tokens
        pattern = Pattern.compile(generatePatternText(otherPatterns));
    }

    /**
     * Replaces the smiley tokens found within the passed <code>CharSequence</code> with the icon
     * representing the smiley.
     *
     * @param original the <code>CharSequence</code> to process.
     * @param textSize the size of the text measured in pixels.
     * @param context  a <code>Context</code> instance used to fetch the images representing the
     *                 smiley
     * @return the <code>CharSequence</code> after replacing the tokens with smiley icons.
     */
    public CharSequence replacePatternsWithImage(CharSequence original, float textSize, Context context) {
        Spannable spannable = SPANNABLE_FACTORY.newSpannable(original);
        replacePatternsWithImage(spannable, textSize, context);

        return spannable;
    }

    /**
     * Replaces the smiley tokens found within the passed <code>Spannable</code> with the icon
     * representing the smiley.
     * <p/>
     * Reference <a href="http://stackoverflow.com/questions/3341702/displaying-emoticons-in-android">
     * http://stackoverflow.com/questions/3341702/displaying-emoticons-in-android</a>
     *
     * @param spannable the <code>Spannable</code> to process.
     * @param textSize  the size of the text measured in pixels.
     * @param context   a <code>Context</code> instance used to fetch the images representing the
     *                  smiley
     */
    public void replacePatternsWithImage(Spannable spannable, float textSize, Context context) {
        Matcher matcher = pattern.matcher(spannable);
        final Bitmap icon = getBitmap(context, textSize);
        boolean set;

        while (matcher.find()) {
            set = true;

            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end())
                    spannable.removeSpan(span);
                else {
                    set = false;
                    break;
                }
            }

            if (set) {
                spannable.setSpan(new ImageSpan(context, icon), matcher.start(),
                        matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * Replaces all occurrences of the smiley tokens (primary or secondary) within the passed
     * argument with an empty string.
     *
     * @param original the <code>CharSequence</code> to process.
     * @return a copy of the passed <code>CharSequence</code> free of the smiley's tokens.
     */
    public CharSequence removePatterns(CharSequence original) {
        return pattern.matcher(original).replaceAll("");
    }

    /**
     * Creates a <code>String</code> pattern that can be used to instantiate a <code>Pattern</code>
     * instance which holds the primary and secondary smiley tokens.
     *
     * @param secondaryPatterns the secondary smiley tokens
     * @return the <code>String</code> that combines all the smiley's tokens in a single pattern.
     */
    private String generatePatternText(String[]secondaryPatterns) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("(%s)", Pattern.quote(String.valueOf(PRIMARY_PATTERN))));

        if (secondaryPatterns.length > 0) {
            builder.append("|");

            for (int i = 0; i < secondaryPatterns.length; i++) {
                builder.append(String.format("(%s)", Pattern.quote(secondaryPatterns[i])));

                if (i < secondaryPatterns.length - 1)
                    builder.append("|");
            }
        }

        return builder.toString();
    }


    /**
     * Retrieves the <code>Bitmap</code> representing the smiley's icon, and scales it if necessary.
     * @param context the <code>Context</code> instance used to fetch the <code>Bitmap</code> resource.
     * @param textSize the text size that will influence the size of the icon after scaling. Could
     *                 be a negative value if no scaling is desired.
     * @return the (possibly scaled) <code>Bitmap</code> that represents the smiley's icon.
     */
    private Bitmap getBitmap(Context context, float textSize) {
        if (textSize > 0) {
            int size = DimensionsHelper.getDipsFromPixel(context, (int) (textSize * SMILEY_TO_TEXT_SIZE_RATIO));// MDPI
            Bitmap smileyImg = BitmapFactory.decodeResource(context.getResources(), RESOURCE_ID);

            return Bitmap.createScaledBitmap(smileyImg, size, size, true);
        } else {
            return BitmapFactory.decodeResource(context.getResources(), RESOURCE_ID);
        }
    }
}
