package ghareeb.smsplus;

import ghareeb.smsplus.fragments.Fragment_EnteringUserPhoneNumber;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import ghareeb.smsplus.guihelpers.SoftKeyboardMonitoringActivity;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;

public class Activity_NumberEntering extends SoftKeyboardMonitoringActivity implements FragmentListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (clockValidated)
		{
			if (isRegistered())
			{
				gotoMainActivity();
			}
			else
			{
				setContentView(R.layout.activity_number_entering);
				SharedPreferencesHelper.setUri(this, SharedPreferencesHelper.PREF_NOTIFICATIONS_RING_TONE,
						android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
			}
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2)
	{
		super.onActivityResult(arg0, arg1, arg2);

		if (arg1 == Activity.RESULT_OK)
			gotoMainActivity();
	}

	private void register(String number, String countryCode)
	{
		if ((number.length() >= 4 + 5) && PhoneNumberUtils.isWellFormedSmsAddress(number))
		{
			startRegistration(number, countryCode);
		}
		else
		{
			String message = getString(R.string.register_number_missing_invalid);
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	private void startRegistration(String fullNumber, String countryCode)
	{
		Intent i = new Intent(this, Activity_RegistrationProgress.class);
		i.putExtra(Activity_RegistrationProgress.KEY_PHONE_NUMBER, fullNumber);
		i.putExtra(Activity_RegistrationProgress.KEY_COUNTRY_CODE, countryCode);
		startActivityForResult(i, 0);
	}

	private boolean isRegistered()
	{
		String result = SharedPreferencesHelper.getUserPhoneNumber(this);

		return !result.equals("");

		// SharedPreferencesHelper.setString(this,
		// SharedPreferencesHelper.PREF_REGISTERED_NUMBER, "+963999488197");
		// return true;
	}

	private void gotoMainActivity()
	{
		Intent i = new Intent(this, Activity_Main.class);
		startActivity(i);
		finish();
	}

	@Override
	public void eventOccurred(int type, Object obj)
	{
		switch (type)
		{
			case Fragment_EnteringUserPhoneNumber.EVENT_REGISTER_BUTTON_CLICKED:
				Fragment_EnteringUserPhoneNumber fragment = (Fragment_EnteringUserPhoneNumber) getSupportFragmentManager().findFragmentById(R.id.numberEnteringFragmentContainer);
				String number = fragment.getPhoneNumber();
				String countryCode = fragment.getCountryCode();
				register(number, countryCode);
				break;
		}

	}

	@Override
	protected int getRootViewId()
	{
		return R.id.numberEnteringActivityRoot;
	}
}
