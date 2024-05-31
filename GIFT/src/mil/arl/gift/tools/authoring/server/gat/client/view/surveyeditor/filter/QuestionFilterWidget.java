/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.filter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.FilterChangeEvent;

/**
 * The widget containing the filter for the questions. The Survey Editor Panel uses this widget to filter questions.
 * 
 * @author tflowers
 *
 */
public class QuestionFilterWidget extends Composite {
    
    private static Logger logger = Logger.getLogger(QuestionFilterWidget.class.getName());
    
    private static QuestionFilterWidgetUiBinder uiBinder = GWT.create(QuestionFilterWidgetUiBinder.class);

    interface QuestionFilterWidgetUiBinder extends
            UiBinder<Widget, QuestionFilterWidget> {
    }
    
    @UiField
    protected MultipleSelect conceptSelection;
    
    @UiField
    protected TextBox searchBox;
    
    @UiField
    protected Button searchButton;
    
    /** The filter for the questions */
    private final QuestionFilter filter = new QuestionFilter();
    
    /** Determines if the filter is active or not */
    private boolean isFilterActive = false;
    
    private String currentSearchTerm = null;
    
    /**
     * Constructor
     * 
     * @param concepts - the list of possible concepts to filter by. If null, concept drop down is not set.
     */
    public QuestionFilterWidget(Iterable<String> concepts) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("ctor(" + concepts + ")");
        }
        
        initWidget(uiBinder.createAndBindUi(this));
        
        updateFilterActive();
        populateConceptDropdown(concepts);
    }
    
    /**
     * Returns the active question filter
     * @return The filter that is currently active or null if no filter is active
     */
    public QuestionFilter getFilter() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("getFilter()");
        }
        
        return isFilterActive ? filter : null;
    }
    
    /**
     * Gets the active status of the filter
     * 
     * @return if the filter is active
     */
    public boolean isFilterActive() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("isFilterActive()");
        }
        
        return isFilterActive;
    }
    
    @UiHandler("conceptSelection")
    protected void onSelectedConceptsChanged(ValueChangeEvent<List<String>> event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("onSelectedConceptsChanged(")
                    .append(event.getValue())
                    .append(")")
                    .toString());
        }
        
        //Determine if filter is active
        updateFilterActive();
        
        //Update the question filter with the selected concepts
        filter.setConcepts(event.getValue());
        
        //Raise event to alert the SurveyEditorPanel that the filter has changed
        SharedResources.getInstance().getEventBus().fireEvent(new FilterChangeEvent());
    }
    
    @UiHandler("searchBox")
    protected void onSearchTextChange(KeyUpEvent keyPress) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("onSearchTextChange(")
                    .append(keyPress)
                    .append(")")
                    .toString());
        }
        
        //Handle submission
        if(keyPress.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
            searchButton.click();
        }
        
        //Handle cancel change
        if(keyPress.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
            searchBox.setText(currentSearchTerm);
            searchBox.setFocus(false);
        }
        
        updateSearchButton();
    }
    
    @UiHandler("searchButton")
    protected void onSearchButtonClick(ClickEvent event) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("onSearchButtonClick(")
                    .append(event)
                    .append(")")
                    .toString());
        }
        
        //Sets the term list appropriately based on the current state of the search box
        if(searchBoxMatchesCurrentSearchTerm()) {
            searchBox.setText(currentSearchTerm = null);
        } else {
            currentSearchTerm = searchBox.getText();
        }
        
        //Update the UI and the filter
        filter.setSearchTerms(currentSearchTerm);
        updateFilterActive();
        updateSearchButton();
        
        //Alert listeners that the filter has changed
        SharedResources.getInstance().getEventBus().fireEvent(new FilterChangeEvent());
    }
    
    /**
     * Populates the concept dropdown with the given concepts. Does nothing if the list is null
     * @param concepts - the list of possible concepts to filter by
     */
    private void populateConceptDropdown(Iterable<String> concepts) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("populateConceptDropdown(" + concepts + ")");
        }
        
        if (concepts != null) {
            for(String concept : concepts) {
                Option opt = new Option();
                opt.setText(concept);
                opt.setValue(concept.toLowerCase());
                conceptSelection.add(opt);
            }
        }
    }
    
    /**
     * Determines based off of the state of controllers 
     * whether or not there is an active filter.
     */
    private void updateFilterActive() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("updateFilterActive()");
        }
        
        isFilterActive = !searchBox.getText().isEmpty() 
                || !conceptSelection.getValue().isEmpty();
    }
    
    /**
     * Updates the state of the search button based off of the 
     * state of the search box
     */
    private void updateSearchButton() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("updateSearchButton()");
        }
        
        if(searchBoxMatchesCurrentSearchTerm()) {
            searchButton.setType(ButtonType.DANGER);
            searchButton.setIcon(IconType.CLOSE);
        } else {
            searchButton.setType(ButtonType.PRIMARY);
            searchButton.setIcon(IconType.SEARCH);
        }
    }
    
    /**
     * Determines whether the current text in the search box is the text 
     * that has been searched for.
     * @return true if the text in the search box matches the active search term
     */
    private boolean searchBoxMatchesCurrentSearchTerm() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("searchBoxMatchesCurrentSearchTerm()");
        }
        
        return searchBox.getText().equalsIgnoreCase(currentSearchTerm);
    }
}