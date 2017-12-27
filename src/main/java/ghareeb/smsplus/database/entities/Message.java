package ghareeb.smsplus.database.entities;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Message implements Comparable<Message>, Parcelable
{
	private long key;
	private int id;
	private long threadId;
	private String body;
	private Date sendDateTime;
	private boolean isDeleted;

	public boolean isDeleted()
	{
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted)
	{
		this.isDeleted = isDeleted;
	}

	public Date getSendDateTime()
	{
		return sendDateTime;
	}

	public void setSendDateTime(Date sendDateTime)
	{
		this.sendDateTime = sendDateTime;
	}

	public long getThreadId()
	{
		return threadId;
	}

	public void setThreadId(long threadId)
	{
		this.threadId = threadId;
	}

	public long getKey()
	{
		return key;
	}

	public void setKey(long key)
	{
		this.key = key;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	protected String formatDate(Date date)
	{
		SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
		String result = format.format(date);

		return result;
	}

	protected abstract Date getComparisonDate();

	@Override
	public int compareTo(Message another)
	{
		return getComparisonDate().compareTo(another.getComparisonDate());
	}

	public void copyTo(Message message)
	{
		message.body = body;
		message.id = id;
		message.key = key;
		message.threadId = threadId;
		message.sendDateTime = sendDateTime;
	}

	public int describeContents()
	{
		return 0;
	}

	public void writeToParcel(Parcel out, int flags)
	{
		out.writeLong(key);
		out.writeInt(id);
		out.writeString(body);
		out.writeLong(threadId);
		out.writeLong(sendDateTime.getTime());
	}

	public Message()
	{
	}

	public Message(Parcel in)
	{
		key = in.readLong();
		id = in.readInt();
		body = in.readString();
		threadId = in.readLong();
		sendDateTime = new Date(in.readLong());
	}

	public String getFormattedSendDateTime()
	{
		return formatDate(sendDateTime);
	}

}
