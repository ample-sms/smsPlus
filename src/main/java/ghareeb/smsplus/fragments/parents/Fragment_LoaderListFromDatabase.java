package ghareeb.smsplus.fragments.parents;

import ghareeb.smsplus.helper.SharedPreferencesHelper;

public abstract class Fragment_LoaderListFromDatabase<T> extends Fragment_LoaderList<T>
{
	private int lastDBVersion = -1;
	private boolean isFirstRun = true;

	@Override
	public void onResume()
	{
		super.onResume();
		int newVersion = lastDBVersion;
		
		if (getActivity() != null)
		{
			 newVersion = SharedPreferencesHelper.getDBVersion(getActivity());
		}

		if (isFirstRun)
			isFirstRun = false;
		else
		{
			if (newVersion > lastDBVersion && !isLoading())
			{
				reload();
			}
		}

		lastDBVersion = newVersion;
	}
}
