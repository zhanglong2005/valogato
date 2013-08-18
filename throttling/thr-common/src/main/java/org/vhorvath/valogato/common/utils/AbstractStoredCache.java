package org.vhorvath.valogato.common.utils;

/**
 * @author Viktor Horvath
 */
public abstract class AbstractStoredCache {
	
	private Object cache = null;

	public Object getCache() {
		return cache;
	}

	public void setCache(Object cache) {
		this.cache = cache;
	}
	
	public abstract void shutdown();
	
}
