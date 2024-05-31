/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiChild;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A panel similar in design and function to ScrollPanel that supports having a header widget with an unknown, possibly-dynamic height. This 
 * widget was implemented to deal with a longstanding issue with implementing scrolling in HTML interfaces where the ordinary restrictions made 
 * it impossible to fill a container with a header using an unknown size and a scrollable area below it without hackishly modifying sizes 
 * directly. With this widget, a header can be made any size, and the scrollable area below will automatically resize to fill the remaining area
 * of this widget. Scrollable area must be all together in one containter, such as a FlowPanel<br/>
 * <br/>
 * This widget uses UiBinder and can be used in a ui.xml like so:<br/>
 * <br/>
 * &lt;c:DynamicHeaderScrollPanel width='100%' height='100% '&gt;<br/>
 * <span style='padding-left:30px;'/>    &lt;c:header&gt;<br/>
 * <span style='padding-left:60px;'/>&lt;!-- Header widget goes here --&gt; <br/>       
 * <span style='padding-left:30px;'/>&lt;/c:header&gt; <br/>
 * <span style='padding-left:30px;'/>&lt;c:scrollableContent&gt; <br/>
 * <span style='padding-left:60px;'/>&lt;!-- Scrollable content widget goes here --&gt; <br/>
 * <span style='padding-left:30px;'/>&lt;/c:scrollableContent&gt; <br/>
 * &lt;/c:DynamicHeaderScrollPanel&gt; <br/>
 * 
 * @author nroberts
 */
public class DynamicHeaderScrollPanel extends Composite implements RequiresResize, ProvidesResize{

    private static DynamicHeaderScrollPanelUiBinder uiBinder = GWT
            .create(DynamicHeaderScrollPanelUiBinder.class);

    interface DynamicHeaderScrollPanelUiBinder extends
            UiBinder<Widget, DynamicHeaderScrollPanel> {
    }
    
    @UiField
    protected SimplePanel northContainer;
    
    @UiField
    protected SimplePanel centerContainer;
    
    @UiField
    protected SimplePanel southContainer;
    
    @UiField
    protected HTMLPanel centerContainerCell;
    
    @UiField
    protected HTMLPanel scrollableContainerCell;

    private Widget north;

    private Widget center;
    
    private Widget south;
    
    /**
     * Creates an empty scrollable area with no header
     */
    public DynamicHeaderScrollPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    /** 
     * Creates a scrollable area containing the given content widget with the specified header widget and the given footer widget
     * 
     * @param header the widget to use for the header
     * @param scrollableContent the widget containing the content to be scrolled
     * @param footer the widget to use for the header
     */
    public DynamicHeaderScrollPanel(Widget header, Widget scrollableContent, Widget footer) {
        this(); 
        
        setHeader(header);
        setScrollableContent(scrollableContent);
        setFooter(footer);
    }

    /** 
     * Gets the widget being used as the header
     * 
     * @return the widget being used as the header
     */
    public Widget getHeader() {
        return north;
    }

    /**
     * Sets the widget to use as the header
     * 
     * @param header the widget to use as the header
     */
    @UiChild(tagname="header")
    public void setHeader(Widget header) {
        this.north = header;
        northContainer.setWidget(header);
    }

    /**
     * Gets the widget containing the content being scrolled
     * 
     * @return the widget containing the content being scrolled
     */
    public Widget getScrollableContent() {
        return center;
    }

    /**
     * Sets the widget containing the content to be scrolled
     * 
     * @param scrollableContent the widget containing the content to be scrolled
     */
    @UiChild(tagname="scrollableContent", limit=1)
    public void setScrollableContent(Widget scrollableContent) {
        this.center = scrollableContent;
        centerContainer.setWidget(scrollableContent);
    }
    
    /** 
     * Gets the widget being used as the footer
     * 
     * @return the widget being used as the footer
     */
    public Widget getFooter() {
        return south;
    }

    /**
     * Sets the widget to use as the footer
     * 
     * @param footer the widget to use as the footer
     */
    @UiChild(tagname="footer", limit=1)
    public void setFooter(Widget footer) {
        this.south = footer;
        southContainer.setWidget(footer);
    }
    
    @Override
    public void onResize() {
        if (center instanceof RequiresResize) {
          ((RequiresResize) center).onResize();
        }
    }
    
    /** 
     * Gets the north widget 
     * 
     * @return the north widget
     */
    public Widget getNorth() {
        return north;
    }

    /**
     * Sets the north widget
     * 
     * @param north the north widget
     */
    @UiChild(tagname="north")
    public void setNorth(Widget north) {
        this.north = north;
        northContainer.setWidget(north);
    }
    
    /** 
     * Gets the south widget 
     * 
     * @return the south widget
     */
    public Widget getSouth() {
        return south;
    }

    /**
     * Sets the south widget
     * 
     * @param south the south widget
     */
    @UiChild(tagname="south", limit=1)
    public void setSouth(Widget south) {
        this.south = south;
        southContainer.setWidget(south);
    }
    
    /** 
     * Gets the center widget 
     * 
     * @return the center widget
     */
    public Widget getCenter() {
        return center;
    }

    /**
     * Sets the center widget
     * 
     * @param center the center widget
     */
    @UiChild(tagname="center", limit=1)
    public void setCenter(Widget center) {
        this.center = center;
        centerContainer.setWidget(center);
    }
    
    /**
     * Adds a style name to the center container
     * 
     * @param style The style name to add
     */
    public void setAddCenterStyleName(String style) {
        centerContainerCell.addStyleName(style);
    }

    /**
     * Scrolls the scrollable area to the top.
     */
    public void scrollToTop() {
        scrollableContainerCell.getElement().setScrollTop(0);
    }
    
    /**
     * Scrolls the scrollable area to the left.
     */
    public void scrollToBegin(){
        scrollableContainerCell.getElement().setScrollLeft(0);
    }
}
