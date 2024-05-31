/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.LearnerAction;
import generated.dkf.Strategy;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * The widget that displays a list of learner actions that reference a single instructional strategy within the DKF.
 * 
 * @author mhoffman
 *
 */
public class LearnerActionReferenceWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LearnerActionReferenceWidget.class.getName());

    /** Combines this java class with the ui.xml */
    private static final LearnerActionReferenceWidgetUiBinder uiBinder = GWT.create(LearnerActionReferenceWidgetUiBinder.class);

    /** Defines the binder that combines the java class with the ui.xml */
    interface LearnerActionReferenceWidgetUiBinder extends UiBinder<Widget, LearnerActionReferenceWidget> {
    }

    /** The collapse that contains {@link #listEditor} */
    @UiField
    protected Collapse collapse;

    /** The header/title for the control */
    @UiField
    protected PanelHeader panelHeader;
    
    /**
     * The control that displays the each of the elements contained within this
     * control
     */
    @UiField(provided = true)
    protected GenericListEditor<LearnerAction> listEditor = new GenericListEditor<LearnerAction>(new Stringifier<LearnerAction>() {
        @Override
        public String stringify(LearnerAction learnerAction) {
            return learnerAction.getDisplayName();
        }
    });
    
    /** Action to jump to the selected learner action page. This will be visible for each item in the {@link GenericListEditor} table. */
    protected ItemAction<LearnerAction> rowJumpToAction = new ItemAction<LearnerAction>() {
        
        @Override
        public boolean isEnabled(LearnerAction item) {
            return true;
        }
        
        @Override
        public String getTooltip(LearnerAction item) {
            return "Click to navigate to this learner action";
        }
        
        @Override
        public IconType getIconType(LearnerAction item) {
            return IconType.EXTERNAL_LINK;
        }
        
        @Override
        public void execute(LearnerAction item) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("jumpToAction.execute(" + item + ")");
            }
            
            ScenarioEventUtility.fireJumpToEvent(ScenarioClientUtility.getAvailableLearnerActions(), item, false);
        }
    };
    
    /** action that will add a jump icon to the row, doesn't actually jump.  Jump to is handled by the row action */
    protected ItemAction<LearnerAction> columnJumpToAction = new ItemAction<LearnerAction>() {
        
        @Override
        public boolean isEnabled(LearnerAction item) {
            return true;
        }
        
        @Override
        public String getTooltip(LearnerAction item) {
            return "Click to navigate to this learner action";
        }
        
        @Override
        public IconType getIconType(LearnerAction item) {
            return IconType.EXTERNAL_LINK;
        }
        
        @Override
        public void execute(LearnerAction item) {
            // do nothing, this action is just for the icon and tooltip... the row action will handle this jump
        }
    };

    /**
     * The scenario object for which the referencing
     * {@link generated.dkf.LearnerAction} are being shown
     */
    private Serializable parentScenarioObject = null;

    /**
     * Default constructor for the {@link LearnerActionReferenceWidget}
     */
    public LearnerActionReferenceWidget() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));
 
        // Populate the list widget
        // the column action is just for the jump icon, the row action handles jumping too
        listEditor.setColumnActions(Arrays.asList(columnJumpToAction));
        listEditor.setRowAction(rowJumpToAction);

        // Populate the collapsible sections with a randomly generate id
        String id = Document.get().createUniqueId();
        panelHeader.setDataTarget("#" + id);
        collapse.setId(id);

        updateReadOnly();
    }

    /**
     * Adds the provided {@link LearnerAction} to the UI. A call to refresh is
     * not necessary.
     * 
     * @param learnerAction The learnerAction to add to the list. Can't be null.
     */
    public void add(LearnerAction learnerAction) {
        if (learnerAction == null) {
            throw new IllegalArgumentException("The parameter 'learnerAction' cannot be null.");
        }

        listEditor.addItem(learnerAction);
    }

    /**
     * Removes the provided {@link LearnerAction} from the UI. A call to
     * refresh is not necessary.
     * 
     * @param learnerAction The learnerAction to remove from the list. Can't be null.
     */
    public void remove(LearnerAction learnerAction) {
        if (learnerAction == null) {
            throw new IllegalArgumentException("The parameter 'learnerAction' cannot be null.");
        }

        listEditor.removeItem(learnerAction);
    }

    /**
     * Updates the UI to reflect any mutations to the underlying
     * {@link LearnerAction} contained within this widget
     */
    public void refresh() {
        listEditor.refresh();
    }

    /**
     * Sets the label/description for the table editor.
     * 
     * @param html The HTML as a {@link String} to display as the description.
     *        The value can be null.
     */
    public void setTableLabel(String html) {
        listEditor.setTableLabel(html);
    }

    /**
     * Shows each {@link LearnerAction} that references the given
     * {@link Strategy}
     * 
     * @param strategy The {@link Strategy} for which to find references.
     */
    public void showLearnerActions(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showTransitions(" + strategy + ")");
        }
        
        parentScenarioObject = strategy;
        HashSet<LearnerAction> learnerActions = strategy == null ? new HashSet<LearnerAction>()
                : ScenarioClientUtility.getLearnerActionsThatReferenceStrategy(strategy.getName());
        listEditor.replaceItems(learnerActions);

        updateReadOnly();
    }
    
    /**
     * Return whether the list of learner actions in this widget is empty.
     * @return true if no learner action references are listed in this widget.
     */
    public boolean isEmpty(){
        return listEditor.size() == 0;
    }

    /**
     * Updates the read only mode based on the state of the widget.
     */
    private void updateReadOnly() {
        boolean isReadOnly = parentScenarioObject == null || ScenarioClientUtility.isReadOnly();
        setReadonly(isReadOnly);
    }

    /**
     * Sets the read only mode of the widget
     * 
     * @param isReadOnly If true, disable all widgets that allow editing. If
     *        false, enable all widgets that allow editing.
     */
    private void setReadonly(boolean isReadOnly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadOnly + ")");
        }

    }
}