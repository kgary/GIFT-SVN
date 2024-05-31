/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.widgets;

import org.gwtbootstrap3.client.ui.Panel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.DisplayMessageWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * A widget used to display text in the footer of the document
 *
 * @author bzahid
 */
public class DisplayFooterWidget extends Composite implements RequiresResize {

    private Panel containerPanel = new Panel();

    private final HTML contentWidget = new HTML();

    private SimplePanel mediaScrollPanel;
    
    boolean contentFillScrollPanel = false;
    
    /**
     * Constructor
     *
     * @param instance The instance of the display text widget
     */
    public DisplayFooterWidget(WidgetInstance instance) {
        WidgetProperties properties = instance.getWidgetProperties();
        initWidget(containerPanel);

        if (properties != null) {

            DisplayMessageTutorRequest parameters = DisplayMessageWidgetProperties.getParameters(properties);
            
            if (parameters.getMessage() != null) {

                String messageHtml = "<p style=\"font-size:15px;text-align:center;\">" + parameters.getMessage() + "</p>";
                contentWidget.setHTML(messageHtml);
                mediaScrollPanel = new ScrollPanel();
                mediaScrollPanel.add(contentWidget);
                mediaScrollPanel.setSize("100%", "100%");
                containerPanel.add(mediaScrollPanel);
                containerPanel.setStyleName("footerPanel");
                
            } 
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        onResize();
    }

    @Override
    public void onResize() {
        
        Widget parent = this.getParent();
        
        if (parent != null && contentFillScrollPanel) {
            
            int widgetHeight = parent.getOffsetHeight();
            containerPanel.setHeight(widgetHeight + "px");
                       
            int mediaPanelHeight = widgetHeight;

            if (mediaScrollPanel.isAttached()) {
                mediaPanelHeight -= 27;
            }
            
            if(mediaPanelHeight < 200) {
                
                mediaPanelHeight = 200;
            }
            
            mediaScrollPanel.setHeight(mediaPanelHeight + "px");
            contentWidget.setHeight((mediaScrollPanel.getOffsetHeight()) + "px");
            containerPanel.setHeight((mediaPanelHeight+ 10) + "px");
                
        }
    }
    
    @Override
    public int getOffsetHeight() {
        return contentWidget.getOffsetHeight() + 30;
    }
}
