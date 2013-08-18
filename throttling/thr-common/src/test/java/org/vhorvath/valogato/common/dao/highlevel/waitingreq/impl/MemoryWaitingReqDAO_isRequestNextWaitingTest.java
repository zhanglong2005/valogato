package org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.easymock.EasyMock;
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
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.BackendServiceConfigDAOFactory;
import org.vhorvath.valogato.common.dao.highlevel.configuration.backendservice.impl.DummyBackendServiceConfigDAO;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.dao.lowlevel.cache.ICache;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({BackendServiceConfigDAOFactory.class, CacheDAOFactory.class})
public class MemoryWaitingReqDAO_isRequestNextWaitingTest {

	
	private static final Integer PERIOD = 3000;
	private static final Integer AVERAGERESPONSETIME = 10*1000;
	private static final Integer WAITINGREQLISTMAXSIZE = 2;
	private static final Integer MAXNUMBEROFWAITINGREQS = 10;
	
	private MemoryWaitingReqDAO instance = new MemoryWaitingReqDAO();
	
	
	@Test
	// if the strategy is not registeringRequestsIndividually then it is not needed to examine the waiting req list
	public void testIsRequestNextWaiting_preCondition_checkStrategy() throws ThrottlingConfigurationException {
		String requestId = null;
		
		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.maintiningFreeSlots.toString());
		
		String simulatedServiceName = "sim1";
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO());
		PowerMock.replay(BackendServiceConfigDAOFactory.class);
		
		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();

		assertTrue(result);
	}

	
	@Test
	// first = 0, last = 2
	// the first one in the list
	public void testIsRequestNextWaiting_itIsTheNextOne_NoneExpiredReqInList() throws ThrottlingConfigurationException {
		String requestId = "Viktor-PC<421bd21949ea4dbe9546046335750074>171259333714896";
		
		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		String simulatedServiceName = "sim1";
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);

		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(requestId);
		
		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO()).times(2);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", Set.class))
			.andReturn(waitingReqListFirst).times(1);
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(4);
		PowerMock.replay(CacheDAOFactory.class);

		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
	}
	

	@Test
	// first = 0, last = 2
	// the request id is not the next one in the list but the first in the list has expired so it must return true and the expired
	//    request must be removed from the list
	public void testIsRequestNextWaiting_itIsNotInFront_TheOneInFrontOfItHasExpired() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		
		LinkedHashSet<String> waitingReqListFirst = new LinkedHashSet<String>();
		waitingReqListFirst.add(createRequestId(AVERAGERESPONSETIME+PERIOD*3));
		waitingReqListFirst.add(requestId);		
		
		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		String simulatedServiceName = "sim1";
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);

		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO()).times(4);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class)).andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", Set.class)).andReturn(waitingReqListFirst).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", waitingReqListFirst);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(5);
		PowerMock.replay(CacheDAOFactory.class);

		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("["+requestId+"]", waitingReqListFirst.toString());
	}

	
	@Test
	// first = 1, last = 3
	// the request id is not the next one in the list, there are two req being expired and the requestId is in an other list
	// request id is in list 1 on 2nd place -> list2:{otherReq1},list1:{otherReq1, requestId} 
	// the first list will be deleted
	public void testIsRequestNextWaiting_itIsNotInFrontAndInOtherList_TwoReqsInFrontOfItHaveExpired() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		
		LinkedHashSet<String> waitingReqList1st = new LinkedHashSet<String>();
		waitingReqList1st.add(createRequestId(AVERAGERESPONSETIME+PERIOD*5));
		waitingReqList1st.add(createRequestId(AVERAGERESPONSETIME+PERIOD*4));
		
		LinkedHashSet<String> waitingReqList2nd = new LinkedHashSet<String>();
		waitingReqList2nd.add(createRequestId(AVERAGERESPONSETIME+PERIOD*3));
		waitingReqList2nd.add(requestId);

		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		String simulatedServiceName = "sim1";
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(1, 3);

		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO()).times(8);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "1", Set.class))
			.andReturn(waitingReqList1st).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "1", waitingReqList1st);
		EasyMock.expectLastCall();
		mockedCache.remove(ThrConstants.PREFIX_WAITING_REQ_LIST + "1");
		EasyMock.expectLastCall();
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "2", Set.class))
			.andReturn(waitingReqList2nd).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "2", waitingReqList2nd);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(9);
		PowerMock.replay(CacheDAOFactory.class);

		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=2,last=3]", firstLast.toString());
		assertEquals("["+requestId+"]", waitingReqList2nd.toString());
		assertEquals("[]", waitingReqList1st.toString());
	}

	
	@Test
	// first = 0, last = 2
	// the request id is not the next one in the list, there are two req being expired and the requestId is in an other list
	// request id is in list 1 on 2nd place -> list2:{otherReq1},list1:{otherReq1, requestId} 
	// the 0th list cannot be deleted but emptied
	public void testIsRequestNextWaiting_itIsNotInFrontAndInOtherList_TwoReqsInFrontOfItHaveExpired2() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		
		LinkedHashSet<String> waitingReqList0th = new LinkedHashSet<String>();
		waitingReqList0th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*5));
		waitingReqList0th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*4));
		
		LinkedHashSet<String> waitingReqList1st = new LinkedHashSet<String>();
		waitingReqList1st.add(createRequestId(AVERAGERESPONSETIME+PERIOD*3));
		waitingReqList1st.add(requestId);

		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		String simulatedServiceName = "sim1";
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);

		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO()).times(8);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", Set.class))
			.andReturn(waitingReqList0th).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", waitingReqList0th);
		EasyMock.expectLastCall().times(2);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "1", Set.class))
			.andReturn(waitingReqList1st).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "1", waitingReqList1st);
		EasyMock.expectLastCall();
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(9);
		PowerMock.replay(CacheDAOFactory.class);

		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=1,last=2]", firstLast.toString());
		assertEquals("["+requestId+"]", waitingReqList1st.toString());
		assertEquals("[]", waitingReqList0th.toString());
	}

	
	@Test
	// first = 5, last = 0
	// the request id is not the next one in the list, there are five reqs being expired and the requestId is in an other list and must be rolled
	// request id is in list 1 on 2nd place and the number of first list 5 -> list5:{otherReq5,otherReq4}, list6:{otherReq3,otherReq2}, 
	//                                                                        list0:{otherReq1, requestId} 
	// the 5th and 6th list must be deleted and the first list must be the 0th
	public void testIsRequestNextWaiting_itIsNotInFrontAndInOtherList_FiveReqsInFrontOfItHaveExpired_rolling() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		
		LinkedHashSet<String> waitingReqList5th = new LinkedHashSet<String>();
		waitingReqList5th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*10));
		waitingReqList5th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*9));
		
		LinkedHashSet<String> waitingReqList6th = new LinkedHashSet<String>();
		waitingReqList6th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*8));
		waitingReqList6th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*7));

		LinkedHashSet<String> waitingReqList0th = new LinkedHashSet<String>();
		waitingReqList0th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*5));
		waitingReqList0th.add(requestId);

		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		String simulatedServiceName = "sim1";
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(5, 0);

		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO()).times(12);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "5", Set.class))
			.andReturn(waitingReqList5th).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "5", waitingReqList5th);
		EasyMock.expectLastCall();
		mockedCache.remove(ThrConstants.PREFIX_WAITING_REQ_LIST + "5");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "6", Set.class))
			.andReturn(waitingReqList6th).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "6", waitingReqList6th);
		EasyMock.expectLastCall();
		mockedCache.remove(ThrConstants.PREFIX_WAITING_REQ_LIST + "6");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", Set.class))
			.andReturn(waitingReqList0th).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", waitingReqList0th);
		EasyMock.expectLastCall();
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall().times(2);
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(13);
		PowerMock.replay(CacheDAOFactory.class);

		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertTrue(result);
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=0,last=0]", firstLast.toString());
		assertEquals("[]", waitingReqList6th.toString());
		assertEquals("[]", waitingReqList5th.toString());
		assertEquals("["+requestId+"]", waitingReqList0th.toString());
	}


	@Test
	// first = 0, last = 2
	// the request id is not the next one in the list, there are three reqs in front of it -> 2 of them expired, the third is not => return false
	public void testIsRequestNextWaiting_itIsNotInFrontAndInOtherList_ThreeReqsInFrontOfItHaveExpired2() throws ThrottlingConfigurationException {
		String requestId = createRequestId(0);
		String requestIdNotExpired = createRequestId(AVERAGERESPONSETIME+PERIOD*0);
		
		LinkedHashSet<String> waitingReqList0th = new LinkedHashSet<String>();
		waitingReqList0th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*5));
		waitingReqList0th.add(createRequestId(AVERAGERESPONSETIME+PERIOD*4));
		
		LinkedHashSet<String> waitingReqList1st = new LinkedHashSet<String>();
		waitingReqList1st.add(requestIdNotExpired);
		waitingReqList1st.add(requestId);

		BackendServiceBean backendServiceBean = createBackendServiceBean(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString());
		
		String simulatedServiceName = "sim1";
		
		WaitingReqFirstLastListBean firstLast = new WaitingReqFirstLastListBean(0, 2);

		// mocking BackendServiceConfigDAOFactory
		PowerMock.mockStatic(BackendServiceConfigDAOFactory.class);
		EasyMock.expect(BackendServiceConfigDAOFactory.getDAO()).andReturn(new DummyBackendServiceConfigDAO()).times(6);
		PowerMock.replay(BackendServiceConfigDAOFactory.class);

		// mocking ICache
		ICache mockedCache = EasyMock.createMock(ICache.class);
		mockedCache.lock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", WaitingReqFirstLastListBean.class))
			.andReturn(firstLast);
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", Set.class))
			.andReturn(waitingReqList0th).times(1);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "0", waitingReqList0th);
		EasyMock.expectLastCall().times(2);
		mockedCache.put(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST", firstLast);
		EasyMock.expectLastCall();
		EasyMock.expect(mockedCache.get(ThrConstants.PREFIX_WAITING_REQ_LIST + "1", Set.class))
			.andReturn(waitingReqList1st).times(1);
		mockedCache.unlock(ThrConstants.PREFIX_WAITING_REQ_LIST + "FIRST_LAST_LIST");
		EasyMock.expectLastCall();
		EasyMock.replay(mockedCache);
		
		// mocking CacheDAOFactory
		PowerMock.mockStatic(CacheDAOFactory.class);
		EasyMock.expect(CacheDAOFactory.getCache()).andReturn(mockedCache).times(8);
		PowerMock.replay(CacheDAOFactory.class);

		boolean result = instance.isRequestNextWaiting(requestId, backendServiceBean, simulatedServiceName);
		
		PowerMock.verifyAll();
		EasyMock.verify(mockedCache);
		
		assertFalse(result);
		assertEquals("org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean[first=1,last=2]", firstLast.toString());
		assertEquals("["+requestIdNotExpired+", "+requestId+"]", waitingReqList1st.toString());
		assertEquals("[]", waitingReqList0th.toString());
	}

	
	private BackendServiceBean createBackendServiceBean(String strategy) {
		BackendServiceBean backendServiceBean = new BackendServiceBean();
		
		List<SimulatedServiceBean> simulatedServices = new ArrayList<SimulatedServiceBean>();
		SimulatedServiceBean sim0 = new SimulatedServiceBean();
		sim0.setName("sim0");
		FeatureBean feature0 = new FeatureBean();
		Map<String, String> params0 = new HashMap<String, String>();
		params0.put(ThrConstants.FeatureParam.strategy.toString(), "kuttykurutty");
		feature0.setParams(params0);
		sim0.setFeature(feature0);
		simulatedServices.add(sim0);
		
		SimulatedServiceBean sim1 = new SimulatedServiceBean();
		sim1.setName("sim1");
		FeatureBean feature1 = new FeatureBean();
		Map<String, String> params1 = new HashMap<String, String>();
		params1.put(ThrConstants.FeatureParam.strategy.toString(), strategy);
		params1.put(ThrConstants.FeatureParam.period.toString(), PERIOD.toString());
		params1.put(ThrConstants.FeatureParam.waitingReqListMaxSize.toString(), WAITINGREQLISTMAXSIZE.toString());
		params1.put(ThrConstants.FeatureParam.maxNumberOfWaitingReqs.toString(), MAXNUMBEROFWAITINGREQS.toString());
		feature1.setParams(params1);
		sim1.setFeature(feature1);
		simulatedServices.add(sim1);
		
		backendServiceBean.setSimulatedService(simulatedServices);
		backendServiceBean.setAverageResponseTime(AVERAGERESPONSETIME);
		
		return backendServiceBean;
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
