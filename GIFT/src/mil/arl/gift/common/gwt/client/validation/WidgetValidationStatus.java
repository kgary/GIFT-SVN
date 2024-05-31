/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A {@link ValidationStatus} for widgets and elements. The widget can be used for a 'scroll to its
 * location' action.
 * 
 * @author sharrison
 */
public class WidgetValidationStatus extends ValidationStatus {
    /** Interface to allow CSS file access */
    public interface Bundle extends ClientBundle {
        /** The instance of the bundle */
        public static final Bundle INSTANCE = GWT.create(Bundle.class);

        /**
         * The specific css resource
         * 
         * @return the css resource
         */
        @Source("ValidationStyles.css")
        public MyResources css();
    }

    /** Interface to allow CSS style name access */
    interface MyResources extends CssResource {
        /**
         * Styles the widget to indicate that it is invalid.
         * 
         * @return the style name an invalid widget
         */
        String invalidWidgetStyle();
    }

    /** The CSS resource */
    protected static final MyResources CSS = Bundle.INSTANCE.css();

    static {
        /* Make sure the css style names are accessible */
        Bundle.INSTANCE.css().ensureInjected();
    }

    /**
     * The widget that is the target of the validation status check. This will be the primary
     * target; if it doesn't exist, {@link #elements} will be used.
     */
    private Widget widget;

    /**
     * The elements that are the targets of the validation status check. This will be used if
     * {@link #widget} is null. A map is required because the element can be modified externally so
     * we need a key to identify the right one.
     */
    private Map<Object, Element> elementMap = new HashMap<>();

    /**
     * Constructor. Use this constructor if there is no specific widget that is
     * responsible for the validation. Use
     * {@link #addElement(Element, Object, Widget)} to add the responsible
     * element later.
     * 
     * @param errorMsg The error message to display to the user if the
     *        validation check fails. This should try to be a unique message for
     *        this validation test.
     */
    public WidgetValidationStatus(String errorMsg) {
        super(errorMsg);

        addValidationStatusChangedCallback(new ValidationStatusChangedCallback() {
            @Override
            public void changedValidity(boolean isValid, boolean fireEvents) {
                for (Element element : getElements()) {
                    if (isValid) {
                        removeStyle(element);
                    } else {
                        addStyle(element);
                    }
                }
            }
        });
    }

    /**
     * Constructor.<br>
     * <br>
     * If using a UiField as the widget, this MUST be initialized AFTER initWidget() is called
     * otherwise the widget will be null (this is excluding UiFields that have been marked as
     * 'provided=true').
     * 
     * @param widget The widget that is the target of the validation status check. Can't be null.
     * @param errorMsg The error message to display to the user if the widget is invalid. This
     *        should try to be a unique message for this specific widget.
     */
    public WidgetValidationStatus(Widget widget, String errorMsg) {
        this(errorMsg);
        if (widget == null) {
            throw new IllegalArgumentException(
                    "The parameter 'widget' cannot be null. This may be null because the widget hasn't been initialized yet via initWidget().");
        }

        this.widget = widget;
        addElement(widget.getElement(), widget, widget);
    }

    /**
     * The widget that is the target of the validation status check.
     * 
     * @return the widget. Can be null.
     */
    public Widget getWidget() {
        return widget;
    }

    /**
     * The elements that are the target of the validation status check.
     * 
     * @return the element. Can't be null. Can be empty.
     */
    public Collection<Element> getElements() {
        return elementMap.values();
    }

    /**
     * The elements and their identifiers that are the target of the validation status check.
     * 
     * @return the element. Can't be null. Can be empty.
     */
    public Map<Object, Element> getElementIdentifierMap() {
        return elementMap;
    }

    /**
     * Populates the validation status with the element that is the target of the validation status
     * check.
     * 
     * @param element The element that is the target of the validation status check. Can't be null.
     * @param elementIdentifier The object that can be used to identify the element. The element can
     *        be modified externally so we need a key object to identify the right one. Can't be
     *        null.
     * @param parentWidget The widget that contains the provided element. This is necessary to
     *        perform certain validation actions such as jump and expand. Overrides any previous
     *        widget set. Can't be null.
     */
    public void addElement(Element element, Object elementIdentifier, Widget parentWidget) {
        if (element == null) {
            throw new IllegalArgumentException("The parameter 'elements' cannot be null.");
        } else if (elementIdentifier == null) {
            throw new IllegalArgumentException("The parameter 'elementIdentifier' cannot be null.");
        } else if (parentWidget == null) {
            throw new IllegalArgumentException("The parameter 'parentWidget' cannot be null.");
        }

        // if the status is invalid; update UI style
        if (!isValid()) {
            addStyle(element);
        }

        this.widget = parentWidget;
        this.elementMap.put(elementIdentifier, element);
    }

    /**
     * Remove an element from the list
     * 
     * @param elementIdentifier the object used to identify the correct element.
     */
    public void removeElementByKey(Object elementIdentifier) {
        Element value = elementMap.remove(elementIdentifier);
        if (value != null) {
            removeStyle(value);
        }
    }

    /**
     * Add a red border around the element.
     * 
     * @param element the element that is invalid.
     */
    private void addStyle(Element element) {
        element.addClassName(CSS.invalidWidgetStyle());
    }

    /**
     * Remove the red border around the element.
     * 
     * @param element the element that is valid.
     */
    private void removeStyle(Element element) {
        element.removeClassName(CSS.invalidWidgetStyle());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
        result = prime * result + (isValid() ? 1231 : 1237);
        result = prime * result + ((widget == null) ? 0 : widget.hashCode());
        result = prime * result + ((elementMap == null) ? 0 : elementMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // Check that the object to compare is of this type
        if (!(obj instanceof WidgetValidationStatus)) {
            return false;
        }

        WidgetValidationStatus otherContainer = (WidgetValidationStatus) obj;

        // First check the container reference
        if (this == otherContainer) {
            return true;

            /* Then check if the containers have the same error message (excluding blank). Both
             * containers need null widgets in order to key off of the error message */
        } else if (getWidget() == null && otherContainer.getWidget() == null && StringUtils.isNotBlank(getErrorMsg())
                && StringUtils.equals(getErrorMsg(), otherContainer.getErrorMsg())) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder("[WidgetValidationStatus: ").append("errorMsg = ").append('"').append(getErrorMsg())
                .append('"').append(", valid = ").append(isValid()).append(", widget = ")
                .append(widget != null ? widget.getClass().getSimpleName() : null).append("]").toString();
    }
}
