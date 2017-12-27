package ghareeb.smsplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Receiver_MessagePartReceived extends BroadcastReceiver
{
	private byte[] pdu;
	
	@Override
	public void onReceive(Context arg0, Intent arg1)
	{
		readMessage(arg1);
		Intent i = new Intent(arg0, Service_MessagePartReceived.class);
		i.putExtra(Service_MessagePartReceived.KEY_PDU, pdu);
		arg0.startService(i);
		abortBroadcast();
	}

	public void readMessage(Intent intent)
	{
		Bundle bundle = intent.getExtras();
		if (bundle != null)
		{
			// ---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			
			if(pdus != null && pdus.length > 0)
				pdu = (byte[])pdus[0];
			else
				Log.e("Receiver_MessagePartReceived", "PDUS is empty");
			
		}
	}
}
