package ghareeb.smsplus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;
import ghareeb.smsplus.fragments.Dialog_DeleteThread;
import ghareeb.smsplus.fragments.Fragment_ThreadsList;
import ghareeb.smsplus.fragments.helper.FragmentListener;

public class Activity_Main extends AppCompatActivity implements FragmentListener
{
	class ReceivedMessageReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			long key = intent.getLongExtra(Service_MessagePartReceived.KEY_RECEIVED_MESSAGE_KEY, -1);

			if (key > 0)
			{
				SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(Activity_Main.this);
				ReceivedMessage message = helper.ReceivedMessage_getReceivedMessageByKey(key);

				if (message != null)
					((Fragment_ThreadsList) getSupportFragmentManager().findFragmentById(R.id.threadsFragment))
							.handleReceivedMessage(message);
			}
		}
	}

	private ReceivedMessageReceiver receivedMessageReceiver;
	private int indexOfContactToBeCalled = -1;
	private static final int EVENT_CALL_PERMISSION_REQUIRED = 1;

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (receivedMessageReceiver == null)
			receivedMessageReceiver = new ReceivedMessageReceiver();

		registerReceiver(receivedMessageReceiver, new IntentFilter(Service_MessagePartReceived.ACTION_MESSAGE_RECEIVED));
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (receivedMessageReceiver != null)
			unregisterReceiver(receivedMessageReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_bar_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case R.id.createNewMessage:
				createNewMessage();
				return true;
			case R.id.about:
				Intent i = new Intent(this, Activity_About.class);
				startActivity(i);
				return true;
			case R.id.settings:
				Intent ii = new Intent(this, Activity_Settings.class);
				startActivity(ii);
				return true;
			case R.id.statistics:
				Intent iii = new Intent(this, Activity_Statistics.class);
				startActivity(iii);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void createNewMessage()
	{
		Intent createSMSIntent = new Intent(this, Activity_MessageCreation.class);
		createSMSIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(createSMSIntent);
	}

	@Override
	public void eventOccurred(int type, Object obj)
	{
		switch (type)
		{
			case Fragment_ThreadsList.EVENT_ITEM_CLICKED:
				ThreadEntity thread = (ThreadEntity) obj;
				Intent chatIntent = new Intent(Activity_Main.this, Activity_Chat.class);
				chatIntent.putExtra(Activity_Chat.KEY_CONTACT_PHONE_NUMBER, thread.getContact().getNumber().toString());
				chatIntent.putExtra(Activity_Chat.KEY_THREAD_ID, thread.getId());
				startActivity(chatIntent);
				break;
			case Fragment_ThreadsList.EVENT_DELETE_CLICKED:
				Dialog_DeleteThread deleteDialog = new Dialog_DeleteThread();
				deleteDialog.show(getSupportFragmentManager(), "DeleteDialog");
				break;
			case Fragment_ThreadsList.EVENT_CALL_CLICKED:
				int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

				if(permission == PackageManager.PERMISSION_GRANTED) {
					Fragment_ThreadsList fragment = (Fragment_ThreadsList) getSupportFragmentManager().findFragmentById(
							R.id.threadsFragment);
					fragment.performCall((int)obj);
				} else {
					this.indexOfContactToBeCalled = (int)obj;
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.CALL_PHONE}, EVENT_CALL_PERMISSION_REQUIRED);
				}
				break;
			case Dialog_DeleteThread.EVENT_YES_PRESSED:
				Fragment_ThreadsList fragment = (Fragment_ThreadsList) getSupportFragmentManager().findFragmentById(
						R.id.threadsFragment);
				fragment.confirmDeleteThread();
				break;
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode){
			case EVENT_CALL_PERMISSION_REQUIRED:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Fragment_ThreadsList fragment = (Fragment_ThreadsList) getSupportFragmentManager().findFragmentById(
							R.id.threadsFragment);
					fragment.performCall(this.indexOfContactToBeCalled);
				} else {
					Log.e(Activity_Main.class.getName(), "CALL_PHONE permission not granted");
				}
		}
	}
}
