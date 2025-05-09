/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.embedded.message;

public class EmbeddedSimpleExampleState {

    /** just an example class attribute for this game state class */
    private String var;

    /**
     * Class constructor - set attribute(s).
     *
     * @param var just an example class attribute for this game state class
     */
    public EmbeddedSimpleExampleState(String var){

        if(var == null){
            throw new IllegalArgumentException("The var can't be null.");
        }

        this.var = var;
    }

    /**
     * Return the var value.
     *
     * @return String
     */
    public String getVar(){
        return var;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EmbeddedSimpleExampleState: ");
        sb.append("var = ").append(getVar());
        sb.append("]");
        return sb.toString();
    }
}
