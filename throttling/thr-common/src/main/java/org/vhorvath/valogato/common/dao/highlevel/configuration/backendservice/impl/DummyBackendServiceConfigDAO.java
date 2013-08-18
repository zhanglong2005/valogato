package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl;


import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.ParentBackendServiceConfigDAO;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;


/**
 * @author Viktor Horvath
 */
public class DummyBackendServiceConfigDAO extends ParentBackendServiceConfigDAO {

	
	private static Map<String,BackendServiceBean> backendServices = null;
	
	
	public void put(String backendServiceName, BackendServiceBean backendServiceBean) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);
		checkArgument(backendServiceBean != null);

		if (backendServices == null) {
			backendServices = new HashMap<String,BackendServiceBean>();
		}
		
		backendServices.put(backendServiceName, backendServiceBean);
	}

	
	public BackendServiceBean getBackendService(String backendServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);
		
		if (backendServices == null) {
			throw new ThrottlingConfigurationException(false);
		}

		return backendServices.get(backendServiceName);
	}

	
	public List<BackendServiceBean> getBackendServices() throws ThrottlingConfigurationException {
		if (backendServices == null) {
			throw new ThrottlingConfigurationException(false);
		}
		
		return new ArrayList<BackendServiceBean>(backendServices.values());
	}

	
	public Integer getMaxLoading(String backendServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);

		return getBackendService(backendServiceName).getMaxLoading();
	}

	
	public FeatureBean getFeature(String backendServiceName, String simulatedServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);
		checkArgument(simulatedServiceName != null);

		BackendServiceBean backendServiceBean = getBackendService(backendServiceName);
		return getFeature(backendServiceBean, simulatedServiceName);
	}

	
	public Integer getAverageResponseTime(String backendServiceName) throws ThrottlingConfigurationException {
		checkArgument(backendServiceName != null);

		return getBackendService(backendServiceName).getAverageResponseTime();
	}


	protected void initWaitingReqFirstLastList() throws ThrottlingConfigurationException {
		// TODO ???
	}


	@Override
	protected void initFirstWaitingReqList() throws ThrottlingConfigurationException {
		// TODO ???
	}

	
}
