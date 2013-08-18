package org.vhorvath.valogato.common.controller;

import java.util.Calendar;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.simulation.ISimulatedService;

/**
 * Interface class for using the Valogato throttling system.
 * 
 * @author Viktor Horvath
 */
public interface IThrottlingController<RQ, RS, EX extends Exception> {

	/*
	 * Starting to process the request by the Valogato system.
	 * This is the entry point of the throttling system.
	 * 
	 * @param req the request object
	 * @param simulatedInterface the instantiated ISimulatedService object (usually passed by the 'this' keyword)
	 * @param nameBackendService the name of the backend service being called
	 * @param simulatedServiceName the name of the simulated service
	 * @return the response object getting back from the backend service
	 * @throws EX the exception being thrown by the backend service or a feature
	 */	
	RS processRequest(RQ req,
                      ISimulatedService<RQ, RS, EX> simulatedInterface,			          
			          String nameBackendService,
			          String simulatedServiceName) throws EX;

	/*
	 * The method processRequestAfterSleeping mustn't be called!
	 * This method is only called from inside the throttling system.
	 */	
	RS processRequestAfterSleeping(RQ req,
                                   ISimulatedService<RQ, RS, EX> simulatedInterface,
                                   String nameBackendService,
                                   String simulatedServiceName,
                                   BackendServiceBean backendServiceBean) throws EX;

	/*
	 * Getting the start time of the processing.
	 * 
	 * @return the start time of the processing
	 */	
	Calendar getStartOfProcessing();

	/*
	 * Getting the unique ID of the request in the throttling system.
	 * 
	 * @return the unique ID of the request
	 */	
	String getRequestId();
	
}