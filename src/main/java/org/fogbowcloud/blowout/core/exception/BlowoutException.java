package org.fogbowcloud.blowout.core.exception;

public class BlowoutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2520888793776997437L;

	public BlowoutException (String msg){
		super(msg);
	}
	
	public BlowoutException (String msg, Exception e){
		super(msg, e);
	}
}
