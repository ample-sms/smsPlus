package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import ghareeb.smsplus.fragments.parents.Dialog_DeleteParent;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

public class Dialog_DeleteThread extends Dialog_DeleteParent
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = getBuilder();
		builder.setTitle(R.string.dialog_delete_thread_title);
		builder.setMessage(R.string.dialog_delete_thread_body);
		
		return builder.create();
	}
}
