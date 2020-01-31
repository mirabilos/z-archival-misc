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
package ewe.datastore;
import ewe.io.*;
import ewe.util.*;
//##################################################################
public class DataStorage{//Don't inherit from anything else!
//##################################################################
//================================ Don't move these.
public DataProcessor encryptor;
public DataProcessor decryptor;
ByteArray tempBytes;
//================================
public ewe.sys.Locale locale =  ewe.sys.Vm.getLocale();
int numChanges = 0;
static final int entryLen = 4*8; // Entry len must be a multiple of 16.
static final int tempLen = entryLen*2;
static final int preamble = 256;
static final byte [] fileTag = {(byte)0,(byte)'D',(byte)0,(byte)'A',(byte)0,(byte)'T',(byte)0,(byte)'A'};
byte [] temp = new byte[tempLen];
RandomAccessStream stream;
Changes changes = new Changes(this);
public String fileName = "Unknown";
public boolean isValid = false;
public String error = null;
ewe.io.File storageFile;

/**
* Set the File of this DataStorage. This is not necessary if you opened it using
* a File object or file name.
**/
//===================================================================
public void setDatastoreFile(File file)
//===================================================================
{
	storageFile = file;
}
//-------------------------------------------------------------------
void throwException(String errorMessage) throws IOException
//-------------------------------------------------------------------
{
	throw new IOException(error == null ? errorMessage : error);
}
//-------------------------------------------------------------------
void throwWriteError() throws IOException
//-------------------------------------------------------------------
{
	throwException("Error writing to DataStorage");
}
//-------------------------------------------------------------------
void throwReadError() throws IOException
//-------------------------------------------------------------------
{
	throwException("Error reading from DataStorage");
}
static boolean test(boolean retValue,DataStorage storage,boolean isRead) throws IOException
{
	if (!retValue)
		if (isRead) storage.throwReadError();
		else storage.throwWriteError();
	return retValue;
}
//-------------------------------------------------------------------
DataTable table = new DataTable(this,null);
//-------------------------------------------------------------------



/**
 * Create a new DataStorage - but do not open the associated file. After using this constructor
 * you should use createNew() or open().
 * @param fileName The name of the file.
 * @deprecated - use one of the other constructors that automatically open and create instead.
 */
//===================================================================
public DataStorage(String fileName)
//===================================================================
{
	this.fileName = fileName;
	storageFile = File.getNewFile(fileName);
}
/**
* Utility used by the DataStorage.
**/
public static final void
//============================================================
	writeInt(int val,byte [] dest,int offset) {ewe.util.Utils.writeInt(val,dest,offset,4);}
//============================================================
/**
* Utility used by the DataStorage.
**/
public static final int
//============================================================
	readInt(byte [] source,int offset) {return ewe.util.Utils.readInt(source,offset,4);}
//============================================================

//===================================================================
final int readAnInt() throws IOException
//===================================================================
{
	IO.readAll(stream,temp,0,4);
	return readInt(temp,0);
}
/**
* Utility used by the DataStorage.
**/
public final int
//===================================================================
	readInt()
//===================================================================
{
	try{
		return readAnInt();
	}catch(IOException e){
		returnError(e);
		return 0;
	}
}
/**
* Utility used by the DataStorage.
**/
public final int
//===================================================================
	writeInt(int value)
//===================================================================
{
	try{
		writeInt(value,temp,0);
		stream.write(temp,0,4);
		return 4;
	}catch(IOException e){
		returnError(e);
		return 0;
	}
}
/**
* Utility used by the DataStorage.
**/
//===================================================================
public final int writeIntAt(int location,int val)
//===================================================================
{
	stream.seek(location);
	return writeInt(val);
}
/**
* Utility used by the DataStorage.
**/
//===================================================================
public final int readIntAt(int location)
//===================================================================
{
	stream.seek(location);
	return readInt();
}

//===================================================================
boolean flush()
//===================================================================
{
	try{
		stream.flush();
		return true;
	}catch(IOException e){
		return false;
	}
}
//-------------------------------------------------------------------
protected WeakSet entries = new WeakSet();
//-------------------------------------------------------------------
protected static entryFinder finder = new entryFinder();
//-------------------------------------------------------------------
DataEntry getEntryAt(int location)
//-------------------------------------------------------------------
{
	if (location == 0) return null;
	DataEntry found = (DataEntry)entries.find(finder.set(location));
	if (found != null) return found;
	found = readEntryAt(location);
	//if (found != null) entries.add(found);
	return found;
}

/**
 * This gets the root (/) entry of the DataStorage. If this returns null an error has occured.
 * @return The root entry of the DataStorage. If this returns null an error has occured.
 * @deprecated use getRootEntry() instead.
 */
//===================================================================
public DataEntry getRoot()
//===================================================================
{
	return getEntryAt(preamble);
}
/**
 * Gets the root (/) entry of the DataStorage - which must always exist in a properly formatted DataStorage file.
 * @return The root (/) entry of the DataStorage
 * @exception IOException If the root entry could not be found.
 */
//===================================================================
public DataEntry getRootEntry() throws IOException
//===================================================================
{
	DataEntry rt = getRoot();
	if (rt == null) throw new IOException("Root entry not found - bad file format.");
	return rt;
}
/**
 * This returns the entry that holds all freed entries. Do not use this method.
 */
//===================================================================
public DataEntry getFree()
//===================================================================
{
	return getEntryAt(preamble+entryLen);
}
//===================================================================
DataEntry readEntryAt(int location)
//===================================================================
{
	try{
		if (location == 0) return null;
		stream.seek(location);
		DataEntry de = new DataEntry(this);
		de.myLocation = location;
		IO.readAll(stream,temp,0,entryLen);
		de.next = readInt(temp,0);
		de.prev = readInt(temp,4);
		de.parent = readInt(temp,8);
		de.children = readInt(temp,12);
		de.dataLocation = readInt(temp,16);
		de.allocatedLength = readInt(temp,20);
		return de;
	}catch(IOException e){
		returnError(e);
		return null;
	}
}

//-------------------------------------------------------------------
ByteArray getDataEntryData(int location,ByteArray dest)
//-------------------------------------------------------------------
{
	try{
	if (location == 0) return null;
	stream.seek(location+16);
	IO.readAll(stream,temp,0,8);
	int where = readInt(temp,0);
	int size = readInt(temp,4);
	if (dest == null) dest = new ByteArray();
	dest.clear();
	if (where == 0) return dest;
	if (!stream.seek(where)) return null;
	if (decryptor != null) size = readInt();
	dest.makeSpace(0,size);
	IO.readAll(stream,dest.data,0,size);
	if (decryptor != null){
		int bs = decryptor.getBlockSize();
		if (bs < 1) bs = 1;
		size -= size%bs; //We would have padded UP when we encrypted.
		byte [] src = dest.data;
		dest.data = new byte[size];
		try{
			dest = decryptor.processBlock(src,0,size,true,dest);
		}catch(IOException e){
			returnError(e);
			dest = null;
		}
	}
		//ewe.io.IO.processInPlace(decryptor,dest.data,0,size);
	return dest;
	}catch(IOException e){
		e.printStackTrace();
		returnError(e);
		return null;
	}

}
//-------------------------------------------------------------------
ByteArray readEntryData(DataEntry de,ByteArray dest)
//-------------------------------------------------------------------
{
	if (de == null) return null;
	if (de.dataLocation == 0) return null;
	return getDataEntryData(de.dataLocation,dest);
}
//===================================================================
boolean writeEntryAt(DataEntry de,int location)
//===================================================================
{
	try{
		if (location == 0) return false;
		if (!stream.seek(location)) return false;
		writeInt(de.next,temp,0);
		writeInt(de.prev,temp,4);
		writeInt(de.parent,temp,8);
		writeInt(de.children,temp,12);
		writeInt(de.dataLocation,temp,16);
		writeInt(de.allocatedLength,temp,20);
		stream.write(temp,0,entryLen);
		return true;
	}catch(IOException e){
		return returnError(e);
	}
}
//===================================================================
protected boolean pad(int length)
//===================================================================
{
	try{
		if (length <= 0) return true;
		for (int i = 0; i<tempLen; i++) temp[i] = 0;
		while(length > 0){
			int tw = (length > tempLen) ? tempLen : length;
			stream.write(temp,0,tw);
			length -= tw;
		}
		return true;
	}catch(IOException e){
		return returnError(e);
	}
}
private byte [] encrypted = new byte[0];
//===================================================================
boolean changeEntryData(DataEntry de,int dataLocation,byte [] data,int dataOffset,int dataLen,int allocatedLength)
//===================================================================
{
	try{
		if (!stream.seek(dataLocation)) return false;
		/*
		if (encryptor != null){
			if (encrypted.length < dataLen) encrypted = new byte[dataLen];
			ewe.sys.Vm.copyArray(data,dataOffset,encrypted,0,dataLen);
			ewe.io.IO.processInPlace(encryptor,encrypted,0,dataLen);
			if (stream.writeBytes(encrypted,0,dataLen) != dataLen) return false;
		}else{
			if (stream.writeBytes(data,dataOffset,dataLen) != dataLen) return false;
		}
		*/
		if (encryptor != null)
			if (writeInt(dataLen) != 4) return false;
		stream.write(data,dataOffset,dataLen);
		if (!pad(allocatedLength-(dataLen+(encryptor != null ? 4 : 0)))) return false;
		de.allocatedLength = allocatedLength;
		de.dataLocation = dataLocation;
		// Use a change structure here.
		return writeEntryAt(de,de.myLocation);
	}catch(IOException e){
		return returnError(e);
	}
}
//-------------------------------------------------------------------
boolean returnError(Exception e)
//-------------------------------------------------------------------
{
	error = e == null ? "IO Error occured" : e.getMessage();
	return false;
}
//-------------------------------------------------------------------
boolean writeEntryData(DataEntry de,byte [] data,int dataOffset,int dataLen)
//-------------------------------------------------------------------
{
	int extra = 0;
	if (encryptor != null){
		extra += 4;
		int bs = encryptor.getBlockSize();
		if (bs <= 1) bs = 1;
		if ((dataLen % bs) != 0){
			byte [] d2 = new byte[((dataLen+bs)/bs)*bs];
			ewe.sys.Vm.copyArray(data,dataOffset,d2,0,dataLen);
			data = d2;
			dataLen = d2.length;
			dataOffset = 0;
		}
		try{
			tempBytes = encryptor.processBlock(data,dataOffset,dataLen,true,tempBytes);
			if (tempBytes == null) return false;
			dataLen = tempBytes.length;
			data = tempBytes.data;
			dataOffset = 0;
		}catch(IOException e){
			return returnError(e);
		}
	}
	if (de.dataLocation == 0 || (dataLen+extra) > de.allocatedLength) {
		if (!findOrMakeSpace(alignTo16(((dataLen+extra)*12)/10),space)) return false;
		return changeEntryData(de,space[0],data,dataOffset,dataLen,space[1]);
	}
	else return changeEntryData(de,de.dataLocation,data,dataOffset,dataLen,de.allocatedLength);
}
/*
//-------------------------------------------------------------------
ByteArray readEntryData(DataEntry de,ByteArray dest)
//-------------------------------------------------------------------
{
	if (de.dataLocation == 0) return null;
	if (dest == null) dest = new ByteArray();
	dest.clear();
	dest.makeSpace(0,de.allocatedLength);
	if (!stream.seek(de.dataLocation)) return null;
	if (stream.readBytes(dest.data,0,de.allocatedLength) != de.allocatedLength) return null;
	if (decryptor != null)
		ewe.io.IO.processInPlace(decryptor,dest.data,0,de.allocatedLength);
	return dest;
}
*/

//===================================================================
boolean padTo16()
//===================================================================
{
	int cur = stream.getLength();
	if (!stream.seek(cur)) return false;
	int aligned = (cur+0xf) & 0xfffffff0;
	if (!pad(aligned-cur)) return false;
	return true;
}

final int [] space = new int[2];

//===================================================================
boolean findFreeSpace(int size,int [] found)
//===================================================================
{
	size = alignTo16(size);
	if (size == 0) return false;
	return findFreeInChildren(getFree(),size,found);
}
/*
//===================================================================
boolean mergeFree(DataEntry toFree,DataEntry parent)
//===================================================================
{
	if (toFree == null) return false;
	boolean dataCanMerge = false;
	if (toFree.dataLocation == 0){
		toFree.allocatedLength = 0; //This is just to make sure. It should already be 0 if dataLocation is zero.
		dataCanMerge = true;
	}else
		dataCanMerge = (toFree.dataLocation == toFree.myLocation+entryLen);
	int fd = toFree.dataLocation;
	int fl = toFree.myLocation;
	for (DataEntry f = parent.getFirstChild(); f != null; f = f.getNext()){
			if (f.dataLocation == 0) continue;
			int end = f.dataLocation+f.allocatedLength;
			if (end ==
}
*/
//===================================================================
boolean findFreeInChildren(DataEntry parent,int size,int [] found)
//===================================================================
{
	for (DataEntry f = parent.getFirstChild(); f != null; f = f.getNext()){
		if (f.allocatedLength == 0 && f.children == 0){
			if (size <= entryLen) {
				remove(f); //Remove it from the free list.
				found[0] = f.myLocation;
				found[1] = entryLen;
				return true;
			}
		}else{
			if (f.allocatedLength >= size){
				if (f.allocatedLength-size > entryLen){ // Should it be split?
					found[0] = f.dataLocation;
					found[1] = size;
					f.allocatedLength -= size;
					f.dataLocation += size;
				}else{
					found[0] = f.dataLocation;
					found[1] = f.allocatedLength;
					f.allocatedLength = 0;
					f.dataLocation = 0;
				}
				return writeEntryAt(f,f.myLocation);
			}
		}
		if (f.children != 0)
			if (findFreeInChildren(f,size,found))
				return true;
	}
	return false;
}

//-------------------------------------------------------------------
boolean findOrMakeSpace(int size,int [] found)
//-------------------------------------------------------------------
{
	if (findFreeSpace(size,found)) return true;
	if (!padTo16()) return false;
	found[0] = stream.getLength();
	found[1] = size;
	return true;
}

/**
 * Create a new DataEntry that is not the child of any other data entry. If you call this method
 * you must then later add it as the child of another data entry, or delete it.
 * @return a new DataEntry in the DataStorage.
 */
//===================================================================
public DataEntry makeNewEntry()
//===================================================================
{
	if (!findOrMakeSpace(entryLen,space)) return null;
	DataEntry de = new DataEntry(this);
	de.myLocation = space[0];
	//entries.add(de);
	if (!writeEntryAt(de,de.myLocation)) return null;
	return de;
}
//===================================================================
int alignTo16(int value)
//===================================================================
{
	return (value+0xf) & 0xfffffff0;
}

/**
* Do not use! Open a DataStorage after constructing it with DataStorage(String file).
*
* @param mode should be RandomAccessStream.READ_ONLY or RandomAccessStream.READ_WRITE.
* @return true if it was opened successfully.
* @deprecated - use one of the constructors that automatically open the DataStorage instead.
*/
//===================================================================
public boolean open(int mode)
//===================================================================
{
	return open(ewe.sys.Vm.newFileObject().getNew(fileName).getRandomAccessStream(mode));
}
/**
* Do not use! Open a DataStorage after constructing it with DataStorage(String file).
*
* @param f an open RandomAccessStream.
* @return true if it was opened successfully.
* @deprecated - use one of the constructors that automatically open the DataStorage instead.
*/
//===================================================================
public boolean open(RandomAccessStream f)
//===================================================================
{
	try{
		stream = f;
		if (!stream.isOpen()) return false;
		if (!stream.seek(0)) return false;
		byte [] start = new byte[fileTag.length];
	 	IO.readAll(stream,start,0,start.length);
		for (int i = 0; i<start.length; i++)
			if (start[i] != fileTag[i])
				return false;
		byte [] lock = new byte[8];
		IO.readAll(stream,lock,0,1);
		int len = lock[0] & 0xff;
		if (decryptor == null){
			if (len != 0 && len != 8) return false;
			lock = new byte[8];
		}else{
			if (len < 8 || len > 32) return false;
			lock = new byte[len];
		}
		IO.readAll(stream,lock,0,lock.length);
		if (decryptor == null){
			for (int i = 0; i<lock.length; i++)
				if (lock[i] != 0) return false;
		}else{
			try{
				tempBytes = decryptor.processBlock(lock,0,lock.length,true,tempBytes);
				if (tempBytes.length != 8) return false;
				byte total = 0;
				for (int i = 0; i<tempBytes.length; i++) total += tempBytes.data[i];
				if (total != 0) return false;
			}catch(IOException e){return false;}
		}
		//
		if (changes.readChanges())
			changes.implementChanges();
		return isValid = true;
	}catch(IOException e){
		return isValid = returnError(e);
	}
}

/**
* Creates a new DataStorage and opens up the specified file. If the file
* does not exist and create is true, it will create a new one, otherwise
* it will open it in the specified mode (RandomAccessFile.READ_ONLY or READ_WRITE).
* After constructing you should check the isValid() flag to determine if it is
* valid.
* @deprecated - use one of the Constructors that throw an IOException instead.
**/
//===================================================================
public DataStorage(String file,int mode,boolean create)
//===================================================================
{
	isValid = false;
	if (file == null) return;
	fileName = file;
	File fl = storageFile = File.getNewFile(fileName);
	if (fl.canRead()) open(mode);
	else if (create) createNew();
}
/**
 * Open or create a DataStorage file. If the mode indicates read-write ("rw") AND the file does not
 * exist, then it will be created and initialized as a new DataStorage file.
 * @param file The data storage file.
 * @param mode must be "r" or "rw". An illegal argument exception will be thrown if it is neither.
 * @exception IOException if there is an error opening or creating the DataStorage file.
 */
//===================================================================
public DataStorage(File file,String mode) throws IOException, IllegalArgumentException
//===================================================================
{
	this(file,mode,null,null);
}
/**
 * Open or create a DataStorage file. If the mode indicates read-write ("rw") AND the file does not
 * exist, then it will be created and initialized as a new DataStorage file.
 * @param file The data storage file.
 * @param mode must be "r" or "rw". An illegal argument exception will be thrown if it is neither.
 * @param decryptor An optional decryptor - necessary for reading the file if it is encrypted.
 * @param encryptor An optional encryptor - necessary for writing to the file if it is encrypted.
 * @exception IOException if there is an error opening or creating the DataStorage file.
 */
//===================================================================
public DataStorage(File file,String mode,DataProcessor decryptor,DataProcessor encryptor) throws IOException, IllegalArgumentException
//===================================================================
{
	fileName = file.getFullPath();
	storageFile = file;
	this.decryptor = decryptor;
	this.encryptor = encryptor;
	int md = FileBase.convertMode(mode);
	if (!file.exists())
		if (md == File.READ_ONLY) throw new IOException("File not found for reading: "+file);
		else { //Create it.
			RandomAccessStream ras = file.toRandomAccessStream("rw");
			if (!createNew(ras)) {
				ras.close();
				throw new IOException("Cannot create DataStorage file: "+file);
			}
			return;
		}
	open(file.toRandomAccessStream(mode));
	if (!isValid){
		stream.close();
		throw new IOException("Could not open DataStorage. May be encrypted: "+file);
	}
}
/**
 * Open or create a DataStorage file in a RandomAccessStream. If the stream length is zero a new DataStorage
 * will be created in the stream (if it is open for read-write). Otherwise, the existing data is used.
 * @param stream The stream to use.
 * @exception IOException if there was an error creating or reading the DataStorage.
 */
//===================================================================
public DataStorage(RandomAccessStream stream) throws IOException
//===================================================================
{
	this(stream,null,null);
}
/**
 * Open or create a DataStorage file in a RandomAccessStream. If the stream length is zero a new DataStorage
 * will be created in the stream (if it is open for read-write). Otherwise, the existing data is used.
 * @param stream The stream to use.
 * @param decryptor An optional decryptor - necessary for reading the file if it is encrypted.
 * @param encryptor An optional encryptor - necessary for writing to the file if it is encrypted.
 * @exception IOException if there was an error creating or reading the DataStorage.
 */
//===================================================================
public DataStorage(RandomAccessStream stream,DataProcessor decryptor,DataProcessor encryptor) throws IOException
//===================================================================
{
	this.decryptor = decryptor;
	this.encryptor = encryptor;
	if (stream.getLength() == 0){
		if (!createNew(stream)) {
			stream.close();
			throw new IOException("Could not create DataStorage file.");
		}
	}
	open(stream);
	if (!isValid){
		stream.close();
		throw new IOException("Could not open DataStorage. May be encrypted!");
	}
}
/**
 * Open or create a DataStorage file. If the mode indicates read-write ("rw") AND the file does not
 * exist, then it will be created and initialized as a new DataStorage file.
 * @param fileName The full name of the data storage file.
 * @param mode must be "r" or "rw". An illegal argument exception will be thrown if it is neither.
 * @exception IOException if there is an error opening or creating the DataStorage file.
 */
//===================================================================
public DataStorage(String fileName,String mode) throws IOException
//===================================================================
{
	this(File.getNewFile(fileName),mode);
}
/**
 * Open or create a DataStorage file. If the mode indicates read-write ("rw") AND the file does not
 * exist, then it will be created and initialized as a new DataStorage file.
 * @param fileName The full name of the data storage file.
 * @param mode must be "r" or "rw". An illegal argument exception will be thrown if it is neither.
 * @param decryptor An optional decryptor - necessary for reading the file if it is encrypted.
 * @param encryptor An optional encryptor - necessary for writing to the file if it is encrypted.
 * @exception IOException if there is an error opening or creating the DataStorage file.
 */
//===================================================================
public DataStorage(String fileName,String mode,DataProcessor decryptor,DataProcessor encryptor) throws IOException
//===================================================================
{
	this(File.getNewFile(fileName),mode,decryptor,encryptor);
}
/**
 * Create a new DataStorage file after opening with DataStorage(String fileName).
 * @return true if the DataStorage was created successfully.
 * @deprecated - use one of the constructors that automatically create a DataStorage instead.
 */
//===================================================================
public boolean createNew()
//===================================================================
{
	File f = ewe.sys.Vm.newFileObject().getNew(fileName);
	return createNew(f.getRandomAccessStream(RandomAccessFile.CREATE));
}
/**
 * Create a new DataStorage file after opening with DataStorage(String fileName).
 * @param f an open RandomAccessStream to use.
 * @return true if the DataStorage was created successfully.
 * @deprecated - use one of the constructors that automatically create a DataStorage instead.
 */
//===================================================================
public boolean createNew(RandomAccessStream f)
//===================================================================
{
	try{
		stream = f;
		if (!stream.isOpen()) return false;
		if (!stream.seek(0)) return false;
		if (!pad(preamble)) return false;
		DataEntry de = new DataEntry(this);
		de.myLocation = preamble; // Root.
		if (!writeEntryAt(de,de.myLocation)) return false;
		de.myLocation = preamble+entryLen; // Free.
		if (!writeEntryAt(de,de.myLocation)) return false;
		de.myLocation = preamble+(entryLen*2); // Temp.
		if (!writeEntryAt(de,de.myLocation)) return false;
		de.myLocation = preamble+(entryLen*3); // Reserved.
		if (!writeEntryAt(de,de.myLocation)) return false;
		if (!stream.seek(0)) return false;
		stream.write(fileTag,0,fileTag.length);
		if (encryptor != null){
			byte [] lock = new byte[8];
			ewe.sys.Math.srand(ewe.sys.Vm.getTimeStamp());
			int rnd = ewe.sys.Math.rand();
			ewe.util.Utils.writeInt(rnd,lock,0,4);
			rnd = ewe.sys.Math.rand();
			ewe.util.Utils.writeInt(rnd,lock,4,4);
			byte total = 0;
			for (int i = 0; i<lock.length-1; i++)
				total += lock[i];
			lock[lock.length-1] = (byte)(~total+1);
			try{
				tempBytes = encryptor.processBlock(lock,0,lock.length,true,tempBytes);
			}catch(IOException e){
				e.printStackTrace();
				return returnError(e);
			}
			if (tempBytes == null) return false;
			if (tempBytes.length < 0 || tempBytes.length > 32) return false;
			lock[0] = (byte)(tempBytes.length);
			stream.write(lock,0,1);
			stream.write(tempBytes.data,0,tempBytes.length);
		}
		return isValid = true;
	}catch(IOException e){
		return isValid = returnError(e);
	}

}
/**
 * Close the DataStorage and the underlying RandomAccessStream. Do not use it again after calling this.
 */
//===================================================================
public void close()
//===================================================================
{
	if (stream != null)
		if (stream.isOpen())
			stream.close();
	stream = null;
}

//===================================================================
int locationOf(DataEntry who)
//===================================================================
{
	return who == null ? 0 : who.myLocation;
}

//-------------------------------------------------------------------
protected void remove(DataEntry toMove,Changes changes)
//-------------------------------------------------------------------
{
	changes.monitor(toMove);
	DataEntry oldParent = getEntryAt(toMove.parent);
	DataEntry oldPrev = getEntryAt(toMove.prev);
	DataEntry oldNext = getEntryAt(toMove.next);
	changes.monitor(oldParent);changes.monitor(oldPrev);changes.monitor(oldNext);
	if (oldParent != null)
		if (oldParent.children == locationOf(toMove))
			oldParent.children = locationOf(oldNext);
	if (oldPrev != null) oldPrev.next = locationOf(oldNext);
	if (oldNext != null) oldNext.prev = locationOf(oldPrev);
	toMove.next = toMove.prev = toMove.parent = 0;
}
//-------------------------------------------------------------------
protected void insert(DataEntry toMove,DataEntry destParent,DataEntry before,Changes changes)
//-------------------------------------------------------------------
{
	changes.monitor(toMove);
	DataEntry after = (before == null) ? destParent.getLastChild() : getEntryAt(before.prev);
	changes.monitor(destParent);changes.monitor(after);changes.monitor(before);
	toMove.parent = locationOf(destParent);
	toMove.next = locationOf(before);
	toMove.prev = locationOf(after);
	if (before != null) before.prev = locationOf(toMove);
	if (after != null) after.next = locationOf(toMove);
	if (toMove.prev == 0) destParent.children = locationOf(toMove); // Am now the new first child.
	//if (before == null && destParent != null) destParent.lastChild = locationOf(toMove);
}
//
// Datastore Alteration.
//
//-------------------------------------------------------------------
boolean move(DataEntry toMove,DataEntry destParent,DataEntry before)
//-------------------------------------------------------------------
{
	if (toMove == null || destParent == null) return false;
	if (before != null){
    if (before.parent != locationOf(destParent))
      before = null; //**************modified by Serg Kosinov April 2004 *****************   //return false;
		//if (before.parent != locationOf(destParent)) return false;
		if (before == toMove) return false;
	}
	changes.clear();
	remove(toMove,changes);
	insert(toMove,destParent,before,changes);
	return changes.doChanges();
}
//
// Datastore Alteration.
//
//-------------------------------------------------------------------
boolean remove(DataEntry toRemove)
//-------------------------------------------------------------------
{
	if (toRemove == null) return false;
	changes.clear();
	remove(toRemove,changes);
	toRemove.next = toRemove.prev = toRemove.parent = 0;
	return changes.doChanges();
}
//
// Datastore Alteration.
//
//-------------------------------------------------------------------
boolean delete(DataEntry toDelete)
//-------------------------------------------------------------------
{
	changes.clear();
	remove(toDelete,changes);
	DataEntry free = getFree();
	insert(toDelete,free,free.getFirstChild(),changes);
	entries.remove(toDelete);
	return changes.doChanges();
}
//
// Datastore Alteration.
//
//-------------------------------------------------------------------
boolean replace(DataEntry newOne,DataEntry oldOne)
//-------------------------------------------------------------------
{
	if (oldOne == null || newOne == null || newOne == oldOne) return false;
	changes.clear();
	DataEntry before = oldOne.getNext();
	DataEntry parent = oldOne.getParent();
	remove(oldOne,changes);
	remove(newOne,changes);
	insert(newOne,parent,before,changes);
	insert(oldOne,getFree(),null,changes);
	return changes.doChanges();
}
//-------------------------------------------------------------------
boolean readData(DataEntry de,byte [] dest,int offset)
//-------------------------------------------------------------------
{
	try{
		if (!stream.seek(de.dataLocation)) return false;
		IO.readAll(stream,dest,offset,de.allocatedLength);
		return true;
	}catch(IOException e){
		return returnError(e);
	}
}
//-------------------------------------------------------------------
boolean hasChildren(DataEntry de)
//-------------------------------------------------------------------
{
	try{
		if (!stream.seek(de.myLocation+12)) return false;
		IO.readAll(stream,temp,0,4);
		return Utils.readInt(temp,0,4) != 0;
	}catch(IOException e){
		return returnError(e);
	}
}
protected boolean hasNative = true;
//-------------------------------------------------------------------
IntArray getAllChildIds(DataEntry de,IntArray ia)
//-------------------------------------------------------------------
{
	if (de == null) return null;
	if (ia == null) ia = new IntArray();
	ia.clear();
	boolean got = false;
	if (hasNative && (stream instanceof RandomAccessFile)){
		try{
			got = native_getAllChildIds((RandomAccessFile)stream,de.myLocation,ia);
			if (got) return ia;
			else return null;
		}catch(Error e){
			hasNative = false;
		}catch(SecurityException e){
			hasNative = false;
		}
	}
	got = getAllChildIds(stream,de.myLocation,ia);
	if (got) return ia;
	else return null;
}
//-------------------------------------------------------------------
static native boolean native_getAllChildIds(ewe.io.RandomAccessFile s,int parentLocation,IntArray ia);
//-------------------------------------------------------------------
//-------------------------------------------------------------------
static boolean getAllChildIds(ewe.io.RandomAccessStream s,int parentLocation,IntArray ia)
//-------------------------------------------------------------------
{
	byte [] temp = new byte[4];
	try{
		if (!s.seek(parentLocation+12)) return false;
		IO.readAll(s,temp,0,4);
		int child = Utils.readInt(temp,0,4);
		while (child != 0){
			ia.add(child);
			if (!s.seek(child)) return false;
			IO.readAll(s,temp,0,4);
			child = Utils.readInt(temp,0,4);
		}
		return true;
	}catch(IOException e){
		return false;
	}
}
//-------------------------------------------------------------------
int getLastChild(int parentLocation)
//-------------------------------------------------------------------
{
	try{
		RandomAccessStream s = stream;
		if (!s.seek(parentLocation+12)) return 0;
		IO.readAll(s,temp,0,4);
		int child = Utils.readInt(temp,0,4);
		if (child == 0) return 0;
		while (true){
			if (!s.seek(child)) return 0;
			IO.readAll(s,temp,0,4);
			int next = Utils.readInt(temp,0,4);
			if (next == 0) return child;
			child = next;
		}
	}catch(IOException e){
		error = e.getMessage();
		return 0;
	}
}
/*
DataEntryObjectComparer dec;
//===================================================================
public void sort(int [] values,int length,Comparer c,boolean descending,Object obj)
//===================================================================
{
	//FIX - remove these
	if (dec == null) dec = new DataEntryObjectComparer(this,c,obj);
	CompareInts ci = dec;
	Utils.sort(values,length,ci,descending);
}
*/
/**
* This deletes the DataStorage, closing it first.
**/
//===================================================================
public void delete() throws IOException
//===================================================================
{
	if (storageFile == null) throw new IOException("Could not delete database.");
	close();
	if (!storageFile.delete()) throw new IOException("Could not delete database.");
}
/**
* This renames the DataStorage closing it first, but does not move it to a new directory
* or give it a new extension.
**/
//===================================================================
public void rename(String newName) throws IOException
//===================================================================
{
	if (storageFile == null) throw new IOException("Could not rename database.");
	close();
	try{
		String where = File.getFileExt(newName);
		String nameOnly = ewe.util.mString.leftOf(where,'.');
		String old = ewe.util.mString.rightOf(storageFile.getFileExt(),'.');
		if (old != null && old.length() != 0) nameOnly += "."+old;
		if (!storageFile.rename(nameOnly)) throw new Exception();
	}catch(Exception e){
		throw new IOException("Could not rename database.");
	}
}

//##################################################################
}
//##################################################################



