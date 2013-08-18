package org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.IWaitingReqDAO;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

/**
 * @author Viktor Horvath
 */
public class DummyWaitingReqDAO implements IWaitingReqDAO {

	public boolean registerRequest(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName, Integer waitingReqListMaxSize, 
			Integer maxNumberOfWaitingReqs)	throws ThrottlingConfigurationException {
		throw new UnsupportedOperationException();
	}

	public boolean isRequestNextWaiting(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName) {
		throw new UnsupportedOperationException();
	}

	public void unregisterRequest(String requestId, Integer waitingReqListMaxSize, Integer maxNumberOfWaitingReqs)
			throws ThrottlingConfigurationException {
		throw new UnsupportedOperationException();
	}

}
