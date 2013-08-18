package org.vhorvath.valogato.common.dao.highlevel.usage.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

/**
 * @author Viktor Horvath
 */
public class DummyUsageDAO implements IUsageDAO {

	private static Map<String, BackendServiceFreqBean> defaultUsageFreq = new Hashtable<String, BackendServiceFreqBean>();
	private static Map<String, BackendServiceSleepingReqBean> defaultUsageWaitingReq = new Hashtable<String, BackendServiceSleepingReqBean>();
	
	static {
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"ICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMS", 
				new BackendServiceFreqBean("ICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMS", 1));
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"WIFI", new BackendServiceFreqBean("WIFI", 5));
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"SPOTIFY", new BackendServiceFreqBean("SPOTIFY", 3));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"Alma", new BackendServiceFreqBean("Alma", 1354));
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"ICOMS", new BackendServiceFreqBean("ICOMS", 535));
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"SAP", new BackendServiceFreqBean("SAP", 3345));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"AUTO", new BackendServiceFreqBean("AUTO", 3));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"KOCSI", new BackendServiceFreqBean("KOCSI", 242543364));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"NEXTENT", new BackendServiceFreqBean("NEXTENT", 2342));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"VM", new BackendServiceFreqBean("VM", 243));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"IQS", new BackendServiceFreqBean("IQS", 23213));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"KOZSO", new BackendServiceFreqBean("KOZSO", 5675));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"FORMA", new BackendServiceFreqBean("FORMA", 2));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"KOMOD", new BackendServiceFreqBean("KOMOD", 0));	
		defaultUsageFreq.put(ThrConstants.PREFIX_CACHE_FREQUENCY+"AGY", new BackendServiceFreqBean("AGY", 23));

		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"ICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMS", 
				new BackendServiceSleepingReqBean("ICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMSICOMS", 2));
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"WIFI", new BackendServiceSleepingReqBean("WIFI", 0));
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"SPOTIFY", new BackendServiceSleepingReqBean("SPOTIFY", 1));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"Alma", new BackendServiceSleepingReqBean("Alma", 3));
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"ICOMS", new BackendServiceSleepingReqBean("ICOMS", 0));
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"SAP", new BackendServiceSleepingReqBean("SAP", 2));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"AUTO", new BackendServiceSleepingReqBean("AUTO", 1));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"KOCSI", new BackendServiceSleepingReqBean("KOCSI", 0));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"NEXTENT", new BackendServiceSleepingReqBean("NEXTENT", 1));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"VM", new BackendServiceSleepingReqBean("VM", 1));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"IQS", new BackendServiceSleepingReqBean("IQS", 0));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"KOZSO", new BackendServiceSleepingReqBean("KOZSO", 4));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"FORMA", new BackendServiceSleepingReqBean("FORMA", 0));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"KOMOD", new BackendServiceSleepingReqBean("KOMOD", 0));	
		defaultUsageWaitingReq.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"AGY", new BackendServiceSleepingReqBean("AGY", 5));
	}
	

	public void setFrequency(String nameBackendService, Integer frequency) {
		BackendServiceFreqBean bean = defaultUsageFreq.get(ThrottlingUtils.getFreqKey(nameBackendService));
		if (bean == null) {
			bean = new BackendServiceFreqBean(nameBackendService, frequency);
		} else {
			bean.setFrequency(frequency);
		}
		defaultUsageFreq.put(ThrottlingUtils.getFreqKey(nameBackendService), bean);
	}

	public void setNumberOfSleepingRequests(String nameBackendService, Integer sleepingRequests) throws ThrottlingConfigurationException {
		BackendServiceSleepingReqBean bean = defaultUsageWaitingReq.get(ThrottlingUtils.getSleepingReqKey(nameBackendService));
		if (bean == null) {
			bean = new BackendServiceSleepingReqBean(nameBackendService, sleepingRequests);
		} else {
			bean.setNumberOfSleepingRequests(sleepingRequests);
		}
		defaultUsageWaitingReq.put(ThrottlingUtils.getSleepingReqKey(nameBackendService), bean);
	}
	
	public List<String> getUsageOfBackendServiceNames() {
		List<String> names = new ArrayList<String>();
		for(BackendServiceFreqBean bean : defaultUsageFreq.values()) {
			names.add(bean.getNameBackendService());
		}
		return names;
	}

	public void increaseNumberOfSleepingRequests(String nameBackendService) throws ThrottlingRuntimeException {
		BackendServiceSleepingReqBean bean = defaultUsageWaitingReq.get(ThrottlingUtils.getSleepingReqKey(nameBackendService));
		if (bean == null) {
			throw new ThrottlingRuntimeException(String.format("The backend service '%s' is not in the cache!", nameBackendService));
		} else {
			bean.setNumberOfSleepingRequests(bean.getNumberOfSleepingRequests() + 1);
			defaultUsageWaitingReq.put(nameBackendService, bean);
		}
	}

	public void decreaseNumberOfSleepingRequests(String nameBackendService) throws ThrottlingRuntimeException {
		BackendServiceSleepingReqBean bean = defaultUsageWaitingReq.get(ThrottlingUtils.getSleepingReqKey(nameBackendService));
		if (bean == null) {
			throw new ThrottlingRuntimeException(String.format("The backend service '%s' is not in the cache!", nameBackendService));
		} else {
			if (bean.getNumberOfSleepingRequests() > 0) {
				bean.setNumberOfSleepingRequests(bean.getNumberOfSleepingRequests() - 1);
				defaultUsageWaitingReq.put(nameBackendService, bean);
			}
		}
	}

	public void initBackendServiceUsage(String nameBackendService) throws ThrottlingConfigurationException {
		// if there is not a BackendServiceUsageBean (frequency) in the cache with this name then add one! -> it is needed 
		//    for the initialization of usages in cache
		BackendServiceFreqBean freqBean = getFreqOfBackendService(nameBackendService);
		if (freqBean == null) {
			// add the usage bean to the cache
			defaultUsageFreq.put(ThrottlingUtils.getFreqKey(nameBackendService), new BackendServiceFreqBean(nameBackendService, 0));
		}
		BackendServiceSleepingReqBean waitingFreqBean = getSleepingReqOfBackendService(nameBackendService);
		if (waitingFreqBean == null) {
			// add the usage bean to the cache
			defaultUsageWaitingReq.put(ThrottlingUtils.getSleepingReqKey(nameBackendService), new BackendServiceSleepingReqBean(nameBackendService, 0));
		}
	}

	public List<BackendServiceFreqBean> getFreqOfBackendServices() throws ThrottlingConfigurationException {
		return new ArrayList(defaultUsageFreq.values());
	}

	public List<BackendServiceSleepingReqBean> getSleepingReqOfBackendServices() throws ThrottlingConfigurationException {
		return new ArrayList(defaultUsageWaitingReq.values());
	}

	public BackendServiceFreqBean getFreqOfBackendService(String nameBackendService) throws ThrottlingConfigurationException {
		return defaultUsageFreq.get(ThrottlingUtils.getFreqKey(nameBackendService));
	}

	public BackendServiceSleepingReqBean getSleepingReqOfBackendService(String nameBackendService) throws ThrottlingConfigurationException {
		return defaultUsageWaitingReq.get(ThrottlingUtils.getSleepingReqKey(nameBackendService));
	}

}
