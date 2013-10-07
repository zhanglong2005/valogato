package org.vhorvath.valogato.web.actions.administration;

import java.util.ArrayList;
import java.util.List;

import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;

public class LoadBackendServiceConfigAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = 8161254786032418530L;
	private Boolean successful = null;
	private List<String> configurationErrorMessage = null;
	
	
	public String execute() throws ThrottlingConfigurationException {
		setSuccessful(true);
		try {
			BackendServiceConfigDAOFactory.getDAO().loadConfig();
		} catch(ThrottlingConfigurationException e) {
			if (e.getCause() instanceof org.simpleframework.xml.core.ElementException) {
				setConfigurationErrorMessage(e.getCause().getMessage());
			} else if (e.getErrors() != null) {
				setConfigurationErrorMessage(e.getErrors());
			} else {
				LOGGER.error("Unexpected exception occurred!", e);
				setConfigurationErrorMessage(e.getMessage());
			}
		} finally {
			ThrottlingStorage.removeCache();
		}
		
		return "SUCCESS";
	}

	public Boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(Boolean successful) {
		this.successful = successful;
	}	
	
	public List<String> getConfigurationErrorMessage() {
		return configurationErrorMessage;
	}
	
	protected void setConfigurationErrorMessage(String configurationErrorMessage) {
		List<String> list = new ArrayList<String>();
		list.add(configurationErrorMessage);
		setConfigurationErrorMessage(list);
	}
	public void setConfigurationErrorMessage(List<String> configurationErrorMessage) {
		this.configurationErrorMessage = configurationErrorMessage;
		setSuccessful(false);
	}
}
