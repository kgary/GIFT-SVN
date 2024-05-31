/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.Map.Entry;

import mil.arl.gift.common.io.ImageProperties;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains common widget property logic.
 * 
 * @author mhoffman
 *
 */
public class CommonWidgetProperties implements IsSerializable {
    
    public static final String IMAGES_BUILD_PREFIX = "images";

    /**
     * Add the image paths to the Widget Properties so that they can be used by the Tutor UI.
     * 
     * @param properties the properties for a widget
     */
    public static void setImagePaths(WidgetProperties properties){
        
        for(Entry<Object, Object> entry : ImageProperties.getInstance().getProperties()){            
            properties.setPropertyValue((String)entry.getKey(), IMAGES_BUILD_PREFIX + "/" + (String)entry.getValue());
        }        
    }
}
