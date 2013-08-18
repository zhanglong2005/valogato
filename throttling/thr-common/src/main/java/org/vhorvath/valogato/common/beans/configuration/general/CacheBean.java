package org.vhorvath.valogato.common.beans.configuration.general;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;

/**
 * @author Viktor Horvath
 */
public class CacheBean implements Serializable {

	private static final long serialVersionUID = 4820267894701930398L;

	@Attribute
	private String type;
	
	@ElementMap(entry="param", key="name", attribute=true, inline=true, required=false)
	private Map<String, String> params;	
	
	public String getType() {
		return type;
	}

	public Map<String, String> getParams() {
		return params;
	} 
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("[");
		sb.append("type="+type);
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
