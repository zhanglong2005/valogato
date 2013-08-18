package org.vhorvath.valogato.core.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.WaitingReqDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.feature.FeatureParamGetter;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.core.statistics.StatisticsStoreManager;

/**
 * @author Viktor Horvath
 */
public class ResourceManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	public void releaseResources(String backendServiceName, String requestId, StatisticsStoreManager statisticsStoreManager, String simulatedServiceName) 
			throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		LOGGER.debug(String.format("### Releasing the reserved resources. isChangedFreq=%s, isChangedTheWaitingReqNumber=%s, isAddedToWaitingReqList=%s",
				ThrottlingStorage.isChangedFreq().toString(),
				ThrottlingStorage.isChangedTheSleepingReqNumber().toString(), 
				ThrottlingStorage.isAddedToWaitingReqList().toString()));
		
		// decrease the frequency
		if (ThrottlingStorage.isChangedFreq()) {
			LOGGER.debug(String.format("### Decreasing the number of frequency. backendservice = %s", backendServiceName));
			statisticsStoreManager.unregister(backendServiceName);
		}
		
		// Decreasing the number of waiting req number if it has not been decreased yet
		if (ThrottlingStorage.isChangedTheSleepingReqNumber()) {
			LOGGER.debug(String.format("### Decreasing the number of waiting requests as it has not been decreased yet. backendservice = %s", backendServiceName));
			UsageDAOFactory.getDAO().decreaseNumberOfSleepingRequests(backendServiceName);
		}
		
		// Unregister the request from waiting req list
		// The existence of the feature params are not needed to check because if the getRollbackNeededInWaitingReqList() returns true
		//   then they definitely exist
		if (ThrottlingStorage.isAddedToWaitingReqList()) {
			LOGGER.debug(String.format("### Unregistering the request from waiting req list. backendservice = %s", backendServiceName));
			BackendServiceBean backendServiceBean = BackendServiceConfigDAOFactory.getDAO().getBackendService(backendServiceName);
			Integer waitingReqListMaxSize = FeatureParamGetter.getWaitingReqListMaxSize(backendServiceBean, simulatedServiceName);
			Integer maxNumberOfWaitingReqs = FeatureParamGetter.getMaxNumberOfWaitingReqs(backendServiceBean, simulatedServiceName);
			WaitingReqDAOFactory.getDAO().unregisterRequest(requestId, waitingReqListMaxSize, maxNumberOfWaitingReqs);
		}
	}

}
