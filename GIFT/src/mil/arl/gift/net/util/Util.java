/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a general utility class for GIFT network.  It doesn't contain ActiveMQ, JSON,
 * message type information or logic, nor anything else specific to between module communication.
 * 
 * @author mhoffman
 *
 */
public class Util {
	
	/** instance of the logger */
    private static final Logger logger = LoggerFactory.getLogger(Util.class);
	
    /** contains all the IP addresses (IPv4 only) across all network adapters for this machine */
	private static Set<InetAddress> localAddresses;
	
	/** contains all the IP addresses (IPv4 only) across all network adapters for this machine */
	private static Set<String> localAddressesAsStrings;
	
	/** 
	 * The address of the local host. 
	 * This is achieved by retrieving the name of the host from the system, then resolving that name into an InetAddress. 
	 */
	private static InetAddress localHostAddress;
	
	/** minimum valid port */
	private static int MIN_PORT = 1;
	
	/** maximum valid port */
	private static int MAX_PORT = 65535;
	
	/**
	 * Currently this class only provides static implementations
	 */
	private Util(){ }

    static{
    	
        // set up the collection of local addresses
        Set<InetAddress> addresses = new HashSet<>(5);
        localAddressesAsStrings = new HashSet<>();

        try {

            for (Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces();
                i.hasMoreElements(); ) {

                NetworkInterface iface = i.nextElement();

                for (Enumeration<InetAddress> j = iface.getInetAddresses();
                    j.hasMoreElements(); ) {

                    InetAddress a = j.nextElement();
                    if(a instanceof Inet4Address){
                        addresses.add(a);
                        localAddressesAsStrings.add(a.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {

        	System.out.println("Caught exception while trying to get local host address");
        	e.printStackTrace();
            logger.error("Caught SocketException while trying to get network interfaces", e);
            throw new AssertionError(e);
        }

        localAddresses = addresses;
        
        try{
        	localHostAddress = InetAddress.getLocalHost();
	
	        // Get IP Address
	        //ipaddr = addr.getHostAddress();
	
	        //debug
	        //System.out.println("addr = "+ipaddr);
        } catch (Exception e) {

            System.out.println("Caught exception while trying to get local host address");
            e.printStackTrace();
            logger.error("Caught exception while trying to get local host address", e);
            throw new AssertionError(e);
        }
    }
    
    /**
     * Return the collection of all the IP addresses across all network adapters for this machine 
     * 
     * @return the unique set of IP addresses
     */
    public static Set<InetAddress> getLocalAddresses(){
    	return localAddresses;
    }
    
    /**
     * Return the collection of all the IP addresses across all network adapters for this machine 
     * 
     * @return the unique set of IP addresses as strings (e.g. 10.1.21.13)
     */
    public static Set<String> getLocalAddressesAsStrings(){
        return localAddressesAsStrings;
    }
    
    /**
     * Return whether the address provided matches one of the local addresses
     * for this machine.  All network interfaces and their address will be included
     * in this search.
     * 
     * @param address the address to search
     * @return true iff the address matches one of the addresses for this machine
     */
    public static boolean isLocalAddress(InetAddress address){
    	return localAddresses != null && address != null && localAddresses.contains(address);
    }
    
    
    /**
     * Return whether the address (in string format) provided matches one of the local addresses
     * for this machine.  All network interfaces and their address will be included in this search.
     * The match must be exact (ignoring case).  
     * @param address - address (in string format to search).  
     * @return true if the address matches one of the addresses for this machine.
     */
    public static boolean isLocalAddress(String address) {
        
        boolean isLocal = false;
        if (address != null && !address.isEmpty()) {
            
            for (InetAddress inetAddr : localAddresses) {
                
                if (!inetAddr.getHostAddress().isEmpty() && 
                      inetAddr.getHostAddress().equalsIgnoreCase(address)) {
                    isLocal = true;
                }
            }
        }
        
        return isLocal;
    }
    /**
     * Return the local host address
     * 
     * @return the local host address
     */ 
    public static InetAddress getLocalHostAddress(){
    	return localHostAddress;
    }
    
    /**
     * Return true iff the port provided is a valid TCP/UDP port (i.e. is it in the range allowed).
     * True doesn't indicate that the port is available for use or that it is being used.
     * 
     * @param port the port to check
     * @return boolean
     */
    public static boolean isValidPort(int port){
        return port >= MIN_PORT && port <= MAX_PORT;
    }
}
