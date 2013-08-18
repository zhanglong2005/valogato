package org.vhorvath.valogato.web.beans.frequency;

public class FrequencyAndSleepingReqWebBean {

	private String nameBackendService;
	private Integer frequency;
	private Integer sleepingRequests;
	
	public FrequencyAndSleepingReqWebBean(String nameBackendService, Integer frequency, Integer sleepingRequests) {
		this.nameBackendService = nameBackendService;
		this.frequency = frequency;
		this.sleepingRequests = sleepingRequests;
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
