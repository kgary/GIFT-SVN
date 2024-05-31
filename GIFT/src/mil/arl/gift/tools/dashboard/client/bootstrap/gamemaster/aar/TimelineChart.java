/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import java.util.Date;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.arrays.ForEachCallback;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.UpdateSelection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.scales.OrdinalScale;
import com.github.gwtd3.api.svg.Axis;
import com.github.gwtd3.api.svg.Axis.Orientation;
import com.github.gwtd3.api.time.TimeFormat;
import com.github.gwtd3.api.time.TimeScale;
import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.animation.client.AnimationScheduler.AnimationHandle;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsDate;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.event.ExtendedBrowserEvents;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.IntervalClickHandler;

/**
 * A modified, interactive Gantt chart used to display a timeline of how a series of events evolve over 
 * a period of time.
 * <br/><br/>
 * The core rendering logic for this widget is based off that of the 
 * <a href='https://github.com/dk8996/Gantt-Chart'>D3 Gantt Chart library</a>, albeit with modifications to improve
 * usability and interaction with other GIFT widgets.
 * 
 * @author nroberts
 */
public class TimelineChart extends Composite {

    private static final Logger logger = Logger.getLogger(TimelineChart.class.getName());
    
    /** The time format that labels in the time axis should use when rendering their text */
    private static final TimeFormat TICK_FORMAT = D3.time().format("%H:%M:%S"); //HH:MM:SS format
    
    /** The default height of each event in the chart (in pixels) */
    private static final int DEFAULT_EVENT_HEIGHT = 30;
    
    /** The minimum number of pixels that should separate each tick in the x axis */
    private static final int X_TICK_WIDTH = 150;
    
    /** The number of pixels to offset labels horizontally before nesting is applied*/
    private static final int LABEL_INITIAL_OFFSET = 20;
    
    /** The number of pixels to offset labels horizontally for each level of nesting */
    private static final int LABEL_NESTING_OFFSET = 20;
    
    /** Unicode string for the icon used to display that a timeline event is expanded to show its sub-events */
    private static final String EXPANDED_ICON_UNICODE = "\uf0d7";
    
    /** Unicode string for the icon used to display that a timeline event is expanded to show its sub-events */
    private static final String COLLAPSED_ICON_UNICODE = "\uf0da";
    
    /** The CSS class used to identify elements representing intervals within an event */
    private static final String INTERVAL = "interval";
    
    /** The CSS clased used to identify the group containing the elements representing intervals within an event */
    private static final String INTERVALS = "intervals";
    
    /** The CSS class used to identify the interactive area over the time axis used to control the playback time */
    private static final String SEEK_AREA = "seekArea";

    /** The CSS class used to identify the seek line shown when the mouse is over the seek area*/
    private static final String SEEK = "seek";
    
    /** The CSS class used to determine whether the mouse has clicked on the seek area or is just hovering over it */
    private static final String SEEK_ACTIVE = "seekActive";
    
    /** The CSS class used to identify the line shown to represent the current playback time */
    private static final String PLAYBACK = "playback";

    /** The CSS class used to identify the background area of each y-axis tick */
    private static final String TICK_AREA = "tickArea";
    
    /** The CSS class used to identify elements representing the text in timeline events */
    private static final String EVENT_TEXT = "eventText";
    
    /** The CSS class used to identify elements representing the button used to expand or collapse timeline events */
    private static final String EVENT_BUTTON = "eventButton";
    
    /** The CSS class used to identify elements representing timeline events */
    private static final String EVENT = "event";
    
    /** The CSS class used to identify the group containing the elements representing each non-fixed (i.e. scrolling) timeline event */
    private static final String EVENTS = "events";
    
    /** The CSS class used to identify the group containing the elements representing each fixed timeline event */
    private static final String FIXED_EVENTS = "fixedEvents";
    
    /** The CSS class used to identify elements whose underlying data objects are selected by the user*/
    private static final String SELECTED = "selected";

    /** The default date to start the timeline at, if no start time is provided */
    private static final JsDate DEFAULT_START_DATE = D3.time().hour().offset(JsDate.create(), -1);

    /** The default date to start the timeline at, if no start time is provided */
    private static final JsDate DEFAULT_END_DATE = D3.time().hour().offset(JsDate.create(), 1);
    
    /** The default formatter used to style chart elements if no other formatter is provided */
    private static final Formatter DEFAULT_FORMATTER = new Formatter() {};
    
    /** The format to use when displaying dates in the playback time label */
    private static final DateTimeFormat PLAYBACK_TIME_LABEL_FORMAT = DateTimeFormat.getFormat("H:mm:ss EEEE, MMMM dd, yyyy");
    
    private static TimelineChartUiBinder uiBinder = GWT.create(TimelineChartUiBinder.class);

    interface TimelineChartUiBinder extends UiBinder<Widget, TimelineChart> {
    }
    
    /** The container area where the chart will be drawn */
    @UiField
    protected Widget chartArea;
    
    /** The container area where the labels will be drawn */
    @UiField
    protected Widget labelsArea;
    
    /** The widget that the chart scrolls inside when it becomes too large */
    @UiField
    protected Widget chartScroller;
    
    /** The widget that the labels scroll inside when they become too large */
    @UiField
    protected Widget labelScroller;
    
    /** A layout panel used to detect when the user has moved the splitter separating the labels and chart */
    @UiField(provided=true)
    protected SimpleLayoutPanel chartLayout = new SimpleLayoutPanel() {
        
        @Override
        public void onResize() {
            super.onResize();
            
            if(isAttached()) {
                redraw(); //redraw whenever the splitter separating the labels and chart is moved
            }
        }
    };
    
    /** A layout panel containing the labels area. Used to adjust the label size automatically.*/
    @UiField
    protected SimpleLayoutPanel labelLayout;
    
    /** A panel that separates the labels and chart areas with a splitter and allows them to be resized by the user */
    @UiField
    protected SplitLayoutPanel chartSplitter;
    
    /** whether looping of playback is enabled */
    private boolean loopEnabled = false;

    /** The main D3 selector for this widget that corresponds to the chart element in the DOM*/
    private Selection chartSelector;
    
    /** The main D3 selector for this widget that corresponds to the labels element in the DOM*/
    private Selection labelsSelector;
    
    /** The formatter that the chart will use to style event data as it is drawn */
    private Formatter formatter = DEFAULT_FORMATTER;
    
    /** The height (in pixels) to give the x scale (i.e. the time scale) */
    private int xAxisHeight;
    
    /** The calculated combined height of all the non-fixed (i.e. scrolling) events in the chart */
    private double scrollEventHeight;
    
    /** The calculated combined height of all the fixed events in the chart */
    private double fixedEventHeight;
    
    /** The width to give the SVG area that forms the chart. This does not include the size of the label area. */
    private double chartWidth;

    /** The scale that determines how a point of data should be translated to an x position in the chart */
    private TimeScale xScale;

    /** The scale that determines how a non-fixed (i.e. scrolling) event should be translated to an y position in the chart */
    private OrdinalScale scrollingEventYScale;
    
    /** The scale that determines how a fixed event should be translated to an y position in the chart */
    private OrdinalScale fixedEventYScale;
    
    /** The scaling factor (i.e. the multiplier) used to zoom the chart's horizontal scale */
    private double xZoom = 1;
    
    /** The scaling factor (i.e. the multiplier) used to zoom the chart's vertical scale */
    private double yZoom = 1;

    /** The axis that draws a visual representation of the x scale */
    private Axis xAxis;
    
    /** The start date of timeline (i.e. the start of the time scale's domain data) */
    private JsDate timeDomainStart = DEFAULT_START_DATE; //default to a dummy time to avoid NPEs

    /** The end date of timeline (i.e. the end of the time scale's domain data) */
    private JsDate timeDomainEnd = DEFAULT_END_DATE; //default to a dummy time to avoid NPEs

    /** The history of events (i.e. the timeline) currently being displayed by this chart */
    private TimelineHistory timelineHistory = TimelineHistory.create(DEFAULT_START_DATE, DEFAULT_END_DATE);
    
    /** 
     * The non-fixed (i.e. scrolling) events that are currently visually represented by the chart. 
     * The events in this array are backed by the timeline history, but the array itself is filtered 
     * separately so that the user can view smaller sections of the history at a time, depending on 
     * which events are currently expanded or collapsed.
     */
    private Array<TimelineEvent> scrollingEvents = Array.create();
    
    /** 
     * The fixed events that are currently visually represented by the chart. 
     * The events in this array are backed by the timeline history, but the array itself is filtered 
     * separately so that the user can view smaller sections of the history at a time, depending on 
     * which events are currently expanded or collapsed.
     */
    private Array<TimelineEvent> fixedEvents = Array.create();
    
    /** The rightmost x-position of any of the chart's labels. This includes both the label's offset AND its length */
    private double rightmostLabelX = 0;
    
    /**
     * The function used to determine the  CSS class that should be applied to the rectangle element
     * rendered for a given {@link TimelineEvent}. This controls the element's styling in the chart.
     */
    private DatumFunction<String> eventClass = new DatumFunction<String>() {

        @Override
        public String apply(Element context, Value d, int index) {
            
            TimelineEvent event = d.<TimelineEvent>as();
            
            String cssClass = formatter.getCssClass(event);
            
            String selectionClass = (selectedEvent != null && selectedEvent.equals(event))
                    ? " " + SELECTED 
                    : "";
            
            if(cssClass == null) {
                return EVENT + selectionClass; //don't apply any formatter styling
            }
            
            //style the rect using the appropriate CSS class from the provided formatter
            return EVENT + " " + cssClass + selectionClass;
        }
    };
    
    /**
     * The function used to determine the type of transformation that should be applied to the rectangle element
     * rendered for a given {@link EventInterval}. This controls the element's positioning in the chart.
     */
    private DatumFunction<String> intervalTransform = new DatumFunction<String>() {

        @Override
        public String apply(Element context, Value datum, int index) {
            
            /*
             * position the rect at the x and y positions corresponding to the start date and event
             * of the event interval, according to the appropriate scales
             */
            EventInterval d = datum.as();
            return "translate(" 
                    + xScale.apply(d.getStartDate()).asDouble() + ",0)";
        }
    };

    /**
     * The function used to determine the CSS class that should be applied to the rectangle element
     * rendered for a given {@link EventInterval}. This controls the element's styling in the chart.
     */
    private DatumFunction<String> intervalClass = new DatumFunction<String>() {

        @Override
        public String apply(Element context, Value d, int index) {
            
            String cssClass = formatter.getCssClass(d.<EventInterval>as());
            
            if(cssClass == null) {
                return INTERVAL; //don't apply any styling
            }
            
            //style the rect using the appropriate CSS class from the provided formatter
            return INTERVAL + " " + cssClass;
        }
    };
    
    /**
     * The function used to determine the x offset that should be applied to the label rendered for a 
     * given {@link EventInterval}.
     */
    private DatumFunction<Double> intervalLabelX = new DatumFunction<Double>() {

        @Override
        public Double apply(Element context, Value d, int index) {
            
            TimelineEvent event = d.as();
            
            //indent the event's name based on how many parents it has
            double numParents = 0;
            TimelineEvent currEvent = event.getParent();
            while(currEvent != null) {
                numParents++;
                currEvent = currEvent.getParent();
            }
            
            double offset = LABEL_INITIAL_OFFSET + LABEL_NESTING_OFFSET * numParents;
            
            //calculate if the right edge of this label has the highest x position
            double labelRightX = offset + getComputedTextLength(context);
            if(labelRightX > rightmostLabelX) {
                rightmostLabelX = labelRightX;
            }
            
            return offset;
        }
    };

    /**
     * The function used to determine the height that should be applied to the rectangle element
     * rendered for a given {@link EventInterval}. This controls the element's height in the chart.
     */
    private DatumFunction<Double> intervalRectHeight = new DatumFunction<Double>() {

        @Override
        public Double apply(Element context, Value d, int index) {
            
            //have the rect fill up the full height allocated to the interval's associated event in the y scale
            return scrollingEventYScale.rangeBand();
        }
    };

    /**
     * The function used to determine the width that should be applied to the rectangle element
     * rendered for a given {@link EventInterval}. This controls the element's width in the chart.
     */
    private DatumFunction<Double> intervalRectWidth = new DatumFunction<Double>() {

        @Override
        public Double apply(Element context, Value d, int index) {
            
            //set the rect's width to the difference between the x positions of the event interval's start and end dates
            EventInterval datum = d.as();
            return xScale.apply(datum.getEndDate()).asDouble() - xScale.apply(datum.getStartDate()).asDouble();
        }
    };
    
    /**
     * The function used to determine the x position of the text that is rendered for a given {@link EventInterval}
     */
    private DatumFunction<Double> intervalTextX = new DatumFunction<Double>() {

        @Override
        public Double apply(Element context, Value d, int index) {
            
            EventInterval datum = d.as();
            boolean shouldCenter = formatter.shouldCenterIcon(d.<EventInterval>as());
            if(shouldCenter) {
                
                //for an icon to be centered, it's x position must be halfway between the interval's start and end dates
                return (xScale.apply(datum.getEndDate()).asDouble() - xScale.apply(datum.getStartDate()).asDouble())/2;
                
            } else {
                return null;
            }
        }
    };
    
    /**
     * The function used to determine the type of transformation that should be applied to the group
     * element for each {@link TimelineEvent}. This controls the element's positioning in the chart.
     */
    private DatumFunction<String> eventTransform = new DatumFunction<String>() {

        @Override
        public String apply(Element context, Value datum, int index) {
            
            TimelineEvent d = datum.as();
            if(d.isFixed()) {
                
                //fixed events should be positioned relative to the bottom of the X axis and other fixed events
                return "translate(0," + (fixedEventYScale.apply(d.getId()).asDouble() + getXAxisBottom()) + ")";
                
            } else {
                
                /* non-fixed events should be positioned relative to the chart's origin and offset 
                 * to provide enough space for the X axis and any fixed events */
                return "translate(0," + (scrollingEventYScale.apply(d.getId()).asDouble() + xAxisHeight + fixedEventHeight) + ")";
            }
        }
    };

    /** An optional handler that handles when the user performs interactions that change the playback */
    private PlaybackHandler playbackHandler;
    
    /** The manager used to handle playback for this chart */
    private PlaybackManager playbackManager = new PlaybackManager();

    /** 
     * The number of pixels that the chart is scrolled vertically from the top. This value will always be
     * 0 if the chart is not tall enough to cause vertical scrolling.
     */
    private int topScrollOffset = 0;

    /** The element used to contain tooltips displayed for timeline elements */
    private Element tooltipContainer;

    /** Flag indicating if the {@link #playPauseButton} should be disabled. */
    private boolean playPauseDisabled = false;

    /** 
     * Whether the next scroll event received by either the chart or its labels is an automatic event. This is used to
     * avoid looping scroll events when the scroll position of the chart is changed to match its labels or vice-versa.
     */
    private boolean isAutoScrolling = false;
    
    /** 
     * The timeline event that is currently selected by the user. Used to synch the selection 
     * between the chart and its labels 
     */
    private TimelineEvent selectedEvent = null;

    /** The SVG group where the chart is drawn*/
    private Selection chartGroup;
    
    /** The UI elements used to control this timeline's visuals and playback */
    private TimelineControls controls = null;
    
    /** A manager used to handle scrolling this timeline */
    private ScrollManager scrollManager = new ScrollManager();

    /**
     * Creates an empty timeline chart with no data in it. Visually, the chart will appear as a blank panel until
     * data is added to it.
     */
    public TimelineChart() {
        initWidget(uiBinder.createAndBindUi(this));
        
        chartScroller.addDomHandler(new ScrollHandler() {
            
            @Override
            public void onScroll(ScrollEvent event) {
                
                boolean shouldSkip = isAutoScrolling;
                isAutoScrolling  = !isAutoScrolling;
                
                if(shouldSkip) {
                    return;
                }
                
                onChartScroll();
            }
        }, ScrollEvent.getType());
        
        labelScroller.addDomHandler(new ScrollHandler() {
            
            @Override
            public void onScroll(ScrollEvent event) {
                
                boolean shouldSkip = isAutoScrolling;
                isAutoScrolling  = !isAutoScrolling;
                
                if(shouldSkip) {
                    return;
                }
                
                //sync chart position as the labels are scrolled
                chartScroller.getElement().setScrollTop(labelScroller.getElement().getScrollTop());
                
                applyScrollingToChart();
            }
        }, ScrollEvent.getType());
        
        setTooltipContainer(null); //use the default tooltip container
        
        chartSelector = D3.select(chartArea);
        labelsSelector = D3.select(labelsArea);
    }
    
    /**
     * Whether looping is enabled.
     * 
     * @return true if looping of playback is enabled
     */
    public boolean isLoopEnabled(){
        return loopEnabled;
    }

    /**
     * Initializes the start and end times of the timeline to fit the given history of events and builds the scales and axes
     * needed to map that data to positions in the chart. 
     * <br/><br/>
     * The beginning of the timeline will match the earliest start time in the provided data, 
     * while the end of the timeline will match the latest end time. Each unique event encountered in the
     * data will also be given a position and label in the appropriate axis.
     * 
     * @param history the history of events to use in order to initialize the timeline
     */
    private void initTimeDomain() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Initializing time domain");
        }
        
        JsDate historyStart = timelineHistory != null 
                ? timelineHistory.getStartDate()
                : null;
                
        JsDate historyEnd = timelineHistory != null 
                ? timelineHistory.getEndDate() 
                : null;
        
        timeDomainStart = historyStart != null 
                ? historyStart 
                : DEFAULT_START_DATE;
        
        timeDomainEnd = historyEnd != null 
                ? historyEnd 
                : DEFAULT_END_DATE;
        
        //initialize the chart's scale's and axes to reflect the changed time and event domain
        initAxes();
    }
    
    /**
     * Builds the scales and axes needed to map event data passed into this widget to positions in the chart.
     * <br/><br/>
     * If the time domain has changed via {@link #initTimeDomain(Array)}, then the scales and axes will be adjusted
     * to fit the new time domain. If this widget's size has changed, then the scales and axes will be adjusted to 
     * fill the widget's new width and height.
     */
    private void initAxes() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Calculating x axis proportions");
        }
        
        int xAxisLineThickness = 1; //thickness of the x axis' dividing line
        int xAxisTickLength = 5;    //length of ticks in the x axis
        int xAxisTextPadding = 5;   //padding between label text and ticks in the x axis
        int xAxisTextSize = 11;     //text size of labels in the x axis
        
        //gather the IDs from the fixed events
        Array<Integer> fixedEventIds = Array.create();

        for(Value value : fixedEvents.asIterable()) {
            TimelineEvent event = value.<TimelineEvent>as();
            fixedEventIds.push(event.getId());
        }
        
        fixedEventHeight = DEFAULT_EVENT_HEIGHT * yZoom * fixedEventIds.length();
        
        //create a scale mapping each fixed event in the history to a y position in the chart
        fixedEventYScale = D3.scale.ordinal()
                .domain(fixedEventIds) //set the range of events names to convert to y positions
                .rangeRoundBands(Array.fromDoubles(0, fixedEventHeight), 0, 0); //set the range of y positions
        
        //determine the height of the x axis so that rendered event interval elements can be placed below it
        xAxisHeight = xAxisLineThickness + xAxisTickLength + xAxisTextPadding + xAxisTextSize;
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Calculating chart dimmensions");
        }
        
        /* 
         * Set the width of the chart to fill the entire visual area allocated to this widget (times the zoom factor)
         * 
         * You might be wondering why we aren't just using chartArea.getOffsetWidth() here. Well, if the labels
         * have changed size and chartSplitter is still laying out labelLayout, then chartArea.getOffsetWidth() 
         * will NOT match chartArea's size after the splitter lays everything out. Instead, it will act as if the
         * label size if 0 (the size before it is layed out) and return the remaining space. So, basically, if we
         * use chartArea.getOffsetWidth(), then chartArea will incorrectly take up the entire chart chartWidth when the
         * chart is drawn for the first time (i.e. when the labels are laid out for the first time).
         * 
         * Fortunately, chartSplitter knows the width of labelLayout before the resize completes, so we can calculate the
         * needed size of the chart preemptively by subtracting the width of the label area from the total width.
         */
        chartWidth = (getOffsetWidth() - chartSplitter.getSplitterSize() - chartSplitter.getWidgetSize(labelLayout)) * xZoom;
        
        if(chartWidth < 0) {
            chartWidth = 0; //prevent values lower than zero, since SVG elements throw errors if width is negative
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Creating scale to map dates");
        }

        //create a scale mapping each date between the start and end times of the event intervals to an x position in the chart
        xScale = D3.time().scale()
            .domain(Array.fromObjects(timeDomainStart, timeDomainEnd)) //set the range of dates to convert to x positions
            .range(Array.fromDoubles(0, chartWidth)) //set the range of x positions
            .clamp(false);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Creating scale to map event");
        }
        
        //gather the labels from the currently rendered events
        Array<Integer> eventLabels = Array.create();

        Array<TimelineEvent> events = scrollingEvents;
        for(Value value : events.asIterable()) {
            TimelineEvent event = value.<TimelineEvent>as();
            eventLabels.push(event.getId());
        }
        
        //set the height of the chart to the total height needed for all of its events (times the zoom factor)
        scrollEventHeight = DEFAULT_EVENT_HEIGHT * yZoom * eventLabels.length();
        
        //create a scale mapping each non-fixed (i.e. scrolling) event in the history to a y position in the chart
        scrollingEventYScale = D3.scale.ordinal()
                .domain(eventLabels) //set the range of events names to convert to y positions
                .rangeRoundBands(Array.fromDoubles(0, scrollEventHeight), 0, 0); //set the range of y positions
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Creating x axis");
        }
        
        //create an axis to draw the x scale
        xAxis = D3.svg().axis().scale(xScale)
                .orient(Orientation.TOP) //position the x axis' ticks and labels above its main line
                .ticks((int) Math.ceil(chartWidth/X_TICK_WIDTH)) //determine number of ticks by dividing total width by width of each tick
                .tickFormat(new DatumFunction<String>() {
        
                    @Override
                    public String apply(Element context, Value d, int index) {
                        return TICK_FORMAT.apply((JsDate) d.as());
                    }
                })
                .innerTickSize(xAxisTickLength)
                .outerTickSize(xAxisLineThickness)
                .tickPadding(xAxisTextPadding);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Initializing playback");
        }
        
        //clamp the playback manager's current playback date in case it falls outside of the new time domain
        playbackManager.clampPlaybackDate();
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Finished initializing time domain");
        }
    }

    /**
     * Sets the history of events (i.e the timeline) that this chart should display and renders it. 
     * <br/><br/>
     * If this chart already has a history drawn to it, then the existing drawn elements will be 
     * either updated or removed to match the new history. New elements will be drawn for any history 
     * data that has not yet been drawn.
     * <br/><br/>
     * <b>Note:</b> Due to the rendering limitations of SVG elements, the rendered chart cannot
     * recalculate its size automatically based on the size of this widget, so the 
     * {@link #redraw()} method must be called to manually resize the chart when this widget's
     * size is changed.
     * 
     * @param history the history of events (i.e. the timeline) to populate the chart with. 
     * If null, the chart will be empty.
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart setHistory(TimelineHistory history) {
        
        timelineHistory = history != null 
                ? history 
                : TimelineHistory.create(DEFAULT_START_DATE, DEFAULT_END_DATE);
        
        refresh();
        
        return this;
    }
    
    /**
     * Sorts all of the events in the timeline so that they will be visually rendered in the correct order.
     * <br/><br/>
     * The rendering logic expects that all events it receives will be arranged from top to bottom in the data
     * structure that's passed in, so for things like nested events that should be grouped together visually, 
     * we need to sort them ahead of time so that they are processed in the correct visual order rather then
     * their original semantic order.
     * 
     * @return the same events sorted in the proper render order. Will not be null.
     */
    private Array<TimelineEvent> sortAllEvents(){
        
        final Array<TimelineEvent> topLevelEvents = timelineHistory.getEvents().filter(new ForEachCallback<Boolean>() {
            
            @Override
            public Boolean forEach(Object thisArg, Value element, int index, Array<?> array) {
                
                TimelineEvent currentEvent = element.<TimelineEvent>as();
                while(currentEvent.getParent() != null) {
                    
                    //filter out this event since it has a parent
                    return false;
                }
                
                if(currentEvent.isFixed()) {
                    
                    //this event is fixed, so it needs to be rendered separately
                    fixedEvents.push(currentEvent);
                    return false;
                }
                
                return true;
            }
        });
        
        return sortEvents(topLevelEvents);
    }
    
    /**
     * Sorts the given events so that they will be visually rendered in the correct order.
     * 
     * @return the same events sorted in the proper render order. Will not be null.
     */
    private Array<TimelineEvent> sortEvents(final Array<TimelineEvent> events){
        
        final Array<TimelineEvent> sortedEvents = Array.create();
        
        events.forEach(new ForEachCallback<Void>() {
            
            @Override
            public Void forEach(Object thisArg, Value element, int index, Array<?> array) {
                
                /* Add the parent event first */
                TimelineEvent currentEvent = element.<TimelineEvent>as();
                sortedEvents.push(currentEvent);
                
                /* If the parent event is not collapsed, place its children immediately below it */
                if(!currentEvent.isCollapsed()) {
                    
                    /* Sort each child's children */
                    Array<TimelineEvent> sortedChildren = sortEvents(currentEvent.getSubEvents());
                    sortedChildren.forEach(new ForEachCallback<Void>() {
                        
                        @Override
                        public Void forEach(Object thisArg, Value element, int index, Array<?> array) {
                            
                            /* Add each child's sorted children under its parent */
                            TimelineEvent childEvent = element.<TimelineEvent>as();
                            sortedEvents.push(childEvent);
                            
                            return null;
                        }
                    });
                }
                
                return null;
            }
        });
        
        return sortedEvents;
    }
    
    /**
     * Refreshes the history data that is currently being displayed by the chart and redraws its elements as needed. 
     * <br/><br/>
     * If this chart already has elements drawn to it, then the existing drawn elements will be 
     * either updated or removed to match the current history. New elements will be drawn for any history 
     * data that has not yet been drawn.
     * <br/><br/>
     * This method differs from {@link #redraw()} in the sense that redrawing does not insert or remove elements or
     * update properties that are independent of the chart's size, while refreshing does. Redrawing should generally
     * be preferred for more frequent minor changes to the chart, while refreshing should be used for changes
     * to the overall visual structure.
     */
    private void refresh() {
        
        fixedEvents = Array.create();
        
        //filter out any events that should be hidden because their parent events are collapsed
        scrollingEvents = sortAllEvents();
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Populating history for timeline chart");
        }

        //use the provided history to update the time domain used by the chart's scales and axes
        initTimeDomain();
        
        //draw the labels for the chart
        refreshLabels();
        
        /* if the panel containing the labels does not yet have a size, resize it to either the 
         * x position of the rightmost label OR 1/4 of the total chart size, depending on which
         * is smaller */
        int scrollPadding = labelScroller.getElement().getScrollHeight() > labelScroller.getElement().getOffsetHeight() ? 20 : 0;
        chartSplitter.setWidgetSize(labelLayout, Math.min(rightmostLabelX, getOffsetWidth()/4) + scrollPadding);
        
        /* Re-initialize the size of the x-axis. This is needed in order to properly have said axis account for the newly
         * calculated size of the labels.*/
        initAxes();
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing core chart elements");
        }
        
        Selection chartSvg = chartSelector.select("svg");
        if(chartSvg.empty()) {
            
            //draw the chart's SVG context if it has not already been drawn
            chartSvg = chartSelector.append("svg").attr("class", "chart");
        }
        
        //set the size of the chart's SVG context
        chartSvg.attr("width", chartWidth)
            .attr("height", getTotalChartHeight());
        
        Selection chart = chartSvg.select("g");
        if(chart.empty()) {
            
            /* draw the chart itself if it has not already been drawn and add handlers to update the
             * chart's playhead as needed as the mouse is moved over it */
            chart = chartSvg.append("g")
                .on(ExtendedBrowserEvents.POINTER_DOWN, new DatumFunction<Void>() {
    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        handleSeekPreview(ExtendedBrowserEvents.POINTER_DOWN, context);
                        return null;
                    }
                })
                .on(ExtendedBrowserEvents.POINTER_MOVE, new DatumFunction<Void>() {
    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        handleSeekPreview(ExtendedBrowserEvents.POINTER_MOVE, context);
                        return null;
                    }
                })
                .on(ExtendedBrowserEvents.POINTER_OUT, new DatumFunction<Void>() {
    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        handleSeekPreview(ExtendedBrowserEvents.POINTER_OUT, context);
                        return null;
                    }
                })
                .on(ExtendedBrowserEvents.POINTER_UP, new DatumFunction<Void>() {
    
                    @Override
                    public Void apply(Element context, Value d, int index) {
                        handleSeekPreview(ExtendedBrowserEvents.POINTER_UP, context);
                        return null;
                    }
                });
        }
        
        chartGroup = chart;
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing updated events");
        }
        
        //draw the scrolling events
        refreshEvents(chart, false);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing updated fixed events");
        }
        
        //draw the fixed events afterward so they appear on tip
        refreshEvents(chart, true);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing x axis");
        }
        
        Selection xAxisGroup = chartGroup.select(".x");
        if(xAxisGroup.empty()) {
            
            //draw the x axis if it has not already been drawn and set up its mouse interactions
            xAxisGroup = chart.append("g")
                .attr("class", "x axis")
                .attr("cursor", "pointer");
        }
        
        //redraw the x axis with its new dimensions
        xAxisGroup.attr("transform", "translate(0, " + (getXAxisBottom()) + ")")
            .call(xAxis);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing seek area");
        }
        
        Selection seekArea = xAxisGroup.select("." + SEEK_AREA);
        if(seekArea.empty()) {
            
            //draw an white rectangle behind the time axis so it is shown on top of intervals while scroling
            seekArea = xAxisGroup.insert("rect", ":first-child")
                .attr("class", SEEK_AREA)
                .attr("fill", "white"); //can't be "none" since that prevents mouse events      
        }
        
        //redraw the seek area with its new dimensions
        seekArea.attr("width", chartWidth)
            .attr("height", xAxisHeight)
            .attr("y", -xAxisHeight);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing playback");
        }
        
        Selection playbackLine = chart.select("." + PLAYBACK);
        if(playbackLine.empty()) {
            
            //draw a line to represent the current playback time
            playbackLine = chart.append("line")
                .attr("class", PLAYBACK)
                .attr("stroke", "blue")
                .attr("style", "pointer-events: none;")
                .attr("x2", 0)
                .style("transition-property", "transform")
                .style("transition-timing-function", "linear");
        }
        
        //redraw the playback components in case the scales changed
        playbackManager.redraw();
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Finished populating task data for timeline chart");
        }
    }
    
    /**
     * Refreshes the history data that is currently being displayed by an event area and redraws the rendered
     * event elements inside of it as needed. 
     * 
     * @param chart the selector where the event area is located inside. Cannot be null.
     * @param whether the scrolling event area should be refreshed or the fixed event area.
     */
    private void refreshEvents(Selection chart, boolean isFixedEvents) {
        
        String eventGroupClass = isFixedEvents ? FIXED_EVENTS : EVENTS;
        
        Selection background = chart.select("." + eventGroupClass);
        if(background.empty()) {
            
            //draw a group to contain the timeline events
            background = chart.append("g").attr("class", eventGroupClass);
        }
        
        UpdateSelection events = background.selectAll("." + EVENT).data(isFixedEvents ? fixedEvents : scrollingEvents);
        
        //erase events that have been removed
        events.exit().remove();
        
        //draw any new events
        Selection insertEvents = events.enter().insert("g", ":first-child")
            .attr("class", eventClass);
        
        addEventSelectionHandlers(insertEvents); //handle mouse selection on these events
        
        //add a rect to each new event element to behave as its clickable background
        insertEvents.append("rect")
            .attr("class", TICK_AREA)
            .attr("fill", "transparent");
        
        //add a group to each new event element to contain its intervals
        insertEvents.append("g").attr("class", INTERVALS);
        
        //redraw any existing events
        events.attr("class", eventClass)
            .attr("transform", eventTransform)
            .select("." + TICK_AREA)
                .attr("height", isFixedEvents ? fixedEventYScale.rangeBand() : scrollingEventYScale.rangeBand())
                .attr("width", chartWidth);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing updated intervals");
        }
        
        UpdateSelection intervals = events.select("." + INTERVALS).selectAll("." + INTERVAL).data(new DatumFunction<Array<EventInterval>>() {

            @Override
            public Array<EventInterval> apply(Element context, Value d, int index) {
                
                //for each event, populate the data model for its intervals group with all of the event's intervals
                TimelineEvent event = d.as();
                return event.getIntervals();
            }
            
        });

        //erase event intervals that have been removed
        intervals.exit().remove().each(new DatumFunction<Void>() {
                
            @Override
            public Void apply(Element context, Value d, int index) {
                
                //hide the tooltip for each interval that is removed
                hideTooltip(context);
                return null;
            }
        });

        //draw any new event intervals
        Selection insertIntervals = intervals.enter().append("g");
        
        //add a rectangle to show the length of time each interval spans
        insertIntervals.append("rect");
        
        //add some text to show icons for each interval
        insertIntervals.append("text")
            .attr("font-family", "FontAwesome") //needed to use Font Awesome icons
            .attr("dy", "0.32em")
            .attr("y", (isFixedEvents ? fixedEventYScale.rangeBand() : scrollingEventYScale.rangeBand())/2)
            .style("text-anchor", "middle");
        
        //redraw existing event intervals
        Selection existingIntervals = intervals
            .each(new DatumFunction<Void>() {
                @Override
                public Void apply(Element context, Value d, int index) {
                    final EventInterval eventInterval = d.<EventInterval>as();
                    final String tooltip = eventInterval.getTooltip();
                    if (StringUtils.isNotBlank(tooltip)) {
                        registerTooltip(context, d.<EventInterval>as().getTooltip(), tooltipContainer);
                    } else {
                        destroyTooltip(context);
                    }
                    return null;
                }
            })
            .attr("class", intervalClass)
            .attr("transform", intervalTransform)
            .style("cursor", new DatumFunction<String>() {
                
                @Override
                public String apply(Element context, Value d, int index) {
                    
                    //if an event interval has a click function, change the cursor to indicate it is clickable
                    IntervalClickHandler clickFunction = d.<EventInterval>as().getClickFunction();
                    if(clickFunction != null) {
                        return "pointer";
                    }
                    
                    return "default";
                }
            })
            .on(BrowserEvents.MOUSEOUT, new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    /* Hide the tooltip for each interval when the mouse exits it.
                     * 
                     * This should ONLY be done for mouse events instead of pointer events, 
                     * since touch screens always consider the "pointer" to be out*/
                    hideTooltip(context); 
                    return null;
                }
            })
            .on(BrowserEvents.CLICK, new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    //invoke each interval's associated click function when it is clicked on, if applicable
                    IntervalClickHandler clickFunction = d.<EventInterval>as().getClickFunction();
                    if(clickFunction != null) {
                        
                        JsDate playbackTime = xScale.invert(D3.mouseX(context)).asJsDate();
                        
                        clickFunction.onClick(context, d.<EventInterval>as(), index, (long) playbackTime.getTime());
                    }
                    
                    return null;
                }
            })
            .on(ExtendedBrowserEvents.POINTER_UP, new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    IntervalClickHandler clickFunction = d.<EventInterval>as().getClickFunction();
                    if(clickFunction != null) {
                        
                        JsDate playbackTime = xScale.invert(D3.mouseX(context)).asJsDate();
                        
                        if(!clickFunction.shouldPropagate(context, d.<EventInterval>as(), index, (long) playbackTime.getTime())) {
                            
                            //avoid propagating since the click handler has disabled it
                            D3.event().stopPropagation();
                        }
                    }
                    
                    return null;
                }
            });
        
        existingIntervals.select("rect")
            .attr("height", intervalRectHeight)
            .attr("width", intervalRectWidth);
        
        existingIntervals.select("text")
            .text(new DatumFunction<String>() {
    
                @Override
                public String apply(Element context, Value d, int index) {
                    return formatter.getIcon(d.<EventInterval>as());
                }
            })
            .attr("x", intervalTextX);
    }

    /**
     * Refreshes the history data that is currently being displayed by the labels and redraws their elements as needed. 
     * <br/><br/>
     * If this labels already has elements drawn to them, then the existing drawn elements will be 
     * either updated or removed to match the current history. New elements will be drawn for any history 
     * data that has not yet been drawn.
     */
    private void refreshLabels() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing core label elements");
        }
        
        Selection labelsSvg = labelsSelector.select("svg");
        if(labelsSvg.empty()) {
            
            //draw the labels's SVG context if it has not already been drawn
            labelsSvg = labelsSelector.append("svg").attr("class", "labels");
        }
        
        //set the size of the labels's SVG context
        labelsSvg.attr("height", getTotalChartHeight());
        
        Selection labels = labelsSvg.select("g");
        if(labels.empty()) {
            
            //draw the labels themselves if they have not already been drawn
            labels = labelsSvg.append("g");
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing updated events");
        }
        
        //refresh the scrolling events
        UpdateSelection labelsEvents = refreshLabelsEvents(labels, false);
        
        //refresh the fixed events afterwards so they appear on top
        UpdateSelection labelsFixedEvents = refreshLabelsEvents(labels, true);
        
        Selection xAxisGroup = labels.select(".x");
        if(xAxisGroup.empty()) {
            
            //draw the x axis if it has not already been drawn
            xAxisGroup = labels.append("g")
                .attr("class", "x")
                .attr("cursor", "pointer");
        }
        
        //redraw the x axis with its new dimensions
        xAxisGroup.attr("transform", "translate(0, " + (getXAxisBottom()) + ")");
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Drawing header area");
        }
        
        Selection seekArea = xAxisGroup.select("." + SEEK_AREA);
        if(seekArea.empty()) {
            
            //draw an white rectangle behind the header so it is shown on top of intervals while scroling
            seekArea = xAxisGroup.insert("rect", ":first-child")
                .attr("class", SEEK_AREA)
                .attr("fill", "white"); //can't be "none" since that prevents mouse events   
            
            xAxisGroup.append("path")
                .attr("d", "M0,-1V0H1554V-1");
        }
        
        repositionLabels(labelsSvg, labelsEvents, labelsFixedEvents);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Finished populating task data for timeline labels");
        }
    }

    private UpdateSelection refreshLabelsEvents(Selection labels, boolean isFixedEvents) {
        
        String eventGroupClass = isFixedEvents ? FIXED_EVENTS : EVENTS;
        
        Selection labelsBackground = labels.select("." + eventGroupClass);
        if(labelsBackground.empty()) {
            
            //draw a group to contain the timeline events
            labelsBackground = labels.append("g").attr("class", eventGroupClass);
        }
        
        UpdateSelection labelsEvents = labelsBackground.selectAll("." + EVENT).data(isFixedEvents ? fixedEvents : scrollingEvents);
        
        //erase events that have been removed
        labelsEvents.exit().remove();
        
        //draw any new events
        Selection insertLabelEvents = labelsEvents.enter().insert("g", ":first-child")
            .attr("class", eventClass)
            .style("cursor", "pointer")
            .on(BrowserEvents.CLICK, new DatumFunction<Void>() {
                
                @Override
                public Void apply(Element context, Value d, int index) {
                    
                    TimelineEvent event = d.<TimelineEvent>as();
                    
                    //toggle the event's collapse state and refresh to add/remove events visually as needed
                    event.setCollapsed(!event.isCollapsed());
                    refresh();
                    
                    return null;
                }
            });
        
        addEventSelectionHandlers(insertLabelEvents); //handle mouse selection on these events
        
        //add a rect to each new event element to behave as its clickable background
        insertLabelEvents.append("rect")
            .attr("class", TICK_AREA)
            .attr("fill", "transparent");
        
        //add a group to each new event element to contain its intervals
        insertLabelEvents.append("g").attr("class", INTERVALS);
        
        //add an expand/collapse button to each new event element to show the event's name
        insertLabelEvents.append("text")
            .attr("dy", "0.32em")
            .attr("dx", "-5")
            .attr("class", EVENT_BUTTON)
            .attr("font-family", "FontAwesome") //needed to use Font Awesome icons
            .style("text-anchor", "end");
        
        //add a label to each new event element to show the event's name
        insertLabelEvents.append("text")
            .attr("dy", "0.32em")
            .attr("class", EVENT_TEXT)
            .style("text-anchor", "start")
            
            /* Need to apply the yZoom here to ensure that the rightmost label X position is consistent
             * after both refreshes and redraws */
            .attr("font-size", (14 * yZoom) + "px");
        
        //redraw any existing events
        labelsEvents.attr("class", eventClass)
            .attr("transform", eventTransform)
            .select("." + TICK_AREA)
                .attr("height", isFixedEvents ? fixedEventYScale.rangeBand() : scrollingEventYScale.rangeBand())
                .attr("width", chartWidth);
        
        //redraws the button used to expand/collapse each event
        labelsEvents.select("." + EVENT_BUTTON)
            .attr("x", new DatumFunction<Double>() {

                @Override
                public Double apply(Element context, Value d, int index) {
                    
                    TimelineEvent event = d.as();
                    
                    //indent the event's name based on how many parents it has
                    double numParents = 0;
                    TimelineEvent currEvent = event.getParent();
                    while(currEvent != null) {
                        numParents++;
                        currEvent = currEvent.getParent();
                    }
                    
                    return LABEL_INITIAL_OFFSET + LABEL_NESTING_OFFSET * numParents;
                }
            })
            .attr("display", new DatumFunction<String>() {

                @Override
                public String apply(Element context, Value d, int index) {
                    
                    TimelineEvent event = d.as();
                    
                    if(event.getSubEvents().length() == 0) {
                        return "none"; //hide the expand/collapse button if this event has no sub events
                    }
                    
                    return null;
                }
            })
            .text(new DatumFunction<String>() {

                @Override
                public String apply(Element context, Value d, int index) {
                    
                    TimelineEvent event = d.<TimelineEvent>as();
                    
                    //show the appropriate icon depending on whether this event is expanded or collapsed
                    if(event.isCollapsed()) {
                        return COLLAPSED_ICON_UNICODE;
                        
                    } else {
                        return EXPANDED_ICON_UNICODE;
                    }
                }
            });
        
        //redraws the text for each event's label
        labelsEvents.select("." + EVENT_TEXT)
            .text(new DatumFunction<String>() {
    
                @Override
                public String apply(Element context, Value d, int index) {
                    
                    //display the event's name as the text for its label
                    return d.<TimelineEvent>as().getName();
                }
            });
        
        return labelsEvents;
    }

    /**
     * Redraws the existing history currently being shown by the chart to match any changes made to it and resizes
     * the chart's drawn elements to fill the size allocated to this widget. This can be used to update the 
     * layout and appearance of the chart without changing the history currently being shown.
     * <br/><br/>
     * If the history that is currently drawn to the chart have been modified since the last time it was drawn,
     * then its drawn elements will be redrawn to match their modifications. Similarly, if this widget's width and height have 
     * changed, the chart and all of its drawn elements will be resized to fill the new dimensions.
     * <br/><br/>
     * This method differs from {@link #refresh()} in the sense that redrawing does not insert or remove elements or
     * update properties that are independent of the chart's size, while refreshing does. Redrawing should generally
     * be preferred for more frequent minor changes to the chart, while refreshing should be used for changes
     * to the overall visual structure.
     * 
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart redraw() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing timeline chart");
        }

        //re-initialize the chart's axes to account for any changes to this widget's size
        initAxes();
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing core chart elements");
        }
        
        //adjust the size of the chart's SVG context
        Selection chart = chartSelector.select("svg")
            .attr("width", chartWidth)
            .attr("height", getTotalChartHeight())
            .select("g");
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing events");
        }

        //draw the scrolling events
        redrawEvents(chart, false);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing fixed events");
        }
        
        //draw the fixed events afterward so they are rendered on top
        redrawEvents(chart, true);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing x axis");
        }
        
        //redraw the x axis with its new dimensions
        chart.select(".x")
            .attr("transform", "translate(0, " + (getXAxisBottom()) + ")")
            .call(xAxis)
            .select("." + SEEK_AREA)
                .attr("width", chartWidth)
                .attr("height", xAxisHeight)
                .attr("y", -xAxisHeight);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing playback");
        }
        
        //redraw the playback line
        playbackManager.redraw();
        
        //redraw the labels associated with the chart
        redrawLabels();
        
        return this;
    }
    
    /**
     * Redraws the existing history currently being shown by the specified event area to match any changes 
     * made to it and resizes its drawn elements to fill the size allocated to this widget.
     * 
     * @param chart the chart where the event area resides
     * @param isFixedEvents whether the scrolling event area should be redrawn or the fixed event area.
     */
    private void redrawEvents(Selection chart, boolean isFixedEvents) {
        
        String eventGroupClass = isFixedEvents ? FIXED_EVENTS : EVENTS;
        
        Selection events = chart.select("." + eventGroupClass).selectAll("." + EVENT).attr("class", eventClass);

        //adjust the size of each timeline event in the chart
        events.attr("transform", eventTransform)
            .select("." + TICK_AREA)
                .attr("height", isFixedEvents ? fixedEventYScale.rangeBand() : scrollingEventYScale.rangeBand())
                .attr("width", chartWidth);
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing event intervals");
        }
        
        //adjust the size of the intervals within each timeline event
        Selection intervals = events.select("." + INTERVALS).selectAll("." + INTERVAL)
            .attr("transform", intervalTransform)
            .attr("class", intervalClass);

        intervals.each(new DatumFunction<Void>() {
            @Override
            public Void apply(Element context, Value d, int index) {
                final EventInterval eventInterval = d.<EventInterval>as();
                final String tooltip = eventInterval.getTooltip();
                if (StringUtils.isNotBlank(tooltip)) {
                    updateTooltip(context, tooltip);
                } else {
                    destroyTooltip(context);
                }
                return null;
            }
        });

        intervals.select("rect")
            .attr("height", intervalRectHeight)
            .attr("width", intervalRectWidth);
        
        intervals.select("text")
            .attr("y", (isFixedEvents ? fixedEventYScale.rangeBand() : scrollingEventYScale.rangeBand())/2)
            .attr("x", intervalTextX);
    }

    /**
     * Redraws the existing history currently being shown by the labels to match any changes made to it and resizes
     * the label area's drawn elements to fill the size allocated to this widget.
     */
    private void redrawLabels() {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing core labels elements");
        }
        
        //adjust the size of the labels's SVG context
        Selection labelsSvg = labelsSelector.select("svg")
            .attr("height", getTotalChartHeight());
        Selection labels = labelsSvg.select("g");
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info("Redrawing events");
        }

        Selection scrollingLabelsEvents = labels.select("." + EVENTS).selectAll("." + EVENT).attr("class", eventClass);
        Selection fixedLabelsEvents = labels.select("." + FIXED_EVENTS).selectAll("." + EVENT).attr("class", eventClass);

        repositionLabels(labelsSvg, scrollingLabelsEvents, fixedLabelsEvents);
    }
    
    /**
     * Sets the timeline event that is currently selected by the user and redraws the events
     * as needed to properly convey the selection state to the user. This is used to synchronize selections
     * between the chart and labels by handling the user's selection at the data level.
     * 
     * @param event the timeline event to select. If null, no timeline event will be selected.
     */
    private void setSelectedEvent(TimelineEvent event) {
        
        if(Objects.equals(selectedEvent, event)) {
            return; //selection isn't actually changing, so no need to update anything
        }
        
        //set the event that is currently selected
        final TimelineEvent oldEvent = selectedEvent;
        selectedEvent = event;
        
        //filter all events to determine which ones need to be redrawn
        DatumFunction<Element> eventFilter = new DatumFunction<Element>() {

            @Override
            public Element apply(Element context, Value d, int index) {
                
                TimelineEvent curEvent = d.<TimelineEvent>as();
                
                boolean isOld = oldEvent != null && oldEvent.equals(curEvent);
                boolean isSelected = selectedEvent != null && selectedEvent.equals(curEvent);
                
                if(isOld || isSelected) {
                    return context; //this event needs to be redrawn
                }
                
                return null; //this event does not need to be redrawn
            }
        };
        
        //redraw all events in the chart that either were selected or are currently selected
        chartSelector.select("svg").select("." + EVENTS).selectAll("." + EVENT).filter(eventFilter)
                .attr("class", eventClass); //CSS class applies selection styling
        
        //redraw all events in the labels that either were selected or are currently selected
        labelsSelector.select("svg").select("." + EVENTS).selectAll("." + EVENT).filter(eventFilter)
                .attr("class", eventClass); //CSS class applies selection styling
    }
    
    /**
     * Resets the positioning of the labels next to the chart. This both lines up the labels with their
     * associated row in the chart and also sets the width of the surrounding SVG area based on the
     * rightmost label so that the labels can be scrolled horizontally.
     * 
     * @param labelsSvg A selection for the SVG area containing all of the labels. Cannot be null.
     * @param labelsEvents A selection of all the label rows representing
     */
    private void repositionLabels(Selection labelsSvg, Selection scrollingLabelsEvents, Selection labelsFixedEvents) {
        
        double labelsWidth = labelLayout.getOffsetWidth();
        
        rightmostLabelX = 0;
        
        //reposition the scrolling events
        scrollingLabelsEvents.select("." + EVENT_BUTTON)
            .attr("y", scrollingEventYScale.rangeBand()/2)
            .attr("font-size", (12 * yZoom) + "px");
        scrollingLabelsEvents.select("." + EVENT_TEXT)
            .attr("x", intervalLabelX)
            .attr("y", scrollingEventYScale.rangeBand()/2)
            .attr("font-size", (14 * yZoom) + "px");
        
        //reposition the fixed events
        labelsFixedEvents.select("." + EVENT_BUTTON)
            .attr("y", fixedEventYScale.rangeBand()/2)
            .attr("font-size", (12 * yZoom) + "px");
        labelsFixedEvents.select("." + EVENT_TEXT)
            .attr("x", intervalLabelX)
            .attr("y", fixedEventYScale.rangeBand()/2)
            .attr("font-size", (14 * yZoom) + "px");
        
        double rowWidth = Math.max(rightmostLabelX, labelsWidth);
        
        //reposition the backgrounds for the scrolling events
        scrollingLabelsEvents.attr("transform", eventTransform)
        .select("." + TICK_AREA)
            .attr("height", scrollingEventYScale.rangeBand())
            .attr("width", rowWidth);
        
        //reposition the backgrounds for the fixed events
        labelsFixedEvents.attr("transform", eventTransform)
        .select("." + TICK_AREA)
            .attr("height", fixedEventYScale.rangeBand())
            .attr("width", rowWidth);
        
        //redraw the header with its new dimensions
        labelsSvg.select(".x")
            .attr("transform", "translate(0, " + (getXAxisBottom()) + ")")
            .select("." + SEEK_AREA)
                .attr("width", rowWidth)
                .attr("height", xAxisHeight)
                .attr("y", -xAxisHeight);
        
        labelsSvg.attr("width", rowWidth);
    }

    /**
     * Remove the event interval from the timeline chart.
     * 
     * @param eventIntervalToRemove the event interval to remove.
     */
    public void removeEventInterval(final EventInterval eventIntervalToRemove) {
        Selection chart = chartSelector.select("svg").attr("width", chartWidth).attr("height", getTotalChartHeight()).select("g");
        Selection events = chart.select("." + EVENTS).selectAll("." + EVENT);
        Selection eventElement = events.select("." + INTERVALS).selectAll("." + INTERVAL)
                .select(new DatumFunction<Element>() {
                    @Override
                    public Element apply(Element context, Value d, int index) {
                        EventInterval eInterval = d.<EventInterval>as();
                        if (eInterval.equals(eventIntervalToRemove)) {
                            return context;
                        }

                        return null;
                    }
                });

        if (!eventElement.empty()) {
            eventElement.remove();
        }
    }

    /**
     * Sets the formatter that this chart will use to style event data that it draws
     * 
     * @param formatter the formatter to use. If null, no styling will be applied to event data drawn by the chart
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart setFormatter(Formatter formatter) {
        this.formatter = formatter != null ? formatter : DEFAULT_FORMATTER;
        return this;
    }
    
    /**
     * Registers the given handler so that it can handle when the user performs interactions that
     * change the timeline's playback
     * 
     * @param handler the handler that should handle when the playback is modified. Can be null.
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart setPlaybackHandler(PlaybackHandler handler) {
        this.playbackHandler = handler;
        return this;
    }

    /**
     * Enables/Disables the {@link #playPauseButton}.
     * 
     * @param enable true to enable the button; false to disable it.
     */
    public void setPlayPauseButtonEnabled(boolean enable) {
        
        if(controls != null) {
            controls.getPlayControls().setPlayPauseEnabled(enable, playPauseDisabled);
        }
        
        this.playPauseDisabled = !enable;        

    }
    
    /**
     * Show any tooltips that should appear when loading the timeline for the first time.
     */
    public void showInitialTooltips(){
        if(controls != null) {
            controls.getPlayControls().showInitialTooltips();
        }
    }

    /**
     * Restarts the playback, moving the line displaying the playback time back to the beginning of the timeline
     * 
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart restart() {
        playbackManager.seek(timeDomainStart);
        return this;
    }
    
    /**
     * Plays the playback, causing the line displaying the playback time to start moving
     * 
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart play() {
        playbackManager.play();
        return this;
    }

    /**
     * Pauses the playback, causing the line displaying the playback time to
     * stop moving.
     * 
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart pause() {
        playbackManager.pause();
        return this;
    }

    /**
     * Return whether or not the timeline is paused.
     * 
     * @return true if the timeline is paused; false otherwise.
     */
    public boolean isPaused() {
        
        if(controls != null) {
            return controls.getPlayControls().isPaused();
        }
        
        return true;
    }

    /**
     * Clears the chart of the history of events (i.e the timeline) that it is currently displaying.
     * This is equivalent to passing an empty history to {@link #setHistory(TimelineHistory)}.
     * 
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart clear() {
        return setHistory(null);
    }
    
    /**
     * Notify the timeline that playback has been terminated.  
     * Currently resets the playback loop setting to the default (off)
     */
    public void terminatePlayback(){        
        
        //reset back to off
        if(controls != null) {
            controls.getPlayControls().setLooping(false);
        }
        
        loopEnabled = false;
    }
    
    /**
     * Sets the playback time, causing the line displaying the playback time to jump to that time
     * 
     * @param dateMillis the time to jump to (in epoch milliseconds)
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart seek(long dateMillis) {
        playbackManager.seek(JsDate.create(dateMillis));
        return this;
    }

    /**
     * Destroys the current tooltip on the element.
     * 
     * @param element the element to remove the tooltip.
     */
    private native void destroyTooltip(Element element)/*-{
		$wnd.jQuery(element).tooltip('destroy');
    }-*/;

    /**
     * Registers a JQuery tooltip for the given element that will appear whenever the mouse hovers over that element
     * 
     * @param element the element to register a tooltip for
     * @param title the text to show in the tooltip. If null, no tooltip will be displayed.
     * @param container the container that the tooltip should be appended to. When displaying tooltips for
     * SVG elements, this should be a nearby HTML element, since tooltips are HTML elements that cannot 
     * be appended within an SVG context.
     */
    private native void registerTooltip(Element element, String title, Element container)/*-{
		$wnd.jQuery(element).tooltip({
			placement : 'top',
			html : true,
			container : container
		})
		
		// For some reason, setting the title in the above call to .tooltip can cause the wrong
		// titles to show up for tooltip elements registered by this method. This problem does 
		// not occur when updating the tooltip text, so we can avoid it by updating the text here
		.attr('title', title).tooltip('fixTitle');
    }-*/;

    /**
     * Updates the tooltip for the given element.
     * 
     * @param element the element to update the tooltip for
     * @param title the new text to show in the tooltip. If null, no tooltip
     *        will be displayed.
     */
    private native void updateTooltip(Element element, String title)/*-{
		$wnd.jQuery(element).tooltip('hide').attr('title', title).tooltip(
				'fixTitle');
    }-*/;

    /**
     * Hides the tooltip that has been registered to the given element
     * 
     * @param element the element whose tooltip should be hidden
     */
    private native void hideTooltip(Element element)/*-{
        $wnd.jQuery(element).tooltip("hide");
    }-*/;
    
    /**
     * Sets the container element where the timeline's tooltips will be placed. This can be used to avoid cutting off
     * tooltips in the timeline due to overflow styling. If no container element is provided, the timeline's tooltips will
     * be restricted to appear only within the timeline area.
     * 
     * @param container the element to use as the container for tooltips. If null, the timeline chart itself will act
     * as the tooltip container.
     * @return the main chart widget. Can be used to chain method calls.
     */
    public TimelineChart setTooltipContainer(Element container) {
        this.tooltipContainer = container != null ? container : getElement();
        return this;
    }
    
    /**
     * Gets the current time of time timeline's playback, in the number of milliseconds that have passed
     * since the beginning of the history of events represented by this chart.
     * 
     * @return the current time of the playback, relative to its starting time
     */
    public double getRelativePlaybackTime() {
        return playbackManager.getRelativePlaybackTime();
    }
    
    /**
     * Applies the current scroll position to the chart and redraws it. This is mainly used to ensure that
     * elements that move with the scroll area maintain the same relative position, such as how the 
     * time scale stays at the top of the chart regardless of the scroll position.
     */
    private void applyScrollingToChart() {
        
        //keep track of how many pixels the chart has been scrolled vertically and/or horizontally
        topScrollOffset = chartScroller.getElement().getScrollTop();
        
        //redraw the chart to adjust the x and y positions of elements that move with the scroll area
        redraw();
    }
    
    /**
     * Gets the computed length of the text within the given text element
     * 
     * @param svgElement the SVG text element to compute the length of. Cannot be null.
     * @return the computed length of the element's rendered text
     */
    private static native double getComputedTextLength(Element svgTextElement)/*-{
        return svgTextElement.getComputedTextLength();
    }-*/;
    
    /**
     * Attaches mouse event listeners to the timeline events in the given selection in order to
     * properly track which event should be considered selected by the user.
     * 
     * @param eventSelection the selection containing the events that the event listeners
     * should be attached to. If null, this method will do nothing.
     */
    private void addEventSelectionHandlers(Selection eventSelection) {
        
        if(eventSelection == null) {
            return;
        }
        
        eventSelection.on(ExtendedBrowserEvents.POINTER_OVER, new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                //select this event when the mouse hovers over it
                setSelectedEvent(d.<TimelineEvent>as()); //need generic type or else GWT compiler fails with no error
                return null;
            }
            
        })
        .on(ExtendedBrowserEvents.POINTER_OUT, new DatumFunction<Void>() {
            
            @Override
            public Void apply(Element context, Value d, int index) {
                
                //deselect this event when the mouse leaves hovers over it
                setSelectedEvent(null);
                return null;
            }
        });
    }
    
    /**
     * Sets the UI elements used to control this timeline's visuals and playback. Once set, 
     * the given controls will invoke the proper interactions within this timeline when the
     * user interacts with them
     * 
     * @param controls the controls to set. Can be null, if the user should not be able
     * to control the timeline.
     */
    public void setControls(final TimelineControls controls) {
        
        if(this.controls != null) {
            this.controls.getPlayControls().setPlayPauseImpl(null);
            this.controls.getPlayControls().setLoopImpl(null);
            this.controls.getScaleControls().setZoomImpl(null);
            this.controls.getScaleControls().setTimescaleImpl(null);
        }
        
        this.controls = controls;
        
        if(controls != null) {
           
           //provide the logic for the play/pause control
           controls.getPlayControls().setPlayPauseImpl(new Command() {
            
                @Override
                public void execute() {
                    
                    if (playPauseDisabled) {
                        return;
                    }
    
                    //resume or pause the playback when the user clicks the appropriate button
                    if(controls.getPlayControls().isPaused()){
                        
                        if(playbackHandler != null) {
                            
                            //notify any handlers that the user changed the playback time
                            playbackHandler.onPlay((long) playbackManager.getDeadReckonedTime());
                        }
                        
                        playbackManager.play();
                        
                    } else {
                        
                        if(playbackHandler != null) {
                            
                            //notify any handlers that the user changed the playback time
                            playbackHandler.onPause((long) playbackManager.getDeadReckonedTime());
                        }
                        
                        playbackManager.pause();
                    }
                }
            });
           
           //provide the logic for the loop control
           controls.getPlayControls().setLoopImpl(new Command() {
            
                @Override
                public void execute() {
                    
                    loopEnabled = !loopEnabled;
                    
                    if(controls != null) {
                        controls.getPlayControls().setLooping(loopEnabled);
                    }
    
                }
            });
           
           //provide the logic for the timescale control
           controls.getScaleControls().setTimescaleImpl(new Command() {
               
               @Override
               public void execute() {
                   
                   Double scaleLevel = controls.getScaleControls().getTimescaleLevel();
                   
                   if(scaleLevel == null) {
                       return;
                   }
                   
                   //adjust the horizontal zoom level based on the value picked by the user from the slider
                   xZoom = scaleLevel;
                           
                   redraw(); //redraw to adjust the x positions of affected elements
               }
           });   
           
           //provide the logic for the zoom control
           controls.getScaleControls().setZoomImpl(new Command() {
               
               @Override
               public void execute() {
                   
                   Double zoomLevel = controls.getScaleControls().getZoomLevel();
                   
                   if(zoomLevel == null) {
                       return;
                   }
                   
                   //adjust the vertical zoom level based on the value picked by the user from the slider
                   yZoom = zoomLevel;
                           
                   redraw(); //redraw to adjust the y positions of affected elements
               }
           });
        }
    }
    
    /**
     * Gets the total calculated height of ALL the data elements rendered by the chart, including the height of
     * the X axis, the events that are fixed to the chart (i.e. non-scrolling events), and the events that scroll
     * within the chart. This is effectively the MAXIMUM size of the chart if no scrolling occurs (i.e. if the 
     * panel containing this widget chart is big enough to show the whole thing).
     * 
     * @return the total calculated height of the chart
     */
    private double getTotalChartHeight() {
        return xAxisHeight + fixedEventHeight + scrollEventHeight;
    }
    
    /**
     * Gets the Y coordinate corresponding to the bottom of where the X axis should be rendered. The X axis
     * is drawn from the bottom up, so this location acts as the origin for the X axis. Note that this value
     * will change as the user scrolls up/down in the chart, since the X needs to move with the current scroll
     * position so that it always appears at the same place visually.
     * 
     * @return the location of the bottom of the X axis
     */
    private double getXAxisBottom() {
        return xAxisHeight + topScrollOffset;
    }
    
    /**
     * Scrolls the chart so that the given date is visible within it. The timeline
     * will scroll in such a way so that a little of the history before and after the
     * date can be seen, rather than scrolling so that the date is at the left end or
     * the right end of the chart.
     * 
     * @param dateMillis the date to scroll to.
     */
    public void scrollToShow(long dateMillis) {
        
        /* 
         * We can't use context.scrollIntoView() here because SVG elements do not scroll natively. 
         * Instead, we need to calculate what time the playhead is at, convert that to an X positon, 
         * and scroll the chart to that x position.
         */
        final int xPos = xScale.apply(JsDate.create(dateMillis)).asInt();
        
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                /* Attempt to set the scroll position so that the date is roughly 1/3 from the left */
                int scrollViewWidth = chartScroller.getElement().getOffsetWidth();
                int totalScrollableWidth = chartScroller.getElement().getScrollWidth();
                int offset = scrollViewWidth/3;
                
                if(xPos > (totalScrollableWidth - (scrollViewWidth - offset))) {
                    
                    /* If the date is so close to the end of the timeline that scrolling would put it within
                     * the right 2/3 of the scrolled view, then don't apply the left offset, since this would
                     * actually end up scrolling the date slightly out of the scrolled view. */
                    scrollManager.smoothChartScroll(totalScrollableWidth, 300);
                    
                } else {
                    
                    /* Otherwise, apply the offset to the scroll position and scroll appropriately */
                    scrollManager.smoothChartScroll(xPos - offset, 300);
                }
            }
        });
    }
    
    /**
     * Gets the vertical scroll position of the chart, in pixels from the true top of the chart. This will
     * be some value between 0 and the true height of the chart, or 0 if there is no vertical scrollbar.
     * 
     * @return the vertical scroll position. Will always be 0 if there is no vertical scrollbar on the chart.
     */
    public int getScrollTop() {
        return chartScroller.getElement().getScrollTop();
    }
    
    /**
     * Sets the vertical scroll position of the chart, in pixels from the true top of the chart. If this value
     * is less than 0 or greater than the true height of the chart, it will be clamped to fit within the chart's
     * actual scroll size. This will have no effect if the chart does not have a vertical scrollbar.
     * 
     * @param topY the Y coordinate of the top of the scroll position.
     */
    public void setScrollTop(int topY) {
        
        chartScroller.getElement().setScrollTop(topY);
        
        onChartScroll();
    }
    
    /**
     * Handles when the chart SVG area is scrolled. This is mainly used to ensure that the scroll position of the
     * label area remains in sync with the chart area, and to ensure that fixed elements in the chart are moved
     * to the proper positions to keep them in view.
     */
    private void onChartScroll() {
        
        //sync label positions as the chart is scrolled
        labelScroller.getElement().setScrollTop(chartScroller.getElement().getScrollTop());
        
        applyScrollingToChart();
    }

    /**
     * A helper class used to determine how a timeline chart should style the data
     * that it draws
     * 
     * @author nroberts
     */
    public abstract static class Formatter {
        
        /**
         * Gets the CSS class name to assign to the given event interval
         * 
         * @param interval the event interval to get the styling for
         * @return the name of the CSS class to use. Can be null.
         */
        public String getCssClass(EventInterval interval) {
            return null;
        }
        
        public String getIcon(EventInterval interval) {
            return null;
        }
        
        public Boolean shouldCenterIcon(EventInterval interval) {
            return null;
        }
        
        /**
         * Gets the CSS class name to assign to the given timeline event
         * 
         * @param event the timeline event to get the styling for
         * @return the name of the CSS class to use. Can be null.
         */
        public String getCssClass(TimelineEvent event) {
            return null;
        }
        
    }
    
    /**
     * A handler used to handle when the user performs interactions that affect playback
     * 
     * @author nroberts
     */
    public interface PlaybackHandler{
        
        /**
         * Handles when the user performs a seek operation to jump to the given playback time
         * 
         * @param dateMillis the date of the seeked playback time, in milliseconds
         */
        public void onSeek(long dateMillis);
        
        /**
         * Handles when the user plays the playback at the given time
         * 
         * @param dateMillis the current playback time, in milliseconds
         */
        public void onPlay(long dateMillis);
        
        /**
         * Handles when the user pauses the playback at the given time
         * 
         * @param dateMillis the current playback time, in milliseconds
         */
        public void onPause(long dateMillis);
    }
    
    /**
     * A helper class used to manage the behavior and movement of the line that displays the current playback time
     * 
     * @author nroberts
     */
    private class PlaybackManager{
        
        /** 
         * The date that the playback has been explicitly set to. Note that this date likely will not match
         * the position of the blue playback line, since that line's location is interpolated using a 
         * dead reckoning algorithm and animated using CSS rather than being manually updated.
         */
        private JsDate playbackDate;
        
        /** 
         * The last time when the playback began playing. Used to determine how much time has passed in
         * order to perform dead reckoning.
         */
        private Long playStartMillis = null;
        
        /**
         * A timer used to periodically refresh the displayed playback time so that it updates as playback progresses
         */
        private Timer playbackTimeLabelRefreshTimer = new Timer() {

            @Override
            public void run() {
                refreshPlaybackTimeLabel();
            }
            
        };
        
        /**
         * Starts playing the playback from the current playback time and begins moving the playback line
         */
        public void play() {
            
            if(controls != null) {
                controls.getPlayControls().setPlaying(true);
            }
            
            if(playStartMillis == null) {
                playStartMillis = System.currentTimeMillis();
            }

            redraw();
            
            playbackTimeLabelRefreshTimer.scheduleRepeating(500);
        }
        
        /**
         * Pauses the playback at the current playback time and stops moving the playback line
         */
        public void pause() {
            
            if(controls != null) {
                controls.getPlayControls().setPlaying(false);
            }
            
            if(playStartMillis != null) {
                playbackDate = JsDate.create(playbackDate.getTime() + (System.currentTimeMillis() - playStartMillis));
                clampPlaybackDate();
            }
            
            playStartMillis = null;
            redraw();
            
            playbackTimeLabelRefreshTimer.cancel();
        }
        
        /**
         * Redraws the playback line at the appropriate time
         */
        private void redraw() {
            
            final Selection playbackLine = chartSelector.select("svg").select("g").select("." + PLAYBACK)
                .attr("y2", getTotalChartHeight());
            
            if(playStartMillis != null) {
                
                //playback is playing, so calculate where the playback line should be based on how much time has passed
                final JsDate deadReckonPlaybackDate = JsDate.create(getDeadReckonedTime());
                
                AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
                    
                    @Override
                    public void execute(double timestamp) {
                        /* Break the animation if the play start time was set to
                         * null before the animation completed */
                        if (playStartMillis == null) {
                            return;
                        }

                        //position the playback line according to the dead reckoned playback time
                        playbackLine.style("transition-duration", "initial")
                            .style("transform", "translate(" + xScale.apply(deadReckonPlaybackDate).asDouble() + "px, 0px)");
                        
                        //animation scheduler is need to allow the playback line to instantly jump to the current playback time
                        AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
                            
                            @Override
                            public void execute(double timestamp) {
                                /* Break the animation if the play start time
                                 * was set to null before the animation
                                 * completed */
                                if (playStartMillis == null) {
                                    return;
                                }

                                //perform an animation that interpolates the position of the line to the end of the timeline
                                playbackLine.style("transition-duration", (int) (timeDomainEnd.getTime() - deadReckonPlaybackDate.getTime()) + "ms")
                                    .style("transform", "translate(" + xScale.apply(timeDomainEnd).asDouble() + "px, 0px)");
                            }
                        });
                
                    }
                });
                
            } else {
                
                //animation scheduler is needed in case a seek call comes in while stopping the playback animation
                AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
                    
                    @Override
                    public void execute(double timestamp) {
                        /* Break the animation if the play start time was
                         * populated before the animation completed */
                        if (playStartMillis != null) {
                            return;
                        }

                        //playback is paused, so position the playback line according to the current playback time
                        playbackLine.style("transition-duration", "initial")
                            .style("transform", "translate(" + xScale.apply(playbackDate).asDouble() + "px, 0px)");
                    }
                });
            }
            
            refreshPlaybackTimeLabel();
        }
        
        /**
         * Gets the dead-reckoned time of the playback based on the last time it was set to and, if
         * it is currently playing, how much time has passed since it last started playing.
         * 
         * @return the current dead-reckoned playback time
         */
        private double getDeadReckonedTime() {
            return playbackDate.getTime() + (playStartMillis != null 
                    ? System.currentTimeMillis() - playStartMillis
                    : 0);
        }

        /**
         * If needed, adjusts the current playback time so that it fits within the chart's time scale.
         */
        public void clampPlaybackDate() {
            
            if(playbackDate == null || playbackDate.getTime() < timeDomainStart.getTime() ) {
                playbackDate = timeDomainStart;
                
            } else if(playbackDate.getTime() > timeDomainEnd.getTime()) {
                playbackDate = timeDomainEnd;
            }
        }

        /**
         * Seeks the given date, which immediately moves the playback line to the position corresponding to that date
         * 
         * @param date the date to seek to. If null, this method will do nothing.
         */
        private void seek(JsDate date) {
            
            if(date == null) {
                return;
            }
            
            playbackDate = date;
            clampPlaybackDate();
            
            if(playStartMillis != null) {
                playStartMillis = null; //reset the time that playing was started
                play();
                
            } else {
                redraw();
            }
        }
        
        /**
         * Refreshes the label displaying the current playback time so that it actually reflects the current time of the playback
         */
        private void refreshPlaybackTimeLabel() {
            
            if(controls != null) {
                controls.refreshPlaybackTimeLabel(PLAYBACK_TIME_LABEL_FORMAT.format(new Date((long) getDeadReckonedTime())));
            }
        }
        
        /**
         * Gets the current time of time timeline's playback, in the number of milliseconds that have passed
         * since the beginning of the history of events represented by this chart.
         * 
         * @return the current time of the playback, relative to its starting time
         */
        public double getRelativePlaybackTime() {
            return getDeadReckonedTime() - timeDomainStart.getTime();
        }
    }
    
    /**
     * Handles the given mouse event and updates the position of the seek preview line accordingly
     * with respect to the given context element. This is used both to dynamically update the rendered
     * location of the preview line shown before seeking as the mouse is moved and 
     * to perform the seek itself when the mouse is clicked.
     * 
     * @param mouseEvent the mouse event being handled. If null, this method will do nothing.
     * @param context the element that the mouse event should be handled with respect to. Used to
     * determine the relative mouse position. Can be null when handling a mouse out event, since 
     * the mouse location is unneeded in that case.
     */
    private void handleSeekPreview(String mouseEvent, Element context) {
        
        if(mouseEvent == null || D3.event() == null) {
            return;
        }
        
        Selection seekPreviewLine;
        
        switch(mouseEvent) {
            case ExtendedBrowserEvents.POINTER_DOWN:
                
                /*
                 * NOTE: Only create preview line on mouse down so user doesn't accidentally seek
                 * by releasing the mouse button while dragging a slider from the toolbar
                 */
                
                seekPreviewLine = chartGroup.select("." + SEEK);
                if(seekPreviewLine.empty()) {
                    
                    //draw a preview line to represent the time position that the mouse is hovering over
                    seekPreviewLine = chartGroup.append("line")
                        .attr("x2", 0)
                        .attr("stroke", "black")
                        .attr("pointer-events", "none"); //mouse events get interrupted if preview line accepts them
                }
                  
                //update the time position of the preview line
                seekPreviewLine.attr("transform", "translate(" + D3.mouseX(context) + ", 0)")
                    .attr("class", SEEK + " " + SEEK_ACTIVE)
                    .attr("y2", getTotalChartHeight());
                return;
            
            case ExtendedBrowserEvents.POINTER_MOVE:
                
                seekPreviewLine = chartGroup.select("." + SEEK);
                if(seekPreviewLine.empty()) {
                    
                    //draw a preview line to represent the time position that the mouse is hovering over
                    seekPreviewLine = chartGroup.append("line")
                        .attr("class", SEEK)
                        .attr("x2", 0)
                        .attr("stroke", "black")
                        .attr("pointer-events", "none"); //mouse events get interrupted if preview line accepts them
                }
                  
                //update the time position of the preview line
                seekPreviewLine.attr("transform", "translate(" + D3.mouseX(context) + ", 0)")
                    .attr("y2", getTotalChartHeight());
                return;
                
            case ExtendedBrowserEvents.POINTER_OUT:
                
                //erase the seek preview line
                chartGroup.select("." + SEEK).remove();
                return;
                
            case ExtendedBrowserEvents.POINTER_UP:
                
                seekPreviewLine = chartGroup.select("." + SEEK);
                if(seekPreviewLine.empty() || !seekPreviewLine.attr("class").contains(SEEK_ACTIVE)) {
                    return;
                }
                
                //retrieve the time corresponding to the mouse's x position over the time scale
                JsDate playbackTime = xScale.invert(D3.mouseX(context)).asJsDate();
                
                //seek the playback to that time
                playbackManager.seek(playbackTime);
                
                if(playbackHandler != null) {
                    
                    //notify any handlers that the user changed the playback time
                    playbackHandler.onSeek((long) playbackTime.getTime());
                }
                
                return;
        }
    }
    
    /**
     * A helper class that manages scrolling operations that involve the chart elements.
     * 
     * @author nroberts
     */
    private class ScrollManager{
        
        /** The time when the latest scroll action started, in milliseconds */
        private Long scrollStartMs = null;
        
        /** The curren scrolling animation that's running, if any */
        private AnimationHandle currentAnimation = null;
        
        /**
         * Scrolls the chart to the given position and, if needed, animates it over the given duration.
         * 
         * @param scrollPos the position to scroll to, in pixels
         * @param durationMs the duration of the scrolling animation
         */
        private void smoothChartScroll(int scrollPos, final int durationMs) {
            
            final int startScrollPos = chartScroller.getElement().getScrollLeft();
            final int scrollPosDelta = scrollPos - startScrollPos;
            scrollStartMs = null;
            
            if(currentAnimation != null) {
                currentAnimation.cancel(); //cancel the animation currently in progress
            }
            
            currentAnimation = AnimationScheduler.get().requestAnimationFrame(new AnimationCallback() {
                
                @Override
                public void execute(double timestamp) {
                    
                    if(scrollStartMs == null) {
                        scrollStartMs = (long) timestamp; //initialize the start of the animation time
                    }
                    
                    //calculate the percentage that the scroll position should move based on the time
                    long ellapsedMs = (long)timestamp - scrollStartMs;
                    double posPercent = Math.min(
                            (double)ellapsedMs/(double)durationMs,  //percent of time complete
                            1                                       //max of 100% complete
                    );
                    
                    //move the current scroll position based on the calculated percentage
                    chartScroller.getElement().setScrollLeft(startScrollPos + (int)(scrollPosDelta*posPercent));
                    
                    if(ellapsedMs < durationMs) {
                        
                        //schedule the next frame of animation
                        currentAnimation = AnimationScheduler.get().requestAnimationFrame(this);
                    }
                }
            });
            
        }
    }
}
