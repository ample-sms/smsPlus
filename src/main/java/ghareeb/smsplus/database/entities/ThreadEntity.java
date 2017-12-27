package ghareeb.smsplus.database.entities;

import java.util.ArrayList;

public class ThreadEntity implements Comparable<ThreadEntity>
{
	//Entity Attributes
	private long id;
	private long contactId;
	private String draft;
	private boolean isDeleted;
	
	//Buffers
	private Contact contact;
	private ArrayList<Message> messages;
	private Message mostRecentMessage;
	private int unSeenMessagesCount = 0;
	
	
	public boolean isDeleted()
	{
		return isDeleted;
	}
	public void setDeleted(boolean isDeleted)
	{
		this.isDeleted = isDeleted;
	}
	public ArrayList<Message> getMessages()
	{
		return messages;
	}
	public void setMessages(ArrayList<Message> messages)
	{
		this.messages = messages;
	}
	public Message getMostRecentMessage()
	{
		return mostRecentMessage;
	}
	public void setMostRecentMessage(Message mostRecentMessage)
	{
		this.mostRecentMessage = mostRecentMessage;
	}
	public int getUnSeenMessagesCount()
	{
		return unSeenMessagesCount;
	}
	public void setUnSeenMessagesCount(int unSeenMessagesCount)
	{
		this.unSeenMessagesCount = unSeenMessagesCount;
	}
	public Contact getContact()
	{
		return contact;
	}
	public void setContact(Contact contact)
	{
		this.contact = contact;
	}
	public long getId()
	{
		return id;
	}
	public void setId(long id)
	{
		this.id = id;
	}
	public long getContactId()
	{
		return contactId;
	}
	public void setContactId(long contactId)
	{
		this.contactId = contactId;
	}
	public String getDraft()
	{
		return draft;
	}
	public void setDraft(String draft)
	{
		this.draft = draft;
	}
	
	@Override
	public boolean equals(Object o)
	{
		return id == ((ThreadEntity)o).getId();
	}
	@Override
	public int compareTo(ThreadEntity another)
	{
		if (mostRecentMessage != null)
		{
			if (another.mostRecentMessage != null)
			{
				return -mostRecentMessage.compareTo(another.mostRecentMessage);
			}
			else
			{
				return 1;
			}
		}
		else
		{
			return 1;
		}
	}
	
}
