/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.LinkedGroup;
import org.gwtbootstrap3.client.ui.LinkedGroupItem;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The panel that is used to explore and select a condition to author.
 * 
 * @author tflowers
 */
public class ConditionSelectionPanel extends Composite implements HasValue<ConditionInfo> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConditionSelectionPanel.class.getName());

    /** Defines the binder for combining this java class with ui.xml */
    private static ConditionSelectionPanelUiBinder uiBinder = GWT.create(ConditionSelectionPanelUiBinder.class);

    /** Defines the interface that combines ui.xml with a java class */
    interface ConditionSelectionPanelUiBinder extends UiBinder<Widget, ConditionSelectionPanel> {
    }

    /** The button to select a training application to select a condition for */
    @UiField
    protected Button selectAppButton;

    /** The list that contains the conditions that match the selected app */
    @UiField
    protected LinkedGroup conditionList;

    /** The button that toggles the learner action collapse panel */
    @UiField
    protected Button learnerActionCollapseBtn;

    /** The collapse panel for the learner actions */
    @UiField
    protected Collapse learnerActionCollapse;

    /** The list that contains the conditions for possible learner actions */
    @UiField
    protected LinkedGroup learnerActionConditionList;

    /** The drop down that contains each of the training applications */
    @UiField
    protected DropDownMenu trainingAppDropDown;

    /** The widget that displays a description of {@link #selectedCondition} */
    @UiField
    protected HTML conditionDescription;

    /** The condition that has been selected by the user through this widget. */
    private ConditionInfo selectedCondition = null;

    /**
     * Constructs the panel for selecting the condition to author
     * 
     * @param trainingApp The training application that a condition is being
     *        authored for.
     */
    public ConditionSelectionPanel(TrainingApplicationEnum trainingApp) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
        
        initWidget(uiBinder.createAndBindUi(this));

        learnerActionCollapseBtn.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (learnerActionCollapse.isShown()) {
                    learnerActionCollapse.hide();
                } else {
                    learnerActionCollapse.show();
                }
            }
        }, ClickEvent.getType());

        /* Populate the training app drop down. If the training app is excluded
         * (blocked from being selected or shown), default it to the most
         * generic training application possible (Simple Example TA) so that the
         * user is able to see something and can then proceed in choosing
         * whichever makes most sense. */
        updateConditionChoices(
                ScenarioClientUtility.isTrainingAppExcluded(trainingApp) ? TrainingApplicationEnum.SIMPLE_EXAMPLE_TA
                        : trainingApp);
        for (final TrainingApplicationEnum taEnum : TrainingApplicationEnum.VALUES()) {
            /* CUSTOM EXCLUSIONS */
            if (ScenarioClientUtility.isTrainingAppExcluded(taEnum)) {
                continue;
            }
            
            AnchorListItem listItem = new AnchorListItem();
            listItem.setText(taEnum.getDisplayName());
            listItem.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    updateConditionChoices(taEnum);
                }
            });

            trainingAppDropDown.add(listItem);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ConditionInfo> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public ConditionInfo getValue() {
        return selectedCondition;
    }

    @Override
    public void setValue(ConditionInfo selectedCondition) {
        setValue(selectedCondition, false);
    }

    @Override
    public void setValue(ConditionInfo selectedCondition, boolean fireEvents) {
        this.selectedCondition = selectedCondition;
        if (fireEvents) {
            ValueChangeEvent.fire(this, this.selectedCondition);
        }
    }

    /**
     * Updates the conditions shown for a provided
     * {@link TrainingApplicationEnum}
     * 
     * @param taEnum The training app for which to display conditions
     */
    private void updateConditionChoices(final TrainingApplicationEnum taEnum) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateConditionChoices(" + taEnum + ")");
        }

        selectAppButton.setText(taEnum.getDisplayName());

        // reset
        conditionList.clear();
        learnerActionConditionList.clear();
        setValue(null);
        conditionDescription.setHTML("");

        ScenarioClientUtility.getConditionsForTrainingApp(taEnum, new AsyncCallback<Set<ConditionInfo>>() {

            @Override
            public void onSuccess(Set<ConditionInfo> result) {
                if (result == null) {
                    logger.severe("The server found no conditions for the Training Application '" + taEnum + "'");
                    return;
                }

                setConditions(result, conditionList);
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE,
                        "There was an error fetching the conditions for the Training Application '" + taEnum + "'",
                        caught);
            }
        });

        ScenarioClientUtility.getConditionsForLearnerActions(new AsyncCallback<List<ConditionInfo>>() {

            @Override
            public void onSuccess(List<ConditionInfo> result) {
                if (result == null) {
                    logger.severe("The server found no conditions for learner actions");
                    return;
                }

                setConditions(result, learnerActionConditionList);
            }

            @Override
            public void onFailure(Throwable caught) {
                logger.log(Level.SEVERE,
                        "There was an error fetching the conditions for learner actions",
                        caught);
            }
        });
    }

    /**
     * Update the {@link #conditionList} to show the provided conditions
     * 
     * @param conditions An {@link Iterable} of {@link ConditionInfo}
     * @param conditionList the UI component to add items too
     */
    private void setConditions(Iterable<ConditionInfo> conditions, final LinkedGroup conditionList) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("setConditions([");
            StringUtils.join(", ", conditions, new Stringifier<ConditionInfo>() {
                @Override
                public String stringify(ConditionInfo obj) {
                    return obj.getDisplayName();
                }
            }, sb);
            sb.append("])");
            logger.fine(sb.toString());
        }

        ArrayList<LinkedGroupItem> items = new ArrayList<LinkedGroupItem>();

        for (final ConditionInfo cInfo : conditions) {

            if (ScenarioClientUtility.isPlayback() && ScenarioClientUtility.doesConditionRequireTrainingApp(cInfo)) {

                /* Skip showing any condition that requires a running training
                 * application while using a playback log */
                continue;

            } else if (GatClientUtility.isRtaLessonLevel() && ScenarioClientUtility.doesConditionRequireTutor(cInfo)) {

                //skip showing any condition that requires the TUI when GIFT's lesson level is set to RTA
                continue;
            }

            final LinkedGroupItem cItem = new LinkedGroupItem();
            cItem.setText(cInfo.getDisplayName());
            cItem.getElement().setAttribute("style", "padding-right:5px;padding-left:10px;");

            // the select button that will load the condition's editor
            EnforcedButton selectButton = new EnforcedButton(IconType.EXTERNAL_LINK, "", "Use this condition",
                    new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("selectConditionButton.onClick(" + event + ")");
                            }

                            setValue(cInfo, true);
                        }
                    });
            selectButton.getElement().setAttribute("style", "float:right;height:27px;margin-top:-6px;background-color:rgba(91, 192, 222, 1);");
            cItem.add(selectButton);
            cItem.addDomHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    // Unselect all items
                    for (int i = 0; i < conditionList.getWidgetCount(); i++) {
                        Widget w = conditionList.getWidget(i);
                        if (w instanceof LinkedGroupItem) {
                            LinkedGroupItem listItem = (LinkedGroupItem) w;
                            listItem.setActive(false);
                        }
                    }

                    // Select the item that was clicked
                    cItem.setActive(true);

                    setValue(cInfo);
                    conditionDescription.setHTML(SafeHtmlUtils.fromTrustedString(cInfo.getConditionDesc()));
                }
            }, ClickEvent.getType());

            items.add(cItem);
        }
        
        // Sort the conditions by display name
        Collections.sort(items, new Comparator<LinkedGroupItem>() {

            @Override
            public int compare(LinkedGroupItem o1, LinkedGroupItem o2) {
                return o1.getText().compareTo(o2.getText());
            }
            
        });

        for (LinkedGroupItem cItem : items) {
            conditionList.add(cItem);
        }
    }
}