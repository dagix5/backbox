package it.backbox.exception;

public class BackBoxException extends Exception {
	
	public BackBoxException(String string) {
		super(string);
	}
	
	public BackBoxException(String message, Throwable cause) {
		super(message, cause);
	}
	
	private static final long serialVersionUID = 1L;

}
