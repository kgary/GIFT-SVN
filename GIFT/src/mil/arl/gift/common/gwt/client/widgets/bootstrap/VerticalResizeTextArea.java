/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * A specialized version of the Summernote editor. This class is used when you want a text area that
 * allows the user to expand/shrink vertically but not horizontally. The text format features (bold,
 * highlight, indent, etc...) can be optionally hidden.
 * <br><br>
 * This is used instead of the gwt TextArea because the resizing feature does not work in IE 11.
 * 
 * @author sharrison
 */
public class VerticalResizeTextArea extends FocusPanel implements HasValue<String> {

    private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

    interface WidgetUiBinder extends UiBinder<Widget, VerticalResizeTextArea> {
    }

    @UiField
    protected Container summerNoteContainer;

    @UiField
    protected FlowPanel editorPanel;

    /** The editor */
    protected Summernote textEditor = new Summernote();

    /** Whether or not the mouse is currently over this widget */
    private boolean isMouseOver = false;

    /**
     * Default toolbar for the summernote editor. This can be overridden by calling the setToolbar
     * method.
     */
    private Toolbar defaultToolbar = new Toolbar().addGroup(ToolbarButton.STYLE)
            .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC).addGroup(ToolbarButton.FONT_NAME)
            .addGroup(ToolbarButton.FONT_SIZE, ToolbarButton.COLOR).addGroup(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
            .addGroup(ToolbarButton.TABLE).addGroup(ToolbarButton.UNDO, ToolbarButton.REDO)
            .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);

    /**
     * Creates a new Summernote editor element
     */
    public VerticalResizeTextArea() {

        setWidget(uiBinder.createAndBindUi(this));

        // make the Summernote toolbar visible
        setToolbar(defaultToolbar);
        getEditor().setDialogsInBody(true);

        setTabIndex(-1); // allow this widget to gain focus

        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {

                if (!isMouseOver) {
                    isMouseOver = true;
                }
            }
        });

        addMouseOutHandler(new MouseOutHandler() {

            @Override
            public void onMouseOut(MouseOutEvent event) {

                if (isMouseOver) {
                    isMouseOver = false;
                }
            }
        });

        getEditor().addSummernoteBlurHandler(new SummernoteBlurHandler() {

            @Override
            public void onSummernoteBlur(SummernoteBlurEvent event) {

                if (isMouseOver) {

                    // The Summernote editor will lose focus when its buttons are pressed, so we
                    // need to detect when that happens and reset the focus back to the editor when
                    // it does.
                    focusSummernote(getEditor().getElement());
                } else {
                    
                    // if the user clicks outside this widget
                    ValueChangeEvent.fire(VerticalResizeTextArea.this, getEditor().getCode());
                }
            }

        });

        // disable the drag and drop feature
        getEditor().setDisableDragAndDrop(true);
        
        editorPanel.add(getEditor());
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public String getValue() {
        return getEditor().getCode();
    }

    /**
     * Gets the editor's text value length with the tags removed.
     * 
     * @return the length of the input text value. Will return 0 if the value is null.
     */
    public int getValueLength() {
        if (getValue() == null) {
            return 0;
        }

        // remove the tags and replace the space strings with space characters.
        return getValue().replaceAll("<p>", "").replaceAll("<br>", "").replaceAll("</p>", "").replaceAll("&nbsp;", " ").trim().length();
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {

        String oldValue = getEditor().getCode();

        getEditor().setCode(value);

        if (fireEvents) {
            ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
        }
    }

    /**
     * Sets the placeholder text to use when no text has been entered
     * 
     * @param placeholder the placeholder text
     */
    public void setPlaceholder(String placeholder) {
        getEditor().setPlaceholder(placeholder);
    }

    /**
     * Enables or disables the Summernote editor.
     * 
     * @param enable true to enable editing; false to disable.
     */
    public void setEnabled(boolean enable) {
        getEditor().setEnabled(enable);
    }

    /**
     * Gets the Summernote editor.
     * 
     * @return the editor
     */
    public Summernote getEditor() {
        return textEditor;
    }

    /**
     * Sets the toolbar buttons for the Summernote editor. This should be called right after
     * creation of the editor.
     * 
     * @param toolbar the new toolbar value for the editor. If null, the toolbar will be hidden.
     */
    public void setToolbar(Toolbar toolbar) {
        if (toolbar == null) {
            setShowToolbar(false);
        } else {
            getEditor().setToolbar(toolbar);
            setShowToolbar(true);
        }
    }
    
    /**
     * Displays or Hides the toolbar for the Summernote editor.
     * 
     * @param show true to show the toolbar; false to have it hidden.
     */
    public void setShowToolbar(boolean show) {
        getEditor().setShowToolbar(show);
    }

    /**
     * Focuses on the Summernote editor with the given element. <br/>
     * <br/>
     * Note: For whatever reason,
     * {@link org.gwtbootstrap3.extras.summernote.client.ui.base.SummernoteBase#setHasFocus(boolean)
     * Summernote's setHasFocus(boolean) method} doesn't seem to work properly, so this method
     * exists as a workaround.
     * 
     * @param e the element for a Summernote editor
     */
    /*
     * Nick - Specifically, I think SummernoteBase.setHasFocus(boolean) is setting the focus on the
     * wrong element. Going into it's Java code reveals that it is using it's getElement() method to
     * get the element it needs to focus on. For most widgets, this would be correct, but for the
     * Summernote widget, getElement() actually returns the topmost div for the Summernote editor,
     * NOT the actual editable area. In order to set focus on the editable area, we need to use the
     * native Summernote JavaScript API in order to invoke the 'focus' method on its editor module.
     */
    private native void focusSummernote(Element e)/*-{
        $wnd.jQuery(e).summernote('focus');
    }-*/;
}
