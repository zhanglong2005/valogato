package org.vhorvath.valogato.common.dao.lowlevel.cache.impl;


import java.net.URL;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

import com.google.gson.Gson;


/**
 * @author Viktor Horvath
 */
public class LocalCache implements ICache {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	private static final String LOCAL_CACHE_NAME = "LOCAL_THROTTLING_DISTRIBUTED_STORE";

	
	public void lock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### LocalCache.lock(%s)...", key));
		getCache().acquireWriteLockOnKey(key);
		LOGGER.trace(String.format("####### Getting the LocalCache.lock(%s) was successful!", key));
	}

	
	public void unlock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### LocalCache.unlock(%s)", key));
		
		try {
			getCache().releaseWriteLockOnKey(key);
		} catch(Exception e) {
			LOGGER.warn(String.format("####### The LocalCache lock cannot be released! key = %s, reason = %s", key, ""+e));
		}
	}

	
	public <T> T get(String key, Class<T> type) throws ThrottlingConfigurationException {
		T value = null;
		try {
			Element element = getCache().get(key);
			if (element == null) {
				return null;
			} else {
				try {
					// deserialize from JSON
					Gson gson = new Gson();
					value = gson.fromJson((String)element.getObjectValue(), type);
					return value;
				} catch(ClassCastException cce) {
					throw new ThrottlingConfigurationException(String.format("The type of the element '%s' is not %s! it was: %s", 
							key, type, element.getObjectValue().getClass()), cce);
				}
			}
		} finally {
			LOGGER.trace(String.format("####### LocalCache.get(%s, %s) = %s", key, type, value));
		}
	}

	
	public void put(String key, Object value) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### LocalCache.put(%s, %s)", key, value));
		// serialize to JSON
		Gson gson = new Gson();
		String jsonString = gson.toJson(value);
		// put into the cache		
		getCache().put(new Element(key, jsonString));
	}
	
	
	public List<String> getKeys() throws ThrottlingConfigurationException {
		LOGGER.trace("####### LocalCache.getKeys()");
		List<String> keys = getCache().getKeys();
		LOGGER.trace("####### keys="+keys);
		return keys;
	}

	
	public void remove(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### LocalCache.remove(%s)", key));
		getCache().remove(key);
	}

	
	private Cache getCache() throws ThrottlingConfigurationException {
		// the name of the ehcache should be able to be configured in the general config XML
		URL url = getClass().getResource("/valogato_local_ehcache.xml");
		CacheManager manager = CacheManager.create(url);
		
		Cache cache = manager.getCache(LOCAL_CACHE_NAME);
		return cache;
	}

	
}
