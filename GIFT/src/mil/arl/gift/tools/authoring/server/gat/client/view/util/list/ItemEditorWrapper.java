/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A class that wraps the custom {@link ItemEditor} within an
 * {@link ItemListEditor} with the standard controls used for saving changes
 * made to a
 * 
 * @author tflowers
 *
 * @param <T> The type of element that is ultimately being edited.
 */
public class ItemEditorWrapper<T> extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ItemEditorWrapper.class.getName());

    /** The binder that combines this java class with the ui.xml */
    private static ItemEditorWrapperUiBinder uiBinder = GWT.create(ItemEditorWrapperUiBinder.class);

    /** Defines the binder that combines the java class with the ui.xml */
    interface ItemEditorWrapperUiBinder extends UiBinder<Widget, ItemEditorWrapper<?>> {
    }

    /** The button that cancels any changes made to item */
    @UiField
    protected Button cancelButton;

    /** The panel that will contain the element for the item */
    @UiField
    protected SimplePanel editorPanel;

    /** The button that saves any chnages made to the item */
    @UiField
    protected Button saveButton;

    /** The editor that is specific to the element type of T */
    private ItemEditor<T> editor;

    /**
     * Constructs a new {@link ItemEditorWrapper} that wraps a provided {@link ItemEditor}.
     * 
     * @param editor The editor that should be wrapped by this {@link ItemEditorWrapper}. If null, a
     *        wrapper with only 'save' and 'cancel' buttons is created.
     * @param parentItemListEditor The parent item list editor
     */
    public ItemEditorWrapper(ItemEditor<T> editor, ItemListEditor<T> parentItemListEditor) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ItemEditorWrapper()");
        }

        initWidget(uiBinder.createAndBindUi(this));
        this.editor = editor;
        editorPanel.setWidget(editor);
        if (editor != null) {
            editor.setParentItemListEditor(parentItemListEditor);
            editor.setParentItemEditorWrapper(this);
        }
    }

    /**
     * Adds a handler that is invoked when the user clicks the
     * {@link #cancelButton}.
     * 
     * @param mouseDownHandler The {@link ClickHandler} to invoke when the cancel
     *        button is clicked.
     * @return {@link HandlerRegistration} used to remove this
     *         {@link ClickHandler}.
     */
    public HandlerRegistration addCancelHandler(MouseDownHandler mouseDownHandler) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addCancelHandler(" + mouseDownHandler + ")");
        }

        return cancelButton.addMouseDownHandler(mouseDownHandler);
    }

    /**
     * Adds a handler that is invoked when the user clicks the
     * {@link #saveButton}.
     * 
     * @param mouseDownHandler The {@link ClickHandler} to invoke when the save
     *        button is clicked.
     * @return {@link HandlerRegistration} used to remove this
     *         {@link ClickHandler}.
     */
    public HandlerRegistration addSaveHandler(MouseDownHandler mouseDownHandler) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addSaveHandler(" + mouseDownHandler + ")");
        }

        return saveButton.addMouseDownHandler(mouseDownHandler);
    }

    /**
     * Gets the {@link ItemEditor} that is being wrapped.
     * 
     * @return The {@link ItemEditor} being wrapped. Can be null.
     */
    public ItemEditor<T> getEditor() {
        return editor;
    }

    /**
     * Retrieves the save button.
     * 
     * @return the save button.
     */
    public Button getSaveButton() {
        return saveButton;
    }

    /**
     * Retrieves the cancel button.
     * 
     * @return the cancel button.
     */
    public Button getCancelButton() {
        return cancelButton;
    }
}