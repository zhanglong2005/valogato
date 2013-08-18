package org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheDAOFactory.class, Collections.class, MemoryWaitingReqDAO.class})
public class MemoryWaitingReqDAO_registerRequestTest {

	
	private MemoryWaitingReqDAO instance = new MemoryWaitingReqDAO();
	
	@Before
	public void setUp() throws Exception {
		String[] methodNames = {"searchingAndCleaning"};
		instance = PowerMock.createPartialMock(MemoryWaitingReqDAO.class, methodNames);
		PowerMock.expectPrivate(instance, "searchingAndCleaning", EasyMock.anyObject(String.class), EasyMock.anyObject(BackendServiceBean.class),
				EasyMock.anyObject(String.class), EasyMock.anyObject(WaitingReqFirstLastListBean.class)).andReturn(true);
		PowerMock.replay(instance);
	}
	
	
	@Test
	// first = 0, last = 2, waitingReqListMaxSize = 100, maxNumberOfWaitingReqs = 1000
	// this this condition meets -> '3.2 if the list is not full'
	public void testRegisterRequest_listIsNotFull1() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 100;
		
		Integer maxNumberOfWaitingReqs = 1000;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), waitingReqListLast);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(5);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("["+requestIdInLastList1+", "+requestId+"]", waitingReqListLast.toString());
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=0,last=2]", firstLast.toString());
	}
	
	
	@Test
	// first = 0, last = 0, waitingReqListMaxSize = 100, maxNumberOfWaitingReqs = 100
	// this this condition meets -> '3.2 if the list is not full'
	public void testRegisterRequest_listIsNotFull2() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 100;
		
		Integer maxNumberOfWaitingReqs = 100;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 0);

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), waitingReqListFirst);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(5);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("["+requestIdInFirstList1+", "+requestId+"]", waitingReqListFirst.toString());
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=0,last=0]", firstLast.toString());
	}


	@Test
	// first = 0, last = 1, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 4
	// this condition meets -> '3.3 if the sizes of lists would exceed the maxNumberOfWaitingReqs value in config'
	public void testRegisterRequest_maxNumberOfWaitingReqsIsFull() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 4;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 1);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);
		waitingReqListLast.add(requestIdInLastList1+"1");

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);
		waitingReqListFirst.add(requestIdInFirstList1+"1");

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(2);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(6);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertFalse(result);
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=0,last=1]", firstLast.toString());
	}


	@Test
	// first = 0, last = 2, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 10
	// this condition meets -> '3.4 if the list is full'
	public void testRegisterRequest_listIsFull() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 10;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);
		waitingReqListLast.add(requestIdInLastList1+"1");

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		LinkedHashSet<String> newSet = new LinkedHashSet<String>();
		
		// mocking Collections
		PowerMock.mockStatic(Collections.class);
		EasyMock.expect(Collections.synchronizedSet(EasyMock.anyObject(LinkedHashSet.class))).andReturn(newSet);
		PowerMock.replay(Collections.class);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(2);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "3", newSet);
		EasyMock.expectLastCall();
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();		
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(8);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("["+requestId+"]", newSet.toString());
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=0,last=3]", firstLast.toString());
	}

	@Test
	// first = 2, last = 6, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 10
	// this condition meets -> '3.4 if the list is full'
	// the value of the last will be 0 
	public void testRegisterRequest_listIsFull_rollingToZero() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 10;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(2, 6);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);
		waitingReqListLast.add(requestIdInLastList1+"1");

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		LinkedHashSet<String> newSet = new LinkedHashSet<String>();
		
		// mocking Collections
		PowerMock.mockStatic(Collections.class);
		EasyMock.expect(Collections.synchronizedSet(EasyMock.anyObject(LinkedHashSet.class))).andReturn(newSet);
		PowerMock.replay(Collections.class);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(2);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", newSet);
		EasyMock.expectLastCall();
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();		
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(8);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("["+requestId+"]", newSet.toString());
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=2,last=0]", firstLast.toString());
	}

	@Test
	// first = 2, last = 0, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 10
	// the lists 3,4,5 and 6 are full => number of elements > maxNumberOfWaitingReqs => return false
	public void testRegisterRequest_listIsFull_firstGreaterThanLast1() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 10;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(2, 0);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);
		waitingReqListLast.add(requestIdInLastList1+"1");

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(2);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst);
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);

		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(6);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertFalse(result);
	}


	@Test
	// first = 3, last = 1, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 10
	// the lists 4,5,6 and 0 are full => number of elements > maxNumberOfWaitingReqs => return false
	public void testRegisterRequest_listIsFull_firstGreaterThanLast2() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 10;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(3, 1);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);
		waitingReqListLast.add(requestIdInLastList1+"1");

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(2);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst);
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(6);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
	
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertFalse(result);
	}


	@Test
	// first = 4, last = 1, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 10
	// the lists 5,6 and 0 are full => number of elements < maxNumberOfWaitingReqs => return true
	public void testRegisterRequest_listIsNotFull_firstGreaterThanLast() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		String requestIdInLastList1 = "Viktor-PC<421bd21949ea4dbe9546046335750073>171259333714897";
		String requestIdInFirstList1 = "Viktor-PC<421bd21949ea4dbe9546046335750072>171259333714898";
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 10;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(4, 1);

		LinkedHashSet<String> waitingReqListLast = new LinkedHashSet<String>();
		waitingReqListLast.add(requestIdInLastList1);
		waitingReqListLast.add(requestIdInLastList1+"1");

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestIdInFirstList1);

		LinkedHashSet<String> newSet = new LinkedHashSet<String>();
		
		// mocking Collections
		PowerMock.mockStatic(Collections.class);
		EasyMock.expect(Collections.synchronizedSet(EasyMock.anyObject(LinkedHashSet.class))).andReturn(newSet);
		PowerMock.replay(Collections.class);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getLast(), Set.class))
			.andReturn(waitingReqListLast).times(2);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "" + firstLast.getFirst(), Set.class))
			.andReturn(waitingReqListFirst);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "2", newSet);
		EasyMock.expectLastCall();
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();		
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(8);
		PowerMock.replay(CacheDAOFactory.class);
		
		boolean result = instance.registerRequest(requestId, null, null, waitingReqListMaxSize, maxNumberOfWaitingReqs);
			
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("["+requestId+"]", newSet.toString());
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=4,last=2]", firstLast.toString());
	}
}