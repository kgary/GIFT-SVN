/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import com.google.gwt.user.client.Command;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The inline editor to be displayed under the 'edited' row within the {@link ItemListEditor}.
 * 
 * @author sharrison
 *
 * @param <T> The type of the list elements
 */
public abstract class ItemEditor<T> extends ScenarioValidationComposite {
    /** The grandparent {@link ItemListEditor} of this editor */
    private ItemListEditor<T> parentItemListEditor;

    /** The parent {@link ItemEditorWrapper} of this editor */
    private ItemEditorWrapper<T> parentItemEditorWrapper;

    /**
     * Populates the {@link ItemEditor inline editor} with the data contained within the provided
     * object.
     * 
     * @param obj the object of class T that contains the data necessary to populate the inline
     *        editor.
     */
    protected abstract void populateEditor(T obj);

    /**
     * Saves the edited fields from the {@link ItemEditor inline editor} to the provided object.
     * None of the changes in the {@link ItemEditor inline editor} will persist until this method is
     * called.
     * 
     * @param obj the object to persist the changes from the {@link ItemEditor inline editor} to.
     *        Can't be null.
     */
    protected abstract void applyEdits(T obj);

    /**
     * Here to be optionally overridden if an apply edits has an rpc call. If this is overridden,
     * {@link #applyEdits(Object)} does not need to be populated.<br/>
     * <br/>
     * Saves the edited fields from the {@link ItemEditor inline editor} to the provided object.
     * None of the changes in the {@link ItemEditor inline editor} will persist until this method is
     * called. Once applyEdits is complete, execute the command to finish the
     * {@link #parentItemListEditor} save action.
     * 
     * @param obj the object to persist the changes from the {@link ItemEditor inline editor} to.
     *        Can't be null.
     * @param command the command to indicate that it is ok to proceed with the rest of the save
     *        operation.
     */
    protected void applyEdits(T obj, Command command) {
        applyEdits(obj);
        command.execute();
    }

    /**
     * Validates the list item object. This is used to determine if any of the
     * rows in the editor are invalid.
     * 
     * @param obj the list item object to validate.
     * @return true if the list item object is valid; false if it is invalid.
     */
    protected abstract boolean validate(T obj);

    /**
     * Perform additional actions on cancel. This is called before the cancel operation is
     * performed.
     */
    protected void onCancel() {
        /* Do nothing by default. This is here in case a child wants to override it to perform
         * additional actions on cancel. */
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    protected abstract void setReadonly(boolean isReadonly);

    /**
     * Sets the save button visibility using the provided boolean. Nothing will change if the
     * {@link #parentItemEditorWrapper} is null.
     * 
     * @param show true to show the save button; false to hide it.
     */
    protected void setSaveButtonVisible(boolean show) {
        if (parentItemEditorWrapper != null) {
            parentItemEditorWrapper.getSaveButton().setVisible(show && !ScenarioClientUtility.isReadOnly());
        }
    }

    /**
     * Sets the cancel button visibility using the provided boolean. Nothing will change if the
     * {@link #parentItemEditorWrapper} is null.
     * 
     * @param show true to show the cancel button; false to hide it.
     */
    protected void setCancelButtonVisible(boolean show) {
        if (parentItemEditorWrapper != null) {
            parentItemEditorWrapper.getCancelButton().setVisible(show);
        }
    }

    /**
     * Sets the text for the save button. Nothing will change if the
     * {@link #parentItemEditorWrapper} is null or if the provided text is blank.
     * 
     * @param saveText the text to use for the save button.
     */
    protected void setSaveButtonText(String saveText) {
        if (parentItemEditorWrapper != null && StringUtils.isNotBlank(saveText)) {
            parentItemEditorWrapper.getSaveButton().setText(saveText);
        }
    }

    /**
     * Sets the text for the cancel button. Nothing will change if the
     * {@link #parentItemEditorWrapper} is null or if the provided text is blank.
     * 
     * @param cancelText the text to use for the cancel button.
     */
    protected void setCancelButtonText(String cancelText) {
        if (parentItemEditorWrapper != null && StringUtils.isNotBlank(cancelText)) {
            parentItemEditorWrapper.getCancelButton().setText(cancelText);
        }
    }

    /**
     * Sets the list editor that is a parent to this item list editor.
     * 
     * @param parentItemListEditor the parent {@link ItemListEditor}.
     */
    void setParentItemListEditor(ItemListEditor<T> parentItemListEditor) {
        this.parentItemListEditor = parentItemListEditor;
    }

    /**
     * Gets the list editor that is a parent to this item list editor.
     * 
     * @return the parent {@link ItemListEditor}.
     */
    protected ItemListEditor<T> getParentItemListEditor() {
        return parentItemListEditor;
    }

    /**
     * Sets the list editor that is a parent to this item list editor.
     * 
     * @param parentItemEditorWrapper the parent {@link ItemListEditor}.
     */
    void setParentItemEditorWrapper(ItemEditorWrapper<T> parentItemEditorWrapper) {
        this.parentItemEditorWrapper = parentItemEditorWrapper;
    }

    /**
     * Optional validation that needs to call remote services.
     * 
     * @param validationCallback the callback to notify when the remote services have returned.
     */
    protected void performRemotePreSaveValidation(final ValidationCallback validationCallback) {
        // default to success
        validationCallback.validationPassed();
    }

    /**
     * The callback to indicate if the validaton has passed or failed.
     * 
     * @author sharrison
     */
    public interface ValidationCallback {
        /** Remote call has returned and validation has succeeded */
        void validationPassed();

        /** Remote call has returned and validation has failed */
        void validationFailed();
    }
}
