package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice;


import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl.DummyBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl.MemoryBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;


/**
 * @author Viktor Horvath
 */
public final class BackendServiceConfigDAOFactory {

	private BackendServiceConfigDAOFactory() { }
	
	// the factory class will return only one instance PER THREAD! (and not per class...)
	public static synchronized IBackendServiceConfigDAO getDAO() throws ThrottlingConfigurationException {
		IBackendServiceConfigDAO result = ThrottlingStorage.getBackendServiceConfigDAOInstance();
		
		if (result == null) {
			String source = GeneralConfigurationUtils.getBackendserviceConfigSource();
			if (source.equals(ThrConstants.Source.cache.toString())) {
				result = new MemoryBackendServiceConfigDAO();
			} else if (source.equals(ThrConstants.Source.dummy.toString())) {
				result = new DummyBackendServiceConfigDAO();
			} else {
				throw new ThrottlingConfigurationException(String.format("Uknown backendserviceConfigSource value in the configfuration! source=%s", source));
			}
			ThrottlingStorage.setBackendServiceConfigDAOInstance(result);
		}
		
		return result;
	}
	
	
}
