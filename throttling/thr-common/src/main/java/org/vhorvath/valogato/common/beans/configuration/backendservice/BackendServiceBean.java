package org.vhorvath.valogato.common.beans.configuration.backendservice;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

/**
 * @author Viktor Horvath
 */
public class BackendServiceBean implements Serializable {

	private static final long serialVersionUID = -4538737569086852521L;

	@Attribute
	private String name;
	
	@Attribute
	private Integer maxLoading;
	
	// TODO this is not the average response time but how much time the feature should wait...
	@Attribute
	private Integer averageResponseTime;
	
	@Element(name="feature")
	private FeatureBean feature;
	
	@ElementList(inline=true, required=false, entry="simulatedService")
	private List<SimulatedServiceBean> simulatedService;
	

	public List<SimulatedServiceBean> getSimulatedService() {
		return simulatedService;
	}

	public String getName() {
		return name;
	}

	public Integer getMaxLoading() {
		return maxLoading;
	}

	public Integer getAverageResponseTime() {
		return averageResponseTime;
	}

	public FeatureBean getFeature() {
		return feature;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMaxLoading(Integer maxLoading) {
		this.maxLoading = maxLoading;
	}

	public void setAverageResponseTime(Integer averageResponseTime) {
		this.averageResponseTime = averageResponseTime;
	}

	public void setFeature(FeatureBean feature) {
		this.feature = feature;
	}

	public void setSimulatedService(List<SimulatedServiceBean> simulatedService) {
		this.simulatedService = simulatedService;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("name=").append(name);
		sb.append(",maxLoading=").append(maxLoading);
		sb.append(",averageResponseTime=").append(averageResponseTime);
		sb.append(",feature=").append(feature);
		if (simulatedService != null) {
			for(SimulatedServiceBean oneSimulatedServiceBean : simulatedService) {
				sb.append(oneSimulatedServiceBean).append(",");			
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
}
