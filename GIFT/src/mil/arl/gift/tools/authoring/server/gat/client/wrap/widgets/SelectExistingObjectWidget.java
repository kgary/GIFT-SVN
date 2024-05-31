/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.wrap.widgets;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;

/**
 * An extension of {@link GIFTWrapHome} that removes the ability to create new training application
 * objects so that users can only select from existing objects. This widget also provides an
 * "Import" button next to each listed training application object so that users can import objects
 * into their courses when this widget is opened in the GAT.
 * 
 * @author nroberts
 */
public class SelectExistingObjectWidget extends GIFTWrapHome {

    /**
     * Creates a new widget for selecting an existing training application object.
     * 
     * @param trainingApplicationType the training application type to be viewed and selected in the
     *        {@link #itemListEditor}. If null, all types will be viewable.
     */
    public SelectExistingObjectWidget(TrainingApplicationEnum trainingApplicationType) {
        super(trainingApplicationType);

        tableLabel.setVisible(false);
    }

    @Override
    protected List<ItemField<CourseObjectWrapper>> buildEditorItemFields() {
        List<ItemField<CourseObjectWrapper>> itemFields = new ArrayList<>();
        itemFields.add(new ItemField<CourseObjectWrapper>() {
            @Override
            public Widget getViewWidget(final CourseObjectWrapper wrapper) {
                /* allow the author to select this training application to import it to the
                 * course */

                Button selectButton = new Button();
                selectButton.setText("Select");
                selectButton.getElement().setAttribute("style", "padding: 3px 10px; border-radius: 20px;");
                selectButton.addMouseDownHandler(new MouseDownHandler() {
                    @Override
                    public void onMouseDown(MouseDownEvent event) {
                        event.stopPropagation();
                        sendPathToParent(wrapper.getCourseObject().getLibraryPath().getRelativePathFromRoot(true));

                    }
                });
                selectButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        event.stopPropagation();
                    }
                });
                selectButton.setType(ButtonType.PRIMARY);

                SimplePanel panel = new SimplePanel();
                panel.setWidget(new Tooltip(selectButton, "Import this content into the GIFT Course"));

                return panel;
            }
        });

        itemFields.addAll(super.buildEditorItemFields());
        return itemFields;
    }

    /**
     * Sends the training application path to
     * {@link mil.arl.gift.tools.authoring.gat.client.presenter.course.CoursePresenter#importTrainingApp}
     * so that it can be imported into the current course.
     * 
     * @param path The path to the training application object
     */
    private native void sendPathToParent(String path) /*-{
		$wnd.parent.importTrainingApp(path);
    }-*/;

    @Override
    protected native void setGIFTWrapDialogCancelButtonVisibility(boolean visible) /*-{
		$wnd.parent.setGIFTWrapDialogCancelButtonVisibility(visible);
    }-*/;
}
