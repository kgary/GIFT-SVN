/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.io.Serializable;
import java.util.Objects;

import mil.arl.gift.common.enums.EchelonEnum;

/**
 * Represents a DIS Entity Type
 *
 * @author jleonard
 */
public class EntityType implements TrainingAppState, Serializable {

    /** Default serial version id */
    private static final long serialVersionUID = 1L;
    
    public static final int ENTITY_TYPE_PLATFORM = 1;
    public static final int ENTITY_TYPE_LIFEFORM = 3;

    /**
     * The kind of entity
     * From the DIS standard:
     * 0    Other
     * 1   Platform
     * 2   Munition
     * 3   Life form
     * 4   Environmental
     * 5   Cultural feature
     * 6   Supply
     * 7   Radio
     * 8   Expendable
     * 9   Sensor/Emitter
     */
    private int entityKind;
    private int domain;
    private int country;
    private int category;
    private int subcategory;
    private int specific;
    private int extra;

    /** The echelon/rank that this entity belongs to. */
    private EchelonEnum echelon;

    /** No-arg constructor required for GWT serialization */
    private EntityType() {
    }

    /**
     * Constructor
     *
     * @param entityKind The ID of the entity's kind
     * @param domain The ID of the entity's domain
     * @param country The ID of the entity's country
     * @param category The ID of the entity's category
     * @param subcategory The ID of the entity's subcategory
     * @param specific The value of the entity's specific field
     * @param extra The value of the entity's extra field
     */
    public EntityType(int entityKind, int domain, int country, int category, int subcategory, int specific, int extra) {
        this();
        this.entityKind = entityKind;
        this.domain = domain;
        this.country = country;
        this.category = category;
        this.subcategory = subcategory;
        this.specific = specific;
        this.extra = extra;
    }

    private EntityType shallowCopy() {
        return new EntityType(entityKind, domain, country, category, subcategory, specific, extra);
    }

    /**
     * Gets the ID of the entity's kind
     * From the DIS standard:
     * 0    Other
     * 1   Platform
     * 2   Munition
     * 3   Life form
     * 4   Environmental
     * 5   Cultural feature
     * 6   Supply
     * 7   Radio
     * 8   Expendable
     * 9   Sensor/Emitter
     *
     * @return int The ID of the entity's kind
     */
    public int getEntityKind() {
        return entityKind;
    }

    /**
     * Gets the ID of the entity's domain
     *
     * @return int The ID of the entity's domain
     */
    public int getDomain() {
        return domain;
    }

    /**
     * Gets the ID of the entity's country
     *
     * @return int The ID of the entity's country
     */
    public int getCountry() {
        return country;
    }

    /**
     * Gets the ID of the entity's category
     *
     * @return int The ID of the entity's category
     */
    public int getCategory() {
        return category;
    }

    /**
     * Gets the ID of the entity's subcategory
     *
     * @return int The ID of the entity's subcategory
     */
    public int getSubcategory() {
        return subcategory;
    }

    /**
     * Gets the value of the entity's specific field
     *
     * @return int The value of the entity's specific field
     */
    public int getSpecific() {
        return specific;
    }

    /**
     * Gets the value of the entity's extra field
     *
     * @return int The value of the entity's extra field
     */
    public int getExtra() {
        return extra;
    }

    /**
     * Getter for the echelon.
     *
     * @return The value of {@link #echelon}.
     */
    public EchelonEnum getEchelon() {
        return echelon;
    }

    /**
     * Setter for the echelon.
     *
     * @param echelon The new value of {@link #echelon}.
     */
    public EntityType replaceEchelon(EchelonEnum echelon) {
        EntityType toRet = shallowCopy();
        toRet.echelon = echelon;
        return toRet;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + category;
        result = prime * result + country;
        result = prime * result + domain;
        result = prime * result + entityKind;
        result = prime * result + extra;
        result = prime * result + specific;
        result = prime * result + subcategory;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityType other = (EntityType) obj;
        if (category != other.category)
            return false;
        if (country != other.country)
            return false;
        if (domain != other.domain)
            return false;
        if (entityKind != other.entityKind)
            return false;
        if (extra != other.extra)
            return false;
        if (specific != other.specific)
            return false;
        if (subcategory != other.subcategory)
            return false;
        if (Objects.equals(echelon, other.echelon))
            return false;
        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[EntityType: ");
        sb.append("Entity Kind = ").append(getEntityKind());
        sb.append(", Domain = ").append(getDomain());
        sb.append(", Country = ").append(getCountry());
        sb.append(", Category = ").append(getCategory());
        sb.append(", SubCategory = ").append(getSubcategory());
        sb.append(", Specific = ").append(getSpecific());
        sb.append(", Extra = ").append(getExtra());
        sb.append("]");

        return sb.toString();
    }
}
