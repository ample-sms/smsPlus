package ghareeb.smsplus.encoding;

public class InvalidCharException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -4387090719385420175L;

    public InvalidCharException() {
	super("This char is not recognized by the encding.");
    }
    
    public InvalidCharException(String message)
    {
	super(message);
    }

}
