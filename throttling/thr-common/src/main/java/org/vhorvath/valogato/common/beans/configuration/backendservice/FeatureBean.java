package org.vhorvath.valogato.common.beans.configuration.backendservice;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;

/**
 * @author Viktor Horvath
 */
public class FeatureBean implements Serializable {

	private static final long serialVersionUID = -4144699938927710788L;

	@Attribute
	private String name;
	
	@ElementMap(entry="param", key="name", attribute=true, inline=true, required=false)
	private Map<String, String> params;	
	
	public String getName() {
		return name;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	} 
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("name=").append(name);
		sb.append(",params=(");
		if (params != null) {
			Iterator<String> iterator = params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				sb.append(key).append("=").append(params.get(key)).append(",");
			}
		}
		sb.append(")]");
		return sb.toString();
	}
	
}
