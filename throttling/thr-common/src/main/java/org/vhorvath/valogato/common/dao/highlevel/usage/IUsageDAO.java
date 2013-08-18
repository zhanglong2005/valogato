package org.vhorvath.valogato.common.dao.highlevel.usage;

import java.util.List;

import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;

/**
 * @author Viktor Horvath
 */
public interface IUsageDAO {

	List<BackendServiceFreqBean> getFreqOfBackendServices() throws ThrottlingConfigurationException;
	
	List<BackendServiceSleepingReqBean> getSleepingReqOfBackendServices() throws ThrottlingConfigurationException;

	BackendServiceFreqBean getFreqOfBackendService(String nameBackendService) throws ThrottlingConfigurationException;

	BackendServiceSleepingReqBean getSleepingReqOfBackendService(String nameBackendService) throws ThrottlingConfigurationException;

	// if there is no value with the specific key in the cache then the value must be put into the cache
	void setFrequency(String nameBackendService, Integer frequency) throws ThrottlingConfigurationException;

	void setNumberOfSleepingRequests(String nameBackendService, Integer numberOfSleepingRequests) throws ThrottlingConfigurationException;

	List<String> getUsageOfBackendServiceNames() throws ThrottlingConfigurationException;
	
	void increaseNumberOfSleepingRequests(String nameBackendService) throws ThrottlingConfigurationException, ThrottlingRuntimeException;

	void decreaseNumberOfSleepingRequests(String nameBackendService) throws ThrottlingConfigurationException, ThrottlingRuntimeException;

	void initBackendServiceUsage(String nameBackendService) throws ThrottlingConfigurationException;
	
}
