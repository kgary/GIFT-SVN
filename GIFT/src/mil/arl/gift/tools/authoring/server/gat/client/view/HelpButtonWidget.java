/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

public class HelpButtonWidget extends Composite implements HasText {

	/**
	 * The Interface HelpButtonWidgetUiBinder.
	 */
	interface HelpButtonWidgetUiBinder extends UiBinder<Widget, HelpButtonWidget> {
	}
	
	private static HelpButtonWidgetUiBinder uiBinder = GWT.create(HelpButtonWidgetUiBinder.class);
	
	private final static String HELP_PAGE = "/gat/docs/index.htm";
	
	protected @UiField (provided = true)
	MenuItem helpItem;
	
	protected @UiField (provided = true)
	MenuItem fileNameMenuItem;
	
	public HelpButtonWidget() {
		
		helpItem = new MenuItem("<img src='images/help.png'>", true, 
			new Command() {
				@Override
				public void execute() {
					Window.open(HELP_PAGE, "_blank", "");
				}
			}
		);
		
		SafeHtmlBuilder sb = new SafeHtmlBuilder();
		sb.appendHtmlConstant("");
		
		fileNameMenuItem = new MenuItem(sb.toSafeHtml());
		
		initWidget(uiBinder.createAndBindUi(this));	
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasText#getText()
	 */
	@Override
	public String getText() {
		return fileNameMenuItem.getText();
	}

	/* (non-Javadoc)
	 * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		fileNameMenuItem.setText(text);
	}
}
