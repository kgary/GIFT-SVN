/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.AvatarData;
import mil.arl.gift.tutor.shared.ChatWindowEntry;

/**
 * Properties for the chat window widget
 *
 * @author jleonard
 */
public class ChatWindowWidgetProperties {

    private final static String AVATAR_PROPERTY = "AVATAR";
    
    private final static String USER_NAME = "USER_NAME";

    private final static String CHAT_LOG_PROPERTY = "CHAT_LOG";

    private final static String ENTERED_TEXT_PROPERTY = "ENTERED_TEXT";

    private final static String FINISHED_PROPERTY = "FINISHED";
    
    private final static String ALLOW_EARLY_EXIT_PROPERTY = "ALLOW_EARLY_EXIT";
    
    private final static String ALLOW_FREE_RESPONSE = "ALLOW_FREE_RESPONSE";
    
    private final static String DISPLAY_CONVERSATION_PANEL = "DISPLAY_CONVERSATION_PANEL";
    
    private final static String CHAT_ID = "CHAT_ID";
    
    private final static String CHAT_NAME = "CHAT_NAME";
    
    private final static String DESCRIPTION = "DESCRIPTION";
    
    private final static String UPDATE_COUNT = "UPDATE_COUNT";
    
    private final static String FULLSCREEN = "IS_FULLSCREEN";
    
    /**
     * Gets the feedback to display in the widget
     *
     * @param properties The widget properties
     * @return ArrayList<ChatWindowEntry> The entries to display.  Can be null or empty.
     */
    @SuppressWarnings(value = "unchecked")
    public static ArrayList<ChatWindowEntry> getChatLog(WidgetProperties properties) {
        return (ArrayList<ChatWindowEntry>) properties.getPropertyValue(CHAT_LOG_PROPERTY);
    }

    /**
     * Sets the feedback to display in the widget
     *
     * @param properties The widget properties
     * @param chatLog The feedback to display
     */
    public static void setChatLog(WidgetProperties properties, List<ChatWindowEntry> chatLog) {
        properties.setPropertyValue(CHAT_LOG_PROPERTY, new ArrayList<ChatWindowEntry>(chatLog));
    }

    /**
     * Gets the avatar to the displayed
     *
     * @param properties The widget properties
     * @return AvatarData The avatar to the displayed
     */
    public static AvatarData getAvatar(WidgetProperties properties) {

        return (AvatarData) properties.getPropertyValue(AVATAR_PROPERTY);
    }

    /**
     * Sets the avatar to the displayed
     *
     * @param properties The widget properties
     * @param avatar The avatar to the displayed
     */
    public static void setAvatar(WidgetProperties properties, AvatarData avatar) {

        properties.setPropertyValue(AVATAR_PROPERTY, avatar);
    }

    /**
     * Gets the text entered in the chat
     *
     * @param properties The widget properties
     * @return String The text entered in the chat.  Can be null or empty string.
     */
    public static String getEnteredText(WidgetProperties properties) {

        return properties.getStringPropertyValue(ENTERED_TEXT_PROPERTY);
    }

    /**
     * Sets the text entered in the chat
     *
     * @param properties The widget properties
     * @param enteredText The text entered in the chat
     */
    public static void setEnteredText(WidgetProperties properties, String enteredText) {

        properties.setPropertyValue(ENTERED_TEXT_PROPERTY, enteredText);
    }

    /**
     * Gets the name of the user in the chat
     *
     * @param properties The widget properties
     * @return String The name of the user in the chat
     */
    public static String getUserName(WidgetProperties properties) {

        return properties.getStringPropertyValue(USER_NAME);
    }

    /**
     * Sets the name of the user in the chat
     *
     * @param properties The widget properties
     * @param userName The the name of the user in the chat
     */
    public static void setUserName(WidgetProperties properties, String userName) {

        properties.setPropertyValue(USER_NAME, userName);
    }

    /**
     * Gets if the chat session is finished
     *
     * @param properties The widget properties
     * @return boolean If the chat session is finished
     */
    public static boolean isFinished(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(FINISHED_PROPERTY);
        return value != null ? value : false;
    }

    /**
     * Sets if the chat session is finished
     *
     * @param properties The widget properties
     * @param isFinished If the chat session is finished
     */
    public static void setIsFinished(WidgetProperties properties, boolean isFinished) {
        properties.setPropertyValue(FINISHED_PROPERTY, isFinished);
    }

    /**
     * Gets if the chat session can be exited early
     *
     * @param properties The widget properties
     * @return boolean If the chat session can be exited early
     */
    public static boolean getAllowEarlyExit(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(ALLOW_EARLY_EXIT_PROPERTY);
        return value != null ? value : false;
    }

    /**
     * Sets if the chat session can be exited early
     *
     * @param properties The widget properties
     * @param allowEarlyExit If the chat session can be exited early
     */
    public static void setAllowEarlyExit(WidgetProperties properties, boolean allowEarlyExit) {
        properties.setPropertyValue(ALLOW_EARLY_EXIT_PROPERTY, allowEarlyExit);
    }
    
    /**
     * Gets if the chat widget should be displayed in fullscreen mode
     *
     * @param properties The widget properties
     * @return boolean Whether or not the widget should be displayed in fullscreen mode
     */
    public static boolean isFullscreen(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(FULLSCREEN);
        return value != null ? value : false;
    }

    /**
     * Sets if the chat widget should be displayed in fullscreen mode
     *
     * @param properties The widget properties
     * @param isFullscreen Whether or not the widget should be displayed in fullscreen mode
     */
    public static void setFullscreen(WidgetProperties properties, boolean isFullscreen) {
        properties.setPropertyValue(FULLSCREEN, isFullscreen);
    }
    
    
    /**
     * Gets if the chat session allows the learner to provide free response input
     *
     * @param properties The widget properties
     * @return boolean If the chat session allows the learner to provide free response input
     */
    public static boolean getAllowFreeResponse(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(ALLOW_FREE_RESPONSE);
        return value != null ? value : false;
    }

    /**
     * Sets if the chat session allows the learner to provide free response input
     *
     * @param properties The widget properties
     * @param allowEarlyExit If the chat session allows the learner to provide free response input
     */
    public static void setAllowFreeResponse(WidgetProperties properties, boolean allowFreeResponse) {
        properties.setPropertyValue(ALLOW_FREE_RESPONSE, allowFreeResponse);
    }
    
    /**
     * Gets whether or not the chat widget should be displayed in the conversations panel.
     *
     * @param properties The widget properties
     * @return true if the chat widget should be displayed in the conversations panel, false otherwise.
     */
    public static boolean shouldDisplayInConversationPanel(WidgetProperties properties) {
        Boolean value = properties.getBooleanPropertyValue(DISPLAY_CONVERSATION_PANEL);
        return value != null ? value : false;
    }

    /**
     * Sets the display property for this chat widget.
     *
     * @param properties The widget properties
     * @param displayConversationPanel If the chat should be displayed in the conversations panel.
     */
    public static void setDisplayInConversationPanel(WidgetProperties properties, boolean displayConversationPanel) {
        properties.setPropertyValue(DISPLAY_CONVERSATION_PANEL, displayConversationPanel);
    }
    
    /**
     * Gets the chat id.
     *
     * @param properties The widget properties
     * @return the unique id of the chat used to match updates or -1 if no chat id exists
     */
    public static int getChatId(WidgetProperties properties) {

        return (properties.getIntegerPropertyValue(CHAT_ID) == null) ? -1 : properties.getIntegerPropertyValue(CHAT_ID);
    }

    /**
     * Sets the chat id.
     *
     * @param properties The widget properties
     * @param chatId the unique id of the chat used to match updates
     */
    public static void setChatId(WidgetProperties properties, int chatId) {

        properties.setPropertyValue(CHAT_ID, chatId);
    }
    
    /**
     * Gets the number of updates available for the chat.
     *
     * @param properties The widget properties
     * @return the number of updates available for the chat.
     */
    public static int getUpdateCount(WidgetProperties properties) {

        return (properties.getIntegerPropertyValue(UPDATE_COUNT) == null) ? 0 : properties.getIntegerPropertyValue(UPDATE_COUNT);
    }

    /**
     * Sets the number of updates available for the chat.
     *
     * @param properties The widget properties
     * @param updateCount the number of updates available for the chat.
     */
    public static void setUpdateCount(WidgetProperties properties, int updateCount) {

        properties.setPropertyValue(UPDATE_COUNT, updateCount);
    }
    
    /**
     * Gets the text entered in the chat
     *
     * @param properties The widget properties
     * @return String The text entered in the chat
     */
    public static String getChatName(WidgetProperties properties) {

        return properties.getStringPropertyValue(CHAT_NAME);
    }

    /**
     * Sets the text entered in the chat
     *
     * @param properties The widget properties
     * @param enteredText The text entered in the chat
     */
    public static void setChatName(WidgetProperties properties, String chatName) {
       properties.setPropertyValue(CHAT_NAME, chatName);
    }
    
    /**
     * Gets the conversation description
     *
     * @param properties The widget properties
     * @return String The conversation description
     */
    public static String getDescription(WidgetProperties properties) {

        return properties.getStringPropertyValue(DESCRIPTION);
    }

    /**
     * Sets the conversation description
     *
     * @param properties The widget properties
     * @param enteredText The conversation description
     */
    public static void setDescription(WidgetProperties properties, String description) {

        properties.setPropertyValue(DESCRIPTION, description);
    }
}
