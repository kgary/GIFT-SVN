/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.segmentgenerator;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.gateway.interop.dis.DISInterface;
import org.jdis.pdu.EntityStatePDU;
import org.jdis.pdu.record.EntityIdentifier;
import org.jdis.pdu.record.WorldCoordinates;

/**
 *
 * @author jleonard
 */
public class EntityGenerator {
    
    private static EntityGenerator instance = null;
    
    private final List<DISInterface> disInterfaces = new ArrayList<DISInterface>();
    
    private int nextEntityId = 0;
    
    public static EntityGenerator getInstance() {
        if(instance == null) {
            instance = new EntityGenerator();
        }
        return instance;
    }
    
    private EntityGenerator() {
        
    }
    
    public void config(List<DISInterface> interfaces) {
        disInterfaces.addAll(interfaces);
    }
    
    public void emitEntityState(int entityId, WorldCoordinates entityPosition, VbsEntityEnum entityType) {
        if(entityType != null && entityPosition != null) {
            for(DISInterface i : disInterfaces) {
                EntityStatePDU pdu = new EntityStatePDU();
                pdu.setEntityID(new EntityIdentifier(i.getSiteID(), i.getApplicationID(), entityId));
                pdu.setForceID(1);
                pdu.setEntityType(entityType.getEntityType());
                pdu.setEntityLocation(entityPosition);
                i.sendPDU(pdu);
            }
        }
    }
    
    public int emitEntityState(WorldCoordinates entityPosition, VbsEntityEnum entityType) {
        emitEntityState(nextEntityId, entityPosition, entityType);
        ++nextEntityId;
        return nextEntityId - 1;
    }
    
    public int emitEntityState(double posX, double posY, double posZ, VbsEntityEnum entityType) {
        emitEntityState(nextEntityId, new WorldCoordinates(posX,posY,posZ), entityType);
        ++nextEntityId;
        return nextEntityId - 1;
    }
    
}
