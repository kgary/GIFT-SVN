/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.Message;

/**
 * Used to filter messages in a view.
 * (it could be used for both view and capture purposes) 
 * 
 * @author nroberts
 */
public abstract class MessageViewFilter implements MessageFilter {
    
    private static Logger logger = LoggerFactory.getLogger(MessageViewFilter.class);

	/** 
	 * A list of all possible filter selections
	 */
	protected final List<MessageTypeEnum> rawList = new ArrayList<>();
	
	/**
	 * Set of accepted message types.
	 */
	private final Set<MessageTypeEnum> acceptedTypes = new HashSet<>();
	
	/**
	 * (optional) an entity state message entity marking to filter entity state messages on
	 */
	private String entityStateURNMarkingFilter = null;
	
	/** 
	 * List of filter listeners.
	 */
	private final List<FilterChangeListener> filterChangeListeners = new ArrayList<>();
	
	/**
	 * Set the entity marking to filter on.  Can be null or empty.
	 * 
	 * @param entityStateURNMarkingFilter the entity marking to filter entity state messages on
	 */
	public void setEntityStateURNMarkingFilter(String entityStateURNMarkingFilter){
	    this.entityStateURNMarkingFilter = entityStateURNMarkingFilter;
	    
	    // notify the listeners so they can redraw if needed
        synchronized(filterChangeListeners) {
            
            for( FilterChangeListener listener : filterChangeListeners ) {

                try{
                    listener.filterChanged(new FilterChangeEvent(this));
                }catch(Exception e){
                    logger.error("Caught exception from misbehaving listener "+listener, e);
                }
            }
        }
	}
		
	@Override
    public boolean acceptMessage(Message msg) {
		
	    boolean isAcceptedType = acceptedTypes.contains(msg.getMessageType());
	    if(isAcceptedType){
	        // apply sub message type filtering here
	        if(StringUtils.isNotBlank(entityStateURNMarkingFilter) && msg.getMessageType() == MessageTypeEnum.ENTITY_STATE){
	            //the entity state entity marking filter has been provided, so check it
	            
	            EntityState entityState = (EntityState)msg.getPayload();
	            return entityStateURNMarkingFilter.equalsIgnoreCase(entityState.getEntityMarking().getEntityMarking());
	        }
	    }
	    
	    return isAcceptedType;
	}
	
	@Override
	public synchronized void addFilterChangeListener(FilterChangeListener l) {		
		if(!filterChangeListeners.contains(l)) {
			filterChangeListeners.add(l);
		}
	}

	@Override
	public synchronized void removeFilterChangeListener(FilterChangeListener l) {		
		if(filterChangeListeners.contains(l)) {
			filterChangeListeners.remove(l);		
		}
	}
	
	/**
	 * Adds a message type as a new choice for the filter
	 * 
	 * @param choice the message type choice to add. Cannot be null.
	 */
	protected void addChoice(MessageTypeEnum choice) {
	    
	    rawList.add(choice);
	    
        /* Select the new message type by default */
	    acceptedTypes.add(choice);
	    
	    onFilterChoicesChanged();
	}
	
	/**
	 * Select all available message types so that all messages are displayed
	 */
	public void acceptAll() {
	    acceptedTypes.addAll(rawList);
	    
	    onFilterChoicesChanged();
	}
	
	/**
	 * Notifies listeners that the list of available filter choices has changed
	 */
	protected void onFilterChoicesChanged() {
	    
	    synchronized(filterChangeListeners) {
            
            for( FilterChangeListener listener : filterChangeListeners ) {

                try{
                    listener.filterChoicesChanged(rawList, new ArrayList<>(acceptedTypes));
                }catch(Exception e){
                    logger.error("Caught exception from misbehaving listener "+listener, e);
                }
            }
        }
	}

	/**
	 * Sets the message types that should be displayed by the filter
	 * 
	 * @param selectedChoices the selected message type choices. Cannot be null.
	 */
    public void setFilterChoices(Set<MessageTypeEnum> selectedChoices) {
        acceptedTypes.clear();
        acceptedTypes.addAll(selectedChoices);
        
        // notify the listeners so they can redraw if needed
        synchronized(filterChangeListeners) {
            
            for( FilterChangeListener listener : filterChangeListeners ) {

                try{
                    listener.filterChanged(new FilterChangeEvent(this));
                }catch(Exception e){
                    logger.error("Caught exception from misbehaving listener "+listener, e);
                }
            }
        }
        
        onFilterChoicesChanged();
    }
}
