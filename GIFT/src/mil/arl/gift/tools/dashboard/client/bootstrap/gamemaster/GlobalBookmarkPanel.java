/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import org.gwtbootstrap3.client.shared.event.HiddenEvent;
import org.gwtbootstrap3.client.shared.event.HiddenHandler;
import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.shared.event.ShownHandler;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.BookmarkProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.BookmarkProvider.BookmarkHandler;
import mil.arl.gift.tools.dashboard.client.gamemaster.SessionStateProvider.SessionStateUpdateHandler;
import mil.arl.gift.tools.dashboard.shared.messages.KnowledgeSessionState;
import mil.arl.gift.tools.dashboard.shared.messages.ProcessedBookmarkCache;

/**
 * A panel that listens for and displays global bookmarks
 * 
 * @author nroberts
 */
public class GlobalBookmarkPanel extends Composite implements BookmarkHandler, SessionStateUpdateHandler{

    private static GlobalBookmarkPanelUiBinder uiBinder = GWT.create(GlobalBookmarkPanelUiBinder.class);

    interface GlobalBookmarkPanelUiBinder extends UiBinder<Widget, GlobalBookmarkPanel> {
    }
    
    /** The panel where bookmarks should be rendered as they are recieved  */
    @UiField
    protected FlowPanel bookmarksPanel;
    
    /** The header for the history of global bookmarks */
    @UiField
    protected PanelHeader bookmarkHeader;
    
    /** The collapse for the history of global bookmarks */
    @UiField
    protected Collapse bookmarkCollapse;
    
    /** The currently selected bookmark entry displayed by this widget */
    private BookmarkEntry selectedEntry = null;

    /** The navigator that can be used to jump to a specific point in the session's timeline */
    private TimelineNavigator timelineNavigator;
    
    /** The current knowledge session */
    private final AbstractKnowledgeSession knowledgeSession;

    /**
     * Creates a new panel to handle global bookmarks and registeres it to receive bookmark updates
     * 
     * @param knowledgeSession the knowledge session used to populate the bookmarks
     */
    public GlobalBookmarkPanel(AbstractKnowledgeSession knowledgeSession) {
        if (knowledgeSession == null) {
            throw new IllegalArgumentException("The parameter 'knowledgeSession' cannot be null.");
        }
        
        this.knowledgeSession = knowledgeSession;
        
        initWidget(uiBinder.createAndBindUi(this));
        
        BookmarkProvider.getInstance().addHandler(this);
        
        bookmarkHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCollapse(bookmarkCollapse.isShown() ? null : bookmarkCollapse);
            }
        }, ClickEvent.getType());
        
        bookmarkCollapse.addHiddenHandler(new HiddenHandler() {
            
            @Override
            public void onHidden(HiddenEvent event) {
                
                //deselect the currently selected bookmark when bookmarks are hidden
                setBookmarkSelected(null, true);
            }
        });
    }
    
    /**
     * Opens the provided collapse and closes all others.
     *
     * @param collapse the collapse to open.
     */
    private void openCollapse(Collapse collapse) {
        boolean hideOthers = false;
        
        if (!hideOthers && bookmarkCollapse.equals(collapse)) {
            bookmarkCollapse.show();
            hideOthers = true;
        } else {
            bookmarkCollapse.hide();
        }
    }

    /**
     * Removes all of the bookmarks that are currently being displayed by this panel
     */
    public void clear() {
        bookmarksPanel.clear();
        selectedEntry = null;
    }

    @Override
    public void addBookmark(int domainSessionId, String evaluator, final long timestamp, String comment, String media,
            boolean isPatched) {
        
        final BookmarkEntry entry = new BookmarkEntry(timestamp, comment, media, evaluator);
        entry.addDomHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                //change this bookmark entry's selections state when it is clicked on
                setBookmarkEntrySelected(entry, !entry.equals(selectedEntry));
                
                if(timelineNavigator != null) {
                    
                    /* If navigating through the session timeline is possible, then allow the user to jump
                     * to the time when a bookmark was applied whenever they click said bookmark to expand it */
                    timelineNavigator.seekTo(timestamp);
                }
            }
        }, ClickEvent.getType());
        
        if(knowledgeSession.inPastSessionMode()){
            
            //if this is a past session, insert bookmarks in the history order
            bookmarksPanel.add(entry);
            
        } else {
            
            //otherwise, insert bookmarks so that the most recent is at the top
            bookmarksPanel.insert(entry, 0);
        }
    }
    
    /**
     * Selects or deselects the bookmark at the given timestamp to show or hide its details
     * 
     * @param timestamp the timestamp of the bookmark to select/deselect. If null, the
     * currently selected bookmark will be deselected.
     * @param selected whether to select or deselect the bookmark. Will be ignored if
     * timestamp is null.
     */
    public void setBookmarkSelected(Long timestamp, boolean selected) {
        
        if(timestamp == null) {
            setBookmarkEntrySelected(null, true);
            return;
        }
        
        for(Widget w : bookmarksPanel) {
            if(w instanceof BookmarkEntry) {
                
                BookmarkEntry entry = (BookmarkEntry) w;
                if(timestamp == entry.getTimestamp()) {
                    setBookmarkEntrySelected(entry, selected);
                    return;
                }
            }
        }
    }
    
    /**
     * Selects or deselects the given bookmark entry to show or hide its details. Only one entry
     * may be selected at a time, so if another entry is currently selected, it will be deselected
     * 
     * @param entry the bookmark entry to select or deselect. If null, If null, the
     * currently selected bookmark will be deselected.
     * @param selected @param selected whether to select or deselect the bookmark entry. Will be ignored if
     * entry is null.
     */
    private void setBookmarkEntrySelected(BookmarkEntry entry, boolean selected) {
        
        if(entry != null) {
            
            //update the specifed bookmark entry's selected state
            entry.setSelected(selected);
        }
        
        if(Objects.equals(selectedEntry, entry)) {
            
            //if the selected entry is deselected, reset the selection
            if(!selected) {
                selectedEntry = null;
            }
            
        } else {
            
            //if a new entry is selected, deselect the old selection
            if(selectedEntry != null) {
                selectedEntry.setSelected(false);
            }
            
            //updated the selection to point to the new entry
            if(selected) {
                selectedEntry = entry;
            }
        }
        
        if(selectedEntry != null) {
            selectedEntry.getElement().scrollIntoView(); //scroll to the selected entry
        }
    }

    /**
     * Sets a navigator that can be used to set the current session time to a particular
     * point in a knowledge session's timeline.
     * 
     * @param navigator the timeline navigator. Can be null, if navigating through a 
     * timeline should not be possible, such as for active sessions.
     */
    public void setTimelineNavigator(TimelineNavigator navigator) {
        this.timelineNavigator = navigator;
    }
    
    /**
     * A method that adds a {@link Collection} of {@link ProcessedBookmarkCache
     * cached processed bookmarks} to the bookmark panel.
     *
     * @param cachedBookmarks The {@link Collection} of
     *        {@link ProcessedBookmarkCache cached processed bookmarks} to add.
     *        If null or empty, no action is taken.
     */
    public void loadBookmarkCache(Collection<ProcessedBookmarkCache> cachedBookmarks) {
        if (CollectionUtils.isEmpty(cachedBookmarks)) {
            return;
        }

        /* Add to bookmarks panel */
        for (ProcessedBookmarkCache cachedItem : cachedBookmarks) {
            addBookmark(knowledgeSession.getHostSessionMember().getDomainSessionId(), 
                    cachedItem.getUserPerformed(), 
                    cachedItem.getTimePerformed(), 
                    cachedItem.getText(), 
                    cachedItem.getMedia(), 
                    false);
        }
    }
    
    /**
     * Opens the global bookmark collapse panel and closes the others.
     * 
     * @param selectTimestamp the timestamp of the bookmark to select when 
     * the bookmark panel is shown. If null, no bookmark will be selected.
     */
    public void openGlobalBookmarkPanel(final Long selectTimestamp) {
        
        if(selectTimestamp != null) {
            if(bookmarkCollapse.isShown()) {
                
                /* If we need to expand and scroll to a bookmark and the bookmark history panel
                 * is already expanded, then just expand the bookmark immediately */
                setBookmarkSelected(selectTimestamp, true);
                
            } else {
                
                /* If we need to expand and scroll to a bookmark and the bookmark history panel
                 * is closed, then we need to wait until it is fully open before we expand the
                 * bookmark, otherwise scrolling to it will not work properly because the bookmark
                 * is still being moved. */
                final HandlerRegistration[] registration = {};
                registration[0] = bookmarkCollapse.addShownHandler(new ShownHandler() {
                    
                    @Override
                    public void onShown(ShownEvent event) {
                        
                        setBookmarkSelected(selectTimestamp, true);
                        registration[0].removeHandler();
                    }
                });
            }
        }
        
        openCollapse(bookmarkCollapse);
    }
    
    @Override
    public void sessionStateUpdate(KnowledgeSessionState state, int domainSessionId) {
        
        if (knowledgeSession.getHostSessionMember().getDomainSessionId() != domainSessionId) {
            return;
        }

        /* Null assessment means that entity received a "visual only" state. Do
         * not process further. This was implemented as a quick hack for ARES
         * visualization. */
        if (state.getLearnerState() != null) {
            PerformanceState performance = state.getLearnerState().getPerformance();
            for (TaskPerformanceState perfState : performance.getTasks().values()) {
                for (ConceptPerformanceState c : perfState.getConcepts()) {
                    if (c.getState().getAssessedTeamOrgEntities().containsValue(null)) {
                        return;
                    }
                }
            }
        }
        
        if (!state.getCachedProcessedStrategies().isEmpty()) {
            loadBookmarkCache(state.getCachedProcessedBookmarks());
        }
    }

    @Override
    public void showTasks(Set<Integer> taskIds) {
        // Nothing to do
    }
}
