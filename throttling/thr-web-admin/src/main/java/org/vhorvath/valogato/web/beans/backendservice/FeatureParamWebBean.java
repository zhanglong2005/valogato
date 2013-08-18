package org.vhorvath.valogato.web.beans.backendservice;

public class FeatureParamWebBean {

	private String name;
	private String value;
	private String title;
	
	public FeatureParamWebBean(String name, String value, String title) {
		this.name = name;
		this.value = value;
		this.title = title;
	}
	
	public FeatureParamWebBean() {
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("name=").append(name);
		sb.append(",value=").append(value);
		sb.append(",title=").append(title);
		sb.append("]");
		return sb.toString();
	}

}
