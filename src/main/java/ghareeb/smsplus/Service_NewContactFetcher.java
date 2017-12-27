package ghareeb.smsplus;

import ghareeb.smsplus.asynctasks.FilterAsyncTask;
import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.helper.NetworkHelper;
import ghareeb.smsplus.helper.NotificationsHelper;
import ghareeb.smsplus.helper.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * This service is responsible of fetching the numbers of newly joining contacts and notifying the
 * user of them.
 */
public class Service_NewContactFetcher extends Service
{
	//Check once per day
	private final static long INTERVAL_BETWEEN_NEW_CUSTOMERS_NOTIFICATIONS_MILLIS = 24L * 60L * 60L * 1000L;
	private final static int NEW_CONTACTS_NOTIFICATION_ID = 100;
	private final static int WAIT_PERIOD_TILL_CONNECTING_MILLIS = 5 * 1000;
	private int numberOfContactsBeforeFetch;


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		int permissionCheck = ContextCompat.checkSelfPermission(this,
				Manifest.permission.READ_CONTACTS);//Only check proceed if we have permission to do
												   //so. We cannot request a permission from a service.

		if (permissionCheck == PackageManager.PERMISSION_GRANTED &&
				NetworkHelper.isConnectedOrConnecting(this))
		{
			if (NetworkHelper.isConnected(this))
			{
				handleConnectionAvailable();
			}
			else
			{
				Timer connectedWaitTimer = new Timer();
				connectedWaitTimer.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						try
						{
							if (NetworkHelper.isConnected(Service_NewContactFetcher.this))
								handleConnectionAvailable();
							else
								stopSelf();
						} catch (Exception e)
						{
							Log.e(Service_NewContactFetcher.class.getName(), e.getMessage());
						}
					}

				}, WAIT_PERIOD_TILL_CONNECTING_MILLIS);
			}
		}
		else
		{
			stopSelf();
		}

		return Service.START_NOT_STICKY;
	}

	private void handleConnectionAvailable()
	{
		TaskListener<ArrayList<Contact>> listener = new TaskListener<ArrayList<Contact>>() {
			@Override
			public void onTaskStarted() {
				try {
					SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(Service_NewContactFetcher.this);
					numberOfContactsBeforeFetch = helper.Contact_getRegisteredContactsCount();
				} catch (Exception e) {
					numberOfContactsBeforeFetch = -1;
				}
			}

			@Override
			public void onTaskFinished(ArrayList<Contact> result) {
				try {
					if (result != null && numberOfContactsBeforeFetch >= 0) {
						int unreportedNewContacts = SharedPreferencesHelper.getInt(Service_NewContactFetcher.this,
								SharedPreferencesHelper.PREF_NUMBER_OF_UNREPORTED_NEW_CONTACTS, 0);
						int newCount = unreportedNewContacts + (result.size() - numberOfContactsBeforeFetch);

						if (newCount > 0) {
							long lastNotification = SharedPreferencesHelper.getLong(Service_NewContactFetcher.this,
									SharedPreferencesHelper.PREF_LAST_NEW_CONTACTS_NOTIFICATION_DATE, 0L);
							long now = System.currentTimeMillis();

							boolean notify = SharedPreferencesHelper.getBoolean(Service_NewContactFetcher.this,
									SharedPreferencesHelper.PREF_NOTIFICATIONS);
							boolean newContacts = SharedPreferencesHelper.getBoolean(Service_NewContactFetcher.this,
									SharedPreferencesHelper.PREF_NOTIFICATIONS_NEW_CONTACTS);
							if ((now - lastNotification > INTERVAL_BETWEEN_NEW_CUSTOMERS_NOTIFICATIONS_MILLIS) && notify
									&& newContacts) {
								// User should be notified
								showNewContactsNotification(newCount);
								// When the user sees Ample SMS contacts the
								// unreported counter will be reset
							} else {
								// The number of not reported new contacts
								// should be
								// stored
								int prevNewCount = SharedPreferencesHelper.getInt(Service_NewContactFetcher.this,
										SharedPreferencesHelper.PREF_NUMBER_OF_UNREPORTED_NEW_CONTACTS, 0);
								SharedPreferencesHelper.setInt(Service_NewContactFetcher.this,
										SharedPreferencesHelper.PREF_NUMBER_OF_UNREPORTED_NEW_CONTACTS, prevNewCount + newCount);
							}

						}
					}
				} catch (Exception e) {
					Log.e(Service_NewContactFetcher.class.getName(), e.getMessage());
				} finally {
					stopSelf();
				}
			}
		};

		String selfNumber = SharedPreferencesHelper.getUserPhoneNumber(this);

		if (selfNumber != null && selfNumber.length() > 0)
		{
			FilterAsyncTask task = new FilterAsyncTask(listener, selfNumber, false);
			task.execute(this);
		}
	}

	private void showNewContactsNotification(int newCount)
	{
		String title = getString(R.string.notification_new_contacts_title);
		String body = String.format("%s (%d)", getString(R.string.notification_new_contacts_body), newCount);
		Intent i = new Intent(Service_NewContactFetcher.this, Activity_MessageCreation.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra(Activity_MessageCreation.KEY_SHOW_PICK_CONTACT_IMMEDIATELY, true);
		PendingIntent pi = PendingIntent.getActivity(Service_NewContactFetcher.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = NotificationsHelper.buildNotification(Service_NewContactFetcher.this, title, body,
				R.drawable.ic_launcher, pi, true, true, null);
		NotificationManager man = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		man.notify(NEW_CONTACTS_NOTIFICATION_ID, notification);
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

}
