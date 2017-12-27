package ghareeb.smsplus.fragments.parents;

import ghareeb.smsplus.fragments.helper.FragmentListener;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class Dialog_OkParent extends DialogFragment
{
	private FragmentListener listener;
	public static final int EVENT_OK_PRESSED = 100;

	private static final String KEY_TITLE = "title";
	private static final String KEY_BODY = "body";
	private static final String KEY_BUTTON = "button";
	private static final String KEY_ICON = "icon";

	private String title;
	private String body;
	private String buttonText;
	private int iconDrawableResourceId = -1;


	public void setTexts(String title, String body, String buttonText)
	{
		this.title = title;
		this.buttonText = buttonText;
		this.body = body;
	}

	@Override
	public void onSaveInstanceState(Bundle arg0)
	{
		super.onSaveInstanceState(arg0);
		arg0.putString(KEY_BODY, body);
		arg0.putString(KEY_BUTTON, buttonText);
		arg0.putInt(KEY_ICON, iconDrawableResourceId);
		arg0.putString(KEY_TITLE, title);
	}

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

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			title = savedInstanceState.getString(KEY_TITLE);
			body = savedInstanceState.getString(KEY_BODY);
			buttonText = savedInstanceState.getString(KEY_BUTTON);
			iconDrawableResourceId = savedInstanceState.getInt(KEY_ICON);
		}
		Builder builder = new Builder(getActivity());

		builder.setTitle(title);
		builder.setMessage(body);

		if (iconDrawableResourceId >= 0)
			builder.setIcon(iconDrawableResourceId);

		builder.setPositiveButton(buttonText, new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				listener.eventOccurred(EVENT_OK_PRESSED, null);
			}
		});

		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog)
	{
		super.onCancel(dialog);
		listener.eventOccurred(EVENT_OK_PRESSED, null);
	}

	/**
	 * We override the default implementation to allow state loss when showing the dialog.
	 * Otherwise, the error "the operation cannot be performed after onSavedInstanceState is called
	 * when trying to show this dialog from a Fragment.
	 * @param manager the fragment manager
	 * @param tag the tag of this dialog
	 */
	@Override
	public void show(FragmentManager manager, String tag) {

		try {
			FragmentTransaction ft = manager.beginTransaction();
			ft.add(this, tag);
			ft.commitAllowingStateLoss();
		} catch (IllegalStateException e) {
			Log.e(Dialog_OkParent.class.getName(), e.getMessage());
		}
	}

}
