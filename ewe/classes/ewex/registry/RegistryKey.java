package ewex.registry;
//##################################################################
public class RegistryKey{
//##################################################################
boolean isRemote;
int root;
String path;
boolean fullAccess;
boolean createIfDoesntExist;
boolean isValid = false;

//-------------------------------------------------------------------
protected RegistryKey()
//-------------------------------------------------------------------
{
}

//-------------------------------------------------------------------
RegistryKey(boolean isRemote,int root,String path,boolean fullAccess,boolean createIfDoesntExist)
//-------------------------------------------------------------------
{
	this.root = root;
	this.path = path;
	this.fullAccess = fullAccess;
	this.createIfDoesntExist = createIfDoesntExist;
	this.isRemote = isRemote;
	try{
		isValid = checkValid();
	}catch(Throwable t){
		isValid = true;
	}
}

//===================================================================
public RegistryKey getCopy()
//===================================================================
{
	return new RegistryKey(isRemote,root,path,fullAccess,createIfDoesntExist);
}
//===================================================================
public RegistryKey getSubKey(String subkeyPath)
//===================================================================
{
	RegistryKey key = getCopy();
	if (key.path.length() == 0) key.path = subkeyPath;
	else key.path += "\\"+subkeyPath;
	return key;
}
//===================================================================
public int getRoot() {return root;}
//===================================================================
public String getPath() {return path;}
//===================================================================
public String getFullPath() {return Registry.roots[root]+"\\"+path;}
//===================================================================
/**
* This returns the name of the subkey without the parent path.
**/
//===================================================================
public String getName()
//===================================================================
{
	if (path.length() == 0) return Registry.roots[root];
	int idx = path.lastIndexOf('\\');
	return path.substring(idx+1);
}
//===================================================================
public String toString()
//===================================================================
{
	return getFullPath();
}
/**
* This returns either a String or a byte array, or a ewe.sys.Long (representing a 32-bit value)
* or null. If valueName
* is null or an empty String then the default value will be returned.
**/
//===================================================================
public Object getValue(String valueName)
//===================================================================
{
	if (valueName == null) valueName = "";
	data.value = data.name = null;
	if (!getNamedValue(valueName,data)) return null;
	if (data.value != null) return data.value;
	return new ewe.sys.Long().set(data.intValue);
}
RegistryData data = new RegistryData();
/**
* Get a value at the specified index. The name of the value is placed in the
* valueName StringBuffer. The return value is either a String or a byte array
* or a Long (representing a 32-bit value) or null.
**/
//===================================================================
public Object getValue(int index,StringBuffer valueName)
//===================================================================
{
	data.value = data.name = null;
	if (valueName == null) valueName = new StringBuffer();
	valueName.setLength(0);
	if (!getIndexedValue(index,data)) return null;
	if (data.name != null) valueName.append(data.name);
	if (data.value != null) return data.value;
	return new ewe.sys.Long().set(data.intValue);
}
/**
* Delete a value with the specified name.
**/
//===================================================================
public boolean deleteValue(String name)
//===================================================================
{
	return deleteAValue(name == null ? "" : name);
}
/**
* Delete the entire key and all its subkeys (if possible).
**/
//===================================================================
public boolean delete()
//===================================================================
{
	return deleteAKey();
}
/**
* Set a String value.
**/
//===================================================================
public boolean setValue(String name,String value)
//===================================================================
{
	return setAStringValue(name == null ? "" : name,value == null ? new String() : value);
}
/**
* Set a binary data value.
**/
//===================================================================
public boolean setValue(String name,byte [] value)
//===================================================================
{
	return setABinaryValue(name == null ? "" : name,value == null ? new byte[0] : value);
}
/**
* Set a 32-bit data value in the default little-endian format.
**/
//===================================================================
public boolean setValue(String name,int value)
//===================================================================
{
	return setAnIntValue(name == null ? "" : name,value);
}
/**
* Set a 32-bit data value in either little-endian or big-endian format.
**/
//===================================================================
//public boolean setValue(String name,int value,boolean bigEndian)
//===================================================================
//{
//	return registryKeySetInt(name,value,bigEndian);
//}
//-------------------------------------------------------------------
protected native boolean getIndexedValue(int index,RegistryData data);
protected native boolean getNamedValue(String name,RegistryData data);
protected native boolean setAStringValue(String name,String value);
protected native boolean setABinaryValue(String name,byte [] value);
protected native boolean setAnIntValue(String name,int value);
protected native boolean deleteAValue(String name);
protected native boolean deleteAKey();
protected native boolean checkValid();
//-------------------------------------------------------------------
public native String getSubKey(int index);

/**
* This is an option for getSubKeys(int options).
**/
public static final int SORT_DONT_SORT = 0x1;
/**
* This is an option for getSubKeys(int options).
**/
public static final int SORT_CASE_SENSITIVE = 0x2;
/**
* This is an option for getSubKeys(int options).
**/
public static final int SORT_DESCENDING = 0x4;
/**
* This is an option for getSubKeys(int options) - when used getSubKey() will return
* an array of integers representing the indexes of all the sub-keys.
**/
public static final int GET_INDEXES = 0x8;
/**
/**
* This is an option for getSubKeys(int options) - when used getSubKey() will return
* an array of longs representing the indexes of all the sub-keys.
**/
public static final int GET_INDEXES_AS_LONGS = 0x10;


/**
 * Return an array of Strings or an array of integers or array of longs representing the
 * subkeys of this key.
 * @param options By default this will return an array of sorted Strings. If the GET_INDEXES
 * option is used, then an array of integer indexes (sorted by the sub-key name) will be returned.
 * If the GET_INDEXES_AS_LONGS option isused, then an array of long indexes (sorted by the sub-key name) will be returned.
 & <p>If SORT_DONT_SORT is used then the subkey list returned is not sorted.
 * @return an array of Strings or an array of integers or array of longs representing the
 * subkeys of this key.
 */
public native Object getSubKeys(int options);

public native int getSubKeyCount();
//public native int getValueCount();
//-------------------------------------------------------------------
//##################################################################
}
//##################################################################

//##################################################################
class RegistryData{
//##################################################################
public String name = null;
public Object value = null;
public int intValue;
//##################################################################
}
//##################################################################




