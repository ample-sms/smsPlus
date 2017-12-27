package ghareeb.smsplus.database.entities;

import java.util.Date;

import android.os.Parcel;

public class ReceivedMessage extends Message
{
	private String sender;
	private Date receiveDateTime;
	private boolean isSeen;

	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		super.writeToParcel(out, flags);
		out.writeString(sender);
		out.writeLong(receiveDateTime.getTime());
		out.writeByte((byte) (isSeen ? 1 : 0));
	}

	public static final Creator<ReceivedMessage> CREATOR = new Creator<ReceivedMessage>()
	{
		public ReceivedMessage createFromParcel(Parcel in)
		{
			return new ReceivedMessage(in);
		}

		public ReceivedMessage[] newArray(int size)
		{
			return new ReceivedMessage[size];
		}
	};

	public ReceivedMessage()
	{}
	
	public ReceivedMessage(Parcel in)
	{
		super(in);
		sender = in.readString();
		receiveDateTime = new Date(in.readLong());
		isSeen = in.readByte() == 1;
	}

	public boolean isSeen()
	{
		return isSeen;
	}

	public void setSeen(boolean isSeen)
	{
		this.isSeen = isSeen;
	}

	public Date getReceiveDateTime()
	{
		return receiveDateTime;
	}

	public void setReceiveDateTime(Date receiveDateTime)
	{
		this.receiveDateTime = receiveDateTime;
	}

	public String getSender()
	{
		return sender;
	}

	public void setSender(String sender)
	{
		this.sender = sender;
	}

	public String getFormattedReceiveDateTime()
	{
		return formatDate(receiveDateTime);
	}

	@Override
	protected Date getComparisonDate()
	{
		return getReceiveDateTime();
	}

}
