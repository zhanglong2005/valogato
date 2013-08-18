package org.vhorvath.valogato.web.actions.backendServiceConfig;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.web.actions.ThrottlingActionSupport;
import org.vhorvath.valogato.web.beans.backendservice.BackendServiceWebBean;
import org.vhorvath.valogato.web.utils.WebBeanUtils;


public class BackendServiceConfigListAction extends ThrottlingActionSupport {
	
	
	private static final long serialVersionUID = 5968093251837908704L;

	private List<BackendServiceWebBean> backendServiceConfigList;
	private InputStream inputStream;
	private String buttonName;
	
	
	public String execute() throws ThrottlingConfigurationException {
		String action = null;
		
		try {
			// clicking on the button 'Export as XML'
			if (buttonName != null && buttonName.equals("Export as XML")) {
				String xml = BackendServiceConfigDAOFactory.getDAO().getBackendServicesAsXMLString();
				setInputStream(new ByteArrayInputStream(xml.getBytes()));
				action = "DOWNLOAD";
			} 
			// populating the list
			else {
				setBackendServiceConfigList(getList());
				action = "SUCCESS";
			}
		} finally {
			ThrottlingStorage.removeCache();
		}
		
		return action;
	}
	
	
	private List<BackendServiceWebBean> getList() throws ThrottlingConfigurationException {
		List<BackendServiceBean> backendServices = BackendServiceConfigDAOFactory.getDAO().getBackendServices();
		List<BackendServiceWebBean> backendServicesWeb = new ArrayList<BackendServiceWebBean>();
		for(BackendServiceBean backendService : backendServices) {
			backendServicesWeb.add(new BackendServiceWebBean(backendService.getName(), 
					                                         backendService.getMaxLoading(), 
					                                         backendService.getAverageResponseTime(), 
					                                         WebBeanUtils.getCommaSeparatedFeatures(backendService),
					                                         WebBeanUtils.getCommaSeparatedSimServices(backendService)));
		}
		return backendServicesWeb;
	}

	
	public List<BackendServiceWebBean> getBackendServiceConfigList() {
		return backendServiceConfigList;
	}

	
	public void setBackendServiceConfigList(List<BackendServiceWebBean> backendServiceConfigList) {
		this.backendServiceConfigList = backendServiceConfigList;
	}


	public InputStream getInputStream() {
		return inputStream;
	}


	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}


	public String getButtonName() {
		return buttonName;
	}


	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}
	
	
}
