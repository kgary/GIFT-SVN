/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api.codec.proto.survey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import generated.proto.common.survey.SurveyResponseProto;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyResponseJSON;
import mil.arl.gift.net.api.message.codec.proto.survey.SurveyResponseProtoCodec;

@SuppressWarnings("javadoc")
public class ProtobufPerformanceTest {

    private static final int TEST_ITERATIONS = 1;

    private static final File JSON_FILE = new File("test/mil/arl/gift/net/api/codec/proto/survey/SurveyLog.json");

    private static final JSONParser JSON_PARSER = new JSONParser();

    private static final SurveyResponseJSON JSON_CODEC = new SurveyResponseJSON();

    private static final SurveyResponseProtoCodec PROTO_CODEC = new SurveyResponseProtoCodec();

    @Test
    public void test() throws IOException, ParseException {

        // TOGGLE FLAGS TO RUN EITHER THE PROTOBUF METRICS OR JSON METRICS
        boolean testProto = true, testJson = true;

        /* Ensures that the JSON input file exists */
        if (!JSON_FILE.exists()) {
            System.out.println("**************** INPUT FILE DOES NOT EXIST.");
            return;
        }

        /* Define the streams for writing and reading the data. */
        final File TEMP_FILE = new File("test/surveyResponseProto.txt");

        /* Parse the object to test from the JSON input file */
        String jsonText = readFileAsString(JSON_FILE);
        JSONObject jsonResponse = (JSONObject) JSON_PARSER.parse(jsonText);
        SurveyResponse javaResponse = (SurveyResponse) JSON_CODEC.decode(jsonResponse);

        /* Define the variables used for performing the test iterations */
        double preTime = 0, postTime = 0;
        byte[] byteArr = null;

        if (testProto) {

            SurveyResponseProto.SurveyResponse protoResponse = PROTO_CODEC.map(javaResponse);
            byteArr = protoResponse.toByteArray();
            double protoWriteTime = 0, protoReadTime = 0, protoDecodeTime = 0, protoEncodeTime = 0;

            // Protobuf metrics
            for (int i = 0; i < TEST_ITERATIONS; i++) {
                try (final FileOutputStream output = new FileOutputStream(TEMP_FILE)) {
                    // Protobuf write metric
                    preTime = System.currentTimeMillis();
                    protoResponse.writeTo(output);
                    postTime = System.currentTimeMillis() - preTime;
                    protoWriteTime += postTime;
                }

                // Protobuf read metric
                preTime = System.currentTimeMillis();
                byteArr = readFileAsBytes(TEMP_FILE);
                postTime = System.currentTimeMillis() - preTime;
                protoReadTime += postTime;

                Files.delete(TEMP_FILE.toPath());

                // Protobuf decode metric
                preTime = System.currentTimeMillis();
                protoResponse = SurveyResponseProto.SurveyResponse.parseFrom(byteArr);
                postTime = System.currentTimeMillis() - preTime;
                protoDecodeTime += postTime;

                // Protobuf encode metric
                preTime = System.currentTimeMillis();
                byteArr = protoResponse.toByteArray();
                postTime = System.currentTimeMillis() - preTime;
                protoEncodeTime += postTime;
            }

            System.out.println(
                    "\nMETRICS: \nProto Encode: " + protoEncodeTime / 1000 + "\nProto Write: " + protoWriteTime / 1000
                            + "\nProto Read: " + protoReadTime / 1000 + "\nProto Decode: " + protoDecodeTime / 1000
                            + "\nProto Message Size: " + byteArr.length);
        }

        if (testJson) {

            byteArr = jsonResponse.toJSONString().getBytes();
            double jsonWriteTime = 0, jsonReadTime = 0, jsonDecodeTime = 0, jsonEncodeTime = 0;

            for (int i = 0; i < TEST_ITERATIONS; i++) {
                try (final FileOutputStream output = new FileOutputStream(TEMP_FILE)) {
                    // JSON write metric
                    preTime = System.currentTimeMillis();
                    output.write(jsonResponse.toJSONString().getBytes());
                    postTime = System.currentTimeMillis() - preTime;
                    jsonWriteTime += postTime;
                }

                // JSON read metric
                preTime = System.currentTimeMillis();
                jsonText = readFileAsString(JSON_FILE);
                jsonResponse = (JSONObject) JSON_PARSER.parse(jsonText);
                postTime = System.currentTimeMillis() - preTime;
                jsonReadTime += postTime;

                Files.delete(TEMP_FILE.toPath());

                // JSON decode metric
                preTime = System.currentTimeMillis();
                javaResponse = (SurveyResponse) JSON_CODEC.decode(jsonResponse);
                postTime = System.currentTimeMillis() - preTime;
                jsonDecodeTime += postTime;

                // JSON encode metric
                jsonResponse.clear();
                preTime = System.currentTimeMillis();
                JSON_CODEC.encode(jsonResponse, javaResponse);
                postTime = System.currentTimeMillis() - preTime;
                jsonEncodeTime += postTime;
            }

            System.out.println(
                    "\nMETRICS: \nJSON Encode: " + jsonEncodeTime / 1000 + "\nJSON Write: " + jsonWriteTime / 1000
                            + "\nJSON Read: " + jsonReadTime / 1000 + "\nJSON Decode: " + jsonDecodeTime / 1000
                            + "\nJSON Message Size: " + byteArr.length);
        }
    }

    private String readFileAsString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    private byte[] readFileAsBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
