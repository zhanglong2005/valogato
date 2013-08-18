package org.vhorvath.valogato.web.beans.backendservice;

public class SimulatedServiceWebBean {

	private String simulatedService;
	private String feature;
	private String backendService;
	
	public SimulatedServiceWebBean(String simulatedService, String feature, String backendService) {
		this.simulatedService = simulatedService;
		this.feature = feature;
		this.backendService = backendService;
	}

	public String getSimulatedService() {
		return simulatedService;
	}
	
	public void setSimulatedService(String simulatedService) {
		this.simulatedService = simulatedService;
	}
	
	public String getFeature() {
		return feature;
	}
	
	public void setFeatures(String feature) {
		this.feature = feature;
	}
	
	public String getBackendService() {
		return backendService;
	}
	
	public void setBackendService(String backendService) {
		this.backendService = backendService;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("simulatedService=").append(simulatedService);
		sb.append(",feature=").append(feature);
		sb.append(",backendService=").append(backendService);
		sb.append("]");
		return sb.toString();
	}
}
