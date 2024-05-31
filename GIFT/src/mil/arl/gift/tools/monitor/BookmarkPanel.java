/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.logger.BookmarkEntry;
import mil.arl.gift.common.logger.BookmarkReader;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.message.Message;

/*
 *      Ideas for improvement: 
 *      
 *      Put date format into properties file (what about column width?)
 *      Put default panel size into properties file
 *      get domain session start time from Domain Module      
 *      Hot-key for creating bookmarks
 *      auto select freshly created bookmarks (set focus so it's immediately editable)      
 *      Multi-select/multi-delete
 *      Hot-key for editing in dialog box
 *      auto-complete?
 */
/**
 * Extension of JPanel that allows creation of time-stamped bookmarks for active
 * (or very recently active) domain sessions.
 *
 * @author cragusa
 *
 */
public class BookmarkPanel extends JPanel implements DomainSessionStatusListener {

    /**
     * Log4j logger for BookmarkPanel
     */
    private static Logger logger = LoggerFactory.getLogger(BookmarkPanel.class);

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PANEL_WIDTH = 800;

    private static final int DEFAULT_PANEL_HEIGHT = 600;

    /**
     * Number of milliseconds that domain session entries will persist in the
     * panel after domainSessionInactive is called (presumably because the
     * domain session has ended). This allows the user to edit bookmarks for a
     * time after the domain session is over.
     */
    private static final int TIMER_DELAY_MILLIS = MonitorModuleProperties.getInstance().getPostDomainSessionBookmarkTimeoutSeconds() * 1000;

    private BookmarkAnnotationDialog editDialog = new BookmarkAnnotationDialog(MonitorModule.getInstance().getFrame());

    static {

        if (logger.isInfoEnabled()) {

            logger.info("TIMER_DELAY_MILLIS is: " + TIMER_DELAY_MILLIS);
        }
    }
    /**
     * Constant boolean value used to improve readability
     */
    private static final boolean FIRST_TIME = true;

    /**
     * Constant defining the column of the bookmark annotation.
     */
    private static final int ANNOTATION_COLUMN_INDEX = 2;

    /**
     * Constant defining the column of the timestamp value.
     */
    private static final int TIMESTAMP_COLUMN_INDEX = 0;
    
    /**
     * Constant defining the column of the domain session timestamp value.
     */
    private static final int DS_TIMESTAMP_COLUMN_INDEX = 1;

    /**
     * The column ID's.
     */
    private static final String[] COLUMN_IDS = new String[]{
        "Time Stamp",
        "Elapsed Time",
        "Annotation"
    };

    /**
     * Static final reference to the domainSessions directory.
     */
    private static final String DOM_SESSIONS_DIR = PackageUtil.getDomainSessions();

    /**
     * Table used to hold the bookmarks. Simple extension of JTable which only
     * allows editing of the annotation field.
     */
    private JTable bookmarkTable = new JTable() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == ANNOTATION_COLUMN_INDEX;
        }
    };

    /**
     * Container for bookmarks.
     */
    private BookmarkListTableModel bookmarkListTableModel = new BookmarkListTableModel();

    /**
     * Container for the domainSessions
     */
    private DefaultListModel<DomainSessionEntry> domainSessionListModel = new DefaultListModel<>();

    /**
     * Button to used to create a new bookmark.
     */
    private JButton createBookmarkButton = new JButton("Create Bookmark");

    /**
     * Component to present the list of domain sessions as JList.
     */
    private JList<DomainSessionEntry> domainSessionList = new JList<DomainSessionEntry>() {
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredSize() {

            final int DOMAIN_SESSION_LIST_WIDTH = 120;
            final int DOMAIN_SESSION_LIST_HEIGHT = 120;

            return new Dimension(DOMAIN_SESSION_LIST_WIDTH, DOMAIN_SESSION_LIST_HEIGHT);
        }
    };
    
    /**
     * The start times associated with the IDs of all active domain sessions
     */
    final HashMap <Integer, Date> domainSessionStartTimes = new HashMap<Integer, Date>();

    /**
     * Date subclass with the ability to print itself using a format string.
     *
     * @author cragusa
     *
     */
    private class FormattedDate extends Date {

        private static final long serialVersionUID = 1L;

        //TODO: pull the string from the properties file?
        private static final String formatString = "h:mm:ss a";

        private final SimpleDateFormat sdf = new SimpleDateFormat(formatString);

        public FormattedDate(long time) {
            super(time);
        }

        @Override
        public String toString() {

            return sdf.format(this);
        }
    }

    /**
     * Extension to DefaulTableModel which allows reading/writing data from/to
     * disk files. Instances are used for each domain session.
     *
     * @author cragusa
     *
     */
    private class BookmarkListTableModel extends DefaultTableModel {

        private static final long serialVersionUID = 1L;

        /**
         * The domain session information.
         */
        private DomainSession domainSession;

        /**
         * Constructs BookmarkListTableModel instance with no args.
         */
        private BookmarkListTableModel() {

            setColumnIdentifiers(COLUMN_IDS);
        }

        /**
         * Constructs a BookmarkListTableModel for a particular domainSessionId.
         *
         * @param domainSession the domain session info 
         * @throws IOException if a problem is encountered while loading
         * bookmarks from file.
         */
        private BookmarkListTableModel(DomainSession domainSession) throws IOException {
            this();

            this.domainSession = domainSession;

            loadBookmarks();

            TableModelListener listener = new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent arg0) {

                    try {

                        //rewrite the file to disk any time the bookmark table changes
                        writeToFile();

                    } catch (IOException e) {

                        logger.error("Failed to write bookmarks to file!", e);
                    }
                }
            };

            this.addTableModelListener(listener);
        }

        /**
         * Generates the bookmark filename as a string using the domain session info.
         *
         * @param domainSession the domain session info for the current session.
         * @return String - the generated filename of the bookmark file
         */
        private String genFilename(DomainSession domainSession) {        	
      	
            String filename = DOM_SESSIONS_DIR + File.separator + domainSession.buildLogFileName() + File.separator + BookmarkReader.genFilename(domainSession);
            return filename;
        }

        /**
         * Load bookmarks from file. If file doesn't exist it will be created.
         *
         * throws IOException if any file IO operations are encountered.
         */
        private void loadBookmarks() throws IOException {

            String filename = genFilename(domainSession);

            if (logger.isInfoEnabled()) {

                logger.info("loading bookmarks: " + filename);
            }

            File file = new File(filename);

            if (!file.createNewFile()) { //if the file doesn't exist we create a new one. otherwise we read the existing one.

                //if the file already exists, read/parse it
                BookmarkReader reader = new BookmarkReader();
                reader.parse(filename);
                for (BookmarkEntry entry : reader.getBookmarks()) {
                    addBookmarkEntry(entry);
                }
            }
            //else the file didn't previously exist, but now it does -- it just doesn't have any bookmarks so there's nothing more to do.
        }

        /**
         * Adds a BookmarkEntry to the BookmarkListTableModel.
         *
         * @param entry
         */
        private void addBookmarkEntry(BookmarkEntry entry) {

            if (logger.isDebugEnabled()) {

                logger.debug("adding bookmark entry: " + entry);
            }

            Vector<Object> row = new Vector<Object>();
            row.add(new FormattedDate(entry.getTime()));
            row.add(entry.getDomainSessionTime() != null ? entry.getDomainSessionTime() : "");
            row.add(entry.getAnnotation());
            this.addRow(row);
        }

        /**
         * Write the contents of the BookmarkListTableModel to disk.
         *
         * @throws IOException if any IO problems are encountered.
         */
        private void writeToFile() throws IOException {

            String filename = genFilename(domainSession);

            if (logger.isInfoEnabled()) {

                logger.info("writing bookmarks: " + filename);
            }

            Writer writer = new FileWriter(filename);
            
            //write user id and domain session id
            writer.write(domainSession.getUserId() + " " + domainSession.getDomainSessionId());

            int rowCount = this.getRowCount();

            for (int r = 0; r < rowCount; r++) {

                Date date = (Date) this.getValueAt(r, 0);
                double elapsedTime = (double) this.getValueAt(r, 1);
                String text = (String) this.getValueAt(r, 2);
                writer.write(date.getTime() + BookmarkReader.DELIM + elapsedTime + BookmarkReader.DELIM + text + "\n");
            }

            writer.close();
        }
    }

    /**
     * Class to represent the individual domain session entries.
     *
     * @author cragusa
     *
     */
    private class DomainSessionEntry {

        /** The domain session id for this instance */
        private DomainSession domainSession;

        /** flag indicating if this entry remains active (influences the return
         * value of toString) */
        private boolean active;

        /**
         * Constructor using the domain session ID and the user ID.
         *
         * @param domainSession the domain session info.
         */
        private DomainSessionEntry(DomainSession domainSession) {
            this.domainSession = domainSession;
            this.active = true;
        }

        @Override
        public int hashCode() {

            return domainSession.getDomainSessionId();
        }

        @Override
        public boolean equals(Object obj) {

            boolean equal = false;

            if (obj != null && obj instanceof DomainSessionEntry) {

                DomainSessionEntry that = (DomainSessionEntry) obj;

                equal = (this.domainSession.getDomainSessionId() == that.domainSession.getDomainSessionId());
            }

            return equal;
        }

        @Override
        public String toString() {

            return "" + domainSession.getUserId() + " : " + domainSession.getDomainSessionId() + " : " + (active ? "active" : "complete");
        }
    }

    /**
     * Loads bookmarks from disk file for a domainSessionEntry.
     *
     * @param domainSessionEntry the domain session entry for which bookmarks
     * should be loaded.
     * @throws IOException if the load process is unsuccessful.
     */
    private void loadBookmarks(DomainSessionEntry domainSessionEntry) throws IOException {

        bookmarkListTableModel = new BookmarkListTableModel(domainSessionEntry.domainSession);
        
        bookmarkTable.setModel(bookmarkListTableModel);

        setupBookmarkTable(!FIRST_TIME);

        bookmarkTable.doLayout();
    }

    /**
     * Creates a new bookmark for the currently selected domain session entry.
     */
    private void createBookmark() {

        final DomainSessionEntry domainSessionEntry = domainSessionList.getSelectedValue();               

        if (domainSessionEntry != null) {
        	
        	final int domainSessionID = domainSessionEntry.domainSession.getDomainSessionId();      	
        	final BookmarkPanel bookmarkPanel = this;
        	final long currentTime = System.currentTimeMillis();

            logger.info("create bookmark for: " + domainSessionEntry);      
            
            if(!domainSessionStartTimes.containsKey(domainSessionID)){
     
	    		MonitorModule.getInstance().requestDomainSessionStartTime(domainSessionID, new MessageCollectionCallback() {
	                
	    			private void addBookmarkEntryToTable(BookmarkEntry entry){
	    				
	    				bookmarkListTableModel.addBookmarkEntry(entry);
                    	
                		int rowCount = bookmarkTable.getRowCount();

                        if (rowCount > 1) {

                             bookmarkTable.requestFocusInWindow();
                             bookmarkTable.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
                             bookmarkTable.setColumnSelectionInterval(ANNOTATION_COLUMN_INDEX, ANNOTATION_COLUMN_INDEX);

                        }

                        bookmarkTable.doLayout();
	    			}
	    			
	    			 private int displayBookmarkCreationErrorMessage(){
	    			    	
    			    	Object[] options = {"Yes", "No"};
    			    	
    			    	int choice = JOptionPane.showOptionDialog(
    			    			bookmarkPanel, 
    			    			"A start time for the selected domain session could not be found within the UMS database.\n\n"
    			    			+ "To avoid parsing errors, the elapsed domain session time value for this domain session will\n"
    			    			+ "be set to a dummy value of -1 should a bookmark for this domain session still be created.\n\n"
    			    			+ "This could potentially make bookmark entries for this domain session unable to be properly\n"
    			    			+ "sorted by domain session time when generating a report using the Event Report Tool\n"
    			    			+ "but will otherwise leave them unchanged.\n\n"
    			    			+ "Would you still like to create a bookmark for this domain session?", 
    			    			"Bookmark Creation Error", 
    			    			JOptionPane.YES_NO_OPTION, 
    			    			JOptionPane.ERROR_MESSAGE, 
    			    			null, 
    			    			options, 
    			    			options[1]);
    			    	
    			    	return choice;
	    			 }
	    			
	                @Override
	                public void success() {
	                    //Nothing to do                 
	                }
	                
	  			  	@Override
	                public void received(Message msg) {
	              	  Object payload = msg.getPayload();
	                    if(payload != null && payload instanceof Date){         
	                    	logger.info("Received start time of " + payload + " for domain session with ID of " + domainSessionID);
	                    	domainSessionStartTimes.put(domainSessionID, (Date) payload);		                    	
	                    	
	                    	addBookmarkEntryToTable(new BookmarkEntry(currentTime, (currentTime - domainSessionStartTimes.get(domainSessionID).getTime())/1000.0, ""));	                    	
	                                                                                                       
	                    }else{
	                        logger.error("Received unhandled message payload when requesting start time of domain dession with ID of " + domainSessionID + " from the UMS");
	                        
	                        if(displayBookmarkCreationErrorMessage() == JOptionPane.OK_OPTION){
	                        	domainSessionStartTimes.put(domainSessionID, new Date(-1));	
	                        	addBookmarkEntryToTable(new BookmarkEntry(currentTime, -1, ""));	                		
	                        }
	                    }                    
	                }
	                
	                @Override
	                public void failure(String why) {
	                    logger.error("Unable to retrieve start time of domain session with ID of "+domainSessionID+" because "+why);                       
	                    
	                    if(displayBookmarkCreationErrorMessage() == JOptionPane.OK_OPTION){
	                    	domainSessionStartTimes.put(domainSessionID, new Date(-1));	
                        	addBookmarkEntryToTable(new BookmarkEntry(currentTime, -1, ""));
                        }
	                }
	                
	                @Override
	                public void failure(Message msg) {
	                    logger.error("Unable to retrieve start time of domain session with ID of "+domainSessionID+" because received response message of "+msg);
	                    
	                    if(displayBookmarkCreationErrorMessage() == JOptionPane.OK_OPTION){
	                    	domainSessionStartTimes.put(domainSessionID, new Date(-1));	
                        	addBookmarkEntryToTable(new BookmarkEntry(currentTime, -1, ""));
                        }
	                }
	        	});       
	    		
            }else{
            	
            	if(domainSessionStartTimes.get(domainSessionID).getTime() != -1){
            		bookmarkListTableModel.addBookmarkEntry(new BookmarkEntry(currentTime, (currentTime - domainSessionStartTimes.get(domainSessionID).getTime())/1000.0, ""));
            	
            	}else{
            		bookmarkListTableModel.addBookmarkEntry(new BookmarkEntry(currentTime, -1, ""));
            	}
            	
        		int rowCount = bookmarkTable.getRowCount();

                if (rowCount > 1) {

                     bookmarkTable.requestFocusInWindow();
                     bookmarkTable.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
                     bookmarkTable.setColumnSelectionInterval(ANNOTATION_COLUMN_INDEX, ANNOTATION_COLUMN_INDEX);

                }

                bookmarkTable.doLayout();
            }
    	}                          
    }

    /**
     * Reacts to changes in selection of domainSessionList, by loading the
     * corresponding bookmarks. createBookmarkButton is activated.
     *
     * @param event
     */
    private void listSelectionChange(ListSelectionEvent event) {

        if (!event.getValueIsAdjusting()) {

            DomainSessionEntry domainSessionEntry = domainSessionList.getSelectedValue();

            if (domainSessionEntry != null) {

                createBookmarkButton.setEnabled(true);

                try {

                    loadBookmarks(domainSessionEntry);
                } catch (IOException ex) {

                    System.err.println(ex);
                    ex.printStackTrace();
                }
            } else {

                createBookmarkButton.setEnabled(false);

                bookmarkListTableModel = new BookmarkListTableModel();
                bookmarkTable.setModel(bookmarkListTableModel);
                bookmarkTable.doLayout();
            }
        }
    }

    /**
     * Deletes the specified row from the bookmark table.
     *
     * @param row the index of the row to delete.
     */
    private void doDeletion(int row) {

        Object timestamp = bookmarkListTableModel.getValueAt(row, TIMESTAMP_COLUMN_INDEX);

        int result = JOptionPane.showConfirmDialog(BookmarkPanel.this, "Delete Bookmark: \n" + timestamp + "?", "Confirmation", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {

            if (logger.isInfoEnabled()) {
                logger.info("removing bookmark row: " + row);
            }

            bookmarkListTableModel.removeRow(row);

            bookmarkListTableModel.fireTableDataChanged();

            int count = bookmarkListTableModel.getRowCount();

            if (row == count) {
                bookmarkTable.getSelectionModel().setSelectionInterval(row - 1, row - 1);
            } else {
                bookmarkTable.getSelectionModel().setSelectionInterval(row, row);
            }
        }
    }

    /**
     * Sets up the bookmark table.
     *
     * @param firstTime flag that if set (i.e. if it's the first time) will
     * cause additional setup processing to occur.
     *
     */
    private void setupBookmarkTable(boolean firstTime) {

        bookmarkTable.setShowHorizontalLines(true);
        bookmarkTable.setShowVerticalLines(true);

        bookmarkTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (firstTime) {

            final JPopupMenu menu = new JPopupMenu();

            JMenuItem editItem = new JMenuItem("Edit");

            editItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {

                    int row = bookmarkTable.getSelectedRow();

                    Object value = bookmarkListTableModel.getValueAt(row, ANNOTATION_COLUMN_INDEX);

                    editDialog.showDialog(value.toString());

                    if (editDialog.getResult() == BookmarkAnnotationDialog.OKAY) {

                        String text = editDialog.getText();

                        text = text.replace("\t", " ");
                        text = text.replace("\n", " ");

                        bookmarkListTableModel.setValueAt(text, row, ANNOTATION_COLUMN_INDEX);

                        bookmarkListTableModel.fireTableDataChanged();

                    }

                }
            });

            menu.add(editItem);


            JMenuItem deleteItem = new JMenuItem("Delete");

            deleteItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {

                    doDeletion(bookmarkTable.getSelectedRow());
                }
            });

            menu.add(deleteItem);


            //do this here instead of in setupBookmarkTable to ensure it's only done once.
            bookmarkTable.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {

                    if (e.getKeyCode() == KeyEvent.VK_DELETE) {

                        int selectedRow = bookmarkTable.getSelectedRow();

                        if (selectedRow >= 0) {

                            doDeletion(selectedRow);
                        }
                    }
                }
            });

            class PopupListener extends MouseAdapter {

                @Override
                public void mousePressed(MouseEvent e) {
                    showPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {

                        int row = bookmarkTable.getSelectedRow();

                        if (row >= 0) {

                            menu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }

            MouseListener popupListener = new PopupListener();

            bookmarkTable.addMouseListener(popupListener);
        }


        bookmarkTable.setModel(bookmarkListTableModel);
        bookmarkTable.getTableHeader().setReorderingAllowed(false);
        final int COLUMN_MARGIN = 10;
        bookmarkTable.getColumnModel().setColumnMargin(COLUMN_MARGIN);
        bookmarkTable.getColumnModel().setColumnSelectionAllowed(false);

        TableColumn column = bookmarkTable.getColumnModel().getColumn(TIMESTAMP_COLUMN_INDEX);
        final int DATE_COLUMN_WIDTH = 85;
        column.setMaxWidth(DATE_COLUMN_WIDTH);
        column.setPreferredWidth(DATE_COLUMN_WIDTH);
        
        TableColumn dscolumn = bookmarkTable.getColumnModel().getColumn(DS_TIMESTAMP_COLUMN_INDEX);
        final int DS_DATE_COLUMN_WIDTH = 85;
        dscolumn.setMaxWidth(DS_DATE_COLUMN_WIDTH);
        dscolumn.setPreferredWidth(DS_DATE_COLUMN_WIDTH);
    }

    /**
     * No-arg constructor
     *
     */
    BookmarkPanel() {

        createBookmarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                createBookmark();
            }
        });


        final int BORDER_WIDTH = 10;
        Border border = BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
        this.setBorder(border);

        domainSessionList.setModel(domainSessionListModel);

        domainSessionList.setBorder(BorderFactory.createTitledBorder("Domain Sessions"));

        domainSessionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        domainSessionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                listSelectionChange(event);
            }
        });

        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        
        domainSessionList.setToolTipText("The list of active domain sessions.");
        leftPanel.add(domainSessionList, BorderLayout.CENTER);

        createBookmarkButton.setEnabled(false);
        createBookmarkButton.setToolTipText("Create a new bookmark for the selected domain session(s).");
        leftPanel.add(createBookmarkButton, BorderLayout.SOUTH);

        this.add(leftPanel, BorderLayout.WEST);

        setupBookmarkTable(FIRST_TIME);

        JScrollPane bookmarkScrollerPane = new JScrollPane(bookmarkTable);

        Border border2 = BorderFactory.createEmptyBorder(0, BORDER_WIDTH, 0, 0);
        Border border3 = BorderFactory.createLineBorder(Color.GRAY);
        Border border4 = BorderFactory.createCompoundBorder(border2, border3);
        bookmarkScrollerPane.setBorder(border4);
        bookmarkScrollerPane.setToolTipText("List of bookmarks for the selected domain session(s).");

        this.add(bookmarkScrollerPane, BorderLayout.CENTER);

    }

    @Override
    public Dimension getPreferredSize() {

        return new Dimension(DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT);
    }

    private void removeDomainSessionEntry(DomainSessionEntry entry) {

        if (domainSessionListModel.contains(entry)) {

            if (logger.isInfoEnabled()) {
                logger.info("removing domain session entry: " + entry);
            }

            domainSessionListModel.removeElement(entry);
        }
    }

    @Override
    public void domainSessionActive(DomainSession domainSession) {

        DomainSessionEntry entry = new DomainSessionEntry(domainSession);

        if (!domainSessionListModel.contains(entry)) {

            if (logger.isInfoEnabled()) {
                logger.info("adding domain session entry: " + entry);
            }

            domainSessionListModel.addElement(entry);
        }
    }

    @Override
    public void domainSessionInactive(DomainSession domainSession) {

        final DomainSessionEntry entry = new DomainSessionEntry(domainSession);

        int index = domainSessionListModel.indexOf(entry);
        if (index > -1) {
            DomainSessionEntry tableEntry = domainSessionListModel.getElementAt(index);

            if (logger.isInfoEnabled()) {
                logger.info("domain session entry went inactive: " + tableEntry);
            }

            tableEntry.active = false;
            domainSessionList.invalidate();
            domainSessionList.repaint();

            Timer timer = new Timer(TIMER_DELAY_MILLIS, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {

                    removeDomainSessionEntry(entry);
                }
            });

            timer.setRepeats(false);
            timer.start();
        }
    }
}
