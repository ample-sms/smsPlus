package ghareeb.smsplus;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * An EditText, which notifies when something was cut/copied/pasted inside it.
 * 
 * @author Lukas Knuth
 * @version 1.0
 */
public class MonitoringEditText extends EditText
{
	interface OnTextPasted
	{
		public void onTextPasted(MonitoringEditText v);
	}

	private OnTextPasted onPasteListener;
	
	public void setOnPasteListener(OnTextPasted onPasteListener)
	{
		this.onPasteListener = onPasteListener;
	}

	/*
	 * Just the constructors to create a new EditText...
	 */
	public MonitoringEditText(Context context)
	{
		super(context);
	}

	public MonitoringEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public MonitoringEditText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/**
	 * <p>
	 * This is where the "magic" happens.
	 * </p>
	 * <p>
	 * The menu used to cut/copy/paste is a normal ContextMenu, which allows us
	 * to overwrite the consuming method and react on the different events.
	 * </p>
	 * 
	 * @see <a
	 *      href="http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3_r1/android/widget/TextView.java#TextView.onTextContextMenuItem%28int%29">Original
	 *      Implementation</a>
	 */
	@Override
	public boolean onTextContextMenuItem(int id)
	{
		// Do your thing:
		boolean consumed = super.onTextContextMenuItem(id);
		// React:
		switch (id)
		{
			case android.R.id.cut:
				onTextCut();
				break;
			case android.R.id.paste:
				onTextPaste();
				break;
			case android.R.id.copy:
				onTextCopy();
		}
		return consumed;
	}

	/**
	 * Text was cut from this EditText.
	 */
	public void onTextCut()
	{
	}

	/**
	 * Text was copied from this EditText.
	 */
	public void onTextCopy()
	{
	}

	/**
	 * Text was pasted into the EditText.
	 */
	public void onTextPaste()
	{
		if(onPasteListener != null)
			onPasteListener.onTextPasted(this);
	}
}