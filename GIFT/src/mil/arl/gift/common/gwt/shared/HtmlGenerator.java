/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;


import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;


/**
 * Generates HTML representations of GIFT data elements
 *
 * @author jleonard
 */
public class HtmlGenerator {
    
    public final static String PASS_DARK_GREEN = "#009900";
    public final static String PASS_GREEN = "#00CC00";
    public final static String FAIL_RED = "#FF0000";
    public final static String INCOMPLETE_YELLOW = "#FFFF00";
    public final static String INCOMPLETE_ORANGE = "#FF8C00";
    
    public final static String SPAN_DARK_GREEN = "<span style=\"color:"+PASS_DARK_GREEN+";\">";
    public final static String SPAN_GREEN = "<span style=\"color:"+PASS_GREEN+";\">";
    public final static String SPAN_RED = "<span style=\"color:"+FAIL_RED+";\">";
    public final static String SPAN_ORANGE = "<span style=\"color:"+INCOMPLETE_ORANGE+";\">";
    public final static String SPAN_END = "</span>";
    
    final static String DIV_OPEN = "<div>";
    final static String DIV_CLOSE = "</div>";
    final static String BR = "<br/>";

    /**
     * Default Constructor
     *
     * Private because this is a helper class with static methods
     */
    private HtmlGenerator() {
    }

    

    /**
     * Generates a HTML formatted string of the after action review details
     *
     * @param buffer The string buffer to append to
     * @param node A node of the AAR data
     */
    private static void generateScoreNodeHtml(StringBuilder buffer, AbstractScoreNode node) {

        buffer.append("<div align=\"left\"; style=\"margin-left:10px;\">");
        buffer.append(node.getName());
        if (node instanceof GradedScoreNode) {
            GradedScoreNode gNode = (GradedScoreNode) node;
            buffer.append(": ");
            boolean colored;
            if (gNode.getAssessment().hasReachedStandards()) {
                buffer.append(SPAN_GREEN);
                colored = true;
            } else {
                buffer.append(SPAN_RED);
                colored = true;
            }
            buffer.append(gNode.getAssessment().hasReachedStandards());
            if (colored) {
                buffer.append("</span>");
            }

            for (AbstractScoreNode i : gNode.getChildren()) {
                generateScoreNodeHtml(buffer, i);
            }
        } else if (node instanceof RawScoreNode) {
            RawScoreNode rNode = (RawScoreNode) node;
            buffer.append(": ");
            buffer.append(rNode.getRawScore().toDisplayString());
            buffer.append(" - ");
            boolean colored = false;
            if (rNode.getAssessment().compareTo(AssessmentLevelEnum.ABOVE_EXPECTATION) == 0) {
                buffer.append(SPAN_GREEN);
                colored = true;
            } else if (rNode.getAssessment().compareTo(AssessmentLevelEnum.BELOW_EXPECTATION) == 0) {
                buffer.append(SPAN_RED);
                colored = true;
            }
            buffer.append(rNode.getAssessment().getDisplayName());
            if (colored) {
                buffer.append("</span>");
            }
        }
        buffer.append(DIV_CLOSE);
    }

    /**
     * Generates a HTML formatted string of the after action review details
     *
     * @param node A node of the AAR data
     * @return String The HTML of the graded score node
     */
    public static String generateScoreNodeHtml(AbstractScoreNode node) {
        StringBuilder htmlBuilder = new StringBuilder();
        generateScoreNodeHtml(htmlBuilder, node);
        return htmlBuilder.toString();
    }
}
