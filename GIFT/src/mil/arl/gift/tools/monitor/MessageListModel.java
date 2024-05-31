/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import mil.arl.gift.net.api.message.Message;

/**
 * Maintains a list of Messages.
 * @author cragusa
 */
public class MessageListModel extends AbstractListModel<Message> implements FilterChangeListener {
    
    /**  max number of messages to store before removing the oldest messages */
	public static final int MESSAGE_LIST_MAX_ELEMENTS = MonitorModuleProperties.getInstance().getMessageDisplayBufferSize();
		
	private static final long serialVersionUID = 1L;
	
	/** container for all messages */
	private final List<Message> masterList  = new LinkedList<>();
	
	/** container for all messages that are able to be displayed (i.e. available for selection) */
	private final List<Message> displayList = new ArrayList<>(); 
	
	private MessageFilter messageDisplayFilter;
	
	private boolean listening = true;
	
	/**
	 * Used to ascertain whether this is listening or not listening.
	 * 
	 * @return
	 */
	private boolean isListening() {
		
		return listening;
	}
	
	/**
	 * Sets the listening state of this MessageListModel.
	 * 
	 * @param listening boolean indicating listening or not listening.
	 * 
	 */
	void setListening(boolean listening) {
		
		this.listening = listening;
	}
	
	/**
	 * No-arg constructor.
	 */
	public MessageListModel(){

	}
	
	/**
	 * Check message against filter (convenience methods does null check).
	 * @param msg the message to check against the filter
	 * @return returns true if the message passes the filter -OR- if the filter is null.
	 */
    private boolean acceptMessage(Message msg) {
		
		if(messageDisplayFilter != null) {
		
			return messageDisplayFilter.acceptMessage(msg);
		}
		
        return true;
	}
	
    /**
     * Clear the available messages list and re-populate it with the current
     * list of available messages for selection.
     */
	private void rebuildDisplayList() {
	    		
		displayList.clear();
		
		for(Message msg : masterList) {
			
			if(acceptMessage(msg) ) {
				
				displayList.add(msg);
			}
		}
		
		this.fireContentsChanged(this, 0, displayList.size() - 1);
	}
		
	/**
	 * Used by clients to post a new message into the model.
	 * @param msg the message to post.
	 */
    void postMessage(final Message msg) {
	    
    	if(this.isListening()) {
    	
	    	Runnable runnable = new Runnable() {
	    	    
	            @Override
	    	    public void run() {            	
	    	    
	        		//assume incoming messages are for the current users
	        		while( masterList.size() >= MESSAGE_LIST_MAX_ELEMENTS ) {
	        		    
	        			masterList.remove(0);
	        		}
	        		
	        		masterList.add(msg);
	        		
	        		while( displayList.size() >= MESSAGE_LIST_MAX_ELEMENTS) {
	        		    
	        		    displayList.remove(0);
	        		    fireIntervalRemoved(MessageListModel.this, 0, 0);
	        		}
	        			
	        	   if( acceptMessage(msg) ) {
	        	       
	        			displayList.add(msg);
	        			fireIntervalAdded(MessageListModel.this, displayList.size() - 1, displayList.size() - 1 );			
	        		}
	    	    }
		    };
		    
		    SwingUtilities.invokeLater(runnable);
    	}
	}
	
	@Override
	public Message getElementAt(final int index) {
	        
	    return displayList.get(index);	
	   
	}

	@Override
	public int getSize() {
	    
		return displayList.size();
	}
	
	@Override
	public void filterChanged(final FilterChangeEvent event) {
		
		//TODO: check source? maybe there's a whole new filter?
		//for now assume the existing filter just changed		
		rebuildDisplayList();
	}

	/**
	 * Sets the display filter for this list model. 
	 * TODO: Allow multiple filters?
	 * TODO: Add support for a capture filter?
	 * @param filter
	 */
	void setMessageDisplayFilter(MessageFilter filter) {
		messageDisplayFilter = filter;
		filter.addFilterChangeListener(this);
		rebuildDisplayList();
	}	
	
	/**
	 * Gets the container for all messages that are able to be displayed.
	 * @return displayList;
	 */
	public List<Message> getDisplayList(){
		return displayList;
	}
}
