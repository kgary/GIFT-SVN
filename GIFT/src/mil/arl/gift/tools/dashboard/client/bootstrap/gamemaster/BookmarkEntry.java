/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to visually display a global bookmark's information
 * 
 * @author nroberts
 */
public class BookmarkEntry extends Composite {
    
    /** The time format to use when displaying the bookmark's timestamp */
    private static final DateTimeFormat TIME_LABEL_FORMAT = DateTimeFormat.getFormat("H:mm:ss MMM dd");
    
    /** A longer time format to use if a bookmark's timestamp falls in a different year */
    private static final DateTimeFormat LONG_TIME_LABEL_FORMAT = DateTimeFormat.getFormat("H:mm:ss MMM dd, yyyy");
    
    /** A time format that just get's a timestamp's year. Used to determine which year a bookmark was made */
    private static final DateTimeFormat YEAR_FORMAT = DateTimeFormat.getFormat("yyyy");

    private static BookmarkEntryUiBinder uiBinder = GWT.create(BookmarkEntryUiBinder.class);

    interface BookmarkEntryUiBinder extends UiBinder<Widget, BookmarkEntry> {
    }
    
    /** An interface used to access this class' CSS styles */
    protected interface Style extends CssResource {
    
        public String selected();
    }
    
    /** A set of styles associated with this widget */
    @UiField
    protected Style style;
    
    /** The label used to display the bookmark's timestamp */
    @UiField
    protected Label timeLabel;
    
    /** The label used to display the evaluator that created the bookmark */
    @UiField
    protected Label evaluatorLabel;
    
    /** A deck used to switch out widgets for the different bookmark types */
    @UiField
    protected DeckPanel typeDeck;
    
    /** A label used to display the text of a basic comment bookmark */
    @UiField
    protected Label commentLabel;
    
    /** A player used to play the audio of media bookmarks */
    @UiField
    protected AudioPlayer audioPlayer;
    
    /** The timestamp of the bookmark represented by this widget */
    private long timestamp;

    /**
     * Creates a new entry representing a bookmark with the given data
     * 
     * @param timestamp the timestamp of the bookmark
     * @param comment the comment text of the bookmark, if any
     * @param media the media of the bookmark, if any
     * @param evaluator the evaluator that created the bookmark, if any
     */
    public BookmarkEntry(long timestamp, String comment, String media, String evaluator) {
        
        initWidget(uiBinder.createAndBindUi(this));
        
        this.timestamp = timestamp;
        
        /* Only show the year of the timestamp if the bookmark was created in a different year than 
         * when it is being viewed. This helps save some horizontal space next to the time label */
        Date date = new Date(timestamp);
        if(!YEAR_FORMAT.format(date).equals(YEAR_FORMAT.format(new Date(System.currentTimeMillis())))) {
            timeLabel.setText(LONG_TIME_LABEL_FORMAT.format(date));
            
        } else {
            timeLabel.setText(TIME_LABEL_FORMAT.format(date));
        }
        
        evaluatorLabel.setText(evaluator);
        
        if(media == null) {
            commentLabel.setText(comment);
            typeDeck.showWidget(typeDeck.getWidgetIndex(commentLabel));
            
        } else {
            audioPlayer.setUrl(media);
            typeDeck.showWidget(typeDeck.getWidgetIndex(audioPlayer));
        }
        
        audioPlayer.setDeletionEnabled(false);
    }
    
    /**
     * Sets whether this bookmark entry should visually appear selected
     * 
     * @param selected whether the entry should be selected
     */
    public void setSelected(boolean selected) {
        
        if(selected) {
            addStyleName(style.selected());
            
        } else {
            removeStyleName(style.selected());
        }
    }

    /**
     * Gets the timestamp of the bookmark represented by this widget
     * 
     * @return the timestamp of the bookmark
     */
    public long getTimestamp() {
        return timestamp;
    }
}
