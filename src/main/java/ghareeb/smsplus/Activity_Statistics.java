package ghareeb.smsplus;

import java.text.SimpleDateFormat;

import ghareeb.smsplus.common.StatisticsBundle;
import ghareeb.smsplus.fragments.Dialog_SavingsDetails;
import ghareeb.smsplus.fragments.Fragment_Statistics;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class Activity_Statistics extends ActionBarActivity implements FragmentListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);

		ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setTitle(R.string.action_bar_title_statistics);
	}

	@Override
	public void eventOccurred(int type, Object obj)
	{
		switch (type)
		{
			case Fragment_Statistics.EVENT_DESCRIPTION_CLICKED:
				showDescriptionDialog((StatisticsBundle) obj);
				break;
		}
	}

	private void showDescriptionDialog(StatisticsBundle stats)
	{
		try
		{
			SimpleDateFormat formatter = (SimpleDateFormat) SimpleDateFormat.getDateInstance();

			String message = String.format(getString(R.string.savings_description), stats.getNumberOfSentMessageActualParts(),
					formatter.format(stats.getDateOfFirstSentMessage()), stats.getNumberOfOrdinarySentMessageParts(),
					stats.getSavedPartsCount());
			Dialog_SavingsDetails dialog = new Dialog_SavingsDetails(message);
			dialog.show(getSupportFragmentManager(), "savings details");
		} catch (Exception e)
		{
		}
	}

}
