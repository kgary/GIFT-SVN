/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.header;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.CourseElementUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.TeamReference;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * A widget used to display a list of course objects referencing a concept
 * 
 * @author nroberts
 */
public class ConceptReferenceList extends Composite{
    
    /** The list of course object references*/
    private GenericListEditor<TeamReference> referenceList;
    
    /** A command to execute whenever the author decides to jump to one of the course object references*/
    private Command onJumpCommand;
    
    /**
     * Creates a new widget that displays a list of course objects referencing a concept
     */
    public ConceptReferenceList() {
        
        referenceList = new GenericListEditor<>(new Stringifier<TeamReference>() {
            @Override
            public String stringify(final TeamReference ref) {
                
                final SafeHtmlBuilder displayString = new SafeHtmlBuilder();
                
                displayString.appendHtmlConstant("<div style='margin-right: 10px; display: inline-block;'>");
                
                displayString.appendEscaped(CourseElementUtil.getTransitionName(ref.getParent()));
                
                displayString.appendHtmlConstant("</div>");
                
                displayString.appendHtmlConstant("<div style='display: inline-block; float: right;'>");
                displayString.appendEscaped(Integer.toString(ref.getReferenceCount()));
                displayString.appendHtmlConstant("</div>");
                
                return displayString.toSafeHtml().asString();
            }
        });
        
        ItemAction<TeamReference> jumpToAction = new ItemAction<TeamReference>() {
            
            @Override
            public boolean isEnabled(TeamReference item) {
                return true;
            }
            
            @Override
            public String getTooltip(TeamReference item) {
                return "Click to navigate to this reference";
            }
            
            @Override
            public IconType getIconType(TeamReference item) {
                return IconType.EXTERNAL_LINK;
            }
            
            @Override
            public void execute(TeamReference item) {
                
                ScenarioEventUtility.fireJumpToEvent(item.getParent());
                
                onJump();
            }
        };
        
        referenceList.setRowAction(jumpToAction);
        referenceList.setPlaceholder("No course objects are referencing this concept");
        referenceList.getElement().getStyle().setProperty("max-height", "200px");
        referenceList.getElement().getStyle().setProperty("border", "1px solid rgb(200,200,200)");
        referenceList.getElement().getStyle().setProperty("border-radius", "3px");
        referenceList.getElement().getStyle().setOverflow(Overflow.AUTO);
        
        initWidget(referenceList);
    }
    
    /**
     * Gets the list used to display the course objects referencing a concept
     * 
     * @return the activity reference list
     */
    public GenericListEditor<TeamReference> getListEditor(){
        return referenceList;
    }
    
    /**
     * Sets a command to be invoked when the author decides to jump to a reference
     * 
     * @param command the command to invoke
     */
    public void setOnJumpCommand(Command command) {
        this.onJumpCommand = command;
    }
    
    /**
     * Notifies listeners that the author has decided to jump to a reference
     */
    private void onJump() {
        
        if(onJumpCommand != null) {
            onJumpCommand.execute();
        }
    }
}