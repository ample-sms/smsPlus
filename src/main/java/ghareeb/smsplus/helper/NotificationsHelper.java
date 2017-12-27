package ghareeb.smsplus.helper;

import ghareeb.smsplus.Activity_Chat;
import ghareeb.smsplus.Activity_Main;
import ghareeb.smsplus.R;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.Builder;

public class NotificationsHelper
{
	private final static int NOTIFICATION_ID_RECEIVED = 2;

	public static Notification buildNotification(Context cntxt, String title, String text, int icon, PendingIntent pi,
			boolean vibrate, Uri ring, boolean lights, Bitmap largeIcon)
	{
		Builder builder = new Builder(cntxt);
		builder.setContentTitle(title);
		CharSequence textCHS = SmiliesHelper.removeAllSmiliesPtterns(text);
		builder.setContentText(textCHS);
		builder.setTicker(textCHS);
		builder.setContentIntent(pi);
		builder.setAutoCancel(true);
		builder.setSmallIcon(icon);

		if (ring != null)
			builder.setSound(ring);

		if (largeIcon != null)
			builder.setLargeIcon(largeIcon);

		int defaults = 0;

		if (vibrate)
			defaults = Notification.DEFAULT_VIBRATE;

		if (lights)
		{
			defaults |= Notification.DEFAULT_LIGHTS;
			builder.setLights(0xff00ff00, 300, 1000);
		}

		if (defaults != 0)
			builder.setDefaults(defaults);

		return builder.build();

	}

	public static Notification buildNotification(Context cntxt, String title, String text, int icon, PendingIntent pi,
			boolean vibrate, boolean lights, Bitmap largeIcon)
	{
		Uri tone = SharedPreferencesHelper.getUri(cntxt, SharedPreferencesHelper.PREF_NOTIFICATIONS_RING_TONE);

		return buildNotification(cntxt, title, text, icon, pi, vibrate, tone, lights, largeIcon);
	}

	private static ArrayList<ThreadEntity> getThreadsOfMessages(ArrayList<ReceivedMessage> messages, Context cntxt)
	{
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(cntxt);
		ArrayList<ThreadEntity> all = helper.Thread_getAllThreadsFilled(false);
		ArrayList<ThreadEntity> result = new ArrayList<ThreadEntity>();
		ThreadEntity temp;

		for (ReceivedMessage msg : messages)
		{
			for (int i = 0; i < all.size(); i++)
			{
				temp = all.get(i);

				if (msg.getThreadId() == temp.getId())
				{
					if (!result.contains(temp))
					{
						temp.getContact().loadContactBasicContractInformation(cntxt);
						result.add(temp);
					}

					all.remove(i);
					break;
				}
			}
		}

		return result;
	}

	public static void refreshMessageReceivedNotification(Context cntxt, boolean notificationEffects)
	{
		boolean notify = SharedPreferencesHelper.getBoolean(cntxt, SharedPreferencesHelper.PREF_NOTIFICATIONS);

		if (!notify)
			return;

		NotificationManager manager = (NotificationManager) cntxt.getSystemService(Context.NOTIFICATION_SERVICE);
		String title;
		String text;
		Intent intent;
		Bitmap largeIcon = null;

		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(cntxt);
		ArrayList<ReceivedMessage> unseen = helper.ReceivedMessage_getAllUnseenMessages();

		if (unseen.size() == 0)
		{
			manager.cancel(NOTIFICATION_ID_RECEIVED);
			return;
		}
		
		ArrayList<ThreadEntity> threads = getThreadsOfMessages(unseen, cntxt);

		// threads list might be 0 in case of deletion
		if (threads.size() > 0)
		{
			if (unseen.size() == 1)// single message from single contact
			{
				title = threads.get(0).getContact().toString();
				largeIcon = threads.get(0).getContact().getPhoto();
				text = unseen.get(0).getBody();
				intent = new Intent(cntxt, Activity_Chat.class);
				intent.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, threads.get(0).getContact().getNumber().toString());
				intent.putExtra(Activity_Chat.KEY_THREAD_ID, threads.get(0).getId());
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			else
			{
				text = String.format("%d %s", unseen.size(),
						cntxt.getResources().getString(R.string.notification_text_unread_messages));

				if (threads.size() == 1)// multiple messages from single
										// contact
				{
					title = threads.get(0).getContact().toString();
					largeIcon = threads.get(0).getContact().getPhoto();
					intent = new Intent(cntxt, Activity_Chat.class);
					intent.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, threads.get(0).getContact().getNumber().toString());
					intent.putExtra(Activity_Chat.KEY_THREAD_ID, threads.get(0).getId());
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}
				else
				{
					title = cntxt.getResources().getString(R.string.notification_title_unread_messages);
					intent = new Intent(cntxt, Activity_Main.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}
			}

			PendingIntent pi = PendingIntent.getActivity(cntxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			int icon = R.drawable.ic_message_received;

			Notification notification = null;
			
			if(notificationEffects)
				notification = buildNotification(cntxt, title, text, icon, pi, true, 
					true, largeIcon);
			else
				notification = buildNotification(cntxt, title, text, icon, pi, false, null,
						false, largeIcon);
			
			manager.notify(NOTIFICATION_ID_RECEIVED, notification);
		}
	}
}
