package org.vhorvath.valogato.common.constants;

/**
 * @author Viktor Horvath
 */
public final class ThrConstants {

	public static final String THROTTLING_NAME = "Throttling-Core";
	public static final String THROTTLING_WEB_NAME = "Throttling-Web";
	
	public static final String PREFIX_CACHE_CONFIGURATION = "CACHE_CONF_BACKEND_SERV_";
	public static final String PREFIX_CACHE_FREQUENCY = "CACHE_FREQUENCY_";
	public static final String PREFIX_CACHE_NR_OF_SLEEPING_REQ = "CACHE_NR_OF_SLEEPING_REQ_";
	public static final String PREFIX_WAITING_REQ_LIST = "CACHE_WAITING_REQ_LIST_";
	
	public static final Integer DEFAULT_AVERAGE_RESPONSE_TIME = 15;
	public static final Integer MAX_WAITING_PERIOD = 20*60*1000;
	
	public static final String PATH_CONFIG_XML = "ConfigurationBackendservice.xml";
	public static final String CACHE_KEY_FOR_VALUE = "value";
	public static final String PATH_GENERAL_CONFIG_XML = "ConfigurationGeneral.xml";
	public static final String PATH_HAZELCAST_CLIENT_CONFIG_FILE = "hazelcast-client.properties";


	private ThrConstants() { }
	
	public enum Features {
		SendBackFaultFeature("SendBackFaultFeature"),
		WaitingFeature("WaitingFeature"),
		ForwarderFeature("ForwarderFeature");
		
		private String featureName;
		private Features(String featureName) { this.featureName = featureName; }
		public String toString() { return featureName; }
	}

	public enum FeatureParam {
		waitingReqListMaxSize("waitingReqListMaxSize", "Max. size of waiting req list", "200"),
		maxNumberOfWaitingReqs("maxNumberOfWaitingReqs", "Max. number of waiting reqs", "2000"),
		strategy("strategy", "Strategy", FeatureParamValue.maintiningFreeSlots.toString()),
		period("period", "Period (in millisec)", "1000"),
		endpoints("endpoints", "Endpoints", "");
		
		private String name; private String title; private String defValue;
		private FeatureParam(String name, String title, String defValue) { this.name=name; this.title=title; this.defValue=defValue; }
		public String toString() { return name; }
		public String getName() { return name; } public String getTitle() { return title; } public String getDefault() { return defValue; }
	}

	public enum FeatureParamValue {
		// for strategy
		fast("FAST"),
		maintiningFreeSlots("MAINTAINING_FREE_SLOTS"),
		registeringRequestsIndividually("REGISTERING_REQUESTS_INDIVIDUALLY");

		private String name;
		private FeatureParamValue(String name) { this.name = name; }
		public String toString() { return name; }
	}

	public enum OpType {
		REGISTER, UNREGISTER;
	}
	
	public enum Source {
		cache("CACHE"),
		dummy("DUMMY");

		private String name;
		private Source(String name) { this.name = name; }
		public String toString() { return name; }		
	}

	public enum CacheType {
		hazelcast("Hazelcast"),
		terracotta("Terracotta"),
		dummy("Dummy"),
		coherence("Coherence"),
		infinispan("Infinispan"), 
		memcached("Memcached"),
		localCache("LocalCache");

		private String name;
		private CacheType(String name) { this.name = name; }
		public String toString() { return name; }		
	}
}