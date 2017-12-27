package ghareeb.smsplus.transmission.sending;

import java.util.BitSet;

import android.content.Context;


public interface ISender {

    void send(BitSet messageBinary, String toAddress, long messageKey, int messageId, int partsCount, Context context);
    
    int getBitsCountPerMessageId();
    
    int getSinglePartMessageHeaderLength();
	
	int getMultipartMessageHeaderLength();
}
