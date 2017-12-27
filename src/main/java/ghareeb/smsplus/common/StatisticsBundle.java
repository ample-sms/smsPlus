package ghareeb.smsplus.common;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A data transfer class for carrying statistics information (for presentation to user)
 *
 * @author Ghareeb Falazi
 * @since 05.02.2016
 */
public class StatisticsBundle implements Parcelable {
    /**
     * used to determine when the program was actually first used
     */
    private Date dateOfFirstSentMessage;
    /**
     * total number of sent sms's
     */
    private int numberOfSentMessageActualParts;
    /**
     * how many sms's would have been sent using a normal app and the very same texts
     */
    private int numberOfOrdinarySentMessageParts;
    /**
     * total number of whole-messages sent  (one could contain several sms's)
     */
    private int numberOfSentMessages;
    /**
     * total number of whole-messages received (one could contain several sms's)
     */
    private int numberOfReceivedMessages;

    /**
     * Getter method
     *
     * @return the total number of whole-messages received (one could contain several sms's)
     */
    public int getNumberOfReceivedMessages() {
        return numberOfReceivedMessages;
    }

    /**
     * Setter method
     *
     * @param numberOfReceivedMessages sets the total number of whole-messages received
     *                                 (one could contain several sms's)
     */
    public void setNumberOfReceivedMessages(int numberOfReceivedMessages) {
        this.numberOfReceivedMessages = numberOfReceivedMessages;
    }

    /**
     * Getter method.
     *
     * @return the total number of whole-messages sent (one could contain several sms's)
     */
    public int getNumberOfSentMessages() {
        return numberOfSentMessages;
    }

    /**
     * Setter method.
     *
     * @param numberOfSentMessages sets the total number of whole-messages sent
     *                             (one could contain several sms's)
     */
    public void setNumberOfSentMessages(int numberOfSentMessages) {
        this.numberOfSentMessages = numberOfSentMessages;
    }

    /**
     * Calculates the total number of sms's saved through using the app
     *
     * @return the total number of sms's saved through using the app
     */
    public int getSavedPartsCount() {
        return numberOfOrdinarySentMessageParts - numberOfSentMessageActualParts;
    }

    /**
     * Getter method.
     *
     * @return a date which is used to determine when the program was actually first used
     */
    public Date getDateOfFirstSentMessage() {
        return dateOfFirstSentMessage;
    }

    /**
     * Setter method.
     *
     * @param dateOfFirstSentMessage
     */
    public void setDateOfFirstSentMessage(Date dateOfFirstSentMessage) {
        this.dateOfFirstSentMessage = dateOfFirstSentMessage;
    }

    /**
     * Getter method.
     *
     * @return the total number of sent sms's
     */
    public int getNumberOfSentMessageActualParts() {
        return numberOfSentMessageActualParts;
    }

    /**
     * Setter method.
     *
     * @param numberOfSentMessageActualParts sets the total number of sent sms's.
     */
    public void setNumberOfSentMessageActualParts(int numberOfSentMessageActualParts) {
        this.numberOfSentMessageActualParts = numberOfSentMessageActualParts;
    }

    /**
     * Getter method.
     *
     * @return an <code>int</code> which indicates how many sms's would have been sent using a
     * normal app and the very same texts
     */
    public int getNumberOfOrdinarySentMessageParts() {
        return numberOfOrdinarySentMessageParts;
    }

    /**
     * Setter method.
     *
     * @param numberOfOrdinarySentMessageParts sets the <code>int</code> which indicates how many
     *                                         sms's would have been sent using a normal app
     *                                         and the very same texts
     */
    public void setNumberOfOrdinarySentMessageParts(int numberOfOrdinarySentMessageParts) {
        this.numberOfOrdinarySentMessageParts = numberOfOrdinarySentMessageParts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeLong(dateOfFirstSentMessage.getTime());
        arg0.writeInt(numberOfOrdinarySentMessageParts);
        arg0.writeInt(numberOfReceivedMessages);
        arg0.writeInt(numberOfSentMessageActualParts);
        arg0.writeInt(numberOfSentMessages);

    }

    /**
     * Generates instances of the <code>StatisticsBundle</code> class
     */
    public static final Creator<StatisticsBundle> CREATOR = new Creator<StatisticsBundle>() {
        public StatisticsBundle createFromParcel(Parcel in) {
            StatisticsBundle temp = new StatisticsBundle();
            temp.dateOfFirstSentMessage = new Date(in.readLong());
            temp.numberOfOrdinarySentMessageParts = in.readInt();
            temp.numberOfReceivedMessages = in.readInt();
            temp.numberOfSentMessageActualParts = in.readInt();
            temp.numberOfSentMessages = in.readInt();

            return temp;
        }

        public StatisticsBundle[] newArray(int size) {
            return new StatisticsBundle[size];
        }
    };

}