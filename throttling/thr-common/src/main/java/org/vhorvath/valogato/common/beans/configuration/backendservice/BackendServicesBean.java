package org.vhorvath.valogato.common.beans.configuration.backendservice;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * @author Viktor Horvath
 */
@Root(name="backendServices")
public class BackendServicesBean implements Serializable {

	
	private static final long serialVersionUID = 9220445450942554768L;

	@ElementList(required=false, inline=true, entry="backendService")
	private List<BackendServiceBean> backendService;

	
	public List<BackendServiceBean> getBackendService() {
		return backendService;
	}
	
	public void setBackendService(List<BackendServiceBean> backendService) {
		this.backendService = backendService;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		if (backendService != null) {
			for(BackendServiceBean oneBackendServiceBean : backendService) {
				sb.append(oneBackendServiceBean).append(",");			
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
