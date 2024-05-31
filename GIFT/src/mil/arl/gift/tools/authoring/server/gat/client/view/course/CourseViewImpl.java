/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gwtbootstrap3.client.shared.event.ModalHideEvent;
import org.gwtbootstrap3.client.shared.event.ModalHideHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

import generated.course.Concepts;
import generated.course.Course;
import mil.arl.gift.common.course.CourseFileAccessDetails.CourseFileUserPermissionsDetails;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility.ServerPropertiesChangeHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseObjectEditorPanel.Orientation;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.HeaderView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.header.HeaderViewImpl;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.media.MediaManagementDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.media.SelectableMediaWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseTree;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.GiftWrapModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchMediaFilesResult;

/**
 * The Class CourseViewImpl.
 */
public class CourseViewImpl extends Composite implements CourseView {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /**
     * The Interface EditCourseViewImplUiBinder.
     */
    interface EditCourseViewImplUiBinder extends
            UiBinder<Widget, CourseViewImpl> {
    }

    /** The ui binder. */
    private static EditCourseViewImplUiBinder uiBinder = GWT
            .create(EditCourseViewImplUiBinder.class);
    
    @UiField
    protected TreeManager treeManager;
    
    @UiField Widget readOnlyLabel;
    
    @UiField protected EditableInlineLabel courseNameButton;
    
    /** Bug text box. */
    @UiField protected TextBox bugTextBox;
    
    /** dialog to show either course description or course concepts editors */
    @UiField protected CourseObjectModal coursePropertiesReuseDialog;
    
    /** GIFT Wrap dialog */
    @UiField protected GiftWrapModal giftWrapDialog;
    
    @UiField
    protected Widget guidanceButton;
    
    @UiField
    protected Widget aarButton;
    
    @UiField
    protected Widget taButton;
    
    @UiField
    protected Widget vbsButton;
    
    @UiField
    protected Widget tc3Button;
    
    @UiField
    protected Widget testbedButton;
    
    @UiField
    protected Widget aresButton;
    
    @UiField
    protected Widget unityButton;
    
    @UiField
    protected Widget mobileAppButton;
    
    /** The button used to create VR-Engage elements */
    @UiField
    protected Widget vrEngageButton;
    
    /** The button used to create standalone Unity elements */
    @UiField
    protected Widget unityStandaloneButton;
    
    /** The button used to create HAVEN elements */
    @UiField
    protected Widget havenButton;
    
    /** The button used to create RIDE elements */
    @UiField
    protected Widget rideButton;
    
    @UiField
    protected Widget exampleAppButton;
    
    @UiField
    protected Widget surveyButton;
    
    @UiField
    protected Widget lmButton;
    
    @UiField
    protected Widget slideShowButton;

    @UiField
    protected Widget pdfButton;
    
    @UiField
    protected Widget youtubeButton;
    
    @UiField
    protected Widget imageButton;
    
    @UiField
    protected Widget webAddressButton;

    @UiField
    protected Widget localWebpageButton;
    
    @UiField
    protected Widget videoButton;
    
    @UiField
    protected Widget mbpButton;
    
    @UiField
    protected Widget autoTutorButton;
    
    @UiField
    protected Widget conversationTreeButton;
    
    @UiField
    protected Widget questionBankButton;
    
    @UiField
    protected Widget ltiButton;
    
    @UiField
    protected Widget courseObjectPanelInstructions;
    
    @UiField
    protected Widget authoredBranchButton;
    
    /** contains the Course Properties, Course Objects and Media stacks */
    @UiField
    protected StackLayoutPanel courseStackPanel;
    
    @UiField
    protected FlowPanel propertiesPanel;
    
    @UiField
    protected Widget propertiesLvl2Panel;
    
    @UiField
    protected Widget propertiesLvl3Panel;
    
    @UiField
    protected Widget propertiesPanelContainer;
    
    @UiField
    protected FocusPanel advancedPanel;
    
    @UiField
    protected Collapse advancedCollapse;
    
    @UiField
    protected FlowPanel advancedContent;
    
    @UiField
    protected Label coursePathLabel;
    
    @UiField
    protected HTML helpTextHtml;
    
    @UiField
    protected HTML diskSpaceLabel;
    
    @UiField
    protected Icon arrowIcon;
    
    @UiField
    protected Image saveCourseImage;
    
    @UiField
    protected FlowPanel saveCourseButton;
    
    @UiField
    protected Image validateCourseImage;
    
    @UiField
    protected FlowPanel validateCourseButton;
    
    @UiField
    protected Image refreshCourseImage;
    
    @UiField
    protected Icon accessTableIcon;
    
    @UiField
    protected Button previewCourseButton;

    @UiField
    protected LayoutPanel uiDeck;
    
    @UiField
    protected FlowPanel lockedCourseButton;
    
    @UiField
    protected Icon lockedCourseIcon;
    
    @UiField(provided=true)
    protected SplitLayoutPanel courseTreeSplitter = new SplitLayoutPanel(){
    
        @Override
        public void onResize() {
    
            super.onResize();
            treeManager.onResize();
        }
    };
    
    // Transition Editors    
    
    /** The course header editor. */
    @UiField protected HeaderViewImpl courseHeaderEditor;
    
    /** The media file manager dialog */
    @UiField protected MediaManagementDialog mediaDialog;    
    
    /** The media files list displayed in the course properties panel */
    @UiField protected CellTable<SelectableMediaWidget> mediaListCellTable;

	/** 
	 * The container for the mediaListCellTable or a message if no media files exist
	 */
	@UiField
	protected FlowPanel mediaSummaryPanel;
	
	/**
	 * The panel that displays either the mediaSummaryPanel or the contentLoadingPanel
	 */
	@UiField
	protected DeckPanel contentDeckPanel;
	
	/**
	 * The loading overlay displayed if the media list is being refreshed
	 */
	@UiField
	protected FlowPanel contentLoadingPanel;
		
    private ListDataProvider<SelectableMediaWidget> mediaListDataProvider = new ListDataProvider<SelectableMediaWidget>();
    
    private Column<SelectableMediaWidget, String> mediaFilesColumn = new Column<SelectableMediaWidget, String>(new TextCell() {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, 
                String value, SafeHtmlBuilder sb) {
            
              SafeHtml html = SafeHtmlUtils.fromTrustedString(value);
              sb.append(html);
        }
        
    }) {

        @Override
        public String getValue(SelectableMediaWidget media) {            
            return media.getIcon().toString() + " " + media.getFileName();
        }
    
    };    
        
    @UiField
    protected SimpleLayoutPanel objectEditorRightPanel;
    
    @UiField
    protected SimpleLayoutPanel objectEditorBottomPanel;
    
    @UiField
    protected Widget mainSplitterPanel;
    
    @UiField
    protected DeckLayoutPanel mainDeck;
    
    @UiField
    protected SimpleLayoutPanel objectEditorFullScreenPanel;
    
    @UiField
    protected LayoutPanel propertiesDeck;
    
    @UiField
    protected DeckPanel propertiesLvl2Deck;
    
    @UiField
    protected DeckPanel propertiesLvl3Deck;
    
    @UiField
    protected DynamicHeaderScrollPanel stackMediaPanel;
    
    @UiField
    protected FocusPanel ltiPropertiesButton;
    
    @UiField
    protected Widget ltiPropertiesPanel;
    
    @UiField
    protected FocusPanel ltiConsumerButton;
    
    @UiField
    protected FocusPanel ltiProviderButton;
    
    @UiField
    protected FocusPanel pathPanel;
    
    @UiField
    protected Collapse pathCollapse;
    
    @UiField
    protected FocusPanel configurationsButton;
    
    @UiField
    protected CollaboratorsTableModal userAccessModal;
    
    
    /** A panel used to create and manage editors for course objects */
    protected CourseObjectEditorPanel editorPanel;
        
    private Summernote descriptionEditor = new Summernote();
    
    private ScheduledCommand saveCourseCmd;
    
    private ScheduledCommand saveDescriptionCmd;
        
    private final int MAX_LENGTH = 128;
    
    /** Indent offset values for the next overlapping panel of course properties. */
    // A - TIER 1
    // ->B - TIER 2
    // ->->C - TIER 3
    private final int LAYER_TIER2_OFFSET = 35;
    private final int LAYER_TIER3_OFFSET = LAYER_TIER2_OFFSET * 2;
    
	/** the last selected stack in the course stack layout panel */
	private Widget lastSelectedCourseStack = null;
	
	/** a command to invoke when the course properties dialog is closed */
	private Command propertiesDialogClosedCmd;
	
    /**
     * Instantiates a new course view impl.
     * 
     * @param guidanceTransEditor the guidance transition editor injected into this view
     */
    @Inject
    public CourseViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
        
        mainDeck.showWidget(mainSplitterPanel);
        
        /*
         * There is an odd bug that is a result of some combination of text 
         * boxes, iFrames, and Internet Explorer. The text boxes would somehow
         * get into a state where clicking on it would be ignored thus
         * preventing the user from typing in it. I've found that putting a
         * text box in the highest GUI element, scheduling this delayed
         * command, and then making the text box invisible prevents the bug
         * from happening. I don't know why this works.
         */
        ScheduledCommand command = new ScheduledCommand() {
            @Override
            public void execute() {
                bugTextBox.setFocus(true);
                bugTextBox.setVisible(false);
            }
        };
        Scheduler.get().scheduleDeferred(command);
        
        //make the course objects in the left panel draggable
        guidanceButton.getElement().setDraggable("true");
        aarButton.getElement().setDraggable("true");
        mbpButton.getElement().setDraggable("true");
        ltiButton.getElement().setDraggable("true");
        surveyButton.getElement().setDraggable("true");
        lmButton.getElement().setDraggable("true");
        slideShowButton.getElement().setDraggable("true");
        pdfButton.getElement().setDraggable("true");
        youtubeButton.getElement().setDraggable("true");
        imageButton.getElement().setDraggable("true");
        webAddressButton.getElement().setDraggable("true");
        localWebpageButton.getElement().setDraggable("true");
        videoButton.getElement().setDraggable("true");
        taButton.getElement().setDraggable("true");
        vbsButton.getElement().setDraggable("true");
        tc3Button.getElement().setDraggable("true");
        testbedButton.getElement().setDraggable("true");
        aresButton.getElement().setDraggable("true");
        unityButton.getElement().setDraggable("true");
        mobileAppButton.getElement().setDraggable("true");
        vrEngageButton.getElement().setDraggable("true");
        unityStandaloneButton.getElement().setDraggable("true");
        havenButton.getElement().setDraggable("true");
        rideButton.getElement().setDraggable("true");
        exampleAppButton.getElement().setDraggable("true");
        autoTutorButton.getElement().setDraggable("true");
        conversationTreeButton.getElement().setDraggable("true");
        questionBankButton.getElement().setDraggable("true");
        authoredBranchButton.getElement().setDraggable("true");
        
        //show the course object panel initially
        courseStackPanel.showWidget(1);
        updateSelectedStack(1);
        
        //highlight the selected stack
        courseStackPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                
                updateSelectedStack(event.getSelectedItem());
            }
        });
        
        //clear the help text if the user clicks somewhere else in the course view UI
        RootPanel.get().addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                getHelpHTML().setHTML("");
            }
        }, ClickEvent.getType());
        
        previewCourseButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				GatClientUtility.showPreviewDialog(editorPanel.getEditingCourseObjectIndex());
			}
		});
        
        ltiPropertiesButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                event.stopPropagation();

                boolean wasShowing = true;
                
                if (propertiesLvl2Deck.getVisibleWidget() != propertiesLvl2Deck.getWidgetIndex(ltiPropertiesPanel)) {

                    // switch the tier 2 properties panel to show the LTI properties panel
                    propertiesLvl2Deck.showWidget(propertiesLvl2Deck.getWidgetIndex(ltiPropertiesPanel));

                    configurationsButton.getElement().getStyle().clearBackgroundColor();
                    configurationsButton.getElement().getStyle().clearCursor();

                    ltiPropertiesButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    ltiPropertiesButton.getElement().getStyle().setCursor(Cursor.POINTER);

                    wasShowing = false;
                }

                if (propertiesDeck.getWidgetContainerElement(propertiesLvl2Panel).getOffsetWidth() <= 5) {

                    // if the tier 2 properties panel isn't showing, display it
                    propertiesDeck.setWidgetLeftRight(propertiesLvl2Panel, LAYER_TIER2_OFFSET, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiPropertiesButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    ltiPropertiesButton.getElement().getStyle().setCursor(Cursor.POINTER);

                } else if (wasShowing) {

                    // otherwise, hide the tier 2 properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl2Panel, 0, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiPropertiesButton.getElement().getStyle().clearBackgroundColor();
                    ltiPropertiesButton.getElement().getStyle().clearCursor();
                }
             
                if (propertiesDeck.getWidgetContainerElement(propertiesLvl3Panel).getOffsetWidth() > 0) {

                    // hide the tier 3 properties panel if the user clicks on the tier 1 properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl3Panel, 0, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiConsumerButton.getElement().getStyle().clearBackgroundColor();
                    ltiConsumerButton.getElement().getStyle().clearCursor();

                    ltiProviderButton.getElement().getStyle().clearBackgroundColor();
                    ltiProviderButton.getElement().getStyle().clearCursor();
                }
            }
        });
        
        ltiConsumerButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();

                boolean wasShowing = true;

                 if (propertiesLvl3Deck.getVisibleWidget() != propertiesLvl3Deck.getWidgetIndex(getHeaderView().getLTIConsumerPropertiesPanel())) {

                    // switch the tier 3 properties panel to show the LTI provider properties panel
                    propertiesLvl3Deck.showWidget(propertiesLvl3Deck.getWidgetIndex(getHeaderView().getLTIConsumerPropertiesPanel()));

                    ltiProviderButton.getElement().getStyle().clearBackgroundColor();
                    ltiProviderButton.getElement().getStyle().clearCursor();
                    
                    ltiConsumerButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    ltiConsumerButton.getElement().getStyle().setCursor(Cursor.POINTER);

                    wasShowing = false;
                } 
                
                if (propertiesDeck.getWidgetContainerElement(propertiesLvl3Panel).getOffsetWidth() <= 5) {

                    // if the tier 3 properties panel isn't showing, display it
                    propertiesDeck.setWidgetLeftRight(propertiesLvl3Panel, LAYER_TIER3_OFFSET, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiConsumerButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    ltiConsumerButton.getElement().getStyle().setCursor(Cursor.POINTER);

                } else if (wasShowing) {

                    // otherwise, hide the tier 3 properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl3Panel, 0, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiConsumerButton.getElement().getStyle().clearBackgroundColor();
                    ltiConsumerButton.getElement().getStyle().clearCursor();
                }
            }
        });
        
        ltiProviderButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                event.stopPropagation();

                boolean wasShowing = true;

                if (propertiesLvl3Deck.getVisibleWidget() != propertiesLvl3Deck.getWidgetIndex(getHeaderView().getLTIProviderPropertiesPanel())) {

                    // switch the tier 3 properties panel to show the LTI provider properties panel
                    propertiesLvl3Deck.showWidget(propertiesLvl3Deck.getWidgetIndex(getHeaderView().getLTIProviderPropertiesPanel()));

                    ltiConsumerButton.getElement().getStyle().clearBackgroundColor();
                    ltiConsumerButton.getElement().getStyle().clearCursor();
                    
                    ltiProviderButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    ltiProviderButton.getElement().getStyle().setCursor(Cursor.POINTER);

                    wasShowing = false;
                } 
                
                if (propertiesDeck.getWidgetContainerElement(propertiesLvl3Panel).getOffsetWidth() <= 5) {

                    // if the tier 3 properties panel isn't showing, display it
                    propertiesDeck.setWidgetLeftRight(propertiesLvl3Panel, LAYER_TIER3_OFFSET, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiProviderButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    ltiProviderButton.getElement().getStyle().setCursor(Cursor.POINTER);

                } else if (wasShowing) {

                    // otherwise, hide the tier 3 properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl3Panel, 0, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiProviderButton.getElement().getStyle().clearBackgroundColor();
                    ltiProviderButton.getElement().getStyle().clearCursor();
                }
            }
        });
        
        configurationsButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                event.stopPropagation();
                
                boolean wasShowing = true;
                
                 if(propertiesLvl2Deck.getVisibleWidget() != propertiesLvl2Deck.getWidgetIndex(getHeaderView().getConfigurationsWidget())){
                    
                    //switch the tier 2 properties panel to show the configurations panel
                    propertiesLvl2Deck.showWidget(propertiesLvl2Deck.getWidgetIndex(getHeaderView().getConfigurationsWidget()));
                    
                    ltiPropertiesButton.getElement().getStyle().clearBackgroundColor();
                    ltiPropertiesButton.getElement().getStyle().clearCursor();
                    
                    configurationsButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    configurationsButton.getElement().getStyle().setCursor(Cursor.POINTER);
                    
                    wasShowing = false;
                }
                
                if(propertiesDeck.getWidgetContainerElement(propertiesLvl2Panel).getOffsetWidth() <= 5){

                    //if the tier 2 properties panel isn't showing, display it
                    propertiesDeck.setWidgetLeftRight(propertiesLvl2Panel, LAYER_TIER2_OFFSET, Unit.PX, 0, Unit.PX);
                    
                    propertiesDeck.animate(250);
                    
                    configurationsButton.getElement().getStyle().setBackgroundColor("rgb(225, 225, 255)");
                    configurationsButton.getElement().getStyle().setCursor(Cursor.POINTER);
                
                } else if(wasShowing){
                    
                    //otherwise, hide the tier 2 properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl2Panel, 0, Unit.PX, 0, Unit.PX);
                    
                    propertiesDeck.animate(250);
                    
                    configurationsButton.getElement().getStyle().clearBackgroundColor();
                    configurationsButton.getElement().getStyle().clearCursor();
                }
            }
        });
        
        editorPanel = new CourseObjectEditorPanel(new CourseObjectEditorPanel.DisplayHandler() {
            
            private Orientation myOrientation = null;

            @Override
            public void updateOrientation(Orientation orientation) {
                    
                if(orientation.equals(Orientation.BOTTOM)){    
                    
                    objectEditorRightPanel.remove(editorPanel);
                    objectEditorFullScreenPanel.remove(editorPanel);
                    objectEditorBottomPanel.setWidget(editorPanel);
                
                    courseTreeSplitter.setWidgetHidden(objectEditorRightPanel, true);
                    courseTreeSplitter.setWidgetHidden(objectEditorBottomPanel, false);
                    showEditorFullScreenPanel(false);
                
                } else if(orientation.equals(Orientation.FULLSCREEN)){    
                    
                    objectEditorRightPanel.remove(editorPanel);
                    objectEditorBottomPanel.remove(editorPanel);
                    objectEditorFullScreenPanel.setWidget(editorPanel);
                
                    courseTreeSplitter.setWidgetHidden(objectEditorRightPanel, true);
                    courseTreeSplitter.setWidgetHidden(objectEditorBottomPanel, true);
                    showEditorFullScreenPanel(true);
                    
                } else {                
                    
                    objectEditorBottomPanel.remove(editorPanel);
                    objectEditorFullScreenPanel.remove(editorPanel);
                    objectEditorRightPanel.setWidget(editorPanel);
                    
                    courseTreeSplitter.setWidgetHidden(objectEditorBottomPanel, true);
                    courseTreeSplitter.setWidgetHidden(objectEditorRightPanel, false);
                    
                    if(GatClientUtility.isRtaLessonLevel()) {
                        
                        // If GIFT's lesson level is set to RTA, try to make the editor take up 2/3 of the available space, since the 
                        // course tree is less needed. Min size must be at least 300 to make sure the editor can be interacted with 
                        // easily, but if possible, leave 300px for the course tree so the user can interact with the course name
                        courseTreeSplitter.setWidgetSize(objectEditorRightPanel, Math.max(
                                300, 
                                Math.min(courseTreeSplitter.getOffsetWidth() * 0.66, courseTreeSplitter.getOffsetWidth() - 300)));
                    }
                    
                    showEditorFullScreenPanel(false);
                }
    
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        courseTreeSplitter.onResize();
                    }
                });                
                
                myOrientation = orientation;
            }

            @Override
            public void updateVisibility(boolean visible) {
                
                if(Orientation.BOTTOM.equals(myOrientation)){
                    courseTreeSplitter.setWidgetHidden(objectEditorBottomPanel, !visible);
                
                } else if(Orientation.FULLSCREEN.equals(myOrientation)){
                    showEditorFullScreenPanel(visible);
                    
                } else {
                    courseTreeSplitter.setWidgetHidden(objectEditorRightPanel, !visible);
                }
                
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    
                    @Override
                    public void execute() {
                        courseTreeSplitter.onResize();
                    }
                });    
            }
            
            private void showEditorFullScreenPanel(boolean visible){
                
                if(visible){
                    mainDeck.showWidget(objectEditorFullScreenPanel);
                
                } else {
                    mainDeck.showWidget(mainSplitterPanel);
                }
            }
        });      
        
        pathPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //show/hide the path text
                pathCollapse.toggle();
            }
        });
        
        propertiesPanelContainer.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if (propertiesDeck.getWidgetContainerElement(propertiesLvl2Panel).getOffsetWidth() > 0) {

                    // hide the tier 2 properties panel if the user clicks on the main properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl2Panel, 0, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    configurationsButton.getElement().getStyle().clearBackgroundColor();
                    configurationsButton.getElement().getStyle().clearCursor();

                    ltiPropertiesButton.getElement().getStyle().clearBackgroundColor();
                    ltiPropertiesButton.getElement().getStyle().clearCursor();
                }

                if (propertiesDeck.getWidgetContainerElement(propertiesLvl3Panel).getOffsetWidth() > 0) {

                    // hide the tier 3 properties panel if the user clicks on the main properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl3Panel, 0, Unit.PX, 0, Unit.PX);

                    propertiesDeck.animate(250);

                    ltiConsumerButton.getElement().getStyle().clearBackgroundColor();
                    ltiConsumerButton.getElement().getStyle().clearCursor();

                    ltiProviderButton.getElement().getStyle().clearBackgroundColor();
                    ltiProviderButton.getElement().getStyle().clearCursor();
                }
            }
            
        }, ClickEvent.getType());
        
        propertiesLvl2Panel.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(propertiesDeck.getWidgetContainerElement(propertiesLvl3Panel).getOffsetWidth() > 0){
                    
                    //hide the tier 3 properties panel if the user clicks on the main properties panel
                    propertiesDeck.setWidgetRightWidth(propertiesLvl3Panel, 0, Unit.PX, 0, Unit.PX);                
                    
                    propertiesDeck.animate(250);
                    
                    ltiConsumerButton.getElement().getStyle().clearBackgroundColor();
                    ltiConsumerButton.getElement().getStyle().clearCursor();
                    
                    ltiProviderButton.getElement().getStyle().clearBackgroundColor();
                    ltiProviderButton.getElement().getStyle().clearCursor();
                }
            }
            
        }, ClickEvent.getType());
        
        courseTreeSplitter.setWidgetHidden(objectEditorRightPanel, true);
        courseTreeSplitter.setWidgetHidden(objectEditorBottomPanel, true);
        
        courseTreeSplitter.forceLayout();        
        
        // If LessonLevel is set to RTA, then the widgets should be hidden.
        // Note: the properties may not have been received by the client yet
        GatClientUtility.addServerPropertiesChangeHandler(new ServerPropertiesChangeHandler() {
            
            @Override
            public void onServerPropertiesChange(ServerProperties properties) {
                if(properties.getLessonLevel().equals(LessonLevelEnum.RTA)){
                    configurationsButton.setVisible(false);
                    ltiPropertiesButton.setVisible(false);
                    ltiButton.setVisible(false);
                    guidanceButton.setVisible(false);
                    imageButton.setVisible(false);
                    webAddressButton.setVisible(false);
                    localWebpageButton.setVisible(false);
                    videoButton.setVisible(false);
                    pdfButton.setVisible(false);
                    youtubeButton.setVisible(false);
                    surveyButton.setVisible(false);
                    autoTutorButton.setVisible(false);
                    conversationTreeButton.setVisible(false);
                    questionBankButton.setVisible(false);
                    lmButton.setVisible(false);
                    mbpButton.setVisible(false);
                    aarButton.setVisible(false);
                    authoredBranchButton.setVisible(false);
                    unityButton.setVisible(false);
                    mobileAppButton.setVisible(false);
                    exampleAppButton.setVisible(false);
                    courseStackPanel.remove(stackMediaPanel);
                    previewCourseButton.setVisible(false);
                    slideShowButton.setVisible(false);
                } 
                
            }
        });

	}
	
	/**
	 * Changes the style of the selected stack (e.g. Course objects) in the course authoring tool so that the
	 * user knows which stack is selected.
	 * 
	 * @param index the zero based index of the stack being selected.  If negative or out of bounds, nothing
	 * will happen.
	 */
	private void updateSelectedStack(Integer index){
	    
	    if(index < 0){
	        return;
	    }
	    
        Widget widget = courseStackPanel.getHeaderWidget(index);
        
        if(widget == null){
            return;
        }
        
        //reset the style of the previously selected stack
        if(lastSelectedCourseStack != null){            
            lastSelectedCourseStack.getElement().getStyle().clearProperty("backgroundImage");
        }
        
        widget.getElement().getStyle().setProperty("backgroundImage", "linear-gradient(rgba(255,165,0, 0.2), rgba(255,165,0 ,1.0))");               
        
        lastSelectedCourseStack = widget;
    }
    
    private void showDialogWidget(String title, final IsWidget isWidget) {
        
        ClickHandler saveHandler;
        saveHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {

                //update the course tree whenever a transition is saved only when not read only
                if(!readOnlyLabel.isVisible()) {
                    getCourseTree().updateTree();
                }
                
                if(isWidget instanceof Summernote) {
                    saveDescriptionCmd.execute();
                } else {
                    saveCourseCmd.execute();
                }
                
                if(propertiesDialogClosedCmd != null) {
                    propertiesDialogClosedCmd.execute();
                }
            }
        };
        
        coursePropertiesReuseDialog.setCourseObjectWidget(title, isWidget);
        if(!readOnlyLabel.isVisible()) {
            coursePropertiesReuseDialog.setSaveButtonHandler(saveHandler);
            coursePropertiesReuseDialog.setSaveAndCloseButtonVisible(true);
            coursePropertiesReuseDialog.setCancelButtonHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    if(propertiesDialogClosedCmd != null) {
                        propertiesDialogClosedCmd.execute();
                    }
                }
            });
        }
        else {
            coursePropertiesReuseDialog.setModalTitle(coursePropertiesReuseDialog.getModalTitle() + " (Read Only)");
            coursePropertiesReuseDialog.setSaveAndCloseButtonVisible(false);
            
        }
        coursePropertiesReuseDialog.show();
    }
    
    @Inject
    private void init(){
        
        // Default the lock label to hidden.
        showLockLabel(false);
        courseHeaderEditor.setNameLimit(MAX_LENGTH);
        
        mediaListCellTable.setPageSize(Integer.MAX_VALUE);
        mediaListCellTable.addColumn(mediaFilesColumn);
        mediaListCellTable.setEmptyTableWidget(new HTML("No media files have been added yet. Click here to upload media files."));
        mediaListDataProvider.addDataDisplay(mediaListCellTable);        
                
        propertiesPanel.insert(getHeaderView().getCoursePropertiesWidget(), 0);
        propertiesLvl2Deck.add(getHeaderView().getConfigurationsWidget());
        propertiesLvl3Deck.add(getHeaderView().getLTIConsumerPropertiesPanel());
        propertiesLvl3Deck.add(getHeaderView().getLTIProviderPropertiesPanel()); 
       
        
        getHeaderView().setLtiConsumerClickHandler(new ClickHandler(){
            
            @Override
            public void onClick(ClickEvent event) {
                showDialogWidget("Edit LTI Providers", getHeaderView().getLtiProvidersPanel());
            }
        });
        
        advancedPanel.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                if(advancedCollapse.isShown()){
                    arrowIcon.setType(IconType.CHEVRON_RIGHT);
                    
                } else {
                    arrowIcon.setType(IconType.CHEVRON_DOWN);
                }
                
                advancedCollapse.toggle();
            }
        });
        
        ClickHandler mediaClickHandler = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                mediaDialog.show(mediaListDataProvider.getList());
            }
            
        };
        
        mediaDialog.addHideHandler(new ModalHideHandler() {

            @Override
            public void onHide(ModalHideEvent event) {
                refreshMediaList();
            }
            
        });        
        
        mediaListCellTable.addDomHandler(mediaClickHandler, ClickEvent.getType());        
    }
    
    @Override
    public void refreshMediaList() {
        
	    contentDeckPanel.showWidget(contentDeckPanel.getWidgetIndex(contentLoadingPanel));
		
        FetchMediaFiles action = new FetchMediaFiles();
        action.setUserName(GatClientUtility.getUserName());
        action.setCourseFolderPath(GatClientUtility.getBaseCourseFolderPath());
        
        SharedResources.getInstance().getDispatchService().execute(action, 
                new AsyncCallback<FetchMediaFilesResult>() {

            @Override
            public void onFailure(Throwable t) {
                WarningDialog.error("Error Retrieving Media", "A server error occurred while attempting to "
                        + "retrieve media files for this course: " + t.getMessage());
                contentDeckPanel.showWidget(contentDeckPanel.getWidgetIndex(mediaSummaryPanel));
            }

            @Override
            public void onSuccess(FetchMediaFilesResult result) {
                if(result.isSuccess()) {
                    mediaListDataProvider.getList().clear();
					List<FileTreeModel> fileList = new ArrayList<FileTreeModel>(result.getFileMap().keySet());
					Collections.sort(fileList, new Comparator<FileTreeModel>() {

                        @Override
                        public int compare(FileTreeModel o1, FileTreeModel o2) {
                            return o1.getFileOrDirectoryName().compareTo(o2.getFileOrDirectoryName());
                        }
					    
					});
        			for(FileTreeModel file : fileList) {
                        SelectableMediaWidget mediaFile = new SelectableMediaWidget(file);
                        mediaListDataProvider.getList().add(mediaFile);
        				mediaDialog.mediaNameToFileUrl.put(mediaFile.getFileName(), result.getFileMap().get(file));
                    }
                } else {
                    ErrorDetailsDialog dialog = new ErrorDetailsDialog(result.getErrorMsg(), result.getErrorDetails(), result.getErrorStackTrace());
                    dialog.setText("Error Retrieving Media");
                    dialog.center();
                }
                contentDeckPanel.showWidget(contentDeckPanel.getWidgetIndex(mediaSummaryPanel));
            }

        }); 
    }
    
    @Override
    public HeaderView getHeaderView(){
        return courseHeaderEditor;
    } 
    
    @Override 
    public void setFileSaveCommand(final ScheduledCommand command){
        saveCourseImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(!readOnlyLabel.isVisible()) {
                    command.execute();
                }
            }            
        });
        saveCourseCmd = command;
    }
    
    @Override
    public void setSaveDescriptionCommand(final ScheduledCommand command) {
        saveDescriptionCmd = command;
    }
    
    @Override
    public String getEditorDescription() {
        return descriptionEditor.getCode();
    }
    
    @Override 
    public void setFileDiscardChangesCommand(final ScheduledCommand command){
        
        refreshCourseImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
            
                OkayCancelDialog.show(
                        "Discard Unsaved Changes?", 
                        "Are you sure you want to discard your unsaved changes and reload this course?"
                        + "<br/><br/>"
                        + "All changes made since your last save will be permanently lost.",
                        "Yes, Discard Changes", 
                        new OkayCancelCallback() {
    
                            @Override
                            public void okay() {
                                command.execute();
                            }
    
                            @Override
                            public void cancel() {
                                //Nothing to do
                            }
                        });
            }
        });
    }
    
    @Override 
    public void setFileSaveAndValidateCommand(final ScheduledCommand command){
        validateCourseImage.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(!readOnlyLabel.isVisible()) {
                    command.execute();
                }
            }
        });
    }
    
    @Override
    public void setUnlockCourseCommand(final ScheduledCommand command) {
        lockedCourseIcon.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                command.execute();
            }
            
        });     
    }
    
    @Override
    public HasClickHandlers getShowUserActionListButton() {
        return accessTableIcon;
    }
    
    @Override
    public void setUserAccessList(List<CourseFileUserPermissionsDetails> userAccessList) {
        userAccessModal.setAccessList(new ArrayList<>(userAccessList));
    }
    
    @Override
    public void showUserAccessList() {
        userAccessModal.show();
    }
        
    @Override
    public void displayDescriptionEditor() {
        descriptionEditor.setCode(getHeaderView().getDescriptionInput().getValue()); 
        showDialogWidget("Edit Description", descriptionEditor);
    }

    /* (non-Javadoc)
     * @see mil.arl.gift.tools.authoring.gat.client.view.course.CourseView#initializeView()
     */
    @Override
    public void initializeView() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public HasValue<String> getCourseNameButton(){
        return courseNameButton;
    }
    
    @Override
    public void showReadOnlyLabel(boolean show) {
        readOnlyLabel.setVisible(show);
        courseNameButton.getElement().getStyle().setProperty("paddingLeft", (show ? "0px" : "13px"));
        if(show) {
            saveCourseImage.setTitle("Unavailable in Read-Only mode");
            saveCourseImage.setResource(GatClientBundle.INSTANCE.save_disabled());
            saveCourseButton.removeStyleName("toolbarButton");
            saveCourseButton.setStyleName("disabledToolbarButton");
            
            
            validateCourseImage.setTitle("Unavailable in Read-Only mode");
            validateCourseImage.setResource(GatClientBundle.INSTANCE.clean_disabled());
            validateCourseButton.removeStyleName("toolbarButton");
            validateCourseButton.setStyleName("disabledToolbarButton");
            
        } else {
            saveCourseImage.setTitle("Save Course");
            saveCourseImage.setResource(GatClientBundle.INSTANCE.save_enabled());
            saveCourseButton.removeStyleName("disabledToolbarButton");
            saveCourseButton.setStyleName("toolbarButton");
            
            validateCourseImage.setTitle("Validate Course");
            validateCourseImage.setResource(GatClientBundle.INSTANCE.clean());
            validateCourseButton.removeStyleName("disabledToolbarButton");
            validateCourseButton.setStyleName("toolbarButton");
        }
    }
    
    @Override 
    public void showLockLabel(boolean show) {
        
        lockedCourseButton.setVisible(show);
       
    }

//    @Override
//    public void clearView() {
//        selectionModel.clear();
//    }    
    
    @Override
    public void hideCourseObjectModal() {
        coursePropertiesReuseDialog.hide();
    }
    
    @Override
    public void setReadOnly(boolean readOnly){
        
        showReadOnlyLabel(readOnly);
        mediaDialog.setReadOnly(readOnly);
        DefaultGatFileSelectionDialog.setReadOnly(readOnly);
		
		if(readOnly) {
		    
		    //Hide tooltips on course objects in the course objects panel
		    guidanceButton.setTitle("");
		    aarButton.setTitle("");
		    mbpButton.setTitle("");
		    ltiButton.setTitle("");
		    surveyButton.setTitle("");
		    lmButton.setTitle("");
		    slideShowButton.setTitle("");
		    pdfButton.setTitle("");
		    youtubeButton.setTitle("");
		    imageButton.setTitle("");
		    webAddressButton.setTitle("");
		    localWebpageButton.setTitle("");
		    videoButton.setTitle("");
		    taButton.setTitle("");
		    vbsButton.setTitle("");
		    tc3Button.setTitle("");
		    testbedButton.setTitle("");
		    aresButton.setTitle("");
		    exampleAppButton.setTitle("");
		    autoTutorButton.setTitle("");
		    conversationTreeButton.setTitle("");
		    questionBankButton.setTitle("");
		    authoredBranchButton.setTitle("");
		}
		
		//Hides the instructions in the course object panel
	    courseObjectPanelInstructions.setVisible(!readOnly);
		
		//Hides the tooltip on the control used to edit the course name
	    courseNameButton.setEditingEnabled(!readOnly);
    }
    
    @Override
    public void createCourseTree(Course course){
        getCourseTree().loadTree(course);
    }
    
    @Override
    public Widget getAddGuidanceButton(){
        return guidanceButton;
    }
    
    @Override
    public Widget getAddTAButton(){
        return taButton;
    }
    
    @Override
    public Widget getAddVbsButton(){
        return vbsButton;
    }
    
    @Override
    public Widget getAddTc3Button(){
        return tc3Button;
    }
    
    @Override
    public Widget getAddTestbedButton(){
        return testbedButton;
    }
    
    @Override
    public Widget getAddAresButton(){
        return aresButton;
    }
    
    @Override
	public Widget getAddUnityButton() {
		return unityButton;
	}
	
	@Override
    public Widget getAddMobileAppButton() {
        return mobileAppButton;
    }
	
	@Override
    public Widget getAddVREngageButton() {
        return vrEngageButton;
    }
	
	@Override
    public Widget getAddUnityStandaloneButton() {
        return unityStandaloneButton;
    }
	
	@Override
	public Widget getAddHAVENButton() {
	    return havenButton;
	}
	
	@Override
    public Widget getAddRIDEButton() {
        return rideButton;
    }
	
	@Override
    public Widget getAddExampleAppButton(){
        return exampleAppButton;
    }
    
    @Override
    public Widget getAddAARButton(){
        return aarButton;
    }
    
    @Override
    public Widget getAddMBPButton(){
        return mbpButton;
    }
    
    @Override
    public Widget getAddLTIButton(){
        return ltiButton;
    }
    
    @Override
    public Widget getAddLessonMaterialButton(){
        return lmButton;
    }
    
    @Override
    public Widget getAddSlideShowButton(){
        return slideShowButton;
    }
    
    @Override
    public Widget getAddImageButton(){
        return imageButton;
    }
    
    @Override
    public Widget getAddPDFButton(){
        return pdfButton;
    }
    
    @Override
    public Widget getAddLocalWebpageButton(){
        return localWebpageButton;
    }
    
    @Override
    public Widget getAddVideoButton(){
        return videoButton;
    }
    
    @Override
    public Widget getAddWebAddressButton(){
        return webAddressButton;
    }
    
    @Override
    public Widget getAddYoutubeVideoButton(){
        return youtubeButton;
    }
    
    @Override
    public Widget getAddSurveyButton(){
        return surveyButton;
    }
    
    @Override
    public Widget getAddAutoTutorButton(){
        return autoTutorButton;
    }
    
    @Override
    public Widget getAddConversationTreeButton(){
        return conversationTreeButton;
    }
    
    @Override
    public Widget getAddQuestionBankButton(){
        return questionBankButton;
    }
    
    @Override
    public Widget getAddAuthoredBranchButton(){
        return authoredBranchButton;
    }
    
    @Override
    public CourseTree getCourseTree(){
        return treeManager.getBaseCourseTree();
    }
    
    @Override
    public Label getCoursePathLabel() {
        return coursePathLabel;
    }
    
    @Override
    public HTML getDiskSpaceLabel(){
        return diskSpaceLabel;
    }
    
    @Override
    public CourseObjectModal getGiftWrapDialog(){
        return giftWrapDialog;
    }

    @Override
    public HTML getHelpHTML() {
        return helpTextHtml;
    }
    
    @Override
    public CourseObjectEditorPanel getEditorPanel(){
        return editorPanel;
}
    
    @Override
    public TreeManager getTreeManager(){
        return treeManager;
    }
    
    @Override
    public void showConceptsEditor(Concepts concepts) {
        
        getHeaderView().getConceptsPanel().edit(concepts);
        
        showDialogWidget("Edit Course Concepts", getHeaderView().getConceptsPanel());
    }
    
    @Override
    public void setPropertiesDialogClosedCommand(Command command) {
        this.propertiesDialogClosedCmd = command;
    }

    @Override
    public void editExternalScenarioDkf(Serializable transition) {
        editorPanel.startEditing(transition);
    }
}
