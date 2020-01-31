package ewe.database;
import ewe.io.IOException;
import ewe.io.RandomAccessStream;
import ewe.io.RandomAccessFile;
import ewe.util.Utils;
import ewe.util.IntArray;
import ewe.util.Debug;
import ewe.io.DataProcessor;
import ewe.io.File;
import ewe.io.FastStream;
import ewe.sys.Time;

//##################################################################
public class RandomAccessDatabaseStream implements DatabaseStream{
//##################################################################

RandomAccessStream stream;
FastStream raf;
File myFile;
String mode;
public static boolean unsafe = true;

//-------------------------------------------------------------------
private IOException ioError(IOException e) throws IOException
//-------------------------------------------------------------------
{
	if (stream != null) stream.close();
	return e;
}

//===================================================================
public boolean canWrite()
//===================================================================
{
	return stream.canWrite();
}
//===================================================================
public boolean temporaryClose() throws IOException
//===================================================================
{
	if (myFile == null || mode == null) return false;
	stream.close();
	return true;
}
//===================================================================
public void reopen() throws IOException
//===================================================================
{
	try{
	if (myFile == null || mode == null) return;
	stream = myFile.toRandomAccessStream(mode);
	//ewe.sys.Vm.debug("Opened!");
	if (stream instanceof FastStream)
		raf = (FastStream)stream;
	else
		raf = null;
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public int getFirstDataLocation()
//===================================================================
{
	return 240;
}
public int maxBufferSize = 1024;
byte [] buffer = new byte[12];
//===================================================================
void checkBuffer(int numBytes)
//===================================================================
{
	if (numBytes > buffer.length){
		if (maxBufferSize <= 12) maxBufferSize = 12;
		if (numBytes > maxBufferSize) numBytes = maxBufferSize;
		buffer = new byte[numBytes];
	}
}

//-------------------------------------------------------------------
protected void write(byte [] data,int offset, int length) throws IOException
//-------------------------------------------------------------------
{
	try{
	if (raf != null) raf.quickWrite(data,offset,length);
	else stream.write(data,offset,length);
	}catch(IOException e){throw ioError(e);}
}
//-------------------------------------------------------------------
protected int read(byte [] data,int offset, int length) throws IOException
//-------------------------------------------------------------------
{
	try{
	if (raf != null) return raf.quickRead(data,offset,length,false);
	return stream.read(data,offset,length);
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public void writeAll(long location, byte[] data, int offset, int length) throws IOException
//===================================================================
{
	try{
	stream.seek(location);
	write(data,offset,length);
	}catch(IOException e){throw ioError(e);}
}
//-------------------------------------------------------------------
void readAll(byte[] data,int offset, int length) throws IOException
//-------------------------------------------------------------------
{
	try{
	while(length > 0){
		int read = read(data,offset,length);
		if (read < 0) throw new IOException("EOF reached.");
		length -= read;
		offset += read;
	}
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public void readAll(long location, byte[] data, int offset, int length) throws IOException
//===================================================================
{
	try{
	stream.seek(location);
	readAll(data,offset,length);
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public void zero(long location, int numBytes) throws IOException
//===================================================================
{
	try{
	checkBuffer(numBytes);
	boolean first = true;
	while(numBytes > 0){
		int toZero = numBytes;
		if (toZero > buffer.length) toZero = buffer.length;
		if (first) ewe.util.Utils.zeroArrayRegion(buffer,0,toZero);
		writeAll(location,buffer,0,toZero);
		location += toZero;
		numBytes -= toZero;
	}
	}catch(IOException e){throw ioError(e);}
}

//===================================================================
public RandomAccessDatabaseStream(File file, String mode)
throws IOException
//===================================================================
{
	this(file,mode,false);
}
//===================================================================
public RandomAccessDatabaseStream(File file, String mode, boolean ignoreInconsistentState)
throws IOException
//===================================================================
{
	RandomAccessStream rs = file.toRandomAccessStream(mode);
	try{
		try{
			set(rs,mode,ignoreInconsistentState);
		}catch(InconsistentDatabaseStateException e){
			//
			// This will ONLY get thrown if the mode was "r" and ignoreInconsistentState was false.
			// So attempt to re-open in "rw" mode.
			//
			RandomAccessStream s = null;
			try{
				s = file.toRandomAccessStream("rw");
			}catch(IOException ie){
				//
				// Could not even open the file in "rw" mode,
				// the file may be set to read-only mode.
				//
				throw new InconsistentDatabaseStateException();
			}
			//
			// Good, now attempt to re-apply the changes.
			//
			set(s,"rw",false);
			s.close();
			//
			set(file.toRandomAccessStream(mode),mode,false);
		}
	}catch(IOException e){
		if (rs != null) rs.close();
		throw e;
	}
	myFile = file;
}

//===================================================================
public RandomAccessDatabaseStream(RandomAccessStream stream, String mode)
throws InconsistentDatabaseStateException, IOException
//===================================================================
{
	this(stream,mode,false);
}

//===================================================================
public RandomAccessDatabaseStream(RandomAccessStream stream, String mode, boolean ignoreInconsistentState)
throws InconsistentDatabaseStateException, IOException
//===================================================================
{
	set(stream,mode,ignoreInconsistentState);
}

//-------------------------------------------------------------------
private void set(RandomAccessStream stream, String mode, boolean ignoreInconsistentState)
throws InconsistentDatabaseStateException, IOException
//-------------------------------------------------------------------
{
	this.stream = stream;
	this.mode = mode;

	if (stream.length() < 240){
		if (!mode.equals("rw")) throw new IOException("The stream is invalid or cannot be initialized.");
		zero(0,240);
	}
	//
	SafeWrites ia = loadSafes();
	//
	if (ia != null) {
		if (mode.equals("rw")) writeSafes(ia);
		else if (!ignoreInconsistentState) {
			try{
				stream.close();
			}catch(Exception e){}
			throw new InconsistentDatabaseStateException();
		}
	}
	//
	if (stream instanceof FastStream)
		raf = (FastStream)stream;
	//
}
//===================================================================
public void flush() throws IOException
//===================================================================
{
	if (unsafe) return;
	try{
	stream.flush();
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public boolean truncateTo(long length) throws IOException
//===================================================================
{
	try{
		stream.setLength(length);
		return true;
	}catch(IOException e){
		return false;
	}
}
//===================================================================
public int readIntAt(long location) throws IOException
//===================================================================
{
	try{
	readAll(location,buffer,0,4);
	return Utils.readInt(buffer,0,4);
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public void writeIntAt(long location,int value) throws IOException
//===================================================================
{
	try{
	Utils.writeInt(value,buffer,0,4);
	writeAll(location,buffer,0,4);
	}catch(IOException e){throw ioError(e);}
}
//-------------------------------------------------------------------
void writeSafes(SafeWrites safes) throws IOException
//-------------------------------------------------------------------
{
	if (safes == null) return;
	for (int i = 0; i<safes.length; i++){
		writeIntAt(safes.locations[i],safes.values[i]);
	}
	flush();
}
//-------------------------------------------------------------------
void writeSafely(SafeWrites safes) throws IOException
//-------------------------------------------------------------------
{
	if (!unsafe) saveSafes(safes);
	writeSafes(safes);
	if (!unsafe) saveSafes(null);
}
static final int SAFE_LOCATION = 16;
//-------------------------------------------------------------------
void saveSafes(SafeWrites safes) throws IOException
//-------------------------------------------------------------------
{
	if (safes == null) {
		writeIntAt(SAFE_LOCATION,0);
		flush();
	}else{
		stream.seek((long)SAFE_LOCATION+4);
		for (int i = 0; i<safes.length; i++){
			if (safes.locations[i] == 0) break;
			Utils.writeLong(safes.locations[i],buffer,0);
			Utils.writeInt(safes.values[i],buffer,8,4);
			write(buffer,0,12);
		}
		Utils.writeLong(0,buffer,0);
		write(buffer,0,8);
		flush();
		writeIntAt(SAFE_LOCATION,-1);
		flush();
	}
}
//-------------------------------------------------------------------
SafeWrites loadSafes() throws IOException
//-------------------------------------------------------------------
{
	stream.seek((long)SAFE_LOCATION);
	readAll(buffer,0,4);
	if (Utils.readInt(buffer,0,4) == 0) return null;
	SafeWrites ia = new SafeWrites();
	while(true){
		readAll(buffer,0,12);
		long loc = Utils.readLong(buffer,4);
		if (loc == 0) break;
		ia.append(loc,Utils.readInt(buffer,8,4));
	}
	return ia;
}
//##################################################################
class SafeWrites{
//##################################################################

long [] locations = new long[4];
int [] values = new int[4];
int length = 0;
//-------------------------------------------------------------------
void clear()
//-------------------------------------------------------------------
{
	length = 0;
}
//-------------------------------------------------------------------
void append(long location, int value)
//-------------------------------------------------------------------
{
	locations[length] = location;
	values[length] = value;
	length++;
}
//##################################################################
}
//##################################################################

SafeWrites safes = new SafeWrites();

//===================================================================
public void safeWrite(long location, int data) throws IOException
//===================================================================
{
	try{
	safes.clear();
	if (location != 0) safes.append(location,data);
	writeSafely(safes);
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public void safeWrite(long loc1, int data1, long loc2, int data2) throws IOException
//===================================================================
{
	try{
	safes.clear();
	if (loc1 != 0){
		safes.append(loc1,data1);
	}
	if (loc2 != 0){
		safes.append(loc2,data2);
	}
	writeSafely(safes);
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public void safeWrite(
//===================================================================
	long loc1, int data1, long loc2, int data2,
	long loc3, int data3, long loc4, int data4) throws IOException
{
	try{
	safes.clear();
	if (loc1 != 0){
		safes.append(loc1,data1);
	}
	if (loc2 != 0){
		safes.append(loc2,data2);
	}
	if (loc3 != 0){
		safes.append(loc3,data3);
	}
	if (loc4 != 0){
		safes.append(loc4,data4);
	}
	writeSafely(safes);
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public boolean atEOF(long location) throws IOException
//===================================================================
{
	try{
	boolean at = location >= stream.length();
	//if (at) ewe.sys.Vm.debug("Loc: "+location+", length: "+stream.length());
	return at;
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public long length() throws IOException
//===================================================================
{
	try{
	return stream.length();
	}catch(IOException e){throw ioError(e);}
}
//===================================================================
public boolean setDecryptor(DataProcessor decryptor) throws IOException
//===================================================================
{
	return true;
}
//===================================================================
public boolean setDecryptorAndEncryptor(DataProcessor decryptor, DataProcessor encryptor) throws IOException
//===================================================================
{
	return true;
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	stream.close();
}

//===================================================================
public void delete() throws IOException
//===================================================================
{
	if (myFile == null) throw new IOException("Cannot delete.");
	close();
	if (!myFile.delete()) throw new IOException("Cannot delete.");
}
//===================================================================
public void rename(String newName) throws IOException
//===================================================================
{
	if (myFile == null) throw new IOException("Cannot rename.");
	close();
	try{
		String where = File.getFileExt(newName);
		String nameOnly = ewe.util.mString.leftOf(where,'.');
		String old = ewe.util.mString.rightOf(myFile.getFileExt(),'.');
		if (old != null && old.length() != 0) nameOnly += "."+old;
		if (!myFile.rename(nameOnly)) throw new Exception();
	}catch(Exception e){
		throw new IOException("Could not rename database.");
	}
}
//===================================================================
public Time getModifiedTime()
//===================================================================
{
	if (myFile == null) return null;
	return myFile.getModified(new Time());
}
//===================================================================
public boolean setModifiedTime(Time t)
//===================================================================
{
	if (myFile == null) return false;
	myFile.setModified(t);
	return true;
}
//##################################################################
}
//##################################################################

