/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.coursecollection;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.AlertType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;
import mil.arl.gift.common.gwt.client.widgets.AbstractWidget;
import mil.arl.gift.common.gwt.shared.ServerProperties;
import mil.arl.gift.tools.dashboard.client.Dashboard;

/**
 * The page used to display the courses within a {@link CourseCollection}.
 *
 * @author tflowers
 *
 */
public class CourseCollectionPage extends AbstractWidget {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(CourseCollectionPage.class.getName());

    /** The binder that combines the java class with the ui.xml */
    private static CourseCollectionUiBinder uiBinder = GWT.create(CourseCollectionUiBinder.class);

    /** The binder that combines this java class with the ui.xml */
    interface CourseCollectionUiBinder extends UiBinder<Widget, CourseCollectionPage> {
    }

    /** The placeholder text for if the collection has no courses */
    private static final String NO_COURSES_PLACEHOLDER = "There are no available courses at the moment.";

    /** The root container */
    @UiField
    protected HTMLPanel rootPanel;

    /** The widget that displays the name of the {@link #collection}. */
    @UiField
    protected Heading collectionNameHeader;

    /** The scroll panel containing the {@link #experimentPanel} */
    @UiField
    protected ScrollPanel scrollPanel;

    /** The panel to which each experiment/course will be added. */
    @UiField
    protected FlowPanel experimentPanel;

    /**
     * The icon to show if the experiment courses go past the scope of the
     * window
     */
    @UiField
    protected Icon overflowIcon;

    /** The course collection */
    private final CourseCollection collection;

    /**
     * Constructs a new, empty {@link CourseCollectionPage}.
     *
     * @param collection The {@link CourseCollection} to display
     *        on this page. Can't be null.
     */
    public CourseCollectionPage(CourseCollection collection) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CourseCollectionPage()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        
        // set the background
        String backgroundImage = Dashboard.getInstance().getServerProperties().getPropertyValue(ServerProperties.BACKGROUND_IMAGE);
        rootPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundImage+"')");

        this.collection = collection;
        if (collection == null) {
            throw new IllegalArgumentException("The parameter 'experimentCollection' cannot be null.");
        }

        collectionNameHeader.setText(collection.getName());
        setCourses(collection.getCourses());

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                evaluateScroll();
            }
        });

        scrollPanel.addScrollHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                evaluateScroll();
            }
        });

        /* Check if the scroll bar is visible and if the overflow icon should be
         * shown. In a schedule deferred block because the scroll bar isn't
         * available right away. */
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                evaluateScroll();
            }
        });
    }

    /**
     * Check the scroll position. Show an icon if components are pushed off the
     * page and require scrolling to see them.
     */
    private void evaluateScroll() {
        /* The maximum value of the scroll bar (the bottom of the bar) */
        final int maxVerticalScrollPosition = scrollPanel.getMaximumVerticalScrollPosition();
        /* The current position of the scroll bar */
        final int verticalScrollPosition = scrollPanel.getVerticalScrollPosition();

        /* Determine if the scroll bar is currently within the threshold */
        final boolean showOverflowIcon = verticalScrollPosition < maxVerticalScrollPosition;

        /* Show the overflow icon if below the threshold; hide it otherwise */
        overflowIcon.setVisible(showOverflowIcon);
    }

    /**
     * Setter for the courses/experiments that should be shown on this page.
     *
     * @param courses The {@link Collection} of {@link DataCollectionItem} to
     *        display on the page. Can't be null.
     */
    private void setCourses(Collection<DataCollectionItem> courses) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setCourses(" + courses + ")");
        }

        if (courses == null) {
            throw new IllegalArgumentException("The parameter 'courses' cannot be null.");
        }

        experimentPanel.clear();

        /* Remove the hash from href to get the desired return url */
        boolean hasCourses = false;
        for (DataCollectionItem course : courses) {
            if (course.isDataSetType(DataSetType.EXPERIMENT) && course.getStatus() == ExperimentStatus.RUNNING) {
                experimentPanel.add(new CourseCollectionCourseTile(course, collection));
                hasCourses = true;
            }
        }

        /* If no courses were started, add a placeholder */
        if (!hasCourses) {
            experimentPanel.add(buildPlaceholder());
        }
    }

    /**
     * Builds a place holder that informs the user that the collection is empty.
     *
     * @return A {@link Widget} that is to be put in place of where the
     *         experiments would be placed {@link #experimentPanel}.
     */
    private Widget buildPlaceholder() {
        Alert alert = new Alert(NO_COURSES_PLACEHOLDER, AlertType.INFO);
        alert.getElement().getStyle().setTextAlign(TextAlign.CENTER);

        return alert;
    }
}
