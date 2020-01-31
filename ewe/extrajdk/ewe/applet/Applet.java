/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/applet/Applet.java,v 1.2 2008/05/02 20:52:02 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2008 Thorsten “mirabilos” Glaser                               *
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
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.applet;

/*
 * Note: Everything that calls ewe code in these classes must be
 * synchronized with respect to the Applet uiLock object to allow ewe
 * programs to be single threaded. This is because of the multi-threaded
 * nature of Java and because timers use multiple threads.
 *
 * Because all calls into ewe are synchronized and users can't call this code,
 * they can't deadlock the program in any way. If we moved the synchronization
 * into ewe code, we would have the possibility of deadlock.
 */

import ewe.ui.*;
import java.util.Vector;
import java.io.*;
import java.util.zip.*;

import ewe.io.RandomAccessStream;
import ewe.sys.Vm;

public class Applet extends java.applet.Applet
{
private static final long serialVersionUID = 8313529565282357395L;
String className;
public static Applet applet;
public static Vector windows = new Vector();
public static Class appletClass;
static{
	try{
		appletClass = Class.forName("ewe.applet.Applet");
	}catch(Exception e){}
}
public static boolean shouldUseFrame = false;
public java.awt.Window frame = null;
public MainWindow mainWindow;
public boolean isApplication = false;
public boolean isColor = false;
public int width,height;
public static int mainWidth;
public static int mainHeight;
public String title = "eWe!";
public static Object uiLock = ewe.sys.Coroutine.lockObject;//new Object();
public static String [] programArguments = new String[0];
public static int myVmFlags;
public static String localLanguage, localCountry;
public static ewe.io.FakeFileSystem fileSystem;
static
{
	//ewe.sys.Coroutine.lockObject = uiLock;
}
public static Applet currentApplet;

public static String programDirectory =  null;//".";
public static String eweFile = null;

int prW, prH;

public java.awt.Dimension getPreferredSize()
{
	return new java.awt.Dimension(prW,prH);
}

public static java.awt.Window tempF;

public static java.awt.Component getImageCreator()
{
	if (currentApplet == null) {
		currentApplet = new Applet();
		currentApplet.isApplication = true;
	}
	if (currentApplet.frame != null) return currentApplet.frame;
	if (currentApplet != null && !currentApplet.isApplication) return currentApplet;
	if (tempF == null) {
		tempF = new java.awt.Window(new java.awt.Frame());
		tempF.setVisible(true);
	}
	return tempF;
}
public static java.awt.Container getDisplayed()
{
	if (currentApplet.frame != null) return currentApplet.frame;
	return currentApplet;
}

Class getAClass(String className)
{
	try{
		return classLoader.getClass(className);
	}catch(Exception e){
		try{
			myVmFlags |= Vm.VM_FLAG_USING_CLASSES;
			return Class.forName(className);
		}catch(Exception e2){
			return null;
		}
	}
}

boolean getMainWindow(Class c,String className) throws Exception
{
	if (c == null) throw new Exception("Class not found: "+className);
	Class mAppClass = getAClass("ewe.ui.mApp");
	if (mAppClass.isAssignableFrom(c))
		mainWindow = (MainWindow)c.newInstance();
	else
		mainWindow = new mApp(c);
	return true;
}

public void init()
	{
	setLayout(new java.awt.GridLayout(1,1));
	// NOTE: getParameter() and size() don't function in a
	// java applet constructor, so we need to call them here
	if (!isApplication)
		{
		boolean doLoad = applet == null;
		applet = currentApplet = this;
		if (doLoad) loadResourceFile("_resources.zip");
		width = height = mainWidth = mainHeight = 0;
		shouldUseFrame = false;
		className = getParameter("appClass");
		title = getParameter("title");
		className = className.replace('/','.');
		//mainWidth = getSize().width;
		//mainHeight = getSize().height;
		if (!shouldUseFrame)
			shouldUseFrame = getParameter("useFrame") != null;
		if (!shouldUseFrame)
			shouldUseFrame = getParameter("frameWidth") != null;
		if (!shouldUseFrame)
			shouldUseFrame = getParameter("frameHeight") != null;
		int nw = ewe.sys.Convert.toInt(getParameter("frameWidth"));
		if (nw != 0) width = nw;
		int nh = ewe.sys.Convert.toInt(getParameter("frameHeight"));
		if (nh != 0) height = nh;
		String pars = getParameter("commandLine");
		if (pars != null){
			processArgs(splitArgs(pars),0);
		}
		//setSize(width,height);
		}
	prW = width;
	prH = height;
	synchronized(Applet.uiLock){
		try{
			if (fileSystem == null){
				if (!isApplication) currentApplet.showStatus("Applet loading virtual file system.");
				RandomAccessStream s = Vm.openRandomAccessStream("_filesystem.zip",ewe.io.RandomAccessFile.READ_ONLY);
				if (s != null){
					ewe.zip.ZipFile zf = new ewe.zip.ZipFile(s);
					if (zf.isOpen()) {
						System.out.println("Loading virtual file system.");
						ewe.zip.ZipEntryFile zef = new ewe.zip.ZipEntryFile(zf);
						fileSystem = new ewe.io.FakeFileSystem();
						fileSystem.addVolume("Disk1",zef.getNew("/"));
					}
					zf.close();
				}
			}
		}catch(Throwable t){
			//t.printStackTrace();
		}
	}
	if (!isApplication) currentApplet.showStatus("Continuing to load application...");
	ewe.ui.Gui.screenSize = null;
	ewe.applet.Frame.curModal = null;
	if (mainWindow ==  null)
	try
		{
	synchronized(Applet.uiLock)
		{
		WinCanvas.inCallback = true;
		getMainWindow(getAClass(className),className);
		WinCanvas.inCallback = false;
		}
		}
	catch (Exception e)
		{
			e.printStackTrace();
		}
	if (mainWindow == null) System.exit(0);
	else synchronized(Applet.uiLock)
		{
		WinCanvas.inCallback = true;
		try{
			mainWindow.onStart();
		}catch(Exception e){
			e.printStackTrace();
			throw new RuntimeException("Could not start application running.");
		}
		WinCanvas.inCallback = false;
		}

	}

public void start()
	{
	Gui.screenSize = null;
	ewe.applet.Frame.curModal = null;
	currentApplet = this;
	ewe.ui.Window.restart();
	}

public void destroy()
	{
	if (mainWindow == null)
		return;
	mainWindow._stopTimer();
	synchronized(Applet.uiLock)
		{
		mainWindow.onExit();
		}
	}

//-------------------------------------------------------------------
void findMainAppClass() throws Exception
//-------------------------------------------------------------------
{
	String line = null;
	if (eweFiles.size() == 0){
		//No eweFiles - look for CommandLine.txt in resourceFile
		InputStream in = openStreamInEwes("/META-INF/CommandLine.txt");
		if (in != null){
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			line = br.readLine();
			br.close();
		}
	}
	if (line == null){
		InputStream in = openStreamInEwes(null,true);
		if (in == null) throw new Exception("mApp class not specified");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		line = br.readLine();
		br.close();
	}
	if (line != null){
		String [] args = splitArgs(line);
		processArgs(args,1);
	}
	if (className == null)
		throw new Exception("mApp class not specified");
}

//===================================================================
String [] splitArgs(String line)
//===================================================================
{
	Vector v = new Vector();
	int i = 0, max = line.length();
	while(i<max){
		StringBuffer sb = new StringBuffer();
		while(i<max && Character.isWhitespace(line.charAt(i))) i++;
		if (i>=max) break;
		if (line.charAt(i) == '"'){
			i++;
			while(i<max && line.charAt(i) != '"') sb.append(line.charAt(i++));
			i++;
			v.addElement(sb.toString());
			continue;
		}else{
			while(i<max && !Character.isWhitespace(line.charAt(i))) sb.append(line.charAt(i++));
			v.addElement(sb.toString());
		}
	}
	String [] got = new String[v.size()];
	v.copyInto(got);
	return got;
}
public static Vector eweFiles = new Vector();
public static Vector resources = new Vector();

//===================================================================
public static ewe.io.Stream openStreamInResources(String path)
//===================================================================
{
	for (int i = 0; i<resources.size(); i += 2){
		String s = (String)resources.elementAt(i);
		if (!s.equals(path)) continue;
		byte [] bytes = (byte [])resources.elementAt(i+1);
		return new InputStreamStream(new ByteArrayInputStream(bytes));
	}
	return null;
}
//===================================================================
public static ewe.io.Stream openStreamForReading(String path)
//===================================================================
{
	ewe.io.Stream stream = openStreamInResources(path);
	if (stream != null) {
		return stream;
	}
	java.io.InputStream is = openInputStream(path);
	if (is != null) return new InputStreamStream(is);
	else return null;
}
static byte [] some = new byte[1024*10];
/**
* Reads all the bytes from a stream into an array.
**/
public final static byte []
//============================================================
	readAllBytes(InputStream is)
//============================================================
{
	byte [] got = null;
	while(true) {
		try {
			int read = is.read(some);
			if (read == -1) break;
			got = addBytes(got,some,read);
		}catch (Exception e) {
			break;
		}
	}
	return got;
}
//====================================================================
public final static byte [] addBytes(byte [] original, byte [] more, int num)
//====================================================================
{
	byte [] now;
	int l = 0;
	if (original == null) now = new byte[num];
	else {
		l = original.length;
		now = new byte[num+l];
		System.arraycopy(original,0,now,0,l);
	}
	System.arraycopy(more,0,now,l,num);
	return now;
}
//===================================================================
public static boolean loadResourceFile(String path)
//===================================================================
{
	try{
		currentApplet.showStatus("Applet loading resource file...");
		java.io.InputStream is = openInputStream(path);
		if (is == null) return false;
		java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new BufferedInputStream(is,1024*10));
		while(true){
			ZipEntry ze = zis.getNextEntry();
			if (ze == null) break;
			byte [] got = readAllBytes(zis);
			if (got == null) continue;
			resources.addElement(ze.getName());
			resources.addElement(got);
		}
		zis.close();
	}catch(Exception e){
		System.out.println("Exception: "+e+" loading resource.");
		return false;
	}
	return true;
}
//===================================================================
public static java.io.InputStream openInputStream(String path)
//===================================================================
{
	if (currentApplet == null){
		currentApplet = new Applet();
		currentApplet.isApplication = true;
	}
	boolean isApp = Applet.currentApplet.isApplication;
	java.io.InputStream stream = null;
	if (isApp)
		{
		stream = ewe.applet.Applet.openStreamInEwes(path);
		if (stream == null)
			try { stream = new java.io.FileInputStream(path); }
			catch (Exception e) {};
		}
	else
		try
			{
			java.net.URL codeBase = Applet.currentApplet.getCodeBase();
			String cb = codeBase.toString();
			char lastc = cb.charAt(cb.length() - 1);
			char firstc = path.charAt(0);
			if (lastc != '/' && firstc != '/')
				cb += "/";
			java.net.URL url = new java.net.URL(cb + path);
			stream = url.openStream();
			}catch(Exception e){
				stream = null;
			}
	return stream;
}
//===================================================================
public static java.io.InputStream openStreamInEwes(String fileName)
//===================================================================
{
	return openStreamInEwes(fileName,false);
}

//===================================================================
public static java.io.InputStream openStreamInEwes(String fileName,boolean runFile)
//===================================================================
{
	if (fileName != null) fileName = fileName.replace('\\','/');
	if (!runFile && fileName != null){
		try{
			String fn = fileName;
			if (!fn.startsWith("/")) fn = '/'+fn;
			InputStream is = appletClass.getResourceAsStream(fn);
			if (is != null){
				return is;
			}
		}catch(Exception e){
		}
	}
	byte [] got = findFileInEwes(fileName,runFile);
	if (got == null) return null;
	return new java.io.ByteArrayInputStream(got);
}
//===================================================================
public static byte [] findFileInEwes(String fileName)
//===================================================================
{
	return findFileInEwes(fileName,false);
}
//===================================================================
public static byte [] findFileInEwes(String fileName,boolean runFile)
//===================================================================
{
	for (int i = 0; i<eweFiles.size(); i++){
		EweFile ef = (EweFile)eweFiles.elementAt(i);
		try{
			byte [] ret =
				runFile ? ef.getRunFile() :  ef.getFile(fileName);
			if (ret != null) {
				if (runFile) {
					if (programDirectory == null)
						programDirectory = new File(ef.myFileName).getParent();
				}
				return ret;
			}
		}catch(IOException e){
			e.printStackTrace();
			continue;
		}
	}
	return null;
}
//===================================================================
public static boolean loaded(String fileName)
//===================================================================
{
	for (int i = 0; i<eweFiles.size(); i++){
		EweFile ef = (EweFile)eweFiles.elementAt(i);
		if (ef.myFileName.equals(fileName)) return true;
	}
	return false;
}
/*
//===================================================================
public void processArguments(String args[])
//===================================================================
{

}
*/

	//##################################################################
	class eweClassLoader extends mClassLoader{
	//##################################################################

	eweClassLoader()
	{
		classInfoLoaders.addElement(new mClassLoader(){
			//-------------------------------------------------------------------
			public boolean getClassBytes(ClassInfo info)
			//-------------------------------------------------------------------
			{
				String look = info.fullPath.replace('.','/')+".class";
				byte [] got = null;
				for (int i = 0; i<eweFiles.size(); i++){
					EweFile ef = (EweFile)eweFiles.elementAt(i);
					try{
						got = ef.getFile(look);
						if (got == null) continue;
						if (ef.pool != null){
							ClassFile cf = new ClassFile(new ewe.util.ByteArray(got));
							got = cf.convertBack(ef.pool);
						}
						break;
					}catch(IOException e){
						e.printStackTrace();
						continue;
					}
				}
				if (got == null) {
					if (info.fullPath.equals(className))
									myVmFlags |= Vm.VM_FLAG_USING_CLASSES;
					return false;
				}
				info.bytes = got;
				return true;
			}
		});
	}
	//##################################################################
	}
	//##################################################################

eweClassLoader classLoader;
{
	try{
		classLoader = new eweClassLoader();
	}catch(Throwable t){}
}

//-------------------------------------------------------------------
void makeLoader()
//-------------------------------------------------------------------
{
	classLoader = new eweClassLoader();
}

//===================================================================
public static Class loadClass(String className)
//===================================================================
{
	try{
		Class c = Class.forName(className);
		if (c != null) return c;

	}catch(Throwable e){}
	if (applet.classLoader != null)
		return applet.classLoader.getClass(className);
	return null;
}
//===================================================================
void processArgs(String [] args,int level)
//===================================================================
{
	int count = args.length, i = 0;
	for (i = 0; i < count; i++)
		{
		String arg = args[i];
		if (arg.toLowerCase().endsWith(".ewe")){
			if (eweFile == null)
				eweFile = arg;
			if (programDirectory == null)
				programDirectory = new File(arg).getParent();
			try{
				if (!loaded(arg)){
					RandomAccessFile raf = new EweFile(arg);
					eweFiles.addElement(raf);
					if (className == null) try{
						findMainAppClass();
					}catch(Exception e){}
				}
			}catch(Exception e){
				System.out.println("Error: "+e.getMessage());
			}
		}else if (arg.charAt(0) != '/') {
			if (className != null) break;
			className = arg;
			i++; break;
		}else if (arg.equals("/-")){
			i++;
			break;
		}else if (arg.equals("/s")){
			myVmFlags |= Vm.VM_FLAG_LOW_MEMORY|Vm.VM_FLAG_NO_MOUSE_POINTER|Vm.VM_FLAG_NO_KEYBOARD;
			myVmFlags |= Vm.VM_FLAG_NO_PEN|Vm.VM_FLAG_HAS_SOFT_KEYS|Vm.VM_FLAG_NO_WINDOWS|Vm.VM_FLAG_USE_NATIVE_TEXT_INPUT;
			//myVmFlags |= Vm.VM_FLAG_IS_MOBILE;
			if (mainWidth == 0 && mainHeight == 0){
				mainWidth = 176;
				mainHeight = 220;
			}
		}else if (arg.equals("/m")){
			myVmFlags |= Vm.VM_FLAG_LOW_MEMORY;
		}else if (arg.equals("/b")){
			if (++i < count) title = args[i];
		}else if (arg.equals("/n")){
			myVmFlags |= Vm.VM_FLAG_NO_WINDOWS;
		}else if (arg.equals("/r")){
			myVmFlags |= Vm.VM_FLAG_IS_MOBILE;
		}else if (arg.equals("/k")){
			myVmFlags |= Vm.VM_FLAG_NO_MOUSE_POINTER|Vm.VM_FLAG_NO_KEYBOARD;
		}else if (arg.equals("/p")){
			myVmFlags |= Vm.VM_FLAG_NO_MOUSE_POINTER|Vm.VM_FLAG_NO_KEYBOARD;
			ewe.sys.Vm.setParameter(ewe.sys.Vm.SIMULATE_SIP,1);
			if (mainWidth == 0 && mainHeight == 0){
				mainWidth = 240;
				mainHeight = 320;
			}
		}else if (arg.equals("/z")){
			myVmFlags |= Vm.VM_FLAG_IS_MONOCHROME;
		}else if (args[i].equals("/d")){
			programDirectory = args[++i];
			if (programDirectory.endsWith("/") || programDirectory.endsWith("\\"))
				programDirectory = programDirectory.substring(0,programDirectory.length()-1);
			if (classLoader != null){
				PathClassInfoLoader pil = new FileClassInfoLoader();
				pil.paths.addElement(programDirectory+"/classes");
				classLoader.classInfoLoaders.addElement(pil);
			}
		}if (args[i].equals("/w"))
			{
			if (++i < count)
				try { mainWidth/*width*/ = Integer.parseInt(args[i]); }
				catch (Exception e)
					{
					e.printStackTrace();
					System.out.println("ERROR: bad width");
					}
			}
		else if (args[i].equals("/h"))
			{
			if (++i < count)
				try { mainHeight/*height*/ = Integer.parseInt(args[i]); }
				catch (Exception e)
					{
					System.out.println("ERROR: bad height");
					}
			}
		else if (args[i].equals("/color"))
			isColor = true;

		else if (args[i].equals("/l")){
			if (++i < count){
				String [] ll = ewe.util.mString.split(args[i],'-');
				if (ll.length >= 1) localLanguage = ll[0];
				if (ll.length >= 2) localCountry = ll[1];
			}
		}

		}
		//if (level > 0) return;
		programArguments = new String[count-i];
		for (int j = 0; j<programArguments.length; j++){
			programArguments[j] = args[i+j];
		}
}

public static String [] commandLine = null;

public static void main(String args[]) throws Exception
	{
	String jeode = System.getProperty("com.jeode.evm.version",null);
	if (jeode != null){
		//System.out.println("Jeode version: "+jeode);
		myVmFlags |= Vm.VM_FLAG_IS_MOBILE|Vm.VM_FLAG_NO_MOUSE_POINTER|Vm.VM_FLAG_NO_KEYBOARD|Vm.VM_FLAG_SLOW_MACHINE;
		if (System.getProperty("awt.toolkit","none").endsWith("QtToolkit")){
			//System.out.println("QtToolkit!");
			myVmFlags |= Vm.VM_FLAG_IS_ZAURUS_EVM;
		}
	}

	boolean isColor = false;
	int width = 240;
	int height = 320;

	if (commandLine != null) args = commandLine;
	int count = args.length;
	if (currentApplet == null){
		currentApplet = new Applet();
		currentApplet.makeLoader();
	}
	applet = currentApplet;
	applet.width = width;
	applet.height = height;
	applet.isApplication = true;
	applet.processArgs(args,0);
	//applet.className = args[count - 1];
	if (applet.className == null) applet.findMainAppClass();
	applet.className = applet.className.replace('/','.');
	applet.isColor = isColor;
	if (programDirectory == null) programDirectory = "";
	try{
		if (programDirectory.equals(".") || programDirectory.length() == 0)
			programDirectory = System.getProperty("user.dir",".");
		else
			programDirectory = new File(programDirectory).getAbsolutePath();
	}catch(Throwable e){}
	shouldUseFrame = true;
	/*
	Frame frame = new Frame();
	frame.setTitle(applet.title);
	frame.setLayout(new java.awt.GridLayout(1,1));
	//frame.setSize(50,50);
	//frame.show();
	applet.frame = frame;
	// NOTE: java requires us to do this to make sure things paint
	//frame.hide();
	java.awt.Insets insets;
	try
		{
  		insets = frame.getInsets();
		}
	catch (NoSuchMethodError e)
		{
		insets = frame.insets(); // this is the JDK 1.02 call to get insets
		}
	if (insets == null)
		insets = new java.awt.Insets(0, 0, 0, 0);
	frame.resize(applet.width + insets.left + insets.right,
		applet.height + insets.top + insets.bottom);
	*/
	applet.init();
	}

//===================================================================
public static String getStackTrace(Throwable t,int lines) {return getStackTrace(t,lines,0);}
public static String getStackTrace(Throwable t,int lines,int ignore)
//===================================================================
{
	StringWriter sw = new StringWriter();
	t.printStackTrace(new PrintWriter(sw));
	BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
	String got = "";
	for (int i = 0; i<lines; i++){
		try{
			String line = br.readLine();
			if (line == null) break;
			if (i >= ignore) got += line+"\n";
		}catch(Exception e){break;}
	}
	return got.replace('\t',' ');
}
public static String getStackTrace(int lines,int ignore)
{
	return getStackTrace(new Exception(),lines,ignore+1);
}




}
//##################################################################
class EweFile extends RandomAccessFile{
//##################################################################

String myFileName;
String myEweName;
ClassFile.UtfPool pool;

//===================================================================
public EweFile(String name) throws java.io.IOException
//===================================================================
{
	super(name,"r");
	myFileName = name;
	myEweName = new java.io.File(name).getName();
	int idx = myEweName.lastIndexOf('/'); //Fix bug in MS Java
	if (idx != -1) myEweName = myEweName.substring(idx+1);
	idx = myEweName.lastIndexOf('\\'); //Fix bug in MS Java
	if (idx != -1) myEweName = myEweName.substring(idx+1);
	idx = myEweName.lastIndexOf('.');
	if (idx != -1) myEweName = myEweName.substring(0,idx);
	byte [] got = getFile("_UtfPool_");
	if (got != null) pool = new ClassFile.UtfPool(got);
}

//===================================================================
public byte [] getRunFile() throws IOException
//===================================================================
{
	return getFile(myEweName+".run");
}

byte [] buff = new byte[256];
/*
//-------------------------------------------------------------------
int readInt() throws IOException
//-------------------------------------------------------------------
{
	readFully(buff,0,4);
	return ewe.util.Utils.readInt(buff,0,4);
}
//-------------------------------------------------------------------
int readShort() throws IOException
//-------------------------------------------------------------------
{
	readFully(buff,0,2);
	return ewe.util.Utils.readInt(buff,0,2);
}
*/
//-------------------------------------------------------------------
int readInt(int where) throws IOException
//-------------------------------------------------------------------
{
	seek(where);
	return readInt();
}
//-------------------------------------------------------------------
int readShort(int where) throws IOException
//-------------------------------------------------------------------
{
	seek(where);
	return readShort();
}
char [] nameBuff = new char[256];
//===================================================================
public byte [] getFile(String name) throws IOException
//===================================================================
{
	int baseP = 0;
	int numRecs = readInt(baseP+4);
	if (numRecs == 0) return null;
	// NOTE: We do a binary search to find the class. So, a search
	// for N classes occurs in O(nlogn) time.
	int top = 0;
	int bot = numRecs;
	while(true){
		int mid = (bot + top) / 2;
		int offP = baseP + 8 + (mid * 4);
		int off = readInt(offP);
		int nextOff = readInt();
		int p = baseP + off;
		int nameLen = readShort(p); //This is the number of bytes representing the string.
		if (nameLen > buff.length) buff = new byte[nameLen];
		readFully(buff,0,nameLen);
		int checkLen = nameLen;
		while(checkLen > 0 && buff[checkLen-1] == 0) checkLen--;
		String inFile = ewe.util.Utils.decodeJavaUtf8String(buff,0,checkLen);
		int cmp = name.compareTo(inFile);
		if (cmp == 0){
			int size = nextOff - off - nameLen - 2;
			byte [] ret = new byte[size];
			readFully(ret,0,size);
			return ret;
		}
		if (mid == top)
			break; // not found
		if (cmp < 0)
			bot = mid;
		else
			top = mid;
		}
	return null;
}
//##################################################################
}
//##################################################################
