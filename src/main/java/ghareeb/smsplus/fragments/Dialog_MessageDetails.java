package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import ghareeb.smsplus.database.entities.Message;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.SentMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class Dialog_MessageDetails extends DialogFragment
{

	private static final String KEY_MESSAGE = "message";
	private Message message;

	public void setMessage(Message message)
	{
		this.message = message;
	}

	private String createSentMessageDetails(SentMessage message)
	{
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		StringBuilder builder = new StringBuilder();
		String to = getString(R.string.chat_message_details_to);
		builder.append(String.format("%s %s\n", to, message.getRecepient()));
		builder.append(String.format("%s %s\n", getString(R.string.chat_message_details_sent),
				format.format(message.getSendDateTime())));
		builder.append(String.format("%s ", getString(R.string.chat_message_details_delivery_report_title)));

		if (message.isHasFailed())
			builder.append(getString(R.string.chat_message_details_delivery_report_failed));
		else
			if (message.isDelivered())
			{
				Date deliveryDateTime = message.getDeliveryDateTime();

				if (deliveryDateTime != null)
				{
					builder.append(String.format("%s (%s)", getString(R.string.chat_message_details_delivery_report_delivered),
							format.format(deliveryDateTime)));
				}
				else
				{
					builder.append(String.format("%s", getString(R.string.chat_message_details_delivery_report_delivered)));
				}
			}
			else
				builder.append(getString(R.string.chat_message_details_delivery_report_requested));

		return builder.toString();
	}

	private String createReceivedMessageDetails(ReceivedMessage message)
	{
		SimpleDateFormat formatReceived = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		SimpleDateFormat formatSent = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		formatSent.setTimeZone(TimeZone.getTimeZone("UTC"));//Trying to reproduce time as shown in the sending device
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%s %s\n", getString(R.string.chat_message_details_from), message.getSender()));
		builder.append(String.format("%s %s\n", getString(R.string.chat_message_details_received),
				formatReceived.format(message.getReceiveDateTime())));
		builder.append(String.format("%s %s", getString(R.string.chat_message_details_sent),
				formatSent.format(message.getSendDateTime())));

		return builder.toString();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		try
		{
			if (savedInstanceState != null)
			{
				message = savedInstanceState.getParcelable(KEY_MESSAGE);
			}
			// Use the Builder class for convenient dialog construction
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			String messageS = "";

			if (message instanceof ReceivedMessage)
				messageS = createReceivedMessageDetails((ReceivedMessage) message);
			else
				messageS = createSentMessageDetails((SentMessage) message);

			builder.setTitle(getString(R.string.chat_message_details_title));
			builder.setMessage(messageS);

			return builder.create();
		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}

		return null;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0)
	{
		try
		{
			super.onSaveInstanceState(arg0);
			arg0.putParcelable(KEY_MESSAGE, message);
		} catch (Exception e)
		{
			writeErrorLogToFile(e);
		}
	}

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
		String result = String.format("State\n\r********\n\rmessage: %s\n\r", message);

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