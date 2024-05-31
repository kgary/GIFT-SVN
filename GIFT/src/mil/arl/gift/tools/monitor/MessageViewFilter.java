/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.net.api.message.Message;

/**
 * Used to filter messages in a view.
 * TODO: Maybe this should just be MessageFilter?
 * (it could be used for both view and capture purposes) 
 * @author cragusa
 *
 */
public abstract class MessageViewFilter implements ListModel<MessageTypeEnum>, ListSelectionListener, MessageFilter {
    
    private static Logger logger = LoggerFactory.getLogger(MessageViewFilter.class);

	/** 
	 * A list of all possible filter selections
	 */
	protected final DefaultListModel<MessageTypeEnum> rawList = new DefaultListModel<>();
	
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
	 * No-Arg constructor
	 */
	public MessageViewFilter(){

	}
	
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
	public void valueChanged(final ListSelectionEvent event) {
		
		if( !event.getValueIsAdjusting()) {
			
            @SuppressWarnings("unchecked")
			final JList<MessageTypeEnum> jlist = (JList<MessageTypeEnum>)(event.getSource());
			
			acceptedTypes.clear();
						
			for(Object selected : jlist.getSelectedValuesList()) {
				
				acceptedTypes.add((MessageTypeEnum)selected);
			}
			
			synchronized(filterChangeListeners) {
				
				for( FilterChangeListener listener : filterChangeListeners ) {

				    try{
				        //TODO: can I share a single event across all listeners? 
				        listener.filterChanged(new FilterChangeEvent(this));
	                }catch(Exception e){
	                    logger.error("Caught exception from misbehaving listener "+listener, e);
	                }
				}
			}
		} 
	}
	
	@Override
	public void addListDataListener(ListDataListener l) {
		rawList.addListDataListener(l);
	}

	@Override
	public MessageTypeEnum getElementAt(int index) {
		return rawList.get(index);
	}

	@Override
	public int getSize() {
		return rawList.getSize();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		rawList.removeListDataListener(l);
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
}
