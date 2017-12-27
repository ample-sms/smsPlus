package ghareeb.smsplus.asynctasks;

import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.helper.NetworkHelper;
import android.content.Context;
import android.os.AsyncTask;

public class InternetCheckerAsynckTask extends AsyncTask<Void, Void, Boolean>
{
	private TaskListener<Boolean> listener;
	private Context context;
	
	public InternetCheckerAsynckTask(TaskListener<Boolean> listener, Context context)
	{
		this.listener = listener;
		this.context = context;
	}
	
	@Override
	protected void onPreExecute()
	{
		listener.onTaskStarted();
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		listener.onTaskFinished(result);
	}
	
	@Override
	protected Boolean doInBackground(Void... params)
	{
		boolean result = NetworkHelper.hasInternetAccess(context);
		
		return result;
	}
	
}
