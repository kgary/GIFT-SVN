package mil.arl.gift.lms.impl.lrs.xapi.processor;

import mil.arl.gift.common.util.StringUtils;

/**
 * Generic class for classes which process / handle GIFT data structures prior to
 * xAPI Statement generation.
 * 
 * @author Yet Analytics
 *
 */
public abstract class AbstractProcessor implements XapiProcessor {
    // Optional
    protected String actorSlug;
    protected Integer domainSessionId;
    protected String domainId;
    // Constructor
    public AbstractProcessor(String actorSlug, String domainId, Integer domainSessionId) {
        setActorSlug(actorSlug);
        if(StringUtils.isBlank(domainId)) {
            throw new IllegalArgumentException("Domain Identifier can not be null!");
        }
        setDomainId(domainId);
        if(domainSessionId == null) {
            throw new IllegalArgumentException("Domain Session Identifier can not be null!");
        }
        setDomainSessionId(domainSessionId);
    }
    public AbstractProcessor(String actorSlug, Integer domainSessionId) {
        setActorSlug(actorSlug);
        if(domainSessionId == null) {
            throw new IllegalArgumentException("Domain Session Identifier can not be null!");
        }
        setDomainSessionId(domainSessionId);
    }
    
    public void setActorSlug(String actorSlug) {
        this.actorSlug = actorSlug;
    }
    public String getActorSlug() {
        return actorSlug;
    }
    public Boolean isActorSlugSet() {
        return !(getActorSlug() == null);
    }
    public void setDomainSessionId(Integer domainSessionId) {
        this.domainSessionId = domainSessionId;
    }
    public Integer getDomainSessionId() {
        return domainSessionId;
    }
    public Boolean isDomainSessionSet() {
        return !(getDomainSessionId() == null);
    }
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }
    public String getDomainId() {
        return domainId;
    }
    public Boolean isDomainSet() {
        return !(getDomainId() == null);
    }
}
