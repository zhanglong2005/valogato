package org.vhorvath.valogato.common.exception;

/**
 * @author Viktor Horvath
 */
public class ThrottlingRuntimeException extends Exception {

	private static final long serialVersionUID = 4372789339577996749L;

	public ThrottlingRuntimeException() {
		super();
	}

	public ThrottlingRuntimeException(Throwable t) {
		super(t);
	}
	
	public ThrottlingRuntimeException(String message) {
		super(message);
	}

	public ThrottlingRuntimeException(String message, Throwable t) {
		super(message, t);
	}

}
