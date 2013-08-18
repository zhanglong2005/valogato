package org.vhorvath.valogato.common.beans.configuration.backendservice;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * @author Viktor Horvath
 */
public class SimulatedServiceBean implements Serializable {

	private static final long serialVersionUID = -5766220710029492398L;

	@Attribute
	private String name;

	@Attribute(required=false)
	private Integer maxLoading;
	
	@Attribute(required=false)
	private Integer averageResponseTime;

	@Element(name="feature")
	private FeatureBean feature;

	
	public String getName() {
		return name;
	}

	public FeatureBean getFeature() {
		return feature;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFeature(FeatureBean feature) {
		this.feature = feature;
	}

	public Integer getMaxLoading() {
		return maxLoading;
	}

	public void setMaxLoading(Integer maxLoading) {
		this.maxLoading = maxLoading;
	}

	public Integer getAverageResponseTime() {
		return averageResponseTime;
	}

	public void setAverageResponseTime(Integer averageResponseTime) {
		this.averageResponseTime = averageResponseTime;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("name=").append(name);
		sb.append("maxLoading=").append(maxLoading);
		sb.append("averageResponseTime=").append(averageResponseTime);
		sb.append(",feature=").append(feature).append("]");
		return sb.toString();
	}

}
