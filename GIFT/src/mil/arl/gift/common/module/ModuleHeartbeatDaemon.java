/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;



/**
 * This class is used by modules in order to send module status messages at
 * a specified rate.
 *   
 * @author mhoffman
 *
 */
public class ModuleHeartbeatDaemon {
	
	/** amount of time in seconds between heartbeats */
	private static final double DEFAULT_HEARTBEAT_INTERVAL_SECONDS = 1.0;
	
	/** a prefix for the heartbeat thread name */
	private static final String HEARTBEAT_PREFIX = "Heartbeat:";
	
	/** amount of time in seconds between heartbeats */ 
	private double heartbeatIntervalSeconds;
	
	/** the name of the heartbeat thread */
	private String threadName;
	
	/** the thread responsible for heartbeat */
	private HeartbeatThread hThread = null;
	
	/** the module instance for this heartbeat */
	private AbstractModule module = null;
	
	/**
	 * Class constructor - uses default heartbeat rate
	 * 
	 * @param module - the module which owns this heartbeat and handles heartbeat logic
	 */
	public ModuleHeartbeatDaemon(AbstractModule module){
		this.module = module;
		this.heartbeatIntervalSeconds = DEFAULT_HEARTBEAT_INTERVAL_SECONDS;
		this.threadName = HEARTBEAT_PREFIX + module.getModuleName();
	}
	
	/**
	 * Class constructor 
	 * 
	 * @param module - the module which owns this heartbeat and handles heartbeat logic
	 * @param heartbeatIntervalSeconds - amount of time in seconds between heartbeats
	 */
	public ModuleHeartbeatDaemon(AbstractModule module, double heartbeatIntervalSeconds){
		this.module = module;
		this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
		this.threadName = HEARTBEAT_PREFIX + module.getModuleName();
	}
	
	
	/**
	 * Initialize the Heartbeat Daemon
	 */
	public void init(){
		
		if(hThread == null){
			hThread = new HeartbeatThread(threadName);
			hThread.start();
		}
	}
	
	/**
	 * Shutdown the Heartbeat Daemon
	 */
	public void shutdown(){
		
		if(hThread != null){
			hThread.alive = false;
			hThread.interrupt();
		}
	}
	
	/**
	 * This class is responsible for requesting that a status message
	 * be sent.
	 *  
	 * @author mhoffman
	 *
	 */
	private class HeartbeatThread extends Thread{
		
		/** whether the thread should continue */
		public boolean alive = true;
		
		public HeartbeatThread(String threadName){
			super(threadName);
		}
		
		/**
		 * The thread logic
		 */
                @Override
		public void run(){
			
			long intervalMillis = (long)(heartbeatIntervalSeconds * 1000.0);
			
			while(alive){
				
	            try {
	        		sleep(intervalMillis);
	        	} catch (@SuppressWarnings("unused") InterruptedException e) {
	        		//upon shutdown, an interrupted exception can occur by interrupted on the thread
	        		//System.out.println("ModuleHeartbeatDaemon.HeartbeatThread: caught exception while sleeping,\n"+e);
	        	}
	        	
	        	handleHeartbeat();
			}
		}
	}
	
	/**
	 * Handle heartbeat logic
	 */
	private void handleHeartbeat(){
		module.sendModuleStatus();
	}	

}
