/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;


/**
 * This message data class contains the burst descriptor attributes associated
 * with weapon fire messages.
 * 
 * @author mhoffman
 *
 */
public class BurstDescriptor implements TrainingAppState {
    
    /** type of munition used in a weapon fire */
    protected EntityType munitionType;
    
    /** the type of warhead */
    protected Integer warhead;
    
    /** the type of fuse */
    protected Integer fuse;
    
    /** number of rounds fired in the burst */
    protected Integer quantity;
    
    /** the rounds per minute */
    protected Integer rate;
    
    /**
     * Class constructor - set class attributes
     * 
     * @param munitionType type of munition used in a weapon fire
     * @param warhead the type of warhead
     * @param fuse the type of fuse
     * @param quantity number of rounds fired in the burst
     * @param rate the rounds per minute
     */
    public BurstDescriptor(EntityType munitionType, Integer warhead, Integer fuse, Integer quantity, Integer rate){
        this.munitionType = munitionType;
        this.warhead = warhead;
        this.fuse = fuse;
        this.quantity = quantity;
        this.rate = rate;
   }    

    public EntityType getMunitionType() {
        return munitionType;
    }

    public Integer getWarhead() {
        return warhead;
    }

    public Integer getFuse() {
        return fuse;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getRate() {
        return rate;
    }

    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BurstDescriptor: ");
        sb.append(", Munition Type = ").append(getMunitionType());
        sb.append(", Warhead = ").append(getWarhead());
        sb.append(", Fuse = ").append(getFuse());
        sb.append(", Quantity = ").append(getQuantity());
        sb.append(", Rate = ").append(getRate());
        sb.append("]");

        return sb.toString();
    }

}
