package org.vhorvath.valogato.common.dao.lowlevel.cache.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.AbstractStoredCache;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;

import com.google.gson.Gson;
import com.hazelcast.client.ClientConfig;
import com.hazelcast.client.ClientConfigBuilder;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Instance;
import com.tangosol.net.CacheFactory;


/**
 * @author Viktor Horvath
 */
public class HazelcastCache implements ICache {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);

	
	public void lock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### HazelcastCache.lock(%s)...", key));
		getHazelcastInstance().getLock(key).lock();
		LOGGER.trace(String.format("####### Getting the HazelcastCache.lock(%s) was successful!", key));
	}

	
	public void unlock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### HazelcastCache.unlock(%s)", key));
		
		try {
			getHazelcastInstance().getLock(key).forceUnlock();
		} catch(Exception e) {
			LOGGER.warn(String.format("####### The HazelcastCache lock cannot be released! key = %s, reason = %s", key, ""+e));
		}
	}

	
	public <T> T get(String key, Class<T> type) throws ThrottlingConfigurationException {
		T value = null;
		try {
			Map<String, String> map = getHazelcastInstance().getMap(key);
			if (map == null || map.get(ThrConstants.CACHE_KEY_FOR_VALUE) == null) {
				return null;
			} else {
				try {
					// deserialize from JSON
					Gson gson = new Gson();
					value = gson.fromJson(map.get(ThrConstants.CACHE_KEY_FOR_VALUE), type);
					return value;
				} catch(ClassCastException cce) {
					throw new ThrottlingConfigurationException(String.format("The type of the element '%s' is not %s!", 
							key, type), cce);
				}
			}
		} finally {
			LOGGER.trace(String.format("####### HazelcastCache.get(%s, %s) = %s", key, type, value));
		}
	}

	
	public void put(String key, Object value) throws ThrottlingConfigurationException {
		// serialize to JSON
		Gson gson = new Gson();
		String jsonString = gson.toJson(value);
		LOGGER.trace(String.format("####### HazelcastCache.put(%s, %s)", key, jsonString));
		// put into the cache
		Map<String, String> map = getHazelcastInstance().getMap(key);
		map.put(ThrConstants.CACHE_KEY_FOR_VALUE, jsonString);
	}

	
	public List<String> getKeys() throws ThrottlingConfigurationException {
		LOGGER.trace("####### HazelcastCache.getKeys()");
		List<String> keys = new ArrayList<String>();
		Iterator<Instance> instances = getHazelcastInstance().getInstances().iterator();
		while (instances.hasNext()) {
			Instance instance = instances.next();
			if (instance.getInstanceType().isMap()) {
				String id = instance.getId().toString();
				if (id.contains(":")) {
					keys.add(id.substring(id.indexOf(":")+1));
				} else {
					keys.add(instance.getId().toString());
				}
			}	
		}
		LOGGER.trace("####### keys="+keys);
		return keys;
	}

	
	public void remove(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### HazelcastCache.remove(%s)", key));
		getHazelcastInstance().getMap(key).destroy();
	}

	
//	public void shutdown() throws ThrottlingConfigurationException {
//		getHazelcastInstance().shutdown();
//	}

	
	private synchronized HazelcastClient getHazelcastInstance() throws ThrottlingConfigurationException {
		if (ThrottlingStorage.getCache() == null) {
			// creating the HazelcastClient
			synchronized (ClientConfigBuilder.class) {
				ClientConfigBuilder builder = null;
				try {
					builder = new ClientConfigBuilder(ThrConstants.PATH_HAZELCAST_CLIENT_CONFIG_FILE);
				} catch (IOException e) {
					throw new ThrottlingConfigurationException(String.format("The file %s cannot be found!", ThrConstants.PATH_HAZELCAST_CLIENT_CONFIG_FILE), e);
				}
				ClientConfig clientConfig = builder.build();
				// adding it to the ThreadLocal
				final HazelcastClient hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
				AbstractStoredCache storedCache = new AbstractStoredCache() {
					@Override
					public void shutdown() {
						hazelcastClient.shutdown();
					}
				};
				storedCache.setCache(hazelcastClient);
				ThrottlingStorage.setCache(storedCache);
			}
		}
		return (HazelcastClient)ThrottlingStorage.getCache().getCache();
	}


}