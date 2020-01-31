/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is free software; you can redistribute      *
 *  it and/or modify it under the terms of the Amended GNU Lesser General        *
 *  Public License distributed with this software.                               *
 *                                                                               *
 *  Under this license, linking this library or part thereof with other files to *
 *  produce an executable does not in itself require the executable to be        *
 *  covered by the GNU Lesser General Public License.                            *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  You should have received a copy of the License along with this software;     *
 *  if not, please downloadoad it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.io;
import ewe.sys.Time;
import ewe.sys.Handle;
import ewe.reflect.Type;
import ewe.ui.Form;
/**
 * File is a file or directory (similar to the Java File) - it is not used for I/O.
 * Use RandomAccessFile for file I/O.
 * <p>
 * The File class will not work under the PalmPilot since it does not
 * contain a filesystem.
 * <p>
 */
public class File extends FileBase
{
java.io.File jfile;

//-------------------------------------------------------------------
protected File(){}
//-------------------------------------------------------------------

//===================================================================
public File(java.io.File jf)
//===================================================================
{
	jfile = jf;
	name = jfilegetAbsolutePath();
}
//===================================================================
public File(String path) {this((String)null,path);}
//===================================================================
//===================================================================
public File(File directory,String path)
//===================================================================
{
	if (directory == null && path == null) return;
	name = _nativeCreate(directory,path);
	try{jfile = new java.io.File(name);}catch(Throwable t){}
}
/**
 * Create a File Object which represents a file on the file system under the
 * specified parent directory. The resulting file may be a file or directory
 * which may or may not exist.
 *
 * The parent directory MAY be null - in which case the path must specify the file fully.
 *
 * Please note that under WindowsCE there is no concept of "Current Working Directory".
 * All file names must have a full path. If you want a file or directory to co-exist within
 * the same directory that your ewe program was installed by the user, then use the call:
 *
 * String pd = File.getProgramDirectory();
 * File programDirectory = new File(pd);
 *
 * Then use programDirectory as the parent directory for your data files/directories.
 */
//===================================================================
public File(String parent,String path)
//===================================================================
{
	this(parent == null ? null : new File(parent),path);
}

//-------------------------------------------------------------------
protected String jfilegetAbsolutePath()
//-------------------------------------------------------------------
{
	String s = jfile.getAbsolutePath().replace('\\','/');
	if (name.length() == 0 && (s.endsWith("//")))
		s = s.substring(0,s.length()-1);
	return s;
}
/**
* Modifies the File to point to a different file on the file system. This
* means that ewe.io.File objects are not immutable, but this avoids a lot
* of object creation when dealing with large directories.
**/
//===================================================================
public void set(File directory,String path)
//===================================================================
{
	name = _nativeCreate(directory,path);
	jfile = new java.io.File(name);
	String s = jfilegetAbsolutePath();
}
//-------------------------------------------------------------------
protected File getNewInstance()
//-------------------------------------------------------------------
{
	return new File();
}
private static final String odd = "/\\";
//-------------------------------------------------------------------
private String _nativeCreate(File directory,String path)
//-------------------------------------------------------------------
{
	//path = removeTrailingSlash(path);
	if (directory == null) return path;
	if (directory.jfile == null) return path;
	java.io.File f = new java.io.File(directory.jfile,path);
	String s = f.getPath();
	if (s.startsWith(odd)) s = "/"+s.substring(2); //Fix a bug in Microsoft's JVM
	else if (s.indexOf(odd) != -1) {
		int i = s.indexOf(odd);
		s = s.substring(0,i)+"/"+s.substring(i+2);
	}
	return s;
}
/**
 * Deletes the file or directory. Returns true if the operation is
 * successful and false otherwise.
 */
//===================================================================
public boolean delete()
//===================================================================
{
	return jfile.delete();
}


/** Returns true if the file exists and false otherwise. If the
Thread does not have read access to the directory, false will be returned.
*/
//===================================================================
public boolean exists()
//===================================================================
{
	try{
		return jfile.exists();
	}catch(Throwable t){
		return false;
	}
}


/** Returns the length of the file in bytes. If the file is not open
  * 0 will be returned.
  */
//===================================================================
public int getLength()
//===================================================================
{
	return (int)jfile.length();
}
/**
 * Return the length of the file in bytes.
 */
//===================================================================
public long length()
//===================================================================
{
	return jfile.length();
}

/**
* Return the fully qualified pathname of the file. Note that this
* will never end with a "/" EXCEPT for the root directory
**/
//===================================================================
public String getFullPath()
//===================================================================
{
	String s = jfilegetAbsolutePath();
	if (s.endsWith(".")){
		if (s.endsWith("/.") && s.length() > 2)
			s = s.substring(0,s.length()-2);
	}else if (s.endsWith("/")){
		if (s.endsWith("/./")  && s.length() > 3)
			s = s.substring(0,s.length()-3);
	}

	return s;
}
/** Return only the filename and extension of the file. **/

/*
//-------------------------------------------------------------------
private int getParentLength(String str)
//-------------------------------------------------------------------
{
	if (str == null) return -1;
	int len = str.length();
	if (len == 0) return -1;
	for (int i = 0; i<len; i++) {

	}
}
*/

/** Returns true if the file is a directory and false otherwise. */
//===================================================================
public boolean isDirectory() {try{return jfile.isDirectory();}catch(Throwable t){return false;}}
//===================================================================

//-------------------------------------------------------------------
private void doList(String mask,int listAndSortOptions,Handle h)
//-------------------------------------------------------------------
{
	try{
		String msk = mask;
		if ((listAndSortOptions & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) != 0)
			msk = null;
		ewe.io.FileComparer fc = new ewe.io.FileComparer(File.this,ewe.sys.Vm.getLocale(),listAndSortOptions,msk);
		String [] got = jfile.list(fc);
		if (got == null) got = new String[0];
		if ((listAndSortOptions & LIST_CHECK_FOR_ANY_MATCHING_CHILDREN) != 0){
			if (got.length == 0) got = null;
			else got = new String[0];
		}else
			ewe.util.Utils.sort(got,fc,((listAndSortOptions & LIST_DESCENDING) != 0));
		h.returnValue = got;
		h.set(h.Succeeded);
	}catch(Exception e){
		h.fail(e);
	}finally{
		h.setFlags(h.Stopped,0);
	}
}
//===================================================================
public Handle listFiles(final String mask,final int listAndSortOptions)
//===================================================================
{
	final Handle ret = new Handle();
	ret.set(Handle.Running);
	if (ewe.sys.Coroutine.getCurrent() != null)
		new Thread(){
			public void run(){
				doList(mask,listAndSortOptions,ret);
			}
		}.start();
	else
		doList(mask,listAndSortOptions,ret);
	return ret;
}
/**
* This moves/renames the file to the destination newFile. The newFile
* should not exist.
**/
//===================================================================
public boolean move(File newFile)
//===================================================================
{
	if (!isSameVolume(newFile)) return false;
	return jfile.renameTo(newFile.jfile);
}
/*
//-------------------------------------------------------------------
long combine(int high,int low)
//-------------------------------------------------------------------
{
	long val = high;
	val <<= 32;
	long lo = low;
	lo &= 0x00000000ffffffffl;
	return val|lo;
}
*/
//-------------------------------------------------------------------
protected void getSetModified(Time time,boolean doGet)
//-------------------------------------------------------------------
{
	try{
		if (doGet){
			time.setTime(Time.convertSystemTime(jfile.lastModified(),false));
		}else{
			jfile.setLastModified(Time.convertSystemTime(time.getTime(),true));
		}
	}catch(Throwable t){
	}
}
static String [] gotRoots;
static String isDrive(char c)
{
	try{
		if (new java.io.File(c+":/").isDirectory())
			return c+":/";
		else
			return null;
	}catch(Exception e){
		return null;
	}
}
/**
* This method is used to get extended information about the File or the File system in general.
* It is used with the INFO_ specifiers and options.
**/
//===================================================================
public Object getInfo(int infoCode,Object sourceParameters,Object resultDestination,int options)
//===================================================================
{
	String ret;
	try{
		switch(infoCode){
			case INFO_ROOT_LIST:
				try{
					java.io.File [] roots = java.io.File.listRoots();
					if (roots == null) return null;
					String [] r = new String[roots.length];
					for (int i = 0; i<roots.length; i++)
						r[i] = roots[i].getAbsolutePath();
					return r;
				}catch(Error e){
					if (gotRoots != null) return gotRoots;
					if (isDrive('C') != null){
						ewe.util.Vector v = new ewe.util.Vector();
						v.add("A:/"); v.add("C:/");
						char ch;
						for (ch = 'D'; ch <= 'Z'; ch++){
							String d = isDrive(ch);
							if (d != null) v.add(d);
						}
						gotRoots = new String[v.size()];
						v.copyInto(gotRoots);
						return gotRoots;
					}
					else
						return "/";
				}
			case INFO_PROGRAM_DIRECTORY:
				ret = ewe.applet.Applet.programDirectory;
				if (ret == null) ret = ".";
				if (ret.equals("."))
					if (ewe.sys.Vm.getAppletProperties() != null)
						ret = "/";
				ret = removeTrailingSlash(ret);
				return ret;
			case INFO_TEMPORARY_DIRECTORY:
				java.io.File tf = java.io.File.createTempFile("ewe",null,null);
				ret = tf.getParent().toString();
				tf.delete();
				return ret;
			case INFO_DEVICE_NAME:
				return "My Computer";
			case INFO_FLAGS:
			case INFO_SYSTEM_TYPE:
				if (!(resultDestination instanceof ewe.sys.Long))
					resultDestination = new ewe.sys.Long();
				((ewe.sys.Long)resultDestination).value = (infoCode == INFO_SYSTEM_TYPE) ? DOS_SYSTEM : 0;
				return resultDestination;
			case INFO_FILE_TIMES:
				if (!(resultDestination instanceof Time []))
					resultDestination = new Time[3];
				for (int i = 0; i<3; i++)
					((Time[])resultDestination)[i] = null;
				Time t = new Time();
				getSetModified(t,true);
				((Time[])resultDestination)[1] = t;
				return resultDestination;
			default:
				return null;
		}
	}catch(IllegalStateException ill){
		throw ill;
	}catch(Exception ex){
		ex.printStackTrace();
		return null;
	}catch(Error e){
		return null;
	}
}
/**
 * Creates a directory. Returns true if the operation is successful and false
 * otherwise.
 */
//===================================================================
public boolean createDir()
//===================================================================
{
	return jfile.mkdir();
}
//-------------------------------------------------------------------
private static int curTemp = 1000;
//-------------------------------------------------------------------
/**
* This tells the system to delete the file when the program exits.
**/
//===================================================================
public void deleteOnExit()
//===================================================================
{
	try{
		jfile.deleteOnExit();
	}catch(Error e){
	}
}

//-------------------------------------------------------------------
protected int getSetPermissionsAndFlags(boolean isGet, int valuesToSetOrGet, int valuesToClear) throws IOException, IllegalArgumentException
//-------------------------------------------------------------------
{
	if (!exists()) throw new IOException("File not found: "+getFullPath());
	if (isGet){
		if ((valuesToSetOrGet & ~FLAG_READONLY) != 0) throw new IllegalArgumentException("Flag/Permission not supported on this platform.");
		if (jfile.isDirectory() || jfile.canWrite()) return 0;
		return FLAG_READONLY;
	}else{
		if (((valuesToSetOrGet|valuesToClear) & ~FLAG_READONLY) != 0) throw new IllegalArgumentException("Flag/Permission not supported on this platform.");
		try{
			if ((valuesToSetOrGet & FLAG_READONLY) != 0) jfile.setReadOnly();
		}catch(Throwable t){}
		return getSetPermissionsAndFlags(true,(valuesToSetOrGet|valuesToClear),0);
	}
}
//-------------------------------------------------------------------
private static String fileChooserClass = null;
//-------------------------------------------------------------------

//===================================================================
public static void setFileChooserClass(String className)
//===================================================================
{
	fileChooserClass = className;
}
//===================================================================
public static String getFileChooserClass()
//===================================================================
{
	if (fileChooserClass == null) {
		Type ty = new Type("ewe.filechooser.FileChooser");
		if (ty.exists() && !ewe.ui.Gui.isSmartPhone) return "ewe.filechooser.FileChooser";
		ty = new Type("ewe.io.SimpleFileChooser");
		if (ty.exists()) return "ewe.io.SimpleFileChooser";
		return null;
	}
	return fileChooserClass;
}
//===================================================================
public boolean executeFileChooser(FileChooserParameters fcp)
throws IllegalStateException
//===================================================================
{
	Type ty = new Type(getFileChooserClass());
	if (!ty.exists()) throw new IllegalStateException();
	fcp.defaultTo(fcp.FILE_MODEL,this);
	String p = getParent();
	if (p == null) p = toString();
	if (isDirectory()) p = toString();
	fcp.defaultTo(fcp.START_LOCATION,p);
	Form f = (Form)ty.newInstance("(Lewe/io/FileChooserParameters;)V",new Object[]{fcp});
	if (f == null) throw new IllegalStateException();
	return f.execute() != f.IDCANCEL;
}

}
