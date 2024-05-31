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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditorResources;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.DifficultyAndConceptsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.HiddenMOCAnswerWeightsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MOCAnswerSetsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.CustomAlignmentPropertySet;

/**
 * The MatrixOfChoicesWidget allows the survey author to create a table of choices that can be
 * selected from within a survey.
 *
 * @author nroberts
 */
public class MatrixOfChoicesWidget extends AbstractQuestionWidget  implements BlurHandler {

    private static Logger logger = Logger.getLogger(MatrixOfChoicesWidget.class.getName());

    /** The ID of the last group of radio buttons added by an instance of this widget*/
    private static int lastRadioGroupId = 0;

	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, MatrixOfChoicesWidget> {
	}


	@UiField
	protected Container pointHeader;

	/**
	 * A pair of row and column coordinates that can be used to locate objects in tables
	 *
	 * @author nroberts
	 */
	private class RowColumnPair{

		private int row;
		private int column;

		public RowColumnPair(int row, int column){
			this.row = row;
			this.column = column;
		}

		public int getColumn() {
			return column;
		}

		public void setColumn(int column) {
			this.column = column;
		}

		public int getRow() {
			return row;
		}

		public void setRow(int row) {
			this.row = row;
		}
	}

	@UiField
	protected Grid choicesContainer;

	/** A mapping from each widget in the grid to its associated location. */
	private Map<Widget, RowColumnPair> widgetToGridLocation = new HashMap<Widget, RowColumnPair>();

	/** The ID for this widget's radio button group */
	private int radioGroupId = generateRadioGroupId();

	/** If set, a fixed width to use for each newly-created column */
	private Integer columnWidth = CustomAlignmentPropertySet.DEFAULT_COLUMN_WIDTH; //default to 50px

	/** The option list of the column corresponding to this widget */
	private OptionList columnOptionList;

	/** The option list of the row corresponding to this widget */
	private OptionList rowOptionList;

	/** The answer weights corresponding to the row/column option lists */
	private MatrixOfChoicesReplyWeights answerWeights;

	/**
	 * collection of attributes for this question to be scored on
	 */
	private Set<AttributeScorerProperties> scoringAttributes;


	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
     * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public MatrixOfChoicesWidget(SurveyEditMode mode, boolean isScored) {
	    super(mode, isScored);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    onEditorModeChanged(mode);
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
	 * Adds a row with the given header text to the table and sets up its event handlers
	 *
	 * @param text the text to be used as the row's header
	 * @return the the label for the row's header
	 */
	private MatrixOfChoicesResponseWidget addRowChoice(String text, boolean isShared){

		int rowIndex = choicesContainer.getRowCount();

		//create a label for the new row
		final MatrixOfChoicesResponseWidget response = new MatrixOfChoicesResponseWidget();


		response.getLabel().addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if(!event.getValue().isEmpty()) {
					// add the option to reply set, or update if it already exists
					if (rowOptionList.getListOptions().size() <= widgetToGridLocation.get(response).getRow() - 1) {
						rowOptionList.getListOptions().add(null);
					}
					if (rowOptionList.getListOptions().get(widgetToGridLocation.get(response).getRow() - 1) == null) {
						logger.info("Adding " + event.getValue() + " to rowOptionList at index " + (widgetToGridLocation.get(response).getRow() - 1));
						rowOptionList.getListOptions().set(widgetToGridLocation.get(response).getRow() - 1, new ListOption(0, event.getValue(), rowOptionList.getId()));
						if (answerWeights.getReplyWeights().size() <= widgetToGridLocation.get(response).getRow() - 1) {
							answerWeights.addRow(choicesContainer.getColumnCount() - 2);
						}
					} else {
						logger.info("Updating index " + (widgetToGridLocation.get(response).getRow() - 1) + " to value = " + event.getValue());
						rowOptionList.getListOptions().get(widgetToGridLocation.get(response).getRow() - 1).setText(event.getValue());
					}

					// update sort key based on new ordering of choices
	                for(int index = 0; index < rowOptionList.getListOptions().size(); index++){
	                    rowOptionList.getListOptions().get(index).setSortKey(index);
	                }

				}

				if(widgetToGridLocation.get(response).getRow() == choicesContainer.getRowCount() - 1){

					if(!event.getValue().isEmpty()){

						response.setTemporary(false);

						//if the user enters text into the last row choice, add another row choice after it
						addRowChoice(null, false);

						refresh();
					}

				} else {

					//if the user removes all the text in any other row choice, remove that choice
					if(event.getValue() == null || event.getValue().isEmpty()){
						logger.info("Attempting to remove index " + (widgetToGridLocation.get(response).getRow() - 1) + " from rowOptionList of size = " + rowOptionList.getListOptions().size());
						if (rowOptionList.getListOptions().size() > widgetToGridLocation.get(response).getRow() - 1) {
							rowOptionList.getListOptions().remove(widgetToGridLocation.get(response).getRow() - 1);
							answerWeights.removeRow(widgetToGridLocation.get(response).getRow() - 1);
							logger.info("Successfully removed index " + (widgetToGridLocation.get(response).getRow() - 1) + " from rowOptionList, now of size = " + rowOptionList.getListOptions().size());
							removeRowChoice(response);
						}

					}
				}

				updateTotalQuestionFlag();
			}
		});

		response.getLabel().setEnterKeyListener(new Command() {

			@Override
			public void execute() {

				if (widgetToGridLocation.get(response).getRow() == choicesContainer.getRowCount() - 1) {

                	//need to wait until after value change event is processed, so defer remaining logic
                	Scheduler.get().scheduleDeferred(new ScheduledCommand() {

						@Override
						public void execute() {

							if (!response.getLabel().getValue().isEmpty()
									&& widgetToGridLocation.get(response).getRow() + 1 == choicesContainer.getRowCount() - 1) {

								//a new response was created by hitting the Enter key on the last response, so start editing
								//the new response
								int nextIndex = widgetToGridLocation.get(response).getRow() + 1;

								for(Widget widget : widgetToGridLocation.keySet()){

									RowColumnPair location = widgetToGridLocation.get(widget);

									if(location.getRow() == nextIndex
											&& location.getColumn() == widgetToGridLocation.get(response).getColumn()
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

				int index = widgetToGridLocation.get(response).getRow();

				if(index > 1){
					swapRows(index, index - 1);
					Collections.swap(rowOptionList.getListOptions(), index - 1, index - 2);
					answerWeights.swapRows(index - 1, index - 2);
					
					// update sort key based on new ordering of choices
	                for(index = 0; index < rowOptionList.getListOptions().size(); index++){
	                    rowOptionList.getListOptions().get(index).setSortKey(index);
	                }
				}				
				
			}
		});

		response.getMoveDownItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				int index = widgetToGridLocation.get(response).getRow();

				if(index < choicesContainer.getRowCount() - 2){
					swapRows(index, index + 1);
					Collections.swap(rowOptionList.getListOptions(), index - 1, index);
					answerWeights.swapRows(index - 1, index);
					
					// update sort key based on new ordering of choices
	                for(index = 0; index < rowOptionList.getListOptions().size(); index++){
	                    rowOptionList.getListOptions().get(index).setSortKey(index);
	                }
				}				
				
			}
		});

		response.getRemoveChoiceItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (rowOptionList.getListOptions().size() > widgetToGridLocation.get(response).getRow() - 1) {
					rowOptionList.getListOptions().remove(widgetToGridLocation.get(response).getRow() - 1);
					answerWeights.removeRow(widgetToGridLocation.get(response).getRow() - 1);
					logger.info("Successfully removed index " + (widgetToGridLocation.get(response).getRow() - 1) + " from rowOptionList, now of size = " + rowOptionList.getListOptions().size());
					removeRowChoice(response);
					
					// update sort key based on new ordering of choices
	                for(int index = 0; index < rowOptionList.getListOptions().size(); index++){
	                    rowOptionList.getListOptions().get(index).setSortKey(index);
	                }

					updateTotalQuestionFlag();
				}
			}
		});

		//set the header's text and styling
		response.getLabel().setPlaceholder("New Row");
		response.getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		response.getLabel().getElementStyle().setTextAlign(TextAlign.RIGHT);

		response.setLabelValue(text, isShared);

		if(text != null && answerWeights.getReplyWeights().size() <= rowIndex - 1) {
			answerWeights.addRow(choicesContainer.getColumnCount() - 2);
		}

		response.setTemporary(text == null || isShared);
		response.setLabelEditable(!isShared && !isReadOnly);

		//add a new empty row the table
		choicesContainer.resizeRows(rowIndex + 1);

		//populate the row with widgets
		for(int i = 0; i < choicesContainer.getColumnCount(); i++){

			Widget widgetToAdd = null;

			if(i == 0){
				widgetToAdd = response;

			} else {

				final FlowPanel panel = new FlowPanel();

				RadioButton button = new RadioButton("MatrixOfChoicesQuestionGroup - " + radioGroupId + " - " +  rowIndex);
				button.addStyleName("plainRadioButton");
				button.setVisible(getEditMode().equals(SurveyEditMode.WritingMode));
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

                        answerWeights.setWeightValue(widgetToGridLocation.get(panel).getRow() - 1, widgetToGridLocation.get(panel).getColumn() - 1, value);
                        doubleBox.setValue(value, false);

                        updateTotalQuestionFlag();
						SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
					}

                });
                doubleBox.setVisible(getEditMode().equals(SurveyEditMode.ScoringMode));
                panel.add(doubleBox);

				widgetToAdd = panel;
			}

			choicesContainer.setWidget(rowIndex, i, widgetToAdd);
			choicesContainer.getCellFormatter().setHorizontalAlignment(rowIndex,i, HasHorizontalAlignment.ALIGN_CENTER);

			widgetToGridLocation.put(widgetToAdd, new RowColumnPair(rowIndex, i));
		}

		return response;
	}

	/**
	 * Removes the row corresponsing to the given choice
	 *
	 * @param response the choice whose row should be deleted
	 */
	private void removeRowChoice(MatrixOfChoicesResponseWidget response) {

		int index = widgetToGridLocation.get(response).getRow();
		Iterator<Widget> itr = widgetToGridLocation.keySet().iterator();

		while(itr.hasNext()){
			Widget w = itr.next();

			RowColumnPair location = widgetToGridLocation.get(w);

			if(location.getRow() == index){
				itr.remove();
			} else if(location.getRow() > index){
				choicesContainer.setWidget(location.getRow() - 1, location.getColumn(), w);

				location.setRow(location.getRow() - 1);
			}
		}

		choicesContainer.resizeRows(choicesContainer.getRowCount() - 1);

		updateTotalQuestionFlag();
		SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
	}

	/**
     * Sets the visibility of the placeholder rows and columns
     *
     * @param true if the placeholder values should be visible, false if they should be hidden
     */
    @Override
    public void setPlaceholderResponseVisible(boolean isSelected) {
        this.isSelected = isSelected;

        int rowIndex = rowOptionList.getListOptions().size() + 1;
        int columnIndex = columnOptionList.getListOptions().size() + 1;
        Iterator<Widget> itr = widgetToGridLocation.keySet().iterator();
        Widget w = null;
        RowColumnPair location = null;

        while(itr.hasNext()){
            w = itr.next();
            location = widgetToGridLocation.get(w);

            try{
                if(w instanceof FlowPanel) {
                    FlowPanel panel = (FlowPanel)w;
                    if (panel != null && panel.getWidgetCount() > 1) {
                        RadioButton button = (RadioButton)panel.getWidget(0);
                        DoubleBox box = (DoubleBox)panel.getWidget(1);
                        button.setEnabled(!isReadOnly);
                        box.setEnabled(!isReadOnly);
                    }
                }else if(w instanceof MatrixOfChoicesResponseWidget) {
                    MatrixOfChoicesResponseWidget response = (MatrixOfChoicesResponseWidget)w;
                    response.refresh(isReadOnly, getEditMode());
                }else{
                    logger.warning("Response widget was not recognized.");
                }
            } catch (Exception e){
                logger.warning("Exception caught when trying to set enabled for Matrix of Choice Widget responses: " + e);
            }

            if (rowIndex != 1 && location.getRow() == rowIndex && !rowOptionList.getIsShared()) {
                // placeholder row

                isSelected &= !isReadOnly;

                if (!isSelected) {
                    // hiding placeholder happens no matter what
                    w.setVisible(isSelected);
                } else if (isSelected && getEditMode() == SurveyEditMode.WritingMode) {
                    //showing placeholder while in writing mode, nothing else to consider, just do it
                    w.setVisible(isSelected);
                } else {
                   //this means in scoring mode, trying to show the placeholder... don't show placeholder
                    w.setVisible(false);
                }

            } else if (columnIndex != 1 && location.getColumn() == columnIndex && !columnOptionList.getIsShared()) {
                // placeholder column

                isSelected &= !isReadOnly;

                if (!isSelected) {
                    // hiding placeholder happens no matter what
                    w.setVisible(isSelected);
                } else if (isSelected && getEditMode() == SurveyEditMode.WritingMode) {
                    //showing placeholder while in writing mode, nothing else to consider, just do it
                    w.setVisible(isSelected);
                } else {
                   //this means in scoring mode, trying to show the placeholder... don't show placeholder
                    w.setVisible(false);
                }

            }
        }
    }

	/**
	 * Swaps the rows with the given indices
	 *
	 * @param firstRowIndex one of the rows to be swapped
	 * @param secondRowIndex the other row to be swapped
	 */
	private void swapRows(int firstRowIndex, int secondRowIndex){

		List<Widget> secondRowWidgets = new ArrayList<Widget>();

		//save the second row's widgets since the row is going to be overwritten
		for(int i = 0; i < choicesContainer.getColumnCount(); i++){

			secondRowWidgets.add(choicesContainer.getWidget(secondRowIndex, i));
		}

		//move the first row's widgets over to the second row
		for(int i = 0; i < choicesContainer.getColumnCount(); i++){

			Widget widget = choicesContainer.getWidget(firstRowIndex, i);

			choicesContainer.setWidget(secondRowIndex, i, widget);
			widgetToGridLocation.put(widget, new RowColumnPair(secondRowIndex, i));
		}

		//move the first row's widgets over to the second row
		for(int i = 0; i < choicesContainer.getColumnCount(); i++){

			Widget widget = secondRowWidgets.get(i);

			choicesContainer.setWidget(firstRowIndex, i, widget);
			widgetToGridLocation.put(widget, new RowColumnPair(firstRowIndex, i));
		}
	}

	/**
	 * Adds a row with the given header text to the table and sets up its event handlers
	 *
	 * @param text the text to be used as the row's header
	 * @return the the label for the row's header
	 */
	private MatrixOfChoicesResponseWidget addColumnChoice(String text, boolean isShared){

		int columnIndex = choicesContainer.getColumnCount();

		//create a label for the new column
		final MatrixOfChoicesResponseWidget response = new MatrixOfChoicesResponseWidget();

		response.getLabel().addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if(!event.getValue().isEmpty()) {
					// add the option to reply set, or update if it already exists
					if (columnOptionList.getListOptions().size() <= widgetToGridLocation.get(response).getColumn() - 1) {
						columnOptionList.getListOptions().add(null);
					}
					if (columnOptionList.getListOptions().get(widgetToGridLocation.get(response).getColumn() - 1) == null) {
						logger.info("Adding " + event.getValue() + " to columnOptionList at index " + (widgetToGridLocation.get(response).getColumn() - 1));
						columnOptionList.getListOptions().set(widgetToGridLocation.get(response).getColumn() - 1, new ListOption(0, event.getValue(), columnOptionList.getId()));
						if (answerWeights.getReplyWeights().size() == 0 || answerWeights.getReplyWeights().get(0).size() <= widgetToGridLocation.get(response).getColumn()) {
						    answerWeights.addColumn();
                        }
					} else {
						logger.info("Updating index " + (widgetToGridLocation.get(response).getColumn() - 1) + " to value = " + event.getValue());
						columnOptionList.getListOptions().get(widgetToGridLocation.get(response).getColumn() - 1).setText(event.getValue());
					}					
		               
	                // update sort key based on new ordering of choices
	                for(int index = 0; index < columnOptionList.getListOptions().size(); index++){
	                    columnOptionList.getListOptions().get(index).setSortKey(index);
	                }
				}


				if(widgetToGridLocation.get(response).getColumn() == choicesContainer.getColumnCount() - 1){

					if(!event.getValue().isEmpty()){

						response.setTemporary(false);

						//if the user enters text into the last row choice, add another row choice after it
						addColumnChoice(null, false);

						refresh();
					}

				} else {

					//if the user removes all the text in any other row choice, remove that choice
					if(event.getValue() == null || event.getValue().isEmpty()){
						if (columnOptionList.getListOptions().size() > widgetToGridLocation.get(response).getColumn() - 1) {
							columnOptionList.getListOptions().remove(widgetToGridLocation.get(response).getColumn() - 1);
							answerWeights.removeColumn(widgetToGridLocation.get(response).getColumn() - 1);
							logger.info("Successfully removed index " + (widgetToGridLocation.get(response).getColumn() - 1) + " from columnOptionList, now of size = " + columnOptionList.getListOptions().size());
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

				if (widgetToGridLocation.get(response).getColumn() == choicesContainer.getColumnCount() - 1) {

                	//need to wait until after value change event is processed, so defer remaining logic
                	Scheduler.get().scheduleDeferred(new ScheduledCommand() {

						@Override
						public void execute() {

							if (!response.getLabel().getValue().isEmpty()
									&& widgetToGridLocation.get(response).getColumn() + 1 == choicesContainer.getColumnCount() - 1) {

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

				if(index > 1){
					swapColumns(index, index - 1);
					Collections.swap(columnOptionList.getListOptions(), index - 1, index - 2);
					answerWeights.swapColumns(index - 1, index - 2);
					
					// update sort key based on new ordering of choices
                    for(index = 0; index < columnOptionList.getListOptions().size(); index++){
                        columnOptionList.getListOptions().get(index).setSortKey(index);
                    }
				}
			}
		});

		response.getMoveDownItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				int index = widgetToGridLocation.get(response).getColumn();

				if(index < choicesContainer.getColumnCount() - 2){
					swapColumns(index, index + 1);
					Collections.swap(columnOptionList.getListOptions(), index - 1, index);
					answerWeights.swapColumns(index - 1, index);
					
					// update sort key based on new ordering of choices
                    for(index = 0; index < columnOptionList.getListOptions().size(); index++){
                        columnOptionList.getListOptions().get(index).setSortKey(index);
                    }
				}
			}
		});

		response.getRemoveChoiceItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				if (columnOptionList.getListOptions().size() > widgetToGridLocation.get(response).getColumn() - 1) {
					columnOptionList.getListOptions().remove(widgetToGridLocation.get(response).getColumn() - 1);
					answerWeights.removeColumn(widgetToGridLocation.get(response).getColumn() - 1);
					logger.info("Successfully removed index " + (widgetToGridLocation.get(response).getColumn() - 1) + " from columnOptionList, now of size = " + columnOptionList.getListOptions().size());
					removeColumnChoice(response);
					
					// update sort key based on new ordering of choices
                    for(int index = 0; index < columnOptionList.getListOptions().size(); index++){
                        columnOptionList.getListOptions().get(index).setSortKey(index);
                    }

					updateTotalQuestionFlag();
				}
			}
		});

		//set the header's text and styling
		response.getLabel().setPlaceholder("New Column");
		response.getLabel().getElementStyle().setTextAlign(TextAlign.CENTER);

        response.setLabelValue(text, isShared);

		if(text != null && (answerWeights.getReplyWeights().size() == 0 || answerWeights.getReplyWeights().get(0).size() <= (columnIndex - 1))) {
			answerWeights.addColumn();
		}

		response.setTemporary(text == null || isShared);

		response.setLabelEditable(!isShared && !isReadOnly);

		//add a new empty column to the table
		choicesContainer.resizeColumns(columnIndex + 1);

		if(columnWidth != null){
			choicesContainer.getColumnFormatter().setWidth(columnIndex,  columnWidth + "px");

		} else {
			choicesContainer.getColumnFormatter().setWidth(columnIndex,  null);
		}

		//populate the new column with widgets
		for(int i = 0; i < choicesContainer.getRowCount(); i++){

			Widget widgetToAdd = null;

			if(i == 0){
				widgetToAdd = response;

			} else {

				final FlowPanel panel = new FlowPanel();

				RadioButton button = new RadioButton("MatrixOfChoicesQuestionGroup - " + radioGroupId + " - " +  i);
				button.addStyleName("plainRadioButton");
				button.setVisible(getEditMode().equals(SurveyEditMode.WritingMode));
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

					    answerWeights.setWeightValue(widgetToGridLocation.get(panel).getRow() - 1, widgetToGridLocation.get(panel).getColumn() - 1, value);
						doubleBox.setValue(value, false);

						updateTotalQuestionFlag();
						SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
					}

                });
                doubleBox.setVisible(getEditMode().equals(SurveyEditMode.ScoringMode));


                panel.add(doubleBox);


				widgetToAdd = panel;
			}

			choicesContainer.setWidget(i, columnIndex, widgetToAdd);
			choicesContainer.getCellFormatter().setHorizontalAlignment(i,columnIndex, HasHorizontalAlignment.ALIGN_CENTER);
			choicesContainer.getCellFormatter().setVerticalAlignment(i, columnIndex, HasVerticalAlignment.ALIGN_BOTTOM);

			widgetToGridLocation.put(widgetToAdd, new RowColumnPair(i, columnIndex));
		}

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
		choicesContainer.resizeColumns(choicesContainer.getColumnCount() - 1);

		updateTotalQuestionFlag();
		SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
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
		for(int i = 0; i < choicesContainer.getRowCount(); i++){

			secondColumnWidgets.add(choicesContainer.getWidget(i, secondColumnIndex));
		}

		//move the first column's widgets over to the second column
		for(int i = 0; i < choicesContainer.getRowCount(); i++){

			Widget widget = choicesContainer.getWidget(i, firstColumnIndex);

			choicesContainer.setWidget(i, secondColumnIndex, widget);
			widgetToGridLocation.put(widget, new RowColumnPair(i, secondColumnIndex));
		}

		//move the first column's widgets over to the second column
		for(int i = 0; i < choicesContainer.getRowCount(); i++){

			Widget widget = secondColumnWidgets.get(i);

			choicesContainer.setWidget(i, firstColumnIndex, widget);
			widgetToGridLocation.put(widget, new RowColumnPair(i, firstColumnIndex));
		}
	}

	/**
	 * Clears the table and adds a placeholder row and column where the user can start adding choices
	 */
	public void resetTable(){

		choicesContainer.clear();
		widgetToGridLocation.clear();

		choicesContainer.resize(1, 1);

	}
    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {

    	super.onPropertySetChange(propSet);

	    if(propSet instanceof CustomAlignmentPropertySet){

	    	CustomAlignmentPropertySet set = (CustomAlignmentPropertySet) propSet;

	    	//This catches the case of old surveys that do not have the new USE_CUSTOM_ALIGNMENT property. will set the
	    	// property to true if the old survey uses a column width or a left margin. New surveys will explicitly set this
	    	//property, so this logic will not be used for new surveys.
	    	if(set.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT) == null &&
	    			((set.getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) != null ||
	    			set.getProperties().getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) != null))){
	    		set.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, true);
	    	}

	    	Integer width = null;
	    	if(set.getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_CUSTOM_ALIGNMENT, false)){

		    	Integer leftMargin = null;

		    	if(set.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY) != null){
		    		leftMargin = set.getIntegerPropertyValue(SurveyPropertyKeyEnum.LEFT_MARGIN_KEY);
		    	}

		    	if(leftMargin != null){
		    		choicesContainer.getElement().getStyle().setMarginLeft(leftMargin, Unit.PX);

		    	} else {
		    		choicesContainer.getElement().getStyle().clearMarginLeft();
		    	}

		    	if(set.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY) != null){
		    		width = set.getIntegerPropertyValue(SurveyPropertyKeyEnum.COLUMN_WIDTH_KEY);

		    	} else {
		    		width = CustomAlignmentPropertySet.DEFAULT_COLUMN_WIDTH; //default to 50px
		    	}
	    	} else {
	    		choicesContainer.getElement().getStyle().clearMarginLeft();
	    		width = CustomAlignmentPropertySet.DEFAULT_COLUMN_WIDTH;
	    	}

	    	columnWidth = width;

	    	for(int i = 1; i < choicesContainer.getColumnCount(); i++){

	    		if(columnWidth != null){
	    			choicesContainer.getColumnFormatter().setWidth(i,  columnWidth + "px");

	    		} else {
	    			choicesContainer.getColumnFormatter().setWidth(i,  null);
	    		}
	    	}
	    } else if(propSet instanceof MOCAnswerSetsPropertySet){
	    	MOCAnswerSetsPropertySet answerPropSet = (MOCAnswerSetsPropertySet) propSet;

	    	OptionList newRowOptionList = (OptionList) answerPropSet.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
	    	OptionList newColumnOptionList = (OptionList) answerPropSet.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
	    	MatrixOfChoicesReplyWeights newAnswerWeights = (MatrixOfChoicesReplyWeights) answerPropSet.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
    		boolean populateOptions = false;

    		if(newRowOptionList != null && rowOptionList != null) {
    			if(newRowOptionList.getName() != null && !newRowOptionList.getName().equals(rowOptionList.getName())) {
    				// only populate answers if the user is selecting an existing answer set
    				populateOptions = true;
    			}
    		}

    		if(newColumnOptionList != null && columnOptionList != null) {
    			if(newColumnOptionList.getName() != null && !newColumnOptionList.getName().equals(columnOptionList.getName())) {
    				// only populate answers if the user is selecting an existing answer set
    				populateOptions = true;
    			}
    		}

    		rowOptionList = newRowOptionList;
    		columnOptionList = newColumnOptionList;

    		if(populateOptions) {
    			choicesContainer.clear();
				widgetToGridLocation.clear();
    			populateAnswers(columnOptionList, rowOptionList);
    			if(newAnswerWeights != null) {
					answerWeights = newAnswerWeights;
				}
    		}

    		populateAnswerWeights();

    		updateTotalQuestionFlag();
        	SurveyEditorResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
        }
    }

	@Override
    protected void addCustomPropertySets() {

        CustomAlignmentPropertySet propSet = new CustomAlignmentPropertySet();
        addPropertySet(propSet);

        MOCAnswerSetsPropertySet ansSet = new MOCAnswerSetsPropertySet();
        addPropertySet(ansSet);

        HiddenMOCAnswerWeightsPropertySet answerWeightsSet = new HiddenMOCAnswerWeightsPropertySet();
        addPropertySet(answerWeightsSet);

        DifficultyAndConceptsPropertySet diffAndConceptSet = new DifficultyAndConceptsPropertySet();
        addPropertySet(diffAndConceptSet);

        columnOptionList = (OptionList) ansSet.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
        rowOptionList = (OptionList) ansSet.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
        answerWeights = (MatrixOfChoicesReplyWeights) answerWeightsSet.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
        scoringAttributes = ((QuestionScorer) answerWeightsSet.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
    }

    @Override
	public void initializeWidget() {
        resetTable();
        addRowChoice(null, false);
        addColumnChoice(null, false);

    }

    @Override
    public void refresh() {
        if (getEditMode() == SurveyEditMode.WritingMode) {
            pointHeader.setVisible(false);
            questionHtml.setEditable(true);
        } else {
            pointHeader.setVisible(true);
            questionHtml.setEditable(false);
        }
        if (isReadOnly) {
            questionHtml.setEditable(false);
        }

        Iterator<Widget> itr = widgetToGridLocation.keySet().iterator();

        while(itr.hasNext()){
            Widget widget = itr.next();

            if (widget instanceof MatrixOfChoicesResponseWidget) {
                MatrixOfChoicesResponseWidget matrixWidget = (MatrixOfChoicesResponseWidget)widget;
                matrixWidget.refresh(isReadOnly, getEditMode());
            } else if (widget instanceof FlowPanel) {
                refreshFlowPanel((FlowPanel)widget, false);
            }

        }

    }

    /**
     * Refreshes the display of the FlowPanel, which contains the
     * radio button/point value edit boxes.  The widget's display
     * is refreshed based upon which editing mode is currently being used.
     *
     * @param panel - The FlowPanel to be refreshed.
     * @param isPlaceholder - True if the current FlowPanel being updated is part of a placeholder response
     */
    private void refreshFlowPanel(FlowPanel panel, boolean isPlaceholder) {
        for (int x=0; x < panel.getWidgetCount(); x++) {
            Widget widget = panel.getWidget(x);

            if (widget instanceof RadioButton) {
                boolean isVisible = false;
                if (!isPlaceholder && getEditMode() == SurveyEditMode.WritingMode) {
                    isVisible = true;
                }
                widget.setVisible(isVisible);
            } else if (widget instanceof DoubleBox) {
                boolean isVisible = false;
                if (!isPlaceholder && getEditMode() == SurveyEditMode.ScoringMode) {
                    isVisible = true;
                }

                widget.setVisible(isVisible);
            }
        }

    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        super.onEditorModeChanged(mode);
        setEditMode(mode);

        int rowCount = choicesContainer.getRowCount();
        int columnCount = choicesContainer.getColumnCount();
        for (int i = 0; i < rowCount; i++) {
        	for(int j = 0; j < columnCount; j++){
        		if(i !=0 || j != 0){
					Widget widget = choicesContainer.getWidget(i,j);

					boolean rowPlaceholder = (i == rowCount - 1) && !rowOptionList.getIsShared();
					boolean columnPlaceholder = (j == columnCount - 1) && !columnOptionList.getIsShared();
					boolean isPlaceholder = rowPlaceholder || columnPlaceholder;

					if (widget instanceof MatrixOfChoicesResponseWidget) {
						MatrixOfChoicesResponseWidget mocrWidget = (MatrixOfChoicesResponseWidget) widget;

						mocrWidget.refresh(isReadOnly, mode);
						if((mocrWidget.getLabel().getValue().isEmpty() && mode.equals(SurveyEditMode.ScoringMode)) || (isPlaceholder && !isSelected)){
							mocrWidget.setVisible(false);
						} else if (mode.equals(SurveyEditMode.WritingMode) && !mocrWidget.isVisible()){
							mocrWidget.setVisible(true);
						}
					} else if(widget instanceof FlowPanel){
						FlowPanel panel = (FlowPanel) widget;

						refreshFlowPanel(panel, isPlaceholder);
						if((((MatrixOfChoicesResponseWidget) choicesContainer.getWidget(i, 0)).getLabel().getValue().isEmpty() || ((MatrixOfChoicesResponseWidget) choicesContainer.getWidget(0, j)).getLabel().getValue().isEmpty()) && mode.equals(SurveyEditMode.ScoringMode) || (isPlaceholder && !isSelected)){
							panel.setVisible(false);
						} else if (mode.equals(SurveyEditMode.WritingMode) && !panel.isVisible()){
							panel.setVisible(true);
						}
					}
        		}
        	}
		}
        refresh();
    }


    /**
     * Updates the QuestionScorer totalQuestion flag based on the current answerweights for the question.
     * This method should be called anytime a score event is fired and or anytime an event occurs that could change
     * the weights for the question (such as during the load process).
     */
    private void updateTotalQuestionFlag() {

        AbstractPropertySet propSet = getPropertySetByType(HiddenMOCAnswerWeightsPropertySet.class.getName());
        HiddenMOCAnswerWeightsPropertySet answerWeightsProps = (HiddenMOCAnswerWeightsPropertySet) propSet;
        MOCAnswerSetsPropertySet answerSetProps = (MOCAnswerSetsPropertySet) getPropertySetByType(MOCAnswerSetsPropertySet.class.getName());
        answerSetProps.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeights);
        updateTotalQuestionFlag(answerWeightsProps.getProperties());
    }

    @Override
    public Double getPossibleTotalPoints() {

        Double totalPoints = 0.0;

        /* subtract 1 from the row/column length for the row/column labels and
         * subtract an additional 1 if not a shared option list since it will
         * have a placeholder */
        int rows = choicesContainer.getRowCount() - (rowOptionList.getIsShared() ? 1 : 2);
        int columns = choicesContainer.getColumnCount() - (columnOptionList.getIsShared() ? 1 : 2);

        try {
            totalPoints = SurveyScorerUtil.getHighestScoreMatrixOfChoice(answerWeights.getReplyWeights(), rows, columns);
        } catch (IllegalArgumentException e) {
            logger.severe("getPossibleTotalPoints() Caught exception: " + e.getMessage() + "  The points will be set to 0.0 for this question.");
        }
        return totalPoints;
    }

    @Override
    public void onBlur(BlurEvent event) {
        logger.info("onBlur() called.");

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
            if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {

                MatrixOfChoicesSurveyQuestion question = (MatrixOfChoicesSurveyQuestion) surveyQuestion;
                SurveyItemProperties properties = question.getProperties();

                MatrixOfChoicesQuestion matrixOfChoicesQuestion = question.getQuestion();

                logger.info("question text: " + matrixOfChoicesQuestion.getText());
                questionHtml.setValue(matrixOfChoicesQuestion.getText());

                // print the question properties
                debugPrintQuestionProperties(properties);
                debugPrintQuestionProperties(matrixOfChoicesQuestion.getProperties());

                // Load the properties (if any)
                AbstractPropertySet propSet = getPropertySetByType(CustomAlignmentPropertySet.class.getName());
                CustomAlignmentPropertySet customAlignPropSet = (CustomAlignmentPropertySet) propSet;
                customAlignPropSet.load(properties);
                customAlignPropSet.load(matrixOfChoicesQuestion.getProperties());

                // Load the Common Properties
                propSet = getPropertySetByType(CommonPropertySet.class.getName());
                CommonPropertySet commonProps = (CommonPropertySet) propSet;
                commonProps.load(properties);
                commonProps.load(matrixOfChoicesQuestion.getProperties());
                commonProps.setSurveyQuestion(surveyQuestion);

                // Load the Image Display Properties
                propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
                QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
                imageProps.load(properties);
                imageProps.load(matrixOfChoicesQuestion.getProperties());

                // Load the Answer Set Properties
                propSet = getPropertySetByType(MOCAnswerSetsPropertySet.class.getName());
                MOCAnswerSetsPropertySet answerSetProps = (MOCAnswerSetsPropertySet) propSet;
                answerSetProps.load(properties);
                answerSetProps.load(matrixOfChoicesQuestion.getProperties());

                // Load the answer weights Properties
                propSet = getPropertySetByType(HiddenMOCAnswerWeightsPropertySet.class.getName());
                HiddenMOCAnswerWeightsPropertySet answerWeightsProps = (HiddenMOCAnswerWeightsPropertySet) propSet;
                answerWeightsProps.load(properties);
                answerWeightsProps.load(matrixOfChoicesQuestion.getProperties());

                // Load the difficulty and concepts Properties
                propSet = getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName());
                DifficultyAndConceptsPropertySet diffAndConceptsSet = (DifficultyAndConceptsPropertySet) propSet;
                diffAndConceptsSet.load(properties);
                diffAndConceptsSet.load(matrixOfChoicesQuestion.getProperties());

                columnOptionList = (OptionList) answerSetProps.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
                rowOptionList = (OptionList) answerSetProps.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
                scoringAttributes = ((QuestionScorer) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
                populateAnswers(columnOptionList, rowOptionList);
                answerWeights = (MatrixOfChoicesReplyWeights) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
                populateAnswerWeights();

                updateTotalQuestionFlag();

                // This should be called after all property sets have been loaded for the abstractsurveyelement.
                addUnsupportedProperties(matrixOfChoicesQuestion.getProperties(), properties);
                onLoadNotifyPropertySetChanges();

                refresh();
            } else {
                throw new LoadSurveyException("Trying to load a Matrix of Choices widget, but encountered non matrix of choices data from the database.", null);
            }
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }

    }

    /**
     * Populates existing answers based on the row and column option lists that were loaded
     *
     * @param columnOptions column option list
     * @param rowOptions row option list
     */
    private void populateAnswers(OptionList columnOptions, OptionList rowOptions) {
    	resetTable();
    	boolean columnIsShared = columnOptions.getIsShared();
    	boolean rowIsShared = rowOptions.getIsShared();

    	if(rowOptions != null){
    		logger.info("Populating row options with " + rowOptions.getListOptions().size() + " options");
    		for(ListOption option : rowOptions.getListOptions()){
    			addRowChoice(option.getText(), rowIsShared);
    		}
    	}
    	if(columnOptions != null){
    		logger.info("Populating column options with " + columnOptions.getListOptions().size() + " options");
    		for(ListOption option : columnOptions.getListOptions()){
    			addColumnChoice(option.getText(), columnIsShared);
    		}
    	}
    	if(!rowIsShared){
    		addRowChoice(null, rowIsShared);
    	}
    	if(!columnIsShared){
    		addColumnChoice(null, columnIsShared);
    	}

	}

    /**
     * Populates the answer weights that were loaded. If non-scored, answer weights
     * should be empty and all weight values will be set to 0.0 and the scores
     * won't be taken into account anywhere
     */
    private void populateAnswerWeights() {

        // subtract 1 from the row/column length if not a shared option list since it will have a placeholder
        int rowLength = (rowOptionList.getIsShared() ? choicesContainer.getRowCount() : (choicesContainer.getRowCount() -1)) - 1;
        int colLength = (columnOptionList.getIsShared() ? choicesContainer.getColumnCount() : (choicesContainer.getColumnCount() -1)) - 1;

        List<List<Double>> weights = answerWeights.getReplyWeights();

    	logger.info("Populating answer weights");
        int rows = weights.size();
        int columns = (rows > 0 ? weights.get(0).size() : 0);

    	for(int i = 1; i <= rows; i++){
    		for(int j = 1; j <= columns; j++){
    		    if (i-1 < rowLength && j-1 < colLength) {
                    if(choicesContainer.getWidget(i, j) instanceof FlowPanel){
                        FlowPanel panel = (FlowPanel) choicesContainer.getWidget(i, j);
                        DoubleBox doubleBox = (DoubleBox) panel.getWidget(1);
                        if(weights.get(i - 1).size() > j - 1){
                            doubleBox.setValue(weights.get(i - 1).get(j - 1));
                        } else {
                            doubleBox.setValue(0.0);
                        }
                    }
    		    } else {
    		        logger.warning("answerWeights index out of bounds at index [ "+i+",  "+j+"]");
    		    }
    		}
    	}
    	onEditorModeChanged(getEditMode());
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
    	((QuestionScorer) getPropertySetByType(HiddenMOCAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).setAttributeScorers(scoringAttributes);
    	logger.info("set scoring attributes = " + ((QuestionScorer) getPropertySetByType(HiddenMOCAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers());
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
     * @param properties
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
		this.questionHtml.setEditable(!readOnly);
		if (readOnly) {
		    this.questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
            this.questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
		}

		Iterator<Widget> itr = widgetToGridLocation.keySet().iterator();
        Widget w = null;

        while(itr.hasNext()){
            w = itr.next();

            try{

                if(w instanceof MatrixOfChoicesResponseWidget) {
                    MatrixOfChoicesResponseWidget response = (MatrixOfChoicesResponseWidget)w;
                    response.refresh(isReadOnly, getEditMode());
                }
            } catch (Exception e){
                logger.warning("Exception caught when trying to set read only mode for Matrix of Choice Widget responses: " + e);
            }
        }
	}
}
