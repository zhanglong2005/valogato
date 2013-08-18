package org.vhorvath.valogato.common.utils;

import org.vhorvath.valogato.common.constants.ThrConstants;


/**
 * @author Viktor Horvath
 */
public final class ThrottlingUtils {

	private ThrottlingUtils() { }
	
	
	public static String commaSeparated(String... strings) {
		if (strings == null) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		for(String string : strings) {
			sb.append(string).append(",");
		}
		
		if (sb.length() > 0) {
			return sb.substring(0, sb.length()-1);
		} else {
			return sb.toString();
		}
	}
	

	public static String getWaitingReqListKey(int ind) {
		return ThrConstants.PREFIX_WAITING_REQ_LIST + ind;
	}

	public static String getWaitingReqFirstLastKey() {
		return ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST";
	}
	
	public static String getConfKey(String backendServiceName) {
		return ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName;
	}

	public static String getFreqKey(String nameBackendService) {
		return ThrConstants.PREFIX_CACHE_FREQUENCY + nameBackendService;
	}

	public static String getSleepingReqKey(String nameBackendService) {
		return ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService;
	}
}
