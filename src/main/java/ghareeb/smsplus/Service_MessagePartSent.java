package ghareeb.smsplus;

import ghareeb.smsplus.common.AppInfo;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.helper.NotificationsHelper;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

public class Service_MessagePartSent extends IntentService
{
	public static final String KEY_RESULT_CODE = AppInfo.PACKAGE + "SentResultCode";

	public static final String ACTION_SENT_MESSAGE_STATUS = AppInfo.PACKAGE + "Action_SentMessageStatus";
	public static final String KEY_MESSAGE_KEY = AppInfo.PACKAGE + "MessageKey";
	private static final int NOTIFICATION_ID_FAILED = 0;

	public Service_MessagePartSent()
	{
		super("MessagePartSentService");
	}

	@Override
	protected void onHandleIntent(Intent arg0)
	{
		long messageKey = arg0.getLongExtra(Receiver_MessagePartSent.KEY_MESSAGE_KEY, -1);
		String recepient = arg0.getStringExtra(Receiver_MessagePartSent.KEY_RECEPIENT);
		int resultCode = arg0.getIntExtra(KEY_RESULT_CODE, Activity.RESULT_OK + 2000);

		if (messageKey < 0)
			return;

		if (resultCode != Activity.RESULT_OK)
		{
			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(this);
			
			if (helper.SentMessage_setErrorDetected(messageKey, true))
			{
				sendStatusBroadCast(messageKey);
				showFailedNotification(messageKey, recepient);
			}//else this message might be deleted
		}
		else
		{
			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(this);
			helper.SentMessage_setPartSent(messageKey, true);

			if (helper.SentMessage_isMessageSent(messageKey, true))
			{
				sendStatusBroadCast(messageKey);
			}
		}

	}

	private void sendStatusBroadCast(long messageKey)
	{
		Intent i = new Intent(ACTION_SENT_MESSAGE_STATUS);
		i.putExtra(KEY_MESSAGE_KEY, messageKey);
		sendBroadcast(i);
	}

	private SentMessage getSentMessage(long messageKey)
	{
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(this);
		SentMessage message = helper.SentMessage_getSentMessageByKey(messageKey);

		return message;
	}

	private void showFailedNotification(long messageKey, String recepient)
	{
		boolean notify = SharedPreferencesHelper.getBoolean(this, SharedPreferencesHelper.PREF_NOTIFICATIONS);

		if (!notify)
			return;

		SentMessage message = getSentMessage(messageKey);
		Intent i = new Intent(this, Activity_Chat.class);
		i.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, recepient);
		i.putExtra(Activity_Chat.KEY_THREAD_ID, message.getThreadId());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		int icon = R.drawable.ic_send_failed;
		String title = getResources().getString(R.string.notification_title_failed);
		Contact temp = new Contact(recepient, this);
		temp.loadContactBasicContractInformation(this);
		String body = String.format("%s %s", getResources().getString(R.string.notification_text_failed), temp.toString());

		Notification notification = NotificationsHelper.buildNotification(this, title, body, icon, pi, true, true, temp.getPhoto());
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID_FAILED, notification);
	}
}
