/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.Task;

/**
 * This class is responsible for processing trigger events (e.g. concept ended, task ended)
 * in a threaded FIFO queue.  It contains publish/subscribe (for now single subscribe) type logic in an effort to decouple
 * the event created from possible synchronization/deadlock issue on resources w/in the 
 * performance node hierarchy.
 * 
 * @author mhoffman
 *
 */
public class TriggerEventManager {
	
    /** The logger for the class */
	private static Logger logger = LoggerFactory.getLogger(TriggerEventManager.class);
	
	/** whether the event manager should still be processing events in the queue */
	private boolean running = true;
	
	/** the queue where events are placed and process in FIFO ordering */
    private final List<ScenarioEvent> scenarioEventList = new LinkedList<ScenarioEvent>();
    
    /** thread used to pull events from the queue */
    private Thread eventListenThread;
    
    /** where to deliver the events too, one at a time, FIFO*/
    private TriggerEventHandler handler;
    
    /** a label associated with the handler */
    private String handlerName;
    
    /**
     * Start the threaded queue logic.
     * 
     * @param handlerName - a (useful) label associated with the handler
     * @param handler - where to deliver the events too, one at a time, FIFO
     */
    public TriggerEventManager(String handlerName, TriggerEventHandler handler){
    	
    	if(handler == null){
    		throw new IllegalArgumentException("The handler can't be null.");
    	}else if(handlerName == null){
    		throw new IllegalArgumentException("The handler name can't be null.");
    	}
    	
    	this.handler = handler;
    	this.handlerName = handlerName;
    }
    
    /**
     * Start the thread that removes events from the queue
     */
    public void start(){
        
        if(eventListenThread != null && eventListenThread.isAlive()){
            logger.warn("The Trigger Event Listen thread is already running.");
            return;
        }
    	
        Runnable listenRunnable = new Runnable() {

            @Override
            public void run() {

            	processEvents();
            }
        };
        
        eventListenThread = new Thread(listenRunnable, handlerName + " Trigger Event Listener");
        eventListenThread.start();
    }
    
    /**
     * Clear any queue'ed events and notify the listener thread
     * to gracefully terminate.
     */
    public void quit(){
    	running = false;

        //cleanup the event queue and thread
        synchronized(scenarioEventList){
        	scenarioEventList.clear();
        	scenarioEventList.notifyAll();
        	
        	//Nick 2/6/15 - Need to call this method in case processEvents() is in the middle of its logic and has already passed this call.
        	handler.eventQueueEmpty();
        }
    }
    
    /**
     * Add an event to the queue to be processed in FIFO order.
     * 
     * @param event the event to add to the queue
     */
    public void addEvent(ScenarioEvent event){
    	
    	if(event == null){
    		throw new IllegalArgumentException("The event can't be null.");
    	}
    	
        //place event in queue and notify listener
        synchronized (scenarioEventList) {
            if (running) {
                scenarioEventList.add(event);
                scenarioEventList.notifyAll();
            }
        }
    }
    
    /**
     * Return whether there are any events to be handled still in the queue
     * 
     * @return boolean
     */
    public boolean isEventQueueEmpty(){
        return scenarioEventList.isEmpty();
    }
    
    /**
     * This is the threaded method used to pull events from the queue and call
     * the appropriate handler methods.
     */
    private void processEvents(){
    	ScenarioEvent event = null;
        do {

	    	synchronized (scenarioEventList) {
	            event = scenarioEventList.isEmpty() ? null : scenarioEventList.remove(0);
	        }
	
	        if(event != null){
	        	try{
	        	
		        	if(event instanceof ConceptStartedEvent){
		        		handler.handleConceptStarted((ConceptStartedEvent) event);
		        	}else if(event instanceof ConceptEndedEvent){
		        		handler.handleConceptEnded((ConceptEndedEvent) event);
		        	}else if(event instanceof ConceptAssessmentEvent){
		        		handler.handleConceptAssessment((ConceptAssessmentEvent) event);
		        	}else if(event instanceof TaskStartedEvent){
		        		handler.handleTaskStarted((TaskStartedEvent) event);
		        	}else if(event instanceof TaskEndedEvent){
		        		handler.handleTaskEnded((TaskEndedEvent) event);
		        	}else if(event instanceof TaskAssessmentEvent){
		        		handler.handleTaskAssessment((TaskAssessmentEvent) event);
		        	}else{
		        		logger.error("Received unhandled event of "+event+".");
		        	}
		        	
	        	}catch(Exception e){
	        		logger.error("Caught exception from misbehaving trigger event handler named "+handlerName+" - "+handler, e);
	        	}
	        }

			synchronized (scenarioEventList) {

                /* If the event manager is still running and there are no items
                 * left to process, signal to the handler that there are no more
                 * events and wait to be notified of more events */
                if (running && scenarioEventList.isEmpty()) {
					handler.eventQueueEmpty();

					try {
						//wait until notified of a new event or when shutting down
						scenarioEventList.wait();

					} catch (@SuppressWarnings("unused") InterruptedException e) {
					}
				}
			}
	        
        }while(running || !scenarioEventList.isEmpty());
    }
    
    /*******************************   Event Types *********************************/

    /**
     * Details about a concept ending
     * 
     * @author mhoffman
     *
     */
	public static class ConceptEndedEvent extends ConceptEvent{
		
		public ConceptEndedEvent(Task ancestorTask, Concept concept){
			super(ancestorTask, concept);

		}
	}
	
	/**
	 * Details about a concept starting
	 * 
	 * @author mhoffman
	 *
	 */
	public static class ConceptStartedEvent extends ConceptEvent{
		
		public ConceptStartedEvent(Task ancestorTask, Concept concept){
			super(ancestorTask, concept);

		}
	}
	
	/**
	 * Details about a concept assessment
	 * 
	 * @author mhoffman
	 *
	 */
	public static class ConceptAssessmentEvent extends ConceptEvent{
		
		public ConceptAssessmentEvent(Task ancestorTask, Concept concept){
			super(ancestorTask, concept);

		}
	}
	
	/**
	 * Details about a task starting
	 * 
	 * @author mhoffman
	 *
	 */
	public static class TaskStartedEvent extends TaskEvent{
		
		public TaskStartedEvent(Task task){
			super(task);
		}
	}
	
	/**
	 * Details about a task ending
	 * 
	 * @author mhoffman
	 *
	 */
	public static class TaskEndedEvent extends TaskEvent{
		
		public TaskEndedEvent(Task task){
			super(task);
		}
	}
	
	/**
	 * Details about a task assessment
	 * 
	 * @author mhoffman
	 *
	 */
	public static class TaskAssessmentEvent extends TaskEvent{
		
		/** contains a proxy of the task's assessment information */
		private ProxyTaskAssessment taskAssessment;
		
		/** the enumerated event that caused this task assessment event */
		private AssessmentUpdateEventType eventType;
		
		/**
		 * Set attributes
		 * @param task the task with a new assessment, can't be null
		 * @param taskAssessment the proxy to the task assessment, can't be null
		 * @param eventType the enumerated event that caused this task assessment event, can't be null
		 */
		public TaskAssessmentEvent(Task task, ProxyTaskAssessment taskAssessment, AssessmentUpdateEventType eventType){
			super(task);

			if(taskAssessment == null){
				throw new IllegalArgumentException("The task assessment proxy can't be null.");
			}
			
			this.taskAssessment = taskAssessment;
			
			if(eventType == null) {
			    throw new IllegalArgumentException("The event type is null");
			}
			
			this.eventType = eventType;
		}
		
		/**
		 * Return the proxy of the task's assessment information
		 * 
		 * @return won't be null
		 */
		public ProxyTaskAssessment getTaskAssessment(){
			return taskAssessment;
		}
		
		/**
		 * Return the enumerated event that caused this task assessment event
		 * @return won't be null
		 */
		public AssessmentUpdateEventType getEventType() {
		    return eventType;
		}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[TaskAssessmentEvent: taskAssessment=");
            builder.append(taskAssessment);
            builder.append(", eventType=");
            builder.append(eventType);
            builder.append("]");
            return builder.toString();
        }

	}
	
	/**
	 * The base class for task event types
	 * 
	 * @author mhoffman
	 *
	 */
	public abstract static class TaskEvent implements ScenarioEvent{
		
	    /** the task reporting an event */
		private Task task;
		
		/**
		 * Set the task reporting an event
		 * @param task can't be null
		 */
		public TaskEvent(Task task){
			
			if(task == null){
				throw new IllegalArgumentException("The task can't be null.");
			}
			
			this.task = task;
		}
		
		/**
		 * Return the task reporting an event
		 * 
		 * @return won't be null
		 */
		public Task getTask(){
			return task;
		}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[TaskEvent: task=");
            builder.append(task);
            builder.append("]");
            return builder.toString();
        }

	}
	
	/**
	 * The base class for concept event types
	 * 
	 * @author mhoffman
	 *
	 */
	public abstract static class ConceptEvent implements ScenarioEvent{
		
	    /** the task which this concent is a descendant of */
		private Task ancestorTask;
		
		/** the concept causing this event */
		private Concept concept;
		
		/**
		 * Set attributes
		 * @param ancestorTask the task which this concent is a descendant of, can't be null
		 * @param concept the concept causing this event, can't be null
		 */
		public ConceptEvent(Task ancestorTask, Concept concept){
			
			if(ancestorTask == null){
				throw new IllegalArgumentException("The ancestor task can't be null.");
			}else if(concept == null){
				throw new IllegalArgumentException("The concept can't be null.");
			}
			
			this.ancestorTask = ancestorTask;
			this.concept = concept;
		}
		
		/**
		 * Return the task that is an ancestor to the concept reporting an event
		 * 
		 * @return won't be null
		 */
		public Task getAncestorTask(){
			return ancestorTask;
		}
		
		/**
		 * Return the concept reporting an event
		 * 
		 * @return won't be null
		 */
		public Concept getConcept(){
			return concept;
		}

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ConceptEvent: ancestorTask=");
            builder.append(ancestorTask);
            builder.append(", concept=");
            builder.append(concept);
            builder.append("]");
            return builder.toString();
        }
		
	}
	
	/**
	 * The common interface for all trigger events handled by this class.
	 * 
	 * @author mhoffman
	 *
	 */
	public interface ScenarioEvent{
		
	}
	
	/**
	 * The various causes of a performance assessment being updated
	 * @author mhoffman
	 *
	 */
	public enum AssessmentUpdateEventType{
	    CONCEPT_CREATED,           // concept constructor was called, normally when loading the DKF
	    CONCEPT_INITIALIZED,       // concept was initialized, normally when loading the DKF
	    CONDITION_SYNC_UPDATED,    // concept's condition assessment was updated as a result of delivering the current training app message
	    CONDITION_ASYNC_UPDATED,   // concept's condition assessment was updated not during the delivery of a training app message
	    TASK_ACTIVATED,            // task was activated, by start trigger logic or manually (e.g. using game master)
	    TASK_INITIALIZED,          // task was initialized, normally when loading the DKF
	    CONCEPT_SYNC_UPDATED,      // concept assessment was updated as a result of delivering the current training app message
	    CONCEPT_ASYNC_UPDATED,     // concept assessment was updated not during the delivery of a training app message
	    SURVEY_ASSESSMENT,         // a mid-lesson survey changed a task/concept assessment - NOTE: Ideally this wouldn't be here, only task/concept/condition events
	    TASK_EVALUATOR_UPDATE,     // the observer provided a manual evaluation update on the task, e.g. using game master
	    CONCEPT_EVALUATOR_UPDATE,  // the observer provided a manual evaluation update on the concept, e.g. using game master
	    STRATEGY_APPLIES_TO_TASK   // an applied strategy effected the task, e.g. updated the task stress level
	}
	
	/**
	 * The interface used to handle de-queued events.
	 * 
	 * @author mhoffman
	 *
	 */
	public interface TriggerEventHandler{
		
		/**
		 * Notification that a concept has started
		 * 
		 * @param event the concept started event information
		 */
		public void handleConceptStarted(ConceptStartedEvent event);
		
		/**
		 * Notification that a concept has ended
		 * 
		 * @param event the concept ended event information
		 */
		public void handleConceptEnded(ConceptEndedEvent event);
		
		/**
		 * Notification that a concept has a new assessment
		 * 
		 * @param event the concept assessment event information
		 */
		public void handleConceptAssessment(ConceptAssessmentEvent event);
		
		/**
		 * Notification that a task has started
		 * 
		 * @param event the task started event information
		 */
		public void handleTaskStarted(TaskStartedEvent event);
		
		/**
		 * Notification that a task has ended
		 * 
		 * @param event the task ended event information
		 */
		public void handleTaskEnded(TaskEndedEvent event);
		
		/**
		 * Notification that a task has a new assessment
		 * 
		 * @param event the task assessment event information
		 */
		public void handleTaskAssessment(TaskAssessmentEvent event);
		
		/**
		 * Notification that the event queue is empty
		 */
		public void eventQueueEmpty();
	}
}
