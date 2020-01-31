/* $MirOS: contrib/hosted/ewe/extrajdk/ewe/applet/ClassFile.java,v 1.2 2008/05/02 20:52:02 tg Exp $ */

package ewe.applet;
import ewe.util.*;
import ewe.io.*;
import ewe.sys.*;

//##################################################################
public class ClassFile{
//##################################################################

public ByteArray data;
public String name = "";
public byte[] modified;

// Constant Pool tags
public static final int CONSTANT_Utf8               =1;
public static final int CONSTANT_Integer            =3;
public static final int CONSTANT_Float              =4;
public static final int CONSTANT_Long               =5;
public static final int CONSTANT_Double             =6;
public static final int CONSTANT_Class              =7;
public static final int CONSTANT_String             =8;
public static final int CONSTANT_Fieldref           =9;
public static final int CONSTANT_Methodref          =10;
public static final int CONSTANT_InterfaceMethodref =11;
public static final int CONSTANT_NameAndType        =12;

public UtfPool pool;
public Vector allClasses = new Vector();

/**
* This constructor is used when gathering and processing a set of class files.
**/
//===================================================================
public ClassFile()
//===================================================================
{
	pool = new UtfPool();
}
/**
* Add a class file to the pool.
**/
//===================================================================
public void addToPool(File f) throws ewe.io.IOException
//===================================================================
{
	MemoryFile mf = new MemoryFile(f.toReadableStream(),"r");
	addToPool(mf.data);
}
//===================================================================
public void addToPool(ByteArray data)
//===================================================================
{
	ClassFile cf = new ClassFile(data);
	cf.getAllStrings(pool);
	add(cf);
	allClasses.add(cf);
}


/**
 * Convert the files in the vector to byte arrays containg the compressed class file.
 * @param h A handle to monitor and control the progress
 * @return true if successful, or false if the handle was stopped.
 */
//===================================================================
public boolean convertFiles(Handle h)
//===================================================================
{
	if (h == null) {
		h = new Handle();
		h.set(h.Running);
	}
	int num = allClasses.size();
	for (int i = 0; i<num; i++){
		h.progress = (float)i/num;
		h.changed();
		mThread.nap(0);
		if (h.shouldStop) return false;
		ClassFile cf = (ClassFile)allClasses.get(i);
		cf.modify(pool);
		allClasses.set(i,cf.modified);
	}
	h.progress = 1.0f;
	h.changed();
	mThread.nap(0);
	return true;
}
/**
* This constructor is used for each individual class file.
**/
//===================================================================
public ClassFile(ByteArray data)
//===================================================================
{
	this.data = data;
	size = data.length;
	byte_reduction = 0;
}

//===================================================================
public String getStats()
//===================================================================
{
	return "Original: "+size+", Reduced by: "+byte_reduction+", Changed: "+percentage_change+"%";
}
//===================================================================
public void add(ClassFile cf)
//===================================================================
{
	size += cf.size;
	entries += cf.entries;
	byte_reduction += cf.byte_reduction;
	percentage_change = size == 0 ? 0 : ((size-byte_reduction)*100)/size;
}
//===================================================================
public boolean modify(Hashtable ht)
//===================================================================
{
	modified = new byte[size-byte_reduction];
	ewe.sys.Vm.copyArray(data.data,0,modified,0,10);
	int offset = 10, dest = 10;
	int count = Utils.readInt(data.data,offset-2,2);
//
	for (int i = 0; i<count; i++){
		int was = offset;
		switch((int)data.data[offset]){
			case CONSTANT_Utf8: {
				UtfStringConstant usc = getUtfString(offset);
				offset += usc.size;
				usc =(UtfStringConstant)ht.get(usc.string_value);
				if (usc != null){
					int newValue = usc.code|0xc000;
					ewe.util.Utils.writeInt(newValue,modified,dest,2);
					dest += 2;
					continue;
				}
				break;
			}
			case CONSTANT_Integer:
			case CONSTANT_Float:
			case CONSTANT_Fieldref:
			case CONSTANT_Methodref:
			case CONSTANT_InterfaceMethodref:
			case CONSTANT_NameAndType:
				offset += 5;
				break;
			case CONSTANT_Class:
			case CONSTANT_String:
				offset += 3;
				break;
			case CONSTANT_Long:
			case CONSTANT_Double:
				offset += 9;
				i++;
				break;
			}
		ewe.sys.Vm.copyArray(data.data,was,modified,dest,offset-was);
		dest += offset-was;
	}
	// Finished constants, now do the rest.
	ewe.sys.Vm.copyArray(data.data,offset,modified,dest,size-offset);
	//ewe.sys.Vm.debug("End at: "+(dest+size-offset)+", in: "+modified.length);
	return true;
}

//-------------------------------------------------------------------
byte [] append(byte [] dest,byte [] source,int offset,int length)
//-------------------------------------------------------------------
{
	byte [] newBytes = new byte[dest.length+length];
	ewe.sys.Vm.copyArray(dest,0,newBytes,0,dest.length);
	ewe.sys.Vm.copyArray(source,offset,newBytes,dest.length,length);
	return newBytes;
}
//-------------------------------------------------------------------
byte [] append(byte [] dest,byte [] source)
//-------------------------------------------------------------------
{
	return append(dest,source,0,source.length);
}
//===================================================================
public byte [] convertBack(UtfPool pool)
//===================================================================
{
	byte [] b = data.data;
	byte [] ret = append(new byte[0],b,0,10);
	int count = Utils.readInt(b,8,2);
	int offset = 10;
	for (int i = 0; i<count; i++){
		int was = offset;
		byte tag = b[offset];
		if ((tag & 0xc0) != 0){
			if ((tag & 0xc0) == 0xc0){
				int code = Utils.readInt(b,offset,2) & 0x3fff;
				ret = append(ret,pool.createTag(code));
				offset += 2;
				continue;
			}else{
				int code = Utils.readInt(b,offset,4) & 0x3fffffff;
				ret = append(ret,pool.createTag(code));
				offset += 4;
				continue;
			}
		}else {
			switch(tag){
				case CONSTANT_Utf8:
					UtfStringConstant usc = getUtfString(offset);
					offset += usc.size;
					break;
				case CONSTANT_Integer:
				case CONSTANT_Float:
				case CONSTANT_Fieldref:
				case CONSTANT_Methodref:
				case CONSTANT_InterfaceMethodref:
				case CONSTANT_NameAndType:
					offset += 5;
					break;
				case CONSTANT_Class:
				case CONSTANT_String:
					offset += 3;
					break;
				case CONSTANT_Long:
				case CONSTANT_Double:
					offset += 9;
					i++;
					break;
			}
			ret = append(ret,b,was,offset-was);
		}
	}
	return append(ret,b,offset,b.length-offset);
}
//===================================================================
public Vector getAllStrings(Hashtable ht)
//===================================================================
{
	Vector v = new Vector();
	int offset = 8;
	int count = Utils.readInt(data.data,offset,2);
	offset += 2;
	for (int i = 0; i<count; i++){
		switch((int)data.data[offset]){
			case CONSTANT_Utf8: {
				UtfStringConstant usc = getUtfString(offset);
				v.add(usc); offset += usc.size;
				byte_reduction += usc.size-2;//4;
				add(ht,usc);
				break;
			}
			case CONSTANT_Integer:
			case CONSTANT_Float:
			case CONSTANT_Fieldref:
			case CONSTANT_Methodref:
			case CONSTANT_InterfaceMethodref:
			case CONSTANT_NameAndType:
				offset += 5;
				break;
			case CONSTANT_Class:
			case CONSTANT_String:
				offset += 3;
				break;
			case CONSTANT_Long:
			case CONSTANT_Double:
				offset += 9;
				i++;
				break;
			}
	}
	percentage_change = size == 0 ? 0 : ((size-byte_reduction)*100)/size;
	entries = v.size();
	return v;
}
//===================================================================
public UtfStringConstant getUtfString(int offset)
//===================================================================
{
	UtfStringConstant usc = new UtfStringConstant();
	usc.length =	Utils.readInt(data.data,offset+1,2);
	usc.bytes = new byte[usc.length];
	ewe.sys.Vm.copyArray(data.data,offset+3,usc.bytes,0,usc.length);
	usc.string_value = Utils.decodeJavaUtf8String(data.data,offset+3,usc.length);
	usc.size = 1+2+usc.length;
	return usc;
}

public int size;
public int entries;
public int byte_reduction;
public int percentage_change;

public String _fields = "size,entries,byte_reduction,percentage_change";

//===================================================================
public static UtfStringConstant add(Hashtable table,UtfStringConstant value)
//===================================================================
{
	Object got = table.get(value.string_value);
	if (got == null) {
		//ewe.sys.Vm.debug("Not found: "+value.string_value);
		value.code = table.size();
		table.put(value.string_value,value);
		return value;
	}else{
		return (UtfStringConstant)got;
	}
}

//===================================================================
public String toString()
//===================================================================
{
	return name;
}
//##################################################################
public static class UtfPool extends Hashtable{
//##################################################################

int table_size = 4;
int data_size = 0;
byte [] bytes;

Vector sequential = new Vector();

//===================================================================
public UtfPool(){}
//===================================================================
//===================================================================
public UtfPool(byte [] bytes)
//===================================================================
{
	this.bytes = bytes;
}
//===================================================================
public byte [] createTag(int index)
//===================================================================
{
	int offset = index*4;
	int location = ewe.util.Utils.readInt(bytes,offset,4);
	int length = ewe.util.Utils.readInt(bytes,offset+4,4)-location;
	byte [] tag = new byte[3+length];
	tag[0] = (byte)CONSTANT_Utf8;
	ewe.util.Utils.writeInt(length,tag,1,2);
	ewe.sys.Vm.copyArray(bytes,location,tag,3,length);
	return tag;
}
//===================================================================
public Object put(Object key,Object value)
//===================================================================
{
	table_size += 4;
	data_size += ((UtfStringConstant)value).length;
	sequential.add(value);
	return super.put(key,value);
}

public byte [] makePool()
{
	byte [] ret = new byte[table_size+data_size];
	int to = 0, so = table_size;
	for (int i = 0; i<sequential.size(); i++){
		UtfStringConstant usc = (UtfStringConstant)sequential.get(i);
		Utils.writeInt(so,ret,to,4);
		ewe.sys.Vm.copyArray(usc.bytes,0,ret,so,usc.length);
		to += 4;
		so += usc.length;
	}
	Utils.writeInt(so,ret,to,4);
	return ret;
}
//##################################################################
}
//##################################################################

//##################################################################
public static class UtfStringConstant{
//##################################################################

//public byte [] data;
//public int [] offset;
public int code;
public int length;
public String string_value;
public byte [] bytes;
public int size;

public String _fields = "length,string_value,size";

//===================================================================
public int hashCode()
//===================================================================
{
	return string_value.hashCode();
}
//===================================================================
public String toString() {return string_value;}
//===================================================================
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (this == other) return true;
	return string_value.equals(other.toString());
}
//##################################################################
}
//##################################################################

static ClassFile first = null;
//===================================================================
public static void doDirectory(File f,ClassFile classes,UtfPool pool) throws ewe.io.IOException
//===================================================================
{
	String [] all = f.list("*.class",f.LIST_FILES_ONLY);
	for (int i = 0; i<all.length; i++){
		ewe.sys.Vm.debug(all[i]);
		MemoryFile mf = new MemoryFile(f.getChild(all[i]).toReadableStream(),"r");
		ClassFile cf = new ClassFile(mf.data);
		if (first == null) {
			first = cf;
			first.name = all[i];
		}
		Vector v = cf.getAllStrings(pool);
		classes.add(cf);
	}
	all = f.list("*",f.LIST_DIRECTORIES_ONLY);
	for (int i = 0; i<all.length; i++)
		doDirectory(f.getChild(all[i]),classes,pool);
}
public static void main(String args[]) throws ewe.io.IOException
{
	ewe.sys.Vm.startEwe(args);
	File f = new File(args[0]);
	UtfPool pool = new UtfPool();
	ClassFile classes = new ClassFile();
	doDirectory(f,classes,pool);
	ewe.sys.Vm.debug(classes.getStats());
	ewe.sys.Vm.debug("Pool size: "+(pool.table_size+pool.data_size)+", Total new size: "+(classes.size-classes.byte_reduction+pool.table_size+pool.data_size));
	int actualReduction = classes.size == 0 ? 0 : ((classes.size-classes.byte_reduction+pool.table_size+pool.data_size)*100)/classes.size;
	ewe.sys.Vm.debug("New size: "+actualReduction+"%, Constants: "+pool.size());
	Stream out = new File("_UtfPool_").toWritableStream(false);
	out.write(pool.makePool());
	out.close();
	ewe.sys.Vm.debug(first+", "+first.getStats());
	first.modify(pool);
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################
