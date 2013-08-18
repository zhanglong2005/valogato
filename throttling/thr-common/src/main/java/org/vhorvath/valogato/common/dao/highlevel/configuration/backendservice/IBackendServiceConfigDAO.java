package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice;

import java.io.File;
import java.util.List;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

/**
 * @author Viktor Horvath
 */
public interface IBackendServiceConfigDAO {

	void put(String backendServiceName, BackendServiceBean backendServiceBean) throws ThrottlingConfigurationException;

	BackendServiceBean getBackendService(String backendServiceName) throws ThrottlingConfigurationException;

	List<BackendServiceBean> getBackendServices() throws ThrottlingConfigurationException;

	Integer getMaxLoading(String backendServiceName) throws ThrottlingConfigurationException;

	FeatureBean getFeature(String backendServiceName, String simulatedServiceName) throws ThrottlingConfigurationException;
	
	FeatureBean getFeature(BackendServiceBean backendServiceBean, String simulatedServiceName) throws ThrottlingConfigurationException;

	Integer getAverageResponseTime(String backendServiceName) throws ThrottlingConfigurationException;
	
	String getBackendServicesAsXMLString() throws ThrottlingConfigurationException;
	
	void loadConfig() throws ThrottlingConfigurationException;
	
	void loadConfig(File file) throws ThrottlingConfigurationException;

}