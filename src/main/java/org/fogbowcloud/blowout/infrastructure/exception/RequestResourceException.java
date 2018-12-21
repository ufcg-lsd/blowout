package org.fogbowcloud.blowout.infrastructure.exception;

public class RequestResourceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5588125728287316546L;
	private static final String ERROR_MESSAGE_DEFAULT = "Specification is not valid";

	public RequestResourceException() {
		this(ERROR_MESSAGE_DEFAULT);
	}

	public RequestResourceException(String errorMsg){
		super(errorMsg);
	}
	
	public RequestResourceException(String errorMsg, Exception ex){
		super(errorMsg, ex);
	}
	
}
