package ghareeb.smsplus.helper;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ghareeb.smsplus.Activity_ClockError;


import android.content.Context;
import android.content.Intent;

public class ClockHelper
{
	private static final int ALLOWED_BACKWARD_CLOCK_DIFFERENCE_HOURS = 1;
	
	private final static int START_YEAR = 2015;
	private final static int START_MONTH = Calendar.FEBRUARY;
	private final static int START_DAY = 1;
	
	public static long getMillisecondsOfStartDateTime()
	{
		final GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.clear();
		cal.set(START_YEAR, START_MONTH, START_DAY);
		long result = cal.getTimeInMillis();
		
		return result;
	}
	
	public static boolean validateClock(final Context context)
	{
		final long defaultRefTime = Math.max(System.currentTimeMillis(), getMillisecondsOfStartDateTime());
		final long recordedTime = SharedPreferencesHelper.getLong(context,
				SharedPreferencesHelper.PREF_LAST_USAGE_DATE_TIME_MILLIS, defaultRefTime);		
		boolean validationResult = validateCurrentTime(recordedTime, context);
		
		if(!validationResult)
		{
			Intent i = new Intent(context, Activity_ClockError.class);
			context.startActivity(i);
		}
		
		return validationResult;	
	}

	public static boolean validateCurrentTime(long referenceTime, Context context)
	{
		final long currentTime = System.currentTimeMillis();
		final long differenceMillis = currentTime - referenceTime;

		if (differenceMillis < 0)
		{
			final long differenceHours =(-differenceMillis)/(1000L * 60L * 60L);

			if (differenceHours > ALLOWED_BACKWARD_CLOCK_DIFFERENCE_HOURS)// One hour is permitted
			{
				return false;
			}
		}
		
		storeCurrentTime(context);
		
		return true;
	}
	
	
	private static void storeCurrentTime(final Context context)
	{
		new Thread()
		{
			@Override
			public void run()
			{
				long currentTime = System.currentTimeMillis();
				SharedPreferencesHelper.setLong(context, SharedPreferencesHelper.PREF_LAST_USAGE_DATE_TIME_MILLIS, currentTime);
			}
		}.start();
	}
}
