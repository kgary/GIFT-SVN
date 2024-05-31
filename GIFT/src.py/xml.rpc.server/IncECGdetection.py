import threading
from multiprocessing import Queue
import time
import csv
import scipy
from scipy import signal
from scipy.signal import bilinear

class QRSdetection():

    def __init__(self, samplingRate = 250, windowSize = 38):

        self.samplingRate = samplingRate
        self.windowSize = windowSize
        self.spki = self.peaki = self.peakf = self.npki = 0
        self.npkf = self.spkf = 1 #initial guess
        self.T1=self.T2=self.F1=self.F2=0
        self.lastFoundBeatAt, self.dataPntIdx = 0, 0
        self.heartBeats = int(0)
        self.signalDataOrig = []
        self.lastHBidx = 0
        self.tBPF = 0

        centerFreq = 5
        Q = .4
        b = scipy.array([0.0,centerFreq/float(Q),0.0])
        a = scipy.array([centerFreq*centerFreq,centerFreq/float(Q),1.0])
        self.b,self.a = bilinear(b,a)
        
        
    def QRSdetect(self,value):
        """ the data, sampling rate, size of the integration window"""

        self.signalDataOrig.append(value)
        self.lastHBidx += 1
        if len(self.signalDataOrig)>self.windowSize:
            self.signalDataOrig = self.signalDataOrig[1:]

            dsmsignal = bpfsignal = self.bandpassfilter(self.signalDataOrig, self.b, self.a)
            dsmsignal = self.QRS_Derivative(dsmsignal)
            dsmsignal = self.QRS_Squaring(dsmsignal)
            dsmsignal = self.QRS_MWIntegration(dsmsignal)
            return self.QRS_Threshold(dsmsignal, bpfsignal)
        else:
            return "null", "null"
         
		

    def bandpassfilter(self,scipyarray,b,a):

        return signal.lfilter(b,a,scipyarray)


    def QRS_Derivative(self,scipyarray):
        """
        output is 1/8*sampling of 4 terms:
            -1x(n-2)        1
            -2x(n-1)        2
            +2x(n+1)        3
            +x(n+2)         4
        """
        returnList = []
        for i in range(0,len(scipyarray)):
            term1=term2=term3=term4=term5=0
            if i > 1: term1 = -1.0*scipyarray[i-2]
            if i > 0: term2 = -2.0*scipyarray[i-1]
            if i+1 < len(scipyarray): term3 = 2.0*scipyarray[i+1]
            if i+2 < len(scipyarray): term4 = scipyarray[i+2]
            returnList.append(1.0/8.0*self.samplingRate*(term1+term2+term3+term4))
        return scipy.array(returnList)

    def QRS_Squaring(self,scipyarray):
        """
        output is the square of the input
            y = x**2
        """
        returnList = []
        for i in range(0,len(scipyarray)):
            returnList.append(scipyarray[i]*scipyarray[i])
        return scipy.array(returnList)

    def QRS_MWIntegration(self,scipyarray):
        """
        output is 1/N * sum(0,N-1,x(n-N)
            y = x**2
        """
        N = int(self.windowSize)
        returnList = []
        for i in range(0,len(scipyarray)):
            value = 0
            for n in range(i-N,i,1):
                if n >= 0:
                    value = value + scipyarray[n]
            returnList.append(value)
        return scipy.array(returnList)


    def QRS_Threshold(self,signalIntegrated, signalFiltered):
        """
        output is the first Threshold
            peak1 is the overall peak
            spk1 is the running estimate of the signal peak
            npk1 is the running estimate of the noise peak
            T1 is the first thresh
            T2 is the second thresh

            The original algorithm has been modified for additional sensitivity:
                the peak is never more than 85% of the max values
                thresholds are halved at each step for detection
                This was done, as adivised in the 11/2/11 class period
        """
        returnList = []
        foundset1 = foundset2 = False
        for i in range(0,len(signalIntegrated)):
            #first signals first
            if signalIntegrated[i] > self.peaki:
                self.peaki = .85*signalIntegrated[i] #if there is a new max?

            if signalIntegrated[i] > self.T1: self.spki = .125*self.peaki + .875*self.spki
            elif signalIntegrated[i] > self.T2: self.npki = .125*self.peaki + .875*self.npki
            self.T1 = self.npki + .25*(self.spki-self.npki)
            self.T2 = .5 * self.T1

            #second signals second (use delayed signal)
            if i > 2:
                if signalFiltered[i-2] > self.peakf:
                    self.peakf = .85*signalFiltered[i-2] #if there is a new max?
                if signalFiltered[i-2] > self.F1: self.spkf = .125*self.peakf + .875*self.spkf
                elif signalFiltered[i-2] > self.F2: self.npkf = .125*self.peakf + .875*self.npkf
                self.F1 = self.npkf + .25*(self.spkf-self.npkf)
                self.F2 = .5*self.F1

            self.T1 = self.T1*.5
            self.F1 = self.F1*.5
            if signalIntegrated[i] > self.T1:
                foundset1 = True
            elif signalIntegrated[i] > self.T2:
                foundset1 = True
                self.spki = .25*self.peaki + .75*self.spki
            else: foundset1 = False

            if signalFiltered[i-2] > self.F1:
                foundset2 = True
            elif signalFiltered[i-2] > self.F2:
                foundset2 = True
                self.spkf = .25*self.peakf + .75*self.spkf
            else: foundset2 = False

            if foundset1 is True and foundset2 is True:
                #FOUND HEARTBEAT!
                returnList.append(int(1))
            else:
                returnList.append(int(0))     #NOT SO MUCH!

            if len(returnList) > 2:
                if returnList[-1] == 0 and returnList[-2] == 1:
                    #how many datapoints has it been?

                    bpm = 60/(self.lastHBidx/float(self.samplingRate))
                    if bpm > 40 and bpm < 180:
                        #if this appears to be a reasonable beat, add it to the list of valid heartbeats
                        self.lastHBidx = 0
                        self.heartBeats += 1
                        #print "Beats: %d, BPM: %d" %(self.heartBeats, round(bpm))
                        return self.heartBeats, round(bpm)
        return "null", "null"
         
			


