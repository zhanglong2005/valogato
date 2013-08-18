package org.vhorvath.valogato.web.actions.frequency;

import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;

public class SleepingReqResetAllAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = -2825386291719741720L;

	public String execute() throws ThrottlingConfigurationException {
		try {
			for (String nameBackendService : UsageDAOFactory.getDAO().getUsageOfBackendServiceNames()) {
				UsageDAOFactory.getDAO().setNumberOfSleepingRequests(nameBackendService, 0);
			}
		} finally {
			ThrottlingStorage.removeCache();
		}
		return "SUCCESS";
	}

}
