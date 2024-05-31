/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.util.StringUtils;

/**
 * The Initialize lesson Request is used to signify the lesson is beginning
 *
 * @author jleonard
 *
 */
public class InitializeLessonRequest {
    
    /**  
     * A reference to the content to be displayed that is unique to this course. (e.g. a DKF path, like
     *  \\Public\\STEELR - Battle Drill 6a - Playback - Log Option 2 - Demo Version\\vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml)
     */
    private String contentReference;

    /**
     * Class constructor
     * @param contentReference a reference to the content to be displayed that is unique to this course (e.g. a DKF path, like
     *  \\Public\\STEELR - Battle Drill 6a - Playback - Log Option 2 - Demo Version\\vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml). 
     * Can't be null or empty.
     */
    public InitializeLessonRequest(String contentReference) {

        if(StringUtils.isBlank(contentReference)){
            throw new IllegalArgumentException("The content reference can't be null or empty.");
        }
        
        this.contentReference = contentReference;
    } 
    
    /**
     * Return a reference to the content to be displayed that is unique to this course. (e.g. a DKF path, like
     *  \\Public\\STEELR - Battle Drill 6a - Playback - Log Option 2 - Demo Version\\vbs2.TSP 07-GFT-0137 ClearBldg.dkf.xml)
     *  
     * @return the reference to the content to be displayed. Won't be null.
     */
    public String getContentReference(){
        return contentReference;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[InitializeLessonRequest: ");
        sb.append("contentRef = ").append(getContentReference());
        sb.append("]");

        return sb.toString();
    }
}
