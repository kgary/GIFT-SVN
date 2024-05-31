/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;


import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;

public class OkayLaterDialog extends ModalDialogBox{

    /** The callback. */
    private OkayCancelCallback callback;
    
    /** The html. */
    private HTML html = new HTML();
    
    /** The widget. */
    private Widget widget;
    
    /** The flow panel. */
    private FlowPanel flowPanel = new FlowPanel();
    
    /** The confirm button */
    private Button confirmButton;
    
    /** The cancel button */
    private Button cancelButton;
    
    public OkayLaterDialog(String title, String msgHtml, Widget widget, String confirm, String cancel, OkayCancelCallback callback) {
        setGlassEnabled(true);
        setText(title);
        this.callback = callback;
        
        confirmButton = new Button((confirm == null || confirm.isEmpty()) ? "Okay" : confirm);
        confirmButton.setType(ButtonType.PRIMARY);
        confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getCallback().okay();
                hide();
            }
        });
        
        
        cancelButton = new Button((cancel == null || cancel.isEmpty()) ? "Later" : cancel);
        cancelButton.setType(ButtonType.DANGER);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getCallback().cancel();
                hide();
            }
        });
        
        html.setHTML(msgHtml);
        setMyWidget(widget);
        
        VerticalPanel vPanel = new VerticalPanel();
        
        vPanel.getElement().getStyle().setProperty("maxWidth", "700px");
        vPanel.getElement().getStyle().setProperty("padding", "10px 10px 0px 10px");
        vPanel.setSpacing(10);
        
        vPanel.add(html);
        vPanel.add(flowPanel);
        
        FlowPanel footerPanel = new FlowPanel();
        footerPanel.add(confirmButton);
        footerPanel.add(cancelButton);
        
        footerPanel.getElement().getStyle().setProperty("display", "inline");
        
        setFooterWidget(footerPanel);
        setEnterButton(confirmButton);      
        
        setWidget(vPanel);
        
    }
    
    
    /**
     * Gets the callback.
     *
     * @return the callback
     */
    private OkayCancelCallback getCallback() {
        return callback;
    }
    
    /**
     * Sets the my widget.
     *
     * @param widget the new my widget
     */
    private void setMyWidget(Widget widget) {
        
        if(this.widget != null) {
            flowPanel.remove(this.widget);
            this.widget = null;
        }
        
        if(widget != null) {
            this.widget = widget;
            flowPanel.add(widget);
        }
    }
}
