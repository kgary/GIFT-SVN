/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.HideEvent;
import org.gwtbootstrap3.client.shared.event.HideHandler;
import org.gwtbootstrap3.client.shared.event.ShowEvent;
import org.gwtbootstrap3.client.shared.event.ShowHandler;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.ChangeCallback;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.map.client.draw.MilitarySymbol.Status;

/**
 * The state pane collapsible section for the state pane. Contains a header and
 * a list group pane.
 * 
 * @author sharrison
 * @param <T> the type of object that is used as an identifier for each
 *        {@link ListGroupItem}.
 */
public class StatePaneSection<T extends Object> extends Composite {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StatePaneSection.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StatePaneSectionUiBinder uiBinder = GWT.create(StatePaneSectionUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StatePaneSectionUiBinder extends UiBinder<Widget, StatePaneSection<?>> {
    }

    /**
     * A style name for hovering over a list item. Should only be applied if
     * they are selectable.
     */
    private static final String HOVER_STYLE = "hoverStyle";

    /**
     * A style name for highlighting an entity in green to indicate an at expectation
     * assessment
     */
    private static final String AT_EXPECTATION_STYLE = "atExpectation";
    
    /**
     * A style name for highlighting an entity in green to indicate an above expectation
     * assessment
     */
    private static final String ABOVE_EXPECTATION_STYLE = "aboveExpectation";

    /**
     * A style name for highlighting an entity in red to indicate a negative
     * assessment
     */
    private static final String BELOW_EXPECTATION_STYLE = "belowExpectation";

    /** The section header */
    @UiField
    protected PanelHeader header;

    /** The label for the section {@link #header} */
    @UiField
    protected Heading headerLabel;
    
    /** The text for the section's header label */
    @UiField
    protected Label headerLabelText;
    
    /** 
     * A panel next to the header label's text where widgets like buttons can be
     * added to the header 
     */
    @UiField
    protected FlowPanel headerLabelBtnPanel;

    /** The collapsible pane that contains the section's content */
    @UiField
    protected Collapse collapse;

    /** The header text for the section */
    private String headerText;

    /** The label when the {@link #collapse} has no content */
    private final Label noContentLabel = new Label("This section is empty.");

    /** Optional callback for when the selection has changed */
    private final ChangeCallback<T> selectionCallback;

    /**
     * Indicates whether or not the items in the list are selectable. This is
     * determined by the {@link #selectionCallback}.
     */
    private final boolean isSelectable;

    /**
     * Indicates whether or not the items in the list are deselectable (e.g. go
     * back to a nothing-selected state).
     */
    private final boolean isDeselectable;

    /** The check mark icon to indicate which team member is selected */
    private final Icon selectedTeamMemberIcon = new Icon(IconType.CHECK);

    /**
     * The list group for the {@link #collapse} pane. This will contain the
     * items in the panel.
     */
    private final ListGroup listGroup = new ListGroup();

    /**
     * A mapping of the identifier class to its associated {@link ListGroupItem}
     */
    private final Map<T, ListGroupItem> itemIdentifierMap = new HashMap<>();

    /** The selected item. Can be null if selections are disabled. */
    private T selectedItem;

    /** The custom comparator for the identifier */
    private Comparator<T> comparator;

    /** Handler registration used to detect when text within this section's header needs to be repositioned */
    private HandlerRegistration resizeHandler;

    /**
     * Constructor. Items added to the section made by this constructor will not
     * be selectable.
     * 
     * @param headerText The default header text for this section. Can't be
     *        blank. Can be changed later.
     */
    @UiConstructor
    public StatePaneSection(final String headerText) {
        this(headerText, null);
    }

    /**
     * Constructor.
     * 
     * @param headerText The default header text for this section. Can't be
     *        blank. Can be changed later.
     * @param selectionCallback the optional callback for when a selection
     *        changes. If null, no selections will be possible.
     */
    public StatePaneSection(final String headerText, final ChangeCallback<T> selectionCallback) {
        this(headerText, false, selectionCallback);
    }

    /**
     * Constructor.
     * 
     * @param headerText The default header text for this section. Can't be
     *        blank. Can be changed later.
     * @param isDeselectable true to allow the user to deselect an item in the
     *        list and return to a nothing-selected state; false to prevent
     *        this.
     * @param selectionCallback the optional callback for when a selection
     *        changes. If null, no selections will be possible.
     */
    public StatePaneSection(final String headerText, boolean isDeselectable,
            final ChangeCallback<T> selectionCallback) {
        if (StringUtils.isBlank(headerText)) {
            throw new IllegalArgumentException("The parameter 'headerText' cannot be blank.");
        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        /* Callback can be null */
        this.selectionCallback = selectionCallback;
        this.isDeselectable = isDeselectable;
        this.isSelectable = selectionCallback != null;
        if (isSelectable) {
            listGroup.addStyleName(HOVER_STYLE);
        }

        initWidget(uiBinder.createAndBindUi(this));

        setHeaderText(headerText);

        /* Revert the label cursor back to the default pointer */
        noContentLabel.getElement().getStyle().setCursor(Cursor.DEFAULT);

        /* Toggle the collapse on header click */
        header.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                collapse.toggle();
            }
        }, ClickEvent.getType());

        /* Set the flex property on this section to 1 if shown; and default if
         * hidden */
        collapse.addHideHandler(new HideHandler() {
            @Override
            public void onHide(HideEvent hideEvent) {
                StatePaneSection.this.getElement().getStyle().clearProperty("flex");
            }
        });
        collapse.addShowHandler(new ShowHandler() {
            @Override
            public void onShow(ShowEvent showEvent) {
                StatePaneSection.this.getElement().getStyle().setProperty("flex", "1");
            }
        });

        /* Style the check mark icon */
        selectedTeamMemberIcon.getElement().getStyle().setPosition(Position.ABSOLUTE);
        selectedTeamMemberIcon.getElement().getStyle().setLeft(0, Unit.PX);
        selectedTeamMemberIcon.getElement().getStyle().setTop(12, Unit.PX);

        /* Init panel */
        collapse.add(noContentLabel);
    }

    /**
     * Set a custom comparator for the identifier.
     * 
     * @param comparator the comparator to set.
     */
    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * Get the current header label text.
     * 
     * @return the current header label text.
     */
    public String getHeaderText() {
        return headerLabelText.getText();
    }

    /**
     * Sets the text to be the header for this section.
     * 
     * @param text the text to display as the header. If blank, nothing will
     *        happen.
     */
    public void setHeaderText(String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        this.headerText = text;
        headerLabelText.setText(text);
    }

    /**
     * Set an additional widget for the section header.
     * 
     * @param widget the widget to add. If null, nothing will be added.
     * @param resetHeader true to remove any existing widgets and reset the
     *        header back to just it's text; false to append a widget to the
     *        existing header.
     */
    public void addHeaderWidget(Widget widget, boolean resetHeader) {
        /* Clear header label and rebuild with widget */
        if (resetHeader) {
            
            headerLabelBtnPanel.clear();
            setHeaderText(headerText);
            
            if(resizeHandler != null) {
                
                /* Stop listening for window resize events */
                resizeHandler.removeHandler();
                resizeHandler = null;
            }
        }

        if (widget != null) {
            
            /* Add the widget to the button panel */
            headerLabelBtnPanel.add(widget);
            
            if(resizeHandler == null) {
                
                /* Register to listen for window resize events so that the position of the
                 * header elements can be readjusted when a resize occurs */
                resizeHandler = Window.addResizeHandler(new ResizeHandler() {
                    
                    @Override
                    public void onResize(ResizeEvent event) {
                        adjustHeaderPosition();
                    }
                });
            }
            
            /* Since a new widget was added to the header, need to readjust the positions
             * of the header elements */
            adjustHeaderPosition();
        }
    }

    /**
     * Adjusts the position of the header text and any buttons or other widgets next to it so that
     * they are properly readable at different window resolutions. This is used to ensure that the
     * header text remains centered as much as possible and to properly wrap text when it exceeds
     * the width of the header rather than overlapping text on top of itself
     */
    private void adjustHeaderPosition() {
        
        /* Calculate the current width of the elements inside the header label. */
        int currentWidth = headerLabelText.getOffsetWidth() + headerLabelBtnPanel.getOffsetWidth();
        int maxWidth = headerLabel.getOffsetWidth();
        
        if(currentWidth > maxWidth) {
            
            /* The button panel is large enough to trigger a wrap, so we don't need to offset the label */
            headerLabelText.getElement().getStyle().clearProperty("marginLeft");
            
        } else if(currentWidth + headerLabelBtnPanel.getOffsetWidth() > maxWidth) {
                
            /* The button panel is large enough to trigger a wrap, but only if we offset the label
             * by the width of the button panel. This means that the max width is wide enough for both the 
             * label and the button panel, but we can't center it with equal spacing on both sides. In 
             * this case, we simply want the label to be positioned as far to the right as possible
             * without wrapping. */
            headerLabelText.getElement().getStyle().setProperty("marginLeft", (maxWidth - currentWidth) + "px");
            
        } else {
            
            /* The button panel is not large enough to trigger a wrap, so we need to offset the label by the
             * size of the button panel. This is needed to make sure the label text remains visually centered.*/
            headerLabelText.getElement().getStyle().setProperty("marginLeft", headerLabelBtnPanel.getOffsetWidth() + "px");
        }
    }
    
    @Override
    protected void onDetach() {
        super.onDetach();
        
        if(resizeHandler != null) {
            
            /* Stop listening for window resize events */
            resizeHandler.removeHandler();
            resizeHandler = null;
        }
    }

    /**
     * Set the text for the label when there is no content in the section.
     * 
     * @param text the text to display when the section is empty. If blank,
     *        nothing will happen.
     */
    public void setEmptyLabelText(String text) {
        if (StringUtils.isBlank(text)) {
            return;
        }

        noContentLabel.setText(text);
    }

    /**
     * Manually show or hide the collapse panel.
     * 
     * @param open true to show/expand the collapse panel; false to hide it.
     */
    public void openCollapse(boolean open) {
        if (open) {
            collapse.show();
        } else {
            collapse.hide();
        }
    }

    /**
     * Clears all content and displays the {@link #noContentLabel}.
     */
    public void clear() {
        collapse.clear();
        listGroup.clear();
        itemIdentifierMap.clear();
        if (selectionCallback != null && selectedItem != null) {
            selectionCallback.onChange(null, selectedItem);
        }
        selectedItem = null;
        collapse.add(noContentLabel);
    }

    /**
     * Adds the HTML as a new {@link ListGroupItem} in the section. Gets
     * appended to the bottom.
     * 
     * @param html the HTML to add. If null, nothing will be added.
     * @param identifier the unique identifier for this {@link ListGroupItem}.
     *        If null or is already linked to an item in this list, nothing will
     *        be added.
     * @return the {@link ListGroupItem} that was added to this list. Can be
     *         null if nothing was added.
     */
    public ListGroupItem add(final SafeHtml html, final T identifier) {
        if (html == null || identifier == null) {
            return null;
        } else if (hasIdentifier(identifier)) {
            logger.warning("This section already contains an identifier " + identifier);
            return null;
        }

        final ListGroupItem listGroupItem = new ListGroupItem();
        listGroupItem.setHTML(html.asString());
        return add(listGroupItem, identifier);
    }

    /**
     * Adds a widget as a new {@link ListGroupItem} in the section. Gets
     * appended to the bottom.
     * 
     * @param w the widget to add. If null, nothing will be added.
     * @param identifier the unique identifier for this {@link ListGroupItem}.
     *        If null or is already linked to an item in this list, nothing will
     *        be added.
     * @return the {@link ListGroupItem} that was added to this list. Can be
     *         null if nothing was added.
     */
    public ListGroupItem add(final Widget w, final T identifier) {
        if (w == null || identifier == null) {
            return null;
        } else if (hasIdentifier(identifier)) {
            logger.warning("This section already contains an identifier " + identifier);
            return null;
        }

        final ListGroupItem listGroupItem = new ListGroupItem();
        listGroupItem.add(w);
        return add(listGroupItem, identifier);
    }

    /**
     * Adds a new {@link ListGroupItem} in the section. Gets appended to the
     * bottom.
     * 
     * @param listGroupItem the item to add. If null, nothing will be added.
     * @param identifier the unique identifier for this {@link ListGroupItem}.
     *        If null or is already linked to an item in this list, nothing will
     *        be added.
     * @return the {@link ListGroupItem} that was added to this list. Can be
     *         null if nothing was added.
     */
    public ListGroupItem add(final ListGroupItem listGroupItem, final T identifier) {
        if (listGroupItem == null || identifier == null) {
            return null;
        } else if (hasIdentifier(identifier)) {
            logger.warning("This section already contains an identifier " + identifier);
            return null;
        }

        /* Add new identifier-group item combo to the map */
        itemIdentifierMap.put(identifier, listGroupItem);

        /* If clickable */
        if (isSelectable) {
            listGroupItem.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    setSelectedItem(identifier);
                }
            }, ClickEvent.getType());
            listGroupItem.getElement().getStyle().setCursor(Cursor.POINTER);
        }

        /* Add to the list */
        listGroup.add(listGroupItem);

        /* Remove the label and add the list group */
        if (collapse.getWidgetIndex(noContentLabel) != -1) {
            collapse.remove(noContentLabel);
            collapse.add(listGroup);
        }

        return listGroupItem;
    }

    /**
     * Removes the {@link ListGroupItem} found by the identifier.
     * 
     * @param identifier the unique identifier for this {@link ListGroupItem}.
     *        If null or unknown, nothing will be removed.
     */
    public void remove(T identifier) {
        remove(getItemByIdentifier(identifier));
    }

    /**
     * Removes the {@link ListGroupItem} from this section.
     * 
     * @param listGroupItem the item to remove from this list. If null or
     *        unknown, nothing will be removed.
     */
    public void remove(ListGroupItem listGroupItem) {
        if (listGroupItem == null) {
            return;
        }

        listGroup.remove(listGroupItem);
    }

    /**
     * Checks if this section is using the provider identifier.
     * 
     * @param identifier the identifier to check.
     * @return true if this section is using the identifier for a list item;
     *         false otherwise.
     */
    public boolean hasIdentifier(T identifier) {
        return getItemByIdentifier(identifier) != null;
    }

    /**
     * Retrieve the {@link ListGroupItem} by its unique identifier.
     * 
     * @param identifier the unique identifier by which to determine the
     *        {@link ListGroupItem} to retrieve.
     * @return the item from the list that is identified by the provided object.
     *         Can be null if the item is not found.
     */
    public ListGroupItem getItemByIdentifier(T identifier) {
        if (identifier == null) {
            return null;
        } else if (comparator == null) {
            return itemIdentifierMap.get(identifier);
        }

        for (T mapId : itemIdentifierMap.keySet()) {
            if (comparator.compare(identifier, mapId) == 0) {
                return itemIdentifierMap.get(mapId);
            }
        }

        return null;
    }

    /**
     * The set of identifiers in the list.
     * 
     * @return the set of identifiers.
     */
    public Set<T> getIdentifiers() {
        return itemIdentifierMap.keySet();
    }

    /**
     * Retrieve the identifier of the selected list item.
     * 
     * @return the item from the list that is selected. Can be null if no
     *         selection was made or if the items aren't selectable.
     */
    public T getSelectedItem() {
        return isSelectable ? selectedItem : null;
    }

    /**
     * Set the selected item in the list.
     * 
     * @param identifier the identifier of one of the items in this list to
     *        select. If null or doesn't match the section's set of identifiers,
     *        nothing will happen.
     */
    public void setSelectedItem(T identifier) {
        if (!hasIdentifier(identifier)) {
            return;
        }

        final T previousSelection = selectedItem;
        final boolean isSameAsPrevious = comparator == null ? previousSelection == identifier
                : comparator.compare(previousSelection, identifier) == 0;

        if (isDeselectable && isSameAsPrevious) {
            /* Deselect */
            selectedItem = null;
        } else if (comparator != null) {
            /* Set the original identifier as the selected */
            for (T mapId : itemIdentifierMap.keySet()) {
                if (comparator.compare(identifier, mapId) == 0) {
                    selectedItem = mapId;
                    break;
                }
            }
        } else {
            selectedItem = identifier;
        }

        if (isSelectable) {
            selectedTeamMemberIcon.removeFromParent();
            final ListGroupItem selectedListGroupItem = getItemByIdentifier(selectedItem);
            if (selectedListGroupItem != null) {
                selectedListGroupItem.insert(selectedTeamMemberIcon, 0);
            }
            selectionCallback.onChange(selectedItem, previousSelection);
        }
    }

    /**
     * Get the number of items in the list.
     * 
     * @return the number of items in the list.
     */
    public int size() {
        return listGroup.getWidgetCount();
    }

    /**
     * Update the item's background color based on the provided assessment
     * status.
     * 
     * @param identifier the identifier of one of the items in this list to
     *        select. If null or doesn't match the section's set of identifiers,
     *        nothing will happen.
     * @param assessmentStatus the new assessment status for the list item. If
     *        null, nothing will happen.
     */
    public void updateItemAssessmentState(T identifier, Status assessmentStatus) {
        if (!hasIdentifier(identifier) || assessmentStatus == null) {
            return;
        }

        AssessmentLevelEnum assessment;
        switch (assessmentStatus) {
        case ANTICIPATED: /* intentional drop-through */
        case PRESENT_FULL_TO_CAPACITY: /* intentional drop-through */
        case PRESENT_FULLY_CAPABLE:
            assessment = AssessmentLevelEnum.AT_EXPECTATION;
            break;
        case PRESENT_DAMAGED: /* intentional drop-through */
        case PRESENT_DESTROYED:
            assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
            break;
        case PRESENT: /* intentional drop-through */
        default:
            assessment = AssessmentLevelEnum.UNKNOWN;
        }

        updateItemAssessmentState(identifier, assessment);
    }

    /**
     * Update the item's background color based on the provided assessment
     * status.
     * 
     * @param identifier the identifier of one of the items in this list to
     *        select. If null or doesn't match the section's set of identifiers,
     *        nothing will happen.
     * @param assessmentStatus the new assessment status for the list item. If
     *        null, nothing will happen.
     */
    public void updateItemAssessmentState(T identifier, AssessmentLevelEnum assessmentStatus) {
        if (!hasIdentifier(identifier) || assessmentStatus == null) {
            return;
        }

        final ListGroupItem listGroupItem = getItemByIdentifier(identifier);

        /* Clear existing styles */
        listGroupItem.removeStyleName(AT_EXPECTATION_STYLE);
        listGroupItem.removeStyleName(ABOVE_EXPECTATION_STYLE);
        listGroupItem.removeStyleName(BELOW_EXPECTATION_STYLE);

        if (AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessmentStatus)) {
            listGroupItem.addStyleName(BELOW_EXPECTATION_STYLE);
        } else if (AssessmentLevelEnum.AT_EXPECTATION.equals(assessmentStatus)) {
            listGroupItem.addStyleName(AT_EXPECTATION_STYLE);
        } else if(AssessmentLevelEnum.ABOVE_EXPECTATION.equals(assessmentStatus)) {
            listGroupItem.addStyleName(ABOVE_EXPECTATION_STYLE);
        }

    }
}
