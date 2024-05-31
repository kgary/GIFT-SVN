#!/usr/bin/env python
#  This is the initial script for the AffectDetection project.
#  This script allows the GIFT learner module to communicate (java) with this xmlrpc python server.
#  The function that GIFT will call is ProcessRawVerts each time new vertex data is received.  
#   GIFT will pass in an input table (an array of an array of float values) that matches the columns as defined below.
#     The input table that GIFT passes in will have a timestamp in the first column with at most 40 seconds of history.  This is configurable in the java class if more or less history is needed.
#   GIFT expects that an input table (an array of an array of float values) to be passed back (the number and columns must match what GIFT and RapidMiner are expecting).
import math
import sys
import datetime


class KinectSensorDataProcessor():

    def __init__(self):
        
        # do nothing here for now.
        pass
    
    def ProcessRawVerts(self,inputTable):
    
        # The input table is a table of raw Kinect vertex data (along with a timestamp (ms) in the first column).  
        # It looks like the format below and MUST match the ProcessedKinectSensorDataModel.java file in GIFT
        # At most it will contain the last 40 seconds of raw data from the kinect sensor.
        # The values should come in as 'float' values.
        # | timestamp (ms) |  Head_X | Head_Y | Head_Z | Top_Skull_X | Top_Skull_Y | Top_Skull_Z | L_Hand_X | L_Hand_Y | L_Hand_Z | R_Hand_X | R_Hand_Y | R_Hand_Z | L_Shoulder_X | L_Shoulder_Y | L_Shoulder_Z | R_Shoulder_X | R_Shoulder_Y | R_Shoulder_Z | C_Shoulder_X | C_Shoulder_Y | C_Shoudler_Z |
        
        # this is a sample of getting the number of rows and columns in the first row of data from the inputTable.
        rowLen = len(inputTable)
        colLen = len(inputTable[0])
        
        # $TODO$ - Process the inputTable data here!
        
        
        

        # The output table will be used to to build the exampleSet for RapidMiner process.  It must match the expectations of the ProcessedKinectSensorDataModel.java file in GIFT which
        # also MUST match the expectations of the RapidMiner model. 
        # Currently it is expected to contain the following output (40 columns):
        # | HEAD_pos_change_last_emo | HEAD_depth_change_3sec | HEAD_pos_change_3sec | HEAD_pos_change_6sec | HEAD_depth_change_6sec | HEAD_depth_change_10sec | HEAD_pos_change_10sec | HEAD_depth_change_last_emo |
        #      | TOP_SKULL_depth_change_last_emo | TOP_SKULL_depth_change_3sec | TOP_SKULL_pos_change_3sec | TOP_SKULL_depth_change_6sec | TOP_SKULL_pos_change_6sec | TOP_SKULL_depth_change_10sec | TOP_SKULL_pos_change_10sec | TOP_SKULL_pos_change_last_emo | 
        #      | LEFT_SHOULDER_depth_change_3sec | LEFT_SHOULDER_pos_change_3sec | LEFT_SHOULDER_depth_change_6sec | LEFT_SHOULDER_pos_change_6sec | LEFT_SHOULDER_depth_change_10sec | LEFT_SHOULDER_pos_change_10sec | LEFT_SHOULDER_depth_change_last_emo | LEFT_SHOULDER_pos_change_last_emo |
        #      | RIGHT_SHOULDER_depth_change_3sec | RIGHT_SHOULDER_pos_change_3sec | RIGHT_SHOULDER_depth_change_6sec | RIGHT_SHOULDER_pos_change_6sec | RIGHT_SHOULDER_depth_change_10sec | RIGHT_SHOULDER_pos_change_10sec | RIGHT_SHOULDER_depth_change_last_emo | RIGHT_SHOULDER_pos_change_last_emo |
        #      | CENTER_SHOULDER_depth_change_3sec | CENTER_SHOULDER_pos_change_3sec | CENTER_SHOULDER_depth_change_6sec | CENTER_SHOULDER_pos_change_6sec | CENTER_SHOULDER_depth_change_10sec | CENTER_SHOULDER_pos_change_10sec | CENTER_SHOULDER_depth_change_last_emo | CENTER_SHOULDER_pos_change_last_emo |
        #  The values are expected to be 'DOUBLE'/floating point values for each column.  No column labels are required at this time.
        outputTable = []
        sampleRow = []
        
        # $TODO$ This is an example only of hardcoded data for each column.
        # Fill in the output table as defined above to match the expected cols of data that RapidMiner process needs.
        for x in range (0, 40):
          sampleRow.append(1.0)

        outputTable.append(sampleRow)

        # The output table should be a table (array of arrays).  On the java side, this outputTable becomes an array of an array of Objects.
        return outputTable
        
    
