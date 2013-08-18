package org.vhorvath.valogato.web.actions.frequency;

import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;

public class FrequencySaveAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = 4862898009955635409L;

	private String nameBackendService;
	private String frequency;
	private String sleepingRequests;
	private String buttonName;
	
	public String execute() throws ThrottlingConfigurationException {
		try {
			if (buttonName.equals("Save")) {
				UsageDAOFactory.getDAO().setFrequency(nameBackendService, Integer.parseInt(frequency));
				UsageDAOFactory.getDAO().setNumberOfSleepingRequests(nameBackendService, Integer.parseInt(sleepingRequests));
			}
		} finally {
			ThrottlingStorage.removeCache();
		}
		return "SUCCESS";
	}

	public void validate() {
		if (buttonName.equals("Save")) {
			checkInteger(this, frequency, "frequency", "Frequency", 0, 99999);
			checkInteger(this, sleepingRequests, "sleepingRequests", "the number of sleeping requests", 0, 99999);
		}
	}
	
	public String getNameBackendService() {
		return nameBackendService;
	}

	public void setNameBackendService(String nameBackendService) {
		this.nameBackendService = nameBackendService;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getButtonName() {
		return buttonName;
	}

	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}

	public String getSleepingRequests() {
		return sleepingRequests;
	}

	public void setSleepingRequests(String sleepingRequests) {
		this.sleepingRequests = sleepingRequests;
	}

}
