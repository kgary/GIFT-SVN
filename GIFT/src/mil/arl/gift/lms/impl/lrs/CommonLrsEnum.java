package mil.arl.gift.lms.impl.lrs;

/**
 * Enums used across the xAPI instrumentation. 
 * 
 * @author Yet Analytics
 *
 */
public enum CommonLrsEnum implements LrsEnum {
    ENCODING("UTF-8"),
    ACTIVITY_PREFIX("activityId:uri/its/"),
    MBOX_PREFIX("mailto:"),
    EMAIL_DOMAIN("@gifttutoring.org"),
    GIFT("GIFT"),
    AGENT("Agent"),
    GROUP("Group"),
    ACTIVITY("Activity"),
    SEPERATOR_COMMA(","),
    SEPERATOR_SLASH("/"),
    SINCE("since"),
    UNTIL("until");
    private String value;
    CommonLrsEnum(String s){
        this.value = s;
    }
    @Override
    public String getValue() {
        return value;
    }

}
