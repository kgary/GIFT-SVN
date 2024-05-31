/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import java.util.List;

import mil.arl.gift.common.DomainOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget used to show the user selected course that will be deleted.
 * 
 * @author nroberts
 *
 */
public class BsDeleteSummaryWidget extends Composite {

	private static BsDeleteSummaryWidgetUiBinder uiBinder = GWT
			.create(BsDeleteSummaryWidgetUiBinder.class);

	interface BsDeleteSummaryWidgetUiBinder extends
			UiBinder<Widget, BsDeleteSummaryWidget> {
	}
	
	@UiField
	protected HTML courseNamesList;

	public BsDeleteSummaryWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	public void buildCoursesToDeleteSummary(List<DomainOption> courses){
        
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<ul>");
        
        for(DomainOption course : courses){
            
            if(course.getDomainName() != null){
                sb.appendHtmlConstant("<li>");
                sb.appendEscaped(course.getDomainName());
                sb.appendHtmlConstant("</li>");
            }
        }
        
        sb.appendHtmlConstant("</ul>");
        
        courseNamesList.setHTML(sb.toSafeHtml());
	}
}
