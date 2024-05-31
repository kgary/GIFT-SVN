/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.LearnerAction;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.LearnerActionEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The panel that appears to allow the user to browse and select a
 * {@link LearnerAction} from the {@link Scenario}
 * 
 * @author tflowers
 *
 */
public class LearnerActionSelectionPanel extends SimplePanel {

    /** The logger for the class */
    static final Logger logger = Logger.getLogger(LearnerActionSelectionPanel.class.getName());

    /** The singleton instance of the class */
    private static LearnerActionSelectionPanel instance;

    /** The picker that is currently using this class */
    private static LearnerActionPicker currentPicker;

    /** The actinos that match the current search terms */
    private static ArrayList<LearnerAction> learnerActionsMatchingTerms = new ArrayList<LearnerAction>();

    /** The list editor that contains all learner actions. */
    protected ItemListEditor<LearnerAction> learnerActionList = new ItemListEditor<>(new LearnerActionEditor());

    /**
     * Private constructor that is used to enforce singleton pattern.
     */
    private LearnerActionSelectionPanel() {

        super();

        addStyleName("contextMenu");

        // initialize the learner action list
        learnerActionList.addStyleName("waypointSelector");
        learnerActionList.getElement().getStyle().setProperty("maxHeight", "400px");
        learnerActionList.getElement().getStyle().setOverflowY(Overflow.AUTO);

        learnerActionList.setPlaceholder("No learner actions were found.");
        learnerActionList.setRemoveItemDialogTitle("Delete Learner Action?");
        learnerActionList.setRemoveItemStringifier(new Stringifier<LearnerAction>() {

            @Override
            public String stringify(LearnerAction obj) {

                StringBuilder builder = new StringBuilder()
                        .append("<b>")
                        .append((obj != null && obj.getDisplayName() != null) ? obj.getDisplayName()
                                : "this learner action")
                        .append("</b>");

                return builder.toString();
            }
        });

        learnerActionList.setFields(buildListFields());

        learnerActionList.addCreateListAction("Click here to add a new learner action",
                new CreateListAction<LearnerAction>() {
            @Override
            public LearnerAction createDefaultItem() {
                return new LearnerAction();
            }
        });

        learnerActionList.addListChangedCallback(new ListChangedCallback<LearnerAction>() {

            @Override
            public void listChanged(ListChangedEvent<LearnerAction> event) {
                final LearnerAction learnerAction = event.getAffectedItems().get(0);
                if (ListAction.REMOVE.equals(event.getActionPerformed())) {
                    if(StringUtils.equals(currentPicker.getValue(), learnerAction.getDisplayName())) {
                        currentPicker.setValue(null);
                    }

                    // notify listeners that the learner action was removed
                    ScenarioEventUtility.fireDeleteScenarioObjectEvent(learnerAction, null);

                } else if (ListAction.ADD.equals(event.getActionPerformed())) {

                    LearnerAction newLearnerAction = learnerAction;
                    ScenarioEventUtility.fireCreateScenarioObjectEvent(newLearnerAction);

                    currentPicker.setValue(newLearnerAction.getDisplayName(), true);

                    loadAndFilterLearnerActions();
                }
            }
        });

        setWidget(learnerActionList);

        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                /* Nick: This logic imitates the auto-hiding logic in
                 * PopupPanel.previewNativeEvent() */
                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());
                boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent) || eventTargetsPartner(nativeEvent);
                if (eventTargetsPopupOrPartner) {
                    event.consume();
                }

                // Switch on the event type
                int type = nativeEvent.getTypeInt();
                switch (type) {

                case Event.ONMOUSEDOWN:
                case Event.ONTOUCHSTART:
                    /* Don't eat events if event capture is enabled, as this can
                     * interfere with dialog dragging, for example. */
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    if (!eventTargetsPopupOrPartner) {
                        hideSelector();
                        return;
                    }
                    break;
                }
            }
        });
    }

    /**
     * Constructs the item fields that are used within
     * {@link #learnerActionList}.
     * 
     * @return The list of {@link ItemField} objects that are used within
     *         {@link #learnerActionList}. Can't be null.
     */
    private List<ItemField<LearnerAction>> buildListFields() {
        List<ItemField<LearnerAction>> fields = new ArrayList<>();
        
        fields.add(new ItemField<LearnerAction>() {

            @Override
            public Widget getViewWidget(final LearnerAction item) {
                
                /* allow the author to select this learner action in the
                 * accompanying learner action picker via a button */
                Button selectButton = new Button();
                selectButton.setText("Select");
                selectButton.getElement().setAttribute("style", "padding: 3px 10px; border-radius: 20px;");
                selectButton.addMouseDownHandler(new MouseDownHandler() {
                    
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        event.stopPropagation();
                        
                        if(currentPicker != null) {
                            
                            /* update the learner action picker with the
                             * selected learner action */
                            currentPicker.setValue(item.getDisplayName(), true);
                            hideSelector();
                        }
                    }
                });
                
                selectButton.setType(ButtonType.PRIMARY);
                
                return selectButton;
            }
            
        });
        
        fields.add(new ItemField<LearnerAction>() {

            @Override
            public Widget getViewWidget(LearnerAction item) {

                Label label = new Label(item.getDisplayName());
                label.setTitle(item.getDisplayName());
                label.setWidth("150px");
                label.getElement().getStyle().setOverflow(Overflow.HIDDEN);
                label.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);

                if (!learnerActionsMatchingTerms.isEmpty() && learnerActionsMatchingTerms.contains(item)) {
                    label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                    label.getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
                }

                label.setTitle(item.getDisplayName());
                return label;
            }
        });
        
        return fields;
    }

    /**
     * Shows the singleton {@link LearnerActionSelectionPanel} with the provided
     * {@link LearnerActionPicker}.
     * 
     * @param picker The {@link LearnerActionPicker} for which to show the
     *        {@link LearnerActionSelectionPanel}.
     */
    public static void showSelector(LearnerActionPicker picker) {

        if (currentPicker == null || !currentPicker.equals(picker)) {

            /* if a different learner action picker is attempting to show the
             * selector, remove the selector from the old picker */
            getInstance().removeFromParent();
        }

        currentPicker = picker;

        /* apply the picker's search text as a filter and update the list of
         * learner actions */
        loadAndFilterLearnerActions();

        if (currentPicker != null) {
            /* show the selector below the picker */
            currentPicker.getSelectorPanel().add(getInstance());
            currentPicker.getSelectorPanel().show();
        }
    }

    /**
     * Separates each {@link LearnerAction} into matching on non-matching
     * categories to be displayed within the UI.
     */
    public static void loadAndFilterLearnerActions() {

        /* cancel any editing, otherwise buttons in the list will be stuck in a
         * disabled state */
        getInstance().learnerActionList.cancelEditing();

        String filterText = null;

        if (currentPicker != null && StringUtils.isNotBlank(currentPicker.getTextBox().getText())) {
            filterText = currentPicker.getTextBox().getText().toLowerCase();
        }

        List<LearnerAction> filteredLearnerActionList = new ArrayList<>();

        // determine search terms in the filter based on whitespace
        String[] searchTerms = null;

        if (filterText != null) {
            searchTerms = filterText.split("\\s+");
        }

        /* Initialize the 'matching' 'non-matching' lists */
        learnerActionsMatchingTerms.clear();
        List<LearnerAction> learnerActionMissingTerms = new ArrayList<>();

        /* Populate the 'matching' and 'non-matching' lists */
        for (LearnerAction learnerAction : ScenarioClientUtility.getUnmodifiableLearnerActionList()) {

            /* If there are no search terms, add it to the list of actions
             * missing terms. */
            if (searchTerms == null) {
                learnerActionMissingTerms.add(learnerAction);
                continue;
            }

            /* Check if each learner action contains the necessary search
             * terms */
            boolean missingTerm = false;
            for (int i = 0; i < searchTerms.length; i++) {
                String name = learnerAction.getDisplayName();
                if (name == null || !name.toLowerCase().contains(searchTerms[i])) {
                    missingTerm = true;
                    break;
                }
            }

            /* Add the learner action to the either the matching list or
             * non-matching list */
            if (!missingTerm) {
                learnerActionsMatchingTerms.add(learnerAction);
            } else {
                learnerActionMissingTerms.add(learnerAction);
            }
        }

        /* Populate the list of learner actions, with learner actions that match
         * search terms listed first */
        filteredLearnerActionList.addAll(learnerActionsMatchingTerms);
        filteredLearnerActionList.addAll(learnerActionMissingTerms);

        getInstance().learnerActionList.setItems(filteredLearnerActionList);
    }

    /**
     * Gets the singleton instance of the {@link LearnerActionSelectionPanel}.
     * 
     * @return The {@link LearnerActionSelectionPanel} instance. Can't be null.
     */
    private static LearnerActionSelectionPanel getInstance() {
        if (instance == null) {
            instance = new LearnerActionSelectionPanel();
        }

        return instance;
    }

    /**
     * Hides this {@link LearnerActionSelectionPanel} for the
     * {@link #currentPicker} which is using it.
     */
    public static void hideSelector() {
        if (currentPicker != null) {
            currentPicker.getSelectorPanel().hide();
        }
    }

    /**
     * Returns the list editor within the {@link LearnerActionSelectionPanel} as
     * a {@link ScenarioValidationComposite}.
     * 
     * @return The {@link ScenarioValidationComposite} which is the list editor for each
     *         {@link LearnerAction} within the {@link Scenario}.
     */
    public static ScenarioValidationComposite getListValidation() {
        return getInstance().learnerActionList;
    }

    /**
     * Does the event target this popup?
     *
     * @param event the native event
     * @return true if the event targets the popup
     */
    private boolean eventTargetsPopup(NativeEvent event) {
        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            return getElement().isOrHasChild(Element.as(target));
        }

        return false;
    }

    /**
     * Does the event target one of the partner elements?
     *
     * @param event the native event
     * @return true if the event targets a partner
     */
    private boolean eventTargetsPartner(NativeEvent event) {

        if (currentPicker == null) {
            return false;
        }

        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            if (currentPicker.getTextBox().getElement().isOrHasChild(Element.as(target))) {
                return true;
            }
        }
        return false;
    }
}
