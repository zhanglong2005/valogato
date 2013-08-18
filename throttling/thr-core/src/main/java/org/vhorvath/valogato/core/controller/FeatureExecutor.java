package org.vhorvath.valogato.core.controller;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.vhorvath.valogato.core.controller.features.ForwarderFeature;
import org.vhorvath.valogato.core.controller.features.SendBackFaultFeature;
import org.vhorvath.valogato.core.controller.features.WaitingFeature;

/**
 * @author Viktor Horvath
 */
public class FeatureExecutor<RQ, RS, EX extends Exception> {


	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	// TODO perhaps implementing custom feature loading without using clazzStorage...? -> load the class every time when it is needed
//	private static Map<String, Class> clazzStorage = Collections.synchronizedMap(new HashMap<String, Class>());

	
	public RS applyFeature(RQ req, 
			               String backendServiceName, 
			               ISimulatedService<RQ, RS, EX> simulatedInterface,
			               String simulatedServiceName, 
			               BackendServiceBean backendServiceBean,
			               IThrottlingController<RQ, RS, EX> throttlingContoller) throws ThrottlingConfigurationException, EX, ThrottlingRuntimeException {
		// get the feature configuration
		FeatureBean featureBean = BackendServiceConfigDAOFactory.getDAO().getFeature(backendServiceBean, simulatedServiceName);
		if (featureBean == null) {
			throw new ThrottlingConfigurationException(String.format("No feature has been found for the backend service %s!", backendServiceName));
		}
		// create the feature that must be executed
		IFeature<RQ, RS, EX> feature = getFeatureImpl(featureBean.getName());
		// run the feature
		LOGGER.info(String.format("# The feature %s is going to be applied for the backend service %s. details: %s", 
				featureBean.getName(), backendServiceName, featureBean));
		
		return feature.apply(backendServiceName, req, simulatedInterface, backendServiceBean, simulatedServiceName, throttlingContoller);
	}


	private IFeature<RQ, RS, EX> getFeatureImpl(String nameFeature) throws ThrottlingConfigurationException {
		checkArgument(nameFeature != null);
		
		IFeature<RQ, RS, EX> feature = null;
		// built-in feature
		if (ThrConstants.Features.SendBackFaultFeature.toString().equals(nameFeature)) {
			feature = new SendBackFaultFeature<RQ, RS, EX>();
		} else if (ThrConstants.Features.WaitingFeature.toString().equals(nameFeature)) {
			feature = new WaitingFeature<RQ, RS, EX>();
		} else if (ThrConstants.Features.ForwarderFeature.toString().equals(nameFeature)) {
			feature = new ForwarderFeature<RQ, RS, EX>();
		} else {
			throw new ThrottlingConfigurationException(String.format("Unknown feature! feature=%s", nameFeature));
		}
//		// user defined features
//		else {
//			// allowing the user of framework to develop new feature(s) => defining this feature in a configuration file and
//			//    loading it runtime a cache was introduced to increase the velocity of class loading 
//			String clazzName = GeneralConfigurationUtils.getFeature(nameFeature).getClazz();
//			
//			feature = getUserDefinedFeature(clazzName);
//		}
		return feature;
	}
	
	
//	private IFeature<RQ, RS, EX> getUserDefinedFeature(String clazzName) throws ThrottlingConfigurationException {
//		synchronized (clazzStorage) {
//			Class clazz = null;
//			Object o = null;
//			
//			// loading the class
//			try {
//				clazz = getClass(clazzName);
//			} catch (ClassNotFoundException e) {
//				throw new ThrottlingConfigurationException(String.format("The class %s cannot be loaded!",clazzName), e);
//			}
//			if (clazz == null) {
//				throw new ThrottlingConfigurationException(String.format("The class %s cannot be loaded! (it was null after loading)",clazzName));
//			}
//			// trying to create an instance
//			try {
//				o = clazz.newInstance();
//			} catch (Exception e) {
//				throw new ThrottlingConfigurationException(String.format("The class %s cannot be instantiated!",clazzName), e);
//			}
//			// trying to cast the instance to IFeature
//			try {
//				IFeature<RQ, RS, EX> feature = (IFeature<RQ, RS, EX>)o;
//				// TODO possible memory leak...
//				//      the instances of the custom feature class cannot be destroyed because there will be references: instances -> custom feature class -> clazzStorage
//				//      probably I shouldn't allow to create custom features at the moment...
//				clazzStorage.put(clazzName, clazz);
//				return feature;
//			} catch(ClassCastException e) {
//				throw new ThrottlingConfigurationException(String.format("The class %s cannot be cast to IFeature<RQ, RS, EX>!", clazzName), e);
//			}
//		}
//	}
//	
//	
//	private Class getClass(String clazzName) throws ClassNotFoundException {
//		Class clazz = clazzStorage.get(clazzName);
//		if (clazz == null) {
//			ClassLoader classLoader = ThrottlingProcessController.class.getClassLoader();
//			clazz = classLoader.loadClass(clazzName);
//		}
//		return clazz;
//	}

	
}
