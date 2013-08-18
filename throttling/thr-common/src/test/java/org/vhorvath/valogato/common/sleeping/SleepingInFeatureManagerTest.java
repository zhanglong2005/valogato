package org.vhorvath.valogato.common.sleeping;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheDAOFactory.class, UsageDAOFactory.class, Thread.class})
public class SleepingInFeatureManagerTest {

	
	private SleepingInFeatureManager instance = new SleepingInFeatureManager();
	
	@Test
	public void testWait_registeringRequestsIndividually() throws ThrottlingConfigurationException, ThrottlingRuntimeException, 
			InterruptedException {
		String backendServiceName = "backendService";
		
		String strategy = ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString();
		long period = 10;

		Map<String, String> params = new HashMap<String, String>();
		params.put(ThrConstants.FeatureParam.period.toString(), Long.toString(period));
		params.put(ThrConstants.FeatureParam.strategy.toString(), strategy);

		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(params);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall().times(2);
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall().times(2);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.increaseNumberOfSleepingRequests(backendServiceName);
		EasyMock.expectLastCall();
		mockedUsageDAO.decreaseNumberOfSleepingRequests(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(2);
		PowerMock.replay(UsageDAOFactory.class);

		// call the method
		instance.wait(backendServiceName, featureBean);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}
	
	
	@Test
	public void testWait_maintiningFreeSlots() throws ThrottlingConfigurationException, ThrottlingRuntimeException, InterruptedException {
		String backendServiceName = "backendService";
		
		String strategy = ThrConstants.FeatureParamValue.maintiningFreeSlots.toString();
		long period = 10;

		Map<String, String> params = new HashMap<String, String>();
		params.put(ThrConstants.FeatureParam.period.toString(), Long.toString(period));
		params.put(ThrConstants.FeatureParam.strategy.toString(), strategy);

		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(params);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall().times(2);
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall().times(2);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.increaseNumberOfSleepingRequests(backendServiceName);
		EasyMock.expectLastCall();
		mockedUsageDAO.decreaseNumberOfSleepingRequests(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(2);
		PowerMock.replay(UsageDAOFactory.class);

		// call the method
		instance.wait(backendServiceName, featureBean);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}
	

	@Test
	public void testWait_fast() throws ThrottlingConfigurationException, ThrottlingRuntimeException, InterruptedException {
		String backendServiceName = "backendService";
		
		String strategy = ThrConstants.FeatureParamValue.fast.toString();
		long period = 10;

		Map<String, String> params = new HashMap<String, String>();
		params.put(ThrConstants.FeatureParam.period.toString(), Long.toString(period));
		params.put(ThrConstants.FeatureParam.strategy.toString(), strategy);

		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(params);
		
		// call the method
		instance.wait(backendServiceName, featureBean);
		
	}


}
