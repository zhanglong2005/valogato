package org.vhorvath.valogato.common.dao.highlevel.usage.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;


/**
 * @author Viktor Horvath
 */
public class MemoryUsageDAO implements IUsageDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);	
	

	public void setFrequency(String nameBackendService, Integer frequency) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.setFrequency(%s, %s)", nameBackendService, Integer.toString(frequency)));
		
		BackendServiceFreqBean bean = CacheDAOFactory.getCache().get(ThrottlingUtils.getFreqKey(nameBackendService), BackendServiceFreqBean.class);
		
		if (bean == null) {
			throw new ThrottlingConfigurationException(String.format("The backend service %s has not been initailized when " +
					"loading the backend service configuration!", nameBackendService));
		} else {
			bean.setFrequency(frequency);
		}
		
		CacheDAOFactory.getCache().put(ThrottlingUtils.getFreqKey(nameBackendService), bean);
	}
	
	
	public void setNumberOfSleepingRequests(String nameBackendService, Integer numberOfSleepingRequests) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.setNumberOfSleepingRequests(%s, %s)", nameBackendService, Integer.toString(numberOfSleepingRequests)));
		
		BackendServiceSleepingReqBean bean = CacheDAOFactory.getCache().get(ThrottlingUtils.getSleepingReqKey(nameBackendService), 
				BackendServiceSleepingReqBean.class);
		
		if (bean == null) {
			throw new ThrottlingConfigurationException(String.format("The backend service %s has not been initailized when " +
					"loading the backend service configuration!", nameBackendService));
		} else {
			bean.setNumberOfSleepingRequests(numberOfSleepingRequests);
		}
		
		CacheDAOFactory.getCache().put(ThrottlingUtils.getSleepingReqKey(nameBackendService), bean);
	}
	

	public List<String> getUsageOfBackendServiceNames() throws ThrottlingConfigurationException {
		LOGGER.debug("##### MemoryUsageDAO.getUsageOfBackendServiceNames()");
		
		List<String> usageList = Collections.synchronizedList(new ArrayList<String>());
		for(Object key : CacheDAOFactory.getCache().getKeys()) {
			if (key != null && key.toString().startsWith(ThrConstants.PREFIX_CACHE_FREQUENCY)) {
				usageList.add(key.toString().substring(ThrConstants.PREFIX_CACHE_FREQUENCY.length()));					
			}
		}
		return usageList;
	}

	
	public void increaseNumberOfSleepingRequests(String nameBackendService) throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		BackendServiceSleepingReqBean bean = CacheDAOFactory.getCache().get(ThrottlingUtils.getSleepingReqKey(nameBackendService), 
				BackendServiceSleepingReqBean.class);
		LOGGER.debug(String.format("##### MemoryUsageDAO.increaseNumberOfSleepingRequests(%s), increased value: %s", nameBackendService, 
				Integer.toString(bean.getNumberOfSleepingRequests() + 1)));
		if (bean != null) {
			bean.setNumberOfSleepingRequests(bean.getNumberOfSleepingRequests() + 1);
			CacheDAOFactory.getCache().put(ThrottlingUtils.getSleepingReqKey(nameBackendService), bean);
			ThrottlingStorage.setChangedTheSleepingReqNumber(true);
		} else {
			throw new ThrottlingConfigurationException(String.format("The backend service %s has not been initailized when " +
					"loading the backend service configuration!", nameBackendService));
		}
	}

	
	public void decreaseNumberOfSleepingRequests(String nameBackendService) throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		BackendServiceSleepingReqBean bean = CacheDAOFactory.getCache().get(ThrottlingUtils.getSleepingReqKey(nameBackendService), 
				BackendServiceSleepingReqBean.class);
		LOGGER.debug(String.format("##### MemoryUsageDAO.decreaseNumberOfSleepingRequests(%s), decreased value: %s", nameBackendService, 
				Integer.toString(bean.getNumberOfSleepingRequests() - 1)));
		if (bean != null) {
			bean.setNumberOfSleepingRequests(bean.getNumberOfSleepingRequests() - 1);
			CacheDAOFactory.getCache().put(ThrottlingUtils.getSleepingReqKey(nameBackendService), bean);
			// setting to false because if it is being decreased now then in the release resource section would be decreased twice
			ThrottlingStorage.setChangedTheSleepingReqNumber(false);
		} else {
			throw new ThrottlingConfigurationException(String.format("The backend service %s has not been initailized when " +
					"loading the backend service configuration!", nameBackendService));
		}
	}


	public void initBackendServiceUsage(String nameBackendService) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.initBackendServiceUsage(%s)", nameBackendService));
		
		// if there is not a BackendServiceUsageBean (frequency) in the cache with this name then add one! -> it is needed 
		//    for the initialization of usages in cache
		BackendServiceFreqBean beanFreq = getFreqOfBackendService(nameBackendService);
		if (beanFreq == null) {
			// add the usage bean to the cache
			CacheDAOFactory.getCache().put(ThrottlingUtils.getFreqKey(nameBackendService), new BackendServiceFreqBean(nameBackendService, 0));
		}

		BackendServiceSleepingReqBean beanSleepingReq = getSleepingReqOfBackendService(nameBackendService);
		if (beanSleepingReq == null) {
			// add the usage bean to the cache
			CacheDAOFactory.getCache().put(ThrottlingUtils.getSleepingReqKey(nameBackendService), new BackendServiceSleepingReqBean(nameBackendService, 0));
		}
	}


	public List<BackendServiceFreqBean> getFreqOfBackendServices() throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.getFreqOfBackendServices()"));
		
		// improving it!! -> this method is slow in my opinion. First I get the keys and after the values one by one ->
		//      it generates too much network traffic; perhaps better to get all the values at once and examine them here
		// only the web application uses this method so probably it is not a catastrophe if it is not the fastest; if it is
		//      slow then it can be sorted it out by getting all the keys at once
		List<BackendServiceFreqBean> usageList = Collections.synchronizedList(new ArrayList<BackendServiceFreqBean>());
		for(Object key : CacheDAOFactory.getCache().getKeys()) {
			if (key != null && key.toString().startsWith(ThrConstants.PREFIX_CACHE_FREQUENCY)) {
				usageList.add(CacheDAOFactory.getCache().get(key.toString(), BackendServiceFreqBean.class));					
			}
		}
		return usageList;
	}


	public List<BackendServiceSleepingReqBean> getSleepingReqOfBackendServices() throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.getSleepingReqOfBackendServices()"));
		
		// improving it!! -> this method is slow in my opinion. First I get the keys and after the values one by one ->
		//      it generates too much network traffic; perhaps better to get all the values at once and examine them here
		// only the web application uses this method so probably it is not a catastrophe if it is not the fastest; if it is
		//      slow then it can be sorted it out by getting all the keys at once
		List<BackendServiceSleepingReqBean> usageList = Collections.synchronizedList(new ArrayList<BackendServiceSleepingReqBean>());
		List<String> keys = CacheDAOFactory.getCache().getKeys();
		for(Object key : keys) {
			if (key != null && key.toString().startsWith(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ)) {
				usageList.add(CacheDAOFactory.getCache().get(key.toString(), BackendServiceSleepingReqBean.class));					
			}
		}
		return usageList;
	}


	public BackendServiceFreqBean getFreqOfBackendService(String nameBackendService) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.getFreqOfBackendService(%s)", nameBackendService));
		
		return CacheDAOFactory.getCache().get(ThrottlingUtils.getFreqKey(nameBackendService), BackendServiceFreqBean.class);
	}


	public BackendServiceSleepingReqBean getSleepingReqOfBackendService(String nameBackendService) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryUsageDAO.getSleepingReqOfBackendService(%s)", nameBackendService));
		
		return CacheDAOFactory.getCache().get(ThrottlingUtils.getSleepingReqKey(nameBackendService), BackendServiceSleepingReqBean.class);
	}


}
