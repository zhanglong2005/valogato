package org.vhorvath.valogato.common.utils;

import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.IBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.IWaitingReqDAO;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;

/**
 * @author Viktor Horvath
 */
public final class ThrottlingStorage {

	private static final ThreadLocal<Boolean> storageChangedSleepingReqNumber = new ThreadLocal<Boolean>();
	private static final ThreadLocal<Boolean> storageAddedToWaitingReqList = new ThreadLocal<Boolean>();
	private static final ThreadLocal<Boolean> storageChangedFreq = new ThreadLocal<Boolean>();
	private static final ThreadLocal<AbstractStoredCache> storageCache = new ThreadLocal<AbstractStoredCache>();
	
	private static final ThreadLocal<IBackendServiceConfigDAO> storageBackendServiceConfigDAOInstance = new ThreadLocal<IBackendServiceConfigDAO>();
	private static final ThreadLocal<IUsageDAO> storageUsageDAOInstance = new ThreadLocal<IUsageDAO>();
	private static final ThreadLocal<IWaitingReqDAO> storageWaitingReqDAOInstance = new ThreadLocal<IWaitingReqDAO>();
	private static final ThreadLocal<ICache> storageCacheInstance = new ThreadLocal<ICache>();
	
	private ThrottlingStorage() { }
	
	// storageAddedToWaitingReqList
	public static void setAddedToWaitingReqList(Boolean added) {
		storageAddedToWaitingReqList.set(added);
	}
	public static Boolean isAddedToWaitingReqList() {
		return storageAddedToWaitingReqList.get();
	}
	
	// storageChangedTheForWaitingReqNumber
	public static void setChangedTheSleepingReqNumber(Boolean changed) {
		storageChangedSleepingReqNumber.set(changed);
	}
	public static Boolean isChangedTheSleepingReqNumber() {
		return storageChangedSleepingReqNumber.get();
	}

	// storageChangedFreq
	public static void setChangedFreq(Boolean changed) {
		storageChangedFreq.set(changed);
	}
	public static Boolean isChangedFreq() {
		return storageChangedFreq.get();
	}

	// AbstractStoredCache
	public static AbstractStoredCache getCache() {
		return storageCache.get();
	}
	public static void setCache(AbstractStoredCache cache) {
		storageCache.set(cache);
	}
	
	// storageBackendServiceConfigDAOInstance
	public static IBackendServiceConfigDAO getBackendServiceConfigDAOInstance() {
		return storageBackendServiceConfigDAOInstance.get();
	}
	public static void setBackendServiceConfigDAOInstance(IBackendServiceConfigDAO instance) {
		storageBackendServiceConfigDAOInstance.set(instance);
	}

	// storageUsageDAOInstance
	public static IUsageDAO getUsageDAOInstance() {
		return storageUsageDAOInstance.get();
	}
	public static void setUsageDAOInstance(IUsageDAO instance) {
		storageUsageDAOInstance.set(instance);
	}
	
	// storageWaitingReqDAOInstance
	public static IWaitingReqDAO getWaitingReqDAOInstance() {
		return storageWaitingReqDAOInstance.get();
	}
	public static void setWaitingReqDAOInstance(IWaitingReqDAO instance) {
		storageWaitingReqDAOInstance.set(instance);
	}
	
	// storageCacheInstance
	public static ICache getCacheInstance() {
		return storageCacheInstance.get();
	}
	public static void setCacheInstance(ICache instance) {
		storageCacheInstance.set(instance);
	}

	
	public static void removeCache() {
		if (storageCache.get() != null) {
			storageCache.get().shutdown();
		}
		storageCache.remove();
	}
	
	
	public static void removeAll() {
		storageChangedSleepingReqNumber.remove();
		storageAddedToWaitingReqList.remove();
		storageChangedFreq.remove();
		storageBackendServiceConfigDAOInstance.remove();
		storageUsageDAOInstance.remove();
		storageWaitingReqDAOInstance.remove();
		storageCacheInstance.remove();
		removeCache();
	}

	public static void init() {
		setChangedTheSleepingReqNumber(false);
		setAddedToWaitingReqList(false);
		setChangedFreq(false);
		storageBackendServiceConfigDAOInstance.set(null);
		storageCache.set(null);
		storageUsageDAOInstance.set(null);
		storageWaitingReqDAOInstance.set(null);
		storageCacheInstance.set(null);
	}
}