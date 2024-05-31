/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.Container;
import generated.course.ConceptQuestions;
import generated.course.ConceptQuestions.AssessmentRules;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.mbp.MbpView.CandidateConcept;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.NumericTextInputCell;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

/**
 * @author nroberts
 *
 */
public class KnowledgeAssessmentQuestionsPanel extends Composite {

	private static KnowlegdeAssessmentQuestionsPanelUiBinder uiBinder = GWT
			.create(KnowlegdeAssessmentQuestionsPanelUiBinder.class);

	interface KnowlegdeAssessmentQuestionsPanelUiBinder extends
			UiBinder<Widget, KnowledgeAssessmentQuestionsPanel> {
	}
	
	/** The style to use for number boxes in this view */
    private static final String NUMBER_BOX_STYLE =  
			"text-align: right;"
			+ "max-width: 30px;"
			+ "margin: 0 auto;"
			+ "display: block;"
	;
	
	@UiField(provided=true)
    protected CellTable<ConceptQuestions> questionCellTable = new CellTable<ConceptQuestions>();
   
    private HashMap<String, KnowledgeAssessmentSlider> conceptSliderMap = new HashMap<>();
    
    private HashMap<String, KnowledgeAssessmentSlider> extraneousConceptSliderMap = new HashMap<>();
    
    @UiField
    protected Container sliderPanel;
    
// -- 'Questions:' cell table column definitions ------------------------------------------------------------------------------------
    
    /** The 'Concept' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> questionConceptColumn = new Column<ConceptQuestions, String>(new TextCell()){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getName() != null){
				return question.getName();
			}
			
			return null;
		}
    	
    };
    
    /** The 'Number of Easy Questions' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> easyColumn = new Column<ConceptQuestions, String>(new NumericTextInputCell(NUMBER_BOX_STYLE)){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getQuestionTypes() != null && question.getQuestionTypes().getEasy() != null){
				return question.getQuestionTypes().getEasy().toString();
			}
			
			return null;
		}
		
        @Override
        public void render(Context context, ConceptQuestions question, SafeHtmlBuilder sb) {
            
            if(GatClientUtility.isReadOnly()){
                sb.appendHtmlConstant(getValue(question));
            }else{
                super.render(context, question, sb);
            }

         }
    	
    };
    
    /** The 'Number of Medium Questions' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> mediumColumn = new Column<ConceptQuestions, String>(new NumericTextInputCell(NUMBER_BOX_STYLE)){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getQuestionTypes() != null && question.getQuestionTypes().getMedium() != null){
				return question.getQuestionTypes().getMedium().toString();
			}
			
			return null;
		}
		
        @Override
        public void render(Context context, ConceptQuestions question, SafeHtmlBuilder sb) {
            
            if(GatClientUtility.isReadOnly()){
                sb.appendHtmlConstant(getValue(question));
            }else{
                super.render(context, question, sb);
            }

         }
    	
    };
    
    /** The 'Number of Hard Questions' column for the 'Questions:' table */
    private Column<ConceptQuestions, String> hardColumn = new Column<ConceptQuestions, String>(new NumericTextInputCell(NUMBER_BOX_STYLE)){

		@Override
		public String getValue(ConceptQuestions question) {

			if(question.getQuestionTypes() != null && question.getQuestionTypes().getHard() != null){
				return question.getQuestionTypes().getHard().toString();
			}
			
			return null;
		}
		
        @Override
        public void render(Context context, ConceptQuestions question, SafeHtmlBuilder sb) {
            
            if(GatClientUtility.isReadOnly()){
                sb.appendHtmlConstant(getValue(question));
            }else{
                super.render(context, question, sb);
            }

         }
    	
    };
    
	public KnowledgeAssessmentQuestionsPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		
        questionCellTable.setPageSize(Integer.MAX_VALUE);
		
		// attach the 'Questions:' cell table to its associated columns
        
        questionCellTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
        
        questionCellTable.addColumn(questionConceptColumn, "Concept");       
        questionCellTable.setColumnWidth(questionConceptColumn, "33%");
        
        questionCellTable.addColumn(easyColumn, new SafeHtmlHeader(new SafeHtmlBuilder().appendHtmlConstant(
        		"<div title='The number of easy-difficulty questions to show for each concept'>Easy</div>"
        	).toSafeHtml())
        );
        questionCellTable.setColumnWidth(easyColumn, "22%");
        
        questionCellTable.addColumn(mediumColumn, new SafeHtmlHeader(new SafeHtmlBuilder().appendHtmlConstant(
        		"<div title='The number of medium-difficulty questions to show for each concept'>Medium</div>"
        	).toSafeHtml())
        );
        questionCellTable.setColumnWidth(mediumColumn, "22%");
        
        questionCellTable.addColumn(hardColumn, new SafeHtmlHeader(new SafeHtmlBuilder().appendHtmlConstant(
        		"<div title='The number of hard-difficulty questions to show for each concept'>Hard</div>"
        	).toSafeHtml())
        );
        questionCellTable.setColumnWidth(hardColumn, "22%");
        
        VerticalPanel emptyQuestionsWidget = new VerticalPanel();
        
        emptyQuestionsWidget.add(new HTML("<br>"));
        
        Label emptyQuestionsLabel = new Label("Please select one or more concepts above to begin editing concept questions.");
        emptyQuestionsLabel.setHorizontalAlignment(Label.ALIGN_CENTER);
        
        emptyQuestionsWidget.add(emptyQuestionsLabel);
        
        emptyQuestionsWidget.add(new HTML("<br>"));
        
        questionCellTable.setEmptyTableWidget(emptyQuestionsWidget);
        
	}
	
    public HasData<ConceptQuestions> getQuestionCellTable(){
    	return questionCellTable;
    }
    
    public void setEasyColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
    	easyColumn.setFieldUpdater(updater);
    }
    
    public void setMediumColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
    	mediumColumn.setFieldUpdater(updater);
    }
    
    public void setHardColumnFieldUpdater(FieldUpdater<ConceptQuestions, String> updater){
    	hardColumn.setFieldUpdater(updater);
    }
    
    public void redrawQuestionsCellTable(){
    	questionCellTable.redraw();
    }
    
    /**
     * Appends a slider widget to the view
     * 
     * @param conceptName The concept name associated with this slider
     * @return The created slider widget or null if nothing was created.
     */
    public KnowledgeAssessmentSlider appendSlider(String conceptName) {
    	if(extraneousConceptSliderMap.containsKey(conceptName)) {
    		KnowledgeAssessmentSlider slider = extraneousConceptSliderMap.get(conceptName);
    		slider.setExtraneous(false);
    		conceptSliderMap.put(conceptName, slider);
    		return slider;
    		
    	} else if(!conceptSliderMap.containsKey(conceptName)) {
	    	KnowledgeAssessmentSlider slider = new KnowledgeAssessmentSlider(conceptName);
	    	conceptSliderMap.put(conceptName, slider);
	    	sliderPanel.add(slider);
	    	return slider;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Removes the slider associated with the concept name from the view
     * 
     * @param conceptName The concept name
     */
    public void removeSlider(String conceptName) {
    	if(conceptSliderMap.containsKey(conceptName)) {
    		sliderPanel.remove(conceptSliderMap.get(conceptName));
    		conceptSliderMap.remove(conceptName);
    	}
    }
    
    /**
     * Updates the slider widget associated with the concept name
     * 
     * @param conceptName The name of the concept
     * @param totalQuestions The total number of questions available.
     * @param assessmentRules The assessment rules to update the slider with
     * @return the slider widget, or null if there is no slider associated with the concept name.
     */
    public KnowledgeAssessmentSlider updateSlider(String conceptName, int totalQuestions, AssessmentRules assessmentRules) {
    	if(conceptSliderMap.containsKey(conceptName)) {
    		KnowledgeAssessmentSlider slider = conceptSliderMap.get(conceptName);
    		slider.setRange(totalQuestions, assessmentRules);
    		return slider;
    	}
    	return null;
    }
    
    /**
     * Adds a slider representing an extraneous concept to the view.
     * 
     * @param conceptName The name of the concept
     * @param totalQuestions The total number of questions available.
     * @param assessmentRules The assessment rules to update the slider with
     * @param removeCmd The command to execute when the slider is removed.
     */
    public void appendExtraneousSlider(final String conceptName, int totalQuestions, AssessmentRules assessmentRules, final ScheduledCommand removeCmd) {
    	if(!extraneousConceptSliderMap.containsKey(conceptName)) {
	    	KnowledgeAssessmentSlider slider = new KnowledgeAssessmentSlider(conceptName);
	    	extraneousConceptSliderMap.put(conceptName, slider);
	    	slider.setRange(totalQuestions, assessmentRules);
	    	slider.setExtraneous(true);
	    	slider.setRemoveHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					KnowledgeAssessmentSlider slider = extraneousConceptSliderMap.remove(conceptName);
					sliderPanel.remove(slider);
					removeCmd.execute();
				}
	    	});
	    	sliderPanel.add(slider);
    	}
    }
    
    /**
     * Refreshes the list of knowledge assessment sliders
     * 
     * @param conceptList The list of selected concepts
     */
    public void refreshSliderPanel(List<CandidateConcept> conceptList) {
    	List<String> currentConcepts = new ArrayList<String>();
    	currentConcepts.addAll(conceptSliderMap.keySet());
    	
    	int index = 0;
    	for(CandidateConcept concept : conceptList) {
    		currentConcepts.remove(concept.getConceptName());
    		if(conceptSliderMap.containsKey(concept.getConceptName())) {
    			// Move the widget to the correct index
    			sliderPanel.insert(conceptSliderMap.get(concept.getConceptName()), index);
    			index += 1;
    		}
    	}
    	
    	for(String concept : currentConcepts) {
    		removeSlider(concept);
    	}
    }
    
    public void undoQuestionsTableChanges(ConceptQuestions question){
    	((NumericTextInputCell) easyColumn.getCell()).clearViewData(question);
    	((NumericTextInputCell) mediumColumn.getCell()).clearViewData(question);
    	((NumericTextInputCell) hardColumn.getCell()).clearViewData(question);
    }
    
}
