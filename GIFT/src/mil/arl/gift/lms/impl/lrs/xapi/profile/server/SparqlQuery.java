package mil.arl.gift.lms.impl.lrs.xapi.profile.server;

import generated.lms.Parameters;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.lms.impl.common.LmsXapiSparqlException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Execution of SPARQL queries against the ADL xAPI Profile Server.
 * 
 * @author Yet Analytics
 *
 */
public class SparqlQuery {
    
    private URL endpoint;
    private String xApiKey;
    private static final String XAPIKEY = "x-api-key";
    
    public SparqlQuery(Parameters params) throws LmsXapiSparqlException {
        this(params.getProfileServer().getEndPoint(), params.getProfileServer().getApiKey());
    }
    
    public SparqlQuery(String endpoint, String xApiKey) throws LmsXapiSparqlException {
        if(endpoint == null || StringUtils.isBlank(endpoint)) {
            throw new LmsXapiSparqlException("Endpoint must be non-null and non blank!");
        }
        try {
            this.endpoint = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new LmsXapiSparqlException("Invalid Sparql Query endpoint!", e);
        }
        
        if(xApiKey == null || StringUtils.isBlank(xApiKey)) {
            throw new LmsXapiSparqlException("xApiKey must be non-null and non blank!");
        }
        this.xApiKey = xApiKey;
    }
    
    private HttpURLConnection configureRequest() throws LmsXapiSparqlException {
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) endpoint.openConnection();
        } catch (IOException e) {
            throw new LmsXapiSparqlException("Unable to connect to endpoint!", e);
        }
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new LmsXapiSparqlException("Unable to set POST request method!", e);
        }
        conn.setRequestProperty(XAPIKEY, xApiKey);
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        return conn;
    }
    
    public JsonNode executeQuery(String query) throws LmsXapiSparqlException {
        HttpURLConnection conn = configureRequest();
        // SPARQL query string as body of Request
        byte[] input;
        try {
            input = query.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new LmsXapiSparqlException("Unable to convert sparql query to byte array!", e);
        }
        OutputStream os;
        try {
            os = conn.getOutputStream();
        } catch (IOException e) {
            throw new LmsXapiSparqlException("Unable to establish sparql query output stream!", e);
        }
        try {
            os.write(input, 0, input.length);
        } catch (IOException e) {
            throw new LmsXapiSparqlException("Unable to write sparql query to profile server endpoint!", e);
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                throw new LmsXapiSparqlException("Unable to close connection to profile server endpoint!", e);
            }
        }
        // Handle Response
        JsonNode data;
        InputStream is;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            throw new LmsXapiSparqlException("Unable to create input stream containing profile server response", e);
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            try {
                data = mapper.readTree(is);
            } catch (IOException e) {
                throw new LmsXapiSparqlException("Unable to parse profile server response to json!", e);
            }
            return data;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new LmsXapiSparqlException("unable to close profile server response input stream!", e);
            }
        }
    }
    
    public static SparqlResult parseQuery(JsonNode data){
        SparqlResult result = null;
        if(data.isArray()) {
            for(JsonNode node : data) {
                // Init vs rest
                if(result == null) {
                    result = new SparqlResult(node);
                } else {
                    result.merge(new SparqlResult(node));
                }
            }
        } else {
            result = new SparqlResult(data);
        }
        return result;
    }
}
