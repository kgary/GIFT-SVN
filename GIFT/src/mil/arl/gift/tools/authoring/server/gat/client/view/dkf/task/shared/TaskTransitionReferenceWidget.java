/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright ments as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared;

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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Task;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Strategy;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.HelpLink;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * The widget that displays a list of tasks that reference a specific
 * instructional strategy within the DKF.
 * 
 * @author gbegawala
 *
 */
public class TaskTransitionReferenceWidget extends Composite {
	/** The logger for the class */
	private static final Logger logger = Logger.getLogger(TaskTransitionReferenceWidget.class.getName());

	/** The UiBinder that combines the ui.xml with this java class */
	private static TaskTransitionReferenceWidgetUiBinder uiBinder = GWT
			.create(TaskTransitionReferenceWidgetUiBinder.class);

	/** Defines the UiBinder that combines the ui.xml with a java class */
	interface TaskTransitionReferenceWidgetUiBinder extends UiBinder<Widget, TaskTransitionReferenceWidget> {
	}

	/** The collapse that contains {@link #listEditor} */
	@UiField
	protected Collapse collapse;

	/** The header/title for the control */
	@UiField
	protected PanelHeader panelHeader;

	/** The link to display the help text */
	@UiField
	protected HelpLink helpLink;

	/** The text to be displayed in the help dialog */
	@UiField
	protected HTML helpText;

	/**
	 * The control that displays the each of the elements contained within this
	 * control
	 */
	@UiField(provided = true)
	protected GenericListEditor<Task> listEditor = new GenericListEditor<Task>(new Stringifier<Task>() {
		@Override
		public String stringify(Task task) {
			return task.getName();
		}
	});

	/**
	 * Action to jump to the selected tasks page. This will be visible for each item
	 * in the {@link GenericListEditor} table.
	 */
	protected ItemAction<Task> jumpToAction = new ItemAction<Task>() {

		@Override
		public boolean isEnabled(Task item) {
			return true;
		}

		@Override
		public String getTooltip(Task item) {
			return "Click to navigate to this task";
		}

		@Override
		public IconType getIconType(Task item) {
			return IconType.EXTERNAL_LINK;
		}

		@Override
		public void execute(Task item) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("jumpToAction.execute(" + item + ")");
			}

			ScenarioEventUtility.fireJumpToEvent(item);
		}
	};

	/**
	 * Default constructor for the {@link TaskTransitionReferenceWidget}
	 */
	public TaskTransitionReferenceWidget() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(".ctor()");
		}

		initWidget(uiBinder.createAndBindUi(this));

		// Populate the list widget
		listEditor.setRowAction(jumpToAction);

		// Populate the collapsable sections with a randomly generate id
		String id = Document.get().createUniqueId();
		panelHeader.setDataTarget("#" + id);
		collapse.setId(id);

		helpLink.setVisible(false);
	}

	/**
	 * Sets the label/description for the table editor.
	 * 
	 * @param html The HTML as a {@link String} to display as the description. The
	 *             value can be null.
	 */
	public void setTableLabel(String html) {
		listEditor.setTableLabel(html);
	}

	/**
	 * Adds the provided {@link Task} to the UI. A call to refresh is not necessary.
	 * 
	 * @param task The task to add to the list. Can't be null.
	 */
	public void add(Task task) {
		if (task == null) {
			throw new IllegalArgumentException("The parameter 'task' cannot be null.");
		}

		listEditor.addItem(task);
	}

	/**
	 * Removes the provided {@link Task} from the UI. A call to refresh is not
	 * necessary.
	 * 
	 * @param task The task to remove from the list. Can't be null.
	 */
	public void remove(Task task) {
		if (task == null) {
			throw new IllegalArgumentException("The parameter 'task' cannot be null.");
		}

		listEditor.removeItem(task);
	}

	/**
	 * Updates the UI to reflect any mutations to the underlying {@link Task}
	 * contained within this widget
	 */
	public void refresh() {
		listEditor.refresh();
	}

	/**
	 * Shows each {@link Task} that references the given {@link Strategy}
	 * 
	 * @param strategy The {@link Strategy} for which to find references.
	 */
	public void showTasks(Strategy strategy) {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("showTransitions(" + strategy + ")");
		}

		HashSet<Task> tasks = ScenarioClientUtility.getTasksThatReferenceStrategy(strategy.getName());
		listEditor.replaceItems(tasks);
	}

}
