package org.vhorvath.valogato.common.exception;


import java.util.List;


/**
 * @author Viktor Horvath
 */
public class ThrottlingConfigurationException extends Exception {

	
	private static final long serialVersionUID = -4602478683774860211L;
	
	private boolean loaded = true;
	private List<String> errors = null;

	
	public ThrottlingConfigurationException(String message) {
		super(message);
	}
	
	public ThrottlingConfigurationException(Throwable t) {
		super(t);
	}
	
	public ThrottlingConfigurationException(String message, Throwable t) {
		super(message, t);
	}

	public ThrottlingConfigurationException(boolean loaded) {
		this.loaded = loaded;
	}
	
	public ThrottlingConfigurationException(List<String> errors) {
		this.errors = errors;
	}
	

	public boolean isLoaded() {
		return loaded;
	}

	public List<String> getErrors() {
		return errors;
	}
	
}