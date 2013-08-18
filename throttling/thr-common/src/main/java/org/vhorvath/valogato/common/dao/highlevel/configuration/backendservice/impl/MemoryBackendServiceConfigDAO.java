package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl;


import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.ParentBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;


/**
 * @author Viktor Horvath
 */
public class MemoryBackendServiceConfigDAO extends ParentBackendServiceConfigDAO {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	
	public void put(String backendServiceName, BackendServiceBean backendServiceBean) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);
		checkArgument(backendServiceBean != null);
		
		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.put(%s, %s)", backendServiceName, backendServiceBean));
		
		String cacheKey = ThrottlingUtils.getConfKey(backendServiceName);
		
		// add the backend service config to the cache
		CacheDAOFactory.getCache().lock(cacheKey);
		try {
			CacheDAOFactory.getCache().put(cacheKey, backendServiceBean);
		} finally {
			CacheDAOFactory.getCache().unlock(cacheKey);
		}
	}
	

	public BackendServiceBean getBackendService(String backendServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);
		
		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.getBackendService(%s)", backendServiceName));

		BackendServiceBean backendServiceBean = CacheDAOFactory.getCache().get(ThrottlingUtils.getConfKey(backendServiceName), 
				BackendServiceBean.class);
		if (backendServiceBean == null) {
			LOGGER.error(String.format("##### ERROR! The BackendServiceBean is null, probably the configuration is not loaded yet! backend service name = %s", 
					backendServiceName));
			throw new ThrottlingConfigurationException(false);
		}
		
		return backendServiceBean;
	}

	
	public List<BackendServiceBean> getBackendServices() throws ThrottlingConfigurationException {
		LOGGER.debug("##### MemoryBackendServiceConfigDAO.getBackendServices()");
		
		List<BackendServiceBean> backendServiceList = Collections.synchronizedList(new ArrayList<BackendServiceBean>());
		for(Object key : CacheDAOFactory.getCache().getKeys()) {
			if (key != null && key.toString().startsWith(ThrConstants.PREFIX_CACHE_CONFIGURATION)) {
				backendServiceList.add(CacheDAOFactory.getCache().get(key.toString(), BackendServiceBean.class));					
			}
		}
		if (backendServiceList.size() == 0) {
			throw new ThrottlingConfigurationException(false);
		}
		return backendServiceList;
	}

	
	public Integer getMaxLoading(String backendServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);

		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.getMaxLoading(%s)", backendServiceName));
		
		BackendServiceBean backendService = getBackendService(backendServiceName);
		return backendService.getMaxLoading();
	}
	

	// get the feature should be applied -> first search in the sim. serv. features and if no feature has been found then return 
	//   the feature of backendservice
	public FeatureBean getFeature(String backendServiceName, String simulatedServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);
		checkArgument(simulatedServiceName != null);
		
		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.getFeature(%s, %s)", backendServiceName, simulatedServiceName));

		BackendServiceBean backendServiceBean = getBackendService(backendServiceName);
		if (backendServiceBean == null) {
			return null;
		} else {
			return getFeature(backendServiceBean, simulatedServiceName);
		}
	}

	
	public Integer getAverageResponseTime(String backendServiceName) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.getAverageResponseTime(%s)", backendServiceName));
		
		BackendServiceBean backendService = getBackendService(backendServiceName);
		if (backendService == null) {
			return null;
		} else {
			return backendService.getAverageResponseTime();
		}
	}

	
	protected void initWaitingReqFirstLastList() throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.initWaitingReqFirstLastList()"));
		
		WaitingReqFirstLastListBean bean = CacheDAOFactory.getCache().get(ThrottlingUtils.getWaitingReqFirstLastKey(), 
				WaitingReqFirstLastListBean.class);
		if (bean == null) {
			bean = new WaitingReqFirstLastListBean(0, 0);
			CacheDAOFactory.getCache().put(ThrottlingUtils.getWaitingReqFirstLastKey(), bean);
		}
	}


	@Override
	protected void initFirstWaitingReqList() throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryBackendServiceConfigDAO.initFirstWaitingReqList()"));
		
		String key = ThrottlingUtils.getWaitingReqListKey(0);
		Set<String> list = CacheDAOFactory.getCache().get(key, Set.class);
		
		if (list == null) {
			list = Collections.synchronizedSet(new LinkedHashSet<String>());
			CacheDAOFactory.getCache().put(key, list);
		}
	}

}