/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
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
package ewe.database;
import ewe.io.CorruptedDataException;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.sys.Handle;
import ewe.util.ByteArray;
import ewe.util.CompareInts;
import ewe.util.Comparer;
import ewe.util.Debug;
import ewe.util.IntArray;
import ewe.util.Utils;
//##################################################################
public class RecordFoundEntries extends FoundEntriesObject{
//##################################################################
//-------------------------------------------------------------------
protected RecordFoundEntries(Database database)
//-------------------------------------------------------------------
{
	super(database);
	ids = new IntArray();
}
//-------------------------------------------------------------------
protected void copyEntriesFrom(FoundEntriesObject from,int start,int length)
//-------------------------------------------------------------------
{
	clearEntries();
	addEntriesFrom(from,start,length);
}
//-------------------------------------------------------------------
protected void clearEntries()
//-------------------------------------------------------------------
{
	ids.clear();
}
//-------------------------------------------------------------------
protected void addEntriesFrom(FoundEntriesObject from,int start,int length)
//-------------------------------------------------------------------
{
	IntArray other = ((RecordFoundEntries)from).ids;
	ids.append(other.data,start,length);
}
//-------------------------------------------------------------------
protected int doChange(int index,DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	return doStoreChange(index,entry,false);
}
//-------------------------------------------------------------------
protected int doStore(int index,DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	return doStoreChange(index,entry,true);
}
//-------------------------------------------------------------------
protected int doStoreChange(int index,DatabaseEntryObject entry,boolean isStore) throws IOException
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doStoreChange()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doStoreChange");
	}
	*/
	RecordDatabaseEntry e = (RecordDatabaseEntry)entry;
	try{
		int was = ids.data[index];
		e.stored = was;
		if (isStore) e.store();
		else e.save();
		ids.data[index] = e.stored;
		int where = index;
		if (sortState){
			ids.removeAtIndex(index);
			where = findInsertIndex(e);
			ids.insert(e.stored,where);
		}
		recordChange(index,where,e);
		return where;
	}finally{
		/*
		if (!validateEntriesData("End of doStoreChange()")){
			DatabaseManager.logError("caused by doStoreChange("+index+", "+e.stored+", "+isStore+")");
		}
		*/
	}
}
/*
//-------------------------------------------------------------------
protected int doAppend(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	RecordDatabaseEntry e = (RecordDatabaseEntry)entry;
	e.stored = 0;
	e.save();
	ids.insert(e.stored,ids.length);
	recordChange(-1,ids.length-1,e.stored);
	return ids.length-1;
}
*/
//-------------------------------------------------------------------
protected int doAdd(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doAdd()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doAdd");
	}
	*/
	RecordDatabaseEntry e = (RecordDatabaseEntry)entry;
	try{
		e.stored = 0;
		e.save();
		int where = findInsertIndex(e);
		/*
		if (!validateEntries()){
			DatabaseManager.logError("At start of add()");
		}
		*/
		ids.insert(e.stored,where);
		/*
		if (!validateEntries()){
			DatabaseManager.logError("Caused by insert: "+e.stored+" to: "+where);
		}
		*/
		recordChange(-1,where,e);
		return where;
	}finally{
		/*
		if (!validateEntriesData("End of doAdd()")){
			DatabaseManager.logError("caused by doAdd("+e.stored+")");
		}
		*/
	}
}

//-------------------------------------------------------------------
protected void doDelete(int index,DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doDelete()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doDelete");
	}
	*/
	try{
		entry.delete();
		ids.removeAtIndex(index);
		recordChange(index,-1,null);
	}finally{
		/*
		if (!validateEntriesData("End of doDelete()")){
			DatabaseManager.logError("caused by doDelete("+index+")");
		}
		*/
	}
}
//-------------------------------------------------------------------
protected void doErase(int index,DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doErase()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doErase");
	}
	*/
	try{
		entry.erase();
		ids.removeAtIndex(index);
		recordChange(index,-1,null);
	}finally{
		/*
		if (!validateEntriesData("End of doErase()")){
			DatabaseManager.logError("caused by doErase("+index+")");
		}
		*/
	}
}
//-------------------------------------------------------------------
protected int doInclude(DatabaseEntryObject entry) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doInclude()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doInclude");
	}
	*/
	int where = findInsertIndex(entry);
	try{
		int now = ((RecordDatabaseEntry)entry).stored;
		ids.insert(now,where);
		recordChange(-1,where,entry);
		return where;
	}finally{
		/*
		if (!validateEntriesData("End of doInclude()")){
			DatabaseManager.logError("caused by doInclude("+where+")");
		}
		*/
	}
}
//-------------------------------------------------------------------
protected void doUpdate(int index,DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doUpdate()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doUpdate");
	}
	*/
	int[] data = ids.data;
	try{
	//
	// Bug in MS Java Compiler 6.00.8424 when in release mode - Aug. 2003
	// This generates an ArrayIndexOutOfBounds exception.
	///
	// int was = index == -1 ? 0 : ids.data[index];
	//
	// Have to do it like this.
	//
	int was = 0;
	if (index >= 0) was = data[index];
	//
	int now = ((RecordDatabaseEntry)entry).stored;
	if (index != -1) ids.removeAtIndex(index);
	int where = findInsertIndex(entry);
	ids.insert(now,where);
	if (now == 0) throw new IOException("doUpdate() with value of 0");
	//if (!validateEntries()) throw new IOException("doUpdate() caused 0 to be inserted");
	recordChange(index,where,entry);
	}catch(IndexOutOfBoundsException e){
		String msg = "Index: "+index+", in data of length: "+(data == null ? "null" : (""+data.length))+", in buffer length: "+ids.length;
		throw new IndexOutOfBoundsException(msg);
	}finally{
		/*
		if (!validateEntriesData("End of doUpdate()")){
			DatabaseManager.logError("caused by doUpdate("+index+")");
		}
		*/
	}
}
//-------------------------------------------------------------------
protected void doExclude(int index)
//-------------------------------------------------------------------
{
	/*
	if (!validateEntriesData("Start of doExclude()")){
		throw new IndexOutOfBoundsException("Bad ids.data in doExclude");
	}
	*/
	try{
		ids.removeAtIndex(index);
		recordChange(index,-1,null);
	}catch(IOException e){
	}finally{
		/*
		if (!validateEntriesData("End of doExclude()")){
			DatabaseManager.logError("caused by doExclude("+index+")");
		}
		*/
	}
}
//-------------------------------------------------------------------
protected void doLoad(int index,DatabaseEntryObject data) throws IOException
//-------------------------------------------------------------------
{
	((RecordDatabaseEntry)data).stored = ids.data[index];
	data.load();
}
//===================================================================
public int indexOf(DatabaseEntry entry)
//===================================================================
{
	if (entry == null) return -1;
	return ids.indexOf(((RecordDatabaseEntry)entry).stored);
}
//##################################################################
class compareInts implements CompareInts{
//##################################################################
private Comparer comparer;
private RecordDatabaseEntry entryOne, entryTwo;
//===================================================================
public compareInts(Comparer comparer)
//===================================================================
{
	this.comparer = comparer;
	entryOne = (RecordDatabaseEntry)getNew();
	entryTwo = (RecordDatabaseEntry)getNew();
}
//===================================================================
public int compare(int one, int two)
//===================================================================
{
	if (one == two) return 0;
	if (one <= 0) return -1;
	if (two <= 0) return 1;

	try{
		if (entryOne.stored != one){
			entryOne.stored = one;
			entryOne.load();
		}
		if (entryTwo.stored != two){
			entryTwo.stored = two;
			entryTwo.load();
		}
	}catch(IOException e){
		throw new DatabaseIOException(e);
	}
	return comparer.compare(entryOne,entryTwo);
}
//##################################################################
}
//##################################################################

//-------------------------------------------------------------------
protected boolean doSortMe(Handle h,Comparer comparer) throws IOException
//-------------------------------------------------------------------
{
	IntArray toSort = ids;
	try{
		if (!Utils.sort(h,toSort.data,toSort.length,new compareInts(comparer),false))
			return false;
	}catch(DatabaseIOException e){
		throw e.toIOException();
	}
	return true;
}


static byte [] indexBuffer = new byte[16];
ByteArray recorder = new ByteArray();
private static boolean hasNative = true;

//-------------------------------------------------------------------
private static native boolean nativeRead(ByteArray source,IntArray dest);
private static native boolean nativeWrite(IntArray source,ByteArray dest);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
int appendInt(int value,ByteArray dest)
//-------------------------------------------------------------------
{
	int a = 0;
	if (value < -1 || value > 0x1fffffff){
		indexBuffer[0] = (byte)0xe0;
		indexBuffer[1] = (byte)((value >> 24) & 0xff);
		indexBuffer[2] = (byte)((value >> 16) & 0xff);
		indexBuffer[3] = (byte)((value >> 8) & 0xff);
		indexBuffer[4] = (byte)((value) & 0xff);
		a = 5;
	}else if (value == -1 || value <= 0x3f) {
		indexBuffer[0] = (byte)value;
		a = 1;
	}else if (value <= 0x3fff){
		indexBuffer[0] = (byte)(((value >> 8) & 0x3f)|0x40);
		indexBuffer[1] = (byte)((value) & 0xff);
		a = 2;
	}else if (value <= 0x3fffff){
		indexBuffer[0] = (byte)(((value >> 16) & 0x3f)|0x80);
		indexBuffer[1] = (byte)((value >> 8) & 0xff);
		indexBuffer[2] = (byte)((value) & 0xff);
		a = 3;
	}else{
		indexBuffer[0] = (byte)(((value >> 24) & 0x1f)|0xc0);
		indexBuffer[1] = (byte)((value >> 16) & 0xff);
		indexBuffer[2] = (byte)((value >> 8) & 0xff);
		indexBuffer[3] = (byte)((value) & 0xff);
		a = 4;
	}
	dest.append(indexBuffer,0,a);
	return a;
}
//-------------------------------------------------------------------
int readInt(byte[] source,int offset,int[] used)
//-------------------------------------------------------------------
{
	byte one = source[offset];
	used[0] = 1;
	int cx = one & 0xc0;
	if (one == (byte)0xff || cx == 0) return (int)one;
	else if (cx == 0x40){
		used[0] = 2;
		return ((one & 0x3f) << 8)|(source[offset+1] & 0xff);
	}else if (cx == 0x80){
		used[0] = 3;
		return ((one & 0x3f) << 16)|((source[offset+1] & 0xff) << 8)| (source[offset+2] & 0xff);
	}else if ((one & 0xe0) == 0xc0){
		used[0] = 4;
		return ((one & 0x1f) << 24)|((source[offset+1] & 0xff) << 16)|((source[offset+2] & 0xff) << 8)| (source[offset+3] & 0xff);
	}else if ((one & 0xe0) == 0xe0){
		used[0] = 5;
		return ((source[offset+1] & 0xff) << 24)|((source[offset+2] & 0xff) << 16)|((source[offset+3] & 0xff) << 8)| (source[offset+4] & 0xff);
	}else throw new IllegalArgumentException();
}
//-------------------------------------------------------------------
protected void recordChange(int oldIndex, int newIndex, DatabaseEntry e) throws IOException
//-------------------------------------------------------------------
{
	super.recordChange(oldIndex,newIndex,e);
	if (indexRecorder == null) return;
	if (recorder == null) recorder = new ByteArray();
	recorder.clear();
	appendInt(oldIndex,recorder);
	appendInt(newIndex,recorder);
	appendInt(e == null ? 0 : ((RecordDatabaseEntry)e).stored,recorder);
	indexRecorder.write(recorder.data,0,recorder.length);
}
/**
Write the FoundEntries to an OutputStream and put in the returnValue of the Handle,
an ewe.sys.Long object holding the number of bytes written. If stream is null then set the
returnValue of the Handle to be an ewe.sys.Long object that holds the number of bytes needed.
**/
//-------------------------------------------------------------------
protected boolean writeTo(OutputStream stream,Handle h) throws IOException
//-------------------------------------------------------------------
{
	ByteArray out = new ByteArray();
	out.data = new byte[ids.length*2];
	if (hasNative) try{
		if (!nativeWrite(ids,out)) return false;
		return writeAndReturnTrue(stream,out,h);
	}catch(SecurityException e){
		hasNative = false;
	}catch(UnsatisfiedLinkError e){
		hasNative = false;
	}
	int num = ids.length;
	appendInt(num,out);
	for (int i = 0; i<num; i++)
		appendInt(ids.data[i],out);
	return writeAndReturnTrue(stream,out,h);
}

//-------------------------------------------------------------------
protected boolean readFrom(InputStream stream,Handle h,int size) throws IOException
//-------------------------------------------------------------------
{
	//ewe.sys.Vm.debug("Reading");
	try{
		if (h != null) h.resetTime("Reading...");
		ByteArray in = new ByteArray();
		//ewe.sys.Vm.debug("Starting to read: "+((DatabaseIndex)this).getName());
		Debug.startTiming("Reading-"+size);
		int need = size;
		while(true){
			int toRead = need <= 0 ? 1024 : need;
			//ewe.sys.Vm.debug("Reading: "+toRead);
			in.makeSpace(in.length,toRead);
			in.length -= toRead;
			int got = stream.read(in.data,in.length,toRead);
			if (got == -1) break;
			in.length += got;
			if (need > 0){
				need -= got;
				if (need == 0){
					if (h != null) h.setProgress(1.0f);
					break;
				}
			}
			if (h != null) h.setProgress(size <= 0 ? -1f : (float)(in.length)/size);
		}
		Debug.startTiming("Decoding Native-"+size);
		//ewe.sys.Vm.debug("Read all, now decoding: "+((DatabaseIndex)this).getName());
		if (in.length == 0) return false;
		if (hasNative) try{
			ids.length = 0;
			if (!nativeRead(in,ids)) return false;
			int baseSize = readInt(in.data,0,new int[1]);
			if (baseSize*2 < ids.length && ids.length > 100) needCompact = true;
			/*
			int total = 0;
			for (int i = 0; i<ids.length; i++)
				total += ids.data[i];
			ewe.sys.Vm.debug("Done: "+ids.length+" = "+total);
			*/
			//ewe.sys.Vm.debug("Done: "+((DatabaseIndex)this).getName());
			Debug.endTiming();
			return true;
		}catch(SecurityException e){
			hasNative = false;
		}catch(UnsatisfiedLinkError e){
			hasNative = false;
		}
		Debug.startTiming("Decoding Java-"+size);
		int[] used = new int[1];
		try{
			int off = 0;
			int baseSize = readInt(in.data,off,used);
			off += used[0];
			if (baseSize < 0) baseSize = 0;
			ids.length = 0;
			ids.data = new int[baseSize+(baseSize/10)+10];
			for (int i = 0; i<baseSize; i++){
				ids.data[i] = readInt(in.data,off,used); off += used[0];
			}
			ids.length = baseSize;
			int max = in.length;
			while(off < max){
				int oldIndex = readInt(in.data,off,used); off += used[0];
				int newIndex = readInt(in.data,off,used); off += used[0];
				int newPos = readInt(in.data,off,used); off += used[0];
				if (oldIndex != -1){
					if (newIndex != oldIndex) {
						ids.removeAtIndex(oldIndex);
						if (newIndex == -1) continue;
						ids.makeSpace(newIndex,1);
					}
					ids.data[newIndex] = newPos;
				}else if (newIndex != -1){
					ids.makeSpace(newIndex,1);
					ids.data[newIndex] = newPos;
				}
				/*
				if (h != null)
					if (size < 0) h.setProgress(-1);
					//else h.setProgress((float)actuallyRead/size);
				*/
			}
			//ewe.sys.Vm.debug("Read");
			//ewe.sys.Vm.debug("Done: "+((DatabaseIndex)this).getName());
			/*
			int total = 0;
			for (int i = 0; i<ids.length; i++)
				total += ids.data[i];
			ewe.sys.Vm.debug("Done: "+ids.length+" = "+total);
			*/
			Debug.endTiming();
			if (baseSize*2 < ids.length && ids.length > 100) needCompact = true;
			return true;
		}catch(Exception e){
			//e.printStackTrace();
			//ewe.sys.mThread.nap(2000);
			throw new CorruptedDataException(e);
		}
	}finally{
		//validateEntriesData("After readFrom()");
	}
}
//-------------------------------------------------------------------
protected void indexFromFoundEntries(FoundEntries entries)
//-------------------------------------------------------------------
{
	RecordFoundEntries fe = (RecordFoundEntries)entries;
	copyStateFrom(fe);
	ids = fe.ids;
	fe.ids = new IntArray();
}
//##################################################################
}
//##################################################################

