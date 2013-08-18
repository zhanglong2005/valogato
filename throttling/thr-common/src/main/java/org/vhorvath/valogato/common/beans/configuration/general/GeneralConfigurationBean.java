package org.vhorvath.valogato.common.beans.configuration.general;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * @author Viktor Horvath
 */
@Root(name="generalConfiguration")
public class GeneralConfigurationBean implements Serializable {

	private static final long serialVersionUID = -1992569446589721841L;

	@ElementList(inline=true, required=false, entry="newFeature")
	private List<NewFeatureBean> newFeature;

	@Element(name="backendserviceConfigSource")
	private String backendserviceConfigSource;
	
	@Element(name="statisticsSource")
	private String statisticsSource;

	@Element(name="cache")
	private CacheBean cache;
	
	public CacheBean getCache() {
		return cache;
	}

	public String getBackendserviceConfigSource() {
		return backendserviceConfigSource;
	}

	public List<NewFeatureBean> getNewFeature() {
		return newFeature;
	}
	
	public String getStatisticsSource() {
		return statisticsSource;
	}

	public void setStatisticsSource(String statisticsSource) {
		this.statisticsSource = statisticsSource;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("backendserviceConfigSource=").append(backendserviceConfigSource).append(",");
		sb.append("statisticsSource=").append(statisticsSource).append(",");
		if (newFeature != null) {
			for(NewFeatureBean oneNewFeatureBean : newFeature) {
				sb.append(oneNewFeatureBean).append(",");
			}
		}
		sb.append(",cache=").append(cache);
		sb.append("]");
		return sb.toString();
	}

}
