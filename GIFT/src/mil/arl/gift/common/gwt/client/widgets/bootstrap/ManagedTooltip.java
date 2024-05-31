/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wraps the bootstrap3 Tooltip so we can ensure the tooltip is hidden, even if
 * the original widget is removed prior to a mouse-out event.
 * 
 * @author sharrison
 */
public class ManagedTooltip extends org.gwtbootstrap3.client.ui.Tooltip {
    /** The attach handler registration for the widget */
    private HandlerRegistration registration;

    /**
     * Creates the empty Popover
     */
    public ManagedTooltip() {
        super();
        setContainer("body");
    }

    /**
     * Creates the tooltip with given title. Remember to set the content and
     * widget as well.
     * 
     * @param title title for the tooltip
     */
    public ManagedTooltip(String title) {
        super(title);
        setContainer("body");
    }

    /**
     * Creates the tooltip around this widget
     * 
     * @param w widget for the tooltip
     */
    public ManagedTooltip(Widget w) {
        super(w);
        setContainer("body");
    }

    /**
     * Creates the tooltip around this widget with given title and content.
     * 
     * @param w widget for the tooltip
     * @param title title for the tooltip
     */
    public ManagedTooltip(Widget w, String title) {
        super(w, title);
        setContainer("body");
    }

    @Override
    public void setWidget(Widget w) {
        super.setWidget(w);

        if (registration != null) {
            registration.removeHandler();
        }

        if (w != null) {
            // when using a touch screen device the managed tooltip would remain on the screen
            // after touching + releasing the widget (even when the widget was removed from view),
            // the tooltip would sometimes be removed if you touched + released the tooltip itself.
            // By adding this click handler logic, the tooltip is guaranteed to be removed once you
            // click the widget.  This is acceptable for non touch screens as well as it removes the
            // tooltip from view since the tooltip shouldn't be needed anymore anyways now that the
            // user is interacting with the widget.
            w.addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent arg0) {
                    hide();
                }
            }, ClickEvent.getType());
            registration = w.addAttachHandler(new Handler() {
                @Override
                public void onAttachOrDetach(AttachEvent event) {
                    if (!event.isAttached() && getWidget() != null) {
                        hide();
                    }
                }
            });
        }
    }

    @Override
    public void clear() {
        super.clear();
        if (registration != null) {
            registration.removeHandler();
        }
    }

    /**
     * Sets whether the containing widget is visible.
     * 
     * @param visible true to show the containing widget, false to hide it
     */
    public void setVisible(boolean visible) {
        if (asWidget() != null) {
            asWidget().setVisible(visible);
        }
    }

    /**
     * Attach a {@link ManagedTooltip} to the provided widget.
     * 
     * @param w the widget to wrap in the {@link ManagedTooltip}. Can't be null.
     * @param tooltip the tooltip text.
     * @return the {@link ManagedTooltip} wrapping the provided widget.
     */
    public static ManagedTooltip attachTooltip(Widget w, String tooltip) {
        if (w == null) {
            throw new IllegalArgumentException("The parameter 'w' cannot be null.");
        }

        return new ManagedTooltip(w, tooltip);
    }

    /**
     * Attach a {@link ManagedTooltip} to the provided widget.
     * 
     * @param w the widget to wrap in the {@link ManagedTooltip}. Can't be null.
     * @param tooltip the tooltip text.
     * @return the {@link ManagedTooltip} wrapping the provided widget.
     */
    public static ManagedTooltip attachTooltip(Widget w, SafeHtml tooltip) {
        if (w == null) {
            throw new IllegalArgumentException("The parameter 'w' cannot be null.");
        }

        final ManagedTooltip managedTooltip = new ManagedTooltip(w);
        managedTooltip.setHtml(tooltip);
        return managedTooltip;
    }
}
