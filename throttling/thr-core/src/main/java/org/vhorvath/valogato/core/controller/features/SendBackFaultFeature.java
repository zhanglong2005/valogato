package org.vhorvath.valogato.core.controller.features;


import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.feature.IFeature;
import org.vhorvath.valogato.common.simulation.ISimulatedService;


/**
 * @author Viktor Horvath
 */
public class SendBackFaultFeature<RQ, RS, EX extends Exception> implements IFeature<RQ, RS, EX> {

	
	public RS apply(String backendServiceName, 
			        RQ req, 
			        ISimulatedService<RQ, RS, EX> simulatedInterface, 
			        BackendServiceBean backendServiceBean,
			        String simulatedServiceName,
			        IThrottlingController<RQ, RS, EX> thrController) throws EX {
		throw simulatedInterface.buildFault("SendBackFaultFeature: the backend system is overwhelmed.");
	}

	
}
