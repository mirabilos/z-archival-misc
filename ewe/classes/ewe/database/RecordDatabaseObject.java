package ewe.database;
import ewe.io.IOException;
import ewe.io.DataProcessor;
import ewe.io.RandomAccessFile;
import ewe.sys.Handle;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.sys.Locale;
import ewe.util.ByteArray;
import ewe.util.IntArray;
import ewe.util.Utils;
import ewe.util.Vector;
import ewe.util.mString;
import ewe.util.TextEncoder;
import ewe.util.TextDecoder;
import ewe.util.Iterator;
import ewe.util.ObjectIterator;
import ewe.reflect.Wrapper;
import ewe.reflect.FieldTransfer;
import ewe.reflect.Reflect;
import ewe.util.WeakSet;
import ewe.util.IteratorEnumerator;

//##################################################################
public abstract class RecordDatabaseObject extends DatabaseObject {
//##################################################################
/**
* Get all the records locations and place them in the destination int array.
**/
//-------------------------------------------------------------------
protected abstract boolean getAllRecords(Handle h,int[] destination,int offset,int reported) throws IOException;
protected abstract void doMarkAsDeleted(int location,long OID,long when) throws IOException;
protected abstract void doErase(int location,boolean isDeleted) throws IOException;
protected abstract int doSave(int location,byte[] data,int offset,int length) throws IOException;
protected abstract ByteArray doLoad(int location,ByteArray dest) throws IOException;
protected abstract void removeFieldIDs(int fieldID) throws IOException;
protected abstract int getNextEntry(int prev) throws IOException;
//-------------------------------------------------------------------

//===================================================================
public Iterator entries()
//===================================================================
{
	return new IteratorEnumerator(){
		int nextToGo = 0;
		RuntimeException error;
		{
			updateNext();
		}
		void updateNext(){
			try{
				nextToGo = getNextEntry(nextToGo);
			}catch(IOException e){
				nextToGo = 0;
				error = new DatabaseIOException(e);
			}
		}
		public boolean hasNext(){
			return nextToGo != 0;
		}
		public Object next(){
			if (nextToGo == 0) {
				if (error != null) throw error;
				return null;
			}
			int now = nextToGo;
			updateNext();
			RecordDatabaseEntry e = (RecordDatabaseEntry)getNewData();
			e.stored = now;
			try{
				doLoad(e);
			}catch(IOException er){
				nextToGo = 0;
				error = new DatabaseIOException(er);
				throw error;
			}
			return e;
		}
	};
}
//-------------------------------------------------------------------
protected DatabaseEntry makeNewData()
//-------------------------------------------------------------------
{
	return new RecordDatabaseEntry(this);
}
//-------------------------------------------------------------------
protected FoundEntries makeNewFoundEntries()
//-------------------------------------------------------------------
{
	return new RecordFoundEntries(this);
}
//-------------------------------------------------------------------
protected IntArray getAllEntries(Handle h,IntArray array) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	if (array == null) array = new IntArray();
	array.length = 0;
	long num = getEntriesCount();
	if ((int)num != num) throw new DataTooBigException();
	int need = (int)num;
	if (array.data == null || array.data.length < need)
		array.data = new int[need];
	if (!getAllRecords(h,array.data,0,need)) return null;
	array.length = need;
	return array;
}
//-------------------------------------------------------------------
protected FoundEntries getAllFoundEntries(Handle h) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	IntArray got = getAllEntries(h,null);
	if (got == null) return null;
	RecordFoundEntries rfe = (RecordFoundEntries)getEmptyEntries();
	rfe.ids = got;
	return rfe;
}
/*
//===================================================================
public DatabaseEntry getDeletedEntry(long OID) throws IOException
//===================================================================
{
	int location = getDeletedEntry(OID,now);
	if (location == 0) return null;
	DatabaseEntry ret = getNewData();
	ret.clear();
	ret.setField(OID_FIELD,OID);
	ret.setField(MODIFIED_FIELD,now);
	ret.stored = location;
	return ret;
}
*/
//-------------------------------------------------------------------
protected void doMarkAsDeleted(DatabaseEntryObject e) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	RecordDatabaseEntry entry = (RecordDatabaseEntry)e;
	if (entry.stored == 0 || e.isADeletedEntry()) return;
	long oid = entry.getField(OID_FIELD,(long)0);
	long del = now.setToCurrentTime().getEncodedTime();
	doMarkAsDeleted(entry.stored,oid,del);
	entry.stored = 0;
}
//-------------------------------------------------------------------
protected void doErase(DatabaseEntryObject e) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	RecordDatabaseEntry entry = (RecordDatabaseEntry)e;
	if (entry.stored == 0) return;
	int was = entry.stored;
	doErase(entry.stored,entry.isADeletedEntry());
	entry.stored = 0;
}
//-------------------------------------------------------------------
protected boolean doStore(DatabaseEntryObject e) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	RecordDatabaseEntry entry = (RecordDatabaseEntry)e;
	boolean isNew = entry.stored == 0;
	if (encryptor == null){
		ByteArray s = entry.getDataForSaving();
		entry.stored = doSave(entry.stored,s.data,0,s.length);
	}else{
		byteArray = e.encode(byteArray,encryptor);
		entry.stored = doSave(entry.stored,byteArray.data,0,byteArray.length);
	}
	return isNew;
}
//-------------------------------------------------------------------
protected void doLoad(DatabaseEntryObject e) throws IOException, IllegalStateException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	RecordDatabaseEntry entry = (RecordDatabaseEntry)e;
	if (entry.stored == 0) throw new IllegalStateException();
	if (decryptor == null){
		doLoad(entry.stored,entry.getDataForLoading());
		entry.decodeRecord();
	}else{
		ByteArray ba = byteArray;
		if (ba == null) ba = new ByteArray();
		ba.clear();
		ba = doLoad(entry.stored,ba);
		entry.decode(ba.data,0,ba.length,decryptor);
	}
}
//##################################################################
}
//##################################################################

