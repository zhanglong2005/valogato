package org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BackendServiceConfigDAOFactory.class, CacheDAOFactory.class})
public class MemoryWaitingReqDAO_unregisterRequestTest {

	
	private MemoryWaitingReqDAO instance = new MemoryWaitingReqDAO();
	
	
	@Test
	// first = 0, last = 2, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 6
	// the list is not empty after removing the req id
	public void testUnregisterRequest_listWontBeEmpty() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		String nextRequestId = createRequestId(343);
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 6;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);
		
		LinkedHashSet<String> waitingReqList = new LinkedHashSet<String>();
		waitingReqList.add(requestId);
		waitingReqList.add(nextRequestId);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", Set.class))
			.andReturn(waitingReqList).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", waitingReqList);
		EasyMock.expectLastCall().times(1);
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(5);
		PowerMock.replay(CacheDAOFactory.class);
		
		instance.unregisterRequest(requestId, waitingReqListMaxSize, maxNumberOfWaitingReqs);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=0,last=2]", firstLast.toString());
		assertEquals("["+nextRequestId+"]", waitingReqList.toString());
	}
	
	
	@Test
	// first = 1, last = 3, waitingReqListMaxSize = 2, maxNumberOfWaitingReqs = 6
	// the waiting req list becomes empty after removing the req id
	public void testUnregisterRequest_listWillBeEmpty() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		
		Integer waitingReqListMaxSize = 2;
		
		Integer maxNumberOfWaitingReqs = 6;
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(1, 3);
		
		LinkedHashSet<String> waitingReqList1 = new LinkedHashSet<String>();
		waitingReqList1.add(requestId);
		
		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "1", Set.class))
			.andReturn(waitingReqList1).times(1);
		mockedCache.remove(ThrConstants.PREFIX_WAITING_REQ_LIST + "1");
		EasyMock.expectLastCall().times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(6);
		PowerMock.replay(CacheDAOFactory.class);
		
		instance.unregisterRequest(requestId, waitingReqListMaxSize, maxNumberOfWaitingReqs);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=2,last=3]", firstLast.toString());
		assertEquals("[]", waitingReqList1.toString());
	}

	
	private String createRequestId(long milisec) {
		String computername = "VHORVATH";
		
		// we don't need the complete nanotime but its end
		long currentNanoTime = System.nanoTime();
		String nanoTime = Long.toString(currentNanoTime - milisec * 1000000);
		
		String id = computername + "<" + UUID.randomUUID().toString().replace("-", "") + ">" + nanoTime;
		return id;
	}

}