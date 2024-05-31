/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jms.JMSException;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.net.api.message.RawMessageHandler;

/**
 * A unit test for the topic message client
 * 
 * @author jleonard
 */
@Ignore
public class TopicMessageClientTest {

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

	private static void handleString(String str) {

		JSONObject obj = (JSONObject) (JSONValue.parse(str));

		if (obj != null) {
			System.out.println("Num:" + obj.get("num") + ", " + "Text:"
					+ obj.get("txt"));
		} else {
			System.out.println("Malformed JSON Message.");
		}
	}

	@Ignore
	public void testConnect() {

		// Sets the URL of the ActiveMQ server
		// Sets the name of the topic to connect to be "TEST.FOO"
		TopicMessageClient topicSubB = new TopicMessageClient(
				"tcp://localhost:61617",
				"ActiveMQ.Advisory.Producer.Topic.Monitor_Topic");
		// Set the class to handle a 'message received' callback to be the JSON
		// Message Handler
		topicSubB.setMessageHandler(new RawMessageHandler() {
            
            @Override
            public boolean processMessage(String msg,
                    MessageEncodingTypeEnum encodingType) {

                handleString(msg);

                return true;
            }
        });

		boolean success;

		try {

			success = topicSubB.connect();

			assertTrue(success);
			
			topicSubB.disconnect(false);

		} catch (JMSException e) {

			fail("Caught JMSException " + e.getMessage());
		}
	}
}
