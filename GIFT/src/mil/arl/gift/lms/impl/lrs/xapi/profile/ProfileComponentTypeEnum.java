package mil.arl.gift.lms.impl.lrs.xapi.profile;

import mil.arl.gift.lms.impl.lrs.LrsEnum;

/**
 * Enumeration of the xAPI Profile Component types.
 * 
 * @author Yet Analytics
 *
 */
public enum ProfileComponentTypeEnum implements LrsEnum {
    // Concepts
    VERB("Verb"),
    ACTIVITY_TYPE("ActivityType"),
    ATTACHMENT_USAGE_TYPE("AttachmentUsageType"),
    CONTEXT_EXTENSION("ContextExtension"),
    RESULT_EXTENSION("ResultExtension"),
    ACTIVITY_EXTENSION("ActivityExtension"),
    STATE_RESOURCE("StateResource"),
    AGENT_PROFILE_RESOURCE("AgentProfileResource"),
    ACTIVITY_PROFILE_RESOURCE("ActivityProfileResource"),
    ACTIVITY("Activity"),
    // Statement Template
    STATEMENT_TEMPLATE("StatementTemplate");
    private String value;
    ProfileComponentTypeEnum(String componentType) {
        this.value = componentType;
    }
    @Override
    public String getValue() {
        return value;
    }
}