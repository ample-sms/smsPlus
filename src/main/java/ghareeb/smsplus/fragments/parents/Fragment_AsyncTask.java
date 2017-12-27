package ghareeb.smsplus.fragments.parents;

import ghareeb.smsplus.fragments.helper.FragmentListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class Fragment_AsyncTask<T> extends Fragment
{
	class Task extends AsyncTask<Void, Void, T>
	{
		private boolean isRunning = false;

		@Override
		protected void onPreExecute()
		{
			isRunning = true;
			onTaskStarted();
		}

		@Override
		protected void onPostExecute(T result)
		{
			onTaskFinished(result);
		}

		@Override
		protected T doInBackground(Void... params)
		{
			isRunning = false;
			return onTaskDoInBackground();
		}

	}

	public static final int EVENT_TASK_STARTED = 0;
	public static final int EVENT_TASK_FINISHED = 1;
	private Task task;
	protected FragmentListener listener;

	protected void showProgress(boolean isShown)
	{
		if (getActivity() != null)
		{
			View container = getActivity().findViewById(getContainerViewId());
			View progressBar = getActivity().findViewById(getProgressBarViewId());

			if (isShown)
			{
				container.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
			else
			{
				progressBar.setVisibility(View.GONE);
				container.setVisibility(View.VISIBLE);
			}
		}
	}

	protected void onTaskStarted()
	{
		showProgress(true);
		listener.eventOccurred(EVENT_TASK_STARTED, null);
	}

	protected void onTaskFinished(T result)
	{
		showProgress(false);
		listener.eventOccurred(EVENT_TASK_FINISHED, result);
	}

	protected abstract T onTaskDoInBackground();

	protected abstract int getContainerViewId();

	protected abstract int getProgressBarViewId();

	protected abstract int getLayoutId();
	
	protected boolean isTaskRunning()
	{
		if (task != null && task.isRunning)
			return true;

		return false;
	}
	
	protected void startTask()
	{
		task = new Task();
		task.execute();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		
		try
		{
			listener = (FragmentListener)getActivity();
		}
		catch(Exception e)
		{
			throw new ClassCastException(getActivity().toString() + " must implement FragmentListener");
		}
		
		if(isTaskRunning())
			showProgress(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(getLayoutId(), container, false);
	}
}
