package org.vhorvath.valogato.core.controller;


import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.WaitingReqDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.feature.FeatureParamGetter;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.core.statistics.StatisticsStoreManager;
import org.vhorvath.valogato.core.transaction.ResourceManager;


// TODO using the fastest XML parser
// http://stackoverflow.com/questions/960357/are-there-faster-xml-parsers-in-java-than-xalan-xerces
// http://stackoverflow.com/questions/6709205/metro-stax-implementation-how-to-configure
// http://stackoverflow.com/questions/5915091/how-do-i-configure-jaxb-to-use-woodstox-on-jboss-6


/*
 *       **************     +------------------+     **************     +------------------+
 *   --> * throttling * --> |   real service   | --> * throttling * --> |  backend service |
 *       **************     +------------------+     **************     +------------------+
 *            HERE              *throttling*          OR HERE
 *    if the real service       ************        if the backend service
 *    should be protected                           should be protected
 *
 * if we want to protect the 'backend service' then we HAVE TO put the throttling module (simulated service) directly in front of  
 *   the backend service!! (i.e. in this case the first throttling is not in the right place on the diagram above)
 * if we want to defend the 'real service' then the first 
 *                             
 * The throttling module can be 
 *    - in front of the the service which must be protected: simulatedServiceName is needed, multiple backend service name can be defined
 *    - in the real service: TODO I haven't implemented yet...
 */


/**
 * @author Viktor Horvath
 */
public class ThrottlingProcessController<RQ, RS, EX extends Exception> implements IThrottlingController<RQ, RS, EX> {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
			
	private Calendar startOfProcessing = null;
	private String requestId = null;
	private boolean afterSleeping = false;
	private StatisticsStoreManager statisticsStoreManager = null;
	private ResourceManager resourceManager = null;

	
	public ThrottlingProcessController() {
		// setting the start time of the request processing
		startOfProcessing = Calendar.getInstance();
		// creating the unique id of the request 
		requestId = createUniqueReqId();
		// putting the request id into a SLF4J store in order to logging it
		MDC.put("reqId", requestId);
		// setting attributes to ThreadLocal -> they are needed for the roll back operation
		ThrottlingStorage.init();
		// initializing instances
		statisticsStoreManager = new StatisticsStoreManager();
		resourceManager = new ResourceManager();
	}
	
	
	/*
	 * The method is called from processRequest or processRequestAfterSleeping
	 */
	private RS innerProcessRequest(RQ req,
                                   ISimulatedService<RQ, RS, EX> simulatedInterface,
                                   String backendServiceName,
                                   String simulatedServiceName) throws EX {
		try {
			checkArgument(req != null);
			checkArgument(simulatedInterface != null);
			checkArgument(backendServiceName != null);
		} catch(Exception e) {
			return handleException("# Problems with the arguments!", e, simulatedInterface, req);
		}
		
		boolean can = false;

		// the backend service config will be loaded here because we can avoid the request being stuck (e.g. JVM crashes on one machine and
		//    because there is request in the waiting req list which will be never processed then this req will get stuck behind the inactive
		//    req until it will be removed from the list somehow -> this way it is possible to change the strategy and this req will be processed)
		BackendServiceBean backendServiceBean = null;
		try {
			backendServiceBean = BackendServiceConfigDAOFactory.getDAO().getBackendService(backendServiceName);
		} catch (Exception e) {
			return handleException("# ERROR: the config of the backend service "+backendServiceName+" couldn't be loaded!", e, 
					simulatedInterface, req);
		}

		// check the availability of the backend system in store -> canBackendBeCalled
		try {
			can = statisticsStoreManager.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
			LOGGER.debug(String.format("# Can the backend %s be called? = %s", backendServiceName, Boolean.toString(can)));
		} catch (Exception e) {
			return handleException("# ERROR: the DiagnosticStore was not able to be called!", e, simulatedInterface, req);
		}
		
		// if the backend cannot be called then apply a feature
		if (!can) {
			// add the request id to the waiting request list if necessary
			try {
				registerRequestIdInWaitingReqList(backendServiceBean, simulatedServiceName, can, simulatedInterface);
			} catch (ThrottlingConfigurationException e) {
				return handleException("# ERROR: unable to register the request into the waiting request list!", e, simulatedInterface, req);
			}
			try {
				LOGGER.debug("# The backend service cannot be called and a feature will be used ...");
				// apply the feature
				return new FeatureExecutor<RQ, RS, EX>().applyFeature(req, backendServiceName, simulatedInterface, simulatedServiceName, 
						backendServiceBean, this);
			} catch (ThrottlingConfigurationException tce) {
				return handleException("# ERROR: the feature couldn't be appplied!", tce, simulatedInterface, req);
			} catch (ThrottlingRuntimeException tre) {
				return handleException("# ERROR: runtime error occurred when tried to apply the feature!", tre, simulatedInterface, req);
			// all the exceptions mustn't be caught because SOAP fault cannot be thrown in that case => all the exception being able to occur must 
			//		be converted to ThrottlingConfigurationException or ThrottlingRuntimeException
			}
		}
		
		// if the backend can be called then call it ...
		else {
			LOGGER.debug("# The backend service may be called and it is going to be called ...");
			return simulatedInterface.forwardRequest(req);
		}
	}


	// when the request first arrives to throttling then this method is used
	public RS processRequest(RQ req,
                             ISimulatedService<RQ, RS, EX> simulatedInterface,
                             String backendServiceName,
                             String simulatedServiceName) throws EX {
		try {
			LOGGER.info(String.format("# The method ThrController.processRequest has been called! backendServiceNames = %s, " +
					"requestId = %s, simulatedServiceName = %s", backendServiceName, requestId, simulatedServiceName));

			try {
				afterSleeping = false;
				return innerProcessRequest(req, simulatedInterface, backendServiceName, simulatedServiceName);
			} finally {
				try {
					resourceManager.releaseResources(backendServiceName, requestId, statisticsStoreManager, simulatedServiceName);
				} catch (Exception e) {
					LOGGER.error("# ERROR: Error when trying to release the resources! ", e);
				}
			}
		} finally {
			LOGGER.info(String.format("# The method ThrController.processRequest has finished. backendServiceNames = %s, " +
					"requestId = %s, simulatedServiceName = %s", backendServiceName, requestId, simulatedServiceName));
			try {
				ThrottlingStorage.removeAll();
			} finally {
				MDC.remove("reqId");
			}
		}
	}
	
	
	// this method is used if the request wakes up from sleeping and processing again
	public RS processRequestAfterSleeping(RQ req,
                                          ISimulatedService<RQ, RS, EX> simulatedInterface,
                                          String backendServiceName,
                                          String simulatedServiceName,
                                          BackendServiceBean backendServiceBean) throws EX {
		// TODO using {} instead of String.format(...)
//		LOGGER.info("# The method ThrController.processRequestAfterSleeping has been called! backendServiceNames = {}, " +
//				"requestId = {}, simulatedServiceName = {}", backendServiceName, requestId, simulatedServiceName);
		LOGGER.info(String.format("# The method ThrController.processRequestAfterSleeping has been called! backendServiceNames = %s, " +
				"requestId = %s, simulatedServiceName = %s", backendServiceName, requestId, simulatedServiceName));
		
		afterSleeping = true;
		return innerProcessRequest(req, simulatedInterface, backendServiceName, simulatedServiceName);
	}
	
	
	public Calendar getStartOfProcessing() {
		return startOfProcessing;
	}

	
	public String getRequestId() {
		return requestId;
	}
	

	private void registerRequestIdInWaitingReqList(BackendServiceBean backendServiceBean, String simulatedServiceName, boolean can, 
			ISimulatedService<RQ, RS, EX> simulatedInterface) throws ThrottlingConfigurationException, EX {
		if (!can && !afterSleeping) {
			String strategy = FeatureParamGetter.getStrategy(backendServiceBean, simulatedServiceName);
			// if the feature has got the REGISTERING_REQUESTS_INDIVIDUALLY strategy then the request must be added to the waiting req list.
			if (strategy != null && strategy.equals(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString())) {
				Integer waitingReqListMaxSize =  FeatureParamGetter.getWaitingReqListMaxSize(backendServiceBean, simulatedServiceName);
				Integer maxNumberOfWaitingReqs = FeatureParamGetter.getMaxNumberOfWaitingReqs(backendServiceBean, simulatedServiceName);
				// register in the waiting request list
				boolean wasAbleToAdd = WaitingReqDAOFactory.getDAO().registerRequest(requestId, backendServiceBean, simulatedServiceName, waitingReqListMaxSize, 
						maxNumberOfWaitingReqs);
				if (!wasAbleToAdd) {
					throw simulatedInterface.buildFault(String.format("WaitingFeature: The waiting requests have exceeded the limit (%s)!", 
							maxNumberOfWaitingReqs));
				} else {
					ThrottlingStorage.setAddedToWaitingReqList(true);
				}
			}
		}
	}


	private RS handleException(String errorMessage, Exception e, ISimulatedService<RQ, RS, EX> simulatedInterface, RQ req) throws EX {
		LOGGER.error(errorMessage, e);
		// in case of error we mustn't let the call be lost so we call the backend system directly
		LOGGER.info("# The backend service is going to be called directly.");
		return simulatedInterface.forwardRequest(req);
	}


	private String createUniqueReqId() {
		String machineName = "";
		try {
			machineName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			LOGGER.warn("# The name of the machine cannot be determined! " + e.getMessage());
		}
		
		// we don't need the complete nanotime but its end
		String nanoTime = Long.toString(System.nanoTime());
		
		return machineName + "<" + UUID.randomUUID().toString().replace("-", "") + ">" + nanoTime;
	}

}