package org.vhorvath.valogato.core.controller.features;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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

/**
 * Forwarding the message to an other backend service (like a loadbalancer)
 * Using round-robin algorythm
 * 
 * @author Viktor Horvath
 */
public class ForwarderFeature<RQ, RS, EX extends Exception> implements IFeature<RQ, RS, EX> {

	
	// the endpoints of the specific backendservice+simulatedservice combinations are stored in this Map
	// e.g.    BigSystem                    =132.32.42.123/BigSystemPort/service, 192.168.123.124/BigSystemPort/service 
	//         FragileSystem-SimFragileSyste=132.32.42.123/SimBigSystemPort/service, 192.168.123.124/SimBigSystemPort/service
	// it is used to check if the endpoints were changed -> if they were then the queue (which endpoint has to be called next) must be rebuilt
	private static Map<String, String> endpointMapConstant = Collections.synchronizedMap(new HashMap<String, String>());

	// the endpoint queues (what is the next endpoint that must be used) of the specific backendservice+simulatedservice combinations are stored in this Map
	private static Map<String, Queue<String>> endpointMapQueue = Collections.synchronizedMap(new HashMap<String, Queue<String>>());
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
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
		
		// add the endpoint data to the static maps if it is not put there
		addToStatic(backendServiceName, simulatedServiceName, featureBean);
		
		// get the endpoint
		String endpoint = getEndpoint(backendServiceName, simulatedServiceName);
		LOGGER.debug(String.format("### The ForwarderFeature will use the endpoint '%s'", endpoint));
		
		// set the endpoint
		simulatedInterface.setEndpoint(endpoint);
		
		// call the simulated service
		return simulatedInterface.forwardRequest(req);
	}

	
	private void addToStatic(String backendServiceName, String simulatedServiceName, FeatureBean featureBean) throws ThrottlingConfigurationException {
		synchronized (endpointMapQueue) { 
				String key = backendServiceName + (simulatedServiceName != null ? "-"+simulatedServiceName : "");
				String endpoints = featureBean.getParams().get(ThrConstants.FeatureParam.endpoints.toString());
				String endpointsInStaticMaps = endpointMapConstant.get(key);
				// if the feature data is not in the static maps then it must be added
				if (endpointsInStaticMaps == null) {
					if (endpoints == null) {
						throw new ThrottlingConfigurationException(String.format("The feature of the backend service doesn't have %s parameter! backendService=%s, simulatedService=%s", 
								ThrConstants.FeatureParam.endpoints.toString(), backendServiceName, simulatedServiceName));
					}
					endpointMapConstant.remove(key);
					endpointMapConstant.put(key, endpoints);
					endpointMapQueue.remove(key);
					endpointMapQueue.put(key, getEndpointsAsQueue(featureBean));
				}
				// if the feature data is in the static maps then it has to be examined if the endpoints has been changed
				else {
					// if yes then the maps must be refreshed
					if (!endpoints.equals(endpointsInStaticMaps)) {
						endpointMapConstant.remove(key);
						endpointMapConstant.put(key, endpoints);
						endpointMapQueue.remove(key);
						endpointMapQueue.put(key, getEndpointsAsQueue(featureBean));
					}
				}
			}
		}


	private String getEndpoint(String backendServiceName, String simulatedServiceName) {
		synchronized (endpointMapQueue) {
			// get the endpoint
			String key = backendServiceName + (simulatedServiceName != null ? "-"+simulatedServiceName : "");
			// get the queue
			Queue<String> queue = endpointMapQueue.get(key);
			// retrieve and remove
			String endpoint = queue.poll();
			// add to the end of the queue
			queue.add(endpoint);
			return endpoint;
		}
	}


	private Queue<String> getEndpointsAsQueue(FeatureBean featureBean) {
		Queue<String> endpointQueue = new LinkedList<String>();
		String endpoints = featureBean.getParams().get(ThrConstants.FeatureParam.endpoints.toString());
		for (String endpoint : endpoints.trim().split(";")) {
			endpointQueue.add(endpoint.trim());
		}
		return endpointQueue;
	}
	
}