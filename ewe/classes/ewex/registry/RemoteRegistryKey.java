package ewex.registry;

//##################################################################
class RemoteRegistryKey extends RegistryKey{
//##################################################################

//-------------------------------------------------------------------
RemoteRegistryKey(boolean isRemote,int root,String path,boolean fullAccess,boolean createIfDoesntExist)
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
	return new RemoteRegistryKey(isRemote,root,path,fullAccess,createIfDoesntExist);
}
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
public native Object getSubKeys(int options);
public native int getSubKeyCount();


//##################################################################
}
//##################################################################

