/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.coursecollection;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.io.ExperimentUrlManager;

/**
 * A widget that represents a course/experiment on the
 * {@link CourseCollectionPage}.
 *
 * @author tflowers
 *
 */
public class CourseCollectionCourseTile extends Composite {

    /** The binder that combines the java class with the ui.xml */
    private static CourseCollectionCourseTileUiBinder uiBinder = GWT.create(CourseCollectionCourseTileUiBinder.class);

    /** The interface that combines this java class with the ui.xml */
    interface CourseCollectionCourseTileUiBinder extends UiBinder<Widget, CourseCollectionCourseTile> {
    }

    /** The button the user clicks to take the experiment. */
    @UiField
    protected Button experimentButton;

    /** The experiment to execute */
    private final DataCollectionItem experiment;
    
    /** The parent course collection */
    private final CourseCollection parentCollection;

    /**
     * Creates a {@link CourseCollectionCourseTile} that represents a
     * {@link DataCollectionItem}.
     *
     * @param experiment The {@link DataCollectionItem} this object represents.
     *        Can't be null.
     * @param parentCollection The parent {@link CourseCollection}. Can't be null.
     */
    public CourseCollectionCourseTile(DataCollectionItem experiment, CourseCollection parentCollection) {
        if (experiment == null) {
            throw new IllegalArgumentException("The parameter 'experiment' cannot be null.");
        } else if (parentCollection == null) {
            throw new IllegalArgumentException("The parameter 'parentCollection' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.experiment = experiment;
        this.parentCollection = parentCollection;
        experimentButton.setText(experiment.getName());
    }

    /**
     * Navigates to the experiment when the {@link #experimentButton} is
     * clicked.
     *
     * @param event The event describing the click. Can't be null.
     */
    @UiHandler("experimentButton")
    protected void onButtonClicked(ClickEvent event) {
        StringBuilder sb = new StringBuilder(experiment.getUrl());

        /* Add return url */
        sb.append("&").append(ExperimentUrlManager.RETURN_URL_PARAMETER).append("=")
                .append(URL.encodeQueryString(parentCollection.getUrl()));

        /* Assign url for clicked experiment */
        Window.Location.assign(sb.toString());
    }
}
