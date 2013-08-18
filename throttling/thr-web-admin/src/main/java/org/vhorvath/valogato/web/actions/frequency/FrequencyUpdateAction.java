package org.vhorvath.valogato.web.actions.frequency;

import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;
import org.vhorvath.valogato.web.utils.exception.ThrottlingWebException;

public class FrequencyUpdateAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = -5849882414990266812L;

	private String nameBackendService;
	private Integer frequency;
	private Integer sleepingRequests;
	
	public String execute() throws ThrottlingConfigurationException, ThrottlingWebException {
		try {
			// setting frequency
			BackendServiceFreqBean beanFreq = UsageDAOFactory.getDAO().getFreqOfBackendService(nameBackendService);
			if (beanFreq == null) {
				throw new ThrottlingWebException("The backend service '" + nameBackendService + "' cannot be found in the cache!");
			}
			setFrequency(beanFreq.getFrequency());
			// setting sleeping req
			BackendServiceSleepingReqBean beanSleepingReq = UsageDAOFactory.getDAO().getSleepingReqOfBackendService(nameBackendService);
			if (beanSleepingReq == null) {
				throw new ThrottlingWebException("The backend service '" + nameBackendService + "' cannot be found in the cache!");
			}
			setSleepingRequests(beanSleepingReq.getNumberOfSleepingRequests());
		} finally {
			ThrottlingStorage.removeCache();
		}
		return "SUCCESS";
	}

	public String getNameBackendService() {
		return nameBackendService;
	}

	public void setNameBackendService(String nameBackendService) {
		this.nameBackendService = nameBackendService;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public Integer getSleepingRequests() {
		return sleepingRequests;
	}

	public void setSleepingRequests(Integer sleepingRequests) {
		this.sleepingRequests = sleepingRequests;
	}

}
