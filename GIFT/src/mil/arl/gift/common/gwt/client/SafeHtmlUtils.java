/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A utility class that contains various useful methods for building safe html strings.
 * 
 * @author sharrison
 */
public class SafeHtmlUtils {
    /**
     * Surrounds the text value with bold tags.
     * 
     * @param value the value to bold.
     * @return the safe html.
     */
    public static SafeHtml bold(String value) {
        return bold(com.google.gwt.safehtml.shared.SafeHtmlUtils.fromString(value));
    }

    /**
     * Surrounds the text value with bold tags.
     * 
     * @param value the value to bold.
     * @return the safe html.
     */
    public static SafeHtml bold(SafeHtml value) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<b>").append(value).appendHtmlConstant("</b>");
        return sb.toSafeHtml();
    }

    /**
     * Surrounds the text value a font tag and specifies the provided color.
     * 
     * @param value the value to color.
     * @param color the color string.
     * @return the safe html.
     */
    public static SafeHtml color(String value, String color) {
        return color(com.google.gwt.safehtml.shared.SafeHtmlUtils.fromString(value), color);
    }

    /**
     * Surrounds the text value a font tag and specifies the provided color.
     * 
     * @param value the value to color.
     * @param color the color string.
     * @return the safe html.
     */
    public static SafeHtml color(SafeHtml value, String color) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<span style=\"color:").appendEscaped(color).appendHtmlConstant("\">").append(value)
                .appendHtmlConstant("</span>");
        return sb.toSafeHtml();
    }
}
