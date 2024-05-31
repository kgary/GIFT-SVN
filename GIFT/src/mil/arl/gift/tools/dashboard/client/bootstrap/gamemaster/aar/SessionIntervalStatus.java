/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * An object containing status information for visualizing event intervals for events that occurr
 * during a knowledge session
 * 
 * @author nroberts
 */
public class SessionIntervalStatus extends JavaScriptObject{

    /**
     * The types of event intervals that can occur in a session
     *
     * @author nroberts
     */
    public enum IntervalType{
        
        //===========================================================
        // General Types
        //===========================================================
        AT_EXPECTATION,
        ABOVE_EXPECTATION("\uf005" /* STAR */, true),
        BELOW_EXPECTATION,
        UNKNOWN,
        BOOKMARK("\uf02e" /* BOOKMARK */),
        OBSERVER_EVALUATION("\uf0e3" /* GAVEL */),
        STRATEGY("\uf0e7" /* BOLT */),
        
        //===========================================================
        // Strategy Activity Types
        //===========================================================
        FEEDBACK("\uf075" /* COMMENT */, IntervalType.STRATEGY),
        PRESENT_MEDIA("\uf15b" /* FILE */,IntervalType.STRATEGY),
        MODIFY_SCENARIO("\uf1bb" /* TREE */, IntervalType.STRATEGY),
        PRESENT_SURVEY("\uf14b" /* PENCIL_SQUARE */,IntervalType.STRATEGY),
        START_CONVERSATION("\uf086" /* COMMENTS */, IntervalType.STRATEGY),
        
        //===========================================================
        // Feedback SubTypes
        //===========================================================
        PRESENT_MESSAGE("\uf075" /* COMMENT */, IntervalType.FEEDBACK),
        FEEDBACK_LOCAL_WEBPAGE("\uf15b" /* FILE */, IntervalType.FEEDBACK),
        PLAY_AUDIO("\uf001" /* MUSIC */, IntervalType.FEEDBACK),
        AVATAR_SCRIPT("\uf007" /* USER */, IntervalType.FEEDBACK),
        
        //===========================================================
        // Present Media SubTypes
        //===========================================================
        SLIDE_SHOW("\uf2d0" /* WINDOW_MAXIMIZE */, IntervalType.PRESENT_MEDIA),
        PDF("\uf1c1" /* FILE_PDF_O */,IntervalType.PRESENT_MEDIA),
        MEDIA_LOCAL_WEBPAGE("\uf15b" /* FILE */, IntervalType.PRESENT_MEDIA),
        LOCAL_IMAGE("\uf03e" /* PICTURE_O */, IntervalType.PRESENT_MEDIA),
        WEB_ADDRESS("\uf0ac" /* GLOBE */, IntervalType.PRESENT_MEDIA),
        YOUTUBE_VIDEO("\uf16a" /* YOUTUBE_PLAY */, IntervalType.PRESENT_MEDIA),
        
        //===========================================================
        // Modify Scenario SubTypes
        //===========================================================
        CREATE_ACTORS("\uf234" /* USER_PLUS*/,IntervalType.MODIFY_SCENARIO),
        REMOVE_ACTORS("\uf235" /* USER_TIMES */, IntervalType.MODIFY_SCENARIO),
        TELEPORT("\uf21d" /* STREET_VIEW */, IntervalType.MODIFY_SCENARIO),
        HIGHLIGHT("\uf245" /* MOUSE_POINTER */, IntervalType.MODIFY_SCENARIO),
        REMOVE_HIGHLIGHT("\uf12d" /* ERASER */, IntervalType.MODIFY_SCENARIO),
        BREADCRUMBS("\uf018" /* ROAD */, IntervalType.MODIFY_SCENARIO),
        REMOVE_BREADCRUMBS("\uf12d" /* ERASER */, IntervalType.MODIFY_SCENARIO),
        FOG("\uf070" /* EYE_SLASH */, IntervalType.MODIFY_SCENARIO),
        OVERCAST("\uf0c2" /* CLOUD */, IntervalType.MODIFY_SCENARIO),
        RAIN("\uf043" /* DROP */, IntervalType.MODIFY_SCENARIO),
        TIME_OF_DAY("\uf185" /* SUN_O */, IntervalType.MODIFY_SCENARIO),
        ENDURANCE("\uf240" /* BATTERY_FULL */, IntervalType.MODIFY_SCENARIO),
        FATIGUE_RECOVERY("\uf243" /* BATTER_QUARTER */, IntervalType.MODIFY_SCENARIO),
        SCRIPT("\uf120" /* TERMINAL */, IntervalType.MODIFY_SCENARIO);
        
        /** The subtypes that belong to this type*/
        private List<IntervalType> subTypes;
        
        /** The parent type for this interval type, if applicable */
        private IntervalType parentType;
        
        /** The icon to use when this interval is displayed, in the form of a unicode string */
        private String displayIcon;
        
        /** Whether the displayed icon, if any, should be centered relative to its interval */
        private boolean centerIcon;
        
        /**
         * Creates a new interval type with no display icon and no parent type
         */
        private IntervalType() {
            this(null, null);
        }
        
        /**
         * Creates a new interval type with a display icon but no parent type
         * 
         * @param displayIcon the icon to use when this type is displayed. 
         * Can be null, if no icon should be displayed.
         */
        private IntervalType(String displayIcon) {
            this(displayIcon, null);
        }
        
        /**
         * Creates a new interval type with a display icon but no parent type
         * 
         * @param displayIcon the icon to use when this type is displayed. 
         * Can be null, if no icon should be displayed.
         * @param whether the displayed icon, if any, should be centered relative to its interval
         */
        private IntervalType(String displayIcon, boolean centerIcon) {
            this(displayIcon);
            this.centerIcon = centerIcon;
        }
        
        /**
         * Creates a new interval type with a parent type but no display icon
         * 
         * @param parentType the parent type for this interval type. Can be null.
         */
        private IntervalType(IntervalType parentType) {
            this(null, parentType);
        }
        
        /**
         * Creates a new interval type with the given display icon and parent type
         * 
         * @param displayIcon the icon to use when this type is displayed. 
         * Can be null, if no icon should be displayed.
         * @param parentType the parent type for this interval type. Can be null.
         */
        private IntervalType(String displayIcon, IntervalType parentType) {
            this.displayIcon = displayIcon;
            this.parentType = parentType;
        }
        
        /**
         * Gets the icon to use when this interval type is displayed, in the 
         * form of a unicode string
         * 
         * @return the display icon. Can be null, if no icon should be shown.
         */
        public String getDisplayIcon() {
            return displayIcon;
        }
        
        /**
         * Gets the subtypes of this interval type. These can be considered
         * more-specific versions of this type
         *
         * @return the subtypes. Will not be null.
         */
        public List<IntervalType> getSubTypes(){

            if(subTypes == null) {

                subTypes = new ArrayList<>();

                //iterate through all of the attributes to find the ones that belong to this group
                for(IntervalType subType : IntervalType.values()) {

                    if(this.equals(subType.getParentType())){
                        subTypes.add(subType);
                    }
                }
            }

            return subTypes;
        }
        
        /**
         * Gets the parent type of this interval type. This interval type is considered
         * a more-specific version of its parent type, if applicable.
         * 
         * @return the parent type. Can be null, if this type has no parent.
         */
        public IntervalType getParentType() {
            return parentType;
        }
        
        /**
         * Gets the topmost parent type of this interval type. The topmost parent type is
         * considered the least-specific version of the type.
         * 
         * @return the base type. Can be null, if this type has no parent.
         */
        public IntervalType getBaseType() {
            
            IntervalType currentType = this;
            
            while(currentType.getParentType() != null) {
                currentType = currentType.getParentType();
            }
            
            return currentType;
        }

        /**
         * Gets whether the displayed icon, if any, should be centered relative to its interval
         * 
         * @return whether to center the icon
         */
        public boolean shouldCenterIcon() {
            return centerIcon;
        }
    }
    
   

    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected SessionIntervalStatus() {}
    
    /**
     * Creates a new set of status information for an interval
     * 
     * @param type the type of interval. Cannot be null.
     * @return the status information. Will not be null.
     */
    final public static SessionIntervalStatus create(IntervalType type) {
        return createJs().setType(type);
    }
    
    //native implementation of create(IntervalType)
    final private static native SessionIntervalStatus createJs()/*-{
        return {};
    }-*/;
    
    /**
     * Sets the type of interval
     * 
     * @param type the type of interval. Cannot be null.
     * @return the status information. Will not be null.
     */
    final private SessionIntervalStatus setType(IntervalType type) {
        
        if(type == null) {
            throw new IllegalArgumentException("An interval's type cannot be null");
        }
        
        return setTypeJs(type.name());
    }
    
    //native implementation of setType(IntervalType)
    final private native SessionIntervalStatus setTypeJs(String type)/*-{
        this.type = type;
        return this;
    }-*/;
    
    /**
     * Gets the type of interval
     * 
     * @return the type of interval. Will not be null.
     */
    final public IntervalType getType() {
        return IntervalType.valueOf(getTypeJs());
    }
    
    //native implementation of getType()
    final private native String getTypeJs()/*-{
        return this.type;
    }-*/;
}
