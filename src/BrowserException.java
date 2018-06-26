
public class BrowserException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	//issue on code
	public BrowserException (String message, Object ... values) {
        super(String.format(message, values));
    }
    
    // caught exception with message
    public BrowserException (Throwable cause, String message, Object ... values) {
        super(String.format(message, values), cause);
    }

    // caught exception without message
    public BrowserException (Throwable exception) {
        super(exception);
    }
}
