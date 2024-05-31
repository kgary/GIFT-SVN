/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.concept;

import com.google.gwt.core.client.JavaScriptObject;

import generated.course.ConceptNode;

import com.github.gwtd3.api.arrays.Array;

/**
 * A native JavaScript representation of a {@link ConceptNode}. This is used to pass course concepts between GAT editors.
 * 
 * @author nroberts
 */
public class JsConceptNode extends JavaScriptObject {

    /** No arg constructor required by {@link JavaScriptObject} */
    protected JsConceptNode() {}
    
    /**
     * Creates a JavaScript object representing the given course concept
     * 
     * @param node the course concept node to represent. If null, null will be returned.
     * @return an equivalent JavaScript object representing the same course concept.
     */
    public static JsConceptNode create(ConceptNode node) {
        
        if(node == null) {
            return null;
        }
        
        Array<JsConceptNode> nodes = null;
        
        if(node.getConceptNode() != null) {
            
            nodes = Array.create();
            
            for(ConceptNode child : node.getConceptNode()) {
                JsConceptNode jsChild = create(child);
                if(jsChild != null) {
                    nodes.push(jsChild);
                }
            }
        }
        
        return create(node.getName(), 
                JsAuthoritativeResource.create(node.getAuthoritativeResource()), 
                nodes);
    }
    
    private static native JsConceptNode create(String name, JsAuthoritativeResource resource, Array<JsConceptNode> nodes)/*-{
        return {
            name : name,
            resource : resource,
            nodes : nodes
        }
    }-*/;
    
    private final native String getName()/*-{
        return this.name;
    }-*/;
    
    private final native JsAuthoritativeResource getResource()/*-{
        return this.resource;
    }-*/;

    private final native Array<JsConceptNode> getNodes()/*-{
        return this.nodes;
    }-*/;

    /**
     * Gets the original course concept node that this JavaScript object was created from. Note that the
     * returned concept is technically a different object created using the same underlying data.
     * 
     * @return the original course concept node. Cannot be null.
     */
    public final ConceptNode getOriginalNode() {
        
        ConceptNode origNode = new ConceptNode();
        origNode.setName(getName());
        
        JsAuthoritativeResource resource = getResource();
        if(resource != null) {
            origNode.setAuthoritativeResource(resource.getOriginalResource());
        }
        
        Array<JsConceptNode> nodes = getNodes();
        
        if(nodes != null) {
            for(JsConceptNode jsChild : nodes.asList()) {
                
                ConceptNode child = jsChild.getOriginalNode();
                if(child != null) {
                    origNode.getConceptNode().add(child);
                }
            }
        }
        
        return origNode;
    }

}
