package ghareeb.smsplus.webservice;

public final class WebServiceContract
{
	public final static String WEB_SERVICE_URL = "http://ghareebboo-001-site1.htempurl.com/Service.asmx";
	// public final static String WEB_SERVICE_URL =
	// "http://aspspider.info/ghareebboo1/service.asmx";
	public final static String WEB_SERVICE_NAMESPACE = "http://tempuri.org/";

	public static class TokenizedWebMethod
	{
		public final static String PARAM_INT_CLIENT_ID = "clientId";
		public final static String PARAM_STRING_TOKEN = "token";
	}
	
	public static class RequestAccessToken
	{
		public final static String WEB_METHOD_NAME = "RequestAccessToken";
		public final static String PARAM1_INT_CLIENT_ID = "clientId";
	}
	public static class RegisterWebMethod
	{
		public final static String WEB_METHOD_NAME = "Register";
		public final static String PARAM1_STRING_PHONE_NUMBER = "phoneNumber";
	}

	public static class FilterRegisteredWebMethod
	{
		public final static String WEB_METHOD_NAME = "FilterRegistered";
		public final static String PARAM1_STRING_LIST_OF_NUMBERS = "listOfNumbers";
	}
	
	public static class GetUTCTimeMillisWebMethod
	{
		public final static String WEB_METHOD_NAME = "GetUTCTimeMillis";
	}

	// public final static String WEB_SERVICE_URL =
	// "http://smsplus.somee.com/Service.asmx";
	//
	// public final static String WEB_SERVICE_NAMESPACE = new String(new char[]
	// { (char) 104, (char) 116, (char) 116, (char) 112, (char) 58, (char) 47,
	// (char) 47, (char) 116, (char) 101, (char) 109,
	// (char) 112, (char) 117, (char) 114, (char) 105, (char) 46, (char) 111,
	// (char) 114, (char) 103, (char) 47 });
	//
	// public static class RegisterWebMethod
	// {
	// public final static String WEB_METHOD_NAME = new String(new char[]
	// { (char) 82, (char) 101, (char) 103, (char) 105, (char) 115, (char) 116,
	// (char) 101, (char) 114 });
	//
	// public final static String PARAM1_STRING_PHONE_NUMBER = new String(new
	// char[]
	// { (char) 112, (char) 104, (char) 111, (char) 110, (char) 101, (char) 78,
	// (char) 117, (char) 109, (char) 98, (char) 101,
	// (char) 114 });
	// }
	//
	// public static class FilterRegisteredWebMethod
	// {
	// public final static String WEB_METHOD_NAME = new String(new char[]
	// { (char) 70, (char) 105, (char) 108, (char) 116, (char) 101, (char) 114,
	// (char) 82, (char) 101, (char) 103, (char) 105,
	// (char) 115, (char) 116, (char) 101, (char) 114, (char) 101, (char) 100
	// });
	// public final static String PARAM1_STRING_LIST_OF_NUMBERS = new String(new
	// char[]
	// { (char) 108, (char) 105, (char) 115, (char) 116, (char) 79, (char) 102,
	// (char) 78, (char) 117, (char) 109, (char) 98,
	// (char) 101, (char) 114, (char) 115 });
	// }

}
