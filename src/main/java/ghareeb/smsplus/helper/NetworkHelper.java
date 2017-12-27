package ghareeb.smsplus.helper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkHelper
{
	private final static String TAG = "NetworkHelper";
	
	public static boolean isConnectedOrConnecting(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if( activeNetworkInfo != null)
		{
			return activeNetworkInfo.isConnectedOrConnecting();
		}
		
		return false;
	}
	
	public static boolean isConnected(Context context)
	{
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		
		if( activeNetworkInfo != null)
		{
			return activeNetworkInfo.isConnected();
		}
		
		return false;
	}

	public static boolean hasInternetAccess(Context context)
	{
		if (isConnected(context))
		{
			try
			{
				HttpURLConnection urlc = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204").openConnection());
				urlc.setRequestProperty("User-Agent", "Android");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(1500);
				urlc.connect();
				return (urlc.getResponseCode() == 204 && urlc.getContentLength() == 0);
			} catch (IOException e)
			{
				Log.e(TAG, "Error checking internet connection", e);
			}
		}
		else
		{
			Log.d(TAG, "No network available!");
		}
		return false;
	}
}
