package ghareeb.smsplus;

import ghareeb.smsplus.Component_MessageCreator.OnSmiliesBarVisibilityChanged;
import ghareeb.smsplus.common.AppInfo;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.Message;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.fragments.Dialog_DeleteMessage;
import ghareeb.smsplus.fragments.Dialog_MessageDetails;
import ghareeb.smsplus.fragments.Fragment_ChatList;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import ghareeb.smsplus.guihelpers.MessageSendingActivity;
import ghareeb.smsplus.helper.NotificationsHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * The activity that shows a thread of conversation
 */
public class Activity_Chat extends MessageSendingActivity implements FragmentListener
{
	@Override
	protected Component_MessageCreator getMessageCreator() {
		return creator;
	}

	/**
	 * Broadcast receiver for detecting the status of a sent message
	 */
	class SentMessageStatusReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context arg0, Intent arg1)
		{
			try
			{
				//retrieve the m
				long messageKey = arg1.getLongExtra(Service_MessagePartSent.KEY_MESSAGE_KEY, -1);

				if (messageKey > 0)
				{
					SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(Activity_Chat.this);
					SentMessage message = helper.SentMessage_getSentMessageByKey(messageKey);

					if (message != null && message.getThreadId() == threadId)
					{
						Fragment_ChatList fragment = (Fragment_ChatList) getSupportFragmentManager().findFragmentById(
								R.id.chatFragmentContainer);

						if (fragment != null)
							fragment.replaceSentMessage(message);
					}

				}
			} catch (Exception e)
			{
				Log.e("Activity_Chat", e.getMessage());
			}
		}
	}

	class ReceivedMessageReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			long key = intent.getLongExtra(Service_MessagePartReceived.KEY_RECEIVED_MESSAGE_KEY, -1);

			if (key > 0)
			{
				SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(Activity_Chat.this);
				ReceivedMessage message = helper.ReceivedMessage_getReceivedMessageByKey(key);

				if (message.getThreadId() == threadId)
				{
					message.setSeen(true);
					helper.ReceivedMessage_makeMessageSeen(message.getKey(), true);
					addMessage(message);

				}
			}

		}

	}

	class DeliveredMessageReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			long key = intent.getLongExtra(Service_MessagePartDelivered.KEY_DELIVERED_MESSAGE_KEY, -1);

			if (key > 0)
			{
				SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(Activity_Chat.this);
				SentMessage message = helper.SentMessage_getSentMessageByKey(key);

				if (message != null && message.getThreadId() == threadId)
				{
					Fragment_ChatList fragment = (Fragment_ChatList) getSupportFragmentManager().findFragmentById(
							R.id.chatFragmentContainer);

					if (fragment != null)
						fragment.replaceSentMessage(message);
				}
			}

		}

	}

	/* Attributes */
	private SentMessageStatusReceiver sentMessageReceiver;
	private ReceivedMessageReceiver receivedMessageReceiver;
	private DeliveredMessageReceiver deliveredMessageReceiver;
	private Component_MessageCreator creator;
	private String number;
	private long threadId;
	private int normalCreatorControlHeight;
	private boolean receivedMessageNotificationShown;

	/* Constants */
	public static final String KEY_CONTACT_PHONE_NUMBER = AppInfo.PACKAGE + "contactPhoneNumber";
	public static final String KEY_THREAD_ID = AppInfo.PACKAGE + "threadId";
	public static final String KEY_INITIAL_TEXT = AppInfo.PACKAGE + "initialText";
	public static final String KEY_RECEIVED_MESSAGE_NOTIFIACTION = AppInfo.PACKAGE + "receivedMessageNotification";
	public static final double CREATOR_CONTROL_HEIGHT_MULTIPLICATION_FACTOR = 2.5;
	private static final int EVENT_CALL_PHONE_PERMISSION_REQUESTED = 1;

	/* Overridden Methods */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);

			if (clockValidated)
			{
				setContentView(R.layout.activity_chat);
				PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
				creator = findViewById(R.id.messageCreator);
				ActionBar bar = getSupportActionBar();
				bar.setDisplayHomeAsUpEnabled(true);

				if (savedInstanceState != null)
				{
					number = savedInstanceState.getString(KEY_CONTACT_PHONE_NUMBER);
					threadId = savedInstanceState.getLong(KEY_THREAD_ID);
					receivedMessageNotificationShown = savedInstanceState.getBoolean(KEY_RECEIVED_MESSAGE_NOTIFIACTION);
					initCreator();
				}
				else
				{
					Intent starter = getIntent();
					handleIntent(starter);
				}

				Fragment_ChatList fragment = (Fragment_ChatList) getSupportFragmentManager().findFragmentById(
						R.id.chatFragmentContainer);

				if (fragment == null)
				{
					FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragment = new Fragment_ChatList(number, threadId);
					fragmentTransaction.add(R.id.chatFragmentContainer, fragment);
					fragmentTransaction.commit();
				}
			}

		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

	@Override
	protected void onResume()
	{
		try
		{
			super.onResume();

			if (sentMessageReceiver == null)
				sentMessageReceiver = new SentMessageStatusReceiver();

			if (receivedMessageReceiver == null)
				receivedMessageReceiver = new ReceivedMessageReceiver();

			if (deliveredMessageReceiver == null)
				deliveredMessageReceiver = new DeliveredMessageReceiver();

			registerReceiver(sentMessageReceiver, new IntentFilter(Service_MessagePartSent.ACTION_SENT_MESSAGE_STATUS));
			registerReceiver(receivedMessageReceiver, new IntentFilter(Service_MessagePartReceived.ACTION_MESSAGE_RECEIVED));
			registerReceiver(deliveredMessageReceiver, new IntentFilter(Service_MessagePartDelivered.ACTION_MESSAGE_DELIVERED));

			creator.initializeCounting();

		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

	@Override
	protected void onPause()
	{
		try
		{
			super.onPause();

			if (sentMessageReceiver != null)
				unregisterReceiver(sentMessageReceiver);

			if (receivedMessageReceiver != null)
				unregisterReceiver(receivedMessageReceiver);

			if (deliveredMessageReceiver != null)
				unregisterReceiver(deliveredMessageReceiver);

			creator.stopCounting();
			String draft = creator.getMessageText();

			if (isFinishing() && draft != null && draft.length() > 0)
			{ // Using application context in case the activity is destroyed
				// during process
				saveDraft(draft, threadId, getApplicationContext());
				Toast.makeText(getApplicationContext(),
						getApplicationContext().getResources().getText(R.string.chat_draft_saved), Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

	@Override
	public void onBackPressed()
	{
		if (creator != null && creator.isSmiliesBarShown())
			creator.setSmiliesBarShown(false);
		else
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_chat, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.chatFragmentContainer);

		if (f != null)
		{
			Contact c = ((Fragment_ChatList) f).getContact();

			if (c != null)
			{
				// Handle presses on the action bar items
				switch (item.getItemId())
				{
					case R.id.call:
						int permission = ContextCompat.checkSelfPermission(this,
								Manifest.permission.CALL_PHONE);
						if(permission == PackageManager.PERMISSION_GRANTED){
							c.call(this);
						}else{
							ActivityCompat.requestPermissions(this,
									new String[]{Manifest.permission.CALL_PHONE}, EVENT_CALL_PHONE_PERMISSION_REQUESTED);
						}

						return true;
					case R.id.view:
						if (!c.viewDetails(this))
							Toast.makeText(this, R.string.contact_not_found, Toast.LENGTH_SHORT).show();
						return true;
				}
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private void performCall() {
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.chatFragmentContainer);

		if (f != null) {
			Contact c = ((Fragment_ChatList) f).getContact();
			c.call(this);
		}
	}

	// Called when the user clicks on the delivery report while
	// using this activity, or when returning to this activity after pressing
	// home screen button
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		handleIntent(intent);
		Fragment_ChatList fragment = (Fragment_ChatList) getSupportFragmentManager().findFragmentById(R.id.chatFragmentContainer);

		if (fragment != null)
		{
			fragment.changeContact(number, threadId);
		}

		// SharedPreferencesHelper.setMessagesChanged(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putLong(KEY_THREAD_ID, threadId);
		outState.putString(KEY_CONTACT_PHONE_NUMBER, number);
		outState.putBoolean(KEY_RECEIVED_MESSAGE_NOTIFIACTION, receivedMessageNotificationShown);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if(receivedMessageNotificationShown)
		{
			NotificationsHelper.refreshMessageReceivedNotification(this, false);
			receivedMessageNotificationShown = false;
		}
		
		return super.dispatchTouchEvent(ev);
	}
	/* Implemented Methods */
	@Override
	public void eventOccurred(int type, Object obj)
	{
		switch (type)
		{
			case Fragment_ChatList.EVENT_LOADING_STARTED:
				creator.setUsable(false);
				break;
			case Fragment_ChatList.EVENT_LOADING_FINISHED:
				creator.setUsable(true);
				break;
			case Fragment_ChatList.EVENT_DETAILS_CLICKED:
				handleContextualShowDetails((Message) obj);
				break;
			case Fragment_ChatList.EVENT_LAST_MESSAGE_DELETED:
				Intent intent = new Intent(this, Activity_Main.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
				break;
			case Fragment_ChatList.EVENT_RESEND_CLICKED:
				creator.resendMessage((String) obj);
				break;
			case Fragment_ChatList.EVENT_DELETE_CLICKED:
				Dialog_DeleteMessage deleteDialog = new Dialog_DeleteMessage();
				deleteDialog.show(getSupportFragmentManager(), "DeleteDialog");
				break;
			case Dialog_DeleteMessage.EVENT_YES_PRESSED:
				Fragment_ChatList fragment = (Fragment_ChatList) getSupportFragmentManager().findFragmentById(
						R.id.chatFragmentContainer);
				fragment.confirmDeleteMessage();
				break;

		}
	}

	/* Helper Methods */
	// ////////////////
	/* Intent Handling */
	private void handleIntent(Intent starter)
	{
		String number1 = starter.getStringExtra(KEY_CONTACT_PHONE_NUMBER);
		long threadId1 = starter.getLongExtra(KEY_THREAD_ID, -1);
		String initialText = starter.getStringExtra(KEY_INITIAL_TEXT);

		if (!PhoneNumberUtils.compare(number, number1))// if receiving an intent
														// for a new number
		{
			number = number1;
			threadId = threadId1;

			if (number == null || number.equals(""))
				finish();

			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(this);
			String draft = helper.Thread_getDraft(threadId);

			if (initialText != null && initialText.length() > 0)
			{
				creator.setMessageText(initialText);
			}
			else
				if (draft != null && draft.length() > 0)
				{
					helper.Thread_updateDraft(threadId, "");
					creator.setMessageText(draft);
				}
				else
					creator.setMessageText("");

			initCreator();
		}
	}

	/* Initialization */
	private void initCreator()
	{
		creator.setRecipient(number);
		creator.setThreadId(threadId);
		ViewGroup.LayoutParams params = creator.getLayoutParams();
		normalCreatorControlHeight = params.height;
		creator.setOnSendingListener(new Component_MessageCreator.OnSendingListener()
		{
			@Override
			public void sendRequested(SentMessage message)
			{
				onSendRequested(message);
			}
		});

		creator.setOnSmiliesVisibilityChangedListener(new OnSmiliesBarVisibilityChanged()
		{
			@Override
			public void visibilityChanged(boolean isVisible)
			{
				ViewGroup.LayoutParams params = creator.getLayoutParams();
				boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

				if (isVisible)
				{
					if (isPortrait && lastDetectedPortKeyboardHeight > 0)
						params.height = lastDetectedPortKeyboardHeight + normalCreatorControlHeight;
					else
						if (!isPortrait && lastDetectedLandKeyboardHeight > 0)
							params.height = lastDetectedLandKeyboardHeight + normalCreatorControlHeight;
						else
							params.height = (int) (normalCreatorControlHeight * CREATOR_CONTROL_HEIGHT_MULTIPLICATION_FACTOR);
				}
				else
				{
					params.height = normalCreatorControlHeight;
				}

			}
		});
	}

	/* Draft */
	private void saveDraft(String draft, long threadId, Context context)
	{
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(context);
		helper.Thread_updateDraft(threadId, draft);
	}

	/* Event Handling */
	private void handleContextualShowDetails(Message selected)
	{
		try
		{
			Dialog_MessageDetails dialog = new Dialog_MessageDetails();
			dialog.setMessage(selected);
			dialog.show(getSupportFragmentManager(), "message details");
		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

	private void onSendRequested(SentMessage message)
	{
		addMessage(message);
	}

	private void addMessage(Message message)
	{
		Fragment_ChatList fragment = (Fragment_ChatList) getSupportFragmentManager().findFragmentById(R.id.chatFragmentContainer);

		if (fragment != null)
		{
			fragment.addMessage(message);
			//fragment.startReceivedMessagesNotificationRefreshTimer(RECEIVED_MESSAGE_NOTIFICATION_DISMISSAL_TIMEOUT_MILLIS);
			receivedMessageNotificationShown = true;
		}
	}

	/* Error Logging */
	private void writeErrorLogToFile(Exception e)
	{
		try
		{
			final String DEBUG_FILE_NAME = "error-" + String.valueOf(System.currentTimeMillis()) + ".txt";

			if (isExternalStorageWritable())
			{
				File dir = getDebugDir();
				File file = new File(dir, DEBUG_FILE_NAME);
				FileOutputStream stream = new FileOutputStream(file);
				PrintWriter pw = new PrintWriter(stream);
				pw.println(getErrorString(e));
				e.printStackTrace(pw);
				pw.flush();
				pw.close();
				stream.close();
			}
		} catch (Exception ee)
		{
			Log.e("ErrorLogError", "Cannot write to error log");
		}
	}

	private String getStateAsString()
	{

		return String.format("State\n\r********\n\rcreator:%s\n\rnumber:%s\n\r", creator, number);
	}

	private String getErrorString(Exception e)
	{

		return getStateAsString() +
				"\n\rError Type: " +
				e.getClass().toString() +
				"\n\r********************************\n\rError Message\n\r**********\n\r" +
				e.getMessage() +
				"\n\rError Stack Trace\n\r******************\n\r";
	}

	private boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	private File getDebugDir()
	{
		final String DEBUG_DIR = "SmsPlusDebugDir";
		// Get the directory for the user's public pictures directory.
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DEBUG_DIR);
		if (!file.isDirectory() && !file.mkdirs())
		{
			Log.e("Dir ERROR", "Directory not created");
		}
		return file;
	}

	@Override
	protected int getRootViewId()
	{
		return R.id.chat_activity_root;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			case EVENT_CALL_PHONE_PERMISSION_REQUESTED:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
					performCall();
				}else{
					Log.e(Activity_Chat.class.getName(), "PHONE_CALL permission not granted");
				}

		}
	}
}