package org.vhorvath.valogato.common.dao.lowlevel.cache.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.AbstractStoredCache;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

import com.google.gson.Gson;
import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemcachedCache implements ICache {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);
	private static final String LOCK_PREFIX = "THROTTLING_8erqD4c_LOCK_";
	
//	private static final String[] servers = {"localhost:11241","localhost:11242"};
	static {
		//http://grepcode.com/file/repo1.maven.org/maven2/com.whalin/Memcached-Java-Client/3.0.1/com/whalin/MemCached/SockIOPool.java?av=f
		try {
	        SockIOPool pool = SockIOPool.getInstance("Throttling_pool");
	        pool.setServers( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("servers"), String[].class) );
	        pool.setHashingAlg( getHashingAlg(GeneralConfigurationUtils.getCache().getParams().get("hashingAlg")) );
	        pool.setFailover( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("failover"), Boolean.class) );
	        pool.setInitConn( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("initConn"), Integer.class) );
	        pool.setMinConn( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("minConn"), Integer.class) );
	        pool.setMaxConn( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("maxConn"), Integer.class) );
	        pool.setMaintSleep( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("maintSleep"), Integer.class) );
	//        pool.setNagle( true );
	        // socket timeout
	        pool.setSocketTO( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("socketTO"), Integer.class) );
	        pool.setAliveCheck( ThrottlingUtils.get(GeneralConfigurationUtils.getCache().getParams().get("aliveCheck"), Boolean.class) );
	        
	        pool.initialize();
		} catch(Exception e) {
			LOGGER.error("Initialization of MemcachedCache was not successful!", e);
		}
	}

	
	public void lock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### MemcachedCache.lock(%s)...", key));
		MemCachedClient client = getMemcachedInstance();
		while(!client.add(LOCK_PREFIX + key, LOCK_PREFIX + key)) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				LOGGER.trace(String.format("####### MemcachedCache.lock(%s)...", key), e);
			}
		}
		LOGGER.trace(String.format("####### Getting the MemcachedCache.lock(%s) was successful!", key));
	}

	
	private static int getHashingAlg(String hashingAlgText) throws ThrottlingConfigurationException {
		if (hashingAlgText == null) {
			return SockIOPool.CONSISTENT_HASH;
		} else if (hashingAlgText.equals("CONSISTENT_HASH")) {
			return SockIOPool.CONSISTENT_HASH;
		} else if (hashingAlgText.equals("OLD_COMPAT_HASH")) {
			return SockIOPool.OLD_COMPAT_HASH;
		} else if (hashingAlgText.equals("NEW_COMPAT_HASH")) {
			return SockIOPool.NEW_COMPAT_HASH;
		} else if (hashingAlgText.equals("NATIVE_HASH")) {
			return SockIOPool.NATIVE_HASH;
		}
		throw new ThrottlingConfigurationException(String.format("Incorert hashingAlg text in the configuration file! The correct values: %s, %s, %s, %s", 
				SockIOPool.NATIVE_HASH, SockIOPool.OLD_COMPAT_HASH, SockIOPool.NEW_COMPAT_HASH, SockIOPool.CONSISTENT_HASH));
	}


	public void unlock(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### MemcachedCache.unlock(%s)", key));
		
		try {
			MemCachedClient client = getMemcachedInstance();
			client.delete(LOCK_PREFIX + key);
		} catch(Exception e) {
			LOGGER.warn(String.format("####### The MemcachedCache lock cannot be released! key = %s, reason = %s", key, ""+e));
		}
	}

	
	public <T> T get(String key, Class<T> type) throws ThrottlingConfigurationException {
		T value = null;
		try {
			String json = (String) getMemcachedInstance().get(key);
			if (json == null) {
				return null;
			} else {
				try {
					// deserialize from JSON
					Gson gson = new Gson();
					value = gson.fromJson(json, type);
					return value;
				} catch(ClassCastException cce) {
					throw new ThrottlingConfigurationException(String.format("The type of the element '%s' is not %s! it was: %s", 
							key, type, map.get(ThrConstants.CACHE_KEY_FOR_VALUE).getClass()), cce);
				}
			}
		} finally {
			LOGGER.trace(String.format("####### MemcachedCache.get(%s, %s) = %s", key, type, value));
		}
	}

	
	public void put(String key, Object value) throws ThrottlingConfigurationException {
		// serialize to JSON
		Gson gson = new Gson();
		String jsonString = gson.toJson(value);
		LOGGER.trace(String.format("####### MemcachedCache.put(%s, %s)", key, jsonString));
		// put into the cache
		getMemcachedInstance().set(key, jsonString);
	}

	
	public List<String> getKeys() throws ThrottlingConfigurationException {
		LOGGER.trace("####### MemcachedCache.getKeys()");
		List<String> keys = new ArrayList<String>();
		MemCachedClient client = getMemcachedInstance();
		
		// getting the item number for getting the keys from the cache dump
		
		Map<String, Map<String, String>> items = client.statsItems();
        Iterator<String> itemsKeysIterator = items.keySet().iterator();
        // a set for the item details (e.g. ['items:1:evicted_time', 'items:1:age'], ...)
        Set<String> itemNumberDetailsSet = new HashSet<String>();
        while(itemsKeysIterator.hasNext()) {
        	// e.g. 'localhost:11241'
            String server = itemsKeysIterator.next();
            itemNumberDetailsSet.addAll(items.get(server).keySet());
        }

        // getting the item number and putting them in a set
        Set<String> itemNumbersSet = new HashSet<String>();
        Iterator<String> itemNumberDetailsIterator = itemNumberDetailsSet.iterator();
        while(itemNumberDetailsIterator.hasNext()) {
            String noText = itemNumberDetailsIterator.next();
            itemNumbersSet.add(noText.split(":")[1]);
        }
        // getting the keys from the cache dumps one by one based on the 
        Iterator<String> itemNumbersIterator = itemNumbersSet.iterator();
        while(itemNumbersIterator.hasNext()) {
            String itemNumberText = itemNumbersIterator.next();
            Map<String, Map<String, String>> cacheDumps = client.statsCacheDump(Integer.parseInt(itemNumberText), 999999);
            // getting the key from the cache dump
            Iterator<String> cacheDumpsIterator = cacheDumps.keySet().iterator();
            while(cacheDumpsIterator.hasNext()) {
                String server = cacheDumpsIterator.next();
                Map<String, String> keysInOneServer = cacheDumps.get(server);
                keys.addAll(keysInOneServer.keySet());
            }
            
        }
		LOGGER.trace("####### keys="+keys);
		return keys;
	}

	
	public void remove(String key) throws ThrottlingConfigurationException {
		LOGGER.trace(String.format("####### MemcachedCache.remove(%s)", key));
		getMemcachedInstance().delete(key);
	}

	
	public void shutdown() throws ThrottlingConfigurationException {
	}

	
	private synchronized MemCachedClient getMemcachedInstance() throws ThrottlingConfigurationException {
		if (ThrottlingStorage.getCache() == null) {
			// creating the memcached client
			// adding it to the ThreadLocal
			AbstractStoredCache storedCache = new AbstractStoredCache() {
				@Override
				public void shutdown() { }
			};
			storedCache.setCache(new MemCachedClient("Throttling_pool"));
			ThrottlingStorage.setCache(storedCache);
		}
		return (MemCachedClient)ThrottlingStorage.getCache().getCache();
	}

}
