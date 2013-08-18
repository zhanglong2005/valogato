package org.vhorvath.valogato.web.actions.administration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;

public class UploadNewBackendServiceConfigAction extends ThrottlingActionSupport {

	private static final long serialVersionUID = 9834576291664835L;
	
	private File file;
	private String contentType;
	private String filename;
	private boolean successfulUpload;
	private List<String> configurationErrorMessage = null;	

	public String execute() throws ThrottlingConfigurationException {
		setSuccessfulUpload(true);
		if (!contentType.equals("text/xml")) {
			setConfigurationErrorMessage("Only XML file can be loaded!");
		} else {
			try {
				BackendServiceConfigDAOFactory.getDAO().loadConfig(file);
			} catch(ThrottlingConfigurationException e) {
				if (e.getCause() instanceof org.simpleframework.xml.core.ElementException) {
					setConfigurationErrorMessage(e.getCause().getMessage());
				} else if (e.getErrors() != null) {
					setConfigurationErrorMessage(e.getErrors());
				} else {
					setConfigurationErrorMessage(e.getMessage());
				}
			} finally {
				ThrottlingStorage.removeCache();
			}
		}

		return "SUCCESS";
	}
	
	private void setSuccessfulUpload(boolean successfulUpload) {
		this.successfulUpload = successfulUpload;
	}

	public void setUpload(File file) {
		this.file = file;
	}	
	
	public void setUploadContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setUploadFileName(String filename) {
		this.filename = filename;
	}

	public boolean isSuccessfulUpload() {
		return successfulUpload;
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
		setSuccessfulUpload(false);
	}

}
