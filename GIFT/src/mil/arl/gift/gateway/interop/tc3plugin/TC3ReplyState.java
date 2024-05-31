/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.tc3plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Helper class that holds the data for a reply message.
 * 
 * @author asanchez
 * 
 */
public class TC3ReplyState
{
	/** The unique Id of this message. */
	private Integer _UniqueReplayId;
	
	/** The command name that was sent to TC3. */
	public String _CmdName = null;
	
	/** The result data sent back from TC3, if any. */
	public Map<String, String> _Results = null;
	
	/**
     * Class constructor.
     * 
     * @param messageUniqueId - A unique message ID specific only to this game state message.
     * @param cmdName - The command sent to TC3.
     */
	public TC3ReplyState( Integer messageUniqueId, String cmdName )
	{
		_UniqueReplayId = messageUniqueId;
		_CmdName = cmdName;
		
		_Results = new HashMap<String, String>();
	}
	
	/**
     * Gets the unique ID specifically to this message.
     * 
     * @return Integer - This reply state message's unique ID.
     */
	public Integer getUniqueMessageId()
	{
		return _UniqueReplayId;
	}
	
	/**
     * Gets the command name sent to TC3.
     * 
     * @return String - The command name sent to TC3.
     */
	public String getCommandName()
	{
		return _CmdName;
	}
	
    /* Gets a value from the attribute data map given a key.
     * 
     * @param key - The key string from which the value will be identified from.
     * @return Object - The object associated with the key. Null if key is not associated with any values.
     * @throws NullPointerException - The data map is null or invalid.
     * @throws IllegalArgumentException - The key string is null or invalid.
     */
    public Object getValue( String key )
    {
        if ( _Results == null )
        {
            throw new NullPointerException( "The data map on the TC3GameState must not be null." );
        }
        
        if ( key == null || key.isEmpty() )
        {
            throw new IllegalArgumentException( "The key string must not be null or empty." );
        }
        
        return _Results.get( key );
    }
    
    /* Gets the data map from the state.
     * 
     * @return - Map<String, String> - The data map attributes associated to this state.
     */
    public Map<String, String> getData()
    {
        if ( _Results == null )
        {
            return null;
        }
        
        return _Results;
    }
    
    /* Stores a key/value pair in the state.
    * 
    * @param key - The key string from which the value will be identified from.
    * @param value - The value associated with the key.
    * @throws NullPointerException - The data map is null or invalid.
    * @throws IllegalArgumentException - Either the key string or the value object is invalid.
    */
    public void setAttributes( String key, String value )
    {
        if ( _Results == null )
        {
            throw new NullPointerException( "The data map on the TC3GameState must not be null." );
        }
        
        if ( key == null || key.isEmpty() )
        {
            throw new IllegalArgumentException( "The key string must not be null or empty." );
        }
        
        if ( value == null || value.isEmpty() )
        {
            throw new IllegalArgumentException( "The value object must not be null or empty." );
        }
        
        _Results.put( key, value );
    }
    
    /* Stores all the passed in map contents into the data map.
     * 
     * @param inMap - The map holding all the key value pairs.
     * @throws NullPointerException - The data map is null or invalid.
     * @throws IllegalArgumentException - The map parameters is null.
     */
    public void setAttributeMap( Map<String, String> inMap )
    {
        if ( _Results == null )
        {
            throw new NullPointerException( "The data map on the TC3GameState must not be null." );
        }
        
        if ( inMap == null )
        {
            throw new IllegalArgumentException( "Map is null when setting the attribute map in TC3GameState." );
        }

        if ( !inMap.isEmpty() )
        {
        	_Results.putAll( inMap );
        }
    }
	
	/* Spits all the data in the TC3ReplyState as a string.
     * 
     * @return String - All the data presented as a string.
     */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "[Message Unique ID: ").append(getUniqueMessageId().toString() );
		sb.append( ", Command Name: ").append(getCommandName() );

        for ( Entry<String, String> _curEntry : _Results.entrySet() )
        {
            if ( _curEntry.getKey() != null && !_curEntry.getKey().isEmpty() )
            {
                sb.append( ", ").append(_curEntry.getKey()).append(" = ").append(( _curEntry.getValue() != null ? _curEntry.getValue() : "null" ) );
            }
        }
        
        sb.append( "]" );
        
        return sb.toString();
	}
}
