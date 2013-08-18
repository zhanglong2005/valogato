package org.vhorvath.valogato.core.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.IWaitingReqDAO;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.WaitingReqDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheDAOFactory.class, UsageDAOFactory.class, WaitingReqDAOFactory.class, GeneralConfigurationUtils.class})
public class StatisticsStoreManagerTest {

	
	private StatisticsStoreManager instance = null;
	
	
	@Before
	public void setUp() {
		instance = new StatisticsStoreManager();
		ThrottlingStorage.init();
	}

	
	/* ************************************** method manage ************************************** */
	@Test
	public void testManage_REGISTER_lockIsNeeded() throws Exception {
		String backendServiceName = "backendService";
		ThrConstants.OpType opType = ThrConstants.OpType.REGISTER;
		Integer numberOfServedReq = 10;
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.setFrequency(backendServiceName, numberOfServedReq+1);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO);
		PowerMock.replay(UsageDAOFactory.class);
		
		Whitebox.invokeMethod(instance, "manage", backendServiceName, opType, numberOfServedReq);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedUsageDAO);
	}

	
	@Test
	public void testManage_UNREGISTER_lockIsNeeded() throws Exception {
		String backendServiceName = "backendService";
		ThrConstants.OpType opType = ThrConstants.OpType.UNREGISTER;
		Integer numberOfServedReq = 10;
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.setFrequency(backendServiceName, numberOfServedReq-1);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO);
		PowerMock.replay(UsageDAOFactory.class);
		
		Whitebox.invokeMethod(instance, "manage", backendServiceName, opType, numberOfServedReq);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedUsageDAO);
	}


	@Test
	public void testManage_REGISTER_lockedIsNotNeeded() throws Exception {
		String backendServiceName = "backendService";
		ThrConstants.OpType opType = ThrConstants.OpType.REGISTER;
		Integer numberOfServedReq = 10;
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.setFrequency(backendServiceName, numberOfServedReq+1);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO);
		PowerMock.replay(UsageDAOFactory.class);
		
		Whitebox.invokeMethod(instance, "manage", backendServiceName, opType, numberOfServedReq);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedUsageDAO);
	}
	
	
	/* ************************************** method getFreeSlots ************************************** */
	@Test
	public void testGetFreeSlots_notAfterSleeping() throws Exception {
		Integer maxLoading = 100;
		Integer numberOfServedReq = 20;
		Integer numberOfWaitingRequests = 5;
		boolean afterSleeping = false;
		
		Integer freeslots = Whitebox.<Integer>invokeMethod(instance, "getFreeSlots", maxLoading, numberOfServedReq, numberOfWaitingRequests, afterSleeping);
		
		assertEquals(75, freeslots.intValue());
	}


	@Test
	public void testGetFreeSlots_afterSleeping() throws Exception {
		Integer maxLoading = 100;
		Integer numberOfServedReq = 20;
		Integer numberOfWaitingRequests = 5;
		boolean afterSleeping = true;
		
		Integer freeslots = Whitebox.<Integer>invokeMethod(instance, "getFreeSlots", maxLoading, numberOfServedReq, numberOfWaitingRequests, afterSleeping);
		
		assertEquals(80, freeslots.intValue());
	}
	

	/* ************************************** method canBackendBeCalled ************************************** */
	@Test
	// maxLoading = 100, frequency = 12, waitingRequests = 11, afterSleeping = false
	// isRequestNextWaiting is not called
	public void testCanBackendBeCalled_can_withoutIsRequestNextWaiting() throws ThrottlingConfigurationException {
		int maxLoading = 100;
		int frequency = 12;
		int waitingRequests = 11;
		
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setMaxLoading(maxLoading);
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		BackendServiceFreqBean beanFreq = new BackendServiceFreqBean();
		beanFreq.setFrequency(frequency);
		BackendServiceSleepingReqBean beanWaitingReq = new BackendServiceSleepingReqBean();
		beanWaitingReq.setNumberOfSleepingRequests(waitingRequests);
		
		boolean afterSleeping = false;
		
		ThrottlingStorage.setAddedToWaitingReqList(false);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		EasyMock.expect(mockedUsageDAO.getFreqOfBackendService(backendServiceName)).andReturn(beanFreq);
		EasyMock.expect(mockedUsageDAO.getSleepingReqOfBackendService(backendServiceName)).andReturn(beanWaitingReq);
		mockedUsageDAO.setFrequency(backendServiceName, frequency+1);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);
		
		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(3);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking GeneralConfigurationUtils
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getBackendserviceConfigSource()).andReturn(ThrConstants.Source.cache.toString());
		PowerMock.replay(GeneralConfigurationUtils.class);

		boolean can = instance.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
		
		assertTrue(can);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}


	@Test
	// maxLoading = 100, frequency = null, waitingRequests = 11, afterSleeping = false
	// isRequestNextWaiting is not called
	public void testCanBackendBeCalled_can_withoutIsRequestNextWaiting_numberOfServedReqIsNull() throws ThrottlingConfigurationException {
		int maxLoading = 100;
		int waitingRequests = 11;
		
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setMaxLoading(maxLoading);
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		BackendServiceFreqBean beanFreq = new BackendServiceFreqBean();
		beanFreq.setFrequency(null);
		BackendServiceSleepingReqBean beanWaitingReq = new BackendServiceSleepingReqBean();
		beanWaitingReq.setNumberOfSleepingRequests(waitingRequests);
		
		boolean afterSleeping = false;
		
		ThrottlingStorage.setAddedToWaitingReqList(false);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		EasyMock.expect(mockedUsageDAO.getFreqOfBackendService(backendServiceName)).andReturn(beanFreq);
		EasyMock.expect(mockedUsageDAO.getSleepingReqOfBackendService(backendServiceName)).andReturn(beanWaitingReq);
		mockedUsageDAO.setFrequency(backendServiceName, 1);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);
		
		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(3);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking GeneralConfigurationUtils
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getBackendserviceConfigSource()).andReturn(ThrConstants.Source.cache.toString());
		PowerMock.replay(GeneralConfigurationUtils.class);		
		
		boolean can = instance.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
		
		assertTrue(can);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}

	@Test
	// maxLoading = 0, frequency = null, waitingRequests = 11, afterSleeping = false
	// isRequestNextWaiting is not called
	public void testCanBackendBeCalled_notCan_withoutIsRequestNextWaiting_numberOfServedReqIsNull_maxLoadingIsZero() throws ThrottlingConfigurationException {
		int maxLoading = 0;
		int waitingRequests = 11;
		
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setMaxLoading(maxLoading);
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		BackendServiceFreqBean beanFreq = new BackendServiceFreqBean();
		beanFreq.setFrequency(null);
		BackendServiceSleepingReqBean beanWaitingReq = new BackendServiceSleepingReqBean();
		beanWaitingReq.setNumberOfSleepingRequests(waitingRequests);
		
		boolean afterSleeping = false;
		
		ThrottlingStorage.setAddedToWaitingReqList(false);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		EasyMock.expect(mockedUsageDAO.getFreqOfBackendService(backendServiceName)).andReturn(beanFreq);
		EasyMock.expect(mockedUsageDAO.getSleepingReqOfBackendService(backendServiceName)).andReturn(beanWaitingReq);
		EasyMock.replay(mockedUsageDAO);
		
		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(2);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking GeneralConfigurationUtils
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getBackendserviceConfigSource()).andReturn(ThrConstants.Source.cache.toString());
		PowerMock.replay(GeneralConfigurationUtils.class);		

		boolean can = instance.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
		
		assertFalse(can);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}


	@Test
	// maxLoading = 100, frequency = 82, waitingRequests = 21, afterSleeping = false
	// isRequestNextWaiting is not called
	public void testCanBackendBeCalled_notCan_withoutIsRequestNextWaiting_noFreeSlots() throws ThrottlingConfigurationException {
		int maxLoading = 100;
		int frequency = 82;
		int waitingRequests = 21;
		
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setMaxLoading(maxLoading);
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());

		BackendServiceFreqBean beanFreq = new BackendServiceFreqBean();
		beanFreq.setFrequency(frequency);
		BackendServiceSleepingReqBean beanWaitingReq = new BackendServiceSleepingReqBean();
		beanWaitingReq.setNumberOfSleepingRequests(waitingRequests);
		
		boolean afterSleeping = false;
		
		ThrottlingStorage.setAddedToWaitingReqList(false);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		EasyMock.expect(mockedUsageDAO.getFreqOfBackendService(backendServiceName)).andReturn(beanFreq);
		EasyMock.expect(mockedUsageDAO.getSleepingReqOfBackendService(backendServiceName)).andReturn(beanWaitingReq);
		EasyMock.replay(mockedUsageDAO);
		
		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(2);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking GeneralConfigurationUtils
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getBackendserviceConfigSource()).andReturn(ThrConstants.Source.cache.toString());
		PowerMock.replay(GeneralConfigurationUtils.class);		

		boolean can = instance.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
		
		assertFalse(can);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}


	@Test
	// maxLoading = 100, frequency = 82, waitingRequests = 21, afterSleeping = true
	// isRequestNextWaiting is called
	public void testCanBackendBeCalled_can_withIsRequestNextWaiting_afterSleepingIsTrue() throws ThrottlingConfigurationException {
		int maxLoading = 100;
		int frequency = 82;
		int waitingRequests = 21;
		
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		String waitingReqListMaxSize = "100";
		
		String maxNumberOfWaitingReqs = "1000";
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setMaxLoading(maxLoading);
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), 
				waitingReqListMaxSize);
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), 
				maxNumberOfWaitingReqs);

		BackendServiceFreqBean beanFreq = new BackendServiceFreqBean();
		beanFreq.setFrequency(frequency);
		BackendServiceSleepingReqBean beanWaitingReq = new BackendServiceSleepingReqBean();
		beanWaitingReq.setNumberOfSleepingRequests(waitingRequests);
		
		boolean afterSleeping = true;
		
		ThrottlingStorage.setAddedToWaitingReqList(true);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		EasyMock.expect(mockedUsageDAO.getFreqOfBackendService(backendServiceName)).andReturn(beanFreq);
		EasyMock.expect(mockedUsageDAO.getSleepingReqOfBackendService(backendServiceName)).andReturn(beanWaitingReq);
		mockedUsageDAO.setFrequency(backendServiceName, frequency+1);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);
		
		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(3);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking IWaitingReqDAO
		IWaitingReqDAO mockedWaitingReqDAO = EasyMock.createMock(IWaitingReqDAO.class);
		EasyMock.expect(mockedWaitingReqDAO.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName)).andReturn(true);
		mockedWaitingReqDAO.unregisterRequest(requestId, Integer.parseInt(waitingReqListMaxSize), Integer.parseInt(maxNumberOfWaitingReqs));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedWaitingReqDAO);

		// mocking WaitingReqDAOFactory
		PowerMock.mockStatic(WaitingReqDAOFactory.class);
		EasyMock.expect(WaitingReqDAOFactory.getDAO()).andReturn(mockedWaitingReqDAO).times(2);
		PowerMock.replay(WaitingReqDAOFactory.class);
		
		// mocking GeneralConfigurationUtils
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getBackendserviceConfigSource()).andReturn(ThrConstants.Source.cache.toString()).times(1);
		PowerMock.replay(GeneralConfigurationUtils.class);		

		boolean can = instance.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
		
		assertTrue(can);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
		EasyMock.verify(mockedWaitingReqDAO);
	}


	@Test
	// maxLoading = 100, frequency = 82, waitingRequests = 21, afterSleeping = false
	// isRequestNextWaiting is not called
	public void testCanBackendBeCalled_can_withIsRequestNextWaiting_afterSleepingIsFalse() throws ThrottlingConfigurationException {
		int maxLoading = 100;
		int frequency = 81;
		int waitingRequests = 18;
		
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setMaxLoading(maxLoading);
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());

		BackendServiceFreqBean beanFreq = new BackendServiceFreqBean();
		beanFreq.setFrequency(frequency);
		BackendServiceSleepingReqBean beanWaitingReq = new BackendServiceSleepingReqBean();
		beanWaitingReq.setNumberOfSleepingRequests(waitingRequests);
		
		boolean afterSleeping = false;
		
		ThrottlingStorage.setAddedToWaitingReqList(false);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.lock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getFreqKey(backendServiceName));
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrottlingUtils.getSleepingReqKey(backendServiceName));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		EasyMock.expect(mockedUsageDAO.getFreqOfBackendService(backendServiceName)).andReturn(beanFreq);
		EasyMock.expect(mockedUsageDAO.getSleepingReqOfBackendService(backendServiceName)).andReturn(beanWaitingReq);
		mockedUsageDAO.setFrequency(backendServiceName, frequency+1);
		EasyMock.replay(mockedUsageDAO);
		
		// UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(3);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking GeneralConfigurationUtils
		PowerMock.mockStatic(GeneralConfigurationUtils.class);
		EasyMock.expect(GeneralConfigurationUtils.getBackendserviceConfigSource()).andReturn(ThrConstants.Source.cache.toString()).times(1);
		PowerMock.replay(GeneralConfigurationUtils.class);		

		boolean can = instance.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, afterSleeping);
		
		assertTrue(can);
		assertFalse(ThrottlingStorage.isAddedToWaitingReqList());
		assertTrue(ThrottlingStorage.isChangedFreq());
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		EasyMock.verify(mockedUsageDAO);
	}

}