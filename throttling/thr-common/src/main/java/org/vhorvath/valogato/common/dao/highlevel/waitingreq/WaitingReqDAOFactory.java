package org.vhorvath.valogato.common.dao.highlevel.waitingreq;

import org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl.DummyWaitingReqDAO;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl.MemoryWaitingReqDAO;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;

/**
 * @author Viktor Horvath
 */
public final class WaitingReqDAOFactory {

	private WaitingReqDAOFactory() { }
	
	// the factory class will return only one instance PER THREAD! (and not per class...)
	public synchronized static IWaitingReqDAO getDAO() throws ThrottlingConfigurationException {
		IWaitingReqDAO result = ThrottlingStorage.getWaitingReqDAOInstance();
		
		if (result == null) {
			String source = GeneralConfigurationUtils.getStatisticsSource();
			if (source.equals("CACHE")) {
				result = new MemoryWaitingReqDAO();
			} else if (source.equals("DUMMY")) {
				result = new DummyWaitingReqDAO();
			} else {
				throw new ThrottlingConfigurationException(String.format("Uknown statisticsSource value in the configfuration! source=%s", source));
			}
			ThrottlingStorage.setWaitingReqDAOInstance(result);
		}
		
		return result;
	}
	
}
