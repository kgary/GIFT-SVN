/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.gwtbootstrap3.client.ui.CheckBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A wrapper widget in which other widgets can be placed in order to provide them with
 * settings to control how properties from a domain session should be displayed
 * 
 * @author nroberts
 */
public abstract class DisplaySessionPropertiesWrapper<T extends Serializable> extends Composite implements IsWidget, HasWidgets, HasValue<T>{

    private static DisplaySessionPropertiesUiBinder uiBinder = GWT.create(DisplaySessionPropertiesUiBinder.class);

    interface DisplaySessionPropertiesUiBinder extends UiBinder<Widget, DisplaySessionPropertiesWrapper<?>> {
    }
    
    /** The checkbox used to decide whether the current domain session information should be passed to an external server */
    @UiField
    protected CheckBox requestStateBox;
    
    /** The panel containing the widget in the recording booth */
    @UiField
    protected SimplePanel wrapper;
    
    /** The current properties for displaying domain session information */
    protected T value;
    
    /**
     * Creates a new wrapper that can provide another widget with settings to control how domain session properties should be displayed
     */
    protected DisplaySessionPropertiesWrapper() {
        initWidget(uiBinder.createAndBindUi(this));
        
        requestStateBox.setEnabled(!ScenarioClientUtility.isReadOnly());
        requestStateBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                onRequestStateChaged(event.getValue());
            }
        });
    }
    
    /**
     * Handles when the author changes the check box that determine whether a resource should be
     * requested from an external assessment server
     * 
     * @param state whether the resource should be requested from an external assessment server. Can be null.
     */
    protected abstract void onRequestStateChaged(Boolean state);

    /**
     * Creates a new wrapper that provides the given widget with settings to control how domain session properties should be displayed
     * 
     * @param widget the widget to wrap. Can be null.
     */
    public DisplaySessionPropertiesWrapper(Widget widget) {
        this();
        setWrappedWidget(widget);
    }
    
    /** 
     * Removes the widget placed inside this wrapper
     */
    @Override
    public void clear() {
        wrapper.clear();
    }
    
    /**
     * Gets the widget inside this wrapper. Can return null, if no widget is inside this wrapper.
     */
    @Override
    public Widget getWidget() {
        return wrapper.getWidget();
    }
    
    /** {@inheritDoc} */
    @Override
    public Iterator<Widget> iterator() {
        // Simple iterator for the widget
        return new Iterator<Widget>() {

            boolean hasElement = getWidget() != null;

            Widget returned = null;

            /** {@inheritDoc} */
            @Override
            public boolean hasNext() {
                return hasElement;
            }

            /** {@inheritDoc} */
            @Override
            public Widget next() {
                if (!hasElement || (getWidget() == null)) {
                    throw new NoSuchElementException();
                }
                hasElement = false;
                return (returned = getWidget());
            }

            /** {@inheritDoc} */
            @Override
            public void remove() {
                if (returned != null) {
                    DisplaySessionPropertiesWrapper.this.remove(returned);
                }
            }
        };
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean remove(final Widget w) {
        // Validate.
        if (getWidget() != w) {
            return false;
        }
        // Logical detach.
        clear();
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Widget child) {
        if (getWidget() != null) {
            throw new IllegalStateException("Can only contain one child widget");
        }
        setWrappedWidget(child);
    }
    
    /**
     * Sets the widget that should be placed in this wrapper.
     * 
     * @param w the widget to place in the wrapper. Can be null.
     */
    public void setWidget(final IsWidget w) {
        setWrappedWidget(w.asWidget());
    }
    
    /**
     * Sets the widget that should be placed in this wrapper.
     * 
     * @param w the widget to place in the wrapper. Can be null.
     */
    /*
     * Note: Can't name this method setWidget since Composite has it flagged as deprecated, which causes build
     * warnings when compiled. This also annoyingly prevents us from using the AcceptsOneWidget interface, so callers
     * simply have to know that this widget can only wrap one other widget.
     */
    public void setWrappedWidget(final Widget w) {
        // Validate
        if (w == getWidget()) {
            return;
        }

        // Detach new child
        if (w != null) {
            w.removeFromParent();
        }

        // Remove old child
        if (getWidget() != null) {
            remove(getWidget());
        }

        // Logical attach, but don't physical attach; done by jquery.
        wrapper.setWidget(w);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<T> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setValue(T value) {
        setValue(value, false);
    }

    @Override
    public void setValue(T value, boolean fireEvents) {
        this.value = value;
        
        if(fireEvents){
            ValueChangeEvent.fire(this, value);
        }
        
        refresh();
    }

    /**
     * Refreshes the UI state to reflect the domain session property display settings that have been set
     */
    protected abstract void refresh();
}
