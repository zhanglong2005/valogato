package org.vhorvath.valogato.web.beans.backendservice;

public class BackendServiceWebBean {

    private String backendService;
    private Integer maxLoading;
    private Integer averageResponseTime;
    private String features;
    private String simulatedServices;
    
    public BackendServiceWebBean() {	
    }
    
    public BackendServiceWebBean(String backendService, Integer maxLoading, Integer averageResponseTime, String features,
			String simulatedServices) {
        this.backendService = backendService;
        this.maxLoading = maxLoading;
        this.averageResponseTime = averageResponseTime;
        this.features = features;
        this.simulatedServices = simulatedServices;
    }
    
	public String getBackendService() {
		return backendService;
	}

	public Integer getMaxLoading() {
		return maxLoading;
	}
	
	public Integer getAverageResponseTime() {
		return averageResponseTime;
	}

	public String getFeatures() {
		return features;
	}

	public String getSimulatedServices() {
		return simulatedServices;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("backendService=").append(backendService);
		sb.append(",maxLoading=").append(maxLoading);
		sb.append(",averageResponseTime=").append(averageResponseTime);
		sb.append(",features=").append(features);
		sb.append(",simulatedServices=").append(simulatedServices);
		sb.append("]");
		return sb.toString();
	}

}