/* $MirOS: contrib/hosted/ewe/classes/ewe/database/DatabaseObject.java,v 1.4 2008/05/02 20:52:00 tg Exp $ */

package ewe.database;
import ewe.data.PropertyList;
import ewe.io.BlockingStreamObject;
import ewe.io.DataProcessor;
import ewe.io.File;
import ewe.io.IO;
import ewe.io.IOException;
import ewe.io.InputStream;
import ewe.io.OutputStream;
import ewe.io.RandomAccessFile;
import ewe.io.Stream;
import ewe.reflect.FieldTransfer;
import ewe.reflect.Method;
import ewe.reflect.Reflect;
import ewe.reflect.Wrapper;
import ewe.sys.Handle;
import ewe.sys.Locale;
import ewe.sys.Lock;
import ewe.sys.Long;
import ewe.sys.Time;
import ewe.sys.Vm;
import ewe.util.ByteArray;
import ewe.util.ByteEncoder;
import ewe.util.Comparer;
import ewe.util.ComparerObjectFinder;
import ewe.util.EventDispatcher;
import ewe.util.Hashtable;
import ewe.util.IntArray;
import ewe.util.Iterator;
import ewe.util.IteratorEnumerator;
import ewe.util.ObjectFinder;
import ewe.util.ObjectIterator;
import ewe.util.Vector;
import ewe.util.WeakSet;
import ewe.util.mString;
/**
* A building block for a Database. It handles everything to do with
* storing fields and sorts.
**/
//##################################################################
public abstract class DatabaseObject implements Database{//implements Database{
//##################################################################
/* Don't move the next four.*/
protected RandomAccessFile file;
protected DatabaseStream stream;
protected DataProcessor decryptor;
protected DataProcessor encryptor;
/*---------------------------*/
protected Locale myLocale = Vm.getLocale();
protected ByteArray byteArray = new ByteArray();
protected WeakSet foundEntries;
protected WeakSet entries;
//protected Vector foundEntries = new Vector();
protected Time now = new Time();
protected Lock lock = new Lock();
/*---------------------------*/
protected DatabaseEntry buffer;
protected String openMode;
protected Hashtable openIndexes;
protected Vector openModifiers;

private long currentState;
private EventDispatcher dispatcher;

private PropertyList pl = new PropertyList();
protected DataValidator dataValidator;
protected boolean encryptorNotSet = false;

protected final int INDEX_FLAG_DO_REINDEX = 0x1;
protected MetaFlag indexFlag;

//===================================================================
public abstract long getEntriesCount() throws IOException;
//===================================================================

//===================================================================
public boolean isOpenForReadWrite()
//===================================================================
{
	return openMode.equalsIgnoreCase("rw");
}
//===================================================================
public PropertyList getProperties()
//===================================================================
{
	return pl;
}

//-------------------------------------------------------------------
Method getSetKey(Object obj)
//-------------------------------------------------------------------
{
	return Reflect.getForObject(obj).getMethod("setKey","(Ljava/lang/Object;)V",Method.PUBLIC);
}
//-------------------------------------------------------------------
void setKey(DataProcessor processor,Object key)
//-------------------------------------------------------------------
{
	Wrapper w = new Wrapper().setObject(key);
	getSetKey(processor).invoke(processor,new Wrapper[]{w},null);
}

//-------------------------------------------------------------------
DataProcessor createWithKey(String name,Object key)
//-------------------------------------------------------------------
{
	Reflect r = Reflect.loadForName(name);
	DataProcessor ret;
	if (r == null) return null;
	Wrapper w = new Wrapper().setObject(key);
	Wrapper[] p = new Wrapper[]{w};
	ret = (DataProcessor)r.newInstance("(Ljava/lang/Object;)V",p);
	if (ret != null) return ret;
	if (key instanceof String){
		ret = (DataProcessor)r.newInstance("(Ljava/lang/String;)V",p);
		if (ret != null) return ret;
	}
	if (key instanceof byte[]){
		w.setArray(key);
		ret = (DataProcessor)r.newInstance("([B)V",p);
		if (ret != null) return ret;
	}
	ret = (DataProcessor)r.newInstance();
	if (ret == null) return null;
	try{
		setKey(ret,key);
		return ret;
	}catch(Exception e){
		return null;
	}
}

//-------------------------------------------------------------------
boolean testEncryption(DataProcessor d,DataProcessor e,boolean saveIt) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	ByteArray got = null;
	if (true || saveIt){
		got = ewe.security.Encryptor.makeEncryptorTest((DataProcessor)e,64,null);
		if (!ewe.security.Encryptor.testDecryptor((DataProcessor)d,got.data,0,got.length))
			throw new IllegalArgumentException();
	}
	byte[] all = getMetaData("_EncryptionTest_");
	if (all != null){
		if (!ewe.security.Encryptor.testDecryptor((DataProcessor)d,all,0,all.length))
			return false;
		//ewe.sys.Vm.debug("Passed.");
	}else{
		if (saveIt) {
			byte[] gtb = got.toBytes();
			setMetaData("_EncryptionTest_",gtb);
		}
	}
	return true;
}
//===================================================================
public boolean useEncryption(DataProcessor decryptor, DataProcessor encryptor)
throws IOException, IllegalArgumentException
//===================================================================
{
	if (specs.encryptorClass == null) return false;
	if (!testEncryption(decryptor,encryptor,false)) return false;
	this.decryptor = decryptor;
	this.encryptor = encryptor;
	encryptorNotSet = false;
	return true;
}
//===================================================================
public boolean usePassword(Object obj)
//===================================================================
{
	if (obj == null){
		if (specs.encryptorClass == null) return true;
	}else if (specs.encryptorClass != null){
		try{
			DataProcessor e = createWithKey(specs.encryptorClass,obj);
			DataProcessor d = createWithKey(specs.decryptorClass,obj);
			if (useEncryption(d,e)) return true;
		}catch(Exception e){
		}
	}
	ewe.sys.mThread.nap(1000);
	return false;
}
//===================================================================
public void setEncryption(DataProcessor decryptor,DataProcessor encryptor) throws IllegalArgumentException, IOException
//===================================================================
{
	try{
		if (this.decryptor != null || this.encryptor != null || getEntriesCount() != 0) throw new IllegalStateException();
		if (!testEncryption(decryptor,encryptor,true)) throw new IllegalArgumentException();
		String d = decryptor.getClass().getName();
		String e = encryptor.getClass().getName();
		specs.encryptorClass = e;
		specs.decryptorClass = d;
		save();
		this.decryptor = decryptor;
		this.encryptor = encryptor;
	}catch(IOException e){
		throw e;
	}catch(Exception e){
		e.printStackTrace();
		throw new IllegalArgumentException();
	}
}
//===================================================================
public void setPassword(String password) throws IllegalArgumentException, IOException
//===================================================================
{
	if (password != null) {
		DataProcessor d = new ewe.security.Decryptor(password);
		DataProcessor e = new ewe.security.Encryptor(password);
		setEncryption(new ewe.security.Decryptor(password),new ewe.security.Encryptor(password));
	}
}
//===================================================================
public Locale getLocale()
//===================================================================
{
	return myLocale;
}
//===================================================================
public void setLocale(Locale locale)
//===================================================================
{
	myLocale = locale;
}
//-------------------------------------------------------------------
protected abstract DatabaseEntry makeNewData();
protected abstract FoundEntries makeNewFoundEntries();
protected abstract DatabaseIndex makeNewIndex(String name);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected FoundEntries registerNewFoundEntries(FoundEntries fe)
//-------------------------------------------------------------------
{
	if (foundEntries == null) foundEntries = new WeakSet();
	foundEntries.add(fe);
	return fe;
}
//-------------------------------------------------------------------
protected DatabaseIndex registerIndex(DatabaseIndex index,String name) throws IOException
//-------------------------------------------------------------------
{
	if (openMode.equals("rw")){
		MetaData mt = getNewMetaData("Index:"+name);
		mt.openForAppending(false);
		((FoundEntriesObject)index).indexRecorder = new OutputStream(mt);
	}
	if (openIndexes == null) openIndexes = new Hashtable();
	openIndexes.put(name,index);
	return index;
}
//-------------------------------------------------------------------
protected DatabaseIndex findOpenIndex(String indexName)
//-------------------------------------------------------------------
{
	if (openIndexes == null) return null;
	return (DatabaseIndex)openIndexes.get(indexName);
}
//===================================================================
public FoundEntries getEmptyEntries()
//===================================================================
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	return registerNewFoundEntries(makeNewFoundEntries());
}
//===================================================================
public DatabaseEntry getNewData()
//===================================================================
{
	DatabaseEntry de = makeNewData();
	if (entries == null) entries = new WeakSet();
	entries.add(de);
	return de;
}
//-------------------------------------------------------------------
protected Iterator getMyFoundEntries()
//-------------------------------------------------------------------
{
	return foundEntries == null ? new ObjectIterator(null) : foundEntries.entries();
	//return foundEntries == null ? new ObjectIterator(null) : foundEntries.iterator();
}
//-------------------------------------------------------------------
protected Iterator getMyDatabaseEntries()
//-------------------------------------------------------------------
{
	return entries == null ? new ObjectIterator(null) : entries.entries();
}
//-------------------------------------------------------------------
protected boolean hasFoundEntries()
//-------------------------------------------------------------------
{
	return foundEntries != null && !foundEntries.isEmpty();
}
//-------------------------------------------------------------------
protected boolean hasDatabaseEntries()
//-------------------------------------------------------------------
{
	return entries != null && !entries.isEmpty();

}
/*
//===================================================================
public boolean isDescending(int sortID) throws IllegalArgumentException
//===================================================================
{
	FieldSortEntry fse = findSort(sortID);
	if (fse == null) throw new IllegalArgumentException();
	return fse.isDescending();
}
*/
//===================================================================
public int[] toCriteria(int sortID) throws IllegalArgumentException
//===================================================================
{
	FieldSortEntry fse = findSort(sortID);
	if (fse == null) throw new IllegalArgumentException();
	return fse.toCriteria();
}
/***********************************************************************

These deal with fields and sorts and specs.

***********************************************************************/


protected abstract MetaData getNewMetaData(String name) throws IOException;

//##################################################################
protected abstract class MetaData extends BlockingStreamObject{
//##################################################################

String name;

/**
This is only valid after a read operation.
**/
public int numberOfFragments;
//-------------------------------------------------------------------
protected MetaData(String name)
//-------------------------------------------------------------------
{
	this.name = name;
}
//===================================================================
// Replace the other with the same name. A consistent replace is done
// at close(). i.e. either the record is replaced or it is not. So an
// error will lose the new but not the old.
//
public abstract void openForReplacing(int bytesNeededToWrite) throws IOException;
//===================================================================
// Add on to an existing record.
//
public abstract boolean openForAppending(boolean mustExist) throws IOException;
//===================================================================
// Overwrite the existing record. An error will lose both the old and the new.
//
public abstract void openForOverwriting(int bytesNeededToWrite) throws IOException;
//===================================================================
public abstract int openForReading() throws IOException;
//===================================================================
public abstract void write(byte[] data, int offset, int length) throws IOException;
public abstract int read(byte[] data, int offset, int length) throws IOException;
//===================================================================
public abstract long size() throws IOException;
//===================================================================
public abstract boolean delete() throws IOException;
//===================================================================
public void readAll(byte[] data,int offset, int length) throws IOException
//===================================================================
{
	IO.readAll(this,data,offset,length);
}
//===================================================================
public ByteArray openAndReadAll(ByteArray ba) throws IOException
//===================================================================
{
	if (ba == null) ba = new ByteArray();
	ba.clear();
	int size = openForReading();
	if (size == -1) return null;
	ba.makeSpace(0,size);
	readAll(ba.data,0,size);
	super.close();
	return ba;
}
//===================================================================
public byte[] openAndReadAll() throws IOException
//===================================================================
{
	int size = openForReading();
	if (size == -1) return null;
	byte[] ret = new byte[size];
	readAll(ret,0,size);
	super.close();
	return ret;
}
//##################################################################
}
//##################################################################
//===================================================================
public Stream openStreamForWriting(String name,boolean append) throws IOException
//===================================================================
{
	MetaData mt = getNewMetaData(name);
	if (append) mt.openForAppending(false);
	else mt.openForOverwriting(0);
	return mt;
}
//===================================================================
public Stream openStreamForReplacing(String name) throws IOException
//===================================================================
{
	MetaData mt = getNewMetaData(name);
	mt.openForReplacing(0);
	return mt;
}
//===================================================================
public Stream openStreamForReading(String name) throws IOException
//===================================================================
{
	MetaData mt = getNewMetaData(name);
	mt.openForReading();
	return mt;
}
//===================================================================
public boolean deleteStream(String name) throws IOException
//===================================================================
{
	MetaData mt = getNewMetaData(name);
	return mt.delete();
}
//===================================================================
public long getStreamLength(String name) throws IOException
//===================================================================
{
	MetaData mt = getNewMetaData(name);
	return mt.size();
}
//-------------------------------------------------------------------
protected byte[] getMetaData(String name) throws IOException
//-------------------------------------------------------------------
{
	return getNewMetaData(name).openAndReadAll();
}
//-------------------------------------------------------------------
protected void setMetaData(String name, byte[] data) throws IOException
//-------------------------------------------------------------------
{
	MetaData md = getNewMetaData(name);
	md.openForReplacing(data.length);
	md.write(data,0,data.length);
	md.close();
}
//-------------------------------------------------------------------
protected void overwriteMetaData(String name, byte[] data) throws IOException
//-------------------------------------------------------------------
{
	MetaData md = getNewMetaData(name);
	md.openForOverwriting(data.length);
	md.write(data,0,data.length);
	md.close();
}

private Vector fields = new Vector();
private Vector sorts = new Vector();
private Reflect objectClass;

//-------------------------------------------------------------------
private FieldSortEntry find(int id,Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	for (int i = 0; i<sz; i++){
		FieldSortEntry fe = (FieldSortEntry)v.get(i);
		if (fe.id == id) return fe;
	}
	return null;
}
//-------------------------------------------------------------------
private FieldSortEntry find(String name,Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	for (int i = 0; i<sz; i++){
		FieldSortEntry fe = (FieldSortEntry)v.get(i);
		if (fe.name.equalsIgnoreCase(name)) return fe;
	}
	return null;
}
//-------------------------------------------------------------------
private boolean deleteId(int id,Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	for (int i = 0; i<sz; i++){
		FieldSortEntry fe = (FieldSortEntry)v.get(i);
		if (fe.id == id){
			v.del(i);
			return true;
		}
	}
	return true;
}
//-------------------------------------------------------------------
protected FieldSortEntry findField(int fieldID) {return find(fieldID,fields);}
protected FieldSortEntry findSort(int fieldID) {return find(fieldID,sorts);}
//-------------------------------------------------------------------
//-------------------------------------------------------------------
private int findReservedFieldIndex(int fieldID)
//-------------------------------------------------------------------
{
	for (int i = 0; i<reservedFieldIDs.length; i++)
		if (reservedFieldIDs[i] == fieldID) return i;
	return -1;
}
/**
* This is used to set the class of the object used for data transfer to
* and from the table. If you call setFields(Object objectOrClass,String fields)
* then you do not need to call this method as it will be done for you.
* @param objectOrClass
*/
//===================================================================
public void setObjectClass(Object objectOrClass) throws IllegalArgumentException
//===================================================================
{
	objectClass = Reflect.toReflect(objectOrClass);
	if (objectClass == null) throw new IllegalArgumentException();
}
//===================================================================
public Reflect getObjectClass()
//===================================================================
{
	return objectClass;
}
//-------------------------------------------------------------------
private int toType(Wrapper w)
//-------------------------------------------------------------------
{
	switch(w.getType()){
		case 'I': return INTEGER;
		case 'D': return DOUBLE;
		case 'Z': return BOOLEAN;
		case 'J': return LONG;
		case '[':
		case 'L':
			{
			Reflect r = Reflect.getForObject(w.getObject());
			if (r == null) return 0;
			if (r.getClassName().equals("[B")) return BYTE_ARRAY;
			if (r.isTypeOf("Ljava/lang/String;")) return STRING;
			if (r.isTypeOf("Lewe/sys/TimeOfDay;")) return TIME;
			if (r.isTypeOf("Lewe/sys/DayOfYear;")) return DATE;
			if (r.isTypeOf("Lewe/sys/Decimal;")) return DECIMAL;
			if (r.isTypeOf("Lewe/database/TimeStamp;")) return TIMESTAMP;
			if (r.isTypeOf("Lewe/sys/Time;")) return DATE_TIME;
			if (r.isTypeOf("Lewe/util/ByteArray;")) return BYTE_ARRAY;
			else return JAVA_OBJECT;
		}
	}
	return 0;
}
//-------------------------------------------------------------------
private int toType(FieldTransfer ft,Object data)
//-------------------------------------------------------------------
{
	String fieldType = ft.transferType;
	switch(fieldType.charAt(0)){
		case 'I': return INTEGER;
		case 'D': return DOUBLE;
		case 'Z': return BOOLEAN;
		case 'J': return LONG;
		case '[': if(fieldType.equals("[B")) return BYTE_ARRAY;
			/* XXX really? */
			/* FALLTHROUGH */
		case 'L':{
			if (fieldType.equals("Ljava/lang/String;")) return STRING;
			if (Reflect.isTypeOf(fieldType,"Lewe/sys/TimeOfDay;")) return TIME;
			if (Reflect.isTypeOf(fieldType,"Lewe/sys/DayOfYear;")) return DATE;
			if (Reflect.isTypeOf(fieldType,"Lewe/sys/Decimal;")) return DECIMAL;
			if (Reflect.isTypeOf(fieldType,"Lewe/database/TimeStamp;")) return TIMESTAMP;
			if (Reflect.isTypeOf(fieldType,"Lewe/sys/Time;")) return DATE_TIME;
			if (Reflect.isTypeOf(fieldType,"Lewe/util/ByteArray;")) return BYTE_ARRAY;
			//if (Reflect.isTypeOf(fieldType,"Lewe/util/ByteEncodable;")) return BYTE_ARRAY;
			return JAVA_OBJECT;
			/*
			if (Reflect.isTypeOf(fieldType,"Lewe/data/EditableData;")){
				if (data == null) return 0;
				ewe.data.EditableData obj = (ewe.data.EditableData)ft.getFieldValue(data);
				if (obj == null) return 0;
				Wrapper w = new Wrapper();
				obj.toSaveableData(w);
				return toType(w);
			}
			*/
		}
	}
	return 0;
}
//-------------------------------------------------------------------
private FieldTransfer getFieldTransfer(String field,Object data,Object di) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	return getFieldTransfer(field,0,data,di);
}
//-------------------------------------------------------------------
private FieldTransfer getFieldTransfer(String field,int fieldType,Object data,Object di) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (objectClass == null) throw new IllegalArgumentException();
	FieldTransfer ft = new FieldTransfer(objectClass,data,field,di,null);
	if (!ft.isValid() && fieldType != 0) ft = new FieldTransfer(objectClass,data,field+"$"+DatabaseUtils.getTypeSpecifier(fieldType,true),di,null);
	if (ft.isValid()){
		if (toType(ft,data) != 0)
			return ft;
	}
	throw new IllegalArgumentException(ft.isValid()+", "+field+" is not a valid field");
}
Vector fieldTransfers;

//===================================================================
public int [] setSorts(Object objectOrClass) throws IllegalArgumentException
//===================================================================
{
	return setSorts(objectOrClass,null);
}

//===================================================================
public int [] setSorts(Object objectOrClass,String sorts) throws IllegalArgumentException
//===================================================================
{
	return setSorts(objectOrClass,sorts,SETFIELDS_MODE_SET);
}
//===================================================================
public int [] ensureSorts(Object objectOrClass,String sorts) throws IllegalArgumentException
//===================================================================
{
	return setSorts(objectOrClass,sorts,SETFIELDS_MODE_ENSURE);
}
//===================================================================
public int [] overrideSorts(Object objectOrClass,String sorts) throws IllegalArgumentException
//===================================================================
{
	return setSorts(objectOrClass,sorts,SETFIELDS_MODE_ENSURE);
}

//-------------------------------------------------------------------
public int [] setSorts(Object objectOrClass,String sorts,int operation) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (sorts == null) sorts = ewe.data.LiveObject.appendAllFields("_sorts",objectOrClass,false);
	String [] all = mString.split(sorts,'|');
	int [] ret = new int[all.length/2];
	for (int i = 0; i<all.length-1; i+=2){
		String name = ewe.util.mString.leftOf(all[i],'$');
		String opts = ewe.util.mString.rightOf(all[i],'$');
		int options = 0;
		for (int j = 0; j<opts.length(); j++)
			switch(opts.charAt(j)){
				case 'i': case 'I': options |= SORT_IGNORE_CASE; break;
				//case 'd': case 'D': options |= SORT_DESCENDING; break;
				case 't': case 'T': options |= SORT_DATE_ONLY; break;
				case 'm': case 'M': options |= SORT_TIME_ONLY; break;
				case 'u': case 'U': options |= SORT_UNKNOWN_IS_LESS_THAN_KNOWN; break;
			}
		int id = findSort(name);
		if (id == 0 || operation == SETFIELDS_MODE_SET){
			ret[i/2] = addSort(name,options,all[i+1]);
		}else {
			int so = getSortOptions(id);
			int [] fields = getSortFields(id);
			int [] myFields = makeFields(all[i+1]);
			boolean matches = true;
			if (options != so) matches = false;
			if (fields.length != myFields.length) matches = false;
			if (matches) for (int is = 0; is<fields.length; is++)
				if (fields[is] != myFields[is]){
					matches = false;
				}

			if (matches) ret[i/2] = id;
			else if (operation == SETFIELDS_MODE_ENSURE)
				throw new IllegalArgumentException("Existing sort is incompatible.");
			else{
				removeSort(so);
				ret[i/2] = addSort(name,options,all[i+1]);
			}
		}
	}
	return ret;
}
/**
 * Set the fields for the DataTable, which must match fields in the objectOrClass
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,null,null);
}
/**
 * Set the fields for the DataTable, which must match fields in the objectOrClass
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields. The types of these fields must be one of:
 <ul><li>int<li>long<li>double<li>boolean<li>String<li>Date<li>byte []</ul>
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,fields,null);
}
/**
 * Set the fields for the DataTable, which must match fields in the objectOrClassOrReflect
 * transfer object.
 * @param objectOrClass The object or class of the object to be used for data transfer.
 * @param fields A comma separated list of fields. The types of these fields must be one of:
 <ul><li>int<li>long<li>double<li>boolean<li>String<li>Date<li>byte []</ul>
 * @param headers An optional comma separated list of headers.
 * @return an array of integers representing the field IDs.
 */
//===================================================================
public int [] setFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,fields,headers,SETFIELDS_MODE_SET);
}
//===================================================================
public int [] ensureFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,fields,headers,SETFIELDS_MODE_ENSURE);
}
//===================================================================
public int [] overrideFields(Object objectOrClass,String fields,String headers) throws IllegalArgumentException
//===================================================================
{
	return setFields(objectOrClass,fields,headers,SETFIELDS_MODE_OVERRIDE);
}

protected static final int SETFIELDS_MODE_SET = 1;
protected static final int SETFIELDS_MODE_ENSURE = 2;
protected static final int SETFIELDS_MODE_OVERRIDE = 3;

//-------------------------------------------------------------------
protected int [] setFields(Object objectOrClass,String fields,String headers,int mode) throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (fields == null) fields = ewe.data.LiveObject.getFieldList(objectOrClass,false);
	DatabaseEntry ded = getNewData();
	setObjectClass(objectOrClass);
	if (fieldTransfers == null) fieldTransfers = new Vector();
	String [] s = mString.split(fields,',');
	String [] h = headers == null ? null : mString.split(headers,',');
	int [] ret = new int[s.length];
	Object data = Reflect.toNonReflect(objectOrClass);
	for (int i = 0; i<s.length; i++){
		FieldTransfer ft = getFieldTransfer(s[i],data,ded);
		String fieldName = ft.pureName;//ft.fieldName;
		fieldTransfers.add(ft);
		int type = toType(ft,data);
		int f = findField(fieldName);
		if (f == 0 || mode == SETFIELDS_MODE_SET)
			ret[i] = addField(fieldName,type);
		else{
			int ty = getFieldType(f);
			if (ty == type) ret[i] = f;
			else if (mode == SETFIELDS_MODE_ENSURE) throw new IllegalArgumentException("Incompatible field type: "+fieldName);
			else{
				removeField(f);
				ret[i] = addField(fieldName,type);
			}
		}
		//ewe.sys.Vm.debug("Field: "+fieldName+" is: "+getFieldType(ret[i]));
		if (h != null)
			findField(ret[i]).header = h[i];
		//ewe.sys.Vm.debug(fieldName+" = "+toType(ft));
	}
	return ret;
}
/**
* Find the FieldID of a named field. This is NOT case sensistive.
* @param fieldName The name of the field.
* @return The field id, or 0 if not found.
*/
//===================================================================
public int findField(String fieldName)
//===================================================================
{
	FieldSortEntry fe = find(fieldName,fields);
	if (fe != null) return fe.id;
	//......................................................
	// See if it is a reserved field.
	//......................................................
	for (int i = 0; i<reservedFieldNames.length; i++)
		if (reservedFieldNames[i].equalsIgnoreCase(fieldName))
			return reservedFieldIDs[i];
	return 0;
}

/**
* Similar to findField(), find the FieldID of a named field. This is NOT case sensistive.
* @param fieldName The name of the field.
* @return The field id.
* @exception IllegalArgumentException if the field is not found.
*/
//===================================================================
public int getField(String fieldName) throws IllegalArgumentException
//===================================================================
{
	int ret = findField(fieldName);
	if (ret == 0) throw new IllegalArgumentException("Field: "+fieldName+" not found.");
	return ret;
}
/**
* Get an array of fieldIds for an array of field names.
**/
//===================================================================
public int [] findFields(String [] fieldNames)
//===================================================================
{
	int [] ret = new int[fieldNames.length];
	for (int i = 0; i<ret.length; i++) ret[i] = findField(fieldNames[i]);
	return ret;
}
/**
* Get an array of fieldIds for a comma separated list of field names.
**/
//===================================================================
public int [] findFields(String fieldNames)
//===================================================================
{
	return findFields(mString.split(fieldNames,','));
}
//===================================================================
public String getFieldName(int fieldID)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t != null) return t.name;
	int idx = findReservedFieldIndex(fieldID);
	if (idx == -1) return null;
	return reservedFieldNames[idx];
}
//===================================================================
public String getFieldHeader(int fieldID)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t != null) return t.header;
	int idx = findReservedFieldIndex(fieldID);
	if (idx == -1) return null;
	return reservedFieldHeaders[idx];
}
//===================================================================
public boolean setFieldHeader(int fieldID,String newHeader)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t == null) return false;
	t.header = newHeader;
	return true;
}
/**
 * This converts a name with underscores to a header with capital letters and spaces where
 * the underscores were.
 * @param name
 * @return The converted name.
 */
//-------------------------------------------------------------------
private static String nameToHeader(String name)
//-------------------------------------------------------------------
{
	return ewe.ui.InputStack.nameToPrompt(name);
}

//===================================================================
public String getSortName(int sortID)
//===================================================================
{
	FieldSortEntry t = findSort(sortID);
	if (t != null) return t.name;
	return null;
}
//===================================================================
public int [] getSortFields(int sortID)
//===================================================================
{
	FieldSortEntry t = findSort(sortID);
	if (t == null) return null;
	IntArray ia = new IntArray();
	if (t.field1 != 0) ia.append(t.field1);
	if (t.field2 != 0) ia.append(t.field2);
	if (t.field3 != 0) ia.append(t.field3);
	if (t.field4 != 0) ia.append(t.field4);
	return ia.toIntArray();
}
//===================================================================
public int getSortOptions(int sortID)
//===================================================================
{
	FieldSortEntry t = findSort(sortID);
	if (t == null) return 0;
	return t.type;
}
//===================================================================
public int getFieldType(int fieldID)
//===================================================================
{
	FieldSortEntry t = findField(fieldID);
	if (t != null) return t.type;
	int idx = findReservedFieldIndex(fieldID);
	if (idx == -1) return 0;
	return reservedFieldTypes[idx];
}

//-------------------------------------------------------------------
private int [] getIds(Vector v)
//-------------------------------------------------------------------
{
	int sz = v.size();
	int [] ret = new int[sz];
	for (int i = 0; i<sz; i++)
		ret[i] = ((FieldSortEntry)v.get(i)).id;
	return ret;
}
public int setOptions(int optionsToSet, int optionsToClear)
{
	specs.options |= optionsToSet;
	specs.options &= ~optionsToClear;
	return specs.options;
}
//===================================================================
public int [] getFields() {return getIds(fields);}
public int [] getSorts() {return getIds(sorts);}
//===================================================================
//-------------------------------------------------------------------
private FieldSortEntry assignNewID(String name,int typeOptions,Vector dest,int max,boolean isSort)
throws IllegalArgumentException
//-------------------------------------------------------------------
{
	if (find(name,dest) != null) throw new IllegalArgumentException("Field/Sort: "+name+" already used.");
	int start = isSort ? specs.lastSort+1 : specs.lastField+1;
	int toAssign = -1;
	for (int i = start; i<=max; i++){
		if (find(i,dest) != null) continue;
		toAssign = i;
		break;
	}
	if (toAssign == -1)
		for (int i = 1; i < start; i++){
			if (find(i,dest) != null) continue;
			toAssign = i;
			break;
		}
	if (toAssign == -1)
		throw new IllegalArgumentException("Too many "+(isSort ? "sorts." :"fields."));
	FieldSortEntry fe = new FieldSortEntry();
	fe.name = name;
	fe.header = nameToHeader(name);
	fe.id = toAssign;
	fe.type = typeOptions;
	dest.add(fe);
	if (isSort) specs.lastSort = toAssign;
	else specs.lastField = toAssign;
	return fe;
}
/**
* Add a new field. This will return the ID of the new field or throw an exception on failure.
* This will method will fail if:
* 1. An invalid fieldType is specified.
* 2. An already used fieldName is specified.
* 3. There are already the maximum of MAX_ID fields.
**/
//===================================================================
public int addField(String fieldName,int fieldType) throws IllegalArgumentException
//===================================================================
{
	if (fieldType <= 0) throw new IllegalArgumentException();
	FieldSortEntry fe = assignNewID(fieldName,fieldType,fields,MAX_ID,false);
	if (fe == null) return 0;
	return fe.id;
}

//===================================================================
public int findSort(String sortName)
//===================================================================
{
	FieldSortEntry fe = find(sortName,sorts);
	if (fe != null) return fe.id;
	else return 0;
}
/**
* Create a new sort criteria. Returns the ID of the new sort.
**/
//===================================================================
public int addSort(String sortName,int options,int field) throws IllegalArgumentException
//===================================================================
{
	return addSort(sortName,options,field,0,0,0);
}
/**
* Create a new sort criteria allowing you to specify up to four fields.
* Set any unused sort fields to zero. Returns the ID of the new sort.
**/
//===================================================================
public int addSort(String sortName,int options,int field1,int field2,int field3,int field4)
throws IllegalArgumentException
//===================================================================
{
	FieldSortEntry fe = assignNewID(sortName,options,sorts,MAX_ID,true);
	if (fe == null) return 0;
	fe.field1 = field1;
	fe.field2 = field2;
	fe.field3 = field3;
	fe.field4 = field4;
	fe.type1 = getFieldType(field1);
	fe.type2 = getFieldType(field2);
	fe.type3 = getFieldType(field3);
	fe.type4 = getFieldType(field4);
	return fe.id;
}
//-------------------------------------------------------------------
private int[] makeFields(String fieldList)
//-------------------------------------------------------------------
{
	int [] all = findFields(fieldList);
	if (all.length > 4) throw new IllegalArgumentException("Only four fields can be specified for sorting.");
	for (int i = 0; i<all.length; i++)
		if (all[i] == 0) throw new IllegalArgumentException("Illegal field specified.");
	return all;
}
/**
* Create a new sort criteria allowing you to specify up to four fields.
 * @param sortName The name of the sort.
 * @param options Sorting options.
 * @param fieldList A comma separated field list.
 * @return The ID of the new sort.
 * @exception IllegalArgumentException if any of the fields could not be found, or if too many
 * fields are specified.
 */
//===================================================================
public int addSort(String sortName,int options,String fieldList) throws IllegalArgumentException
//===================================================================
{
	int all[] = makeFields(fieldList);
	return addSort(sortName,options,
		all.length < 1 ? 0 : all[0],
		all.length < 2 ? 0 : all[1],
		all.length < 3 ? 0 : all[2],
		all.length < 4 ? 0 : all[3]);
}
/**
* Note that if this DataTable is a live one (i.e. it exists within an open database), then
* calling this method may take time as all of the records in the table will have this
* field removed from them. This does not happen with addField().
**/
//===================================================================
public void removeField(int fieldID)
//===================================================================
{
	if (!deleteId(fieldID,fields)) return;
	//removeFieldIDs(fieldID);
	return;
}
//===================================================================
public void removeSort(int sortID) {deleteId(sortID,sorts);}
//===================================================================

//-------------------------------------------------------------------
protected IndexEntry findIndex(String name,int sortID)
//-------------------------------------------------------------------
{
	for (int i = 0; i<specs.indexes.size(); i++){
		IndexEntry t = (IndexEntry)specs.indexes.get(i);
		if (name != null){
			if (t.name.equals(name)) return t;
		}else {
			if (t.sortID == sortID) return t;
		}
	}
	return null;
}
//-------------------------------------------------------------------
protected IndexEntry findIndex(int sortID,Comparer c)
//-------------------------------------------------------------------
{
	Class cl = c == null ? null : c.getClass();
	for (int i = 0; i<specs.indexes.size(); i++){
		IndexEntry t = (IndexEntry)specs.indexes.get(i);
		if (c != null){
			if (!t.hasCustomComparer()) continue;
			if (cl.equals(t.getCustomComparerClass())) return t;
		}else {
			if (t.sortID == sortID) return t;
		}
	}
	return null;
}

/**
* This tells the Database to keep an index using a particular Comparer class and
* distinct name. If an index of the same name is already being kept
* then this will simply return true immediately. Otherwise the index will be created.<p>
* Creating a new index will usually involve sorting all the entries of the database
* so this may take some time to complete and you can monitor and abort the operation
* using the Handle parameter.
* @param h An optional handle that you can use to monitor and control any index creation
operation.
* @param comparer A valid sort ID for the database.
* @param name A distinct name for the index, which must not be null.
* @return true if the operation completed successfully, false if it was aborted.
* @exception IOException If an IO error occured using the database.
* @exception IllegalArgumentException if the comparer or name is invalid.
*/
//===================================================================
public boolean indexBy(Handle h,Class comparer,String name) throws IOException, IllegalArgumentException
//===================================================================
{
	try{
		DatabaseEntryComparer dc = (DatabaseEntryComparer)comparer.newInstance();
	}catch(Exception e){
		throw new IllegalArgumentException();
	}
	if (name == null){
		name = comparer.getName();
		if (name.indexOf('.') != -1)
			name = mString.rightOf(name,'.');
	}
	for (int i = 0; i<specs.indexes.size(); i++){
		IndexEntry t = (IndexEntry)specs.indexes.get(i);
		if (t.name.equals(name)){
			if (t.hasCustomComparer())
				if (comparer.equals(t.getCustomComparerClass())) return true;
			specs.indexes.remove(t);
			break;
		}
	}
	IndexEntry t = new IndexEntry();
	t.name = name;
	t.comparerClassName = comparer.getName();
	specs.indexes.add(t);
	createIndex(h,t);
	return true;
}
//===================================================================
public boolean indexBy(Handle h,int sortID,String name) throws IllegalArgumentException, IOException
//===================================================================
{
	String sname = getSortName(sortID);
	if (sname == null) throw new IllegalArgumentException();
	if (name == null) name = sname;
	for (int i = 0; i<specs.indexes.size(); i++){
		IndexEntry t = (IndexEntry)specs.indexes.get(i);
		if (t.name.equals(name)){
			if (sortID == t.sortID) return true;
			specs.indexes.remove(t);
			break;
		}
	}
	IndexEntry t = new IndexEntry();
	t.name = name;
	t.sortID = sortID;
	specs.indexes.add(t);
	createIndex(h,t);
	return true;
}
//===================================================================
public void indexBy(int sortID) throws IOException
//===================================================================
{
	indexBy(null,sortID,null);
}
//===================================================================
public boolean isIndexedBy(int sortID)
//===================================================================
{
	return findIndex(null,sortID) != null;
}
//===================================================================
public IndexEntry [] getIndexes()
//===================================================================
{
	IndexEntry [] ret = new IndexEntry[specs.indexes.size()];
	for (int i = 0; i<specs.indexes.size(); i++){
		IndexEntry t = (IndexEntry)specs.indexes.get(i);
		ret[i] = t.getCopy();
	}
	return ret;
}
//===================================================================
public int getIndexSort(String indexName)
//===================================================================
{
	IndexEntry tg = findIndex(indexName,0);
	if (tg == null) return 0;
	return tg.sortID;
}
//-------------------------------------------------------------------
protected DatabaseIndex createIndex(Handle h,String name) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	//
	// See if it actually exists.
	//
	IndexEntry ie = findIndex(name,0);
	if (ie == null) throw new IllegalArgumentException();
	return createIndex(h,ie);
}
//-------------------------------------------------------------------
protected boolean saveIndex(Handle h,DatabaseIndex di) throws IOException
//-------------------------------------------------------------------
{
	FoundEntriesObject f = (FoundEntriesObject)di;
	Handle h2 = new Handle();
	if (!f.writeTo(null,h2)) return false;
	long did = h2.returnValue instanceof ewe.sys.Long ? ((ewe.sys.Long)h2.returnValue).value : 0;
	if (did > Integer.MAX_VALUE) did = 0;
	MetaData mt = getNewMetaData("Index:"+di.getName());
	//ewe.sys.Vm.debug("Going to save index: "+di.getName()+" of size: "+f.size()+", with: "+did+" bytes.");
	mt.openForReplacing((int)did);
	if (!f.writeTo(new OutputStream(mt),h)) return false;
	mt.close();
	return true;
}
//-------------------------------------------------------------------
protected DatabaseIndex createIndex(Handle h,IndexEntry ie) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	FoundEntries fe = getFoundEntriesForIndex(h,ie);
	if (fe == null) return null;
	FoundEntriesObject di = (FoundEntriesObject)registerNewFoundEntries(makeNewIndex(ie.name));
	di.indexFromFoundEntries(fe);
	if (!saveIndex(h,(DatabaseIndex)di)) return null;
	return registerIndex((DatabaseIndex)di,ie.name);
}
//-------------------------------------------------------------------
protected boolean doReIndex(Handle h) throws IOException
//-------------------------------------------------------------------
{
	DatabaseSpecs specs = getSpecs();
	for (int i = 0; i<specs.indexes.size(); i++){
		IndexEntry t = (IndexEntry)specs.indexes.get(i);
		if (createIndex(h,t) == null) return false;
	}
	markForReIndex(false);
	return true;
}
//-------------------------------------------------------------------
protected DatabaseIndex openIndex(Handle h,IndexEntry ie) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	FoundEntriesObject di = (FoundEntriesObject)findOpenIndex(ie.name);
	if (di != null) return (DatabaseIndex)di;
	return doOpenIndex(h,ie);
}
//-------------------------------------------------------------------
protected DatabaseIndex doOpenIndex(Handle h,String name) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	//
	// See if it actually exists.
	//
	IndexEntry ie = findIndex(name,0);
	if (ie == null) throw new IllegalArgumentException();
	return doOpenIndex(h,ie);
}
//-------------------------------------------------------------------
protected DatabaseIndex doOpenIndex(Handle h,IndexEntry ie) throws IllegalArgumentException, IOException
//-------------------------------------------------------------------
{
	//
	// It exists so try to read it.
	//
	Comparer cc = ie.getCustomComparerInstance(this);
	if (cc == null && ie.sortID == 0) throw new IllegalArgumentException();
	String nm = "Index:"+ie.name;
	MetaData mt = getNewMetaData(nm);
	//try{
		int size = mt.openForReading();
		//
		// If not saved will have to create it.
		//
		if (size == -1){
			return createIndex(h,ie);
		}
		//
		// Read it.
		//
		FoundEntriesObject di = (FoundEntriesObject)makeNewIndex(ie.name);
		if (!di.readFrom(new InputStream(mt),h,size)) return null;
		if (!di.needCompact) di.needCompact = mt.numberOfFragments > 4;// && size > 10000;
		//if (di.needCompact) ewe.sys.Vm.debug(ie.name+" should compact: "+di.needCompact+", "+mt.numberOfFragments);
		di.setSort(ie.sortID,cc);
		mt.close();
		registerNewFoundEntries(di);
		//
		if (isOpenForReadWrite() && !ewe.sys.Vm.isMobile()){
			boolean shouldCompact = ((DatabaseIndex)di).needsCompacting();
			if (shouldCompact)
				((DatabaseIndex)di).compact(h);
		}
		//
		return registerIndex((DatabaseIndex)di,ie.name);
	/*}catch(Exception e){
		e.printStackTrace();
		return null;
	}*/
}
//-------------------------------------------------------------------
protected void recordIndexChange(FoundEntriesObject obj,byte[] toRecord,int offset,int length)
//-------------------------------------------------------------------
{

}
private DatabaseSpecs specs;

//-------------------------------------------------------------------
private DatabaseSpecs getSpecs() throws IOException
//-------------------------------------------------------------------
{
	if (specs != null) return specs;
	specs = new DatabaseSpecs();
	byte [] got = getMetaData("_DatabaseSpecs_");
	if (got != null){
		/*
		String data = Utils.decodeJavaUtf8String(got,0,got.length);
		//ewe.sys.Vm.debug(data);
		Utils.textDecode(specs,data);
		*/
		try{
			specs = (DatabaseSpecs)ByteEncoder.decodeObject(got,0,got.length,specs);
		}catch(Exception e){
			return null;
		}
	}
	return specs;
}
//-------------------------------------------------------------------
private void saveSpecs() throws IOException
//-------------------------------------------------------------------
{
	if (specs == null) return;
	ByteArray ba = new ByteArray();
	ByteEncoder.encodeObject(ba,specs);
	//String toSave = Utils.textEncode(specs);
	overwriteMetaData("_DatabaseSpecs_",ba.toBytes());//Utils.encodeJavaUtf8String(toSave));
	//ewe.sys.Vm.debug(toSave);
	//
	// FIXME - update the open indexes to reflect the new changes.
	//
}

//-------------------------------------------------------------------
protected long getNewOID() throws IOException
//-------------------------------------------------------------------
{
	return DatabaseUtils.getNewOID();
}
//===================================================================
public int getIdentifier() throws IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) return 0;
	return specs.myOID;
}
//===================================================================
public void setSynchronizedTime(int remoteDatabaseID,ewe.sys.Time syncTime) throws ewe.io.IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) return;
	specs.setSynchronizedTime(remoteDatabaseID,syncTime);
	saveSpecs();
}
//===================================================================
public ewe.sys.Time getSynchronizedTime(int remoteDatabaseID) throws ewe.io.IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) return null;
	return specs.getSynchronizedTime(remoteDatabaseID);
}
/*
//-------------------------------------------------------------------
private String outputFieldSort(Vector v,boolean sorts)
//-------------------------------------------------------------------
{
	String out = "";
	for (Iterator it = v.iterator(); it.hasNext();){
		FieldSortEntry fe = (FieldSortEntry)it.next();
		out += (fe.name+"¬"+fe.header)+"|"+fe.id+"|"+fe.type+"|";
		out += fe.field1+"|"+fe.field2+"|"+fe.field3+"|"+fe.field4+"|";
	}
	return out;
}
//-------------------------------------------------------------------
private void inputFieldSort(Vector v,boolean sorts,String str)
//-------------------------------------------------------------------
{
	String [] fds = mString.split(str,'|');
	int add = 3;
	if (sorts || true) add += 4;
	v.clear();
	for (int i = 0; i<fds.length+1-add; i += add){
		FieldSortEntry fse = new FieldSortEntry();
		fse.name = mString.leftOf(fds[i],'¬');
		fse.header = mString.rightOf(fds[i],'¬');
		if (fse.header.length() == 0) fse.header = fse.name;
		fse.id = ewe.sys.Convert.toInt(fds[i+1]);
		fse.type = ewe.sys.Convert.toInt(fds[i+2]);
		if (sorts){
			fse.field1 = ewe.sys.Convert.toInt(fds[i+3]);
			fse.field2 = ewe.sys.Convert.toInt(fds[i+4]);
			fse.field3 = ewe.sys.Convert.toInt(fds[i+5]);
			fse.field4 = ewe.sys.Convert.toInt(fds[i+6]);
			fse.type1 = getFieldType(fse.field1);
			fse.type2 = getFieldType(fse.field2);
			fse.type3 = getFieldType(fse.field3);
			fse.type4 = getFieldType(fse.field4);
		}
		if (fse.id == 0) continue;
		v.add(fse);
	}
}
*/
//-------------------------------------------------------------------
protected void updateTypes(FieldSortEntry fse)
//-------------------------------------------------------------------
{
	fse.type1 = getFieldType(fse.field1);
	fse.type2 = getFieldType(fse.field2);
	fse.type3 = getFieldType(fse.field3);
	fse.type4 = getFieldType(fse.field4);
}
//===================================================================
public void save() throws IOException
//===================================================================
{
	specs = getSpecs();
	if (specs == null) throw new IOException("Can't save data.");
	specs.fields = fields;
	specs.sorts = sorts;
	specs.objectClass = objectClass == null ? null : objectClass.getClassName();
	/*
	TextEncoder te = new TextEncoder();
	te.addValue("Fields",outputFieldSort(fields,false));
	te.addValue("Sorts",outputFieldSort(sorts,true));
	if (objectClass != null) te.addValue("ObjectClass",objectClass.getClassName());
	specs.fieldsAndSorts = te.toString();
	*/
	saveSpecs();
}
/**
* Create and return a new instance of the object class assigned to the
* database.
* @return a new instance of the object class assigned to the
* database.
* @exception IllegalStateException if no object class is specified or the
* object could not be instantiated.
*/
//===================================================================
public Object getNewDataObject() throws IllegalStateException
//===================================================================
{
	if (objectClass == null) throw new IllegalStateException();
	Object ret = objectClass.newInstance();
	if (ret == null) throw new IllegalStateException();
	return ret;
}
/**
 * Return if the specified object is an instance of the data transfer object
 * assigned in setObjectClass();
 * @param data the object to check.
 * @return true if the specified object is an instance of the data transfer object
 * assigned in setObjectClass();
 */
//===================================================================
public boolean isInstanceOfDataObject(Object data)
//===================================================================
{
	if (objectClass == null) return false;
	return objectClass.isInstance(data);
}
//===================================================================
public void setDataValidator(DataValidator validator)
//===================================================================
{
	dataValidator = validator;
	specs.dataValidatorClass = validator == null ? "" : Reflect.getForObject(validator).getClassName();
}
//===================================================================
public void modifyField(int fieldID, int modifier, Object modifierData) throws IllegalArgumentException, IOException
//===================================================================
{
	for (int i = 0; i<specs.modifiers.size(); i++){
		FieldModifier old = (FieldModifier)specs.modifiers.get(i);
		if (old.fieldID == fieldID) {
			specs.modifiers.del(i);
			break;
		}
	}
	FieldModifier fm = modifier == 0 ? null : new FieldModifier();
	if (fm != null) {
		fm.set(this,fieldID,modifier,modifierData);
		specs.modifiers.add(fm);
	}
	fm.decodeAfterReading(this);
	if (openModifiers == null) openModifiers = new Vector();
	for (int i = 0; i<openModifiers.size(); i++){
		FieldModifier m = (FieldModifier)openModifiers.get(i);
		if (m.fieldID == fieldID){
			openModifiers.del(i);
			break;
		}
	}
	if (fm != null) openModifiers.add(fm);
	if (openModifiers.size() == 0) openModifiers = null;
}
/**
* This should be called when the Database is opened.
**/
//-------------------------------------------------------------------
protected void load(String mode, boolean ignoreInconsistentState) throws IOException, InconsistentDatabaseStateException
//-------------------------------------------------------------------
{
	openMode = mode;
	specs = getSpecs();
	if (specs.encryptorClass != null) encryptorNotSet = true;
	try{
		if (specs.dataValidatorClass == null) specs.dataValidatorClass = "";
		Reflect r = specs.dataValidatorClass.length() == 0 ? null : Reflect.getForName(specs.dataValidatorClass);
		dataValidator = r == null ? null : (DataValidator)r.newInstance();
	}catch(Exception e){}
	fields = specs.fields;
	sorts = specs.sorts;
	if (fields == null) fields = new Vector();
	if (sorts == null) sorts = new Vector();
	for (int i = 0; i<sorts.size(); i++)
		updateTypes((FieldSortEntry)sorts.get(i));
	/*
	TextDecoder td = new TextDecoder(specs.fieldsAndSorts);
	inputFieldSort(fields,false,td.getValue("Fields"));
	inputFieldSort(sorts,true,td.getValue("Sorts"));
	*/
	fieldTransfers = null;
	objectClass = null;
	//
	String oc = specs.objectClass;//td.getValue("ObjectClass");
	if (oc != null){
		if ((objectClass = Reflect.getForName(oc)) != null){
			Object got = objectClass.newInstance();
			fieldTransfers = new Vector();
			int [] all = getFields();
			DatabaseEntry ded = getNewData();
			for (int i = 0; i<all.length; i++){
				try{
					String fn = getFieldName(all[i]);
					FieldTransfer ft = getFieldTransfer(fn,getFieldType(all[i]),got,ded);
					if (toType(ft,got) == getFieldType(all[i])){
						fieldTransfers.add(ft);
					}
				}catch(IllegalArgumentException e){
				}
			}
		}
	}
	//
	// See if the file needs re-indexing.
	//
	indexFlag = new MetaFlag("_IndexFlags_",this);
	int value = indexFlag.getValue();
	//
	if ((value & INDEX_FLAG_DO_REINDEX) != 0){
		if ((specs.options & OPTION_ERROR_ON_NEED_REINDEX) != 0) throw new IOException("Re-index required - database corrupted by incomplete operation.");
		if (!mode.equals("r")) doReIndex(null);
		else if (!ignoreInconsistentState){
			try{
				close();
			}catch(Exception e){}
			throw new InconsistentDatabaseStateException();
		}
	}
	//
	// If we are open for writing, then entries may be added, in which case
	// indexes will need to be kept up to date.
	//
	if (mode.equals("rw")){
		for (int i = 0; i<specs.indexes.size(); i++){
			IndexEntry t = (IndexEntry)specs.indexes.get(i);
			doOpenIndex(null,t);
		}
	}
	//
	if (specs.modifiers.size() != 0){
		openModifiers = new Vector();
		for (int i = 0; i<specs.modifiers.size(); i++){
			FieldModifier m = (FieldModifier)specs.modifiers.get(i);
			if (m.decodeAfterReading(this)) openModifiers.add(m);
		}
		if (openModifiers.size() == 0) openModifiers = null;
	}
}
//-------------------------------------------------------------------
protected boolean hasField(int fieldID)
//-------------------------------------------------------------------
{
	return findField(fieldID) != null;
}

private Vector des = new Vector(), founds = new Vector();
private IntArray indexes = new IntArray();
/*
public Object traceFounds(String msg)
{
	//new RuntimeException().printStackTrace();
	Object ret = null;
	if (hasFoundEntries())
		for (Iterator it = getMyFoundEntries(); it.hasNext();){
			FoundEntriesObject fe = (FoundEntriesObject)it.next();
			Vm.debug("Founds: "+
					Integer.toHexString(System.identityHashCode(fe))+"->"+
					Integer.toHexString(System.identityHashCode(fe.ids)));
			if (ret == null) ret = fe.ids;
			if (!fe.validateEntries()){
				DatabaseManager.logError(msg+" - "+fe+"\n");
				//addTrace(msg+" - "+fe+"\n");
				throw new RuntimeException("Messed up index: "+Integer.toHexString(System.identityHashCode(fe.ids)));
			}
		}
	return ret;
	//DatabaseManager.logError("OK at: "+msg+"\n");
}
*/
/**
This is done just before a DatabaseEntry is about to be modified in some way. It collects
all the FoundEntries that refer to this entry and gets them ready for the change.

After this method returns the actual operation on the entry is done, and then
one of the updateDeleted() or updateStored() methods are called.
**/
//===================================================================
void checkRefs(DatabaseEntryObject entry) throws IOException
//===================================================================
{
	des.clear();
	founds.clear();
	indexes.clear();
	if (hasDatabaseEntries()){
		for (Iterator it = getMyDatabaseEntries(); it.hasNext();){
			DatabaseEntry de = (DatabaseEntry)it.next();
			if (de != entry && de.isPointingTo(entry)) des.add(de);
		}
	}
	boolean saved = entry.isSaved();
	if (hasFoundEntries()){
		for (Iterator it = getMyFoundEntries(); it.hasNext();){
			FoundEntries fe = (FoundEntries)it.next();
			if (entry.modifyingInside == fe) continue;
			if (!saved && fe.isAllInclusive()){
				founds.add(fe);
				indexes.add(-1);
			}else{
				int idx = fe.indexOf(entry);
				if (idx != -1){
					founds.add(fe);
					indexes.add(idx);
				}
			}
		}
	}
	markForReIndex(true);
	//
	// After this method is called, the actual entry operation is done
	// and then, the indexes will be saved, and then the
	//
}
//-------------------------------------------------------------------
void updateStored(DatabaseEntryObject entry,boolean isANewOne) throws IOException
//-------------------------------------------------------------------
{
	try{
	for (int i = 0; i<des.size(); i++)
		((DatabaseEntry)des.get(i)).pointTo(entry);
	}catch(Throwable t){
		t.printStackTrace();
	}
	for (int i = 0; i<founds.size(); i++){
		FoundEntriesObject fe = (FoundEntriesObject)founds.get(i);
		fe.doUpdate(indexes.data[i],entry);
	}
	change();
	dispatchEvent(isANewOne ? DatabaseEvent.ENTRY_ADDED : DatabaseEvent.ENTRY_CHANGED);
}
//-------------------------------------------------------------------
void updateDeleted(DatabaseEntryObject entry,boolean isADel) throws IOException
//-------------------------------------------------------------------
{
	for (int i = 0; i<des.size(); i++)
		((DatabaseEntry)des.get(i)).pointTo(null);
	for (int i = 0; i<founds.size(); i++){
		FoundEntriesObject fe = (FoundEntriesObject)founds.get(i);
		fe.deleted(indexes.data[i]);
	}
	change();
	dispatchEvent(isADel ? DatabaseEvent.DELETED_ENTRY_ERASED : DatabaseEvent.ENTRY_DELETED);
}
DatabaseEntry old;
//-------------------------------------------------------------------
void setupOld(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	if (old == null) old = getNewData();
	if (entry.isSaved()){
		old.pointTo(entry);
		old.revert();
	}
}

 Lock traceLock = new Lock();
 StringBuffer traceBuffer = new StringBuffer();

 void addTrace(String trace)
{
	traceLock.synchronize(); try{
		if (traceBuffer == null) return;
		traceBuffer.append(trace);
	}finally{
		traceLock.release();
	}
}
 public String getName()
 {
 	File f = stream instanceof RandomAccessDatabaseStream ? ((RandomAccessDatabaseStream)stream).myFile : null;
 	if (f == null) return "unnamed";
 	else return f.getFileExt();
 }

 void outputTrace()
{
	traceLock.synchronize(); try{
		if (traceBuffer != null) DatabaseManager.logError("Database: "+getName()+"\n"+traceBuffer.toString());
		traceBuffer = null;
	}finally{
		traceLock.release();
	}
}
 void startTrace()
{
	traceLock.synchronize(); try{
		traceBuffer = new StringBuffer();
	}finally{
		traceLock.release();
	}
}
 void clearTrace()
{
	traceLock.synchronize(); try{
		traceBuffer = null;
	}finally{
		traceLock.release();
	}
}

//-------------------------------------------------------------------
void store(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		if (openMode.equals("a")){
			doStore(entry);
			return;
		}
		boolean isANewOne = !entry.isSaved();
		if (openModifiers != null || dataValidator != null) setupOld(entry);
		if (openModifiers != null){
			for (int i = 0; i<openModifiers.size(); i++){
				FieldModifier fm = (FieldModifier)openModifiers.get(i);
				fm.modify(entry,isANewOne ? null : old);
			}
		}
		//Debug.startTiming("store() - validateEntry");
		startTrace();
		try{
			if (dataValidator != null)
				dataValidator.validateEntry(this,entry,isANewOne ? null : old);
			//traceFounds("Before checkRefs()");
			checkRefs(entry);
			//traceFounds("Before doStore()");
			doStore(entry);
			//traceFounds("Before updateStored()");
			updateStored(entry,isANewOne);
			//traceFounds("Before markForReIndex(false)");
			//Debug.startTiming("store() - markForReIndex()");
			markForReIndex(false);
			//traceFounds("Before after markForReIndex(false)");
			//Debug.endTiming();
		}catch(RuntimeException e){
			addTrace(Vm.getStackTrace(e)+"\n");
			outputTrace();
			throw e;
		}
		clearTrace();
	}finally{lock.release();}
}
//-------------------------------------------------------------------
void markAsDeleted(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		if (openModifiers != null || dataValidator != null) setupOld(entry);
		if (openModifiers != null){
			for (int i = 0; i<openModifiers.size(); i++){
				FieldModifier fm = (FieldModifier)openModifiers.get(i);
				fm.modify(null,old);
			}
		}
		if (dataValidator != null)
			dataValidator.validateEntry(this,null,old);
		checkRefs(entry);
		doMarkAsDeleted(entry);
		updateDeleted(entry,false);
		markForReIndex(false);
	}finally{lock.release();}
}
//-------------------------------------------------------------------
void erase(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	boolean isDel = entry.isADeletedEntry();
	lock.synchronize(); try{
		if (!isDel && (openModifiers != null || dataValidator != null)){
			setupOld(entry);
			if (openModifiers != null){
				for (int i = 0; i<openModifiers.size(); i++){
					FieldModifier fm = (FieldModifier)openModifiers.get(i);
					fm.modify(null,old);
				}
			}
			if (dataValidator != null)
				dataValidator.validateEntry(this,null,old);
		}
		checkRefs(entry);
		doErase(entry);
		updateDeleted(entry,isDel);
		markForReIndex(false);
	}finally{lock.release();}
}
//-------------------------------------------------------------------
void load(DatabaseEntryObject entry) throws IOException
//-------------------------------------------------------------------
{
	lock.synchronize(); try{
		doLoad(entry);
			if (openModifiers != null){
				for (int i = 0; i<openModifiers.size(); i++){
					FieldModifier fm = (FieldModifier)openModifiers.get(i);
					fm.modify(entry,entry);
				}
			}
		if (dataValidator != null)
			dataValidator.validateEntry(this,entry,entry);
	}finally{lock.release();}
}
//-------------------------------------------------------------------
/**
* Return true if the entry is a new entry, false if not.
**/
protected abstract boolean doStore(DatabaseEntryObject entry) throws IOException;
protected abstract void doLoad(DatabaseEntryObject entry) throws IOException, IllegalStateException;
protected abstract void doMarkAsDeleted(DatabaseEntryObject entry) throws IOException;
protected abstract void doErase(DatabaseEntryObject entry) throws IOException;
protected abstract void removeFieldIDs(int fieldID) throws IOException;
//-------------------------------------------------------------------

//===================================================================
public Time getTimeOfDeletion(long oid,Time dest) throws IOException
//===================================================================
{
	lock.synchronize();try{
		buffer = getDeletedEntry(oid,buffer);
		if (buffer == null) return null;
		Time ret = (Time)buffer.getFieldValue(MODIFIED_FIELD,DATE_TIME,dest);
		return ret;
	}finally{lock.release();}
}
/**
* Get the OIDs of all entries deleted after the specified time (but not AT the
* specified time).
**/
//===================================================================
public long [] getDeletedSince(Time t) throws IOException
//===================================================================
{
	Vector v = new Vector();
	long [] all = getDeletedEntries();
	for (int i = 0; i<all.length; i++){
		Time td = getTimeOfDeletion(all[i],now);
		if (td == null) continue;
		if (t != null)
			if (t.compareTo(td) >= 0) continue;
		v.add(new ewe.sys.Long().set(all[i]));
	}
	long [] ret = new long[v.size()];
	for (int i = 0; i<ret.length; i++)
		ret[i] = ((ewe.sys.Long)v.get(i)).value;
	return ret;
}
//===================================================================
public void addSpecialField(int id) throws IllegalArgumentException
//===================================================================
{
	if (find(id,fields) != null) return;
	int idx = findReservedFieldIndex(id);
	if (idx == -1) throw new IllegalArgumentException();
	if (findField(id) != null) return;
	FieldSortEntry fe = new FieldSortEntry();
	fe.id = id; fe.type = reservedFieldTypes[idx];
	fe.name = reservedFieldNames[idx];
	fe.header = nameToHeader(fe.name);
	fields.add(fe);
}
	//-------------------------------------------------------------------
 int addSpecialSort(String sortName,int options,int field1,int field2,int field3,int field4)
	//-------------------------------------------------------------------
{
	int id = findSort(sortName);
	if (id == 0) id = addSort(sortName,options,field1,field2,field3,field4);
	return id;
}
//===================================================================
public boolean enableSynchronization(Handle h,int syncOptions) throws ewe.io.IOException
//===================================================================
{
	addSpecialField(OID_FIELD);
	int sortID = addSpecialSort(OidSortName,0,OID_FIELD,0,0,0);
	if (!indexBy(h,sortID,null)) return false;
	addSpecialField(FLAGS_FIELD);
	sortID = addSpecialSort(SyncSortName,0,FLAGS_FIELD,OID_FIELD,0,0);
	if (!indexBy(h,sortID,null)) return false;

	if ((syncOptions & SYNC_STORE_CREATION_DATE) != 0){
		addSpecialField(CREATED_FIELD);
		addSpecialSort(CreatedSortName,0,CREATED_FIELD,OID_FIELD,0,0);
	}

	if ((syncOptions & SYNC_STORE_MODIFICATION_DATE) != 0) {
		addSpecialField(MODIFIED_FIELD);
		addSpecialSort(ModifiedSortName,0,MODIFIED_FIELD,OID_FIELD,0,0);
	}

	if ((syncOptions & SYNC_STORE_MODIFIED_BY) != 0) {
		addSpecialField(MODIFIED_BY_FIELD);
		addSpecialSort(ModifiedBySortName,0,MODIFIED_BY_FIELD,OID_FIELD,0,0);
	}

	save();
	return true;
}
//===================================================================
public void enableSynchronization(int syncOptions) throws ewe.io.IOException
//===================================================================
{
	enableSynchronization(null,syncOptions);
}
//-------------------------------------------------------------------
Iterator prepareFieldTransfer(Object data)
//-------------------------------------------------------------------
{
	boolean badType = objectClass == null ? true : !objectClass.isInstance(data);
	if (badType){
		throw new IllegalStateException("The objectClass for the DataTable has not been set or is incompatible with the data object.");
	}
	if (fieldTransfers == null) return new ObjectIterator(null);
	return fieldTransfers.iterator();
}
//-------------------------------------------------------------------
void getSetFields(DatabaseEntry ded,Object data,boolean isGet)
//-------------------------------------------------------------------
{
	for (Iterator i = prepareFieldTransfer(data); i.hasNext();){
		FieldTransfer ft = (FieldTransfer)i.next();
		ft.dataInterface = ded;
		try{
			ft.transfer(data,isGet ? ft.TO_OBJECT : ft.FROM_OBJECT);
		}catch(Exception e){
			String s = ewe.sys.Vm.getStackTrace(e,4);
		}
	}
}
/**
 * Sets the fields in the DatabaseEntry from the fields in the data object - which must not be null.
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
 */
//-------------------------------------------------------------------
protected void setData(DatabaseEntry ded,Object data)
//-------------------------------------------------------------------
{
	getSetFields(ded,data,false);
}
/**
 * Gets the fields from the DatabaseEntry to the fields in the data object - which must not be null.
 * @param ded The DatabaseEntry to set.
 * @param data The data object, which must be of the type objectClass or an exception will be thrown.
 */
//-------------------------------------------------------------------
protected Object getData(DatabaseEntry ded,Object data)
//-------------------------------------------------------------------
{
	if (data == null && objectClass != null) data = objectClass.newInstance();
	if (data == null || objectClass == null) throw new IllegalStateException();
	getSetFields(ded,data,true);
	return data;
}

/*
//===================================================================
public Object getFieldData(int id,int fieldID,Object dest) throws IOException
//===================================================================
{
	DatabaseEntry ded = loadEntry(id,null);
	if (ded == null) return null;
	return ded.getFieldValue(fieldID,dest);
}
*/
//===================================================================
public Iterator entries(Object searchData,Comparer comparer) throws IOException
//===================================================================
{
	return entries(new ComparerObjectFinder(comparer,searchData));
}
//===================================================================
public Iterator entries(final ObjectFinder finder) throws IOException
//===================================================================
{
	final Iterator got = entries();
	return new IteratorEnumerator(){
		Object nextToGo = null;
		RuntimeException error;
		{
			updateNext();
		}
		void updateNext(){
			while(got.hasNext()){
				try{
					Object o = got.next();
					if (!finder.lookingFor(o)) continue;
					nextToGo = o;
					break;
				}catch(RuntimeException e){
					error = e;
					break;
				}
			}
		}

		public boolean hasNext(){
			return nextToGo != null || error != null;
		}

		public Object next(){
			if (nextToGo != null) {
				Object next = nextToGo;
				if (error == null) updateNext();
				return next;
			}
			if (error != null) throw error;
			return null;
		}

	};
}
/**
* Override this to do an estimate on the number of entries if you can't tell the
* exact number of entries.
**/
//-------------------------------------------------------------------
protected long doEstimateEntriesCount() throws IOException
//-------------------------------------------------------------------
{
	return -1;
}

//===================================================================
public long estimateEntriesCount() throws IOException
//===================================================================
{
	long count = getEntriesCount();
	if (count != -1) return count;
	return doEstimateEntriesCount();
}
//===================================================================
public Handle countEntries()
//===================================================================
{
	try{
		long count = getEntriesCount();
		if (count != -1){
			Handle h = new Handle();
			h.returnValue = new Long().set(count);
			h.set(h.Succeeded);
			return h;
		}
		final long total = doEstimateEntriesCount();
		return new ewe.sys.TaskObject(){
			protected void doRun(){
				handle.resetTime("Counting");
				long count = 0;
				try{
					for (Iterator it = entries(); it.hasNext();){
						it.next();
						count++;
						if (count % 10 == 0)
							handle.setProgress(total <= 0 || count > total ? -1 : (float)((double)count/total));
					}
					handle.returnValue = new Long().set(count);
					handle.set(handle.Succeeded);
				}catch(Exception e){
					handle.fail(e);
				}
			}
		}.startTask();
	}catch(Exception e){
		Handle h = new Handle();
		h.failed(e);
		return h;
	}
}
//-------------------------------------------------------------------
protected abstract FoundEntries getAllFoundEntries(Handle h) throws IOException;
//-------------------------------------------------------------------

protected IntArray intArray;
/**
* Override this to locate the found entries in your own way. By default this will
* first create a FoundEntries containing ALL the entries and then sort them,
* and then either search or filter it and return the subset.
**/
//-------------------------------------------------------------------
protected FoundEntries getFoundEntries(Handle h,int sortId,EntrySelector selector,ObjectFinder finder,Comparer c,boolean useIndexes) throws IOException
//-------------------------------------------------------------------
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	lock.synchronize(); try{
		FoundEntriesObject all = null;
		if (useIndexes && (sortId != 0 || c != null)){
			IndexEntry t = findIndex(sortId,c);
			if (t != null){
				DatabaseIndex di = openIndex(h,t);
				if (di != null) all = (FoundEntriesObject)di.getEntries();
			}
		}
		if (all == null){
			all = (FoundEntriesObject)getAllFoundEntries(h);
			if (all == null) return null;
			if (sortId != 0 || c != null) {
				if (!all.sortMe(h,c,sortId)) return null;
			}
		}
		if (selector != null || finder != null){
			EntriesView view = all.getEmptyView();
			view = finder != null ? view.search(h,finder) : view.search(h,selector);
			if (view == null) return null;
			all = (FoundEntriesObject)all.getSubSet(view);
		}
		return all;
	}finally{lock.release();}
}
//-------------------------------------------------------------------
protected FoundEntries getFoundEntries(Handle h,int sortId,EntrySelector selector,ObjectFinder finder) throws IOException
//-------------------------------------------------------------------
{
	return getFoundEntries(h,sortId,selector,finder,null,true);
}
//-------------------------------------------------------------------
protected FoundEntries getFoundEntriesForIndex(Handle h,IndexEntry ie) throws IOException
//-------------------------------------------------------------------
{
	return getFoundEntries(h,ie.sortID,null,null,ie.getCustomComparerInstance(this),false);
}
//-------------------------------------------------------------------
Handle getFoundEntries(final int sortID,final EntrySelector selector,final ObjectFinder finder,final Comparer comparer)
//-------------------------------------------------------------------
{
	return new ewe.sys.TaskObject(){
		protected void doRun(){
			try{
				FoundEntries fe = getFoundEntries(handle,sortID,selector,finder,comparer,true);
				if (fe == null) {
					handle.set(Handle.Aborted);
					return;
				}
				handle.returnValue = fe;
				handle.set(Handle.Succeeded);
			}catch(Exception e){
				handle.fail(e);
			}
		}
	}.startTask();
}

//===================================================================
public FoundEntries getFoundEntries(Handle h,Comparer comparer) throws IOException
//===================================================================
{
	return getFoundEntries(h,0,null,null,comparer,false);
}
/*
//===================================================================
public Handle getFoundEntries()
//===================================================================
{
	return getFoundEntries(0,null,null,null);
}
*/
//===================================================================
public Handle getFoundEntries(int sortID)
//===================================================================
{
	return getFoundEntries(sortID,null,null,null);
}
//===================================================================
public Handle getFoundEntries(int sortID,EntrySelector selector)
//===================================================================
{
	return getFoundEntries(sortID,selector,null,null);
}
//===================================================================
public Handle getFoundEntries(int sortID,Object primarySearchFields)
//===================================================================
{
	return getFoundEntries(sortID,new EntrySelector(this,primarySearchFields,sortID,true),null,null);
}
//===================================================================
public Handle getFoundEntries(int sortID,ObjectFinder finder)
//===================================================================
{
	return getFoundEntries(sortID,null,finder,null);
}
//===================================================================
public Handle getFoundEntries(Comparer comparer)
//===================================================================
{
	return getFoundEntries(0,null,null,comparer);
}

//===================================================================
public FoundEntries getFoundEntries(Handle h,String indexName) throws IOException, IllegalArgumentException
//===================================================================
{
	if (encryptorNotSet) throw new IllegalStateException("Encryptor not set.");
	IndexEntry ie = findIndex(indexName,0);
	if (ie == null) throw new IllegalArgumentException();
	if (!ie.hasCustomComparer()) return getFoundEntries(h,ie.sortID,null,null,null,true);
	else return getFoundEntries(h,0,null,null,ie.getCustomComparerInstance(this),true);
}
//===================================================================
public Handle getFoundEntries(String indexName)
//===================================================================
{
	IndexEntry ie = findIndex(indexName,0);
	if (ie == null) {
		Handle h = new Handle(Handle.Failed,null);
		h.fail(new IllegalArgumentException());
		return h;
	}
	if (!ie.hasCustomComparer()) return getFoundEntries(ie.sortID,null,null,null);
	else return getFoundEntries(0,null,null,ie.getCustomComparerInstance(this));
}
/*
//===================================================================
public Handle getFoundEntries(int sortID,Object searchData,Comparer comparer)
//===================================================================
{
	return getFoundEntries(sortID,searchData,comparer,null);
}
*/
//===================================================================
public FoundEntries getEntries() throws IOException
//===================================================================
{
	return getFoundEntries(null,0,null,null);
}
//===================================================================
public FoundEntries getEntries(int sortID) throws IOException
//===================================================================
{
	return getFoundEntries(null,sortID,null,null);
}
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID,ObjectFinder finder) throws IOException
//===================================================================
{
	return getFoundEntries(h,sortID,null,finder);
}
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID,EntrySelector selector) throws IOException
//===================================================================
{
	return getFoundEntries(h,sortID,selector,null);
}
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID) throws IOException
//===================================================================
{
	return getFoundEntries(h,sortID,null,null);
}
//===================================================================
public FoundEntries getFoundEntries(Handle h,int sortID,Object primarySearchFields) throws IOException
//===================================================================
{
	return getFoundEntries(h,sortID,new EntrySelector(this,primarySearchFields,sortID,true),null);
}
//===================================================================
public long getCurrentState() {return currentState;}
//===================================================================
//===================================================================
public boolean hasChangedSince(long previousState) {return previousState != currentState;}
//===================================================================
//===================================================================
public void change(){currentState++;}
//===================================================================
//===================================================================
public EventDispatcher getEventDispatcher()
//===================================================================
{
	if (dispatcher == null) dispatcher = new EventDispatcher();
	return dispatcher;
}
//-------------------------------------------------------------------
protected void dispatchEvent(int type)
//-------------------------------------------------------------------
{
	if (dispatcher != null && !dispatcher.isEmpty())
		dispatcher.dispatch(new DatabaseEvent(type,this));
}
private Lock lookupLock = new Lock();
private int openCount = 0;
private boolean lookupEnabled = false;


/**
 * This should check if the underlying stream supports temporary closing/re-opening.
 * If it does it should close it temporarily and return true. Otherwise it should
 * return false.
 * If this method returns true it will not be called again - even if enableLookupMode() is called
 * again. This method is not called if the Database is opened in R/W mode.
 * @return true if lookup mode was successfully done.
 * @exception IOException if an error occured which renders the Database unusable.
 */
//-------------------------------------------------------------------
protected boolean doEnableLookupMode() throws IOException
//-------------------------------------------------------------------
{
	return false;
}

/**
 * This should temporarily close or re-open the underlying data. This will only be called
 * if doEnableLookupMode() returned true.
 * @param isOpen true to reopen the underlying data, false to close it.
 * @exception IOException if an error occured which renders the Database unusable.
 */
//-------------------------------------------------------------------
protected void doOpenCloseLookup(boolean isOpen) throws IOException
//-------------------------------------------------------------------
{
}

/**
This method tells the database that it will be used for read-only lookups and that it can
close its underlying file if it wishes - and later re-open it when lookups are done without
re-reading the database info.<p>
Not all Databases may support this and if it does not, it will return false - although normal
database operations will not be affected. In other words - if this method returns false, the
application can continue as if it returned true - but its operations will not be optimized
in this way.
 * @return true if this optimization is enabled, false if not.
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public boolean enableLookupMode() throws IOException
//===================================================================
{
	lookupLock.synchronize(); try{
		if (lookupEnabled) return true;
		if (isOpenForReadWrite()) return false;
		return lookupEnabled = doEnableLookupMode();
	}finally{
		lookupLock.release();
	}
}
/**
 * This is used with enableLookupMode() - it tells the database that data is about to be read in
 * and if the underlying file is closed - then it should be re-opened and kept open until closeLookup()
 * is called. Each call to openLookup() should have a corresponding call to closeLookup().
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public void openLookup() throws IOException
//===================================================================
{
	lookupLock.synchronize(); try{
		if (!lookupEnabled) return;
		openCount++;
		if (openCount == 1) doOpenCloseLookup(true);
	}finally{
		lookupLock.release();
	}
}
/**
 * This is used with enableLookupMode() - it tells the database that data reading is complete
 * and the underlying file may be closed.
 * Each call to openLookup() should have a corresponding call to closeLookup().
 * @exception IOException if an IO error occurs.
 */
//===================================================================
public void closeLookup() throws IOException
//===================================================================
{
	lookupLock.synchronize(); try{
		if (!lookupEnabled || openCount == 0) return;
		openCount--;
		if (openCount == 0) doOpenCloseLookup(false);
	}finally{
		lookupLock.release();
	}
}
//-------------------------------------------------------------------
protected void markForReIndex(boolean needReIndex) throws IOException
//-------------------------------------------------------------------
{
	boolean isAppend = openMode.equals("a");
	openMode = "rw";
	int value = indexFlag.getValue();
	if (needReIndex) value |= INDEX_FLAG_DO_REINDEX;
	else value &= ~INDEX_FLAG_DO_REINDEX;
	indexFlag.setValue(value);
	if (isAppend) openMode = "a";
}
//===================================================================
public boolean reIndex(Handle h) throws IOException
//===================================================================
{
	if (!openMode.equals("a")) throw new IOException("Not open for appending");
	if (!doReIndex(h)) return false;
	close();
	return true;
}
//===================================================================
public void append(DatabaseEntry de) throws IOException
//===================================================================
{
	if (!openMode.equals("a")) throw new IOException("Not open for appending");
	doAppend(de);
	markForReIndex(true);
}
//-------------------------------------------------------------------
protected void doAppend(DatabaseEntry de) throws IOException
//-------------------------------------------------------------------
{
	de.save();
}
//##################################################################
}
//##################################################################
