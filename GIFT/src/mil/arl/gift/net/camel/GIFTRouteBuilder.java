/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.camel;

import mil.arl.gift.net.api.SubjectUtil;

import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author jleonard
 */
public class GIFTRouteBuilder extends RouteBuilder {
    
    private String routeInbox, routeOutbox;
    
    public GIFTRouteBuilder(String inbox) {
        routeInbox = inbox;
        routeOutbox = inbox.substring(0, inbox.indexOf("Inbox") - 1);
//        
//        System.out.println("\n\n------------------------------------------------------");
//        Map<String, String> properties = this.getContext().getProperties();
//        if(properties != null){
//            for(String property : properties.keySet()){
//                System.out.println(property+" = "+properties.get(property));
//            }
//        }
//        System.out.println("\n------------------------------------------------------");
    }

    @Override
    public void configure() throws Exception {  
        
        System.out.println("Configuring "+this);
        
        //send all messages to outbox, logger and monitor
        this.from("activemq:queue:"+routeInbox).routeId(routeInbox)
        .multicast()
        .to("activemq:queue:"+routeOutbox,
            "activemq:queue:Logger_Queue",
            "activemq:topic:"+SubjectUtil.MONITOR_TOPIC);                    
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[GIFTRouteBuilder: ");
        sb.append("inbox = ").append(routeInbox);
        sb.append(", outbox = ").append(routeOutbox);
        sb.append("]");
        
        return sb.toString();
    }
    
}
