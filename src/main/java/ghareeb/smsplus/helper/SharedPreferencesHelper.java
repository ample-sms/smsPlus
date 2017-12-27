package ghareeb.smsplus.helper;

import ghareeb.smsplus.common.PhoneNumber;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper
{
	/* Settings */
	public static final String PREF_CALL_CENTER_CATEGORY = "pref_call_center_category";
	public static final String PREF_CALL_CENTER = "pref_call_center";

	public static final String PREF_NOTIFICATIONS_CATEGORY = "pref_notifications_category";
	public static final String PREF_NOTIFICATIONS_DELIVERY_REPORTS = "pref_delivery_reports";
	public static final String PREF_NOTIFICATIONS = "pref_notifications";
	public static final String PREF_NOTIFICATIONS_NEW_CONTACTS = "pref_notifications_new_contacts";
	public static final String PREF_NOTIFICATIONS_RING_TONE = "pref_notifications_ring_tone";

	/* Non-Settings */
	// To know when to update message and thread lists
	// Gets incremented when messages/threads in DB gets changed in any way
	public static final String PREF_DB_VERSION = "abc";
	public final static String PREF_DELIVERY_REPORT_NOTIFICATION_RECEPIENT = "b";
	public static final String PREF_LAST_VERIFIED_NUMBER = "bbb";
	public static final String PREF_DEVICE_ID_FOR_WEB_METHODS_TOKENS = "bbbbb";
	public static final String PREF_LAST_NEW_CONTACTS_NOTIFICATION_DATE = "a";
	public static final String PREF_NUMBER_OF_UNREPORTED_NEW_CONTACTS = "aa";
	public static final String PREF_LAST_KNOWN_SOFT_KEYBOARD_PORT_HEIGHT_PIXELS = "c";
	public static final String PREF_LAST_KNOWN_SOFT_KEYBOARD_LAND_HEIGHT_PIXELS = "cc";
	public static final String PREF_LAST_USAGE_DATE_TIME_MILLIS = "d";
	public static final String PREF_REGISTERED_NUMBER = "bbbb";
	public static final String PREF_REGISTERED_NUMBER_COUNTRY_CODE = "dd";
	public static final String PREF_SUBSCRIPTION_EXPIRATION_DATE_TIME = "e";
	

	public static Uri getUri(Context context, final String preference)
	{
		String uriString = getString(context, preference);

		if (uriString != null && uriString.length() > 0)
			return Uri.parse(uriString);

		return null;
	}

	public static void setUri(Context context, final String preference, Uri value)
	{
		String uriString = "";
		
		if (value != null)
		{
			uriString = value.toString();
		}
		
		setString(context, preference, uriString);
	}

	public static long getLong(Context context, final String preference, long defaultValue)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		long result = prefs.getLong(preference, defaultValue);

		return result;
	}

	public static void setDate(Context context, final String preference, Date value)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putLong(preference, value.getTime());
		editor.commit();
	}

	public static Date getDate(Context context, final String preference)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		long result = prefs.getLong(preference, 0L);

		return new Date(result);
	}

	public static void setLong(Context context, final String preference, long value)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putLong(preference, value);
		editor.commit();
	}

	public static String getString(Context context, final String preference)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String result = prefs.getString(preference, "");

		return result;
	}

	public static void setString(Context context, final String preference, String value)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString(preference, value);
		editor.commit();
	}

	public static int getInt(Context context, final String preference, int defaultValue)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int result = prefs.getInt(preference, defaultValue);

		return result;
	}

	public static void setInt(Context context, final String preference, int value)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putInt(preference, value);
		editor.commit();
	}

	public static boolean getBoolean(Context context, final String preference)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean result = prefs.getBoolean(preference, false);

		return result;
	}

	public static void setBoolean(Context context, final String preference, boolean value)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putBoolean(preference, value);
		editor.commit();
	}

	public static void incrementDBVersion(Context context)
	{
		int old = getDBVersion(context);
		old++;
		setInt(context, PREF_DB_VERSION, old);
	}

	public static int getDBVersion(Context context)
	{
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		int version = sharedPref.getInt(PREF_DB_VERSION, 0);

		return version;
	}


	public static String getUserPhoneNumber(Context context)
	{
		String natNum = getString(context, PREF_REGISTERED_NUMBER);
		
		if(natNum != null && natNum.length() > 0)
		{
			String countryCode = getString(context, PREF_REGISTERED_NUMBER_COUNTRY_CODE);
			
			if(countryCode != null && countryCode.length() > 0)
			{
				PhoneNumber number = PhoneNumber.parse(natNum, countryCode);
				
				return number.toString();
			}
		}
		
		return "";
	}
}
