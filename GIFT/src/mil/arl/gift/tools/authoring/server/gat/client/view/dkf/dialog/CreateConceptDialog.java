/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.dialog;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.extras.select.client.ui.OptGroup;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Concept;
import generated.dkf.Conditions;
import generated.dkf.Task;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * A dialog used to create a concept. A parent task/concept will be selected that will contain this concept.
 * 
 * @author sharrison
 */
public class CreateConceptDialog extends Modal implements HasValue<CreateScenarioObjectEvent> {
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static CreateConceptDialogUiBinder uiBinder = GWT.create(CreateConceptDialogUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface CreateConceptDialogUiBinder extends UiBinder<Widget, CreateConceptDialog> {
    }

    /** The gwt bootstrap form that contains the widgets with which to perform validation. */
    @UiField
    protected Form form;
    
    /**
     * The select widget for a list of tasks and concepts. The user will select the task/concept that will reference the
     * concept being created.
     */
    @UiField
    protected Select taskConceptSelect;

    /** The confirmation button that will trigger the dialog value change handler */
    private Button confirmButton = new Button();

    /** The cancel button that will exit the panel without any changes being committed */
    private Button cancelButton = new Button("Cancel");

    /** The dialog title */
    protected HTML caption = new HTML();

    /** The body of the modal that will contain the task/concept picker and concept name text box */
    protected ModalBody body = new ModalBody();

    /** The footer of the modal that will contain the confirm and cancel buttons */
    protected ModalFooter footer = new ModalFooter();

    /** The generated default name to set the concept to */
    private String name;
    
    /**
     * Comparator used to sort {@link Task} objects by their name. This is useful for the
     * task/concept select widget.
     */
    private static final Comparator<Task> TASK_COMPARATOR = new Comparator<Task>() {
        @Override
        public int compare(Task o1, Task o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    /**
     * Comparator used to sort {@link Concept} objects by their name. This is useful for the
     * task/concept select widget.
     */
    private static final Comparator<Concept> CONCEPT_COMPARATOR = new Comparator<Concept>() {
        @Override
        public int compare(Concept o1, Concept o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    /**
     * Creates a new dialog. <br\> The confirm button uses the
     * {@link org.gwtbootstrap3.client.ui.constants.ButtonType.PRIMARY} css. <br\> The cancel button
     * uses the {@link org.gwtbootstrap3.client.ui.constants.ButtonType.DANGER} css.
     */
    public CreateConceptDialog() {

        super();

        /* Nick: This class used to extend ModalDialogBox instead of TopLevelModal, but we ran into
         * an issue where a CourseObjectModal was stealing focus from this dialog's textbox, which
         * would make the textbox unresponsive in Chrome and Firefox. This problem is the product of
         * some strange functionality in Bootstrap in which modals will attempt to gain focus
         * whenever they are shown, which can potentially rob focus away from any elements that
         * currently have it. To fix this, I basically converted this class to a modal so that it
         * can steal focus back from CourseObjectModal, preventing the issue. I don't like doing
         * this, but after trying many different solutions, this was the only solution that wouldn't
         * affect most of the GAT, since other solutions involve overwriting the behavior of
         * Bootstrap modals, which would affect many areas in the GAT. */

        setDataBackdrop(ModalBackdrop.STATIC);
        setDataKeyboard(false);
        setClosable(false);
        setFade(true);

        setWidth("600px");

        ModalHeader header = new ModalHeader();
        header.setClosable(false);

        Heading heading = new Heading(HeadingSize.H3);
        heading.add(caption);

        header.add(heading);
        add(header);

        caption.setText("Create Concept");

        body.add(uiBinder.createAndBindUi(this));
        add(body);

        cancelButton.setType(ButtonType.DANGER);
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        confirmButton.setType(ButtonType.PRIMARY);
        confirmButton.setText("Create Concept");
        confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Concept concept = ScenarioClientUtility.generateNewConcept();
                CreateConceptDialog.this.name = concept.getName();

                BigInteger selectedNodeId = new BigInteger(taskConceptSelect.getSelectedItem().getValue());
                Serializable taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(selectedNodeId);

                CreateScenarioObjectEvent createEvent = new CreateScenarioObjectEvent(concept, taskOrConcept);
                ValueChangeEvent.fire(CreateConceptDialog.this, createEvent);
            }
        });

        FlowPanel footerPanel = new FlowPanel();
        footerPanel.add(confirmButton);
        footerPanel.add(cancelButton);

        footer.add(footerPanel);
        add(footer);

        addDomHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                int key = event.getNativeKeyCode();

                if (key == KeyCodes.KEY_ENTER) {
                    confirmButton.click();
                }
            }

        }, KeyUpEvent.getType());

        resetDialog();
        
    }

    /**
     * Clears the dialog fields and builds the task/concept dropdown list.
     */
    private void resetDialog() {
        taskConceptSelect.clear();

        // Get full list of performance node tasks and concepts.
        List<Task> tasks = new ArrayList<>(ScenarioClientUtility.getUnmodifiableTaskList());
        List<Concept> concepts = new ArrayList<>(ScenarioClientUtility.getUnmodifiableConceptList());
        
        if (!tasks.isEmpty()) {
            Collections.sort(tasks, TASK_COMPARATOR);
            OptGroup taskGroup = new OptGroup();
            taskGroup.setLabel("Tasks");
            for (Task task : tasks) {
                Option option = new Option();
                option.setText(task.getName());
                option.setValue(task.getNodeId().toString());
                taskGroup.add(option);
            }
            taskConceptSelect.add(taskGroup);
        }
        if (!concepts.isEmpty()) {
            Collections.sort(concepts, CONCEPT_COMPARATOR);
            OptGroup conceptGroup = new OptGroup();
            conceptGroup.setLabel("Concepts");
            for (Concept concept : concepts) {
                // can't use concepts that already have conditions
                if (concept.getConditionsOrConcepts() != null && concept.getConditionsOrConcepts() instanceof Conditions) {
                    continue;
                }
                
                Option option = new Option();
                option.setText(concept.getName());
                option.setValue(concept.getNodeId().toString());
                conceptGroup.add(option);
            }
            if (conceptGroup.getWidgetCount() != 0) {
                taskConceptSelect.add(conceptGroup);
            }
        }

        taskConceptSelect.refresh();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CreateScenarioObjectEvent> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public CreateScenarioObjectEvent getValue() {
        if (taskConceptSelect.getSelectedItem() == null) {
            return null;
        }

        Concept concept = new Concept();
        concept.setName(this.name);
        concept.setNodeId(ScenarioClientUtility.generateTaskOrConceptId());

        Serializable taskOrConcept;
        try {
            BigInteger selectedNodeId = new BigInteger(taskConceptSelect.getSelectedItem().getValue());
            taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(selectedNodeId);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            return null;
        }

        return new CreateScenarioObjectEvent(concept, taskOrConcept);
    }

    @Override
    public void setValue(CreateScenarioObjectEvent createEvent) {
        setValue(createEvent, false);
    }

    @Override
    public void setValue(CreateScenarioObjectEvent createEvent, boolean fireEvents) {
        resetDialog();

        if (createEvent != null) {
            
            Serializable taskOrConcept = createEvent.getParent();
            if (taskOrConcept instanceof Task) {
                Task task = (Task) taskOrConcept;
                taskConceptSelect.setValue(task.getNodeId().toString());
            } else if (taskOrConcept instanceof Concept) {
                Concept concept = (Concept) taskOrConcept;
                taskConceptSelect.setValue(concept.getNodeId().toString());
            }
        }

        if (fireEvents) {
            ValueChangeEvent.fire(this, createEvent);
        }
    }

    @Override
    public void show() {
        super.show();
    }

    /** Displays this dialog */
    public void center() {
        show();
    }
}