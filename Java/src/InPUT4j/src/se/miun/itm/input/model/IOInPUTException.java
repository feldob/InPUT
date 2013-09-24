package se.miun.itm.input.model;

/**
 * An {@link Exception} that signals that InPUT has encountered an I/O error.
 * 
 * @author Stefan Karlsson
 */
public class IOInPUTException extends InPUTException {
	
	private static final long serialVersionUID = 6403605639770168905L;
	
	/**
	 * Constructs an {@code IOInPUTException} with the given detail message.
	 * 
	 * @param message The detail message of this {@code IOInPUTException}.
	 */
	public IOInPUTException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code IOInPUTException} with the given detail message and cause.
	 * 
	 * @param message The detail message of this {@code IOInPUTException}.
	 * @param cause The cause of this {@code IOInPUTException}.
	 */
	public IOInPUTException(String message, Throwable cause) {
		super(message, cause);
	}
}
