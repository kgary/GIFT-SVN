/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents a panel that contains a collection of items that can be smoothly
 * updated in real-time.
 *
 * @author tflowers
 * @param <T> The type of elements that are reported in the
 *        {@link PriorityPanel}
 * @param <U> The widget associated with the backing data object of type T
 */
public class PriorityPanel<T, U extends Widget> extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(PriorityPanel.class.getName());

    /** The binder that combines the java class with the ui.xml */
    private static PriorityPanelUiBinder uiBinder = GWT.create(PriorityPanelUiBinder.class);

    /** Defines the binder that combines this java class with the ui.xml */
    interface PriorityPanelUiBinder extends UiBinder<Widget, PriorityPanel<?, ?>> {
    }

    /**
     * Defines an interface that builds a widget from an object of type T
     *
     * @param <T> The type of element from which the widget should be built.
     * @param <U> The widget associated with the backing data object of type T.
     */
    public static interface WidgetBuilder<T, U extends Widget> {
        /**
         * Builds a widget from a given element.
         *
         * @param element The element from which to build the element. Can't be
         *        null.
         * @return The {@link Widget} that was constructed. Can't be null.
         */
        U buildWidget(T element);
    }

    /** The bubble indicating that there are pending items. */
    @UiField
    protected Button pendingBubble;

    /** The panel that contains each feed item */
    @UiField
    protected FlowPanel itemsPanel;
    
    /** The deck containing the main panel and the label shown when it is empty */
    @UiField
    protected DeckPanel mainDeck;
    
    /** The main panel containing the items and the pending button */
    @UiField
    protected Widget mainPanel;
    
    /** A label shown when the main panel doesn't contain any items */
    @UiField
    protected Label emptyLabel;

    /**
     * The backing collection that is represented by each individual widget in
     * the {@link PriorityPanel}.
     */
    private final List<T> items = new ArrayList<>();

    /** Holds pending items when feed is paused */
    private final Queue<T> pendingItems = new LinkedList<>();

    /** The widget builder that is used to transform backing */
    private final WidgetBuilder<T, U> widgetBuilder;

    /** The comparator that decides how to sort the items in the feed */
    private final Comparator<T> itemComparator;

    /** Indicates whether the feed should be updated in real time */
    private boolean isLive = true;

    /**
     * Constructs a new {@link PriorityPanel}.
     *
     * @param widgetBuilder The {@link WidgetBuilder} that should be used to
     *        construct widgets from elements of the {@link PriorityPanel}.
     * @param itemComparator The {@link Comparator} that is used to sort
     *        elements within the feed.
     */
    public PriorityPanel(WidgetBuilder<T, U> widgetBuilder, Comparator<T> itemComparator) {
        if (widgetBuilder == null) {
            throw new IllegalArgumentException("The parameter 'widgetBuilder' cannot be null.");
        }

        if (itemComparator == null) {
            throw new IllegalArgumentException("The parameter 'itemComparator' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        this.widgetBuilder = widgetBuilder;
        this.itemComparator = itemComparator;
        
        //show the empty label by default
        mainDeck.showWidget(mainDeck.getWidgetIndex(emptyLabel));
    }

    /**
     * Handler that adds all the currently pending items and then hides the
     * pending bubble.
     *
     * @param event The {@link ClickEvent} describing the click. Can't be null.
     */
    @UiHandler("pendingBubble")
    protected void onPendingBubbleClicked(ClickEvent event) {
        addPendingItems();
    }

    /**
     * Used to set whether or not the {@link PriorityPanel}.
     *
     * @param isLive true to set to live mode; false to set to pause mode.
     */
    public void setLiveMode(boolean isLive) {
        this.isLive = isLive;

        /* If switching to live mode. Insert all pending items */
        if (isLive) {
            addPendingItems();
        }
    }

    /**
     * Called to add all items from the {@link #pendingItems} {@link Queue} to
     * the {@link PriorityPanel} interface.
     */
    private void addPendingItems() {
        while (!pendingItems.isEmpty()) {
            addLive(pendingItems.poll());
        }

        pendingBubble.setVisible(false);
        
        updateItemVisibility();
    }

    /**
     * Adds a new element to the {@link PriorityPanel}.
     *
     * @param element The element to add to the feed.
     */
    public void add(T element) {
        if (isLive) {
            addLive(element);
        } else {
            addPaused(element);
        }
    }

    /**
     * Adds multiple T elements to the {@link PriorityPanel}.
     *
     * @param elements The {@link Collection} of T to add to the
     *        {@link PriorityPanel}. Can't be null. An empty collection performs
     *        no operation.
     */
    public void addAll(Collection<? extends T> elements) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addAll(" + elements + ")");
        }

        if (elements == null) {
            throw new IllegalArgumentException("The parameter 'elements' cannot be null.");
        }

        for (T element : elements) {
            add(element);
        }
    }

    /**
     * Adds a new element to the {@link PriorityPanel} using live mode behavior.
     *
     * @param element The element to add.
     */
    private void addLive(T element) {
        int i = 0;
        while (i < size() && itemComparator.compare(element, items.get(i)) > 0) {
            i++;
        }

        /* Add the item to the collection */
        items.add(i, element);

        /* Add the item to the panel */
        Widget widget = widgetBuilder.buildWidget(element);
        itemsPanel.insert(widget, i);
        
        updateItemVisibility();
    }

    /**
     * Adds a new element to the {@link PriorityPanel} using paused mode
     * behavior.
     *
     * @param element The element to add.
     */
    private void addPaused(T element) {
        pendingItems.add(element);

        int pendingCount = pendingItems.size();
        StringBuilder sb = new StringBuilder()
                .append(pendingCount)
                .append(" new item")
                .append((pendingCount == 1 ? "" : "s"));
        pendingBubble.setText(sb.toString());
        pendingBubble.setVisible(true);
        
        updateItemVisibility();
    }

    /**
     * Removes a given element from the {@link PriorityPanel}.
     *
     * @param element The element to remove.
     * @return True if the element was successfully removed, false if the
     *         element was not contained in the {@link PriorityPanel}.
     */
    public boolean remove(T element) {
        int i = items.indexOf(element);

        /* An invalid index was returned, therefore the element does exist,
         * failed to remove */
        if (i < 0) {
            return false;
        }

        items.remove(i);
        itemsPanel.remove(i);
        
        updateItemVisibility();
        
        return true;
    }
    
    /**
     * Checks if the {@link PriorityPanel} is empty
     * 
     * @return True if the {@link PriorityPanel} contains 0 elements
     */
    public boolean isEmpty() {
        int size = size();
        return size == 0;
    }

    /**
     * Gets the size of the {@link PriorityPanel}
     * 
     * @return the number of elements in the {@link PriorityPanel} 
     */
    public int size() {
        return items.size();
    }

    /**
     * Gets the widget in the {@link PriorityPanel} associated with the backing
     * data object of type T. Unchecked warning suppressed because the only
     * methods that add to the {@link PriorityPanel} list add objects that
     * extend type T so this is a safe cast.
     * 
     * @param object The data object of type T that backs the widget in the
     *        items panel
     * @return the widget that is modeled from the object
     */
    @SuppressWarnings("unchecked")
    public U getWidget(T object) {
        int index = items.indexOf(object);

        if (index != -1 && index < itemsPanel.getWidgetCount()) {
            return (U) itemsPanel.getWidget(index);
        }

        return null;
    }

    /**
     * Retrieves an unmodifiable list of items in the panel.
     * 
     * @return an unmodifiable list of items in the panel.
     */
    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Retrieves a copy of the list of backing widgets in the panel.
     * 
     * @return a copy of the list of backing widgets in the panel.
     */
    public List<U> getWidgetItems() {
        List<U> widgetList = new ArrayList<>();

        for (T item : items) {
            U widget = getWidget(item);
            if (widget != null) {
                widgetList.add(widget);
            }
        }

        return widgetList;
    }

    /**
     * Clears all items from the {@link PriorityPanel}
     */
    public void clear() {
        items.clear();
        itemsPanel.clear();
        pendingItems.clear();
        pendingBubble.setVisible(false);
        
        updateItemVisibility();
    }
    
    /**
     * Checks to see if there are any items to display to the user and shows them if there are. If there are no items to shown
     * then a label indicating such will be shown instead.
     */
    private void updateItemVisibility() {
        
        if(items.isEmpty() && pendingItems.isEmpty()) {
            mainDeck.showWidget(mainDeck.getWidgetIndex(emptyLabel)); //no items available, so show the empty label
            
        } else {
            mainDeck.showWidget(mainDeck.getWidgetIndex(mainPanel)); //items are available, so show them
        }
    }
    
    /**
     * Sets the text for the label that is displayed when there are no items to show
     * 
     * @param text the text to display
     */
    public void setEmptyLabel(String text) {
        emptyLabel.setText(text);
    }
}