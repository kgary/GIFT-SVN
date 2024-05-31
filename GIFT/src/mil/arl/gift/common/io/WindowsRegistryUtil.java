/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Utilities class for reading and modifying the Windows registry between 32-bit and 64-bit JVM and Windows architectures.
 * 
 * This code was originally posted on Stack Overflow by a user named Petrucio in the following discussion:
 * http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java/11854901#11854901
 * 
 * This class handles discrepancies between how 32-bit and 64-bit JVMs access the Windows registry on 64-bit architectures
 * and has been modified to work with GIFT and conform to GIFT's coding standards. 
 */
public class WindowsRegistryUtil {
	
	/** Location of Window's current user directory in the registry*/
	public static final int HKEY_CURRENT_USER = 0x80000001;
	
	/** Location of Window's local machine directory in the registry*/
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	
	/** Number code used by Windows to indicate successful access to registry entries */
	public static final int REG_SUCCESS = 0;
	
	/** Number code used by Windows to indicate that an entry in the registry could not be found */
	public static final int REG_NOTFOUND = 2;
	
	/** Number code used by Windows to indicate that access to a registry entry was denied */
	public static final int REG_ACCESSDENIED = 5;

	/** Key used to explicitly specify using a 32-bit registry view */
	public static final int KEY_WOW64_32KEY = 0x0200;
	
	/** Key used to explicitly specify using a 64-bit registry view */
	public static final int KEY_WOW64_64KEY = 0x0100;

	/** Key used to give commands privileges to read values, write values, create keys, etc. */
	private static final int KEY_ALL_ACCESS = 0xf003f;
	
	/** Key used to give commands the privilege to read values */
	private static final int KEY_READ = 0x20019;
	
	/** Root used to store and retrieve Windows user preferences */
	private static Preferences userRoot = Preferences.userRoot();
	
	/** Root used to store and retrieve Windows system preferences */
	private static Preferences systemRoot = Preferences.systemRoot();
		
	private static Class<? extends Preferences> userClass = userRoot.getClass();
	
	/**
	 * Windows-specific preferences methods used to modify, read, and create registry values. 
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry, but there does not appear to be a way to accomplish this programmatically.
	 */
	private static Method regOpenKey = null;
	private static Method regCloseKey = null;
	private static Method regQueryValueEx = null;
	private static Method regEnumValue = null;
	private static Method regQueryInfoKey = null;
	private static Method regEnumKeyEx = null;
	private static Method regCreateKeyEx = null;
	private static Method regSetValueEx = null;
	private static Method regDeleteKey = null;
	private static Method regDeleteValue = null;

	static {
		try {
			regOpenKey		 = userClass.getDeclaredMethod("WindowsRegOpenKey",		 new Class<?>[] { long.class, byte[].class, int.class });
			regOpenKey.setAccessible(true);
			regCloseKey		= userClass.getDeclaredMethod("WindowsRegCloseKey",		new Class<?>[] { long.class });
			regCloseKey.setAccessible(true);
			regQueryValueEx= userClass.getDeclaredMethod("WindowsRegQueryValueEx",new Class<?>[] { long.class, byte[].class });
			regQueryValueEx.setAccessible(true);
			regEnumValue	 = userClass.getDeclaredMethod("WindowsRegEnumValue",	 new Class<?>[] { long.class, int.class, int.class });
			regEnumValue.setAccessible(true);
			regQueryInfoKey=userClass.getDeclaredMethod("WindowsRegQueryInfoKey1",new Class<?>[] { long.class });
			regQueryInfoKey.setAccessible(true);
			regEnumKeyEx	 = userClass.getDeclaredMethod("WindowsRegEnumKeyEx",	 new Class<?>[] { long.class, int.class, int.class });	
			regEnumKeyEx.setAccessible(true);
			regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", new Class<?>[] { long.class, byte[].class });
			regCreateKeyEx.setAccessible(true);	
			regSetValueEx	= userClass.getDeclaredMethod("WindowsRegSetValueEx",	new Class<?>[] { long.class, byte[].class, byte[].class });	
			regSetValueEx.setAccessible(true); 
			regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue", new Class<?>[] { long.class, byte[].class });	
			regDeleteValue.setAccessible(true); 
			regDeleteKey	 = userClass.getDeclaredMethod("WindowsRegDeleteKey",	 new Class<?>[] { long.class, byte[].class });	
			regDeleteKey.setAccessible(true); 
		
		}catch (Exception e) {
		    System.out.println("There was a problem defining the Windows registry util methods.");
			e.printStackTrace();
		}
	}

	/**
 	 * Returns the string representation of the value with the specified value name in the specified key in the registry
 	 * 
 	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
 	 * @param key The key to read from
 	 * @param valueName The name of the value to read in the key
 	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
 	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
 	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
 	 * @return The value with the specified value name in the specified key
 	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to the value is not allowed
 	 * @throws InvocationTargetException If an error occurs while reading the value
 	 */
	public static String readString(int hkey, String key, String valueName, int wow64) 
		throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
	
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readString(systemRoot, hkey, key, valueName, wow64);
			
		}else if (hkey == HKEY_CURRENT_USER) {
			return readString(userRoot, hkey, key, valueName, wow64);
			
		}else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Returns a map of all value names in the specified key to their respective values in the registry
	 * 
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to read from
	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @return A map of all value names in the specified key to their respective values
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to a value is not allowed
 	 * @throws InvocationTargetException If an error occurs while reading the values
	 */
	public static Map<String, String> readStringValues(int hkey, String key, int wow64) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readStringValues(systemRoot, hkey, key, wow64);
			
		}else if (hkey == HKEY_CURRENT_USER) {
			return readStringValues(userRoot, hkey, key, wow64);
			
		}else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Returns the list of value names in the specified key in the registry
	 * 
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to read from
	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @return The list of value names in the specified key
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to a value is not allowed
 	 * @throws InvocationTargetException If an error occurs while reading the value names
	 */
	public static List<String> readStringSubKeys(int hkey, String key, int wow64) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readStringSubKeys(systemRoot, hkey, key, wow64);
		
		}else if (hkey == HKEY_CURRENT_USER) {
			return readStringSubKeys(userRoot, hkey, key, wow64);
			
		}else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Creates a key in the registry
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE
	 * @param key The name of the key to create
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access is not allowed
 	 * @throws InvocationTargetException If an error occurs while creating the key
	 */
	public static void createKey(int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		int [] ret;
		
		if (hkey == HKEY_LOCAL_MACHINE) {
			ret = createKey(systemRoot, hkey, key);
			regCloseKey.invoke(systemRoot, new Object[] { Long.valueOf(ret[0]) });
		
		}else if (hkey == HKEY_CURRENT_USER) {
			ret = createKey(userRoot, hkey, key);
			regCloseKey.invoke(userRoot, new Object[] { Long.valueOf(ret[0]) });
		
		}else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
		
		if (ret[1] != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + ret[1] + "	key=" + key);
		}
	}

	/**
	 * Write a string value with the specified value name in the specified key in the registry. 
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
 	 * @param key The key to write to
 	 * @param valueName The name of the value to write to in the key
 	 * @param value The value to write with the specified value name in the specified key
 	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
 	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
 	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access is not allowed
 	 * @throws InvocationTargetException If an error occurs while writing the value
	 */
	public static void writeStringValue(int hkey, String key, String valueName, String value, int wow64) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		if (hkey == HKEY_LOCAL_MACHINE) {
			writeStringValue(systemRoot, hkey, key, valueName, value, wow64);
			
		}else if (hkey == HKEY_CURRENT_USER) {
			writeStringValue(userRoot, hkey, key, valueName, value, wow64);
			
		}else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Deletes the specified key from the registry. 
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
 	 * @param key The key to delete
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access is not allowed
 	 * @throws InvocationTargetException If an error occurs while deleting the key
	 */
	public static void deleteKey(int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		int returnCode = -1;
		
		if (hkey == HKEY_LOCAL_MACHINE) {
			returnCode = deleteKey(systemRoot, hkey, key);
			
		}else if (hkey == HKEY_CURRENT_USER) {
			returnCode = deleteKey(userRoot, hkey, key);			
		}
		
		if (returnCode != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + returnCode + "	key=" + key);
		}
	}

	/**
	 * Delete a value with the given value name in the given key from the registry.
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
 	 * @param key The key to delete from
 	 * @param valueName The name of the value to delete
	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access is not allowed
 	 * @throws InvocationTargetException If an error occurs while deleting the value
	 */
	public static void deleteValue(int hkey, String key, String valueName, int wow64) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		int returnCode = -1;
		
		if (hkey == HKEY_LOCAL_MACHINE) {
			returnCode = deleteValue(systemRoot, hkey, key, valueName, wow64);
			
		}else if (hkey == HKEY_CURRENT_USER) {
			returnCode = deleteValue(userRoot, hkey, key, valueName, wow64);
		}
		
		if (returnCode != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + returnCode + "	key=" + key + "	value=" + valueName);
		}
	}

	/**
	 * Deletes a value with the specified value name in the specified key from the registry
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param root The root of the system preferences used to access the registry
	 * @param key The key to delete from
 	 * @param valueName The name of the value to delete
 	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
 	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
 	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
 	 * @return The return code of the operation
 	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to the value is not allowed
 	 * @throws InvocationTargetException If an error occurs while reading the value
 	 */
	private static int deleteValue(Preferences root, int hkey, String key, String value, int wow64)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {
				Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_ALL_ACCESS | wow64)
		});
		
		if (handles[1] != REG_SUCCESS) {
			return (int) handles[1];	// can be REG_NOTFOUND, REG_ACCESSDENIED
		}
		
		int returnCode =((Integer) regDeleteValue.invoke(root, new Object[] { 
		        Integer.valueOf((int) handles[0]), toCstr(value) 
		})).intValue();
		
		regCloseKey.invoke(root, new Object[] { Long.valueOf(handles[0]) });
		
		return returnCode;
	}

	/**
	 * Deletes the specified key from the registry
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param root The root of the system preferences used to access the registry
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to delete
	 * @return The return code of the operation
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private static int deleteKey(Preferences root, int hkey, String key) 
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		int returnCode =((Integer) regDeleteKey.invoke(root, new Object[] {
		        Integer.valueOf(hkey), toCstr(key)
		})).intValue();
		
		return returnCode;	// can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
	}
	
	/**
	 * Returns the value with the specified value name in the specified key in the registry
	 * 
	 * @param root The root of the system preferences used to access the registry
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to read from
 	 * @param valueName The name of the value to read in the key
 	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
 	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
 	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
 	 * @return The value with the specified value name in the specified key
 	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to the value is not allowed
 	 * @throws InvocationTargetException If an error occurred while reading the value
 	 */
	private static String readString(Preferences root, int hkey, String key, String valueName, int wow64)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {
		        Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_READ | wow64)
		});
		
		if (handles[1] != REG_SUCCESS) {
			return null; 
		}
		
		byte[] valueBytes = (byte[]) regQueryValueEx.invoke(root, new Object[] {
		        Integer.valueOf((int) handles[0]), toCstr(valueName)
		});
		
		regCloseKey.invoke(root, new Object[] { Long.valueOf(handles[0]) });
		
		return (valueBytes != null ? new String(valueBytes).trim() : null);
	}

	/**
	 * Returns a map of all value names in the specified key to their respective values in the registry
	 * 
	 * @param root The root of the system preferences used to access the registry
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to read from
	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @return A map of all value names in the specified key to their respective values
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to a value is not allowed
 	 * @throws InvocationTargetException If an error occurs while reading the values
	 */
	private static Map<String,String> readStringValues(Preferences root, int hkey, String key, int wow64)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		HashMap<String, String> results = new HashMap<String,String>();
		
		long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {
		        Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_READ | wow64)
		});
		
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		
		int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] {
		        Integer.valueOf((int) handles[0])
		});

		int count	= info[2]; // count	
		int maxLength = info[3]; // value length max
		for(int index=0; index<count; index++)	{
			
			byte[] name = (byte[]) regEnumValue.invoke(root, new Object[] {
			        Integer.valueOf((int) handles[0]), Integer.valueOf(index), Integer.valueOf(maxLength + 1)
			});
			
			String value = readString(hkey, key, new String(name), wow64);			
			results.put(new String(name).trim(), value);
		}
		
		regCloseKey.invoke(root, new Object[] { Long.valueOf(handles[0]) });
		
		return results;
	}

	/**
	 * Returns the list of value names in the specified key in the registry
	 * 
	 * @param The root of the system preferences used to access the registry
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to read from
	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @return The list of value names in the specified key
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access to a value is not allowed
 	 * @throws InvocationTargetException If an error occurs while reading the value names
	 */
	private static List<String> readStringSubKeys(Preferences root, int hkey, String key, int wow64)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		List<String> results = new ArrayList<String>();
		
		long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {
		        Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_READ | wow64) 
		});
		
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		
		int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] {
		        Integer.valueOf((int) handles[0])
		});

		int count	= info[0]; //count
		int maxLength = info[3]; // value length max
		
		for(int index=0; index<count; index++)	{
			
			byte[] name = (byte[]) regEnumKeyEx.invoke(root, new Object[] {
			        Integer.valueOf((int) handles[0]), Integer.valueOf(index), Integer.valueOf(maxLength + 1)
			});
			
			results.add(new String(name).trim());
		}
		
		regCloseKey.invoke(root, new Object[] { Long.valueOf(handles[0]) });
		return results;
	}

	/**
	 * Creates a key in the registry.
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry.
	 * 
	 * @param root The root of the system preferences used to access the registry
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The name of the key to create
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access is not allowed
 	 * @throws InvocationTargetException If an error occurs while creating the key
	 */
	private static int [] createKey(Preferences root, int hkey, String key)
		throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		
		return (int[]) regCreateKeyEx.invoke(root, new Object[] {
		        Integer.valueOf(hkey), toCstr(key)
		});
	}

	/**
	 * Write a value with the specified value name in the specified key in the registry. 
	 * 
	 * If the Windows registry does not contain the key "Prefs" at HKEY_LOCAL_MACHINE\Software\Javasoft, Java will not
	 * be able to modify the registry. Creating this key manually via Windows' regedit application will allow Java 
	 * permission to modify the registry. 
	 * 
	 * Exercise extreme caution when modifying the registry!
	 * 
	 * @param root The root of the system preferences used to access the registry
	 * @param hkey The HKEY root of the key to read from (i.e. HKEY_CURRENT_USER or HKEY_LOCAL_MACHINE)
	 * @param key The key to write to
 	 * @param valueName The name of the value to write to in the key
 	 * @param value The value to write with the specified value name in the specified key
 	 * @param wow64	0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
 	 *				or KEY_WOW64_32KEY to force access to 32-bit registry view,
 	 *				or KEY_WOW64_64KEY to force access to 64-bit registry view
	 * @throws IllegalArgumentException If the HKEY root passed in is not a valid HKEY value
 	 * @throws IllegalAccessException If access is not allowed
 	 * @throws InvocationTargetException If an error occurs while writing the value
	 */
	private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value, int wow64)
		throws IllegalArgumentException, IllegalAccessException, InvocationTargetException 
	{
		long[] handles = (long[]) regOpenKey.invoke(root, new Object[] {
		        Integer.valueOf(hkey), toCstr(key), Integer.valueOf(KEY_ALL_ACCESS | wow64)
		});
		regSetValueEx.invoke(root, new Object[] { 
		        Integer.valueOf((int) handles[0]), toCstr(valueName), toCstr(value) 
					}); 
		regCloseKey.invoke(root, new Object[] { Long.valueOf(handles[0]) });
	}

	/**
	 * Converts a Java strings to the C string format recognized by Windows
	 * 
	 * @param str the Java string to convert to C format
	 * @return A byte array representation of a C string
	 */
	private static byte[] toCstr(String str) {
		byte[] result = new byte[str.length() + 1];

		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}
}