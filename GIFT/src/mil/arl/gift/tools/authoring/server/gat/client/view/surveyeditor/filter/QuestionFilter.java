/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.HTML;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.util.StringUtils;

/**
 * The data model for the survey filter containing the required concepts to filter questions by.
 * 
 * @author tflowers
 *
 */
public class QuestionFilter {
    
    private static Logger logger = Logger.getLogger(QuestionFilter.class.getName());
    
    /** The list of concepts to filter questions by */
    private Collection<String> conceptsToMatch = new HashSet<String>();
    
    /** The search term expression to filter questions by */
    private String searchTermsToMatch = null;
    
	 /** A regular expression used to locate words and phrases in a search text expression */
	private static final String wordExpression = 
		"-?\"[^\"]*\"" +	//double quotes around phrases(s)
        "|-?[A-Za-z0-9']+"  //single word
	;
	
	/** A regular expression used to locate binary operators in a search text expression */
	private static final String binaryOperatorExpression = "(" + wordExpression + ")(\\s+(AND|OR)\\s+(" + wordExpression + "))+";

    /**
     * Determines if an element matches the criteria defined by this filter
     * @param e The survey element to test
     * @return True if the element fits the criteria, false otherwise
     */
    public boolean matches(AbstractSurveyElement e) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("matches(")
                    .append(e)
                    .append(")")
                    .toString());
        }
        
        if(e instanceof AbstractSurveyQuestion) {
            
            AbstractSurveyQuestion<?> surveyQuestion = (AbstractSurveyQuestion<?>) e;
            
            //Tests which parameters of the filter that the survey question matches
            boolean matchesConcept = matchesConcepts(surveyQuestion);
            boolean matchesSearchTerms = matchesTerms(searchTermsToMatch, surveyQuestion);
            
            return matchesConcept && matchesSearchTerms;
        }
        
        return false;
    }
    
    /**
     * Updates the concepts that the filter should consider
     * when filtering questions
     * @param concepts the list of concepts to consider when filtering questions, 
     * null is treated as an empty list
     */
    public void setConcepts(List<String> concepts) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("setConcepts(")
                    .append(concepts)
                    .append(")")
                    .toString());
        }
        
        conceptsToMatch.clear();
        
        if(concepts == null) {
            return;
        }
        
        conceptsToMatch.addAll(concepts);
    }
    
    /**
     * Updates the search terms that the filter should consider
     * when filtering questions
     * @param terms a string of the search term to consider when filtering questions, can be null or empty
     */
    public void setSearchTerms(String terms) {
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("setSearchTerms(")
                    .append(terms)
                    .append(")")
                    .toString());
        }
        
        searchTermsToMatch = terms;
        
    }
    
    /**
     * Determines whether or not the survey question matches the current filter
     * with respect to the supplied concepts.
     * @param surveyQuestion the question to test against the filter
     * @return true if the question applies to any of the concepts to the
     * filter or if no concepts were supplied to the filter, otherwise false or false if any component is null
     */
    private boolean matchesConcepts(AbstractSurveyQuestion<?> surveyQuestion) {
        
        if (surveyQuestion == null) {
            return false;
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("matchesConcepts(")
                    .append(surveyQuestion)
                    .append(")")
                    .toString());
        }
        
        //If no concepts are supplied to the filter, return true
        if(conceptsToMatch == null || conceptsToMatch.isEmpty()) {
            return true;
        }
        
        AbstractQuestion question = surveyQuestion.getQuestion();
        if (question == null || question.getProperties() == null) {
            return false;
        }

        //Gets the concepts that apply to this question
        List<String> questionConcepts = question.getProperties().getStringListPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS);
        
        if (questionConcepts != null) {
            //If the question applies to any of the supplied concepts, return true
            for(String qConcept : questionConcepts) {
                for(String concept : conceptsToMatch) {
                    if(qConcept.equalsIgnoreCase(concept)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Determines whether or not the survey question matches the current filter 
     * with respect to the supplied search terms
     * 
     * @param surveyQuestion the question to test against the filter
     * @return true if the question's text contains any of the search terms or if no 
     * search terms are provided, otherwise false or false if any component is null.
     */
    private boolean matchesTerms(String filterExpression, AbstractSurveyQuestion<?> question) {
        
        // Return false if the question is null
        if (question == null) {
            return false;
        }
        
        if(logger.isLoggable(Level.INFO)) {
            logger.info(new StringBuilder()
                    .append("matchesTerms(")
                    .append(question)
                    .append(")")
                    .toString());
        }
        
        // If no search terms are provided, return true
        if(filterExpression == null || filterExpression.isEmpty()) {
            return true;
        }
        
        // Add the question to a list to filter by
        List<AbstractSurveyQuestion<?>> toFilter = new ArrayList<AbstractSurveyQuestion<?>>();
        toFilter.add(question);
        
        // filter the question list
        List<AbstractSurveyQuestion<?>> result = filterQuestionsByText(filterExpression, toFilter);
        
        // the question matches if it is contained in the filtered list
        return !result.isEmpty();
    }
    
    /**
     * Filters the given list of questions using the given filter expression.
     * 
     * @param filterExpression the expression used to filter. If null or empty
     *        the list provided is not reduced in size.
     * @param toFilter the list of questions to filter. If null or empty, an empty list will
     *        be returned.
     * @return the filtered list. Can't be null but can be empty.
     */
    public static List<AbstractSurveyQuestion<?>> filterQuestionsByText(final String filterExpression, List<AbstractSurveyQuestion<?>> toFilter) {
        List<AbstractSurveyQuestion<?>> result = new ArrayList<>();
        if (toFilter == null || toFilter.isEmpty()) {
            return result;
        }
        if (StringUtils.isBlank(filterExpression)) {
            result.addAll(toFilter);
            return result;
        }
        final List<AbstractSurveyQuestion<?>> toFilterCopy = new ArrayList<>(toFilter);
        final RegExp searchTermExp = RegExp.compile(binaryOperatorExpression + "|" + wordExpression, "gm");
        /* Parse the filter expression to get the list of search terms */
        final List<String> searchTerms = new ArrayList<String>();
        for (MatchResult matcher = searchTermExp.exec(filterExpression); matcher != null; matcher = searchTermExp
                .exec(filterExpression)) {
            searchTerms.add(matcher.getGroup(0));
        }
        for (String currentTerm : searchTerms) {
            if (StringUtils.isBlank(currentTerm) || (currentTerm.equals("AND") && searchTerms.size() == 1) || (currentTerm.equals("OR") && searchTerms.size() == 1)) {
                continue;
            }
            currentTerm = currentTerm.trim();
            /* If a term matches the regular expression for a binary operator
             * chain, perform the binary operations specified and add the
             * resulting rows to the result */
            if (currentTerm.matches(binaryOperatorExpression)) {
                /* Parse the binary operator chain for its operands */
                final List<String> operands = Arrays.asList(currentTerm.split("\\s+AND\\s+|\\s+OR\\s+"));
                /* Parse the binary operator chain for its operators */
                for (String operand : operands) {
                    currentTerm = currentTerm.replaceAll(operand, "");
                }
                currentTerm = currentTerm.trim();
                final List<String> operators = Arrays.asList(currentTerm.split("\\s+"));
                /* For each operand, perform the next binary operation specified
                 * using result of the previous binary operation and the operand
                 * itself */
                final List<AbstractSurveyQuestion<?>> binaryOpResult = new ArrayList<>();
                for (String operand : operands) {
                    final int j = operands.indexOf(operand);
                    if (operands.indexOf(operand) == 0) {
                        binaryOpResult.addAll(filterQuestionsByText(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("AND")) {
                        binaryOpResult.retainAll(filterQuestionsByText(operand, toFilterCopy));
                    } else if (operators.get(j - 1).matches("OR")) {
                        binaryOpResult.addAll(filterQuestionsByText(operand, toFilterCopy));
                    }
                }
                /* Add what items remain to the result */
                result.addAll(binaryOpResult);
            } else if (currentTerm.startsWith("-")) {
                /* If a term starts with a '-', then all rows captured by
                 * searching for the remainder of the term will be removed from
                 * the result */
                /* If there are already items in the result, search for the
                 * remainder of the search term and remove all items found in
                 * the search */
                if (!result.isEmpty()) {
                    result.removeAll(filterQuestionsByText(currentTerm.substring(1), toFilterCopy));
                    /* Otherwise, add all the items in the table to the result,
                     * search for the remainder of the search term, and remove
                     * all items found in the search */
                } else {
                    result.addAll(toFilterCopy);
                    result.removeAll(filterQuestionsByText(currentTerm.substring(1), toFilterCopy));
                }
            } else {
                /* It is still possible for an AND/OR keyword to make it here
                 * if it is the final term of a search query. If it does make 
                 * it to this point, skip over the currentTerm.
                 */
                if(currentTerm.equals("AND") || currentTerm.equals("OR")) {
                    continue;
                }
                /* Otherwise, treat the term as a single phrase */
                /* If the term begins and ends with quotes, remove the quotes
                 * before evaluating the term */
                boolean exactMatch = false;
                if (currentTerm.startsWith("\"") && currentTerm.endsWith("\"")) {
                    exactMatch = true;
                    currentTerm = currentTerm.substring(1, currentTerm.length() - 1);
                }
                /* Find all the published items containing the course's text
                 * (ignoring case) #5015 - ignore case so user doesn't have to worry about matching*/
                for (AbstractSurveyQuestion<?> q : toFilterCopy) {
                    
                    if (q == null || q.getQuestion() == null || q.getQuestion().getText() == null) {
                          continue;
                      }
                    
                    if (q.getQuestion().getText() != null && 
                        ((exactMatch && new HTML(q.getQuestion().getText()).getText().contains(currentTerm))
                        || (!exactMatch && new HTML(q.getQuestion().getText().toLowerCase()).getText().contains(currentTerm.toLowerCase())))){
                        result.add(q);
                    }
                }
            }
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionFilter: ");
        sb.append("conceptsToMatch = ").append(conceptsToMatch);
        sb.append(", searchTermsToMatch = ").append(searchTermsToMatch);
        sb.append("]");
        return sb.toString();
    }
}