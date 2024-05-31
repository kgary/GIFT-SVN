/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Iterator;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;

/**
 * A {@link PopupPanel} that is used to ask for confirmation before taking some
 * action.
 *
 * @author tflowers
 *
 */
public class OverlayPopup extends Composite implements HasWidgets {

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface OverlayPopupUiBinder extends UiBinder<Widget, OverlayPopup> {
    }

    /** The UiBinder that combines the ui.xml with this java class */
    OverlayPopupUiBinder uiBinder = GWT.create(OverlayPopupUiBinder.class);

    /** The outer zero size panel */
    @UiField
    protected SimplePanel outerPanel;

    /** The inner absolutely positioned panel */
    @UiField
    protected FlowPanel innerPanel;

    /** The icon used to close the popup */
    private final Icon closeButton = new Icon(IconType.TIMES);
    {
        final Style iconStyle = closeButton.getElement().getStyle();
        iconStyle.setCursor(Cursor.POINTER);
        iconStyle.setFloat(Style.Float.RIGHT);
        iconStyle.setPaddingBottom(5, Unit.PX);
    }

    /** The tooltip for the {@link #closeButton} */
    private final ManagedTooltip closeButtonTooltip = new ManagedTooltip(closeButton, "Close");

    /**
     * Creates the {@link OverlayPopup} with the given text in their
     * corresponding locations.
     *
     * @param promptText The text used to prompt the user for a decision. Null
     *        is treated as blank.
     * @param confirmText The text used to describe the confirm option. Null is
     *        treated as blank.
     * @param denyText The text used to describe the deny option. Null is
     *        treated as blank.
     */
    @UiConstructor
    public OverlayPopup() {
        initWidget(uiBinder.createAndBindUi(this));

        outerPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        }, ClickEvent.getType());

        Event.addNativePreviewHandler(new NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                // Nick: This logic imitates the auto-hiding logic in
                // PopupPanel.previewNativeEvent()
                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());
                if (eventTargetsPopup(nativeEvent)) {
                    event.consume();
                }

                // Switch on the event type
                switch (nativeEvent.getTypeInt()) {

                case Event.ONMOUSEDOWN:
                case Event.ONTOUCHSTART:
                    // Don't eat events if event capture is enabled, as this can
                    // interfere with dialog dragging, for example.
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    if (!eventTargetsPopup(nativeEvent)) {
                        hide();
                        return;
                    }
                    break;
                }
            }
        });

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
                hide();
            }
        });

    }

    /**
     * Does the event target this popup?
     *
     * @param event the native event
     * @return true if the event targets the popup
     */
    private boolean eventTargetsPopup(NativeEvent event) {
        EventTarget target = event.getEventTarget();
        if (Element.is(target)) {
            return getElement().isOrHasChild(Element.as(target));
        }
        return false;
    }

    /**
     * Set whether or not to show a close icon in the top right corner of the
     * popup. Defaults to false
     * 
     * @param show true to show; false to hide.
     */
    public void setCloseIconVisible(boolean show) {
        if (show) {
            /* Only add once */
            if (innerPanel.getWidgetIndex(closeButtonTooltip) == -1) {
                if (innerPanel.getWidgetCount() == 0) {
                    innerPanel.add(closeButtonTooltip);
                } else {
                    innerPanel.insert(closeButtonTooltip, 0);
                }
            }
        } else {
            innerPanel.remove(closeButtonTooltip);
        }
    }

    /**
     * Displays the {@link OverlayPopup} to the user.
     */
    public void show() {
        outerPanel.setVisible(true);
    }

    /**
     * Hides the {@link OverlayPopup} from the user.
     */
    public void hide() {
        outerPanel.setVisible(false);
    }

    @Override
    public void add(Widget w) {
        if (w == null) {
            throw new IllegalArgumentException("The parameter 'w' cannot be null.");
        }

        innerPanel.add(w);
    }

    @Override
    public void clear() {
        innerPanel.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return innerPanel.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return innerPanel.remove(w);
    }
}
