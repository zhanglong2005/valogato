package org.vhorvath.valogato.core.controller.features;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
import org.vhorvath.valogato.common.beans.configuration.backendservice.SimulatedServiceBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl.MemoryBackendServiceConfigDAO;
import org.vhorvath.valogato.common.simulation.ISimulatedService;
import org.vhorvath.valogato.core.controller.ThrottlingProcessController;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ForwarderFeature.class, BackendServiceConfigDAOFactory.class})
public class ForwarderFeatureTest {

	
	private ForwarderFeature<String, String, RuntimeException> instance = null;
	
	
	@Before
	public void setUp() {
		instance = new ForwarderFeature<String, String, RuntimeException>();
	}
	
	
	@Test
	public void testApply() throws Exception {
		// initializing parameters
		String backendServiceName = "backendServiceName";
		
		String simulatedServiceName = "simulatedServiceName";
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setFeature(new FeatureBean());
		backendServiceBean.setSimulatedService(new ArrayList<SimulatedServiceBean>());
		backendServiceBean.getSimulatedService().add(new SimulatedServiceBean());
		backendServiceBean.getSimulatedService().get(0).setFeature(new FeatureBean());
		backendServiceBean.getSimulatedService().get(0).setName(simulatedServiceName);
		
		String req = "req";

		// mocking instance
		instance = PowerMock.createPartialMock(ForwarderFeature.class, "addToStatic");
		PowerMock.expectPrivate(instance, "addToStatic", backendServiceName, simulatedServiceName, backendServiceBean.getSimulatedService().get(0).getFeature());
		PowerMock.replay(instance);
		
		// mocking ISimulatedService
		ISimulatedService<String, String, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
		mockedSimulatedInterface.setEndpoint("1");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedSimulatedInterface.forwardRequest(req)).andReturn(null);
		EasyMock.replay(mockedSimulatedInterface);
		
		IThrottlingController<String, String, RuntimeException> thrController = new ThrottlingProcessController<String, String, RuntimeException>();
		
		//mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new MemoryBackendServiceConfigDAO());
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		// set endpointMapQueue
		Queue<String> q = new LinkedList<String>();
		q.add("1"); q.add("2"); q.add("3");
		Map<String, Queue<String>> map = new HashMap<String, Queue<String>>();
		map.put(backendServiceName+"-"+simulatedServiceName, q);
		Whitebox.getField(ForwarderFeature.class, "endpointMapQueue").set(null, map);
		
		instance.apply(backendServiceName, req, mockedSimulatedInterface, backendServiceBean, simulatedServiceName, thrController);

		// examining the endpointMapQueue
		Map<String, Queue<String>> endpointMapQueue = (Map<String, Queue<String>>) Whitebox.getField(ForwarderFeature.class, "endpointMapQueue").get(null);
		assertEquals("2", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("3", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("1", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedSimulatedInterface);
	}
	
	
	@Test
	// the static Variables Are empty -> the endpoints must be added
	public void testAddToStatic_staticVariablesAreEmpty() throws Exception {
		String endpoints = "loclahost1; localhost2";
		
		String backendServiceName = "backendServiceName";
		
		String simulatedServiceName = "simulatedServiceName";
		
		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.endpoints.toString(), endpoints);
		
		Whitebox.invokeMethod(instance, "addToStatic", backendServiceName, simulatedServiceName, featureBean);
		
		Map<String, String> endpointMapConstant = (Map<String, String>) Whitebox.getField(ForwarderFeature.class, "endpointMapConstant").get(null);
		assertEquals(endpoints, endpointMapConstant.get(backendServiceName+"-"+simulatedServiceName));
		
		Map<String, Queue<String>> endpointMapQueue = (Map<String, Queue<String>>) Whitebox.getField(ForwarderFeature.class, "endpointMapQueue").get(null);
		assertEquals("loclahost1", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("localhost2", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
	}
	


	@Test
	// the static Variables Are not empty and endpoints are different -> the endpoints must be updated
	public void testAddToStatic_staticVariablesAreNotEmptyAndDifferent() throws Exception {
		String endpoints1 = "loclahost1; localhost2";
		String endpointsNew = "loclahost1; localhost3; localhost2";
		
		String backendServiceName = "backendServiceName";
		
		String simulatedServiceName = "simulatedServiceName";
		
		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.endpoints.toString(), endpointsNew);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(backendServiceName+"-"+simulatedServiceName, endpoints1);
		Whitebox.getField(ForwarderFeature.class, "endpointMapConstant").set(null, map);
		
		Whitebox.invokeMethod(instance, "addToStatic", backendServiceName, simulatedServiceName, featureBean);
		
		Map<String, String> endpointMapConstant = (Map<String, String>) Whitebox.getField(ForwarderFeature.class, "endpointMapConstant").get(null);
		assertEquals(endpointsNew, endpointMapConstant.get(backendServiceName+"-"+simulatedServiceName));
		
		Map<String, Queue<String>> endpointMapQueue = (Map<String, Queue<String>>) Whitebox.getField(ForwarderFeature.class, "endpointMapQueue").get(null);
		assertEquals("loclahost1", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("localhost3", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("localhost2", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
	}
	
	
	@Test
	public void testGetEndpoint() throws Exception {
		String backendServiceName = "backendServiceName";
		
		String simulatedServiceName = "simulatedServiceName";

		// set endpointMapQueue
		Queue<String> q = new LinkedList<String>();
		q.add("a5"); q.add("d2"); q.add("r123");
		Map<String, Queue<String>> map = new HashMap<String, Queue<String>>();
		map.put(backendServiceName+"-"+simulatedServiceName, q);
		Whitebox.getField(ForwarderFeature.class, "endpointMapQueue").set(null, map);

		String endpoint = Whitebox.invokeMethod(instance, "getEndpoint", backendServiceName, simulatedServiceName);
		
		assertEquals("a5", endpoint);

		// examining the endpointMapQueue
		Map<String, Queue<String>> endpointMapQueue = (Map<String, Queue<String>>) Whitebox.getField(ForwarderFeature.class, "endpointMapQueue").get(null);
		assertEquals("d2", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("r123", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
		assertEquals("a5", endpointMapQueue.get(backendServiceName+"-"+simulatedServiceName).poll());
	}
	
	
	@Test
	public void testGetEndpointsAsQueue() throws Exception {
		FeatureBean featureBean = new FeatureBean();
		featureBean.setParams(new HashMap<String, String>());
		featureBean.getParams().put(ThrConstants.FeatureParam.endpoints.toString(), "34; 31");
		
		Queue<String> q = Whitebox.invokeMethod(instance, "getEndpointsAsQueue", featureBean);
		
		assertEquals("34", q.poll());
		assertEquals("31", q.poll());
	}
}
