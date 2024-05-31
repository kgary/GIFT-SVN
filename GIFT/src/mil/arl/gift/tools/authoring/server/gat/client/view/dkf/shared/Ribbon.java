/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Caption;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.ThumbnailPanel;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * This class easily develops ribbons in the UI that have clickable buttons.
 *
 * @author sharrison
 */
public class Ribbon extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(Ribbon.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static RibbonUiBinder uiBinder = GWT.create(RibbonUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface RibbonUiBinder extends UiBinder<Widget, Ribbon> {
    }

    /** Interface to allow CSS style name access */
    public interface Style extends CssResource {

        /** @return The style for the panel surrounding the thumbnail panel */
        String thumbnailFlowPanel();

        /** @return The style for the thumbnail panel */
        String thumbnailPanel();

        /** @return The style for the border around the icon */
        String iconBorder();

        /** @return The style for tile caption */
        String tileCaption();

        /** @return The style for the caption itself */
        String caption();

        /** @return The style for the tile add button */
        String addButtonWidth();
        
        /** @return the style for the label shown when there are no tiles in the ribbon */
        String noTileLabel();
    }

    /** The css Styles declared in the ui.xml */
    @UiField
    protected Style style;

    /** The panel that all the ribbon items will be added to */
    @UiField
    protected FlowPanel ribbonPanel;

    /** The default tile height */
    private static final double DEFAULT_TILE_HEIGHT = 125;

    /** The tile height used for the ribbon tiles */
    private double tileHeight = DEFAULT_TILE_HEIGHT;
    
    /** The default tile width */
    private static final double DEFAULT_TILE_WIDTH = 125;

    /** The tile width used for the ribbon tiles */
    private double tileWidth = DEFAULT_TILE_WIDTH;

    /** The unique set of 'add' buttons. Stored for read-only purposes. */
    private final Set<Button> addButtons = new HashSet<>();
    
    /** an optional label to show when there are no tiles in the ribbon */
    private Label noTileMessageLabel = null;

    /**
     * Constructor
     */
    public Ribbon() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Set the tile height for items in the ribbon.
     *
     * @param tileHeight height in PX, if less than zero the value is not used.
     */
    public void setTileHeight(double tileHeight) {

        if (tileHeight > 0) {
            this.tileHeight = tileHeight;
        }
    }
    
    /**
     * Set the tile width for items in the ribbon.
     *
     * @param tileWidth width in PX, if less than zero the value is not used.
     */
    public void setTileWidth(double tileWidth) {

        if (tileWidth > 0) {
            this.tileWidth = tileWidth;
        }
    }
    
    /**
     * Show a label as the only item in the ribbon.  This is useful for when the ribbon is empty which can
     * happen when exploring the authoring UI.  For example, the author is creating a PowerPoint DKF and
     * has reached the place of interest coordinate type picker.  PowerPoint doesn't support coordinates so the
     * ribbon would be empty.
     *  
     * @param message the message to show in the label in the ribbon.  Can't be null or empty.
     */
    public void setNoRibbonItemMessage(String message){
        
        if(StringUtils.isBlank(message)){
            logger.severe("Unable to add the no ribbon item message because the provided message is empty/null.");
        }else if(ribbonPanel.getWidgetCount() > 1){
            logger.severe("Unable to add the no ribbon item message because there are items in the ribbon.");
        }else if(noTileMessageLabel == null && ribbonPanel.getWidgetCount() == 0){
            //create and add the label as the only item
            noTileMessageLabel = new Label();
            noTileMessageLabel.setStyleName(style.noTileLabel());
            ribbonPanel.add(noTileMessageLabel);
        }
        
        if(noTileMessageLabel != null){
            //update the label with the text provided
            noTileMessageLabel.setHTML(message);   
        }
    }

    /**
     * Adds a new ribbon item to the panel.
     *
     * @param iconType creates the icon that will be displayed using this type.
     * @param label the label under the icon
     * @param tooltip the tooltip that is displayed when hovering over the ribbon item.
     * @param clickHandler the 'add' click handler when the add button is selected for a ribbon
     *        item.
     * @return the thumbnail panel that was created and added to the ribbon
     */
    public Widget addRibbonItem(IconType iconType, String label, String tooltip, ClickHandler clickHandler) {
        if (iconType == null) {
            throw new IllegalArgumentException("The parameter 'iconType' cannot be null.");
        } else if (clickHandler == null) {
            throw new IllegalArgumentException("The parameter 'clickHandler' cannot be null.");
        }

        Icon icon = new Icon(iconType);
        return addRibbonItem(icon, label, tooltip, clickHandler);
    }

    /**
     * Adds a new ribbon item to the panel.
     *
     * @param icon the icon that will be displayed
     * @param label the label under the icon
     * @param tooltip the tooltip that is displayed when hovering over the ribbon item.
     * @param clickHandler the 'add' click handler when the add button is selected for a ribbon
     *        item.
     * @return the thumbnail panel that was created and added to the ribbon
     */
    public Widget addRibbonItem(Widget icon, String label, String tooltip, ClickHandler clickHandler) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(icon, label, tooltip, clickHandler);
            logger.fine("addRibbonItem(" + StringUtils.join(", ", params) + ")");
        }

        if (icon == null) {
            throw new IllegalArgumentException("The parameter 'icon' cannot be null.");
        } else if (clickHandler == null) {
            throw new IllegalArgumentException("The parameter 'clickHandler' cannot be null.");
        }

        // make sure the icon is of the correct size
        if (icon instanceof Icon) {
            ((Icon) icon).setSize(IconSize.TIMES2);
        }

        Widget thumbnailPanel = generateNewThumbnailPanel(icon, label, tooltip, clickHandler);
        
        ribbonPanel.add(thumbnailPanel);
        
        //remove the now message label if it was set
        if(noTileMessageLabel != null){
            ribbonPanel.remove(noTileMessageLabel);
            noTileMessageLabel = null;
        }

        return thumbnailPanel;
    }
    
    /**
     * Returns the widget for the only choice in this ribbon if there is only one choice 
     * in the ribbon and that item is not the no ribbon item message label.
     * 
     * @return the widget for the only choice in this ribbon if there is only one item to 
     * choose from in the ribbon.  Returns null if there is zero or more than one choice 
     * or if the only items is the no ribbon item message label.
     */
    public Widget containsOneChoice(){
        
        Widget oneWidget = null;
        Iterator<Widget> itr = ribbonPanel.iterator();
        while(itr.hasNext()){
            Widget candidate = itr.next();
            if(candidate.getElement().getStyle().getDisplay().equalsIgnoreCase(Display.NONE.name())){
                //found a hidden widget in the ribbon, ignore it
                continue;
            }else if(oneWidget == null && (noTileMessageLabel == null || candidate != noTileMessageLabel) ){
                //found the first displayed widget that isn't the no tile message label in the ribbon
                oneWidget = candidate;
            }else{
                //already found a displayed widget in the ribbon
                return null;
            }                    
        }
        
        return oneWidget;

    }

    /**
     * Generates a styled panel for the new ribbon item.
     *
     * @param image the icon or image that will be displayed
     * @param label the label under the icon
     * @param tooltip the tooltip that is displayed when hovering over the ribbon item.
     * @param clickHandler the 'add' click handler when the add button is selected for a ribbon
     *        item.
     * @return the styled panel containing the new ribbon item.
     */
    private Widget generateNewThumbnailPanel(Widget image, String label, String tooltip, ClickHandler clickHandler) {

        FlowPanel iconBorder = new FlowPanel();
        iconBorder.addStyleName(style.iconBorder());
        iconBorder.add(image);

        Heading heading = new Heading(HeadingSize.H3);
        heading.setText(label);
        heading.addStyleName(style.caption());

        Caption caption = new Caption();
        caption.add(heading);
        caption.addStyleName(style.tileCaption());

        Button addButton = new Button("Add", clickHandler);
        addButton.setType(ButtonType.SUCCESS);
        addButton.addStyleName(style.addButtonWidth());
        addButton.addStyleName("surveySelectButtonStyle");
        addButtons.add(addButton);

        Container buttonContainer = new Container();
        buttonContainer.addStyleName("surveySelectHover");
        buttonContainer.addStyleName("surveySelectStyle");
        buttonContainer.add(addButton);

        Tooltip buttonTooltip = new Tooltip();
        buttonTooltip.setTitle(tooltip);
        buttonTooltip.setPlacement(Placement.BOTTOM);
        buttonTooltip.add(buttonContainer);

        ThumbnailPanel thumbnailPanel = new ThumbnailPanel();
        thumbnailPanel.addStyleName(style.thumbnailPanel());
        thumbnailPanel.getElement().getStyle().setHeight(tileHeight, Unit.PX);
        thumbnailPanel.getElement().getStyle().setWidth(tileWidth, Unit.PX);
        thumbnailPanel.add(iconBorder);
        thumbnailPanel.add(caption);
        thumbnailPanel.add(buttonTooltip);

        FlowPanel panelContainer = new FlowPanel();
        panelContainer.addStyleName(style.thumbnailFlowPanel());
        panelContainer.add(thumbnailPanel);

        return panelContainer;
    }

    /**
     * Clears the ribbon of all widgets.
     */
    public void clearRibbon() {
        ribbonPanel.clear();
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     *
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        for (Button button : addButtons) {
            button.setEnabled(!isReadonly);
        }
    }
}
