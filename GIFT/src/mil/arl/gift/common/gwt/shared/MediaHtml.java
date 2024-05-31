/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The HTML to display for some item of media
 *
 * @author jleonard
 */
public class MediaHtml implements IsSerializable {

    private String name;

    private String html;

    private String width;

    private String height;

    private String uri;
    
    private Serializable media;
    
    /** The Same Origin Policy violation flag. */
    private boolean isSameOriginPolicyViolator;
    
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public MediaHtml() {
    }

    /**
     * Constructor
     *
     * @param name The name of the media
     * @param html The html that displays the media
     * @param width The width of the content
     * @param height The height of the content
     */
    public MediaHtml(String name, String html, String width, String height) {
        this.name = name;
        this.html = html;
        this.width = width;
        this.height = height;
    }
    
    /**
     * Constructor with URI and Same Origin Policy violation indicator.
     *
     * @param media The media course object. Cannot be null and must be an instance of either
     * generatec.course.Media or generated.dkf.Media.
     * @param isSameOriginPolicyViolator Is the uri a same origin policy violator
     */
    public MediaHtml(Serializable media, boolean isSameOriginPolicyViolator) {
    	this.media = media;
        this.isSameOriginPolicyViolator = isSameOriginPolicyViolator;
        
        if(media instanceof generated.course.Media) {
            name = ((generated.course.Media) media).getName();
            uri = ((generated.course.Media) media).getUri();
            
        } else if(media instanceof generated.dkf.Media) {
            name = ((generated.dkf.Media) media).getName();
            uri = ((generated.dkf.Media) media).getUri();
            
        } else {
            throw new IllegalArgumentException("The media object provided must be a valid course or DKF media item.");
        }
    }
    
    /**
     * Gets the name of the media
     *
     * @return String The name of the media
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the HTML code that displays the media
     *
     * @return STring The HTML code that displays the media
     */
    public String getHtml() {
        return html;
    }

    /**
     * Sets the HTML code that displays the media
     * @param html The HTML code that displays the media
     */
    public void setHtml(String html) {
        this.html =  html;
    }

    /**
     * Gets the width of the content
     *
     * @return String The width of the content
     */
    public String getWidth() {
        return width;
    }

    /**
     * Gets the height of the content
     *
     * @return String The height of the content
     */
    public String getHeight() {
        return height;
    }
    
    /**
     * Gets the uri of the content
     *
     * @return String The uri of the content
     */
    public String getUri() {
        return uri;
    }
        
    /**
     * Gets the media properties. The returned properties will always be an instance of either 
     * generated.course.Media or generated.dkf.Media
     * 
     * @return The media properties.
     */
    public Serializable getProperties() {
        
    	if(media != null) {
    	    
    	    if(media instanceof generated.course.Media) {
        	    return ((generated.course.Media) media).getMediaTypeProperties();
        	    
        	} else if(media instanceof generated.dkf.Media) {
                return ((generated.dkf.Media) media).getMediaTypeProperties();
            }
    	}
    	
    	return null;
    }
    
    /**
     * Gets the informative message for this media item
     * 
     * @return the informative message for this media item. Can be null.
     */
    public String getMessage() {
        
        if(media instanceof generated.course.Media) {
            return ((generated.course.Media) media).getMessage();
            
        } else if(media instanceof generated.dkf.Media) {
            return ((generated.dkf.Media) media).getMessage();
        }
        
        return null;
    }
    
    /**
     * Returns whether or not the media item is a slide show
     * 
     * @return true if the media item is a slide show, false otherwise
     */
    public boolean isSlideShow() {
        
        if(media != null) {
            
            if(media instanceof generated.course.Media) {
                return ((generated.course.Media) media).getMediaTypeProperties() instanceof generated.course.SlideShowProperties;
                
            } else if(media instanceof generated.dkf.Media) {
                return ((generated.dkf.Media) media).getMediaTypeProperties() instanceof generated.dkf.SlideShowProperties;
            }
        }
        
        return false;
    }
    
    /**
     * Returns whether or not the media item is LTI
     * 
     * @return true if the media item is LTI, false otherwise
     */
    public boolean isLtiMedia() {
        
        if(media != null) {
            
            if(media instanceof generated.course.Media) {
                return ((generated.course.Media) media).getMediaTypeProperties() instanceof generated.course.LtiProperties;
                
            } else if(media instanceof generated.dkf.Media) {
                return ((generated.dkf.Media) media).getMediaTypeProperties() instanceof generated.dkf.LtiProperties;
            }
        }
        
        return false;
    }
    
    /**
     * True is the URI is in violation with the same origin policy.
     * @return the isSameOriginPolicyViolator flag
     */
    public boolean isSameOriginPolicyViolator() {
        return isSameOriginPolicyViolator;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[MediaHtml: name=");
        builder.append(name);
        builder.append(", html=");
        builder.append(html);
        builder.append(", width=");
        builder.append(width);
        builder.append(", height=");
        builder.append(height);
        builder.append(", uri=");
        builder.append(uri);
        builder.append(", isSameOriginPolicyViolator=");
        builder.append(isSameOriginPolicyViolator);
        builder.append("]");
        return builder.toString();
    }

}
