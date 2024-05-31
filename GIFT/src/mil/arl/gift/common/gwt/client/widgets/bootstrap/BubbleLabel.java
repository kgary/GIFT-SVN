/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.InlineHTML;

/**
 * Class to apply a specific 'bubble' styling around an {@link InlineHTML}.
 * 
 * @author tflowers
 */
public class BubbleLabel extends InlineHTML {

    /**
     * Constructor
     */
    public BubbleLabel() {
        Style style = getElement().getStyle();
        style.setDisplay(Display.INLINE_BLOCK);

        /* Style the border */
        style.setBorderColor("#000000");
        style.setBorderWidth(1, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);
        style.setProperty("borderRadius", "8px");

        /* Style the spacing of the widget */
        style.setProperty("padding", "2px 8px");
        style.setProperty("margin", "0px 4px");
    }

    /**
     * Constructor
     * 
     * @param html the string to use to populate the {@link InlineHTML}
     */
    public BubbleLabel(String html) {
        this();
        setHTML(html);
    }

    /**
     * Constructor
     * 
     * @param html the {@link SafeHtml} to use to populate the {@link InlineHTML}
     */
    public BubbleLabel(SafeHtml html) {
        this();
        setHTML(html);
    }
}
