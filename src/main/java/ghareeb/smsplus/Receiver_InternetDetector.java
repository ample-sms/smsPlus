package ghareeb.smsplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver_InternetDetector extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent i = new Intent(context, Service_NewContactFetcher.class);
		context.startService(i);	
	}

}
