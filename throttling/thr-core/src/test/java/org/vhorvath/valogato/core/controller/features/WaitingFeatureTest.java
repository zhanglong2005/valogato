package org.vhorvath.valogato.core.controller.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.SimulatedServiceBean;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.IBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.lowlevel.configuration.general.GeneralConfigurationUtils;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.common.sleeping.SleepingInFeatureManager;


@RunWith(PowerMockRunner.class)
@PrepareForTest({GeneralConfigurationUtils.class, WaitingFeature.class, BackendServiceConfigDAOFactory.class})
public class WaitingFeatureTest {

	
	private WaitingFeature<String, String, RuntimeException> instance = new WaitingFeature<String, String, RuntimeException>();
	private FeatureBean featureBean2 = null;
	
	
	@Test
	public void testApply_successful() throws Exception {
		String backendServiceName = "backendServiceBean";
		
		String req = "req";
		
		ISimulatedService<String, String, RuntimeException> simulatedInterface = EasyMock.createMock(ISimulatedService.class);
		EasyMock.replay(simulatedInterface);
		
		BackendServiceBean backendServiceBean = createBackendserviceBean();
		
		String simulatedServiceName = "simulatedService2";
		
		Integer averageResponseTime = 3000;
		
		Calendar startOfProcessing = Calendar.getInstance();
		startOfProcessing.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - 0*averageResponseTime);

		IThrottlingController<String, String, RuntimeException> mockedThrController = EasyMock.createMock(IThrottlingController.class);
		EasyMock.expect(mockedThrController.getStartOfProcessing()).andReturn(startOfProcessing);
		EasyMock.expect(mockedThrController.processRequestAfterSleeping(req, simulatedInterface, backendServiceName, simulatedServiceName, backendServiceBean))
			.andReturn(null);
		EasyMock.replay(mockedThrController);
		
		// mocking SleepingInFeatureManager
		SleepingInFeatureManager mockedSleepingInFeatureManager = EasyMock.createMock(SleepingInFeatureManager.class);
		mockedSleepingInFeatureManager.wait(backendServiceName, featureBean2);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedSleepingInFeatureManager);
		PowerMock.expectNew(SleepingInFeatureManager.class).andReturn(mockedSleepingInFeatureManager);
		PowerMock.replay(SleepingInFeatureManager.class);
		
		// mocking IBackendServiceConfigDAO
		IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
		EasyMock.expect(mockedBackendServiceConfigDAO.getAverageResponseTime(backendServiceName)).andReturn(averageResponseTime);
		EasyMock.expect(mockedBackendServiceConfigDAO.getFeature(backendServiceBean, simulatedServiceName)).andReturn(featureBean2);
		EasyMock.replay(mockedBackendServiceConfigDAO);

		//mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO).times(2);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		instance.apply(backendServiceName, req, simulatedInterface, backendServiceBean, simulatedServiceName, mockedThrController);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedSleepingInFeatureManager);
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedThrController);
	}

	
	@Test
	public void testApply_averageResponseTimeExceeded() throws Exception {
		String backendServiceName = "backendServiceBean";
		
		String req = "req";
		
		Integer averageResponseTime = 3000;
		
		String exceptionText = String.format("WaitingFeature: the average response (%s) time has been exceeded!", 
				averageResponseTime.toString());
		
		ISimulatedService<String, String, RuntimeException> simulatedInterface = EasyMock.createMock(ISimulatedService.class);
		EasyMock.expect(simulatedInterface.buildFault(exceptionText)).andReturn(new RuntimeException(exceptionText));
		EasyMock.replay(simulatedInterface);
		
		BackendServiceBean backendServiceBean = createBackendserviceBean();
		
		String simulatedServiceName = "simulatedService2";
		
		Calendar startOfProcessing = Calendar.getInstance();
		startOfProcessing.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - 2*averageResponseTime);

		// mocking IThrottlingController
		IThrottlingController<String, String, RuntimeException> mockedThrController = EasyMock.createMock(IThrottlingController.class);
		EasyMock.expect(mockedThrController.getStartOfProcessing()).andReturn(startOfProcessing);
		EasyMock.replay(mockedThrController);
		
		// mocking SleepingInFeatureManager
		SleepingInFeatureManager mockedSleepingInFeatureManager = EasyMock.createMock(SleepingInFeatureManager.class);
		mockedSleepingInFeatureManager.wait(backendServiceName, featureBean2);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedSleepingInFeatureManager);
		PowerMock.expectNew(SleepingInFeatureManager.class).andReturn(mockedSleepingInFeatureManager);
		PowerMock.replay(SleepingInFeatureManager.class);
		
		// mocking IBackendServiceConfigDAO
		IBackendServiceConfigDAO mockedBackendServiceConfigDAO = EasyMock.createMock(IBackendServiceConfigDAO.class);
		EasyMock.expect(mockedBackendServiceConfigDAO.getAverageResponseTime(backendServiceName)).andReturn(averageResponseTime);
		EasyMock.expect(mockedBackendServiceConfigDAO.getFeature(backendServiceBean, simulatedServiceName)).andReturn(featureBean2);
		EasyMock.replay(mockedBackendServiceConfigDAO);

		//mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(mockedBackendServiceConfigDAO).times(2);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		try {
			instance.apply(backendServiceName, req, simulatedInterface, backendServiceBean, simulatedServiceName, mockedThrController);
			fail("RuntimeException should have been thrown!");
		} catch(RuntimeException re) {
			assertEquals(exceptionText, re.getMessage());
		}
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedSleepingInFeatureManager);
		EasyMock.verify(mockedBackendServiceConfigDAO);
		EasyMock.verify(mockedThrController);
		EasyMock.verify(simulatedInterface);
	}
	
	private BackendServiceBean createBackendserviceBean() {
		List<SimulatedServiceBean> simulatedServices = new ArrayList<SimulatedServiceBean>();
		
		SimulatedServiceBean simulatedService1 = new SimulatedServiceBean();
		simulatedService1.setName("simulatedService1");
		FeatureBean featureBean1 = new FeatureBean();
		featureBean1.setName("feature1");
		simulatedService1.setFeature(featureBean1);
		simulatedServices.add(simulatedService1);
		
		SimulatedServiceBean simulatedService2 = new SimulatedServiceBean();
		simulatedService2.setName("simulatedService2");
		featureBean2 = new FeatureBean();
		featureBean2.setName("feature2");
		simulatedService2.setFeature(featureBean2);
		simulatedServices.add(simulatedService2);

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName("backendServiceBean");
		backendServiceBean.setSimulatedService(simulatedServices);
		FeatureBean featureBean0 = new FeatureBean();
		featureBean0.setName("feature0");
		backendServiceBean.setFeature(featureBean0);
		
		return backendServiceBean;
	}
	
}
