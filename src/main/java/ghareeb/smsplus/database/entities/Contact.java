package ghareeb.smsplus.database.entities;

import ghareeb.smsplus.common.PhoneNumber;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

public class Contact implements Comparable<Contact>
{
	// Entity attributes
	private long id;
	private String lookupKey;
	private boolean isRegistered;
	private PhoneNumber number;

	// Buffers
	private String name;
	private Bitmap photo;

	public boolean isRegistered()
	{
		return isRegistered;
	}

	public void setRegistered(boolean isRegistered)
	{
		this.isRegistered = isRegistered;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getLookupKey()
	{
		return lookupKey;
	}

	public void setLookupKey(String lookupKey)
	{
		this.lookupKey = lookupKey;
	}

	public Bitmap getPhoto()
	{
		return photo;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public PhoneNumber getNumber()
	{
		return number;
	}

	public void setNumber(PhoneNumber number)
	{
		this.number = number;
	}

	public void setNumber(String number, Context context)
	{
		this.number = PhoneNumber.parse(number, context);
	}
	public Contact()
	{
	}

	public Contact(String number, Context context)
	{
		setNumber(PhoneNumber.parse(number, context));
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Contact)
		{
			PhoneNumber number1 = getNumber();
			PhoneNumber number2 = ((Contact) other).getNumber();
			return number1.equals(number2);
		}

		return false;
	}

	public void loadContactBasicContractInformation(Context context)
	{
		if (lookupKey == null || !loadContactBasicInfoUsingLookupKey(context))
		{
			loadContactBasicUsingPhoneLookup(context);
		}
	}

	public void call(Context c)
	{
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + number));

		if (intent.resolveActivity(c.getPackageManager()) != null)
		{
			c.startActivity(intent);
		}

	}

	public boolean viewDetails(Context c)
	{
		if (lookupKey != null && lookupKey.length() > 0)
		{
			Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
			Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
			
			if (intent.resolveActivity(c.getPackageManager()) != null)
			{
				c.startActivity(intent);
				
				return true;
			}
		}

		return false;
	}

	private boolean loadContactBasicInfoUsingLookupKey(Context context)
	{
		try
		{
			String[] projection = new String[]
			{ ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_ID };

			Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
			Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

			if (cursor != null)
			{
				if (cursor.moveToFirst())
				{// if contact is found
					name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
					int photoId = cursor.getInt(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID));
					photo = queryContactImage(photoId, context);
				}
				cursor.close();
			}

			return true;
		} catch (Exception e)
		{
			return false;
		}
	}

	private void loadContactBasicUsingPhoneLookup(Context context)
	{
		try
		{
			String[] projection = new String[]
			{ ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_ID , ContactsContract.PhoneLookup.LOOKUP_KEY};

			Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number.toString()));

			Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

			if (cursor != null)
			{
				if (cursor.moveToFirst())
				{// if contact is found
					name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
					int photoId = cursor.getInt(cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_ID));
					photo = queryContactImage(photoId, context);
					lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY));
				}
				cursor.close();
			}
		} catch (Exception e)
		{
		}
	}

	private Bitmap queryContactImage(int imageDataRow, Context context)
	{
		Cursor c = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, new String[]
		{ ContactsContract.CommonDataKinds.Photo.PHOTO }, ContactsContract.Data._ID + " = ? ", new String[]
		{ Integer.toString(imageDataRow) }, null);
		byte[] imageBytes = null;
		if (c != null)
		{
			if (c.moveToFirst())
			{
				imageBytes = c.getBlob(0);
			}
			c.close();
		}

		if (imageBytes != null)
		{
			return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		}
		else
		{
			return null;
		}
	}

	@Override
	public int compareTo(Contact another)
	{
		return toString().toUpperCase().compareTo(another.toString().toUpperCase());
	}

	public String toString()
	{
		if (name != null && !name.equals(""))
			return name;

		return number.toString();
	}

	public static ArrayList<Contact> getAllPhoneNumbersInPhonebook(Context context)
	{
		ArrayList<Contact> result = new ArrayList<>();

		String[] proj =
		{ ContactsContract.CommonDataKinds.Phone.NUMBER };

		Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, proj, null, null,
				null);
		String phoneNumber;
		Contact current;

		while (phones.moveToNext())
		{
			phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			current = new Contact(phoneNumber, context);

			if (current.number != null && !result.contains(current))
				result.add(current);
		}

		phones.close();

		return result;
	}
}
