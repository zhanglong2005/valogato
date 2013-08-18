package org.vhorvath.valogato.common.dao.lowlevel.configuration.general;


import java.io.InputStream;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.vhorvath.valogato.common.beans.configuration.general.CacheBean;
import org.vhorvath.valogato.common.beans.configuration.general.GeneralConfigurationBean;
import org.vhorvath.valogato.common.beans.configuration.general.NewFeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;


/**
 * @author Viktor Horvath
 */
public final class GeneralConfigurationUtils {
	
	// TODO Adding a button to the web app to reload the general configuration
	private static GeneralConfigurationBean generalConfiguration = null;
	
	
	private GeneralConfigurationUtils() { }
	
	
//	public static NewFeatureBean getFeature(String nameFeature) throws ThrottlingConfigurationException {
//		checkArgument(nameFeature != null);
//
//		init();
//		for (NewFeatureBean newFeature : generalConfiguration.getNewFeature()) {
//			if (newFeature.getName().equals(nameFeature)) {
//				return newFeature;
//			}
//		}
//		throw new ThrottlingConfigurationException(String.format("The feature '%s' cannot be found in the general configuration XML file!",
//				nameFeature));
//	}


	public static List<NewFeatureBean> getFeatures() throws ThrottlingConfigurationException {
		init();
		
		return generalConfiguration.getNewFeature();
	}

	
	public static String getBackendserviceConfigSource() throws ThrottlingConfigurationException {
		init();

		String backendserviceConfigSource = generalConfiguration.getBackendserviceConfigSource();
		if (backendserviceConfigSource != null && backendserviceConfigSource.trim().length() > 0 && 
				(backendserviceConfigSource.equals(ThrConstants.Source.cache.toString()) || 
				 backendserviceConfigSource.equals(ThrConstants.Source.dummy.toString()))) {
			return backendserviceConfigSource;
		} else {
			throw new ThrottlingConfigurationException(String.format("The source of the backendservice config file (e.g. <backendserviceConfigSource>" +
					"DUMMY</backendserviceConfigSource>) contains an invalid value! invalid value:'%s'", backendserviceConfigSource));
		}	
	}
	
	
	public static String getStatisticsSource() throws ThrottlingConfigurationException {
		init();

		String statisticsSource = generalConfiguration.getStatisticsSource();
		if (statisticsSource != null && statisticsSource.trim().length() > 0 && 
				(statisticsSource.equals(ThrConstants.Source.cache.toString()) || 
				 statisticsSource.equals(ThrConstants.Source.dummy.toString()))) {
			return statisticsSource;
		} else {
			throw new ThrottlingConfigurationException(String.format("The source of the statisticsSource (e.g. <statisticsSource>DUMMY" +
					"</statisticsSource>) contains an invalid value! invalid value:'%s'", statisticsSource));
		}	
	}

	
	public static CacheBean getCache() throws ThrottlingConfigurationException {
		init();
		
		CacheBean cache = generalConfiguration.getCache();
		if (cache != null) {
			return cache;
		} else {
			throw new ThrottlingConfigurationException("The element cache (e.g. <cache type=\"...\">...</cache>) " +
					"cannot be found in the general configuration XML file!");
		}
	}

	
	private static synchronized void init() throws ThrottlingConfigurationException {
		if (generalConfiguration == null) {
			Serializer serializer = new Persister();
	
			try {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ThrConstants.PATH_GENERAL_CONFIG_XML);
				if (in == null) {
					throw new ThrottlingConfigurationException(String.format("The general configuration XML cannot be read! path=%s",
							ThrConstants.PATH_GENERAL_CONFIG_XML));					
				}
				generalConfiguration = serializer.read(GeneralConfigurationBean.class, in);
			} catch(ThrottlingConfigurationException tce) {
				throw tce;
			} catch(Exception e) {
				throw new ThrottlingConfigurationException("The general configuration XML is incorrect!", e);
			}
		}
	}
	

	static void setGeneralConfiguration(GeneralConfigurationBean generalConfiguration) {
		GeneralConfigurationUtils.generalConfiguration = generalConfiguration;
	}
	
}