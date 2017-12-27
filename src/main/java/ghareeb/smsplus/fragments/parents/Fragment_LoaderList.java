package ghareeb.smsplus.fragments.parents;

import ghareeb.smsplus.fragments.helper.FragmentListener;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

public abstract class Fragment_LoaderList<T> extends ListFragment
{
	class LoaderTask extends AsyncTask<Void, Void, ArrayList<T>>
	{
		private boolean isRunning = false;

		@Override
		protected void onPreExecute()
		{
			isRunning = true;
			try
			{
				onLoaderTaskStarted();
			} catch (Exception e)
			{

			}
		}

		@Override
		protected ArrayList<T> doInBackground(Void... params)
		{
			try
			{
				return onLoaderTaskDoInBackground();
			} catch (Exception e)
			{
			}

			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<T> result)
		{
			try
			{
				onLoaderTaskFinished(result);
			} catch (Exception e)
			{
			}

			isRunning = false;
		}
	}

	public static final int EVENT_LOADING_STARTED = 0;
	public static final int EVENT_LOADING_FINISHED = 1;
	public static final int EVENT_ITEM_CLICKED = 2;

	protected ArrayAdapter<T> adapter;
	protected ArrayList<T> items;
	protected LoaderTask loaderTask;
	protected FragmentListener listener;

	protected void onLoaderTaskStarted()
	{
		setListAdapter(null);

		if (items != null)
		{
			items.clear();
		}

		setListShown(false);
		listener.eventOccurred(EVENT_LOADING_STARTED, null);
	}

	protected abstract ArrayList<T> onLoaderTaskDoInBackground();

	protected void onLoaderTaskFinished(ArrayList<T> result)
	{
		items = result;
		adapter = instantiateArrayAdapter();
		setListAdapter(adapter);
		setListShown(true);
		listener.eventOccurred(EVENT_LOADING_FINISHED, null);
	}

	protected abstract void onItemClicked(int index);

	protected abstract CharSequence getEmptyText();

	// Must return null when instantiation is not possible
	protected abstract ArrayAdapter<T> instantiateArrayAdapter();

	protected boolean isLoading()
	{
		return loaderTask != null && loaderTask.isRunning;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		CharSequence emptyText = getEmptyText();
		
		if(emptyText != null && emptyText.length() > 0)
			setEmptyText(getEmptyText());
		
		getListView().setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				onItemClicked(arg2);
			}
		});

		try
		{
			listener = (FragmentListener) getActivity();
		} catch (ClassCastException e)
		{
			throw new ClassCastException(getActivity().toString() + " must implement FragmentListener");
		}
		
		//Otherwise multiple rotations (on first run) may spawn multiple loader threads
		if(items == null && !isLoading())
			reload();
	}

	public void reload()
	{
		loaderTask = new LoaderTask();
		loaderTask.execute();
	}
}
