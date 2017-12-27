package ghareeb.smsplus.encoding;

public class InvalidCodeException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -7211709551849433438L;

    public InvalidCodeException() {
	super("This code is not recognized by the encding.");
    }
    
    public InvalidCodeException(String message)
    {
	super(message);
    }
}
