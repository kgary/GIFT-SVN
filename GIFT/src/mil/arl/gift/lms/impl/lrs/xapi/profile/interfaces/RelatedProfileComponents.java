package mil.arl.gift.lms.impl.lrs.xapi.profile.interfaces;

import java.net.URI;
import java.util.List;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;

/**
 * Helper methods for AbstractConceptRelation class.
 * 
 * @author Yet Analytics
 *
 */
public interface RelatedProfileComponents {
    
    public List<URI> getBroader();
    public void setBroader(List<URI> broader);
    public void addBroader(String conceptId) throws LmsXapiProfileException;
    public void addBroader(URI conceptId);
    public List<URI> getBroadMatch();
    public void setBroadMatch(List<URI> broadMatch);
    public void addBroadMatch(String conceptId) throws LmsXapiProfileException;
    public void addBroadMatch(URI conceptId);
    public List<URI> getNarrower();
    public void setNarrower(List<URI> narrower);
    public void addNarrower(String conceptId) throws LmsXapiProfileException;
    public void addNarrower(URI conceptId);
    public List<URI> getNarrowMatch();
    public void setNarrowMatch(List<URI> narrowMatch);
    public void addNarrowMatch(String conceptId) throws LmsXapiProfileException;
    public void addNarrowMatch(URI conceptId);
    public List<URI> getRelated();
    public void setRelated(List<URI> related);
    public void addRelated(String conceptId) throws LmsXapiProfileException;
    public void addRelated(URI conceptId);
    public List<URI> getRelatedMatch();
    public void setRelatedMatch(List<URI> relatedMatch);
    public void addRelatedMatch(String conceptId) throws LmsXapiProfileException;
    public void addRelatedMatch(URI conceptId);
    public List<URI> getExactMatch();
    public void setExactMatch(List<URI> exactMatch);
    public void addExactMatch(String conceptId) throws LmsXapiProfileException;
    public void addExactMatch(URI conceptId);
}
