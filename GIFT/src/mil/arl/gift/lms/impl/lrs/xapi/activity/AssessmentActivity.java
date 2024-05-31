package mil.arl.gift.lms.impl.lrs.xapi.activity;

import java.net.URISyntaxException;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.ActivityDefinition;
import com.rusticisoftware.tincan.Statement;
import generated.course.ConceptNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;

/**
 * Activity representation of an Assessment from the MOM xAPI Profile
 * 
 * @author Yet Analytics
 *
 */
public class AssessmentActivity extends AbstractGiftActivity {
    
    private static final String slug = "course.concept";
    
    /**
     * Creation of Activity identifier
     * 
     * @param conceptName - string which makes up the trailing part of identifier
     * 
     * @return Activity identifier containing the passed in conceptName
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier from passed in conceptName
     */
    public static String createAssessmentId(String conceptName) throws LmsXapiActivityException {
        return createId(slug, conceptName.toLowerCase());
    }
    
    /**
     * Creation of Assessment Activity with identifier, display name and description
     * 
     * @param assessmentName - name of assessment used within identifier creation and set as display name
     * @param assessmentDescription - string to set as Activity description
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier from passed in assessmentName
     */
    public AssessmentActivity(String assessmentName, String assessmentDescription) throws LmsXapiActivityException {
        super(createAssessmentId(assessmentName), assessmentName, assessmentDescription);
    }
    
    /**
     * Creation of Assessment Activity with identifier and display name
     * 
     * @param assessmentName - name of assessment used within identifier and set as display name
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier from passed in assessmentName
     */
    public AssessmentActivity(String assessmentName) throws LmsXapiActivityException {
        super(createAssessmentId(assessmentName), assessmentName);
    }
    
    /**
     * Create Assessment Activity from Concept Performance State
     * 
     * @param concept - Concept Performance State to create activity from
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public AssessmentActivity(ConceptPerformanceState concept) throws LmsXapiActivityException {
        this(concept.getState().getName());
    }
    
    /**
     * Create Assessment Activity from Intermediate Concept Performance State
     * 
     * @param intermediateConcept - Intermediate Concept Performance State to create activity from
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public AssessmentActivity(IntermediateConceptPerformanceState intermediateConcept) throws LmsXapiActivityException {
        this(intermediateConcept.getState().getName());
    }
    
    /**
     * Create Assessment Activity from Task Performance State
     * 
     * @param task - Task Performance State to create activity from
     * 
     * @throws LmsXapiActivityException when unable to create activity id
     */
    public AssessmentActivity(TaskPerformanceState task) throws LmsXapiActivityException {
        this(task.getState().getName());
    }
    
    /**
     * Creation of Assessment Activity with identifier, display name and description from GradedScoreNode 
     * 
     * @param conceptNode - Graded Score Node to convert to Assessment Activity
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier from GradedScoreNode's name
     */
    public AssessmentActivity(GradedScoreNode conceptNode) throws LmsXapiActivityException {
        this(conceptNode.getName(), conceptNode.getFullName());
    }
    
    /**
     * Creation of Assessment Activity whose identifier is the Graded Score Nodes authoritative resource id and
     * with display name and description from corresponding ConceptNode
     * 
     * @param conceptNode - Graded Score Node with authoritative resource identifier
     * @param cnode - associated ConceptNode used to set Activity display name and description
     * 
     * @throws LmsXapiActivityException when unable to create Activity identifier from authoritative resource identifier
     */
    public AssessmentActivity(GradedScoreNode conceptNode, ConceptNode cnode) throws LmsXapiActivityException {
        this(conceptNode);
        ActivityDefinition d = getDefinition();
        if(cnode != null && cnode.getAuthoritativeResource() != null && cnode.getAuthoritativeResource().getId() != null) {
            String competencyId = cnode.getAuthoritativeResource().getId();
            if(StringUtils.isNotBlank(competencyId)) {
                try {
                    d.setMoreInfo(competencyId);
                } catch (URISyntaxException e) {
                    throw new LmsXapiActivityException("unable to set authoritative resource id as moreInfo!", e);
                }
            }
        }
        this.setDefinition(d);
    }
    
    /**
     * Wrapper for creation of Assessment Activity from existing Activity
     * 
     * @param a - Activity to convert to Assessment Activity
     * 
     * @throws URISyntaxException when passed in activity does not contain valid identifier
     */
    public AssessmentActivity(Activity a) throws URISyntaxException {
        super(a);
    }

    @Override
    public Activity parseFromStatement(Statement statement) throws LmsXapiActivityException {
        // Assessment Activity can be Statement Object
        Activity match = parseFromStatementTarget(statement.getObject()); 
        if(match != null) {
            return match;
        }
        // Assessment Activity can be found within Other Context Activities or within Parent Context Activities
        match = parseFromCollection(ContextActivitiesHelper.getOtherActivities(statement.getContext()));
        if(match != null) {
            return match;
        } else {
            return parseFromCollection(ContextActivitiesHelper.getParentActivities(statement.getContext()));
        }
    }
}
