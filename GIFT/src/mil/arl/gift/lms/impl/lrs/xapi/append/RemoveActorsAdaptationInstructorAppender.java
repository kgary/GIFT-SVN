package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.util.ArrayList;
import java.util.List;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Group;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates xAPI Group from collection of Remove Actor targets. When the collection of targets is empty,
 * the xAPI Statement is not modified. If there are targets, the resulting Group is added as Instructor.
 * 
 * @author Yet Analytics
 *
 */
public class RemoveActorsAdaptationInstructorAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Remove Actors Environment Adaptation Appender";
    /** appender description */
    private static final String appenderInfo = "Creates xAPI Group from targets of the Environment Adaptation and set as Instructor";
    /** xAPI Agents per Environment Adaptation target */
    private List<Agent> targetActors;
    /**
     * Creates collection of xAPI Agents from targets
     * 
     * @param targets - Collection of names from Environment Adaptation
     */
    public RemoveActorsAdaptationInstructorAppender(List<String> targets) throws LmsXapiAppenderException {
        super(appenderName, appenderInfo);
        if(targets == null) {
            throw new IllegalArgumentException("targets can not be null!");
        }
        List<Agent> member = new ArrayList<Agent>(targets.size());
        for(String target : targets) {
            Agent agent;
            try {
                agent = PersonaHelper.createMboxAgent(target);
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiAppenderException("Unable to create Agent from target removed actor name! "+target, e);
            }
            member.add(agent);
        }
        this.targetActors = member;
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context context = statement.getContext();
        if(CollectionUtils.isNotEmpty(targetActors)) {
            Group instructor = new Group();
            instructor.setName("Removed Actors");
            instructor.setMembers(targetActors);
            context.setInstructor(instructor);
        }
        statement.setContext(context);
        return statement;
    }
}
