package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import ghareeb.smsplus.Service_MessagePartDelivered;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.Message;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;
import ghareeb.smsplus.fragments.parents.Fragment_LoaderListFromDatabase;
import ghareeb.smsplus.helper.NotificationsHelper;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import ghareeb.smsplus.helper.SmiliesHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class Fragment_ChatList extends Fragment_LoaderListFromDatabase<Message>
{
	class ChatAdapter extends ArrayAdapter<Message>
	{
		private Context context;
		private int layoutResourceId;
		private ArrayList<Message> data = null;
		private final static char CHECK = '✓';

		public ChatAdapter(Context context, int layoutResourceId, ArrayList<Message> data)
		{
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (data != null)
			{
				View row = convertView;
				MessageViewsHolder holder = null;

				if (row == null)
				{
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					row = inflater.inflate(layoutResourceId, parent, false);

					holder = new MessageViewsHolder();
					holder.messageBody = (TextView) row.findViewById(R.id.messageTV);
					holder.dateStatus = (TextView) row.findViewById(R.id.dateStatusTV);
					holder.layout = (LinearLayout) row.findViewById(R.id.chatMessageLL);
					holder.deliveryStatus = (TextView)row.findViewById(R.id.deliveryStatusTV);
					row.setTag(holder);
				}
				else
				{
					holder = (MessageViewsHolder) row.getTag();
				}

				Message message = data.get(position);
				fillView(holder, message);

				return row;
			}

			return null;
		}

		public SentMessage getSentMessageByMessageKey(long messageKey)
		{
			if (data != null)
			{
				for (Message m : data)
				{
					if (m instanceof SentMessage)
					{
						if (((SentMessage) m).getKey() == messageKey)
							return (SentMessage) m;
					}
				}
			}
			return null;
		}

		public boolean replaceSentMessage(SentMessage newMessage)
		{
			SentMessage old = getSentMessageByMessageKey(newMessage.getKey());

			if (old == null)// if the message belongs to another chat thread
				return false;

			newMessage.copyTo(old);
			notifyDataSetChanged();
			return true;
		}

		private void fillView(MessageViewsHolder holder, Message message)
		{
			String body = message.getBody();

			if (isRTL(body))
			{
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageBody.getLayoutParams();
				params.gravity = Gravity.RIGHT;
				holder.messageBody.setLayoutParams(params);
				holder.messageBody.setGravity(Gravity.RIGHT);
			}
			else
			{
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageBody.getLayoutParams();
				params.gravity = Gravity.LEFT;
				holder.messageBody.setLayoutParams(params);
				holder.messageBody.setGravity(Gravity.LEFT);
			}

			holder.messageBody.setText(SmiliesHelper.replaceAllPatternsWithImages(body, holder.messageBody.getTextSize(),
					getActivity()));
			String status = null;
			SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
			int color;

			if (message instanceof SentMessage)
			{
				holder.layout.setBackgroundResource(R.drawable.blue_chat);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.layout.getLayoutParams();
				params.gravity = Gravity.RIGHT;
				holder.layout.setLayoutParams(params);

				if (((SentMessage) message).isHasFailed())
				{
					holder.deliveryStatus.setText("");
					status = getResources().getString(R.string.chat_failed);
					color = getResources().getColor(R.color.color_red);
				}
				else
				{
					if (!((SentMessage) message).isSent())
					{
						holder.deliveryStatus.setText("");
						status = getResources().getString(R.string.message_creation_sending);
						color = getResources().getColor(R.color.color_blue);
					}
					else
					{
						status = formatter.format(((SentMessage) message).getSendDateTime());

						if (((SentMessage) message).isDelivered())
						{
							holder.deliveryStatus.setText(String.format("%c%c", CHECK, CHECK));
						}
						else
						{
							holder.deliveryStatus.setText(String.format("%c", CHECK));
						}

						color = getResources().getColor(R.color.color_black);
					}
				}
			}
			else
			{
				holder.layout.setBackgroundResource(R.drawable.orange_chat);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.layout.getLayoutParams();
				params.gravity = Gravity.LEFT;
				holder.layout.setLayoutParams(params);
				holder.deliveryStatus.setText("");

				status = formatter.format(((ReceivedMessage) message).getReceiveDateTime());
				color = getResources().getColor(R.color.color_black);
			}

			holder.dateStatus.setText(status);
			holder.dateStatus.setTextColor(color);
		}

		private boolean isRTL(String text)
		{
			if (text == null || text.equals(""))
				return false;

			char[] chars = text.toCharArray();
			byte dir;

			for (char c : chars)
			{
				dir = Character.getDirectionality(c);

				if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT || dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
						|| dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING
						|| dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE)
					return true;
			}

			return false;
		}

		@Override
		public int getCount()
		{
			if (data != null)
				return data.size();

			return 0;
		}
	}

	static class MessageViewsHolder
	{
		TextView messageBody;
		TextView dateStatus;
		TextView deliveryStatus;
		LinearLayout layout;
		
	}

	/* Events */
	public static final int EVENT_LAST_MESSAGE_DELETED = 3;
	public static final int EVENT_RESEND_CLICKED = 4;
	public static final int EVENT_DETAILS_CLICKED = 5;
	public static final int EVENT_DELETE_CLICKED = 6;

	/* Attributes */
	// private Timer notificationDismissalTimer;
	private String otherNumber;
	private ThreadEntity thread;
	private int lastSelectedPosition = -1;

	/* Constructors */
	public Fragment_ChatList(String otherNumber, long threadId)
	{
		this.otherNumber = otherNumber;
		Contact c = new Contact(otherNumber, getActivity());
		thread = new ThreadEntity();
		thread.setContact(c);
		thread.setId(threadId);
	}

	public Fragment_ChatList()
	{

	}

	/* Inherited Methods */
	@Override
	protected ArrayList<Message> onLoaderTaskDoInBackground()
	{
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
		thread.getContact().loadContactBasicContractInformation(getActivity());
		helper.Thread_loadMessagesOfThread(thread);
		makeMessagesSeen(helper);
		helper.close();

		try
		{
			NotificationsHelper.refreshMessageReceivedNotification(getActivity(), false);

			String numberOfDeliveryReport = SharedPreferencesHelper.getString(getActivity(),
					SharedPreferencesHelper.PREF_DELIVERY_REPORT_NOTIFICATION_RECEPIENT);

			if (!numberOfDeliveryReport.equals(""))
			{
				if (PhoneNumberUtils.compare(numberOfDeliveryReport, otherNumber))
				{
					NotificationManager manager = (NotificationManager) getActivity().getSystemService(
							Context.NOTIFICATION_SERVICE);
					manager.cancel(Service_MessagePartDelivered.NOTIFICATION_CODE_DELIVERED);
				}
			}

			return thread.getMessages();
		} catch (Exception e)
		{
		}
		return null;
	}

	@Override
	protected void onLoaderTaskFinished(ArrayList<Message> result)
	{
		super.onLoaderTaskFinished(result);

		if (adapter != null)
		{
			int selPos = adapter.getCount();

			if (selPos == 0)
				listener.eventOccurred(EVENT_LAST_MESSAGE_DELETED, null);
			else
			{
				getListView().setSelection(selPos);
				ActionBar gbf_rsn = ((ActionBarActivity) getActivity()).getSupportActionBar();
				gbf_rsn.setTitle(getActionBarTitle());
			}
		}
	}

	@Override
	protected void onItemClicked(int index)
	{
		// Nothing happens
	}

	@Override
	protected CharSequence getEmptyText()
	{
		// Activity should close when all messages are deleted
		return "";
	}

	@Override
	protected ArrayAdapter<Message> instantiateArrayAdapter()
	{
		return new ChatAdapter(getActivity(), R.layout.list_item_chat_message, items);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		try
		{
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater inflater = getActivity().getMenuInflater();
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			Message selected = adapter.getItem(info.position);

			if (selected instanceof SentMessage && ((SentMessage) selected).isHasFailed())
				inflater.inflate(R.menu.context_menu_chat_item_failed, menu);
			else
				inflater.inflate(R.menu.context_menu_chat_item, menu);
		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		try
		{
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			lastSelectedPosition = info.position;

			switch (item.getItemId())
			{
				case R.id.copy:
					handleContextualCopy(info.position);
					return true;
				case R.id.delete:
					listener.eventOccurred(EVENT_DELETE_CLICKED, info.position);
					return true;
				case R.id.showDetails:
					handleContextualShowDetails(info.position);
					return true;
				case R.id.resend:
					handleContextualResend(info.position);
					return true;
			}

		} catch (Exception e)
		{
			writeErrorLogToFile(e);

		}

		return super.onContextItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		ListView lv = getListView();
		registerForContextMenu(lv);
		lv.setDivider(null);
		lv.setDividerHeight(0);
		lv.setStackFromBottom(true);
		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

		ActionBar gbf_rsn = ((ActionBarActivity) getActivity()).getSupportActionBar();
		gbf_rsn.setTitle(getActionBarTitle());
	}

	/* Public Methods */
	public void addMessage(Message message)
	{
		if (adapter != null)
		{
			adapter.add(message);
			int pos = adapter.getPosition(message);
			getListView().setSelection(pos);
		}
	}

	public void replaceSentMessage(SentMessage message)
	{
		if (adapter != null)
			((ChatAdapter) adapter).replaceSentMessage(message);
	}

	public void changeContact(String number, long threadId)
	{
		if (otherNumber == null || !otherNumber.equals(number))
		{
			this.otherNumber = number;
			Contact contact = new Contact(otherNumber, getActivity());
			thread = new ThreadEntity();
			thread.setContact(contact);
			thread.setId(threadId);
		}

		if (!isLoading())
			reload();
	}

	public void confirmDeleteMessage()
	{
		if (lastSelectedPosition >= 0)
		{
			performMessageDelete(lastSelectedPosition);

			if (items.size() == 0)
			{
				deleteThread();
				listener.eventOccurred(EVENT_LAST_MESSAGE_DELETED, null);
			}
			else
			{
				if (adapter != null)
				{
					adapter.notifyDataSetChanged();
				}
			}

			lastSelectedPosition = -1;
		}

	}

	public Contact getContact()
	{
		if (thread != null)
			return thread.getContact();

		return null;
	}

	// public void startReceivedMessagesNotificationRefreshTimer(int
	// delayMillis)
	// {
	// if (notificationDismissalTimer != null)
	// notificationDismissalTimer.cancel();
	//
	// notificationDismissalTimer = new Timer("Notifications Dismissal Timer");
	// notificationDismissalTimer.schedule(new TimerTask()
	// {
	// @Override
	// public void run()
	// {
	// if (getActivity() != null)
	// {
	// getActivity().runOnUiThread(new Runnable()
	// {
	// @Override
	// public void run()
	// {
	// try
	// {
	// NotificationsHelper.refreshMessageReceivedNotification(getActivity(),
	// false);
	// } catch (Exception e)
	// {
	// }
	// }
	// });
	// }
	//
	// }
	// }, delayMillis);
	// }

	/* Helper Methods */
	// ////////////////

	/* Loading Methods */

	private void makeMessagesSeen(SmsPlusDatabaseHelper helper)
	{
		helper.ReceivedMessage_makeMessagesOfThreadSeen(thread.getId());
	}

	private String getActionBarTitle()
	{
		/* Author Ghareeb Falazi */
		StringBuilder builder = new StringBuilder();

		if (thread != null && thread.getContact() != null)
		{
			String name = thread.getContact().getName();
			String fin = "";

			if (name != null && !name.equals(""))
			{
				builder.append(name);
				builder.append(" <");
				fin = ">";
			}

			builder.append(otherNumber);
			builder.append(fin);

		}

		return builder.toString();
	}

	private void deleteThread()
	{
		if (thread != null)
		{
			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
			helper.Thread_delete(thread);
		}
	}

	/* Context Menu Related */
	@SuppressWarnings("deprecation")
	private void handleContextualCopy(int position)
	{
		if (adapter != null)
		{
			Message selected = adapter.getItem(position);

			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB)
			{
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(
						Context.CLIPBOARD_SERVICE);
				clipboard.setText(selected.getBody());
			}
			else
			{
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(
						Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData.newPlainText("text label", selected.getBody());
				clipboard.setPrimaryClip(clip);
			}
		}
	}

	private void performMessageDelete(int position)
	{
		if (adapter != null)
		{
			Message selected = adapter.getItem(position);
			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());

			if (selected instanceof ReceivedMessage)
			{
				helper.ReceivedMessage_deleteMessage(selected.getKey());
			}
			else
			{
				helper.SentMessage_deleteMessage(selected.getKey());
			}

			adapter.remove(selected);
		}
	}

	private synchronized void handleContextualResend(int position)
	{
		if (adapter != null)
		{
			String message = adapter.getItem(position).getBody();
			performMessageDelete(position);
			listener.eventOccurred(EVENT_RESEND_CLICKED, message);
		}
	}

	private void handleContextualShowDetails(int position)
	{
		try
		{
			Message selected = adapter.getItem(position);

			if (selected instanceof SentMessage)
				((SentMessage) selected).setRecepient(otherNumber);
			else
				((ReceivedMessage) selected).setSender(otherNumber);

			listener.eventOccurred(EVENT_DETAILS_CLICKED, selected);
		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

	/* Error Logging Methods */
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
		String result = String.format(
				"State\n\r********\n\rcontact:%s\n\radapter:%s\n\rlistView:%s\n\rloader:%s\n\rnumber:%s\n\r", thread, adapter,
				getListView(), loaderTask, otherNumber);

		return result;
	}

	private String getErrorString(Exception e)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getStateAsString());
		builder.append("\n\rError Type: ");
		builder.append(e.getClass().toString());
		builder.append("\n\r********************************\n\rError Message\n\r**********\n\r");
		builder.append(e.getMessage());
		builder.append("\n\rError Stack Trace\n\r******************\n\r");

		return builder.toString();
	}

	private boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state))
		{
			return true;
		}
		return false;
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

}
