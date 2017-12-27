package ghareeb.smsplus.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import ghareeb.smsplus.R;
import ghareeb.smsplus.fragments.parents.Dialog_YesNoParent;

public class Dialog_SendVerification extends Dialog_YesNoParent
{
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = getBuilder();
		builder.setTitle(R.string.dialog_send_verification_title);
		builder.setMessage(R.string.dialog_send_verification_body);
		builder.setPositiveButton(R.string.dialog_yes, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				listener.eventOccurred(EVENT_YES_PRESSED, null);
			}
		});
		
		return builder.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog)
	{
		super.onCancel(dialog);
		listener.eventOccurred(EVENT_CANCEL_PRESSED, null);
	}
	
}
