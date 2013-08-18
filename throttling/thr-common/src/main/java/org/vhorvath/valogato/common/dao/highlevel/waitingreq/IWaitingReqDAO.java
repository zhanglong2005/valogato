package org.vhorvath.valogato.common.dao.highlevel.waitingreq;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

/**
 * @author Viktor Horvath
 */
public interface IWaitingReqDAO {

	boolean registerRequest(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName, Integer waitingReqListMaxSize, 
			Integer maxNumberOfWaitingReqs)	throws ThrottlingConfigurationException;
	
	boolean isRequestNextWaiting(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName)	
			throws ThrottlingConfigurationException;

	void unregisterRequest(String requestId, Integer waitingReqListMaxSize, Integer maxNumberOfWaitingReqs) 
			throws ThrottlingConfigurationException;
	
}
