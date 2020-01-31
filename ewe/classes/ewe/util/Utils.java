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
package ewe.util;
import ewe.reflect.*;
import ewe.sys.Handle;
/**
* This class provides a variety of programmer utility functions.
**/
//##################################################################
public class Utils{
//##################################################################
/**
* This attempts to compare two objects which may or may not implement
* the Comparable interface. If they do, then it is used, otherwise the equals
* method is used.
* @param one The first object.
* @param two The second object
* @return less than 1 if one is considered less than two, greater than 1 if one is
* considered greater than two, 0 if they are considered equal.
*/
//===================================================================
public static int compare(Object one,Object two)
//===================================================================
{
	if (one == two) return 0;
	if (one == null) return -1;
	else if (two == null) return 1;
	else if (one instanceof Comparable) return ((Comparable)one).compareTo(two);
	else if (two instanceof Comparable) return -((Comparable)two).compareTo(one);
	else return one.equals(two) ? 0 : 1;
}
/**
* This attempts to get a copy of an object. If the object implements Copyable then
* a copy of that object is returned, otherwise the object itself is returned.
* @param toCopy The object to copy.
*/
//===================================================================
public static Object getCopy(Object toCopy)
//===================================================================
{
		if (toCopy instanceof Copyable) return ((Copyable)toCopy).getCopy();
		return toCopy;
}
/**
* This copies one field from the source object to the destination object using the
* provided Reflect Object as the class specifier. If the
* field value being copied implements the Copyable interface, then getCopy() is called
* on the Object and the copy is assigned to the destination field. Otherwise the destination
* field is made equal to the source field.
* <p>
* In order for this to work the Class AND the Field must be public.
* @param field The field name.
* @param source The source object.
* @param dest The destination object.
* @param theClass A Reflect object representing the exact class which is having its field copied.
*/
//===================================================================
public static void copyField(String field,Object source,Object dest,Reflect theClass)
//===================================================================
{
	Field f = theClass.getField(field,Reflect.DECLARED);
	Wrapper ret = f.getValue(source,f.wrapper);
	if (f.wrapperType == Wrapper.OBJECT){
		Object got = ret.getObject();
		if ((got instanceof Copyable) && got != null){
			f.setValue(dest,ret.setObject(((Copyable)got).getCopy()));
			return;
		}
	}
	f.setValue(dest,ret);
}
/**
* This copies data from the source to the destination. Copying is done as
* follows. The class of the source is determined using the Reflection API. If
* that class DECLARES a "_fields" String variable (which must be public to
* work under Java) then a copyField() is done on each of the fields listed in
* the "_fields" variable. The value of this variable should be a comma separated
* list of fields. This process is repeated for each successive superclass of
* the source object.
**/
//===================================================================
public static void copy(Object source,Object dest)
//===================================================================
{
	if (source == null || dest == null) return;
	Vector fields = new Vector();
	for (Reflect r = Reflect.getForObject(source); r != null; r = r.superClass()){
		String s = ewe.data.LiveObject.getFieldList(r,source,true);
		/*
		Field f = r.getField("_fields",Reflect.DECLARED);
		if (f == null) continue;
		String s = (String)f.getValue(source,f.wrapper).getObject();
		*/
		if (s.length() == 0) continue;
		fields.clear();
		mString.split(s,',',fields);
		int l = fields.size();
		for (int i = 0; i<l; i++)
			copyField((String)fields.get(i),source,dest,r);
	}
}
/**
* This puts a sequence of integers into a int array.
* @param sequence The destination for the integers.
* @param destOffset The index in the destination to start placing the values.
* @param first The first value.
* @param step The value to add to each value to get the next value.
* @param length The number of values to place.
*/
//===================================================================
public static native void getIntSequence(int[] sequence,int destOffset,int first,int step,int length);
//===================================================================
/*
{
	for (int i = first, n = 0; n < length; n++){
		sequence[destOffset+n] = i;
		i += step;
	}
}
*/
/**
* This fills the int array with an increasing sequence of integers starting with first. This calls:
* getIntSequence(sequence,0,first,1,sequence.length);
**/
//===================================================================
public static void getIntSequence(int [] sequence,int first)
//===================================================================
{
	getIntSequence(sequence,0,first,1,sequence.length);
}
/**
* This sorts a set of integers in place using the provided CompareInts object.
* @param values the sequence of integers to sort.
* @param length The length of the sequence.
* @param comparer This compares two integer values together. Note that this does not imply a
* numerical comparison. The integers may represent indexes into an object array or they may
* represent locations for data in a file. Therefore comparing the two integers may involve
* comparing the data they refer to rather than their numeric values.
* @param descending Set this true if you want to sort in descending order.
* Note that this is a blocking call - while this sort is going on all other threads are stopped.
*/
//===================================================================
public static native void sort(int[] values,int length,CompareInts comparer,boolean descending);
//===================================================================
//-------------------------------------------------------------------
protected static boolean merge(Handle h,int [] source,int sourceLength,int one,int two,int length,int [] dest,boolean descending,CompareInts comparer)
//-------------------------------------------------------------------
{
	int o = one, t = two, d = one;
	int omax = one+length, tmax = two+length;
	if (omax > sourceLength) omax = sourceLength;
	if (tmax > sourceLength) tmax = sourceLength;

	while(true) {
		ewe.sys.mThread.yield(250);
		if (h != null && h.shouldStop) return false;
		if (o >= omax) {
			if (t >= tmax) return true;
			dest[d++] = source[t++];
		}else {
			if (t >= tmax) dest[d++] = source[o++];
			else {
				int c = 0;
				if (comparer != null) c = comparer.compare(source[o],source[t]);
				else if (source[o] < source[t]) c = -1;
				else if (source[o] > source[t]) c = +1;
				if (((c <= 0) && !descending) || ((c > 0) && descending))
				 dest[d++] = source[o++];
				else dest[d++] = source[t++];
			}
		}
	}
}
/**
* This sorts the array in place.
* If the handle is not null then the sort will
* yield occasionally to let other threads operate. The handle will be updated to
* show the progress but the handle Handle.Stopped bit will never be set.
* If this returns false this indicates that stop() was called on the handle.
*/
//===================================================================
public static boolean sort(Handle h,int[] what,int length,CompareInts comparer,boolean descending)
//===================================================================
{
	if (h == null) {
		sort(what,length,comparer,descending);
		return true;
	}
	if (length <= 0) return true;
	int len = length;
	int needPasses = 0;
	int l2 = len;
	do{
		needPasses++;
		l2 >>= 1;
	}while(l2 != 0);

	int [] source = what, dest = new int[len], temp;
	int mergeLength = 1, passes = 0;
	//if (h != null){
		h.resetTime("Sorting");
	//}
	while(true) {
		int mergesDid = 0, one = 0, two = 0;
		while(true) {
			if (one >= len) break;
			two = one+mergeLength;
			if ((two >= len) && (mergesDid == 0)) break;
			mergesDid++;
			if (!merge(h,source,length,one,two,mergeLength,dest,descending,comparer))
				return false;
			one += mergeLength*2;
		}
		if (mergesDid == 0) break;
		temp = dest; dest = source; source = temp;
		mergeLength *= 2;
		passes++;
		//if (h != null) {
			h.progress = (float)(((double)passes)/(needPasses));
			h.changed();
		//}
	}
	if (source != what) ewe.sys.Vm.copyArray(source,0,what,0,len);
	return true;
}
/**
* This sorts the objects in place.
* If the handle is not null then the sort will
* yield occasionally to let other threads operate. The handle will be updated to
* show the progress but the handle Handle.Stopped bit will never be set.
**/
//===================================================================
public static boolean sort(Handle h,Object [] values,Comparer comparer,boolean descending)
//===================================================================
{
	int [] vals = new int[values.length];
	for (int i = 0; i<vals.length; i++) vals[i] = i;
	if (h == null) sort(vals,vals.length,new CompareArrayElements(values,comparer),descending);
	else
		if (!sort(h,vals,vals.length,new CompareArrayElements(values,comparer),descending))
			return false;
	Object [] newVals = new Object[vals.length];
	for (int i = 0; i<vals.length; i++) newVals[i] = values[vals[i]];
	ewe.sys.Vm.copyArray(newVals,0,values,0,values.length);
	return true;
}
/**
* This sorts the objects in place. If the Comparer is null then it is assumed that all
* of the objects implements Comparable and so are able to compare themselves.
* @param values The Object values to sort.
* @param comparer An object that will compare the Objects.
* @param descending Set this true if you want to sort in descending order.
* Note that this is a blocking call - while this sort is going on all other threads are stopped.
*/
//===================================================================
public static void sort(Object[] values,Comparer comparer,boolean descending)
//===================================================================
{
	sort(null,values,comparer,descending);
}
/**
* This writes an integer value to a byte array with the specified number
* of bytes. It always writes out the high byte first.
* @param val The byte, short or integer value to write.
* @param dest The destination array.
* @param offset The location in the destination to write to.
* @param numBytes The number of bytes 1, 2 or 4 to write out.
*/
public static final void
	//============================================================
	writeInt(int val,byte[] dest,int offset,int numBytes)// throws IOException
//============================================================
{
	for (int i = offset+numBytes-1; i>=offset; i--) {
		dest[i] = (byte)(val & 0xff);
		val = val>>8;
	}
}
/**
* This reads an integer value from a byte array with the specified number
* of bytes. It always reads in the high byte first.
* @param source The source of the bytes.
* @param offset The location in the source.
* @param numBytes The number of bytes 1, 2 or 4 to read in.
* @return the integer value read in.
*/
public static final native int
//============================================================
	readInt(byte[] source,int offset,int numBytes);
//============================================================
/**
* This reads a long value from a byte array as an 8 byte sequence - high byte first.
* @param source The source of the bytes.
* @param offset The location in the source.
* @return The long value read in.
*/
//===================================================================
public static final long readLong(byte[] source,int offset)
//===================================================================
{
	long val = (long)readInt(source,offset,4) << 32;
	val |= (readInt(source,offset+4,4) & 0x0ffffffffL);
	return val;
}
/**
* This writes a long value to a byte array as an 8 byte sequence - high byte first.
* @param value The long value to write.
* @param dest The destination for the bytes.
* @param offset The location in the destination to write to.
*/
//===================================================================
public static final void writeLong(long value,byte[] dest,int offset)
//===================================================================
{
	writeInt((int)((value >> 32) & 0x0ffffffffL),dest,offset,4);
	writeInt((int)(value & 0x0ffffffffL),dest,offset+4,4);
}
/*
{
	int ret = 0;
	for (int i = offset; i<offset+numBytes; i++) {
		ret = (ret<<8) & 0xffffff00;
		ret |= ((int)source[i])&0xff;
	}
	return ret;
}
*/
/**
* Returns the number of characters needed to store a string which has been
* encoded in the Java UTF8 format.
* @param data the encoded data.
* @param start the start location in the data.
* @param numberOfBytes the number of bytes of encoded data.
* @return the number of characters for the String.
*/
public static native final int
//===================================================================
	sizeofJavaUtf8String(byte[] data,int start,int numberOfBytes);
//===================================================================
/*
{
	int size = 0;
	for (int i = 0; i<numberOfBytes; i++){
		size++;
		byte c = data[i+start];
		if ((c & 0x80) == 0) continue;
		else if ((c & 0xe0) == 0xc0) i++;
		else if ((c & 0xf0) == 0xe0) i+=2;
	}
	return size;
}
*/
/**
* Returns the number of bytes needed to encode a String in the Java UTF8 format.
* @param toEncode The characters to encode.
* @param start The start index of the character.
* @param length The number of characters to encode.
* @return the number of bytes needed to encode a String in the Java UTF8 format.
*/
public static native final int
//===================================================================
	sizeofJavaUtf8String(char[] toEncode,int start,int length);
//===================================================================
/*
{
	if (toEncode == null) return 0;
	int max = length;
	int size = 0;
	for (int i = 0; i<max; i++){
		size++;
		char c = toEncode[i+start];
		if (c >= 0x1 && c <= 0x7f) continue;
		else if (c == 0 || (c >= 0x80 && c <= 0x7ff)) size++;
		else size += 2;
	}
	return size;
}
*/

/**
 * Convert a Java Utf8 byte encoded string into a string of characters.
 * @param data The encoded data bytes.
 * @param start The start index of the data bytes.
 * @param numberOfBytes The number of bytes of encoded data.
 * @param chars The destination array.
 * @param offset The start index of the destination to place the characters.
 * @return The destination array.
 */
public static native final char []
//===================================================================
decodeJavaUtf8String(byte[] data,int start,int numberOfBytes,char[] chars,int offset);
//===================================================================
/*
{
	int size = sizeofJavaUtf8String(data,start,numberOfBytes);
	if (buffer != null)
		if (buffer.length < size) buffer = null;
	if (buffer == null) buffer = new char[size];
	int i = 0, t = offset;
	for (i = 0; i<numberOfBytes; i++){
		byte c = data[i+start];
		if ((c & 0x80) == 0)
			buffer[t] = (char)c;
		else if ((c & 0xe0) == 0xc0) {
			buffer[t] = (char)((((char)c & 0x1f)<<6) + ((char)data[i+start+1] & 0x3f));
			i++;
		}else if ((c & 0xf0) == 0xe0) {
			buffer[t] = (char)((((char)c & 0x0f)<<12) + (((char)data[i+start+1] & 0x3f)<<6)+((char)data[i+start+2] & 0x3f));
			i += 2;
		}
		t++;
	}
	return buffer;
}
*/
/**
 * Encode a String of characters a Java Utf8 encoded bytes.
 * @param toEncode The characters to encode.
 * @param offset The start index of the character.
 * @param length The number of characters to encode.
 * @param destination The destination for the bytes.
 * @param destOffset The offset for the destination bytes
 * @return the number of bytes used to encode the String.
 */
public static native int
//===================================================================
encodeJavaUtf8String(char[] toEncode,int offset,int length,byte[] destination,int destOffset);
//===================================================================
/*
{
	if (toEncode == null) return 0;
	int max = length;
	int size = destOffset;
	for (int i = 0; i<max; i++){
		char c = toEncode[i+offset];
		if (c >= 0x1 && c <= 0x7f) destination[size++] = (byte)c;
		else if (c == 0 || (c >= 0x80 && c <= 0x7ff)) {
			destination[size++] = (byte)(0xc0 | ((c >> 6) & 0x1f));
			destination[size++] = (byte) (0x80 | (c & 0x3f));
		}else{
			destination[size++] = (byte)(0xe0 | ((c >> 12) & 0xf));
			destination[size++] = (byte) (0x80 | ((c >> 6) & 0x3f));
			destination[size++] = (byte) (0x80 | (c  & 0x3f));
		}
	}
	return size-destOffset;
}
*/
/**
 * Encodes a String as Java Utf8 bytes.
 * @param str the String to encode.
 * @return the encoded bytes.
 */
//===================================================================
public static byte [] encodeJavaUtf8String(String str)
//===================================================================
{
	char [] chars = ewe.sys.Vm.getStringChars(str);
	int size = sizeofJavaUtf8String(chars,0,chars.length);
	byte [] got = new byte[size];
	encodeJavaUtf8String(chars,0,chars.length,got,0);
	return got;
}
/**
 * Decode a String encoded as Java Utf8 bytes.
 * @param bytes The encoded bytes.
 * @param start The start index of the encoded bytes.
 * @param length The number of encoded bytes.
 * @return The decoded String.
 */
//===================================================================
public static String decodeJavaUtf8String(byte[] bytes,int start,int length)
//===================================================================
{
	int size = sizeofJavaUtf8String(bytes,start,length);
	char [] got = new char [size];
	decodeJavaUtf8String(bytes,start,length,got,0);
	return new String(got);
}

/**
 * Encode the array of primitive Java values as a String. The length of the array is not encoded
 * along with this data, nor is the type of the data. You will have to store these yourself.
 * <p>See the TextEncoder/TextDecoder objects for how to do this.
 * @param obj An array of any Java type except Object or array.
 * @return The String encoded data.
 */
//===================================================================
public static String primitiveArrayToString(Object obj)
//===================================================================
{
	byte [] toSave = null;
	if (obj instanceof byte []){
		byte [] a = (byte [])obj;
		toSave = new byte[a.length];
		for (int i = 0; i<a.length; i++) toSave[i] = a[i];
	}else if (obj instanceof boolean []){
		boolean [] a = (boolean [])obj;
		toSave = new byte[a.length];
		for (int i = 0; i<a.length; i++) toSave[i] = a[i] ? (byte)1 : (byte)0;
	}else if (obj instanceof short []){
		short [] a = (short [])obj;
		toSave = new byte[a.length*2];
		for (int i = 0; i<a.length; i++)
			writeInt(a[i],toSave,i*2,2);
	}else if (obj instanceof char []){
		char [] a = (char [])obj;
		toSave = new byte[a.length*2];
		for (int i = 0; i<a.length; i++)
			writeInt(a[i],toSave,i*2,2);
	}else if (obj instanceof int []){
		int [] a = (int [])obj;
		toSave = new byte[a.length*4];
		for (int i = 0; i<a.length; i++)
			writeInt(a[i],toSave,i*4,4);
	}else if (obj instanceof long []){
		long [] a = (long [])obj;
		toSave = new byte[a.length*8];
		for (int i = 0; i<a.length; i++)
			writeLong(a[i],toSave,i*8);
	}else if (obj instanceof float []){
		float [] a = (float [])obj;
		toSave = new byte[a.length*8];
		for (int i = 0; i<a.length; i++)
			writeLong(ewe.sys.Convert.toLongBitwise(a[i]),toSave,i*8);
	}else if (obj instanceof double []){
		double [] a = (double [])obj;
		toSave = new byte[a.length*8];
		for (int i = 0; i<a.length; i++)
			writeLong(ewe.sys.Convert.toLongBitwise(a[i]),toSave,i*8);
	}
	if (toSave == null) return "";
	char [] ret = new char[toSave.length*2];
	for (int i = 0; i<toSave.length; i++){
		ret[i*2] = (char)(((toSave[i] >> 4) & 0xf)+'0');
		if (ret[i*2] > '9') ret[i*2] -= '9'-('A'-1);
		ret[i*2+1] = (char)(((toSave[i]) & 0xf)+'0');
		if (ret[i*2+1] > '9') ret[i*2+1] -= '9'-('A'-1);
	}
	return new String(ret);
}
/**
 * Decode an array of primitive values, encoded as a String.
 * @param obj The array of the correct type and length.
 * @param data The encoded data.
 */
//===================================================================
public static void primitiveArrayFromString(Object obj,String data)
//===================================================================
{
	char [] from = ewe.sys.Vm.getStringChars(data);
	byte [] toSave = new byte[from.length/2];
	for (int i = 0; i<toSave.length; i++){
		int one = 0; char c = from[i*2];
		if (c <= '9') one = c-'0';
		else if (c <= 'F') one = c-'A'+10;
		else if (c <= 'f') one = c-'a'+10;
		int two = 0; c = from[i*2+1];
		if (c <= '9') two = c-'0';
		else if (c <= 'F') two = c-'A'+10;
		else if (c <= 'f') two = c-'a'+10;
		toSave[i] = (byte)(one << 4 | two);
	}
	if (obj instanceof byte []){
		byte [] a = (byte [])obj;
		ewe.sys.Vm.copyArray(toSave,0,a,0,toSave.length);
	}else if (obj instanceof boolean []){
		boolean [] a = (boolean [])obj;
		for (int i = 0; i<a.length; i++) a[i] = toSave[i] == 0 ? false : true;
	}else if (obj instanceof short []){
		short [] a = (short [])obj;
		for (int i = 0; i<a.length; i++)
			a[i] = (short)readInt(toSave,i*2,2);
	}else if (obj instanceof char []){
		char [] a = (char [])obj;
		for (int i = 0; i<a.length; i++)
			a[i] = (char)readInt(toSave,i*2,2);
	}else if (obj instanceof int []){
		int [] a = (int [])obj;
		for (int i = 0; i<a.length; i++)
			a[i] = readInt(toSave,i*4,4);
	}else if (obj instanceof long []){
		long [] a = (long [])obj;
		for (int i = 0; i<a.length; i++)
			a[i] = readLong(toSave,i*8);
	}else if (obj instanceof float []){
		float [] a = (float [])obj;
		for (int i = 0; i<a.length; i++)
			a[i] = (float)ewe.sys.Convert.toDoubleBitwise(readLong(toSave,i*8));
	}else if (obj instanceof double []){
		double [] a = (double [])obj;
		for (int i = 0; i<a.length; i++)
			a[i] = ewe.sys.Convert.toDoubleBitwise(readLong(toSave,i*8));
	}
}
/**
Find the index of a value within an array. The value should be converted to an appropriate
integer/long value and passed to the method. The type of the array will determine the how
the lookingFor value will be used.<p>
Searching is done within the range of index from minIndex up to maxIndex-1.
If backwards is true searching goes from maxIndex-1 to minIndex (inclusive).
Otherwise searching goes from minIndex to maxIndex-1.
A return value of -1 indicates that the value was not found.
**/
public static native int indexOf(Object array, long lookingFor, int minIndex, int maxIndex, boolean backwards);
/*
{
	int i;
	int len = ewe.reflect.Array.getLength(array);
	//
	if (len == -1) throw new IllegalArgumentException();
	if (maxIndex > len) maxIndex = len;
	//
	if (backwards){
		if (minIndex >= maxIndex) minIndex = maxIndex-1;
		if (minIndex < 0) return -1;
	}else{
		if (minIndex < 0) minIndex = 0;
		if (minIndex >= maxIndex) return -1;
	}
	//
	if (array instanceof int[]){
		int[] s = (int[])array;
		int look = (int)lookingFor;
		if (backwards) for (i = maxIndex-1; i >= minIndex; i--) if (s[i] == look) return i;
		else for (i = minIndex; i < maxIndex; i++) if (s[i] == look) return i;
	}
	return -1;
}
*/
/**
* Find the index of a CRLF sequence or LF character in byte data.
* @param data The byte data.
* @param start The start index of the data.
* @param length The number of data bytes.
* @return the index of the CRLF sequence or LF character.
*/
//===================================================================
public static native int findCRLF(byte[] data,int start,int length);
//===================================================================
/**
* Creates a hashCode from a sequence of bytes.
**/
//===================================================================
public static native int makeHashCode(byte [] data,int start,int length);
//===================================================================
/**
* Creates a hashCode from a sequence of chars.
**/
//===================================================================
public static native int makeHashCode(char [] data,int start,int length);
//===================================================================

//-------------------------------------------------------------------
 static Field [] fieldsFor(Object obj)
//-------------------------------------------------------------------
{
	if (obj == null) return null;
	Reflect r = Reflect.getForObject(obj);
	Field [] allFields = null;
	Field f = r.getField("_fields",Reflect.PUBLIC);
	if (f != null) {
		String [] s = mString.split((String)f.getValue(obj,f.wrapper).getObject(),',');
		allFields = new Field[s.length];
		for (int i = 0; i<s.length; i++)
			allFields[i] = r.getField(s[i],Reflect.PUBLIC);
	}else{
		allFields = r.getFields(Reflect.PUBLIC);
	}
	return allFields;
}
/**
 * Encode the fields of the Encodable object.
 * @param obj The object to encode.
 * @return A simple encoded string for the object.
 */
//===================================================================
public static String textEncode(Encodable obj)
//===================================================================
{
	Field [] allFields = fieldsFor(obj);
	if (allFields == null) return null;
	TextEncoder te = new TextEncoder();
	for (int i = 0; i<allFields.length; i++){
		if (allFields[i] == null) continue;
		if (Modifier.isStatic(allFields[i].getModifiers())) continue;
		char c = allFields[i].getType().charAt(0);
		String toSave = null;
		if (c == '[' || c == 'L'){
			TextEncoder t2 = new TextEncoder();
			TextEncoder.saveFullObject(allFields[i].getValue(obj,null).getObject(),t2);
			toSave = t2.toString();
		}else{
			toSave = TextEncoder.toString(allFields[i],obj);
		}
		te.addValue(allFields[i].getName(),toSave);
	}
	return te.toString();
}
/**
 * Encode the fields of the Encodable object.
 * @param obj The object to encode.
 * @return A simple encoded string for the object.
 */
//===================================================================
public static void textDecode(Encodable obj,String data)
//===================================================================
{
	Field [] allFields = fieldsFor(obj);
	if (allFields == null) return;
	TextDecoder td = new TextDecoder(data);
	for (int i = 0; i<allFields.length; i++){
		if (allFields[i] == null) continue;
		if (Modifier.isStatic(allFields[i].getModifiers())) continue;
		char c = allFields[i].getType().charAt(0);
		String s = td.getValue(allFields[i].getName());
		if (s != null)
			if (c == '[' || c == 'L'){
				allFields[i].setValue(obj,new Wrapper().setObject(TextEncoder.getFullObject(s,obj.getClass())));
			}else{
				TextEncoder.fromString(allFields[i],obj,s);
			}
	}
}
/**
 * Get a sub-array containing a set of elements from an existing array.
 * @param original The original array.
 * @param start The first element to copy.
 * @param length The number of elements to copy.
 * @return a new array containing the selected elements.
 */
//===================================================================
public static Object subArray(Object original,int start,int length)
//===================================================================
{
	if (original == null) return null;
	Reflect r = Reflect.getForObject(original);
	if (!r.isArray()) throw new IllegalArgumentException("Parameter is not an array.");
	if (start < 0 || start+length > Reflect.arrayLength(original)) throw new IndexOutOfBoundsException();
	Object got = Reflect.newArrayInstance(r.getComponentType(),length);
	if (length != 0) ewe.sys.Vm.copyArray(original,start,got,0,length);
	return got;
}
/**
 * Join two arrays together to form a new array which contains the elements of the first followed by the elements of the second.
 * @param original The first array. Can be null, in which case only the elements of toAdd will go into the new array.
 * @param toAppend The second array. Can be null, in which case only the elements of original will go into the new array.
 * @return a new array representing original + toAppend. If both original and toAppend are null, this will return null.
 */
//===================================================================
public static Object appendArray(Object original,Object toAppend)
//===================================================================
{
	if (original == null && toAppend == null) return null;
	Reflect r = Reflect.getForObject(original == null ? toAppend : original);
	if (!r.isArray()) throw new IllegalArgumentException("Only arrays can be appended.");
	int oldLen = original == null ? 0 : Reflect.arrayLength(original),
		newLen = toAppend == null ? 0 : Reflect.arrayLength(toAppend);
	Object got = Reflect.newArrayInstance(r.getComponentType(),newLen+oldLen);
	if (oldLen != 0) ewe.sys.Vm.copyArray(original,0,got,0,oldLen);
	if (newLen != 0) ewe.sys.Vm.copyArray(toAppend,0,got,oldLen,newLen);
	return got;
}
//-------------------------------------------------------------------
static native void nativeZero(Object array, int offset, int length);
//-------------------------------------------------------------------
//===================================================================
public static void zeroArrayRegion(Object array, int offset, int length)
//===================================================================
{
	Class cType = array.getClass().getComponentType();
	if (cType == null) throw new IllegalArgumentException();
	//Do this in the native one.
	//if (offset < 0 || offset+length > ewe.reflect.Array.getLength(array)) throw new ArrayIndexOutOfBoundsException();
	if (length == 0) return;
	//try{
		nativeZero(array,offset,length);
		//return;
	//}catch(Throwable t){
		//t.printStackTra
	//}
	int max = offset+length;
	if (array instanceof byte[]){
		byte[] a = (byte[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof boolean[]){
		boolean[] a = (boolean[])array;
		for (int i = offset; i<max; i++) a[i] = false;
	}else if (array instanceof char[]){
		char[] a = (char[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof short[]){
		short[] a = (short[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof int[]){
		int[] a = (int[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof float[]){
		float[] a = (float[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof double[]){
		double[] a = (double[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof long[]){
		long[] a = (long[])array;
		for (int i = offset; i<max; i++) a[i] = 0;
	}else if (array instanceof Object[]){
		Object[] a = (Object[])array;
		for (int i = offset; i<max; i++) a[i] = null;
	}
}
/*
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	int all[] = new int[] {10,11,12,13,14,15,16,17,18,19,20};
	int [] sub = (int [])subArray(all,0,all.length+1);
	for (int i = 0; i<sub.length; i++) ewe.sys.Vm.debug(""+sub[i]);
	ewe.sys.Vm.debug("---");

	ewe.fx.Rect r = new ewe.fx.Rect(123,456,789,987);
	ewe.sys.Vm.debug(textEncode(r));
	ewe.sys.Vm.sleep(5000);
	ewe.sys.Vm.exit(0);
}
*/
//===================================================================
public static String fileLengthDisplay(int len)
//===================================================================
{
	String add = "";
	if (len > 9999) {
		len/=1024;
		add = "K";
	}
	if (len > 9999) {
		len/=1024;
		add = "M";
	}
	return len+add;
}

//##################################################################
}
//##################################################################

