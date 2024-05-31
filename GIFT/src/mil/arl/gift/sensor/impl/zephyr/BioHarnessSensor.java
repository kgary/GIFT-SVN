/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl.zephyr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.swing.JOptionPane;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.IntegerValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.tools.ModuleUserInterfaceEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This sensor class interfaces with the Zephyr BioHarness 3.0 sensor hardware.
 * 
 * @author ohasan, dramsey, cragusa
 */
public class BioHarnessSensor extends AbstractSensor {

	/**
	 * Reference to the Logger object.
	 */
	private static Logger logger = LoggerFactory.getLogger(BioHarnessSensor.class);
	
	private static final boolean IS_LOGGER_DEBUG_ENABLED = logger.isDebugEnabled();

	/** information about the data this sensor can produce */    
	protected static EventProducerInformation eventProducerInformation;

	static{
		eventProducerInformation = new EventProducerInformation();
		eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HEART_RATE, DoubleValue.class));
		eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RESPIRATION_RATE, DoubleValue.class));
		//eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.SKIN_TEMPERATURE, DoubleValue.class));
		eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.POSTURE, DoubleValue.class));
		eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.BREATHING_WAVEFORM_SAMPLE, DoubleValue.class));
		eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE, DoubleValue.class));
	}	

	/**
	 * Used to find the BioHarness 3.0 Bluetooth remote device by name
	 * comparison. The BioHarness friendly Bluetooth name will start with this
	 * string of characters.
	 */
	private static final String SEARCH_NAME = "BH BHT";

	/** the thread that determines when a new value is calculated */
	private SensorThread sThread = null;

	/** used to prevent interruption of sensor thread while it is reading data */
	private Object sensorMutex = new Object();

	/**
	 * Contains all the Bluetooth devices discovered
	 */
	private Vector<RemoteDevice> devicesDiscovered = new Vector<RemoteDevice>();

	/**
	 * contains all the services found for the BioHarness sensor device (if
	 * found)
	 */
	private Vector<ServiceRecord> services = new Vector<ServiceRecord>();

	// http://www.bluecove.org/bluecove/apidocs/javax/bluetooth/UUID.html
	// Serial Port 0x1101
	private static final UUID[] UUIDS = new UUID[] { new UUID(0x1101) };

	/**
	 * The Bluetooth connections.
	 */

	/** Input stream of the BioHarness sensor Bluetooth connection */
	private DataInputStream dis;

	/** Output stream of the BioHarness sensor Bluetooth connection */
	private DataOutputStream dos;

	/** Byte array for receiving data from the serial port */
	private static final int BUFFER_SIZE = 256;
	private byte[] packet = new byte[BUFFER_SIZE];

	/**
	 * Bioharness packet constants
	 */

	/** Ack Responses */
	private static final byte ACK = 0x06;
	private static final byte NAK = 0x15;

	/** Start of text */
	private static final byte STX = 0x02;

	/** End of text */
	private static final byte ETX = 0x03;

	/** Set Packet Transmit Message IDs */
	private static final byte SET_GENERAL_DATA_PACKET = 0x14;
	private static final byte SET_BREATHING_WAVEFORM_PACKET = 0x15;
	private static final byte SET_ECG_WAVEFORM_PACKET = 0x16;
	private static final byte SET_BT_LINK_CONFIG = (byte) 0xA4;    
	//The following set packets are supported by the device but not the current GIFT code.
	//  private static final byte SET_R_TO_R_PACKET = 0x19;
	//  private static final byte SET_ACCELEROMETER_PACKET = 0x1E;

	/** Periodic Packet Message IDs */
	private static final byte GENERAL_DATA_PACKET_TYPE = 0x20;
	private static final byte BREATHING_WAVEFORM_PACKET_TYPE = 0x21;
	private static final byte ECG_WAVEFORM_PACKET_TYPE = 0x22;    
	//The following packets are supported by the device, but not supported by the current GIFT code.
	//  private static final byte LIFESIGN_PACKET_TYPE = 0x23;
	//  private static final byte R_TO_R_PACKET_TYPE = 0x24;
	//  private static final byte ACCELEROMETER_PACKET_TYPE = 0x25;

	private static final int ECG_PACKET_DLC = 88;
	private static final int BREATHING_PACKET_DLC = 32;

	/**
	 * Configure using the default configuration.
	 * 
	 * @param sensorName an authored name for this sensor.  Can't be null or empty.
	 */
	public BioHarnessSensor(String sensorName) {
		super(sensorName, SensorTypeEnum.BIOHARNESS);
		setEventProducerInformation(eventProducerInformation);
	}

	/**
	 * Configure using the sensor configuration input for
	 * this sensor
	 * 
	 * @param sensorName an authored name for this sensor.  Can't be null or empty.
	 * @param configuration parameters to configure this sensor
	 */
	public BioHarnessSensor(String sensorName, generated.sensor.BioHarnessSensor configuration) {
		this(sensorName);
	}

	@Override
	public boolean test() {

		if (!findBioHarnessSensor()) {
			createSensorError("Unable to find BioHarness sensor");
			throw new ConfigurationException("Unable to find BioHarness sensor",
			        "The BioHarness sensor could not be found.  There should be other errors related to the search for the sensor.",
			        null);
		}

		return true;
	}

	/**
	 * Search for the BioHarness sensor connection
	 * 
	 * @return boolean return true iff the connection was successfully opened to the BioHarness sensor
	 */
	private boolean findBioHarnessSensor() {

		boolean found = false;

		final Object inquiryCompletedEvent = new Object();

		// check if the data stream is already initialized
		if (dis != null) {
			try {
				if (dis.available() > 0) {
					return true;
				}
			} catch (IOException e) {
				logger.error("Exception while trying to open the BioHarness sensor connection after already configured", e);
                displayMessageToUser("Caught exception while trying to open the BioHarness sensor connection after already configured. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
			}
		}

        //
        // Display dialog to get user's attention
        //
        JOptionPane.showMessageDialog(null, 
                "The BioHarness Sensor needs your attention.\n" +
                "\nIf your running GIFT in power user mode, please open the command prompt window, otherwise\n" +
                "select OK on this dialog and interact with the sensor module user interface (when presented).", 
                "BioHarness Sensor", 
                JOptionPane.INFORMATION_MESSAGE);

		// Start new search
		devicesDiscovered.clear();
		services.clear();

		// setup the device listener used for finding devices and their services
		DiscoveryListener listener = new DiscoveryListener() {

			@Override
			public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				displayMessageToUser("Device " + btDevice.getBluetoothAddress() + " found", null);
				devicesDiscovered.addElement(btDevice);
				try {
					displayMessageToUser("     name " + btDevice.getFriendlyName(false), null);
				} catch (IOException exception) {
                    logger.error("Caught exception while trying to retrieve the friendly name of a device that was discovered.", exception);
                    displayMessageToUser("Caught exception while trying to retrieve the friendly name of a device that was discovered.. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
                }
			}

			@Override
			public void inquiryCompleted(int discType) {
				displayMessageToUser("Device Inquiry completed!", null);

				// Release the wait on waiting for devices
				synchronized (inquiryCompletedEvent) {
					inquiryCompletedEvent.notifyAll();
				}
			}

			@Override
			public void serviceSearchCompleted(int transID, int respCode) {
				displayMessageToUser("Device Service Search completed!", null);

				switch (respCode) {
				case DiscoveryListener.SERVICE_SEARCH_COMPLETED:

					if (services.isEmpty()) {
						displayMessageToUser("The service was not found", null);
					}

					break;
				case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
					displayMessageToUser("Device not Reachable", null);
					break;
				case DiscoveryListener.SERVICE_SEARCH_ERROR:
					displayMessageToUser("Service serch error", null);
					break;
				case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
					displayMessageToUser("No records returned", null);
					break;
				case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
					displayMessageToUser("Inqury Cancled", null);
					break;
				default:
					displayMessageToUser("Received unhandled response code of " + respCode, null);
				}

				// Release the wait on searching for services
				synchronized (inquiryCompletedEvent) {
					inquiryCompletedEvent.notifyAll();
				}
			}

			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {

				for (int i = 0; i < servRecord.length; i++) {
					// displayMessageToUser("Found service: "+servRecord[i]);
					services.addElement(servRecord[i]);
				}

			}
		};

		synchronized (inquiryCompletedEvent) {
			boolean started = false;
			try {
				LocalDevice localDevice = LocalDevice.getLocalDevice();
				started = localDevice.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
			} catch (BluetoothStateException e) {
				logger.error("Caught exception caught while starting to search for devices", e);
				displayMessageToUser("Caught exception while starting to search for devices. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
			}
			if (started) {

				displayMessageToUser("\n *** Please ensure that the BioHarness sensor is turned on. *** ", null);
				displayMessageToUser("Searching for BioHarness sensor, please wait ...\n", null);
				try {
					inquiryCompletedEvent.wait();
				} catch (InterruptedException e) {
					logger.error("Caught exception while searching for BioHarness sensor", e);
	                displayMessageToUser("Caught exception while searching for BioHarness sensor. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
				}
				displayMessageToUser("\n" + devicesDiscovered.size() + " device(s) found", null);
			}
		}

		String name;
		for (RemoteDevice device : devicesDiscovered) {

			try {
				name = device.getFriendlyName(false);
				displayMessageToUser("Looking at " + name, null);

				if (name != null && name.contains(SEARCH_NAME)) {

				    displayMessageToUser("Found BioHarness sensor, please wait while searching for services...", null);

					synchronized (inquiryCompletedEvent) {
						LocalDevice localDevice = LocalDevice.getLocalDevice();
						localDevice.getDiscoveryAgent().searchServices(null, UUIDS, device, listener);
						try {
							inquiryCompletedEvent.wait();
						} catch (InterruptedException e) {
							logger.error("Exception caught while waiting for BioHarness sensor services to be found", e);
		                    displayMessageToUser("Caught exception while waiting for BioHarness sensor services to be found. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
						}
					}

					displayMessageToUser("Found " + services.size() + " services", null);
					if (!services.isEmpty()) {
						StreamConnection conn = null;
						ServiceRecord service = services.get(0);

						displayMessageToUser("Establishing connection to service", null);

						String url = service.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);

						try {

							// establish the connection
							conn = (StreamConnection) Connector.open(url);
							dis = conn.openDataInputStream();

							found = dis != null;

							dos = conn.openDataOutputStream();

							if ((dis != null) && (dos != null)) {

								displayMessageToUser("BioHarness sensor connection opened\n", null);
							} else {

								createSensorError("While searching for an active BioHarness sensor, there was an error when reading from device named ");
							}

							// conn.close();
						} catch (Exception e) {
							logger.error("Exception caught while trying to open the BioHarness sensor connection", e);
		                    displayMessageToUser("Caught exception while trying to open the BioHarness sensor connection. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
						}
					}

					if (found) {
						// found active BioHarness sensor, don't need to look at other device
						// disable the Bluetooth timeout so that the sensor does not timeout before the course starts
						disableBluetoothTimeout();

						break;
					}

				} else {
					displayMessageToUser(name + " is not a BioHarness sensor.", null);
				}

			} catch (IOException e) {
				logger.error("Caught exception caught while looking at the found device(s)", e);
                displayMessageToUser("Caught exception while looking at the found device(s). Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
			}

		}

		// Allow the user to search again before the sensor module fails to
		// initialize
		if (!found) {

			createSensorError("Unable to find BioHarness sensor.  Are you properly synching the device when the sensor module requests?");

			displayMessageToUser("\nTry again? (y): ", new ModuleUserInterfaceEventListener() {
                
                @Override
                public void textEntered(String text) {
                    setUserInput(text);                    
                }

                @Override
                public void errorOccurred(String message) {
                    displayMessageToUser("There was a problem reading your response: "+message, null); 
                    
                    //make sure the thread-wait continues...
                    setUserInput(null);
                }
            });
            
            //wait for the user's response (from either command prompt window or module user interface frame)
            try {
                
                synchronized(userInputMonitor){
                    logger.info("Waiting for user input...");
                    userInputMonitor.wait();
                }
                logger.info("User input is now '"+userInput+"'.");
                
            } catch (InterruptedException e) {
                logger.error("Caught exception while waiting for your response.", e);
                displayMessageToUser("Exception caught while waiting for your response. Refer to the latest sensor module log file in GIFT\\output\\logger\\module\\ for more information.", null);
            }

			if ("y".equals(userInput)) {
				found = findBioHarnessSensor();
			}
		}

		return found;
	}

	@Override
	public void start(long domainSessionStartTime) throws Exception {

		if (sThread == null || sensorState == SensorStateEnum.STOPPED) {
			super.start(domainSessionStartTime);

			logger.info("Starting Sensor Thread - finding BioHarnessSensor");

			if (findBioHarnessSensor()) {

				// request that the BioHarness sensor start sending the General
				// Data Packet
				requestGeneralDataPacket(true);

				// request that the BioHarness sensor start sending the
				// Breathing Waveform Packet
				requestBreathingWaveformPacket(true);

				requestEcgWaveformPacket(true);

				sThread = new SensorThread(getSensorName(), getSensorInterval());
				sThread.start();
				logger.info("Sensor Thread started for " + this);
			} else {
				createSensorError("Unable to find BioHarness sensor");
				logger.error("Unable to open the BioHarness sensor serial port, therefore the sensor thread will not be started");
			}
		}
	}

	@Override
	public void stop() {

		if (sThread != null) {
			super.stop();
			sensorState = SensorStateEnum.STOPPED;

			//tell Bioharness to stop sending packets
			requestGeneralDataPacket(false);
			requestBreathingWaveformPacket(false);
			requestEcgWaveformPacket(false);

			synchronized (sensorMutex) {
				// stop the thread
				sThread.interrupt();
			}

			logger.info("Sensor Thread stopped for " + this);
		}
	}

	/**
	 * Send the sensor data event
	 * 
	 * @param data the sensor data to send
	 */
	protected void sendDataEvent(SensorData data) {

		super.sendDataEvent(data);
	}

	/**
	 * This thread is responsible for ticking the sensor which retrieves the
	 * sensor's next value. The sensor value is then sent via sensor data event.
	 */
	private class SensorThread extends Thread {

		private int intervalMillis;

		public SensorThread(String threadName, double intervalSecs) {
			super(threadName);

			intervalMillis = (int)(intervalSecs * SECONDS_TO_MILLISECONDS);
		}

		@Override
		public void run() {

			sensorState = SensorStateEnum.RUNNING;

			while (sensorState == SensorStateEnum.RUNNING) {

				synchronized (sensorMutex) {

					try {

						dis.read(packet);

						long elapsedTime = System.currentTimeMillis() - getDomainSessionStartTime();

						if (packet != null && packet.length >= 5) {

							if (packet[0] == STX) {

								// Index 0 : STX, Index 1 : Msg Type, Index 2 : Size
								int size = ZephyrUtils.byteAsInteger(packet[2]);

								if (packet.length > size) {

									if (!ZephyrUtils.checkCRC(packet)) {
										logger.error("CRC error on Bioharness"); 
									} else {

										// Index 0 : STX, Index 1 : Msg Type
										byte msgID = packet[1];

										if(IS_LOGGER_DEBUG_ENABLED) {
											logger.debug("Received BioHarness data: type: " + msgID);
										}	

										//4 Hz
										if (msgID == ECG_WAVEFORM_PACKET_TYPE) {

											if (size == ECG_PACKET_DLC) {

												final int BYTE_OFFSET = 12;
												final int VALUE_COUNT = 63;
												final int ECG_INTERVAL = 4;
												final double SCALE_FACTOR = 0.1;

												elapsedTime -= (VALUE_COUNT - 1) * ECG_INTERVAL; // 63 packets at 4 ms interval

												int[] rawVals = unpackByteArray(packet, BYTE_OFFSET, VALUE_COUNT);

												for(int i = 0; i < rawVals.length; i++) {

													Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue =
															new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();                                                

													DoubleValue doubleValue = new DoubleValue(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE, rawVals[i] * SCALE_FACTOR);

													sensorAttributeToValue.put(SensorAttributeNameEnum.ECG_WAVEFORM_SAMPLE, doubleValue);

													SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);

													sendDataEvent(data);                                                

													elapsedTime += ECG_INTERVAL;
												}

											} else {

												logger.warn("expected ECG waveform packet with DLC (size) of " + ECG_PACKET_DLC + " but packet DLC was " + size + " ... dropping packet"); 
											}
											
										} else if  (msgID == GENERAL_DATA_PACKET_TYPE) {

											Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue =
													mapGeneralDataPacketData(packet);

											if (sensorAttributeToValue != null) {
												SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
												sendDataEvent(data);
											}

											// Every 1.008 seconds, different timing than General Data due to sampling rate
										} else if (msgID == BREATHING_WAVEFORM_PACKET_TYPE ) {                                            

											if (size == BREATHING_PACKET_DLC) {

												final int BYTE_OFFSET = 12;
												final int VALUE_COUNT = 18;
												final int WAVEFORM_INTERVAL = 56;
												final double SCALE_FACTOR = 0.1;

												elapsedTime -= (VALUE_COUNT - 1) * WAVEFORM_INTERVAL; // 18 packets at 56 ms interval

												int[] rawVals = unpackByteArray(packet, BYTE_OFFSET, VALUE_COUNT);

												for(int i = 0; i < rawVals.length; i++) {

													Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue =
															new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();                                                

													DoubleValue doubleValue = new DoubleValue(SensorAttributeNameEnum.BREATHING_WAVEFORM_SAMPLE, rawVals[i] * SCALE_FACTOR);

													sensorAttributeToValue.put(SensorAttributeNameEnum.BREATHING_WAVEFORM_SAMPLE, doubleValue);

													SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);

													sendDataEvent(data);                                                

													elapsedTime += WAVEFORM_INTERVAL;
												}

											} else {

												logger.warn("expected breathing waveform packet with DLC (size) of " + BREATHING_PACKET_DLC + " but packet DLC was " + size + " ... dropping packet"); 
											} 

										} else if (msgID == SET_BREATHING_WAVEFORM_PACKET) {

											if (packet[4] == ACK) {
												logger.info("Breathing Waveform Packet Transmit was set");
											} else if (packet[4] == NAK) {
												logger.error("Breathing Waveform Packet Transmit was not set");
											} else {
												logger.error("Invalid Ack for Breathing Waveform Packet Transmit");
											}
										} else if (msgID == SET_ECG_WAVEFORM_PACKET) {

											if (packet[4] == ACK) {
												logger.info("Ecg Waveform Packet Transmit was set");
											} else if (packet[4] == NAK) {
												logger.error("Ecg Waveform Packet Transmit was not set");
											} else {
												logger.error("Invalid Ack for Ecg Waveform Packet Transmit");
											}
										} 
									}
								}
							}
						}
					} catch (Exception e) {
						logger.error("Caught exception while trying to read or send sensor data", e);
						createSensorError("There was a problem reading/sending BioHarness sensor data");
					}
				}

				try {
					sleep(intervalMillis);
				} catch (@SuppressWarnings("unused") InterruptedException e) {
					logger.warn("Sensor thread interrupted");
				}
			}
		}
	}


	/**
	 * Disables the bluetooth timeout
	 */
	private void disableBluetoothTimeout() {

		logger.info("Disabling Bluetooth timeout");

		byte[] data = new byte[9];
		data[0] = STX;
		data[1] = SET_BT_LINK_CONFIG; // msg ID 0XA4
		data[2] = 0x04; // DLC (message size)
		data[3] = (byte) 0; // link timeout LS byte
		data[4] = (byte) 0; // link timeout MS byte
		data[5] = (byte) 0; // lifesign period LS byte
		data[6] = (byte) 0; // lifesign period MS byte
		data[7] = (byte) ZephyrUtils.generateCRC(data); // CRC
		data[8] = ETX;

		try {
			dos.write(data);
		} catch (IOException e) {
			logger.error("Received exception while trying to disable bluetooth timeout.", e);
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to the BioHarness sensor to enable/disable the Bluetooth
	 * transmission of the periodic General Data Packet.
	 * 
	 * @param enable
	 *            boolean indicating if the transmission of the General Data
	 *            Packet should be enabled or not
	 */
	private void requestGeneralDataPacket(boolean enable) {

		logger.info("BioHarness sensor - requesting General Data Packet: " + enable);

		byte[] data = new byte[6];
		data[0] = STX;
		data[1] = SET_GENERAL_DATA_PACKET; // msg ID 14
		data[2] = 0x01; // DLC (data size)
		data[3] = enable ? (byte) 0x01 : (byte) 0x00; // enabled
		data[4] = (byte) ZephyrUtils.generateCRC(data); // CRC-8
		data[5] = ETX;

		try {
			dos.write(data);
		} catch (IOException e) {
			logger.error("Received exception while trying to request general data packet.", e);
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to the BioHarness sensor to enable/disable the Bluetooth
	 * transmission of the periodic Breathing Waveform Packet.
	 * 
	 * @param enable boolean indicating if the transmission of the Breathing Waveform Packet should be enabled or not
	 */
	private void requestBreathingWaveformPacket(boolean enable) {

		logger.info("BioHarness sensor - requesting Breathing Waveform Packet: " + enable);

		byte[] data = new byte[6];
		data[0] = STX;
		data[1] = SET_BREATHING_WAVEFORM_PACKET; // msg ID 0x15
		data[2] = 0x01; // DLC (data size)
		data[3] = enable ? (byte) 0x01 : (byte) 0x00; // enabled
		data[4] = (byte) ZephyrUtils.generateCRC(data); // CRC-8
		data[5] = ETX;

		try {
			dos.write(data);
		} catch (IOException e) {
			logger.error("Received exception while trying to request breathing waveform packet.", e);
			e.printStackTrace();
		}
	}

	/**
	 * Sends a request to the BioHarness sensor to enable/disable the Bluetooth
	 * transmission of the periodic ECG Waveform Packet.
	 * 
	 * @param enable boolean indicating if the transmission of the ECG Waveform Packet should be enabled or not
	 */
	private void requestEcgWaveformPacket(boolean enable) {

		logger.info("BioHarness sensor - requesting ECG Waveform Packet: " + enable);

		byte[] data = new byte[6];
		data[0] = STX;
		data[1] = SET_ECG_WAVEFORM_PACKET; // msg ID 0x16
		data[2] = 0x01; // DLC (data size)
		data[3] = enable ? (byte) 0x01 : (byte) 0x00; // enabled
		data[4] = (byte) ZephyrUtils.generateCRC(data); // CRC-8
		data[5] = ETX;

		try {
			dos.write(data);
		} catch (IOException e) {
			logger.error("Received exception while trying to request ecg waveform packet.", e);
			e.printStackTrace();
		}   	

	}



	/**
	 * Maps the information in the specified General Data packet byte array into
	 * the attribute map placed into a SensorData message.
	 * 
	 * @param packet byte array containing the General Data packet data
	 * @return an attribute map to be placed into a SensorData message
	 */
	private Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> mapGeneralDataPacketData(byte[] packet) {

		Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = 
				new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>();

		int heartRate = ZephyrUtils.byteAsInteger(packet[12]) | (ZephyrUtils.byteAsInteger(packet[13]) << 8);
		sensorAttributeToValue.put(SensorAttributeNameEnum.HEART_RATE, new IntegerValue(
				SensorAttributeNameEnum.HEART_RATE, heartRate));

		int respirationRate = ZephyrUtils.byteAsInteger(packet[14]) | (ZephyrUtils.byteAsInteger(packet[15]) << 8);
		double bpm = respirationRate * 0.1;
		sensorAttributeToValue.put(SensorAttributeNameEnum.RESPIRATION_RATE, new DoubleValue(
				SensorAttributeNameEnum.RESPIRATION_RATE, bpm));

		byte lower = packet[18];
		byte upper = packet[19];
		int posture = lower | (upper << 8);
		if ((upper & 0x80) != 0) {
			posture = -(0xFFFFFFFF - posture);
		}
		sensorAttributeToValue.put(SensorAttributeNameEnum.POSTURE, new IntegerValue(
				SensorAttributeNameEnum.POSTURE, posture));

		return sensorAttributeToValue;
	}

	/**
	 * Bioharness breathing and ecg waveforms arrive as a sequence of 10-bit unsigned integer values packed into a byte array.
	 * 
	 * This method unpacks the values from (a subset of) a byte array.
	 * 
	 * @param ba the array containing the bytes to be unpacked
	 * @param offset the offset index for the start of the bytes to be unpacked
	 * @param valCount the number of values that should be unpacked from the byte array
	 * @return the values unpacked from the byte array.
	 */
	private int[] unpackByteArray(byte[] ba, int offset, int valCount) {

		int[] vals = new int[valCount];

		final int BITS_PER_VALUE = 10;
		final int BITS_PER_BYTE = 8;

		for(int i = 0; i < valCount; i++) {

			final int relBitOffset = i * BITS_PER_VALUE;
			final int lowByteIndex = offset + (relBitOffset / BITS_PER_BYTE);
			//final int highByteIndex = lowByteIndex + 1;

			final int lowByteShift = relBitOffset % BITS_PER_BYTE;

			//int highByteShift = BITS_PER_BYTE - (BITS_PER_VALUE - BITS_PER_BYTE) - lowByteShift; //(is this the general form?)
			final int highByteShift = 6 - lowByteShift;

			int  lowBytes = (ba[lowByteIndex] & 0xFF)   >> lowByteShift;
			int highBytes = ((ba[lowByteIndex + 1] & 0xFF) << highByteShift) & 0xFF;

			highBytes <<= (BITS_PER_VALUE - BITS_PER_BYTE);
			vals[i] = (lowBytes | highBytes) & 0xFFFF;		
		}

		return vals;
	}  

}
