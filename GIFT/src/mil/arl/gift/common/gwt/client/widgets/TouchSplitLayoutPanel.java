/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Touch;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.event.ExtendedBrowserEvents;

/*
 * Note: This widget is mostly based on an implementation of SplitLayoutPanel with 
 * touch event support found here: 
 * http://gwtnews.blogspot.com/2018/04/re-gwt-251-splitlayoutpanel-draggers.html
 * 
 * Some modifications have been made to this implementation to allow this widget
 * to better behave as a drop-in replacement for the original SplitLayoutPanel. 
 * Overall, this widget functions as a hybrid of both implementations and
 * inherits some code from both.
 */
/**
 * An alternative implementation of GWT's built-in 
 * {@link com.google.gwt.user.client.ui.SplitLayoutPanel SplitLayoutPanel}widget
 * that supports adjusting the size of the splitter bar using touch gestures.
 * 
 * <p>
 * Aside from supporting touch events, this widget behaves identically to
 * SplitLayoutPanel and can generally be used as a drop-in replacement wherever a 
 * SplitLayoutPanel needs to support touch events.
 * </p>
 */
public class TouchSplitLayoutPanel extends DockLayoutPanel {
    
    /** The CSS styling used by this widget*/
    private static final Style CSS = Bundle.INSTANCE.css();
    
    /** Interface to allow CSS file access */
    public static interface Bundle extends ClientBundle {
        public static final Bundle INSTANCE = GWT.create(Bundle.class);

        @Source("TouchSplitLayoutPanel.css")
        public Style css();
    }
    
    /** Interface to allow CSS style name access */
    protected static interface Style extends CssResource {

        /** 
         * A special style used when this widget is displayed on 
         * devices that support touch gestures 
         */
        public String touch();
    }
    
    static{
        CSS.ensureInjected();
    }

    class HSplitter extends Splitter {
        public HSplitter(Widget target, boolean reverse) {
            super(target, reverse);
            getElement().getStyle().setPropertyPx("width", splitterSize);
            setStyleName("gwt-SplitLayoutPanel-HDragger");
        }

        @Override
        protected int getAbsolutePosition() {
            return getAbsoluteLeft();
        }

        @Override
        protected int getScrollPosition() {
            return getElement().getScrollLeft();
        }

        @Override
        protected double getCenterSize() {
            return getCenterWidth();
        }

        @Override
        protected int getEventPosition(Event event) {
            JsArray<Touch> touches = event.getTouches();
            if(touches != null && touches.length() > 0) {
                return touches.get(0).getClientX();
                
            } else {
                return event.getClientX();
            }
            
        }

        @Override
        protected int getTargetPosition() {
            return target.getAbsoluteLeft();
        }

        @Override
        protected int getTargetSize() {
            return target.getOffsetWidth();
        }
    }

    abstract class Splitter extends Widget {
        protected final Widget target;

        private int offset;
        private boolean mouseDown;
        private ScheduledCommand layoutCommand;

        private final boolean reverse;
        private int minSize;
        private int snapClosedSize = -1;
        private double centerSize, syncedCenterSize;

        private boolean toggleDisplayAllowed = false;
        private double lastClick = 0;

        public Splitter(Widget target, boolean reverse) {
            this.target = target;
            this.reverse = reverse;

            setElement(Document.get().createDivElement());
            
            /* Register to receive pointer events */
            DOM.sinkBitlessEvent(getElement(), ExtendedBrowserEvents.POINTER_UP);
            DOM.sinkBitlessEvent(getElement(), ExtendedBrowserEvents.POINTER_DOWN);
            DOM.sinkBitlessEvent(getElement(), ExtendedBrowserEvents.POINTER_MOVE);
            
            /* As a fallback, also register for mouse and touch events as well */
            sinkEvents(Event.MOUSEEVENTS);
            sinkEvents(Event.TOUCHEVENTS);
            
            /* 
             * This helps address #4725
             * 
             * This property is super-important to allow pointer events to be processed correctly
             * on certain touch devices. 
             * 
             * If touchAction is not set to none, then some devices will attempt to claim pointer
             * events for built-in operations like panning and zooming, which will trigger a
             * pointercancel event on the splitter element that prevents further pointer events
             * from being triggered.
             * 
             * Setting touchAction to none prevents this behavior and allows the splitter to 
             * properly handle pointer events from all devices.
             */
            getElement().getStyle().setProperty("touchAction", "none");
        }

        @Override
        public void onBrowserEvent(Event event) {
            
            /* 
             * This helps address #4725
             * 
             * Use pointer events to detect when the splitter is dragged by any device. This is
             * an improvement over SplitLayoutPanel's normal logic, since that only handles mouse
             * events and doesn't work on touch screen devices like tablets. 
             * 
             * Unlike touch events, pointer events work for devices that use pens/styles as well
             * as traditional mice, so they work best as an all around solution
             */
            String type = event.getType();
            switch (type) {
            case ExtendedBrowserEvents.POINTER_DOWN:
                mouseDown = true;
                /*
                 * Resize glassElem to take up the entire scrollable window
                 * area, which is the greater of the scroll size and the
                 * client size.
                 */
                int width = Math.max(Window.getClientWidth(), Document.get().getScrollWidth());
                int height = Math.max(Window.getClientHeight(), Document.get().getScrollHeight());
                glassElem.getStyle().setHeight(height, Unit.PX);
                glassElem.getStyle().setWidth(width, Unit.PX);
                Document.get().getBody().appendChild(glassElem);
                offset = getEventPosition(event) - getAbsolutePosition();
                if (offset < 0)
                    offset = -(offset - splitterSize);
                
                /* Using this method instead of Event.setCapture(Element) helps address #4725 */
                setPointerCapture(getElement(), event);
                
                event.preventDefault();
                break;

            case ExtendedBrowserEvents.POINTER_UP:
                mouseDown = false;

                glassElem.removeFromParent();

                // Handle double-clicks.
                // Fake them since the double-click event aren't fired.
                if (this.toggleDisplayAllowed) {
                    double now = Duration.currentTimeMillis();
                    if (now - this.lastClick < DOUBLE_CLICK_TIMEOUT) {
                        now = 0;
                        LayoutData layout = (LayoutData) target.getLayoutData();
                        if (layout.size == 0) {
                            // Restore the old size.
                            setAssociatedWidgetSize(layout.oldSize);
                        } else {
                            /*
                             * Collapse to size 0. We change the size instead of
                             * hiding the widget because hiding the widget can
                             * cause issues if the widget contains a flash
                             * component.
                             */
                            layout.oldSize = layout.size;
                            setAssociatedWidgetSize(0);
                        }
                    }
                    this.lastClick = now;
                }

                /* Using this method instead of Event.releaseCapture(Element) helps address #4725 */
                releasePointerCapture(getElement(), event);
                
                event.preventDefault();
                break;

            case ExtendedBrowserEvents.POINTER_MOVE:
                if (mouseDown) {
                    int size;
                    if (reverse) {
                        size = getTargetPosition() + getTargetSize() - getEventPosition(event) - offset;
                    } else {
                        size = getEventPosition(event) - getTargetPosition() - offset;
                    }
                    ((LayoutData) target.getLayoutData()).hidden = false;
                    setAssociatedWidgetSize(size);
                    event.preventDefault();
                }
                break;
            }
        }

        public void setMinSize(int minSize) {
            this.minSize = minSize;
            LayoutData layout = (LayoutData) target.getLayoutData();

            // Try resetting the associated widget's size, which will enforce
            // the new
            // minSize value.
            setAssociatedWidgetSize((int) layout.size);
        }

        public void setSnapClosedSize(int snapClosedSize) {
            this.snapClosedSize = snapClosedSize;
        }

        public void setToggleDisplayAllowed(boolean allowed) {
            this.toggleDisplayAllowed = allowed;
        }

        protected abstract int getAbsolutePosition();

        protected abstract int getScrollPosition();

        protected abstract double getCenterSize();

        protected abstract int getEventPosition(Event event);

        protected abstract int getTargetPosition();

        protected abstract int getTargetSize();

        private double getMaxSize() {
            // To avoid seeing stale center size values due to deferred layout
            // updates, maintain our own copy up to date and resync when the
            // DockLayoutPanel value changes.
            double newCenterSize = getCenterSize();
            if (syncedCenterSize != newCenterSize) {
                syncedCenterSize = newCenterSize;
                centerSize = newCenterSize;
            }

            return Math.max(((LayoutData) target.getLayoutData()).size + centerSize, 0);
        }

        private void setAssociatedWidgetSize(double size) {
            double maxSize = getMaxSize();
            if (size > maxSize) {
                size = maxSize;
            }

            if (snapClosedSize > 0 && size < snapClosedSize) {
                size = 0;
            } else if (size < minSize) {
                size = minSize;
            }

            LayoutData layout = (LayoutData) target.getLayoutData();
            if (size == layout.size) {
                return;
            }

            // Adjust our view until the deferred layout gets scheduled.
            centerSize += layout.size - size;
            layout.size = size;

            // Defer actually updating the layout, so that if we receive many
            // mouse events before layout/paint occurs, we'll only update once.
            if (layoutCommand == null) {
                layoutCommand = new ScheduledCommand() {
                    @Override
                    public void execute() {
                        layoutCommand = null;
                        forceLayout();
                    }
                };
                Scheduler.get().scheduleDeferred(layoutCommand);
            }
        }
    }

    class VSplitter extends Splitter {
        public VSplitter(Widget target, boolean reverse) {
            super(target, reverse);
            getElement().getStyle().setPropertyPx("height", splitterSize);
            setStyleName("gwt-SplitLayoutPanel-VDragger");
        }

        @Override
        protected int getAbsolutePosition() {
            return getAbsoluteTop();
        }

        @Override
        protected int getScrollPosition() {
            return getElement().getScrollTop();
        }

        @Override
        protected double getCenterSize() {
            return getCenterHeight();
        }

        @Override
        protected int getEventPosition(Event event) {
            
            JsArray<Touch> touches = event.getTouches();
            if(touches != null && touches.length() > 0) {
                return touches.get(0).getClientY();
                
            } else {
                return event.getClientY();
            }
        }

        @Override
        protected int getTargetPosition() {
            return target.getAbsoluteTop();
        }

        @Override
        protected int getTargetSize() {
            return target.getOffsetHeight();
        }
    }

    /* 
     * If touch gestures are supported, use a larger default splitter size 
     * than the regular SplitLayoutPanel to make the splitter easier 
     * to move with touch gestures
     */
    private static final int DEFAULT_SPLITTER_SIZE = 16;
    private static final int DOUBLE_CLICK_TIMEOUT = 500;

    /**
     * The element that masks the screen so we can catch mouse events over
     * iframes.
     */
    private static Element glassElem = null;

    private final int splitterSize;

    /**
     * Construct a new {@link SplitLayoutPanel} with the default splitter size.
     */
    @UiConstructor
    public TouchSplitLayoutPanel() {
        this(DEFAULT_SPLITTER_SIZE);
    }


    /**
     * Construct a new {@link SplitLayoutPanel} with the specified splitter size
     * in pixels.
     *
     * @param splitterSize
     *            the size of the splitter in pixels
     */
    public TouchSplitLayoutPanel(int splitterSize) {
        super(Unit.PX);
        this.splitterSize = splitterSize;
        setStyleName("gwt-SplitLayoutPanel");
        addStyleName(CSS.touch());

        if (glassElem == null) {
            glassElem = Document.get().createDivElement();
            glassElem.getStyle().setPosition(Position.ABSOLUTE);
            glassElem.getStyle().setTop(0, Unit.PX);
            glassElem.getStyle().setLeft(0, Unit.PX);
            glassElem.getStyle().setMargin(0, Unit.PX);
            glassElem.getStyle().setPadding(0, Unit.PX);
            glassElem.getStyle().setBorderWidth(0, Unit.PX);

            // We need to set the background color or mouse events will go right
            // through the glassElem. If the SplitPanel contains an iframe, the
            // iframe will capture the event and the slider will stop moving.
            glassElem.getStyle().setProperty("background", "white");
            glassElem.getStyle().setOpacity(0.0);
        }
    }
    
    /**
     * Return the size of the splitter in pixels.
     *
     * @return the splitter size
     */
    public int getSplitterSize() {
        return splitterSize;
    }

    @Override
    public void insert(Widget child, Direction direction, double size, Widget before) {
        super.insert(child, direction, size, before);
        if (direction != Direction.CENTER) {
            insertSplitter(child, before);
        }
    }

    @Override
    public boolean remove(Widget child) {
        assert !(child instanceof Splitter) : "Splitters may not be directly removed";

        int idx = getWidgetIndex(child);
        if (super.remove(child)) {
            // Remove the associated splitter, if any.
            // Now that the widget is removed, idx is the index of the splitter.
            if (idx < getWidgetCount()) {
                // Call super.remove(), or we'll end up recursing.
                super.remove(getWidget(idx));
            }
            return true;
        }
        return false;
    }

    @Override
    public void setWidgetHidden(Widget widget, boolean hidden) {
        super.setWidgetHidden(widget, hidden);
        Splitter splitter = getAssociatedSplitter(widget);
        if (splitter != null) {
            // The splitter is null for the center element.
            super.setWidgetHidden(splitter, hidden);
        }
    }

    /**
     * Sets the minimum allowable size for the given widget.
     *
     * <p>
     * Its associated splitter cannot be dragged to a position that would make
     * it smaller than this size. This method has no effect for the
     * {@link com.google.gwt.user.client.ui.DockLayoutPanel.Direction#CENTER}
     * widget.
     * </p>
     *
     * @param child
     *            the child whose minimum size will be set
     * @param minSize
     *            the minimum size for this widget
     */
    public void setWidgetMinSize(Widget child, int minSize) {
        assertIsChildWidget(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setMinSize(minSize);
        }
    }

    /**
     * Sets a size below which the slider will close completely. This can be
     * used in conjunction with {@link #setWidgetMinSize} to provide a
     * speed-bump effect where the slider will stick to a preferred minimum size
     * before closing completely.
     *
     * <p>
     * This method has no effect for the
     * {@link com.google.gwt.user.client.ui.DockLayoutPanel.Direction#CENTER}
     * widget.
     * </p>
     *
     * @param child
     *            the child whose slider should snap closed
     * @param snapClosedSize
     *            the width below which the widget will close or -1 to disable.
     */
    public void setWidgetSnapClosedSize(Widget child, int snapClosedSize) {
        assertIsChildWidget(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setSnapClosedSize(snapClosedSize);
        }
    }

    /**
     * Sets whether or not double-clicking on the splitter should toggle the
     * display of the widget.
     *
     * @param child
     *            the child whose display toggling will be allowed or not.
     * @param allowed
     *            whether or not display toggling is allowed for this widget
     */
    public void setWidgetToggleDisplayAllowed(Widget child, boolean allowed) {
        assertIsChildWidget(child);
        Splitter splitter = getAssociatedSplitter(child);
        // The splitter is null for the center element.
        if (splitter != null) {
            splitter.setToggleDisplayAllowed(allowed);
        }
    }

    private void assertIsChildWidget(Widget widget) {
        assert (widget == null) || (widget.getParent() == this) : "The specified widget is not a child of this panel";
    }

    private Splitter getAssociatedSplitter(Widget child) {
        // If a widget has a next sibling, it must be a splitter, because the
        // only widget that *isn't* followed by a splitter must be the CENTER,
        // which has no associated splitter.
        int idx = getWidgetIndex(child);
        if (idx > -1 && idx < getWidgetCount() - 1) {
            Widget splitter = getWidget(idx + 1);
            assert splitter instanceof Splitter : "Expected child widget to be splitter";
            return (Splitter) splitter;
        }
        return null;
    }

    private void insertSplitter(Widget widget, Widget before) {
        int size = getChildren().size();
        assert size > 0 : "Can't add a splitter before any children";

        LayoutData layout = (LayoutData) widget.getLayoutData();
        Splitter splitter = null;
        switch (getResolvedDirection(layout.direction)) {
        case WEST:
            splitter = new HSplitter(widget, false);
            break;
        case EAST:
            splitter = new HSplitter(widget, true);
            break;
        case NORTH:
            splitter = new VSplitter(widget, false);
            break;
        case SOUTH:
            splitter = new VSplitter(widget, true);
            break;
        default:
            assert false : "Unexpected direction";
        }

        super.insert(splitter, layout.direction, splitterSize, before);
            
        //allow the splitter to overflow when displayed on mobile devices so its handle is more visible
        super.getWidgetContainerElement(splitter).getStyle().setOverflow(Overflow.VISIBLE);
    }
    
    /**
     * Tells the browser to capture all pointer events on the page using the given element
     * <br/><br/>
     * Internally, this calls the native JavaScript setPointerCapture function. This can be thought of
     * as a native alternative to GWT's Event.setCapture(getElement()) method. The GWT method only handles
     * mouse events unless pointer events are registered for event capture with DOMImplStanard, 
     * whereas this method handles all pointer events without any extra work.
     * 
     * @param element the element that should capture all pointer events. Cannot be null.
     * @param pointerEvent the pointer event used to kick off the capture. Needed to get the ID
     * of the pointer whose events should be captured.
     */
    private native void setPointerCapture(Element element, Event pointerEvent)/*-{
        if(element){
            element.setPointerCapture(pointerEvent.pointerId);
        }
    }-*/;

    /**
     * Tells the browser to stop capturing all pointer events on the page using the given element
     * <br/><br/>
     * Internally, this calls the native JavaScript releasePointerCapture function. This can be thought of
     * as a native alternative to GWT's Event.releaseCapture(getElement()) method. The GWT method only handles
     * mouse events unless pointer events are registered for event capture with DOMImplStanard, 
     * whereas this method handles all pointer events without any extra work.
     * 
     * @param element the element that should stop capturing all pointer events. Cannot be null.
     * @param pointerEvent the pointer event used to kick off stopping the capture. Needed to get the ID
     * of the pointer whose events should not be captured..
     */
    private native void releasePointerCapture(Element element, Event pointerEvent)/*-{
        if(element){
            element.releasePointerCapture(pointerEvent.pointerId);
        }
    }-*/;
}