package com.mycorp;


/**
 * 
 * @author Andres.Vicente
 *
 */
public class ZendeskException  extends RuntimeException {

	/**
	 *
	 */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param message
     */
    public ZendeskException(String message) {
        super(message);
    }

    /**
     * 
     */
    public ZendeskException() {
    }

    /**
     * 
     * @param cause
     */
    public ZendeskException(Throwable cause) {
        super(cause);
    }

    /**
     * 
     * @param message
     * @param cause
     */
    public ZendeskException(String message, Throwable cause) {
        super(message, cause);
    }
}
