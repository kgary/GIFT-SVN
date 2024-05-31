/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.List;
import javax.swing.JTabbedPane;
import mil.arl.gift.tools.monitor.MessageStatsModel.MessageStatistics;

/**
 * A container for displaying the message animation panel and statistics
 *
 * @author jleonard
 */
public class MessagePanelContainer extends JTabbedPane implements MessageStatsModel.MessageStatisticsUpdateListener {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * A panel to display message statistics.
     */
    private final MessageStatsPanel messsageStatistics = new MessageStatsPanel();
        
    /**
     * Constructor for the panel.
     * 
     * @param messageViewPanel the panel for this container
     */
    public MessagePanelContainer(MessageViewPanel messageViewPanel) {
        this.add("Message Flow", messageViewPanel);
        this.add("Statistics", messsageStatistics);
    }

    @Override
    public void onMessageStatisticsUpdate(List<MessageStatistics> messageStatistics) {
        messsageStatistics.onMessageStatisticsUpdate(messageStatistics);
    }
}
