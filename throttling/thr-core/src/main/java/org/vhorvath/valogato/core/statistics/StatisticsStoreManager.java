package org.vhorvath.valogato.core.statistics;


import static com.google.common.base.Preconditions.checkArgument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.WaitingReqDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.feature.FeatureParamGetter;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;


/**
 * @author Viktor Horvath
 */
public class StatisticsStoreManager {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);

	
	public boolean canBackendBeCalled(String backendServiceName, 
			                          String requestId, 
			                          String simulatedServiceName, 
			                          BackendServiceBean backendServiceBean,
			                          boolean afterSleeping) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("### The method StatisticsStoreManager.canBackendBeCalled has been called! " +
				"backendServiceName = %s, requestId = %s, simulatedServiceName = %s, afterSleeping = %s", backendServiceName, requestId, 
				simulatedServiceName, Boolean.toString(afterSleeping)));
		
		checkArgument(backendServiceName != null);
		checkArgument(requestId != null);
		
		Integer numberOfServedReq = 0;
		
		// 1. apply a write lock on the backend service usage value in the distributed cache
		CacheDAOFactory.getCache().lock(ThrottlingUtils.getFreqKey(backendServiceName));
		
		try {
			boolean can = true;
			// get the beans
			BackendServiceFreqBean beanFreq = UsageDAOFactory.getDAO().getFreqOfBackendService(backendServiceName);
			BackendServiceSleepingReqBean beanSleepingReq = getBackendServiceSleepingReqBean(backendServiceBean, simulatedServiceName, backendServiceName);
			// 2. check the number of the served requests
			numberOfServedReq = beanFreq == null ? null : beanFreq.getFrequency();
			// check the max number of the service can be loaded
			Integer maxLoading = backendServiceBean.getMaxLoading();
			// check the number of sleeping requests
			Integer numberOfSleepingRequests = beanSleepingReq==null ? 0 : (beanSleepingReq.getNumberOfSleepingRequests()==null ? 0 : 
				beanSleepingReq.getNumberOfSleepingRequests());
			
			// if the backend service is not in the store yet then ...
			if (numberOfServedReq == null) {
				if (maxLoading > 0) {
					LOGGER.debug("### The backend service %s is not in the store yet, probably it will be registered... (it depends" +
							" on the result of the method WaitingReqDAOFactory.getDAO().isRequestNextWaiting(...).)");
					numberOfServedReq = 0;
					// we have to register it (in the end of the method if isRequestNextWaiting is true) and assign a value to it and return true
				} else {
					LOGGER.debug("### The max loading number is zero.");
					can = false;
				}
				
			} else {
				int freeSlots = getFreeSlots(maxLoading, numberOfServedReq, numberOfSleepingRequests, afterSleeping);
				// 3.a) if everything is all right then then register and return true
				if (freeSlots > 0) {
					LOGGER.info(String.format("### The backend service %s probably can be called...(it depends on the result of the method " +
							"WaitingReqDAOFactory.getDAO().isRequestNextWaiting(...).)", backendServiceName));
				}
				// 3.b) if not then return false
				else {
					LOGGER.info(String.format("### The backend service %s cannot be called! the number of the served " +
							"requests: %s, maximum loading: %s, the number of sleeping requests: %s", backendServiceName, 
							Integer.toString(numberOfServedReq), Integer.toString(maxLoading), Integer.toString(numberOfSleepingRequests)));
					can = false;					
				}
			}
			
			// 4. if the backend service may still be called then before calling the backend a check is needed if this request 
			//    started to wait earliest and whether or not there is an other request started waiting earlier
			// ------------ SCENARIO for testing the REGISTERING_REQUESTS_INDIVIDUALLY strategy ------------
			// waiting in soapUI is 12 sec, maxLoading = 1, period = 8 sec
			//    sec  0 - R1 	->	processing
			//    sec  2 - R2	->	waiting 8 sec
			//    sec  6 - R3	->	waiting 8 sec
			//    
			//    sec 10 - R2	->	wakes up -> no free slot => waiting 8 sec
			//    sec 12 - R1	->	processed => one free slot
			//    sec 14 - R3	->	wakes up -> there is one free slot but it is not the next one => waiting 8 sec
			//    sec 18 - R2	->	wakes up -> there is one free slot -> processing
			//    
			//    sec 22 - R3	->	wakes up -> no free slot => waiting 8 sec
			//    sec 30 - R3	->	wakes up -> no free slot => waiting 8 sec
			//    sec 33 - R2	-> 	processed => there is one free slot
			//    sec 38 - R3	->	wakes up -> there is one free slot -> processing
			//    sec 54 - R3	->	processed
			
			// if the backend service can be called but a sleeping has already happened (in the strategy MAINTAINING_FREE_SLOTS 
			//     a request must get a free slot immediately if this req had already slept at least once => that's why the variable 'can' can be true ) 
			//     then the waiting req list must be examined -> in case of new req (afterSleeping=false) it is sure that the new request is not in the waiting
			//     list, it will be put later if it is needed
			// see: StatisticsStoreManager.getFreeSlots(...) -> in case of afterSleeping the numberOfSleepingRequests won't be calculated
			if (can && afterSleeping) {
				if (!WaitingReqDAOFactory.getDAO().isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName)) {
					LOGGER.info("### The backend service cannot be called as there is other request that started to wait earlier...");
					can = false;
				}
			}
			
			// the frequency should be increased only in the end! if all the conditions have been evaluated
			if (can) {
				LOGGER.debug(String.format("### The frequency will be increased. numberOfServedReq=%s",numberOfServedReq));
				manage(backendServiceName, ThrConstants.OpType.REGISTER, numberOfServedReq);
				ThrottlingStorage.setChangedFreq(Boolean.TRUE);
				// the request id should be removed not to hold other request (it would hold other requests as the request id is removed from the waiting
				//   list only in the end of processing i.e. after calling the real service; imagine if 4 sec is needed to get back the response from the 
				//   real service)
				if (ThrottlingStorage.isAddedToWaitingReqList()) {
					LOGGER.debug(String.format("### Unregistering the request from waiting req list. backendservice = %s", backendServiceName));
					Integer waitingReqListMaxSize = FeatureParamGetter.getWaitingReqListMaxSize(backendServiceBean, simulatedServiceName);
					Integer maxNumberOfWaitingReqs = FeatureParamGetter.getMaxNumberOfWaitingReqs(backendServiceBean, simulatedServiceName);
					WaitingReqDAOFactory.getDAO().unregisterRequest(requestId, waitingReqListMaxSize, maxNumberOfWaitingReqs);
					ThrottlingStorage.setAddedToWaitingReqList(false);
				}
			}

			return can;
		} finally {
			// 5. release the write lock
			CacheDAOFactory.getCache().unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		}
	}
	
	
	private BackendServiceSleepingReqBean getBackendServiceSleepingReqBean(BackendServiceBean backendServiceBean, String simulatedServiceName, 
			String backendServiceName) throws ThrottlingConfigurationException {
		BackendServiceSleepingReqBean beanSleepingReq = null;
		// if the strategy is FAST then the nr. of sleeping req is not needed to lock
		String strategy = FeatureParamGetter.getStrategy(backendServiceBean, simulatedServiceName);
		// if the feature has got the REGISTERING_REQUESTS_INDIVIDUALLY strategy then the request must be added to the waiting req list.
		if (strategy != null && !strategy.equals(ThrConstants.FeatureParamValue.fast.toString())) {
			try {
				CacheDAOFactory.getCache().lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
				beanSleepingReq = UsageDAOFactory.getDAO().getSleepingReqOfBackendService(backendServiceName);
			} finally {
				CacheDAOFactory.getCache().unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
			}
		}
		return beanSleepingReq;
	}


	private int getFreeSlots(Integer maxLoading, Integer numberOfServedReq, Integer numberOfSleepingRequests, boolean afterSleeping) {
		int freeSlots = 0;
		if (!afterSleeping) {
			// free slots = maxLoading - numberOfServedReq - number of sleeping requests
			freeSlots = maxLoading - numberOfServedReq - numberOfSleepingRequests;
		} else {
			// free slots = maxLoading - numberOfServedReq
			// the value number of sleeping requests must be omit from the calculation as the request just being woken up has got priority 
			freeSlots = maxLoading - numberOfServedReq;
		}
		LOGGER.debug(String.format("### freeSlots:%s, numberOfServedReq: %s, getMaxLoading: %s, numberOfSleepingRequests: %s, " +
				"afterSleeping: %s", Integer.toString(freeSlots), Integer.toString(numberOfServedReq), Integer.toString(maxLoading), 
				Integer.toString(numberOfSleepingRequests), Boolean.toString(afterSleeping)));
		return freeSlots;
	}


	public void unregister(String backendServiceName) throws ThrottlingConfigurationException {
		try {
			// apply a write lock on the backend service usage value in the distributed cache
			CacheDAOFactory.getCache().lock(ThrottlingUtils.getFreqKey(backendServiceName));
			// get the BackendServiceUseBean
			BackendServiceFreqBean bean = UsageDAOFactory.getDAO().getFreqOfBackendService(backendServiceName);
			// decrease the frequency
			manage(backendServiceName, ThrConstants.OpType.UNREGISTER, bean.getFrequency());
		} finally {
			// release the write lock
			CacheDAOFactory.getCache().unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		}
	}
	
	
	private void manage(String backendServiceName, ThrConstants.OpType opType, Integer numberOfServedReq) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("### The method DiagnosticStoreManager.manage has been called! backendServiceName: %s, " +
				"opType: %s, numberOfServedReq: %d", backendServiceName, opType.toString(), numberOfServedReq));
		// 2. unregister
		if (numberOfServedReq == null) {
			throw new ThrottlingConfigurationException(String.format("The backend service %s has not been loaded yet!", 
					backendServiceName));
		} else {
			LOGGER.debug(String.format("### manage - numberOfServedReq: %s", numberOfServedReq));
			if (opType.equals(ThrConstants.OpType.UNREGISTER)) {
				UsageDAOFactory.getDAO().setFrequency(backendServiceName, numberOfServedReq == 0 ? 0 : numberOfServedReq-1);
			} else {
				UsageDAOFactory.getDAO().setFrequency(backendServiceName, numberOfServedReq+1);
			}
		}
	}
	
	
}