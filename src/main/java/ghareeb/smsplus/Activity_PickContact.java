package ghareeb.smsplus;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import ghareeb.smsplus.common.AppInfo;
import ghareeb.smsplus.fragments.Fragment_RegisteredNumbersList;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import ghareeb.smsplus.helper.SharedPreferencesHelper;

public class Activity_PickContact extends AppCompatActivity implements FragmentListener
{
	public static final String KEY_SELECTED_NUMBER = AppInfo.PACKAGE + "selectedNumber";
	private static final String KEY_LOADING = AppInfo.PACKAGE + "keyLoading";
	private static final String KEY_REFRESHING = AppInfo.PACKAGE + "keyRefreshing";
	private boolean loading;
	private boolean refreshing;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_contact);

		ActionBar actionBar = getSupportActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.recepient_title);
		SharedPreferencesHelper.setInt(this, SharedPreferencesHelper.PREF_NUMBER_OF_UNREPORTED_NEW_CONTACTS, 0);


		if (savedInstanceState != null)
		{
			loading = savedInstanceState.getBoolean(KEY_LOADING);
			refreshing = savedInstanceState.getBoolean(KEY_REFRESHING);
			
			if(!loading)
			{
				if(refreshing)
					setListShown(false);
				else
					setListShown(true);
			}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_LOADING, loading);
		outState.putBoolean(KEY_REFRESHING, refreshing);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_pick_contact, menu);
		MenuItem item = menu.findItem(R.id.refresh);

		if (loading || refreshing)
		{
			item.setVisible(false);
		}
		else
		{
			item.setVisible(true);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case R.id.refresh:
				startRefreshing();
				return true;
			case R.id.deviceContacts:
				pickDeviceContact();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void startRefreshing()
	{
		String registeredNumber = SharedPreferencesHelper.getUserPhoneNumber(this);
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment_RegisteredNumbersList fragment = (Fragment_RegisteredNumbersList) fragmentManager
				.findFragmentById(R.id.fragment);
		fragment.startFiltering(registeredNumber);
	}

	private void pickDeviceContact()
	{
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		// Explicitly set the 'type' to 'phone numbers'
		intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
		startActivityForResult(intent, 0);
	}

	private void setListShown(boolean isShown)
	{
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment f = fragmentManager.findFragmentById(R.id.fragment);

		if (f != null)
		{
			Fragment_RegisteredNumbersList fragment = (Fragment_RegisteredNumbersList) f;
			fragment.setListShown(isShown);
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2)
	{
		if (arg1 == Activity.RESULT_OK)
		{
			int permissionCheck = ContextCompat.checkSelfPermission(this,
					Manifest.permission.READ_CONTACTS);

			if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
				handleContactSelected(arg2.getData());
			} else {
				Log.e(Activity_RegistrationProgress.class.getName(), "No permission to read contacts");

			}

		}
	}

	private void handleContactSelected(Uri phoneNumberUri)
	{
		Cursor c = getContentResolver().query(phoneNumberUri, null, null, null, null);
		String contactNumber;

		if (c != null)
		{
			if (c.moveToFirst())
			{
				contactNumber = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				Intent resultIntent = new Intent();
				resultIntent.putExtra(KEY_SELECTED_NUMBER, contactNumber);
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			}

			c.close();
		}

	}

	@Override
	public void eventOccurred(int type, Object obj)
	{
		switch (type)
		{
			case Fragment_RegisteredNumbersList.EVENT_LOADING_STARTED:
				loading = true;
				supportInvalidateOptionsMenu();
				break;
			case Fragment_RegisteredNumbersList.EVENT_LOADING_FINISHED:
				loading = false;
				supportInvalidateOptionsMenu();
				break;
			case Fragment_RegisteredNumbersList.EVENT_REFRESHING_STARTED:
				setListShown(false);
				refreshing = true;
				supportInvalidateOptionsMenu();
				break;
			case Fragment_RegisteredNumbersList.EVENT_REFRESHING_FINISHED:
				setListShown(true);
				refreshing = false;
				supportInvalidateOptionsMenu();

				if (!(Boolean) obj)
					Toast.makeText(this, getString(R.string.internet_fail), Toast.LENGTH_LONG).show();
				break;
			case Fragment_RegisteredNumbersList.EVENT_ITEM_CLICKED:
				String number = (String) obj;

				if (number != null)
				{
					Intent resultIntent = new Intent();
					resultIntent.putExtra(KEY_SELECTED_NUMBER, number);
					setResult(Activity.RESULT_OK, resultIntent);
					finish();
				}
				break;
		}

	}

}
