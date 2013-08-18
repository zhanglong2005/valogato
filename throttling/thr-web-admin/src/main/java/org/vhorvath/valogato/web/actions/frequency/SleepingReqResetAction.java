package org.vhorvath.valogato.web.actions.frequency;

import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;

public class SleepingReqResetAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = -1586197112335837339L;

	private String nameBackendService;

	public String execute() throws ThrottlingConfigurationException {
		try {
			UsageDAOFactory.getDAO().setNumberOfSleepingRequests(nameBackendService, 0);
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

	
}
