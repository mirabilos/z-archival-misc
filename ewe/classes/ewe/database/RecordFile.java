package ewe.database;
import ewe.io.IOException;
import ewe.util.ByteArray;
import ewe.sys.Handle;
import ewe.sys.Time;
import ewe.sys.mThread;
import ewe.io.RandomAccessFile;
import ewe.io.RandomAccessStream;
import ewe.util.Utils;
import ewe.io.DataProcessor;
import ewe.io.CorruptedDataException;
import ewe.util.IntArray;
import ewe.io.File;
import ewe.util.mString;
import ewe.util.Hashtable;
import ewe.util.Debug;
import ewe.sys.Lock;

/**
A RecordFile is an implementation of a Database that uses a DatabaseStream
to store a set of Records as linked lists within the file.<p>

**/
//##################################################################
public class RecordFile extends RecordDatabaseObject{
//##################################################################

static final String BadRecordFile = "Invalid record file.";

static final int EOF_RECORD = 0x00000000;
static final int UNCONNECTED_FREE_RECORD = 0x10000000;
static final int CONNECTED_FREE_RECORD = 0x20000000;
static final int NORMAL_RECORD = 0x30000000;
static final int META_DATA_RECORD = 0x40000000;
static final int DELETED_DATA_RECORD = 0x50000000;
static final int FREE_DELETED_DATA_RECORD = 0x60000000;
static final int EXTENDED_META_DATA_RECORD = 0x70000000;
static final int LENGTH_MASK = 0x00ffffff;

int DIRECTORY_OFFSET = 0x100;
int META_DATA_LOCATION = DIRECTORY_OFFSET+0;
int FIRST_FREE_LOCATION = DIRECTORY_OFFSET+4;
int APPEND_LOCATION = DIRECTORY_OFFSET+8;
int RECORD_COUNT = DIRECTORY_OFFSET+12;
int firstRecord;

//
// All records use the first four bytes to indicate the type of record and
// the number of lines in the record (a line is 16 bytes).
//
// Chained records use the second four bytes to indicate the next
// location in the chain.
//
// Meta Data records use the third four bytes...
//
// Meta Data records use the fourth four bytes to indicate the first Extended
// Meta Data record in the Meta Data structure.
//
// Extended Meta Data records use the second four bytes to indicate the next Extended Meta
// Data record in the chain.
//

boolean modifyAlwaysMoves = false;

/**
* Use this to synchronize operations on the RecordFile.
**/
public ewe.sys.Lock lock = new ewe.sys.Lock();

/**
 * Open a Record file using an already open DatabaseStream.
 * @param stream The alredy opened DatabaseStream
 * @param mode the mode to use the RecordFile. This can be "r" or "rw" or "a" (for append only).
	Note that you may have to open the DatabaseStream in "rw" mode even if you are only going
	to access the records in read-only ("r") mode. This is because the safeWrite feature
	of the DatabaseStream may require you always open in "rw" mode to ensure that incomplete
	operations are completed.
 * @exception IOException
 * @exception InconsistentDatabaseStateException
 */
//===================================================================
public RecordFile(DatabaseStream stream,String mode) throws IOException, InconsistentDatabaseStateException
//===================================================================
{
	setDatabaseStream(stream,mode,false);
}
/**
 * Open a Record file using an already open RandomAccessStream.
 * @param stream The alredy opened RandomAccessStream. This will be wrapped in a RandomAccessDatabaseStream
	for use by the RecordFile.
 * @param mode the mode to use the RecordFile. This can be "r" or "rw" or "a" (for append only).
	Note that you may have to open the RandomAccessStream in "rw" mode even if you are only going
	to access the records in read-only ("r") mode. This is because the safeWrite feature
	of the RandomAccessDatabaseStream may require you always open in "rw" mode to ensure that incomplete
	operations are completed.
 * @exception IOException
 * @exception InconsistentDatabaseStateException
 */
//===================================================================
public RecordFile(RandomAccessStream stream, String mode) throws IOException, InconsistentDatabaseStateException
//===================================================================
{
	String sm = mode.equals("a") ? "rw" : mode;
	setDatabaseStream(new RandomAccessDatabaseStream(stream,sm),mode,false);
}
/**
 * Open a Record file using a specified File.
 * @param stream The alredy opened RandomAccessStream. This will be wrapped in a RandomAccessDatabaseStream
	for use by the RecordFile.
 * @param mode the mode to use the RecordFile. This can be "r" or "rw" or "a" (for append only).
 * @exception IOException
 * @exception InconsistentDatabaseStateException
 */
//===================================================================
public RecordFile(File file, String mode) throws IOException, InconsistentDatabaseStateException
//===================================================================
{
	this(file,mode,false);
}
//===================================================================
public RecordFile(RandomAccessStream stream, String mode,boolean ignoreInconsistentState) throws IOException, InconsistentDatabaseStateException
//===================================================================
{
	String sm = mode.equals("a") ? "rw" : mode;
	setDatabaseStream(new RandomAccessDatabaseStream(stream,sm,ignoreInconsistentState),mode,ignoreInconsistentState);
}
//===================================================================
public RecordFile(File file, String mode,boolean ignoreInconsistentState) throws IOException, InconsistentDatabaseStateException
//===================================================================
{
	getProperties().set("file",file);
	getProperties().set("name",file.getFileExt());
	getProperties().set("baseName",mString.leftOf(file.getFileExt(),'.'));
	String sm = mode.equals("a") ? "rw" : mode;
	RandomAccessDatabaseStream ras = new RandomAccessDatabaseStream(file,sm,ignoreInconsistentState);
	try{
		setDatabaseStream(ras,mode,ignoreInconsistentState);
	}catch(InconsistentDatabaseStateException ie){
		//
		// This will ONLY get thrown if the mode was "r" and ignoreInconsistentState was false.
		// So attempt to re-open in "rw" mode.
		//
		RandomAccessDatabaseStream s = null;
		try{
			s = new RandomAccessDatabaseStream(file,"rw",false);
		}catch(IOException ioe){
			//
			// Could not even open the file in "rw" mode,
			// the file may be set to read-only mode.
			//
			throw new InconsistentDatabaseStateException();
		}
		//
		// Good, now attempt to re-apply the changes.
		//
		setDatabaseStream(s,"rw",false);
		s.close();
		//
		// Now re-open in read-only mode as first requested.
		//
		ras = new RandomAccessDatabaseStream(file,sm,false);
		setDatabaseStream(ras,mode,false);
	}
}

//===================================================================
public void setDatabaseStream(DatabaseStream stream,String mode,boolean ignoreInconsistentState) throws IOException
//===================================================================
{
	file = null;
	DIRECTORY_OFFSET = stream.getFirstDataLocation();
	META_DATA_LOCATION = DIRECTORY_OFFSET+0;
	FIRST_FREE_LOCATION = DIRECTORY_OFFSET+4;
	APPEND_LOCATION = DIRECTORY_OFFSET+8;
	RECORD_COUNT = DIRECTORY_OFFSET+12;
	firstRecord = (DIRECTORY_OFFSET+16)/16;
	this.stream = stream;
	if (stream.length() <= DIRECTORY_OFFSET) initialize();
	try{
		file = (RandomAccessFile)((RandomAccessDatabaseStream)stream).stream;
	}catch(Exception e){}
	load(mode,ignoreInconsistentState);
}
//-------------------------------------------------------------------
protected boolean doEnableLookupMode() throws IOException
//-------------------------------------------------------------------
{
	return stream.temporaryClose();
}
//-------------------------------------------------------------------
protected void doOpenCloseLookup(boolean isOpen) throws IOException
//-------------------------------------------------------------------
{
	if (isOpen) stream.reopen();
	else stream.temporaryClose();
}

private void _lock() {/*1ock.synchronize();*/}
private void _unlock() {/*lock.release();*/}

/**
 * Initialize the RecordFile - you do not need to call this for new RecordFiles, it is done
 * if a new RecordFile is created automatically. This will erase any data in the File, setup
 * the internal directory and truncate the file to the minimum length if possible.
 * @exception IOException
 */
//===================================================================
public void initialize() throws IOException
//===================================================================
{
	_lock(); try{
		stream.zero(DIRECTORY_OFFSET,16);
		stream.safeWrite(firstRecord*16,0);
		stream.truncateTo(firstRecord*16+4);
	}finally{_unlock();}
}

public final int minimumLines = 2;

//-------------------------------------------------------------------
long _positionOf(int location)
//-------------------------------------------------------------------
{
	return ((long)location << 4) & 0xffffffff0L;
}
//-------------------------------------------------------------------
int _typeAndLength(int location) throws IOException
//-------------------------------------------------------------------
{
	return stream.readIntAt(((long)location << 4) & 0xffffffff0L);
}
//-------------------------------------------------------------------
int _first(long startPosition) throws IOException
//-------------------------------------------------------------------
{
	return stream.readIntAt(startPosition);
}
//-------------------------------------------------------------------
int _next(int prevLocation) throws IOException
//-------------------------------------------------------------------
{
	if (prevLocation == 0) throw new RuntimeException();
	return stream.readIntAt(_pointerToNext(prevLocation));
}
//-------------------------------------------------------------------
int _nextMetaExtention(int prevLocation) throws IOException
//-------------------------------------------------------------------
{
	if (prevLocation == 0) throw new RuntimeException();
	return stream.readIntAt((((long)prevLocation << 4) & 0xffffffff0L)+4L);
}
//-------------------------------------------------------------------
long _pointerToNext(int location)
//-------------------------------------------------------------------
{
	return (((long)location << 4) & 0xffffffff0L)+4L;
}
//##################################################################
class chainWalker{
//##################################################################
boolean broken = true;
private long pointerToNext;
long pointerToCurrent;
int nextWillBe;
int curLocation;
int curType;
int curLength;

private int move(long pointer) throws IOException
{
	if (broken) throw new IllegalStateException();
	if (pointer == 0) return 0;
	pointerToCurrent = pointer;
	curLocation = stream.readIntAt(pointerToCurrent);
	pointerToNext = 0;
	nextWillBe = curType = curLength = 0;
	if (curLocation != 0){
		pointerToNext = _pointerToNext(curLocation);
		nextWillBe = stream.readIntAt(pointerToNext);
		int ty = _typeAndLength(curLocation);
		curType = ty & ~LENGTH_MASK;
		curLength = ty & LENGTH_MASK;
	}
	return curLocation;
}
//
// Free records are kept in descending order.
//
int first(long firstPointer) throws IOException
{
	broken = false;
	return move(firstPointer);
}
//
// Free records are kept in descending order.
//
int next() throws IOException
{
	return move(pointerToNext);
}
boolean find(int location,long firstPointer) throws IOException
{
	for (int f = first(firstPointer); f != 0; f = next())
		if (f == location) return true;
	return false;
}
boolean delinkAndFree() throws IOException
{
	if (curLocation != 0) {
		delinkAndMakeUnconnectedFree();
		insertConnectedFree(curLocation,0,0);
		return true;
	}
	return false;
}
boolean delinkAndFree(int location,long firstPointer) throws IOException
{
	if (!find(location,firstPointer)) return false;
	delinkAndFree();
	return true;
}
boolean delinkAndMakeUnconnectedFree(int location,long firstPointer) throws IOException
{
	if (!find(location,firstPointer)) return false;
	delinkAndMakeUnconnectedFree();
	return true;
}
//
// Free records are kept in descending order.
//
long delinkAndMakeUnconnectedFree() throws IOException
{
	return delinkAndMakeUnconnectedFree(0);
}
//
// Free records are kept in descending order.
//
long delinkAndMakeUnconnectedFree(int setAs) throws IOException
{
	if (setAs == 0) setAs = UNCONNECTED_FREE_RECORD;
	else if (setAs == NORMAL_RECORD) throw new IllegalArgumentException();
	if (curLocation == 0 || broken) throw new IllegalStateException();
	broken = true;
	long cl = _positionOf(curLocation);
	stream.safeWrite(pointerToCurrent,nextWillBe, cl,setAs|curLength);
	return cl;
}
//
// Free records are kept in descending order.
//
long insertBefore(int location,int setAs, long extraLoc, int extraValue) throws IOException
{
	if (broken || pointerToCurrent == 0) throw new IllegalStateException();
	if (setAs == 0 || location == 0) throw new IllegalArgumentException();
	broken = true;
	long cl = _positionOf(location);
	int length = stream.readIntAt(cl) & LENGTH_MASK;
	stream.safeWrite(pointerToCurrent,location, _pointerToNext(location),curLocation, cl,setAs|length, extraLoc, extraValue);
	return cl;
}
//
// Free records are kept in descending order.
//
long insertConnectedFree(int location, long extraLoc, int extraValue) throws IOException
{
	if (location == 0) throw new IllegalArgumentException();
	int myLength = _typeAndLength(location) & LENGTH_MASK;
	if (myLength <= 0) throw new IllegalArgumentException();
	for (int free = first(FIRST_FREE_LOCATION);; free = next()){
		if (curLength <= myLength)
			return insertBefore(location,CONNECTED_FREE_RECORD, extraLoc,extraValue);
		if (curLocation == 0) throw new RuntimeException();
	}
}
//
// Free records are kept in descending order.
//
int getFreeRecord(int bytesNeeded) throws IOException
{
	int need = bytesNeeded <= 12 ? 1 : 1+((bytesNeeded-12)+15)/16;
	int free = first(FIRST_FREE_LOCATION);
	if (curLength < need) return 0;
	long freePosition = delinkAndMakeUnconnectedFree();
	int left = curLength-need;
	if (left < minimumLines) return free;
	free += left;
	stream.safeWrite(freePosition, UNCONNECTED_FREE_RECORD|left, freePosition+left*16, UNCONNECTED_FREE_RECORD|need);
	insertConnectedFree(curLocation,0,0);
	return free;
}
//-------------------------------------------------------------------
void replace(int oldLoc, int newLoc, long firstPosition, int setAs) throws IOException
//-------------------------------------------------------------------
{
	broken = true;
	int oldLen = oldLoc == 0 ? 0 : _typeAndLength(oldLoc) & LENGTH_MASK;
	int newLen = _typeAndLength(newLoc) & LENGTH_MASK;
	if (oldLoc != 0) {
		if (!find(oldLoc,firstPosition)){
			first(firstPosition);
			oldLoc = 0;
		}else{ //Found it, so replace.
			int pointTo = stream.readIntAt(_positionOf(oldLoc)+4);
			stream.safeWrite(
			_positionOf(oldLoc),UNCONNECTED_FREE_RECORD|oldLen, //Disconnect old.
			_positionOf(newLoc),setAs|newLen, //Setup new.
			_positionOf(newLoc)+4, pointTo, //Point to what the old one was.
			pointerToCurrent, newLoc); //Get previous pointer to point to the new one.
			return;
		}
	}else
		first(firstPosition);
	stream.safeWrite(
	_positionOf(newLoc),setAs|newLen, //Setup new.
	_positionOf(newLoc)+4, stream.readIntAt(pointerToCurrent), //Point to what the old one was.
	pointerToCurrent, newLoc,
	0,0
	); //Get previous pointer to point to the new one.

}
//##################################################################
}
//##################################################################

chainWalker freeList = new chainWalker();

//-------------------------------------------------------------------
void _free(int location, int oldLength, long extraPosition,int extraValue) throws IOException
//-------------------------------------------------------------------
{
	freeList.insertConnectedFree(location,extraPosition,extraValue);
}
//-------------------------------------------------------------------
void _freeMeta(int location) throws IOException
//-------------------------------------------------------------------
{
	int was = location;
	//
	// The pointer to the first meta extension is at offset 12 in the Meta Data record.
	//
	location = stream.readIntAt(_positionOf(location)+12);
	//
	// Now we free the MetaData record. The extended meta data records become unconnected,
	// but floating extended meta data records is not a problem if freeing them
	// fails later.
	//
	_free(was,-1,0,0);
	//
	while(location != 0){
		int next = _nextMetaExtention(location);
		_free(location,-1,0,0);
		location = next;
	}
	//_free(was,-1,0,0); // Used to do this after, but this is incorrect.
}

//-------------------------------------------------------------------
int _addOn(int lengthNeeded, byte[] toAdd, int offset) throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
	int location = stream.readIntAt(APPEND_LOCATION);
	if (location == 0) location = ((APPEND_LOCATION & 0xfffffff0)+16) >> 4;
	int needLines = lengthNeeded <= 12 ? 1 : 1+((lengthNeeded-12)+15)/16;
	if (needLines < minimumLines) needLines = minimumLines;
	int totalBytes = needLines*16+4; //The last 4 is for the EOF marker for the next record.
	long where = ((long)location << 4) & 0xffffffff0L;
	if (toAdd != null){
		stream.zero(where,4);
		stream.writeAll(where+4,toAdd,offset,lengthNeeded);
		stream.zero(where+4+lengthNeeded,totalBytes-4-lengthNeeded);
	}else
		stream.zero(where,totalBytes);
	stream.flush();
	stream.safeWrite(
		where,UNCONNECTED_FREE_RECORD|(needLines & LENGTH_MASK),
		APPEND_LOCATION, location+needLines);
	return location;
	}finally{_unlock();}
}
//
// If this returns a record, that record is marked as unconnected free.
//
//-------------------------------------------------------------------
int _findFree(int lengthNeeded) throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
		return freeList.getFreeRecord(lengthNeeded);
	}finally{_unlock();}
}
//-------------------------------------------------------------------
int _findFree(byte[] data, int offset, int length) throws IOException
//-------------------------------------------------------------------
{
	int free = _findFree(length);
	if (free == 0) return 0;
	long where = _positionOf(free);
	stream.writeAll(where+4,data,offset,length);
	return free;
}
//-------------------------------------------------------------------
int _findOrAdd(int bytesNeeded) throws IOException
//-------------------------------------------------------------------
{
	int free = _findFree(bytesNeeded);
	if (free == 0) return _addOn(bytesNeeded,null,0);
	return free;
}
//-------------------------------------------------------------------
int _findOrAdd(byte[] data,int offset,int length) throws IOException
//-------------------------------------------------------------------
{
	int free = _findFree(data,offset,length);
	if (free == 0) return _addOn(length,data,offset);
	return free;
}

//-------------------------------------------------------------------
protected MetaData getNewMetaData(String name)
//-------------------------------------------------------------------
{
	return new metaData(name);
}
//-------------------------------------------------------------------
protected DatabaseIndex makeNewIndex(String name)
//-------------------------------------------------------------------
{
	return new RecordDatabaseIndex(this,name);
}

//##################################################################
class metaData extends MetaData{
//##################################################################

String name;
int location;
long position;
int size;
chainWalker meta = new chainWalker();
//long startPointer;
//long pointer;
boolean forWriting;
boolean isAppending;
int filePos;
int currentLocation, leftInCurrent;
long currentPosition;

metaData(String name)
{
	super(name);
	this.name = name;
}
void free() throws IOException
{
	_lock(); try{
	int location = meta.curLocation;
	meta.delinkAndMakeUnconnectedFree();
	_freeMeta(location);
	}finally{_unlock();}
}
int getNextMeta() throws IOException
{
	_lock(); try{
	while(meta.curLocation != 0){
		if ((_typeAndLength(meta.curLocation) & ~LENGTH_MASK) == META_DATA_RECORD)
			return meta.curLocation;
		meta.next();
	}
	return 0;
	}finally{_unlock();}
}
int first() throws IOException
{
	meta.first(META_DATA_LOCATION);
	return getNextMeta();
}
int next() throws IOException
{
	meta.next();
	return getNextMeta();
}
byte[] b = new byte[2];
//-------------------------------------------------------------------
int find() throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
	if (name == null) return 0;
	for (int m = first(); m!= 0; m = next()){
		long p = _positionOf(m)+16;
		stream.readAll(p,b,0,2);
		int num = Utils.readInt(b,0,2)+2;
		if (b.length < num) b = new byte[num];
		stream.readAll(p,b,0,num);
		String s = EncodedUTF8String.load(b,0);
		if (s == null) continue;
		if (name.equalsIgnoreCase(s)) return m;
	}
	return 0;
	}finally{_unlock();}
}
//-------------------------------------------------------------------
protected void openForWriting(int bytesNeeded) throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
		byte[] str = EncodedUTF8String.store(name);
		if (bytesNeeded <= 0) bytesNeeded = 256-8;
		//
		// Need to store:
		// link to next meta, number of bytes, link to data.
		//
		location = _findOrAdd(str.length+4+4+4);
		position = _positionOf(location);
		stream.zero(position+4,12);
		stream.writeAll(position+16,str,0,str.length);
		//
		int first = _findOrAdd(bytesNeeded+4);
		int len = _typeAndLength(first) & LENGTH_MASK;
		long firstPosition = _positionOf(first);
		stream.zero(firstPosition+4,4);
		stream.safeWrite(firstPosition,EXTENDED_META_DATA_RECORD|len, position+12,first);
		//
		size = filePos = 0;
		currentLocation = first;
		leftInCurrent = len*16-8;
		currentPosition = _positionOf(currentLocation)+8;
		stream.zero(currentPosition,bytesNeeded);
		//
		forWriting = true;
	}finally{_unlock();}
}
//-------------------------------------------------------------------
void setCurrent(int loc) throws IOException
//-------------------------------------------------------------------
{
	currentLocation = loc;
	currentPosition = _positionOf(currentLocation)+8;
	leftInCurrent = (_typeAndLength(currentLocation)&LENGTH_MASK)*16-8;
}
/**
* Setup so that the current point is at the start.
**/
//-------------------------------------------------------------------
void setup(int location) throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
	this.location = location;
	position = _positionOf(location);
	size = stream.readIntAt(position+8);
	//
	setCurrent(stream.readIntAt(position+12));
	//
	filePos = 0;
	}finally{_unlock();}
}
//-------------------------------------------------------------------
void setup() throws IOException
//-------------------------------------------------------------------
{
	setup(location);
}
//-------------------------------------------------------------------
void goToEnd() throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
	int skipOver = size-filePos;
	while(leftInCurrent < skipOver){
		skipOver -= leftInCurrent;
		setCurrent(stream.readIntAt(_positionOf(currentLocation)+4));
	}
	leftInCurrent -= skipOver;
	currentPosition += skipOver;
	filePos = size;
	}finally{_unlock();}
}
//===================================================================
public boolean openForAppending(boolean mustExist) throws IOException
//===================================================================
{
	_lock(); try{
	location = find();
	if (location == 0) {
		if (mustExist) return false;
		openForWriting(50);
		this.close();
		location = find();
	}
	setup();
	goToEnd();
	isAppending = true;
	return true;
	}finally{_unlock();}
}
//===================================================================
public long openForDirectWrite(boolean mustExist,int bytesNeeded) throws IOException
//===================================================================
{
	_lock(); try{
	location = find();
	if (location == 0) {
		if (mustExist) return -1;
		openForWriting(bytesNeeded);
		this.close();
		return currentPosition;
	}
	setup();
	if (leftInCurrent < bytesNeeded){
		free();
		openForWriting(bytesNeeded);
		this.close();
		return currentPosition;
	}
	return currentPosition;
	}finally{_unlock();}
}
//===================================================================
public void openForOverwriting(int bytesNeeded) throws IOException
//===================================================================
{
	_lock(); try{
	int old = find();
	if (old != 0) {
		//ewe.sys.Vm.debug("Reusing!");
		meta.delinkAndMakeUnconnectedFree();
		setup(old);
		size = 0;
		forWriting = true;
	}
	else openForWriting(bytesNeeded);
	//ewe.sys.Vm.debug("At: "+location);
	}finally{_unlock();}
}
//===================================================================
public void openForReplacing(int bytesNeeded) throws IOException
//===================================================================
{
	_lock(); try{
	openForWriting(bytesNeeded);
	}finally{_unlock();}
}
//===================================================================
public int openForReading() throws IOException
//===================================================================
{
	_lock(); try{
	numberOfFragments = 0;
	location = find();
	if (location == 0) return -1;
	setup();
	return size;
	}finally{_unlock();}
}
//===================================================================
public long size() throws IOException
//===================================================================
{
	_lock(); try{
	int size = openForReading();
	this.close();
	return size;
	}finally{_unlock();}
}
//===================================================================
public boolean delete() throws IOException
//===================================================================
{
	_lock(); try{
	location = find();
	if (location == 0) return false;
	free();
	return true;
	}finally{_unlock();}
}
//===================================================================
public void write(byte[] data, int offset, int length) throws IOException
//===================================================================
{
	_lock(); try{
	while(length > 0){
		int canWriteInCurrent = leftInCurrent >= length ? length : leftInCurrent;
		if (canWriteInCurrent != 0){
			stream.writeAll(currentPosition,data,offset,canWriteInCurrent);
			currentPosition += canWriteInCurrent;
			leftInCurrent -= canWriteInCurrent;
			size += canWriteInCurrent;
			filePos += canWriteInCurrent;
			if (isAppending)
				stream.safeWrite(position+8,size);
			offset += canWriteInCurrent;
			length -= canWriteInCurrent;
		}else{
			int next = stream.readIntAt(_positionOf(currentLocation)+4);
			if (next == 0) {
				next = _findOrAdd(256-8);
				int len = _typeAndLength(next) & LENGTH_MASK;
				stream.safeWrite(
				_positionOf(currentLocation)+4,next,
				_positionOf(next), EXTENDED_META_DATA_RECORD|len,
				_positionOf(next)+4,0,
				0,0);
			}
			setCurrent(next);
		}
	}
	}finally{_unlock();}
}
//===================================================================
public int read(byte[] data, int offset, int length) throws IOException
//===================================================================
{
	_lock(); try{
	int canRead = size-filePos;
	if (canRead == 0) return -1;
	if (canRead > length) canRead = length;
	int didRead = 0;
	boolean show = length > 500000;
	while(canRead > 0){
		int readNow = canRead > leftInCurrent ? leftInCurrent : canRead;
		if (readNow != 0){
			//if (show) ewe.sys.Vm.debug("RN: "+readNow);
			stream.readAll(currentPosition,data,offset,readNow);
			currentPosition += readNow;
			leftInCurrent -= readNow;
			filePos += readNow;
			didRead += readNow;
			offset += readNow;
			canRead -= readNow;
		}else{
			int next = stream.readIntAt(_positionOf(currentLocation)+4);
			if (next == 0) break;
			setCurrent(next);
			numberOfFragments++;
		}
	}
	return didRead == 0 ? -1 : didRead;
	}finally{_unlock();}
}
/*
//-------------------------------------------------------------------
void saveAndReplace(int replacing) throws IOException
//-------------------------------------------------------------------
{
	long wrote = pointer-startPointer;
	stream.writeIntAt(startPointer-4,(int)wrote);
	meta.first(META_DATA_LOCATION);
	if (replacing == 0)
		meta.insertBefore(location,META_DATA_RECORD,0,0);
	else{
		long rp = _positionOf(replacing);
		int length = stream.readIntAt(rp) & LENGTH_MASK;
		meta.insertBefore(location,META_DATA_RECORD, rp,UNCONNECTED_FREE_RECORD|length);
		meta.delinkAndFree(replacing,META_DATA_LOCATION);
	}
}
//===================================================================
public void save(boolean replace) throws IOException
//===================================================================
{
	saveAndReplace(replace ? find() : 0);
}
*/
//===================================================================
public boolean closeStream() throws IOException
//===================================================================
{
	_lock(); try{
	if (!forWriting || isAppending) return true;
	stream.safeWrite(position+8,size);
	int old = find();
	//ewe.sys.Vm.debug("Old: "+old);
	meta.replace(old,location,META_DATA_LOCATION,META_DATA_RECORD);
	if (old != 0) _freeMeta(old);
	return true;
	}finally{_unlock();}
}
//##################################################################
}
//##################################################################

//void closeMetaData(int oldLocation)


/*
//-------------------------------------------------------------------
int _setRecord(int location,int type,byte[] data,int offset,int length,boolean incCount) throws IOException
//-------------------------------------------------------------------
{
	int wasLength = _typeAndLength(location) & LENGTH_MASK;
	long where = ((long)location << 4) & 0xffffffff0L;
	stream.writeAll(where+4,data,offset,length);
	stream.flush();
	if (incCount){
		int count = stream.readIntAt(RECORD_COUNT)+1;
		stream.safeWrite(where,wasLength|type, RECORD_COUNT,count);
	}else
		stream.safeWrite(where,wasLength|type);
	return location;
}
*/
/**
 * Delete the record at the specified location. This can only be applied to a Data Record.
 * @param location The record location.
 * @exception IOException
 */
//===================================================================
public void deleteRecord(int location) throws IOException
//===================================================================
{
	_lock(); try{
		_mustBeData(location,"deleteRecord()");
		int count = stream.readIntAt(RECORD_COUNT)-1;
		if (count < 0) count = 0;
		_free(location, _typeAndLength(location), RECORD_COUNT, count);
	}finally{_unlock();}
}

private byte[] deleteBuffer;

//-------------------------------------------------------------------
private void _eraseEntry(int location) throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
		int type = _typeAndLength(location);
		if ((type & ~LENGTH_MASK) == NORMAL_RECORD)
			deleteRecord(location);
		else if ((type & ~LENGTH_MASK) == DELETED_DATA_RECORD){
			long where = _positionOf(location);
			stream.safeWrite(where,(type & LENGTH_MASK)|FREE_DELETED_DATA_RECORD);
		}
	}finally{_unlock();}
}
/**
* Delete the record, leaving behind a special deleted record marker.
* @param location The record to delete.
* @param OID a unique ID to associate with the record.
* @param deleteTime a time stamp (from Time.getTime()) to associate with the deletion.
* @exception IOException on error.
* @exception DatabaseOperationException if the location is not a valid database record.
*/
//===================================================================
public synchronized void markAsDeleted(int location,long OID,long deleteTime) throws IOException
//===================================================================
{
	_lock(); try{
		_mustBeData(location,"markAsDeleted()");
		if (location == 0) return;
		long position = _positionOf(location);
		int length = stream.readIntAt(position) & LENGTH_MASK;
		//
		if (deleteBuffer == null) deleteBuffer = new byte[20];
		Utils.writeInt(0,deleteBuffer,0,4);
		Utils.writeLong(OID,deleteBuffer,4);
		Utils.writeLong(deleteTime,deleteBuffer,12);
		//
		// Going to save the OID and deleted time.
		//
		chainWalker cw = new chainWalker();
		boolean did = false;
		int count = stream.readIntAt(RECORD_COUNT)-1;
		if (count < 0) count = 0;
		int md = 0;
		for (md = cw.first(META_DATA_LOCATION); md != 0; md = cw.next())
			if (cw.curType == FREE_DELETED_DATA_RECORD) break;
		if (md == 0){
			md = _findOrAdd(deleteBuffer.length);
			cw.first(META_DATA_LOCATION);
			cw.insertBefore(md,FREE_DELETED_DATA_RECORD,0,0);
		}
		long pd = _positionOf(md);
		int ml = stream.readIntAt(pd) & LENGTH_MASK;
		stream.writeAll(pd+8,deleteBuffer,4,deleteBuffer.length-4);
		stream.safeWrite(
			pd, DELETED_DATA_RECORD|ml,
			position, UNCONNECTED_FREE_RECORD|length,
			RECORD_COUNT, count,
			0, 0
		);
		_free(location,_typeAndLength(location),0,0);
	}finally{_unlock();}
}
/**
* Returns the number of deleted records.
**/
//===================================================================
public synchronized int countDeletedEntries() throws IOException
//===================================================================
{
	_lock(); try{
		int count = 0;
		chainWalker cw = new chainWalker();
		for (int md = cw.first(META_DATA_LOCATION); md != 0; md = cw.next())
			if (cw.curType == DELETED_DATA_RECORD) count++;
		return count;
	}finally{_unlock();}
}
//-------------------------------------------------------------------
private IntArray getDeletedEntries(IntArray destination) throws IOException
//-------------------------------------------------------------------
{
	if (destination == null) destination = new IntArray();
	destination.clear();
	chainWalker cw = new chainWalker();
	for (int md = cw.first(META_DATA_LOCATION); md != 0; md = cw.next())
		if (cw.curType == DELETED_DATA_RECORD){
			destination.append(md);
		}
	return destination;
}
//-------------------------------------------------------------------
private long getDeletedEntryInfo(int location, Time deleteTime) throws IOException
//-------------------------------------------------------------------
{
	int need = lengthOfRecord(location);
	if (deleteBuffer == null || deleteBuffer.length < need)
		deleteBuffer = new byte[need];
	readRecord(location,deleteBuffer,0);
	if (deleteTime != null) deleteTime.setEncodedTime(Utils.readLong(deleteBuffer,12));
	return Utils.readLong(deleteBuffer,4);
}
//-------------------------------------------------------------------
private int _locateDeletedEntry(long OID) throws IOException
//-------------------------------------------------------------------
{
	chainWalker cw = new chainWalker();
	for (int md = cw.first(META_DATA_LOCATION); md != 0; md = cw.next())
		if (cw.curType == DELETED_DATA_RECORD){
			if (getDeletedEntryInfo(md, null) == OID)
				return md;
		}
	return 0;
}
/**
* Free all the deleted records.
**/
//===================================================================
public synchronized void eraseDeletedEntries() throws IOException
//===================================================================
{
	_lock(); try{
		chainWalker cw = new chainWalker();
		for (int md = cw.first(META_DATA_LOCATION); md != 0; md = cw.next())
			if (cw.curType == DELETED_DATA_RECORD)
				stream.safeWrite(_positionOf(md),cw.curLength|FREE_DELETED_DATA_RECORD);
	}finally{_unlock();}
}
/**
* Free a particular deleted entry.
**/
//===================================================================
public synchronized boolean eraseDeletedEntry(long oid) throws IOException
//===================================================================
{
	_lock(); try{
		int where = _locateDeletedEntry(oid);
		if (where == 0) return false;
		_eraseEntry(where);
		return true;
	}finally{_unlock();}
}
//-------------------------------------------------------------------
private int _addRecord(byte[] data, int offset, int length, int replacing, boolean useFreeSpace) throws IOException
//-------------------------------------------------------------------
{
	int free = useFreeSpace ? _findOrAdd(data,offset,length) : _addOn(length, data, offset);
	long where = _positionOf(free);
	int got = stream.readIntAt(where) & LENGTH_MASK;
	int count = stream.readIntAt(RECORD_COUNT)+1;
	if (replacing == 0)
		stream.safeWrite(where,NORMAL_RECORD|got, RECORD_COUNT, count);
	else{
		long old = _positionOf(replacing);
		int oldLength = stream.readIntAt(old) & LENGTH_MASK;
		stream.safeWrite(where,NORMAL_RECORD|got, old,UNCONNECTED_FREE_RECORD|oldLength);
	}
	return free;
}

//-------------------------------------------------------------------
private void _mustBeData(int location, String message) throws IOException
//-------------------------------------------------------------------
{
	if (location == 0 || (_typeAndLength(location) & ~LENGTH_MASK) != NORMAL_RECORD)
		throw new DatabaseOperationException(message+" - Record at: 0x"+Integer.toHexString(location)+" is not a data record.");
}
/**
 * Modify the record data, returning a new location if the record had to be moved. This should
 * only be applied to normal records.
 * @param location the location of the record.
 * @param data The data bytes.
 * @param offset The index of the first data byte.
 * @param length The number of data bytes.
 * @param useFreeSpace if this is false then it will be appended to the file without
 * any attempt to use space occupied by previously deleted records. This is the fastest
 * modify that you can do.
 * @return the location of the saved record.
 * @exception IOException
 */
//===================================================================
public synchronized int modifyRecord(int location, byte[] data, int offset, int length, boolean useFreeSpace) throws IOException
//===================================================================
{
	_lock(); try{
		_mustBeData(location,"modifyRecord()");
		int newLoc = _addRecord(data,offset,length,location,useFreeSpace);
		_free(location,-1,0,0);
		return newLoc;
	}finally{_unlock();}
}
/**
 * Add the data as a new record. This is ONLY to be used with normal
 * @param data The data bytes.
 * @param offset The index of the first data byte.
 * @param length The number of data bytes.
 * @param useFreeSpace if this is false the record will be appended to the file without
 * any attempt to use space occupied by previously deleted records. This is the fastest
 * add that you can do.
 * @return the location of the saved record.
 * @exception IOException
*/
//===================================================================
public synchronized int addRecord(byte[] data, int offset, int length, boolean useFreeSpace) throws IOException
//===================================================================
{
	_lock(); try{
		return _addRecord(data,offset,length,0,defaultUseFree);
	}finally{_unlock();}
}
//===================================================================
public synchronized int modifyRecord(int location, byte[] data, int offset, int length) throws IOException
//===================================================================
{
	return modifyRecord(location,data,offset,length,defaultUseFree);
}
//===================================================================
public synchronized int addRecord(byte[] data, int offset, int length) throws IOException
//===================================================================
{
	return addRecord(data,offset,length,defaultUseFree);
}
/**
 * Modify the record data, returning a new location if the record had to be moved.
 * @param location the location of the record.
 * @param data The data bytes.
 * @param useFreeSpace if this is false and the record must be placed in a new location
 * because its data now exceeds its space allocation, then it will be appended to the file without
 * any attempt to use space occupied by previously deleted records. This is the fastest
 * add that you can do.
 * @return the location of the saved record.
 * @exception IOException
 */
//===================================================================
public synchronized int modifyRecord(int location, byte[] data, boolean useFreeSpace) throws IOException
//===================================================================
{
	return modifyRecord(location,data,0,data.length,useFreeSpace);
}
/**
 * Set the meta data.
 * @param data the data to set it to. A null value indicates a deletion of the meta data.
 * @param offset the index of the first byte of data.
 * @param length the length of the data.
 * @exception IOException
 */
	/*
//===================================================================
public void setMetaData(byte[] data, int offset, int length) throws IOException
//===================================================================
{
	int oldData = stream.readIntAt(META_DATA_LOCATION);
	int was = oldData == 0 ? 0 : _typeAndLength(oldData) & LENGTH_MASK;
	if (data == null){ //Clear the meta data.
		if (oldData == 0) return;
		_free(oldData, was, META_DATA_LOCATION,0);
		return;
	}
	int need = length <= 12 ? 1 : 1+((length-12)+15)/16;
	if (need <= was){
		_setRecord(oldData,META_DATA_RECORD,data, offset, length, false);
	}else{
		int newLoc = _addOn(length,data,offset);
		int lines = _typeAndLength(newLoc) & LENGTH_MASK;
		stream.safeWrite((((long)newLoc << 4) & 0xffffffff0L), META_DATA_RECORD|lines);
		//
		// At this point the new record is flagged as META_DATA
		// but the META_DATA_LOCATION does not point to it yet.
		// A failure at this point means that it will be left floating,
		// but this is not an inconsistent state since it will be ignored
		// during normal record traversing and can be cleaned up later.
		//
		if (oldData != 0)
			_free(oldData, was,META_DATA_LOCATION,newLoc);
		else
			stream.safeWrite(META_DATA_LOCATION,newLoc);
	}
}
*/
/**
 * Set the meta data.
 * @param data the data to set it to. A null value indicates a deletion of the meta data.
 * @exception IOException
 */
	/*
//===================================================================
public void setMetaData(byte[] data) throws IOException
//===================================================================
{
	setMetaData(data,0,data == null ? 0 : data.length);
}
*/
/**
 * Return the number of bytes needed to fetch a record.
 * @param record the record location.
 * @return the number of bytes needed to fetch a record.
 * @exception IOException
 */
//===================================================================
public synchronized int lengthOfRecord(int record) throws IOException
//===================================================================
{
	_lock(); try{
		if (record == 0) return 0;
		int tl = _typeAndLength(record);
		int len = tl & LENGTH_MASK;
		if ((tl & 0x0f000000) != 0 || len <= 0)
			throw new IOException("Database corrupted at record: 0x"+Integer.toHexString(record));
		if (len == 1) return 12;
		return 12+(len-1)*16;
	}finally{_unlock();}
}
/**
 * Read in the record data.
 * @param record the record location.
 * @param dest the destination for the data bytes.
 * @param offset the location to read to.
 * @return the number of bytes read.
 * @exception IOException
 */
//===================================================================
public synchronized int readRecord(int record, byte[] dest, int offset) throws IOException
//===================================================================
{
	_lock(); try{
		int need = lengthOfRecord(record);
		stream.readAll((((long)record << 4) & 0xffffffff0L)+4, dest, offset, need);
		return need;
	}finally{_unlock();}
}
/**
 * Read in and return the record data. This is a very inefficient way of doing this
 * since it creates a new byte array each time.
 * @param record the record location.
 * @return the record data as an array of bytes.
 * @exception IOException
 */
//===================================================================
public synchronized byte[] readRecord(int record) throws IOException
//===================================================================
{
	_lock(); try{
		if (record == 0) return null;
		int length = lengthOfRecord(record);
		byte[] ret = new byte[length];
		readRecord(record,ret,0);
		return ret;
	}finally{_unlock();}
}
/**
 * Read the record data and <b>append</b> it to a ByteArray.
 * @param record the record to read.
 * @param dest a destination ByteArray. This can be null in which case a new one will be created.
 * @return the dest ByteArray or a new one.
 * @exception IOException
 */
//===================================================================
public synchronized ByteArray readRecord(int record, ByteArray dest) throws IOException
//===================================================================
{
	_lock(); try{
		if (dest == null) dest = new ByteArray();
		int need = lengthOfRecord(record);
		if (need == 0) return dest;
		int was = dest.length;
		dest.makeSpace(dest.length, need);
		readRecord(record,dest.data,was);
		return dest;
	}finally{_unlock();}
}
/**
 * Read in the meta-data.
 * @return the meta-data or null if none is set.
 * @exception IOException
 */
//===================================================================
public byte[] getMetaData() throws IOException
//===================================================================
{
	return readRecord(stream.readIntAt(META_DATA_LOCATION));
}
/**
 * Retrieve the location of the first record. There is no getLastRecord()
 * since record ordering is meaningless in this file.
 * @return the location of the first record or 0 if there is no first record.
* @exception IOException
*/
//===================================================================
public int getFirstEntry() throws IOException
//===================================================================
{
	return getNextEntry(0);
}
/**
 * Retrieve the location of the normal record following the previous record.
 * @param previous The previous record. A value of zero will return the first record.
 * @return the location of the record following the previous record.
* @exception IOException
 */
//===================================================================
public int getNextEntry(int previous) throws IOException
//===================================================================
{
	_lock(); try{
		while(true){
			int next = firstRecord;
			if (previous != 0){
			  int tl = _typeAndLength(previous);
				if ((tl & 0x0f000000) != 0)
					throw new CorruptedDataException(
						"Bad record type of: "+Integer.toHexString((tl >> 24) & 0xff)+" at: 0x"+Integer.toHexString(previous)
						//BadRecordFile
					);
				int len = tl & LENGTH_MASK;
				if (len < 1) throw new CorruptedDataException(
					"Bad length of: "+len+" at: 0x"+Integer.toHexString(previous)
					//BadRecordFile
				);
				next = previous+len;
			}
			//
			//
			if (stream.atEOF((((long)next << 4) & 0xffffffff0L))) {
				throw new CorruptedDataException(
					"Truncated record at: 0x"+Integer.toHexString(previous)
					//BadRecordFile
				);
			}
			previous = next;
			int type = _typeAndLength(previous) & ~LENGTH_MASK;
			if (type == EOF_RECORD) return 0;
			if (type != NORMAL_RECORD) continue;
			return next;
		}
	}finally{_unlock();}
}
//-------------------------------------------------------------------
private static boolean hasNative = true;
native int nativeGetAllRecords(int firstRecord, int[] dest, int offset);
//-------------------------------------------------------------------

/**
 * Copy all the records into the array. Use countEntries() to find out how many records there
 * are.
 * @param dest the destination for the records.
 * @param offset the location in the destination to write to.
 */
//-------------------------------------------------------------------
protected boolean getAllRecords(Handle h,int[] dest,int offset,int need) throws IOException
//-------------------------------------------------------------------
{
	_lock(); try{
		if (/*h == null &&*/ hasNative && file != null) try{
			if (dest == null) throw new NullPointerException();
			int ret = nativeGetAllRecords(firstRecord*16,dest,offset);
			if (ret == need) return true;
			// If an error ocurs then we will go to the java version
			// which will give a more comprehensive errot.
		}catch(SecurityException e){
			hasNative = false;
		}catch(UnsatisfiedLinkError er){
			hasNative = false;
		}
		if (h != null){
			h.doing = "Locating Records";
			h.changed();
		}
		int i = offset, rec;
		try{
			for (i = offset, rec = getFirstEntry(); rec != 0; rec = getNextEntry(rec)){
				dest[i++] = rec;
				//if (((i-offset)%1000) == 0) ewe.sys.Vm.debug("Got: "+(i-offset));
				if (h != null)
					if (h.shouldStop) break;
					else mThread.yield(250);
			}
			if (i-offset != need) throw new CorruptedDataException("Incorrect number of records found.");
		}catch(CorruptedDataException cde){
			String cd = cde.getMessage();
			if (cd != null && cd.length() != 0) cd += "\n";
			else cd = "";
			cd += "Got: "+(i-offset)+" records of reported: "+need;
			throw new CorruptedDataException(cd);
		}
		if (h != null) return !h.shouldStop;
		return true;
	}finally{_unlock();}
}
//===================================================================
public void close() throws IOException
//===================================================================
{
	_lock(); try{
		stream.close();
	}finally{_unlock();}
}
//===================================================================
public void delete() throws IOException
//===================================================================
{
	_lock(); try{
		stream.delete();
	}finally{_unlock();}
}
//===================================================================
public void rename(String newName) throws IOException
//===================================================================
{
	_lock(); try{
		stream.rename(newName);
	}finally{_unlock();}
}
//===================================================================
public long getEntriesCount() throws IOException
//===================================================================
{
	_lock(); try{
		return stream.readIntAt(RECORD_COUNT);
	}finally{_unlock();}
}

boolean defaultUseFree = true;
/*
//-------------------------------------------------------------------
void dumpRecords() throws IOException
//-------------------------------------------------------------------
{
	ewe.sys.Vm.debug("--------------------");
	for (int rec = getFirstEntry(); rec != 0; rec = getNextEntry(rec)){
		byte [] data = readRecord(rec);
		ewe.sys.Vm.debug(loadUtf8String(data,0));
	}
	ewe.sys.Vm.debug("--------------------");
	ewe.sys.Vm.debug(loadUtf8String(readMetaData(),0));
	ewe.sys.Vm.debug("--------------------");
}
*/
//
// FIXME - implement this.
//
public void removeFieldIDs(int fieldID) throws IOException
{

}

//-------------------------------------------------------------------
protected void doErase(int location, boolean isDeleted) throws IOException
//-------------------------------------------------------------------
{
	if (location == 0) return;
	_lock(); try{
		_eraseEntry(location);
	}finally{_unlock();}
}
//-------------------------------------------------------------------
protected void doMarkAsDeleted(int location,long OID,long deleteTime) throws IOException
//-------------------------------------------------------------------
{
	if (location == 0) return;
	markAsDeleted(location,OID,deleteTime);
}
//-------------------------------------------------------------------
protected ByteArray doLoad(int location,ByteArray dest) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	if (dest != null) dest.clear();
	return readRecord(location,dest);
}
//-------------------------------------------------------------------
protected int doSave(int location,byte[] data,int offset,int length) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	if (location == 0) return addRecord(data,offset,length);
	else return modifyRecord(location,data,offset,length);
}
//===================================================================
public long[] getDeletedEntries() throws IOException
//===================================================================
{
	_lock(); try{
		IntArray got = getDeletedEntries(null);
		long [] ret = new long[got.length];
		for (int i = 0; i<ret.length; i++)
			ret[i] = getDeletedEntryInfo(got.data[i],null);
		return ret;
	}finally{_unlock();}
}
//===================================================================
public Time getModifiedTime() throws IOException
//===================================================================
{
	return stream.getModifiedTime();
}
//===================================================================
public boolean setModifiedTime(Time t) throws IOException
//===================================================================
{
	return stream.setModifiedTime(t);
}
private Hashtable metas;

//##################################################################
class metaEntry{
//##################################################################
long position;
int length;
String name;
//##################################################################
}
//##################################################################

//-------------------------------------------------------------------
private metaEntry findMeta(String name)
//-------------------------------------------------------------------
{
	if (metas == null) return null;
	return (metaEntry)metas.get(name);
}
//===================================================================
public Object getMetaData(String name,int length,boolean mustExist) throws IOException, IllegalArgumentException
//===================================================================
{
	metaEntry e = findMeta(name);
	if (e != null){
		if (length > e.length) throw new IllegalArgumentException();
		return e;
	}
	metaData mt = new metaData(name);
	long where = mt.openForDirectWrite(mustExist,length);
	if (where == -1) return null;
	e = new metaEntry();
	e.position = where;
	e.length = length;
	if (metas == null) metas = new Hashtable();
	metas.put(name,e);
	return e;
}
//===================================================================
public void readMetaData(Object metaLocation,int metaOffset,byte[] data,int offset,int length) throws IOException
//===================================================================
{
	metaEntry me = (metaEntry)metaLocation;
	if (metaOffset+length > me.length) throw new IllegalArgumentException();
	stream.readAll(me.position+metaOffset,data,offset,length);
}
//===================================================================
public void writeMetaData(Object metaLocation,int metaOffset,byte[] data,int offset,int length) throws IOException
//===================================================================
{
	metaEntry me = (metaEntry)metaLocation;
	if (metaOffset+length > me.length) throw new IllegalArgumentException();
	stream.writeAll(me.position+metaOffset,data,offset,length);
}
//===================================================================
public boolean deleteMetaData(String name) throws IOException
//===================================================================
{
	metaEntry mt = findMeta(name);
	if (mt != null) metas.remove(name);
	metaData md = new metaData(name);
	return md.delete();
}
//===================================================================
public int metaDataLength(String name) throws IOException
//===================================================================
{
	metaEntry mt = findMeta(name);
	if (mt != null) return mt.length;
	metaData md = new metaData(name);
	return (int)md.size();
}
//===================================================================
public int readMetaDataInt(Object metaLocation,int offset) throws IOException
//===================================================================
{
	metaEntry me = (metaEntry)metaLocation;
	if (offset+4 > me.length) throw new IllegalArgumentException();
	return stream.readIntAt(me.position+offset);
}
//===================================================================
public void writeMetaDataInt(Object metaLocation,int offset,int value) throws IOException
//===================================================================
{
	metaEntry me = (metaEntry)metaLocation;
	if (offset+4 > me.length) throw new IllegalArgumentException();
	stream.safeWrite(me.position+offset,value);
}
//===================================================================
public DatabaseEntry getDeletedEntry(long oid,DatabaseEntry dest) throws IOException
//===================================================================
{
	_lock(); try{
		int where = _locateDeletedEntry(oid);
		if (where == -1) return null;
		if (dest == null) dest = getNewData();
		RecordDatabaseEntry entry = (RecordDatabaseEntry)dest;
		dest.reset();
	  entry.stored = where;
		entry.isDeleted = true;
		getDeletedEntryInfo(where,now);
		dest.setField(OID_FIELD,oid);
		dest.setField(MODIFIED_FIELD,now);
		return dest;
	}finally{_unlock();}
}

/*
//=================================================================
public static void main(String[] args) throws IOException
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	RandomAccessDatabaseStream s = new RandomAccessDatabaseStream(new RandomAccessFile("records.dat","rw"),"rw");
	RecordFile rf = new RecordFile();
	rf.setDatabaseStream(s);
	ByteArray ba = new ByteArray();
	if (args.length != 0 && args[0].equalsIgnoreCase("-write")){
		rf.initialize();
		byte [] all = EncodedUTF8String.store("0000000 - Record stored in this record file!");
		char [] dest = new char[7];
		int start = ewe.sys.Vm.getTimeStamp(), now = start;
		for (int i = 0;i<5000;i++){
			int len = ewe.sys.Convert.formatInt(i,dest,0);
			Utils.encodeJavaUtf8String(dest,0,len,all,9-len);
			rf.addRecord(all,false);
			int nn = ewe.sys.Vm.getTimeStamp();
			if (nn-now >= 1000){
				now = nn;
				int num = (i*1000)/(now-start);
				ewe.sys.Vm.debug("RPS: "+num+", "+i);
			}
		}
	}else{
		int start = ewe.sys.Vm.getTimeStamp(), now = start;
		IntArray all = rf.getAllEntries(null);
		now = ewe.sys.Vm.getTimeStamp()-now;
		ewe.sys.Vm.debug("Read all: "+now);
		start = now = ewe.sys.Vm.getTimeStamp();
		int did = 0;
		for (int i = 0; i<10; i++){
			String got = EncodedUTF8String.load(rf.readRecord(all.data[i*10+3]),0);
			ewe.sys.Vm.debug("At pos "+(i*10+3)+": "+got);
		}
		for (int rec = rf.getFirstEntry(); rec != 0; rec = rf.getNextEntry(rec)){
			ba.clear();
			rf.readRecord(rec,ba);
			int nn = ewe.sys.Vm.getTimeStamp();
			if (nn-now >= 1000){
				now = nn;
				int num = (did*1000)/(now-start);
				ewe.sys.Vm.debug("RPS: "+num+", "+did);
			}
			did++;
		}
	}
	rf.close();
	ewe.sys.Vm.debug("Done!");
	ewe.sys.mThread.nap(1000);
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

