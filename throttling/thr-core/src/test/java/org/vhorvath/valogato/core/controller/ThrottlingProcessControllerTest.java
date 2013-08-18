package org.vhorvath.valogato.core.controller;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.IBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl.MemoryBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.IWaitingReqDAO;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.WaitingReqDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;
import org.vhorvath.valogato.core.statistics.StatisticsStoreManager;
import org.vhorvath.valogato.core.transaction.ResourceManager;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BackendServiceConfigDAOFactory.class, WaitingReqDAOFactory.class, ThrottlingStorage.class, MDC.class,
	ThrottlingProcessController.class, UUID.class})
@SuppressStaticInitializationFor({"org.slf4j.MDC"})
public class ThrottlingProcessControllerTest {

	
	private IThrottlingController<String, String, RuntimeException> instance = null;
	private MDCAdapter mockedMdcAdapter = null;
	
	
	@Before
	public void setUp() {
		// mocking MDCAdapter
		mockedMdcAdapter = EasyMock.createMock(MDCAdapter.class);
		mockedMdcAdapter.put(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class));
		EasyMock.replay(mockedMdcAdapter);
		Whitebox.setInternalState(MDC.class, "mdcAdapter", mockedMdcAdapter);

		instance = new ThrottlingProcessController<String, String, RuntimeException>();
	}
	
	
	@Test
	// strategy = registeringRequestsIndividually, the request will be registered
	public void testRegisterRequestIdInWaitingReqList_wasAbleToAdd() throws Exception {
		// initializing parameters
		Integer waitingReqListMaxSize = 100;
		Integer maxNumberOfWaitingReqs = 200;

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(),
				waitingReqListMaxSize.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(),
				maxNumberOfWaitingReqs.toString());
		
		String simulatedServiceName = null;
		
		boolean can = false;
		
		ISimulatedService<String, String, RuntimeException> simulatedInterface = null;
		
		String requestId = "requestId";
		
		Whitebox.setInternalState(instance, "requestId", requestId);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new MemoryBackendServiceConfigDAO()).times(3);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		// mocking IWaitingReqDAO
		IWaitingReqDAO mockedWaitingReqDAO = EasyMock.createMock(IWaitingReqDAO.class);
		EasyMock.expect(mockedWaitingReqDAO.registerRequest(requestId, backendServiceBean, simulatedServiceName, waitingReqListMaxSize, 
				maxNumberOfWaitingReqs)).andReturn(true);
		EasyMock.replay(mockedWaitingReqDAO);

		// mocking WaitingReqDAOFactory
		PowerMock.mockStatic(WaitingReqDAOFactory.class);
		EasyMock.expect(WaitingReqDAOFactory.getDAO()).andReturn(mockedWaitingReqDAO);
		PowerMock.replay(WaitingReqDAOFactory.class);

		Whitebox.invokeMethod(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, simulatedInterface);
		
		assertTrue(ThrottlingStorage.isAddedToWaitingReqList());
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedWaitingReqDAO);
	}

	
	@Test
	// strategy = registeringRequestsIndividually, the req won't be registered -> simulatedInterface.buildFault(...)
	public void testRegisterRequestIdInWaitingReqList_wasNotAbleToAdd() throws Exception {
		// initializing parameters
		Integer waitingReqListMaxSize = 100;
		Integer maxNumberOfWaitingReqs = 200;

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(),
				waitingReqListMaxSize.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(),
				maxNumberOfWaitingReqs.toString());
		
		String simulatedServiceName = null;
		
		boolean can = false;
		
		String exceptionText = String.format("WaitingFeature: The waiting requests have exceeded the limit (%s)!", 
				maxNumberOfWaitingReqs);

		String requestId = "requestId";
		
		Whitebox.setInternalState(instance, "requestId", requestId);
		
		// mocking ISimulatedService
		ISimulatedService<String, String, RuntimeException> simulatedInterface = EasyMock.createMock(ISimulatedService.class);
		EasyMock.expect(simulatedInterface.buildFault(exceptionText)).andReturn(new RuntimeException(exceptionText));
		EasyMock.replay(simulatedInterface);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new MemoryBackendServiceConfigDAO()).times(3);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		// mocking IWaitingReqDAO
		IWaitingReqDAO mockedWaitingReqDAO = EasyMock.createMock(IWaitingReqDAO.class);
		EasyMock.expect(mockedWaitingReqDAO.registerRequest(requestId, backendServiceBean, simulatedServiceName, waitingReqListMaxSize, 
				maxNumberOfWaitingReqs)).andReturn(false);
		EasyMock.replay(mockedWaitingReqDAO);

		// mocking WaitingReqDAOFactory
		PowerMock.mockStatic(WaitingReqDAOFactory.class);
		EasyMock.expect(WaitingReqDAOFactory.getDAO()).andReturn(mockedWaitingReqDAO);
		PowerMock.replay(WaitingReqDAOFactory.class);

		try {
			Whitebox.invokeMethod(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, simulatedInterface);
			fail("RuntimeException should have been thrown!");
		} catch (RuntimeException re) {
			assertEquals(exceptionText, re.getMessage());
		}
		
		assertFalse(ThrottlingStorage.isAddedToWaitingReqList());
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedWaitingReqDAO);
	}


	@Test
	public void testRegisterRequestIdInWaitingReqList_canIsTrue() throws Exception {
		Integer waitingReqListMaxSize = 100;
		Integer maxNumberOfWaitingReqs = 200;

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(),
				waitingReqListMaxSize.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(),
				maxNumberOfWaitingReqs.toString());
		
		String simulatedServiceName = null;
		
		boolean can = true;
		
		Whitebox.invokeMethod(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, null);
		
		assertFalse(ThrottlingStorage.isAddedToWaitingReqList());
		
		PowerMock.verifyAll();
	}


	@Test
	public void testRegisterRequestIdInWaitingReqList_differentStrategy() throws Exception {
		Integer waitingReqListMaxSize = 100;
		Integer maxNumberOfWaitingReqs = 200;

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setParams(new HashMap<String, String>());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.strategy.toString(), 
				ThrConstants.FeatureParamValue.maintiningFreeSlots.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(),
				waitingReqListMaxSize.toString());
		backendServiceBean.getFeature().getParams().put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(),
				maxNumberOfWaitingReqs.toString());
		
		String simulatedServiceName = null;
		
		boolean can = false;
		
		ISimulatedService<String, String, RuntimeException> simulatedInterface = null;
		
		String requestId = "requestId";
		
		Whitebox.setInternalState(instance, "requestId", requestId);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new MemoryBackendServiceConfigDAO()).times(1);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		Whitebox.invokeMethod(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, simulatedInterface);
		
		assertFalse(ThrottlingStorage.isAddedToWaitingReqList());
		
		PowerMock.verifyAll();
	}

	
	@Test
	public void testProcessRequest() throws Exception {
		// initializing parameters
		String req = "req";
		
		ISimulatedService<String, String, RuntimeException> simulatedInterface = null;
		
		String backendServiceName = "backendService";
		
		String simulatedServiceName = null;
		
		String requestId = "requestId";
		
		// mocking ThrottlingProcessController.innerProcessRequest
		instance = PowerMock.createPartialMock(ThrottlingProcessController.class, "innerProcessRequest");
		PowerMock.expectPrivate(instance, "innerProcessRequest", req, simulatedInterface, backendServiceName, simulatedServiceName).andReturn(null);
		PowerMock.replay(instance);

		Whitebox.setInternalState(instance, "requestId", requestId);
		
		// mocking logger
		Logger mockedLogger = EasyMock.createMock(Logger.class);
		mockedLogger.error(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.info(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(mockedLogger);
		Whitebox.setInternalState(ThrottlingProcessController.class, "LOGGER", mockedLogger);
		
		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		EasyMock.replay(mockedStatisticsStoreManager);
		Whitebox.setInternalState(instance, "statisticsStoreManager", mockedStatisticsStoreManager);

		// mocking creating new instance of ResourceManager
		ResourceManager mockedResourceManager = EasyMock.createMock(ResourceManager.class);
		mockedResourceManager.releaseResources(backendServiceName, requestId, mockedStatisticsStoreManager, simulatedServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedResourceManager);
		Whitebox.setInternalState(instance, "resourceManager", mockedResourceManager);
		
		// mocking ThrottlingStorage
		PowerMock.mockStatic(ThrottlingStorage.class);
		ThrottlingStorage.removeAll();
		EasyMock.expectLastCall();
		PowerMock.replay(ThrottlingStorage.class);
		
		// mocking MDC
		PowerMock.mockStatic(MDC.class);
		MDC.remove("reqId");
		EasyMock.expectLastCall();
		PowerMock.replay(MDC.class);
		
		instance.processRequest(req, simulatedInterface, backendServiceName, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(instance);
		EasyMock.verify(mockedResourceManager);
		EasyMock.verify(mockedStatisticsStoreManager);
	}
	
	
	@Test
	// can = true, afterSleeping = false -> normal processing, backend will be called
	public void testInnerProcessRequest_canIsTrue() throws Exception {
		// initializing parameters
		String req = "req";
		
        String backendServiceName = "backendService";
        
        String simulatedServiceName = null;
        
        BackendServiceBean backendServiceBean = new BackendServiceBean();
		
		String requestId = "requestId";

		Boolean afterSleeping = false;

		Boolean can = true;

		Whitebox.setInternalState(instance, "requestId", requestId);
		Whitebox.setInternalState(instance, "afterSleeping", afterSleeping);
		
		// mocking ISimulatedService
        ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
        EasyMock.expect(mockedSimulatedInterface.forwardRequest(req)).andReturn(null);
        EasyMock.replay(mockedSimulatedInterface);
        
        // mocking IBackendServiceConfigDAO
        IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
        EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
        EasyMock.replay(mockedBackendServiceConfigDAO);

        // mocking BackendServiceConfigDAOFactory
        PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO);
        PowerMock.replay(BackendServiceConfigDAOFactory.class);
        
		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		EasyMock.expect(mockedStatisticsStoreManager.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, 
				afterSleeping)).andReturn(can);
		EasyMock.replay(mockedStatisticsStoreManager);
		Whitebox.setInternalState(instance, "statisticsStoreManager", mockedStatisticsStoreManager);
        
		// mocking Logger
		Logger mockedLogger = EasyMock.createMock(Logger.class);
		mockedLogger.error(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.info(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(mockedLogger);
		Whitebox.setInternalState(ThrottlingProcessController.class, "LOGGER", mockedLogger);
		
		Whitebox.invokeMethod(instance, "innerProcessRequest", req, mockedSimulatedInterface, backendServiceName, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedSimulatedInterface);
		EasyMock.verify(mockedStatisticsStoreManager);
//		EasyMock.verify(mockedLogger);
	}
	

	@Test
	// can = false, afterSleeping = true -> applying a feature
	public void testInnerProcessRequest_canIsFalse() throws Exception {
		// initializing parameters
		String req = "req";
		
        String simulatedServiceName = null;
        
        BackendServiceBean backendServiceBean = new BackendServiceBean();

		Boolean can = false;

        String backendServiceName = "backendService";
        
		String requestId = "requestId";

		Boolean afterSleeping = true;

        // mocking ISimulatedService
		ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
        EasyMock.replay(mockedSimulatedInterface);

        // mocking ThrottlingProcessController
        instance = PowerMock.createPartialMock(ThrottlingProcessController.class, "registerRequestIdInWaitingReqList");
		PowerMock.expectPrivate(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, 
				mockedSimulatedInterface);
		PowerMock.replay(instance);

		Whitebox.setInternalState(instance, "requestId", requestId);
		Whitebox.setInternalState(instance, "afterSleeping", afterSleeping);
		
        // mocking IBackendServiceConfigDAO
        IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
        EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
        EasyMock.replay(mockedBackendServiceConfigDAO);

        // mocking BackendServiceConfigDAOFactory
        PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO);
        PowerMock.replay(BackendServiceConfigDAOFactory.class);
        
		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		EasyMock.expect(mockedStatisticsStoreManager.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, 
				afterSleeping)).andReturn(can);
		EasyMock.replay(mockedStatisticsStoreManager);
		Whitebox.setInternalState(instance, "statisticsStoreManager", mockedStatisticsStoreManager);
        
//		// mocking Logger
//		Logger mockedLogger = EasyMock.createMock(Logger.class);
//		mockedLogger.error(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//		mockedLogger.debug(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//		mockedLogger.info(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//		EasyMock.replay(mockedLogger);
//		Whitebox.setInternalState(instance, "logger", mockedLogger);

		// mocking FeatureExecutor
		FeatureExecutor<String, String, RuntimeException> mockedFeatureExecutor = EasyMock.createMock(FeatureExecutor.class);
		EasyMock.expect(mockedFeatureExecutor.applyFeature(req, backendServiceName, mockedSimulatedInterface, simulatedServiceName, 
				backendServiceBean, instance)).andReturn(null);
		EasyMock.replay(mockedFeatureExecutor);
		PowerMock.expectNew(FeatureExecutor.class).andReturn(mockedFeatureExecutor);
		PowerMock.replay(FeatureExecutor.class);
		
		Whitebox.invokeMethod(instance, "innerProcessRequest", req, mockedSimulatedInterface, backendServiceName, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedSimulatedInterface);
		EasyMock.verify(mockedStatisticsStoreManager);
//		EasyMock.verify(mockedLogger);
		EasyMock.verify(mockedFeatureExecutor);
	}


	@Test
	// exception in getBackendService -> calling backend diretcly
	public void testInnerProcessRequest_exceptionAtGetBackendService() throws Exception {
		// initializing parameters
		String req = "req";
		
        String simulatedServiceName = null;
        
        String backendServiceName = "backendService";
        
		String requestId = "requestId";

		Boolean afterSleeping = true;

		Whitebox.setInternalState(instance, "requestId", requestId);
		Whitebox.setInternalState(instance, "afterSleeping", afterSleeping);
		
        // mocking ISimulatedService
		ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
        EasyMock.expect(mockedSimulatedInterface.forwardRequest(req)).andReturn(null);
        EasyMock.replay(mockedSimulatedInterface);

        // mocking IBackendServiceConfigDAO
        IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
        EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andThrow(new RuntimeException("1"));
        EasyMock.replay(mockedBackendServiceConfigDAO);

        // mocking BackendServiceConfigDAOFactory
        PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO);
        PowerMock.replay(BackendServiceConfigDAOFactory.class);
        
		// mocking Logger
		Logger mockedLogger = EasyMock.createMock(Logger.class);
		mockedLogger.error(EasyMock.anyObject(String.class), EasyMock.anyObject(Throwable.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.info(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(mockedLogger);
		Whitebox.setInternalState(ThrottlingProcessController.class, "LOGGER", mockedLogger);
		
		Whitebox.invokeMethod(instance, "innerProcessRequest", req, mockedSimulatedInterface, backendServiceName, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedSimulatedInterface);
//		EasyMock.verify(mockedLogger);
	}

	
	@Test
	// exception in canBackendBeCalled -> calling backend diretcly
	public void testInnerProcessRequest_exceptionAtCanBackendBeCalled() throws Exception {
		// initializing parameters
		String req = "req";
		
        String simulatedServiceName = null;
        
        BackendServiceBean backendServiceBean = new BackendServiceBean();

        String backendServiceName = "backendService";
        
		String requestId = "requestId";

		Boolean afterSleeping = true;

		Whitebox.setInternalState(instance, "requestId", requestId);
		Whitebox.setInternalState(instance, "afterSleeping", afterSleeping);
		
		// mocking ISimulatedService
		ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
        EasyMock.expect(mockedSimulatedInterface.forwardRequest(req)).andReturn(null);
        EasyMock.replay(mockedSimulatedInterface);

        // mocking IBackendServiceConfigDAO
        IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
        EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
        EasyMock.replay(mockedBackendServiceConfigDAO);

        // mocking BackendServiceConfigDAOFactory
        PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO);
        PowerMock.replay(BackendServiceConfigDAOFactory.class);
        
		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		EasyMock.expect(mockedStatisticsStoreManager.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, 
				afterSleeping)).andThrow(new RuntimeException("1"));
		EasyMock.replay(mockedStatisticsStoreManager);
		Whitebox.setInternalState(instance, "statisticsStoreManager", mockedStatisticsStoreManager);
        
		// mocking Logger
		Logger mockedLogger = EasyMock.createMock(Logger.class);
		mockedLogger.error(EasyMock.anyObject(String.class), EasyMock.anyObject(Throwable.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.info(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(mockedLogger);
		Whitebox.setInternalState(ThrottlingProcessController.class, "LOGGER", mockedLogger);
		
		Whitebox.invokeMethod(instance, "innerProcessRequest", req, mockedSimulatedInterface, backendServiceName, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedSimulatedInterface);
		EasyMock.verify(mockedStatisticsStoreManager);
//		EasyMock.verify(mockedLogger);
	}

	
	@Test
	// ThrottlingRuntimeException in applyFeature -> calling backend diretcly
	public void testInnerProcessRequest_exceptionAtApplyFeature() throws Exception {
		// initializing parameters
		String req = "req";
		
        String simulatedServiceName = null;
        
        BackendServiceBean backendServiceBean = new BackendServiceBean();

		Boolean can = false;

        String backendServiceName = "backendService";
        
		String requestId = "requestId";

		Boolean afterSleeping = true;

		// mocking ISimulatedService
        ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
        EasyMock.expect(mockedSimulatedInterface.forwardRequest(req)).andReturn(null);
        EasyMock.replay(mockedSimulatedInterface);

		// mocking ThrottlingProcessController
        instance = PowerMock.createPartialMock(ThrottlingProcessController.class, "registerRequestIdInWaitingReqList");
		PowerMock.expectPrivate(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, 
				mockedSimulatedInterface);
		PowerMock.replay(instance);

		Whitebox.setInternalState(instance, "requestId", requestId);
		Whitebox.setInternalState(instance, "afterSleeping", afterSleeping);
		
        // mocking IBackendServiceConfigDAO
        IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
        EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
        EasyMock.replay(mockedBackendServiceConfigDAO);

        // mocking BackendServiceConfigDAOFactory
        PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO);
        PowerMock.replay(BackendServiceConfigDAOFactory.class);
        
		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		EasyMock.expect(mockedStatisticsStoreManager.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, 
				afterSleeping)).andReturn(can);
		EasyMock.replay(mockedStatisticsStoreManager);
		Whitebox.setInternalState(instance, "statisticsStoreManager", mockedStatisticsStoreManager);
        
		// mocking Logger
		Logger mockedLogger = EasyMock.createMock(Logger.class);
		mockedLogger.error(EasyMock.anyObject(String.class), EasyMock.anyObject(Throwable.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.debug(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		mockedLogger.info(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(mockedLogger);
		Whitebox.setInternalState(ThrottlingProcessController.class, "LOGGER", mockedLogger);
		
		// mocking FeatureExecutor
		FeatureExecutor<String, String, RuntimeException> mockedFeatureExecutor = EasyMock.createMock(FeatureExecutor.class);
		EasyMock.expect(mockedFeatureExecutor.applyFeature(req, backendServiceName, mockedSimulatedInterface, simulatedServiceName, 
				backendServiceBean, instance)).andThrow(new ThrottlingRuntimeException("1"));
		EasyMock.replay(mockedFeatureExecutor);
		PowerMock.expectNew(FeatureExecutor.class).andReturn(mockedFeatureExecutor);
		PowerMock.replay(FeatureExecutor.class);
		
		Whitebox.invokeMethod(instance, "innerProcessRequest", req, mockedSimulatedInterface, backendServiceName, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedSimulatedInterface);
		EasyMock.verify(mockedStatisticsStoreManager);
//		EasyMock.verify(mockedLogger);
		EasyMock.verify(mockedFeatureExecutor);
	}
	

	@Test
	// RuntimeException (not ThrottlingConfigurationException or ThrottlingRuntimeException!) in applyFeature -> throwing the exception
	public void testInnerProcessRequest_notExpectedExceptionAtApplyFeature() throws Exception {
		// initializing parameters
		String req = "req";
		
        String simulatedServiceName = null;
        
        BackendServiceBean backendServiceBean = new BackendServiceBean();

		Boolean can = false;

        String backendServiceName = "backendService";
        
		String requestId = "requestId";

		Boolean afterSleeping = true;

		// mocking ISimulatedService
        ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
        EasyMock.replay(mockedSimulatedInterface);

		// mocking ThrottlingProcessController
        instance = PowerMock.createPartialMock(ThrottlingProcessController.class, "registerRequestIdInWaitingReqList");
		PowerMock.expectPrivate(instance, "registerRequestIdInWaitingReqList", backendServiceBean, simulatedServiceName, can, 
				mockedSimulatedInterface);
		PowerMock.replay(instance);

		Whitebox.setInternalState(instance, "requestId", requestId);
		Whitebox.setInternalState(instance, "afterSleeping", afterSleeping);
		
        // mocking IBackendServiceConfigDAO
        IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
        EasyMock.expect(mockedBackendServiceConfigDAO.getBackendService(backendServiceName)).andReturn(backendServiceBean);
        EasyMock.replay(mockedBackendServiceConfigDAO);

        // mocking BackendServiceConfigDAOFactory
        PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO);
        PowerMock.replay(BackendServiceConfigDAOFactory.class);
        
		// mocking StatisticsStoreManager
		StatisticsStoreManager mockedStatisticsStoreManager = EasyMock.createMock(StatisticsStoreManager.class);
		EasyMock.expect(mockedStatisticsStoreManager.canBackendBeCalled(backendServiceName, requestId, simulatedServiceName, backendServiceBean, 
				afterSleeping)).andReturn(can);
		EasyMock.replay(mockedStatisticsStoreManager);
		Whitebox.setInternalState(instance, "statisticsStoreManager", mockedStatisticsStoreManager);
        
//		// mocking Logger
//		Logger mockedLogger = EasyMock.createMock(Logger.class);
//		mockedLogger.error(EasyMock.anyObject(String.class), EasyMock.anyObject(Throwable.class));
//		EasyMock.expectLastCall().anyTimes();
//		mockedLogger.debug(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//		mockedLogger.info(EasyMock.anyObject(String.class));
//		EasyMock.expectLastCall().anyTimes();
//		EasyMock.replay(mockedLogger);
//		Whitebox.setInternalState(instance, "logger", mockedLogger);

		// mocking FeatureExecutor
		FeatureExecutor<String, String, RuntimeException> mockedFeatureExecutor = EasyMock.createMock(FeatureExecutor.class);
		EasyMock.expect(mockedFeatureExecutor.applyFeature(req, backendServiceName, mockedSimulatedInterface, simulatedServiceName, 
				backendServiceBean, instance)).andThrow(new RuntimeException("1"));
		EasyMock.replay(mockedFeatureExecutor);
		PowerMock.expectNew(FeatureExecutor.class).andReturn(mockedFeatureExecutor);
		PowerMock.replay(FeatureExecutor.class);
		
		try {
			Whitebox.invokeMethod(instance, "innerProcessRequest", req, mockedSimulatedInterface, backendServiceName, simulatedServiceName);
			fail("RuntimeException should have been thrown");
		} catch(RuntimeException re) {
			assertEquals("1", re.getMessage());
		}
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedSimulatedInterface);
		EasyMock.verify(mockedStatisticsStoreManager);
//		EasyMock.verify(mockedLogger);
		EasyMock.verify(mockedFeatureExecutor);
	}

	
//	@Test
//	public void testCreateUniqueReqId() throws Exception {
//		String machineName = "localhost";
//		Long nanoTime = 8734654937649l;
//		String textUUID = "12-3-456-";
//
//		// moking InetAddress
//		InetAddress mockedInetAddress = EasyMock.createMock(InetAddress.class);
//		EasyMock.expect(mockedInetAddress.getHostName()).andReturn(machineName);
//		EasyMock.replay(mockedInetAddress);
//
//		// moking InetAddress.getLocalHost()
//		PowerMock.mockStaticPartial(InetAddress.class, "getLocalHost");
//		EasyMock.expect(InetAddress.getLocalHost()).andReturn(mockedInetAddress);
//		PowerMock.replay(InetAddress.class);
//		
//		// mocking System
//		PowerMock.mockStaticPartial(System.class, "nanoTime");
//		EasyMock.expect(System.nanoTime()).andReturn(nanoTime);
//		PowerMock.replay(System.class);
//		
//		// mocking UUID
//		UUID mockedUUID = PowerMock.createNiceMock(UUID.class);
//		EasyMock.expect(mockedUUID.toString()).andReturn(textUUID);
//		PowerMock.replay(mockedUUID);
//
//		// mocking UUID.randomUUID()
//		PowerMock.mockStatic(UUID.class);
//		EasyMock.expect(UUID.randomUUID()).andReturn(mockedUUID);
//		PowerMock.replay(UUID.class);
//		
//		String reqId = Whitebox.<String>invokeMethod(instance, "createUniqueReqId");
//		
//		assertEquals(machineName + "<" + "123456" + ">" + nanoTime, reqId);
//		
//		PowerMock.verifyAll();
//		EasyMock.verify(mockedInetAddress);
//	}
	
	
}
