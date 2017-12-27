package ghareeb.smsplus;

import ghareeb.smsplus.MonitoringEditText.OnTextPasted;
import ghareeb.smsplus.asynctasks.CharactersCounterThread;
import ghareeb.smsplus.asynctasks.helper.ProgressiveTaskListener;
import ghareeb.smsplus.common.IndexedCountInformation;
import ghareeb.smsplus.common.Smiley;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.encoding.Encoder;
import ghareeb.smsplus.encoding.IEncoder;
import ghareeb.smsplus.encoding.InvalidCharException;
import ghareeb.smsplus.guihelpers.MessageSendingActivity;
import ghareeb.smsplus.helper.SmiliesHelper;
import ghareeb.smsplus.transmission.sending.DataMessageSender;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

//Should call initializeCounting() to start counting 
//Should call stopCounting() when the component is to be destroyed
//Can only be used with ActionBarActivity's
public class Component_MessageCreator extends RelativeLayout
{
	/* Inner Types */
	class SendAsyncTask extends AsyncTask<String, Void, Boolean>
	{
		private SentMessage result;

		@Override
		protected Boolean doInBackground(String... params)
		{
			String message = params[0];

			try
			{
				if (threadId < 0)//
					result = encoder.send(message, recipient, getContext());
				else
					result = encoder.send(message, threadId, recipient, getContext());

				return true;
			} catch (InvalidCharException e)
			{
				e.printStackTrace();
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			if (!result)
				Toast.makeText(getContext(), getContext().getString(R.string.message_creation_error), Toast.LENGTH_SHORT).show();
			else
			{
				if (this.result != null && sendingListener != null)
				{
					sendingListener.sendRequested(this.result);
				}
			}
		}
	}

	class SmiliesAdapter extends ArrayAdapter<Smiley>
	{
		private Context context;
		private int layoutResourceId;
		private Smiley[] data = null;

		public SmiliesAdapter(Context context, int resId, Smiley[] data)
		{
			super(context, resId, data);
			this.layoutResourceId = resId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (data != null)
			{
				View row = convertView;
				SmileyHolder holder = null;

				if (row == null)
				{
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					row = inflater.inflate(layoutResourceId, parent, false);

					holder = new SmileyHolder();
					holder.smileyImage = (ImageView) row.findViewById(R.id.smileyInGridIV);

					row.setTag(holder);
				}
				else
				{
					holder = (SmileyHolder) row.getTag();
				}

				Smiley smiley = data[position];
				holder.smileyImage.setImageResource(smiley.RESOURCE_ID);

				return row;
			}

			return null;
		}
	}

	static class SmileyHolder
	{
		ImageView smileyImage;
	}

	interface OnSendingListener
	{
		void sendRequested(SentMessage message);
	}

	interface OnSmiliesBarVisibilityChanged
	{
		void visibilityChanged(boolean isVisible);
	}

	/* Attributes */
	private static final String TAG = "Component_MC";
	// Sending & Counting Related
	private OnSendingListener sendingListener;
	private String recipient;
	private IEncoder encoder;
	private SendAsyncTask sender;
	private CharactersCounterThread countingThread;
	private long threadId = -1;
	private long letterChangeCounter;

	// Views
	private TextView letterCounterTV;
	private MonitoringEditText messageET;
	private ImageView sendImageView;
	private ImageView smiliesImageView;
	private ImageView backSpaceImageView;
	private GridView smiliesGridView;
	private RelativeLayout smiliesRL;
	// Smilies Related
	private SmiliesAdapter adapter;
	private boolean isSmiliesBarShown;
	private OnSmiliesBarVisibilityChanged smiliesVisibilityChangedListener;
	private Timer smiliesBarVisibilityTimer;
	private static final int SHOW_HIDE_DELAY_MILLIS = 100;


	/* Constructor */
	public Component_MessageCreator(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.message_creation_compound_component, this);
		// MESSAGE_ET_BOTTOM_MARGIN_PIXELS =
		// DimensionsHelper.getPixelsFromDips(context,
		// MESSAGE_ET_BOTTOM_MARGIN_DIPS);
		encoder = new Encoder(new DataMessageSender());

		letterCounterTV = (TextView) findViewById(R.id.charsPartsCounterTV);
		messageET = (MonitoringEditText) findViewById(R.id.messageBodyET);
		sendImageView = (ImageView) findViewById(R.id.sendIB);
		smiliesImageView = (ImageView) findViewById(R.id.smiliesIV);
		smiliesGridView = (GridView) findViewById(R.id.smiliesGV);
		smiliesRL = (RelativeLayout) findViewById(R.id.smiliesRL);
		backSpaceImageView = (ImageView) findViewById(R.id.backspaceIV);

		adapter = new SmiliesAdapter(context, R.layout.list_item_smilies, SmiliesHelper.getAllSmilies());
		smiliesGridView.setAdapter(adapter);
		sendImageView.setEnabled(false);

		backSpaceImageView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int cursorPosition = messageET.getSelectionEnd();

				if (cursorPosition > 0 && getContext() != null)
				{
					StringBuilder builder = new StringBuilder(messageET.getText());
					builder.deleteCharAt(cursorPosition - 1);
					CharSequence result = SmiliesHelper.replaceAllPatternsWithImages(builder.toString(), messageET.getTextSize(),
							getContext());
					messageET.setText(result);
					messageET.setSelection(cursorPosition - 1);
				}

			}
		});

		smiliesGridView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				int cursorPosition = messageET.getSelectionEnd();

				if (cursorPosition >= 0 && adapter != null && adapter.data != null && getContext() != null)
				{
					SmiliesHelper.addSmileyAtPosition(messageET, cursorPosition, adapter.data[arg2], getContext());
				}
			}
		});

		smiliesImageView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				showSmiliesBar(!isSmiliesBarShown);
			}
		});

		sendImageView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				sendClicked(v);
			}
		});

		messageET.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}

			@Override
			public void afterTextChanged(Editable s)
			{
				if (countingThread != null)
				{
					incrementLettersChangeCounter();
					countingThread.submitTask(getLetterChangeCounter(), s.toString());
				}

				refreshSendButtonState();
			}
		});

		messageET.setOnFocusChangeListener(new OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					smiliesImageView.setEnabled(true);
				}
			}
		});

		messageET.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setSmiliesBarShown(false);
			}
		});

		messageET.setOnPasteListener(new OnTextPasted()
		{
			@Override
			public void onTextPasted(MonitoringEditText v)
			{
				int cursorPosition = v.getSelectionEnd();
				CharSequence result = SmiliesHelper.replaceAllPatternsWithImages(v.getText(), v.getTextSize(), getContext());
				v.setText(result);
				v.setSelection(cursorPosition);
			}
		});

		messageET.setOnEditorActionListener(new OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2)
			{
				boolean handled = false;
				if (arg1 == EditorInfo.IME_ACTION_DONE)
				{
					hideSoftKeyboard();
					handled = true;
				}
				return handled;
			}
		});
	}

	/* Helper Methods */
	// Counting
	private synchronized long getLetterChangeCounter()
	{
		return letterChangeCounter;
	}

	private synchronized void incrementLettersChangeCounter()
	{
		letterChangeCounter++;
	}

	private void refreshCount()
	{
		if (messageET != null && countingThread != null)
			countingThread.submitTask(getLetterChangeCounter(), messageET.getText().toString());
	}

	// Sending
	private void refreshSendButtonState()
	{
		if (messageET.getText().toString().length() > 0 && recipient != null && !recipient.equals(""))
			sendImageView.setEnabled(true);
		else
			sendImageView.setEnabled(false);

	}

	private void performSend(String message)
	{
		sender = new SendAsyncTask();
		sender.execute(message);
		//Toast.makeText(getContext(), getContext().getString(R.string.message_creation_sending), Toast.LENGTH_SHORT).show();
	}

	//TODO restore subscription checking
	private boolean verifySubscription()
	{
		/*long expiryDateMillis = SharedPreferencesHelper.getLong(getContext(),
				SharedPreferencesHelper.PREF_SUBSCRIPTION_EXPIRATION_DATE_TIME, Long.MAX_VALUE);

		if (System.currentTimeMillis() >= expiryDateMillis)
		{
			Resources res = getContext().getResources();
			Dialog_OkParent dialog = new Dialog_OkParent();
			dialog.setTexts(res.getString(R.string.subscription_expired_title),
					res.getString(R.string.subscription_expired_description), res.getString(R.string.ok));

			dialog.show(((ActionBarActivity) getContext()).getSupportFragmentManager(), "subscription expired dialog");
			return true;
		}*/
//		else
//		{
//			int days = (int)((expiryDateMillis - System.currentTimeMillis())/(24L * 60L * 60L * 1000L));
//			Toast.makeText(getContext(), days + " Days", Toast.LENGTH_SHORT).show();
//		}

		return true;
	}
	// Smilies
	private void startSmiliesVisibilityTimer(final boolean isShown, int delayMillis)
	{
		if (smiliesBarVisibilityTimer != null)
			smiliesBarVisibilityTimer.cancel();

		smiliesBarVisibilityTimer = new Timer("Visibility Timer");
		smiliesBarVisibilityTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					((Activity) getContext()).runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (isShown)
							{
								smiliesRL.setVisibility(View.VISIBLE);
								Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getContext(),
										R.anim.abc_slide_in_bottom);
								smiliesGridView.startAnimation(hyperspaceJumpAnimation);
							}
							else
							{
								smiliesRL.setVisibility(View.GONE);
							}

							if (smiliesVisibilityChangedListener != null)
								smiliesVisibilityChangedListener.visibilityChanged(isShown);
						}
					});

				} catch (Exception e)
				{
				}
			}
		}, delayMillis);
	}

	private void showSmiliesBar(boolean isShown)
	{
		isSmiliesBarShown = isShown;

		if (isShown)
		{
			hideSoftKeyboard();
			startSmiliesVisibilityTimer(true, SHOW_HIDE_DELAY_MILLIS);
			smiliesImageView.setImageResource(R.drawable.ic_smilies_selected);
		}
		else
		{
			startSmiliesVisibilityTimer(false, 0);
			smiliesImageView.setImageResource(R.drawable.ic_smilies_not_selected);
		}
	}

	private void hideSoftKeyboard()
	{
		try
		{
			Activity a = (Activity) getContext();
			InputMethodManager inpMan = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			View v = a.getCurrentFocus();
			inpMan.hideSoftInputFromWindow(v.getWindowToken(), 0);
		} catch (Exception e)
		{
			Log.e(TAG, e.toString());
		}
	}

	/* Public Interface */
	// Sending
	public void setOnSendingListener(OnSendingListener sendingListener)
	{
		this.sendingListener = sendingListener;
	}

	public void setThreadId(long threadId)
	{
		this.threadId = threadId;
	}

	public String getRecipient()
	{
		return recipient;
	}

	public void setRecipient(String recipient)
	{
		this.recipient = recipient;
		refreshSendButtonState();
	}

	public void sendClicked(View v) {
		((MessageSendingActivity)getContext()).initiateSendMessage();
	}

	public void sendMessage(){
		if (verifySubscription())
		{
			String message = messageET.getText().toString();
			performSend(message);
			messageET.setText("");
			setSmiliesBarShown(false);
			hideSoftKeyboard();
		}
	}

	public void resendMessage(String message)
	{
		performSend(message);
	}


	// Counting
	public void initializeCounting()
	{
		countingThread = new CharactersCounterThread();
		countingThread.setTaskListener(new ProgressiveTaskListener<Void, IndexedCountInformation>()
		{
			@Override
			public void onTaskStarted()
			{
				refreshCount();
			}

			@Override
			public void onTaskFinished(Void result)
			{

			}

			@Override
			public void onTaskProgress(IndexedCountInformation result)
			{
				if (result == null || result.getIndex() < getLetterChangeCounter())
					return;

				letterCounterTV.setText(result.toString());

			}
		});
		countingThread.start();
	}

	public void stopCounting()
	{
		if (countingThread != null)
		{
			countingThread.stopCounting();
			countingThread = null;
		}
	}

	// Misc
	public void setUsable(boolean isEnabled)
	{
		if (!isEnabled)
		{
			sendImageView.setEnabled(false);
			messageET.setEnabled(false);
		}
		else
		{
			refreshSendButtonState();
			messageET.setEnabled(true);
		}
	}

	public void setMessageText(String text)
	{
		if (text == null)
			text = "";

		messageET.setText(SmiliesHelper.replaceAllPatternsWithImages(text, messageET.getTextSize(), getContext()));
		int selectionIndex = text.length();

		if (selectionIndex > 0)
			messageET.setSelection(selectionIndex);
	}

	public String getMessageText()
	{
		return messageET.getText().toString();
	}

	// Smilies
	public void setSmiliesBarShown(boolean isShown)
	{
		showSmiliesBar(isShown);
	}

	public void disableSmilies()
	{
		setSmiliesBarShown(false);
		smiliesImageView.setEnabled(false);
	}

	public boolean isSmiliesBarShown()
	{
		return isSmiliesBarShown;
	}

	public void setOnSmiliesVisibilityChangedListener(OnSmiliesBarVisibilityChanged smiliesVisibilityChangedListener)
	{
		this.smiliesVisibilityChangedListener = smiliesVisibilityChangedListener;
	}

	public void setSmiliesBarHeightInPixels(int pixels)
	{
		LayoutParams params = (LayoutParams) smiliesRL.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, 0);
		params.height = pixels;
	}

	public void setSmiliesBarHeightToDefault()
	{
		LayoutParams params = (LayoutParams) smiliesRL.getLayoutParams();
		params.addRule(RelativeLayout.BELOW, R.id.rightBarRL);
		params.height = ViewGroup.LayoutParams.MATCH_PARENT;
	}



}
