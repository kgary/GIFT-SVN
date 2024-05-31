package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import java.net.URI;
import java.util.List;
import com.rusticisoftware.tincan.Verb;
import mil.arl.gift.lms.impl.lrs.xapi.profile.AbstractStatementTemplate;
import mil.arl.gift.lms.impl.lrs.xapi.profile.ActivityTypeConcept;
import mil.arl.gift.lms.impl.lrs.xapi.profile.AttachmentUsageTypeConcept;
import mil.arl.gift.lms.impl.lrs.xapi.profile.StatementTemplateRule;
import mil.arl.gift.lms.impl.lrs.xapi.profile.VerbConcept;

/**
 * Helper methods for Statement Templates.
 * 
 * @author Yet Analytics
 *
 */
public interface StatementTemplateComponent {
    
    public VerbConcept getVerbConcept();
    public void setVerbConcept(VerbConcept verbConcept);
    public Verb getVerb();
    public URI getVerbId();   
    
    public ActivityTypeConcept getObjectActivityType();
    public void setObjectActivityType(ActivityTypeConcept activityTypeConcept);
    
    public List<ActivityTypeConcept> getGroupingActivityType();
    public void setGroupingActivityType(List<ActivityTypeConcept> groupingActivityTypeConcepts);
    public void addGroupingActivityType(ActivityTypeConcept groupingActivityTypeConcept);
    
    public List<ActivityTypeConcept> getParentActivityType();
    public void setParentActivityType(List<ActivityTypeConcept> parentActivityTypeConcepts);
    public void addParentActivityType(ActivityTypeConcept parentActivityTypeConcept);
    
    public List<ActivityTypeConcept> getOtherActivityType();
    public void setOtherActivityType(List<ActivityTypeConcept> otherActivityTypeConcepts);
    public void addOtherActivityType(ActivityTypeConcept otherActivityTypeConcept);
    
    public List<ActivityTypeConcept> getCategoryActivityType();
    public void setCategoryActivityType(List<ActivityTypeConcept> categoryActivityTypeConcepts);
    public void addCategoryActivityType(ActivityTypeConcept categoryActivityTypeConcept);
    
    public List<AttachmentUsageTypeConcept> getAttachmentUsageType();
    public void setAttachmentUsageType(List<AttachmentUsageTypeConcept> attachmentUsageTypes);
    public void addAttachmentUsageType(AttachmentUsageTypeConcept attachmentUsageType);
    
    public List<AbstractStatementTemplate> getObjectStatementRefTemplate();
    public void setObjectStatementRefTemplate(List<AbstractStatementTemplate> statementTemplates);
    public void addObjectStatementRefTemplate(AbstractStatementTemplate statementTempalte);
    
    public List<AbstractStatementTemplate> getContextStatementRefTemplate();
    public void setContextStatementRefTemplate(List<AbstractStatementTemplate> statementTemplates);
    public void addContextStatementRefTemplate(AbstractStatementTemplate statementTemplate);
    
    public List<StatementTemplateRule> getRules();
    public void setRules(List<StatementTemplateRule> rules);
    public void addRule(StatementTemplateRule rule);
}
