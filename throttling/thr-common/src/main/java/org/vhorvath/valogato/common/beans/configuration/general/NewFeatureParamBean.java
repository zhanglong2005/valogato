package org.vhorvath.valogato.common.beans.configuration.general;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

/**
 * @author Viktor Horvath
 */
public class NewFeatureParamBean implements Serializable {

	private static final long serialVersionUID = -5945331280081730395L;

	@Attribute
	private String name;

	@Attribute
	private String title;
	
	public NewFeatureParamBean() {
	}
	
	public NewFeatureParamBean(String name, String title) {
		this.name = name;
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("name=").append(name);
		sb.append(",title=").append(title);
		sb.append("]");
		return sb.toString();
	}

}
