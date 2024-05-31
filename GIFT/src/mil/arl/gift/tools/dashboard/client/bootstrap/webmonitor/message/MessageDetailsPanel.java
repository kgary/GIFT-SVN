/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message;

import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageDisplayData;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageEntryMetadata;

/**
 * A panel used to display detailed information about a specific message
 * 
 * @author nroberts
 */
public class MessageDetailsPanel extends Composite {

    /** The text to display when a given attribute has no value */
    private static final String NONE = "None";

    private static MessageDetailsPanelUiBinder uiBinder = GWT.create(MessageDetailsPanelUiBinder.class);

    interface MessageDetailsPanelUiBinder extends UiBinder<Widget, MessageDetailsPanel> {
    }
    
    /** The panel containing the source module type to display. */
    @UiField
    protected FlowPanel sourceModuleTypePanel;
    
    /** The panel containing the sequence number to display. */
    @UiField
    protected FlowPanel seqNumPanel;
    
    /** The panel containing the source event id to display. */
    @UiField
    protected FlowPanel sourceEventIdPanel;
    
    /** The panel containing the needs ACK information to display. */
    @UiField
    protected FlowPanel needsACKPanel;
    
    /** The label displaying the message's source address */
    @UiField
    protected Label source;
    
    /** The label displaying the message's source module type */
    @UiField
    protected Label sourceModuleType;
    
    /** The label displaying the message's destination address */
    @UiField
    protected Label destination;
    
    /** The label displaying the message's type */
    @UiField
    protected Label type;
    
    /** The label displaying the message's sequence number */
    @UiField
    protected Label seqNum;
    
    /** The label displaying the message's source event ID */
    @UiField
    protected Label sourceEventId;
    
    /** The label displaying the message's needsACK value */
    @UiField
    protected Label needsACK;
    
    /** The label displaying the message's user ID */
    @UiField
    protected Label userId;
    
    /** The label displaying the message's username */
    @UiField
    protected HTML username;
    
    /** The label displaying the timestamp the message was sent at */
    @UiField
    protected Label timestamp;
    
    /** The label displaying the message's JSON details */
    @UiField
    protected Label details;

    /**
     * Creates a new panel capable of showing message details
     */
    public MessageDetailsPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    /**
     * The read only user constant from common.properties to check is a user should be anonymized.
     */
    private final String READ_ONLY_USER = UiManager.getInstance().getReadOnlyUser();
    
    /**
     * The name GIFT will use to display a read only user in active sessions.
     */
    private final String ANONYMOUS_USER = "Anonymized";
    
    /**
     * Shows detailed message information for the message with the given metadata
     * 
     * @param domainSessionId the ID of the domain session the message is a part of. Can be null.
     * @param message the metadata of the message to display. Cannot be null.
     */
    public void showMessage(Integer domainSessionId, final MessageEntryMetadata message) {
        
        UiManager.getInstance().getDashboardService().getMessageDisplayData(
                UiManager.getInstance().getUserName(), 
                UiManager.getInstance().getSessionId(), 
                domainSessionId, 
                message, 
                new AsyncCallback<GenericRpcResponse<MessageDisplayData>>() {
            
            @Override
            public void onSuccess(GenericRpcResponse<MessageDisplayData> result) {
                
                if(!result.getWasSuccessful()) {
                    UiManager.getInstance().displayDetailedErrorDialog("Failed to Show Message Details", 
                            result.getException());
                    
                    return;
                }
                
                MessageDisplayData messageData = result.getContent();
                
                if(messageData == null) {
                    
                    NotifySettings settings = NotifySettings.newSettings();
                    settings.setType(NotifyType.WARNING);
                    settings.setDelay(10000);
                    
                    Notify.notify("<b>Message Details Unavailable</b><br/>", 
                            "The details of this message could not be shown because it is no "
                            + "longer tracked by the server. This likely happened because the "
                            + "server has reached its maximum message buffer size and just removed "
                            + "the message before your browser was notified about the removal, "
                            + "which may have been delayed due to latency", 
                            settings);
                    return;
                }
                
                /* Populate all available labels with metadata information from the message, which will be displayed when 
                 * the advanced header checkbox is enabled. */
                source.setText(messageData.getMetadata().getSender() != null
                        ? messageData.getMetadata().getSender()
                        : NONE
                );
                
                destination.setText(messageData.getDestination() != null
                        ? messageData.getDestination()
                        : NONE
                );
                type.setText(messageData.getMetadata().getType() != null
                        ? messageData.getMetadata().getType().getDisplayName()
                        : NONE);
                
                timestamp.setText(Long.toString(messageData.getMetadata().getTimestamp()));
                
                sourceModuleType.setText(messageData.getMetadata().getSenderModuleType() != null
                        ? messageData.getMetadata().getSenderModuleType().getDisplayName()
                        : NONE);
                
                sourceEventId.setText(Integer.toString(messageData.getMetadata().getSourceEventId()));
                needsACK.setText(Boolean.toString(messageData.getMetadata().isNeedsACK()));
                seqNum.setText(Integer.toString(messageData.getMetadata().getSequenceNumber()));
                
                /* Clear the details and then display then new details using a JSON viewer*/
                details.getElement().setInnerHTML("");
                if(messageData.getDetailsJson() != null) {
                    render(JsonUtils.safeEval(messageData.getDetailsJson()), details.getElement());
                }
                 
                if(messageData.getUserSession() != null) {
                    
                    userId.setText(Integer.toString(messageData.getUserSession().getUserId()));
                    
                    if(messageData.getUserSession().getUsername().equals(READ_ONLY_USER)) {
                        
                        /* Show italicized text to indicate that this isn't a real username */
                        username.setHTML(new SafeHtmlBuilder()
                               .appendHtmlConstant("<i>")
                               .appendEscaped(ANONYMOUS_USER)
                               .appendHtmlConstant("</i>")
                               .toSafeHtml());
                        
                    } else {
                        username.setText(messageData.getUserSession().getUsername());
                    }
                    
                } else {
                    userId.setText(NONE);
                    username.setText(NONE);
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                
                UiManager.getInstance().displayDetailedErrorDialog("Failed to Show Message Details", 
                        "GIFT display details for the selected message because an unexpected error occured", 
                        "An exception was thrown while displaying " + message + ": " + caught, 
                        null, null);
            }
        });
    }

    /**
     * Renders the given JSON-created JavaScript object to the given target element as a formatted
     * collapsible hierarchy
     * 
     * @param json a JavaScript object created from a message's JSON. Cannot be null.
     * @param target the target element where the JSON hierarchy should be rendered. Cannot be null.
     */
    private static native void render(JavaScriptObject json, Element target)/*-{
        
        var jsonViewer = new $wnd.JSONViewer();
        jsonViewer.showJSON(json, -1, -1)
        target.appendChild(jsonViewer.getContainer());
    }-*/;
}
