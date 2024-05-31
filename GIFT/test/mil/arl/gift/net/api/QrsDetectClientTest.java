/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import mil.arl.gift.common.util.StringUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.python.ProcessStreamProcessor;
import mil.arl.gift.common.python.ProcessStreamProcessor.Type;
import mil.arl.gift.common.python.XMLRPCPythonServerManager;
import mil.arl.gift.net.xmlrpc.XMLRPCClient;

/**
 * This class is a manual unit test for ECG heartbeat detection using the
 * XmlRpcService. (Allows testing in absence of live data feed from a GIFT ECG
 * Sensor) It's not intended for use in real GIFT application.
 * 
 * @author cragusa
 */
public class QrsDetectClientTest {
	
	final static String PYTHON_XML_RPC_SERVER_SCRIPT = "../../src.py/xml.rpc.server/XmlRpcServer.py -p";

	private XMLRPCClient client;
	
	private ProcessStreamProcessor errStreamProcessor;	
	
	private ProcessStreamProcessor outStreamProcessor;
	
	private Process serviceProcess;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * The service method name to use for the QRS detection.
	 */
	private static final String QRS_DETECT_SERVICE_METHOD_NAME = "QRSdetect";

	/**
	 * Convenience method that calls the remote XML RPC
	 * 
	 * @param val
	 *            the ecg value to send to the QRS detection service.
	 * @return An Object array containing the returned values.
	 * 
	 *         Ultimately the values that are returned depend on the underlying
	 *         python code. As of this writing here is the behavior:
	 * 
	 *         When a new QRS detection is made:
	 * 
	 *         Object[0] is the heartbeat number (i.e. a counter that indicates
	 *         how many heartbeats have been detected thus far). Object[1] is
	 *         the heart rate as computed by the QRS detection algorithm.
	 * 
	 *         When a new QRS detection is not made (which is what happens most
	 *         often):
	 * 
	 *         Object[0] is equal to "null". Object[1] is equal to "null".
	 * 
	 * @throws XmlRpcException
	 *             if there is a problem executing the XML RPC call.
	 */
	private Object[] qrsDetect(double val) throws XmlRpcException {
		Vector<Object> params = new Vector<Object>();
		params.addElement(val);

		StringBuilder errorMsg = new StringBuilder();

		return (Object[]) client.callMethod(QRS_DETECT_SERVICE_METHOD_NAME,
				params, errorMsg);
	}

	/**
	 * Test method that reads in ECG data from a file, iterates over each line
	 * in the file and calls QRSdetect with each signal value.
	 * 
	 * @throws IOException
	 *             if there is a problem processing the input file.
	 * @throws XmlRpcException
	 *             if there is a problem executing the XML RPC call.
	 */
	@Test
	public void testQrsDetectClient() throws IOException,
			XmlRpcException {
		
		final int port = 8000;
		final String SERVICE_CLASSNAME = "QRSdetection";
		
		String launchCommand = "cmd.exe \"python " + PYTHON_XML_RPC_SERVER_SCRIPT + " " + port + " -c " + SERVICE_CLASSNAME + "\"";			

		serviceProcess = Runtime.getRuntime().exec(launchCommand);
				
		errStreamProcessor = new ProcessStreamProcessor(PYTHON_XML_RPC_SERVER_SCRIPT, serviceProcess.getErrorStream(), Type.ERROR);            
		outStreamProcessor = new ProcessStreamProcessor(PYTHON_XML_RPC_SERVER_SCRIPT, serviceProcess.getInputStream(), Type.OUTPUT);
		
		errStreamProcessor.start();
		outStreamProcessor.start();

		//Make sure the process gets killed if the process is terminate 
		//(Note that shutdown hooks do not get called when process is terminated prematurely from from Eclipse)
		Runtime.getRuntime().addShutdownHook(new Thread("Python XML RPC Server Shutdown Thread") {
			@Override
			public void run() {
				
				if(serviceProcess != null) {			
					serviceProcess.destroy();
					serviceProcess = null;
				}
				
				if(errStreamProcessor != null) {
					errStreamProcessor.shutdown();
					errStreamProcessor = null;
				}
				
				if(outStreamProcessor != null) {
					outStreamProcessor.shutdown();
					outStreamProcessor = null;
				}
			}
		});
		
		client = new XMLRPCClient(
				XMLRPCPythonServerManager.DEFAULT_SERVER_BASE_URL, port);

		final String filename = "data/tests/ecg127a.txt";

		System.out.println("processing: " + filename);

		InputStream fis = new FileInputStream(filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		String line;
		int remoteCallCount = 0;

		final long startTime = System.currentTimeMillis();
		
		System.out.println("Please wait. Processing will take a while.");

		while ((line = reader.readLine()) != null) {
            /* Skip blank lines */
            if (StringUtils.isBlank(line)) {
                continue;
            }

			line = line.trim();
			final String WHITESPACE = "\\s+";
			String[] tokens = line.split(WHITESPACE);
			// System.out.println(tokens[0]);
			Double doubleValue = Double.parseDouble(tokens[1]);
			++remoteCallCount;
			
			if(remoteCallCount % 10 == 0){
				System.out.println(".");
			}
			
			Object[] result = qrsDetect(doubleValue.doubleValue());

			if (result != null && result.length > 0 && result[0] != null
					&& !result[0].equals("null")) {
				Double doubleResult = Double.parseDouble(result[0].toString());
				if (doubleResult.doubleValue() >= 0) {
					System.out.println(""); //newline before result
					XMLRPCPythonServerManager.printResult(result);
				}
			}
		}
		reader.close();

		double elapsedMillis = (double) System.currentTimeMillis() - startTime;
		double millisPerCall = elapsedMillis / remoteCallCount;
		System.out.println(String.format("%.2f", millisPerCall)
				+ " milliseconds per call");

		destroyServiceProcess();
	}

	/**
	 * Destroys the service process. 
	 * Optionally, call when done with XML RPS 
	 */
	private void destroyServiceProcess() {
		if(serviceProcess != null) {			
			serviceProcess.destroy();
			serviceProcess = null;
		}
		
		if(errStreamProcessor != null) {
			errStreamProcessor.shutdown();
			errStreamProcessor = null;
		}
		
		if(outStreamProcessor != null) {
			outStreamProcessor.shutdown();
			outStreamProcessor = null;
		}
	}
}
