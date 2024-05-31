/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.RequestDoNothingTactic;
import mil.arl.gift.common.RequestInstructionalIntervention;
import mil.arl.gift.common.RequestPerformanceAssessment;
import mil.arl.gift.common.RequestScenarioAdaptation;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ActiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo.AdvancementConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.BranchAdpatationStrategyTypeInterface;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ConstructiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.InteractiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.MetadataAttributeItem;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.PassiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ProgressionInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;

/**
 * This class represents a pedagogical request event that can be included in an ERT report.  It has the logic to
 * convert a pedagogical request object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class PedagogicalRequestEvent extends DomainSessionEvent {
    
    private static final String PERFORMANCE_ASSESSMENT = "RequestPerformanceAssessment";
    private static final String SCENARIO_ADAPT = "RequestScenarioAdaptation";
    private static final String INSTRUCTIONAL_INTERVENTION = "RequestInstructionalIntervention";
    private static final String DO_NOTHING = "RequestDoNothing";
    private static final String BRANCH_ADAPTATION = "RequestBranchAdaptation";
    
    private static final String ADVANCEMENT_INFO_BRANCH_ADAPTATION_COL_NAME = "Expert Knowledge";
    private static final String PROGRESS_INFO_BRANCH_ADAPTATION_COL_NAME = "Goto Next Adaptive Courseflow Phase";
    private static final String INTERACTIVE_ACTIVITY_TYPE_CELL_VALUE = "Interactive";
    private static final String CONSTRUCTIVE_ACTIVITY_TYPE_CELL_VALUE = "Constructive";
    private static final String ACTIVE_ACTIVITY_TYPE_CELL_VALUE = "Active";

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Class constructor - set attributes and parse ped request event.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param pedRequest the request content
     */
    public PedagogicalRequestEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, PedagogicalRequest pedRequest) {
        super(MessageTypeEnum.PEDAGOGICAL_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);
               
        if(pedRequest == null){
            throw new IllegalArgumentException("The pedagogical request can't be null.");
        }
        parseEvent(pedRequest);
    }
    
    /**
     * Gather information on the columns and cell content for the content of the ped request
     * 
     * @param pedRequest
     */
    private void parseEvent(PedagogicalRequest pedRequest){
        
        for(List<AbstractPedagogicalRequest> requestList : pedRequest.getRequests().values()){
            for(AbstractPedagogicalRequest request : requestList){
            
                if(request instanceof RequestPerformanceAssessment){
                    RequestPerformanceAssessment rpa = (RequestPerformanceAssessment)request;
                    EventReportColumn col = new EventReportColumn(PERFORMANCE_ASSESSMENT, PERFORMANCE_ASSESSMENT);
                    columns.add(col);
                    cells.add(new Cell(rpa.getStrategyName(), col));
                    
                }else if(request instanceof RequestScenarioAdaptation){
                    
                    RequestScenarioAdaptation rsa = (RequestScenarioAdaptation)request;
                    EventReportColumn col = new EventReportColumn(SCENARIO_ADAPT, SCENARIO_ADAPT);
                    columns.add(col);
                    cells.add(new Cell(rsa.getStrategyName(), col));
                    
                }else if(request instanceof RequestInstructionalIntervention){
                 
                    RequestInstructionalIntervention rii = (RequestInstructionalIntervention)request;
                    EventReportColumn col = new EventReportColumn(INSTRUCTIONAL_INTERVENTION, INSTRUCTIONAL_INTERVENTION);
                    columns.add(col);
                    cells.add(new Cell(rii.getStrategyName(), col));
                    
                } else if(request instanceof RequestDoNothingTactic){
                    
                    RequestDoNothingTactic doNothing = (RequestDoNothingTactic)request;
                    EventReportColumn col = new EventReportColumn(DO_NOTHING, DO_NOTHING);
                    columns.add(col);
                    cells.add(new Cell(doNothing.getStrategyName(), col));
                    
                } else if(request instanceof RequestBranchAdaptation){
                    
                    RequestBranchAdaptation branchAdaptation = (RequestBranchAdaptation)request;

                    EventReportColumn col;
                    
                    BranchAdaptationStrategy strategy = branchAdaptation.getStrategy();
                    BranchAdpatationStrategyTypeInterface strategyType = strategy.getStrategyType();
                    if(strategyType instanceof AdvancementInfo){
                        //Details concepts that the user is an expert on at this point
                        
                        StringBuilder cellContentSB = new StringBuilder();
                        col = new EventReportColumn(ADVANCEMENT_INFO_BRANCH_ADAPTATION_COL_NAME, ADVANCEMENT_INFO_BRANCH_ADAPTATION_COL_NAME);

                        AdvancementInfo advancement = (AdvancementInfo)strategyType;
                        for(AdvancementConcept concept : advancement.getConcepts()){
                            
                            if(cellContentSB.length() > 0){
                                cellContentSB.append(Constants.COMMA);
                            }
                            
                            cellContentSB.append(concept.getConcept()); 
                        }
                        
                        columns.add(col);
                        cells.add(new Cell(cellContentSB.toString(), col));
                        
                    }else if(strategyType instanceof ProgressionInfo){
                        //Details that the next adaptive courseflow phase should be entered (e.g. Example),
                        //could contain additional information like metadata attributes to use to find content.
                        
                        StringBuilder cellContentSB = new StringBuilder();
                        col = new EventReportColumn(PROGRESS_INFO_BRANCH_ADAPTATION_COL_NAME, PROGRESS_INFO_BRANCH_ADAPTATION_COL_NAME);
                        
                        ProgressionInfo progressionInfo = (ProgressionInfo)strategyType;
                        MerrillQuadrantEnum quadrant = progressionInfo.getQuadrant();
                        cellContentSB.append(quadrant).append(" - {");
                        if(progressionInfo.getAttributes() != null){
                            for(MetadataAttributeItem metadataAttributeItem : progressionInfo.getAttributes()){
                                
                                if(metadataAttributeItem.getLabel() != null && !metadataAttributeItem.getLabel().isEmpty()){
                                    cellContentSB.append(metadataAttributeItem.getLabel()).append(":");
                                }
                                
                                cellContentSB.append(metadataAttributeItem.getAttribute().getValue()).append(Constants.COMMA).append(Constants.SPACE);
                                
                            }
                        }
                        
                        cellContentSB.append("}");
                        
                        columns.add(col);
                        cells.add(new Cell(cellContentSB.toString(), col));
                        
                    }else if(strategyType instanceof RemediationInfo){
                        //Details which concepts need remediation and the prioritized list of remediation activities
                        //for each concept.
                        
                        RemediationInfo remediationInfo = (RemediationInfo)strategyType;
                        for(String concept : remediationInfo.getRemediationMap().keySet()){
                            
                            StringBuilder cellContentSB = new StringBuilder();
                            col = new EventReportColumn("Remediation - "+concept, "Remediation - "+concept);
                                                     
                            List<AbstractRemediationConcept> remediations = remediationInfo.getRemediationMap().get(concept);
                            for(AbstractRemediationConcept remediation : remediations){
                                
                                if(cellContentSB.length() > 0){
                                    cellContentSB.append(Constants.COMMA).append(Constants.SPACE);
                                }
                                
                                if(remediation instanceof InteractiveRemediationConcept){
                                    
                                    cellContentSB.append(INTERACTIVE_ACTIVITY_TYPE_CELL_VALUE);
                                    
                                }else if(remediation instanceof ConstructiveRemediationConcept){
                                    
                                    cellContentSB.append(CONSTRUCTIVE_ACTIVITY_TYPE_CELL_VALUE);
                                    
                                }else if(remediation instanceof ActiveRemediationConcept){
                                    
                                    cellContentSB.append(ACTIVE_ACTIVITY_TYPE_CELL_VALUE);
                                    
                                }else if(remediation instanceof PassiveRemediationConcept){
                                    
                                    cellContentSB.append("Passive - {");
                                    PassiveRemediationConcept passiveRemediation = (PassiveRemediationConcept)remediation;
                                    for(MetadataAttributeItem metadataAttributeItem : passiveRemediation.getAttributes()){
                                        
                                        if(metadataAttributeItem.getLabel() != null && !metadataAttributeItem.getLabel().isEmpty()){
                                            cellContentSB.append(metadataAttributeItem.getLabel()).append(":");
                                        }
                                        
                                        cellContentSB.append(metadataAttributeItem.getAttribute().getValue()).append(Constants.COMMA).append(Constants.SPACE);
                                        
                                    }
                                    
                                    cellContentSB.append("}");
                                }else{
                                    
                                    cellContentSB.append("unknown remediation type - ").append(remediation);
                                }
                            }
                            
                            columns.add(col);
                            cells.add(new Cell(cellContentSB.toString(), col));
                        }
                        
                    }else{
                        StringBuilder cellContentSB = new StringBuilder();
                        col = new EventReportColumn(BRANCH_ADAPTATION, BRANCH_ADAPTATION);
                        cellContentSB.append("unknown strategy of ").append(strategyType);
                        
                        columns.add(col);
                        cells.add(new Cell(cellContentSB.toString(), col));
                    }
                    

                }
            }//end for
        }//end for
    }

    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PedagogicalRequestEvent: ");
        sb.append(super.toString());
        
        sb.append(", columns = {");
        for(EventReportColumn column : columns){
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
