package ghareeb.smsplus.common;

import ghareeb.smsplus.helper.SharedPreferencesHelper;
import android.content.Context;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;

/**
 * Represents the phone number of a contact or for the user of the program
 */
public class PhoneNumber
{
	/**
	 * The country code part of the phone number e.g. 963, 49, .. etc.
	 */
	private int countryCode;
	/**
	 * The national (local) part of the phone number
	 */
	private long nationalNumber;
	/**
	 * The country code of the user of the app which is stored as a SharedPreference
	 * used as a singleton for fast retrieval
	 */
	private static String userCountryCode = null;

	/**
	 * Getter method
	 * @return the country code part of the phone number e.g. 963, 49, .. etc.
	 */
	public String getCountryCode()
	{
		return String.valueOf(countryCode);
	}

	/**
	 * Getter method
	 * @return the national (local) part of the phone number.
	 */
	public String getNationalNumber()
	{
		return String.valueOf(nationalNumber);
	}

	private PhoneNumber()
	{

	}

	/**
	 * Gets the current user's country code either from the static variable, or from a SharedPreference
	 * @param context the context used to retrieve the country code from the SharedPreference.
	 * @return the country code of the user of the app e.g. SY
	 */
	private static String getUserCountryCode(Context context)
	{
		if (userCountryCode == null || userCountryCode.length() == 0)
			userCountryCode = SharedPreferencesHelper.getString(context,
					SharedPreferencesHelper.PREF_REGISTERED_NUMBER_COUNTRY_CODE);

		return userCountryCode;
	}

	/**
	 * Parses the national part of a phone number plus the country code of the country which the
	 * national number belongs to into an instance of PhoneNumber
	 * @param nationalPart the national part of the phone number.
	 * @param countryCode the country code of the country which the national number belongs to.
	 * @return a new <Code>PhoneNumber</Code> instance or  <code>null</code> if the parsing was unsuccessful.
	 */
	public static PhoneNumber parse(String nationalPart, String countryCode)
	{
		PhoneNumberUtil util = PhoneNumberUtil.getInstance();
		try
		{
			com.google.i18n.phonenumbers.Phonenumber.PhoneNumber number = util.parse(nationalPart,
					countryCode);
			PhoneNumber result = new PhoneNumber();
			result.countryCode = number.getCountryCode();
			result.nationalNumber = number.getNationalNumber();
			
			return result;
		} catch (NumberParseException e)
		{
			Log.e("Error", "Failed parsing of national phone number plus country code.");
			return null;
		}
	}

	/**
	 * Parses the national part of a phone number, and tries to get the current user's country code
	 * which is needed in the parsing process.
	 * @param phoneNumber the national part of the number to be parsed.
	 * @param context the <code>Context</code> used to get the user's country code
	 * @return a new <Code>PhoneNumber</Code> instance or  <code>null</code> if the parsing was
	 * unsuccessful.
	 */
	public static PhoneNumber parse(String phoneNumber, Context context)
	{
		PhoneNumberUtil util = PhoneNumberUtil.getInstance();
		com.google.i18n.phonenumbers.Phonenumber.PhoneNumber number;

		if (phoneNumber != null && phoneNumber.length() >= 2)
		{
			if (phoneNumber.charAt(0) == '0' && phoneNumber.charAt(1) == '0')
				phoneNumber = phoneNumber.replaceFirst("00", "+");

			try
			{
				if (phoneNumber.charAt(0) != '+')
					number = util.parse(phoneNumber, getUserCountryCode(context));
				else
					number = util.parse(phoneNumber, "");

				PhoneNumber result = new PhoneNumber();
				result.countryCode = number.getCountryCode();
				result.nationalNumber = number.getNationalNumber();

				return result;

			} catch (NumberParseException e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Returns the standard E164 representation of the <code>PhoneNumber</code> instance.
	 * @return the standard E164 representation of the <code>PhoneNumber</code> instance, or an
	 * empty string if generating the standard form was not possible.
	 */
	@Override
	public String toString()
	{
		String temp = String.format("+%d%d", countryCode, nationalNumber);

		try
		{
			PhoneNumberUtil utils = PhoneNumberUtil.getInstance();
			com.google.i18n.phonenumbers.Phonenumber.PhoneNumber number = utils.parse(temp, null);

			return utils.format(number, PhoneNumberFormat.E164);
		} catch (NumberParseException e)
		{
			return "";
		}

	}

	/**
	 * Compares two instances of <code>PhoneNumber</code> for equality.
	 * @param o the <code>PhoneNumber</code> instance to compare the local instance to.
	 * @return true if the two instances are equal in terms of <code>nationalNumber</code> and
	 * <code>countryCode</code>, false otherwise.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof PhoneNumber)
		{
			PhoneNumber another = (PhoneNumber) o;

			return nationalNumber == another.nationalNumber && countryCode == another.countryCode;
		}

		return false;
	}
}
