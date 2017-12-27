package ghareeb.smsplus;

import ghareeb.smsplus.common.AppInfo;
import android.content.Context;
import android.content.Intent;

public class Receiver_MessagePartDelivered extends Receiver_MessageSendingReceiver
{

	public static final String ACTION_MESSAGE_PART_DELIVERED = AppInfo.PACKAGE + "actionMessagePart";

	@Override
	public void onReceive(Context arg0, Intent arg1)
	{
		if (arg1.getAction().equals(ACTION_MESSAGE_PART_DELIVERED))
		{
			//boolean isMultipart = arg1.getBooleanExtra(KEY_IS_MULTIPART, false);
			long messageKey = arg1.getLongExtra(KEY_MESSAGE_KEY, -1);
			String recepient = arg1.getStringExtra(KEY_RECEPIENT);

			Intent serviceIntent = new Intent(arg0, Service_MessagePartDelivered.class);
			//serviceIntent.putExtra(KEY_IS_MULTIPART, isMultipart);
			serviceIntent.putExtra(KEY_MESSAGE_KEY, messageKey);
			serviceIntent.putExtra(KEY_RECEPIENT, recepient);
			arg0.startService(serviceIntent);
		}
	}

}
