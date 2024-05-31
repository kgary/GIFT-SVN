/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.ComplexAttributeFieldManager;
import mil.arl.gift.common.sensor.ComplexAttributeFieldManager.ComplexAttributeField;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.UnfilteredSensorData;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.SensorStatus;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.UserSessionMessage;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Month;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for creating/updating/disposing of sensor graphs.
 * 
 * @author mhoffman
 *
 */
public class SensorDataManager implements MonitorMessageListener, DomainSessionMonitorListener, DomainSessionStatusListener {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SensorDataManager.class);
    private static boolean isDebug = logger.isDebugEnabled();
    
    /** map of unique domain session ID to local class instance containing the sensors graph */
    private Map<Integer, LearnerSensors> domainSessionIdToSensors = new HashMap<Integer, LearnerSensors>();
    
    /** list of attributes that are blacklisted because they can not or are not providing numbers to graph */
    private List<SensorAttributeNameEnum> blacklistedAttr = new ArrayList<>();
    
    /** instance of the sensor graph panel which contains various GUI elements responsible for displaying sensor data */
    private SensorGraphPanel sgPanel = new SensorGraphPanel();
    
    /** 
     * currently the sensor graph manager can only manage displaying the sensor graphs for a single domain session, 
     * this is the id for the current domain session id being displayed 
     */
    private Integer currentDomainSessionId = null;
    
    /** instance of the sensor attribute field manager that contains information about complex sensor attribute getter methods */
    private ComplexAttributeFieldManager attributeFieldMgr = ComplexAttributeFieldManager.getInstance();

    /** singleton instance of this class */
    private static SensorDataManager instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return SensorDataManager
     */
    public static SensorDataManager getInstance(){
        
        if(instance == null){
            instance = new SensorDataManager();
        }
        
        return instance;
    }
    
    /**
     * Default constructor
     */
    private SensorDataManager(){
        
    }
    
    /**
     * The callback for when a message is received by the Monitor module
     * 
     * @param msg The message received by the Monitor module
     */
    @Override
    public void handleMessage(Message msg){
        
        //TODO: Commenting out the following code appears to remove the last "memory leak" in the monitor. 
        //      We need to investigate further to implement better memory management of the downstream code.
        if(msg.getMessageType() == MessageTypeEnum.SENSOR_FILTER_DATA){
            handleFilteredSensorMessage(msg);
        }else if(msg.getMessageType() == MessageTypeEnum.SENSOR_DATA){
            handleSensorMessage(msg);
        }else if(msg.getMessageType() == MessageTypeEnum.SENSOR_STATUS){
            handleSensorErrorMessage(msg);
        }
    }
    
    /**
     * Handle the sensor status message by providing the message to the appropriate graph
     * 
     * @param message - incoming sensor message
     */
    public void handleSensorErrorMessage(Message message) {

        SensorStatus sensorStatus = (SensorStatus) message.getPayload();

        if (message instanceof UserSessionMessage) {

            //the error is from a sensor assigned to a user
            UserSessionMessage userSessionMessage = (UserSessionMessage) message;

            sgPanel.addSensorStatusMessage(userSessionMessage.getUserId(),
                    sensorStatus,
                    Long.toString(message.getTimeStamp()));

        } else {

            sgPanel.addSensorStatusMessage(sensorStatus,
                    Long.toString(message.getTimeStamp()));
        }
    }
    
    /**
     * Handle the sensor message by providing the sensor data to the appropriate graph.
     * 
     * @param message - incoming filtered sensor message
     */
    public void handleFilteredSensorMessage(Message message){  
        
        FilteredSensorData fSensorData = (FilteredSensorData)message.getPayload();
        
        handleSensorData(((UserSessionMessage)message).getUserId(), ((DomainSessionMessage)message).getDomainSessionId(), fSensorData.getSensorName(), fSensorData.getSensorType(), 
                fSensorData.getElapsedTime(), fSensorData.getAttributeValues());
    }
    
    /**
     * Handle the sensor message by providing the sensor data to the appropriate graph.
     * 
     * @param message - incoming un-filtered sensor message
     */
    public void handleSensorMessage(Message message){
        
        UnfilteredSensorData sensorData = (UnfilteredSensorData)message.getPayload();
        
        handleSensorData(((UserSessionMessage)message).getUserId(), 
        			     ((DomainSessionMessage)message).getDomainSessionId(), 
        			     sensorData.getSensorName(), sensorData.getSensorType(), 
        			     sensorData.getElapsedTime(), sensorData.getAttributeValues());
    }
    
    /**
     * The callback for when a domain session is activated by the Monitor module
     *
     * @param domainSessionId The ID of the domain session activated by the Monitor module
     */
    @Override
    public void monitorDomainSession(int domainSessionId) {
        //set the current domain session to graph their sensors data
        logger.info("Notified that domain session " + domainSessionId + " has been activated");
        setCurrentDomainSession(domainSessionId);
    }

    /**
     * The callback for when a domain session is deactivated by the Monitor module
     *
     * @param domainSessionId The domain session deactivated by the Monitor module
     */
    @Override
    public void ignoreDomainSession(int domainSessionId) {
        //nothing to do
        logger.info("Notified that domain session " + domainSessionId + " has been deactivated");
    }

    @Override
    public void domainSessionActive(DomainSession domainSession) {
        logger.info("Notified that domain session " + domainSession.getDomainSessionId() + " has been added");
    }

    @Override
    public void domainSessionInactive(DomainSession domainSession) {
        logger.info("Notified that domain session " + domainSession.getDomainSessionId() + " has been removed");
        removeDomainSession(domainSession.getDomainSessionId());
    }

    /**
     * Set the current domain session for which sensor graphs should be available for display on the panel.
     * Note: a domain session Id of null will clear the sensor list.
     * 
     * @param domainSessionId - the domain session to use for the current graphs
     */
    public void setCurrentDomainSession(Integer domainSessionId){

        logger.debug("Changing current domain session ID from " + this.currentDomainSessionId + " to " + domainSessionId);

        //this needs to be set before showing graph as this variable is used
        this.currentDomainSessionId = domainSessionId;
        
        if(domainSessionId == null){
            //reset panel
            logger.info("Resetting sensor graph panel because domain session id is null");
            sgPanel.reset();
        }else{
            Collection<SensorGraph> sGraphs = getSensorList(domainSessionId);
            
            logger.info("Resetting sensor graph panel for domain session "+domainSessionId);
            sgPanel.reset();
            
            if(sGraphs != null){
                
                for(SensorGraph sGraph : sGraphs){
                    logger.debug("Adding sensor "+sGraph.getSensorName()+" to sensor graph panel");
                    sgPanel.addSensor(sGraph);
                }
                
            }else{
                
                logger.info("Unable to find a graph to add sensors too for domain session id = "+domainSessionId+".  The most likely cause is that no sensor data has been received yet.");
            }
            
            //show a graph, any graph
            sgPanel.showAnyGraph();
        }

    }
    
    /**
     * Remove the domain session's sensor graphs
     * 
     * @param domainSessionId - the domain session associated with one or more sensor graphs
     */
    public void removeDomainSession(Integer domainSessionId){
        
        if(domainSessionId != null){
            
            Collection<SensorGraph> sGraphs = getSensorList(domainSessionId);
            
            if(sGraphs != null){
                for(SensorGraph sGraph : sGraphs){
                    sgPanel.removeSensor(sGraph);
                }
                
                domainSessionIdToSensors.remove(domainSessionId);
                
                if(isDomainSession(domainSessionId)){
                    //reset panel
                    logger.info("Resetting sensor graph panel because domain session "+domainSessionId+" is being removed");
                    sgPanel.reset();
                }
                
            }else{
                
                logger.info("Unable to remove sensor graph panel contents because unable to find a graph for domain session id = "+domainSessionId+".  Most likely cause is that no sensor data was ever received");
            }
            
            logger.info("Removed domain session "+domainSessionId);
        }
    }
    
    /**
     * Return the list of Sensor Graph instances for the specified domain session id.
     * 
     * @param domainSessionId
     * @return Collection<SensorGraph>
     */
    private Collection<SensorGraph> getSensorList(int domainSessionId){
               
        LearnerSensors tSensors = domainSessionIdToSensors.get(domainSessionId);
        
        Collection<SensorGraph> sGraphs = null;
        if(tSensors != null){
            sGraphs = tSensors.getSensorGraphs();
        }
        
        return sGraphs;
    }
    
    /**
     * Return the chart panel for the given selected object.
     * 
     * @param sGraph - a sensor graph
     * @return ChartPanel - the panel for that graph
     */
    public ChartPanel getChartPanel(Object sGraph){
        
        if(sGraph instanceof SensorGraph){
            return ((SensorGraph)sGraph).getChartPanel();
        }
        
        return null;
    }

    /**
     * Return the current domain session ID
     *
     * @return Integer
     */
    public int getDomainSessionId() {
        return currentDomainSessionId;
    }
    
    /**
     * Return the sensor type for the given selected object.
     * 
     * @param sGraph - the graph for a sensor
     * @return String - the sensor type for the graph
     */
    public String getSensorType(Object sGraph){
        
        if(sGraph instanceof SensorGraph){
            return ((SensorGraph)sGraph).getSensorType().getDisplayName();
        }
        
        return null;
    }
    
    /**
     * Return the graph panel instance containing the current sensor graphs for a learner.
     * 
     * @return SensorGraphPanel
     */
    public SensorGraphPanel getGraphPanel(){
        return sgPanel;
    }
    
    /**
     * Get the learner sensors object for the given domain session.
     * 
     * @param userId
     * @param domainSessionId
     * @return LearnerSensors
     */
    private LearnerSensors getLearnerSensors(Integer userId, Integer domainSessionId){
        
        LearnerSensors tSensors = domainSessionIdToSensors.get(domainSessionId);
        if(tSensors == null){
            tSensors = new LearnerSensors(userId, domainSessionId);
            domainSessionIdToSensors.put(domainSessionId, tSensors);
            
            if(isDebug){
                logger.debug("Created learner sensors graph handler for domain session "+ domainSessionId);
            }

        }
        
        return tSensors;
    }
    
    /**
     * Get the sensor graph object for the given user's sensor.
     * 
     * @param userId
     * @param domainSessionId
     * @param sensorName - the name of the sensor to get the graph for
     * @param sensorType - the type of sensor the named sensor is
     * @return SensorGraph
     */
    private SensorGraph getSensorGraph(Integer userId, Integer domainSessionId, String sensorName, SensorTypeEnum sensorType){
        
        LearnerSensors tSensors = getLearnerSensors(userId, domainSessionId);
        SensorGraph sGraph = tSensors.getSensorGraph(sensorName);
        if(sGraph == null){            
            sGraph = tSensors.addSensor(sensorName, sensorType);
            
            // Add the graph to the panel if it sensor associated with the current domain session
            if (this.currentDomainSessionId != null && this.currentDomainSessionId == domainSessionId) {
                logger.debug("Adding sensor " + sGraph.getSensorName() + " to sensor graph panel");
                sgPanel.addSensor(sGraph);
                
                //Select the graph if it is the only one so far
                sgPanel.showAnyGraph();
            }
        }
        
        return sGraph;
    }
    
    /**
     * Gets the ID of the user that the sensor's domain session is for
     *
     * @return Integer The ID of the user of the sensor
     */
    public Integer getUserId() {
        LearnerSensors tSensors = domainSessionIdToSensors.get(currentDomainSessionId);
        if (tSensors != null) {
            return tSensors.getUserId();
        }
        return null;
    }
    
    /**
     * Handle sensor data by adding the data to the appropriate sensor's graph
     * 
     * @param userId - the user session id (i.e. the user for which) in which the sensor data is being produced
     * @param domainSessionId - the domain session id in which the sensor data is being produced
     * @param sensorName - the display name of the sensor producing the data
     * @param sensorType - the enumerated type of sensor producing the specified data
     * @param elapsedTime - the time (milliseconds) at which the data was produced
     * @param attributeValues - map of sensor attribute name to its current value
     */
    private void handleSensorData(Integer userId, Integer domainSessionId, String sensorName, SensorTypeEnum sensorType, long elapsedTime, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributeValues){
        
        SensorGraph sgraph = getSensorGraph(userId, domainSessionId, sensorName, sensorType);        
        sgraph.addSensorData(elapsedTime, attributeValues);
    }
    
    /**
     * Check if the provided domain session ID matches the current sensor monitored domain session id
     *  
     * @param domainSessionId
     * @return boolean
     */
    private boolean isDomainSession(int domainSessionId){
        
        if(this.currentDomainSessionId == null || this.currentDomainSessionId != domainSessionId){
            return false;
        }
        
        return true;
    }
    
    /**
     * This local class contains the graphs for each sensor in a single sensor module
     * 
     * @author mhoffman
     *
     */
    private class LearnerSensors{
        
        private int userId;
        
        private int domainSessionId;
        
        /** map of sensor name to the sensor's graph instance */
        private Map<String, SensorGraph> sensorNameToGraph = new HashMap<String, SensorGraph>();
        
        /**
         * Default constructor
         * @param userId - the user id associated with a set of sensors
         * @param domainSessionId - the domain session running the set of sensors
         */
        public LearnerSensors(int userId, int domainSessionId){
            this.userId = userId;
            this.domainSessionId = domainSessionId;
        }
        
        /**
         * Create a sensor graph for the named sensor
         * 
         * @param sensorName - the unique name of a sensor being graphed
         * @param sensorType - the type of sensor being graphed
         * @return SensorGraph - the graph instance for that sensor
         */
        public SensorGraph addSensor(String sensorName, SensorTypeEnum sensorType){
            
            SensorGraph sGraph = sensorNameToGraph.get(sensorName);
            if(sGraph == null){
                sGraph = new SensorGraph(sensorName, sensorType);
                sensorNameToGraph.put(sensorName, sGraph);
                
                if(isDebug){
                    logger.debug("Created sensor graph for sensor named = "+sensorName);
                }
            }
            
            return sGraph;
        }
        
        /**
         * Return the sensor graph (local class) instance for the named sensor in this sensor module instance
         *  
         * @param sensorName - the unique name of a sensor being graphed
         * @return SensorGraph - the graph for that sensor
         */
        public SensorGraph getSensorGraph(String sensorName){
            return sensorNameToGraph.get(sensorName);
        }
        
        /**
         * Return the Sensor Graph instance(s) for this learner
         * 
         * @return Collection<SensorGraph>
         */
        public Collection<SensorGraph> getSensorGraphs() {
            return sensorNameToGraph.values();
        }

        /**
         * Get the ID of the user in the sensor's domain session
         *
         * @return int The ID of the user
         */
        public int getUserId() {
            return userId;
        }
        
        /**
         * Return the current domain session id for this learner
         * 
         * @return int
         */
        public int getDomainSessionId(){
            return domainSessionId;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[LearnerSensors: ");
            sb.append("user = ").append(getUserId());
            sb.append(", domain session id = ").append(getDomainSessionId());
            
            sb.append(", sensor names = {");
            for(String name : sensorNameToGraph.keySet()){
                sb.append(name).append(", ");
            }
            sb.append("}");
            
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * This local class contains the graph content for a sensor
     * 
     * @author mhoffman
     *
     */
    private class SensorGraph{
        
        /** axis labels */
        private static final String X_AXIS = "elapsed time (hh:mm:ss)";
        private static final String Y_AXIS = "value";
        
        /** the line graph containing the data series */
        private JFreeChart chart;
        
        /** the data series for all lines in the graph */
        private TimeSeriesCollection dataset;
        
        private ChartPanel chartPanel;
        
        /** map of sensor attribute name to that attributes data series */
        private Map<SensorAttributeNameEnum, TimeSeriesCollection> attrNameToSeries = new HashMap<>();

        /**
         * Default time series day, month, year values
         */
        private static final int DAY = 1;
        private static final int MONTH = Month.JANUARY;
        private static final int YEAR = 1900;
        
        /**
         * Components displaying the legend for the graph
         */
        private JPanel legendPanel;
        private JScrollPane legendScrollPane;
        private JPanel legendItemPanel;
        private ArrayList <JCheckBox> legendItemList = new ArrayList<JCheckBox>();
        private JPanel legendButtonPanel;
        private JButton plotAllButton;
        private JButton plotNoneButton;
        
        /** a data series for all lines in the graph that will not be plotted*/
        private TimeSeriesCollection unplottedDataset = new TimeSeriesCollection();
        
        private SensorTypeEnum sensorType;
        
        /**
         * Class constructor
         * 
         * @param title - title of the graph
         * @param sensorType - the type of sensor for this graph
         */
        public SensorGraph(String title, SensorTypeEnum sensorType){
            init(title);
            this.sensorType = sensorType;
        }
        
        /**
         * Create a window with the graph with an empty data set.
         * 
         * @param title
         */
        private void init(String title){
            
            // create a dataset...
            dataset = new TimeSeriesCollection();
            
            // create a chart...
            chart = ChartFactory.createTimeSeriesChart(title, X_AXIS, Y_AXIS, dataset, true, true, false);
            chart.setPadding(new org.jfree.ui.RectangleInsets(0,0,62,0));
            chart.getLegend().setVisible(false);	// hide the default legend for the chart
            
            // limit amount of data shown          
            final XYPlot plot = chart.getXYPlot();
            ValueAxis yAxis = plot.getRangeAxis();
            yAxis.setAutoRangeMinimumSize(1.0);     // needed this for when first incoming data is constant, otherwise y-axis will only have on tick which is the whole side of the graph
            ValueAxis xAxis = plot.getDomainAxis();
//            xAxis.pan(0.50);                        //pan 50% to right to make first data points be in middle of graph
            xAxis.setAutoRange(true);               // automatically remove old data points and slide x-axis values along
            xAxis.setFixedAutoRange(120000.0);      // milliseconds
            
            // create a chart panel to display the chart on...
            chartPanel = new ChartPanel(chart);
            chartPanel.setLayout(new java.awt.BorderLayout());
            chartPanel.setBorder(new javax.swing.border.LineBorder(java.awt.Color.gray));
            
            // create a panel to display the list of legend items...
            legendItemPanel = new JPanel();
            legendItemPanel.setToolTipText("Legend corresponing to the data displayed on the above graph");
            
            // create a scroll pane to view the legend item panel...
            legendScrollPane = new JScrollPane();
            legendScrollPane.setPreferredSize(new java.awt.Dimension(580, 52));
            legendScrollPane.setEnabled(true);
            
            // create a button to select all items in the legend...
            plotAllButton = new JButton("Plot All");
            plotAllButton.setPreferredSize(new java.awt.Dimension(85, 18));
            plotAllButton.setToolTipText("Begins plotting all legend items on the above graph");
            plotAllButton.addActionListener(new java.awt.event.ActionListener(){
            	@Override
                public void actionPerformed(java.awt.event.ActionEvent e){
            		for(JCheckBox legendItem : legendItemList){
            			legendItem.setSelected(true);
            		}
            	}
            });
            plotAllButton.setEnabled(false);
                        
            // create a button to deselect all items in the legend...
            plotNoneButton = new JButton("Plot None");
            plotNoneButton.setPreferredSize(new java.awt.Dimension(85, 18));
            plotNoneButton.setToolTipText("Stops plotting all legend items on the above graph");
            plotNoneButton.addActionListener(new java.awt.event.ActionListener(){
            	@Override
                public void actionPerformed(java.awt.event.ActionEvent e){
            		for(JCheckBox legendItem : legendItemList){
            			legendItem.setSelected(false);
            		}
            	}
            });
            plotNoneButton.setEnabled(false);
            
            // create a panel to display the buttons on...
            legendButtonPanel = new JPanel();
            legendButtonPanel.setPreferredSize(new java.awt.Dimension(85, 52));
            legendButtonPanel.add(plotAllButton);
            legendButtonPanel.add(plotNoneButton);
            
            // create a panel to display the entire legend...
            legendPanel = new JPanel();
            legendPanel.setPreferredSize(new java.awt.Dimension(645, 62));
            legendPanel.add(legendScrollPane);
            legendPanel.add(legendButtonPanel);
            
            chartPanel.add(legendPanel, java.awt.BorderLayout.SOUTH);
        }
        
        /**
         * Return the sensor name for this graph
         * 
         * @return String - the name of the sensor
         */
        public String getSensorName(){
            return chart.getTitle().getText();
        }
        
        /**
         * Return the enumerated sensor type
         *  
         * @return SensorTypeEnum
         */
        public SensorTypeEnum getSensorType(){
            return sensorType;
        }
        
        /**
         * Return the chart panel for this sensor graph
         * 
         * @return ChartPanel
         */
        public ChartPanel getChartPanel(){
            return chartPanel;
        }
        
        /**
         * Add sensor data to the graph by adding data for each attribute to that attribute's data set.
         * 
         * @param elapsedTime - time (milliseconds) at which these data points where created (x-value)
         * @param attributeValues - map of sensor attribute name to attribute's current value (y-values)
         */
        public void addSensorData(long elapsedTime, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributeValues){
            
            for(SensorAttributeNameEnum attr : attributeValues.keySet()){
                
                if(blacklistedAttr.contains(attr)){
                    continue;
                }
                
                AbstractSensorAttributeValue attrValue = attributeValues.get(attr);
                
                //check if the series was already created
                TimeSeriesCollection seriesCollection = attrNameToSeries.get(attr);                
                if(seriesCollection == null){ 
                    
                    seriesCollection = new TimeSeriesCollection();
                    
                    //determine how many series should be created
                    List<ComplexAttributeField> attributeFields = attributeFieldMgr.getFieldsForAttributeClass(attrValue.getClass());                    
                    if(attributeFields == null || attributeFields.isEmpty() || attributeFields.size() == 1){
                        //simple attribute, only 1 series is needed
                        
                        TimeSeries series = new TimeSeries(attr.getName());
                        seriesCollection.addSeries(series);
                        dataset.addSeries(series);
                        addSeriesToLegend(series);
                        
                    }else{
                        //complex attribute
                        
                        for(ComplexAttributeField attributeField : attributeFields){
                            
                            String label = attributeField.getLabel();
                            
                            try{
                                Method getterMethod = attributeField.getMethod();
                                Object returnVal = getterMethod.invoke(attrValue);
                            
                                if(returnVal instanceof Number){
                                    //the return value of this attribute must be a number if it wants to be graphed
                                    
                                    TimeSeries series = new TimeSeries(attr.getName() + ":" + label);
                                    TimeSeries existingSeries = seriesCollection.getSeries(series.getKey());
                                    if(existingSeries == null){
                                        seriesCollection.addSeries(series);                            
                                        dataset.addSeries(series);
                                        addSeriesToLegend(series);
                                    }else{
                                        logger.error("Found colliding sensor attribute label of "+series.getKey()+".  Therefore removing this series from the graph to avoid corruption when graphing the stream with that label.");
                                        seriesCollection.addSeries(existingSeries);                            
                                        dataset.addSeries(existingSeries);
                                        addSeriesToLegend(existingSeries);
                                    }
                                    
                                }else{
                                    logger.warn("Ignoring sub-attribute named "+label+" of sensor attribute named "+attr+" of sensor "+getSensorName()+" because it's getter method is not returning a number and a number is needed to be graphed.");
                                    sgPanel.addSensorStatusMessage(new SensorStatus(getSensorName(), null,
                                            "Ignoring sub-attribute named " + label + " of sensor attribute of " + attr + " from sensor " + getSensorName() + " because unable to determine how to obtain a number to graph."),
                                            Long.toString((new Date()).getTime()));
                                }
                            }catch(Exception e){
                                logger.error("Caught exception while trying to check if the sensor sub-attribute of "+label+" from attribute "+attr+" of sensor "+getSensorName()+" will be providing a number value.", e);
                            }
                            
                        }
                    }
                    
                    if(seriesCollection.getSeriesCount() == 0){
                        
                        logger.warn("Adding sensor attribute of "+attr+" from sensor "+getSensorName()+" to blacklist of sensor attributes because there are no getters which return numbers.");
                        blacklistedAttr.add(attr);
                        sgPanel.addSensorStatusMessage(new SensorStatus(getSensorName(), null,
                                "Adding sensor attribute of " + attr + " from sensor " + getSensorName() + " to blacklist of sensor attributes because unable to determine how to obtain numbers to graph."),
                                Long.toString((new Date()).getTime()));

                        continue;
                        
                    }else{                    
                        attrNameToSeries.put(attr, seriesCollection);
                    }
                    
                }//end if
                
                //
                // Update the series with the new sensor data
                //
                
                if(attrValue.isNumber()){
                    //attribute is a single number, therefore there should only be 1 series in the collection
                    
                    addDataItem(elapsedTime, ((TimeSeries)seriesCollection.getSeries().get(0)), attrValue.getNumber());

                }else if(seriesCollection.getSeriesCount() > 0){
                    
                    List<ComplexAttributeField> attributeFields = attributeFieldMgr.getFieldsForAttributeClass(attrValue.getClass());    
                    for(ComplexAttributeField attributeField : attributeFields){
                        
                        String label = attributeField.getLabel();
                        
                        try{
                            Method getterMethod = attributeField.getMethod();
                            Object returnVal = getterMethod.invoke(attrValue);
                        
                            if(returnVal instanceof Number){
                                //the return value of this attribute must be a number if it wants to be graphed
                                
                                TimeSeries series = seriesCollection.getSeries(attr.getName() + ":" + label);
                                if(series != null){
                                    addDataItem(elapsedTime, series, (Number)returnVal);
                                }else{
                                    //ERROR
                                    logger.error("Unable to find the time series with label of '"+attr.getName() + ":" + label+"', therefore the value of "+returnVal+" will not be graphed.");
                                }
                            }
                            
                        }catch(@SuppressWarnings("unused") Exception e){
                            //do nothing because this could potentially flood the log
                        }
                    }
                }else{
                    logger.error("Unable to graph a non-number value for attribute named "+attr+" on sensor named "+getSensorName());
                }
                
            }//end for 
        }
        
        /**
         * Add a point to the graph.
         * 
         * @param elapsedTime - the time value of the x-axis
         * @param series - the graph's series to add the point too
         * @param value - the value of the y-axis
         */
        private void addDataItem(long elapsedTime, TimeSeries series, Number value){
        	
          int ms = (int) elapsedTime;
          int sec = 0;
          int min = 0;
          int hr = 0;
          //ms, sec, min, hr, day, month, yr
          series.add(new Millisecond(ms, sec, min, hr, DAY, MONTH, YEAR), value);
          //System.out.println("(time = "+elapsedTime+") => ms: "+ms+" sec: "+sec+" min: "+min+" hr: "+hr);
        }
        
        /**
         * Add a series to the graph's legend.
         * 
         * @param series - the graph's series to add to the legend
         */       
        private void addSeriesToLegend(TimeSeries series){     	
        		
    		//create a check box for each data series which will allow users to choose which data series to display
    		final JCheckBox legendCheckBox = new JCheckBox((String) series.getKey());
    		
    		// create a small panel indicating the color in the graph which corresponds to each check box        		
    		JPanel legendColorIndicator = new JPanel();
    		legendColorIndicator.setPreferredSize(new java.awt.Dimension(20, 2));
    		
    		org.jfree.chart.renderer.xy.AbstractXYItemRenderer renderer = (org.jfree.chart.renderer.xy.AbstractXYItemRenderer) chart.getXYPlot().getRenderer();
    		legendColorIndicator.setBackground((java.awt.Color) renderer.lookupSeriesPaint(dataset.getSeries().indexOf(series))); 
    		
    		// add the color panel and checkbox to the legend item panel and update the legend scroll pane
    		legendItemPanel.add(legendColorIndicator); 		 		
    		legendItemPanel.add(legendCheckBox);
    		legendItemList.add(legendCheckBox);
    		legendScrollPane.setViewportView(legendItemPanel);
    		
    		// add functionality to the check box based on its current state
    		legendCheckBox.addItemListener(new java.awt.event.ItemListener(){
    			@Override
                public void itemStateChanged(java.awt.event.ItemEvent e){
    				
    				//if a checkbox is not selected, add the corresponding time series to the unplotted data set and set it invisible in the graph
    				if(!legendCheckBox.isSelected()){
    					unplottedDataset.addSeries(dataset.getSeries(legendItemList.indexOf(legendCheckBox)));
    					chart.getXYPlot().getRenderer().setSeriesVisible(dataset.indexOf(dataset.getSeries(legendItemList.indexOf(legendCheckBox))), false);
    				}else{
    					//Otherwise, remove the corresponding time series from the unplotted data set and set it visible in the graph
    					unplottedDataset.removeSeries(dataset.getSeries(legendItemList.indexOf(legendCheckBox)));
    					chart.getXYPlot().getRenderer().setSeriesVisible(dataset.indexOf(dataset.getSeries(legendItemList.indexOf(legendCheckBox))), true);
    				}
    				
    				//disable the "Plot None" button when all legend items have been deselected
					if(unplottedDataset.getSeriesCount() == dataset.getSeriesCount() && plotNoneButton.isEnabled()){
						plotNoneButton.setEnabled(false);
					}else if(unplottedDataset.getSeriesCount() < dataset.getSeriesCount() && !plotNoneButton.isEnabled()){
						plotNoneButton.setEnabled(true);
    				}
					
					//disable the "Plot All" button when all legend items have been selected
					if(unplottedDataset.getSeriesCount() == 0 && plotAllButton.isEnabled()){
						plotAllButton.setEnabled(false);
					}else if (unplottedDataset.getSeriesCount() > 0 && !plotAllButton.isEnabled()){
						plotAllButton.setEnabled(true);
					}
    			}
    		});
    		
    		legendCheckBox.setEnabled(true);
    		legendCheckBox.setSelected(true);
    		
    	}
        
        /**
         * Return a string representation of this class
         * 
         * @return String
         */
        @Override
        public String toString(){
            return getSensorName();
        }
    }
}
