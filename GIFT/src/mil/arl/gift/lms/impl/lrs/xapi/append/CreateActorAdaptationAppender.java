package mil.arl.gift.lms.impl.lrs.xapi.append;

import com.rusticisoftware.tincan.Context;
import generated.dkf.EnvironmentAdaptation.CreateActors;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates CreateActors Extension from the Environment Adaptation and adds
 * to xAPI Statement as Context Extension.
 * 
 * @author Yet Analytics
 *
 */
public class CreateActorAdaptationAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Create Actors Environment Adaptation Context Extension Appender";
    /** appender description */
    private static final String appenderInfo = "Attaches the Created Actor Context Extension";
    /** Environment Adaptation to create Extension JSON from */
    private CreateActors adaptation;
    /** Context Extension from xAPI Profile */
    private ItsContextExtensionConcepts.CreatedActor extension;
    /**
     * Parses Context Extension from xAPI Profile
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    private CreateActorAdaptationAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.extension = ItsContextExtensionConcepts.CreatedActor.getInstance();
    }
    /**
     * Sets Create Actors Environment Adaptation
     * 
     * @param createActors - Environment Adaptation to create Extension JSON from
     * 
     * @throws LmsXapiProfileException when unable to parse Context Extension from xAPI Profile
     */
    public CreateActorAdaptationAppender(CreateActors createActors) throws LmsXapiProfileException {
        this();
        if(createActors == null) {
            throw new IllegalArgumentException("Create Actors Environment Adaptation can not be null!");
        }
        this.adaptation = createActors;
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context context = statement.getContext();
        extension.addToContext(context, adaptation);
        statement.setContext(context);
        return statement;
    }
}
