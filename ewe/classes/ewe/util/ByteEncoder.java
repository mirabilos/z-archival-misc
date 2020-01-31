package ewe.util;
import ewe.reflect.*;
import ewe.sys.Convert;
import ewe.sys.Vm;
import ewe.data.LiveData;
import ewe.io.StreamCorruptedException;
/**
* A ByteEncoder is used to encode data as a sequence of bytes in an efficient manner.
* It consists of a number of static methods that encode data to a ByteArray or decode
* data from a sequence of bytes.
**/
//##################################################################
public class ByteEncoder{
//##################################################################

private static Wrapper wrapper = new Wrapper();
private static Wrapper[] forCreate;
private static Range range = new Range(0,0);

//-------------------------------------------------------------------
private static int append(ByteArray dest,char value)
//-------------------------------------------------------------------
{
	if (dest == null) return 1;
	dest.makeSpace(dest.length,1);
	dest.data[dest.length-1] =(byte)value;
	return 1;
}
//-------------------------------------------------------------------
private static int appendInt(ByteArray dest,int value,int numBytes)
//-------------------------------------------------------------------
{
	if (dest == null) return numBytes;
	int len = dest.length;
	dest.makeSpace(len,numBytes);
	Utils.writeInt(value,dest.data,len,numBytes);
	return numBytes;
}
//-------------------------------------------------------------------
private static int appendInt(ByteArray dest,char type,int value,int numBytes)
//-------------------------------------------------------------------
{
	int size = 1+numBytes;
	if (dest == null) return size;
	int len = dest.length;
	dest.makeSpace(len,size);
	dest.data[len++] =(byte)type;
	Utils.writeInt(value,dest.data,len,numBytes);
	return size;
}
//-------------------------------------------------------------------
private static int appendLong(ByteArray dest,char type,long value)
//-------------------------------------------------------------------
{
	int size = 9;
	if (dest == null) return size;
	int len = dest.length;
	dest.makeSpace(len,size);
	dest.data[len++] =(byte)type;
	Utils.writeLong(value,dest.data,len);
	return size;
}
//-------------------------------------------------------------------
private static int appendDouble(ByteArray dest,char type,double value)
//-------------------------------------------------------------------
{
	int size = 9;
	if (dest == null) return size;
	int len = dest.length;
	dest.makeSpace(len,size);
	dest.data[len++] =(byte)type;
	Utils.writeLong(Convert.toLongBitwise(value),dest.data,len);
	return size;
}
//-------------------------------------------------------------------
private static int sizeofVarSize(int size)
//-------------------------------------------------------------------
{
	if (size < 0) throw new IllegalArgumentException();
	if (size <= 0x7f) return 1;
	if (size <= 0x3fff) return 2;
	return 4;
}
//-------------------------------------------------------------------
private static int readVarSize(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//-------------------------------------------------------------------
{
	if (lengthLeft < 1) throw new StreamCorruptedException();
	byte one = source[offset];
	if ((one & 0x80) == 0) return (int)one;
	if ((one & 0xc0) == 0x80)
		if (lengthLeft < 2) throw new StreamCorruptedException();
		else return ((one & 0x3f) << 8)|(source[offset+1] & 0xff);

	if (lengthLeft < 4) throw new StreamCorruptedException();
	return ((one & 0x3f) << 24) |((source[offset+1] & 0xff) << 16)|((source[offset+2] & 0xff) << 8)|(source[offset+3] & 0xff);
}
//-------------------------------------------------------------------
private static int appendVarSize(ByteArray dest,int size)
//-------------------------------------------------------------------
{
	if (size < 0) throw new IllegalArgumentException();
	if (size <= 0x7f) return appendInt(dest,size,1);
	if (size <= 0x3fff) return appendInt(dest,0x8000|size,2);
	return appendInt(dest,0xc0000000|size,4);
}
//-------------------------------------------------------------------
private static int appendString(int utfSize,ByteArray dest,char[] ch,int offset,int length)
//-------------------------------------------------------------------
{
	if (utfSize < 0) utfSize = Utils.sizeofJavaUtf8String(ch,offset,length);
	if (dest == null) return utfSize;
	dest.makeSpace(dest.length,utfSize);
	Utils.encodeJavaUtf8String(ch,0,ch.length,dest.data,dest.length-utfSize);
	return utfSize;
}
//-------------------------------------------------------------------
private static int arrayElementSize(char ch)
//-------------------------------------------------------------------
{
	switch(ch){
	//
	// Note a char array is UTF encoded with a type of '#'
	//
		case 'z': return 1;
		case 'b': return 1;
		case 's': return 2;
		case 'i': return 4;
		case 'f': return 8;
		case 'd': return 8;
		case 'j': return 8;
		default: return 0;
	}
}
static Object[] created = new Object[1];

//
//FIXME make this native.
//
//-------------------------------------------------------------------
private static int createNewArray(byte[] source,int offset,int left,Object[] created)
throws StreamCorruptedException
//-------------------------------------------------------------------
{
	int i = 0;
	for (i = 0; i<left && source[offset+i] != 0; i++)
		;
	if (i >= left) throw new StreamCorruptedException();

	String component = Utils.decodeJavaUtf8String(source,offset,i);
	i++;// Go past the trailing 0.
	int elements = readVarSize(source,offset+i,left-i);
	i += sizeofVarSize(elements);
	if (component.equals("#")) created[0] = new Object[elements];
	else if (component.equals("$")) created[0] = new String[elements];
	else {
		component = component.replace('.','/');
		if (component.charAt(0) != '[') component = "L"+component+";";
		created[0] = Array.newInstance(component,elements);
	}
	return i;
}

private final static String[] decodables = new String[]{
"Lewe/util/ByteDecodable;",
"Lewe/data/LiveData;",
"Lewe/util/Vector;",
"Lewe/util/Textable;",
"Lewe/util/Stringable;",
"Lewe/util/Encodable;",
};

//
//FIXME make this native.
//
//-------------------------------------------------------------------
private static int createNewObject(byte[] source,int offset,int left,Object[] created)
throws StreamCorruptedException, ClassNotFoundException
//-------------------------------------------------------------------
{
	int i = 0;
	for (i = 0;i<left && source[offset+i] != 0; i++)
		;
	if (i >= left) throw new StreamCorruptedException();

	String component = Utils.decodeJavaUtf8String(source,offset,i);
	i++;// Go past the trailing 0.

	if (component.equals("#")) component = "java.lang.Object";
	else if (component.equals("$")) component = "java.lang.String";

	Class c = Reflect.loadClass(component);
	if (c == null) throw new ClassNotFoundException();

	Reflect r = new Reflect(c);
	boolean canDecode = false;
	for (int t = 0; t<decodables.length; t++){
		if (r.isTypeOf(decodables[t])){
			canDecode = true;
			break;
		}
	}

	if (!canDecode) created[0] = null;

	if (created[0] == null || !r.isInstance(created[0])){
		if (forCreate == null){
			forCreate = new Wrapper[3];
			for (int j = 0; j<forCreate.length; j++)
				forCreate[j] = new Wrapper();
		}
		forCreate[0].setArray(source);
		forCreate[1].setInt(offset+i);
		forCreate[2].setInt(left-i);
		created[0] = r.newInstance("[BII",forCreate);
		if (created[0] != null) return left;
		if (!canDecode) return 0;
		created[0] = r.newInstance();
	}
	if (created[0] == null) return 0;
	return i;
}

//-------------------------------------------------------------------
private static Object decodeLObject(byte[] source,int offset,int length,Object dest)
throws StreamCorruptedException, ClassNotFoundException
//-------------------------------------------------------------------
{
	created[0] = dest;
	int where = offset;
	int read = createNewObject(source,where,length,created);
	if (created[0] == null || read == length) return created[0];
	if (read != 0){
		where += read;
		length -= read;
		Object ret = created[0];
		decodeObjectData(source,where,length,ret);
		return ret;
	}else
		return null;
}
/*
//===================================================================
public static int decode(byte[] source,int offset,int lengthLeft,Wrapper dest)
//===================================================================
{

}
*/
//-------------------------------------------------------------------
private static void appendStart(ByteArray dest,char type,int total,int size)
//-------------------------------------------------------------------
{
	dest.makeSpace(dest.length,total);
	dest.length -= total-1;
	dest.data[dest.length-1] = (byte)type;
	if (size >= 0) appendVarSize(dest,size);
}
//-------------------------------------------------------------------
private static Object decodePrimitiveArray(byte[] source,int offset,int lengthLeft,char type,int elements)
throws StreamCorruptedException
//-------------------------------------------------------------------
{
	int where = offset;
	switch(type){
		case 's': {
			if (elements*2 > lengthLeft) throw new StreamCorruptedException();
			short[]a = new short[elements];
			for (int i = 0; i<elements; i++){
				a[i] = (short)Utils.readInt(source,where,2);
				where += 2;
			}
			return a;
		}
		case 'i': {
			if (elements*4 > lengthLeft) throw new StreamCorruptedException();
			int[]a = new int[elements];
			for (int i = 0; i<elements; i++){
				a[i] = Utils.readInt(source,where,4);
				where += 4;
			}
			return a;
		}
		case 'j': {
			if (elements*8 > lengthLeft) throw new StreamCorruptedException();
			long[]a = new long[elements];
			for (int i = 0; i<elements; i++){
				a[i] = Utils.readLong(source,where);
				where += 8;
			}
			return a;
		}
		case 'f': {
			if (elements*8 > lengthLeft) throw new StreamCorruptedException();
			float[]a = new float[elements];
			for (int i = 0; i<elements; i++){
				a[i] = (float)Convert.toDoubleBitwise(Utils.readLong(source,where));
				where += 8;
			}
			return a;
		}
		case 'd': {
			if (elements*8 > lengthLeft) throw new StreamCorruptedException();
			double[]a = new double[elements];
			for (int i = 0; i<elements; i++){
				a[i] = Convert.toDoubleBitwise(Utils.readLong(source,where));
				where += 8;
			}
			return a;
		}
		case 'z': {
			if (elements*1 > lengthLeft) throw new StreamCorruptedException();
			boolean[]a = new boolean[elements];
			for (int i = 0; i<elements; i++){
				a[i] = source[where++] != 0;
			}
			return a;
		}
		case 'b': {
			if (elements*1 > lengthLeft) throw new StreamCorruptedException();
			byte[]a = new byte[elements];
			if (elements != 0) Vm.copyArray(source,where,a,0,elements);
			return a;
		}
		default:
			throw new StreamCorruptedException();
	}
}
//-------------------------------------------------------------------
private static void appendPrimitiveArray(ByteArray dest,Object array)
//-------------------------------------------------------------------
{
	int where = dest.length;
	byte[] data = dest.data;
	/* Char arrays are stored as utf encoded arrays.
	if (array instanceof char[]){
		char[]a = (char[])array;
		for (int i = 0; i<a.length; i++){
			Utils.writeInt(a[i],data,where,2);
			where += 2;
		}
	}
	*/
	if (array instanceof short[]){
		short[]a = (short[])array;
		for (int i = 0; i<a.length; i++){
			Utils.writeInt(a[i],data,where,2);
			where += 2;
		}
	}
	if (array instanceof int[]){
		int[]a = (int[])array;
		for (int i = 0; i<a.length; i++){
			Utils.writeInt(a[i],data,where,4);
			where += 4;
		}
	}
	if (array instanceof long[]){
		long[]a = (long[])array;
		for (int i = 0; i<a.length; i++){
			Utils.writeLong(a[i],data,where);
			where += 8;
		}
	}
	if (array instanceof float[]){
		float[]a = (float[])array;
		for (int i = 0; i<a.length; i++){
			Utils.writeLong(Convert.toLongBitwise(a[i]),data,where);
			where += 8;
		}
	}
	if (array instanceof double[]){
		double[]a = (double[])array;
		for (int i = 0; i<a.length; i++){
			Utils.writeLong(Convert.toLongBitwise(a[i]),data,where);
			where += 8;
		}
	}
	if (array instanceof boolean[]){
		boolean[]a = (boolean[])array;
		for (int i = 0; i<a.length; i++){
			if (a[i]) data[where] = 1;
			else data[where] = 0;
			where += 1;
		}
	}
	if (array instanceof byte[]){
		byte[]a = (byte[])array;
		if (a.length != 0)
			Vm.copyArray(a,0,data,where,a.length);
		where += a.length;
	}
	dest.length = where;
}
/**
 * Encode the fields of an Encodable object. The data is encoded by encoding a sequence
 * of field name and field value pairs. After it is encoded you can add on extra fields
 * if you like, as long as their names do not conflict with already saved field names.<p>
 * Note that this method does not store information about the class of the object. This means
 * you must know the object class and have a pre-created object to call the corresponding
 * decodeEncodableObjectData() when you are ready to decode it.
 * @param dest The destination ByteArray or null to just get the number of bytes needed.
 * @param obj The object to encode.
 * @return the number of bytes encoded or would need to be encoded.
 */
//===================================================================
public static int encodeEncodableObjectData(ByteArray dest,Encodable obj)
//===================================================================
{
	int total = 0;
	Field [] allFields = Utils.fieldsFor(obj);
	if (allFields == null) return 0;
	for (int i = 0; i<allFields.length; i++){
		if (allFields[i] == null) continue;
		if (Modifier.isStatic(allFields[i].getModifiers())) continue;
		total += encodeObject(dest,allFields[i].getName(),true);
		wrapper.zero();
		allFields[i].getValue(obj,wrapper);
		total += encode(dest,wrapper);
	}
	return total;
}
/**
 * Encode only the data for the Object, not any information about the class of the Object. Do
	not call this method for arrays.
<p>
The method used to encode the object depends on the class of the object or the interfaces it
implements. The type of Objects you can encode are:
<ul>
<li>A ewe.util.Vector - in which case it is encoded as an array of objects, each one
encoded using encodeObject().
<li>A ByteEncodable object - in which case the encodeBytes() method is used.
<li>A Textable object - in which case the getText() method is called on the object and the
returned String is encoded.
<li>A LiveData object - in which case the textEncode() method is called on the object and the
returned String is encoded.
<li>An Encodable object - in which case all public fields are encoded individually using encodeEncodableObjectData().
<li>Any other object has its toString() method called on it and the returned String is then
encoded.
</ul>
 * @param dest The destination ByteArray or null to just get the number of bytes needed.
 * @param obj The object to encode.
 * @return the number of bytes encoded or would need to be encoded.
 */
//===================================================================
public static int encodeObjectData(ByteArray dest,Object obj)
//===================================================================
{
	int total = 0;
	if (obj instanceof Vector){
		Vector v = (Vector)obj;
		for (int i = 0; i<v.size(); i++)
			total += encodeObject(dest,v.get(i),true);
		return total;
	}else if (obj instanceof ByteEncodable){
		return ((ByteEncodable)obj).encodeBytes(dest);
	}else if (obj instanceof Textable){
		return encodeObject(dest,((Textable)obj).getText(),true);
	}else if (obj instanceof LiveData){
		return encodeObject(dest,((LiveData)obj).textEncode(),true);
	}else if (obj instanceof Encodable){
		return encodeEncodableObjectData(dest,(Encodable)obj);
	}else
		return encodeObject(dest,obj.toString(),true);
}
/**
 * Decode the fields of an Encodable object, as encoded by encodeEncodableObjectData().
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param dataLength the number of bytes.
 * @param obj the destination object.
 */
//===================================================================
public static void decodeEncodableObjectData(byte[] source,int offset,int dataLength,Object obj)
throws StreamCorruptedException
//===================================================================
{
	Field [] allFields = Utils.fieldsFor(obj);
	while(dataLength > 0){
		wrapper.zero();
		try{
			int read = decode(source,offset,dataLength,wrapper);
			String fieldName = (String)wrapper.getObject();
			offset += read;
			dataLength -= read;
			wrapper.zero();
			boolean didRead = false;
			for (int i = 0; i<allFields.length; i++){
				if (allFields[i] == null) continue;
				if (allFields[i].getName().equals(fieldName)){
					allFields[i].getValue(obj,wrapper);
					read = decode(source,offset,dataLength,wrapper);
					offset += read;
					dataLength -= read;
					didRead = true;
					allFields[i].setValue(obj,wrapper);
					break;
				}
			}
			if (!didRead){
				int sz = sizeOfData(source,offset,dataLength);
				offset += sz;
				dataLength -= sz;
			}
		}catch(ClassNotFoundException e){
			throw new StreamCorruptedException();
		}catch(RuntimeException e){
			throw new StreamCorruptedException();
		}
	}
}
/**
*  Decode the data bytes of the object, as encoded by encodeObjectData. This is not to be used
* on arrays.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param dataLength the number of bytes.
 * @param obj the destination object.
*/
//===================================================================
public static void decodeObjectData(byte[] source,int offset,int dataLength,Object obj)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	int total = 0;
	if (obj instanceof Vector){
		Vector v = (Vector)obj;
		v.clear();
		Object[] all = decodeObjects(source,offset,dataLength,null);
		v.addAll(all);
	}else if (obj instanceof ByteDecodable){
		((ByteDecodable)obj).decodeBytes(source,offset,dataLength);
	}else if (obj instanceof Textable){
		wrapper.zero();
		decode(source,offset,dataLength,wrapper);
		((Textable)obj).setText((String)wrapper.getObject());
	}else if (obj instanceof LiveData){
		wrapper.zero();
		decode(source,offset,dataLength,wrapper);
		((LiveData)obj).textDecode((String)wrapper.getObject());
	}else if (obj instanceof Encodable){
		decodeEncodableObjectData(source,offset,dataLength,(Encodable)obj);
	}else if (obj instanceof Stringable){
		wrapper.zero();
		decode(source,offset,dataLength,wrapper);
		((Stringable)obj).fromString((String)wrapper.getObject());
	}else
		return;//encodeObject(dest,obj.toString(),true);
	return;
}
//-------------------------------------------------------------------
private static int encodeObject(ByteArray dest,Object obj,boolean longStrings)
//-------------------------------------------------------------------
{
	if (obj == null) return append(dest,(char)0);
	if (obj instanceof String){
		char[] ch = ewe.sys.Vm.getStringChars((String)obj);
		int size = Utils.sizeofJavaUtf8String(ch,0,ch.length);
		int total = 1+size;
		//
		if (longStrings) total += sizeofVarSize(size);
		else total++;
		//
		if (dest == null) return total;
		appendStart(dest,longStrings ? '%' : '$',total,longStrings ? size : -1);
		if (longStrings){
			appendString(size,dest,ch,0,ch.length);
		}else{
			appendString(size,dest,ch,0,ch.length);
			append(dest,(char)0);
		}
		return total;
	}else if (Array.isArray(obj)){
		Class c = Array.getComponentType(obj);
		int length = Array.getLength(obj);
		char type = Character.toLowerCase(Reflect.getWrapperType(c));
		if (c.isPrimitive()){
			if (type == 'c'){
				char[] ch = (char[])obj;
				int size = Utils.sizeofJavaUtf8String(ch,0,ch.length);
				int total = 1+size+sizeofVarSize(size);
				if (dest == null) return total;
				appendStart(dest,'#',total,size);
				appendString(size,dest,ch,0,ch.length);
				return total;
			}else if (type == 'b'){
				byte[] by = (byte[])obj;
				return encode(dest,by,0,by.length);
			}else{
				int total = 1+sizeofVarSize(length)+(length*arrayElementSize(type));
				if (dest == null) return total;
				appendStart(dest,type,total,length);
				appendPrimitiveArray(dest,obj);
				return total;
			}
		}else{
			Object[] a = (Object[])obj;
			String comp = c.getName();
			if (comp.equals("java.lang.String")) comp = "$";
			else if (comp.equals("java.lang.Object")) comp = "#";
			int len = a.length;
			char [] cc = Vm.getStringChars(comp);
			int utfSize = Utils.sizeofJavaUtf8String(cc,0,cc.length);
			int size = utfSize+1+sizeofVarSize(len);
			ByteArray ba = new ByteArray();
			for (int i = 0; i<len; i++)
				size += encodeObject(ba,a[i],true);
			int total = 1+sizeofVarSize(size)+size;
			if (dest == null) return total;
			appendStart(dest,'[',total,size);
			appendString(utfSize,dest,cc,0,cc.length);
			append(dest,(char)0);
			appendVarSize(dest,len);
			dest.append(ba.data,0,ba.length);
			return total;
		}
	}else{
	/*
	* Write an 'L'; the size of the data (including the class name);
	* the class name UTF encoded with a zero terminator; the encoded class bytes.
	*/
		char [] cc = Vm.getStringChars(obj.getClass().getName());
		int utfSize = Utils.sizeofJavaUtf8String(cc,0,cc.length);
		int size = utfSize+1;
		ByteArray ba = new ByteArray();
		size += encodeObjectData(ba,obj);
		int total = 1+sizeofVarSize(size)+size;
		if (dest == null) return total;
		appendStart(dest,'L',total,size);
		appendString(utfSize,dest,cc,0,cc.length);
		append(dest,(char)0);
		dest.append(ba.data,0,ba.length);
		return total;
	}
}
/**
* Return the number of bytes stored starting at the particular offset.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param dataLength the number of bytes.
 * @return the number of bytes stored starting at the particular offset.
**/
//===================================================================
public static int sizeOfData(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//===================================================================
{
	if (lengthLeft < 1) throw new StreamCorruptedException();
	char ch = (char)source[offset];
	switch(ch){
		case 0  : return 1;

		case '0': return 1; // Integer constant value 0
		case '1': return 2; // One-byte Integer value
		case '2': return 3; // Two-byte Integer value

		case '.': return 1; // Double constant value 0.0
		case ',': return 1; // Float constant value 0.0f
		case ';': return 1; // Long constant value 0

		case 'Y':           // Boolean value false.
		case 'Z': return 1; // Boolean value true.

		case 'B': return 2;
		case 'C':
		case 'S': return 3;
		case 'I': return 5;
		case 'F':
		case 'D':
		case 'J': return 9;
		case '$': int i = 1;
							while(source[offset+i] != 0) i++;
							return i+1;
		default:
		/*
		case 'L':
		case '[':
		case '%':
		case '#':
		*/
			int dataLen = readVarSize(source,offset+1,lengthLeft-1);
			int sizeLen = sizeofVarSize(dataLen);
			int elen = arrayElementSize(ch);
			if (elen == 0) return 1+dataLen+sizeLen;
			else return 1+sizeLen+(dataLen*elen);
	}
}

/**
 * Return the Wrapper type of the data stored at the specified location.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param lengthLeft the number of bytes.
 * @param obj the destination object.
 * @return The type of the data as a Wrapper type. If lengthLeft is less than or equal to zero
 * then a StreamCorruptedException() will be thrown.
 */
//===================================================================
public static int typeOfData(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//===================================================================
{
	if (lengthLeft < 1) throw new StreamCorruptedException();
	char s = (char)source[offset];
	if (s == 'Y') return 'Z';
	if (s >= 'A' && s <= 'Z') return s;
	if (s >= 'a' && s <= 'z') return '[';
	if (s == '[' || s == '#') return '[';
	if (s == 'L' || s == '$' || s == '%' || s == 0)
		return 'L';
	if (s >= '0' && s <= '2') return 'I';
	if (s == '.') return 'D';
	if (s == ',') return 'F';
	if (s == ';') return 'J';
	throw new StreamCorruptedException();
}
//===================================================================
public static boolean isString(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//===================================================================
{
	if (lengthLeft <= 0) throw new StreamCorruptedException();
	char s = (char)source[offset];
	return (s == '$' || s == '%');
}
//===================================================================
public static boolean isObjectOrArray(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//===================================================================
{
	int ty = typeOfData(source,offset,lengthLeft);
	return ty == 'L' || ty == '[';
}
/**
 * Return the class of the data as a string as it would be returned if
 * you had called getClass().getName() on the original data.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param lengthLeft the number of bytes.
 * @param obj the destination object.
 * @return the class of the data stored at a location. If this is null then a null object
 * pointer is stored at that location.
 * @exception IllegalArgumentException if the data at the specified location is not
 * an Object or Array class.
 */
//===================================================================
public static String classOfData(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//===================================================================
{
	if (lengthLeft <= 0) throw new StreamCorruptedException();
	char s = (char)source[offset];
	if (s == '#') return "[C";
	else if (s == '%' || s == '$') return "java.lang.String";
	else if (s >= 'a' && s <= 'z') return "["+Character.toUpperCase(s);
	else if (s == 'L' || s == '['){
		int dataLen = readVarSize(source,offset+1,lengthLeft-1);
		int sizeLen = sizeofVarSize(dataLen);
		offset += sizeLen+1;
		int i = 0;
		for (i = 0;source[offset+i] != 0; i++)
			;
		String ret = Utils.decodeJavaUtf8String(source,offset,i);
		if (s == 'L') return ret;
		if (ret.charAt(0) == '[') return "["+ret;
		return "[L"+ret+";";
	}else if (s == 0){
		return null;
	}else
		throw new StreamCorruptedException();
}

/**
 * Count how many data units (primitive values or object values) are stored in sequence
 * in the specified sequence of bytes.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param lengthLeft the number of bytes.
 * @return the number of data units stored at the specified offset.
 */
//===================================================================
public static int countEncoded(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException
//===================================================================
{
	for (int num = 0;; num++){
		if (lengthLeft < 1) return num;
		int size = sizeOfData(source,offset,lengthLeft);
		offset += size;
		lengthLeft -= size;
	}
}
/**
 * Fully encode any data type, including a null object reference.
 * @param dest the destination ByteArray or null to determine the number of bytes needed.
 * @param w a wrapper that holds the data to be encoded.
 * @return the number of bytes encoded or decoded.
 */
//===================================================================
public static int encode(ByteArray dest,Wrapper w)
//===================================================================
{
	if (w == null) return append(dest,(char)0);
	switch(w.getType()){
		case Wrapper.OBJECT:
		case Wrapper.ARRAY: return encodeObject(dest,w.getObject(),true);

		case Wrapper.INT:
			int i = w.getInt();
			if (i == 0) return append(dest,'0');
			else if (i > 0){
				if (i <= 0x7f) return appendInt(dest,'1',i,1);
				else if (i <= 0x7fff) return appendInt(dest,'2',i,2);
			}else{
				if ((i & 0xffffff80) == 0xffffff80) return appendInt(dest,'1',i,1);
				else if ((i & 0xffff8000) == 0xffff8000) return appendInt(dest,'2',i,2);
			}
			return appendInt(dest,'I',i,4);
		case Wrapper.SHORT: return appendInt(dest,'S',w.getShort(),2);
		case Wrapper.CHAR: return appendInt(dest,'C',w.getChar(),2);
		case Wrapper.BYTE: return appendInt(dest,'B',w.getChar(),1);

		case Wrapper.LONG:
			long v = w.getLong();
			if (v == 0) return append(dest,';');
			else return appendLong(dest,'J',v);

		case Wrapper.FLOAT:
			float f = w.getFloat();
			if (f == 0) return append(dest,',');
			else return appendDouble(dest,'F',f);

		case Wrapper.DOUBLE:
			double d = w.getDouble();
			if (d == 0) return append(dest,'.');
			else return appendDouble(dest,'D',d);

		case Wrapper.BOOLEAN: return append(dest, w.getBoolean() ? 'Z' : 'Y');
	}
	throw new IllegalArgumentException();
}
/**
* Decode a data unit at the specified location, placing it in the destination Wrapper.
If the stored value is an object you can set a destination object by calling setObject()
on the Wrapper before calling this method. Then the data will attempt to be decoded into
that object. However if that object is inappropriate, then a new object will be created
and returned instead.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param lengthLeft the number of bytes.
 * @return the number of bytes used by the data unit.
**/
//===================================================================
public static int decode(byte[] source,int offset,int lengthLeft,Wrapper dest)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	char type = (char)source[offset];
	switch(type){
		case 0  : dest.setObject(null); return 1;
		case 'Y': dest.setBoolean(false); return 1;
		case 'Z': dest.setBoolean(true); return 1;
		case 'B': dest.setByte(source[offset+1]); return 2;
		case 'C': dest.setChar((char)Utils.readInt(source,offset+1,2)); return 3;
		case 'S': dest.setShort((short)Utils.readInt(source,offset+1,2)); return 3;

		case '0': dest.setInt(0); return 1;
		case '.': dest.setDouble(0); return 1;
		case ',': dest.setFloat(0); return 1;
		case ';': dest.setLong(0); return 1;

		case '1': dest.setInt((int)source[offset+1]); return 2;
		case '2': short s = (short)((source[offset+1] << 8) | ((source[offset+2]) & 0xff));
							dest.setInt((int)s); return 3;

		case 'I': dest.setInt(Utils.readInt(source,offset+1,4)); return 5;
		case 'F': dest.setFloat((float)Convert.toDoubleBitwise(Utils.readLong(source,offset+1))); return 9;
		case 'D': dest.setDouble(Convert.toDoubleBitwise(Utils.readLong(source,offset+1))); return 9;
		case 'J': dest.setLong(Utils.readLong(source,offset+1)); return 9;
		case '$': int i = 1;
							while(source[offset+i] != 0) i++;
							dest.setObject(Utils.decodeJavaUtf8String(source,offset+1,i-1));
							return i+1;
		default:
		/*
		case 'L':
		case '[': //Array of objects.
		case '%':
		case '#'
		*/
					int dataLen = readVarSize(source,offset+1,lengthLeft-1);
					int sizeLen = sizeofVarSize(dataLen);
					int elen = arrayElementSize(type);
					int where = offset+1+sizeLen;
					lengthLeft -= 1+sizeLen;

					if (type == '%'){
						if (dataLen > lengthLeft) throw new StreamCorruptedException();
						dest.setObject(Utils.decodeJavaUtf8String(source,where,dataLen));
					}else if (type == '#'){
						if (dataLen > lengthLeft) throw new StreamCorruptedException();
						int numChars = Utils.sizeofJavaUtf8String(source,where,dataLen);
						char[] ret = new char[numChars];
						Utils.decodeJavaUtf8String(source,where,dataLen,ret,0);
						dest.setArray(ret);
					}else if (type == '['){
						created[0] = null;
						int read = createNewArray(source,where,dataLen,created);
						if (read != 0){
							Object[] a = (Object[])created[0];
							where += read;
							decodeObjects(source,where,dataLen+2-(where-offset),a);
							dest.setArray(a);
						}
					}else if (type == 'L'){
						if (dataLen > lengthLeft) throw new StreamCorruptedException();
						Object d = null;
						if (dest.getType() == dest.OBJECT) d = dest.getObject();
						dest.setObject(decodeLObject(source,where,dataLen,d));
					}else if (elen != 0){
						dest.setArray(decodePrimitiveArray(source,where,lengthLeft,type,dataLen));
						return 1+sizeLen+(dataLen*elen);
					}
					return 1+dataLen+sizeLen;
	}
}
//===================================================================
public static int decodeString(byte[] source,int offset,int lengthLeft,Wrapper dest)
throws StreamCorruptedException
//===================================================================
{
	try{
		if (!isString(source,offset,lengthLeft)) throw new StreamCorruptedException();
		return decode(source,offset,lengthLeft,wrapper);
	}catch(ClassNotFoundException c){
		throw new StreamCorruptedException();
	}
}
/**
 * Encode an object, specifying the full class of the object. If you want to just encode
 * the data of the object, use encodeObjectData() instead, however you should not use that
 * method for immutable objects such as Strings. For immutable object you should use this.
 * @param dest The destination ByteArray or null to just find out how many bytes are needed.
 * @param obj The object to encode.
 * @return The number of bytes encoded.
 */
//===================================================================
public static int encodeObject(ByteArray dest,Object obj)
//===================================================================
{
	return encodeObject(dest,obj,false);
}
/**
 * Decode an object into the specified destination object. This method is capable of creating
 * a new object as long as it has an appropriate constructor. If you encoded the object using
 * encodeObjectData() then you should decode it using decodeObjectData() instead. The encodeObjectData()
 * method does not store class information about the object and so cannot create a new instance
 * of an encoded class.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param length the number of bytes.
 * @param dest the destination object which may be null, in which case a new instance of the
 * object will be created (if possible).
 * @return the destination object or a new object if no destination or an incorrect destination
 * is provided.
 */
//===================================================================
public static Object decodeObject(byte[] source,int offset,int lengthLeft,Object dest)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	wrapper.setObject(dest);
	decode(source,offset,lengthLeft,wrapper);
	if (wrapper.getType() != wrapper.ARRAY && wrapper.getType() != wrapper.OBJECT)
		throw new StreamCorruptedException();
	return wrapper.getObject();
/*
	if (!isObjectOrArray(source,offset,lengthLeft)) throw new StreamCorruptedException();
	char type = (char)source[offset];
	switch(type){
		case '$': int i = 1;
							while(i < lengthLeft && source[offset+i] != 0) i++;
							if (i == lengthLeft) throw new StreamCorruptedException();
							return Utils.decodeJavaUtf8String(source,offset+1,i-1);
		default:
							int dataLen = readVarSize(source,offset+1,lengthLeft-1);
							int sizeLen = sizeofVarSize(dataLen);
							int elen = arrayElementSize(type);
							int where = offset+1+sizeLen;
							lengthLeft -= 1+sizeLen;
							if (dataLen > lengthLeft) throw new StreamCorruptedException();
							if (type == '%'){
								return Utils.decodeJavaUtf8String(source,where,dataLen);
							}else if (type == 'L'){
								return decodeLObject(source,where,dataLen,dest);
							}else
								return null;
	}
*/
}
/**
 * Decode an array of Objects.
 * @param source the source of the data bytes.
 * @param offset the offset of the first byte.
 * @param lengthLeft the number of bytes.
 * @param dest the destination objects which may be null, in which case a new instance of the
 * objects will be returned.
 * @return
 */
//===================================================================
public static Object[] decodeObjects(byte[] source,int offset,int lengthLeft,Object[] dest)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	Wrapper w = new Wrapper();
	int len = countEncoded(source,offset,lengthLeft);
	if (dest == null) dest = new Object[len];
	for (int num = 0;num < len;num++){
		w.zero();
		w.setObject(dest[num]);
		int size = decode(source,offset,lengthLeft,w);
		dest[num] = w.getObject();
		offset += size;
		lengthLeft -= size;
	}
	return dest;
}

//===================================================================
public static Wrapper[] decode(byte[] source,int offset,int lengthLeft)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	Wrapper[] ret = new Wrapper[countEncoded(source,offset,lengthLeft)];
	for (int num = 0;num < ret.length;num++){
		int size = decode(source,offset,lengthLeft,ret[num] = new Wrapper());
		offset += size;
		lengthLeft -= size;
	}
	return ret;
}
//===================================================================
public static int encodeField(ByteArray dest,String fieldName,Wrapper data)
//===================================================================
{
	int num = encodeObject(dest,fieldName);
	num += encode(dest,data);
	return num;
}
//===================================================================
public static int encodeField(ByteArray dest,String fieldName,Object data)
//===================================================================
{
	int num = encodeObject(dest,fieldName);
	num += encodeObject(dest,data);
	return num;
}
//===================================================================
public static int encodeField(ByteArray dest,String fieldName,byte[] data,int offset,int length)
//===================================================================
{
	int num = encodeObject(dest,fieldName);
	num += encode(dest,data,offset,length);
	return num;
}
/**
 * This encodes the bytes as a byte array data unit. When decoded using decode() it will
 * be decoded as a new byte array of the exact length as specified in this call.
 * @param dest
 * @param source
 * @param offset
 * @param length
 * @return The number of bytes used.
 */
//===================================================================
public static int encode(ByteArray dest,byte[] source, int offset, int length)
//===================================================================
{
	int total = 1+sizeofVarSize(length)+length;
	if (dest == null) return total;
	appendStart(dest,'b',total,length);
	Vm.copyArray(source,offset,dest.data,dest.length,length);
	dest.length += length;
	return total;
}
//===================================================================
public static Range findField(byte[] source,int offset,int length,String fieldName,Range dest)
throws StreamCorruptedException
//===================================================================
{
	if (dest == null) dest = new Range(0,0);
	while(length > 0){
		int read = decodeString(source,offset,length,wrapper);
		offset += read;
		length -= read;
		if (length < 1) return null; //Nothing after the fieldname.
		read = sizeOfData(source,offset,length);
		if (fieldName.equals(wrapper.getObject())){
			dest.first = offset;
			dest.last = offset+read-1;
			return dest;
		}
		offset += read;
		length -= read;
	}
	return null;
}
//===================================================================
public static Range getByteArrayBytes(byte[] source,int offset,int length,Range dest)
throws StreamCorruptedException
//===================================================================
{
	if (dest == null) dest = new Range(0,0);
	if (length <= 0 || source[offset] != 'b') throw new StreamCorruptedException();
	int num = readVarSize(source,offset+1,length-1);
	dest.first = offset+1+sizeofVarSize(num);
	dest.last = dest.first+num-1;
	return dest;
}
//===================================================================
public static Range getByteArrayBytes(byte[] source,Range foundData,Range dest)
throws StreamCorruptedException
//===================================================================
{
	return getByteArrayBytes(source,foundData.first,foundData.last-foundData.first+1,dest);
}
//===================================================================
public static Range getFieldByteArray(byte[] source,int offset,int length,String fieldName,Range dest)
throws StreamCorruptedException
//===================================================================
{
	dest = findField(source,offset,length,fieldName,dest);
	if (dest == null) return null;
	return getByteArrayBytes(source,dest,dest);
}
/**
* This searchs for a field name and returns the encoded value associated with the field.
* It is assumed that the section of bytes contains a repeating sequence of a field name, followed
* by a value.
**/
//===================================================================
public static Wrapper decodeField(byte[] source,int offset,int length,String fieldName,Wrapper dest)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	if (dest == null) dest = new Wrapper();
	//dest.zero();
	if (findField(source,offset,length,fieldName,range) == null) return null;
	decode(source,range.first,range.last-range.first+1,dest);
	return dest;
}
//===================================================================
public static Object decodeObjectField(byte[] source,int offset,int length,String fieldName,Object dest)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	if (findField(source,offset,length,fieldName,range) == null) return null;
	return decodeObject(source,range.first,range.last-range.first+1,dest);
}
//===================================================================
public static Object decodeObjectField(ByteArray source,String fieldName)
throws StreamCorruptedException, ClassNotFoundException
//===================================================================
{
	return decodeObjectField(source.data,0,source.length,fieldName,null);
}
//===================================================================
public static String decodeStringField(byte[] source,int offset,int length,String fieldName)
throws StreamCorruptedException
//===================================================================
{
	if (findField(source,offset,length,fieldName,range) == null) return null;
	wrapper.zero();
	decodeString(source,range.first,range.last-range.first+1,wrapper);
	return (String)wrapper.getObject();
}
//===================================================================
public static String decodeStringField(ByteArray source,String fieldName)
throws StreamCorruptedException
//===================================================================
{
	return decodeStringField(source.data,0,source.length,fieldName);
}
//##################################################################
}
//##################################################################

