package ghareeb.smsplus.common;

/**
 * Count information with an index indicating the index of the last processed character within the
 * message being processed.
 */
public class IndexedCountInformation extends CountInformation
{
	/**
	 * the index of the last processed character within the message being processed.
	 */
	private long index;

	/**
	 * getter method for the index
	 * @return the index of the last processed character within the message being processed.
	 */
	public long getIndex()
	{
		return index;
	}


	/**
	 * setter method
	 * @param index the new value of the index of the last processed character within the message
	 *                 being processed.
	 */
	public void setIndex(long index)
	{
		this.index = index;
	}


	/**
	 * creates a new instance of the <code>IndexedCountInformation</code>.
	 * @param remainingCharsInCurrentMessage the remaining characters in the current sms.
	 * @param messagePartsCount the total number of sms within the current message.
	 * @param index the index of the last processed letter within the message.
	 */
	public IndexedCountInformation(int remainingCharsInCurrentMessage, int messagePartsCount, long index)
	{
		super(remainingCharsInCurrentMessage, messagePartsCount);
		this.index = index;
	}

	/**
	 * creates a new instance of the <code>IndexedCountInformation</code>.
	 * @param info an instance of <code>IndexedCountInformation</code> used as a basis for this instance.
	 * @param index the index of the last processed letter within the message.
	 */
	public IndexedCountInformation(CountInformation info, long index)
	{
		this(info.getRemainingCharsInCurrentMessage(), info.getMessagePartsCount(), index);
	}

}
