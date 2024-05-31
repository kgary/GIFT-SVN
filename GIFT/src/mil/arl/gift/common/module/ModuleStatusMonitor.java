/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class is responsible for determining if a module has timed out because it has 
 * been some time since the last module status was received.
 * 
 * @author mhoffman
 *
 */
public class ModuleStatusMonitor {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ModuleStatusMonitor.class);
    
    /** 
     * Contains information on the last time a module was heard from  
     * The reason this is not a multi-key map is because there are two use cases:
     *  1. get information for all modules of a given module type
     *  2. update the module information for a single module of a given type
     */
    // synchronization notes:
    // 1) put
    //     moduleStatusInfo - receivedModuleStatus method - only when there isn't a sub-map already
    //     submap - receivedModuleStatus method - only when there isn't an info entry already
    // 2) remove
    //     moduleStatusInfo - never
    //     submap - removeModule method
    // 3) get
    //    moduleStatusInfo - getLastStatus method, dont want underlying map to change while gathering statuses
    //                     - removeModule method, dont want REMOVE to happen during status update because subsequent gets will have no mapping
    //                     - receivedModuleStatus method, dont want REMOVE to happen during status update
    //    
    private Map<ModuleTypeEnum, Map<String, StatusReceivedInfo>> moduleStatusInfo = new HashMap<ModuleTypeEnum, Map<String, StatusReceivedInfo>>();
    
    /**
     * contains the listeners interested in being notified about module status updates (add, remove, changed).
     * Using a ConcurrentLinkedQueue because most of the operations involve notifying all listeners in the collection
     * and we were getting an occasional concurrent modification exception with a List in the updateModule method most
     * likely because the same thread was iterating and then adding.removing a new listener.
     */
    private final Queue<ModuleStatusListener> listeners = new ConcurrentLinkedQueue<>();
    
    /** this is the thread that checks for timed-out message clients */
    private Timer timer;
    
    /** time between checks */
    private static final int DELAY_MS = 5000; 
    
    /** maximum time between module status message before it is consider timed-out */
    private static final long LAST_STATUS_TIMEOUT = CommonProperties.getInstance().getModuleStatusMonitorTimeoutMs();
    
    /** the singleton instance of this class */
    private static ModuleStatusMonitor instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return ModuleStatusMonitor
     */
    public static ModuleStatusMonitor getInstance(){
        
        if(instance == null){
            instance = new ModuleStatusMonitor();
        }
        
        return instance;
    }

    /**
     * Class constructor
     */
    private ModuleStatusMonitor(){        
        timer = new Timer("ModuleStatusMonitorTimer");
    }
    
    /**
     * Close this monitor by cleaning up any monitored modules
     */
    public void close() {

        timer.cancel();
    }
    
    /**
     * Return the last module status information for the modules of the given type
     * 
     * @param moduleType - module type to get module status information for
     * @return the last module status for each module instance of the given module type. Can be null.
     */
    public List<ModuleStatus> getLastStatus(ModuleTypeEnum moduleType){
                    
        Map<String, StatusReceivedInfo> modulesInfoMap = moduleStatusInfo.get(moduleType);
            
        if(modulesInfoMap != null){
            
            synchronized(modulesInfoMap){
                
                List<ModuleStatus> statusList = new ArrayList<ModuleStatus>(modulesInfoMap.values().size());
                for(StatusReceivedInfo info : modulesInfoMap.values()){
                    
                    statusList.add(info.getModuleStatus().copy());
                }
                
                return statusList;

            }

        }//end if
        
        //unable to find status list
        return null;
    }
    
    /**
     * Return the last module status information for the given address of a module which is also provided
     * in a module status object.
     * 
     * @param moduleType - the type of module that has the given address
     * @param moduleAddress - the address of the module whose last module status information is needed.  If null this method returns null.
     * @return ModuleStatus - the last module status of the desired module
     */
    public ModuleStatus getLastStatus(ModuleTypeEnum moduleType, String moduleAddress){
        
        if(StringUtils.isBlank(moduleAddress)) {
            return null;
        }
        
        Map<String, StatusReceivedInfo> modulesInfoMap = moduleStatusInfo.get(moduleType);
        
        if(modulesInfoMap != null){
            
            synchronized(modulesInfoMap){
                StatusReceivedInfo info = modulesInfoMap.get(moduleAddress);
                
                if(info != null){
                    return info.getModuleStatus();
                }
            }

        }//end if
        
        //unable to find status list
        return null;
    }
    
    /**
     * Adds a listener to the container of listeners interested in module status events
     * 
     * @param listener - the listener to add
     */
    public void addListener(final ModuleStatusListener listener) {
        
        synchronized(listeners){
            if(listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }
    
    /**
     * Removes a listener from the container of listeners interested in module status events
     * 
     * @param listener - the listener to remove
     */
    public void removeListener(final ModuleStatusListener listener) {
        
        synchronized(listeners){
            if(listeners != null && listeners.contains(listener)) {
                listeners.remove(listener);
            }
        }
    }
    
    /**
     * Used to cancel the monitoring of a module's status.  This is most likely used
     * when a module has been ordered to close (i.e. kill module, JWS GW close domain session).
     * 
     * @param status can't be null
     */
    public void cancelModuleListening(ModuleStatus status){
        
        if(status == null){
            throw new IllegalArgumentException("The module status can't be null.");
        }
        
        //get the status object and set that it should be cancelled
        Map<String, StatusReceivedInfo> statusMap = moduleStatusInfo.get(status.getModuleType());
        if(statusMap == null){
            return; 
        }
            
        StatusReceivedInfo info = null;
        synchronized(statusMap){                
                info = statusMap.get(status.getQueueName());
                
                if(info != null){
                    info.setCancelledMonitoring(true);
                    if(logger.isInfoEnabled()){
                        logger.info("Set flag to cancel module status monitoring of "+status+".");
                    }
                }
        }
    }
    
    /**
     * The module has become stale (i.e. timed-out).  Notify the listeners of the event.
     * 
     * @param info
     */
    private void removeModule(StatusReceivedInfo info){
        
        synchronized(listeners){
            
            Iterator<ModuleStatusListener> listenerItr = listeners.iterator();
            while(listenerItr.hasNext()){
                ModuleStatusListener listener = listenerItr.next();
                
                try{
                    listener.moduleStatusRemoved(info);
                }catch(Exception e){
                    logger.error("Caught exception from mis-behaving listener "+listener+" when removing module for "+info,e);
                }
            }
            
            synchronized(moduleStatusInfo){
                Map<String, StatusReceivedInfo> statusMap = moduleStatusInfo.get(info.getModuleStatus().getModuleType());
                if(statusMap != null){
                    statusMap.remove(info.getModuleStatus().getQueueName());
                }
            }
        }
    }
    
    /**
     * A module has just been added (i.e. joined the GIFT network).  Notify listeners of the event.
     * 
     * @param info
     */
    private void addModule(StatusReceivedInfo info){
        
        synchronized(listeners){
            
            Iterator<ModuleStatusListener> listenerItr = listeners.iterator();
            while(listenerItr.hasNext()){
                ModuleStatusListener listener = listenerItr.next();
                try{
                    listener.moduleStatusAdded(info.getSentTime(), info.getModuleStatus());
                }catch(Exception e){
                    logger.error("Caught exception from mis-behaving listener "+listener,e);
                }
            }
        }
    }
    
    /**
     * A module status has just been received for a module.  Notify listeners of the event.
     * 
     * @param info
     */
    private void updateModule(StatusReceivedInfo info){
        
        synchronized(listeners){
            
            Iterator<ModuleStatusListener> listenerItr = listeners.iterator();
            while(listenerItr.hasNext()){
                ModuleStatusListener listener = listenerItr.next();
                
                try{
                    listener.moduleStatusChanged(info.getSentTime(), info.getModuleStatus());
                }catch(Exception e){
                    logger.error("Caught exception from mis-behaving listener "+listener,e);
                }
            }
        }
    }
    
    /**
     * Create and initialize a new instance of the subclass StatusReceivedInfo for the module 
     * status information being provided.
     * 
     * @param time
     * @param status
     * @return StatusReceivedInfo - new instance
     */
    private StatusReceivedInfo createInfo(long time, ModuleStatus status){
        
        StatusReceivedInfo info = new StatusReceivedInfo(time, status);            
        timer.schedule(new StatusTimerTask(info), DELAY_MS, DELAY_MS);
        
        return info;
    }
    
    /**
     * Notify the monitor that a module was heard from
     * 
     * @param time - the time at which the module status was created
     * @param status - the module status being received for a module
     */
    public void receivedModuleStatus(long time, ModuleStatus status) {
        
        if(status == null){
            return;
        }
        
        boolean updateOperation = false;
        Map<String, StatusReceivedInfo> statusMap = moduleStatusInfo.get(status.getModuleType());
        if(statusMap == null){
            statusMap = new ConcurrentHashMap<String, StatusReceivedInfo>();
            moduleStatusInfo.put(status.getModuleType(), statusMap); // (ADD
                                                                     // OPERATION)
        }
            
        StatusReceivedInfo info = null;
        synchronized(statusMap){
                
                info = statusMap.get(status.getQueueName());
                if(info != null){ 
                    updateOperation = true;
                }else{
                    info = createInfo(time, status);
                    statusMap.put(info.getModuleStatus().getQueueName(), info); 
                    if(logger.isInfoEnabled()){
                        logger.info("Discovered module from status: "+status);
                    }
                }
                
        }//release statusMap to perform listener notification method calls

        
        if(updateOperation){                    
            info.update(time, status);
            updateModule(info);            
        }else{        
            addModule(info);
        }
    }
    
    /**
     * This class contains a time stamp associated with a module status and is used
     * to determine when the last time the module was heard from.
     * 
     * @author mhoffman
     *
     */
    public class StatusReceivedInfo{
        
        /** last time a module status message was received for a module (milliseconds) */
        private long lastRcvdTime;
        
        /** the last status for the module associated with this received information */        
        private ModuleStatus status;
        
        /** the time at which the status was created/sent */
        private long sentTime;
        
        /** 
         * the amount of time in milliseconds since the last received timestamp that caused a module timeout
         * because the value was higher than the timeout threshold
         */
        private long timeoutValue = 0L;
        
        /**
         * whether the monitoring for the module represented by this class was cancelled
         */
        private boolean cancelledMonitoring = false;
        
        /**
         * Class constructor 
         * 
         * @param sentTime - the time at which the status was created/sent
         * @param status the last status for the module associated with this received information
         */
        public StatusReceivedInfo(long sentTime, ModuleStatus status){
            this.status = status;
            this.sentTime = sentTime;
            
            // Default the value with the current time.
            this.lastRcvdTime = System.currentTimeMillis();
        }
        
        /**
         * Another message was received from this client, update the last time received information
         * 
         * @param sentTime - the time at which the status was created/sent
         * @param status the last status for the module associated with this received information
         */
         void update(long sentTime, ModuleStatus status){
            this.status = status;
            this.sentTime = sentTime;
            lastRcvdTime = System.currentTimeMillis();
        }
        
        /**
         * Return the last time a module status message was received for the client.
         * 
         * @return long
         */
        public long getTimeReceived(){
            return lastRcvdTime;
        }
        
        /**
         * Return the time at which the status was created/sent
         * 
         * @return long
         */
        public long getSentTime(){
            return sentTime;
        }
        
        /**
         * Return the last module status associated with this received information
         * 
         * @return ModuleStatus
         */        
        public ModuleStatus getModuleStatus(){
            return status;
        }
        
        void setTimeoutValue(long value){
            this.timeoutValue = value;
        }
        
        /**
         * Return the amount of time in milliseconds since the last received timestamp that caused a module timeout
         * because the value was higher than the timeout threshold.
         * 
         * @return can be zero if not set because there wasn't a timeout
         */
        public long getTimeoutValue() {
            return timeoutValue;
        }
        
        public boolean shouldCancelledMonitoring() {
            return cancelledMonitoring;
        }

        public void setCancelledMonitoring(boolean cancelledMonitoring) {
            this.cancelledMonitoring = cancelledMonitoring;
        }

        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ReceivedInfo:");
            sb.append(" time = ").append(getTimeReceived());
            sb.append(", sentTime = ").append(getSentTime());
            sb.append(", timeout = ").append(getTimeoutValue());
            sb.append(", cancelMonitoring = ").append(shouldCancelledMonitoring());
            sb.append(", status = ").append(getModuleStatus());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This class contains the timer task associated with a single module and it's status.
     * It will check for a timed-out module by examining the timestamp associated with the last received status.
     * 
     * @author mhoffman
     *
     */
    protected class StatusTimerTask extends TimerTask{
        
        /** container for the last module status received */
        private StatusReceivedInfo info;
        
        /** used to hold the "now" time */
        private long now;
        
        /**
         * Class constructor
         * 
         * @param info container for the last module status received
         */
        public StatusTimerTask(StatusReceivedInfo info){
            this.info = info;
        }
        
        @Override
        public void run() {
            
            now = System.currentTimeMillis();
            
            //check if module has timed-out
            if((now - LAST_STATUS_TIMEOUT) > info.getTimeReceived()){
                
                info.setTimeoutValue((now - info.getTimeReceived()));
                if(!info.shouldCancelledMonitoring()){
                    if(logger.isInfoEnabled()){
                        logger.info("Module has timed out having not heard from it in "+(info.getTimeoutValue()/1000.0)+" seconds - "+info);
                    }
                }else{
                    if(logger.isInfoEnabled()){
                        logger.info("Module status monitoring timer task is gracefully ending for "+info+".");
                    }
                }
                
                removeModule(info);
                
                cancel();
            }
        }
    }
}
