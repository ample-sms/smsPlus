package ghareeb.smsplus.fragments;

import ghareeb.smsplus.R;
import ghareeb.smsplus.fragments.helper.FragmentListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class Fragment_EnteringUserPhoneNumber extends Fragment
{
	class CountryCode implements Comparable<CountryCode>
	{
		String codeString;
		String codeNumber;
		String countryName;
		
		CountryCode(String codeString, String codeNumber)
		{
			this.codeString = codeString;
			this.codeNumber = codeNumber;
			this.countryName = getCountryZipCode(codeString);
		}
		

		public String toString()
		{
			String result = String.format("%s (+%s)", countryName, codeNumber);
			
			return result;
		}

		@Override
		public int compareTo(CountryCode another)
		{
			return toString().compareTo(another.toString());
		}
	}
	
	public static final int EVENT_REGISTER_BUTTON_CLICKED = 0;

	private FragmentListener listener;
	private Spinner countryCodesSpinner;
	private EditText phonNumberET;
	private Button registerB;
	private ArrayAdapter<String> adapter;
	private ArrayList<CountryCode> codes; 

	/*Inherited*/
	@Override

	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		countryCodesSpinner = (Spinner) getActivity().findViewById(R.id.countryCodesSpinner);
		phonNumberET = (EditText) getActivity().findViewById(R.id.phoneNumberET);
		registerB = (Button) getActivity().findViewById(R.id.registerB);
		
		fillSpinner();
		registerB.setOnClickListener(new View.OnClickListener()
		{	
			@Override
			public void onClick(View v)
			{
				String number = getFullPhoneNumber();
				
				if(number != null)
					listener.eventOccurred(EVENT_REGISTER_BUTTON_CLICKED, null);
			}
		});
		
		try
		{
			listener = (FragmentListener) getActivity();
		} catch (ClassCastException e)
		{
			throw new ClassCastException(getActivity().toString() + " must implement FragmentListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_entering_user_phone_number, container, false);
	}

	/*Public*/
	public String getPhoneNumber()
	{
		return getFullPhoneNumber();
	}

	public String getCountryCode()
	{
		int index = countryCodesSpinner.getSelectedItemPosition();
		
		return codes.get(index).codeString;
	}
	/*Helper*/
	private String getFullPhoneNumber()
	{
		String phoneNumber = removeLeadingZeros(phonNumberET.getText().toString());

		if (phoneNumber != null && !phoneNumber.equals(""))
		{
			String[] parts = ((String) countryCodesSpinner.getSelectedItem()).split(" ");
			String code = parts[1];
			code = code.replace("(", "");
			code = code.replace(")", "");
			code = code.replace("-", "");
			code = code.replace(" ", "");
			String result = String.format("%s%s", code, phoneNumber);

			return result;
		}

		return "";
	}

	private String removeLeadingZeros(String number)
	{
		int leadingZerosCounter = 0;

		for (int i = 0; i < number.length(); i++)
		{
			if (number.charAt(i) == '0')
				leadingZerosCounter++;
			else
				break;
		}

		if (leadingZerosCounter == number.length())
			return "";

		return number.substring(leadingZerosCounter);
	}

	private void fillSpinner()
	{
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		codes = new ArrayList<CountryCode>();
		
		CountryCode current;
		String[] resourseList = this.getResources().getStringArray(R.array.country_codes);

		for (int i = 0; i < resourseList.length; i++)
		{
			current = getCountryCode(resourseList[i]);
			codes.add(current);
		}

		Collections.sort(codes);

		for (CountryCode item : codes)
			adapter.add(item.toString());

		int index = findIndexOfCurrentCountry();
		countryCodesSpinner.setAdapter(adapter);

		if (index >= 0)
			countryCodesSpinner.setSelection(index);
	}

	private CountryCode getCountryCode(String unformatted)
	{
		String[] parts = unformatted.split(",");
		CountryCode result = new CountryCode(parts[1], parts[0]);

		return result;
	}

	private String getCountryZipCode(String ssid)
	{
		Locale loc = new Locale("", ssid);

		return loc.getDisplayCountry().trim();
	}

	private int findIndexOfCurrentCountry()
	{
		int result = -1;
		String country = getUserCountryCode(getActivity());

		if (country != null)
		{
			for (int i = 0; i < codes.size(); i++)
			{
				if (codes.get(i).codeString.equalsIgnoreCase(country))
				{
					result = i;
					break;
				}
			}
		}

		return result;
	}

	/*
	 * Get ISO 3166-1 alpha-2 country code for this device (or null if not
	 * available)
	 */
	private String getUserCountryCode(Context context)
	{
		try
		{
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final String simCountry = tm.getSimCountryIso();
			
			if (simCountry != null && simCountry.length() == 2)
			{ // SIM country code is available
				return simCountry;
			}
			else
			{
				if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA)
				{ // device is not 3G (would be unreliable)
					String networkCountry = tm.getNetworkCountryIso();
					if (networkCountry != null && networkCountry.length() == 2)
					{ // network country code is available
						return networkCountry;
					}
				}
			}
		} catch (Exception e)
		{
		}
		return null;
	}

}
