package ghareeb.smsplus.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.webservice.TokenizedWebMethodCall;
import ghareeb.smsplus.webservice.WebServiceContract;

public class FilterAsyncTask extends AsyncTask<Context, Void, ArrayList<Contact>>
{
	private final TaskListener<ArrayList<Contact>> LISTENER;
	private final String SELF_NUMBER;
	private final boolean FILL_CONTACT;

	public FilterAsyncTask(TaskListener<ArrayList<Contact>> listener, String selfNumber, boolean fillContacts)
	{
		this.LISTENER = listener;
		this.SELF_NUMBER = selfNumber;
		this.FILL_CONTACT = fillContacts;
	}

	@Override
	protected void onPreExecute()
	{
		LISTENER.onTaskStarted();
	}

	@Override
	protected ArrayList<Contact> doInBackground(Context... params)
	{
		Context context = params[0];

		String list = generateCommaList(Contact.getAllPhoneNumbersInPhonebook(context));
		try
		{
			TokenizedWebMethodCall call = new TokenizedWebMethodCall(
					WebServiceContract.FilterRegisteredWebMethod.WEB_METHOD_NAME, context);
			call.addStringParameter(WebServiceContract.FilterRegisteredWebMethod.PARAM1_STRING_LIST_OF_NUMBERS, list);
			call.execute();
			Object response = call.getResponse();
			String result = response.toString();
			SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(params[0]);
			String[] split = generateList(result);

			if (split.length == 1 && !PhoneNumberUtils.isWellFormedSmsAddress(split[0]))
				return new ArrayList<>();

			helper.Contact_makeAllContactsUnregistered();
			ArrayList<Contact> registeredContacts = helper.Contact_makeNumbersRegistered(split);

			if (FILL_CONTACT)
			{
				for (Contact c : registeredContacts)
				{
					c.loadContactBasicContractInformation(params[0]);
				}

				Collections.sort(registeredContacts);
			}

			helper.close();

			return registeredContacts;
		} catch (Exception e)
		{
			Log.e(FilterAsyncTask.class.getName(), e.getMessage());
		}

		return null;
	}

	private String generateCommaList(ArrayList<Contact> contacts)
	{
		StringBuilder builder = new StringBuilder();
		String current;

		for (int i = 0; i < contacts.size(); i++)
		{
			current = contacts.get(i).getNumber().toString();

			if (!PhoneNumberUtils.compare(current, SELF_NUMBER))
			{
				builder.append(current);

				if (i < contacts.size() - 1)
					builder.append(";");
			}
		}

		return builder.toString();
	}

	private String[] generateList(String list)
	{
		return list.split(";");
	}

	@Override
	protected void onPostExecute(ArrayList<Contact> result)
	{
		LISTENER.onTaskFinished(result);
	}
}
