package org.vhorvath.valogato.common.dao.lowlevel.cache;

import org.vhorvath.valogato.common.beans.configuration.general.CacheBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.impl.DummyCache;
import org.vhorvath.valogato.common.dao.lowlevel.cache.impl.HazelcastCache;
import org.vhorvath.valogato.common.dao.lowlevel.cache.impl.TerracottaCache;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;

/**
 * @author Viktor Horvath
 */
public final class CacheDAOFactory {

	private CacheDAOFactory() { }
	
	// the factory class will return only one instance PER THREAD! (and not per class...)
	public static synchronized ICache getCache() throws ThrottlingConfigurationException {
		ICache result = ThrottlingStorage.getCacheInstance();
		
		if (result == null) {
			CacheBean cacheBean = GeneralConfigurationUtils.getCache();
			if (cacheBean.getType().equals(ThrConstants.CacheType.hazelcast.toString())) {
				result = new HazelcastCache();
			} else if (cacheBean.getType().equals(ThrConstants.CacheType.terracotta.toString())) {
				result = new TerracottaCache();
			} else if (cacheBean.getType().equals(ThrConstants.CacheType.dummy.toString())) {
				result = new DummyCache();
			} else {
				throw new ThrottlingConfigurationException(String.format("Uknown cache type in the configuration: %s!", cacheBean.getType()));
			}
			ThrottlingStorage.setCacheInstance(result);
		}
		
		return result;
	}
	
}
