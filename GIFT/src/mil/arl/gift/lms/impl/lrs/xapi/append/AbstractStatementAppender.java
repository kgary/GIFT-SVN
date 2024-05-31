package mil.arl.gift.lms.impl.lrs.xapi.append;

import mil.arl.gift.common.util.StringUtils;

/**
 * Parent Class for Statement Appenders; requires appender authors to provide
 * a name and description for an appender. Implements StatementAppender interface
 * to attach public appendToStatement method to all extending classes. That method
 * is called within AbstractStatementGenerator's generateStatement method. Appenders
 * are attached to Generators.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractStatementAppender implements StatementAppender {
    /** name of the appender */
    private String appenderName;
    /** appender description */
    private String info;
    /**
     * Enforces name and description convention for all Statement Appenders
     * 
     * @param name - name of the appender
     * @param description - brief description of how the appender mutates xAPI Statement
     */
    protected AbstractStatementAppender(String name, String description) {
        if(StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Appender name can not be null or empty!");
        }
        this.appenderName = name;
        if(StringUtils.isBlank(description)) {
            throw new IllegalArgumentException("Appender description can not be null or empty!");
        }
        this.info = description; 
    }
    /**
     * @return name of the appender
     */
    public String getName() {
        return appenderName;
    }
    /**
     * @return brief description of how the appender mutates xAPI Statement
     */
    public String getInfo() {
        return info;
    }
}
