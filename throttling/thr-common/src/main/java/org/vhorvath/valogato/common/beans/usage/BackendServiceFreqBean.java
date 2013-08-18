package org.vhorvath.valogato.common.beans.usage;

import java.io.Serializable;

/**
 * @author Viktor Horvath
 */
public class BackendServiceFreqBean implements Serializable {

	private static final long serialVersionUID = -4861391153L;

	private String nameBackendService;
	private Integer frequency;
	
	public BackendServiceFreqBean() { }
	
	public BackendServiceFreqBean(String nameBackendService, Integer frequency) {
		this.nameBackendService = nameBackendService;
		this.frequency = frequency;
	}

	public String getNameBackendService() {
		return nameBackendService;
	}
	
	public void setNameBackendService(String nameBackendService) {
		this.nameBackendService = nameBackendService;
	}
	
	public Integer getFrequency() {
		return frequency;
	}
	
	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("nameBackendService=").append(nameBackendService);
		sb.append(", frequency=").append(frequency);
		sb.append("]");
		return sb.toString();
	}

}
