/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance;


import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider;
import mil.arl.gift.tools.authoring.server.gat.client.provider.CourseReadOnlyProvider.CourseReadOnlyHandler;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;

/**
 * An editor used to author guidance
 */
public class GuidanceViewImpl extends Composite implements GuidanceView, CourseReadOnlyHandler {

	/** The ui binder. */
	private static GuidanceViewImplUiBinder uiBinder = GWT
			.create(GuidanceViewImplUiBinder.class);
	
	/**
	 * The Interface GuidanceViewImplUiBinder.
	 */
	interface GuidanceViewImplUiBinder extends
			UiBinder<Widget, GuidanceViewImpl> {
	}
	
	/** The display time. */
	@UiField
	protected TextBox displayTime;
	
	/** The full screen. */
	@UiField
	protected CheckBox fullScreen;
	
	/** the panel with the display time components */
	@UiField
	protected FlowPanel displayTimePanel;
	
	/** The disable checkbox. */
    @UiField
    protected CheckBox disabled;
	
	@UiField 
	protected org.gwtbootstrap3.client.ui.Button textButton;
	
	@UiField 
	protected org.gwtbootstrap3.client.ui.Button fileButton;
	
	@UiField 
	protected org.gwtbootstrap3.client.ui.Button urlButton;
	
	/** The deck panel. */
	@UiField
	protected DeckPanel deckPanel;
	
	/** The guidance file editor. */
	@UiField
	protected GuidanceFileSelectionEditor guidanceFileEditor;
	
	/** The guidance url editor. */
	@UiField
	protected GuidanceUrlEditor guidanceUrlEditor;
	
	/** The guidance message editor. */
	@UiField
	protected GuidanceMessageEditor guidanceMessageEditor;
	
	@UiField
	protected DisclosurePanel optionsPanel;
	
	@UiField
	protected HasHTML displayTimeTooltip;
	
	@UiField
	protected DeckPanel choiceDeck;
	
	@UiField
	protected Widget choicePanel;
	
	@UiField
	protected Widget editorPanel;
	
	/** The file selection dialog. */
	private DefaultGatFileSelectionDialog fileSelectionDialog = new DefaultGatFileSelectionDialog();
	
	/**
	 * Instantiates a new guidance view impl.
	 */
	public GuidanceViewImpl() {
		initWidget(uiBinder.createAndBindUi(this));
		
		CourseReadOnlyProvider.getInstance().addReadOnlyHandlerManaged(this);
		
		init();
	}
	
	/**
	 * Inits the.
	 */
	private void init(){
		
        if(GatClientUtility.isReadOnly()) {
            
            //in case no file is selected update that UI
            guidanceFileEditor.selectFileLabel.setText("No file selected");
            guidanceFileEditor.selectFileLabel.getElement().getStyle().setProperty("cursor", "not-allowed");
            
            //in case a file is selected disable the remove component
            guidanceFileEditor.removeFileButton.setEnabled(false);
            guidanceFileEditor.removeFileButton.addStyleName("buttonDisabled");
            
            guidanceMessageEditor.setEnabled(false);
            
            displayTime.setEnabled(false);
            fullScreen.setEnabled(false);
            disabled.setEnabled(false);
        } else {
            
            fileSelectionDialog.getFileSelector().setAllowedFileExtensions(Constants.html_supported_types);
            fileSelectionDialog.setText("Select a File");
            fileSelectionDialog.setIntroMessageHTML("Choose a webpage file (e.g. html, htm) to show to the learner.");
            
            //display successful upload message in Notify UI not a dialog
            fileSelectionDialog.setMessageDisplay(DefaultMessageDisplay.ignoreInfoMessage);
        }
        
        //handle browsing for a guidance file
        guidanceFileEditor.getFileBrowseInput().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if(!GatClientUtility.isReadOnly()) {
                    fileSelectionDialog.center();
                }
            }
            
        });
		
		choiceDeck.showWidget(choiceDeck.getWidgetIndex(choicePanel));
		
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getDisplayTimeInput()
	 */
	@Override
	public HasValue<String> getDisplayTimeInput(){
		return displayTime;
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getFullScreenInput()
	 */
	@Override
	public HasValue<Boolean> getFullScreenInput(){
		return fullScreen;
	}
	
	/* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getDisabledInput()
     */
    @Override
    public HasValue<Boolean> getDisabledInput(){
        return disabled;
    }
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getUseFileInput()
	 */
	@Override
	public HasClickHandlers getUseFileInput(){
		return fileButton;
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getUseUrlInput()
	 */
	@Override
	public HasClickHandlers getUseUrlInput(){
		return urlButton;
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getUseMessageContentInput()
	 */
	@Override
	public HasClickHandlers getUseMessageContentInput(){
		return textButton;
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#setMessageContent()
	 */
	@Override
	public void setMessageContent(String html){
		guidanceMessageEditor.setMsgHTML(html);
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getMessageContent()
	 */
	@Override
	public String getMessageContent(){
		return guidanceMessageEditor.getMsgHTML();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#addMessageContentInputBlurHandler()
	 */
	@Override
	public void addMessageContentInputBlurHandler(SummernoteBlurHandler handler){
		guidanceMessageEditor.addMessageContentInputBlurHandler(handler);
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getUrlAddressInput()
	 */
	@Override
	public HasValue<String> getUrlAddressInput(){
		return guidanceUrlEditor.getUrlAddressInput();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getUrlPreviewButton()
	 */
	@Override
	public HasClickHandlers getUrlPreviewButton() {
		return guidanceUrlEditor.getUrlPreviewButton();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getUrlMessageInput()
	 */
	@Override
	public Summernote getUrlMessageInput(){
		return guidanceUrlEditor.getMessageInput();
	}
	
	@Override
	public HasText getFileNameLabel(){
		return guidanceFileEditor.getFileNameLabel();
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getFileBrowseInput()
	 */
	@Override
	public HasClickHandlers getFileBrowseInput(){
		return guidanceFileEditor.getFileBrowseInput();
	}
	
	@Override
	public Summernote getFileMessageInput(){
		return guidanceFileEditor.getMessageInput();
	}
	
	/* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getFilePreviewButton()
     */
    @Override
    public HasClickHandlers getFilePreviewButton() {
        return guidanceFileEditor.getFilePreviewButton();
    }
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#getFileSelectionView()
	 */
	@Override
	public HasValue<String> getFileSelectionDialog(){
		return fileSelectionDialog;
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#showGuidanceMessageEditor()
	 */
	@Override
	public void showGuidanceMessageEditor(){
		
		deckPanel.showWidget(deckPanel.getWidgetIndex(guidanceMessageEditor));
		choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#showGuidanceUrlEditor()
	 */
	@Override
	public void showGuidanceUrlEditor(){
		
		deckPanel.showWidget(deckPanel.getWidgetIndex(guidanceUrlEditor));
		choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));
	}
	
	/* (non-Javadoc)
	 * @see mil.arl.gift.tools.authoring.gat.client.view.course.guidance.GuidanceView#showGuidanceFileEditor()
	 */
	@Override
	public void showGuidanceFileEditor(){
		
		deckPanel.showWidget(deckPanel.getWidgetIndex(guidanceFileEditor));
		choiceDeck.showWidget(choiceDeck.getWidgetIndex(editorPanel));
	}
	
	@Override
	public void showChoicePanel(){
		choiceDeck.showWidget(choiceDeck.getWidgetIndex(choicePanel));
	}
	
	@Override
    public void hideDisabledOption(boolean hide) {
	    disabled.setVisible(!hide);
	}
	
	@Override
    public void hideDisplayTimePanel(boolean hide){
	    displayTimePanel.setVisible(!hide);
	}
    
	@Override
    public void hideDisplayFullScreenOption(boolean hide){
	    fullScreen.setVisible(!hide);
	}
	
	@Override
	public void hideInfoMessage(boolean hide) {
		guidanceFileEditor.hideMessageInput(hide);
		guidanceUrlEditor.hideMessageInput(hide);
	}
	
	@Override
	public HasHTML getDisplayTimeTooltip(){
		return displayTimeTooltip;
	}

    @Override
    public void setGuidanceFileAttributes(String value) {
        
        guidanceFileEditor.getFileNameLabel().setText(value);
        fileSelectionDialog.setValue(value);

        if(value != null){
            //show guidance file selected UI component
            guidanceFileEditor.selectFilePanel.setVisible(false);
            guidanceFileEditor.fileSelectedPanel.setVisible(true);
        }else{
          //show guidance file selection UI component
            guidanceFileEditor.selectFilePanel.setVisible(true);
            guidanceFileEditor.fileSelectedPanel.setVisible(false);
        }

    }

    @Override
    public HasClickHandlers getRemoveFileInput() {
        return guidanceFileEditor.removeFileButton;
    }

    /**
     * Sets whether or not this editor is embedded within a training application editor and adjusts its UI components appropriately
     * 
     * @param isTrainingAppEmbedded whether this editor is embedded within a training application editor
     */
    public void setTrainingAppEmbedded(boolean isTrainingAppEmbedded) {
        fullScreen.setVisible(!isTrainingAppEmbedded);
    }

	@Override
	public void onReadOnlyChange(boolean isReadOnly) {
		guidanceMessageEditor.setEnabled(!isReadOnly);
		displayTime.setEnabled(!isReadOnly);
		fullScreen.setEnabled(!isReadOnly);
		disabled.setEnabled(!isReadOnly);
	}

}
