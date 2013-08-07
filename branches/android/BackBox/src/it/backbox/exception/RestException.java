package it.backbox.exception;

import it.backbox.client.rest.bean.BoxError;

import com.google.api.client.http.HttpResponseException;

public class RestException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private BoxError error;
	private HttpResponseException httpException;

	public RestException(String message) {
		super(message);
	}
	
	public RestException(String message, HttpResponseException httpException, BoxError error) {
		super(message);
		setHttpException(httpException);
		setError(error);
	}

	public BoxError getError() {
		return error;
	}

	private void setError(BoxError error) {
		this.error = error;
	}

	public HttpResponseException getHttpException() {
		return httpException;
	}

	private void setHttpException(HttpResponseException httpException) {
		this.httpException = httpException;
	}

}
