package mil.arl.gift.lms.impl.lrs.xapi.profile.server;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;

/**
 * Parses xAPI Profiles stored in LMS Configuration directory and provides search which returns
 * found items in the same class used for the live client (SparqlResult).
 * 
 * @author Yet Analytics
 *
 */
public class LocalClient {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String CONCEPTS = "concepts";
    private static final String TEMPLATES = "templates";
    private static final String PATTERNS = "patterns";
    
    private static String fileLocation(String profileName) {
        return PackageUtil.getConfiguration() +
                File.separator+"lms"+File.separator+"profiles"+File.separator+profileName+".jsonld";
    }
    
    private static JsonNode profileAsJson(String profileName) throws LmsXapiProfileException {
        try {
            return mapper.readTree(new File(fileLocation(profileName)));
        } catch (IOException e) {
            throw new LmsXapiProfileException("Unable to read in profile from JSON file!", e);
        }
    }
    
    // Individual xAPI Profiles
    private JsonNode adlVocabulary;
    private JsonNode momTla;
    private JsonNode gift;
    private JsonNode tincan;
    private JsonNode scorm;
    
    // Across xAPI Profiles
    private ArrayNode concepts;
    private ArrayNode templates;
    private ArrayNode patterns;
    
    public LocalClient() throws LmsXapiProfileException {
        this.adlVocabulary = profileAsJson("AdlVocabularyProfile");
        this.momTla = profileAsJson("MomTlaProfile");
        this.gift = profileAsJson("gift_min");
        this.tincan = profileAsJson("tincan");
        this.scorm = profileAsJson("scorm");
        setConcepts();
        setTemplates();
        setPatterns();
    }
    
    private void updateArray(ArrayNode coll, JsonNode src) {
        if(coll != null && src != null) {
            for(JsonNode node : src) {
                coll.add(node);
            }
        }
    }
    
    private void setConcepts() {
        ArrayNode arrayNode = mapper.createArrayNode();
        updateArray(arrayNode, getAdlVocabulary().get(CONCEPTS));
        updateArray(arrayNode, getMomTla().get(CONCEPTS));
        updateArray(arrayNode, getGift().get(CONCEPTS));
        updateArray(arrayNode, getTincan().get(CONCEPTS));
        updateArray(arrayNode, getScorm().get(CONCEPTS));
        this.concepts = arrayNode;
    }
    
    private void setTemplates() {
        ArrayNode arrayNode = mapper.createArrayNode();
        updateArray(arrayNode, getAdlVocabulary().get(TEMPLATES));
        updateArray(arrayNode, getMomTla().get(TEMPLATES));
        updateArray(arrayNode, getGift().get(TEMPLATES));
        updateArray(arrayNode, getTincan().get(TEMPLATES));
        updateArray(arrayNode, getScorm().get(TEMPLATES));
        this.templates = arrayNode;
    }
    
    private void setPatterns() {
        ArrayNode arrayNode = mapper.createArrayNode();
        updateArray(arrayNode, getAdlVocabulary().get(PATTERNS));
        updateArray(arrayNode, getMomTla().get(PATTERNS));
        updateArray(arrayNode, getGift().get(PATTERNS));
        updateArray(arrayNode, getTincan().get(PATTERNS));
        updateArray(arrayNode, getScorm().get(PATTERNS));
        this.patterns = arrayNode;
    }
    
    public JsonNode getAdlVocabulary() {
        return adlVocabulary;
    }
    
    public JsonNode getMomTla() {
        return momTla;
    }
    
    public JsonNode getGift() {
        return gift;
    }
    
    public JsonNode getTincan() {
        return tincan;
    }
    
    public JsonNode getScorm() {
        return scorm;
    }
    
    public JsonNode getConcepts() {
        return concepts;
    }
    
    public JsonNode getTemplates() {
        return templates;
    }
    
    public JsonNode getPatterns() {
        return patterns;
    }
    
    private SparqlResult localSearch(String id, JsonNode stored) {
        if(stored.isArray()) {
            for(JsonNode node : stored) {
                String nodeId = node.get("id").asText(); 
                if(nodeId.equals(id)) {
                    return new SparqlResult(node);
                }
            }   
        }
        return null;
    }
    
    public SparqlResult searchConcepts(String id) {
        return localSearch(id, getConcepts());
    }
    
    public SparqlResult searchAdlConcepts(String id) {
        return localSearch(id, getAdlVocabulary().get(CONCEPTS));
    }
    
    public SparqlResult searchMomConcepts(String id) {
        return localSearch(id, getMomTla().get(CONCEPTS));
    }
    
    public SparqlResult searchGiftConcepts(String id) {
        return localSearch(id, getGift().get(CONCEPTS));
    }
    
    public SparqlResult searchTincanConcepts(String id) {
        return localSearch(id, getTincan().get(CONCEPTS));
    }
    
    public SparqlResult searchScormConcepts(String id) {
        return localSearch(id, getScorm().get(CONCEPTS));
    }
    
    public SparqlResult searchTemplates(String id) {
        return localSearch(id, getTemplates());
    }
    
    public SparqlResult searchMomTemplates(String id) {
        return localSearch(id, getMomTla().get(TEMPLATES));
    }
    
    public SparqlResult searchGiftTemplates(String id) {
        return localSearch(id, getGift().get(TEMPLATES));
    }
    
    public SparqlResult searchPatterns(String id) {
        return localSearch(id, getPatterns());
    }
    
}
