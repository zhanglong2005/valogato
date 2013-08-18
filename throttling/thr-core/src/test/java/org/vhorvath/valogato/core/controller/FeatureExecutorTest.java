package org.vhorvath.valogato.core.controller;


import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl.MemoryBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.feature.IFeature;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.core.controller.features.ForwarderFeature;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BackendServiceConfigDAOFactory.class, FeatureExecutor.class, GeneralConfigurationUtils.class})
public class FeatureExecutorTest {

	
	private FeatureExecutor<String, String, RuntimeException> instance = null;
	
	
	@Before
	public void setUp() {
		instance = new FeatureExecutor<String, String, RuntimeException>();
	}
	
	
	@Test
	public void testApplyFeature() throws Exception {
		// initializing parameters
		String req = "req";
		
		String backendServiceName = "backendServiceName";
		
		ISimulatedService<String, String, RuntimeException> simulatedInterface = null;
		
		String simulatedServiceName = null;
		
		IThrottlingController<String, String, RuntimeException> throttlingContoller = null;
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.getFeature().setName("fateurename");

		// mocking IFeature
		IFeature<String, String, RuntimeException> mockedIFeature = EasyMock.createMock(IFeature.class);
		EasyMock.expect(mockedIFeature.apply(backendServiceName, req, simulatedInterface, backendServiceBean, simulatedServiceName, throttlingContoller))
			.andReturn(null);
		EasyMock.replay(mockedIFeature);

		// mocking instance
		String[] methodNames = {"getFeatureImpl"};
		instance = PowerMock.createPartialMock(FeatureExecutor.class, methodNames);
		PowerMock.expectPrivate(instance, "getFeatureImpl", "fateurename").andReturn(mockedIFeature);
		PowerMock.replay(instance);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new MemoryBackendServiceConfigDAO()).times(1);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		// mocking logger
		Logger mockedLogger = EasyMock.createMock(Logger.class);
		mockedLogger.info(EasyMock.anyObject(String.class));
		EasyMock.expectLastCall().anyTimes();
		EasyMock.replay(mockedLogger);
		
		instance.applyFeature(req, backendServiceName, simulatedInterface, simulatedServiceName, backendServiceBean, throttlingContoller);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedIFeature);
		EasyMock.verify(mockedLogger);
	}
	
	
	@Test
	// feature = ForwarderFeature
	public void testGetFeatureImpl_ForwarderFeature() throws Exception {
		String nameFeature = ThrConstants.Features.ForwarderFeature.toString();
		
		IFeature<String,String,RuntimeException> feature = Whitebox.invokeMethod(instance, "getFeatureImpl", nameFeature);
		
		assertTrue(feature instanceof ForwarderFeature);
	}
	

//	@Test
//	// feature = ForwarderFeature
//	public void testGetFeatureImpl_notBuiltinFeature() throws Exception {
//		String nameFeature = "";
//		
//		NewFeatureBean newFeatureBean = new NewFeatureBean();
//		newFeatureBean.setClazz("aaa");
//		
//		PowerMock.mockStatic(GeneralConfigurationUtils.class);
//		EasyMock.expect(GeneralConfigurationUtils.getFeature(nameFeature)).andReturn(newFeatureBean);
//		PowerMock.replay(GeneralConfigurationUtils.class);
//
//		instance = PowerMock.createPartialMock(FeatureExecutor.class, "getUserDefinedFeature");
//		PowerMock.expectPrivate(instance, "getUserDefinedFeature", "aaa").andReturn(new FeatureImpl());
//		PowerMock.replay(instance);
//		
//		IFeature<String,String,RuntimeException> feature = Whitebox.invokeMethod(instance, "getFeatureImpl", nameFeature);
//		
//		assertTrue(feature instanceof FeatureImpl);
//		
//		PowerMock.verifyAll();
//	}


//	@Test
//	public void testGetUserDefinedFeature_successful() throws Exception {
//		String clazz = "org.vhorvath.valogato.core.controller.FeatureImpl";
//		
//		IFeature feature = Whitebox.invokeMethod(instance, "getUserDefinedFeature", clazz);
//		
//		assertTrue(feature instanceof FeatureImpl);
//	}
	

//	@Test
//	public void testGetUserDefinedFeature_cannotBeCastToIFeature() throws Exception {
//		String clazz = "java.lang.String";
//		
//		try {
//			IFeature feature = Whitebox.invokeMethod(instance, "getUserDefinedFeature", clazz);
//			fail("ThrottlingConfigurationException should have been thrown!");
//		} catch(ThrottlingConfigurationException e) {
//			e.printStackTrace();
//			assertEquals("The class java.lang.String cannot be cast to IFeature<RQ, RS, EX>!", e.getMessage());
//		}
//	}


//	@Test
//	public void testGetUserDefinedFeature_cannotBeInstantiated() throws Exception {
//		String clazz = "org.vhorvath.valogato.core.controller.AbstractFeatureImpl";
//		
//		try {
//			IFeature feature = Whitebox.invokeMethod(instance, "getUserDefinedFeature", clazz);
//			fail("ThrottlingConfigurationException should have been thrown!");
//		} catch(ThrottlingConfigurationException e) {
//			e.printStackTrace();
//			assertEquals("The class "+clazz+" cannot be instantiated!", e.getMessage());
//		}
//	}


//	@Test
//	public void testGetUserDefinedFeature_cannotBeLoaded() throws Exception {
//		String clazz = "org.vhorvath.valogato.core.contr1111111111oller.AbstractFeatureImpl";
//		
//		try {
//			IFeature feature = Whitebox.invokeMethod(instance, "getUserDefinedFeature", clazz);
//			fail("ThrottlingConfigurationException should have been thrown!");
//		} catch(ThrottlingConfigurationException e) {
//			e.printStackTrace();
//			assertEquals("The class "+clazz+" cannot be loaded!", e.getMessage());
//		}
//	}
}

//class FeatureImpl implements IFeature<String, String, RuntimeException> {
//
//	public String apply(
//			String backendServiceName,
//			String req,
//			ISimulatedService<String, String, RuntimeException> simulatedInterface,
//			BackendServiceBean backendServiceBean,
//			String simulatedServiceName,
//			IThrottlingController<String, String, RuntimeException> thrController)
//			throws RuntimeException, ThrottlingConfigurationException,
//			ThrottlingRuntimeException {
//		return null;
//	}
//	
//}

//abstract class AbstractFeatureImpl implements IFeature<String, String, RuntimeException> {
//
//	public String apply(
//			String backendServiceName,
//			String req,
//			ISimulatedService<String, String, RuntimeException> simulatedInterface,
//			BackendServiceBean backendServiceBean,
//			String simulatedServiceName,
//			IThrottlingController<String, String, RuntimeException> thrController)
//			throws RuntimeException, ThrottlingConfigurationException,
//			ThrottlingRuntimeException {
//		return null;
//	}
//	
//}
