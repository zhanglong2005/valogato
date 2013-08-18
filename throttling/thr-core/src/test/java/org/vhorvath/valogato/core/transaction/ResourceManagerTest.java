package org.vhorvath.valogato.core.transaction;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.IBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.IWaitingReqDAO;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.WaitingReqDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.core.statistics.StatisticsStoreManager;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BackendServiceConfigDAOFactory.class, ThrottlingStorage.class, UsageDAOFactory.class, BackendServiceConfigDAOFactory.class,
	WaitingReqDAOFactory.class})
public class ResourceManagerTest {

	
	private ResourceManager instance = new ResourceManager();
	
	
	@Test
	public void testReleaseResources_releasingAll() throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		String waitingReqListMaxSize = "100";
		String maxNumberOfWaitingReqs = "1000";
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();

		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), waitingReqListMaxSize);
		featureBean.getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), maxNumberOfWaitingReqs);

		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		mockedStatisticsStoreManager.unregister(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedStatisticsStoreManager);

		// mocking ThrottlingStorage
		PowerMock.mockStatic(ThrottlingStorage.class);
		EasyMock.expect(ThrottlingStorage.isChangedTheSleepingReqNumber()).andReturn(true).times(2);
		EasyMock.expect(ThrottlingStorage.isAddedToWaitingReqList()).andReturn(true).times(2);
		EasyMock.expect(ThrottlingStorage.isChangedFreq()).andReturn(true).times(2);
		PowerMock.replay(ThrottlingStorage.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.decreaseNumberOfSleepingRequests(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(1);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking IBackendServiceConfigDAO
		IBackendServiceConfigDAO mockedBackendServiceConfigDAO = PowerMock.createMock(IBackendServiceConfigDAO.class);
		EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
		EasyMock.expect(mockedBackendServiceConfigDAO.getFeature(EasyMock.anyObject(BackendServiceBean.class),
				EasyMock.anyObject(String.class))).andReturn(featureBean).times(2);
		PowerMock.replay(mockedBackendServiceConfigDAO);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO).times(3);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking IWaitingReqDAO
		IWaitingReqDAO mockedWaitingReqDAO = EasyMock.createMock(IWaitingReqDAO.class);
		mockedWaitingReqDAO.unregisterRequest(requestId, Integer.valueOf(waitingReqListMaxSize), Integer.valueOf(maxNumberOfWaitingReqs));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedWaitingReqDAO);

		// mocking WaitingReqDAOFactory
		PowerMock.mockStatic(WaitingReqDAOFactory.class);
		EasyMock.expect(WaitingReqDAOFactory.getDAO()).andReturn(mockedWaitingReqDAO).times(1);
		PowerMock.replay(WaitingReqDAOFactory.class);
		
		instance.releaseResources(backendServiceName, requestId, mockedStatisticsStoreManager, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedStatisticsStoreManager);
		EasyMock.verify(mockedUsageDAO);
		EasyMock.verify(mockedWaitingReqDAO);
	}


	@Test
	public void testReleaseResources_WaitingReqNumber_notChanged() throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		String waitingReqListMaxSize = "100";
		String maxNumberOfWaitingReqs = "1000";
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();

		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), waitingReqListMaxSize);
		featureBean.getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), maxNumberOfWaitingReqs);

		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		mockedStatisticsStoreManager.unregister(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedStatisticsStoreManager);

		// mocking ThrottlingStorage
		PowerMock.mockStatic(ThrottlingStorage.class);
		EasyMock.expect(ThrottlingStorage.isChangedTheSleepingReqNumber()).andReturn(false).times(2);
		EasyMock.expect(ThrottlingStorage.isAddedToWaitingReqList()).andReturn(true).times(2);
		EasyMock.expect(ThrottlingStorage.isChangedFreq()).andReturn(true).times(2);
		PowerMock.replay(ThrottlingStorage.class);
		
		// mocking IBackendServiceConfigDAO
		IBackendServiceConfigDAO mockedBackendServiceConfigDAO = PowerMock.createMock(IBackendServiceConfigDAO.class);
		EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
		EasyMock.expect(mockedBackendServiceConfigDAO.getFeature(EasyMock.anyObject(BackendServiceBean.class),
				EasyMock.anyObject(String.class))).andReturn(featureBean).times(2);
		PowerMock.replay(mockedBackendServiceConfigDAO);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO).times(3);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking IWaitingReqDAO
		IWaitingReqDAO mockedWaitingReqDAO = EasyMock.createMock(IWaitingReqDAO.class);
		mockedWaitingReqDAO.unregisterRequest(requestId, Integer.valueOf(waitingReqListMaxSize), Integer.valueOf(maxNumberOfWaitingReqs));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedWaitingReqDAO);

		// mocking WaitingReqDAOFactory
		PowerMock.mockStatic(WaitingReqDAOFactory.class);
		EasyMock.expect(WaitingReqDAOFactory.getDAO()).andReturn(mockedWaitingReqDAO).times(1);
		PowerMock.replay(WaitingReqDAOFactory.class);
		
		instance.releaseResources(backendServiceName, requestId, mockedStatisticsStoreManager, simulatedServiceName);

		PowerMock.verifyAll();
		EasyMock.verify(mockedStatisticsStoreManager);
		EasyMock.verify(mockedWaitingReqDAO);
	}


	@Test
	public void testReleaseResources_nothingWasAddedToWaitingReqList() throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		String waitingReqListMaxSize = "100";
		String maxNumberOfWaitingReqs = "1000";
		
		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), waitingReqListMaxSize);
		featureBean.getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), maxNumberOfWaitingReqs);

		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		mockedStatisticsStoreManager.unregister(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedStatisticsStoreManager);

		// mocking ThrottlingStorage
		PowerMock.mockStatic(ThrottlingStorage.class);
		EasyMock.expect(ThrottlingStorage.isChangedTheSleepingReqNumber()).andReturn(true).times(2);
		EasyMock.expect(ThrottlingStorage.isAddedToWaitingReqList()).andReturn(false).times(2);
		EasyMock.expect(ThrottlingStorage.isChangedFreq()).andReturn(true).times(2);
		PowerMock.replay(ThrottlingStorage.class);
		
		// mocking IUsageDAO
		IUsageDAO mockedUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedUsageDAO.decreaseNumberOfSleepingRequests(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedUsageDAO);

		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedUsageDAO).times(1);
		PowerMock.replay(UsageDAOFactory.class);
		
		instance.releaseResources(backendServiceName, requestId, mockedStatisticsStoreManager, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedStatisticsStoreManager);
		EasyMock.verify(mockedUsageDAO);
	}


	@Test
	public void testReleaseResources_onlyStatisticsStoreManagerUnregister() throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		String backendServiceName = "backendService";
		
		String requestId = "requestId";
		
		String simulatedServiceName = null;
		
		String waitingReqListMaxSize = "100";
		String maxNumberOfWaitingReqs = "1000";
		
		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), waitingReqListMaxSize);
		featureBean.getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), maxNumberOfWaitingReqs);

		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		mockedStatisticsStoreManager.unregister(backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedStatisticsStoreManager);

		// mocking ThrottlingStorage
		PowerMock.mockStatic(ThrottlingStorage.class);
		EasyMock.expect(ThrottlingStorage.isChangedTheSleepingReqNumber()).andReturn(false).times(2);
		EasyMock.expect(ThrottlingStorage.isAddedToWaitingReqList()).andReturn(false).times(2);
		EasyMock.expect(ThrottlingStorage.isChangedFreq()).andReturn(true).times(2);
		PowerMock.replay(ThrottlingStorage.class);
		
		instance.releaseResources(backendServiceName, requestId, mockedStatisticsStoreManager, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedStatisticsStoreManager);
	}

}