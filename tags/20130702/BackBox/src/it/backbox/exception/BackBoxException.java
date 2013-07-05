package it.backbox.exception;

import java.sql.SQLException;

public class BackBoxException extends Exception {
	
	public BackBoxException(String string) {
		super(string);
	}
	
	public BackBoxException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public BackBoxException(SQLException sqle, String query) {
		super("Database: error executing query: " + query, sqle);
	}

	private static final long serialVersionUID = 1L;

}
