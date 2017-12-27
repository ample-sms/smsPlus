package ghareeb.smsplus.asynctasks;

import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.webservice.TokenizedWebMethodCall;
import ghareeb.smsplus.webservice.WebServiceContract;
import android.content.Context;
import android.os.AsyncTask;

public class RegistrationAsyncTask extends AsyncTask<String, Void, Integer>
{
	private final TaskListener<Integer> listener;
	private Context context;

	public RegistrationAsyncTask(TaskListener<Integer> listener, Context context)
	{
		this.listener = listener;
		this.context = context;
	}

	@Override
	protected void onPreExecute()
	{
		if (listener != null)
			listener.onTaskStarted();
	}

	@Override
	protected Integer doInBackground(String... params)
	{
		try
		{
			TokenizedWebMethodCall call = new TokenizedWebMethodCall(WebServiceContract.RegisterWebMethod.WEB_METHOD_NAME,
					context);
			call.addStringParameter(WebServiceContract.RegisterWebMethod.PARAM1_STRING_PHONE_NUMBER, params[0]);
			call.execute();
			context = null;
			Object response = call.getResponse();
			int result = Integer.parseInt(response.toString());

			return result;
		} catch (Exception e)
		{
			context = null;
			return -1;
		}
		
	}

	@Override
	protected void onPostExecute(Integer result)
	{
		if (listener != null)
			listener.onTaskFinished(result);
	}
}
