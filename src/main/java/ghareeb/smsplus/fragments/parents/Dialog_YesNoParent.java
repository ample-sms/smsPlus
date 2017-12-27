package ghareeb.smsplus.fragments.parents;

import ghareeb.smsplus.R;
import ghareeb.smsplus.fragments.helper.FragmentListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class Dialog_YesNoParent extends DialogFragment
{
	protected FragmentListener listener;

	// Be Ware!!
	public static final int EVENT_YES_PRESSED = 10;
	public static final int EVENT_CANCEL_PRESSED = 11;

	
	@Override
	public void onActivityCreated(Bundle arg0)
	{
		super.onActivityCreated(arg0);

		try
		{
			listener = (FragmentListener) getActivity();
		} catch (ClassCastException e)
		{
			throw new ClassCastException(getActivity().toString() + " must implement FragmentListener");
		}
	}

	protected AlertDialog.Builder getBuilder()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				listener.eventOccurred(EVENT_CANCEL_PRESSED, null);
				// Dismissing is automatic
			}
		});
		
		return builder;
	}

}
