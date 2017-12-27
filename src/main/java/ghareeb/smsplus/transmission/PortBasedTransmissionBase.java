package ghareeb.smsplus.transmission;

public class PortBasedTransmissionBase
{
    public final static int MESSAGE_BYTES_COUNT = 140;
	public final static short PORT = 5555;
	public final static int PORT_HEADER_BITS_COUNT = 7 * 8;
	public final static int MESSAGE_ID_BITS_COUNT = 5;
	public final static int PARTS_COUNT_BITS_COUNT = 5;
	public final static int PART_NUMBER_BITS_COUNT = 5;
	public final static int SINGLE_PART_MESSAGE_HEADER_BITS_COUNT = 1;
	public final static int MULTIPART_MESSAGE_HEADER_BITS_COUNT = SINGLE_PART_MESSAGE_HEADER_BITS_COUNT + MESSAGE_ID_BITS_COUNT
			+ PARTS_COUNT_BITS_COUNT + PART_NUMBER_BITS_COUNT;
	public final static int MESSAGE_BITS_COUNT = MESSAGE_BYTES_COUNT * 8;
}
