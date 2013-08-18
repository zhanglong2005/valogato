package org.vhorvath.valogato.web.utils.exception;

public class ThrottlingWebException extends Exception {

	private static final long serialVersionUID = -8281605942267429139L;

	public ThrottlingWebException(String message) {
		super(message);
	}
	
}
