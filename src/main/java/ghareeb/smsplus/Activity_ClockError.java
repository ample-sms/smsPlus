package ghareeb.smsplus;

import ghareeb.smsplus.fragments.helper.FragmentListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class Activity_ClockError extends ActionBarActivity implements FragmentListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clock_error);
	}

	@Override
	public void eventOccurred(int type, Object obj)
	{
		
	}
}
