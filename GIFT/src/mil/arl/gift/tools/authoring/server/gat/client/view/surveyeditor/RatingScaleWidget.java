/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.DoubleBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AnswerSetPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.DifficultyAndConceptsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.HiddenAnswerWeightsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.CustomAlignmentPropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleAppearancePropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleImagePropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleLayoutPropertySet;

/**
 * The RatingScaleWidget allows the survey author to create a rating scale question in a survey page.
 * 
 * @author nblomberg
 *
 */
public class RatingScaleWidget extends AbstractQuestionWidget implements BlurHandler  {

    /** The logger for the class */
    private static Logger logger = Logger.getLogger(RatingScaleWidget.class.getName());
    
    private static int SCALE_IMAGE_ROW_INDEX = 0;
    private static int ANSWER_LABELS_ROW_INDEX = 1;
    private static int ANSWER_BUTTONS_ROW_INDEX = 2;
    private static int SCALE_LABELS_ROW_INDEX = 3;
    
    /** The ID of the last group of radio buttons added by an instance of this widget*/
    private static int lastRadioGroupId = 0;
    
    /** The UiBinder for combining the ui.xml file with this java class */
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** The interface used for combining the ui.xml file with the java class */
	interface WidgetUiBinder extends
			UiBinder<Widget, RatingScaleWidget> {
	}
	
	@UiField
	Container pointHeader;

	/**
	 * A pair of row and column coordinates that can be used to locate objects in tables
	 * 
	 * @author nroberts
	 */
	private class RowColumnPair{
		
        /** The row index of the pair */
		private int row;

        /** The column index of the pair */
		private int column;
		
        /**
         * Constructs a row pair with the given row and column index
         * 
         * @param row the index for the row component
         * @param column the index for the column component
         */
		public RowColumnPair(int row, int column){
			this.row = row;
			this.column = column;
		}

        /**
         * Gets the column index of the pair
         * 
         * @return the value of the column index
         */
		public int getColumn() {
			return column;
		}

        /**
         * Sets the column index of the pair
         * 
         * @param column the new value of the column index
         */
		public void setColumn(int column) {
			this.column = column;
		}

        /**
         * Get the row index of the pair
         * 
         * @return the value of the row index
         */
		public int getRow() {
			return row;
		}

        /**
         * Set the row index of the pair
         * 
         * @param row the new value of the row index
         */
		@SuppressWarnings("unused")
        public void setRow(int row) {
			this.row = row;
		}
	}
	
	@UiField
	protected FlexTable choicesContainer;
	
	/** A mapping from each widget in the grid to its associated location. */
	protected Map<Widget, RowColumnPair> widgetToGridLocation = new HashMap<Widget, RowColumnPair>();
	
	/** The ID for this widget's radio button group */
	private int radioGroupId = generateRadioGroupId(); 
	
	/** Whether or not this rating scale is in bar mode */
	boolean isBarMode = false;
	
	/** The currently selected response when in bar mode (used to update styling) */
	protected MatrixOfChoicesResponseWidget selectedResponse = null;
	
	/** If set, a fixed width to use for each newly-created column */
	private Integer columnWidth = CustomAlignmentPropertySet.DEFAULT_COLUMN_WIDTH; //default to 50px
	
	/** Whether or not answer labels should be hidden */
	private boolean hideAnswerLabels = false;
	
	/** The option list of the Rating Scale question corresponding to this widget */
	private OptionList optionList;
	
	/** List of weights for all the answers */
	private List<Double> answerWeights = new ArrayList<Double>();
	
    /** collection of attributes for this question to be scored on */
	private Set<AttributeScorerProperties> scoringAttributes;
	
    /** The last value of the {@link ScaleAppearancePropertySet} */
    private ScaleAppearancePropertySet scaleAppearancePropertySet = null;

	/** The pending image property set which is valid while the image is loading. */
    private ScaleImagePropertySet pendingImagePropSet;
    
    final Image scaleImage = new Image();
    
    final Image hiddenServerImage = new Image();
    
	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
     * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public RatingScaleWidget(SurveyEditMode mode, boolean isScored) {
	    super(mode, isScored);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    	    
	    scaleImage.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {
                logger.info("scale image onLoad() called");
                
                // Do nothing here.  The questionImage width and height will be
                // set by the hiddenServerImage load handler.

            }
        });
        
        hiddenServerImage.addLoadHandler(new LoadHandler() {

            @Override
            public void onLoad(LoadEvent arg0) {
                logger.info("server scale image onLoad() called");
                // Update the image in the dom once it has been loaded.
                updateImageOnLoad();
                
            }
        });
        
        // Add the hidden image to any container (it will remain invisible).
        hiddenServerImage.setVisible(false);
        pointHeader.add(hiddenServerImage);
	    
	}
	
	/**
	 * Generates a new ID for a radio button group
	 * 
	 * @return the new ID
	 */
	private int generateRadioGroupId() {
		
		lastRadioGroupId = lastRadioGroupId + 1;
		
		return lastRadioGroupId;
	}

	/**
	 * Adds a column with the given header text to the table and sets up its event handlers
	 * 
	 * @param text the text to be used as the column's header
	 * @return the the label for the column's header
	 */
	protected MatrixOfChoicesResponseWidget addColumnChoice(String text, boolean isShared){
		
		int columnIndex = choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX);
		//create a label for the new column
		final MatrixOfChoicesResponseWidget response = new MatrixOfChoicesResponseWidget();
		
		response.getLabel().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!event.getValue().isEmpty()) {
					// add the option to reply set, or update if it already
					// exists
					if (optionList.getListOptions().size() <= widgetToGridLocation.get(response).getColumn()) {
						optionList.getListOptions().add(null);
					}
					if (optionList.getListOptions().get(widgetToGridLocation.get(response).getColumn()) == null) {
						logger.info("Adding " + event.getValue() + " to optionList at index " +  (widgetToGridLocation.get(response).getColumn()));
						int index = widgetToGridLocation.get(response).getColumn();
						optionList.getListOptions().set(index, new ListOption(0, event.getValue(), optionList.getId()));
						if (answerWeights.size() <= widgetToGridLocation.get(response).getColumn()) {
							answerWeights.add(0.0);
						}
                        Widget widget = choicesContainer.getWidget(ANSWER_LABELS_ROW_INDEX, index);
                        if(widget instanceof MatrixOfChoicesResponseWidget){
                            ((MatrixOfChoicesResponseWidget) widget).getLabel().setVisible(!hideAnswerLabels);
                        }
						getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
					} else {
						logger.info("Updating index " + (widgetToGridLocation.get(response).getColumn()) + " to value = " + event.getValue());
						optionList.getListOptions().get(widgetToGridLocation.get(response).getColumn()).setText(event.getValue());
					}
					
					// update sort key based on new ordering of choices
                    for(int index = 0; index < optionList.getListOptions().size(); index++){
                        optionList.getListOptions().get(index).setSortKey(index);
                    }
				}
				
				if(widgetToGridLocation.get(response).getColumn() == choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1){
					
					if(!event.getValue().isEmpty()){
						
						response.setTemporary(false);
						
						//if the user enters text into the last column choice, add another column choice after it
						addColumnChoice(null, false);
						
						refresh();
					}
					
				} else {
					
					//if the user removes all the text in any other column choice, remove that choice
					if(event.getValue() == null || event.getValue().isEmpty()){	
						if (optionList.getListOptions().size() > widgetToGridLocation.get(response).getColumn()) {
							optionList.getListOptions().remove(widgetToGridLocation.get(response).getColumn());
							answerWeights.remove(widgetToGridLocation.get(response).getColumn());
							getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
							removeColumnChoice(response);
						}
						
					}
				}
				
				updateTotalQuestionFlag();
			}
		});
		
		response.getLabel().setEnterKeyListener(new Command() {
			
			@Override
			public void execute() {
				
				if (widgetToGridLocation.get(response).getColumn() == choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1) {	
                	
                	//need to wait until after value change event is processed, so defer remaining logic
                	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							
							if (!response.getLabel().getValue().isEmpty()
									&& widgetToGridLocation.get(response).getColumn() + 1 == choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1) {

								//a new response was created by hitting the Enter key on the last response, so start editing
								//the new response
								int nextIndex = widgetToGridLocation.get(response).getColumn() + 1;
								
								for(Widget widget : widgetToGridLocation.keySet()){
									
									RowColumnPair location = widgetToGridLocation.get(widget);
									
									if(location.getRow() == widgetToGridLocation.get(response).getRow()
											&& location.getColumn() == nextIndex
											&& widget instanceof MatrixOfChoicesResponseWidget){
										
										((MatrixOfChoicesResponseWidget) widget).startEditing();
										
										break;
									}
								}
							}
						}
					});
				}
			}
		});
		
		response.getMoveUpItem().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				int index = widgetToGridLocation.get(response).getColumn();
				
				if(index > 0){					
					swapColumns(index, index - 1);
					Collections.swap(answerWeights, index, index - 1);
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
					String answerWeightProps = SurveyItemProperties.encodeDoubleListString(answerWeights);
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					getPropertySetByType(AnswerSetPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					
					// update sort key based on new ordering of choices
                    for(index = 0; index < optionList.getListOptions().size(); index++){
                        optionList.getListOptions().get(index).setSortKey(index);
                    }
				}
			}
		});
		
		response.getMoveDownItem().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				int index = widgetToGridLocation.get(response).getColumn();
				
				if(index < choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 2){
					swapColumns(index, index + 1);
					Collections.swap(answerWeights, index, index + 1);
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
					String answerWeightProps = SurveyItemProperties.encodeDoubleListString(answerWeights);
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					getPropertySetByType(AnswerSetPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					
					// update sort key based on new ordering of choices
                    for(index = 0; index < optionList.getListOptions().size(); index++){
                        optionList.getListOptions().get(index).setSortKey(index);
                    }
				}
			}
		});
		
		response.getRemoveChoiceItem().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if (optionList.getListOptions().size() > widgetToGridLocation.get(response).getColumn()) {
					optionList.getListOptions().remove(widgetToGridLocation.get(response).getColumn());
					answerWeights.remove(widgetToGridLocation.get(response).getColumn());
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
					logger.info("Removed index " +  (widgetToGridLocation.get(response).getColumn()) + " from optionList which is now size = " + optionList.getListOptions().size());
					removeColumnChoice(response);
					
					// update sort key based on new ordering of choices
                    for(int index = 0; index < optionList.getListOptions().size(); index++){
                        optionList.getListOptions().get(index).setSortKey(index);
                    }
					
					updateTotalQuestionFlag();
				}
			}
		});
		
		response.addDomHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if(isBarMode){
					
					if(selectedResponse != null){
						selectedResponse.removeStyleName("active");
					}
					
					selectedResponse = response;
					selectedResponse.addStyleName("active");
				}
			}
			
		}, ClickEvent.getType());
		
		//set the header's text and styling
		response.getLabel().setPlaceholder("New Choice");
		response.getLabel().getElementStyle().setTextAlign(TextAlign.CENTER);
		
		response.setLabelValue(text, isShared);
		
        if (text != null) {
            response.getLabel().setVisible(!hideAnswerLabels);
            if (answerWeights.size() <= (columnIndex - 1)) {
				answerWeights.add(0.0);
			}
		}
		
		response.setLabelEditable(!isShared && !isReadOnly);
		response.setTemporary(text == null || isShared);
		
		setBarStyleActive(response, isBarMode);
				
		//add a new empty column to the table
		choicesContainer.addCell(ANSWER_LABELS_ROW_INDEX);
		choicesContainer.addCell(ANSWER_BUTTONS_ROW_INDEX);
		
		if(columnWidth != null){
			choicesContainer.getColumnFormatter().setWidth(columnIndex,  columnWidth + "px");
			
		} else {
			choicesContainer.getColumnFormatter().setWidth(columnIndex,  null);
		}
		
		//populate the new column with widgets
		for(int i = ANSWER_LABELS_ROW_INDEX; i < choicesContainer.getRowCount() - 1; i++){
			
			Widget widgetToAdd = null;
			
			if(i == ANSWER_LABELS_ROW_INDEX){
				widgetToAdd = response;
				
			} else {
				
				FlowPanel panel = new FlowPanel();
				
				RadioButton button = new RadioButton("RatingScaleQuestionGroup - " + radioGroupId + " - " +  i);
				button.addStyleName("plainRadioButton");
				button.setVisible(getEditMode().equals(SurveyEditMode.WritingMode) && !isBarMode);
				
				panel.add(button);
				
				final DoubleBox doubleBox = new DoubleBox();
				doubleBox.addStyleName("surveyScoreBoxStyle");
				doubleBox.setValue(0.0);
				doubleBox.addValueChangeHandler(new ValueChangeHandler<Double>(){

					@Override
					public void onValueChange(ValueChangeEvent<Double> event) {
					    //Treat blank/null values as 0
					    Double value = event.getValue() != null ? 
					            event.getValue() :
					            0.0;
					    
			            doubleBox.setValue(value, false);
						answerWeights.set(widgetToGridLocation.get(response).getColumn(), value);
						getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
						updateTotalQuestionFlag();
						SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
					}
					
				});
				panel.add(doubleBox);
				doubleBox.setVisible(getEditMode().equals(SurveyEditMode.ScoringMode));
				
				if(text != null && answerWeights.size() > columnIndex){
					doubleBox.setValue(answerWeights.get(columnIndex));
				} else {
					doubleBox.setValue(0.0);
				}
				
				widgetToAdd = panel;
			}
			
			choicesContainer.setWidget(i, columnIndex, widgetToAdd);
			choicesContainer.getCellFormatter().setHorizontalAlignment(i,columnIndex, HasHorizontalAlignment.ALIGN_CENTER);
			choicesContainer.getCellFormatter().setVerticalAlignment(i, columnIndex, HasVerticalAlignment.ALIGN_BOTTOM);
			
			widgetToGridLocation.put(widgetToAdd, new RowColumnPair(i, columnIndex));
		}
		
		if(!isBarMode){
    		// add symmetrical space on either side of the column label so the radio button falls below the center of the text and that
    		// each column label is separated from the next column label
            choicesContainer.getCellFormatter().getElement(ANSWER_LABELS_ROW_INDEX, columnIndex).getStyle().setPaddingRight(5, Unit.PX);
            choicesContainer.getCellFormatter().getElement(ANSWER_LABELS_ROW_INDEX, columnIndex).getStyle().setPaddingLeft(5, Unit.PX);
		}
		
		response.refresh(isReadOnly, getEditMode());
		
		resizeScaleLabels();
		
		return response;
	}
	
	/**
	 * Removes the column corresponding to the given choice
	 * 
	 * @param response the choice whose column should be deleted
	 */
	private void removeColumnChoice(MatrixOfChoicesResponseWidget response) {
		
		int index = widgetToGridLocation.get(response).getColumn();
		
		//move all columns past the index over to the left by 1 index (effectively erasing the column at the index)
		Iterator<Widget> itr = widgetToGridLocation.keySet().iterator();
		
		while(itr.hasNext()){
			
			Widget w = itr.next();
			
			RowColumnPair location = widgetToGridLocation.get(w);
			
			if(location.getColumn() == index){
				itr.remove();
				
			} else if(location.getColumn() > index){
				
				choicesContainer.setWidget(location.getRow(), location.getColumn() - 1, w);
				
				location.setColumn(location.getColumn() - 1);
			}
		}
		
		//resize the columns
		choicesContainer.removeCell(ANSWER_LABELS_ROW_INDEX, choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1);
		choicesContainer.removeCell(ANSWER_BUTTONS_ROW_INDEX, choicesContainer.getCellCount(ANSWER_BUTTONS_ROW_INDEX) - 1);
		
		resizeScaleLabels();
		
		updateTotalQuestionFlag();
		SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
	}
	
	/**
	 * Sets the visibility of the placeholder responses
	 *  
	 * @param isSelected true if the placeholder should be displayed, false if hidden
	 */
    @Override
    public void setPlaceholderResponseVisible(boolean isSelected) {

        int size = optionList.getListOptions().size();
        
        Iterator<Widget> itr = widgetToGridLocation.keySet().iterator();
        
        while(itr.hasNext()){
            
            Widget w = itr.next();
            
            RowColumnPair location = widgetToGridLocation.get(w);
            
            // remove widgets if they are in the last column and they aren't the only response
            if (size == 0) {
                //placeholder choice and no choices have been authored, don't show placeholder cause we can't
                //disable the point entry easily
                
                w.setVisible(getEditMode() == SurveyEditMode.WritingMode);
                
            }else if(location.getColumn() == size){
                //placeholder choice and there are choices authored, don't show placeholder when in scoring mode
                
                isSelected &= !isReadOnly;
                
                if(!isSelected){
                    //hiding placeholder happens no matter what
                    w.setVisible(isSelected);
                }else if(isSelected && getEditMode() == SurveyEditMode.WritingMode){
                    //showing placeholder while in writing mode, nothing else to consider, just do it
                    w.setVisible(isSelected);
                }else{
                   //this means in scoring mode, trying to show the placeholder... don't show placeholder
                   w.setVisible(false);
                }
                
            }

            if (w instanceof MatrixOfChoicesResponseWidget) {
                MatrixOfChoicesResponseWidget response = (MatrixOfChoicesResponseWidget) w;
                if (isReadOnly) {
                    response.getLabel().setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
                }
            }
        }
    }
	
	/**
	 * Swaps the columns with the given indices
	 * 
	 * @param firstColumnIndex one of the columns to be swapped
	 * @param secondColumnIndex the other column to be swapped
	 */
	private void swapColumns(int firstColumnIndex, int secondColumnIndex){
		
		List<Widget> secondColumnWidgets = new ArrayList<Widget>();
		
		//save the second column's widgets since the column is going to be overwritten
		for(int i = ANSWER_LABELS_ROW_INDEX; i < choicesContainer.getRowCount() - 1; i++){		
			
			secondColumnWidgets.add(choicesContainer.getWidget(i, secondColumnIndex));
		}
		
		//move the first column's widgets over to the second column
		for(int i = ANSWER_LABELS_ROW_INDEX; i < choicesContainer.getRowCount() - 1; i++){	
			
			Widget widget = choicesContainer.getWidget(i, firstColumnIndex);
			
			choicesContainer.setWidget(i, secondColumnIndex, widget);
			widgetToGridLocation.put(widget, new RowColumnPair(i, secondColumnIndex));
		}
		
		//move the second column's widgets over to the first column
		for(int i = ANSWER_LABELS_ROW_INDEX; i < choicesContainer.getRowCount() - 1; i++){	
			
			Widget widget = secondColumnWidgets.get(i - 1);
			
			choicesContainer.setWidget(i, firstColumnIndex, widget);
			widgetToGridLocation.put(widget, new RowColumnPair(i, firstColumnIndex));		
		}
		
		Collections.swap(optionList.getListOptions(), firstColumnIndex, secondColumnIndex);
	}
	
	/**
	 * Clears the table and adds a placeholder column where the user can start adding choices
	 */
	public void resetTable(){
		
		choicesContainer.clear();
		widgetToGridLocation.clear();
		
		choicesContainer.insertRow(0); //row for scale image
		choicesContainer.insertRow(0); //row for reply option labels
		choicesContainer.insertRow(0); //for for radio buttons
		choicesContainer.insertRow(0); //row for scale labels
		
	}
	
    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {
    	
        logger.info("onPropertySetChange called for rating scale widget: " + propSet.getClass().getName());
    	super.onPropertySetChange(propSet);
        
        if (propSet instanceof ScaleLayoutPropertySet) {
            onScaleLayoutPropertyChange((ScaleLayoutPropertySet) propSet);
        } else if (propSet instanceof CustomAlignmentPropertySet) {
            onCustomAlignmentPropertyChange((CustomAlignmentPropertySet) propSet);
        } else if (propSet instanceof ScaleAppearancePropertySet) {
            onScaleAppearancePropertyChange((ScaleAppearancePropertySet) propSet);
        } else if (propSet instanceof ScaleImagePropertySet) {
            onScaleImagePropertyChange((ScaleImagePropertySet) propSet);
        } else if (propSet instanceof AnswerSetPropertySet) {
            onAnswerSetPropertyChange((AnswerSetPropertySet) propSet);
        }
    }

    /**
     * Handles a change in the ScaleLayoutPropertySet for the widget.
     * 
     * @param scaleLayoutProperties The updated property set
     */
    private void onScaleLayoutPropertyChange(ScaleLayoutPropertySet scaleLayoutProperties) {
        isBarMode = scaleLayoutProperties.getUseBarLayout();
        for (Widget widget : widgetToGridLocation.keySet()) {
            if (widget instanceof MatrixOfChoicesResponseWidget) {
                setBarStyleActive(widget, isBarMode);
            } else if (widget instanceof FlowPanel) {
                refreshFlowPanel((FlowPanel) widget);
            }
        }
    }

    /**
     * Handles a change in the CustomAlignmentPropertySet for the widget.
     * 
     * @param customAlignmentProperties The updated property set
     */
    private void onCustomAlignmentPropertyChange(CustomAlignmentPropertySet customAlignmentProperties) {

        // This catches the case of old surveys that do not have the new
        // USE_CUSTOM_ALIGNMENT property. will set the
        // property to true if the old survey uses a column width or a left
        // margin. New surveys will explicitly set this
        // property, so this logic will not be used for new surveys.
        boolean hasCustomAlignment = customAlignmentProperties.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT) != null;
        boolean hasColumnWidth = customAlignmentProperties.getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) != null;
        boolean hasLeftMargin = customAlignmentProperties.getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY) != null;
        if (!hasCustomAlignment && (hasColumnWidth || hasLeftMargin)) {
            customAlignmentProperties.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, true);
        }

        Integer width = null;
        if (customAlignmentProperties.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, false)) {

            Integer leftMargin = null;

            if (customAlignmentProperties.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY) != null) {
                leftMargin = customAlignmentProperties.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
            }

            if (leftMargin != null) {
                choicesContainer.getElement().getStyle().setMarginLeft(leftMargin, Unit.PX);
            } else {
                choicesContainer.getElement().getStyle().clearMarginLeft();
            }

            if (customAlignmentProperties.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) != null) {
                width = customAlignmentProperties.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);
            } else {
                // default to 50px
                width = CustomAlignmentPropertySet.DEFAULT_COLUMN_WIDTH;
            }
        } else {
            choicesContainer.getElement().getStyle().clearMarginLeft();
            width = CustomAlignmentPropertySet.DEFAULT_COLUMN_WIDTH;
        }

        columnWidth = width;

        for (int i = 0; i < choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX); i++) {
            if (columnWidth != null) {
                choicesContainer.getColumnFormatter().setWidth(i, columnWidth + "px");
            } else {
                choicesContainer.getColumnFormatter().setWidth(i, null);
            }
        }

    }

    /**
     * Handles a change in the ScaleAppearancePropertySet for the widget.
     * 
     * @param scaleAppearanceProperties The updated property set
     */
    private void onScaleAppearancePropertyChange(ScaleAppearancePropertySet scaleAppearanceProperties) {

        this.scaleAppearancePropertySet = scaleAppearanceProperties;

        // Gets the flag specifying whether or not display the scale labels
        boolean displayScaleLabels = scaleAppearanceProperties.getIsDisplayScaleLabels();

        if (displayScaleLabels) {
            applyScaleLabels(scaleAppearanceProperties.getLeftExtremeLabel(),
                    scaleAppearanceProperties.getRightExtremeLabel(), scaleAppearanceProperties.getMidScaleLabels());
        } else {
            choicesContainer.getRowFormatter().setVisible(SCALE_LABELS_ROW_INDEX, false);
        }

        hideAnswerLabels = scaleAppearanceProperties.getReplyOptionLabelsAreHidden();

        int cellCount = choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX);
        for (int i = 0; i < cellCount; i++) {

            Widget widget = choicesContainer.getWidget(ANSWER_LABELS_ROW_INDEX, i);

            if (widget instanceof MatrixOfChoicesResponseWidget) {
                // hide all but the last placeholder answer
                if (i < cellCount - 1 || ((MatrixOfChoicesResponseWidget) widget).isShared()) {
                    ((MatrixOfChoicesResponseWidget) widget).getLabel().setVisible(!hideAnswerLabels);
                }
            }
        }
    }

    /**
     * Handles a change in the ScaleImagePropertySet for the widget.
     * 
     * @param scaleImageProperties The updated property set
     */
    private void onScaleImagePropertyChange(ScaleImagePropertySet scaleImageProperties) {

        Boolean displayImage = scaleImageProperties.getBooleanPropertyValue(SurveyPropertyKeyEnum.DISPLAY_SCALE_IMAGE);

        String imageLocation = null;
        if (scaleImageProperties.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY) instanceof String) {
            imageLocation = (String) scaleImageProperties.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY);
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("displayImage = " + displayImage);
            logger.info("imageLocation = " + imageLocation);
        }

        if (displayImage != null && displayImage && imageLocation != null && !imageLocation.isEmpty()) {

            this.pendingImagePropSet = scaleImageProperties;
            // This will kick off the onLoad() event for the image.
            scaleImage.setUrl(imageLocation);
            hiddenServerImage.setUrl(imageLocation);

            choicesContainer.setWidget(SCALE_IMAGE_ROW_INDEX, 0, scaleImage);

            choicesContainer.getCellFormatter().setHorizontalAlignment(SCALE_IMAGE_ROW_INDEX, 0,
                    HasHorizontalAlignment.ALIGN_CENTER);

            try {
                choicesContainer.getFlexCellFormatter().setColSpan(SCALE_IMAGE_ROW_INDEX, 0,
                        choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1);

            } catch (@SuppressWarnings("unused") Exception e) {
                // GWT can't set the column span yet, so don't worry about it
            }

            choicesContainer.getRowFormatter().setVisible(SCALE_IMAGE_ROW_INDEX, true);

        } else {

            // remove the image
            choicesContainer.getRowFormatter().setVisible(SCALE_IMAGE_ROW_INDEX, false);
        }

    }

    /**
     * Handles a change in the AnswerSetPropertySet for the widget.
     * 
     * @param answerSetProperties The updated property set
     */
    private void onAnswerSetPropertyChange(AnswerSetPropertySet answerSetProperties) {

        OptionList newOptionList = (OptionList) answerSetProperties
                .getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);

        List<Double> newAnswerWeights = null;
        if (answerSetProperties.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null) {
            newAnswerWeights = SurveyItemProperties.decodeDoubleListString((String) answerSetProperties.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
        }

        if (newOptionList != null && optionList != null) {
            if (newOptionList.getName() != null && !newOptionList.getName().equals(optionList.getName())) {
                // only populate answers if the user is selecting an existing
                // answer set
                choicesContainer.clear();
                widgetToGridLocation.clear();
                populateAnswers(newOptionList);
                if (newAnswerWeights != null) {
                    answerWeights = newAnswerWeights;
                }

            }
        }

        /* We have the new answer weights now. Push to properties */
        getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(
                SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));

        optionList = newOptionList;
        populateAnswerWeights();
        updateTotalQuestionFlag();

        if (scaleAppearancePropertySet != null && scaleAppearancePropertySet.getIsDisplayScaleLabels()) {
            applyScaleLabels(scaleAppearancePropertySet.getLeftExtremeLabel(),
                    scaleAppearancePropertySet.getRightExtremeLabel(), scaleAppearancePropertySet.getMidScaleLabels());
        }

        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
    }
    
    /**
     * Sets the given scale labels to the UI
     * 
     * @param leftLabel The text to use for the left extreme label. If null, the
     * label will be blank.
     * @param rightLabel The text to use for the right extreme label. If null,
     * the label will be blank.
     * @param midScaleLabels a collection of text for the labels that are evenly
     * dispersed between the left extreme and right extreme labels. If null, the
     * list is treated as empty
     */
    private void applyScaleLabels(String leftLabel, String rightLabel, List<String> midScaleLabels) {

        FlexTable labelTable = new FlexTable();
        labelTable.setWidth("100%");

        // Updates the left scale label text
        int columnIndex = 0;
        if (leftLabel != null) {
            labelTable.setWidget(0, columnIndex, new Label(leftLabel));
        } else {
            labelTable.setWidget(0, columnIndex, new Label());
        }
        columnIndex++;

        // Updates the text for the mid scale labels
        if (midScaleLabels != null && !midScaleLabels.isEmpty()) {
            for (String midScaleLabel : midScaleLabels) {
                labelTable.setWidget(0, columnIndex, new Label(midScaleLabel));
                labelTable.getCellFormatter().setHorizontalAlignment(0, columnIndex,
                        HasHorizontalAlignment.ALIGN_CENTER);

                columnIndex++;
            }
        }

        // Updates the right scale label text
        if (rightLabel != null) {
            labelTable.setWidget(0, columnIndex, new Label(rightLabel));
            labelTable.getCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);
        } else {
            labelTable.setWidget(0, columnIndex, new Label());
        }
        columnIndex++;

        // Evenly spaces the columns that contain each of the choices
        double percent = 100.0 / columnIndex;
        for (int column = 0; column < columnIndex; column++) {
            labelTable.getColumnFormatter().setWidth(column, percent + "%");
        }

        choicesContainer.setWidget(SCALE_LABELS_ROW_INDEX, 0, labelTable);

        try {
            /* If the option list is not shared then we want to ignore the
             * placeholder column when sizing the scale labels */
            int colspan = choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX);
            if (optionList == null || !optionList.getIsShared()) {
                colspan--;
            }

            choicesContainer.getFlexCellFormatter().setColSpan(SCALE_LABELS_ROW_INDEX, 0, colspan);
        } catch (@SuppressWarnings("unused") Exception e) {
            // GWT can't set the column span yet, so don't worry about it
        }

        choicesContainer.getRowFormatter().setVisible(SCALE_LABELS_ROW_INDEX, true);
    }

    /**
	 * Resizes the labels underneath the rating scale to match its current size
	 */
	private void resizeScaleLabels() {	
		
		if(choicesContainer.getRowCount() > SCALE_IMAGE_ROW_INDEX
				&& choicesContainer.getCellCount(SCALE_IMAGE_ROW_INDEX) > 0
				&& choicesContainer.getWidget(SCALE_IMAGE_ROW_INDEX, 0) != null){
			
			try{
				choicesContainer.getFlexCellFormatter().setColSpan(SCALE_IMAGE_ROW_INDEX, 0, choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1);
			} catch(@SuppressWarnings("unused") Exception e){
				//GWT can't set the column span yet, so don't worry about it
			}
		}
		
		if(choicesContainer.getRowCount() > SCALE_LABELS_ROW_INDEX 
				&& choicesContainer.getCellCount(SCALE_LABELS_ROW_INDEX) > 0
				&& choicesContainer.getWidget(SCALE_LABELS_ROW_INDEX, 0) != null){
			
			try{
				choicesContainer.getFlexCellFormatter().setColSpan(SCALE_LABELS_ROW_INDEX, 0, choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1);
			
			} catch(@SuppressWarnings("unused") Exception e){
				//GWT can't set the column span yet, so don't worry about it
			}
		}
	}

	/**
     * Refreshes the flow panel which contains the radio buttons and scoring edit boxes
     * based on the current edit mode of the survey editor.
     * 
     * @param panel - The flow panel to be refreshed.
     */
    private void refreshFlowPanel(FlowPanel panel) {

        for (int x=0; x < panel.getWidgetCount(); x++) {
            Widget widget = panel.getWidget(x);
            if (widget instanceof RadioButton) {
                boolean isVisible = getEditMode() == SurveyEditMode.WritingMode && !isBarMode;
                widget.setVisible(isVisible);
            } else if (widget instanceof DoubleBox) {
                boolean isVisible = getEditMode() == SurveyEditMode.ScoringMode;
                widget.setVisible(isVisible);
            }
        }
    }

    @Override
    protected void addCustomPropertySets() {

        CustomAlignmentPropertySet propSet = new CustomAlignmentPropertySet();
        addPropertySet(propSet);
        
        ScaleLayoutPropertySet slPropSet = new ScaleLayoutPropertySet();
        addPropertySet(slPropSet);
        
        ScaleAppearancePropertySet saPropSet = new ScaleAppearancePropertySet();
        addPropertySet(saPropSet);
        
        ScaleImagePropertySet siPropSet = new ScaleImagePropertySet();
        addPropertySet(siPropSet);
        
        AnswerSetPropertySet ansSet = new AnswerSetPropertySet();
        addPropertySet(ansSet);
        
        HiddenAnswerWeightsPropertySet ansWeightsSet = new HiddenAnswerWeightsPropertySet();
        addPropertySet(ansWeightsSet);
        
        DifficultyAndConceptsPropertySet diffAndConceptSet = new DifficultyAndConceptsPropertySet();
        addPropertySet(diffAndConceptSet);
        
        optionList = (OptionList) ansSet.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);

        scoringAttributes = ((QuestionScorer) ansWeightsSet.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
    }

    @Override
	public void initializeWidget() {
        resetTable();
        addColumnChoice(null, false);
        
    }

    @Override
    public void refresh() {
        
        if (getEditMode() == SurveyEditMode.WritingMode) {
            pointHeader.setVisible(false);
            questionHtml.setEditable(true);
        } else if (getEditMode() == SurveyEditMode.ScoringMode) {
            pointHeader.setVisible(true);
            questionHtml.setEditable(false);
        }
        
        if (isReadOnly) {
            questionHtml.setEditable(false);
        }
        
        for(Widget widget : widgetToGridLocation.keySet()){
            
            if(widget instanceof MatrixOfChoicesResponseWidget){
                MatrixOfChoicesResponseWidget matrixWidget = (MatrixOfChoicesResponseWidget)widget;
                matrixWidget.refresh(isReadOnly, getEditMode());
                
            } else if (widget instanceof FlowPanel) {
                refreshFlowPanel((FlowPanel) widget);
            }
        }
        
    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        super.onEditorModeChanged(mode);
        setEditMode(mode);
        
        for (int x = 0; x < choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX); x++) {
			Widget widget = choicesContainer.getWidget(ANSWER_LABELS_ROW_INDEX, x);

			if (widget instanceof MatrixOfChoicesResponseWidget) {
				MatrixOfChoicesResponseWidget mocrWidget = (MatrixOfChoicesResponseWidget) widget;
				mocrWidget.refresh(isReadOnly, mode);

				if(mocrWidget.getLabel().getValue().isEmpty() && mode.equals(SurveyEditMode.ScoringMode)){
					mocrWidget.setVisible(false);
					((FlowPanel) choicesContainer.getWidget(ANSWER_BUTTONS_ROW_INDEX, x)).setVisible(false);
				} else if (mode.equals(SurveyEditMode.WritingMode) && !mocrWidget.isVisible()){
					mocrWidget.setVisible(true);
					((FlowPanel) choicesContainer.getWidget(ANSWER_BUTTONS_ROW_INDEX, x)).setVisible(true);
				}
			}

		}
        
    }
    
    /** 
     * Sets whether or not to apply bar styling to a reply option
     * 
     * @param widget the widget for the reply option
     * @param active whether or not bar styling should be applied
     */
    private void setBarStyleActive(Widget widget, boolean active){
    	
    	if(active){
    		widget.addStyleName("ratingScaleBarChoice");
			widget.addStyleName("btn");
			widget.addStyleName("btn-default");
    	} else {
    		widget.removeStyleName("ratingScaleBarChoice");
			widget.removeStyleName("btn");
			widget.removeStyleName("btn-default");
    	}
    }
    
    /**
     * Updates the QuestionScorer totalQuestion flag based on the current answerweights for the question.
     * This method should be called anytime a score event is fired and or anytime an event occurs that could change
     * the weights for the question (such as during the load process).
     */
    private void updateTotalQuestionFlag() {
        AbstractPropertySet propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
        HiddenAnswerWeightsPropertySet answerWeightsProps = (HiddenAnswerWeightsPropertySet) propSet;
        
        try {
	        propSet = getPropertySetByType(AnswerSetPropertySet.class.getName());
	        AnswerSetPropertySet answerSetProps = (AnswerSetPropertySet) propSet;
	        answerSetProps.load(answerWeightsProps.getProperties());
        } catch (Exception e) {
        	logger.warning("Caught exception while updating answer weight properties: " + e);
        }
        
        updateTotalQuestionFlag(answerWeightsProps.getProperties());
    }

    @Override
    public Double getPossibleTotalPoints() {
        
        //If the survey is not scored it should not be worth any points
        if(!isScoredSurvey){
            return 0.0;
        }
        
        Double totalPoints = 0.0;
        try {
            totalPoints  = SurveyScorerUtil.getHighestScoreRatingScale(answerWeights);
        } catch (IllegalArgumentException e) {
            logger.severe("getPossibleTotalPoints() Caught exception: " + e.getMessage() + "  The points will be set to 0.0 for this question.");
        }
        
        logger.info("getPossibleTotalPoints() returned: " + totalPoints);
        return totalPoints;
    }

    @Override
    public void onBlur(BlurEvent event) {
        logger.info("onBlur() called ");
        
        updateTotalQuestionFlag();
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
    }

	@Override
	protected void setIsScoredType() { 
    	isScoredType = true;
	}

    @Override
    public void load(AbstractSurveyElement element) throws LoadSurveyException {
        
        if (element instanceof AbstractSurveyQuestion) {
            @SuppressWarnings("unchecked")
            AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;
            if (surveyQuestion instanceof RatingScaleSurveyQuestion) {
            	
                RatingScaleSurveyQuestion question = (RatingScaleSurveyQuestion) surveyQuestion;
                SurveyItemProperties properties = question.getProperties();

                RatingScaleQuestion ratingScaleQuestion = question.getQuestion();
                
                logger.info("question text: " + ratingScaleQuestion.getText());
                questionHtml.setValue(ratingScaleQuestion.getText());

                // print the slider question properties
                debugPrintQuestionProperties(properties);
                
                // Load the properties (if any)
                AbstractPropertySet propSet = getPropertySetByType(CustomAlignmentPropertySet.class.getName());
                CustomAlignmentPropertySet alignmentProps = (CustomAlignmentPropertySet)propSet;
                alignmentProps.load(properties);
                alignmentProps.load(ratingScaleQuestion.getProperties());
                
                // Load the Common Properties
                propSet = getPropertySetByType(CommonPropertySet.class.getName());
                CommonPropertySet commonProps = (CommonPropertySet) propSet;
                commonProps.load(properties);
                commonProps.load(ratingScaleQuestion.getProperties());
                commonProps.setSurveyQuestion(surveyQuestion);
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
                QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
                imageProps.load(properties);
                imageProps.load(ratingScaleQuestion.getProperties());
                
                // Load the Scale Layout Properties
                propSet = getPropertySetByType(ScaleLayoutPropertySet.class.getName());
                ScaleLayoutPropertySet scaleLayoutProps = (ScaleLayoutPropertySet) propSet;
                scaleLayoutProps.load(properties);
                scaleLayoutProps.load(ratingScaleQuestion.getProperties());
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(ScaleAppearancePropertySet.class.getName());
                ScaleAppearancePropertySet scaleAppearanceProps = (ScaleAppearancePropertySet) propSet;
                scaleAppearanceProps.load(properties);
                scaleAppearanceProps.load(ratingScaleQuestion.getProperties());
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(ScaleImagePropertySet.class.getName());
                ScaleImagePropertySet scaleImageProps = (ScaleImagePropertySet) propSet;
                scaleImageProps.load(properties);
                scaleImageProps.load(ratingScaleQuestion.getProperties());
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(AnswerSetPropertySet.class.getName());
                AnswerSetPropertySet answerSetProps = (AnswerSetPropertySet) propSet;
                answerSetProps.load(properties);
                answerSetProps.load(ratingScaleQuestion.getProperties());
                
            	// Load the answer weights Properties
                propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
                HiddenAnswerWeightsPropertySet answerWeightsProps = (HiddenAnswerWeightsPropertySet) propSet;
                answerWeightsProps.load(properties);
                answerWeightsProps.load(ratingScaleQuestion.getProperties());
                
                // Load the difficulty and concepts Properties
                propSet = getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName());
                DifficultyAndConceptsPropertySet diffAndConceptsSet = (DifficultyAndConceptsPropertySet) propSet;
                diffAndConceptsSet.load(properties);
                diffAndConceptsSet.load(ratingScaleQuestion.getProperties());
                
                optionList = (OptionList) answerSetProps.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
                scoringAttributes = ((QuestionScorer) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
                populateAnswers(optionList);
                answerWeights = SurveyItemProperties.decodeDoubleListString((String) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
                populateAnswerWeights();
                
                updateTotalQuestionFlag();
                
                // This should be called after all property sets have been loaded for the abstractsurveyelement.
                addUnsupportedProperties(ratingScaleQuestion.getProperties(), properties);
                onLoadNotifyPropertySetChanges();
                
                // print the question properties
                debugPrintQuestionProperties(ratingScaleQuestion.getProperties());
                
                refresh();
            } else {
                throw new LoadSurveyException("Trying to load a Rating Scale widget, but encountered non rating scale data from the database.", null);
            }
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }

    }
    
    /**
     * Will populate the widget with all the existing options and then put a new option at the end
     * 
     * @param optionList the list of options to put into the question, can be empty, but not null
     */
    private void populateAnswers(OptionList optionList) {
        removeAllCells();
    	if(optionList != null){
    		boolean isShared = optionList.getIsShared();
    		logger.info("populating answers for rating scale question with " + optionList.getListOptions().size() + " reply options, shared answer set = " + isShared);
	    	for(ListOption option : optionList.getListOptions()){
	    		addColumnChoice(option.getText(), isShared);
	    	}
	    	if(!isShared){
	    		addColumnChoice(null, isShared);
	    	}
    	}
	}
    
    private void populateAnswerWeights(){
    	logger.info("populating answer weights, weights = " + answerWeights);
    	
    	if(isScoredSurvey) {
            //If the survey is scored and there isn't a weight for every question, 
            //add the required number of weights
    	    while(answerWeights.size() < optionList.getListOptions().size()){
    	        answerWeights.add(0.0);
    	    }
    	}
    	
        // subtract 1 from the column length if not a shared option list since it will have a placeholder
		int columns = optionList.getIsShared() ? choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) : (choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX) - 1);
    	for(int i = 0; i < columns; i++){
    	    
    	    try{
        		Widget widget = choicesContainer.getWidget(ANSWER_BUTTONS_ROW_INDEX, i);
        		if(widget instanceof FlowPanel){
        			FlowPanel panel = (FlowPanel) widget;
        			DoubleBox doubleBox = (DoubleBox) panel.getWidget(1);
        			
        			doubleBox.setValue(answerWeights.get(i));  
        		}
    	    }catch(IndexOutOfBoundsException e){
    	        logger.log(Level.SEVERE, 
    	                "Caught index out of bounds exception for answer weight index of "+i+". The choices container column size is "+columns+".\n",
    	                e);
    	    }
    		
    	}
    	onEditorModeChanged(getEditMode());
    	logger.info("Finished populateAnswerWeights()");
    }
    
    /**
     * Will completely remove all cells from the answer labels and answer buttons rows.
     * The cells need to actually be removed instead of just cleared so indexing and inserting
     * can be done correctly. 
     */
    private void removeAllCells() {
    	//Calling choicesContainer.clear() will only clear the content, but the cell will still be present.
    	choicesContainer.removeCells(ANSWER_LABELS_ROW_INDEX, 0, choicesContainer.getCellCount(ANSWER_LABELS_ROW_INDEX));
    	choicesContainer.removeCells(ANSWER_BUTTONS_ROW_INDEX, 0, choicesContainer.getCellCount(ANSWER_BUTTONS_ROW_INDEX));
	}
    
    /**
     * Sets the collection of attributes for this question to be scored on. This collection comes from
     * the question container widget and is set onValueChange
     * 
     * @param attributes the collection of Attributes
     */
    @Override
    public void setScorerProperty(Set<AttributeScorerProperties> attributes){
    	scoringAttributes = attributes;
    	((QuestionScorer) getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).setAttributeScorers(scoringAttributes);
    }
    
    /**
     * Returns the current list of scoring attributes as a string list so the 
     * question container's multiselect box can be populated on load
     * 
     * @return the string list of the attributes
     */
    @Override
    public List<String> getScoringAttributesAsStringList(){
    	List<String> stringList = new ArrayList<String>();
    	for(AttributeScorerProperties attribute : scoringAttributes){
    		stringList.add(attribute.getAttributeType().getName());
    	}
    	return stringList;
    }

	/**
     * Debug print function used to print the properties for the question.
     * 
     * @param properties the properties that should be logged/printed
     */
    public void debugPrintQuestionProperties(SurveyItemProperties properties) {
        
        if (properties != null) {
            // DEBUG PRINT THE properties
            logger.info("Properties size = " + properties.getPropertyCount());
            
            for (SurveyPropertyKeyEnum key : properties.getKeys()) {
                logger.info("Key name = " + key + "\nKey value = " + properties.getPropertyValue(key));
            }
        }
    }
    
    /**
     * Updates the scale image width/height based on the actual width/height of the image's original
     * width and height (as determined from the server).  The original width/height are modified by parameters
     * set by the user (for example, setting the width to 50% -- the width is 50% of the original width of the image).
     */
    private void updateImageOnLoad() {

        if (this.pendingImagePropSet != null) {
            
            ScaleImagePropertySet set = this.pendingImagePropSet;
            
            String imageLocation = null;
            
            if(set.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY) instanceof String){
                imageLocation = (String) set.getPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_URI_KEY);
            }
            
            Integer imageWidth = null;
            
            if(set.getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY) != null){
                imageWidth = set.getIntegerPropertyValue(SurveyPropertyKeyEnum.SCALE_IMAGE_WIDTH_KEY);
            }

            logger.info("Updating scale image(" + imageLocation + ")  with width percentage: " + imageWidth);

            if(imageWidth != null){
                
                 float widthPercentage = (imageWidth / 100f);

                 if (widthPercentage == 0) {

                     widthPercentage = 1f;
                 }    
                

                 // NOTE - Using the original width of the image as determined by the server.
                 // Since the width/height of the questionImage is changing (by calling setWidth(), Gwt doesn't appear to
                 // have a way to query the actual width/height of the image.  The hiddenServerImage is being used to
                 // keep the original width/height of the image.
                 int modifiedWidth = (int)(hiddenServerImage.getWidth() * widthPercentage);
                 logger.info("Original image width from server (in pixels) = " + modifiedWidth);
                 logger.info("Setting actual image width (in pixels) = " + modifiedWidth);
                 scaleImage.setWidth(modifiedWidth + "px");
                 scaleImage.setHeight((widthPercentage * 100) + "%");      
            }
            
            this.pendingImagePropSet = null;
        }
        
    }

    @Override
	public void setDifficulty(QuestionDifficultyEnum difficulty) {
		getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, difficulty.getDisplayName());
	}
	
    @Override
	public String getDifficulty(){
		return (String) getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY);
	}
	
    @Override
	public void setConcepts(ArrayList<String> concepts){
		getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS, SurveyItemProperties.encodeListString(concepts));
	}
	
	@Override
	public List<String> getConcepts(){
		return SurveyItemProperties.decodeListString((String) getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS));
	}
	
	@Override
	public void setReadOnlyMode(boolean readOnly) {
	    this.isReadOnly = readOnly;
	    
	    if (readOnly) {
	        questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
	        questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
	    }

	    refresh();
	}
}