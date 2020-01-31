package ewex.registry;
import ewe.io.*;
//import ewe.datastore.*;
//import ewe.database.*;
import ewe.util.*;
import ewe.sys.Convert;
import ewe.sys.HandleStoppedException;
/**
* This allows access to keys in the local system registry. It will be extended soon to
* cover accessing keys in a mobile pc registry.
**/
//##################################################################
public class Registry{
//##################################################################

public static double testValue = 1234567.89012;

static boolean initialized = false;
static boolean nativeInitialized = false;
//static String registryFile = null;
static String registryDatabase = null;
static boolean triedFile = false;

static ewe.sys.Lock timeoutLock = new ewe.sys.Lock();
//static ewe.datastore.DataStorage registry;
//static ewe.database.Database registry;
static Object registry;
static ewe.sys.TimeOut accessTimeout;
static boolean closeRegistry = false;

//-------------------------------------------------------------------
static boolean openRegistry()
//-------------------------------------------------------------------
{
	if (true) return false;
 	if (accessTimeout == null) {
		accessTimeout = new ewe.sys.TimeOut(100);
		/*
		new ewe.sys.mThread(){
			public void run(){
				timeoutLock.synchronize(); try{
					while(true){
						try{
							if (closeRegistry) return;
							if (accessTimeout.hasExpired()){
								if (registry != null) {
									//ewe.sys.Vm.debug("Closing.");
									registry.close();
									registry = null;
								}
								timeoutLock.waitOn(ewe.sys.TimeOut.Forever);
							}else{
								timeoutLock.waitOn(accessTimeout);
							}
						}catch(Exception e){
						}

					}
				}finally{
					timeoutLock.release();
				}
			}
		}.start();
	*/
	}
	if (timeoutLock.grab()){
		if (registryDatabase == null) {
			timeoutLock.release();
			return false;
		}
		/*
		if (registry != null) return true; //Will still have the lock when we leave this method.
		try{
			for (int i = 0; i<10; i++){
				try{
					registry = DatabaseManager.initializeDatabase(null,registryDatabase,null);
					if (registry != null){
						//ewe.sys.Vm.debug("Initializing!");
						int pathField = registry.addField("path",Database.STRING);
						int nameField = registry.addField("name",Database.STRING);
						int typeField = registry.addField("type",Database.INTEGER);
						registry.addField("value",Database.STRING);
						int sort = registry.addSort("ByPath",Database.SORT_IGNORE_CASE,pathField,nameField,typeField,0);
						registry.indexBy(null,sort,"ByPath");
						registry.setObjectClass(new RegistryEntry());
						registry.save();
						registry.close();
					}
					registry = DatabaseManager.openDatabase(null,registryDatabase,"rw");
					//ewe.sys.Vm.debug("Opened: "+registry+" at "+registryDatabase);
					accessTimeout.reset();
					return true; //Will still have the lock when we leave this method.
				}catch(ewe.io.IOException e){
					e.printStackTrace();
					//ewe.sys.Vm.debug("Can't open now.");
					ewe.sys.mThread.nap(100);
				}catch(Exception e2){
					e2.printStackTrace();
				}
			}
		}catch(Exception e){}
		timeoutLock.release();
		*/
		return false;
	}else{
		//ewe.sys.Vm.debug("Could not open!");
		return false;
	}
}

//-------------------------------------------------------------------
static void closeRegistry()
//-------------------------------------------------------------------
{
	try{
		try{
			if (registry != null){
				//ewe.sys.Vm.debug("Closing.");
				//FIXME!
				//registry.close();
				//registry = null;
			}
		}finally{
			timeoutLock.notifyAllWaiting();
			timeoutLock.release();
		}
	}catch(Exception e){
		//ewe.sys.Vm.debug(e.getMessage());
	}
}

static {
	nativeInitialized = initialized = loadLibrary("ewex_registry");
}

//-------------------------------------------------------------------
protected static boolean loadLibrary(String libraryName)
//-------------------------------------------------------------------
{
	try{
		boolean ret = ewe.sys.Vm.loadLibrary(libraryName);
		return ret;
	}catch(NoClassDefFoundError e){
		try{
			//System.loadLibrary(libraryName);
			return true;
		}catch(Throwable t){
		}
	}
	return false;
}

public static final int HKEY_CLASSES_ROOT = 1;
public static final int HKEY_CURRENT_USER = 2;
public static final int HKEY_LOCAL_MACHINE = 3;
public static final int HKEY_USERS = 4;
public static final int HKEY_CURRENT_CONFIG = 5;
public static final int HKEY_DYN_DATA = 6;

public static String [] roots  =
{"","HKEY_CLASSES_ROOT","HKEY_CURRENT_USER","HKEY_LOCAL_MACHINE","HKEY_USERS","HKEY_CURRENT_CONFIG","HKEY_DYN_DATA"};

/**
* This converts a path that starts with a string representation of one of the roots to an int value which is one
* of the HKEY_ values. It will return 0 if a valid root was not found.
**/
//===================================================================
public static int toRootAndPath(String path,StringBuffer pathWithoutRoot)
//===================================================================
{
	int idx = path.indexOf('\\');
	if (idx == -1) return 0;
	pathWithoutRoot.append(path.substring(idx+1));
	path = path.substring(0,idx).toUpperCase();
	String hk = "HKEY_";
	if (!path.startsWith(hk)) path = hk+path;
	for (int i = 0; i<roots.length; i++)
		if (roots[i].equals(path)) return i;
	return 0;
}
/**
* Get a key in the local registry. If the path or root specified is invalid for any reason, it will
* return null. If the ewex_registry.dll could not be loaded, it will return null.<p>
* Note that the path MUST be separated by '\' characters and NOT '/' characters.<p>
* If a <b>root</b> of 0 is used, it will be assumed that the path will start with "HKEY_CLASSES_ROOT\..."
* or one of the other text representation of the roots. These roots are:<br>
* "HKEY_CLASSES_ROOT","HKEY_CURRENT_USER","HKEY_LOCAL_MACHINE","HKEY_USERS","HKEY_CURRENT_CONFIG","HKEY_DYN_DATA"
**/
//===================================================================
public static RegistryKey getLocalKey(int root,String path,boolean fullAccess,boolean createIfDoesntExist)
//===================================================================
{
	//
	if (!initialized) return null; //FIXME
	//
	/*
	if (!initialized) {
		if (registryDatabase == null && triedFile) return null;
		else if (registryDatabase == null){
			try{
				File ep = ewe.io.File.getNewFile(ewe.sys.Vm.getPathToEweVM()).getParentFile();
				ep = ep.getChild("Registry"); // Registry.dat for datastore.
				registryDatabase = ep.getAbsolutePath();
				if (!openRegistry()){
					triedFile = true;
					closeRegistry = true;
					timeoutLock.notifyAllWaiting();
					registryDatabase = null;
				}else{
					initialized = true;
					closeRegistry();
				}
			}catch(Exception e){
				triedFile = true;
				return null;
			}
		}
	}
	*/
	if (root == 0) {
		StringBuffer sb = new StringBuffer();
		root = toRootAndPath(path,sb);
		path = sb.toString();
	}
	RegistryKey key =
		//registryDatabase != null ? new FileRegistryKey(false,root,path,fullAccess,createIfDoesntExist):
		new RegistryKey(false,root,path,fullAccess,createIfDoesntExist);
	if (!key.isValid) {
		//ewe.sys.Vm.debug("Key was not valid!",0);
		return null;
	}else{
		//ewe.sys.Vm.debug("Got valid key!",0);
		return key;
	}
}
static boolean remoteLoaded = false;
static boolean remoteInitialized = false;
/**
* Gets a remote key. ewex.Rapi.initialize() must be called first. If the ewex_remote_registry.dll could not
* be loaded, this will return null.<p>If the path or root specified is invalid for any reason, it will
* return null. If the ewex_registry.dll could not be loaded, it will return null.<p>
* Note that the path MUST be separated by '\' characters and NOT '/' characters.<p>
* If a <b>root</b> of 0 is used, it will be assumed that the path will start with "HKEY_CLASSES_ROOT\..."
* or one of the other text representation of the roots. These roots are:<br>
* "HKEY_CLASSES_ROOT","HKEY_CURRENT_USER","HKEY_LOCAL_MACHINE","HKEY_USERS","HKEY_CURRENT_CONFIG","HKEY_DYN_DATA"
**/
//===================================================================
public static RegistryKey getRemoteKey(int root,String path,boolean fullAccess,boolean createIfDoesntExist)
//===================================================================
{
	if (!remoteLoaded){
		remoteLoaded = true;
		remoteInitialized = loadLibrary("ewex_remote_registry");
	}
	if (!remoteInitialized) return null;
	if (root == 0) {
		StringBuffer sb = new StringBuffer();
		root = toRootAndPath(path,sb);
		path = sb.toString();
	}
	RegistryKey key = new RemoteRegistryKey(true,root,path,fullAccess,createIfDoesntExist);
	if (!key.isValid) return null;
	return key;
}

//===================================================================
public static boolean isInitialized(boolean forRemote)
//===================================================================
{
	if (forRemote){
		if (!remoteLoaded){
			remoteLoaded = true;
			remoteInitialized = loadLibrary("ewex_remote_registry");
		}
		return remoteInitialized;
	}
	else
		return initialized;
}

/**
* Returns true if a TRUE native registry is available. False if not.
**/
//===================================================================
public static boolean isNativeInitialized()
//===================================================================
{
	return nativeInitialized;
}
public static final int PLATFORM_WIN32s             = 0;
public static final int PLATFORM_WIN32_WINDOWS      = 1;
public static final int PLATFORM_WIN32_NT           = 2;
public static final int NOT_INITIALIZED_ERROR = -1;

//===================================================================
public static int getPlatform()
//===================================================================
{
	if (!initialized) return NOT_INITIALIZED_ERROR;
	return nativeGetPlatform();
}
public static final int FOLDER_WINDOWS            = -1;
public static final int FOLDER_DESKTOP            = 0x0000;
public static final int FOLDER_PROGRAMS           = 0x0002;
public static final int FOLDER_CONTROLS           = 0x0003;
public static final int FOLDER_PRINTERS           = 0x0004;
public static final int FOLDER_PERSONAL           = 0x0005;
public static final int FOLDER_FAVORITES          = 0x0006;
public static final int FOLDER_STARTUP            = 0x0007;
public static final int FOLDER_RECENT             = 0x0008;
public static final int FOLDER_SENDTO             = 0x0009;
public static final int FOLDER_BITBUCKET          = 0x000a;
public static final int FOLDER_STARTMENU          = 0x000b;
public static final int FOLDER_DESKTOPDIRECTORY   = 0x0010;
public static final int FOLDER_DRIVES             = 0x0011;
public static final int FOLDER_NETWORK            = 0x0012;
public static final int FOLDER_NETHOOD            = 0x0013;
public static final int FOLDER_FONTS		 					 = 0x0014;
public static final int FOLDER_TEMPLATES          = 0x0015;
//===================================================================

//-------------------------------------------------------------------
private native static String nativeGetSpecialFolder(int folder);
//-------------------------------------------------------------------

//===================================================================
public static String getSpecialFolder(int folder)
//===================================================================
{
	String got = nativeGetSpecialFolder(folder);
	if (got != null) return got;
	String windows = nativeGetSpecialFolder(FOLDER_WINDOWS);
	if (windows == null){
		if (getPlatform() == PLATFORM_WIN32_NT){
			windows = "C:\\Winnt";
			if (!new File(windows).exists()) windows = "C:\\Windows";
		}else
			windows = "C:\\Windows";
	}
	if (getPlatform() == PLATFORM_WIN32_NT){
		switch(folder){
			case FOLDER_STARTMENU:
				String ret = windows+"\\Profiles\\All Users\\Start Menu";
				if (!new File(ret).exists()) ret = "C:\\Documents and Settings\\All Users\\Start Menu";
				return ret;
			case FOLDER_PROGRAMS:
				return "C:\\Program Files";
		}
	}else{
		switch(folder){
			case FOLDER_STARTMENU:
				return windows+"\\Start Menu";
			case FOLDER_PROGRAMS:
				return "C:\\Program Files";
		}
	}
	return null;
}
//-------------------------------------------------------------------
private native static int nativeGetPlatform();
//-------------------------------------------------------------------
//===================================================================
public native static boolean createShortcut(String target,String arguments,String shortcutPath);
//===================================================================

/**
* An option for associateFile().
**/
public static final int ASSOCIATE_EWE_APPLICATION = 0x1;
/**
* An option for associateFile().
**/
public static final int ASSOCIATE_ICON_IS_RESOURCE = 0x2;

//-------------------------------------------------------------------
private static RegistryKey getClassesRoot(String name) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	RegistryKey key = getLocalKey(HKEY_CLASSES_ROOT,name,true,true);
	if (key == null) throw new ewe.io.IOException("Could not create registry key.");
	return key;
}
//-------------------------------------------------------------------
private static void checkValue(boolean value) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	if (!value) throw new ewe.io.IOException("Could not set registry value.");
}

//-------------------------------------------------------------------
private static String quotedEweVMPath(String commandLine) throws ewe.io.IOException
//-------------------------------------------------------------------
{
	String path = ewe.sys.Vm.getPathToEweVM();
	if (path == null) throw new ewe.io.IOException("Could not get path to Ewe VM");
	if (!(path.charAt(0) == '"')) path = "\""+path+"\"";
	if (commandLine != null) commandLine = path+" "+commandLine;
	else commandLine = path;
	return File.toSystemDependantPath(commandLine);
}
/**
 * This associates files with a certain extension to a particular application.
 * @param extension This is the file extension (e.g. ".pnf")
 * @param shortFileDescription This is a mandatory short file description (e.g. "JewelFile");
 * @param fileDescription This is an optional long file description (e.g. "Jewel Program Specs")
 * @param commandLine This is the command line to execute - it will usually include "%1" (including the quotes)
	within it to indicate where the file name gets inserted into the command line passed to the application. If you want to execute a .ewe file,
	then set the command line to point to the .ewe file (in quotes) and then set options to
	be ASSOCIATE_EWE_APPLICATION.
 * @param icon The file name of an optional icon to associate with files of this extension.
 * @param options Can be any of the following values ORed together: <br>
	<b>ASSOCIATE_ICON_IS_RESOURCE</b> - this indicates that the specified icon resides as a resource
	with the application and should be extracted and saved to the file system if necessary.
	If this is not selected then the icon is assumed to be an absoulte file name.<br>
  <b>ASSOCIATE_EWE_APPLICATION</b> - this indicates that the Ewe VM should be used to start
	the application.
 * @exception ewe.io.IOException If an error occurs saving the registry information.
 */
//===================================================================
public static void associateFile(String extension,String shortFileDescription,String fileDescription,String commandLine,String icon,int options)
throws ewe.io.IOException
//===================================================================
{
	RegistryKey key;
	if (extension.charAt(0) != '.') extension = "."+extension;
	if (shortFileDescription == null) throw new IllegalArgumentException();
	extension = extension.toLowerCase();
	checkValue(getClassesRoot(extension).setValue(null,shortFileDescription));
	key = getClassesRoot(shortFileDescription);
	if (fileDescription != null) checkValue(key.setValue(null,fileDescription));
	if (icon != null){
		if ((options & ASSOCIATE_ICON_IS_RESOURCE) != 0){
			icon = icon.replace('\\','/');
			String iconName = icon;
			int last = icon.lastIndexOf("/");
			if (last != 0) iconName = icon.substring(last+1);
			try{
				File outFile = File.getNewFile(File.getProgramDirectory()).getChild(iconName);
				if (!outFile.exists()){
					Stream in = ewe.sys.Vm.openRandomAccessStream(icon,"r");
					Stream out = outFile.toWritableStream(false);
					new IOTransfer().transfer(in,out);
					in.close();
					out.close();
				}
				icon = outFile.getAbsolutePath();
			}catch(RuntimeException e){
				throw new ewe.io.IOException("Could not access or save icon.");
			}
		}
		checkValue(getClassesRoot(shortFileDescription+"\\DefaultIcon").setValue(null,"\""+File.toSystemDependantPath(icon)+"\""));
	}
	if (commandLine != null){
		if ((options & ASSOCIATE_EWE_APPLICATION) != 0)
			commandLine = quotedEweVMPath(commandLine);
		checkValue(getClassesRoot(shortFileDescription+"\\Shell\\Open\\Command").setValue(null,commandLine));
	}
}
/**
 * This associates files with a certain extension to the running Ewe application. The running
 * application should be run from a ".ewe" file or from a ".exe" file created from a ".ewe" file.
 * @param extension This is the file extension (e.g. ".pnf")
 * @param shortFileDescription This is a mandatory short file description (e.g. "JewelFile");
 * @param fileDescription This is an optional long file description (e.g. "Jewel Program Specs")
 * @param arguments These are the arguments to pass to the application - it will usually include "%1" (including the quotes)
	within it to indicate where the file name gets inserted into the command line passed to the application.
 * @param icon The file name of an optional icon to associate with files of this extension.
 * @param options Can be any of the following values ORed together: <br>
	<b>ASSOCIATE_ICON_IS_RESOURCE</b> - this indicates that the specified icon resides as a resource
	with the application and should be extracted and saved to the file system if necessary.
	If this is not selected then the icon is assumed to be an absolute file name.<br>
 * @exception ewe.io.IOException If an error occurs saving the registry information.
 */
//===================================================================
public static void associateFileWithMe(String extension,String shortFileDescription,String fileDescription,String arguments,String icon,int options)
throws ewe.io.IOException
//===================================================================
{
	options &= ~ASSOCIATE_EWE_APPLICATION;
	boolean isEwe = false;
	//
	// NOTE: the Vm.getProperty() method will return null for this call IF
	// the path of this exe matches the path of the Ewe VM exe.
	// In that case we will assume it is running from a .ewe file.
	//
	String exe = ewe.sys.Vm.getProperty("this.exe.path",null);
	if (exe == null){
		isEwe = true;
		options |= ASSOCIATE_EWE_APPLICATION;
		exe = ewe.sys.Vm.getProperty("this.ewe.path",null);
		if (exe == null) throw new ewe.io.IOException("The application is not being run in a ewe or exe file.");
	}else
		exe = File.toSystemDependantPath(exe);
	exe = "\""+exe+"\"";
	if (arguments != null) exe += " "+arguments;
	else exe += " \"%1\"";
	//ewe.sys.Vm.debug("Associate: "+exe);
	associateFile(extension,shortFileDescription,fileDescription,exe,icon,options);
}
/**
 * This associates files with a certain extension to the running Ewe application. The running
 * application should be run from a ".ewe" file or from a ".exe" file created from a ".ewe" file.
 * @param extension This is the file extension (e.g. ".pnf")
 * @param shortFileDescription This is a mandatory short file description (e.g. "JewelFile");
 * @param fileDescription This is an optional long file description (e.g. "Jewel Program Specs")
 * @param arguments These are the arguments to pass to the application - it will usually include "%1" (including the quotes)
	within it to indicate where the file name gets inserted into the command line passed to the application.
 * @param icon The file name of an optional icon to associate with files of this extension.
 * @param options Can be any of the following values ORed together: <br>
	<b>ASSOCIATE_ICON_IS_RESOURCE</b> - this indicates that the specified icon resides as a resource
	with the application and should be extracted and saved to the file system if necessary.
	If this is not selected then the icon is assumed to be an absoulte file name.<br>
 * @exception ewe.io.IOException If an error occurs saving the registry information.
 */
//===================================================================
public static void associateFileWithMe(String extension,String shortFileDescription,String fileDescription,String icon)
throws ewe.io.IOException
//===================================================================
{
	associateFileWithMe(extension,shortFileDescription,fileDescription,null,icon,ASSOCIATE_ICON_IS_RESOURCE);
}
//public native static Object nativeTest(Object who);
//===================================================================
/**
* Create an absolute command line to execute the Ewe VM on my Ewe file.
* @param myEweFile the name (without a path specification) of the Ewe file this application
* is packaged in. If it is null the VM will attempt to lookup the name of the currently running
* .ewe file.
* @param extraArguments Additional application arguments - can be null.
* @param vmArguments Additional VM arguments - can be null.
* @param includePathToVM Set this true to include the path to the Ewe VM.
* @return An absolute command line for executing the currently running application.
* @exception ewe.io.IOException if there is a problem with any of the data.
*/
//===================================================================
public static String getEweCommandLine(String myEweFile,String extraArguments,String vmArguments,boolean includePathToVM)
throws ewe.io.IOException
//===================================================================
{
	String myName = null;
	if (myEweFile != null){
		ewe.io.File pd = ewe.io.File.getNewFile(ewe.io.File.getProgramDirectory()).getChild(myEweFile);
		myName = "\""+pd.getAbsolutePath()+"\"";
	}else{
		myEweFile = ewe.sys.Vm.getProperty("this.ewe.path",null);
		if (myEweFile == null) throw new IOException("Cannot determine Ewe file.");
		myName = "\""+myEweFile+"\"";
	}
	if (vmArguments != null) myName = vmArguments+" "+myName;
	if (extraArguments != null) myName += " "+extraArguments;
	if (includePathToVM) return quotedEweVMPath(myName);
	else return myName;
}
/**
* Create an absolute command line to execute the Ewe VM on my Ewe file.
* @param extraArguments Additional application arguments - can be null.
* @param vmArguments Additional VM arguments - can be null.
* @param includePathToVM Set this true to include the path to the Ewe VM.
* @return An absolute command line for executing the currently running application.
* @exception ewe.io.IOException if there is a problem with any of the data.
*/
//===================================================================
public static String getEweCommandLineToMe(String extraArguments,String vmArguments,boolean includePathToVM)
throws ewe.io.IOException
//===================================================================
{
	return getEweCommandLine(null,extraArguments,vmArguments,includePathToVM);
}

//##################################################################
}
//##################################################################

/*
//##################################################################
class FileRegistryKey extends RegistryKey{
//##################################################################

//-------------------------------------------------------------------
FileRegistryKey(boolean isRemote,int root,String path,boolean fullAccess,boolean createIfDoesntExist)
//-------------------------------------------------------------------
{
	this.root = root;
	this.path = path;
	this.fullAccess = fullAccess;
	this.createIfDoesntExist = createIfDoesntExist;
	this.isRemote = isRemote;

	this.isValid = checkValid();
}

private DataEntry myEntry;
private DataEntryData myData;
private TextDecoder myEncodedData;

//-------------------------------------------------------------------
private DataEntry toDataEntry()
//-------------------------------------------------------------------
{
	myEntry = null;
	myData = null;
	myEncodedData = null;
	String path = Registry.roots[root]+"\\"+this.path;
	if (!Registry.openRegistry()) return null;
	else try{
		DataEntry root = Registry.registry.getRootEntry();
		myEntry = root.find(path.replace('/','_').replace('\\','/'),createIfDoesntExist);
		if (myEntry == null) throw new NullPointerException();
		myData = myEntry.getData();
		if (myData != null)
			myEncodedData = new TextDecoder(myData.getFieldString(Database.OBJECT_TEXT_FIELD,""));
		else
			myEncodedData = new TextDecoder("");
		return myEntry;
	}catch(Exception e){
		Registry.closeRegistry();
		return null;
	}
}

//-------------------------------------------------------------------
private boolean update()
//-------------------------------------------------------------------
{
	try{
		String updated = myEncodedData.encode();
		myData.setFieldValue(Database.OBJECT_TEXT_FIELD,updated);
		myData.save();
		//ewe.sys.Vm.debug(updated);
		return true;
	}catch(Exception e){
		return false;
	}
}
//-------------------------------------------------------------------
private boolean updateAndSave(String name,String value)
//-------------------------------------------------------------------
{
	if (name == null) name = "";
	if (value == null) value = "";
	try{
		//ewe.sys.Vm.debug("Setting: "+name+" to be "+value);
		myEncodedData.setValue(name,value);
		return update();
	}catch(Exception e){
		return false;
	}
}
//-------------------------------------------------------------------
protected  boolean getIndexedValue(int index,RegistryData data)
//-------------------------------------------------------------------
{
	DataEntry key = toDataEntry();
	if (key == null) return false;
	else try{
		String value = myEncodedData.getValue(index);
		if (value == null) return false;
		String name = myEncodedData.getName(index);
		if (name == null) name = "";
		if (name.endsWith("$i")){
			data.name = name.substring(0,name.length()-2);
			data.intValue = ewe.sys.Convert.toInt(value);
		}else{
			data.name = name;
			data.value = value;
		}
		return true;
	}catch(Exception e){
		return false;
	}finally{
		Registry.closeRegistry();
	}
}
//-------------------------------------------------------------------
protected boolean getNamedValue(String name,RegistryData data)
//-------------------------------------------------------------------
{
	DataEntry key = toDataEntry();
	if (key == null) return false;
	else try{
		try{
			String value = myEncodedData.getValue(name);
			if (value == null) {
				value = myEncodedData.getValue(name+"$i");
				if (value != null){
					data.intValue = ewe.sys.Convert.toInt(value);
					return true;
				}else
					return false;
			}else{
				data.value = value;
			}
		}catch(Exception e){
			return false;
		}
		return true;
	}finally{
		Registry.closeRegistry();
	}
}

//-------------------------------------------------------------------
protected boolean setAStringValue(String name,String value)
//-------------------------------------------------------------------
{
	DataEntry key = toDataEntry();
	if (key == null) return false;
	else try{
		return updateAndSave(name,value);
	}finally{
		Registry.closeRegistry();
	}
}
//-------------------------------------------------------------------
protected boolean setABinaryValue(String name,byte [] value)
//-------------------------------------------------------------------
{
	if (true) return false;
	try{
		DataEntry key = toDataEntry();
		if (key == null)
			return false;
		return true;
	}finally{
		Registry.closeRegistry();
	}
}
//-------------------------------------------------------------------
protected boolean setAnIntValue(String name,int value)
//-------------------------------------------------------------------
{
	return setAStringValue(name+"$i",""+value);
}
//-------------------------------------------------------------------
protected boolean deleteAValue(String name)
//-------------------------------------------------------------------
{
	DataEntry key = toDataEntry();
	if (key == null) return false;
	else try{
		if (name == null) name = "";
		myEncodedData.deleteValue(name);
		return update();
	}catch(Exception e){
		return false;
	}
}
//-------------------------------------------------------------------
protected boolean deleteAKey()
//-------------------------------------------------------------------
{
	DataEntry key = toDataEntry();
	if (key == null) return false;
	else try{
		try{
			return key.delete();
		}catch(Exception e){
			return false;
		}
	}finally{
		Registry.closeRegistry();
	}
}
//-------------------------------------------------------------------
protected boolean checkValid()
//-------------------------------------------------------------------
{
	DataEntry key = toDataEntry();
	if (key == null) return false;
	else try{
		return true;
	}finally{
		Registry.closeRegistry();
	}
}

//===================================================================
public String getSubKey(int index)
//===================================================================
{
	DataEntry key = toDataEntry();
	if (key == null) return null;
	else try{
		try{
			int i = 0;
			for (DataEntry de = key.getFirstChild(); de != null; de = de.getNext()){
				if (i == index) return de.getName();
				else i++;
			}
			return null;
		}catch(Exception e){
			return null;
		}
	}finally{
		Registry.closeRegistry();
	}
}

//===================================================================
public Object getSubKeys(int options)
//===================================================================
{
	DataEntry key = toDataEntry();
	if (key == null) return null;
	else try{
		String [] names = null;
		Vector v = new Vector();
		int num = 0;
		//
		boolean sorted = (options & SORT_DONT_SORT) == 0;
		boolean indexes = (options & (GET_INDEXES_AS_LONGS|GET_INDEXES)) != 0;
		boolean keepNames = sorted || !indexes;
		int sort = 0;
		if ((options & SORT_CASE_SENSITIVE) == 0) sort |= ewe.sys.Locale.IGNORE_CASE;
		//
		DataEntry de = key.getFirstChild();
		for (num = 0; de != null; num++){
			if (keepNames) v.add(de.getName());
			de = de.getNext();
		}
		//
		if (keepNames) {
			names = new String[num];
			v.copyInto(names);
			v.clear();
		}
		//
		int [] ids = new int[num];
		ewe.util.Utils.getIntSequence(ids,0);
		if (sorted){
			boolean descending = ((options & SORT_DESCENDING) != 0);
			ewe.util.Utils.sort(ids,ids.length,new CompareArrayElements(names,ewe.sys.Vm.getLocale().getStringComparer(sort)),descending);
			Object [] newVals = new Object[names.length];
			for (int i = 0; i<num; i++) newVals[i] = names[ids[i]];
			ewe.sys.Vm.copyArray(newVals,0,names,0,num);
		}
		//
		if (!indexes) return names;
		//
		if ((options & GET_INDEXES_AS_LONGS) != 0){
			long [] ret = new long[num];
			for (int i = 0; i<num; i++)
				ret[i] = ids[i];
			return ret;
		}else{
			return ids;
		}
	}catch(Exception e){
		return null;
	}finally{
		Registry.closeRegistry();
	}
}
//===================================================================
public int getSubKeyCount()
//===================================================================
{
	DataEntry key = toDataEntry();
	if (key == null) return 0;
	else try{
		try{
			int i = 0;
			for (DataEntry de = key.getFirstChild(); de != null; de = de.getNext())
				i++;
			return i;
		}catch(Exception e){
			return 0;
		}
	}finally{
		Registry.closeRegistry();
	}
}
//===================================================================
public RegistryKey getCopy()
//===================================================================
{
	return new FileRegistryKey(isRemote,root,path,fullAccess,createIfDoesntExist);
}

//##################################################################
}
//##################################################################
*/
/*
//##################################################################
class FileRegistryKey extends RegistryKey{
//##################################################################
public String name = "";
public String savedData = "";
public int type = 0;

public static final int TYPE_NODE = 0;
public static final int TYPE_STRING = 1;
public static final int TYPE_INTEGER = 2;
public static final int TYPE_BYTE_ARRAY = 3;

//-------------------------------------------------------------------
String stripSlashes(String path)
//-------------------------------------------------------------------
{
	while (path.startsWith("\\")) path = path.substring(1);
	while (path.endsWith("\\")) path = path.substring(0,path.length()-1);
	return path;
}
//-------------------------------------------------------------------
FileRegistryKey(boolean isRemote,int root,String path,boolean fullAccess,boolean createIfDoesntExist)
//-------------------------------------------------------------------
{
	this.root = root;
	this.path = stripSlashes(path);
	this.fullAccess = fullAccess;
	this.createIfDoesntExist = createIfDoesntExist;
	this.isRemote = isRemote;
	this.isValid = checkValid();
}

//private DatabaseEntry myEntry;

//===================================================================
public RegistryKey getCopy()
//===================================================================
{
	return new FileRegistryKey(isRemote,root,path,fullAccess,createIfDoesntExist);
}
//-------------------------------------------------------------------
private Database openRegistry()
//-------------------------------------------------------------------
{
	if (!Registry.openRegistry()) return null;
	return Registry.registry;
}
//-------------------------------------------------------------------
private void closeRegistry()
//-------------------------------------------------------------------
{
	Registry.closeRegistry();
}
//-------------------------------------------------------------------
private DatabaseEntry getEntryFor(String fullPath)
//-------------------------------------------------------------------
{
	Database db = openRegistry();
	if (db == null) return null;
	try{
		int sort = db.findSort("ByPath");
		FoundEntries fe = db.getFoundEntries(null,sort,new EntrySelector(db,fullPath,sort,false));
		if (fe.size() != 0) return fe.get(0);
		if (createIfDoesntExist) return fe.getNew();
		return null;
	}catch(IOException e){
		return null;
	}
}
//-------------------------------------------------------------------
private String toFullPath(String name)
//-------------------------------------------------------------------
{
	String full = path;
	if (root != 0) {
		full = Registry.roots[root];
		if (path != null && path.length() != 0)
			full += "\\"+path;
	}
	full += "\\"+(name == null ? "" : stripSlashes(name));
	return full;
}
//-------------------------------------------------------------------
private String toFullPath()
//-------------------------------------------------------------------
{
	String full = path;
	if (root != 0) {
		full = Registry.roots[root];
		if (path != null && path.length() != 0)
			full += "\\"+path;
	}
	return full;
}
//-------------------------------------------------------------------
protected boolean setAnIntValue(String name,int value)
//-------------------------------------------------------------------
{
	return setAValue(name,Convert.toString(value),TYPE_INTEGER);
}
//-------------------------------------------------------------------
protected boolean setAStringValue(String name,String value)
//-------------------------------------------------------------------
{
	return setAValue(name,value,TYPE_STRING);
}

//-------------------------------------------------------------------
private FoundEntries getEntries()
//-------------------------------------------------------------------
{
	Database db = openRegistry();
	if (db == null) return null;
	try{
		int sort = db.findSort("ByPath");
		return db.getFoundEntries(null,sort);
	}catch(IOException e){
		return null;
	}
}
//-------------------------------------------------------------------
protected boolean setAValue(String name, String textValue, int type)
//-------------------------------------------------------------------
{
	String full = toFullPath();
	name = stripSlashes(name);
	try{
		FoundEntries fe = getEntries();
		if (fe == null) return false;
		int first = fe.findFirst(null,new Object[]{full,name},false);
		int last = fe.findLast(null,new Object[]{full,name},false);
		DatabaseEntry de = null;
		RegistryEntry re = new RegistryEntry();
		for (int i = first; i >= 0 && i<=last; i++){
			fe.getData(i,re);
			if (re.type == TYPE_NODE) continue;
			de = fe.get(i);
			break;
		}
		if (de == null) de = fe.getNew();
		re.path = full;
		re.name = name;
		re.value = textValue;
		re.type = type;
		de.setData(re);
		try{
			de.save();
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}
	}catch(Exception e){
		e.printStackTrace();
		return false;
	}finally{
		closeRegistry();
	}
	return true;
}

//-------------------------------------------------------------------
private String[] getSubKeys() throws IOException
//-------------------------------------------------------------------
{
	try{
		String mask = toFullPath("*");
		FoundEntries fe = getEntries();
		int first = fe.findFirst(null,mask,true);
		if (first == -1) return new String[0];
		int last = fe.findLast(null,mask,true);
		Vector v = new Vector();
		RegistryEntry re = new RegistryEntry();
		int lookFrom = mask.length()-1;
		for (int i = first; i <= last; i++){
			fe.getData(i,re);
			if (re.path.indexOf('\\',lookFrom) != -1) continue;
			if (re.type != TYPE_NODE) continue;
			v.add(re.path.substring(lookFrom));
		}
		String[] ret = new String[v.size()];
		v.copyInto(ret);
		return ret;
	}catch(HandleStoppedException e){
		return new String[0];
	}finally{
		closeRegistry();
	}
}
//===================================================================
public int getSubKeyCount()
//===================================================================
{
	try{
		return getSubKeys().length;
	}catch(IOException e){
		return 0;
	}
}
//===================================================================
public String getSubKey(int index)
//===================================================================
{
	try{
		return getSubKeys()[index];
	}catch(Exception e){
		return null;
	}
}
//===================================================================
public Object getSubKeys(int options)
//===================================================================
{
	try{
		boolean indexes = (options & (GET_INDEXES_AS_LONGS|GET_INDEXES)) != 0;
		String[] all = getSubKeys();
		int num = all.length;
		int [] ids = new int[num];
		if ((options & SORT_DESCENDING) != 0)
			ewe.util.Utils.getIntSequence(ids,0,num-1,-1,num);
		else
			ewe.util.Utils.getIntSequence(ids,0);
		//
		if (!indexes) return all;
		//
		if ((options & GET_INDEXES_AS_LONGS) != 0){
			long [] ret = new long[num];
			for (int i = 0; i<num; i++)
				ret[i] = ids[i];
			return ret;
		}else{
			return ids;
		}
	}catch(Exception e){
		return null;
	}
}
//-------------------------------------------------------------------
protected boolean checkValid()
//-------------------------------------------------------------------
{
	String path = toFullPath();
	try{
		FoundEntries fe = getEntries();
		if (fe == null) return false;
		int last = -1;
		RegistryEntry re = new RegistryEntry();
		while(true){
			last = path.indexOf('\\',last+1);
			String toCheck = last == -1 ? path : path.substring(0,last);
			try{
				int got = fe.findFirst(null,new Object[]{toCheck,""},false);
				if (got == -1){
					//Not found - so add if necessary.
					if (!createIfDoesntExist) return false;
					DatabaseEntry de = fe.getNew();
					re.path = toCheck;
					re.name = re.value = "";
					re.type = TYPE_NODE;
					de.setData(re);
					fe.add(de);
				}
			}catch(HandleStoppedException e){}
			if (last == -1) return true;
		}
	}catch(IOException e){
		return false;
	}finally{
		closeRegistry();
	}
}
//-------------------------------------------------------------------
private EntriesView getValues(String name) throws IOException
//-------------------------------------------------------------------
{
	FoundEntries fe = getEntries();
	EntriesView ev = fe.getEmptyView();
	Object search = toFullPath();
 	if (name != null) search = new Object[]{search,name};
	ev.search(null,search,false);
	if (ev.size() > 0){
		RegistryEntry re = new RegistryEntry();
		ev.getData(0,re);
		if (re.type == TYPE_NODE) ev.excludeAt(0);
	}
	return ev;
}
//-------------------------------------------------------------------
protected boolean getIndexedValue(int index,RegistryData data)
//-------------------------------------------------------------------
{
	try{
		EntriesView ev = getValues(null);
		if (index >= ev.size() || index < 0)
			return false;
		RegistryEntry re = new RegistryEntry();
		ev.getData(index,re);
		data.name = re.name;
		if (re.type == TYPE_INTEGER)
			data.intValue = ewe.sys.Convert.toInt(re.value);
		else
			data.value = re.value;
		return true;
	}catch(Exception e){
		return false;
	}finally{
		closeRegistry();
	}
}
//-------------------------------------------------------------------
protected boolean getNamedValue(String name,RegistryData data)
//-------------------------------------------------------------------
{
	try{
		EntriesView ev = getValues(name);
		if (ev.size() == 0) return false;
		RegistryEntry re = new RegistryEntry();
		ev.getData(0,re);
		data.name = re.name;
		if (re.type == TYPE_INTEGER)
			data.intValue = ewe.sys.Convert.toInt(re.value);
		else
			data.value = re.value;
		return true;
	}catch(Exception e){
		return false;
	}finally{
		closeRegistry();
	}
}
//##################################################################
}
//##################################################################
*/

