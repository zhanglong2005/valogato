package org.vhorvath.valogato.web.actions.frequency;

import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;

public class FrequencyResetAllAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = -7994629243138581240L;

	public String execute() throws ThrottlingConfigurationException {
		try {
			for (String nameBackendService : UsageDAOFactory.getDAO().getUsageOfBackendServiceNames()) {
				UsageDAOFactory.getDAO().setFrequency(nameBackendService, 0);
			}
		} finally {
			ThrottlingStorage.removeCache();
		}
		return "SUCCESS";
	}

}
