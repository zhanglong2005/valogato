package org.vhorvath.valogato.web.beans.backendservice;

public class FeatureWebBean {

	private String key;
	private String value;
	
	public FeatureWebBean(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("key=").append(key);
		sb.append(",value=").append(value);
		sb.append("]");
		return sb.toString();
	}
}
