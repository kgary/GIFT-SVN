/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An entry in the chat window
 *
 * @author jleonard
 */
public class ChatWindowEntry implements IsSerializable {

    private String source;

    private String text;

    private List<String> choices;
    
    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public ChatWindowEntry() {
    }

    /**
     * Constructor
     *
     * @param source Who said
     * @param text What was said
     */
    public ChatWindowEntry(String source, String text) {

        this.source = source;

        this.text = text;
    }

    /**
     * Gets the source of what was said (e.g. Whom)
     *
     * @return String The source of what was said
     */
    public String getSource() {

        return source;
    }

    /**
     * Gets the text that was said
     *
     * @return String The text that was said
     */
    public String getText() {

        return text;
    }
    
    /**
     * Sets the available choices for the chat text.
     * 
     * @param choices A list of choices the user can select.
     */
    public void setChoices(List<String> choices) {
    	this.choices = choices;
    }
    
    /**
     * Gets the available choices for the chat text.
     * 
     * @return choices A list of choices the user can select.
     */
    public List<String> getChoices() {
    	return choices;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("ChatWindowEntry = [");
        sb.append("source = ").append(source);
        sb.append(", text = ").append(text);
        
        if (choices != null && !choices.isEmpty()) {
            sb.append(", choices = [");
            
            for (String choice : choices) {
                sb.append(choice).append(", ");
            }
            sb.append("]");
        }
        
        sb.append("]");
        return sb.toString();
    }
}
