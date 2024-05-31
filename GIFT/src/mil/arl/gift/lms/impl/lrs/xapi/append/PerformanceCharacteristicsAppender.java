package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.util.ArrayList;
import java.util.List;
import com.rusticisoftware.tincan.Context;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

public class PerformanceCharacteristicsAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Performance Characteristics Context Extension Appender";
    /** appender description */
    private static final String appenderInfo = "Creates Performance Characteristics Extension representing TaskScoreNode(s) and/or EnvironmentControl(s) and/or"
            + "TaskPerformanceState(s) and adds to xAPI Statement as Context Extension";
    /** class which handles conversion from GIFT objects to Extension */
    protected ItsContextExtensionConcepts.PerformanceCharacteristics contextExtension;
    /** collection of TaskScoreNode &&|| TaskPerformanceState &&|| EnvironmentControl to represent within extension */
    protected List<Object> tasksAndOrConditions;
    
    /**
     * Parses Performance Characteristics Context Extension from xAPI Profile and initialize
     * empty collection. TaskScoreNode and/or EnvironmentControl and/or TaskPerformanceState
     * added to this collection are represented within the extension.
     * 
     * @throws LmsXapiProfileException when unable to parse extension from xAPI Profile
     */
    public PerformanceCharacteristicsAppender() throws LmsXapiProfileException {
        super(appenderName, appenderInfo);
        this.contextExtension = ItsContextExtensionConcepts.PerformanceCharacteristics.getInstance();
        this.tasksAndOrConditions = new ArrayList<Object>(0);
    }
    
    /**
     * Add TaskScoreNode to represent within extension as a Task  
     * 
     * @param node - TaskScoreNode to represent
     */
    public void addTaskScoreNode(TaskScoreNode node) {
        if(node == null) {
            throw new IllegalArgumentException("node must be non-null!");
        }
        tasksAndOrConditions.add(node);
    }
    
    /**
     * Add one or more TaskScoreNode to represent within extension as Tasks
     * 
     * @param nodes - TaskScoreNodes to represent
     */
    public void addTaskScoreNode(List<TaskScoreNode> nodes) {
        if(nodes == null) {
            throw new IllegalArgumentException("node must be non-null!");
        }
        tasksAndOrConditions.addAll(nodes);
    }
    
    /**
     * Add TaskPerformanceState to represent within extension as a Task
     * 
     * @param state - TaskPerformanceState to represent
     */
    public void addTaskPerformanceState(TaskPerformanceState state) {
        if(state == null) {
            throw new IllegalArgumentException("envControl must be non-null!");
        }
        boolean containsData = state.getStress() != null || 
                StringUtils.isNotBlank(state.getStressReason()) ||
                state.getDifficulty() != null ||
                StringUtils.isNotBlank(state.getDifficultyReason());
        if(containsData) {
            tasksAndOrConditions.add(state);
        }
    }
    
    /**
     * Add one or more TaskPerformanceState to represent within extension as Tasks
     * 
     * @param states - TaskPerformanceStates to represent
     */
    public void addTaskPerformanceState(List<TaskPerformanceState> states) {
        if(states == null) {
            throw new IllegalArgumentException("envControl must be non-null!");
        }
        for(TaskPerformanceState state : states) {
            addTaskPerformanceState(state);
        }
    }
    
    /**
     * Add EnvironmentControl to represent within extension as a Condition
     * 
     * @param envControl - EnvironmentControl to represent
     */
    public void addEnvironmentControl(EnvironmentControl envControl) {
        if(envControl == null) {
            throw new IllegalArgumentException("envControl must be non-null!");
        }
        if(envControl.getStress() != null) {
            tasksAndOrConditions.add(envControl);
        }   
    }
    
    /**
     * Add one or more EnvironmentControl to represent within extension as Conditions
     * 
     * @param envControls - EnvironmentControls to represent
     */
    public void addEnvironmentControl(List<EnvironmentControl> envControls) {
        if(envControls == null) {
            throw new IllegalArgumentException("envControl must be non-null!");
        }
        for(EnvironmentControl envControl : envControls) {
            addEnvironmentControl(envControl);
        }
    }

    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context context = statement.getContext();
        try {
            contextExtension.addToContext(context, tasksAndOrConditions);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiAppenderException("Unable to append Performance Characterstic Context Extension to statement!", e);
        }
        statement.setContext(context);
        return statement;
    }
}
