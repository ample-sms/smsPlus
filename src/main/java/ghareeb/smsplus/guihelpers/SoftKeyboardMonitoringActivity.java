package ghareeb.smsplus.guihelpers;

import ghareeb.smsplus.helper.ClockHelper;
import ghareeb.smsplus.helper.DimensionsHelper;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;

public abstract class SoftKeyboardMonitoringActivity extends AppCompatActivity
{
	private final static int MINIMUM_SOFT_KEYBOARD_PORT_HEIGHT_DIPS = 120;
	private final static int MINIMUM_SOFT_KEYBOARD_LAND_HEIGHT_DIPS = 60;

	private final static String KEY_LAST_DETECTED_PORT_KEYBOARD_HEIGHT = "portKeyboardHeightKey";
	private final static String KEY_LAST_DETECTED_LAND_KEYBOARD_HEIGHT = "landKeyboardHeightKey";

	protected int lastDetectedPortKeyboardHeight;
	protected int lastDetectedLandKeyboardHeight;
	protected boolean clockValidated;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		clockValidated = ClockHelper.validateClock(this);
		
		if (clockValidated)
		{

			if (savedInstanceState != null)
			{
				lastDetectedPortKeyboardHeight = savedInstanceState.getInt(KEY_LAST_DETECTED_PORT_KEYBOARD_HEIGHT);
				lastDetectedLandKeyboardHeight = savedInstanceState.getInt(KEY_LAST_DETECTED_LAND_KEYBOARD_HEIGHT);
			}
			else
			{
				lastDetectedPortKeyboardHeight = SharedPreferencesHelper.getInt(this,
						SharedPreferencesHelper.PREF_LAST_KNOWN_SOFT_KEYBOARD_PORT_HEIGHT_PIXELS, 0);
				lastDetectedLandKeyboardHeight = SharedPreferencesHelper.getInt(this,
						SharedPreferencesHelper.PREF_LAST_KNOWN_SOFT_KEYBOARD_LAND_HEIGHT_PIXELS, 0);
			}
		}
		else
			finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_LAST_DETECTED_PORT_KEYBOARD_HEIGHT, lastDetectedPortKeyboardHeight);
		outState.putInt(KEY_LAST_DETECTED_LAND_KEYBOARD_HEIGHT, lastDetectedLandKeyboardHeight);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		final View root = findViewById(getRootViewId());
		root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

				Rect r = new Rect();
				root.getWindowVisibleDisplayFrame(r);

				int screenHeight = root.getRootView().getHeight();
				int heightDifference = screenHeight - (r.bottom - r.top);
				int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

				if (resourceId > 0)
				{
					heightDifference -= getResources().getDimensionPixelSize(resourceId);
				}

				int minimumKeyboardHeightPixels;

				if (isPortrait)
				{
					minimumKeyboardHeightPixels = DimensionsHelper.getPixelsFromDips(SoftKeyboardMonitoringActivity.this,
							MINIMUM_SOFT_KEYBOARD_PORT_HEIGHT_DIPS);

					if (heightDifference > minimumKeyboardHeightPixels)
					{
						if (heightDifference != lastDetectedPortKeyboardHeight)
						{
							lastDetectedPortKeyboardHeight = heightDifference;
							SharedPreferencesHelper.setInt(SoftKeyboardMonitoringActivity.this,
									SharedPreferencesHelper.PREF_LAST_KNOWN_SOFT_KEYBOARD_PORT_HEIGHT_PIXELS,
									lastDetectedPortKeyboardHeight);
							onSoftKeyboardPortHeightDetected(lastDetectedPortKeyboardHeight);
						}
					}
				}
				else
				{
					minimumKeyboardHeightPixels = DimensionsHelper.getPixelsFromDips(SoftKeyboardMonitoringActivity.this,
							MINIMUM_SOFT_KEYBOARD_LAND_HEIGHT_DIPS);

					if (heightDifference > minimumKeyboardHeightPixels)
					{
						if (heightDifference != lastDetectedLandKeyboardHeight)
						{
							lastDetectedLandKeyboardHeight = heightDifference;
							SharedPreferencesHelper.setInt(SoftKeyboardMonitoringActivity.this,
									SharedPreferencesHelper.PREF_LAST_KNOWN_SOFT_KEYBOARD_LAND_HEIGHT_PIXELS,
									lastDetectedLandKeyboardHeight);
							onSoftKeyboardLandHeightDetected(lastDetectedLandKeyboardHeight);
						}
					}
				}

			}

		});
	}

	protected abstract int getRootViewId();

	protected void onSoftKeyboardPortHeightDetected(int keyBoardHeightPixels)
	{

	}

	protected void onSoftKeyboardLandHeightDetected(int keyboardHeightInPixels)
	{

	}
}
