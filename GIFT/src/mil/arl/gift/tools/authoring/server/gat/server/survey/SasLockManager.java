/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.survey;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use this class to manage the locking and unlocking of SAS objects. You first
 * acquire a lock by calling the lock method with True for the acquisition
 * parameter. From then on it is your responsibility to continue to renew the
 * lock via calls to the lock method with False for the acquisition. If a
 * lock hasn't been renewed in more than 1.5 minutes then it is at risk
 * of being automatically released by subsequent interactions with the
 * lock manager. To manually release a lock just call the unlock method.
 * 
 * NOTE: Originally, I wanted the lock and release process to be a completely
 * manually process. I hate the idea of forcing the client to constantly renew
 * the lock over and over again. Unfortunately if the user closes the browser
 * GWT doesn't give us enough time for the client to tell the server to release
 * the lock. In that case the lock would remain engaged forever. So this is the
 * workaround, if you can think of something better then please do.
 * @author elafave
 *
 */
public class SasLockManager {

	private static SasLockManager instance = null;
	
	/**
	 * Maps the Question ID to the time it was last locked.
	 * 
	 * I can see client requests come in on their own threads. I don't see any
	 * code that deals with a multi-threaded environment so for the sake of
	 * safety I used a ConcurrentHashMap.
	 */
	private ConcurrentHashMap<Integer, Long> questionIdToLockTimeMap = new ConcurrentHashMap<Integer, Long>();
	
	/**
	 * Maps the Survey ID to the time it was last locked.
	 * 
	 * I can see client requests come in on their own threads. I don't see any
	 * code that deals with a multi-threaded environment so for the sake of
	 * safety I used a ConcurrentHashMap.
	 */
	private ConcurrentHashMap<Integer, Long> surveyIdToLockTimeMap = new ConcurrentHashMap<Integer, Long>();
	
	/**
	 * Maps the survey context ID to the time it was last locked.
	 * 
	 * I can see client requests come in on their own threads. I don't see any
	 * code that deals with a multi-threaded environment so for the sake of
	 * safety I used a ConcurrentHashMap.
	 */
	private ConcurrentHashMap<Integer, Long> surveyContextIdToLockTimeMap = new ConcurrentHashMap<Integer, Long>();
	
	private SasLockManager() {
		
	}
	
	/**
	 * Get the instance of this singleton.
	 * @return The one and only instance of this class.
	 */
	static public SasLockManager getInstance() {
		if(instance == null) {
			instance = new SasLockManager();
		}
		return instance;
	}
	
	/**
	 * Determines if the question associated with the ID is locked.
	 * @param questionId ID of the question to check.
	 * @return True if the question is locked, false otherwise.
	 */
	public boolean isQuestionLocked(int questionId) {
		return isLocked(questionIdToLockTimeMap, questionId);
	}
	
	/**
	 * Attempts to lock the question with the given ID.
	 * @param questionId ID associated with question to be locked.
	 * @param acquisition True if we're trying to acquire the lock for the 
	 * first time, false if we're simply renewing a lock we already have.
	 * @return False if the question couldn't be locked (i.e. we were in 
	 * acquisition mode and the file was already locked), true otherwise.
	 */
	public boolean lockQuestion(int questionId, boolean acquisition) {
		return lock(questionIdToLockTimeMap, questionId, acquisition);
	}
	
	/**
	 * Unlocks the question associated with the ID.
	 * @param questionId ID of the question to unlock.
	 */
	public void unlockQuestion(int questionId) {
		unlock(questionIdToLockTimeMap, questionId);
	}
	
	/**
	 * Determines if the survey associated with the ID is locked.
	 * @param surveyId ID of the survey to check.
	 * @return True if the survey is locked, false otherwise.
	 */
	public boolean isSurveyLocked(int surveyId) {
		return isLocked(surveyIdToLockTimeMap, surveyId);
	}
	
	/**
	 * Attempts to lock the survey with the given ID.
	 * @param surveyId ID associated with survey to be locked.
	 * @param acquisition True if we're trying to acquire the lock for the 
	 * first time, false if we're simply renewing a lock we already have.
	 * @return False if the survey couldn't be locked (i.e. we were in 
	 * acquisition mode and the file was already locked), true otherwise.
	 */
	public boolean lockSurvey(int surveyId, boolean acquisition) {
		return lock(surveyIdToLockTimeMap, surveyId, acquisition);
	}
	
	/**
	 * Unlocks the survey associated with the ID.
	 * @param surveyId ID of the survey to unlock.
	 */
	public void unlockSurvey(int surveyId) {
		unlock(surveyIdToLockTimeMap, surveyId);
	}
	
	/**
	 * Determines if the survey context associated with the ID is locked.
	 * @param id ID of the survey context to check.
	 * @return True if the survey context is locked, false otherwise.
	 */
	public boolean isSurveyContextLocked(int id) {
		return isLocked(surveyContextIdToLockTimeMap, id);
	}
	
	/**
	 * Attempts to lock the survey context with the given ID.
	 * @param id ID associated with survey context to be locked.
	 * @param acquisition True if we're trying to acquire the lock for the 
	 * first time, false if we're simply renewing a lock we already have.
	 * @return False if the survey context couldn't be locked (i.e. we were in 
	 * acquisition mode and the file was already locked), true otherwise.
	 */
	public boolean lockSurveyContext(int id, boolean acquisition) {
		return lock(surveyContextIdToLockTimeMap, id, acquisition);
	}
	
	/**
	 * Unlocks the survey context associated with the ID.
	 * @param id ID of the survey context to unlock.
	 */
	public void unlockSurveyContext(int id) {
		unlock(surveyContextIdToLockTimeMap, id);
	}
	
	/**
	 * Removes any stale locks from the map and then checks to see if the given
	 * id is still locked.
	 * @param idToLockTimeMap Maps IDs to the time their lock was last acquired/renewed.
	 * @param id ID whose lock status must be checked..
	 * @return True if the ID is locked, false otherwise.
	 */
	static private boolean isLocked(ConcurrentHashMap<Integer, Long> idToLockTimeMap, int id) {
		releaseStaleLocks(idToLockTimeMap);
		
		if(idToLockTimeMap.containsKey(id)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Attempts to lock the given ID.
	 * @param idToLockTimeMap Maps IDs to the time their lock was last acquired/renewed.
	 * @param id ID to be locked.
	 * @param acquisition True if we're trying to acquire the lock for the 
	 * first time, false if we're simply renewing a lock we already have.
	 * @return False if the ID couldn't be locked (i.e. we were in 
	 * acquisition mode and the ID was already locked), true otherwise.
	 */
	static private boolean lock(ConcurrentHashMap<Integer, Long> idToLockTimeMap, int id, boolean acquisition) {
		//You can't acquire a lock if it has already been
		//acquired.
		if(acquisition && isLocked(idToLockTimeMap, id)) {
			return false;
		}
		
		//The lock will be granted in either of the following cases:
		//1.) Acquiring a lock that is available.
		//2.) Renewing a lock that has already been acquired.
		idToLockTimeMap.put(id, System.currentTimeMillis());
		return true;
	}
	
	/**
	 * Unlocks the ID.
	 * @param idToLockTimeMap Maps IDs to the time their lock was last acquired/renewed.
	 * @param id ID to unlock.
	 */
	static private void unlock(ConcurrentHashMap<Integer, Long> idToLockTimeMap, int id) {
		releaseStaleLocks(idToLockTimeMap);
		idToLockTimeMap.remove(id);
	}
	
	/**
	 * Removes any locks that haven't been locked in the last 1.5 minutes.
	 */
	static private void releaseStaleLocks(ConcurrentHashMap<Integer, Long> idToLockTimeMap) {
		try {
			long currentTime = System.currentTimeMillis();
			
			Iterator<Entry<Integer, Long>> it = idToLockTimeMap.entrySet().iterator();
			while(it.hasNext()) {
				long lockTime = it.next().getValue();
				long delta = currentTime - lockTime;
				
				//If the file hasn't been locked for 1.5 minutes then release
				//the lock.
				if(delta >= 90000) {
					it.remove();
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
