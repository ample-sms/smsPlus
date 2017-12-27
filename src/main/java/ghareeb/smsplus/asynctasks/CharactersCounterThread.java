package ghareeb.smsplus.asynctasks;

import ghareeb.smsplus.asynctasks.helper.ProgressiveTaskListener;
import ghareeb.smsplus.common.CountInformation;
import ghareeb.smsplus.common.IndexedCountInformation;
import ghareeb.smsplus.encoding.Encoder;
import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.transmission.sending.DataMessageSender;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class CharactersCounterThread extends Thread
{
	private Handler threadHandler;
	private final Handler uiHandler;
	private ProgressiveTaskListener<Void, IndexedCountInformation> listener;
	private Encoder encoder;
	private static final int WHAT_TASK_STARTED = 0;
	private static final int WHAT_TASK_FINISHED = 1;
	private static final int WHAT_TASK_PROGRESS = 2;

	private boolean hasStarted = false;

	public boolean hasStarted()
	{
		return hasStarted;
	}

	class CountingParameters
	{
		long index;
		String text;
	}

	public CharactersCounterThread()
	{
		uiHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (listener != null)
				{
					switch (msg.what)
					{
						case WHAT_TASK_STARTED:
							listener.onTaskStarted();
							break;
						case WHAT_TASK_FINISHED:
							listener.onTaskFinished(null);
							break;
						case WHAT_TASK_PROGRESS:
							listener.onTaskProgress((IndexedCountInformation) msg.obj);
							break;
					}
				}
			}
		};
	}

	public void run()
	{
		encoder = new Encoder(new DataMessageSender());
		Looper.prepare();

		threadHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				CountingParameters params = (CountingParameters) msg.obj;
				IndexedCountInformation result = null;

				try
				{
					CountInformation info = encoder.getCountInformation(params.text);
					result = new IndexedCountInformation(info, params.index);

				} catch (InvalidCharException e)
				{
				}

				Message msgToUi = Message.obtain(uiHandler);
				msgToUi.what = WHAT_TASK_PROGRESS;
				msgToUi.obj = result;
				msgToUi.sendToTarget();
			}
		};

		hasStarted = true;

		Message msgToUi = Message.obtain(uiHandler);
		msgToUi.what = WHAT_TASK_STARTED;
		msgToUi.sendToTarget();

		Looper.loop();

	}

	public void stopCounting()
	{
		try
		{
			hasStarted = false;
			threadHandler.getLooper().quit();
		} catch (Exception e)
		{
		}
	}

	public void setTaskListener(ProgressiveTaskListener<Void, IndexedCountInformation> listener)
	{
		this.listener = listener;
	}

	public void submitTask(long index, String text)
	{
		if (hasStarted)
		{
			CountingParameters params = new CountingParameters();
			params.index = index;
			params.text = text;
			Message msg = Message.obtain(threadHandler);
			msg.obj = params;
			msg.sendToTarget();
		}
	}

}
