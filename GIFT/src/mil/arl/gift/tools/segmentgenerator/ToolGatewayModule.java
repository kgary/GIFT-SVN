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
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ta.state.Detonation;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.WeaponFire;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageHandler;
import org.apache.log4j.PropertyConfigurator;

/**
 * A gateway module for capturing outside communication to do analysis on it
 *
 * @author jleonard
 */
public class ToolGatewayModule extends GatewayModule {

    /** The object to receive the DIS traffic */
    private final List<DISListener> listeners = new ArrayList<DISListener>();

    private boolean pduOnEntityState = false;

    private boolean pduOnWeaponFire = false;

    private boolean pduOnDetonation = false;

    static {
        //use gateway log4j
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/gateway.log4j.properties");
    }

    /**
     * Constructor
     */
    public ToolGatewayModule() {
        super(PackageUtil.getSource() + "/mil/arl/gift/tools/segmentgenerator/test.interopConfig.xml");
        instance = this;
    }

    @Override
    protected void init() {

        createSubjectTopicClient(this.getGatewayTopicName(), new MessageHandler() {

            @Override
            public boolean processMessage(Message message) {

                if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {
                    handleEntityStateMessage(message);
                } else if (message.getMessageType() == MessageTypeEnum.DETONATION) {
                    handleDetonationMessage(message);
                } else if (message.getMessageType() == MessageTypeEnum.WEAPON_FIRE) {
                    handleWeaponFireMessage(message);
                }
                return true;
            }
        });
        super.init();
    }

    /**
     * Add a listener for DIS packets received by the Gateway module
     *
     * @param listener The listener for DIS packets
     */
    public void addListener(DISListener listener) {
        listeners.add(listener);
    }

    /**
     * Sets if a small entity should be generated for each entity state PDU
     * received, for marking where entity states occurred
     *
     * @param on If a small entity should be generated for each entity state PDU
     */
    public void setPduOnEntityState(boolean on) {
        pduOnEntityState = on;
    }

    /**
     * Sets if a small entity should be generated for each weapon fires PDU
     * received, for marking where weapon fires occurred
     *
     * @param on If a small entity should be generated for each weapon fire PDU
     */
    public void setPduOnWeaponFire(boolean on) {
        pduOnWeaponFire = on;
    }

    /**
     * Sets if a small entity should be generated for each detonation PDU
     * received, for marking where detonations occurred
     *
     * @param on If a small entity should be generated for each detonation PDU
     */
    public void setPduOnDetonation(boolean on) {
        pduOnDetonation = on;
    }

    /**
     * A entity state packet has been received
     *
     * @param msg An entity state packet
     */
    private void handleEntityStateMessage(Message msg) {
        for (DISListener i : listeners) {
            i.entityStateReceived(msg);
        }
        if (pduOnEntityState) {
            Point3d pos = ((EntityState) msg.getPayload()).getLocation();
            EntityGenerator.getInstance().emitEntityState(
                    pos.getX(), pos.getY(), pos.getZ(),
                    VbsEntityEnum.SPHERE_50CM);
        }
    }

    /**
     * A detonation packet has been received
     *
     * @param msg A detonation packet
     */
    private void handleDetonationMessage(Message msg) {
        for (DISListener i : listeners) {
            i.detonationReceived(msg);
        }

        if (pduOnDetonation) {
            Vector3d pos = ((Detonation) msg.getPayload()).getLocation();
            EntityGenerator.getInstance().emitEntityState(
                    pos.getX(), pos.getY(), pos.getZ(),
                    VbsEntityEnum.APACHE_AH1);
        }
    }

    /**
     * A weapon fire packet has been received
     *
     * @param msg A weapon fire packet
     */
    private void handleWeaponFireMessage(Message msg) {
        for (DISListener i : listeners) {
            i.weaponFireReceived(msg);
        }
        if (pduOnWeaponFire) {
            Vector3d pos = ((WeaponFire) msg.getPayload()).getLocation();
            EntityGenerator.getInstance().emitEntityState(
                    pos.getX(), pos.getY(), pos.getZ(),
                    VbsEntityEnum.APACHE_AH1);
        }
    }
}
