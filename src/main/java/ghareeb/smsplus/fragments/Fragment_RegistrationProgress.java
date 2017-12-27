package ghareeb.smsplus.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ghareeb.smsplus.R;
import ghareeb.smsplus.asynctasks.FilterAsyncTask;
import ghareeb.smsplus.asynctasks.InternetCheckerAsynckTask;
import ghareeb.smsplus.asynctasks.RegistrationAsyncTask;
import ghareeb.smsplus.asynctasks.helper.TaskListener;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import ghareeb.smsplus.helper.SharedPreferencesHelper;

public class Fragment_RegistrationProgress extends Fragment
{

	class VerificationMessageReceivedReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context arg0, Intent arg1)
		{
			Bundle bundle = arg1.getExtras();

			if (bundle != null)
			{
				// ---retrieve the SMS message received---
				Object[] pdus = (Object[]) bundle.get("pdus");

				if (pdus != null && pdus.length > 0)
				{
					byte[] pdu = (byte[]) pdus[0];
					SmsMessage message;
					message = SmsMessage.createFromPdu(pdu);
					String sender = message.getOriginatingAddress();
					byte[] partData = message.getUserData();

					if (PhoneNumberUtils.compare(sender, phoneNumber) && isCodeValid(partData))
					{
						unRegisterVerificationReceiver();
						toggleWaitingTimer(false);
						SharedPreferencesHelper.setString(getActivity(), SharedPreferencesHelper.PREF_LAST_VERIFIED_NUMBER,
								phoneNumber);
						startStep2NumberRegistration();
					}
				}
				else
					Log.e("Receiver_MPR", "PDUS is empty");
			}
		}
	}

	/* Attributes */
	private VerificationMessageReceivedReceiver messageReceivedReceiver;

	private String phoneNumber;
	private String countryCode;
	private int subscriptionRemainingDays = 0;
	private Timer messageWaitingTimer;

	private byte[] verificationCode;
	private boolean isReceiverRegistered = false;
	private boolean isProgressing = false;
	private boolean hasFailed = false;
	private int currentStep;
	private int lastStatusResourceId = -1;

	private FragmentListener listener;

	/* Constants */
	private static final int PROGRESS_STEPS_COUNT = 4;
	private static final int VERIFICATION_CODE_LENGTH_BYTES = 32;
	private static final short VERIFICATION_MESSAGE_PORT = 6666;
	private static final int VERIFICATION_MESSAGE_WAITING_TIME_SECONDS = 30;
	private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;


	public static final int EVENT_VERIFICATION_REQUIRED = 20;
	public static final int EVENT_VERIFICATION_FAILED = 21;
	public static final int EVENT_INTERNET_ERROR = 22;
	public static final int EVENT_REGISTRATION_SUCCESSFUL = 23;
	public static final int EVENT_NO_INTERNET_CONNECTION = 24;
	public static final int EVENT_READ_CONTACT_PERMISSION_REQUIRED = 25;

	public Fragment_RegistrationProgress()
	{
	}

	public Fragment_RegistrationProgress(String phoneNumber, String countryCode)
	{
		this.phoneNumber = phoneNumber;
		this.countryCode = countryCode;
	}

	/* Overridden Methods */
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (!isProgressing)
		{
			setRetainInstance(true);

			try
			{
				listener = (FragmentListener) getActivity();
				startProgress();
			} catch (ClassCastException e)
			{
				throw new ClassCastException(getActivity().toString() + " must implement FragmentListener");
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_registration_progress, container, false);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		unRegisterVerificationReceiver();
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (isReceiverRegistered)
			registerVerificationReceiver();

		if (lastStatusResourceId != -1)
		{
			TextView statusTV = getActivity().findViewById(R.id.registrationStatusTV);
			statusTV.setText(lastStatusResourceId);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		toggleWaitingTimer(false);
	}

	/* Public Methods */
	public boolean hasFailed()
	{
		return hasFailed;
	}
	
	/* Helper Methods */
	private void startProgress()
	{
		isProgressing = true;
		startStep0InternetConnectionChecking();
	}


	// Step 0
	public void startStep0InternetConnectionChecking()
	{
		TaskListener<Boolean> internetListener = new TaskListener<Boolean>() {

			@Override
			public void onTaskStarted() {
				currentStep = 0;
				setProgress(currentStep, R.string.register_verifying_internet_connection);
			}

			@Override
			public void onTaskFinished(Boolean result) {
				if (!result) {
					hasFailed = true;
					showIndeterminateProgress(false);
					listener.eventOccurred(EVENT_NO_INTERNET_CONNECTION, null);
				} else
					startStep1NumberVerification();
			}
		};

		InternetCheckerAsynckTask internetAsyncTask = new InternetCheckerAsynckTask(internetListener, getActivity());
		internetAsyncTask.execute();

	}

	// Step 1

	private void startStep1NumberVerification()
	{
		if (!isNumberAlreadyVerified())
		{
			showIndeterminateProgress(false);
			listener.eventOccurred(EVENT_VERIFICATION_REQUIRED, null);
		}
		else
		{
			startStep2NumberRegistration();
		}
	}

	public void confirmVerificationRequest()
	{
		// startStep1NumberVerification();
		showIndeterminateProgress(true);

		registerVerificationReceiver();
		isReceiverRegistered = true;

		verificationCode = generateRandomBytes(VERIFICATION_CODE_LENGTH_BYTES);
		sendVerificationMessage();
		toggleWaitingTimer(true);

		currentStep = 1;
		setProgress(currentStep, R.string.register_verifying_number);
	}

	private void toggleWaitingTimer(boolean isStarted)
	{
		if (isStarted)
		{
			messageWaitingTimer = new Timer("Message Waiting Timer");
			messageWaitingTimer.schedule(new TimerTask()
			{
				private int counter = VERIFICATION_MESSAGE_WAITING_TIME_SECONDS;

				@Override
				public void run()
				{
					setTimerText(String.format("(%d)", counter));

					if (counter == 0)
						onNumberVerificationFailed();

					counter--;
				}
			}, 0, 1000);
		}
		else
		{
			setTimerText("");

			if (messageWaitingTimer != null)
			{
				messageWaitingTimer.cancel();
				messageWaitingTimer = null;
			}
		}
	}

	private void setTimerText(final String value)
	{
		try
		{
			getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					TextView timer = getActivity().findViewById(R.id.waitTimerTV);
					timer.setText(value);
				}
			});
		} catch (Exception e)
		{
			Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
		}
	}

	private boolean isNumberAlreadyVerified()
	{
		String stored = SharedPreferencesHelper.getString(getActivity(), SharedPreferencesHelper.PREF_LAST_VERIFIED_NUMBER);

		if (stored != null && stored.length() > 0 && phoneNumber != null)
		{
			if (PhoneNumberUtils.compare(stored, phoneNumber))
				return true;
		}

		return false;
	}

	private void sendVerificationMessage()
	{
		String sc = SharedPreferencesHelper.getString(getActivity(), SharedPreferencesHelper.PREF_CALL_CENTER);
		SmsManager manager = SmsManager.getDefault();

		if (sc == null || sc.equals(""))
			manager.sendDataMessage(phoneNumber, null, VERIFICATION_MESSAGE_PORT, verificationCode, null, null);
		else
			manager.sendDataMessage(phoneNumber, sc, VERIFICATION_MESSAGE_PORT, verificationCode, null, null);
	}

	private byte[] generateRandomBytes(int count)
	{
		byte[] result = new byte[count];
		Random r = new Random();
		r.nextBytes(result);

		return result;
	}

	private boolean isCodeValid(byte[] receivedCode)
	{
		if (receivedCode == null || receivedCode.length != verificationCode.length)
			return false;

		for (int i = 0; i < verificationCode.length; i++)
		{
			if (receivedCode[i] != verificationCode[i])
				return false;
		}

		return true;
	}

	private void registerVerificationReceiver()
	{
		try
		{
			if (messageReceivedReceiver == null)
				messageReceivedReceiver = new VerificationMessageReceivedReceiver();

			IntentFilter receivedFilter = new IntentFilter("android.intent.action.DATA_SMS_RECEIVED");
			receivedFilter.setPriority(999);
			receivedFilter.addDataScheme("sms");
			receivedFilter.addDataAuthority("*", String.valueOf(VERIFICATION_MESSAGE_PORT));
			getActivity().registerReceiver(messageReceivedReceiver, receivedFilter);
		} catch (Exception e)
		{
			Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
		}
	}

	private void unRegisterVerificationReceiver()
	{
		try
		{
			if (messageReceivedReceiver != null)
				getActivity().unregisterReceiver(messageReceivedReceiver);
		} catch (Exception e)
		{
			Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
		}
	}

	private void onNumberVerificationFailed()
	{
		try
		{
			getActivity().runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					hasFailed = true;
					unRegisterVerificationReceiver();
					toggleWaitingTimer(false);
					setProgress(currentStep, R.string.register_failed);
					showIndeterminateProgress(false);
					listener.eventOccurred(EVENT_VERIFICATION_FAILED, null);
				}
			});
		} catch (Exception e)
		{
			Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
		}
	}

	// Step 2
	private void startStep2NumberRegistration()
	{
		TaskListener<Integer> registrationListener = new TaskListener<Integer>() {
			@Override
			public void onTaskStarted() {
				setTimerText("");
				currentStep = 2;
				setProgress(currentStep, R.string.register_registering_number);
			}

			@Override
			public void onTaskFinished(Integer result) {
				// A call to this may occur if fragment was closed
				try {
					if (result == -1)//Internet problem
					{
						hasFailed = true;
						setProgress(currentStep, R.string.register_failed);
						showIndeterminateProgress(false);
						listener.eventOccurred(EVENT_INTERNET_ERROR, null);
					} else {
						subscriptionRemainingDays = result;
						long expirationDateTimeMillis = System.currentTimeMillis() + (result * MILLIS_PER_DAY);
						SharedPreferencesHelper.setLong(getActivity(), SharedPreferencesHelper.PREF_SUBSCRIPTION_EXPIRATION_DATE_TIME, expirationDateTimeMillis);
						SharedPreferencesHelper.setString(getActivity(), SharedPreferencesHelper.PREF_REGISTERED_NUMBER,
								phoneNumber);
						SharedPreferencesHelper.setString(getActivity(),
								SharedPreferencesHelper.PREF_REGISTERED_NUMBER_COUNTRY_CODE, countryCode);
						startStep3Filtering();
					}
				} catch (Exception e) {
					Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
				}
			}
		};

		RegistrationAsyncTask registrationTask = new RegistrationAsyncTask(registrationListener, getActivity().getApplicationContext());
		registrationTask.execute(phoneNumber);
	}

	// Step 3
	private void startStep3Filtering()
	{
		currentStep = 3;
		setProgress(currentStep, R.string.register_retrieving_numbers);

		int permissionCheck = ContextCompat.checkSelfPermission(getContext(),
				Manifest.permission.READ_CONTACTS);

		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			executeStep3();
		} else {
			listener.eventOccurred(EVENT_READ_CONTACT_PERMISSION_REQUIRED, null);
		}

	}

	public void informResultOfReadContactPermissionRequest(boolean permissionGranted){
		if(permissionGranted)
			executeStep3();
		else
			finalizeProcess();
	}

	private void finalizeProcess() {
		try {
			currentStep++;
			setProgress(currentStep, R.string.register_ok_title);
			isProgressing = false;
			showIndeterminateProgress(false);
			listener.eventOccurred(EVENT_REGISTRATION_SUCCESSFUL, subscriptionRemainingDays);

		} catch (Exception e) {
			Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
		}
	}
	private void executeStep3() {
		TaskListener<ArrayList<Contact>> filterListener = new TaskListener<ArrayList<Contact>>() {
			@Override
			public void onTaskStarted() {

			}

			@Override
			public void onTaskFinished(ArrayList<Contact> result) {
				// A call to this may occur if fragment was closed
				finalizeProcess();
			}
		};

		FilterAsyncTask filterTask = new FilterAsyncTask(filterListener, phoneNumber, false);
		filterTask.execute(getActivity());
	}

	// General
	private void setProgress(final int step, final int statusResourceId)
	{
		try
		{
			getActivity().runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					int value;

					if (step < PROGRESS_STEPS_COUNT)
						value = (100 / PROGRESS_STEPS_COUNT) * step;
					else
						value = 100;

					ProgressBar progressBar = getActivity().findViewById(R.id.registrationProgressBar);
					progressBar.setProgress(value);
					TextView statusTV = getActivity().findViewById(R.id.registrationStatusTV);
					statusTV.setText(statusResourceId);
					lastStatusResourceId = statusResourceId;
				}
			});
		} catch (Exception e)
		{
			Log.e(Fragment_RegistrationProgress.class.getName(), e.getMessage());
		}

	}

	private void showIndeterminateProgress(boolean isShown)
	{
		View pb = getActivity().findViewById(R.id.progressBar1);

		if (isShown)
			pb.setVisibility(View.VISIBLE);
		else
			pb.setVisibility(View.GONE);
	}

}
