/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.monitor.PanelManager.ModuleListModel;

/**
 * Panel that graphically depicts messages flowing between modules during 
 * a User Session(i.e. messages that are unique to the time when a User is logged in).
 * 
 * @author cragusa
 *
 */
public class MessageAnimationPanel extends JPanel implements ListDataListener, ListSelectionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final int MIN_PANEL_WIDTH  = 1024;
	private static final int MIN_PANEL_HEIGHT =  480;	
	
	private static final int MAX_PANEL_WIDTH = 2 * MIN_PANEL_WIDTH;
    private static final int MAX_PANEL_HEIGHT= 2 * MIN_PANEL_HEIGHT;
	
	private int panelWidth  = MIN_PANEL_WIDTH;
	private int panelHeight = MIN_PANEL_HEIGHT;
	
	private static final int TOP_MARGIN     =   50;
	private static final int LEFT_MARGIN    =   50;
		
	//TODO: make this settable in properties
	private static final int MESSAGE_GRAPHIC_TIMEOUT_MILLIS = 100;
		
	private ModuleListModel moduleListModel;
	private MessageListModel messageListModel;
	
	private final Map<String, ModuleGraphic> queueNameToModuleGraphic = new HashMap<>();
	
	private boolean manualMode = false;
	
	
	boolean isManualMode() {
	    
	    return manualMode;
	}
	
	void setManualMode(boolean enabled) {
	    
	    manualMode = enabled;	        
        removeAnyMessages();
        recomputeModuleGraphics();
        scheduleRepaint();
	}
	
	
	/**
	 * Recomputes the graphics that respresent the modules.
	 */
	
	private void recomputeModuleGraphics() {
		
		final int count = moduleListModel.getSize();
		
		for(int i = 0; i < count; i++) {
			
			final int moduleX = (int)(.75*ModuleGraphic.WIDTH*i) + LEFT_MARGIN;
			
			if( (moduleX + ModuleGraphic.WIDTH) > panelWidth ) {
				
				panelWidth = moduleX + ModuleGraphic.WIDTH + 20;
			}
			
			final int moduleY = TOP_MARGIN + (i%2)* 2*ModuleGraphic.HEIGHT;
			
			final String queueName = moduleListModel.getElementAt(i);
			
			ModuleGraphic moduleGraphic;
			
			if(!queueNameToModuleGraphic.containsKey(queueName)) {
				
				moduleGraphic = new ModuleGraphic(queueName);
				
				queueNameToModuleGraphic.put(queueName, moduleGraphic);				
			}
			else {
				
				moduleGraphic = queueNameToModuleGraphic.get(queueName);
			}
			
			moduleGraphic.setDrawOrigin(moduleX, moduleY);
		}		
	}
	
	
	/**
	 * Removes a message from the animation so it won't be drawn.
	 * 
	 * @param msg the message to remove.
	 */
	private void removeMessageFromScene(final Message msg) {
		
		final String senderQueueName  = msg.getSenderAddress();
		queueNameToModuleGraphic.get(senderQueueName).removeFromMessage(msg);
		
		final String destQueueName = msg.getDestinationQueueName();
		queueNameToModuleGraphic.get(destQueueName).removeToMessage(msg);		
				
		this.repaint();
	}
	
	/**
	 * Schedules a message to be removed at some time in the future.
	 * 
	 * @param msg the message to schedule for removal.
	 */
	private void scheduleMessageRemoval(final Message msg) {
		
		final Timer timer = new Timer(MESSAGE_GRAPHIC_TIMEOUT_MILLIS, new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent event) {
				removeMessageFromScene(msg);
			}
		});		
		timer.setRepeats(false);
		timer.start();		
	}
	

	@Override
	public void contentsChanged(final ListDataEvent event) {
		
		if(event.getSource() == moduleListModel) {			
			recomputeModuleGraphics();
			scheduleRepaint();		
		}
		else if (event.getSource() == messageListModel ) {
			//probably don't care about this 
		}
	}
	
	
	/**
	 * Adds a message to the animation.
	 * 
	 * @param msg the message to be added.
	 */
	private void addMessageToAnimation(Message msg) {
	    
	    String senderAddress = msg.getSenderAddress();
	    
	    //TODO: Need to go back and find out why these null checks are necessary.
	    //      When code was first written, I didn' think nulls were possible. -LCR
	    if(senderAddress != null) {
	        
	        ModuleGraphic graphic = queueNameToModuleGraphic.get(senderAddress);
	        
	        if(graphic != null) {
	            
	            graphic.addFromMessage(msg);
	        }
	    }
	    
		queueNameToModuleGraphic.get(msg.getDestinationQueueName()).addToMessage(msg);
	}
	
	
	@Override
	public void intervalAdded(final ListDataEvent event) {
	    		
		if(event.getSource() == moduleListModel) {

		    recomputeModuleGraphics();
		}
		else if (event.getSource() == messageListModel) {
		    
			final int firstIndex = event.getIndex0();
			final int lastIndex  = event.getIndex1();
			
			for(int i = firstIndex; i <= lastIndex; i++) {
			
				final Object obj = messageListModel.getElementAt(i);
				
				if(obj instanceof Message) {
					
				    if( !manualMode ) {
				        
				        final Message msg = (Message)obj;					
					    addMessageToAnimation(msg);					
					    scheduleMessageRemoval(msg);
				    }
				}
			}
		}
		
		scheduleRepaint();
	}
	
	@Override
	public void intervalRemoved(final ListDataEvent event) {
		
		if(event.getSource() == moduleListModel) {
			
			recomputeModuleGraphics();
			scheduleRepaint();		
		}
		else if (event.getSource() == messageListModel) {			
			//don't care about this
		} 
	}
	
	private void scheduleRepaint() {
		
		SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
				MessageAnimationPanel.this.repaint();
			}
		});	
	}


	@Override
	public void paint(final Graphics g) {
	    
		 g.setColor(Color.WHITE);		 
		 g.fillRect(0, 0, MAX_PANEL_WIDTH, MAX_PANEL_HEIGHT);
		 
		 for( ModuleGraphic graphic : queueNameToModuleGraphic.values() ) {
			 graphic.paint(g);			
		 }	 
		 
		 g.setColor(Color.BLACK);
		 
		 //TODO: Factor this so it uses same (related) coded to ModuleGraphic
		 final int count = queueNameToModuleGraphic.size();	 
		 final int activeMqWidth = (int)((count-1)*.75*ModuleGraphic.WIDTH + ModuleGraphic.WIDTH);		 
		 g.drawRect(ModuleGraphic.MESSAGE_BUS_RECT_X,ModuleGraphic.MESSAGE_BUS_RECT_Y, activeMqWidth, ModuleGraphic.MESSAGE_BUS_RECT_HEIGHT );
		 
		 final String messageBusLabel = "Message Bus";
		 final int lengthOfLabelInPixels = 100; //TODO: figure out how to find this dynamically		 
		 final int drawLabelX = ModuleGraphic.MESSAGE_BUS_RECT_X + activeMqWidth/2 - lengthOfLabelInPixels/2;		 
		 g.drawString(messageBusLabel, drawLabelX, ModuleGraphic.MESSAGE_BUS_RECT_Y + ModuleGraphic.MESSAGE_BUS_RECT_HEIGHT - 10);
	}	
	
	/**
	 * Sets the ListModel to be used as the UserSessionModuleListModel.
	 * @param moduleListModel
	 */
	void setModuleListModel(ModuleListModel moduleListModel) {
		
		if(this.moduleListModel != null) {
			
			this.moduleListModel.removeListDataListener(this);
		}		
		this.moduleListModel = moduleListModel;	
		
		this.moduleListModel.addListDataListener(this);
		
		this.recomputeModuleGraphics();
		
		scheduleRepaint();
	}
	
	/**
	 * Sets the ListModel to be used as the UserSessionMessageListModel	
	 * @param messageListModel
	 */
	void setUserSessionMessageListModel(MessageListModel messageListModel) {
		
		if(this.messageListModel != null) {
			
			this.messageListModel.removeListDataListener(this);
		}		
		this.messageListModel = messageListModel;	
		
		this.messageListModel.addListDataListener(this);		
	}
	
	
	/**
	 * Removes any/all messages still being animated/drawn.
	 * 
	 */
	private void removeAnyMessages() {

	    final int count = moduleListModel.getSize();

	    for(int i = 0; i < count; i++) {

	        final String queueName = moduleListModel.getElementAt(i);

	        if(queueNameToModuleGraphic.containsKey(queueName)) {
	            
	            ModuleGraphic moduleGraphic = queueNameToModuleGraphic.get(queueName);
	            
	            moduleGraphic.removeAllMessages();
	        }	       
	    }     
	}
	
	
	@Override
    public Dimension getPreferredSize() {
    	return new Dimension(panelWidth, panelHeight);       
    }

	
	@Override
	public void valueChanged(ListSelectionEvent event) {

	    if( !event.getValueIsAdjusting() ) {

            @SuppressWarnings("unchecked")
            JList<Message> source = (JList<Message>) event.getSource();

	        int selectedIndex = source.getSelectedIndex();		    
	        source.getModel().getSize();

	        if(selectedIndex < source.getModel().getSize() ) {

	            final Message msg = source.getSelectedValue();         

	            if(msg != null && manualMode) {
	                
	                removeAnyMessages();
	                addMessageToAnimation(msg);  
	                this.recomputeModuleGraphics();
	                //scheduleMessageRemoval(msg);
	                this.scheduleRepaint();
	            }
	            else {	//msg is null                
	                
	                //Unusual keystrokes can cause this -- e.g. "CTRL-A" after having selected a row
	                //I think it's okay just to ignore these things - LCR
	            }
	        }
	        else {
	            
	            //LCR: This appears to happen if user has selected an entry with a high index, and then the filter
	            // is changed so that the selected index is no longer valid (i.e. it's beyond the last value in the underlying model).
	            // A better fix than this if-statement would be to reset the selected index/value of the jlist. 
	        }
	    }
	}	    
}

/**
 * Encapsulates the information required to paint a Module.  This includes 
 * painting the messages to and from the module (i.e. to/from the MessageBus).
 * @author cragusa
 *
 */
class ModuleGraphic {
    
	/** map of message type to color */
    private static final Map<MessageTypeEnum, Color> typeToColorMap = new HashMap<>();
    
    /** boolean constant identifying the direction (up/down) of an arrow */
    private static final boolean UP_ARROW   = true;
        
    /** boolean constant identifying the direction (up/down) of an arrow */
    private static final boolean DOWN_ARROW = false;
    
    /** X-Offset of the MessageBus rectangle origin */
    static final int MESSAGE_BUS_RECT_X      =   50;
    
    /** Y-offset of the MessageBus rectangle origin */
    static final int MESSAGE_BUS_RECT_Y      =  300;
    
    /** Height of MessageBus rectangle - note that the width changes to accommodate all modules */
    static final int MESSAGE_BUS_RECT_HEIGHT =   30;
    
    /** Width of a module graphic rectangle. TODO: Later make this dynamic based on displayText */
    static final int WIDTH         = 180;
    
    /** Height of module graphic rectangle */
    static final int HEIGHT        =  25;
    
    /**Indentation for display label*/
    static final int TEXT_INDENT   =   3;
    
    /**Pixels up from rectangle bottom - used to position text vertically in the rectangle */
    static final int BOTTOM_MARGIN =   7;
    
    /** The text to use for a module graphic (essentialy the label) */
    private String displayText;
    
    /** the x-coordinate describing where to draw the Module Graphic */
    private int    x;
    
    /** the y-coordinate describing where to draw the Module Graphic */
    private int    y;   
    
    /** List of messages flowing to this module graphic */
    private final List<Message> toMsgList   = new ArrayList<>();
    
    /** List of messages flowing away from this module graphic */
    private final List<Message> fromMsgList = new ArrayList<>();

    /**
     * Constructs a new ModuleGraphic.
     * @param queueName name of the Queue - used as label for the graphic
     */
    ModuleGraphic(final String queueName) {
        
        int suffixIndex = queueName.indexOf("Inbox");
        
        if(suffixIndex > 0) {
            
            displayText = queueName.substring(0, suffixIndex - 1);
        }
        else {
            displayText = queueName;
        }
    }   
    
    /**
     * Standard paint call.
     * @param g
     */
    void paint(final Graphics g) {      
        g.setColor(Color.BLACK);    
        g.drawRect(x, y, WIDTH, HEIGHT);
        g.drawString(displayText, x + TEXT_INDENT , y + HEIGHT - BOTTOM_MARGIN);
        paintMessages(g);       
    }
    
    
    /**
     * Creates a map of message type to color.
     * 
     */
    private static void initializeMessageTypeToColorMap() {
        
        final Color[] colors = new Color[27];
        int colorIndex = 0;
        
        final int START = 50;
        final int STEP  = 75;
        final int STOP  = START + 2*STEP;       
        
        for(int r = START; r<=STOP; r+=STEP) {
            for (int g = START; g<=STOP; g+=STEP) {
                for (int b = START; b<=STOP; b+=STEP) {
                    colors[colorIndex++] = new Color(r,g,b);
                }   
            }
        }

        colorIndex = 0;
        for(MessageTypeEnum type:  MessageTypeEnum.VALUES()) {
            colorIndex = (colorIndex + 1) % colors.length;
            typeToColorMap.put(type, colors[colorIndex]);
        }
        
    }
    
    static {        
        initializeMessageTypeToColorMap();
    }
    
    
    /**
     * Get the color (used for painting) based on a message type.
     * @param type the type of message for which a color is needed
     * @return the color for the provided message type
     */
    private static Color getColorForType(final MessageTypeEnum type) {
        return typeToColorMap.get(type);
    }
    
    
    /**
     * Paints an arrow tip.
     *     
     * @param g 
     * @param tipX x-dimension in pixels of the arrow tip
     * @param tipY y-dimension in pixels of the arrow tip
     * @param up indicates whether to draw an up arrow or a down arrow
     */
    private void paintArrow(final Graphics g, 
                            final int tipX,
                            final int tipY,
                            final boolean up) {
        
        final int dy = up ? 10 : -10;
        
        g.drawLine(tipX, tipY, tipX-5, tipY + dy );   //left side
        g.drawLine(tipX, tipY, tipX+5, tipY + dy );   //right side
    }
    
    
    /**
     * Paint the messages
     * 
     * @param g the Graphics object to use for painting.
     */
    private void paintMessages(final Graphics g) {
        
        final int centerX = x + WIDTH/2;
        final int bottomY = y + HEIGHT;
        
        int count = 0;
        
        for(Message msg : toMsgList) {
            g.setColor(getColorForType(msg.getMessageType()));          
            g.drawLine(centerX-7, bottomY, centerX-7, MESSAGE_BUS_RECT_Y);
            g.drawString(msg.getSequenceNumber()+"", centerX - 50, bottomY + 20 + 20*count++);          
            paintArrow(g, centerX-7, bottomY, UP_ARROW);
        }

        count = 0;
        for(Message msg : fromMsgList) {
            g.setColor(getColorForType(msg.getMessageType()));          
            g.drawLine(centerX + 7, bottomY, centerX+7, MESSAGE_BUS_RECT_Y);
            g.drawString(msg.getSequenceNumber()+"", centerX+20, bottomY+20 + 20*count++);
            paintArrow(g, centerX+7, MESSAGE_BUS_RECT_Y, DOWN_ARROW);
        }
    }   
    
    /**
     * Top left corner of the Module graphic rectangle in local (panel) coordinates.
     * @param x
     * @param y
     */
    void setDrawOrigin(final int x, final int y) {      
        this.x = x;
        this.y = y;
    }
    
    
    /**
     * Removes messages to from this particular module graphic so they won't be drawn.
     */
    void removeAllMessages() {
        
        toMsgList.clear();
        fromMsgList.clear();
    }
    
    
    /**
     * Adds an AbstractUserSessionMessage to be drawn as a message <i>to</i> this module.
     * @param msg
     */
    void addToMessage(final Message msg) {
        toMsgList.add(msg);
    }
    
    /**
     * Removes an AbstractUserSessionMessage from the <i>to</i> message list.
     * @param msg
     */
    void removeToMessage(final Message msg) {
        toMsgList.remove(msg);
    }
    
    /**
     * Adds an AbstractUserSessionMessage to be drawn as a message <i>from</i> this module.
     * @param msg
     */
    void addFromMessage(final Message msg) {
        fromMsgList.add(msg);
    }
    
    /**
     * Removes an AbstractUserSessionMessage from the <i>from</i> message list.
     * @param msg
     */ 
    void removeFromMessage(final Message msg) {
        fromMsgList.remove(msg);
    }
}