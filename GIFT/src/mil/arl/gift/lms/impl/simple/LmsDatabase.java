/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.Concepts;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.LMSConnectionInfo;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.course.AssessmentChainOfCustody;
import mil.arl.gift.common.course.CourseRecordRef;
import mil.arl.gift.common.course.CourseRecordRef.IntCourseRecordRefId;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.score.RawScore;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.ta.request.EnvironmentControl;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.AbstractLms;
import mil.arl.gift.lms.impl.common.Assessment;
import mil.arl.gift.lms.impl.common.LmsDomainSessionException;
import mil.arl.gift.lms.impl.common.LmsException;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.lms.impl.common.LmsStatementIdException;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.simple.db.LMSDatabaseManager;
import mil.arl.gift.lms.impl.simple.db.table.AbstractScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.GradedScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.RawScoreNode;
import mil.arl.gift.lms.impl.simple.db.table.ScoreMetadata;
import mil.arl.gift.net.api.message.DomainSessionMessage;

/**
 * This class uses a database to manage LMS entries.
 * 
 * @author mhoffman
 *
 */
public class LmsDatabase extends AbstractLms {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(LmsDatabase.class);
    
    private static List<Assessment> EMPTY_ASSESSMENTS = new ArrayList<>();
    
    /** string delimiter for the list of usernames in the RawScoreNode table username column */
    private static final String USERNAME_DELIMITER = ",";
    
    /** A list query parameter used to filter course record queries so that only course records with particular domain session
     * IDs are requested by a query */
    private static final String DOMAIN_SESSION_ID_FILTER_PARAM = "dsIds";
    
    /** instance of the database manager to use for database queries */
    private LMSDatabaseManager dbMgr = LMSDatabaseManager.getInstance();
    
    /**
     * Class constructor
     */
    public LmsDatabase(){
        
    }   

    @Override
    public LMSConnectionInfo getConnectionInfo() {
        
        if(connectionInfo == null){
            connectionInfo = new LMSConnectionInfo(getName());
        }
        
        return connectionInfo;
    }

    @Override
    public void connect(generated.lms.Parameters parameters) throws LmsIoException{
        //nothing to do because this LMS implementation doesn't need parameters in this manner as they
        //are stored in the lms.hibernate.cfg.xml file
    }
    
    @Override
    public void disconnect() throws LmsIoException{
        //nothing to do
    }

    @Override
    public void createUser(String userId) 
        throws LmsInvalidStudentIdException, LmsIoException{
                
        throw new LmsIoException("Unable to create a new user in this manner.  Users are associated with their course records in this LMS implementation.", null);
    }
    
    /**
     * Get the LMS course records for the score metadata information provided.
     * 
     * @param scoreMetadatas LMS database entries that contain user history to analyze for a request
     * @param pageStart the index of the first record returned in this request.  For example if the request
     * should return the 5th and onward records, the value should be 4 (zero based index). Must be non-negative.  
     * Zero indicates to start with the first record that satisfies the request requirements.
     * @param pageSize how many records to return, must be non-negative.  Zero indicates to return all records that satisfy the request requirements.
     * @param sortDescending whether to sort the records by date with the latest records first
     * @return LMS records found
     */
    private List<LMSCourseRecord> getCourseRecordsForMetadatas(List<ScoreMetadata> scoreMetadatas, int pageStart, int pageSize, final boolean sortDescending){
        
        if(scoreMetadatas.isEmpty()){
            return new ArrayList<>(0);
        }
        
        if (sortDescending) {

            Collections.sort(scoreMetadatas, new Comparator<ScoreMetadata>() {
                @Override
                public int compare(ScoreMetadata o1, ScoreMetadata o2) {

                    return o2.getTime().compareTo(o1.getTime());
                }
            });

        } else {

            Collections.sort(scoreMetadatas, new Comparator<ScoreMetadata>() {
                @Override
                public int compare(ScoreMetadata o1, ScoreMetadata o2) {

                    return o1.getTime().compareTo(o2.getTime());
                }
            });
        }

        List<LMSCourseRecord> records;
        if (pageSize > 0) {

            records = new ArrayList<>(pageSize);

        } else {

            records = new ArrayList<>(scoreMetadatas.size());
        }
        
        //
        // group by domain session in order to make sure all records are returned for a course execution
        //
        Map<Integer, List<ScoreMetadata>> domainSessionResults = new HashMap<>();
        for (ScoreMetadata scoreMetadata : scoreMetadatas) {
            
            int dsId = scoreMetadata.getDomainSessionId();
            List<ScoreMetadata> dsScores = domainSessionResults.get(dsId);
            if(dsScores == null){
                dsScores = new ArrayList<>();
                domainSessionResults.put(dsId, dsScores);
            }
            
            dsScores.add(scoreMetadata);
        }
        
        //need to sort each domain session group by timestamp
        List<List<ScoreMetadata>> dsScoreGroups = new ArrayList<List<ScoreMetadata>>(domainSessionResults.values());
        
        Collections.sort(dsScoreGroups, new Comparator<List<ScoreMetadata>>() {

            @Override
            public int compare(List<ScoreMetadata> o1, List<ScoreMetadata> o2) {
                
                if(sortDescending){
                    return o2.get(0).getTime().compareTo(o1.get(0).getTime());
                    
                } else {
                    return o1.get(0).getTime().compareTo(o2.get(0).getTime());
                }
            }
        });

        int scoreIndex = 0;  //how many domain sessions have been looked at
        int scoresAdded = 0;  //how many domain sessions have been included

        //build hierarchy for each domain session
        for(List<ScoreMetadata> dsScores : dsScoreGroups){
            
            boolean addedRecord = false;  //whether a domain session record was added
            
            if (scoreIndex >= pageStart) {
                
                for (ScoreMetadata scoreMetadata : dsScores) {
        
                    if (scoreMetadata.getRoot() != null) {
                        //found a valid entry
        
                        mil.arl.gift.common.score.GradedScoreNode gradedScoreNode = new mil.arl.gift.common.score.GradedScoreNode(scoreMetadata.getRoot().getName());
                        
                        if(scoreMetadata.getRoot().getPerformanceNodeId() != null){
                            gradedScoreNode.setPerformanceNodeId(scoreMetadata.getRoot().getPerformanceNodeId());
                        }
    
                        //get all children starting at this root node and recursively build the tree structure.
                        getChildren(scoreMetadata.getRoot(), gradedScoreNode);
                        
                        // set the node assessment level                        
                        try {
                            AssessmentLevelEnum assessment = AssessmentLevelEnum.valueOf(scoreMetadata.getRoot().getGrade());
                            gradedScoreNode.updateAssessment(assessment);
                        }catch(@SuppressWarnings("unused") EnumerationNotFoundException e) {
                            // Legacy value - convert old PassFailEnum name to new AssessmentLevelEnum (#5197), other enums cases default to unknown            
                            AssessmentLevelEnum assessment = AssessmentLevelEnum.fromPassFailEnum(scoreMetadata.getRoot().getGrade());   
                            gradedScoreNode.updateAssessment(assessment);
                        }
    
                        //only root nodes create new course records
                        if (scoreMetadata.getRoot().getParent() == null) {
    
                            LMSCourseRecord aRecord = new LMSCourseRecord(
                                    CourseRecordRef.buildCourseRecordRefFromInt(scoreMetadata.getScoreMetadataId()), 
                                    scoreMetadata.getDomainName(), gradedScoreNode, scoreMetadata.getTime());
                            aRecord.setLMSConnectionInfo(getConnectionInfo());
                            aRecord.setGiftEventId(scoreMetadata.getDomainSessionId());
                            records.add(aRecord);
                            addedRecord = true;
                        }
        
                    }//end if

                } //end for
            }//end if
            
            //
            // 
            //
            
            if(addedRecord){
                scoresAdded++;
            }
            
            scoreIndex += 1; 
            
            if (pageSize > 0 && scoresAdded >= pageSize) {
                //added enough records for this page
                break;
            }            

        } //end for

        return records;
    }
    
    @Override
    public List<LMSCourseRecord> getCourseRecords(String studentId, int userId, int pageStart, int pageSize, 
            boolean sortDescending, Set<String> domainIds, Set<Integer> domainSessionIds)
            throws LmsIoException, LmsInvalidStudentIdException {
                
        List<LMSCourseRecord> records;
        if(domainIds != null && !domainIds.isEmpty()){
            //gather records for each domain specified
            
            records = new ArrayList<>();
            for(String domainName : domainIds){
            
                try{
                    List<ScoreMetadata> scoreMetadatas = dbMgr.getCourseRecords(userId, domainName, domainSessionIds, pageStart, pageSize, sortDescending);

                    records.addAll(getCourseRecordsForMetadatas(scoreMetadatas, 0, scoreMetadatas.size(), sortDescending));    
                }catch(@SuppressWarnings("unused") Exception e){
                    //failed to retrieve record, continue to next domain (best effort)
                    //Not logging to prevent possible flooding of GIFT log files
                }
            }
        }else{
            //gather records for any domain 
            
            List<ScoreMetadata> scoreMetadatas = dbMgr.getCourseRecords(userId, null, domainSessionIds, pageStart, pageSize, sortDescending);

            records = getCourseRecordsForMetadatas(scoreMetadatas, pageStart, pageSize, sortDescending);
        }        

        return records;
    }
    
    @Override
    public LMSCourseRecord getCourseRecord(String studentId, int userId, CourseRecordRef recordRef)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {
        
        if(recordRef == null){
            throw new IllegalArgumentException("The record ref is null");
        }else if(!(recordRef.getRef() instanceof IntCourseRecordRefId)){
            throw new IllegalArgumentException("The record ref id must be an integer for LmsDatabase");
        }
        
        int recordId = ((IntCourseRecordRefId)recordRef.getRef()).getRecordId();

        ScoreMetadata scoreMetadata = dbMgr.selectRowById(recordId, ScoreMetadata.class);
        
        if(scoreMetadata == null){
            //unable to find record
            throw new LmsInvalidCourseRecordException("Unable to find course record with id of "+recordId+".", null);
        }

        //build hierarchy for score tree
        if (scoreMetadata.getRoot() != null) {

            mil.arl.gift.common.score.GradedScoreNode gradedScoreNode = new mil.arl.gift.common.score.GradedScoreNode(scoreMetadata.getRoot().getName());

            //get all children starting at this root node and recursively build the tree structure.
            getChildren(scoreMetadata.getRoot(), gradedScoreNode);
            
            // set the node assessment level                        
            try {
                AssessmentLevelEnum assessment = AssessmentLevelEnum.valueOf(scoreMetadata.getRoot().getGrade());
                gradedScoreNode.updateAssessment(assessment);
            }catch(@SuppressWarnings("unused") EnumerationNotFoundException e) {
                // Legacy value - convert old PassFailEnum name to new AssessmentLevelEnum (#5197), other enums cases default to unknown            
                AssessmentLevelEnum assessment = AssessmentLevelEnum.fromPassFailEnum(scoreMetadata.getRoot().getGrade());   
                gradedScoreNode.updateAssessment(assessment);
            }

            //only root nodes create new course records
            if (scoreMetadata.getRoot().getParent() == null) {

                LMSCourseRecord aRecord = new LMSCourseRecord(
                        CourseRecordRef.buildCourseRecordRefFromInt(scoreMetadata.getScoreMetadataId()),
                        scoreMetadata.getDomainName(), gradedScoreNode, scoreMetadata.getTime());
                aRecord.setLMSConnectionInfo(getConnectionInfo());
                return aRecord;
            }

        }

        throw new LmsInvalidCourseRecordException("Unable to find root node of course record with id of "+recordId+".", null);
    }
    
    @Override
    public List<LMSCourseRecord> getLatestRootCourseRecordsPerDomain(String studentId, int userId, List<Integer> domainSessionIds)
            throws LmsIoException, LmsInvalidStudentIdException {

        String queryString = "from ScoreMetadata where userId = "+userId;
        
        Map<String, Collection<? extends Object>> listParams = null;
        if(CollectionUtils.isNotEmpty(domainSessionIds)) {
            
            /* If a set of domain session IDs to filter by has been provided, modify the query so that only course 
             * records with domain session IDs within that set are requested */
            listParams = new HashMap<>();
            listParams.put(DOMAIN_SESSION_ID_FILTER_PARAM, domainSessionIds);
            queryString += " and domainSessionId in (:" + DOMAIN_SESSION_ID_FILTER_PARAM + ")";
        }
        
        // List<LMSCourseRecord> records = null;
        List<LMSCourseRecord> records = new ArrayList<LMSCourseRecord>();
        
        //gather records for any domain 
        
        // FIXME: bellow results in - ERROR org.hibernate.hql.PARSER - <AST>:0:0: unexpected end of subtree - when there's no stored history???
        
        List<ScoreMetadata> scoreMetadatas = dbMgr.selectRowsByQuery(ScoreMetadata.class, queryString, -1, -1, listParams);
        
        Map<String, ScoreMetadata> domainNameToMetadata = new HashMap<String, ScoreMetadata>();
        
        for(ScoreMetadata scoreMetadata : scoreMetadatas){
            
            if(scoreMetadata.getDomainName() != null && scoreMetadata.getTime() != null){
                
                if(domainNameToMetadata.get(scoreMetadata.getDomainName()) == null 
                        || scoreMetadata.getTime().compareTo(domainNameToMetadata.get(scoreMetadata.getDomainName()).getTime()) == 1){
                    
                    //get the latest score metadata for each domain name
                    domainNameToMetadata.put(scoreMetadata.getDomainName(), scoreMetadata);
                }
            }
        }

        for(ScoreMetadata scoreMetadata : domainNameToMetadata.values()){
            
            //get the root score node without recursively gathering its children, since all we need is the latest root node
            mil.arl.gift.common.score.GradedScoreNode gradedScoreNode = new mil.arl.gift.common.score.GradedScoreNode(scoreMetadata.getRoot().getName());
            
            if(scoreMetadata.getRoot().getPerformanceNodeId() != null){
                gradedScoreNode.setPerformanceNodeId(scoreMetadata.getRoot().getPerformanceNodeId());
            }

            //only root nodes create new course records
            if (scoreMetadata.getRoot().getParent() == null) {

                LMSCourseRecord aRecord = new LMSCourseRecord(
                        CourseRecordRef.buildCourseRecordRefFromInt(scoreMetadata.getScoreMetadataId()),
                        scoreMetadata.getDomainName(), gradedScoreNode, scoreMetadata.getTime());
                aRecord.setLMSConnectionInfo(getConnectionInfo());
                aRecord.setGiftEventId(scoreMetadata.getDomainSessionId());
                
                if(records == null){
                    records = new ArrayList<LMSCourseRecord>();
                }
                
                records.add(aRecord);
            }
        }

        return records;
    }
    
    @Override
    public List<LMSCourseRecord> getCourseRecords(String studentId, int userId, List<CourseRecordRef> recordRefs)
            throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {

        List<LMSCourseRecord> records = new ArrayList<>();
        for (CourseRecordRef recordRef : recordRefs) {
            records.add(getCourseRecord(studentId, userId, recordRef));
        }

        return records;
    }

    /**
     * Recursively gets and adds children to their parent
     *
     * @param parentTable The hibernate table entity that has children
     * @param parent The actual GIFT representation of parentTable
     */
    private void getChildren(GradedScoreNode parentTable, mil.arl.gift.common.score.GradedScoreNode parent) {
        for(AbstractScoreNode nodeTable : parentTable.getChildren()){
            
            mil.arl.gift.common.score.AbstractScoreNode scoreNode = null;
            if(nodeTable instanceof GradedScoreNode){
                scoreNode = new mil.arl.gift.common.score.GradedScoreNode(nodeTable.getName());
                getChildren((GradedScoreNode)nodeTable, (mil.arl.gift.common.score.GradedScoreNode)scoreNode);
                
            }else if(nodeTable instanceof RawScoreNode){
                
                // parse comma delimited list of usernames
                Set<String> usernames = new HashSet<>();
                String usernamesDelimited = ((RawScoreNode) nodeTable).getUsernames();
                String[] usernamesSplit = null;
                if(usernamesDelimited != null){
                    usernamesSplit = usernamesDelimited.split(USERNAME_DELIMITER);
                    usernames.addAll(Arrays.asList(usernamesSplit));
                }
                
                
                //for now only have DefaultRawScore
                RawScore rawscore = new DefaultRawScore(((RawScoreNode)nodeTable).getValue(), ((RawScoreNode)nodeTable).getUnits());
                if(CollectionUtils.isEmpty(usernames)){
                    scoreNode = new mil.arl.gift.common.score.RawScoreNode(nodeTable.getName(), rawscore, 
                            AssessmentLevelEnum.valueOf(((RawScoreNode)nodeTable).getAssessment()));
                }else{
                    scoreNode = new mil.arl.gift.common.score.RawScoreNode(nodeTable.getName(), rawscore, 
                            AssessmentLevelEnum.valueOf(((RawScoreNode)nodeTable).getAssessment()), usernames);
                }
                
            }else{
                logger.error("Found unhandled node table of "+nodeTable+" when trying to build child score object");
                continue;
            }
            
            if(nodeTable.getPerformanceNodeId() != null){
                scoreNode.setPerformanceNodeId(nodeTable.getPerformanceNodeId());
            }
                                  
            parent.addChild(scoreNode);
        }
    }
    
    /**
     * Insert new child score nodes for the parent provided
     * 
     * @param children - child nodes to add
     * @param parent - the parent to the children
     * @param domainSessionId - the domain session id for the domain session which populated the scores
     * @throws LmsInvalidCourseRecordException
     */
    private void insertChildrenNodes(Collection<mil.arl.gift.common.score.AbstractScoreNode> children, GradedScoreNode parent, int domainSessionId) throws LmsInvalidCourseRecordException{
        
        for(mil.arl.gift.common.score.AbstractScoreNode node : children){
            
            if(node.isLeaf()){
                //is a Raw Score node
                
                mil.arl.gift.common.score.RawScoreNode rawNode = (mil.arl.gift.common.score.RawScoreNode)node;
                mil.arl.gift.common.score.RawScore rawScore = rawNode.getRawScore();
                
                rawNode.getParent();
               
                RawScoreNode rawNodeTable = new RawScoreNode(node.getName(), rawScore.getValueAsString(), 
                        rawScore.getUnitsLabel(), rawNode.getAssessment().toString(), parent, StringUtils.join(USERNAME_DELIMITER, rawNode.getUsernames()) );
                try{
                    insertScoreNodeRow(node, rawNodeTable);
                }catch(Exception e){
                    throw new LmsInvalidCourseRecordException("Unable to insert raw score node record, check log for more details", e);
                }
                
            }else{
                //is a parent node, therefore not a raw score node
                
                mil.arl.gift.common.score.GradedScoreNode gradeNode = (mil.arl.gift.common.score.GradedScoreNode)node;
                
                GradedScoreNode gradeNodeTable = new GradedScoreNode(node.getName(), parent, gradeNode.getAssessment().getName(), domainSessionId);
                try{
                    insertScoreNodeRow(node, gradeNodeTable);
                }catch(Exception e){
                    throw new LmsInvalidCourseRecordException("Unable to insert graded score node record, check log for more details", e);
                }
                
                insertChildrenNodes(gradeNode.getChildren(), gradeNodeTable, domainSessionId);
            }
        }
    }
    
    /**
     * Insert new child score nodes into the database
     * 
     * @param node - the score node common information
     * @param scoreNode - the table score node 
     * @throws Exception if there was a problem inserting
     */
    private void insertScoreNodeRow(mil.arl.gift.common.score.AbstractScoreNode node, AbstractScoreNode scoreNode) throws Exception{
        
        if(node.getPerformanceNodeId() != null){
            scoreNode.setPerformanceNodeId(node.getPerformanceNodeId());
        }
        
        dbMgr.insertRow(scoreNode);
    }
    
    @Override
    protected CourseRecordRef insertCourseRecord(String studentId, int userId, int domainSessionId, LMSCourseRecord record, Concepts.Hierarchy concepts)
        throws LmsException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException{
        
        mil.arl.gift.common.score.GradedScoreNode root = record.getRoot();
        
        if( !root.isValid() ) {
            logger.error("Attempt to insert invalid ScoreNode hierarchy into database! Course record will not be saved!");
            throw new LmsInvalidCourseRecordException("Invalid Hierarchy", null);
        }
        
        GradedScoreNode gradedScoreNode = new GradedScoreNode(root.getName(), null, root.getAssessment().getName(), domainSessionId);
        try{
            insertScoreNodeRow(root, gradedScoreNode);
        }catch(Exception e){
                throw new LmsInvalidCourseRecordException("Unable to insert graded score node record, check log for more details", e);
        }
        
        insertChildrenNodes(root.getChildren(), gradedScoreNode, domainSessionId);   
        
        ScoreMetadata metadata = new ScoreMetadata(userId, domainSessionId, record.getDate(), gradedScoreNode, record.getDomainName());
        try{
            dbMgr.insertRow(metadata);
        }catch(Exception e){
            throw new LmsInvalidCourseRecordException("Unable to insert metadata record, check log for more details", e);
        }
        
        if(logger.isDebugEnabled()){
            logger.debug("Successfully inserted new course record on domain "+root.getName()+" for user "+userId+" in domain session "+domainSessionId);
        }
        
        CourseRecordRef ref = new CourseRecordRef();
        IntCourseRecordRefId intRefId = new IntCourseRecordRefId(metadata.getScoreMetadataId());
        ref.setRef(intRefId);
        
        return ref;
        
    }   

    @Override
    public List<Assessment> getAssessments(String username)
            throws LmsIoException, LmsInvalidStudentIdException,
            LmsInvalidCourseRecordException {
        return EMPTY_ASSESSMENTS;
    }
    
    @Override
    protected void insertSurveyResult(String studentId, int userId, int domainSessionID, double score, double maxScore,
            Date endTime, String surveyName, String courseName, Map<LearnerStateAttributeNameEnum, String> learnerStates, SubmitSurveyResults surveyResults) {
        // LMS does not store complete survey results. This is handled by the LRS.
    }

    @Override
    public List<AbstractScale> getLearnerStateAttributes(String username, Set<String> courseConcepts, Date sinceWhen) throws LmsIoException, LmsInvalidStudentIdException, LmsInvalidCourseRecordException {
        // LMS does not retrieve learner state attributes because it retrieves full course records.
        // Retrieving learner state attributes is handled by the LRS.
        return null;
    }    

    @Override
    protected void insertLearnerState(String studentId, int userId, int domainSessionId, LearnerState learnerState) {
        // LMS does not store learner state info at this time        
    }    

    @Override
    protected void insertPedagogicalRequest(String studentId, int userId, int domainSessionId,
            PedagogicalRequest pedagogicalRequest) {
        // LMS does not store ped request info at this time        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LMSDatabase: ");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }

    @Override
    protected void insertDomainSessionInit(DomainSessionMessage message) throws LmsDomainSessionException {
        // no-op
    }

    @Override
    protected void insertDomainSessionStarted(DomainSessionMessage message) 
            throws LmsDomainSessionException, LmsIoException, LmsStatementIdException, LmsXapiAgentException, LmsXapiActivityException {
        // no-op   
    }

    @Override
    protected void insertDomainSessionClose(DomainSessionMessage message) 
            throws LmsDomainSessionException, LmsIoException, LmsStatementIdException, LmsXapiAgentException, LmsXapiActivityException {
        // no-op
    }

    @Override
    public void pastSessionLearnerStateUpdated(AbstractKnowledgeSession knowledgeSession, DomainSession domainSession,
            LearnerState newLearnerState, LearnerState oldLearnerState) throws LmsIoException {
        // no-op
        
    }

    @Override
    protected void insertKnowledgeSessionDetails(String studentId, int userId, int domainSessionId,
            KnowledgeSessionCreated knowledgeSessionCreated) {
        // no-op
    }

    @Override
    protected void insertLessonCompleted(String studentId, int userId, int domainSessionId,
            LessonCompleted lessonCompleted) {
        // no-op        
    }

    @Override
    public void pastSessionCourseRecordUpdated(AbstractKnowledgeSession knowledgeSession, AssessmentChainOfCustody chainOfCustody,
            LMSCourseRecord newCourseRecord, LMSCourseRecord oldCourseRecord, Concepts.Hierarchy concepts) throws LmsIoException {
        // no-op         
    }

    @Override
    protected void insertEnvironmentAdaptation(String studentId, int userId, int domainSessionId,
            EnvironmentControl eControl) {
        // no-op         
    }

}