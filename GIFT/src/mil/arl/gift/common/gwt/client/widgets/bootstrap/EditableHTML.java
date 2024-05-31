/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteChangeEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteChangeHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.util.StringUtils;

/**
 * A widget used to display HTML that can be clicked on to open a WYSIWYG editor (in this case, Summernote) 
 * that can be used to modify the HTML.
 * <br/><br/>
 * This widget behaves sort of a as a complex hybrid of {@link com.google.gwt.user.client.ui.TextArea TextArea} and 
 * {@link com.google.gwt.cell.client.EditTextCell EditTextCell} that 
 * uses the Summernote editor to modify its values rather than an HTML &lt;textarea&gt; element.
 * Like TextArea, this widget implements {@link com.google.gwt.user.client.ui.HasValue HasValue}&lt;String&gt;, allowing other 
 * widgets to assign values to it and listen for changes made to it by the user. Like EditTextCell, this widget looks like
 * ordinary HTML until the user interacts with it, allowing users to see what their text will actually look like outside the 
 * editor and using less space when the editor isn't open.
 * 
 * @author nroberts
 */
public class EditableHTML extends FocusPanel implements HasValue<String>{
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, EditableHTML> {
	}
	
	@UiField
	protected Container summerNoteContainer;
	
	@UiField
	protected HTML html;
	
	@UiField
	protected Collapse editorCollapse;
	
	@UiField
	protected Collapse textCollapse;
	
	protected Summernote htmlEditor = new Summernote();
	
	@UiField
	protected Tooltip tooltip;
	
	/** The text to use as a placeholder when the user hasn't entered any text yet*/
	private String placeholder = null;
	
	/** Whether or not the mouse is currently over this widget*/
	private boolean isMouseOver = false;
	
	/** Controls whether the widget is editable. */
	private boolean isEditable = true;
	
	/** A panel containing a loading indicator for when Summernote is being initialized. */
	private FlowPanel loadingIndicatorPanel = new FlowPanel();
	
	/** Default toolbar for the summernote editor.  This can be overridden by calling the setToolbar method. */
	private Toolbar defaultToolbar = new Toolbar()
	       .addGroup(ToolbarButton.STYLE)
	       .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC)
	       .addGroup(ToolbarButton.FONT_NAME)
	       .addGroup(ToolbarButton.FONT_SIZE, ToolbarButton.COLOR)
	       .addGroup(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
	       .addGroup(ToolbarButton.TABLE)
	       .addGroup(ToolbarButton.UNDO, ToolbarButton.REDO)
	       .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);
	
	/** the default font color of the placeholder text */
	private static final String DEFAULT_PLACEHOLDER_FONT_COLOR = "gray";
	
	/** the current color of the placeholder text, useful for refresh */
	private String currentColor = DEFAULT_PLACEHOLDER_FONT_COLOR;

	/**
	 * Creates a new editable HTML element
	 */
	public EditableHTML() {
	    
		setWidget(uiBinder.createAndBindUi(this));

		//make the Summernote toolbar visible
		htmlEditor.setShowToolbar(true);
		htmlEditor.setDialogsInBody(true);
		htmlEditor.setToolbar(defaultToolbar);
		
		//make the display text show a "text" cursor to help let the user know it is editable
		html.getElement().getStyle().setCursor(Cursor.TEXT);
		
		setTabIndex(-1); //allow this widget to gain focus
		
		loadingIndicatorPanel.getElement().setAttribute("style", 
				"background-color: rgb(200,200,200); "
				+ "position: absolute; "
				+ "padding: 5px; "
				+ "top: 0px; "
				+ "width: 100%; "
				+ "border-radius: 5px; "
				+ "height:100%; "
				+ "text-align: center;"
		);
		
		loadingIndicatorPanel.add(new InlineLabel("Loading..."));
		
		addMouseOverHandler(new MouseOverHandler() {
			
			@Override
			public void onMouseOver(MouseOverEvent event) {
				
				if(!isMouseOver){
					isMouseOver = true;
				}
			}
		});
		
		addMouseOutHandler(new MouseOutHandler() {
			
			@Override
			public void onMouseOut(MouseOutEvent event) {
				
				if(isMouseOver){
					isMouseOver = false;
				}
			}
		});
	    
		//hide the HTML editor when the user clicks outside this widget
		htmlEditor.addSummernoteBlurHandler(new SummernoteBlurHandler() {
			
			@Override
			public void onSummernoteBlur(SummernoteBlurEvent event) {
				
				if(isMouseOver){
					
					//The Summernote editor will lose focus when its buttons are pressed, so we need to detect when
					//that happens and reset the focus back to the editor when it does.
					focusSummernote(htmlEditor.getElement());
					
				} else {
				
					//if the user clicks outside this widget, we need to hide the editor
					if(editorCollapse.isVisible()){			
						
						editorCollapse.setVisible(false);
						
						html.getElement().getStyle().setVisibility(Visibility.VISIBLE);
						textCollapse.setVisible(true);

                        String value = containsText() ? htmlEditor.getCode() : "";
                        ValueChangeEvent.fire(EditableHTML.this, value);
					}
				}
			}
			
	    });
	    
		//update the display text when the HTML editor has fired a change
	    htmlEditor.addSummernoteChangeHandler(new SummernoteChangeHandler() {
			
			@Override
			public void onSummernoteChange(SummernoteChangeEvent event) {
			    refreshWidget();
			}
		});
	    
	    //show the HTML editor when the display text is clicked on
		addDomHandler(new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
						
				if(textCollapse.isVisible() && isEditable){
					
					event.preventDefault();
					
					startEditing();
				}
			}
		
	    }, MouseDownEvent.getType());
	}

	/**
	 * Refreshes the display of the widget to show the placeholder text, or the actual
	 * html text (if the htmleditor is not empty).
	 */
	public void refreshWidget() {
	    if (containsText()) {
	        html.setHTML(htmlEditor.getCode());
	    } else {
	        showPlaceholderText(currentColor);
	    }
    }

    /**
     * Checks if the editor contains text.
     * 
     * @return true if the editor contains text; false if it is empty or only
     *         contains formatting.
     */
    public boolean containsText() {
        /* Remove the paragraph and break tags */
        String editorCode = htmlEditor.getCode().replaceAll("<p>", "").replaceAll("<br>", "").replaceAll("</p>", "");

        /* Check for formatted paragraphs and remove them */
        while (true) {
            final int startIndex = editorCode.indexOf("<p ");
            if (startIndex == -1) {
                break;
            }

            final int endIndex = editorCode.indexOf(">", startIndex);

            if (endIndex == editorCode.length() - 1) {
                /* The whole code is the formatted paragraph */
                editorCode = "";
            } else if (startIndex == 0) {
                /* Remove the paragraph tag from the beginning */
                editorCode = editorCode.substring(endIndex + 1);
            } else {
                /* Remove the paragraph tag from the middle */
                editorCode = editorCode.substring(0, startIndex - 1) + editorCode.substring(endIndex + 1);
            }
        }

        return StringUtils.isNotBlank(editorCode);
    }

    @Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {

		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {	
		return containsText() ? htmlEditor.getCode() : "";
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		
		String oldValue = htmlEditor.getCode();
		
		htmlEditor.setCode(value);		

		if(fireEvents){
			ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
		}
		
		refreshWidget();
	}
	
	/**
     * Sets the placeholder text to use when no text has been entered.
     * Uses the default color.
     * 
     * @param placeholder the placeholder text
     */
    public void setPlaceholder(String placeholder){
        setPlaceholder(placeholder, currentColor);
    }

	/**
	 * Sets the placeholder text to use when no text has been entered
	 * 
	 * @param placeholder the placeholder text
	 * @param color the font color the placeholder text should be.  If null  or empty the default
	 * color is used.
	 */
	public void setPlaceholder(String placeholder, String color){
	    
	    if(StringUtils.isBlank(color)){
	        color = DEFAULT_PLACEHOLDER_FONT_COLOR;
	    }
		
		this.placeholder = placeholder;
		this.currentColor = color;
		
		htmlEditor.setPlaceholder(placeholder);
		
		if(htmlEditor.isEmpty()){			
			showPlaceholderText(color);
		}
	}
	
	/**
	 * Updates the display text to show the placeholder text
	 * 
	 * @param color the font color the placeholder text should be
	 */
	private void showPlaceholderText(String color){

	    SafeHtmlBuilder sb = new SafeHtmlBuilder();
		sb.appendHtmlConstant("<span style='color:").appendEscaped(color).appendHtmlConstant(";'>")
			.appendEscaped(placeholder)
			.appendHtmlConstant("</span>");
		
		html.setHTML(sb.toSafeHtml());			
	}
	
	
	/**
	 * Sets the editable state of the widget.
	 * 
	 * @param editable - True to allow editing in the widget, false to disallow editing.
	 */
	public void setEditable(boolean editable) {
	    isEditable = editable;
	    tooltip.setTitle(editable ? "Click to edit" : "Cannot currently edit." );

        /* Make the display text show a "text" or "default" cursor to help let
         * the user know it is editable or readonly, respectively */
        html.getElement().getStyle().setCursor(editable ? Cursor.TEXT : Cursor.DEFAULT);
	}
	
	/**
	 * Gets the editor used to modify this widget's HTML
	 * 
	 * @return the editor
	 */
	public Summernote getHtmlEditor(){
		return htmlEditor;
	}
	
	/**
	 * Sets the toolbar buttons for the Summernote HTML editor.  This should be called right after
	 * creation of the editor.
	 * @param toolbar the new toolbar value for the HTML editor
	 */
	public void setToolbar(Toolbar toolbar) {
	    htmlEditor.setToolbar(toolbar);
	}
	
	/*
	 * Sets the text to be displayed within the tool tip
	 */
	public void setTooltip(String tooltip) {
	    this.tooltip.setTitle(tooltip);
	}
	
	/**
	 * Sets the focus on this widget and begins editing it's contents
	 */
	public void startEditing(){
		
		if(!htmlEditor.isAttached()){
			
			//if the Summernote editor has not been loaded, we need to load it
			
			html.getElement().getStyle().setVisibility(Visibility.HIDDEN);	
			
			textCollapse.add(loadingIndicatorPanel);	
			
			// Nick: Need to wait until the browser event loop returns control here, otherwise IE won't always be able to show
			// the loading indicator before we start loading Summernote.
			Scheduler.get().scheduleEntry(new ScheduledCommand() {
				
				@Override
				public void execute() {
						
					// Nick: To help with performance, we're deferring the process of loading the Summernote editor so that
					// it is only loaded once the user clicks on an instance of this widget to start editing it. This creates
					// a slight delay during the initial click, but it also prevents the browser from getting bogged down when
					// many instances of this widget are being added at the same time, such as when a survey is being loaded.
					editorCollapse.add(htmlEditor);
					
					textCollapse.setVisible(false);
					editorCollapse.setVisible(true);
					
					htmlEditor.setHasFocus(true);
					focusSummernote(htmlEditor.getElement());
					
					textCollapse.remove(loadingIndicatorPanel);
				}
			});
			
		} else {
			
			//otherwise, we can just switch to the editor immediately
			
			html.getElement().getStyle().setVisibility(Visibility.HIDDEN);	
			
			textCollapse.setVisible(false);
			editorCollapse.setVisible(true);
			
			htmlEditor.setHasFocus(true);
			focusSummernote(htmlEditor.getElement());
				
		}
		
		
	}
	
	/**
	 * Focuses on the Summernote editor with the given element.
	 * <br/><br/>
	 * Note: For whatever reason, {@link org.gwtbootstrap3.extras.summernote.client.ui.base.SummernoteBase#setHasFocus(boolean) 
	 * Summernote's setHasFocus(boolean) method} doesn't seem to work properly, so this method exists as a workaround.
	 * 
	 * @param e the element for a Summernote editor
	 */
	/*
	 * Nick - Specifically, I think SummernoteBase.setHasFocus(boolean) is setting the focus on the wrong element. Going into it's
	 * Java code reveals that it is using it's getElement() method to get the element it needs to focus on. For most widgets, 
	 * this would be correct, but for the Summernote widget, getElement() actually returns the topmost div for the Summernote editor, 
	 * NOT the actual editable area. In order to set focus on the editable area, we need to use the native Summernote JavaScript API
	 * in order to invoke the 'focus' method on its editor module.
	 */
	private native void focusSummernote(Element e)/*-{
	    $wnd.jQuery(e).summernote('focus');
	}-*/;
}
