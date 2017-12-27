package ghareeb.smsplus.webservice;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

@SuppressWarnings("rawtypes")
public class WebMethodCall
{
	private static final Class INTEGER = int.class;
	private static final Class DOUBLE = double.class;
	private static final Class STRING = String.class;
	private static final Class BOOLEAN = boolean.class;
	private String nameSpace;
	private String method;
	private String url;
	private String action;
	private SoapObject soapObject;
	private SoapSerializationEnvelope envelope;


	public WebMethodCall(String webServiceNamespace, String webMethodName, String webServiceUrl)
	{
		this.nameSpace = webServiceNamespace;
		this.method = webMethodName;
		this.url = webServiceUrl;
		this.action = this.nameSpace + this.method;

		soapObject = new SoapObject(nameSpace, this.method);
		envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		MarshalDouble mDouble = new MarshalDouble();
		mDouble.register(envelope);
		envelope.setOutputSoapObject(soapObject);
		envelope.dotNet = true;
	}
	
	public void addMapping(String className, KvmSerializable emptyObjectOfSerializableClass)
	{
		envelope.addMapping(nameSpace, className, emptyObjectOfSerializableClass.getClass());
	}

	public void addIntegerParameter(String parameterName, Object parameterValue)
	{
		addParameter(INTEGER, parameterName, parameterValue);
	}

	public void addDoubleParameter(String parameterName, Object parameterValue)
	{
		addParameter(DOUBLE, parameterName, parameterValue);
	}

	public void addBooleanParameter(String parameterName, Object parameterValue)
	{
		addParameter(BOOLEAN, parameterName, parameterValue);
	}

	public void addStringParameter(String parameterName, Object parameterValue)
	{
		addParameter(STRING, parameterName, parameterValue);
	}

	public void execute() throws IOException, XmlPullParserException 
	{
		HttpTransportSE androidHttpTransport = new HttpTransportSE(url, 30000);
		androidHttpTransport.call(action, envelope);
		
	}

	public Object getResponse()
	{
		try
		{
			return envelope.getResponse();
		} catch (SoapFault e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Object getBodyIn()
	{
		return envelope.bodyIn;
	}
	// should use the type specified by this class
	private void addParameter(Class parameterType, String parameterName,
			Object parameterValue)
	{
		PropertyInfo parameter = new PropertyInfo();
		parameter.setName(parameterName);
		parameter.setType(parameterType);
		parameter.setValue(parameterValue);
		soapObject.addProperty(parameter);
	}

	private class MarshalDouble implements Marshal
	{

		public Object readInstance(XmlPullParser parser, String namespace,
				String name, PropertyInfo expected) throws IOException,
				XmlPullParserException
		{

			return Double.parseDouble(parser.nextText());
		}

		public void register(SoapSerializationEnvelope cm)
		{
			cm.addMapping(cm.xsd, "double", Double.class, this);

		}

		public void writeInstance(XmlSerializer writer, Object obj)
				throws IOException
		{
			writer.text(obj.toString());
		}

	}

}
