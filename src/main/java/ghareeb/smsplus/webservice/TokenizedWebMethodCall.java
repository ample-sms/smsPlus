package ghareeb.smsplus.webservice;

import ghareeb.enhancedaeslibrary.Decryptor;
import ghareeb.enhancedaeslibrary.TextUtils;
import ghareeb.smsplus.helper.SharedPreferencesHelper;
import ghareeb.smsplus.webservice.WebServiceContract.TokenizedWebMethod;

import java.io.IOException;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

public class TokenizedWebMethodCall extends WebMethodCall
{
	private WebMethodCall tokenCall;
	private String token;
	private Context context;
	private static final String KEY = new String(new char[]	{
		(char) 66, (char) 55, (char) 70, (char) 65, (char) 67, (char) 49, (char) 53, (char) 68, (char) 54, (char) 52, (char) 53,
		(char) 52, (char) 54, (char) 57, (char) 52, (char) 68, (char) 70, (char) 48, (char) 65, (char) 54, (char) 49,
		(char) 56, (char) 66, (char) 56, (char) 50, (char) 53, (char) 52, (char) 51, (char) 54, (char) 54, (char) 69,
		(char) 56 });

	public TokenizedWebMethodCall(String webMethodName, Context context)
	{
		super(WebServiceContract.WEB_SERVICE_NAMESPACE, webMethodName, WebServiceContract.WEB_SERVICE_URL);
		tokenCall = new WebMethodCall(WebServiceContract.WEB_SERVICE_NAMESPACE,
				WebServiceContract.RequestAccessToken.WEB_METHOD_NAME, WebServiceContract.WEB_SERVICE_URL);
		this.context = context;
	}

	@Override
	public void execute() throws IOException, XmlPullParserException
	{
		int deviceId = getDeviceId();
		tokenCall.addIntegerParameter(WebServiceContract.RequestAccessToken.PARAM1_INT_CLIENT_ID, deviceId);
		tokenCall.execute();// May itself throw an exception
		Object resultO = tokenCall.getResponse();
		token = resultO.toString();

		if (token != null && token.length() > 0)
		{
			try
			{
				Decryptor dec = new Decryptor(KEY);
				token = TextUtils.decodeHex(dec.decrypt(TextUtils.encodeHex(token)));
			} catch (Exception e)// should never occur
			{
			}

			addIntegerParameter(TokenizedWebMethod.PARAM_INT_CLIENT_ID, deviceId);
			addStringParameter(TokenizedWebMethod.PARAM_STRING_TOKEN, token);

			super.execute();

		}
		else
			throw new IOException("Internal server error.");
	}

	private int getDeviceId()
	{
		int deviceId = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.PREF_DEVICE_ID_FOR_WEB_METHODS_TOKENS, -1);

		if (deviceId == -1)
		{
			Random r = new Random();
			deviceId = r.nextInt(Integer.MAX_VALUE);
			SharedPreferencesHelper.setInt(context, SharedPreferencesHelper.PREF_DEVICE_ID_FOR_WEB_METHODS_TOKENS, deviceId);
		}

		context = null;

		return deviceId;
	}
}
