/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dbio;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.table.DbCategory;
import mil.arl.gift.ums.db.table.DbListOption;
import mil.arl.gift.ums.db.table.DbOptionList;
import mil.arl.gift.ums.db.table.DbPropertyKey;
import mil.arl.gift.ums.db.table.DbQuestion;
import mil.arl.gift.ums.db.table.DbQuestionProperty;
import mil.arl.gift.ums.db.table.DbQuestionPropertyValue;
import mil.arl.gift.ums.db.table.DbQuestionType;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;


/**
 * Utility class to import survey questions using flat files as input. The current implementation only supports multiple choice
 * questions with a single correct answer.
 * 
 * It should be easy to expand to support fill-in-the-blank questions and questions with multiple correct answers.
 * 
 * Import requires two CSV files. The first CSV file is the questions file, with one question per line. 
 * Refer to QuestionBean class to see the supported format. The second CSV file is the answers file, 
 * with one answer per line. Refer to the AnswerBean class to see the supported format.
 * 
 * NOTES: 
 * 
 * Though expandable, this class is very much a point solution for the METER CLS question import.
 * 
 * Currently Question Categories are imported independent of questions. This is probably not desirable!  Better would be only to insert categories with question import.
 * 
 * Questions also must pass a validation check.  No attempt will be made to import questions that fail the validation check. See the isValidQuestion method for details.
 * 
 * Questions failing validation are printed at the end of the run.
 * 
 * Database transactions are performed at the question level.  If any part of a question import fails, all database operations associated with that question will be rolled back.
 * 
 * Relevant parts of questions that fail at the database/hibernate level are printed out in line. 
 * 
 * Ideas for future:
 * 
 *   Support additional question types.
 *   Support multiple correct answers.
 *   Print out of questions that faile database import.
 *   Optionally make the import an "all or nothing" proposition (all questions succeed or else none succeed.  Currently partial imports are possible).
 *   Attempt to locate existing questions to avoid duplicate imports.
 *   Insert categories as part of question import so that categories are only created as needed.
 *   Others? 
 * 
 * 
 * @author cragusa
 */
public class SurveyQuestionImport {
	
	//LEVELS: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE
    //private static final Logger logger = LoggerFactory.getLogger(SurveyQuestionImport.class);
    
    static {
    	
    	//Used by log4j, which is in turn used by the dbMgr.
        PropertyConfigurator.configure(PackageUtil.getConfiguration() + "/tools/dbio/log4j.properties");
        System.setErr(System.out);
    }
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Developer settings -- change these according to your needs.
    // TODO: make some (all) of these developer setting available as command line args or GUI settable values
    //       when developing a generalized import tool.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    /** filename of CSV file containing the questions. Formatted in accordance with QuestionBean. See QuestionBean docs for details. */
    final static String questionsCsvFilename = "D:\\work2\\meter.questions\\fwmeterquestionsunclassified\\CLS09-Questions-FINAL AMEDD-reduced.csv";    
    
    /** filename of CSV file containing the answers. Formatted in accordance with AnswerBean  See AnswerBean docs for details. */
    final static String answersCsvFilename   = "D:\\work2\\meter.questions\\fwmeterquestionsunclassified\\CLS09-Answers-FINAL AMEDD-reduced.csv";
    
    /** Developer flag to use during debugging. If set, the questions are only printed -- not inserted into the database */
    //static final boolean DEBUGGING = false;
    static final boolean DEBUGGING = false;
    
    /** Developer flag used to control verbosity of output */
    static final boolean VERBOSE = true;
    
    /** Developer setting to limit database operations to a small number of question insertions. Useful for testing */
    static final int MAX_NUMBER_OF_QUESTIONS_TO_PROCESS = Integer.MAX_VALUE;
    //static final int MAX_NUMBER_OF_QUESTIONS_TO_PROCESS = 10;
    
    /** String that will be used as category name (i.e. a common QuestionCategory) for all questions imported with the batch. 
     *  Change this according to what you are importing  
     */
    static final String importName = "CLS";
    
    //Regarding the following two flags:
    //   Both can be set to false, in which case the only category will be the importName.
    //   Either can be set to true, and the other set to false. 
    //   If both are set true, then USE_CLS_PRIORITIES_AS_CATEGORIES will take precedence.
    
    /**
     * Flag indicating whether or not CLS priorities (read from question csv file) should be used to categorize questions.
     */
    static final boolean USE_CLS_PRIORITIES_AS_CATEGORIES = true;

    /** Flag indicating whether or not CLS task numbers (read from question csv file) should be used to categorize questions */     
    static final boolean USE_CLS_TASK_NUMBERS_AS_CATEGORIES = false;
    
    
    
    /** Flag indicating if the correct answer should be recorded as a question property. */
    static final boolean RECORD_CORRECT_ANSWER_AS_QUESTION_PROPERTY = true;
    
    /** Flag indicating if CLS priority should be recorded as a question property. */
    static final boolean RECORD_CLS_PRIORITY_AS_QUESTION_PROPERTY = true;
        
    ///////////////////////////////////////////////////////////
    // Below here should be left alone.
    ///////////////////////////////////////////////////////////     
    /** List of QuestionBeans resulting from parsing the questionsCsvInputFilename */
    final static List<QuestionBean> questionList = new ArrayList<>();
    
    /** List of AnswerBeans resulting from parsing the answersCsvInputFilename */
    final static List<AnswerBean>   answerList   = new ArrayList<>();
    
    /** Map of taskNumber to corresponding QuestionCategory instance */
    final static Map<String, DbCategory> taskNumberToCategory = new HashMap<>();
    
    /** Map of priority (String) to corresponding QuestionCategory instance */
    final static Map<String, DbCategory> priorityToCategory = new HashMap<>();
    
    /** Map of questionId to CategorySet */
    final static Map<Integer, Set<DbCategory>> questionIdToCategorySet = new HashMap<>();
    
    /** List of QuestionBeans that were NOT imported because they (or one of their answers) did not pass validation - as of this writing, they fail because either question text or answer text is > 255 characters in length */
    final static List<QuestionBean> rejectedQuestions = new ArrayList<>();
    
    /** List of AnswerBeans that were NOT imported because they (or their corresponding answers) did not pass validation - as of this writing, they fail because either question text or answer text is > 255 characters in length */
    final static List<ArrayList<AnswerBean>> rejectedAnswers   = new ArrayList<>();    
    
    /** reference to the UMS Database Manager -- used to interact with the database via Hibernate */
    final static UMSDatabaseManager dbMgr = UMSDatabaseManager.getInstance();
    
    /** Map of questioId to ArrayList of AnswerBeans */
    final static Map<Integer, ArrayList<AnswerBean>> questionIdToAnswerBeans = new HashMap<>();
     
    
    /**
     * Convenience method for printing arrays of strings.
     * 
     * @param array the string array to print.
     */
    static void stringArrayPrintln(String[] array) {

        for(int i = 0; i<array.length; i++) {
        	
            String prefix = (i>0) ? "," : "";
            System.out.print(prefix + array[i]);
        }

        System.out.println();
    }        

   /**
    * Top-level method that parses a CSV file using SuperCSV library.
    * 
    * @param <T> The bean class type
    * @param csvFilename path/filename to the CSV file.
    * @param cellProcessors cell processors matching the input file in accordance with SuperCSV requirements.
    * @param beanClass the java bean class matching the CSV file format in accordance with SuperCSV requirements.
    * @param beanList a List to hold the beans as they are parsed.
    * @throws Exception if any of the I/O functions fail.
    */
    static <T> void parseCsvFile(String csvFilename, CellProcessor[] cellProcessors, Class<T> beanClass, List<T> beanList ) throws Exception {
    	
    	System.out.println("\nParsing CSV file: " + csvFilename);
        
        ICsvBeanReader inFile = new CsvBeanReader(new FileReader(csvFilename), CsvPreference.EXCEL_PREFERENCE);

        try {

            final String[] header = inFile.getCSVHeader(true);
            
            System.out.print("CSV file header: ");
            stringArrayPrintln(header);

            T bean;

            do {

                bean = inFile.read(beanClass, header, cellProcessors);

                if(bean != null) {       
                    
                    beanList.add(bean);                    
                }

            } while (bean != null);

        } finally {

            inFile.close();
        }
    }
    
    /** 
     * Common error handling method. Update to use log4j logger when appropriate.
     * 
     * @param errorMessage error message to print.
     * @param e associated exception
     */
    static void handleError(String errorMessage, Exception e) {
    	
    	//TODO: use log4j?
    	System.err.println(errorMessage);
    	e.printStackTrace(System.err);
    }
    
    
    /**
     * Retrieves a question category by name.
     * 
     * @param categoryName the name of the category to retrieve.
     * 
     * @return the Category object representing the one in the database, or null if there isn't a matching on in the database.
     */
    static DbCategory retrieveCategoryByName(String categoryName) {
    	
    	DbCategory dbCategory = new DbCategory(categoryName);        
        
        return dbMgr.selectRowByExample(dbCategory, DbCategory.class);        
    }
    
    
    /**
     * Inserts a category in the database using the provided category name.
     * 
     * @param categoryName the name of the category to insert.
     * 
     * @return the QuestionCategory if successful, otherwise null.
     */
    static DbCategory insertCategory(String categoryName) {
        
    	System.out.println("Inserting question category: \"" + categoryName + "\"");
        
    	DbCategory dbCategory = null;
    	
        try {
            
            dbCategory = new DbCategory();            
            dbCategory.setName(categoryName);
            
            dbMgr.insertRow(dbCategory);
            
        } catch (Exception e) {
        	
        	dbCategory = null;
            handleError("Caught an exception while inserting category: \"" + categoryName + "\"", e);
            
        }
        
        return dbCategory;
    }
    
    
    /**
     * Returns a Category object representing a row in the database corresponding to the provided categoryName.  If matching row does not exist, it will be created.
     * 
     * @param categoryName the category name to match (or to use when creating a new Category).
     * @return the Category object.
     */
    static DbCategory retrieveOrInsertCategoryByName(String categoryName) {
    	
    	DbCategory category = retrieveCategoryByName(categoryName);

    	if(category == null) {
    		
    		category = insertCategory(categoryName);    		
    	}
    	
    	return category;
    }
    

    
    /**
     * Helper method to ensure strings are trimmed. Add other "sanitization" functions as needed.
     * 
     * @param text the text to sanitize.
     * @return the sanitized text.
     */
    static String sanitizeText(String text) {
        
        String sanitizedText = text;
        
        if(text != null) {
            
            sanitizedText = sanitizedText.trim();     
        }
        
        return sanitizedText;
    }
    
    /**
     * Validates a question and its answers based based on whatever implementation is provided.
     * Default implementation is that all are valid! (insert appropriate abstraction at some later time). 
     * 
     * @param questionBean QuestionBean containing the question to validate.
     * @param answerBeanList list of AnswerBeans for the provided question. 
     * @return true if question text and all answer texts are valid. 
     */
    static boolean isValidQuestion(QuestionBean questionBean, ArrayList<AnswerBean> answerBeanList) {
        
        boolean valid = true;
        
        //TODO: add any extra validation as required
        //valid = questionBean.getQuestion_text().length() <= 255;
        
        return valid;        
    }    
    
    
   /**
    * Convenience method to print a question with its associated answers.
    * 
    * @param questionBean the questionBean to print.
    * @param answerBeanList list of AnswerBeans to print.
    */
    static void printQuestionWithAnswers(QuestionBean questionBean, ArrayList<AnswerBean> answerBeanList) {
    	
        System.out.println(questionBean);        
        for(AnswerBean answer: answerBeanList) {            
            System.out.println("\t"+answer);
        }
    }
    
    /**
     * Inserts a QuestionProperty into the database.
     * 
     * @param question the question to which the property applies.
     * @param keyName the key of the property.
     * @param propertyValue the value of the property.
     * @param session The database session to insert the question property in
     * @throws Exception if there was a problem inserting
     */
    static void insertQuestionProperty(DbQuestion question, String keyName, String propertyValue, Session session) throws Exception {
    	
    	//get the property key
    	DbPropertyKey propertyKey = new DbPropertyKey(keyName);
    	propertyKey = dbMgr.selectRowByExample(propertyKey, DbPropertyKey.class, session);
        
    	//insert the question property value
        DbQuestionPropertyValue questionPropertyValue = new DbQuestionPropertyValue(propertyValue);        
        dbMgr.insertRow(questionPropertyValue, session);         
                
		DbQuestionProperty property = new DbQuestionProperty();        
		property.setPropertyKey(propertyKey);        
        property.setPropertyValue(questionPropertyValue);
        property.setQuestion(question);   
        
        dbMgr.insertRow(property, session);
    }
    
    /** Insert a QuestionProperty into the database.
     * 
     * @param question the question to which the property applies.
     * @param keyName the key of the property.
     * @param propertyOptionList the value of the property
     * @param session The database session to insert the question property in
     * @throws Exception if there was a problem inserting
     */
    static void insertQuestionProperty(DbQuestion question, String keyName, DbOptionList propertyOptionList, Session session) throws Exception {
    	
    	//get the property key
    	DbPropertyKey propertyKey = new DbPropertyKey(keyName);
    	propertyKey = dbMgr.selectRowByExample(propertyKey, DbPropertyKey.class, session);
    	
    	//insert the question property value
        DbQuestionPropertyValue questionPropertyValue = new DbQuestionPropertyValue(propertyOptionList);        
        dbMgr.insertRow(questionPropertyValue, session);  
        
        DbQuestionProperty property = new DbQuestionProperty();        
        property.setPropertyKey(propertyKey);        
        property.setPropertyValue(questionPropertyValue);
        
        property.setQuestion(question);
                
        dbMgr.insertRow(property, session);
    }
    
        
    /**
     * Method to insert a question with answers and categories into the database.
     * 
     * WARNING: We currently don't have sufficient support for transactions to implement this method properly. 
     * As it is now, we can have partial success (or partial failure, depending on your point of view).
     * 
     * Order of insertion is as follows:
     * 
     *  optionList
     *      options
     *          question
     *              properties
     *              
     *  As long as we keep succeeding we proceed to the next insertion. If we fail at any step, the subsequent insertions are not performed.
     *  Thus we could wind up with incomplete insertions.             
     * 
     * @param questionBean the QuestionBean containing the question to be inserted.
     * @param answerBeanList the list of AnswerBeans containing the answers to be inserted.
     * @param questionCategories the Set of QuestionCategories to be associated with this question.
     * @return true if the COMPLETE insertion succeeds, otherwise false.
     */    
    static boolean insertQuestion(QuestionBean questionBean,
            ArrayList<AnswerBean> answerBeanList,
            Set<DbCategory> questionCategories) {


    	Session session = dbMgr.getCurrentSession();    	
    	Transaction transaction  = session.beginTransaction();

    	boolean success = true;

    	//insert optionList        
    	DbOptionList dbOptionList = new DbOptionList(importName + " import #" + questionBean.getQuestion_number());
    	try{
    	    dbMgr.insertRow(dbOptionList, session);
    	}catch(Exception e){
    	    
            System.out.println(":\t Failed to insert optionList!");            
            System.out.println(dbOptionList+ "\n");
    	    e.printStackTrace();   
    	    
    	    transaction.rollback();
    	    return false;
    	}

    	//System.out.println(dbOptionList);

        // insert options
        int sortKey = 0;
//      int correctAnswerId = -1;

        for (AnswerBean answer : answerBeanList) {

            DbListOption listOption = new DbListOption();
            listOption.setOptionList(dbOptionList);
            listOption.setText(sanitizeText(answer.getAnswer_text()));
            listOption.setSortKey(sortKey);
            
            try{
                dbMgr.insertRow(listOption, session);
            }catch(Exception e){

                System.out.println(":\t Failed to insert listOption: ");
                System.out.println(listOption);
                System.out.println("ignoring any remaining list options for this question\n");
                e.printStackTrace();
                
                transaction.rollback();
                return false;
            }
            
//          if (answer.isCorrect()) {
//              correctAnswerId = listOption.getListOptionId();
//          }
            
            //System.out.println(listOption);
            
            ++sortKey;              
        } 
        
        if (success) {

            DbQuestionType replyType = new DbQuestionType(QuestionTypeEnum.MULTIPLE_CHOICE.getName(), null);                
            replyType = dbMgr.selectRowByExample(replyType, DbQuestionType.class, session);                        

            success = (replyType != null);

            if(!success) {
                System.out.println(":\tFailed to find replyType of \"" + QuestionTypeEnum.MULTIPLE_CHOICE.getName() + "\" ... aborting \n");
            }
            else {

                //insert question
                DbQuestion question = new DbQuestion();
                
                question.setText(sanitizeText(questionBean.getQuestion_text()));
                question.setCategories(questionCategories);                 

                question.setQuestionType(replyType);
                Set<DbQuestionProperty> questionProperties = new HashSet<>();
                question.setQuestionProperties(questionProperties);
                try{
                    dbMgr.insertRow(question, session);
                }catch(Exception e){
                    System.out.println(":\tFailed to insert question");
                    System.out.println(question + "\n");
                    e.printStackTrace();
                    
                    transaction.rollback();
                    return false;
                }
                
                //TODO: modify to support fill-in-the-blank questions and questions with multiple correct answers
                
                // insert question properties
                try{
                    insertQuestionProperty(question, SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY.getName(), dbOptionList, session);
                }catch(Exception e){
                    System.out.println(":\t failed to insert question property: " + SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY.getName() + "\n");
                    e.printStackTrace();
                    
                    transaction.rollback();
                    return false;
                }

                if(RECORD_CLS_PRIORITY_AS_QUESTION_PROPERTY) {
                    
                    try{
                        insertQuestionProperty(question,
                                SurveyPropertyKeyEnum.CLS_QUESTION_PRIORITY.getName(),
                                questionBean.getPrioritization_CLS(), session);
                    }catch(Exception e){
                        System.out.println(":\t failed to insert question property: " + SurveyPropertyKeyEnum.CLS_QUESTION_PRIORITY.getName()+ "\n");
                        e.printStackTrace();
                        
                        transaction.rollback();
                        return false;
                    }
                }
            }
        }

    	transaction.commit();

    	return true;
    }
    
    /** Method to map the answers by question ID */       
    static void mapAnswersByQuestionId() {
    	
        //Answers could be processed in-line during initial read, but prefer to keep the bean read code generic.
        //Process here as a second pass
        //Iterate over all answers. Answers to the same question go into a dedicated list, in the order they are found.  
        //Put the dedicated list into map using the question number (id) as the key        
        for(AnswerBean answer: answerList) {
            
            int questionId = answer.getQuestion_ID();
            
            ArrayList<AnswerBean> answersToQuestion;
            
            if( !questionIdToAnswerBeans.containsKey(questionId) ) {
            	
                answersToQuestion = new ArrayList<>();
                questionIdToAnswerBeans.put(questionId, answersToQuestion);
            }
            else {
            	
                answersToQuestion = questionIdToAnswerBeans.get(questionId);
            }            
            answersToQuestion.add(answer);
        }        
    }
    
        
    
    /**
     * Finds the unique categories, insert them to the database (or retrieves existing ones that match), and populates a map which maps question ID's to the corresponding sets of categories
     */
    static void insertCategoriesIntoDatabase() {
    	
    	DbCategory commonImportCategory = retrieveOrInsertCategoryByName(importName);
        
        for(QuestionBean question: questionList) {
            
            Set<DbCategory> categorySet = new HashSet<>();
            
            categorySet.add(commonImportCategory);     

            if(USE_CLS_PRIORITIES_AS_CATEGORIES) {

                String priority = sanitizeText(question.getPrioritization_CLS());
                
                if(priority != null ) {

                    DbCategory priorityCategory;

                    if(priorityToCategory.containsKey(priority)) { 

                        priorityCategory = priorityToCategory.get(priority);
                    }
                    else {
                    	
                    	priorityCategory = retrieveOrInsertCategoryByName("CLS Priority " + priority);
                    	
                        //put the category in the map even if its null (i.e. even if the insert failed), so I only report the failure once
                        priorityToCategory.put(priority, priorityCategory);

                        if(priorityCategory == null) {

                            System.out.println("Failed to insert or retrieve category for: " + priority);
                        }
                    }       

                    if( priorityCategory != null ) {

                        categorySet.add(priorityCategory);
                    }
                }

            }
            else if (USE_CLS_TASK_NUMBERS_AS_CATEGORIES) { 

                String taskNumber = sanitizeText(question.getTask_number()); 
                
                if(taskNumber != null ) {

                    DbCategory taskNumberCategory;

                    if(taskNumberToCategory.containsKey(taskNumber)) {

                        taskNumberCategory = taskNumberToCategory.get(taskNumber);
                    }
                    else {
                        
                        taskNumberCategory = retrieveOrInsertCategoryByName("CLS Task " + taskNumber); 
                        
                        //put the category in the map even if its null (i.e. even if the insert failed), so I only report the failure once
                        taskNumberToCategory.put(taskNumber, taskNumberCategory);
                        
                        if(taskNumberCategory == null) {
                            
                            System.out.println("Failed to insert or retrieve category for: " + taskNumberCategory.getName());
                        }
                    }
                    
                    if(taskNumberCategory != null) {
                        
                        categorySet.add(taskNumberCategory);
                    }
                }
            }
            else {

                //use only the "commonImportCategory"
            }

            questionIdToCategorySet.put(question.getQuestion_number(), categorySet);
        }
    }
    
    /**
     * Convenience method that prints the questions (with their corresponding answers) that were rejected by the validation process.
     */
    static void printRejectedQuestions() {
        
        System.out.println("\nThe following " + rejectedQuestions.size() + " questions were not imported because they failed the validation check: ");
        
        for(int i = 0; i < rejectedQuestions.size(); i++) {
            
        	System.out.println();
            printQuestionWithAnswers(rejectedQuestions.get(i), rejectedAnswers.get(i));
        }
    }
    
    
    /**
     * Aggregate method that iterates over the list of QuestionBeans and inserts all the questions into the database.
     * 
     * @return boolean If the insertion of questions was successful
     */
    static boolean insertQuestionsIntoDatabase() {
        
    	System.out.println("\nProcessing questions...\n");
    	
        int processedQuestionCount = 0;
        
        final int totalInsertCount = Math.min(MAX_NUMBER_OF_QUESTIONS_TO_PROCESS, questionList.size());
           
        if ( questionList.size() > MAX_NUMBER_OF_QUESTIONS_TO_PROCESS ) {
        	
        	System.out.println("WARNING: number of questions imported limited by variable: MAX_NUMBER_OF_QUESTIONS_TO_PROCESS");
        }
        
        //Iterate over each question. For each question get the answer set and process: either print or do database operation
        for(QuestionBean question: questionList) {
            
            ++processedQuestionCount;
            
            final int questionNumber = question.getQuestion_number();
            
            ArrayList<AnswerBean> answersToQuestion = questionIdToAnswerBeans.get(questionNumber);
            
            System.out.print("Processing question " + processedQuestionCount + " of " + totalInsertCount);

            if( isValidQuestion(question, answersToQuestion) ) {     

            	boolean success = true;

            	if(!DEBUGGING) {
            		
            		success = insertQuestion(question, answersToQuestion, questionIdToCategorySet.get(question.getQuestion_number()));
            	}

            	if(success) {

            		System.out.println(":\tSuccess");

            		if(VERBOSE) {
            			printQuestionWithAnswers(question, answersToQuestion);                    
            			System.out.println();
            		}
            	}
            	else {
            		
            		//TODO: accumulate and print failed questions the same way rejected questions are handled?
            		//call to insert question already printed the error!
            	}
            }
            else {

            	System.out.println(":\tFailed validation");                    
            	rejectedQuestions.add(question);
            	rejectedAnswers.add(answersToQuestion);
            }

            //limit insertions for testing
            if(processedQuestionCount >= MAX_NUMBER_OF_QUESTIONS_TO_PROCESS) {

            	break;
            }
        }
        
        return true; // needs work -- handle case if fails
    }
    
    /**
     * The main method to run this tool.
     * 
     * @param args not used.
     * @throws Exception if a CSV file failed to parse
     */
    public static void main(String[] args) throws Exception {
        
        parseCsvFile(questionsCsvFilename, QuestionBean.cellProcessors, QuestionBean.class, questionList );
        parseCsvFile(answersCsvFilename,   AnswerBean.cellProcessors,     AnswerBean.class,   answerList );
        
        mapAnswersByQuestionId();

        insertCategoriesIntoDatabase();
 
        insertQuestionsIntoDatabase();
        
        //TODO: validate questions as a separate pass prior to insertion
        //      give user the opportunity to skip rejected questions, truncate long strings, or bail out completely.
        if( !rejectedQuestions.isEmpty() ) {
        	
        	if(VERBOSE) {        		
        		printRejectedQuestions();
        	}
        }
    }
}
