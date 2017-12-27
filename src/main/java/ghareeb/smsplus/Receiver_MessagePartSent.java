package ghareeb.smsplus;

import ghareeb.smsplus.common.AppInfo;
import android.content.Context;
import android.content.Intent;

public class Receiver_MessagePartSent extends Receiver_MessageSendingReceiver
{
	public static final String ACTION_MESSAGE_PART_SENT = AppInfo.PACKAGE + "actionMessagePartSent";

	@Override
	public void onReceive(Context arg0, Intent arg1)
	{
		if (arg1.getAction().equalsIgnoreCase(ACTION_MESSAGE_PART_SENT))
		{
			Intent serviceIntent = new Intent(arg0, Service_MessagePartSent.class);
			// serviceIntent.putExtra(KEY_IS_MULTIPART,
			// arg1.getBooleanExtra(KEY_IS_MULTIPART, false));
			serviceIntent.putExtra(KEY_MESSAGE_KEY, arg1.getLongExtra(KEY_MESSAGE_KEY, -1));
			serviceIntent.putExtra(KEY_RECEPIENT, arg1.getStringExtra(KEY_RECEPIENT));
			serviceIntent.putExtra(Service_MessagePartSent.KEY_RESULT_CODE, getResultCode());
			arg0.startService(serviceIntent);
		}
	}
}
