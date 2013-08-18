package org.vhorvath.valogato.common.dao.highlevel.usage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.usage.BackendServiceFreqBean;
import org.vhorvath.valogato.common.beans.usage.BackendServiceSleepingReqBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.exception.ThrottlingRuntimeException;
import org.vhorvath.valogato.common.utils.ThrottlingStorage;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheDAOFactory.class})
public class MemoryUsageDAOTest {

	
	private MemoryUsageDAO instance = null;
	
	@Before
	public void setUp() {
		instance = new MemoryUsageDAO();
	}
	
	@Test
	public void testGetFreqOfBackendServices() throws ThrottlingConfigurationException {
		BackendServiceFreqBean use1 = new BackendServiceFreqBean();
		use1.setNameBackendService("use1");
		BackendServiceFreqBean use2 = new BackendServiceFreqBean();
		use2.setNameBackendService("use2");

		List<String> keys = new ArrayList<String>();
		keys.add(ThrConstants.PREFIX_CACHE_FREQUENCY+"key1"); 
		keys.add(ThrConstants.PREFIX_CACHE_FREQUENCY+"key2");
		keys.add("mas");

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_FREQUENCY + "key1", BackendServiceFreqBean.class))
			.andReturn(use1);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_FREQUENCY + "key2", BackendServiceFreqBean.class))
			.andReturn(use2);
		EasyMock.expect(mockedCache.getKeys()).andReturn(keys);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(3);
		PowerMock.replay(CacheDAOFactory.class);

		List<BackendServiceFreqBean> backendServiceUseBeanList = instance.getFreqOfBackendServices();

		assertEquals(2, backendServiceUseBeanList.size());
		assertEquals("use1", backendServiceUseBeanList.get(0).getNameBackendService());
		assertEquals("use2", backendServiceUseBeanList.get(1).getNameBackendService());

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
	
	@Test
	public void testGetWaitingReqOfBackendServices() throws ThrottlingConfigurationException {
		BackendServiceSleepingReqBean use1 = new BackendServiceSleepingReqBean();
		use1.setNameBackendService("use1");
		BackendServiceSleepingReqBean use2 = new BackendServiceSleepingReqBean();
		use2.setNameBackendService("use2");

		List<String> keys = new ArrayList<String>();
		keys.add(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"key1");
		keys.add(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ+"key2");
		keys.add("mas");

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + "key1", BackendServiceSleepingReqBean.class))
			.andReturn(use1);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + "key2", BackendServiceSleepingReqBean.class))
			.andReturn(use2);
		EasyMock.expect(mockedCache.getKeys()).andReturn(keys);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(3);
		PowerMock.replay(CacheDAOFactory.class);

		List<BackendServiceSleepingReqBean> backendServiceUseBeanList = instance.getSleepingReqOfBackendServices();

		assertEquals(2, backendServiceUseBeanList.size());
		assertEquals("use1", backendServiceUseBeanList.get(0).getNameBackendService());
		assertEquals("use2", backendServiceUseBeanList.get(1).getNameBackendService());

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetFreqOfBackendService() throws ThrottlingConfigurationException {
		String nameBackendService = "BackendService";
		
		BackendServiceFreqBean use = new BackendServiceFreqBean();
		use.setNameBackendService(nameBackendService);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_FREQUENCY + nameBackendService, BackendServiceFreqBean.class))
			.andReturn(use);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);
		
		BackendServiceFreqBean bean = instance.getFreqOfBackendService(nameBackendService);
		
		assertEquals(use.getNameBackendService(), bean.getNameBackendService());

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
	
	@Test
	public void testGetWaitingReqOfBackendService() throws ThrottlingConfigurationException {
		String nameBackendService = "BackendService";
		
		BackendServiceSleepingReqBean use = new BackendServiceSleepingReqBean();
		use.setNameBackendService(nameBackendService);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService, BackendServiceSleepingReqBean.class))
			.andReturn(use);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);
		
		BackendServiceSleepingReqBean bean = instance.getSleepingReqOfBackendService(nameBackendService);
		
		assertEquals(use.getNameBackendService(), bean.getNameBackendService());

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testSetFrequency() throws ThrottlingConfigurationException {
		String backendServiceName = "name";
		
		Integer frequency = 12;
		
		BackendServiceFreqBean use = new BackendServiceFreqBean();

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_FREQUENCY + backendServiceName, BackendServiceFreqBean.class))
			.andReturn(use);
		mockedCache.put(ThrConstants.PREFIX_CACHE_FREQUENCY + backendServiceName, use);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(2);
		PowerMock.replay(CacheDAOFactory.class);
		
		instance.setFrequency(backendServiceName, frequency);
	
		assertEquals(frequency, use.getFrequency());
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
	
	@Test
	public void testSetWaitingRequests() throws ThrottlingConfigurationException {
		String backendServiceName = "name";
		
		Integer waitingRequests = 12;
		
		BackendServiceSleepingReqBean use = new BackendServiceSleepingReqBean();

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + backendServiceName, BackendServiceSleepingReqBean.class))
			.andReturn(use);
		mockedCache.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + backendServiceName, use);
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(2);
		PowerMock.replay(CacheDAOFactory.class);
		
		instance.setNumberOfSleepingRequests(backendServiceName, waitingRequests);
	
		assertEquals(waitingRequests, use.getNumberOfSleepingRequests());
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testGetUsageOfBackendServiceName() throws ThrottlingConfigurationException {
		List<String> keys = new ArrayList<String>();
		keys.add(ThrConstants.PREFIX_CACHE_FREQUENCY+"key1"); 
		keys.add(ThrConstants.PREFIX_CACHE_FREQUENCY+"key2");
		keys.add("mas");

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.getKeys()).andReturn(keys);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
		PowerMock.replay(CacheDAOFactory.class);

		List<String> nameList = instance.getUsageOfBackendServiceNames();

		assertEquals(2, nameList.size());
		assertEquals("key1", nameList.get(0));
		assertEquals("key2", nameList.get(1));

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
	
//	@Test
//	public void testGetBackendServiceUsageBean() throws ThrottlingConfigurationException {
//		String nameBackendService = "BackendService";
//		
//		BackendServiceFreqBean bean = new BackendServiceFreqBean();
//		bean.setNameBackendService(nameBackendService);
//
//		// mocking ICache
//		ICache mockedCache = EasyMock.createMock(ICache.class);
//		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_FREQUENCY + nameBackendService, BackendServiceFreqBean.class)).andReturn(bean);
//		EasyMock.replay(mockedCache);
//		
//		// mocking CacheDAOFactory
//		PowerMock.mockStatic(CacheDAOFactory.class);
//		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(1);
//		PowerMock.replay(CacheDAOFactory.class);
//
//		BackendServiceFreqBean use = instance.getBackendServiceUsageBean(nameBackendService );
//		
//		assertEquals(nameBackendService, use.getNameBackendService());
//
//		EasyMock.verify(mockedCache);
//		PowerMock.verify(CacheDAOFactory.class);
//	}
	
	@Test
	public void testIncreaseNumberOfWaitingRequests() throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		String nameBackendService = "BackendService";
		
		BackendServiceSleepingReqBean use = new BackendServiceSleepingReqBean();
		use.setNameBackendService(nameBackendService);
		use.setNumberOfSleepingRequests(11);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService, BackendServiceSleepingReqBean.class))
			.andReturn(use);
		mockedCache.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService, use);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(2);
		PowerMock.replay(CacheDAOFactory.class);
		
		instance.increaseNumberOfSleepingRequests(nameBackendService);
		
		assertEquals(12, use.getNumberOfSleepingRequests().intValue());
		assertTrue(ThrottlingStorage.isChangedTheSleepingReqNumber());
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
	
	@Test
	public void testDecreaseNumberOfWaitingRequests() throws ThrottlingConfigurationException, ThrottlingRuntimeException {
		String nameBackendService = "BackendService";
		
		BackendServiceSleepingReqBean use = new BackendServiceSleepingReqBean();
		use.setNameBackendService(nameBackendService);
		use.setNumberOfSleepingRequests(11);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService, BackendServiceSleepingReqBean.class))
			.andReturn(use);
		mockedCache.put(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService, use);
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(2);
		PowerMock.replay(CacheDAOFactory.class);
		
		instance.decreaseNumberOfSleepingRequests(nameBackendService);
		
		assertEquals(10, use.getNumberOfSleepingRequests().intValue());
		assertFalse(ThrottlingStorage.isChangedTheSleepingReqNumber());
		
		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}

	@Test
	public void testInitBackendServiceUsage() throws ThrottlingConfigurationException {
		String nameBackendService = "BackendService";
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_FREQUENCY + nameBackendService, BackendServiceFreqBean.class))
			.andReturn(null);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_CACHE_NR_OF_SLEEPING_REQ + nameBackendService, BackendServiceSleepingReqBean.class))
			.andReturn(null);
		mockedCache.put(EasyMock.anyObject(String.class), EasyMock.anyObject(BackendServiceFreqBean.class));
		mockedCache.put(EasyMock.anyObject(String.class), EasyMock.anyObject(BackendServiceSleepingReqBean.class));
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);

		instance.initBackendServiceUsage(nameBackendService );

		EasyMock.verify(mockedCache);
		PowerMock.verify(CacheDAOFactory.class);
	}
}
