package ghareeb.smsplus.asynctasks;

import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.common.StatisticsBundle;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.helper.SavingsHelper;
import android.content.Context;
import android.os.AsyncTask;

public class StatisticsAsyncTask extends AsyncTask<Void, Void, StatisticsBundle>
{
	private TaskListener<StatisticsBundle> listener;
	private Context context;
	private boolean isRunning = false;
	
	public StatisticsAsyncTask(TaskListener<StatisticsBundle>listener, Context context)
	{
		this.listener = listener;
		this.context = context;
	}
	
	@Override
	protected void onPreExecute()
	{
		isRunning = true;
		listener.onTaskStarted();
	}
	
	@Override
	protected StatisticsBundle doInBackground(Void... params)
	{
		
		StatisticsBundle stats = SavingsHelper.calculateSavings(context);
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(context);
		int receivedCount = helper.ReceivedMessage_getAllReceivedMessagesCount(false);
		stats.setNumberOfReceivedMessages(receivedCount);
		
		return stats;
	}
	
	@Override
	protected void onPostExecute(StatisticsBundle result)
	{
		isRunning = false;
		listener.onTaskFinished(result);
	}

	public boolean isRunning()
	{
		return isRunning;
	}
}
