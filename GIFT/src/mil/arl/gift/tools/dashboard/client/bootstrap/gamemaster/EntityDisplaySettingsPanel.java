/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ThreeStateCheckbox;
import mil.arl.gift.common.util.StringUtils;

/**
 * A panel that provides the user with UI controls to modify the settings that control how
 * map entities are displayed to their associated maps
 *
 * @author nroberts
 */
public class EntityDisplaySettingsPanel extends Composite {

    /** The newline character used to separate attribute data values shown in entity labels */
    private static final String ENTITY_LABEL_NEWLINE = "\n";

    /** The string to show in place of data values for attributes that do not have any renderable data */
    private static final String NO_VALUE_PLACEHOLDER = "N/A";

    /** The comparator used to sort the display settings visually */
    private static final Comparator<RenderableAttribute> ATTRIBUTE_DISPLAY_ORDER_COMPARATOR = new Comparator<RenderableAttribute>() {

        @Override
        public int compare(RenderableAttribute o1, RenderableAttribute o2) {

            //sort attributes alphabetically by their display names
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
    };

    /**
     * An enumeration denoting the various groups of renderable entity attributes
     *
     * @author nroberts
     */
    public static enum AttributeGroup{

        /** The group of entity attributes whose data is provided by training applications */
        TRAINING_APPLICATION,

        /** The group of entity attributes whose data is provided by GIFT */
        GIFT;

        /** The ordered set of attributes that belong to this group*/
        private SortedSet<RenderableAttribute> attributes;

        /**
         * Gets the ordered set of attributes that belong to this group
         *
         * @return the set of attributes. Will not be null.
         */
        public SortedSet<RenderableAttribute> getAttributes(){

            if(attributes == null) {

                TreeSet<RenderableAttribute> tempAttributes = new TreeSet<>(ATTRIBUTE_DISPLAY_ORDER_COMPARATOR);

                //iterate through all of the attributes to find the ones that belong to this group
                for(RenderableAttribute attribute : RenderableAttribute.values()) {

                    if(this.equals(attribute.getGroup())){
                        tempAttributes.add(attribute);
                    }
                }

                attributes = Collections.unmodifiableSortedSet(tempAttributes);
            }

            return attributes;
        }
    }

    /**
     * An enumeration denoting the various entity attributes whose data can be displayed to the user through
     * some renderable means
     *
     * @author nroberts
     */
    public static enum RenderableAttribute{

        /** The entity's location in the scenario environment */
        LOCATION("Location", AttributeGroup.TRAINING_APPLICATION, false),

        /** The domain session hosting the knowledge session this entity is a part of*/
        HOST_DOMAIN_SESSION("Host Domain Session", AttributeGroup.GIFT, false),

        /** The domain session of the learner controlling the entity */
        DOMAIN_SESSION("Domain Session", AttributeGroup.GIFT, false),

        /** The role that the entity plays in its associated knowledge session's team organization*/
        ROLE("Role", AttributeGroup.GIFT, true),
        
        /** The entity's health in the scenario environment */
        HEALTH("Health", AttributeGroup.TRAINING_APPLICATION, false),

        /** The entity's velocity in the scenario environment */
        VELOCITY("Velocity", AttributeGroup.TRAINING_APPLICATION, false),

        /** The entity's orientation in the scenario environment */
        ORIENTATION("Orientation", AttributeGroup.TRAINING_APPLICATION , false),

        /** The marker that this entity's training application uses to distinguish it*/
        ENTITY_MARKER("Entity Marker", AttributeGroup.TRAINING_APPLICATION , true),
        
        /** The id that this entity's training application uses to distinguish it*/
        ENTITY_ID("Entity Id", AttributeGroup.TRAINING_APPLICATION , true),
        
        /** The entity's SIDC in the scenario environment */
        SIDC("SIDC", AttributeGroup.TRAINING_APPLICATION, false),
        
        /** The entity's posture in the scenario environment */
        POSTURE("Posture", AttributeGroup.TRAINING_APPLICATION, false),
        
        /** whether the entity is playable in the scenario environment as defined by the GIFT DKF team organization */
        PLAYABLE("Is Playable", AttributeGroup.GIFT, false),

        /** The user name associated with the entity */
        USER_NAME("Username", AttributeGroup.GIFT, false);

        /** This attribute's display name */
        private String displayName;

        /** Whether this attribute should be displayed by default */
        private boolean displayedByDefault;

        /** The attribute group that this attribute belongs to*/
        private AttributeGroup group;

        /**
         * Creates a new renderable attribute
         *
         * @param displayName the attribute's display name. Cannot be null or empty.
         * @param group the attribute  group that this attribute belongs to. Cannot be null.
         * @param displayedByDefault whether this attribute is displayed by default
         */
        private RenderableAttribute(String displayName, AttributeGroup group, boolean displayedByDefault) {

            if(StringUtils.isBlank(displayName)) {
                throw new IllegalArgumentException("The display name for an entity attribute cannot be null");
            }

            if(group == null) {
                throw new IllegalArgumentException("The group that an entity attribute belongs to cannot be null");
            }

            this.displayName = displayName;
            this.displayedByDefault = displayedByDefault;
            this.group = group;
        }

        /**
         * Gets this attribute's display name
         *
         * @return this attribute's display name. Will not be null or empty.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets whether this attribute is displayed by default
         *
         * @return whether this attribute is displayed by default
         */
        public boolean isDisplayedByDefault() {
            return displayedByDefault;
        }

        /**
         * Gets the attribute group that this attribute belongs to
         *
         * @return the attribute group. Will not be null.
         */
        public AttributeGroup getGroup() {
            return group;
        }
    }

    private static EntityDisplaySettingsPanelUiBinder uiBinder = GWT.create(EntityDisplaySettingsPanelUiBinder.class);

    interface EntityDisplaySettingsPanelUiBinder extends UiBinder<Widget, EntityDisplaySettingsPanel> {}

    /** The panel where renderable attributes will be listed so that the user can contro how they are displayed */
    @UiField
    protected FlowPanel attributePanel;

    /** The check box used to select/deselect all of the visible data attributes simultaneously */
    @UiField
    protected ThreeStateCheckbox allAttributesBox;

    /** A mapping from each renderable attribute to the setting used to control whether or not it is displayed*/
    public HashMap<RenderableAttribute, EntityAttributeSetting> attributeToSetting = new HashMap<>();

    /** The settings panel that acts as this panel's parent. Will be notified when display settings change. */
    private EntitySettingsPanel parentSettings;

    /**
     * Creates a panel where the user can adjust the display settings of map entities and sets
     * the default display settings
     *
     * @param parentSettings the main settings panel that acts as this panel's parent
     */
    public EntityDisplaySettingsPanel(EntitySettingsPanel parentSettings) {

        if(parentSettings == null) {
            throw new IllegalArgumentException("The parent options whose display attributes should be modified cannot be null");
        }

        this.parentSettings = parentSettings;

        initWidget(uiBinder.createAndBindUi(this));

        //create UI elements for each group of renderable attributes so that the user can control how they are displayed
        for(AttributeGroup group : AttributeGroup.values()) {

            SortedSet<RenderableAttribute> groupAttributes = group.getAttributes();

            if(groupAttributes != null) {

                EntityAttributeSetting option = null;

                for(RenderableAttribute attribute : groupAttributes) {

                    option = new EntityAttributeSetting(attribute);
                    attributeToSetting.put(attribute, option);
                    attributePanel.add(option);
                }

                if(option != null) {

                    //add a dividing line after the last attribute in a group
                    option.getElement().getStyle().setProperty("borderBottom", "1px dotted rgb(200,200,200");
                }
            }
        }

        updateAllAttributesCheck();

        allAttributesBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                //whenever the checkbox for all of the visible attributes changes value, update every attribute's
                //checkbox to match the new value
                for(EntityAttributeSetting setting : attributeToSetting.values()) {
                    setting.setShouldDisplay(event.getValue());
                }

                //notify the parent settings that display settings have been changed
                EntityDisplaySettingsPanel.this.parentSettings.onSettingChanged();
            }
        });
    }

    /**
     * Gets whether the user's current settings specify that the given attribute should be displayed
     *
     * @param attribute the renderable attribute to be displayed
     * @return whether or not the attribute should be displayed according to the user's settings
     */
    public boolean shouldDisplayAttribute(RenderableAttribute attribute) {

        EntityAttributeSetting option = attributeToSetting.get(attribute);

        if(option == null) {
            return false;
        }

        return option.shouldDisplay();
    }

    /**
     * Builds a renderable label for the given entity that contains only the attribute data that is
     * explictly allowed by the user's current display settings
     *
     * @param entity the entity that a label is being built for. Cannot be null.
     * @return a renderable label containing only the entity data allowed by the user's display settings
     */
    public String buildEntityLabel(AbstractMapDataPoint entity) {

        if(entity == null) {
            throw new IllegalArgumentException("The entity to build a label for cannot be null");
        }

        StringBuilder sb = new StringBuilder();

        //create strings representing each attribute that should be displayed according to the user's settings
        for(AttributeGroup group : AttributeGroup.values()) {
            for(RenderableAttribute attribute : group.getAttributes()) {
                if(shouldDisplayAttribute(attribute)) {

                    sb.append(attribute.getDisplayName())
                    .append(" = ");

                    Object data = entity.getAttributeData(attribute);

                    if(data != null) {
                        sb.append(data);

                    } else {
                        sb.append(NO_VALUE_PLACEHOLDER);
                    }

                    sb.append(ENTITY_LABEL_NEWLINE);
                }
            }
        }

        return sb.toString();
    }

    /**
     * Synchronizes the state of the display whenever a setting is changed
     */
    private void onDisplaySettingChanged() {

        //synchronize the checkbox used to enable/disable all visible attributes
        updateAllAttributesCheck();

        //notify the parent settings that display settings have changed
        parentSettings.onSettingChanged();
    }

    /**
     * Updates the state of the checkbox used to enable/disable all visible attributes so
     * that it accurately reflects how many attributes have been selected/deselected
     */
    private void updateAllAttributesCheck() {

        boolean oneEnabled = false;
        boolean allEnabled = true;

        for(EntityAttributeSetting setting : attributeToSetting.values()) {

            if(setting.shouldDisplay()) {

                if(!oneEnabled) {
                    oneEnabled = true;
                }

            } else if(allEnabled){
                allEnabled = false;
            }
        }

        allAttributesBox.setValue(oneEnabled == allEnabled ? oneEnabled : null);
    }

    /**
     * A widget that represents a renderable entity attribute and provides the user with UI elements
     * to control how it is displayed
     *
     * @author nroberts
     */
    private class EntityAttributeSetting extends FlowPanel{

        /** The checkbox that to defines whether this setting's associated attribute should be displayed*/
        private CheckBox shouldDisplayCheckBox;

        /**
         * Creates a new setting widget that allows the user to control how the given attribute is displayed
         *
         * @param attribute the attribute that this setting should control. Cannot be null.
         */
        public EntityAttributeSetting(RenderableAttribute attribute) {
            super();

            if(attribute == null) {
                throw new IllegalArgumentException("An attribute must be provided to create an attribute option");
            }

            shouldDisplayCheckBox = new CheckBox(attribute.getDisplayName());
            shouldDisplayCheckBox.setValue(attribute.isDisplayedByDefault());
            shouldDisplayCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {

                    //notify the surrounding panel whenever this setting is changed
                    onDisplaySettingChanged();
                }
            });

            add(shouldDisplayCheckBox);
        }

        /**
         * Gets whether this setting's associated attribute should be displayed
         *
         * @return whether the attribute should be displayed
         */
        public boolean shouldDisplay() {
            return shouldDisplayCheckBox.getValue();
        }

        /**
         * Sets whether this setting's associated attribute should be displayed
         *
         * @param shouldDisplay whether the attribute should be displayed
         */
        public void setShouldDisplay(Boolean shouldDisplay) {
            shouldDisplayCheckBox.setValue(shouldDisplay);
        }
    }
}
