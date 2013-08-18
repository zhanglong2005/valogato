package org.vhorvath.valogato.common.feature;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.simulation.ISimulatedService;

/**
 * @author Viktor Horvath
 */
public interface IFeature<RQ, RS, EX extends Exception> {

	
	RS apply(String backendServiceName, 
			 RQ req, 
			 ISimulatedService<RQ, RS, EX> simulatedInterface, 
			 BackendServiceBean backendServiceBean,
		     String simulatedServiceName,
			 IThrottlingController<RQ, RS, EX> thrController) throws EX, ThrottlingConfigurationException, ThrottlingRuntimeException;
	
}
