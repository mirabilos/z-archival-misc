package ewex.rapi;
import ewe.io.*;
import ewe.sys.Time;
//##################################################################
public class RapiFile extends FileAdapter{
//##################################################################

String rapiFileNativeCreate(String fullPath) {return fullPath;}


//===================================================================
public native boolean exists();
//===================================================================
public native boolean isDirectory();
//===================================================================
public native boolean createDir();
//===================================================================
public native int getLength();
//===================================================================
//===================================================================
public native boolean delete();
//===================================================================
/*
//===================================================================
public String [] list(String mask,int listAndSortOptions)
//===================================================================
{
	int got = startFind(mask,listAndSortOptions);
	if (got == 0) return null;
	ewe.util.Vector v = new ewe.util.Vector();
	//ewe.util.FileComparer fc = new ewe.util.FileComparer(this,ewe.sys.Vm.getLocale(),listAndSortOptions,mask);
	while(true){
		String s = findNext(got);
		if (s == null) break;
		//if (!fc.accept(this,s)) continue;
		v.add(s);
		if ((listAndSortOptions & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) != 0) break;
	}
	endFind(got);
	if ((listAndSortOptions & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) != 0)
		if (v.size() == 0) return null;
	else
		return new String[0];
	String [] ret = new String[v.size()];
	v.copyInto(ret);
	//ewe.util.Utils.sort(ret,fc,((listAndSortOptions & LIST_DESCENDING) != 0));
	return ret;
}
*/

//-------------------------------------------------------------------
protected native int startFind(String mask,int options);
protected int startFind(String mask) {return startFind(mask,0);}
protected native Object findNext(int search);
protected native void endFind(int search);
//-------------------------------------------------------------------

//===================================================================
public void set(File parent,String file)
//===================================================================
{
/*
	file = file.replace('\\','/');
	if (parent != null) file = ewe.util.mString.removeTrailingSlash(parent.getFullPath()"/"+file;
	if (file.length() == 0) file = "/";
	else {
		if (file.charAt(0) != '/') file = "/"+file;
		if (file.length() != 1)
			file = ewe.util.mString.removeTrailingSlash(file);
	}
*/
	setFullPathName(parent,file);
	name = rapiFileNativeCreate(name);
}
//===================================================================
public RapiFile(String file) {this(null,file);}
//===================================================================

//===================================================================
public RapiFile(File parent,String file)
//===================================================================
{
	set(parent,file);
}
//===================================================================
public File getNew(File parent,String file)
//===================================================================
{
	return new RapiFile(parent,file);
}
//===================================================================
public Object getInfo(int value,Object source,Object resultDestination,int options)
//===================================================================
{
	switch(value){
			case INFO_DEVICE_NAME:
				return "Mobile Device";
			case INFO_FLAGS:
				if (!(resultDestination instanceof ewe.sys.Long))
					resultDestination = new ewe.sys.Long();
				((ewe.sys.Long)resultDestination).value = FLAG_SLOW_ACCESS;
				return resultDestination;
	}
	return null;
}

//-------------------------------------------------------------------
protected native long getSetModifiedTime(long value,boolean doGet);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected  void getSetModified(Time time,boolean doGet)
//-------------------------------------------------------------------
{
	if (doGet) time.setEncodedTime(getSetModifiedTime(0,true));
	else getSetModifiedTime(time.getEncodedTime(),false);
}
//===================================================================
public String getFullPath() {return name;}
//===================================================================

/**
* This returns a Stream which can be used for Input. If the file does not
* exist it will return null.
**/
//===================================================================
public Stream toReadableStream() throws IOException
//===================================================================
{
	boolean canOpen = exists() && !isDirectory();
	if (canOpen) {
		Stream ret = new RapiInputStream(getFullPath());
		if (ret.isOpen()) return ret;
	}
	throw new IOException("Can't read from: "+this);
}
/**
* This returns a Stream which can be used for Output. This always overwrites
* any existing file and will always create one if it does not exist.
**/
//===================================================================
public Stream toWritableStream(boolean append) throws IOException
//===================================================================
{
	Stream ret = new RapiOutputStream(getFullPath());
	if (ret.isOpen()) return ret;
	throw new IOException("Can't write to: "+this);
}

//##################################################################
}
//##################################################################

//##################################################################
abstract class RapiStream extends StreamObject implements OverridesClose{
//##################################################################
public String fileName;
protected int handle;

public boolean flushStream() throws ewe.io.IOException {return true;}

//===================================================================
RapiStream(String name)
//===================================================================
{
	fileName = name;
	nativeCreate(name);
}

abstract boolean nativeCreate(String name);
public native boolean close();
public native boolean isOpen();

//##################################################################
}
//##################################################################

//##################################################################
class RapiInputStream extends RapiStream{
//##################################################################

//===================================================================
RapiInputStream(String name) {super(name);}
//===================================================================
//===================================================================
public int nonBlockingWrite(byte [] dest,int offset,int num) {return -1;}
//===================================================================

protected native boolean nativeCreate(String name);
public native int nonBlockingRead(byte [] dest,int offset,int num);

//##################################################################
}
//##################################################################
//##################################################################
class RapiOutputStream extends RapiStream{
//##################################################################

//===================================================================
RapiOutputStream(String name) {super(name);}
//===================================================================
//===================================================================
public int nonBlockingRead(byte [] dest,int offset,int num) {return -1;}
//===================================================================

protected native boolean nativeCreate(String name);
public native int nonBlockingWrite(byte [] dest,int offset,int num);

//##################################################################
}
//##################################################################

