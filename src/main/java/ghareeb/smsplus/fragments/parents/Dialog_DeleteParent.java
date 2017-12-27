package ghareeb.smsplus.fragments.parents;

import ghareeb.smsplus.R;
import android.app.AlertDialog;
import android.content.DialogInterface;

public abstract class Dialog_DeleteParent extends Dialog_YesNoParent
{

	protected AlertDialog.Builder getBuilder()
	{
		AlertDialog.Builder builder = super.getBuilder();
		builder.setPositiveButton(R.string.dialog_delete_button, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				listener.eventOccurred(EVENT_YES_PRESSED, null);
				dismiss();
			}
		});

		return builder;
	}
}
