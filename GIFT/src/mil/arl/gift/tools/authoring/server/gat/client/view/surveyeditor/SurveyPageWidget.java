/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Well;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SaveSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AbstractSelectSurveyItemWidget.SurveyItemType;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AddSurveyDialog.SurveyDialogOption;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyAddSurveyItemEvent.InsertOrder;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyDeletePageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyMovePageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveyScrollToElementEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySelectPageEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.SurveySelectQuestionEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.widgets.CollapseButton;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;


/**
 * The survey page widget holds the data for a single page of surveys.  
 * 
 * @author nblomberg
 *
 */
public class SurveyPageWidget extends Composite  {

    private static Logger logger = Logger.getLogger(SurveyPageWidget.class.getName());
    
    private final static String PAGE_TITLE_PLACEHOLDER = "Enter Page Title";
    private final static String READ_ONLY_MODE_PLACEHOLDER = "Cannot edit Page Title in read-only mode";

	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, SurveyPageWidget> {
	}

	
	/** Enum for the order to move items (pages/questions). */
    public enum MoveOrderEnum {
        ORDER_UP,
        ORDER_DOWN,
    }

    @UiField
	CollapseButton collapseButton;
	
	@UiField
	FlowPanel pageContentContainer;
	
	@UiField
	Well pageHeader;
	
	@UiField
	Label pageNumberLabel;
	
	@UiField
	Button deletePage;
	
	@UiField
	Button movePageUp;
	
	@UiField
	Button movePageDown;
	
	@UiField
	EditableInlineLabel pageName;
	
	/** The SurveyPage data that backs the widget */
	private SurveyPage surveyPage;

	/** The internal pageId for the widget.  This should be unique across all SurveyPageWidgets.  */
	private int pageId = -1;
	
	/** The page number */
    private int pageNumber = -1;
	
	private SurveyWidgetId widgetId = null;
	
	/** Indicator if the page is selected.  There should only be one selected page at a time. */
	private boolean isSelected = false;
	
	/** An instance of the select survey item widget. This widget allows for the author to select a new widget to be added to the page. */
	private AbstractSelectSurveyItemWidget selectSurveyItemWidget = null;
	
	/**  Indicates if the page should display the select survey item widget. */
	private boolean showSelectSurveyItemWidget = false;
	
	/** The question widget id that an item will be added before or after when inserting a new item. */
	private SurveyWidgetId insertQuestionId = null;
	
	/** The order (above or below) that an item will be inserted on the page. */
	private InsertOrder insertOrder = InsertOrder.ORDER_AFTER;
	
	/** The current count of the question items on the page. */
	private int questionCount = 0;
	
	/** 
	 * The currently selected questions (if any) in the order they were selected.  
	 * Can be empty if no questions are selected or if the page has no items. 
	 */
	List<QuestionContainerWidget> selectedQuestions = new ArrayList<QuestionContainerWidget>();
	
	/** The current mode of the page (writing or scoring mode). */
	private SurveyEditMode editMode;
	
	/** The type of survey being edited. */
	private SurveyDialogOption surveyType;
	
	/** The set of global survey resources (i.e. survey context, concepts, etc.) that should be referred to while editing surveys */
	private AbstractSurveyResources surveyResources = null;

	/** The database id of the page (if it exists).  0 implies a new page in the database. */
    private int dbId = 0;

    /** Whether or not this widget is in read-only mode */
	private boolean isReadOnly = false;

	/** Callback for when the edit mode has been changed for all elements on the page */
	public interface SurveyPageEditModeChanged {
	    public void onEditModeChangeComplete();
	}
	
	/**
	 * Constructor (default)
	 */
	public SurveyPageWidget(SurveyEditMode mode, SurveyDialogOption type) {
	    
	    logger.info("SurveyPageWidget(mode=" + mode + ", type=" + type + ")");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    setEditMode(mode);
	    setSurveyType(type);
	    
	    widgetId = new SurveyWidgetId();
	    
	    collapseButton.setCollapsed(false);
	    
	    pageName.setTooltipPlacement(Placement.RIGHT);
	    
	    collapseButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                logger.info("pageWidget button click handler()");
                // Hide the page content if we are collapsed
                // Show the page content if we are not collapsed.
                setCollapsed(collapseButton.isCollapsed());
                
            }
	        
	    });
	    
	    deletePage.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                logger.warning("onMouseDownEvent for delete page");
                
                OkayCancelDialog.show("Delete Page", 
                        "Are you sure you want to <b>permanetly</b> delete the page and all of its questions?", 
                        "Delete", 
                        new OkayCancelCallback() {

                            @Override
                            public void okay() {
                                //Deletes the page
                                SharedResources.getInstance().getEventBus().fireEvent(new SurveyDeletePageEvent(widgetId));
                            }

                            @Override
                            public void cancel() {
                                //Do nothing                                
                            }
                            
                });
                                
                // This prevents the page header onClick() from selecting the widget.
                event.stopPropagation();
            }
	        
	    });
	    
	    
	    movePageUp.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyMovePageEvent(widgetId, MoveOrderEnum.ORDER_UP));
                
                // This prevents the page header onClick() from selecting the widget.
                event.stopPropagation();
                
            }
	        
	    });
	    
	    movePageDown.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyMovePageEvent(widgetId, MoveOrderEnum.ORDER_DOWN));
                
                // This prevents the page header onClick() from selecting the widget.
                event.stopPropagation();
            }
	        
	    });
	    
	    ClickHandler pageClick = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                logger.info("page header was clicked.");

                SharedResources.getInstance().getEventBus().fireEvent(new SurveySelectPageEvent(widgetId));
                
            }
	        
	    };
	   
	    pageHeader.addDomHandler(pageClick, ClickEvent.getType());
	    
	    
	    pageContentContainer.clear();
	    
	    setSelected(false);
	    
	    // For question banks, the page header is always hidden. and there is only
	    // always one hidden page that the items are being added to.
	    if (type == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
	        pageHeader.setVisible(false);
	    }
	    
	    
	    if (this.surveyType == SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK) {
	        selectSurveyItemWidget = new SelectSurveyItemQuestionBankWidget();
	    } else {
	        selectSurveyItemWidget = new SelectSurveyItemWidget();
	    }
	    
	    
	    rebuildPage();
	    
	    

	}
	
    /**
     * Accessor to return if the widget is collapsed.
     * 
     * @return true if the widget is collapsed, false otherwise.
     */
    public boolean isCollapsed() {
        return collapseButton.isCollapsed();
    }
    
    /**
     * Accessor to return the SurveyPage
     * 
     * @return the SurveyPage
     */
    public SurveyPage getSurveyPage() {
        return surveyPage;
    }

    /** 
     * 
     * Sets the pageId of the widget.  This identifier should be unique across all other SurveyPageWidget objects.
     * 
     * @param id - The identifier for the widget.
     */
    private void setPageId(int id) {
        pageId = id;
        
    }
    
    /**
     * Sets the page number
     * 
     * @param pageNumber The page number that will be displayed in the survey page header
     */
    public void setPageNumber(int pageNumber) {
        pageNumberLabel.setText("Page " + pageNumber);
        this.pageNumber = pageNumber;
    }
    
     /**
     * Gets the pageId for the widget.  The pageId should be unique across all other SurveyPageWidget objects.
     * 
     * @return int - The identifier of the widget.
     */
    public int getPageId() {
        return pageId;
    }

    
    
    /**
     * Sets the page to be in collapsed mode (true to set the page to collapsed, false
     * to make the page contents uncollapsed).
     * 
     * @param collapsed - True to collapse the page, false, to uncollapse the page.
     */
    public void setCollapsed(boolean collapsed) {
        collapseButton.setCollapsed(collapsed);
        
        pageContentContainer.setVisible(!collapseButton.isCollapsed());
    }

    /**
     * Sets the page to be selected.
     * 
     * @param selected - True to select the page, false to unselect the page.
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
        
        // TODO - For now changing the style of the collapse button icon will
        // signify which page is selected.
        if (isSelected) {
            collapseButton.setType(ButtonType.PRIMARY);
        } else {
            collapseButton.setType(ButtonType.INFO);
        }
        
        
    }
    
    /**
     * Displays the 'Select Survey Item' widget which allows the author to 
     * add a new survey item to the survey page.
     */
    public void showSelectSurveyItemWidget(SurveyWidgetId questionId, InsertOrder order) {
        
        
        insertQuestionId = questionId;
        insertOrder = order;
        showSelectSurveyItemWidget = true;
        
        // If the page is collapsed, then uncollapse it so the dialog is shown.
        if (isCollapsed()) {
            setCollapsed(false);
        }
        
        rebuildPage();
        
    }


    /**
     * Hides the 'Select Survey Item' widget and rebuilds the page.
     */
    public void hideSelectSurveyItemWidget() {
        
        showSelectSurveyItemWidget = false;

        rebuildPage();        
    }


    /**
     * Handler for when a new survey item has been selected from
     * the 'Select Survey Item' widget.
     * @param itemType
     */
    public void onNewSurveyItem(SurveyItemType itemType) {
        logger.info("onNewSurveyItem: " + itemType);
        
        // Create the widget that is needed.
        AbstractQuestionWidget widget = SurveyWidgetFactory.createSurveyWidget(
        		itemType, 
        		editMode, 
        		!getSurveyType().equals(SurveyDialogOption.COLLECTINFO_NOTSCORED),
        		isReadOnly,
        		getSurveyType().equals(SurveyDialogOption.ASSESSLEARNER_QUESTIONBANK)
        );
        
        if(widget != null){
            // Add widget to survey page        
            widget.setReadOnlyMode(isReadOnly);
        
            addWidgetToSurveyPage(widget, true);
        }
        
        hideSelectSurveyItemWidget();
        
        if(widget != null){
        
            if(!(widget instanceof InformativeTextWidget)){
            	
            	//update the page's question numbers if a new question is added
            	updateQuestionNumbers();        
            }
        
            if(editMode == SurveyEditMode.WritingMode) {
            	widget.startEditing();
            }
        }
    }
    
    
    /**
     * Adds a Question Widget to the Survey Page but can optionally fire the events.  Not firing the events should 
     * typically only be used in places where there are many items being inserted at once (such as during the loading
     * of a survey), where the caller may externally fire events once the bulk items are added).
     * 
     * @param widget - The widget to be added to the survey page in the UI.
     * @param isNew - if this is a new question being added or an existing one being loaded
     * @param fireEvents - True if the events should be fired (should typically be used when user adds a single widget to the page).
     *                     False if the events should not be fired (should typically be used when bulk items are being added and the
     *                     caller will manually fire the events externally once all the items are added.)
     */
    public SurveyWidgetId addWidgetToSurveyPage(Widget widget, boolean isNew, boolean fireEvents) {
        SurveyWidgetId toRet = null;
        
        // Add it to the question container widget.
        if (widget != null) {
            QuestionContainerWidget qWidget = new QuestionContainerWidget(this.getWidgetId(), getEditMode(), getSurveyType());
            qWidget.setReadOnlyMode(isReadOnly);
            
            if(!(widget instanceof InformativeTextWidget)) {
            	questionCount++;
            }
            qWidget.initialize(widget, surveyResources, questionCount, isNew);
            toRet = qWidget.getWidgetId();
            
            logger.info("Finished initializing the question widget.");
            if (insertQuestionId == null) {
                pageContentContainer.add(qWidget);
            } else {
                
                // This should always contain 1 AddSurveyItemWidget at the time a new item is inserted.
                int widgetCount = pageContentContainer.getWidgetCount();
                int questionWidgetCount = widgetCount - 1;
                for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
                    Widget iterWidget = pageContentContainer.getWidget(x);
                    
                    if (iterWidget instanceof QuestionContainerWidget) {
                        QuestionContainerWidget curWidget = (QuestionContainerWidget)iterWidget;
                        
                        if (curWidget.getWidgetId().equals(insertQuestionId)) {
                            
                            if (insertOrder == InsertOrder.ORDER_BEFORE) {
                                pageContentContainer.insert(qWidget, x);
                            } else {
                                
                                if (x < questionWidgetCount) {
                                    pageContentContainer.insert(qWidget,  x+1);
                                } else {
                                    pageContentContainer.add(qWidget);
                                }
                                
                                
                            }
                           
                            break;
                        }
                    } 
                }
            }

            // Send notifications out about the newly added item.
            if (fireEvents) {
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyScrollToElementEvent(qWidget.getWidgetId().getWidgetId()));
                SharedResources.getInstance().getEventBus().fireEvent(new SurveySelectQuestionEvent(widgetId, qWidget.getWidgetId()));
                SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
            }
        }
        
        return toRet;
    }
    
    /**
     * Adds a Question Widget to the Survey Page and fires the appropriate events.  This should be
     * typically used when the user adds a single question element to the page since the events should
     * be fired.
     * 
     * @param widget - The widget to be added to the survey page in the UI.
     * @param isNew - if this is a new question being added or an existing one being loaded
     */
    public void addWidgetToSurveyPage(Widget widget, boolean isNew) {
        
        // Automatically sets the flag to fire the notificaitons.
        addWidgetToSurveyPage(widget, isNew, true);
    }
    
    /**
     * Adds a Question Widget to the Survey Page and fires the appropriate events.  This should be
     * typically used when the user adds a single question element to the page since the events should
     * be fired.
     * 
     * @param widget - The widget to be added to the survey page in the UI.
     * @param isNew - if this is a new question being added or an existing one being loaded
     * @param insertQuestionId - the ID of the survey widget the widget should be added around
     * @param insertOrder - the order in which the widget should be added (i.e. before or after)
     */
    public void addWidgetToSurveyPage(Widget widget, boolean isNew, SurveyWidgetId insertQuestionId, InsertOrder insertOrder) {
    	
    	this.insertQuestionId = insertQuestionId;
    	this.insertOrder = insertOrder;
    	
        addWidgetToSurveyPage(widget, isNew);
        
        updateQuestionNumbers();        
    }
    
    /**
     * Rebuilds the page based on the items that are contained within the page.
     */
    public void rebuildPage() {
        
        // Set the page contents visibility based on if the page is collapsed or not.
        pageContentContainer.setVisible(!isCollapsed());
        
        // Remove any addSurveyItemWidget and then readd in the proper place.
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            
            if (iterWidget instanceof AbstractSelectSurveyItemWidget) {
                iterWidget.removeFromParent();
            }
        }
        
        
        
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget curWidget = (QuestionContainerWidget)iterWidget;
                
                boolean insertAddSurveyWidget = false;
                if (insertQuestionId != null &&
                    curWidget.getWidgetId().equals(insertQuestionId)) {
                    insertAddSurveyWidget = true;
                }
                
                if (showSelectSurveyItemWidget && 
                        insertAddSurveyWidget && insertOrder == InsertOrder.ORDER_BEFORE) {
                    addSurveyItemWidget(x);
                    break;
                } else if (showSelectSurveyItemWidget && 
                        insertAddSurveyWidget && insertOrder == InsertOrder.ORDER_AFTER) {
                    
                    
                    addSurveyItemWidget(x+1);
                   
                    
                    break;
                } 
            }
        }
        
        if (showSelectSurveyItemWidget && insertQuestionId == null) {
            addSurveyItemWidget(pageContentContainer.getWidgetCount());
        }
            
            
    }
    
    
    private void addSurveyItemWidget(int x) {
        logger.info("Adding survey item widget to survey page.");
        selectSurveyItemWidget.setParentPageWidget(this);
        
        if (x < pageContentContainer.getWidgetCount()) {
            pageContentContainer.insert(selectSurveyItemWidget, x);
        } else {
            pageContentContainer.add(selectSurveyItemWidget);
        }
        
        
        
        
        if (selectSurveyItemWidget != null && selectSurveyItemWidget.getWidgetId() != null) {
            
            String widgetId = selectSurveyItemWidget.getWidgetId().getWidgetId();
            SharedResources.getInstance().getEventBus().fireEvent(new SurveyScrollToElementEvent(widgetId));
            
            logger.info("Firing SurveyScrollToElementEvent: " + widgetId);
        } else {
            logger.info("Unable to fire survey scroll to element event.");
        }
        
    }


    /**
     * Gets the widget id of the page.
     * 
     * @return SurveyWidgetId - The widget id of the page.
     */
    public SurveyWidgetId getWidgetId() {
        return widgetId;
    }


    /**
     * Moves a survey question up or down in order on the page.
     * 
     * @param questionId - The widget id of the question to be moved.
     * @param pageOrder - The order (up or down) that the question will be moved.
     */
    public void moveSurveyQuestion(SurveyWidgetId questionId, MoveOrderEnum pageOrder) {
        
        //Counts the number of info items
        int iCount = 0;
        
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                
                if(qWidget.getQuestionWidget() instanceof InformativeTextWidget) {
                    iCount++;
                }
                
                if (qWidget.getWidgetId().equals(questionId)) {
                    
                    qWidget.removeFromParent();
                    int widgetCount = pageContentContainer.getWidgetCount();
                    
                    if (pageOrder == MoveOrderEnum.ORDER_UP) {
                        
                        int moveIndex = x - 1;
                        
                        if (moveIndex < 0) {
                            moveIndex = 0;
                        }
                        
                        // Move the question up
                        pageContentContainer.insert(qWidget, moveIndex);
                        
                        //Updates the labels
                        if(pageContentContainer.getWidget(x) instanceof QuestionContainerWidget){
                        	
                        	QuestionContainerWidget otherWidget = (QuestionContainerWidget) pageContentContainer.getWidget(x);
                        	                        
	                        if(!(qWidget.getQuestionWidget() instanceof InformativeTextWidget ||
	                                otherWidget.getQuestionWidget() instanceof InformativeTextWidget)) {
	                        	
	                            qWidget.setQuestionNum(moveIndex + 1 - iCount);
	                            
	                            if(otherWidget != null) {
	                                otherWidget.setQuestionNum(x + 1 - iCount);
	                            }
	                        }
                        }
                        
                    } else {
                    	
                        int moveIndex = x + 1;
                        if (moveIndex >= widgetCount) {
                            pageContentContainer.add(qWidget); 
                            moveIndex = pageContentContainer.getWidgetCount() - 1;
                        } else {
                            pageContentContainer.insert(qWidget,  moveIndex);
                        }
                        
                        //Updates the labels
                        if(pageContentContainer.getWidget(x) instanceof QuestionContainerWidget){
                        	
                        	QuestionContainerWidget otherWidget = (QuestionContainerWidget) pageContentContainer.getWidget(x);
                        	                        
	                        if(!(qWidget.getQuestionWidget() instanceof InformativeTextWidget ||
	                                otherWidget.getQuestionWidget() instanceof InformativeTextWidget)) {
	                        	
	                            qWidget.setQuestionNum(moveIndex + 1 - iCount);
	                            
	                            if(otherWidget != null) {
	                                otherWidget.setQuestionNum(x + 1 - iCount);
	                            }
	                        }
                        }
                    }
                    
                    // The bootstrap slider widget will get reset to default values when the 
                    // element is unloaded (ie. the call to pageContentContainer.clear()).
                    // What we need to do is refresh the widget with the default values since
                    // the underlying bootstrap code does not remember the state when reattaching
                    // an existing element to the DOM.
                    if (qWidget.getQuestionWidget() instanceof SliderBarWidget) {
                        SliderBarWidget sliderBar = (SliderBarWidget)qWidget.getQuestionWidget();
                        sliderBar.refresh();
                        
                    }
                    
                    break;
                }
            }
        }
    }


    /**
     * Deletes the question from the page.
     * 
     * @param questionId - The widget id of the question to be deleted.
     */
    public void deleteQuestion(SurveyWidgetId questionId) {
        //Counts the number of questions that have been passed
        int qCount = 1;
        
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                
                // Counts the question if it isn't an informative text item
                if(!(qWidget.getQuestionWidget() instanceof InformativeTextWidget)) {
                    qCount++;
                }
                
                // If the widget being deleted was selected, then clear the selected widget.
                if (qWidget.getWidgetId().equals(questionId)) {
                    if (selectedQuestions.contains(qWidget)) {
                        selectedQuestions.remove(qWidget);
                    }
                    qWidget.removeFromParent();
                    rebuildPage();
                    
                    if(qWidget.getQuestionWidget() instanceof InformativeTextWidget) {
                        updateQuestionNumbers(x, qCount);
                    } else {
                        updateQuestionNumbers(x, qCount - 1);
                    }
                    
                    questionCount--;
                    break;
                } 
            }
        }
        
    }


    /**
     * Selects a question based on the question id.
     * 
     * @param questionId - The widget id of the question to be selected.
     * @param keepPreviousSelection - Whether or not any previously selected questions should be deselected.
     * 
     * @return true if the questionId matches the only selected question; false if it doesn't match or if the selected question size isn't 1.
     */
    public boolean selectQuestion(SurveyWidgetId questionId, boolean keepPreviousSelection) {
        
        if (!keepPreviousSelection) {
        	for(QuestionContainerWidget question: selectedQuestions){
        		
        		if(questionId == null 
        				|| !questionId.equals(question.getWidgetId())){
        			
        			//deselect any previously selected questions except for the one being selected now
        			question.setSelected(false);
        		}
        	}
        }
        
        boolean questionIdPreviouslySingleSelected = false;
        if (questionId != null) {
            questionIdPreviouslySingleSelected = selectedQuestions.size() == 1 && questionId.equals(selectedQuestions.get(0).getWidgetId());
            
            // Select the question based on the widget id passed in.
            for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
                Widget iterWidget = pageContentContainer.getWidget(x);
                if (iterWidget instanceof QuestionContainerWidget) {
                    QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                    if (qWidget.getWidgetId().equals(questionId)) {
                        
                         if(!keepPreviousSelection){
                                                         
                             qWidget.setSelected(true);
                            
                            //if we aren't keeping previous selections, clear out the list of selected questions
                            selectedQuestions.clear();   
                            
                            if(!selectedQuestions.contains(qWidget)){
                                
                                //if this question hasn't been selected yet, select it
                                selectedQuestions.add(qWidget);
                                
                            } else {
                                
                                //if this question has been selected, make it the last selected question in the list
                                selectedQuestions.remove(qWidget);
                                selectedQuestions.add(qWidget);
                            }
                            
                         } else {
                             
                             //multi-select is enabled, so toggle this question so that it can be deselected.
                             boolean isSelected = selectedQuestions.contains(qWidget);
                             
                             qWidget.setSelected(!isSelected);
                             
                             if(!isSelected){
                                
                                //if this questions hasn't been selected yet, select it
                                selectedQuestions.add(qWidget);
                                
                             } else {
                                
                                //if this question has been selected, deselect it
                                selectedQuestions.remove(qWidget);
                             }
                             
                         }
                        
                        
                        
                        logger.info("Selecting question id: " + questionId);
                    }
                }
            }
        } else {
            selectedQuestions.clear();
        }
        
        return questionIdPreviouslySingleSelected;
    }


    /**
     * Gets the last selected question widget for the page. Returns null if there is no item selected.
     * 
     * @return AbstractQuestionWidget - Returns the widget for the last selected item on the page.  Returns null if no item is selected.
     */
    public AbstractQuestionWidget getLastSelectedQuestionWidget() {
        
        AbstractQuestionWidget qWidget = null;
        
        if (!selectedQuestions.isEmpty()) {
        	
        	QuestionContainerWidget lastSelectedQuestion = selectedQuestions.get(selectedQuestions.size() - 1);
        	
            Widget selectedWidget = lastSelectedQuestion.getQuestionWidget();
            if (selectedWidget != null && selectedWidget instanceof AbstractQuestionWidget) {
                qWidget = (AbstractQuestionWidget) selectedWidget;
            }
        }
        return qWidget;
    }

    /**
     * Gets the selected question widgets for the page. Returns null if there are no items selected.
     * 
     * @param useVerticalOrdering whether or not the returned widgets should be sorted according to their vertical ordering in this
     * page widget. If set to false, the returned widgets will be sorted in the order they were selected in, with the newest selected
     * widget coming last in the result.
     * @return AbstractQuestionWidget - Returns the widget for the last selected item on the page.  Returns null if no item is selected.
     */
    public List<QuestionContainerWidget> getSelectedQuestionWidgets(boolean useVerticalOrdering) {
              
        if(useVerticalOrdering){
            
            List<QuestionContainerWidget> selected = new ArrayList<QuestionContainerWidget>();
            
            for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
                
                Widget iterWidget = pageContentContainer.getWidget(x);
                
                if (iterWidget instanceof QuestionContainerWidget
                        && selectedQuestions.contains(iterWidget)) {
                    
                    QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                    
                    selected.add(qWidget);
                }             
            }
            
            return selected;
            
        } else {
            
            return new ArrayList<QuestionContainerWidget>(selectedQuestions);
        }
    }


    /**
     * Called when the survey editor mode has changed (writing mode to scoring mode).
     * 
     * @param editorMode - The new editor mode (writing or scoring mode).
     */
    public void onEditorModeChanged(SurveyEditMode editorMode, final SurveyPageEditModeChanged callback) {
        
        setEditMode(editorMode);
        
        /*
         * Using scheduleIncremental to force the gui to update in between survey elements. Otherwise, 
         * all gui changes are queued and prevent the progress dialog from updating correctly 
         */
        Scheduler.get().scheduleIncremental(new RepeatingCommand() {
            int widgetCounter = 0;

            @Override
            public boolean execute() {
                
                if(widgetCounter < pageContentContainer.getWidgetCount()) {

                    Widget iterWidget = pageContentContainer.getWidget(widgetCounter);
                    widgetCounter += 1;
                    
                    if (iterWidget instanceof QuestionContainerWidget) {

                        QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                        qWidget.onEditorModeChanged(editMode);   
                        SurveyEditorPanel.updateEditModeProgress(pageNumber, widgetCounter, pageContentContainer.getWidgetCount());                        
                    }

                    // Return true indicates that the command should continue repeating
                    return true;

                } else {
                    
                    if(pageContentContainer.getWidgetCount() == 0) {
                        
                        // Inform the progress dialog that there were no questions to be updated
                        SurveyEditorPanel.updateEditModeProgress(pageNumber, 0, 0);
                    }
                    
                    // All elements have been iterated through, execute the callback before exiting
                    callback.onEditModeChangeComplete();
                    
                    // Return false indicates that the command is finished executing
                    return false;
                }
            }
        });

        
    }


    /**
     * Gets the current mode of the page widget. 
     * 
     * @return SurveyEditMode - The current mode of the page widget.
     */
    public SurveyEditMode getEditMode() {
        return editMode;
    }


    /**
     * Sets the current edit mode of the page widget.
     * 
     * @param mode - The new edit mode for the page widget.
     */
    private void setEditMode(SurveyEditMode mode) {
        this.editMode = mode;
        
        if(mode != SurveyEditMode.WritingMode){
        	
        	//prevent selecting survey items unless we're in writing mode
        	hideSelectSurveyItemWidget();
        }
    }


    /**
     * Gets the possible total points for all scored survey items within the page.
     * 
     * @return Double - The possible total points for all scored survey items within the page.
     */
    public Double getTotalPoints() {
        
        Double totalPoints = 0.0;
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                totalPoints += qWidget.getPossibleTotalPoints();
            }
        }
        
        return totalPoints;
    }


    /**
     * Refreshes the contents of the page widgets when the page is being reattached to the dom.
     * Certain elements such as the bootstrap slider widget get reset to default values when
     * they are removed from the DOM.  This function allows the ability for state of widgets
     * to be refreshed to proper values when the page is reattached to the DOM.
     */
    public void refreshContentsOnReattach() {
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                if (qWidget.getQuestionWidget() instanceof SliderBarWidget) {
                    SliderBarWidget sliderWidget = (SliderBarWidget)qWidget.getQuestionWidget();
                    sliderWidget.refresh();
                }
            }
        }
        
    }


    /**
     * Accessor to get the survey type for the page.
     * 
     * @return SurveyDialogOption - The type of survey that is being edited for the page.
     */
    public SurveyDialogOption getSurveyType() {
        return surveyType;
    }


    /**
     * Accessor to set the survey type for the page.
     * 
     * @param surveyType - The type of survey that is being edited for the page.
     */
    private void setSurveyType(SurveyDialogOption surveyType) {
        this.surveyType = surveyType;
    }


    /**
     * Computes the total points of the survey for each attribute and stores it into a map containing
     * each attribute with the possible total points for each attribute.
     * 
     * @param attributeScoreMap - Map containing the possible total points for each attribute.
     */
    public void computeTotalPointsPerAttribute(HashMap<LearnerStateAttributeNameEnum, Double> attributeScoreMap) {
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                qWidget.computeTotalPointsPerAttribute(attributeScoreMap);
                
            }
        }
    }


    /**
     * Sets the global survey resources (i.e. survey context, concepts, etc.) that should be used by this widget for authoring surveys
     * 
     * @param resources the survey resources
     */
    private void setSurveyResources(AbstractSurveyResources resources) {
        surveyResources = resources;
    }


    /**
     * Initializes the page widget.  This should be called after constructing the object.
     * 
     * @param pageNumber - The page number that will be displayed in the page header
     * @param pageIndex - The unique page identifier.
     * @param resources - The global set of survey resources (e.g. survey context, concepts, etc.) that this widget should use
     */
    public void initialize(int pageNumber, int pageIndex, AbstractSurveyResources resources) {
        setPageNumber(pageNumber);
        setPageId(pageIndex);
        setSurveyResources(resources);
        
        pageName.setEditingEnabled(!isReadOnly);
    }
    
    /**
     * Sets the page name.
     * 
     * @param name - The name of the page that will be displayed in the UI.
     */
    public void setPageName(String name) {
        pageName.setValue(name);
    }

    private String getPageName() {
        return pageName.getValue();
    }
    
    /**
     * Sets the page database id.  
     * 
     * @param id - The database id.  0 is reserved to indicate a new page in the database.
     */
    public void setPageDbId(int id) {
        this.dbId = id;
        
    }


    /**
     * Debug print function used to print the survey page properties from the survey database.
     * 
     * @param properties - The survey page properties from the database.
     */
    public void debugPrintPageProperties(SurveyItemProperties properties) {
        
        if (properties != null) {
            logger.info("Properties size = " + properties.getPropertyCount());
            
            for (SurveyPropertyKeyEnum key : properties.getKeys()) {
                logger.info("Key value = " + key);
            }
        }
        
        
    }


    /**
     * Saves the survey page to the database.
     * 
     * @param survey - The survey object to be saved to the database.
     * @throws SaveSurveyException if there is an error while saving one of the questions
     */
    public void saveSurvey(Survey survey) throws SaveSurveyException {
        List<SurveyPage> pages = survey.getPages();
        
        SurveyPage page = new SurveyPage();
        page.setProperties(new SurveyItemProperties());
        page.setName(getPageName());
        
        // Set the database id (if it exists)
        // This must be done so it can be determined if the data is new data (id of 0) or an
        // update to an existing id.
        page.setId(getPageDbId());
        
        List<AbstractSurveyElement> surveyElements = new ArrayList<AbstractSurveyElement>();
        page.setElements(surveyElements);
        // Build the elements of the page.
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget)iterWidget;
                qWidget.save(surveyElements, survey.getEditableToUserNames(), survey.getVisibleToUserNames());
            }
        }
        
        pages.add(page);
        surveyPage = page;
    }


    /**
     * Gets the page database id.
     * 
     * @return int - The database id for the page.  0 is reserved to indicate that the page is not yet in the database.
     */
    private int getPageDbId() {
        
        return this.dbId;
    }


    /**
     * Updates the database ids of the widgets in the survey editor.
     * 
     * @param surveyPage - The database survey page object containing the ids
     *            for the page.
     * @param questionIdToDataModel - the map that contains the survey elements.
     */
    public void updateSurveyDbIds(SurveyPage surveyPage, Map<SurveyWidgetId, AbstractSurveyElement> questionIdToDataModel) {
        this.surveyPage = surveyPage;
        setPageDbId(surveyPage.getId());

        // Matches the widgets with the survey elements from the database and
        // updates the their ids with the new ids from the database
        for (int x = 0; x < pageContentContainer.getWidgetCount(); x++) {
            Widget iterWidget = pageContentContainer.getWidget(x);
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget) iterWidget;
                SurveyWidgetId widgetId = qWidget.getWidgetId();

                // Finds the element that corresponds with the current widget
                AbstractSurveyElement element = null;
                for (AbstractSurveyElement e : surveyPage.getElements()) {
                    if (widgetId.getWidgetId().equals(e.getWidgetId())) {
                        element = e;
                        break;
                    }
                }

                // Did not find an element to match the widget, log and continue looping.
                // This shouldn't ever happen, but if it does the widget will be treated as a new
                // question and be inserted into the database. This can cause duplicate questions to
                // appear in the survey page.
                if (element == null) {
                    logger.severe("Did not find an element to match widget id [" + widgetId + "]");
                    continue;
                }

                SurveyElementTypeEnum type = element.getSurveyElementType();
                if (type == SurveyElementTypeEnum.QUESTION_ELEMENT) {

                    // Dig deeper in terms of the type of survey.
                    @SuppressWarnings("unchecked")
                    AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;

                    Widget widget = qWidget.getQuestionWidget();
                    if (widget instanceof AbstractQuestionWidget) {
                        AbstractQuestionWidget absWidget = (AbstractQuestionWidget) widget;
                        absWidget.setAbstractSurveyQuestionDbId(surveyQuestion.getId());

                        // update map survey elements with their new ids
                        if (questionIdToDataModel.containsKey(qWidget.getWidgetId())) {
                            questionIdToDataModel.get(qWidget.getWidgetId()).setId(surveyQuestion.getId());
                        }

                        if (surveyQuestion.getQuestion() != null) {
                            absWidget.setAbstractQuestionDbId(surveyQuestion.getQuestion().getQuestionId());
                            absWidget.setQuestionReplyDbIds(surveyQuestion.getQuestion().getProperties());
                        }

                    }
                } else if (type == SurveyElementTypeEnum.TEXT_ELEMENT) {
                    TextSurveyElement textElement = (TextSurveyElement) element;

                    Widget widget = qWidget.getQuestionWidget();
                    if (widget instanceof AbstractQuestionWidget) {
                        AbstractQuestionWidget absWidget = (AbstractQuestionWidget) widget;
                        absWidget.setAbstractSurveyQuestionDbId(textElement.getId());
                    }
                }
            }
        }
    }
    
    /**
     * Updates the numbers shown next to each question in this page after the given starting index to match their actual ordering.
     * 
     * @param start the question index to start the update at
     * @param nextNum the number to start the ordering at
     */
    private void updateQuestionNumbers(int start, int nextNum) {
        
        for(int i = start; i < pageContentContainer.getWidgetCount(); i++) {
            Widget currWidget = pageContentContainer.getWidget(i);
            if(currWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget qWidget = (QuestionContainerWidget) currWidget;
                
                //Skips the widget if its the info type, otherwise it updates its label
                if(qWidget.getQuestionWidget() instanceof InformativeTextWidget) {
                    continue;
                } else {
                    qWidget.setQuestionNum(nextNum++);
                }
            }
        }
    }
    
    /**
     * Gets the current ordering that will be applied to the next insertion
     * 
     * @return the insertion ordering
     */
    InsertOrder getInsertOrder(){
    	return this.insertOrder;
    }
    
    /**
     * Gets the current question ID that will be used to handle the next insertion
     * 
     * @return the insertion question ID
     */
	public SurveyWidgetId getInsertQuestionId() {		
		return this.insertQuestionId;
	}
    
    /**
     * Updates the numbers shown next to each question in this page to match their actual ordering
     */
    void updateQuestionNumbers() {
    	
    	int qNumber = 1;
        
        for(int i = 0; i < pageContentContainer.getWidgetCount(); i++) {
        	
            Widget currWidget = pageContentContainer.getWidget(i);
            
            if(currWidget instanceof QuestionContainerWidget) {
            	
                QuestionContainerWidget qWidget = (QuestionContainerWidget) currWidget;
                
                //Skips the widget if its the info type, otherwise it updates its label
                if(qWidget.getQuestionWidget() instanceof InformativeTextWidget) {
                    continue;
                    
                } else {
                    qWidget.setQuestionNum(qNumber);
                    qNumber++;
                }
            }
        }
    }
    
    /**
   	 * Sets whether or not this widget should be read-only
   	 * 
   	 * @param readOnly whether or not this widget should be read-only
   	 */
   	public void setReadOnlyMode(boolean readOnly) {
   		
   		isReadOnly = readOnly;
   		
   		movePageUp.setVisible(!readOnly);
   		movePageDown.setVisible(!readOnly);
   		deletePage.setVisible(!readOnly);
   		pageName.setEditingEnabled(!readOnly);

   		if (readOnly) {
   		    pageName.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);    
   		} else {
   		    pageName.setPlaceholder(PAGE_TITLE_PLACEHOLDER);
   		}	

   		// set read only mode for each question widget
        for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
            
            Widget iterWidget = pageContentContainer.getWidget(x);
            
            if (iterWidget instanceof QuestionContainerWidget) {
                QuestionContainerWidget container = (QuestionContainerWidget) iterWidget;
                container.setReadOnlyMode(readOnly);
                if(container.getQuestionWidget() instanceof AbstractQuestionWidget){
                    AbstractQuestionWidget questionWidget = (AbstractQuestionWidget) container.getQuestionWidget();
                    questionWidget.setReadOnlyMode(readOnly);
                }
            }
        }
   	}


	/**
	 * Gets a list of the question widgets contained by this page. Modifications to this list will not affect the underlying
	 * data structure containing the question widgets.
	 * 
	 * @return a list of question widgets
	 */
	public List<QuestionContainerWidget> getQuestionWidgets() {
		
		List<QuestionContainerWidget> widgets = new ArrayList<>();
		
		for (int x=0; x < pageContentContainer.getWidgetCount(); x++) {
			
            Widget iterWidget = pageContentContainer.getWidget(x);
            
            if (iterWidget instanceof QuestionContainerWidget) {
            	widgets.add((QuestionContainerWidget) iterWidget);
            }
		}
		
		return widgets;
	}
	
	/**
	 * Gets the number of questions on this page
	 * 
	 * @return The number of questions on this page
	 */
	public int getQuestionCount() {
	    return questionCount;
	}
}
