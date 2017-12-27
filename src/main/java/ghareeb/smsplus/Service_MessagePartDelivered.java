package ghareeb.smsplus;

import ghareeb.smsplus.common.AppInfo;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.helper.NotificationsHelper;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Service_MessagePartDelivered extends IntentService
{
	public static final int NOTIFICATION_CODE_DELIVERED = 1;
	public final static String ACTION_MESSAGE_DELIVERED = AppInfo.PACKAGE + "MessageDelivered";
	public final static String KEY_DELIVERED_MESSAGE_KEY = AppInfo.PACKAGE + "DeliveredMessageKey";

	public Service_MessagePartDelivered()
	{
		super("MessagePartDeliveredService");
	}

	@Override
	protected void onHandleIntent(Intent arg0)
	{
		long messageKey = arg0.getLongExtra(Receiver_MessagePartDelivered.KEY_MESSAGE_KEY, -1);

		if (messageKey < 0)
			return;

		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(this);
		helper.SentMessage_setPartDelivered(messageKey, true);


		if (helper.SentMessage_isMessageDelivered(messageKey, true))
		{
			SentMessage message = helper.SentMessage_getSentMessageByKey(messageKey);
			Intent i = new Intent(ACTION_MESSAGE_DELIVERED);
			i.putExtra(KEY_DELIVERED_MESSAGE_KEY, messageKey);
			sendBroadcast(i);
			
			String recepeint = arg0.getStringExtra(Receiver_MessagePartDelivered.KEY_RECEPIENT);
			messageDeliveredNotification(recepeint, message.getThreadId());
		}

	}

	private void messageDeliveredNotification(String recepeint, long threadId)
	{
		boolean notify = SharedPreferencesHelper.getBoolean(this, SharedPreferencesHelper.PREF_NOTIFICATIONS);
		boolean delivery = SharedPreferencesHelper.getBoolean(this, SharedPreferencesHelper.PREF_NOTIFICATIONS_DELIVERY_REPORTS);
		SharedPreferencesHelper.setString(this, SharedPreferencesHelper.PREF_DELIVERY_REPORT_NOTIFICATION_RECEPIENT, recepeint);
		
		if (!notify || !delivery)
			return;

		String title = getString(R.string.notification_title_delivered);
		Contact temp = new Contact(recepeint, this);
		temp.loadContactBasicContractInformation(this);
		String text = String.format("%s %s", getString(R.string.notification_text_delivered), temp.toString());
		Intent i = new Intent(this, Activity_Chat.class);
		i.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, recepeint);
		i.putExtra(Activity_Chat.KEY_THREAD_ID, threadId);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = NotificationsHelper.buildNotification(this, title, text, R.drawable.ic_message_delivered, pi,
				true, true, temp.getPhoto());
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_CODE_DELIVERED, notification);

	}

}
