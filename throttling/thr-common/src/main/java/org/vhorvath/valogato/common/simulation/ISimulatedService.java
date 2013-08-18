package org.vhorvath.valogato.common.simulation;

/**
 * Interface class for implementing a simulated service.
 * 
 * @author Viktor Horvath
 */
public interface ISimulatedService<RQ, RS, EX extends Exception> {

	/*
	 * Forwarding the request to the backend service and getting back the response.
	 * The backend service call logic must be implemented in this method.
	 * 
	 * @param req the request object
	 * @return the response object getting back from the backend service
	 * @throws EX the exception being thrown by the backend service
	 */
	RS forwardRequest(RQ req) throws EX;
	
	/*
	 * Building a standard exception object which will be thrown back to the client.
	 * 
	 * @param reason the text description of the reason of the exception
	 * @return the fault object that must be thrown back to the client (it must be a child of an Exception)
	 */
	EX buildFault(String reason);
	
	/*
	 * Setting the endpoint of the backend client.
	 * 
	 * @param endpoint a text representation of the endpoint
	 * @return the fault object that must be thrown back to the client (it must be a child of an Exception)
	 */
	void setEndpoint(String endpoint);
	
}
