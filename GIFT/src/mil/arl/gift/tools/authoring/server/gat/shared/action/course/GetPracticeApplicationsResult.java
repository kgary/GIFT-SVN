/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.model.course.PracticeApplicationObject;

/**
 * The result of a get practice application client request.  Contains the practice applications
 * for the required course concepts as well as the practice applications for the other course concepts.
 * 
 * @author mhoffman
 *
 */
public class GetPracticeApplicationsResult extends GatServiceResult{

    /**
     * the practice application objects for the required course concepts
     */
    private List<PracticeApplicationObject> practiceApplications;

    public List<PracticeApplicationObject> getPracticeApplications() {
        return practiceApplications;
    }

    public void setPracticeApplications(List<PracticeApplicationObject> practiceApplications) {
        this.practiceApplications = practiceApplications;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[GetPracticeApplicationsResult: ");
        sb.append(" applications = {\n");
        if(practiceApplications != null){
            for(PracticeApplicationObject app : practiceApplications){
                sb.append(app).append(",\n");
            }
        }
        sb.append("}");
        sb.append("]");
        return sb.toString();
    }

}
