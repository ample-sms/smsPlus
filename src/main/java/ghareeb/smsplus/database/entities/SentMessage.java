package ghareeb.smsplus.database.entities;

import java.util.Date;

import android.os.Parcel;

public class SentMessage extends Message
{
	private String recepient;
	private boolean hasFailed;
	private boolean isDelivered;
	private boolean isSent;

	private Date deliveryDateTime;
	private int partsCount;//Only used for savings counting

	public SentMessage()
	{}
	
	public SentMessage(Parcel in)
	{
		super(in);
		recepient = in.readString();
		hasFailed = in.readByte() == 1;
		isDelivered = in.readByte() == 1;
		isSent = in.readByte() == 1;
		deliveryDateTime = new Date(in.readLong());
	}

	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		super.writeToParcel(out, flags);
		out.writeString(recepient);
		out.writeByte((byte) (hasFailed ? 1 : 0));
		out.writeByte((byte) (isDelivered ? 1 : 0));
		out.writeByte((byte) (isSent ? 1 : 0));
		out.writeLong(deliveryDateTime.getTime());
	}

	public static final Creator<SentMessage> CREATOR = new Creator<SentMessage>()
	{
		public SentMessage createFromParcel(Parcel in)
		{
			return new SentMessage(in);
		}

		public SentMessage[] newArray(int size)
		{
			return new SentMessage[size];
		}
	};

	public int getPartsCount()
	{
		return partsCount;
	}

	public void setPartsCount(int partsCount)
	{
		this.partsCount = partsCount;
	}
	
	public boolean isSent()
	{
		return isSent;
	}

	public void setSent(boolean isSent)
	{
		this.isSent = isSent;
	}

	public String getRecepient()
	{
		return recepient;
	}

	public void setRecepient(String recepient)
	{
		this.recepient = recepient;
	}

	public boolean isHasFailed()
	{
		return hasFailed;
	}

	public void setHasFailed(boolean hasFailed)
	{
		this.hasFailed = hasFailed;
	}

	public boolean isDelivered()
	{
		return isDelivered;
	}

	public void setDelivered(boolean isDelivered)
	{
		this.isDelivered = isDelivered;
	}


	public Date getDeliveryDateTime()
	{
		return deliveryDateTime;
	}

	public void setDeliveryDateTime(Date deliveryDateTime)
	{
		this.deliveryDateTime = deliveryDateTime;
	}



	public String getFormattedDeliveryDateTime()
	{
		return formatDate(deliveryDateTime);
	}

	@Override
	protected Date getComparisonDate()
	{
		return getSendDateTime();
	}

	@Override
	public void copyTo(Message message)
	{
		super.copyTo(message);
		SentMessage other = (SentMessage) message;
		other.recepient = recepient;
		other.deliveryDateTime = deliveryDateTime;
		other.hasFailed = hasFailed;
		other.isDelivered = isDelivered;
		other.isSent = isSent;
	}
}
