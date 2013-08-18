package org.vhorvath.valogato.common.dao.lowlevel.cache;

import java.util.List;

import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

/**
 * @author Viktor Horvath
 */
public interface ICache {

	void lock(String key) throws ThrottlingConfigurationException;
	
	// the operation unlock shouldn't throw an exception if there was not a lock on the key earlier
	void unlock(String key) throws ThrottlingConfigurationException;
	
	<T> T get(String key, Class<T> type) throws ThrottlingConfigurationException;
	
	void put(String key, Object value) throws ThrottlingConfigurationException;
	
	List<String> getKeys() throws ThrottlingConfigurationException;
	
	void remove(String key) throws ThrottlingConfigurationException;
	
}
