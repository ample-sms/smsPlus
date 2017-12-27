package ghareeb.smsplus.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import ghareeb.smsplus.R;
import ghareeb.smsplus.common.StatisticsBundle;
import ghareeb.smsplus.database.SmsPlusDatabaseHelper;
import ghareeb.smsplus.fragments.parents.Fragment_AsyncTask;
import ghareeb.smsplus.helper.SavingsHelper;

public class Fragment_Statistics extends Fragment_AsyncTask<StatisticsBundle>
{
	private TextView savingsTV;
	private TextView sentMessagesTV;
	private TextView receivedMessagesTV;
	private ImageView detailsIV;
	private StatisticsBundle stats = null;
	public static final int EVENT_DESCRIPTION_CLICKED = 2;

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		savingsTV = (TextView) getActivity().findViewById(R.id.totalSavingsTV);
		sentMessagesTV = (TextView) getActivity().findViewById(R.id.totalSentMessagesTV);
		receivedMessagesTV = (TextView) getActivity().findViewById(R.id.totalReceivedMessagesTV);
		detailsIV = (ImageView) getActivity().findViewById(R.id.helpIV);

		detailsIV.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				listener.eventOccurred(EVENT_DESCRIPTION_CLICKED, stats);
			}
		});

		if (stats == null && !isTaskRunning())
			startTask();
		else
			showStats();
	}

	@Override
	protected StatisticsBundle onTaskDoInBackground()
	{
		StatisticsBundle stats = SavingsHelper.calculateSavings(getActivity());
		SmsPlusDatabaseHelper helper = new SmsPlusDatabaseHelper(getActivity());
		int receivedCount = helper.ReceivedMessage_getAllReceivedMessagesCount(false);
		stats.setNumberOfReceivedMessages(receivedCount);

		return stats;
	}

	@Override
	protected int getContainerViewId()
	{
		return R.id.statisticsSV;
	}

	@Override
	protected int getProgressBarViewId()
	{
		return R.id.progressBar;
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.fragment_statistics;
	}

	@Override
	protected void onTaskStarted()
	{
		super.onTaskStarted();
		Toast.makeText(getActivity(), R.string.statistics_processing, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onTaskFinished(StatisticsBundle result)
	{
		super.onTaskFinished(result);

		stats = result;
		showStats();
	}

	private void showStats()
	{
		if (stats != null)
		{
			savingsTV.setText(String.valueOf(stats.getSavedPartsCount()));
			sentMessagesTV.setText(String.valueOf(stats.getNumberOfSentMessages()));
			receivedMessagesTV.setText(String.valueOf(stats.getNumberOfReceivedMessages()));
		}
	}

}
