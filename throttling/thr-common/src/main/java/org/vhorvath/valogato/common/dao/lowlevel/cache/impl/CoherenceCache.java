package org.vhorvath.valogato.common.dao.lowlevel.cache.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.AbstractStoredCache;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;

import com.google.gson.Gson;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

/**
 * @author Viktor Horvath
 */
public class CoherenceCache implements ICache {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	
	public void lock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### CoherenceCache.lock(%s)...", key));
		getCache().lock(key);
		LOGGER.trace(String.format("####### Getting the CoherenceCache.lock(%s) was successful!", key));
	}


	public void unlock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### CoherenceCache.unlock(%s)", key));
		
		try {
			getCache().unlock(key);
		} catch(Exception e) {
			LOGGER.warn(String.format("####### The Coherence lock cannot be released! key = %s, reason = %s", key, ""+e));
		}
	}

	
	public <T> T get(String key, Class<T> type) throws ThrottlingConfigurationException {
		T value = null;
		try {
			Object cacheValue = getCache().get(key);
			if (cacheValue == null) {
				return null;
			} else {
				try {
					// deserialize from JSON
					Gson gson = new Gson();
					value = gson.fromJson(cacheValue.toString(), type);
					return value;
				} catch(ClassCastException cce) {
					throw new ThrottlingConfigurationException(String.format("The type of the element '%s' is not %s! it was: %s", 
							key, type, cacheValue.getClass()), cce);
				}
			}
		} finally {
			LOGGER.trace(String.format("####### CoherenceCache.get(%s, %s) = %s", key, type, value));
		}
	}

	
	public void put(String key, Object value) throws ThrottlingConfigurationException {
		// serialize to JSON
		Gson gson = new Gson();
		String jsonString = gson.toJson(value);
		LOGGER.trace(String.format("####### CoherenceCache.put(%s, %s)", key, jsonString));
		// put into the cache
		getCache().put(key, jsonString);
	}

	
	public List<String> getKeys() throws ThrottlingConfigurationException {
		LOGGER.trace("####### CoherenceCache.getKeys()");

		List<String> keys = new ArrayList<String>();
		Iterator<String> iterator = getCache().keySet().iterator();
		while (iterator.hasNext()) {
			keys.add(iterator.next());
		}
		
		return keys;
	}

	
	public void remove(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### TerracottaCache.remove(%s)", key));
		
		getCache().remove(key);
	}

	
	private synchronized NamedCache getCache() throws ThrottlingConfigurationException {
		if (ThrottlingStorage.getCache() == null) {
			// getting the cache
			String cacheName = GeneralConfigurationUtils.getCache().getParams().get("distributedCacheName");
			CacheFactory.ensureCluster();
			final NamedCache cache = CacheFactory.getCache(cacheName);
			AbstractStoredCache storedCache = new AbstractStoredCache() {
				@Override
				public void shutdown() {
					CacheFactory.releaseCache(cache);
				}
			};
			storedCache.setCache(cache);
			ThrottlingStorage.setCache(storedCache);
		}
		return (NamedCache)ThrottlingStorage.getCache().getCache();
	}

	
}
