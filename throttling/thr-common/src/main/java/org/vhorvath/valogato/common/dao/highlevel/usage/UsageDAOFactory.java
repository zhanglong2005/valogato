package org.vhorvath.valogato.common.dao.highlevel.usage;

import org.vhorvath.valogato.common.dao.highlevel.usage.impl.DummyUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.impl.MemoryUsageDAO;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;

/**
 * @author Viktor Horvath
 */
public final class UsageDAOFactory {

	private UsageDAOFactory() { }
	
	// the factory class will return only one instance PER THREAD! (and not per class...)
	public static synchronized IUsageDAO getDAO() throws ThrottlingConfigurationException {
		IUsageDAO result = ThrottlingStorage.getUsageDAOInstance();
		
		if (result == null) {
			String source = GeneralConfigurationUtils.getStatisticsSource();
			if (source.equals("CACHE")) {
				result = new MemoryUsageDAO();
			} else if (source.equals("DUMMY")) {
				result = new DummyUsageDAO();
			} else {
				throw new ThrottlingConfigurationException(String.format("Uknown statisticsSource value in the configfuration! source=%s", source));
			}
			ThrottlingStorage.setUsageDAOInstance(result);
		}
		
		return result;
	}
	
}
