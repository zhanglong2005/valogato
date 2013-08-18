package org.vhorvath.valogato.common.feature;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

/**
 * @author Viktor Horvath
 */
public final class FeatureParamGetter {

	private FeatureParamGetter() { }
	
	
	public static String getStrategy(BackendServiceBean backendServiceBean, String simulatedServiceName) throws ThrottlingConfigurationException {
		FeatureBean featureBean = BackendServiceConfigDAOFactory.getDAO().getFeature(backendServiceBean, simulatedServiceName);
		if (featureBean == null) {
			return null;
		}
		return getStrategy(featureBean);
	}


	public static String getStrategy(FeatureBean featureBean) throws ThrottlingConfigurationException {
		if (featureBean.getParams() == null) {
			return null;
		}
		String strategy = featureBean.getParams().get(ThrConstants.FeatureParam.strategy.toString());
		if (strategy == null) {
			return null;
		}
		if (strategy.length() == 0) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s cannot be empty!", ThrConstants.FeatureParam.strategy.toString()));			
		}
		if (!strategy.equals(ThrConstants.FeatureParamValue.maintiningFreeSlots.toString()) && !strategy.equals(ThrConstants.FeatureParamValue
				.registeringRequestsIndividually.toString()) && !strategy.equals(ThrConstants.FeatureParamValue.fast.toString())) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s can contain only '%s', '%s' or '%s'!", 
					ThrConstants.FeatureParam.strategy.toString(), 
					ThrConstants.FeatureParamValue.maintiningFreeSlots.toString(), 
					ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString(), 
					ThrConstants.FeatureParamValue.fast.toString()));
		}
		
		return strategy;
	}

	
	public static Integer getPeriod(BackendServiceBean backendServiceBean, String simulatedServiceName) throws ThrottlingConfigurationException {
		FeatureBean featureBean = BackendServiceConfigDAOFactory.getDAO().getFeature(backendServiceBean, simulatedServiceName);
		if (featureBean == null) {
			return null;
		}
		return getPeriod(featureBean);
	}
	
			
	public static Integer getPeriod(FeatureBean featureBean) throws ThrottlingConfigurationException {
		Integer period = null;
		try {
			period = Integer.parseInt(featureBean.getParams().get(ThrConstants.FeatureParam.period.toString()));
		} catch(NumberFormatException nfe) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s doesn't contain a number in the backend service" +
					" configuration for the feature %s! value = %s", 
					ThrConstants.FeatureParam.period.toString(), 
					featureBean.getName(), 
					featureBean.getParams().get(ThrConstants.FeatureParam.period.toString())));
		}
		
		if (period.intValue() > ThrConstants.MAX_WAITING_PERIOD) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s cannot be bigger than %n miliseconds in the " +
					"backend service configuration for the feature %s!", 
					ThrConstants.FeatureParam.period, 
					ThrConstants.MAX_WAITING_PERIOD, 
					featureBean.getName()));
		}
		
		return period;
	}
	
	
	public static Integer getWaitingReqListMaxSize(BackendServiceBean backendServiceBean, String simulatedServiceName) 
			throws ThrottlingConfigurationException {
		FeatureBean featureBean = BackendServiceConfigDAOFactory.getDAO().getFeature(backendServiceBean, simulatedServiceName);
		if (featureBean == null) {
			return null;
		}
		return getWaitingReqListMaxSize(featureBean);
	}
	
	
	public static Integer getWaitingReqListMaxSize(FeatureBean featureBean) throws ThrottlingConfigurationException {
		try {
			return Integer.parseInt(featureBean.getParams().get(ThrConstants.FeatureParam.waitingReqListMaxSize.toString()));
		} catch(NumberFormatException nfe) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s doesn't contain a number in the backend service" +
					" configuration for the feature %s! value = %s", 
					ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), 
					featureBean.getName(), 
					featureBean.getParams().get(ThrConstants.FeatureParam.waitingReqListMaxSize.toString())));
		}
	}
	
	
	public static Integer getMaxNumberOfWaitingReqs(BackendServiceBean backendServiceBean, String simulatedServiceName) 
			throws ThrottlingConfigurationException {
		FeatureBean featureBean = BackendServiceConfigDAOFactory.getDAO().getFeature(backendServiceBean, simulatedServiceName);
		if (featureBean == null) {
			return null;
		}
		return getMaxNumberOfWaitingReqs(featureBean);
	}
			
			
	public static Integer getMaxNumberOfWaitingReqs(FeatureBean featureBean) throws ThrottlingConfigurationException {
		try {
			return Integer.parseInt(featureBean.getParams().get(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString()));
		} catch(NumberFormatException nfe) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s doesn't contain a number in the backend service" +
					" configuration for the feature %s! value = %s", 
					ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), 
					featureBean.getName(), 
					featureBean.getParams().get(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString())));
		}
	}
	
	
	public static String[] getEndpoints(FeatureBean featureBean) throws ThrottlingConfigurationException {
		String endpointsValue = featureBean.getParams().get(ThrConstants.FeatureParam.endpoints.toString());
		if (endpointsValue == null || endpointsValue.length() == 0) {
			throw new ThrottlingConfigurationException(String.format("The parameter %s must contain one or more endpoints! " +
					"value = %s", ThrConstants.FeatureParam.endpoints.toString(), endpointsValue));
		}
		String[] endpoints = endpointsValue.split(";");
		return endpoints;	
	}
	
	
}
