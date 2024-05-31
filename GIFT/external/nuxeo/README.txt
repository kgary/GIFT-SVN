This version of the nuxeo-automation-client-7.1.jar file is a lightweight version with no dependencies.  The additional files in this directory are the 
additional dependencies that are needed in GIFT (that don't already exist somewhere else in the GIFT/external folder).  

There is a version that has dependencies (nuxeo-automation-client-7.1-jar-with-dependencies.jar), however in GIFT those additional 
dependencies were causing conflicts specifically in logging.  GIFT logging was getting overridden when re-including these depenencies.  

We are using the lightweight version and ONLY including dependencies that are needed (and not already in GIFT).  The full set of dependencies that the nuxeo-automation-client-7.1.jar file seem to be documented in the Manifest.MF file and is listed here:

Import-Package: com.sun.jersey.api.client;version="[1.17,2)",com.sun.j
 ersey.api.client.filter;version="[1.17,2)",com.sun.jersey.client.apac
 he4;version="[1.17,2)",com.sun.jersey.core.util;version="[1.17,2)",ja
 vax.activation,javax.mail;version="[1.4,2)",javax.mail.internet;versi
 on="[1.4,2)",javax.ws.rs.core;version="[1.1,2)",org.apache.commons.co
 dec;version="[1.9,2)",org.apache.commons.io;version="[1.4,2)",org.apa
 che.commons.lang;version="[2.6,3)",org.apache.commons.logging,org.apa
 che.http;version="[4.3,5)",org.apache.http.auth;version="[4.3,5)",org
 .apache.http.client;version="[4.3,5)",org.apache.http.client.methods;
 version="[4.3,5)",org.apache.http.conn;version="[4.3,5)",org.apache.h
 ttp.entity;version="[4.3,5)",org.apache.http.impl.client;version="[4.
 3,5)",org.apache.http.impl.conn;version="[4.3,5)",org.apache.http.par
 ams;version="[4.3,5)",org.apache.http.protocol;version="[4.3,5)",org.
 codehaus.jackson;version="[1.8,2)",org.codehaus.jackson.map;version="
 [1.8,2)",org.codehaus.jackson.map.annotate;version="[1.8,2)",org.code
 haus.jackson.map.deser;version="[1.8,2)",org.codehaus.jackson.map.int
 rospect;version="[1.8,2)",org.codehaus.jackson.map.module;version="[1
 .8,2)",org.codehaus.jackson.map.type;version="[1.8,2)",org.codehaus.j
 ackson.type;version="[1.8,2)",org.osgi.framework;version="[1.5,2)"

We are only putting in the jars that are required by GIFT so it is possible that more jars from the above list will
be required.  If you run into issues with NoClassDefFound errors it is possible that you may need to include one or
more of the jar files from the list above.




