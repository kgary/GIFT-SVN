/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class provides an case insensitive ArrayList with strings.
 * The strings in the list are not changed (i.e. they maintain case
 * sensitivity), however operations such as contains, indexOf and remove
 * ignore case.
 * 
 * @author mhoffman
 *
 */
public class CaseInsensitiveList extends ArrayList<String>{
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean contains(Object value){
		
		if(value instanceof String){
			
			for(String item : this){
				
				if(item != null && item.equalsIgnoreCase((String) value)){
					return true;
				}
			}
			
		}else{
			return super.contains(value);
		}
		
		return false;
	}
	
	@Override
	public int indexOf(Object value){
		
		for(int i = 0; i < this.size(); i++){
			
			String item = this.get(i);
			
			if(item != null && item.equalsIgnoreCase((String) value)){
				return i;
			}
		}
		
		return -1;
	}
	
	@Override
	public boolean remove(Object value){
		
		boolean changed = false;
		
		if(value != null){
    			
    		if(value instanceof String){
    			
    			int index = indexOf(value);
    			try{
    				changed |= remove(index) != null;
    			}catch(@SuppressWarnings("unused") Exception e){}
    		}

		}
		
		return changed;

	}
	
	@Override
	public boolean removeAll(Collection<?> c){
		
		boolean changed = false;
		
		if(c != null){
			
			int index;
    		for(Object item : c){
    			
        		if(item instanceof String){
        			
        			index = indexOf(item);
        			try{
        				changed |= remove(index) != null;
        			}catch(@SuppressWarnings("unused") Exception e){}
        			
        		}else{
        		
	        		//in case the item isn't a string
	        		changed |= remove(item);
        		}
    		}
		}
		
		return changed;

	}
}