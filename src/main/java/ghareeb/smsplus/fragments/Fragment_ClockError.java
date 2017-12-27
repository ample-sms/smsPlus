package ghareeb.smsplus.fragments;

import ghareeb.smsplus.Activity_NumberEntering;
import ghareeb.smsplus.R;
import ghareeb.smsplus.fragments.parents.Fragment_AsyncTask;
import ghareeb.smsplus.helper.ClockHelper;
import ghareeb.smsplus.webservice.WebMethodCall;
import ghareeb.smsplus.webservice.WebServiceContract;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Fragment_ClockError extends Fragment_AsyncTask<Long>
{

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		Button settingsB = (Button) getActivity().findViewById(R.id.settingsB);
		Button checkOnlineB = (Button) getActivity().findViewById(R.id.checkOnlineB);

		checkOnlineB.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startTask();
			}
		});
		settingsB.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent i = new Intent(Settings.ACTION_DATE_SETTINGS);

				if (i.resolveActivity(getActivity().getPackageManager()) != null)
				{
					startActivity(i);
					getActivity().finish();
				}
				else
				{
					i = new Intent(Settings.ACTION_SETTINGS);

					if (i.resolveActivity(getActivity().getPackageManager()) != null)
					{
						startActivity(i);
						getActivity().finish();
					}
				}
			}
		});
	}

	@Override
	protected Long onTaskDoInBackground()
	{
		try
		{
			WebMethodCall call = new WebMethodCall(WebServiceContract.WEB_SERVICE_NAMESPACE,
					WebServiceContract.GetUTCTimeMillisWebMethod.WEB_METHOD_NAME, WebServiceContract.WEB_SERVICE_URL);
			call.execute();
			long result = Long.parseLong(call.getResponse().toString());

			return result;
		} catch (Exception e)
		{
			Log.e("Clock Error GetUTCTime WebMethod", e.toString());
		}

		return -1L;
	}

	@Override
	protected void onTaskStarted()
	{
		super.onTaskStarted();
		
		if(getActivity() != null)
			Toast.makeText(getActivity(), getActivity().getString(R.string.date_error_toast_checking_online), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onTaskFinished(Long result)
	{
		super.onTaskFinished(result);
		
		if(getActivity() != null)
		{
			if(result < 0L)
			{
				Toast.makeText(getActivity(), getActivity().getString(R.string.internet_fail), Toast.LENGTH_SHORT).show();
			}
			else
			{
				if(!ClockHelper.validateCurrentTime(result, getActivity()))
					Toast.makeText(getActivity(), getActivity().getString(R.string.date_error_toast_clock_invalid), Toast.LENGTH_SHORT).show();
				else
				{
					Toast.makeText(getActivity(), getActivity().getString(R.string.date_error_toast_clock_valid), Toast.LENGTH_SHORT).show();
					
					Intent i = new Intent(getActivity(), Activity_NumberEntering.class);
					getActivity().startActivity(i);
					getActivity().finish();
				}
			}
		}
	}
	@Override
	protected int getContainerViewId()
	{
		return R.id.clockErrorSV;
	}

	@Override
	protected int getProgressBarViewId()
	{
		return R.id.progressBar;
	}

	@Override
	protected int getLayoutId()
	{
		return R.layout.fragment_clock_error;
	}

}
