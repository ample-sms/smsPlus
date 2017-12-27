package ghareeb.smsplus;

import ghareeb.smsplus.common.AppInfo;
import android.content.BroadcastReceiver;

public abstract class Receiver_MessageSendingReceiver extends BroadcastReceiver
{
//	public final static String KEY_IS_MULTIPART = AppInfo.PACKAGE + "isMultipart";
	public final static String KEY_MESSAGE_KEY = AppInfo.PACKAGE + "receivedMessageKey";
	public static final String KEY_RECEPIENT = AppInfo.PACKAGE + "recepient";
	
}
