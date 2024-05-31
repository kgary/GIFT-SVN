/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyResponseTypeEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.events.FreeResponseScoringWidgetChangedEvent;

/**
 * The FreeResponseScoringWidget allows the author to specify the score for a specific response
 * field within a FreeResponseWidget. Each FreeResponseScoringWidget can contain multiple
 * FreeResponseScoringRangeWidgets.
 * 
 * @author sharrison
 *
 */
public class FreeResponseScoringWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(FreeResponseScoringWidget.class.getName());

    /** UI Binder */
    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    /** UI Binder interface */
    interface WidgetUiBinder extends UiBinder<Widget, FreeResponseScoringWidget> {
    }

    @UiField
    protected Label nameLabel;

    @UiField(provided = true)
    protected Image addButton = new Image(GatClientBundle.INSTANCE.add_image());

    @UiField
    protected Container rangeWidgetContainer;

    /** Command to execute when a change occurs */
    private final Command onChangeCommand = new Command() {

        @Override
        public void execute() {
            validateRanges();
            fireScoringChangedEvent();
        }
    };

    /**
     * Constructor.
     * 
     * @param name the name of the widget to be displayed.
     * @param responseType the {@link SurveyResponseTypeEnum}. Will default to
     *        free text if null.
     * @param values the values to build and populate a pre-existing widget. If
     *        null or empty, the default will be created.
     * @param isInQuestionBank true if the parent survey is a question bank;
     *        false otherwise
     */
    public FreeResponseScoringWidget(String name, SurveyResponseTypeEnum responseType, List<List<Double>> values, boolean isInQuestionBank) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("FreeResponseScoringWidget(");
            List<Object> params = Arrays.<Object>asList(name, responseType, values, isInQuestionBank);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        initWidget(uiBinder.createAndBindUi(this));

        nameLabel.setText(name);

        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // create a new condition row.
                rangeWidgetContainer.insert(createFreeResponseScoringRangeWidget(SurveyResponseTypeEnum.NUMERIC), 0);
            }
        });

        if (isInQuestionBank || SurveyResponseTypeEnum.NUMERIC.equals(responseType)) {
            if (values == null || values.isEmpty()) {
                // new widget
                FreeResponseScoringRangeWidget rangeWidget = createFreeResponseScoringRangeWidget(
                        SurveyResponseTypeEnum.NUMERIC);

                /* we want this to be a default catch-all widget so hide the
                 * range panel and show the catch all label. */
                rangeWidget.showNumberRanges(false);
                rangeWidgetContainer.add(rangeWidget);
            } else {
                // load from existing
                populateFromScores(values);
            }
        } else {
            FreeResponseScoringRangeWidget rangeWidget = createFreeResponseScoringRangeWidget(SurveyResponseTypeEnum.FREE_TEXT);
            rangeWidget.setPointBoxEnabled(false);
            rangeWidget.setCatchAllLabelText("Free Text response fields cannot be scored");
            addButton.setVisible(false);
            rangeWidgetContainer.add(rangeWidget);
        }
    }

    /**
     * Validate the ranges and check for conflicts. If there are any conflicts, set the widgets to
     * invalid.
     */
    private void validateRanges() {
        if (rangeWidgetContainer != null) {
            Iterator<Widget> itr = rangeWidgetContainer.iterator();
            List<FreeResponseScoringRangeWidget> previousRanges = new ArrayList<FreeResponseScoringRangeWidget>();
            while (itr.hasNext()) {
                Widget itrWidget = itr.next();
                if (itrWidget instanceof FreeResponseScoringRangeWidget) {
                    FreeResponseScoringRangeWidget rangeWidget = ((FreeResponseScoringRangeWidget) itrWidget);
                    
                    Double minRange = rangeWidget.getMinRange();
                    Double maxRange = rangeWidget.getMaxRange();
                   
                    if (minRange != null || maxRange != null) {
                        // check if the range is conflicting with a previous range
                        boolean conflict = false;
                        for (FreeResponseScoringRangeWidget previous : previousRanges) {

                            // check if current values conflict with previous
                            if (previous.isConflict(rangeWidget) || rangeWidget.isConflict(previous)) {
                                conflict = true;
                                setWidgetInvalid(rangeWidget, true);
                                setWidgetInvalid(previous, true);
                            }
                        }

                        // if no conflict, clear invalid status
                        if (!conflict) {
                            setWidgetInvalid(rangeWidget, false);
                        }
                    }

                    boolean invalidRange = (minRange != null) && (maxRange != null) && (minRange > maxRange);

                    // set minimum invalid
                    if (invalidRange || minRange == null) {
                        rangeWidget.setMinInvalid(true);
                    }

                    // set maximum invalid
                    if (invalidRange || (rangeWidget.isRangeEnabled() && maxRange == null)) {
                        rangeWidget.setMaxInvalid(true);
                    }

                    previousRanges.add(rangeWidget);
                }
            }
        }
    }
    
    /**
     * Set the given widget's min and max ranges to valid or invalid.
     * 
     * @param widget the widget to update.
     * @param invalid true if the widget is conflicted; false if it is not.
     */
    private void setWidgetInvalid(FreeResponseScoringRangeWidget widget, boolean invalid) {
        if (widget != null) {
            widget.setMinInvalid(invalid);
            widget.setMaxInvalid(invalid);
        }
    }
    
    /**
     * Fires the event to send the notification that the scoring has changed (either the points or
     * the condition range).
     */
    private void fireScoringChangedEvent() {
        SharedResources.getInstance().getEventBus().fireEvent(new FreeResponseScoringWidgetChangedEvent(this));
    }

    /**
     * Executes command to remove any range widgets that have been marked as 'to be removed'.
     */
    public List<List<Double>> updateScores() {
        List<List<Double>> widgetScoresList = new ArrayList<List<Double>>();
        if (rangeWidgetContainer != null) {
            Iterator<Widget> itr = rangeWidgetContainer.iterator();
            while (itr.hasNext()) {
                Widget rangeWidget = itr.next();
                if (rangeWidget instanceof FreeResponseScoringRangeWidget) {
                    List<Double> scores = ((FreeResponseScoringRangeWidget) rangeWidget).getWeightedScores();
                    if (scores != null) {
                        widgetScoresList.add(scores);
                    }
                }
            }
        }
        
        return widgetScoresList;
    }

    /**
     * Populate the widget container using the existing scores. Each list of doubles will create a
     * new widget.
     * 
     * @param values the existing scoring values to create the widgets from.
     */
    private void populateFromScores(List<List<Double>> values) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateFromScores(" + values + ")");
        }

        // start fresh
        rangeWidgetContainer.clear();

        for (int i = 0; i < values.size(); i++) {
            List<Double> scoringRange = values.get(i);
            FreeResponseScoringRangeWidget scoringRangeWidget = createFreeResponseScoringRangeWidget(SurveyResponseTypeEnum.NUMERIC);

            // list will contain scoring points, (optional) min range, (optional) max range
            if (scoringRange.size() >= 1) {
                scoringRangeWidget.setPointBoxValue(scoringRange.get(0));

                // optional min range
                if (scoringRange.size() >= 2) {
                    scoringRangeWidget.setMinRange(scoringRange.get(1));

                    // optional max range
                    if (scoringRange.size() == 3) {
                        scoringRangeWidget.setMaxRange(scoringRange.get(2));
                    }
                }
            }

            // last widget is always catch all, hide number ranges.
            if (i == values.size() - 1) {
                scoringRangeWidget.showNumberRanges(false);
            }

            // insert widget
            rangeWidgetContainer.add(scoringRangeWidget);
        }
        
        // run widget scoring range validation
        validateRanges();
    }

    /**
     * Creates a new instance of {@link FreeResponseScoringRangeWidget}.
     * 
     * @param responseType the {@link SurveyResponseTypeEnum} of this widget.
     * @return the scoring range widget.
     */
    private FreeResponseScoringRangeWidget createFreeResponseScoringRangeWidget(SurveyResponseTypeEnum responseType) {
        final FreeResponseScoringRangeWidget rangeWidget = new FreeResponseScoringRangeWidget(responseType, onChangeCommand);
        rangeWidget.getRemoveButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                rangeWidgetContainer.remove(rangeWidget);
                onChangeCommand.execute();
            }
        });

        return rangeWidget;
    }
}
