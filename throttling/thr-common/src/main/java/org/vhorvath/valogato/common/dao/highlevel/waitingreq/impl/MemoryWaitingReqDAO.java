package org.vhorvath.valogato.common.dao.highlevel.waitingreq.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vhorvath.valogato.common.beans.configuration.backendservice.BackendServiceBean;
import org.vhorvath.valogato.common.beans.usage.WaitingReqFirstLastListBean;
import org.vhorvath.valogato.common.constants.ThrConstants;
import org.vhorvath.valogato.common.dao.highlevel.waitingreq.IWaitingReqDAO;
import org.vhorvath.valogato.common.dao.lowlevel.cache.CacheDAOFactory;
import org.vhorvath.valogato.common.exception.ThrottlingConfigurationException;
import org.vhorvath.valogato.common.feature.FeatureParamGetter;
import org.vhorvath.valogato.common.utils.ThrottlingUtils;


/**
 * @author Viktor Horvath
 */
public class MemoryWaitingReqDAO implements IWaitingReqDAO {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ThrConstants.THROTTLING_NAME);	

	
	public boolean registerRequest(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName, Integer waitingReqListMaxSize, 
			Integer maxNumberOfWaitingReqs) throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryWaitingReqDAO.registerRequest(%s, %s, %s)", requestId, Integer.toString(waitingReqListMaxSize), 
				Integer.toString(maxNumberOfWaitingReqs)));
		
		String key = null;
		
		// 1. lock the CACHE_WAITING_REQ_FIRST_LAST_LIST
		CacheDAOFactory.getCache().lock(ThrottlingUtils.getWaitingReqFirstLastKey());
		try {
			// 2. gets the first last list counter of waiting req lists
			WaitingReqFirstLastListBean firstLastBean = CacheDAOFactory.getCache().get(ThrottlingUtils.getWaitingReqFirstLastKey(), WaitingReqFirstLastListBean.class);
			
			// 2.a. before registering a clean of the inactive reqs would be needed
			searchingAndCleaning(null, backendServiceBean, simulatedServiceName, firstLastBean);

			// 3. lock and gets the waiting req list
			key = ThrottlingUtils.getWaitingReqListKey(firstLastBean.getLast());
			// there is no need to lock the CACHE_WAITING_REQ_LIST_XXX because the CACHE_WAITING_REQ_FIRST_LAST_LIST lock will
			//    hold the other registerRequest method call
			Set<String> list = CacheDAOFactory.getCache().get(key, Set.class);
			
			// 3.1 if the list is null
			if (list == null) {
				list = Collections.synchronizedSet(new LinkedHashSet<String>());
			}
			// 3.2 if the list is not full then the req is may be put into the list
			if (list.size() < waitingReqListMaxSize) {
				list.add(requestId);
				CacheDAOFactory.getCache().put(key, list);
			}
			// 3.3 if the sizes of lists would exceed the maxNumberOfWaitingReqs value in config => simulatedInterface.buildFault
			//     must be called
			else if (getNumberOfElementsInLists(firstLastBean, waitingReqListMaxSize, maxNumberOfWaitingReqs) >= maxNumberOfWaitingReqs) {
				return false;
			}
			// 3.4 if the list is full => new list must be created
			else {
				Integer nextListNumber = getNextListNumber(firstLastBean.getLast(), waitingReqListMaxSize, maxNumberOfWaitingReqs);
				key = ThrottlingUtils.getWaitingReqListKey(nextListNumber);

				// adding a new empty list to the cache with an increased number
				list = Collections.synchronizedSet(new LinkedHashSet<String>());
				list.add(requestId);
				CacheDAOFactory.getCache().put(key, list);
				// increase the last value of CACHE_WAITING_REQ_FIRST_LAST_LIST
				firstLastBean.setLast(nextListNumber);
				CacheDAOFactory.getCache().put(ThrottlingUtils.getWaitingReqFirstLastKey(), firstLastBean);
			}
			return true;
			
		} finally {
			CacheDAOFactory.getCache().unlock(ThrottlingUtils.getWaitingReqFirstLastKey());
		}
	}


	public boolean isRequestNextWaiting(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName) throws ThrottlingConfigurationException {
		// if the strategy param is not REGISTERING_REQUESTS_INDIVIDUALLY then return true immediately (it means that either 
		//   this feature is not a waiting request at all or it has got different strategy)
		// if the feature doesn't have parameters then return true immediately (see below)
		String strategy = FeatureParamGetter.getStrategy(backendServiceBean, simulatedServiceName);
		if (strategy != null && strategy.length() > 0 && !strategy.equals(ThrConstants.FeatureParamValue.registeringRequestsIndividually.toString())) {
			return true;
		}
		
		LOGGER.debug(String.format("##### MemoryWaitingReqDAO.isRequestNextWaiting(%s, %s, %s)", requestId, backendServiceBean, simulatedServiceName));

		// lock the ThrConstants.PREFIX_WAITING_REQ + "FIRST_LAST_LIST"
		CacheDAOFactory.getCache().lock(ThrottlingUtils.getWaitingReqFirstLastKey());
		try {
			// get the number of the first list
			WaitingReqFirstLastListBean firstLastBean = CacheDAOFactory.getCache().get(ThrottlingUtils.getWaitingReqFirstLastKey(), 
					WaitingReqFirstLastListBean.class);
	
			return searchingAndCleaning(requestId, backendServiceBean, simulatedServiceName, firstLastBean);
		} finally {
			// unlock the ThrConstants.PREFIX_WAITING_REQ + "FIRST_LAST_LIST"
			CacheDAOFactory.getCache().unlock(ThrottlingUtils.getWaitingReqFirstLastKey());
		}
	}

	
	public void unregisterRequest(String requestId, Integer waitingReqListMaxSize, Integer maxNumberOfWaitingReqs) 
			throws ThrottlingConfigurationException {
		LOGGER.debug(String.format("##### MemoryWaitingReqDAO.unregisterRequest(%s, %s, %s)", requestId, 
				Integer.toString(waitingReqListMaxSize), Integer.toString(maxNumberOfWaitingReqs)));

		String key = null;
		// 1. lock the CACHE_WAITING_REQ_FIRST_LAST_LIST
		CacheDAOFactory.getCache().lock(ThrottlingUtils.getWaitingReqFirstLastKey());

		try {
			// 2. gets the first last list counter of waiting req lists
			WaitingReqFirstLastListBean bean = CacheDAOFactory.getCache().get(ThrottlingUtils.getWaitingReqFirstLastKey(), 
					WaitingReqFirstLastListBean.class);
			
			// 3. locks and gets the waiting req list
			key = ThrottlingUtils.getWaitingReqListKey(bean.getFirst());
			// there is no need to lock the CACHE_WAITING_REQ_LIST_XXX because the CACHE_WAITING_REQ_FIRST_LAST_LIST lock will
			//    hold the other unregisterRequest method call
			Set<String> list = CacheDAOFactory.getCache().get(key, Set.class);
			
			removeRequestIdFromList(requestId, list, bean, key, waitingReqListMaxSize, maxNumberOfWaitingReqs, false);
		} finally {
			CacheDAOFactory.getCache().unlock(ThrottlingUtils.getWaitingReqFirstLastKey());
		}
	}


	/*At the present the first and last element of CACHE_WAITING_REQ_FIRST_LAST_LIST can increase without limits.
	Because there is a value maxNumberOfWaitingReqs (e.g. maxNumberOfWaitingReqs=2000, waitingReqListMaxSize=200) set so it is known that 
	at the same time the number of lists can be only maxNumberOfWaitingReqs / waitingReqListMaxSize (e.g. max 10 lists) =>
	so if the last value in CACHE_WAITING_REQ_FIRST_LAST_LIST is bigger than 10 that we can use the zero again for the last value
	last: 9, 10, 11, 0, 1, ..., 9, 10, 11, 0, ...
	we need a plus 1 list to prevent overwriting elements*/
	private Integer getNextListNumber(Integer listCounter, Integer waitingReqListMaxSize, Integer maxNumberOfWaitingReqs) {
		if (listCounter * waitingReqListMaxSize > maxNumberOfWaitingReqs) {
			return 0;
		} else {
			return listCounter + 1;
		}
	}


	private Integer getNumberOfElementsInLists(WaitingReqFirstLastListBean bean, Integer waitingReqListMaxSize, int maxNumberOfWaitingReqs) 
			throws ThrottlingConfigurationException {
		int numberOfElementsInLists = 0;
		
		// how many lists are completely full? if first=2 and last=5 then 2 lists are completely full
		if (bean.getLast() >= bean.getFirst()) {
			// add the number of elements which are between the first and last (e.g. first=0, last=3 => numberOfElementsInLists = 2 * waitingReqListMaxSize)
			if (bean.getLast() - bean.getFirst() >= 2) {
				numberOfElementsInLists = (bean.getLast() - bean.getFirst() - 1) * waitingReqListMaxSize;
			}
		} else {
			// if the last has already rolled to zero
			// the maxInd contains the max. index that the waiting req list can reach -> after that it must roll to zero
			int maxInd = maxNumberOfWaitingReqs / waitingReqListMaxSize + 1;
			numberOfElementsInLists = ((maxInd - bean.getFirst()) * waitingReqListMaxSize) + (bean.getLast() * waitingReqListMaxSize);
		}

		// add the number of elements of the first list
		String key = ThrottlingUtils.getWaitingReqListKey(bean.getFirst());
		numberOfElementsInLists += CacheDAOFactory.getCache().get(key, Set.class).size();
		
		// add the number of elements of the last list
		if (bean.getLast() != bean.getFirst()) {
			key = ThrottlingUtils.getWaitingReqListKey(bean.getLast());
			numberOfElementsInLists += CacheDAOFactory.getCache().get(key, Set.class).size();
		}
		
		LOGGER.trace(String.format("##### MemoryWaitingReqDAO.getNumberOfElementsInLists(..): WaitingReqFirstLastListBean:%s, " +
				"waitingReqListMaxSize: %s, numberOfElementsInLists: %s, key: %s", bean, waitingReqListMaxSize, numberOfElementsInLists, key));

		return numberOfElementsInLists;
	}


	// if requestId==null then only the inactive requests must be removed and we don't want to search
	private boolean searchingAndCleaning(String requestId, BackendServiceBean backendServiceBean, String simulatedServiceName, 
			WaitingReqFirstLastListBean firstLastBean) throws ThrottlingConfigurationException {
		boolean isItTheNextOne = false;
		boolean needExamination = true;
		long actualNano = System.nanoTime();

		// the limit shows how many seconds must be waited until it will be inactive
		int inactivationLimit = backendServiceBean.getAverageResponseTime() + 2 * FeatureParamGetter.getPeriod(backendServiceBean, simulatedServiceName);
		
		// get the list itself
		String key = ThrottlingUtils.getWaitingReqListKey(firstLastBean.getFirst());
		//LOGGER.trace(String.format("####### key = %s", key));

		// there is no need to lock the CACHE_WAITING_REQ_LIST_XXX because the CACHE_WAITING_REQ_FIRST_LAST_LIST lock will
		//    hold the other isRequestNextWaiting method call
		Set<String> list = CacheDAOFactory.getCache().get(key, Set.class);
		
		// if the list is empty...
		if (list.size() == 0) {
			return true;
		}

		Iterator<String> iterator = list.iterator();
		while (needExamination) {
			// if the list is empty then examination is not needed -> end of the loop
			if (!iterator.hasNext()) {
				needExamination = false;
			} else {
				String reqIdInFront = iterator.next();
				// if the request id is in the front...
				if (requestId != null && reqIdInFront.equals(requestId)) {
					needExamination = false;
					isItTheNextOne = true;
				} 
				// must check if the request in the front is still active 
				//   if not then remove from the list and continue the loop
				else {
					long reqNano = getNano(reqIdInFront);
					// if the req is inactive then the request id must be removed from the list
					if ( (actualNano - reqNano) / 1000000 > inactivationLimit) {
						Integer waitingReqListMaxSize = FeatureParamGetter.getWaitingReqListMaxSize(backendServiceBean, simulatedServiceName);
						Integer maxNumberOfWaitingReqs = FeatureParamGetter.getMaxNumberOfWaitingReqs(backendServiceBean, simulatedServiceName);
						
						Map<String,Set<String>> result = removeRequestIdFromList(reqIdInFront, list, firstLastBean, key, waitingReqListMaxSize, 
								maxNumberOfWaitingReqs, true);
						key = result.keySet().iterator().next();
						list = result.get(key);
						iterator = list.iterator();
					} 
					// if an element was found whose timestamp has not expired yet then the search must be stopped
					else {
						needExamination = false;
					}
				}
			}
		}
		
		return isItTheNextOne;
	}


	private long getNano(String requestId) throws ThrottlingConfigurationException {
		int lastIndex = requestId.lastIndexOf('>');
		if (lastIndex == -1) {
			throw new ThrottlingConfigurationException(String.format("The requestId is invalid, it doesn't contain the character '>'! reqestId = %s", 
					requestId));
		}
		try {
			return Long.valueOf(requestId.substring(lastIndex+1));
		} catch(NumberFormatException e) {
			throw new ThrottlingConfigurationException(String.format("The value in requestId is not a timestamp!! value = %s", 
					requestId.substring(lastIndex)));
			
		}		
	}


	private Map<String,Set<String>> removeRequestIdFromList(final String requestId, final Set<String> list, final WaitingReqFirstLastListBean bean, 
			final String key, final Integer waitingReqListMaxSize, final Integer maxNumberOfWaitingReqs, final boolean needListBack) 
					throws ThrottlingConfigurationException {
		String modifiableKey = key;
		Set<String> modifiableList = list;
		
		Map<String,Set<String>> result = new HashMap<String, Set<String>>();
		result.put(modifiableKey, modifiableList);
		
		modifiableList.remove(requestId);
		// 3.1 if the list is empty after removing the element
		if (modifiableList.size() == 0) {
			if (bean.getFirst() != 0) {
				// remove the list from the cache
				CacheDAOFactory.getCache().remove(modifiableKey);
			} else {
				// the 0th list mustn't be removed but it has to be emptied
				CacheDAOFactory.getCache().put(modifiableKey, Collections.synchronizedSet(new LinkedHashSet<String>()));
			}
			// increase the first value of CACHE_WAITING_REQ_FIRST_LAST_LIST
			Integer nextListNumber = bean.getFirst();
			// the first cannot be bigger then the last
			if (bean.getFirst().intValue() != bean.getLast().intValue()) {
				nextListNumber = getNextListNumber(bean.getFirst(), waitingReqListMaxSize, maxNumberOfWaitingReqs);
				bean.setFirst(nextListNumber);
				CacheDAOFactory.getCache().put(ThrottlingUtils.getWaitingReqFirstLastKey(), bean);
			}
			if (needListBack) {
				modifiableKey = ThrottlingUtils.getWaitingReqListKey(nextListNumber);
				modifiableList = CacheDAOFactory.getCache().get(modifiableKey, Set.class);
				result = new HashMap<String, Set<String>>();
				result.put(modifiableKey, modifiableList);
			}
		} else {
			// the list must be saved
			CacheDAOFactory.getCache().put(modifiableKey, modifiableList);
		}
		return result;
	}

	
}