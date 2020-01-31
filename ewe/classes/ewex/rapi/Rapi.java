package ewex.rapi;

//##################################################################
public class Rapi{
//##################################################################

static
{
	try{
		ewe.sys.Vm.loadLibrary("ewex_rapi");
	}catch(NoClassDefFoundError e){
		try{
			System.loadLibrary("ewex_rapi");
		}catch(Throwable t){}
	}
}

public static String error = null;
public static boolean initialize(int timeoutMillis,boolean showErrorMessage)
{
	if (initialize(timeoutMillis)) return true;
	if (!showErrorMessage) return false;
	try{
		new ewe.ui.MessageBox("Error","Could not initialize RAPI\nMake sure your device is connected.",0).execute();
	}catch(Throwable t){}
	return false;
}
public static native boolean initialize(int timeoutMillis);
public static native boolean close();
public static native boolean execute(String path,String parameters);
public static native boolean nativeGetSystemInfo(int [] buff);
//===================================================================
public static SystemInfo getSystemInfo()
//===================================================================
{
	int [] buff = new int[7];
	if (!nativeGetSystemInfo(buff)) return null;
	SystemInfo si = new SystemInfo();
	si.processorArchitecture = buff[0];
	si.processorType = buff[1];
	si.processorLevel = buff[2];
	si.processorRevision = buff[3];
	si.osPlatform = buff[4];
	si.osMajorVersion = buff[5];
	si.osMinorVersion = buff[6];
	return si;
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

//-------------------------------------------------------------------
protected native static String nativeGetSpecialFolder(int folder);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected static String checkFolder(String name,String alternate)
//-------------------------------------------------------------------
{
	RapiFile rf = new RapiFile(name);
	if (rf.isDirectory()) return name;
	return alternate;
}
//===================================================================
public static String getSpecialFolder(int folder)
//===================================================================
{
	if (folder == -1) return "\\Windows";
	String got = nativeGetSpecialFolder(folder);
	if (got != null) got = checkFolder(got,null);
	if (got != null) return got;
	switch(folder){
		case FOLDER_PROGRAMS: return checkFolder("\\Program Files","\\Windows\\Programs");
		case FOLDER_STARTMENU: return checkFolder("\\Windows\\Start Menu","\\Windows\\Programs");
		default: return "\\Windows";
	}
}
//===================================================================
public native static boolean createShortcut(String target,String arguments,String shortcutPath);
//===================================================================

//##################################################################
}
//##################################################################


