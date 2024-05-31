/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.logging.Logger;

import com.google.gwt.dom.client.Document;

/**
 * The SurveyWidgetId class represents a guaranteed unique non-persistent identifier (for duration of application) for any generic Survey widget.
 * The id should not be used to be saved into a database for example, as the identifier is not persistent.  
 * GWT does not allow use of certain UUID generator classes on the client so this class encapsulates
 * the ability to generate a unique identifier for a widget on the GWT client in the survey editor panel.
 *  
 * @author nblomberg
 *
 */
public class SurveyWidgetId  {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(SurveyWidgetId.class.getName());
    
    /** The unique id of the widget.  This is guaranteed to be unique for the duration of the web application.  This id is not guaranteed
     * to be unique across different runs of the web application (eg. the id should not be persisted in databases, etc).
     * The class also allows encapsulation of the UUID generator, in this case, we are using the GWT Document.get().createUniqueId() method
     * to generate a unique id.
     */
    private String widgetId = "";
	
	/**
	 * Constructor (default)
	 */
	public SurveyWidgetId() {

	    // Document.get().createUniqueId() generates the unique id upon construction of the object.
	    setWidgetId(Document.get().createUniqueId());
	    
	}

	/**
	 * Accessor to get the widget id
	 * 
	 * @return String - The id of the widget.
	 */
    public String getWidgetId() {
        return widgetId;
    }

    /**
     * Private accessor to set the id of the widget.  This should not be called externally.
     * 
     * @param widgetId - The id of the widget.
     */
    private void setWidgetId(String widgetId) {
        this.widgetId = widgetId;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        boolean isSame = false;
        if (obj instanceof SurveyWidgetId) {
            SurveyWidgetId objId = (SurveyWidgetId) obj;
            if (objId.getWidgetId().compareTo(widgetId) == 0) {
                isSame = true;
            }
        }
        
        return isSame;
    }
    
    @Override 
    public int hashCode() {
        return widgetId.hashCode();
    }
    
    @Override 
    public String toString() {
        return widgetId;
    }
    

}
