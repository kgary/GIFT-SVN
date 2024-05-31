/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalHeader;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.EditorFrame;

/**
 * A resizable modal dialog that displays a widget
 * 
 * @author bzahid
 */
public class CourseObjectModal extends Composite {
    
    private static Logger logger = Logger.getLogger(CourseObjectModal.class.getName());

	interface CourseObjectModalUiBinder extends UiBinder<Widget, CourseObjectModal>{
	}
	
	private static CourseObjectModalUiBinder uiBinder = GWT.create(CourseObjectModalUiBinder.class);
	
	@UiField
	protected Modal courseObjectModal;
	
	@UiField
	protected InlineHTML modalTitle;
	
	@UiField
	protected FlowPanel contentPanel;
	
	@UiField
	protected Button actionButton;
	
	@UiField
	protected Button saveAndCloseButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected ModalHeader modalHeader;
	
	@UiField
	protected ModalHeader innerModalHeader;
	
	@UiField
	protected ModalBody modalBody;
	
	private EditorFrame gatFrame = null;
	
	private HandlerRegistration saveRegistration = null;
	
	private HandlerRegistration additionalRegistration = null;
	
	/** (Optional) Callback that can be used to signal information back to the caller of the modal when the modal dialog has been cancelled. */
    private CourseObjectModalCancelCallback cancelCallback;

    /** Flag to indicate if the modal is being shown */
    private boolean isShowing = false;
	
	/**
	 * Creates a new course object modal
	 */
	public CourseObjectModal() {
		initWidget(uiBinder.createAndBindUi(this));
		
		cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				cancelModal(false);
			}
			
		});
		
		saveAndCloseButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
			
		});
		
	} 
	
	/**
	 * Called when the cancel button was clicked or an embedded editor is requesting to cancel the modal (eg
	 * in the event of a critical error in the embedded editor).
	 * 
	 * @param removeSelection - True if the course transition object should clear/remove the value of the selected file in the modal.  False otherwise.
	 */
	private void cancelModal(boolean removeSelection) {
	    logger.info("cancelModal()");
	    resetEmbeddedSaveObject();
        stopEditor();
        
        if (this.cancelCallback != null) {
            cancelCallback.onCancelModal(removeSelection);
        }
        hide();
	}
	
	/**
	 * Sets the content of the modal dialog.
	 * Note: this will hide the 'additional button'.
	 * 
	 * @param widget The widget to display
	 */
	public void setCourseObjectWidget(String title, IsWidget widget) {
		contentPanel.clear();
		clearAdditionalButton();
		contentPanel.add(widget);
		modalTitle.setText(title);
	}
	
	/**
	 * Adds an iframe to the modal dialog and sets the url.
	 * Note: this will hide the 'additional button'.
	 * 
	 * @param title The title of the modal dialog.  Supports HTML.
	 * @param url The url to display in the iframe
	 */
	public void setCourseObjectUrl(final String title, final String url) {
				
		gatFrame = new EditorFrame(url);	        
		gatFrame.setWidth("100%");
		gatFrame.setHeight("100%");
		gatFrame.getInnerFrame().getElement().getStyle().setProperty("border", "none");
		gatFrame.getElement().getStyle().setProperty("display", "block");	       
		
		contentPanel.clear();
		clearAdditionalButton();
		contentPanel.add(gatFrame);
		modalTitle.setHTML(title);
	}
	
	/**
	 * Sets the save and close button click handler
	 * 
	 * @param saveButtonClickHandler The click handler to execute
	 */
	public void setSaveButtonHandler(ClickHandler saveButtonClickHandler) {
		
		if(saveRegistration != null) {
			saveRegistration.removeHandler();
		}
		
		if(!GatClientUtility.isReadOnly()) {
			saveRegistration = saveAndCloseButton.addClickHandler(saveButtonClickHandler);
		} else {
			setSaveAndCloseButtonVisible(false);
		}
		
	}
	
	/**
	 * Sets the cancel button click handler
	 * 
	 * @param cancelButtonHandler The click handler to execute
	 * @return The handler registration created by adding the click handler
	 */
	public HandlerRegistration setCancelButtonHandler(ClickHandler cancelButtonHandler) {
		
		return cancelButton.addClickHandler(cancelButtonHandler);
	}
	
	/**
	 * Adds an additional button to the modal footer
	 *
	 * @param text The button text
	 * @param buttonHandler The button click handler
	 */
	public void setAdditionalButton(String text, ClickHandler buttonHandler) {
		actionButton.setText(text);
		actionButton.setVisible(true);
		
		if(additionalRegistration != null) {
			additionalRegistration.removeHandler();
		}
		
		additionalRegistration = actionButton.addClickHandler(buttonHandler);
	}
	
	/**
	 * Removes the additional button from the modal footer.
	 */
	public void clearAdditionalButton() {
		actionButton.setVisible(false);
	}
	
	/**
	 * Shows the action button if the action button has previously set text.
	 * See {@link CourseObjectModal#setAdditionalButton(String, ClickHandler)}
	 */
	public void showAdditionalButton(){
	    
	    if(actionButton.getText() != null && !actionButton.getText().isEmpty()){
	        actionButton.setVisible(true);
	    }
	}

    /**
     * Checks if the modal is currenly being shown.
     * 
     * @return true if the modal is being shown; false if it is hidden.
     */
    public boolean isShowing() {
        return isShowing;
    }

	/**
	 * Shows the course object modal
	 */
	public void show() {
	    
	    logger.info("show() called.");
	    String modalUniqueId = Document.get().createUniqueId();
	    courseObjectModal.getElement().setId(modalUniqueId);

		courseObjectModal.show();
		isShowing = true;
		
		//need to append this modal to the base course editor when it is shown so it takes up the full width and height of the editor
		Element body = getBaseWindowBody();
		HTMLPanel bodyPanel = HTMLPanel.wrap(body);
		
		// Registers the native javascript functions that this modal will use.  Each modal has a unique identifier that is used
		// to essentially pass data back and forth between embedded iframes of the modal to the modal itself using the DOM.
		registerNativeFunctions(modalUniqueId);
		
		if (gatFrame != null && gatFrame.getInnerFrame() != null) {
		    registerIFrameLoadHandler(gatFrame.getInnerFrame().getElement(), modalUniqueId);
		} else {
		    logger.info("Unable to attach an iframe handler to this course object modal.  The inner frame was not setup, which may be okay.");
		}
		

		if (bodyPanel != null) {
		    bodyPanel.add(this);
		} else {
		    logger.severe("Unable to find the body panel for this course object modal.");
		}
	}
	
	

    /**
	 * Hides the course object modal
	 */
	public void hide() {
		courseObjectModal.hide();	
		isShowing = false;
		
		//need to remove this modal from the base course editor when it is hidden
		Element body = getBaseWindowBody();
		HTMLPanel bodyPanel = HTMLPanel.wrap(body);
		
		bodyPanel.remove(this);
	}
	
	/**
	 * Adds a hide handler to the modal dialog
	 * 
	 * @param modalHideHandler The handler to execute when the dialog is hidden
	 * @return the modal hide handler registration 
	 */
	public com.google.web.bindery.event.shared.HandlerRegistration addHideHandler(ModalHideHandler modalHideHandler) {
		
		return courseObjectModal.addHideHandler(modalHideHandler);
	}
	
	/**
	 * Sets the visibility of the save and close button on the modal
	 * @param visible The desired value to assign to the button's visibility
	 */
	public void setSaveAndCloseButtonVisible(boolean visible) {
	    saveAndCloseButton.setVisible(visible);
	}

	/**
     * Sets the visibility of the cancel button on the modal
     * 
     * @param visible The desired value to assign to the button's visibility
     */
    public void setCancelButtonVisible(boolean visible) {
        cancelButton.setVisible(visible);
    }
	
	public String getModalTitle() {
	    return modalTitle.getText();
	}
	
	/**
	 * Sets the title of the modal
	 * @param newTitle The desired value to assign to the title
	 */
	public void setModalTitle(String newTitle) {
	    modalTitle.setText(newTitle);
	}
	
	/**
	 * JSNI method that should only be called from native javascript.  This method is used to signal that the modal
	 * dialog should be cancelled and should only be called by embedded iframe editors.
	 * 
	 * @param removeSelection - True if the selected file should be cleared in the UI panel that brought up the editor.  For example
	 *        if the dkf editor within an iframe has a critical error, setting this to true will make the dkf file selected be set back to 
	 *        an empty string so the user does not have an invalid file selected.  Setting to false will leave the selected value unchanged.
	 */
	public void cancelModalJsni(boolean removeSelection) {
	    logger.info("cancelModalJsni() removeSelection=" + removeSelection);
	    cancelModal(removeSelection);
	    
	}
	
	/**
	 * Calls the stop method within the current embedded course
	 * object presenter in order to gracefully handle the lock and cancel
	 * the associated lock timer.
	 */
	public void stopEditor(){
		if(gatFrame != null) {
			stopEditor(gatFrame.getInnerFrame().getElement());
		}
	}

	/**
	 * Gets the &lt;body&gt; element for the the window containing the base course editor.
	 */
	private native Element getBaseWindowBody()/*-{
		
		var baseWnd = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();
		
		return baseWnd.document.body;
	}-*/;
	
	/**
	 * stops the presenter inside the editor frame
	 * 
	 * @param editorFrame The gatFrame's inner frame
	 */
	private native void stopEditor(Element editorFrame)/*-{
		
		if (editorFrame.contentWindow != null) {
		   try {
		     editorFrame.contentWindow.stop();
		   }
		   catch(err) {
		     console.log("stopEditor() called, but error occurred calling stop method.  This may be okay if the editor doesn't support the stop method.");
		   }
		   
		} else {
		    console.log("stopEditor called() but the editorFrame.contentWindow is null.");
		}
		
		
	}-*/;
	
	/**
	 * Sets the current embedded course object to be saved and is
	 * called by nested editors to reset the value as well. 
	 */
	public native static void resetEmbeddedSaveObject()/*-{
		if($wnd.redefineSaveEmbeddedCourseObject != null) {
			$wnd.redefineSaveEmbeddedCourseObject();
		}
	}-*/;
	
	/**
	 * Registers an IFrame load handler for the embedded iframe (if used).  Once the iframe is loaded
	 * the id of the modal dialog is attached to the iFrame content window so that the iFrame will know
	 * which modal it is attached to and can communicate back to the modal window.
	 * 
	 * @param innerFrame - The iFrame element.
	 * @param modalId - The modal id of the CourseObjectModal window.
	 */
	private native void registerIFrameLoadHandler(Element innerFrame, String modalId)  /*-{
    
        if (innerFrame != null) {
        
            innerFrame.onload = function() {
                console.log("iFrame is loaded, setting modal id of the iframe to be: " + modalId);
                
                if (innerFrame.contentWindow != null) {
                    innerFrame.contentWindow.modalId = modalId;
                } else {
                    console.log("SEVERE:  Unable to registerIFrameLoadHandler() - innerFrame.contentWindow is null.");
                }
                
            };
        } else {
            console.log("SEVERE:  Unable to registerIFrameLoadHandler() - innerFrame is null.");
        }
        
    }-*/;
	
	/**
	 * Registers native javascript functions that the CourseObjectModal class will use. These are registered
	 * anytime the dialog is shown since each modal is identified by it's own unique identifier.
	 * @param elementId
	 */
	public native void registerNativeFunctions(String elementId) /*-{
    
        var that = this;
    
    
        var modalWidget = $doc.getElementById(elementId);
        
        if (modalWidget != null) {
        
            modalWidget.cancelModal = $entry(function(removeSelection){
                    that.@mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal::cancelModalJsni(Z)(removeSelection);
            });
            
            modalWidget.setModalSaveEnabled = $entry(function(enabled){
                    that.@mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal::setSaveEnabled(Z)(enabled);
            });
            
            modalWidget.setModalButtonsVisible = $entry(function(saveVisible, cancelVisible){
                    that.@mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal::setSaveAndCloseButtonVisible(Z)(saveVisible);
                    that.@mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal::setCancelButtonVisible(Z)(cancelVisible);
            });
            
            console.log("registerNativefunctions succeeded with element id: " + elementId);
            
        } else {
            console.log("SEVERE:  Unable to register the cancelModal() method for the modal Widget because the modalWidget is null.");
        }

    }-*/;
	
	/**
	 * Sets whether or not this modal should enable its save button
	 * 
	 * @param enabled whether or not this modal should enable its save button
	 */
	private void setSaveEnabled(boolean enabled){
		saveAndCloseButton.setEnabled(enabled);
	}

	/**
	 * Sets the callback (optional) that the CourseObjectModal dialog will use if the dialog is cancelled.
	 * 
	 * @param callback - The callback that will be called when the CourseObjectModal is cancelled.
	 */
    public void setCancelCallback(CourseObjectModalCancelCallback callback) {
        this.cancelCallback = callback;
        
    }
	
    /**
     * Gets the frame used to hold external editors
     * 
     * @return the frame
     */
	public EditorFrame getEditorFrame(){
		return gatFrame;
	}
	
	
}
