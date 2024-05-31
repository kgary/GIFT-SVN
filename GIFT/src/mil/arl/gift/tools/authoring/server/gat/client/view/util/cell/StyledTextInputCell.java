/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.cell;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Text input cell that can have a style applied to it
 * 
 * @author nblomberg
 *
 */
public class StyledTextInputCell extends TextInputCell{
    
    /** The style(s) that are applied to the input field.  This value appended between the class="...." properties so multiple style can be applied.  */
    private String inputStyle = "";
    
    /**
     * Constructor
     * 
     * @param styleName - String containing the style that is applied to the text input field.  The value gets appended to the class="...." property, so multiple styles can be applied to this string.  Must not be null.
     */
    public StyledTextInputCell(String styleName) {
        inputStyle = styleName;
    }
    
    /**
     * Accessor to set the style for the TextInputCell
     * 
     * @param styleName - String containing the style that is applied to the text input field.  The value gets appended to the class="...." property, so multiple styles can be applied to this string.  Must not be null.
     */
    public void setStyle(String styleName) {
        inputStyle = styleName;
    }
    
    /**
     * Accessor to retrieve the style for the TextInputCell
     * 
     * @return - String containing the style that is applied to the text input field.  Can return an empty string if not set.
     */
    public String getStyle() {
        return inputStyle;
    }
    
    @Override
    public void render(Cell.Context context, String value, SafeHtmlBuilder sb) {
        
        if (value != null) {
        	
        	sb.appendHtmlConstant("<input type='text' value='");
        	sb.appendEscaped(value);
        	sb.appendHtmlConstant("' tabindex='-1' class='" + inputStyle + "'>");
        	
        } else {
            sb.appendHtmlConstant("<input type='text' tabindex='-1' class='" + inputStyle + "'>"); 
        }
        
        
    }

}
