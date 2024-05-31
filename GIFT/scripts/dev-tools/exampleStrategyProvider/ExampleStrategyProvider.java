/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A simple Java server that acts as an example of how an external strategy provider can handle
 * training session data from GIFT and display it during a course.
 * 
 * @author nroberts
 */
public class ExampleStrategyProvider {
    
    /** A JSON object containing the latest training session state information that was received from GIFT */
    private static JSONObject latestState = null; 
    
    /** An HTTP server instance used to handle requests */
    private static HttpServer server;
    
    /** A handler that handles HTTP GET requests for the latest state information */
    private static HttpHandler stateRequestHandler = new HttpHandler() {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            exchange.getResponseHeaders().put("statusCode", Arrays.asList("200"));
            exchange.getResponseHeaders().put("Content-Type", Arrays.asList("application/json"));
            
            String response = "";
            
            try {
            
                if(exchange.getRequestMethod().equals("GET")) {
                    
                    if(latestState != null) {
                        
                        /* Write the latest received state back to the HTTP response as raw JSON  */
                        response = latestState.toJSONString();
                    }
                 }
                
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
    };
    
    /** A handler that handles HTTP GET requests for an HTML file */
    private static HttpHandler fileRequestHandler = new HttpHandler() {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            exchange.getResponseHeaders().put("statusCode", Arrays.asList("200"));
            exchange.getResponseHeaders().put("Content-Type", Arrays.asList("text/html"));
            exchange.getResponseHeaders().put("Access-Control-Allow-Origin", Arrays.asList("GET, POST, OPTIONS, PUT, PATCH, DELETE")); 
            exchange.getResponseHeaders().put("Access-Control-Allow-Headers", Arrays.asList("X-Requested-With,content-type")); 
            
            String response = "";
            
            try {
            
                if(exchange.getRequestMethod().equals("GET")) {
                   
                   /* Write the example webpage's HTML content to the HTTP response */
                   Path htmlFilePath = Path.of("exampleWebpage.html");
                   response = new String(Files.readAllBytes(htmlFilePath));
                }
                
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
    };

    /** 
     * A handler that handles HTTP POST requests that pass training session state from GIFT
     * to the strategy provider 
     */
    private static HttpHandler baseRequestHandler = new HttpHandler() {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            exchange.getResponseHeaders().put("statusCode", Arrays.asList("200"));
            exchange.getResponseHeaders().put("Content-Type", Arrays.asList("application/json"));
            
            String response = "";
            
            try {
            
                if(exchange.getRequestMethod().equals("POST")) {
                    
                   /* Parse the JSON from the request body*/
                   InputStream requestBody = exchange.getRequestBody(); 
                   JSONParser parser = new JSONParser();
                   JSONObject latestStateJson = (JSONObject) parser.parse(new InputStreamReader(requestBody, "UTF-8"));
                   
                   /* Print a message to confirm that a state was received from GIFT */
                   System.out.println("Received state from GIFT:");
                   System.out.println(latestStateJson.toJSONString());
                   
                   /* Update the latest internal state for the next time that the /state endpoint is invoked */
                   latestState = latestStateJson;
                   
                   
                   if("webpage".equals(latestStateJson.get("contentType"))) {
                       
                       /* If a webpage needs to be shown, return a URL that GIFT can use to display an HTML
                        * file that will render the state information that was just received */
                       String respUrl = "http://" + exchange.getRequestHeaders().get("host").get(0) + "/file";
                       
                       /* In order to show custom feedback for different team member roles, GIFT includes the name
                        * of the target role that the feedback is intended for. We can account for this by
                        * including the role name in the URL that is used to access the HTML file. */
                       Object role = latestStateJson.get("role");
                       if(latestStateJson != null) {
                           respUrl += "?role=" + URLEncoder.encode((String) role, StandardCharsets.UTF_8);
                       }
                       
                       response = respUrl;
                       
                   } else {
                       
                       /* Otherwise, tell GIFT to show a raw text message containing the JSON information */
                       response = "Printing feedback for the following data" + latestStateJson.toJSONString();
                   }
                }
                
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            /* Write the HTTP response to complete the request */
            exchange.sendResponseHeaders(200, response.getBytes().length);
            
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        
    };
    
    /**
     * Starts a Java process that sets up an HTTP server on the configured port and waits
     * for requests from GIFT, acting as an external strategy provider.
     * 
     * @param args optional command line arguments
     */
    public static void main(String[] args) {
        
        /* The port to establish the server connection on. Can be changed if this port is already in use */
        int port = 3000;
        
        System.out.println("Starting example strategy provider on port " + port);
        
        /* 
         * Initialize the server and define the following URL endpoints:
         * 
         * /request - handles a POST request from GIFT to generate strategy content to display. The expected result is a text message or webpage URL.
         * /file    - handles a GET request to show the example HTML webpage. The expected result is an HTML file.
         * /state   - handles a GET request from the example HTML webpage to get the latest state information. The expected result is a JSON object.
         */
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        
            server.createContext("/file", fileRequestHandler);
            server.createContext("/state", stateRequestHandler);
            server.createContext("/request", baseRequestHandler);
            server.setExecutor(null);
            server.start();
        
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /* Keep the server running until the user says to stop it */ 
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            String input = null;
            do {
                System.out.print("\nPress Enter to stop this server.\n");
                input = inputReader.readLine();

            } while (input != null && input.length() != 0);
        } catch (Exception e) {
            System.err.println("Caught exception while reading input: \n");
            e.printStackTrace();
        }
    }
}
