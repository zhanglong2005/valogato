package org.vhorvath.valogato.core.controller.features;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.junit.Test;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.controller.IThrottlingController;
import org.vhorvath.valogato.common.simulation.ISimulatedService;


public class SendBackFaultFeatureTest {

	
	private SendBackFaultFeature<Object, Object, RuntimeException> instance = new SendBackFaultFeature<Object, Object, RuntimeException>();
	
	
	@Test
	public void testApply() {
		String backendServiceName = "backendService";

		String req = "req";
		
		ISimulatedService<Object, Object, RuntimeException> mockedSimulatedInterface = EasyMock.createMock(ISimulatedService.class);
		EasyMock.expect(mockedSimulatedInterface.buildFault("SendBackFaultFeature: the backend system is overwhelmed.")).andReturn(
				new RuntimeException("SendBackFaultFeature: the backend system is overwhelmed."));
		EasyMock.replay(mockedSimulatedInterface);
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		
		String simulatedServiceName = null;
		
		IThrottlingController<Object, Object, RuntimeException> thrController = null;
		
		try {
			instance.apply(backendServiceName, req, mockedSimulatedInterface, backendServiceBean, simulatedServiceName, thrController);
			fail("RuntimeException should have been thrown!");
		} catch (RuntimeException re) {
			assertEquals("SendBackFaultFeature: the backend system is overwhelmed.", re.getMessage());
		}
	}
	
}
