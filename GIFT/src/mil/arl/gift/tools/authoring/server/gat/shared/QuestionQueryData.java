/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.enums.QuestionTypeEnum;

/**
 * The parameters of the query for questions
 *
 * @author jleonard
 */
public class QuestionQueryData implements IsSerializable {

    /** The question type to search for questions in */
    private List<QuestionTypeEnum> questionTypeQuery = new ArrayList<QuestionTypeEnum>();

    /** The categories to search for questions in */
    private List<String> categoriesQuery = new ArrayList<String>();
    
    /** The set of text to search for questions containing it*/
    private List<String> searchTextQuery = new ArrayList<String>();
    
    /** The user name */
    private String userName;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public QuestionQueryData() {
    }

    /**
     * Constructor
     * 
     * @param questionTypeQuery The question type to search for questions in     
     * @param queryCategories The categories to search for questions in
     * @param searchTextQuery The set of text to search for questions containing it
     * @param userName The user name
     */
    public QuestionQueryData(List<QuestionTypeEnum> questionTypeQuery, List<String> queryCategories, List<String> searchTextQuery, String userName) {

        if(questionTypeQuery != null) {
            
            this.questionTypeQuery.addAll(questionTypeQuery);
        }
        
        if(queryCategories != null) {
            
            this.categoriesQuery.addAll(queryCategories);
        }
        
        if(searchTextQuery != null) {
        	this.searchTextQuery = searchTextQuery;
        }
        
        this.userName = userName;
    }

    /**
     * Gets the list of question types to search for questions in
     * 
     * @return List<QuestionTypeEnum> list of question types to search for questions in
     */
    public List<QuestionTypeEnum> getQueryQuestionTypes() {

        return questionTypeQuery;
    }

    /**
     * Gets the list of categories to search for questions in
     *
     * @return List<String> The list of categories to search for questions in
     */
    public List<String> getQueryCategories() {

        return this.categoriesQuery;
    }
    
    /**
     * Gets the set of text to search for questions containing it
     * 
     * @return List<String> The set of text to search for questions containing it
     */
    public List<String> getQuerySearchText(){
    	
    	return this.searchTextQuery;
    }

    /**
     * Gets the user name
     * 
     * @return String The user name.
     */
	public String getUserName() {
		return userName;
	}
}
