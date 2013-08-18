package org.vhorvath.valogato.common.dao.lowlevel.cache.impl;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

/**
 * @author Viktor Horvath
 */
public class DummyCache implements ICache {
	
	private final Logger logger = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	private static Map<String, Object> dummyCache = new Hashtable<String, Object>();
	
	public void lock(String key) throws ThrottlingConfigurationException {
		logger.debug(String.format("##### Locking the key %s", key));
	}

	public void unlock(String key) throws ThrottlingConfigurationException {
		logger.debug(String.format("##### Unlocking the key %s", key));
	}

	public <T> T get(String key, Class<T> type) throws ThrottlingConfigurationException {
		logger.debug(String.format("##### DummyCache.get(%s, %s)", key, type));
		return type.cast(dummyCache.get(key));
	}

	public void put(String key, Object value) throws ThrottlingConfigurationException {
		logger.debug(String.format("##### DummyCache.put(%s, %s)", key, value));
		dummyCache.put(key, (BackendServiceFreqBean)value);
	}

	public List<String> getKeys() throws ThrottlingConfigurationException {
		return Arrays.asList(dummyCache.keySet().toArray(new String[0]));
	}

	public void remove(String key) {
		dummyCache.remove(key);
	}

}