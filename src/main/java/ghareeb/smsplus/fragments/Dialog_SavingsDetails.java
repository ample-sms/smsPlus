package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class Dialog_SavingsDetails extends DialogFragment
{
	private String message;
	private static final String KEY_MESSAGE = "messageKey";
	
	public Dialog_SavingsDetails()
	{}
	
	public Dialog_SavingsDetails(String message)
	{
		this.message = message;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		if(savedInstanceState != null)
			this.message = savedInstanceState.getString(KEY_MESSAGE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setIcon(R.drawable.ic_dollar);
		builder.setTitle(R.string.dialog_savings_details_title);
		builder.setMessage(message);
		
		return builder.create();
	}
	
	@Override
	public void onSaveInstanceState(Bundle arg0)
	{
		super.onSaveInstanceState(arg0);
		arg0.putString(KEY_MESSAGE, message);
	}
}
