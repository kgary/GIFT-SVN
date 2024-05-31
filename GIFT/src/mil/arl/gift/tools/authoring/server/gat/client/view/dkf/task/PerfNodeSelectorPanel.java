/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Task;
import generated.dkf.Tasks;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The panel that acts as the dropdown for {@link PerfNodeSelectorImpl performance node pickers} and provides the list of performance nodes
 * (i.e. tasks or concepts) that the author can select from.
 * <br/><br/>
 * This panel is absolutely positioned relative to its associated performance node picker so that it moves relative to the picker's
 * rendered position, even when the author scrolls the page. At a basic level, the behavior of this panel mimics that of
 * GWT's PopupPanel, only in this case, the panel is attached to the picker itself rather than to the RootPanel.
 * 
 * @author nroberts
 *
 */
public class PerfNodeSelectorPanel extends SimplePanel {
    
    /** The singleton instance of this class */
    private static PerfNodeSelectorPanel instance;
    
    /** The waypoint picker that this panel is currently being shown for */
    private static PerfNodeSelectorImpl currentPicker;
    
    /** A data provider that provides the list of performance nodes to select from */
    private ListDataProvider<Serializable> perfNodeProvider = new ListDataProvider<Serializable>();
    
    /** A table that renders the list of performance nodes*/
    private CellTable<Serializable> perfNodeList = new CellTable<Serializable>(Integer.MAX_VALUE);
    
    /** The list of performance nodes that contain one or more of the author's entered search terms */
    private static List<Serializable> perfNodesMatchingTerms = new ArrayList<Serializable>();

    /**
     * Creates a new panel for selecting performance nodes, initializes its list of nodes, and sets up its event handlers
     */
    private PerfNodeSelectorPanel() {
        
        super();
        
        addStyleName("contextMenu");
        
        //initialize the performance node list
        perfNodeList.getElement().getStyle().setProperty("maxHeight", "400px");
        perfNodeList.getElement().getStyle().setOverflowY(Overflow.AUTO);
        perfNodeList.getElement().getStyle().setPosition(Position.STATIC);
        perfNodeList.getElement().getStyle().setDisplay(Display.BLOCK);
        
        perfNodeList.addColumn(new Column<Serializable, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(Serializable object) {
                
                //render the performance node's icon and display name
                IconType iconType = ScenarioElementUtil.getTypeIcon(object);
                String name = ScenarioElementUtil.getObjectName(object);
                
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.appendHtmlConstant("<div class='perfNodeSelectorChoice'>");
                builder.appendHtmlConstant(new Icon(iconType).getElement().getString());
                builder.appendHtmlConstant("<div style='margin-left: 5px;'");
                
                if(perfNodesMatchingTerms.contains(object)) {
                    
                    //add special styling to nodes that match the author's search terms so that they stand out
                    builder.appendHtmlConstant(" class='perfNodeMatch'");
                }
                builder.appendHtmlConstant(" title='");
                builder.appendEscaped(name);
                builder.appendHtmlConstant("'>");
                builder.appendEscaped(name);
                builder.appendHtmlConstant("</div></div>");
                
                return builder.toSafeHtml();
            }
        });
        
        final SingleSelectionModel<Serializable> perfNodeSelector = new SingleSelectionModel<>();
        perfNodeSelector.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                
                Serializable node = perfNodeSelector.getSelectedObject();
                
                if(currentPicker != null && node != null) {
                    
                    //update the node picker with the selected node
                    if(node instanceof Task) {
                        currentPicker.setValue(((Task) node).getNodeId(), true);
                        
                    } else if(node instanceof Concept) {
                        currentPicker.setValue(((Concept) node).getNodeId(), true);
                    }
                    
                    hideSelector();
                    
                    perfNodeSelector.setSelected(node, false);
                }
            }
        });
        
        perfNodeList.setSelectionModel(perfNodeSelector);
        
        perfNodeProvider.addDataDisplay(perfNodeList);
        perfNodeProvider.refresh();
        
        setWidget(perfNodeList);
        
        Event.addNativePreviewHandler(new NativePreviewHandler() {

            @Override
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                // Nick: This logic imitates the auto-hiding logic in
                // PopupPanel.previewNativeEvent()
                if (event.isCanceled()) {
                    return;
                }

                // If the event targets the popup or the partner, consume it
                Event nativeEvent = Event.as(event.getNativeEvent());
                boolean eventTargetsPopupOrPartner = eventTargetsPopup(nativeEvent) || eventTargetsPartner(nativeEvent);
                if (eventTargetsPopupOrPartner) {
                    event.consume();
                }

                // Switch on the event type
                int type = nativeEvent.getTypeInt();
                switch (type) {

                case Event.ONMOUSEDOWN:
                case Event.ONTOUCHSTART:
                    // Don't eat events if event capture is enabled, as this can
                    // interfere with dialog dragging, for example.
                    if (DOM.getCaptureElement() != null) {
                        event.consume();
                        return;
                    }

                    if (!eventTargetsPopupOrPartner) {
                        hideSelector();
                        return;
                    }
                    break;
                }
            }
        });
    }
    
    /**
     * Shows the performance node selector for the given performance node picker and applies that picker's search text as a filter
     * for the selector's list of performance nodes
     * 
     * @param picker the performance node picker that the selector is being shown for.  Shouldn't be null.
     */
    public static void showSelector(PerfNodeSelectorImpl picker) {
        
        if(currentPicker == null || !currentPicker.equals(picker)) {
            
            //if a different waypoint picker is attempting to show the selector, remove the selector from the old picker
            getInstance().removeFromParent();
        }
        
        currentPicker = picker;
        
        //apply the picker's search text as a filter and update the list of nodes
        loadAndFilterPerfNodes(picker.getTaskNameToIgnore());
        
        if(currentPicker != null) {
            
            //show the selector below the picker
            currentPicker.getSelectorPanel().add(getInstance());
            currentPicker.getSelectorPanel().show();
        }
    }
    
    /**
     * Loads the list of performance nodes from the underlying schema objects and filters it based on the search text entered into the
     * current performance node picker (if applicable). If no search text has been entered, then all of the nodes will be shown.
     * @param nodeNameToIgnore the task name to filter out in this selector widget.  Can be null
     */
    public static void loadAndFilterPerfNodes(String nodeNameToIgnore) {
        
        String filterText = null;
        
        if(currentPicker != null && StringUtils.isNotBlank(currentPicker.getSearchBox().getText())) {
            filterText = currentPicker.getSearchBox().getText().toLowerCase();
        }
        
        List<Serializable> filteredPerfNodeList = new ArrayList<>();
        
        //determine search terms in the filter based on whitespace
        String[] searchTerms = null;
        
        if(filterText != null) {
            searchTerms = filterText.split("\\s+");
        }
        
        perfNodesMatchingTerms.clear();
        List<Serializable> perfNodesMissingTerms = new ArrayList<>();
        
        Tasks tasks = ScenarioClientUtility.getTasks();
        
        if(tasks != null) {
            for(final Task task : tasks.getTask()) {
                
                // check if this task should be ignored
                if(StringUtils.equalsIgnoreCase(nodeNameToIgnore, task.getName())){
                    continue;
                }
                        
                if(currentPicker != null && currentPicker.getSelectsTasks()) {
                    
                    if(containsSearchTerm(task.getName(), searchTerms)) {
                        perfNodesMatchingTerms.add(task);
                        
                    } else {
                        perfNodesMissingTerms.add(task);
                    }
                    
                } else if(task.getConcepts() != null) {
                    
                    for(Concept concept : task.getConcepts().getConcept()) {
                        
                        loadAndFilterChoice(concept, perfNodesMatchingTerms, perfNodesMissingTerms, searchTerms);
                    }
                }
            }
        }
        
        //populate the list of nodes, with nodes that match search terms listed first
        filteredPerfNodeList.addAll(perfNodesMatchingTerms);
        filteredPerfNodeList.addAll(perfNodesMissingTerms);
        
        getInstance().perfNodeProvider.getList().clear();
        getInstance().perfNodeProvider.getList().addAll(filteredPerfNodeList);
        
        getInstance().perfNodeProvider.refresh();
    }
    
    /**
     * Loads the list of performance nodes from the given concept and filters it based on the search text entered into the
     * current performance node picker (if applicable). If no search text has been entered, then all of the nodes will be shown.
     * 
     * @param concept the concept to look for nodes in
     * @param perfNodesMatchingTerms the list of performance nodes matching at least one search term
     * @param perfNodesMissingTerms the list of performance nodes missing any search terms
     * @param searchTerms the search terms to look for
     */
    private static void loadAndFilterChoice(Concept concept, List<Serializable> perfNodesMatchingTerms,
            List<Serializable> perfNodesMissingTerms, String[] searchTerms) {
        
    	
		if (ScenarioClientUtility.getScenario().getResources().getSourcePath() == null
				|| concept.getExternalSourceId() != null) {
			if (containsSearchTerm(concept.getName(), searchTerms)) {
				perfNodesMatchingTerms.add(concept);

			} else {
				perfNodesMissingTerms.add(concept);
			}
		}
        
        if(concept.getConditionsOrConcepts() instanceof Concepts) {
            
            Concepts concepts = (Concepts) concept.getConditionsOrConcepts();
            
            for(Concept childConcept : concepts.getConcept()) {
                loadAndFilterChoice(childConcept, perfNodesMatchingTerms, perfNodesMissingTerms, searchTerms);
            }
        }
    }

    /**
     * Gets the singleton instance of this class
     * 
     * @return the singleton instance
     */
    private static PerfNodeSelectorPanel getInstance() {
        
        if(instance == null) {
            instance = new PerfNodeSelectorPanel();
        }
        
        return instance;
    }
    
    /**
     * Hides the performance node selector
     */
    public static void hideSelector() {
        
        if(currentPicker != null) {
            currentPicker.getSelectorPanel().hide();
        }
    }
    
    /**
     * Does the event target this popup?
     *
     * @param event the native event
     * @return true if the event targets the popup
     */
    private boolean eventTargetsPopup(NativeEvent event) {
      EventTarget target = event.getEventTarget();
      if (Element.is(target)) {
          return getElement().isOrHasChild(Element.as(target));
      }
      return false;
    }
    
    /**
     * Does the event target one of the partner elements?
     *
     * @param event the native event
     * @return true if the event targets a partner
     */
    private boolean eventTargetsPartner(NativeEvent event) {
        
      if (currentPicker == null) {
          return false;
      }

      EventTarget target = event.getEventTarget();
      if (Element.is(target)) {
          if (currentPicker.getSearchBox().getElement().isOrHasChild(Element.as(target))) {
              return true;
          }
      }
      return false;
    }
    
    /**
     * Gets whether or not the given string contains any of the author's search terms
     * 
     * @param toSearch the string to search for terms in
     * @param searchTerms the author's terms to search for
     * @return whether or not the string contains any of the author's search terms
     */
    private static boolean containsSearchTerm(String toSearch, String[] searchTerms) {
        
        if(toSearch != null && searchTerms != null) {
            
            //check if each waypoint contains the necessary search terms
            for(int i = 0; i < searchTerms.length; i++) {
                
                if(toSearch.toLowerCase().contains(searchTerms[i])) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
