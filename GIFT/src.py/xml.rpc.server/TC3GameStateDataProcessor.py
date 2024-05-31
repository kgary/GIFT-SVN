#!/usr/bin/env python
#  This is the initial script for the AffectDetection project.
#  This script allows the GIFT learner module to communicate (java) with this xmlrpc python server.
#  The function that GIFT will call is ProcessTC3Data each time new TC3 data is received.  
#   GIFT will pass in an input table (an array of an array of float values) that matches the columns as defined below.
#     The input table that GIFT passes in will have a timestamp in the first column with at most 40 seconds of history.  This is configurable in the java class if more or less history is needed.
#   GIFT expects that an input table (an array of an array of float values) to be passed back (the number and columns must match what GIFT and RapidMiner are expecting).
import math
import sys
import datetime


class TC3GameStateDataProcessor():

    
    
    def __init__(self):
        
        # create a dictionary based on domainSessionId to table data.  
        self.sessionMap = dict()
        
    
    def ProcessTC3Data(self, domainSessionId, inputTable):
    
        # this value must match the PYTHON_NO_DATA_VALUE in the ProcessedTC3DataModel.java file in GIFT.
        # this value is used to indicate that the column that is received from GIFT has 'no data' or was not updated for the given timestamp.
        # This value can be configured to any arbitrary float/double value, but must match the java file in GIFT.  A value here should be picked
        # that will never be used by real Tc3 game tate data.
        PYTHON_NO_DATA_VALUE = -9999.99

        # This can be configured, but this value will specify how many seconds of Tc3 game state data should be kept (per user) -- currently 40 seconds.
        MAX_MILLISECS_OF_DATA_TO_KEEP = 40000

        # The expected indices that are coming in from the tc3datamodel.  
        # these index values come from the TC3DataModel.java file and must match what GIFT is expecting.
        # NOTE:  GIFT passes in a timestamp which is index 0.  The index values for the other columns are increased by 1 due to the timestamp being prepended to the row of data.
        IDX_TIMESTAMP = 0
        IDX_BLOODVOLUME = 1
        IDX_HEARTRATE = 2
        IDX_ISSAFE = 3
        IDX_BLEEDRATE = 4
        IDX_LEFTLUNGEFF = 5
        IDX_SYSTOLIC = 6
        IDX_OUTOFCOVERTIME = 7
        IDX_ISUNDERCOVER = 8
        IDX_WITHUNIT = 9
        IDX_UNDERFIRE = 10
        IDX_UNDERTACTFIELDCARE = 11
        IDX_WOUNDEXPOSED = 12
        IDX_HASREQUESTEDHELP = 13
        IDX_ROLLED = 14
        IDX_REQSECSWEEP = 15
        IDX_MOVETOCPP = 16
        IDX_TOURNIQUETSAPPLIED = 17
        IDX_BLOODSWEEP = 18
        IDX_REQUESTCASEVAC = 19
        IDX_BREATHINGCHECKED = 20
        IDX_HASCOMMUNICATED = 21
        IDX_VITALSCHECK = 22
        IDX_HASFIREDWEAPON = 23
        IDX_BANDAGEAPPLIED = 24
        
        # These currently are the computed values that should be computed and processed back to GIFT.
        IDX_MSSINCELASTMOD = 25
        IDX_AVERAGEBLOODVOLUME = 26
        IDX_MINHEARTRATE = 27
        IDX_SUMISSAFE = 28
        
        
        # Map the inputTable data to the current domainSessionId.  If the table already exists, append
        # the data to the current list of data for that particular domain session id.
        if domainSessionId in self.sessionMap:
            # append the inputTable to the current data we have.
            self.sessionMap[domainSessionId] = self.sessionMap[domainSessionId] + inputTable
        else:
            self.sessionMap[domainSessionId] = inputTable
    
        # Prune the input table to keep only the max amount of seconds of data.
        currentTable = self.sessionMap[domainSessionId]
        
        while len(currentTable) > 1 :
            oldestRow = currentTable[0]
            newestRow = currentTable[len(currentTable)-1]
            
            # timestamp is expected to be in the first column (column 0).
            timeStampOldest = oldestRow[IDX_TIMESTAMP]
            timeStampNewest = newestRow[IDX_TIMESTAMP]
            
            if timeStampNewest - timeStampOldest > MAX_MILLISECS_OF_DATA_TO_KEEP :
                # delete the first row in the table, and continue checking.
                del currentTable[0]
            else:
                # the first row in the table to the last doesn't have over 40 seconds of data, so no need to check further.
                break
    
        # $TODO$ - Process the inputTable data here!
        # the following is a sample of getting the average of blood volume with the data sample that is passed in.
        # We will take the newest row of data (with current values), and then compute the blood volume average on the entire data set.
        computedRow = []
        outputRow = []
        if len(currentTable) > 0 :
            # default to pass back the current row of data which is just the last entry.
            computedRow = currentTable[-1]
            
            # Build up a list of valid bloodvolume values that we have for this 40 seconds of data.
            
            validBloodVolumeValues = []
            for x in range (0, len(currentTable)) :
                currentRow = currentTable[x]
                # only include this sample if it is a valid/expected value.
                if currentRow[IDX_BLOODVOLUME] != PYTHON_NO_DATA_VALUE :
                    validBloodVolumeValues.append(currentRow[IDX_BLOODVOLUME])
            
            if len(validBloodVolumeValues) > 0 :
                # compute the average of blood volume for this sample.
                averageBloodVolume = sum(validBloodVolumeValues)/len(validBloodVolumeValues)
                
                # For now we'll compute our values into the 'last' row of our inputTable and we'll send that single row back to GIFT as our output table.
                computedRow = currentTable[-1]
                computedRow[IDX_AVERAGEBLOODVOLUME] = averageBloodVolume

        # save back the processed table to our domainsession state that maintains this list.
        self.sessionMap[domainSessionId] = currentTable
        
        
        # make sure we have data to process, if not, we pass back an empty row.
        if len(computedRow) > 0 :
            # make a copy of the output row and then strip out the timestamp column so that we only return the expected exampleset to GIFT and RapidMiner.
            outputRow = list(computedRow)
            # delete the timestamp index from our output (since it is not passed back to Rapidminer)
            # Once we make this call, it is no longer safe to index by our 'constant' values into the outputRow list!
            del outputRow[IDX_TIMESTAMP]
        
        # The output table will be used to to build the exampleSet for RapidMiner process.  It must match the expectations of the ProcessedTc3DataModel.java file in GIFT which
        # also MUST match the expectations of the RapidMiner model. 
        outputTable = []
        
        # Add a single row into our table that will be passed back to RapidMiner model.
        outputTable.append(outputRow)

        # The output table should be a table (array of arrays).  On the java side, this outputTable becomes an array of an array of Objects.
        return outputTable
        
    
