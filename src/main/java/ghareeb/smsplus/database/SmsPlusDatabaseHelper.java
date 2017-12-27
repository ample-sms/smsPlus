package ghareeb.smsplus.database;

import ghareeb.smsplus.R;
import ghareeb.smsplus.common.PhoneNumber;
import ghareeb.smsplus.database.entities.Contact;
import ghareeb.smsplus.database.entities.Message;
import ghareeb.smsplus.database.entities.ReceivedMessage;
import ghareeb.smsplus.database.entities.SentMessage;
import ghareeb.smsplus.database.entities.ThreadEntity;
import ghareeb.smsplus.helper.SharedPreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

public class SmsPlusDatabaseHelper extends SQLiteOpenHelper
{
	// TODO put synchronizers
	public static final class CreateStatements
	{
		private static final String CREATE_TABLE = "CREATE TABLE ";
		private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY AUTOINCREMENT ,";
		private static final String INTEGER = " INTEGER, ";
		private static final String TEXT = " TEXT, ";

		private static final String CREATE_CONTACT_TABLE = CREATE_TABLE + SmsPlusContract.Contact.TABLE_NAME + " ("
				+ SmsPlusContract.Contact._ID + PRIMARY_KEY + SmsPlusContract.Contact.COLUMN_NAME_PHONE_NUMBER + TEXT
				+ SmsPlusContract.Contact.COLUMN_NAME_LOOKUP_KEY + TEXT + SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED
				+ " INTEGER);";

		public static final String CREATE_THREAD_TABLE = CREATE_TABLE + SmsPlusContract.Thread.TABLE_NAME + " ("
				+ SmsPlusContract.Thread._ID + PRIMARY_KEY + SmsPlusContract.Thread.COLUMN_NAME_DRAFT + TEXT
				+ SmsPlusContract.Thread.COLUMN_NAME_CONTACT_ID + INTEGER + SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED
				+ " INTEGER, FOREIGN KEY ( " + SmsPlusContract.Thread.COLUMN_NAME_CONTACT_ID + " ) REFERENCES "
				+ SmsPlusContract.Contact.TABLE_NAME + " ( " + SmsPlusContract.Contact._ID + " ) );";

		public static final String CREATE_SENT_MESSAGE_TABLE = CREATE_TABLE + SmsPlusContract.SentMessage.TABLE_NAME + " ("
				+ SmsPlusContract.SentMessage._ID + PRIMARY_KEY + SmsPlusContract.SentMessage.COLUMN_NAME_MESSAGE_ID + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_ERROR_DETECTED + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_SEND_DATE_TIME + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_DELIVERY_DATE_TIME + INTEGER
				+ SmsPlusContract.SentMessage.COLUMN_NAME_BODY + TEXT + SmsPlusContract.SentMessage.COLUMN_NAME_THREAD_ID
				+ INTEGER + SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED + " INTEGER, FOREIGN KEY ( "
				+ SmsPlusContract.SentMessage.COLUMN_NAME_THREAD_ID + " ) REFERENCES " + SmsPlusContract.Thread.TABLE_NAME
				+ " ( " + SmsPlusContract.Thread._ID + " ) );";

		public static final String CREATE_RECEIVED_MESSAGE_TABLE = CREATE_TABLE + SmsPlusContract.ReceivedMessage.TABLE_NAME
				+ " (" + SmsPlusContract.ReceivedMessage._ID + PRIMARY_KEY
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_MESSAGE_ID + INTEGER
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_PARTS_COUNT + INTEGER
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_RECEIVE_DATE_TIME + INTEGER
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_SEND_DATE_TIME + INTEGER
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY + TEXT + SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN
				+ INTEGER + SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID + INTEGER
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED + " INTEGER, FOREIGN KEY ( "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID + " ) REFERENCES " + SmsPlusContract.Thread.TABLE_NAME
				+ " ( " + SmsPlusContract.Thread._ID + " ) );";;

		public static final String CREATE_RECEIVED_MESSAGE_PART_TABLE = CREATE_TABLE
				+ SmsPlusContract.ReceivedMessagePart.TABLE_NAME + " ( " + SmsPlusContract.ReceivedMessagePart._ID + PRIMARY_KEY
				+ SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_PART_NUMBER + INTEGER
				+ SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_PART_DATA + " BLOB, "
				+ SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_RECEIVED_MESSAGE_ID + " INTEGER, FOREIGN KEY ( "
				+ SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_RECEIVED_MESSAGE_ID + " ) REFERENCES "
				+ SmsPlusContract.ReceivedMessage.TABLE_NAME + " ( " + SmsPlusContract.ReceivedMessage._ID + " ) );";

	}

	public static final class DropStatements
	{
		private static final String DROP = "DROP TABLE IF EXISTS ";

		public static final String DROP_SENT_MESSAGE_TABLE = DROP + SmsPlusContract.SentMessage.TABLE_NAME;
		public static final String DROP_RECEIVED_MESSAGE_PART_TABLE = DROP + SmsPlusContract.ReceivedMessagePart.TABLE_NAME;
		public static final String DROP_RECEIVED_MESSAGE_TABLE = DROP + SmsPlusContract.ReceivedMessage.TABLE_NAME;
		public static final String DROP_THREAD_TABLE = DROP + SmsPlusContract.Thread.TABLE_NAME;
		public static final String DROP_CONTACT_TABLE = DROP + SmsPlusContract.Contact.TABLE_NAME;
	}

	private SQLiteDatabase database;
	private Context context;
	private final SelectHelper HELPER = new SelectHelper();
	private final SmsPlusContract.ReceivedMessage RECEIVED_M = new SmsPlusContract.ReceivedMessage();
	private final SmsPlusContract.SentMessage SENT_M = new SmsPlusContract.SentMessage();
	// private final SmsPlusContract.Thread THREAD = new
	// SmsPlusContract.Thread();
	private final SmsPlusContract.Contact CONTACT = new SmsPlusContract.Contact();

	private void openDatabaseIfNeeded()
	{
		if (database == null || !database.isOpen())
			database = getWritableDatabase();
	}

	public SmsPlusDatabaseHelper(Context context)
	{
		super(context, SmsPlusContract.DATABASE_NAME, null, SmsPlusContract.DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase arg0)
	{
		try
		{
			Toast.makeText(context, R.string.toast_preparing_database, Toast.LENGTH_SHORT);
		} catch (Exception e)
		{
			Log.e(SmsPlusDatabaseHelper.class.getName(), e.getMessage());
		}
		arg0.execSQL(CreateStatements.CREATE_CONTACT_TABLE);
		arg0.execSQL(CreateStatements.CREATE_THREAD_TABLE);
		arg0.execSQL(CreateStatements.CREATE_SENT_MESSAGE_TABLE);
		arg0.execSQL(CreateStatements.CREATE_RECEIVED_MESSAGE_TABLE);
		arg0.execSQL(CreateStatements.CREATE_RECEIVED_MESSAGE_PART_TABLE);

		// Log.i("Create", CreateStatements.CREATE_SENT_MESSAGE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
	{
		arg0.execSQL(DropStatements.DROP_RECEIVED_MESSAGE_PART_TABLE);
		arg0.execSQL(DropStatements.DROP_RECEIVED_MESSAGE_TABLE);
		arg0.execSQL(DropStatements.DROP_SENT_MESSAGE_TABLE);
		arg0.execSQL(DropStatements.DROP_THREAD_TABLE);
		arg0.execSQL(DropStatements.DROP_CONTACT_TABLE);
		this.onCreate(arg0);
	}

	/* Helper To All Tables */
	// ////////////////////////
	// ////////////////////////
	// ////////////////////////

	private boolean updateById(final String TABLE_NAME, final String ID_COLUMN_NAME, final long ID_VALUE, ContentValues values,
			boolean close)
	{
		openDatabaseIfNeeded();
		String selection = ID_COLUMN_NAME + " = ?";
		String[] selectionArgs =
		{ String.valueOf(ID_VALUE) };

		int rowsAffected = database.update(TABLE_NAME, values, selection, selectionArgs);

		if (close)
			database.close();

		return rowsAffected > 0;
	}

	private Cursor getById(final String TABLE_NAME, final String ID_COLUMN_NAME, final long ID_VALUE)
	{
		openDatabaseIfNeeded();

		String selection = ID_COLUMN_NAME + " = ?";
		String[] selectionArgs =
		{ String.valueOf(ID_VALUE) };
		String[] projection =
		{ "*" };

		Cursor result = database.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);

		return result;
	}

	private long insert(final String TABLE_NAME, ContentValues values, boolean close)
	{
		openDatabaseIfNeeded();
		long rowID = database.insert(TABLE_NAME, null, values);

		if (close)
			database.close();

		return rowID;
	}

	/* Contact Table */
	// ////////////////
	// ////////////////
	// ////////////////

	public int Contact_getRegisteredContactsCount()
	{
		openDatabaseIfNeeded();
		String selection = SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED + " > 0";
		Cursor row = database.query(SmsPlusContract.Contact.TABLE_NAME, new String[]
		{ "count(*)" }, selection, null, null, null, null);
		int result = -1;

		if (row != null)
		{
			if (row.moveToFirst())
			{
				result = row.getInt(0);
			}
			row.close();
		}

		database.close();

		return result;
	}

	// Automatically searches for a lookup key
	// Returns a Contact with entity attributes only
	public Contact Contact_insert(String phoneNumber, boolean isRegistered)
	{
		Contact result = Contact_getLookupKeyAndNumberOfPhoneNumber(phoneNumber);
		String lookupKey = null;

		if (result != null)
		{
			phoneNumber = result.getNumber().toString();
			lookupKey = result.getLookupKey();
		}
		else
		{
			result = new Contact();
			result.setNumber(PhoneNumber.parse(phoneNumber, context));
		}

		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED, isRegistered);
		vals.put(SmsPlusContract.Contact.COLUMN_NAME_PHONE_NUMBER, phoneNumber);

		if (lookupKey != null)
			vals.put(SmsPlusContract.Contact.COLUMN_NAME_LOOKUP_KEY, lookupKey);

		long id = insert(SmsPlusContract.Contact.TABLE_NAME, vals, true);

		if (id > 0)
		{
			result.setId(id);
			result.setRegistered(isRegistered);

			return result;
		}

		return null;
	}

	// TODO re check
	public Contact Contact_findContactByPhoneNumber(String phoneNumber)
	{
		openDatabaseIfNeeded();
		String selection = SmsPlusContract.Contact.COLUMN_NAME_PHONE_NUMBER + " = '"
				+ PhoneNumber.parse(phoneNumber, context).toString() + "'";
		Cursor all = database.query(SmsPlusContract.Contact.TABLE_NAME, null, selection, null, null, null, null);

		Contact current = null;

		if (all != null)
		{
			if (all.moveToNext())
			{
				current = Contact_createContactFromCursor(all);
			}

			all.close();
		}

		database.close();

		return current;
	}

	public void Contact_makeAllContactsUnregistered()
	{
		openDatabaseIfNeeded();
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED, false);
		database.update(SmsPlusContract.Contact.TABLE_NAME, vals, null, null);
		database.close();
	}

	public ArrayList<Contact> Contact_makeNumbersRegistered(String[] numbers)
	{
		ArrayList<Contact> result = new ArrayList<>();
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED, true);
		Contact current = null;

		for (String number : numbers)
		{
			current = Contact_findContactByPhoneNumber(number);

			if (current != null)
			{
				updateById(SmsPlusContract.Contact.TABLE_NAME, SmsPlusContract.Contact._ID, current.getId(), vals, true);
			}
			else
			{
				current = Contact_insert(number, true);
			}

			result.add(current);
		}

		return result;
	}

	public ArrayList<Contact> Contact_getAllContacts()
	{
		ArrayList<Contact> result = new ArrayList<>();
		openDatabaseIfNeeded();
		Cursor rows = database.query(SmsPlusContract.Contact.TABLE_NAME, null, null, null, null, null, null);

		if (rows != null)
		{
			while (rows.moveToNext())
			{
				result.add(Contact_createContactFromCursor(rows));
			}

			rows.close();
		}

		database.close();

		return result;
	}

	public ArrayList<Contact> Contact_getAllRegisteredContacts()
	{
		String selection = SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED + " > 0";
		ArrayList<Contact> result = new ArrayList<>();
		openDatabaseIfNeeded();
		Cursor rows = database.query(SmsPlusContract.Contact.TABLE_NAME, null, selection, null, null, null, null);

		if (rows != null)
		{
			while (rows.moveToNext())
			{
				result.add(Contact_createContactFromCursor(rows));
			}

			rows.close();
		}

		database.close();

		return result;
	}

	// The threads should be not deleted
	public ArrayList<Contact> Contact_getAllContactsWithThreads()
	{
		final String statement = "Select " + HELPER.getFullyQualifiedCols(CONTACT) + " From "
				+ SmsPlusContract.Contact.TABLE_NAME + ", " + SmsPlusContract.Thread.TABLE_NAME + " Where "
				+ SmsPlusContract.Contact.TABLE_NAME + "." + SmsPlusContract.Contact._ID + " = "
				+ SmsPlusContract.Thread.TABLE_NAME + "." + SmsPlusContract.Thread.COLUMN_NAME_CONTACT_ID + " AND "
				+ SmsPlusContract.Thread.TABLE_NAME + "." + SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED + " = 0 ";

		openDatabaseIfNeeded();
		Cursor rows = database.rawQuery(statement, null);
		ArrayList<Contact> result = new ArrayList<>();

		if (rows != null)
		{
			while (rows.moveToNext())
			{
				result.add(Contact_createContactFromCursor(rows));
			}
			rows.close();
		}

		database.close();

		return result;
	}

	public Contact Contact_getContactById(long contactId)
	{
		Cursor c = getById(SmsPlusContract.Contact.TABLE_NAME, SmsPlusContract.Contact._ID, contactId);
		Contact result = null;

		if (c != null)
		{
			if (c.moveToNext())
				result = Contact_createContactFromCursor(c);
			c.close();
		}

		database.close();

		return result;
	}

	// Fixes partially erratic phoneNumber
	private Contact Contact_getLookupKeyAndNumberOfPhoneNumber(String phoneNumber)
	{
		Contact result = null;

		try
		{
			String[] projection = new String[]
			{ ContactsContract.PhoneLookup.LOOKUP_KEY, ContactsContract.PhoneLookup.NUMBER };

			Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

			Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

			if (cursor != null)
			{
				if (cursor.moveToFirst())
				{// if contact is found
					result = new Contact();
					result.setLookupKey(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY)));
					result.setNumber(cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NUMBER)), context);
				}

				cursor.close();
			}
		} catch (Exception e)
		{
		}

		return result;
	}

	private Contact Contact_createContactFromCursor(Cursor row)
	{
		Contact result = new Contact();
		result.setId(row.getLong(row.getColumnIndex(SmsPlusContract.Contact._ID)));
		result.setNumber(row.getString(row.getColumnIndex(SmsPlusContract.Contact.COLUMN_NAME_PHONE_NUMBER)), context);
		result.setLookupKey(row.getString(row.getColumnIndex(SmsPlusContract.Contact.COLUMN_NAME_LOOKUP_KEY)));
		result.setRegistered(row.getInt(row.getColumnIndex(SmsPlusContract.Contact.COLUMN_NAME_IS_REGISTERED)) > 0);

		return result;
	}

	/* ThreadEntity Table */
	// ////////////////
	// ////////////////
	// ////////////////
	private ThreadEntity Thread_createThreadFromCursor(Cursor row)
	{
		ThreadEntity result = new ThreadEntity();
		result.setId(row.getLong(row.getColumnIndex(SmsPlusContract.Thread._ID)));
		result.setContactId(row.getLong(row.getColumnIndex(SmsPlusContract.Thread.COLUMN_NAME_CONTACT_ID)));
		result.setDraft(row.getString(row.getColumnIndex(SmsPlusContract.Thread.COLUMN_NAME_DRAFT)));
		result.setDeleted(row.getInt(row.getColumnIndex(SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED)) > 0);

		return result;
	}

	public ThreadEntity Thread_getThreadOfContact(long contactId)
	{
		Cursor rows = getById(SmsPlusContract.Thread.TABLE_NAME, SmsPlusContract.Thread.COLUMN_NAME_CONTACT_ID, contactId);
		ThreadEntity current = null;

		if (rows != null)
		{
			if (rows.moveToNext())
			{
				current = Thread_createThreadFromCursor(rows);
			}

			rows.close();
		}

		database.close();

		return current;
	}

	public ThreadEntity Thread_findThreadOfPhoneNumber(String number)
	{
		Contact contact = Contact_findContactByPhoneNumber(number);

		if (contact != null)
		{
			return Thread_getThreadOfContact(contact.getId());
		}

		return null;
	}

	public ThreadEntity Thread_insert(long contactId)
	{
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.Thread.COLUMN_NAME_CONTACT_ID, contactId);
		vals.put(SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED, false);
		long id = insert(SmsPlusContract.Thread.TABLE_NAME, vals, true);

		if (id > 0)
		{
			SharedPreferencesHelper.incrementDBVersion(context);
			ThreadEntity result = new ThreadEntity();
			result.setContactId(contactId);
			result.setId(id);

			return result;
		}

		return null;
	}

	// Creates a contact and a thread if needed
	public ThreadEntity Thread_getThreadOfPhoneNumber(String phoneNumber)
	{
		Contact contact = Contact_findContactByPhoneNumber(phoneNumber);
		ThreadEntity thread = null;

		if (contact == null)
		{
			// Both contact and thread doesn't exist
			contact = Contact_insert(phoneNumber, false);
			thread = Thread_insert(contact.getId());
			thread.setContact(contact);
		}
		else
		{
			thread = Thread_getThreadOfContact(contact.getId());

			// A contact exist but no thread
			if (thread == null)
				thread = Thread_insert(contact.getId());
			else
				if (thread.isDeleted())
				{
					ContentValues vals = new ContentValues();
					vals.put(SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED, false);
					updateById(SmsPlusContract.Thread.TABLE_NAME, SmsPlusContract.Thread._ID, thread.getId(), vals, true);
				}
			// else both a contact and a thread exist

			thread.setContact(contact);
		}

		return thread;
	}

	public ArrayList<ThreadEntity> Thread_getAllThreadsFilled(boolean excludeDeleted)
	{
		ArrayList<ThreadEntity> result = new ArrayList<ThreadEntity>();
		openDatabaseIfNeeded();
		String condition = "";

		if (excludeDeleted)
		{
			condition = SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED + " = 0";
		}

		Cursor rows = database.query(SmsPlusContract.Thread.TABLE_NAME, null, condition, null, null, null, null);

		if (rows != null)
		{
			while (rows.moveToNext())
			{
				result.add(Thread_createThreadFromCursor(rows));
			}

			rows.close();
		}

		database.close();

		ArrayList<Contact> contacts = Contact_getAllContactsWithThreads();
		Contact current;

		for (ThreadEntity thread : result)
		{
			for (int i = 0; i < contacts.size(); i++)
			{
				current = contacts.get(i);

				if (current.getId() == thread.getContactId())
				{
					thread.setContact(current);
					contacts.remove(i);
					break;
				}
			}
		}

		return result;
	}

	public void Thread_loadBasicThreadMessagesInformation(ThreadEntity thread)
	{
		ReceivedMessage recentReceived = ReceivedMessage_getMostRecentReceivedMessageOfThread(thread, true);
		SentMessage recentSent = SentMessage_getMostRecentSentMessageOfThread(thread, true);

		if (recentReceived != null)
		{
			if (recentSent != null)
			{
				if (recentReceived.compareTo(recentSent) >= 0)
					thread.setMostRecentMessage(recentReceived);
				else
					thread.setMostRecentMessage(recentSent);
			}
			else
				thread.setMostRecentMessage(recentReceived);
		}
		else
		{
			if (recentSent != null)
				thread.setMostRecentMessage(recentSent);
			else
				thread.setMostRecentMessage(null);
		}

		thread.setUnSeenMessagesCount(ReceivedMessage_getUnseenReceivedMessagesCountOfThread(thread, true));
	}

	public void Thread_loadMessagesOfThread(ThreadEntity thread)
	{
		ArrayList<Message> messages = new ArrayList<Message>();
		messages.addAll(ReceivedMessage_getReceivedMessagesOfThread(thread, true, true));
		messages.addAll(SentMessage_getSentMessagesOfThread(thread, true, true));
		Collections.sort(messages);
		thread.setMessages(messages);
	}

	public void Thread_delete(ThreadEntity thread)
	{
		if (thread.getMessages() == null)
			Thread_loadMessagesOfThread(thread);

		ArrayList<Message> messages = thread.getMessages();

		for (Message message : messages)
		{
			if (message instanceof SentMessage)
				SentMessage_deleteMessage(message.getKey());
			else
				ReceivedMessage_deleteMessage(message.getKey());
		}

		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.Thread.COLUMN_NAME_IS_DELETED, true);
		updateById(SmsPlusContract.Thread.TABLE_NAME, SmsPlusContract.Thread._ID, thread.getId(), vals, true);
		SharedPreferencesHelper.incrementDBVersion(context);
	}

	public void Thread_updateDraft(long threadId, String newValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.Thread.COLUMN_NAME_DRAFT, newValue);
		updateById(SmsPlusContract.Thread.TABLE_NAME, SmsPlusContract.Thread._ID, threadId, vals, true);
		SharedPreferencesHelper.incrementDBVersion(context);
	}

	public String Thread_getDraft(long threadId)
	{
		String selection = SmsPlusContract.Thread._ID + " = ?";
		String[] args = new String[]
		{ String.valueOf(threadId) };
		String[] proj = new String[]
		{ SmsPlusContract.Thread.COLUMN_NAME_DRAFT };
		openDatabaseIfNeeded();
		Cursor rows = database.query(SmsPlusContract.Thread.TABLE_NAME, proj, selection, args, null, null, null);
		String result = null;

		if (rows != null)
		{
			if (rows.moveToFirst())
			{
				result = rows.getString(rows.getColumnIndex(SmsPlusContract.Thread.COLUMN_NAME_DRAFT));
			}
			rows.close();
		}

		database.close();

		return result;
	}

	/* Sent Message Table */
	// /////////////////////
	// /////////////////////
	// /////////////////////
	private SentMessage SentMessage_createSentMessageFromCursor(Cursor row)
	{
		SentMessage result = new SentMessage();
		result.setId(row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_MESSAGE_ID)));
		result.setKey(row.getLong(row.getColumnIndex(SmsPlusContract.SentMessage._ID)));
		result.setBody(row.getString(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_BODY)));
		// result.setRecipient(row.getString(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_RECEPIENT)));
		result.setThreadId(row.getLong(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_THREAD_ID)));
		result.setHasFailed(row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_ERROR_DETECTED)) > 0);
		result.setSendDateTime(new Date(row.getLong(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_SEND_DATE_TIME))));
		result.setDelivered(SentMessage_isMessageDelivered(result.getKey(), true));
		result.setSent(SentMessage_isMessageSent(result.getKey(), true));
		result.setDeleted(row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED)) > 0);

		if (!row.isNull(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_DELIVERY_DATE_TIME)))
			result.setDeliveryDateTime(new Date(row.getLong(row
					.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_DELIVERY_DATE_TIME))));
		else
			result.setDeliveryDateTime(null);

		return result;

	}

	private boolean SentMessage_updateByKey(long messageKey, ContentValues values, boolean close)
	{
		return updateById(SmsPlusContract.SentMessage.TABLE_NAME, SmsPlusContract.SentMessage._ID, messageKey, values, close);
	}

	private Cursor SentMessage_getByKey(long messageKey)
	{
		return getById(SmsPlusContract.SentMessage.TABLE_NAME, SmsPlusContract.SentMessage._ID, messageKey);
	}

	public SentMessage SentMessage_insert(long threadId, String messageBody, int messageIdPrecesionBinary, boolean close)
	{
		ContentValues values = new ContentValues();
		Date now = new Date(System.currentTimeMillis());
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT, 0);
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT, 0);
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_ERROR_DETECTED, false);
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_SEND_DATE_TIME, now.getTime());
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_THREAD_ID, threadId);
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_BODY, messageBody);
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED, false);
		values.putNull(SmsPlusContract.SentMessage.COLUMN_NAME_MESSAGE_ID);
		values.putNull(SmsPlusContract.SentMessage.COLUMN_NAME_DELIVERY_DATE_TIME);
		values.putNull(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT);

		long rowId = insert(SmsPlusContract.SentMessage.TABLE_NAME, values, close);
		int divisor = (int) Math.pow(2, messageIdPrecesionBinary);

		int messageId = (int) rowId % divisor;
		values.clear();
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_MESSAGE_ID, messageId);

		if (SentMessage_updateByKey(rowId, values, close))
		{
			SentMessage result = new SentMessage();
			result.setBody(messageBody);
			result.setKey(rowId);
			result.setId(messageId);
			// result.setRecipient(recepient);
			result.setThreadId(threadId);
			result.setDelivered(false);
			result.setHasFailed(false);
			result.setSent(false);
			result.setDeleted(false);
			result.setSendDateTime(now);
			SharedPreferencesHelper.incrementDBVersion(context);

			return result;
		}

		return null;
	}

	public boolean SentMessage_setErrorDetected(long messageKey, boolean close)
	{
		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_ERROR_DETECTED, true);
		return SentMessage_updateByKey(messageKey, values, close);
	}

	public boolean SentMessage_setPartsCount(long messageKey, int partsCount, boolean close)
	{
		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT, partsCount);
		return SentMessage_updateByKey(messageKey, values, close);
	}

	// Be ware of synchronization issues
	public boolean SentMessage_setPartSent(long messageKey, boolean close)
	{
		Cursor row = SentMessage_getByKey(messageKey);

		if (row.getCount() == 0)
		{
			row.close();

			if (close)
				database.close();

			return false;
		}

		row.moveToFirst();
		int partsCount = row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT));
		int sentPartsCount = row
				.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT));
		row.close();
		sentPartsCount++;

		if (partsCount < sentPartsCount)
		{
			if (close)
				database.close();

			return false;
		}

		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.SentMessage.COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT, sentPartsCount);

		return SentMessage_updateByKey(messageKey, values, close);
	}

	// synchronized because when all parts are delivered it updates the delivery
	// datetime
	public synchronized boolean SentMessage_setPartDelivered(long messageKey, boolean close)
	{
		Cursor row = SentMessage_getByKey(messageKey);
		boolean result = false;

		if (row != null)
		{
			if (row.getCount() > 0)
			{
				row.moveToFirst();
				int partsCount = row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT));
				int deliveredPartsCount = row.getInt(row
						.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT));

				deliveredPartsCount++;

				ContentValues values = new ContentValues();
				values.put(SmsPlusContract.SentMessage.COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT, deliveredPartsCount);

				if (partsCount <= deliveredPartsCount)
					values.put(SmsPlusContract.SentMessage.COLUMN_NAME_DELIVERY_DATE_TIME, System.currentTimeMillis());

				result = SentMessage_updateByKey(messageKey, values, close);
			}
			else
				result = false;

			row.close();
		}

		if (close)
			database.close();

		return result;
	}

	public boolean SentMessage_isMessageSent(long messageKey, boolean close)
	{
		Cursor row = SentMessage_getByKey(messageKey);
		boolean result = false;

		if (row != null)
		{
			if (row.moveToFirst())
			{
				int partsCount = row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT));
				int sentPartsCount = row.getInt(row
						.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT));

				if (partsCount == sentPartsCount)
					result = true;
			}

			row.close();
		}

		if (close)
			database.close();

		return result;
	}

	public boolean SentMessage_isMessageDelivered(long messageKey, boolean close)
	{
		Cursor row = SentMessage_getByKey(messageKey);
		boolean result = false;

		if (row != null)
		{
			if (row.moveToFirst())
			{
				int partsCount = row.getInt(row.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT));
				int deliveredPartsCount = row.getInt(row
						.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT));

				if (partsCount == deliveredPartsCount)
					result = true;
			}

			row.close();
		}

		if (close)
			database.close();

		return result;
	}

	public ArrayList<SentMessage> SentMessage_getSentMessagesOfThread(ThreadEntity thread, boolean close, boolean excludeDeleted)
	{
		String statement = "Select " + HELPER.getFullyQualifiedCols(SENT_M) + " From " + SmsPlusContract.SentMessage.TABLE_NAME
				+ " Where " + SmsPlusContract.SentMessage.COLUMN_NAME_BODY + " NOT NULL AND "
				+ SmsPlusContract.SentMessage.COLUMN_NAME_THREAD_ID + " = ?";

		if (excludeDeleted)
			statement += "AND " + SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED + " = 0";

		openDatabaseIfNeeded();
		Cursor rows = database.rawQuery(statement, new String[]
		{ String.valueOf(thread.getId()) });
		ArrayList<SentMessage> result = new ArrayList<SentMessage>();

		if (rows != null)
		{
			SentMessage current;

			while (rows.moveToNext())
			{
				current = SentMessage_createSentMessageFromCursor(rows);
				result.add(current);
			}

			rows.close();
		}

		if (close)
			database.close();

		return result;
	}

	public SentMessage SentMessage_getMostRecentSentMessageOfThread(ThreadEntity thread, boolean close)
	{
		final String statement = "Select " + HELPER.getFullyQualifiedCols(SENT_M) + ", MAX( "
				+ SmsPlusContract.SentMessage.COLUMN_NAME_SEND_DATE_TIME + " ) From " + SmsPlusContract.SentMessage.TABLE_NAME
				+ " Where " + SmsPlusContract.SentMessage.COLUMN_NAME_BODY + " NOT NULL AND "
				+ SmsPlusContract.SentMessage.COLUMN_NAME_THREAD_ID + " = ? AND "
				+ SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED + " = 0";

		openDatabaseIfNeeded();
		Cursor row = database.rawQuery(statement, new String[]
		{ String.valueOf(thread.getId()) });
		SentMessage result = null;

		if (row != null)
		{
			if (row.moveToFirst() && !row.isNull(row.getColumnIndex(SmsPlusContract.SentMessage._ID)))
			{
				result = SentMessage_createSentMessageFromCursor(row);
			}
			row.close();
		}

		if (close)
			database.close();

		return result;
	}

	public SentMessage SentMessage_getSentMessageByKey(long messageKey)
	{
		Cursor r = SentMessage_getByKey(messageKey);
		SentMessage result = null;

		if (r != null)
		{
			if (r.moveToFirst())
				result = SentMessage_createSentMessageFromCursor(r);

			r.close();
		}

		database.close();

		return result;
	}

	public void SentMessage_deleteMessage(long messageKey)
	{
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED, true);
		SentMessage_updateByKey(messageKey, vals, true);
		SharedPreferencesHelper.incrementDBVersion(context);
	}

	public Date SentMessage_getSendDateOfFirstSentMessage(boolean excludeDeleted)
	{
		String statement = "Select MIN( " + SmsPlusContract.SentMessage.COLUMN_NAME_SEND_DATE_TIME + " ) From "
				+ SmsPlusContract.SentMessage.TABLE_NAME + " Where " + SmsPlusContract.SentMessage.COLUMN_NAME_BODY
				+ " NOT NULL AND " + SmsPlusContract.SentMessage.COLUMN_NAME_ERROR_DETECTED + " = 0";

		if (excludeDeleted)
			statement += " AND " + SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED + " = 0";

		openDatabaseIfNeeded();
		Cursor row = database.rawQuery(statement, null);
		Date result = null;

		if (row != null)
		{
			if (row.moveToFirst())
			{
				long resultLong = row.getLong(0);

				if (resultLong >= 0)
					result = new Date(resultLong);
			}
			row.close();
		}

		database.close();

		return result;
	}

	public ArrayList<SentMessage> SentMessage_getMessageBodyAndPartsCountOfAllSentMessages(boolean excludeDeleted)
	{
		String[] cols =
		{ SmsPlusContract.SentMessage.COLUMN_NAME_BODY, SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT };
		String where = SmsPlusContract.SentMessage.COLUMN_NAME_BODY + " NOT NULL AND "
				+ SmsPlusContract.SentMessage.COLUMN_NAME_ERROR_DETECTED + " = 0 ";

		if (excludeDeleted)
			where += "AND " + SmsPlusContract.SentMessage.COLUMN_NAME_IS_DELETED + " = 0";

		ArrayList<SentMessage> result = new ArrayList<SentMessage>();
		SentMessage current = null;

		openDatabaseIfNeeded();

		Cursor rows = database.query(SmsPlusContract.SentMessage.TABLE_NAME, cols, where, null, null, null, null);

		if (rows != null)
		{
			while (rows.moveToNext())
			{
				current = new SentMessage();
				current.setBody(rows.getString(rows.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_BODY)));
				current.setPartsCount(rows.getInt(rows.getColumnIndex(SmsPlusContract.SentMessage.COLUMN_NAME_PARTS_COUNT)));
				result.add(current);
			}
			rows.close();
		}

		database.close();

		return result;
	}

	/* Received Message Table */
	// /////////////////////////
	// /////////////////////////
	// /////////////////////////

	private ReceivedMessage ReceivedMessage_createReceivedMessageFromCursor(Cursor row)
	{
		ReceivedMessage result = new ReceivedMessage();
		result.setId(row.getInt(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_MESSAGE_ID)));
		result.setKey(row.getLong(row.getColumnIndex(SmsPlusContract.ReceivedMessage._ID)));
		result.setBody(row.getString(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY)));
		result.setReceiveDateTime(new Date(row.getLong(row
				.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_RECEIVE_DATE_TIME))));
		// result.setSender(row.getString(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_SENDER)));
		result.setThreadId(row.getLong(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID)));
		result.setSeen(row.getInt(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN)) > 0);
		result.setSendDateTime(new Date(row.getLong(row
				.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_SEND_DATE_TIME))));
		result.setDeleted(row.getInt(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED)) > 0);

		return result;
	}

	// Depends on the assumption that message body is set to NULL in
	// incompletely received messages
	private Cursor ReceivedMessage_getById(int messageId)
	{
		openDatabaseIfNeeded();
		String[] projection =
		{ "*" };
		String selection = SmsPlusContract.ReceivedMessage.COLUMN_NAME_MESSAGE_ID + " = ? AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY + " IS NULL";
		String[] args =
		{ String.valueOf(messageId) };

		return database.query(SmsPlusContract.ReceivedMessage.TABLE_NAME, projection, selection, args, null, null, null);
	}

	private long ReceivedMessage_getMessageKey(int messageId)
	{
		long messageKey;
		Cursor temp = ReceivedMessage_getById(messageId);

		if (temp == null || temp.getCount() == 0)
			messageKey = -1;
		else
		{
			temp.moveToFirst();
			messageKey = temp.getLong(temp.getColumnIndex(SmsPlusContract.ReceivedMessage._ID));
		}

		temp.close();

		return messageKey;
	}

	private long ReceivedMessage_insert(int messageId, int partsCount, long threadId, boolean close)
	{
		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_MESSAGE_ID, messageId);
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_PARTS_COUNT, partsCount);
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_RECEIVE_DATE_TIME, System.currentTimeMillis());
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID, threadId);
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN, false);
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED, false);
		values.putNull(SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY);

		return insert(SmsPlusContract.ReceivedMessage.TABLE_NAME, values, close);
	}

	private int ReceivedMessage_getPartsCount(int messageId)
	{
		int partsCount = -1;
		Cursor message = ReceivedMessage_getById(messageId);

		if (message != null && message.getCount() > 0)
		{
			message.moveToFirst();
			partsCount = message.getInt(message.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_PARTS_COUNT));
		}

		message.getCount();

		return partsCount;
	}

	public boolean ReceivedMessage_isMessageReceived(int messageId, boolean close)
	{
		final int PARTS_COUNT = ReceivedMessage_getPartsCount(messageId);
		final long MESSAGE_KEY = ReceivedMessage_getMessageKey(messageId);
		boolean result = false;

		if (PARTS_COUNT > 0 && MESSAGE_KEY >= 0)
		{
			String[] projection =
			{ SmsPlusContract.ReceivedMessagePart._ID };
			String seletion = SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_RECEIVED_MESSAGE_ID + " = ?";
			String[] selectionArgs =
			{ String.valueOf(MESSAGE_KEY) };

			Cursor parts = database.query(SmsPlusContract.ReceivedMessagePart.TABLE_NAME, projection, seletion, selectionArgs,
					null, null, null);

			if (parts != null)
			{
				if (parts.getCount() == PARTS_COUNT)
					result = true;
			}

			parts.close();
		}

		if (close)
			database.close();

		return result;
	}

	public boolean ReceivedMessage_setMessageBodyAndSendTime(int messageId, String messageBody, long sendTime, boolean close)
	{
		final long MESSAGE_KEY = ReceivedMessage_getMessageKey(messageId);
		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY, messageBody);
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_SEND_DATE_TIME, sendTime);

		if (updateById(SmsPlusContract.ReceivedMessage.TABLE_NAME, SmsPlusContract.ReceivedMessage._ID, MESSAGE_KEY, values,
				close))
		{
			SharedPreferencesHelper.incrementDBVersion(context);

			return true;
		}

		return false;

	}

	public ArrayList<ReceivedMessage> ReceivedMessage_getReceivedMessagesOfThread(ThreadEntity thread, boolean close,
			boolean excludeDeleted)
	{
		String statement = "Select " + HELPER.getFullyQualifiedCols(RECEIVED_M) + " From "
				+ SmsPlusContract.ReceivedMessage.TABLE_NAME + " Where " + SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY
				+ " NOT NULL AND " + SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID + " = ? ";

		if (excludeDeleted)
			statement += "AND " + SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED + " = 0";

		openDatabaseIfNeeded();
		Cursor rows = database.rawQuery(statement, new String[]
		{ String.valueOf(thread.getId()) });
		ArrayList<ReceivedMessage> result = new ArrayList<ReceivedMessage>();

		if (rows != null)
		{
			ReceivedMessage current;
			while (rows.moveToNext())
			{
				current = ReceivedMessage_createReceivedMessageFromCursor(rows);
				result.add(current);
			}

			rows.close();
		}

		if (close)
			database.close();

		return result;
	}

	public ReceivedMessage ReceivedMessage_getMostRecentReceivedMessageOfThread(ThreadEntity thread, boolean close)
	{
		final String statement = "Select " + HELPER.getFullyQualifiedCols(RECEIVED_M) + ", MAX( "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_RECEIVE_DATE_TIME + " ) From "
				+ SmsPlusContract.ReceivedMessage.TABLE_NAME + " Where " + SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY
				+ " NOT NULL AND " + SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID + " = ? AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED + " = 0";

		openDatabaseIfNeeded();
		Cursor row = database.rawQuery(statement, new String[]
		{ String.valueOf(thread.getId()) });
		ReceivedMessage result = null;

		if (row != null)
		{
			if (row.moveToFirst() && !row.isNull(row.getColumnIndex(SmsPlusContract.ReceivedMessage._ID)))
			{
				result = ReceivedMessage_createReceivedMessageFromCursor(row);
			}
			row.close();
		}

		if (close)
			database.close();

		return result;
	}

	public ReceivedMessage ReceivedMessage_getReceivedMessageByKey(long key)
	{
		openDatabaseIfNeeded();
		String[] projecton =
		{ "*" };
		String selection = SmsPlusContract.ReceivedMessage._ID + " = ?";
		String[] args =
		{ String.valueOf(key) };
		Cursor r = database.query(SmsPlusContract.ReceivedMessage.TABLE_NAME, projecton, selection, args, null, null, null);

		ReceivedMessage result = null;

		if (r != null)
		{
			if (r.moveToFirst())
				result = ReceivedMessage_createReceivedMessageFromCursor(r);

			r.close();
		}

		database.close();

		return result;
	}

	public int ReceivedMessage_getUnseenReceivedMessagesCountOfThread(ThreadEntity thread, boolean close)
	{
		final String statement = "Select Count(*) From " + SmsPlusContract.ReceivedMessage.TABLE_NAME + " Where "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID + " = ? AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN + " = 0 AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED + " = 0";

		openDatabaseIfNeeded();
		Cursor rows = database.rawQuery(statement, new String[]
		{ String.valueOf(thread.getId()) });
		int result = -1;

		if (rows != null)
		{
			if (rows.moveToFirst())
			{
				result = rows.getInt(0);
			}
			rows.close();
		}

		if (close)
			database.close();

		return result;
	}

	public ArrayList<ReceivedMessage> ReceivedMessage_getAllUnseenMessages()
	{
		openDatabaseIfNeeded();
		String[] projection =
		{ "*" };
		String selection = SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN + " = ? AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY + " NOT NULL AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED + " = 0";
		String[] args =
		{ String.valueOf(0) };
		Cursor rows = database.query(SmsPlusContract.ReceivedMessage.TABLE_NAME, projection, selection, args, null, null, null);
		ArrayList<ReceivedMessage> result = new ArrayList<ReceivedMessage>();

		if (rows != null)
		{
			while (rows.moveToNext())
			{
				result.add(ReceivedMessage_createReceivedMessageFromCursor(rows));
			}
			rows.close();
		}

		database.close();

		return result;
	}

	public boolean ReceivedMessage_makeMessageSeen(long messageKey, boolean close)
	{
		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN, true);
		boolean result = updateById(SmsPlusContract.ReceivedMessage.TABLE_NAME, SmsPlusContract.ReceivedMessage._ID, messageKey,
				values, close);

		SharedPreferencesHelper.incrementDBVersion(context);
		return result;
	}

	public void ReceivedMessage_makeMessagesOfThreadSeen(long threadId)
	{
		String selection = SmsPlusContract.ReceivedMessage.COLUMN_NAME_THREAD_ID + " = ? AND "
				+ SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN + " = 0";
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_SEEN, true);

		openDatabaseIfNeeded();

		if (database.update(SmsPlusContract.ReceivedMessage.TABLE_NAME, vals, selection, new String[]
		{ String.valueOf(threadId) }) > 0)
			SharedPreferencesHelper.incrementDBVersion(context);
	}

	public void ReceivedMessage_deleteMessage(long messageKey)
	{
		ContentValues vals = new ContentValues();
		vals.put(SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED, true);
		updateById(SmsPlusContract.ReceivedMessage.TABLE_NAME, SmsPlusContract.ReceivedMessage._ID, messageKey, vals, true);
		SharedPreferencesHelper.incrementDBVersion(context);
	}

	public int ReceivedMessage_getAllReceivedMessagesCount(boolean excludeDeleted)
	{
		String[] cols =
		{ "Count(*)" };
		String selection = SmsPlusContract.ReceivedMessage.COLUMN_NAME_BODY + " NOT NULL";

		if (excludeDeleted)
			selection += " AND " + SmsPlusContract.ReceivedMessage.COLUMN_NAME_IS_DELETED + " = 0";

		int result = 0;

		openDatabaseIfNeeded();
		Cursor row = database.query(SmsPlusContract.ReceivedMessage.TABLE_NAME, cols, selection, null, null, null, null);

		if (row != null)
		{
			if (row.moveToFirst())
			{
				result = row.getInt(0);
			}

			row.close();
		}

		database.close();

		return result;
	}

	/* Received Message Part Table */
	// //////////////////////////////
	// //////////////////////////////
	// //////////////////////////////

	private Cursor ReceivedMessagePart_getByIds(long messageKey, int partId)
	{
		openDatabaseIfNeeded();

		String selection = SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_RECEIVED_MESSAGE_ID + " = ? AND "
				+ SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_PART_NUMBER + " = ?";
		String[] selectionArgs =
		{ String.valueOf(messageKey), String.valueOf(partId) };
		String[] projection =
		{ "*" };

		Cursor result = database.query(SmsPlusContract.ReceivedMessagePart.TABLE_NAME, projection, selection, selectionArgs,
				null, null, null);

		return result;
	}

	// TODO Synchronize
	public long ReceivedMessagePart_receivePart(int messageId, int partId, int partsCount, String sender, byte[] partData,
			boolean close)
	{
		long messageKey = ReceivedMessage_getMessageKey(messageId);

		if (messageKey < 0)
		{
			ThreadEntity thread = Thread_getThreadOfPhoneNumber(sender);
			messageKey = ReceivedMessage_insert(messageId, partsCount, thread.getId(), close);
		}

		ContentValues values = new ContentValues();
		values.put(SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_RECEIVED_MESSAGE_ID, messageKey);
		values.put(SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_PART_NUMBER, partId);
		values.put(SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_PART_DATA, partData);
		insert(SmsPlusContract.ReceivedMessagePart.TABLE_NAME, values, close);

		return messageKey;
	}

	private byte[] ReceivedMessagePart_getPartData(long messageKey, int partId)
	{
		Cursor row = ReceivedMessagePart_getByIds(messageKey, partId);
		byte[] result = null;

		if (row != null && row.getCount() > 0)
		{
			row.moveToFirst();
			result = row.getBlob(row.getColumnIndex(SmsPlusContract.ReceivedMessagePart.COLUMN_NAME_PART_DATA));
		}

		row.close();

		return result;
	}

	public ArrayList<byte[]> ReceivedMessagePart_getOrderedPartsData(int messageId, boolean close)
	{
		long messageKey;
		int partsCount;
		Cursor row = ReceivedMessage_getById(messageId);
		ArrayList<byte[]> result = null;

		if (row != null)
		{
			if (row.getCount() > 0)
			{
				row.moveToFirst();
				messageKey = row.getLong(row.getColumnIndex(SmsPlusContract.ReceivedMessage._ID));
				partsCount = row.getInt(row.getColumnIndex(SmsPlusContract.ReceivedMessage.COLUMN_NAME_PARTS_COUNT));
				result = new ArrayList<byte[]>();
				byte[] temp;

				for (int i = 0; i < partsCount; i++)
				{
					temp = ReceivedMessagePart_getPartData(messageKey, i);

					if (temp == null)
					{
						result = null;
						break;
					}
					else
						result.add(temp);
				}
			}
			row.close();
		}

		if (close)
			database.close();

		return result;
	}

}
