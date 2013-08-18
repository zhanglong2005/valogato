package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServicesBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.SimulatedServiceBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;


/**
 * @author Viktor Horvath
 */
public abstract class ParentBackendServiceConfigDAO implements IBackendServiceConfigDAO {
	
	
	public String getBackendServicesAsXMLString() throws ThrottlingConfigurationException {
		List<BackendServiceBean> backendServiceBeans = getBackendServices();
		
		BackendServicesBean backendServicesBean = new BackendServicesBean();
		backendServicesBean.setBackendService(backendServiceBeans);

		try {
			Writer writer = new StringWriter();
			Serializer serializer = new Persister();
			serializer.write(backendServicesBean, writer);
			
			return writer.toString();
		} catch (Exception e) {
			throw new ThrottlingConfigurationException(e);
		}
	}
	
	
	public FeatureBean getFeature(BackendServiceBean backendServiceBean, String simulatedServiceName) throws ThrottlingConfigurationException {
		if (simulatedServiceName == null) {
			return backendServiceBean.getFeature();
		}
		for(SimulatedServiceBean simulatedService : backendServiceBean.getSimulatedService()) {
			if (simulatedService.getName().equals(simulatedServiceName)) {
				return simulatedService.getFeature();
			}
		}
		return backendServiceBean.getFeature();
	}
	
	
	public void loadConfig() throws ThrottlingConfigurationException {
		Serializer serializer = new Persister();
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(ThrConstants.PATH_CONFIG_XML);
			BackendServicesBean backendServices = serializer.read(BackendServicesBean.class, in);
			processConfiguration(backendServices);
		} catch (ThrottlingConfigurationException tce) {
			throw tce;
		} catch (Exception e) {
			throw new ThrottlingConfigurationException(e);
		}
	}
	
	
	public void loadConfig(File file) throws ThrottlingConfigurationException {
		Serializer serializer = new Persister();
		try {
			BackendServicesBean backendServices = serializer.read(BackendServicesBean.class, file);
			processConfiguration(backendServices);
		} catch (ThrottlingConfigurationException tce) {
			throw tce;
		} catch (Exception e) {
			throw new ThrottlingConfigurationException(e);
		}
	}


	void processConfiguration(BackendServicesBean backendServices) throws ThrottlingConfigurationException {
		List<String> errors = validateBackendServices(backendServices);
		if (errors != null) {
			throw new ThrottlingConfigurationException(errors);
		}		
		
		initWaitingReqFirstLastList();
		initFirstWaitingReqList();

		for(BackendServiceBean backendService : backendServices.getBackendService()) {
			// add the backend service config to the cache
			put(backendService.getName(), backendService);
			// if there is not a BackendServiceUsageBean (frequency) in the cache with this name then add one! -> it is needed 
			//    for the initialization of usages in cache
			UsageDAOFactory.getDAO().initBackendServiceUsage(backendService.getName());
		}
		
		//put the first (zero) waiting requests list into the cache
		String key = ThrottlingUtils.getWaitingReqListKey(0);
		CacheDAOFactory.getCache().put(key, Collections.synchronizedSet(new LinkedHashSet<String>()));
	}
	

	private List<String> validateBackendServices(BackendServicesBean backendServices) {
		List<String> errors = new ArrayList<String>();
		
		for (BackendServiceBean backendService : backendServices.getBackendService()) {
			// check maxLoading
			String error = checkInteger(backendService.getMaxLoading(), "The max loading attribute of the backend service " + backendService.getName(), 0, 99999);
			if (error != null) {
				errors.add(error);
			}
			// check averageResponseTime
			error = checkInteger(backendService.getAverageResponseTime(), "The average response time attribute of the backend service " + backendService.getName(), 
					0, 20*60*1000);
			if (error != null) {
				errors.add(error);
			}
			// check the parameters of the backend service
			if (backendService.getFeature().getParams() != null) {
				Iterator<String> keyIterator = backendService.getFeature().getParams().keySet().iterator();
				while(keyIterator.hasNext()) {
					String key = keyIterator.next();
					String value = backendService.getFeature().getParams().get(key);
					error = checkParam(key, value, String.format("The backendService(%s) / feature param(%s)", backendService.getName(), key));
					if (error != null) {
						errors.add(error);
					}
				}
			}
			// check the parameters of the simulated services
			if (backendService.getSimulatedService() != null) {
				for (SimulatedServiceBean simulatedService : backendService.getSimulatedService()) {
					if (simulatedService.getFeature().getParams() != null) {
						Iterator<String> keyIterator = simulatedService.getFeature().getParams().keySet().iterator();
						while(keyIterator.hasNext()) {
							String key = keyIterator.next();
							String value = simulatedService.getFeature().getParams().get(key);
							error = checkParam(key, value, String.format("The backendService(%s) / simulatedService(%s) / param(%s)", backendService.getName(), 
									simulatedService.getName(), key));
							if (error != null) {
								errors.add(error);
							}
						}						
					}
				}
			}
		}
		return errors.size() > 0 ? errors : null;
	}

	
	private String checkParam(String key, String value, String path) {
		StringBuffer sb = new StringBuffer();
		// the sleeping period must be bigger than 1000 millisec and less than 30 sec
		if (key.equals(ThrConstants.FeatureParam.period.toString())) {
			String error = checkInteger(value, path, 1000, 30000);
			sb.append(error == null ? "" : error);
		}
		// the max number of waiting reqs must be bigger than 50 and less than 100000
		else if (key.equals(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString())) {
			String error = checkInteger(value, path, 50, 100000);
			sb.append(error == null ? "" : error);
		}
		// the waiting Req List Max Size must be bigger than 10 and less than 1000
		else if (key.equals(ThrConstants.FeatureParam.waitingReqListMaxSize.toString())) {
			String error = checkInteger(value, path, 10, 1000);
			sb.append(error == null ? "" : error);
		}
		// the strategy must be in the set (fast,maintiningFreeSlots,registeringRequestsIndividually)
		else if (key.equals(ThrConstants.FeatureParam.strategy.toString())) {
			String error = checkPossibleValues(value, path, ThrConstants.FeatureParamValue.fast.toString(),
					ThrConstants.FeatureParamValue.maintiningFreeSlots.toString(), 
					ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
			sb.append(error == null ? "" : error);
		}
		// the endpoints must have value
		else if (key.equals(ThrConstants.FeatureParam.endpoints.toString())) {
			String error = checkRequired(value, path);
			sb.append(error == null ? "" : error);
		}
		return sb.length() > 0 ? sb.toString() : null;
	}
	

	private String checkInteger(Object value, String field, int... ranges) {
		String required = checkRequired(value, field);
		if (required != null) {
			return required;
		}
		try {
			Integer i = Integer.parseInt(value.toString());
			if (ranges.length == 2) {
				if (i < ranges[0] || i > ranges[1]) {
					return String.format("%s must be in the range %s and %s.", field, Integer.toString(ranges[0]), Integer.toString(ranges[1]));
				}
			}
		} catch(NumberFormatException nfe) {
			return String.format("%s must be an integer.", field);
		}
		return null;
	}
	
	protected String checkRequired(Object value, String field) {
		if (value == null || value.toString().length() == 0) {
			return String.format("%s is required.", field);
		} else {
			return null;
		}
	}

	protected String checkPossibleValues(String value, String field, String... values) {
		String required = checkRequired(value, field);
		if (required != null) {
			return required;
		}
		if (Arrays.binarySearch(values, value) < 0) {
			return String.format("The value (%s) of %s must be in the set (%s).", value, field, ThrottlingUtils.commaSeparated(values));
		} else {
			return null;
		}
	}
			

	
	
	
	protected abstract void initWaitingReqFirstLastList() throws ThrottlingConfigurationException;
	protected abstract void initFirstWaitingReqList() throws ThrottlingConfigurationException;
	
	
}
