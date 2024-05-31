package mil.arl.gift.lms.impl.lrs;

/**
 * Enums for language tags used within language maps found in xAPI Statements.
 * 
 * @author Yet Analytics
 *
 */
public enum LanguageTagEnum implements LrsEnum {
    // Language Tags
    EN_US("en-US"),
    EN("en");
    
    // Construction
    private String value;
    LanguageTagEnum(String s) throws IllegalArgumentException {
        this.value = s;
    }
    @Override
    public String getValue() {
        return value;
    }
}
