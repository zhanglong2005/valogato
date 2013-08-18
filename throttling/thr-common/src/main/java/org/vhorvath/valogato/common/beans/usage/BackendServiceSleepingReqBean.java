package org.vhorvath.valogato.common.beans.usage;

import java.io.Serializable;

/**
 * @author Viktor Horvath
 */
public class BackendServiceSleepingReqBean implements Serializable {

	private static final long serialVersionUID = -4861391153L;

	private String nameBackendService;
	private Integer numberOfSleepingRequests;
	
	public BackendServiceSleepingReqBean() { }
	
	public BackendServiceSleepingReqBean(String nameBackendService, Integer numberOfSleepingRequests) {
		this.nameBackendService = nameBackendService;
		this.numberOfSleepingRequests = numberOfSleepingRequests;
	}

	public String getNameBackendService() {
		return nameBackendService;
	}
	
	public void setNameBackendService(String nameBackendService) {
		this.nameBackendService = nameBackendService;
	}
	
	public Integer getNumberOfSleepingRequests() {
		return numberOfSleepingRequests;
	}

	public void setNumberOfSleepingRequests(Integer numberOfSleepingRequests) {
		this.numberOfSleepingRequests = numberOfSleepingRequests;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("nameBackendService=").append(nameBackendService);
		sb.append(", numberOfSleepingRequests=").append(numberOfSleepingRequests);
		sb.append("]");
		return sb.toString();
	}

}
