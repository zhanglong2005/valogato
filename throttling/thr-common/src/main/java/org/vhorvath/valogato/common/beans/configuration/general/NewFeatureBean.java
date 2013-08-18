package org.vhorvath.valogato.common.beans.configuration.general;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;

/**
 * @author Viktor Horvath
 */
public class NewFeatureBean implements Serializable {

	private static final long serialVersionUID = 7246926686123231700L;

	@Attribute
	private String name;

	@Attribute
	private String clazz;
	
	@ElementList(required=false, inline=true, entry="param")
	private List<NewFeatureParamBean> param;


	public String getName() {
		return name;
	}

	public String getClazz() {
		return clazz;
	}
	
	public List<NewFeatureParamBean> getParam() {
		return param;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public void setParam(List<NewFeatureParamBean> param) {
		this.param = param;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("name=").append(name);
		sb.append(",clazz=").append(clazz);
		if (param != null) {
			for(NewFeatureParamBean oneNewFeatureParamBean : param) {
				sb.append(oneNewFeatureParamBean).append(",");			
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
