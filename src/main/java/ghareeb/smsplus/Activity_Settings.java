package ghareeb.smsplus;

import ghareeb.smsplus.helper.SharedPreferencesHelper;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;

public class Activity_Settings extends PreferenceActivity
{
//	SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener()
//	{
//
//		@Override
//		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
//		{
//			if (key.equals(SharedPreferencesHelper.PREF_NOTIFICATIONS_RING_TONE))
//			{
//				setRingToneSummary();
//			}
//
//		}
//	};

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	//@SuppressWarnings("deprecation")
	@Override
	protected void onResume()
	{
		super.onResume();
		//getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
		setRingToneSummary();
	}

//	@SuppressWarnings("deprecation")
//	@Override
//	protected void onPause()
//	{
//		super.onPause();
//		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
//	}

	private void setRingToneSummary()
	{
		Uri ringtoneUri = SharedPreferencesHelper.getUri(this, SharedPreferencesHelper.PREF_NOTIFICATIONS_RING_TONE);
		String name;

		if (ringtoneUri != null)
		{
			Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
			name = ringtone.getTitle(this);
		}
		else
			name = getString(R.string.pref_ring_tone_silence);

		@SuppressWarnings("deprecation")
		RingtonePreference pref = (RingtonePreference) findPreference(SharedPreferencesHelper.PREF_NOTIFICATIONS_RING_TONE);
		pref.setSummary(name);
	}
}
