package ghareeb.smsplus.common;

/**
 * Container class for encapsulating the current counting information of a message being typed.
 */
public class CountInformation {
    /**
     * the remaining characters that can be typed in the current message using the same set of
     * already used languages, before a new SMS is accounted.
     */
    private int remainingCharsInCurrentMessage;
    /**
     * the total number of SMS's constructing a message.
     */
    private int messagePartsCount;

    /**
     * Creates a new instance of <code>CountInfo</code>.
     * @param remainingCharsInCurrentMessage the initial number of characters remaining in the current
     *                                       message before a new SMS is accounted for.
     * @param messagePartsCount the initial number of SMS's comprising the message.
     */
    public CountInformation(int remainingCharsInCurrentMessage, int messagePartsCount) {
        this.remainingCharsInCurrentMessage = remainingCharsInCurrentMessage;
        this.messagePartsCount = messagePartsCount;
    }

    /**
     * Getter method
     * @return gets the current number of characters remaining in the current message before a new
     * SMS is accounted for.
     */
    public int getRemainingCharsInCurrentMessage() {
        return remainingCharsInCurrentMessage;
    }

    /**
     * Setter method
     * @param remainingCharsInCurrentMessage sets the current number of characters remaining in the
     *                                       current message before a new SMS is accounted for.
     */
    public void setRemainingCharsInCurrentMessage(int remainingCharsInCurrentMessage) {
        this.remainingCharsInCurrentMessage = remainingCharsInCurrentMessage;
    }

    /**
     * Getter method
     * @return gets the current number of SMS's comprising the message.
     */
    public int getMessagePartsCount() {
        return messagePartsCount;
    }

    /**
     * Setter method
     * @param messagePartsCount sets the current number of SMS's comprising the message.
     */
    public void setMessagePartsCount(int messagePartsCount) {
        this.messagePartsCount = messagePartsCount;
    }

    /**
     * Returns a string representation of the instance.
     * @return a string representation of the instance in the format "remaining / sms count".
     */
    public String toString() {
        return String.format("%d / %d", getRemainingCharsInCurrentMessage(), getMessagePartsCount());
    }
}
