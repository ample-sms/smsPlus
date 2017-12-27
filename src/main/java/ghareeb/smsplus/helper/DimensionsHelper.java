package ghareeb.smsplus.helper;

import android.content.Context;
import android.util.TypedValue;

public class DimensionsHelper
{
	public static int getPixelsFromDips(Context cntxt, float dips)
	{
		return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, cntxt.getResources().getDisplayMetrics()) + 0.5f);
	}
	
	public static int getDipsFromPixel(Context cntxt, float pixels)
	{
		return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, cntxt.getResources().getDisplayMetrics()) + 0.5f);
	}
}
