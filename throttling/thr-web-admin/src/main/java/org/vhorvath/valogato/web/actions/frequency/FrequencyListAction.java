package org.vhorvath.valogato.web.actions.frequency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;
import org.vhorvath.valogato.web.beans.frequency.FrequencyAndSleepingReqWebBean;

public class FrequencyListAction extends ThrottlingActionSupport {
	
	private static final long serialVersionUID = 3033082084908831029L;
	
	private List<FrequencyAndSleepingReqWebBean> usageList;
	
	public String execute() throws ThrottlingConfigurationException {
		try {
			setUsageList(createUsageList(UsageDAOFactory.getDAO().getFreqOfBackendServices(), 
					                     UsageDAOFactory.getDAO().getSleepingReqOfBackendServices()));
		} finally {
			ThrottlingStorage.removeCache();
		}
		return "SUCCESS";
	}

	public List<FrequencyAndSleepingReqWebBean> getUsageList() {
		return usageList;
	}

	public void setUsageList(List<FrequencyAndSleepingReqWebBean> usageList) {
		this.usageList = usageList;
	}

	private List<FrequencyAndSleepingReqWebBean> createUsageList(List<BackendServiceFreqBean> freqOfBackendServices,
                                                                 List<BackendServiceSleepingReqBean> sleepingReqOfBackendServices) {
		Map<String, FrequencyAndSleepingReqWebBean> beanMap = new HashMap<String, FrequencyAndSleepingReqWebBean>();
		// add the sleeping req
		for (BackendServiceSleepingReqBean beanSleepingReq : sleepingReqOfBackendServices) {
			beanMap.put(beanSleepingReq.getNameBackendService(), new FrequencyAndSleepingReqWebBean(beanSleepingReq.getNameBackendService(), 0, 
					beanSleepingReq.getNumberOfSleepingRequests()));
		}
		// add the frequencies
		for (BackendServiceFreqBean beanFreq : freqOfBackendServices) {
			if (beanMap.containsKey(beanFreq.getNameBackendService())) {
				FrequencyAndSleepingReqWebBean bean = beanMap.get(beanFreq.getNameBackendService());
				bean.setFrequency(beanFreq.getFrequency());
				beanMap.put(beanFreq.getNameBackendService(), bean);
			} else {
				beanMap.put(beanFreq.getNameBackendService(), new FrequencyAndSleepingReqWebBean(beanFreq.getNameBackendService(), 
						beanFreq.getFrequency(), 0));
			}
		}
		
		return new ArrayList<FrequencyAndSleepingReqWebBean>(beanMap.values());
	}

}
