/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AbstractEnum;
import org.jdis.pdu.record.EntityType;


/**
 * Enumeration of the various VBS entity types used by the segment generator
 * 
 * @author mhoffman
 *
 */
public class VbsEntityEnum extends AbstractEnum {

    private static List<VbsEntityEnum> enumList = new ArrayList<VbsEntityEnum>(4);
    private static int index = 0;

    public static final VbsEntityEnum ARROW_BLUE = new VbsEntityEnum("vbs2_visual_arrow_blue", "vbs2_visual_arrow_blue", new EntityType(248,0,0,0,0,0,0));
    public static final VbsEntityEnum ARROW_GREEN = new VbsEntityEnum("vbs2_visual_arrow_green", "vbs2_visual_arrow_green", new EntityType(248,0,0,0,0,0,1));
    public static final VbsEntityEnum ARROW_RED = new VbsEntityEnum("vbs2_visual_arrow_red", "vbs2_visual_arrow_red", new EntityType(248,0,0,0,0,0,2));
    public static final VbsEntityEnum SPHERE_50CM = new VbsEntityEnum("vbs2_visual_sphere_50cm", "vbs2_visual_sphere_50cm", new EntityType(1,2,225,20,1,4,1));
    public static final VbsEntityEnum WAYPOINT_BLUE = new VbsEntityEnum("vbs2_editor_waypoint_blue", "vbs2_editor_waypoint_blue", new EntityType(248,0,0,0,0,0,4));
    public static final VbsEntityEnum WAYPOINT_GREEN = new VbsEntityEnum("vbs2_editor_waypoint_green", "vbs2_editor_waypoint_green", new EntityType(248,0,0,0,0,0,5));
    public static final VbsEntityEnum WAYPOINT_RED = new VbsEntityEnum("vbs2_editor_waypoint_red", "vbs2_editor_waypoint_red", new EntityType(248,0,0,0,0,0,6));
    public static final VbsEntityEnum CONE = new VbsEntityEnum("vbs2_cone", "vbs2_cone", new EntityType(248,0,0,0,0,0,7));
    public static final VbsEntityEnum APACHE_AH1 = new VbsEntityEnum("vbs2_gb_wah64d_longbow", "Apache AH.1", new EntityType(1,2,225,20,1,4,0));
    
    private static final long serialVersionUID = 1L;
    
    private EntityType entityType;
    
    private VbsEntityEnum(String name, String displayName, EntityType entityType){
    	super(index++, name, displayName);
        this.entityType = entityType;
    	enumList.add(this);
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static VbsEntityEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static VbsEntityEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<VbsEntityEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
