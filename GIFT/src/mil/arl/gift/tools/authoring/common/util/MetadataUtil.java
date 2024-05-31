/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MetadataAttributeEnum;

/**
 * This class contains utility methods for retrieving information for authoring metadata files.
 * 
 * @author mhoffman
 *
 */
public class MetadataUtil {

    /**
     * Returns the collection of metadata attributes available for the specified Merrill's branch
     * point quadrant.  For example the Practice quadrant has different attributes associated with it
     * than the Rule quadrant.
     * 
     * @param quadrant the quadrant to retrieve metadata attributes for.  Can't be null.
     * @return collection of metadata attributes available for the quadrant.
     */
    public static List<MetadataAttributeEnum> getMetadataAttributeByQuadrant(MerrillQuadrantEnum quadrant){
        
        if(quadrant == null){
            throw new IllegalArgumentException("The quadrant can't be null.");
        }
        
        List<MetadataAttributeEnum> attributes = new ArrayList<>();
        for(MetadataAttributeEnum attribute : MetadataAttributeEnum.VALUES()){
            
            //if Practice then gather enumerations that are for practice quadrant
            //otherwise gather enumerations that are NOT for practice quadrant
            if((quadrant == MerrillQuadrantEnum.PRACTICE && attribute.isPracticeAttribute()) ||
                    (quadrant != MerrillQuadrantEnum.PRACTICE && attribute.isContentAttribute())){
                attributes.add(attribute);
            }
        }
        
        return attributes;
    }
}
