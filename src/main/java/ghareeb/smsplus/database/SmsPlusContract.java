package ghareeb.smsplus.database;

import android.provider.BaseColumns;

public final class SmsPlusContract
{
	private SmsPlusContract()
	{
	}

	public static final String DATABASE_NAME = "SmsPlusDB.db";
	public static final int DATABASE_VERSION = 20;

	public static final class SentMessage implements BaseColumns, Table
	{
		public static final String TABLE_NAME = "SentMessage";

		// Columns
		public static final String COLUMN_NAME_MESSAGE_ID = "MessageId";
		public static final String COLUMN_NAME_THREAD_ID = "ThreadId";
		public static final String COLUMN_NAME_PARTS_COUNT = "PartsCount";
		public static final String COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT = "PartSentNotificationsCount";
		public static final String COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT = "PartDeliveredNotificationsCount";
		public static final String COLUMN_NAME_ERROR_DETECTED = "ErrorDetected";
		public static final String COLUMN_NAME_SEND_DATE_TIME = "SendDataTime";
		public static final String COLUMN_NAME_DELIVERY_DATE_TIME = "DeliveryDateTime";
		public static final String COLUMN_NAME_BODY = "Body";
		public static final String COLUMN_NAME_IS_DELETED = "IsDeleted";
		
		public String getTableName()
		{
			return TABLE_NAME;
		}
		
		public String[] getColumnNames()
		{
			return all;
		}
		private static String[] all = 
		{
			_ID, COLUMN_NAME_MESSAGE_ID, COLUMN_NAME_THREAD_ID, COLUMN_NAME_PARTS_COUNT, COLUMN_NAME_PART_SENT_NOTIFICATIONS_COUNT,
			COLUMN_NAME_PART_DELIVERED_NOTIFICATIONS_COUNT, COLUMN_NAME_ERROR_DETECTED, COLUMN_NAME_SEND_DATE_TIME, COLUMN_NAME_DELIVERY_DATE_TIME,
			COLUMN_NAME_BODY, COLUMN_NAME_IS_DELETED
		};
	}

	public static final class ReceivedMessagePart implements BaseColumns, Table
	{
		public static final String TABLE_NAME = "ReceivedMessagePart";

		// Columns
		public static final String COLUMN_NAME_RECEIVED_MESSAGE_ID = "ReceivedMessageId";
		public static final String COLUMN_NAME_PART_NUMBER = "PartNumber";
		public static final String COLUMN_NAME_PART_DATA = "PartData";
		
		public String getTableName()
		{
			return TABLE_NAME;
		}
		
		public String[] getColumnNames()
		{
			return all;
		}
		private static String[] all = 
		{
			_ID, COLUMN_NAME_RECEIVED_MESSAGE_ID, COLUMN_NAME_PART_NUMBER, COLUMN_NAME_PART_DATA
		};
	}

	public static final class ReceivedMessage implements BaseColumns, Table
	{
		public static final String TABLE_NAME = "ReceivedMessage";

		// Columns
		public static final String COLUMN_NAME_MESSAGE_ID = "MessageId";
		public static final String COLUMN_NAME_THREAD_ID = "ThreadId";
		public static final String COLUMN_NAME_PARTS_COUNT = "PartsCount";
		public static final String COLUMN_NAME_RECEIVE_DATE_TIME = "ReceiveDateTime";
		public static final String COLUMN_NAME_BODY = "Body";
		public static final String COLUMN_NAME_IS_SEEN = "IsSeen";
		public static final String COLUMN_NAME_SEND_DATE_TIME = "SendDateTime";
		public static final String COLUMN_NAME_IS_DELETED = "IsDeleted";
		
		public String getTableName()
		{
			return TABLE_NAME;
		}
		
		public String[] getColumnNames()
		{
			return all;
		}
		private static String[] all = 
		{
			_ID, COLUMN_NAME_MESSAGE_ID, COLUMN_NAME_THREAD_ID, COLUMN_NAME_PARTS_COUNT,COLUMN_NAME_RECEIVE_DATE_TIME
			, COLUMN_NAME_BODY, COLUMN_NAME_IS_SEEN, COLUMN_NAME_SEND_DATE_TIME, COLUMN_NAME_IS_DELETED
		};
	}

	public static final class Contact implements BaseColumns, Table
	{
		public static final String TABLE_NAME = "Contact";
		
		//Columns
		public static final String COLUMN_NAME_PHONE_NUMBER="PhoneNumber";
		public static final String COLUMN_NAME_LOOKUP_KEY = "LookupKey";
		public static final String COLUMN_NAME_IS_REGISTERED = "IS_REGISTERED";
		
		public String getTableName()
		{
			return TABLE_NAME;
		}
		
		public String[] getColumnNames()
		{
			return all;
		}
		private static String[] all = 
		{
			_ID, COLUMN_NAME_PHONE_NUMBER, COLUMN_NAME_LOOKUP_KEY, COLUMN_NAME_IS_REGISTERED
		};
	}
	
	public static final class Thread implements BaseColumns, Table
	{
		public static final String TABLE_NAME = "ThreadEntity";
		
		//Columns
		public static final String COLUMN_NAME_CONTACT_ID = "ContactId";
		public static final String COLUMN_NAME_DRAFT = "Draft";
		public static final String COLUMN_NAME_IS_DELETED = "IsDeleted";
		
		public String getTableName()
		{
			return TABLE_NAME;
		}
		
		public String[] getColumnNames()
		{
			return all;
		}
		private static String[] all = 
		{
			_ID, COLUMN_NAME_CONTACT_ID, COLUMN_NAME_DRAFT, COLUMN_NAME_IS_DELETED
		};
	}
}
