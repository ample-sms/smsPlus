package ghareeb.smsplus;

import ghareeb.smsplus.common.AppInfo;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.encoding.Decoder;
import ghareeb.smsplus.encoding.IDecoder;
import ghareeb.smsplus.encoding.InvalidCodeException;
import ghareeb.smsplus.helper.NotificationsHelper;
import ghareeb.smsplus.transmission.receiving.Receiver;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class Service_MessagePartReceived extends IntentService
{
	public final static String KEY_PDU = AppInfo.PACKAGE + "PDU";
	public final static String ACTION_MESSAGE_RECEIVED = AppInfo.PACKAGE + "MessageReceived";
	public final static String KEY_RECEIVED_MESSAGE_KEY = AppInfo.PACKAGE + "ReceivedMessageKey";


	private byte[] partPDU;
	

	public Service_MessagePartReceived()
	{
		super("ServiceMessagePartReceived");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		IDecoder decoder = new Decoder(new Receiver());
		readIntent(intent);

		try
		{
			if (decoder.receiveMessagePart(partPDU, this))// all parts are
															// received
			{
				ReceivedMessage message;
				message = decoder.getReceivedMessage();
				onMessageReceived(message);
			}
		} catch (InvalidCodeException e)
		{
			Log.e("Service_MessagePartReceived", "Invalide Code Exception");
			e.printStackTrace();
		}
	}

	private void onMessageReceived(ReceivedMessage message)
	{
		Intent i = new Intent(ACTION_MESSAGE_RECEIVED);
		i.putExtra(KEY_RECEIVED_MESSAGE_KEY, message.getKey());
		sendBroadcast(i);
		NotificationsHelper.refreshMessageReceivedNotification(this, true);
	}

	private void readIntent(Intent intent)
	{
		partPDU = intent.getByteArrayExtra(KEY_PDU);
	}
	
}
