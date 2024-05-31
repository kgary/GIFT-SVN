/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.PanelBody;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AnswerSetPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MOCAnswerSetsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MultiSelectPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.AnswerSetPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.CommonPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.CustomAlignmentPropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.CustomAlignmentPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.DynamicResponseFieldPropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.DynamicResponseFieldPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.MOCAnswerSetsPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.MultiSelectPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.QuestionImagePropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleAppearancePropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleAppearancePropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleImagePropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleImagePropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleLayoutPropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.ScaleLayoutPropertySetWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.SliderRangePropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.SliderRangePropertySetWidget;

/**
 * The properties panel widget displays the property widgets for the currently selected
 * item and allows the user to assign/unassign properties of the item.
 * 
 * @author nblomberg
 *
 */
public class PropertiesPanelWidget extends Composite  {

    /** Logger for the class */
    private static Logger logger = Logger.getLogger(PropertiesPanelWidget.class.getName());

    /** UiBinder which combines the ui.xml file with the java class */
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** The UiBinder which combines the ui.xml file with this java class */
	interface WidgetUiBinder extends
			UiBinder<Widget, PropertiesPanelWidget> {
	}

	@UiField
	PanelBody propContainer;

	@UiField
	Heading noPropLabel;

	@UiField
	protected BlockerPanel propBlocker;

	protected MultiSelectPropertySetWidget multiSelectWidget;

	/** The widget containing the properties that are displayed in the properties panel. */
	AbstractQuestionWidget displayedWidget = null;

	/** Public answer sets on the database, such as the Frequency Likert Scale */
	List<OptionList> sharedAnswerSets;

	/** The interface used to get the list of images that were obtained from the server. */
	SurveyImageInterface imageInterface = null;

	/**
	 * Constructor (default)
	 */
	public PropertiesPanelWidget(SurveyImageInterface imageInterface) {

	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));

	    displayProperties(null);

	    this.imageInterface = imageInterface;

	}

	/**
	 * Constructor (non-default)
	 */
	public PropertiesPanelWidget(SurveyImageInterface imageInterface, List<OptionList> sharedAnswerSets) {

	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    this.sharedAnswerSets = sharedAnswerSets;

	    displayProperties(null);

	    this.imageInterface = imageInterface;
	}

	/** 
	 * Displays the properties of the selected widget.
	 * 
	 * @param selectedWidget - The widget that is selected. Null is allowed.  If null, no properties are displayed.
	 */
    public void displayProperties(AbstractQuestionWidget selectedWidget) {

        if (logger.isLoggable(Level.INFO)) {
            logger.info("displayProperties() called.");
        }


        if (selectedWidget != null) {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("displayProperties() for widget of type: " + selectedWidget.getClass().getName());
            }

            // only rebuild the properties if the selected widget has been updated.
            if (selectedWidget != displayedWidget) {
                displayedWidget = selectedWidget;

                propContainer.clear();
                ArrayList<AbstractPropertySet> propSets = selectedWidget.getPropertySets();

                for (AbstractPropertySet propSet : propSets) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info("propSet class = " + propSet.getClass().getName() + " is hidden: " + propSet.isHiddenPropertySet());
                    }
                    Composite widget = null;

                    // Only display properties that are not hidden.
                    if (!propSet.isHiddenPropertySet()) {
                        if (propSet instanceof MultiSelectPropertySet) {
                            MultiSelectPropertySet custProp = (MultiSelectPropertySet) propSet;
                            widget = multiSelectWidget = new MultiSelectPropertySetWidget(custProp, selectedWidget);

                        } else if (propSet instanceof QuestionImagePropertySet) {
                            QuestionImagePropertySet custProp = (QuestionImagePropertySet) propSet;                       
                            widget = new QuestionImagePropertySetWidget(custProp, selectedWidget, imageInterface);

                        } else if (propSet instanceof CustomAlignmentPropertySet) {
                            CustomAlignmentPropertySet custProp = (CustomAlignmentPropertySet) propSet;                       
                            widget = new CustomAlignmentPropertySetWidget(custProp, selectedWidget); 

                        } else if (propSet instanceof ScaleAppearancePropertySet) {
                            ScaleAppearancePropertySet custProp = (ScaleAppearancePropertySet) propSet;
                            widget = new ScaleAppearancePropertySetWidget(custProp, selectedWidget);

                        } else if (propSet instanceof SliderRangePropertySet) {
                            SliderRangePropertySet custProp = (SliderRangePropertySet) propSet;
                            widget = new SliderRangePropertySetWidget(custProp, selectedWidget);

                        } else if (propSet instanceof ScaleImagePropertySet) {
                            ScaleImagePropertySet custProp = (ScaleImagePropertySet) propSet;
                            widget = new ScaleImagePropertySetWidget(custProp, selectedWidget, imageInterface);

                        } else if (propSet instanceof ScaleLayoutPropertySet) {
                            ScaleLayoutPropertySet custProp = (ScaleLayoutPropertySet) propSet;
                            widget = new ScaleLayoutPropertySetWidget(custProp, selectedWidget);

                        } else if (propSet instanceof AnswerSetPropertySet) {
                        	if(!(selectedWidget instanceof TrueFalseWidget)){
	                            AnswerSetPropertySet custProp = (AnswerSetPropertySet) propSet;
	                            widget = new AnswerSetPropertySetWidget(custProp, selectedWidget, sharedAnswerSets);
                        	}

                        } else if (propSet instanceof CommonPropertySet) {
                            CommonPropertySet custProp = (CommonPropertySet) propSet;
                            widget = new CommonPropertySetWidget(custProp, selectedWidget);    

                        } else if (propSet instanceof MOCAnswerSetsPropertySet) {
                        	MOCAnswerSetsPropertySet custProp = (MOCAnswerSetsPropertySet) propSet;
                            widget = new MOCAnswerSetsPropertySetWidget(custProp, selectedWidget, sharedAnswerSets);    

                        } else if (propSet instanceof DynamicResponseFieldPropertySet) {
                            DynamicResponseFieldPropertySet custProp = (DynamicResponseFieldPropertySet) propSet;
                            
                            if (selectedWidget instanceof FreeResponseWidget) {
                                FreeResponseWidget freeResponseWidget = (FreeResponseWidget) selectedWidget;
                                widget = new DynamicResponseFieldPropertySetWidget(custProp, selectedWidget,
                                        freeResponseWidget.isInQuestionBank());
                            } else {
                                widget = new DynamicResponseFieldPropertySetWidget(custProp, selectedWidget);
                            }
                        } else {

                            // Log an error to indicate that the property set should have an implementation here.
                            logger.severe("Trying to display properties for an unimplemented property set.  This means the property set " +
                                     "should be implemented: " + propSet.getClass().getName());
                        }
                    }

                    
                    
                    if (widget != null) {
                        propContainer.add(widget);

                        // Add a divider between property sets.
                        if (propContainer.getWidgetCount() > 0) {
                            HTML htmlWidget = new HTML();
                            htmlWidget.setHTML("<hr style='margin: 6.5px 0px'></hr>");
                            propContainer.add(htmlWidget);
                        }
                    }                    
                    
                }
            } else {
                // Do nothing since the properties for the same widget are already being displayed.
            }
            
        } else {
            displayedWidget = null;
            propContainer.clear();
        }

        
        if (propContainer.getWidgetCount() <= 0) {
            propContainer.add(noPropLabel);
        }
        
    }

    /**
     * Gets the MultiSelectPropertySetWidget if it is available.
     * 
     * @return the MultiSelectPropertySetWidget if it is available. Otherwise, returns null.
     */
    public MultiSelectPropertySetWidget getMultiSelectWidget() {
    	if(multiSelectWidget != null && multiSelectWidget.isAttached()) {
    		return multiSelectWidget;
    	} else {
    		return null;
    	}
    }

    /**
	 * Sets whether or not this widget should be read-only
	 * 
	 * @param readOnly whether or not this widget should be read-only
	 */
	public void setReadOnlyMode(boolean readOnly) {
        logger.fine("setReadOnlyMode(" + readOnly + ")");
		propBlocker.setVisible(readOnly);
	}

}