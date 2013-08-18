package org.vhorvath.valogato.common.sleeping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.feature.FeatureParamGetter;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

/**
 * @author Viktor Horvath
 */
public class SleepingInFeatureManager {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	
	public void wait(final String backendServiceName, final FeatureBean featureBean) throws ThrottlingRuntimeException, ThrottlingConfigurationException {
		Integer period = FeatureParamGetter.getPeriod(featureBean);
		String strategy = FeatureParamGetter.getStrategy(featureBean);
		
		// if necessary ... (as per configuration)
		if (strategy.equals(ThrConstants.FeatureParamValue.maintiningFreeSlots.toString()) || strategy.equals(ThrConstants.FeatureParamValue
				.registeringRequestsIndividually.toString())) {
			try {
				// apply a write lock on the backendservice usage value in the distributed cache
				CacheDAOFactory.getCache().lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
				// decreasing the number of waiting requests
				// TODO the name should rather be sleeping requests...
				UsageDAOFactory.getDAO().increaseNumberOfSleepingRequests(backendServiceName);
			} finally {
				// release the write lock
				CacheDAOFactory.getCache().unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
			}
		}
		
		// waiting
		try {
			LOGGER.debug(String.format("##### Sleeping for %s miliseconds.", Integer.toString(period)));
			Thread.sleep(period);
		} catch (InterruptedException e) {
			throw new ThrottlingRuntimeException("Exception in the feature when trying to sleep for " + period + " milliseconds!", e);
		}
		
		// if necessary ... (as per configuration)
		if (strategy.equals(ThrConstants.FeatureParamValue.maintiningFreeSlots.toString()) || strategy.equals(ThrConstants.FeatureParamValue
				.registeringRequestsIndividually.toString())) {
			try {
				// apply a write lock on the backendservice usage value in the distributed cache
				CacheDAOFactory.getCache().lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
				// decreasing the number of waiting requests
				UsageDAOFactory.getDAO().decreaseNumberOfSleepingRequests(backendServiceName);
			} finally {
				// release the write lock
				CacheDAOFactory.getCache().unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
			}
		}
	}

}