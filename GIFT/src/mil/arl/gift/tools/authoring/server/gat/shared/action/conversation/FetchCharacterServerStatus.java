/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.conversation;
 
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action used to check the status of the CharacterServer
 * 
 * @author bzahid
 */
public class FetchCharacterServerStatus implements Action<GatServiceResult> {

    /**
     * Default public no-arg constructor. Needed for serialization.
     */
    public FetchCharacterServerStatus() {
    }
  
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchCharacterServerStatus: ");
        sb.append("]");

        return sb.toString();
    } 
}
