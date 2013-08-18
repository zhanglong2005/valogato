package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServicesBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.SimulatedServiceBean;
import org.vhorvath.valogato.common.dao.highlevel.usage.IUsageDAO;
import org.vhorvath.valogato.common.dao.highlevel.usage.UsageDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UsageDAOFactory.class, CacheDAOFactory.class})
public class ParentBackendServiceConfigDAOTest {

	private TestParentBackendServiceConfigDAO instance = null;
	
	@Before
	public void setUp() {
		instance = new TestParentBackendServiceConfigDAO();
	}
	
	@Test
	public void testGetFeature_withCorrectSimulatedServiceName() throws ThrottlingConfigurationException {
		BackendServiceBean backendServiceBean = createBackendserviceBean();
		
		String simulatedServiceName = "simulatedService2";
		
		FeatureBean foundFeatureBean = instance.getFeature(backendServiceBean, simulatedServiceName);
		
		assertEquals("feature2", foundFeatureBean.getName());
	}

	@Test
	public void testGetFeature_withIncorrectSimulatedServiceName() throws ThrottlingConfigurationException {
		BackendServiceBean backendServiceBean = createBackendserviceBean();
		
		String simulatedServiceName = "simulatedService21";
		
		FeatureBean foundFeatureBean = instance.getFeature(backendServiceBean, simulatedServiceName);
		
		assertEquals("feature0", foundFeatureBean.getName());
	}

	@Test
	public void testGetFeature_withNullSimulatedServiceName() throws ThrottlingConfigurationException {
		BackendServiceBean backendServiceBean = createBackendserviceBean();
		
		String simulatedServiceName = null;
		
		FeatureBean foundFeatureBean = instance.getFeature(backendServiceBean, simulatedServiceName);
		
		assertEquals("feature0", foundFeatureBean.getName());
	}

	@Test
	public void testProcessConfiguration() throws ThrottlingConfigurationException {
		// mocking instance
		String[] methodNames = {"initWaitingReqFirstLastList", "initFirstWaitingReqList", "put"};
		ParentBackendServiceConfigDAO innerInstance = PowerMock.createPartialMock(ParentBackendServiceConfigDAO.class, methodNames);
		innerInstance.initWaitingReqFirstLastList();
		EasyMock.expectLastCall();
		innerInstance.initFirstWaitingReqList();
		EasyMock.expectLastCall();
		innerInstance.put(EasyMock.anyObject(String.class), EasyMock.anyObject(BackendServiceBean.class));
		EasyMock.expectLastCall().times(2);
		PowerMock.replay(innerInstance);
		
		// mocking IUsageDAO
		IUsageDAO mockedIUsageDAO = EasyMock.createMock(IUsageDAO.class);
		mockedIUsageDAO.initBackendServiceUsage("backendServiceBean");
		EasyMock.expectLastCall().times(2);
		EasyMock.replay(mockedIUsageDAO);
		
		// mocking UsageDAOFactory
		PowerMock.mockStatic(UsageDAOFactory.class);
		EasyMock.expect(UsageDAOFactory.getDAO()).andReturn(mockedIUsageDAO).times(2);
		PowerMock.replay(UsageDAOFactory.class);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.put(EasyMock.anyObject(String.class), EasyMock.anyObject(Set.class));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache);
		PowerMock.replay(CacheDAOFactory.class);
		
		BackendServicesBean backendServices = new BackendServicesBean();
		List<BackendServiceBean> backendServiceList = new ArrayList<BackendServiceBean>();
		backendServiceList.add(createBackendserviceBean());
		backendServiceList.add(createBackendserviceBean());
		backendServices.setBackendService(backendServiceList);
		
		innerInstance.processConfiguration(backendServices);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedIUsageDAO);
		EasyMock.verify(mockedCache);
	}
	
	private BackendServiceBean createBackendserviceBean() {
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName("backendServiceBean");
		backendServiceBean.setMaxLoading(100);
		backendServiceBean.setAverageResponseTime(10000);
		
		List<SimulatedServiceBean> simulatedServices = new ArrayList<SimulatedServiceBean>();
		
		SimulatedServiceBean simulatedService1 = new SimulatedServiceBean();
		simulatedService1.setName("simulatedService1");
		FeatureBean featureBean1 = new FeatureBean();
		featureBean1.setName("feature1");
		simulatedService1.setFeature(featureBean1);
		simulatedServices.add(simulatedService1);
		
		SimulatedServiceBean simulatedService2 = new SimulatedServiceBean();
		simulatedService2.setName("simulatedService2");
		FeatureBean featureBean2 = new FeatureBean();
		featureBean2.setName("feature2");
		simulatedService2.setFeature(featureBean2);
		simulatedServices.add(simulatedService2);
		
		backendServiceBean.setSimulatedService(simulatedServices);
		
		FeatureBean featureBean0 = new FeatureBean();
		featureBean0.setName("feature0");
		backendServiceBean.setFeature(featureBean0);
		
		return backendServiceBean;
	}
	
class TestParentBackendServiceConfigDAO extends ParentBackendServiceConfigDAO {

	public void put(String backendServiceName, BackendServiceBean backendServiceBean) throws ThrottlingConfigurationException {
	}

	public BackendServiceBean getBackendService(String backendServiceName) throws ThrottlingConfigurationException {
		return null;
	}

	public List<BackendServiceBean> getBackendServices() throws ThrottlingConfigurationException {
		return null;
	}

	public Integer getMaxLoading(String backendServiceName) throws ThrottlingConfigurationException {
		return null;
	}

	public FeatureBean getFeature(String backendServiceName, String simulatedServiceName) throws ThrottlingConfigurationException {
		return null;
	}

	public Integer getAverageResponseTime(String backendServiceName) throws ThrottlingConfigurationException {
		return null;
	}

	@Override
	protected void initWaitingReqFirstLastList() throws ThrottlingConfigurationException {
	}

	@Override
	protected void initFirstWaitingReqList() throws ThrottlingConfigurationException {
	}
	
}
}
