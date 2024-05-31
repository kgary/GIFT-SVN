/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.ArrayList;

import mil.arl.gift.tutor.shared.AfterActionReviewDetailsNode;

/**
 * Wrapper for web site's properties to get/set AAR specific properties
 *
 * @author jleonard
 */
public class AfterActionReviewWidgetProperties {
    
    /* Property names */
    private final static String OVERVIEW_PROPERTY = "AFTER_ACTION_REVIEW_OVERVIEW";
    private final static String DETAILS_PROPERTY = "AFTER_ACTION_REVIEW_DETAILS";
    private final static String TITLE = "AFTER_ACTION_REVIEW_TITLE";
    
    private final static String DEFAULT_OVERVIEW = "No overview to display.";
    
    /**
     * Gets the AAR overview formatted in HTML
     * 
     * @param properties The properties of the web page with the AAR
     * @return String The AAR overview formatted in HTML
     */
    public static String getOverview(WidgetProperties properties) {
        
        String overview = properties.getStringPropertyValue(OVERVIEW_PROPERTY);
        return overview == null ? DEFAULT_OVERVIEW : overview;
    }

    /**
     * Sets the AAR overview formatted in HTML
     * 
     * @param properties The properties of the web page with the AAR
     * @param overview The AAR overview formatted in HTML
     */
    public static void setOverview(WidgetProperties properties, String overview) {
        
        properties.setPropertyValue(OVERVIEW_PROPERTY, overview);
    }
    
    /**
     * Gets the AAR details formatted in HTML
     * 
     * @param properties The properties of the web page with the AAR
     * @return String The AAR details formatted in HTML
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<AfterActionReviewDetailsNode> getDetails(WidgetProperties properties) {

        ArrayList<AfterActionReviewDetailsNode> details = (ArrayList<AfterActionReviewDetailsNode>) properties.getPropertyValue(DETAILS_PROPERTY);
        return details;
    }

    /**
     * Sets the AAR details formatted in HTML
     * 
     * @param properties The properties of the web page with the AAR
     * @param details The AAR details formatted in HTML
     */
    public static void setDetails(WidgetProperties properties, ArrayList<AfterActionReviewDetailsNode> details) {
        
        properties.setPropertyValue(DETAILS_PROPERTY, details);
    }
    
    /**
     * Set the AAR title.
     * 
     * @param properties the properties of the web page with the AAR
     * @param title the title to show for the AAR
     */
    public static void setTitle(WidgetProperties properties, String title){
        properties.setPropertyValue(TITLE, title);
    }
    
    /**
     * Return the AAR title
     * 
     * @param properties the properties of the web page with the AAR
     * @return the title found in the properties for the AAR.  Can return null if the property was not set or the property
     * was set to null.
     */
    public static String getTitle(WidgetProperties properties){
        return properties != null ? (String) properties.getPropertyValue(TITLE) : null;
    }
    
    private AfterActionReviewWidgetProperties() {
    }
}
