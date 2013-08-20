package se.miun.itm.input.model;

/**
 * An {@link Exception} that signals that InPUT has encountered a SQL database error.
 * 
 * @author Stefan Karlsson
 */
public class SQLInPUTException extends InPUTException {
	private static final long serialVersionUID = 891481283130854209L;
	
	/**
	 * Constructs a {@code SQLInPUTException} with the given detail message.
	 * 
	 * @param message The detail message of this {@code SQLInPUTException}.
	 */
	public SQLInPUTException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a {@code SQLInPUTException} with the given detail message and cause.
	 * 
	 * @param message The detail message of this {@code SQLInPUTException}.
	 * @param cause The cause of this {@code SQLInPUTException}.
	 */
	public SQLInPUTException(String message, Throwable cause) {
		super(message, cause);
	}
}
