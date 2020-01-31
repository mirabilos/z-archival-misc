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
 *  if not, please download it at: www.ewesoft.com/LGPL.txt                      *
 *                                                                               *
 *********************************************************************************/
package ewe.io;
import ewe.util.*;
import ewe.sys.*;
import ewe.data.*;
/**
* A EweFile represents a formatted ".ewe" file. Like a ewe.zip.ZipFile it can be used
* to browse the contents of a ZipFile.
<p>
While there are methods to list and read the entries
of a EweFile it is probably more useful to create a EweEntryFile object for the EweFile.
This file is a type of ewe.io.File and so can be used to browse, list and even read the
contents of the EweFile as if it were an ordinary file. You create a EweEntryFile by first
creating the EweFile object, given a RandomAccessStream that reads the EweFileBytes
and then calling one of the EweEntryFile constructors. This makes that constructed EweEntryFile
represent the root directory of the EweFile and you can then call getChild() or list() on that
root file to browse the contents of the EweFile.
**/
//##################################################################
public class EweFile{
//##################################################################

RandomAccessStream stream;
public String eweName = "Ewe File";
public static ewe.fx.IImage eweIcon;


//-------------------------------------------------------------------
EweFile(RandomAccessStream stream,boolean alwaysFalse)
//-------------------------------------------------------------------
{
	this.stream = stream;
}
//===================================================================
public EweFile(RandomAccessStream stream,ewe.sys.Handle handle)
//===================================================================
{
	if (handle == null) throw new NullPointerException();
	this.stream = stream;
	try{
		readEntries(handle,null);
	}catch(IOException e){

	}
}
//===================================================================
public EweFile(RandomAccessStream stream) throws IOException
//===================================================================
{
	this.stream = stream;
	readEntries(null,null);
}
//===================================================================
public EweFile(File f) throws IOException
//===================================================================
{
	this(f.toRandomAccessStream("r"));
}

byte buff[] = new byte[4];

//-------------------------------------------------------------------
byte [] readFully(byte [] dest,int len) throws IOException
//-------------------------------------------------------------------
{
	if (dest == null) dest = new byte[len];
	if (IO.readFully(stream,dest,0,len) != len) throw new IOException("Error reading file.");
	return dest;
}
//-------------------------------------------------------------------
void seek(int pos) throws IOException
//-------------------------------------------------------------------
{
	if (!stream.seek(pos)) throw new IOException("Error reading file.");
}
//-------------------------------------------------------------------
int readInt() throws IOException
//-------------------------------------------------------------------

{
	return ewe.util.Utils.readInt(readFully(buff,4),0,4);
}
//-------------------------------------------------------------------
int readShort() throws IOException
//-------------------------------------------------------------------
{
	return ewe.util.Utils.readInt(readFully(buff,2),0,2);
}


/**
 * Find a named entry.
 * @param name The case sensitive name to look for.
 * @return the EweEntry if found, null if not.
 */
//===================================================================
public EweEntry findEntry(String name)
//===================================================================
{
	for (int i = 0; i<entries.size(); i++){
		EweEntry e = (EweEntry)entries.get(i);
		if (e.name.equals(name)) return e;
	}
	return null;
}

//-------------------------------------------------------------------
EweEntry findAnEntry(String name)
//-------------------------------------------------------------------
{
	try{
		readEntries(null,name);
		if (entries.size() == 0) return null;
		return (EweEntry)entries.get(0);
	}catch(IOException e){
		return null;
	}
}


/**
 * Get a Stream that can be used for reading a single entry from a RandomAccessStream that
	represents a Ewe file.
 * @param eweStream An open Ewe file.
 * @param name the entry to lok for.
 * @param singleUse if this is true, then closing the returned input stream will close the eweStream as well.
 * @return a Stream for reading.
 * @exception IOException if the ewe file is not formatted correctly.
 */

//===================================================================
public static Stream getInputStream(RandomAccessStream eweStream,String name,boolean singleUse) throws IOException
//===================================================================
{
	EweFile f = new EweFile(eweStream);
	EweEntry got = f.findAnEntry(name);
	if (got == null) {
		if (singleUse) eweStream.close();
		return null;
	}
	PartialInputStream is = new PartialInputStream(eweStream,got.offset,got.length);
	is.closeUnderlying = singleUse;
	return is;
}
//===================================================================
public static ewe.fx.Image getSingleImage(RandomAccessStream eweStream,String baseName,boolean isMask)
//===================================================================
{
	String full = baseName+(isMask ? "Mask" : "");
	try{
		Stream in = EweFile.getInputStream(eweStream,full+".bmp",false);
		if (in == null) in = EweFile.getInputStream(eweStream,full+".png",false);
		if (in == null) {
			if (!isMask) return null;
			full = baseName+"mask";
			in = EweFile.getInputStream(eweStream,full+".bmp",false);
			if (in == null) in = EweFile.getInputStream(eweStream,full+".png",false);
			if (in == null) return null;
		}
		return new ewe.fx.Image(new ewe.util.ByteArray(IO.readAllBytes(in,null)),0);
	}catch(Exception e){
		return null;
	}
}
//===================================================================
public static ewe.fx.IImage getImageAndMask(RandomAccessStream eweStream,String baseName)
//===================================================================
{
	ewe.fx.Image image = getSingleImage(eweStream,baseName,false);
	if (image == null) return null;
	ewe.fx.Image mask = getSingleImage(eweStream,baseName,true);
	ewe.fx.mImage got = new ewe.fx.mImage();
	if (mask == null) got.setImage(image);
	else got.setImage(image,mask);
	return got;
}
//===================================================================
protected void readEntries(ewe.sys.Handle handle,String lookFor) throws IOException
//===================================================================
{
	entries.clear();
	try{
		if (handle != null) {
			handle.set(handle.Running);
			handle.progress = 0;
		}
		int baseP = 0;
		seek(baseP+4);
		int numRecs = readInt();
		for (int i = 0; i<numRecs; i++){
			if (handle != null)
				if (handle.shouldStop){
					handle.set(handle.Failed|handle.Aborted);
					return;
				}
			int off = readInt();
			int nextOff = readInt();
			int size = nextOff-off;

			int here = stream.getFilePosition();
			seek(baseP+off);
			int nameLen = readShort();
			size -= nameLen+2;
			if (nameLen > buff.length) buff = new byte[nameLen];
			readFully(buff,nameLen);
			int strLen = nameLen;
			while(buff[strLen-1] == 0) strLen--;
			String inFile = ewe.util.Utils.decodeJavaUtf8String(buff,0,strLen);
			EweEntry efe = new EweEntry(inFile);
			efe.length = size;
			efe.offset = baseP+off+nameLen+2;
			if (lookFor != null){
				if (lookFor.equals(inFile)){
					entries.add(efe);
					break;
				}
			}else
				entries.add(efe);
			seek(here-4);
			if (handle != null) {
				handle.progress = (float)(i+1)/numRecs;
				handle.changed();
			}
			ewe.sys.mThread.nap(10,0);
		}
		if (handle != null){
			handle.set(handle.Succeeded);
		}
	}catch(IOException e){
		stream = null;
		if (handle != null) {
			handle.errorObject = e;
			return;
		}else
			throw e;
	}
}

//===================================================================
public void close()
//===================================================================
{
	stream.close();
}

protected Vector entries = new Vector();

//===================================================================
public Stream getInputStream(EweEntry entry)
//===================================================================
{
	return new PartialInputStream(stream,entry.offset,entry.length);
}
/**
 * Get an Iterator for the entries in the EweFile.
 */
//===================================================================
public ewe.util.Iterator getEntries()
//===================================================================
{
	return entries.iterator();
}
//===================================================================
public boolean isOpen()
//===================================================================
{
	return stream != null;
}
	public EweEntry root = null;
  /**
   * Read the central directory of a ewe file and fill the entries
   * array.  This is called exactly once by the constructors.
	 * Returns false if there was an error reading the entries.
   */
	//===================================================================
	EweEntry getTree(ewe.sys.Handle h)
	//===================================================================
	{
		ewe.sys.Locale l = ewe.sys.Vm.getLocale();
		if (root != null) return root;
		EweEntry [] entries = (EweEntry [])this.entries.toArray(new EweEntry[this.entries.size()]);
		if (entries == null) return null;
		Vector subs = new Vector();
		SubString ss = new SubString();
		root = new EweEntry("/");
		root.isDir = true;
		int st = 0;
		for (int i = 0; i<entries.length; i++){
			if (h != null){
				ewe.sys.Coroutine.nap(20,0);
				if (h.shouldStop) return null;
			}
			//ewe.sys.Vm.debug("-- "+i);
			EweEntry entry = entries[i];
			String name = entry.getName();
//.....................................................................
			char [] ch = ewe.sys.Vm.getStringChars(name);
			int len = ss.set(ch,0,ch.length).split('/',subs);
			if (ch.length != 0)
				if (ch[ch.length-1] == '/'){
					len--;
					entry.isDir = true;
				}
			//int len = 0;
			//for (;st+len<ch.length;len++) if (ch[st+len] == '/') break;
			EweEntry cur = root;
			for(int depth = 0; depth<len; depth++){
				int num = cur.getChildCount();
				SubString mine = (SubString)subs.get(depth);
				boolean found = false;
				EweEntry child = null;
				for (int tn = 0; tn<num; tn++){
					child = (EweEntry)cur.getChild(tn);
					SubString cs = child.myFileName;
					if (cs.length != mine.length) continue;
					//ewe.sys.Vm.debug("Checking: "+cs+" against: "+mine);
					int match = 0;
					for (int c = 0; c<mine.length; c++)
						if (l.compare(ch[mine.start+c],cs.data[cs.start+c],l.IGNORE_CASE) != 0) break;
						else match++;
					if (match != mine.length) {
						//ewe.sys.Vm.debug(cs+" is not "+mine);
						continue;
					}//else
						//ewe.sys.Vm.debug(cs+" IS "+mine);
					cur = child;
					found = true;
					break;
				}
				if (found){
					if (depth == len-1) cur.linkTo = entry;
					continue;
				}
				if (depth == len-1) {
					child = entry;
				}else {
					child = entry;
					child = new EweEntry(mine.toString());
					child.isDir = true;
				}
				child.myFileName = new SubString().set(mine.data,mine.start,mine.length);
				cur.addChild(child);
				//ewe.sys.Vm.debug(cur.myFileName+"->"+child.myFileName.toString());
				cur = child;
			}
//.....................................................................
		}
		return root;
	}

//##################################################################
public static class EweEntry extends ewe.data.LiveTreeNode{
//##################################################################
/**
* The name of the entry.
**/
public String name;
/**
* The size of the entry in bytes.
**/
public int length;
/**
* The location of the first byte of the entry in the EweFile.
**/
public int offset;

//===================================================================
public EweEntry(String name)
//===================================================================
{
	this.name = name;
}
//===================================================================
public String toString()
//===================================================================
{
	return name;
}
//===================================================================
public String getName()
//===================================================================
{
	return name;
}
//===================================================================
public int compareTo(Object other)
//===================================================================
{
	if (!(other instanceof EweEntry)) return 1;
	return ewe.sys.Vm.getLocale().compare(getName(),((EweEntry)other).getName(),0);
}

EweEntry linkTo = null;

public ewe.util.SubString myFileName;
boolean isDir;

//##################################################################
}
//##################################################################

//===================================================================
public EweEntryFile getEweEntryFile(ewe.sys.Handle h)
//===================================================================
{
	return new EweEntryFile(this,h);
}

//##################################################################
public static class EweEntryFile extends FileAdapter{
//##################################################################

public EweEntry root;
public EweEntry myEntry;
public EweFile eweFile;
public String eweFileName = "Ewe File";
//===================================================================
public File getNew(File parent,String file)
//===================================================================
{
	EweEntryFile zf = new EweEntryFile(eweFile);
	zf.set(parent,file);
	return zf;
}


/**
 * Create an EweEntryFile for a particular EweFile. You can then call
 * getNew(null,"/") to get a representation of the root directory in the EweFile.
 * @param ef the EweFile
 */
//===================================================================
public EweEntryFile(EweFile ef){this(ef,null);}
//===================================================================


/**
 * Create an EweEntryFile for a particular EweFile. You can then call
 * getNew(null,"/") to get a representation of the root directory in the EweFile.
 * @param ef the EweFile
 * @param h an optional handle that can be used to abort the construction.
 */
//===================================================================
public EweEntryFile(EweFile ef,ewe.sys.Handle h)
//===================================================================
{
	eweFile = ef;
	eweFileName = eweFile.eweName;
	this.root = ef.getTree(h);
	if (h != null)
		h.set(root != null ? h.Succeeded : h.Failed);
}
public boolean isValid() {return root != null;}
public boolean exists() {return myEntry != null;}
public boolean isDirectory() {if (myEntry == null) return false; return myEntry.isDir;}
public int getLength()
{
	EweEntry ze = myEntry;
	if (ze == null) return 0;
	if (ze.linkTo != null) ze = ze.linkTo;
	return ze.length;
}
//-------------------------------------------------------------------
protected  void getSetModified(Time time,boolean doGet)
//-------------------------------------------------------------------
{
/*
	EweEntry ze = myEntry;
	if (ze == null) {
		if (doGet) time.setTime(new Time().getTime());
	}else{
		if (ze.linkTo != null) ze = ze.linkTo;
		if (doGet) ze.getTime(time);
	}
	*/
}
boolean showed = false;
public void set(File parent,String file)
{
	name = file.replace('\\','/');
	if (parent instanceof EweEntryFile) {
		root = ((EweEntryFile)parent).root;
		String nm = ((EweEntryFile)parent).name;
		if (nm == null) nm = "";
		name = mString.removeTrailingSlash(nm)+"/"+file;

	}
	if (name.length() == 0) name = "/";
	else {
		if (name.charAt(0) != '/') name = "/"+name;
		if (name.length() != 1)
			name = mString.removeTrailingSlash(name);
	}
	myEntry = null;
	if (name.equals("/")) myEntry = root;
	else{
		String [] got = mString.split(name,'/');
		EweEntry cur = root;
		SubString mine = new SubString();
		for (int i = 1; i<got.length; i++){
			mine.set(got[i]);
			int num = cur.getChildCount();
			boolean found = false;
			EweEntry child = null;
			for (int tn = 0; tn<num; tn++){
				child = (EweEntry)cur.getChild(tn);
				SubString cs = child.myFileName;
				//ewe.sys.Vm.debug(cs.toString()+" = "+mine.toString());
				if (SubString.equals(cs.data,cs.start,cs.length,mine.data,mine.start,mine.length,SubString.IGNORE_CASE)){
					//ewe.sys.Vm.debug("Found!");
					cur = child;
					found = true;
					break;
				}
			}
			if (found) continue;
			cur = null;
			break;
		}
		myEntry = cur;
	}
}
int curFind = 0;
//-------------------------------------------------------------------
protected int startFind(String mask)
//-------------------------------------------------------------------
{
	curFind = 0;
	if (!isDirectory()) return 0;
	return 1;
}
//-------------------------------------------------------------------
protected Object findNext(int search)
//-------------------------------------------------------------------
{
	if (curFind >= myEntry.getChildCount()) return null;
	EweEntry ze = (EweEntry)myEntry.getChild(curFind++);
	return ze.myFileName.toString();
}

protected void endFind(int search){}

//-------------------------------------------------------------------

public boolean simulateCanWrite = false;

//-------------------------------------------------------------------
String [] getDetails(EweEntry ze,String [] dest,Object source,boolean detailView)
//-------------------------------------------------------------------
{
	if (dest == null) dest = new String[2];
	PropertyList pl = source instanceof PropertyList ? (PropertyList)source : new PropertyList();
	Time t = getModified(new Time());
	ewe.sys.Locale l = (ewe.sys.Locale)pl.getValue("locale",ewe.sys.Vm.getLocale());
	dest[0] = "?";

	if (detailView) dest[0] = Utils.fileLengthDisplay(getLength());
	else dest[0] = l.format(l.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(getLength()),",");

	/*
	int cl = ze == null ? -1 : ze.getCompressedSize();
	if (cl != -1)
		if (detailView) dest[1] = ewe.filechooser.FileChooser.lengthToDisplay(cl);
		else dest[1] = l.format(l.FORMAT_PARSE_NUMBER,ewe.sys.Long.l1.set(cl),",");
	*/
	t.format = pl.getString("dateFormat",l.getString(l.LONG_DATE_FORMAT,0,0));
	dest[1] = t.toString();
	if (!detailView){
		t.format = pl.getString("timeFormat",l.getString(l.TIME_FORMAT,0,0));
		dest[1] += " "+t;
	}

	return dest;
}

//===================================================================
public Object getInfo(int value,Object source,Object resultDestination,int options)
//===================================================================
{
	try{
	EweEntry ze = myEntry;
	if (ze != null)
		if (ze.linkTo != null)
			ze = ze.linkTo;
	switch(value){
			case INFO_DEVICE_NAME:
				return eweFileName;
			case INFO_DEVICE_ICON:
				if (eweIcon == null)
					eweIcon = new ewe.fx.mImage("ewe/ewesmall.bmp",ewe.fx.Color.White);
				return eweIcon;
			case INFO_FLAGS:
				if (!(resultDestination instanceof ewe.sys.Long))
					resultDestination = new ewe.sys.Long();
				((ewe.sys.Long)resultDestination).value =
				FLAG_CASE_SENSITIVE|FLAG_SLOW_LIST|(!simulateCanWrite ? FLAG_FILE_SYSTEM_IS_READ_ONLY : 0);
				return resultDestination;
			case INFO_TOOL_TIP:
				String ret = getFileExt();
				if (isDirectory()) return ret;
				String [] dest = getDetails(ze,null,source,false);
				ret += "\n"+dest[0]+" bytes.";
				return ret;
			case INFO_DETAIL_NAMES:
				return mString.split("Length|Date");
			case INFO_DETAIL_WIDTHS:
				PropertyList pl = source instanceof PropertyList ? (PropertyList)source : new PropertyList();
				ewe.sys.Locale l = (ewe.sys.Locale)pl.getValue("locale",ewe.sys.Vm.getLocale());
				String format = pl.getString("dateFormat",l.getString(l.LONG_DATE_FORMAT,0,0));
				return mString.split("LengthXX|XX-XXXX-XX");
			case INFO_DETAILS:
				return getDetails(ze,null,source,true);
	}
	}catch(Exception e){
		//e.printStackTrace();
	}
	return null;
}
//===================================================================
public Stream toReadableStream() throws IOException
//===================================================================
{
	Stream ret = null;
	if (myEntry != null)
		ret = eweFile.getInputStream(myEntry);
	if (ret != null) return ret;
	throw new IOException("Could not read: "+myEntry);
}
//##################################################################
}
//##################################################################

/*
public static void main(String args[]) throws IOException
{
	ewe.sys.Vm.startEwe(args);
	new EweFile(new File(args[0])).close();
	ewe.sys.Coroutine.sleep(-1);
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

