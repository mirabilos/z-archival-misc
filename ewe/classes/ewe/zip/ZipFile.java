/* java.util.zip.ZipFile
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of Jazzlib.

Jazzlib is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

Jazzlib is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License. */

package ewe.zip;
import ewe.io.*;
import ewe.sys.Vm;
import ewe.util.*;
import ewe.zip.ZipEntry;
/*
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.NoSuchElementException;
*/
/**
 * This class represents a Zip archive.  You can ask for the contained
 * entries, or get an input stream for a file entry.  The entry is
 * automatically decompressed.
 *
 * This class is thread safe:  You can open input streams for arbitrary
 * entries in different threads.
 *
 * @author Jochen Hoenicke
 */
//##################################################################
public class ZipFile extends ewe.util.Errorable implements ZipConstants,ewe.util.Comparer
//##################################################################
{
  private String name;
  protected RandomAccessStream raf;
  ZipEntry[] entries;
	public String zipName = "Zip File";

	//===================================================================
	public int compare(Object one,Object two)
	//===================================================================
	{
		if (one == two) return 0;
		if (two == null) return 1;
		else if (one == null) return -1;
		else return ((ZipEntry)one).compareTo(two);
	}
	/**
	* Call this after creating the ZipFile to ensure that it was opened
	* properly.
	**/
	//===================================================================
	public boolean isOpen()
	//===================================================================
	{
		if (raf == null) return false;
		return raf.isOpen();
	}
	/**
	* Opens a Zip file from the RandomAccessStream.
	**/
	//===================================================================
	public ZipFile(RandomAccessStream stream)
	//===================================================================
	{
		this(stream,null);
	}
  /**
   * Opens a Zip file with the given name for reading.
   * @exception IOException if a i/o error occured.
   * @exception ZipException if the file doesn't contain a valid zip
   * archive.
   */
	//===================================================================
  public ZipFile(File file) throws IOException
	//===================================================================
  {

    this(file.toRandomAccessStream("r"));
    this.name = file.getFullPath();
		zipName = file.getFileExt();
  }
  /**
   * Opens a Zip file reading the given File.
   * @exception IOException if a i/o error occured.
   * @exception ZipException if the file doesn't contain a valid zip
   * archive.
   */
	//===================================================================
  public ZipFile(String name) throws IOException// throws ZipException, IOException
	//===================================================================
  {
		this(ewe.sys.Vm.newFileObject().getNew(name));
  }

	/**
	* This should only be called from a Coroutine if the handle is not null. It does not return
	* until the zip file is fully opened unless the stop() method is called on the handle.
	**/
  public ZipFile(RandomAccessStream stream,ewe.sys.Handle h)
	{
		this.raf = stream;
		if (stream.isOpen()){
			if (!readEntries(h)) {
				close();
				if (h != null) {
					h.set(h.Failed);
				}
			}else
				if (h != null) {
					h.set(h.Succeeded);
				}
		}else{
			error = "Could not open file or stream.";
		}
	}

	static byte [] buff = new byte[2];
  /**
   * Read an unsigned short in little endian byte order.
   */
  private final int readLeShort() {//throws IOException {
		if (IO.readFully(raf,buff) != 2) return 0;
    return (buff[0] & 0xff) | (buff[1] & 0xff) << 8;
  }

  /**
   * Read an int in little endian byte order.
   */
  private final int readLeInt() {//throws IOException {
    return readLeShort() | readLeShort() << 16;
  }

	public ZipEntry root = null;
  /**
   * Read the central directory of a zip file and fill the entries
   * array.  This is called exactly once by the constructors.
	 * Returns false if there was an error reading the entries.
   */

	//===================================================================
	ZipEntry getTree(ewe.sys.Handle h)
	//===================================================================
	{
		ewe.sys.Locale l = ewe.sys.Vm.getLocale();
		if (root != null) return root;
		if (entries == null) return null;
		Vector subs = new Vector();
		SubString ss = new SubString();
		root = new ZipEntry("/");
		root.isDir = true;
		int st = 0;
		for (int i = 0; i<entries.length; i++){
			if (h != null){
				ewe.sys.Coroutine.nap(20,0);
				if (h.shouldStop) return null;
			}
			//ewe.sys.Vm.debug("-- "+i);
			ZipEntry entry = entries[i];
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
			ZipEntry cur = root;
			for(int depth = 0; depth<len; depth++){
				int num = cur.getChildCount();
				SubString mine = (SubString)subs.get(depth);
				boolean found = false;
				ZipEntry child = null;
				for (int tn = 0; tn<num; tn++){
					child = (ZipEntry)cur.getChild(tn);
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
					child = new ZipEntry(mine.toString());
					child.isDir = true;
				}
				child.myFileName = new SubString().set(mine.data,mine.start,mine.length);
				cur.addChild(child);
				//ewe.sys.Vm.debug(cur.myFileName+"->"+child.myFileName.toString());
				cur = child;
			}
/*
			int len = ss.set(ch,0,ch.length).split('/',subs);
			//boolean isADir;
			if (ch.length != 0)
				if (ch[ch.length-1] == '/'){
					len--;
					entry.isDir = true;
				}
			ZipEntry cur = root;
			for(int depth = 0; depth<len; depth++){
				int num = cur.getChildCount();
				SubString mine = (SubString)subs.get(depth);
				boolean found = false;
				ZipEntry child = null;
				for (int tn = 0; tn<num; tn++){
					child = (ZipEntry)cur.getChild(tn);
					SubString cs = child.myFileName;
					if (SubString.compare(cs.data,cs.start,cs.length,mine.data,mine.start,mine.length) == 0){
						cur = child;
						found = true;
						if (depth == len-1) cur.linkTo = entry;
						break;
					}
				}
				if (found) continue;
				if (depth == len-1) {
					child = entry;
				}else {
					child = entry;
					child = new ZipEntry(mine.toString());
					child.isDir = true;
				}
				child.myFileName = new SubString().set(mine.data,mine.start,mine.length);
				cur.addChild(child);
			}
		*/
//.....................................................................
		}
		return root;
	}

	//-------------------------------------------------------------------
	private boolean readFully(RandomAccessStream rs, byte[] dest, int offset, int length)
	//-------------------------------------------------------------------
	{
		//return IO.readFully(rs,dest,offset,length) == length;
		try{
			IO.read(rs,dest,offset,length,true,true);
			return true;
		}catch(IOException e){
			return false;
		}
	}
	//-------------------------------------------------------------------
  private boolean readEntries(ewe.sys.Handle h) // throws ZipException, IOException
	//-------------------------------------------------------------------
  {
	//Debug.startTiming("Reading Entries");
	byte[] values = new byte[24];
	byte[] all = new byte[4+(C_COMPRESSION_METHOD-C_VERSION_MADE_BY)+values.length+(C_RELATIVE_OFFSET_LOCAL_HEADER-C_DISK_NUMBER_START)+4];
	byte[] buffer = null;
	try{
    int fileLen = raf.getLength();
    raf.seek(fileLen - EC_SIZE);
    if (readLeInt() != EC_HDR_SIG)
      return returnError("Missing End of Central Directory",false);
    if (!IO.skip(raf,EC_TOTAL_ENTRIES_CENTRAL_DIR - EC_NUMBER_THIS_DISK))
    	return returnError("Unexpected EOF",false);
    int count = readLeShort();
    if (!IO.skip(raf,EC_OFFSET_START_CENTRAL_DIRECTORY - EC_SIZE_CENTRAL_DIRECTORY))
    	return returnError("Unexpected EOF",false);
    int centralOffset = readLeInt();
    entries = new ZipEntry[count];
    raf.seek(centralOffset);
    for (int i = 0; i < count; i++){
			if (h != null){
				ewe.sys.Coroutine.nap(30,0);
				if (h.shouldStop)
					return returnError("User aborted Zip Open",false);
			}
			if (!readFully(raf,all,0,all.length))
				return returnError("Unexpected EOF in Entry",false);
			int head = (all[0]&0xff)|((all[1]&0xff)<<8)|((all[2]&0xff)<<16)|((all[3]&0xff)<<24);
			if (head != C_HDR_SIG)
				return returnError("Wrong Central Directory signature",false);
			/*
			if (readLeInt() != C_HDR_SIG)
				return returnError("Wrong Central Directory signature",false);
			if (!IO.skip(raf,C_COMPRESSION_METHOD - C_VERSION_MADE_BY))
		    return returnError("Unexpected EOF",false);
			if (!readFully(raf,values,0,24))
 				 return returnError("Unexpected EOF",false);
			*/
			System.arraycopy(all,4+(C_COMPRESSION_METHOD - C_VERSION_MADE_BY),values,0,24);
			int method = (values[0]&0xff)|((values[1]&0xff)<<8);
			int dostime = (values[2]&0xff)|((values[3]&0xff)<<8)|((values[4]&0xff)<<16)|((values[5]&0xff)<<24);
			int crc = (values[6]&0xff)|((values[7]&0xff)<<8)|((values[8]&0xff)<<16)|((values[9]&0xff)<<24);
			int csize = (values[10]&0xff)|((values[11]&0xff)<<8)|((values[12]&0xff)<<16)|((values[13]&0xff)<<24);
			int size = (values[14]&0xff)|((values[15]&0xff)<<8)|((values[16]&0xff)<<16)|((values[17]&0xff)<<24);
			int nameLen = (values[18]&0xff)|((values[19]&0xff)<<8);
			int extraLen = (values[20]&0xff)|((values[21]&0xff)<<8);
			int commentLen = (values[22]&0xff)|((values[23]&0xff)<<8);
			/*
			int method = readLeShort();
			int dostime = readLeInt();
			int crc = readLeInt();
			int csize = readLeInt();
			int size = readLeInt();
			int nameLen = readLeShort();
			int extraLen = readLeShort();
			int commentLen = readLeShort();
			if (!IO.skip(raf,C_RELATIVE_OFFSET_LOCAL_HEADER - C_DISK_NUMBER_START))
		    return returnError("Unexpected EOF",false);
			int offset = readLeInt();
			*/
			int end = all.length-4;
			int offset = (all[end]&0xff)|((all[end+1]&0xff)<<8)|((all[end+2]&0xff)<<16)|((all[end+3]&0xff)<<24);
			int need = nameLen > commentLen ? nameLen : commentLen;
			if (buffer == null || buffer.length < need) buffer = new byte[need];
			if (!readFully(raf,buffer, 0, nameLen))
				return returnError("Error in zip entry.",false);
			//ewe.sys.Vm.debug("Length: "+nameLen);
			String name = new String(ewe.util.Utils.decodeJavaUtf8String(buffer,0,nameLen,null,0));
			//ewe.sys.Vm.debug("Did: "+nameLen);
			ZipEntry entry = new ZipEntry(name);
			entry.setMethod(method);
			entry.setCrc(crc);
			entry.setSize(size);
			entry.setCompressedSize(csize);
			entry.setDOSTime(dostime);
			if (extraLen > 0)
			  {
			    byte[] extra = new byte[extraLen];
			    if (!readFully(raf,extra,0,extraLen))
						return returnError("Error in zip entry.",false);
			    entry.setExtra(extra);
			  }
			if (commentLen > 0)
			  {
			    if (!readFully(raf,buffer, 0, commentLen))
						return returnError("Error zip entry.",false);
			    entry.setComment(new String(ewe.util.Utils.decodeJavaUtf8String(buffer, 0, commentLen,null,0)));
			  }
			entry.zipFileIndex = i;
			entry.offset = offset;
			entries[i] = entry;
		}
		//ewe.util.Utils.sort(entries,this,false);
		}finally{
			//Debug.endTiming();
		}
		return true;
  }

  /**
   * Closes the ZipFile.  This also closes all input streams given by
   * this class.  After this is called, no further method should be
   * called.
   * @exception IOException if a i/o error occured.
   */
  public boolean close()// throws IOException
  {
    entries = null;
		if (raf != null) raf.close();
		raf = null;
		return true;
  }

  /**
   * Returns an iterator of all Zip entries in this Zip file.
   */
  public Iterator entries()
  {
    if (entries == null) return null;
      //throw new IllegalStateException("ZipFile has closed");
    return new ZipEntryIterator(entries);//Enumeration(entries);
  }

  private int getEntryIndex(String name)
  {
    for (int i = 0; i < entries.length; i++)
      if (name.equals(entries[i].getName()))
	return i;
    return -1;
  }

  /**
   * Searches for a zip entry in this archive with the given name.
   * @param the name. May contain directory components separated by
   * slashes ('/').
   * @return the zip entry, or null if no entry with that name exists.
   * @see #entries */
  public ZipEntry getEntry(String name)
  {
    if (entries == null) return null;
      //throw new IllegalStateException("ZipFile has closed");
    int index = getEntryIndex(name);
    return index >= 0 ? (ZipEntry) entries[index]:null;//.clone() : null;
  }

  /**
   * Checks, if the local header of the entry at index i matches the
   * central directory, and returns the offset to the data.
   * @return the start offset of the (compressed) data or -1 on error.
   */
  private int checkLocalHeader(ZipEntry entry)// throws IOException
  {
	raf.seek(entry.offset);
	if (readLeInt() != L_HDR_SIG)
		return returnError("Wrong Local header signature",-1);

	/* skip version and flags */
	if (!IO.skip(raf,L_COMPRESSION_METHOD - L_VERSION_NEEDED_TO_EXTRACT))
		return returnError("Unexpected End of File",-1);

	if (entry.getMethod() != readLeShort())
	 	return returnError("Compression method mismatch",-1);

	/* Skip time, crc, size and csize */
	if (!IO.skip(raf,L_FILENAME_LENGTH - L_LAST_MOD_FILE_TIME))
		return returnError("Unexpected End of File",-1);

	if (entry.getName().length() != readLeShort())
	  return returnError("File name length mismatch",-1);

	int extraLen = entry.getName().length() + readLeShort();
	return entry.offset + L_SIZE + extraLen;
  }

  /**
   * Creates an input stream reading the given zip entry as
   * uncompressed data.  Normally zip entry should be an entry
   * returned by getEntry() or entries().
   * @return the input stream or null on error.
   */
  public Stream getInputStream(ZipEntry entry)// throws IOException
  {
    if (entries == null) return (Stream)returnError("ZipFile has closed",null);
    int index = entry.zipFileIndex;
    if (index < 0 || index >= entries.length
				|| entries[index].getName() != entry.getName())
     {
			index = getEntryIndex(entry.getName());
			if (index < 0)
				return (Stream)returnError("That is not a valid entry",null);
     }

    int start = checkLocalHeader(entries[index]);
    int method = entries[index].getMethod();
    Stream is = new PartialInputStream
      (raf, start, entries[index].getCompressedSize());
    switch(method){
      case ZipOutputStream.STORED:
				return is;
      case ZipOutputStream.DEFLATED:
      			//Vm.debug("Starting at: "+start);
      			//Vm.debug("Inflating: "+entries[index].getCompressedSize()+" to: "+entries[index].getSize());
				return new InflaterInputStream(is,true);
      default:
				return (Stream)returnError("Unknown compression method " + method,null);
    }
  }

  /**
   * Returns the name of this zip file.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the number of entries in this zip file.
   */
  public int size()
  {
		if (entries == null) return returnError("ZipFile has closed",0);
		return entries.length;
  }

	public ZipEntry getEntry(int which)
	{
		if (entries == null) return null;
		return entries[which];
	}

/*
//===================================================================
public int findFirst(String )
//===================================================================
{
	int size = size();
	if (fs == null || size < 1) return 0;
	boolean flip = ((fs.type & table.SORT_DESCENDING) != 0);
	int ul = size, ll = -1;
	while(true) {
		if (ul-ll <= 1) {
			//System.out.println(ul);
			return ul;
		}
		int where = ((ul-ll)/2)+ll;
		int cmp = comparer.compare(searchData,get(where,buffer,data));
		if (flip) cmp = -cmp;
		if (cmp > 0) ll = where;
		else ul = where;
	}
}
*/
//##################################################################
}
//##################################################################

//##################################################################
class ZipEntryIterator extends IteratorEnumerator {
//##################################################################
  ZipEntry[] array;
  int ptr = 0;

  public ZipEntryIterator(ZipEntry[] arr)
  {
    array = arr;
  }

  public boolean hasNext()
  {
		if (array == null) return false;
    return ptr < array.length;
  }

  public Object next()
  {
  	return array[ptr++];
  }
//##################################################################
}
//##################################################################

