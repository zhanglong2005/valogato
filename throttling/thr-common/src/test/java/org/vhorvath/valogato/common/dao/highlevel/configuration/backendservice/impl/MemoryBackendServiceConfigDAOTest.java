package org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import org.vhorvath.valogato.common.beans.configuration.backendservice.FeatureBean;
import org.vhorvath.valogato.common.beans.configuration.backendservice.SimulatedServiceBean;
import org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.IBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheDAOFactory.class})
public class MemoryBackendServiceConfigDAOTest {

	private IBackendServiceConfigDAO instance = null;
	
	@Before
	public void setUp() {
		instance = new MemoryBackendServiceConfigDAO();
	}
	
	@Test
	public void testPut() throws ThrottlingConfigurationException {
		String backendServiceName = "name";
		
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName);
		EasyMock.expectLastCall();
		mockedCache.put(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName, backendServiceBean);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(3);
		PowerMock.replay(CacheDAOFactory.class);		
		
		instance.put(backendServiceName, backendServiceBean);
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
	
	@Test
	public void testGetBackendService_notnull() throws ThrottlingConfigurationException {
		String backendServiceName = "dexter";

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName(backendServiceName);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName, BackendServiceBean.class))
			.andReturn(backendServiceBean );
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		BackendServiceBean result = instance.getBackendService(backendServiceName);
		
		assertEquals(backendServiceName, result.getName());

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetBackendService_null() throws ThrottlingConfigurationException {
		String backendServiceName = "dexter";

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName(backendServiceName);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName, BackendServiceBean.class))
			.andReturn(null );
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		try {
			instance.getBackendService(backendServiceName);
			fail("ThrottlingConfigurationException should have been thrown!");
		} catch (ThrottlingConfigurationException e) {
			assertFalse(e.isLoaded());
		}
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetBackendServices_notnull() throws ThrottlingConfigurationException {
		BackendServiceBean backendServiceBean1 = new BackendServiceBean();
		backendServiceBean1.setName("asdhj1");
		BackendServiceBean backendServiceBean2 = new BackendServiceBean();
		backendServiceBean2.setName("asdhj2");

		List<String> keys = new ArrayList<String>();
		keys.add(ThrConstants.PREFIX_CACHE_CONFIGURATION+"key1"); 
		keys.add(ThrConstants.PREFIX_CACHE_CONFIGURATION+"key2");
		keys.add("mas");

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + "key1", BackendServiceBean.class))
			.andReturn(backendServiceBean1);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + "key2", BackendServiceBean.class))
			.andReturn(backendServiceBean2);
		EasyMock.expect(mockedCache.getKeys()).andReturn(keys);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(3);
		PowerMock.replay(CacheDAOFactory.class);

		List<BackendServiceBean> backendServiceList = instance.getBackendServices();
		
		assertEquals(2, backendServiceList.size());
		assertEquals("asdhj1", backendServiceList.get(0).getName());
		assertEquals("asdhj2", backendServiceList.get(1).getName());
	
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetMaxLoading() throws ThrottlingConfigurationException {
		String backendServiceName = "dexter";

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName("asdhj");
		backendServiceBean.setMaxLoading(453);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName, BackendServiceBean.class))
			.andReturn(backendServiceBean );
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		Integer maxLoading = instance.getMaxLoading(backendServiceName);
		
		assertEquals(453, maxLoading.intValue());
	
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetFeature() throws ThrottlingConfigurationException {
		String backendServiceName = "backendServiceBean";
		String simulatedServiceName = "simulatedService2";

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName, BackendServiceBean.class))
			.andReturn(createBackendserviceBean());
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		FeatureBean foundFeatureBean = instance.getFeature(backendServiceName, simulatedServiceName);
		
		assertEquals("feature2", foundFeatureBean.getName());
	
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetAverageResponseTime() throws ThrottlingConfigurationException {
		String backendServiceName = "dexter";

		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName("asdhj");
		backendServiceBean.setAverageResponseTime(452);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_CONFIGURATION + backendServiceName, BackendServiceBean.class))
			.andReturn(backendServiceBean );
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		Integer averageResponseTime = instance.getAverageResponseTime(backendServiceName);
		
		assertEquals(452, averageResponseTime.intValue());
	
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testInitWaitingReqFirstLastList_null() throws ThrottlingConfigurationException {
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrottlingUtils.getWaitingReqFirstLastKey(), WaitingReqFirstLastListBean.class))
			.andReturn(null);
		mockedCache.put(EasyMock.anyObject(String.class), EasyMock.anyObject(WaitingReqFirstLastListBean.class));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(2);
		PowerMock.replay(CacheDAOFactory.class);

		new MemoryBackendServiceConfigDAO().initWaitingReqFirstLastList();
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testInitWaitingReqFirstLastList_notnull() throws ThrottlingConfigurationException {
		WaitingReqFirstLastListBean bean = new WaitingReqFirstLastListBean(0, 0);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrottlingUtils.getWaitingReqFirstLastKey(), WaitingReqFirstLastListBean.class))
			.andReturn(bean);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		new MemoryBackendServiceConfigDAO().initWaitingReqFirstLastList();
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testInitFirstWaitingReqList_null() throws ThrottlingConfigurationException {
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrottlingUtils.getWaitingReqListKey(0), Set.class)).andReturn(null);
		mockedCache.put(EasyMock.anyObject(String.class), EasyMock.anyObject(Set.class));
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(2);
		PowerMock.replay(CacheDAOFactory.class);

		new MemoryBackendServiceConfigDAO().initFirstWaitingReqList();
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testInitFirstWaitingReqList_notnull() throws ThrottlingConfigurationException {
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrottlingUtils.getWaitingReqListKey(0), Set.class)).andReturn(new LinkedHashSet<String>());
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		new MemoryBackendServiceConfigDAO().initFirstWaitingReqList();
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	private BackendServiceBean createBackendserviceBean() {
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		backendServiceBean.setName("backendServiceBean");
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

}
