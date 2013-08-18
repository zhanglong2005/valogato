package org.vhorvath.valogato.core.controller.features;


import static com.google.common.base.Preconditions.checkArgument;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.feature.IFeature;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.common.sleeping.SleepingInFeatureManager;


/**
 * @author Viktor Horvath
 */
public class WaitingFeature<RQ, RS, EX extends Exception> implements IFeature<RQ, RS, EX> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	// http://stackoverflow.com/questions/14195852/waiting-for-a-short-period-of-time-in-ejb-3-0
	public RS apply(String backendServiceName, 
			        RQ req, 
			        ISimulatedService<RQ, RS, EX> simulatedInterface, 
			        BackendServiceBean backendServiceBean,
			        String simulatedServiceName,
			        IThrottlingController<RQ, RS, EX> thrController) throws EX, ThrottlingConfigurationException, ThrottlingRuntimeException {
		checkArgument(backendServiceBean != null);
		checkArgument(backendServiceName != null);
		checkArgument(simulatedInterface != null);
		checkArgument(thrController != null);

		// get the feature configuration
		FeatureBean featureBean = BackendServiceConfigDAOFactory.getDAO().getFeature(backendServiceBean, simulatedServiceName);
		if (featureBean == null) {
			throw new ThrottlingConfigurationException(String.format("No feature has been found for the backend service %s!", backendServiceName));
		}
		
		// waiting
		SleepingInFeatureManager sleepingManager = new SleepingInFeatureManager();
		sleepingManager.wait(backendServiceName,featureBean);
		
		// check if the average response time has been exceeded
		Integer averageResponseTime = BackendServiceConfigDAOFactory.getDAO().getAverageResponseTime(backendServiceName);
		averageResponseTime = averageResponseTime == null ? ThrConstants.DEFAULT_AVERAGE_RESPONSE_TIME : averageResponseTime;
		if (Calendar.getInstance().getTimeInMillis() - thrController.getStartOfProcessing().getTimeInMillis() > averageResponseTime) {
			LOGGER.debug(String.format("WaitingFeature: the average response (%s) time has been exceeded!",	averageResponseTime.toString()));
			// throw a timeout exception
			throw simulatedInterface.buildFault(String.format("WaitingFeature: the average response (%s) time has been exceeded!", 
					averageResponseTime.toString()));
		} else {
			// after waiting trying again...
			return thrController.processRequestAfterSleeping(req, simulatedInterface, backendServiceName, simulatedServiceName, backendServiceBean);
		}
		
	}


}