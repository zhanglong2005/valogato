package org.vhorvath.valogato.common.beans.usage;

import java.io.Serializable;

/**
 * @author Viktor Horvath
 */
public class WaitingReqFirstLastListBean implements Serializable {

	private static final long serialVersionUID = 89224186431063438L;

	public WaitingReqFirstLastListBean() {	}
	
	public WaitingReqFirstLastListBean(Integer first, Integer last) {
		this.first = first;
		this.last = last;
	}
	
	private Integer first = null;
	private Integer last = null;
	
	public Integer getFirst() {
		return first;
	}
	
	public void setFirst(Integer first) {
		this.first = first;
	}

	public Integer getLast() {
		return last;
	}

	public void setLast(Integer last) {
		this.last = last;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("first=").append(first);
		sb.append(",last=").append(last);
		sb.append("]");
		return sb.toString();
	}
	
}
