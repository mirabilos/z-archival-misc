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

//##################################################################
public class TextEncoder{
//##################################################################
public char [] buffer = new char[0];
public int cur = 0;
public int entries = 0;

private static TextEncoder encoder = new TextEncoder();

//===================================================================
public void clear()
//===================================================================
{
	cur = 0;
	entries = 0;
}
//-------------------------------------------------------------------
protected int expand()
//-------------------------------------------------------------------
{
	int bl = buffer.length;
	char [] nb = new char[bl+1000];
	ewe.sys.Vm.copyArray(buffer,0,nb,0,bl);
	buffer = nb;
	bl += 1000;
	return bl;
}
public final static String hex = "0123456789ABCDEF";

//-------------------------------------------------------------------
protected void addValue(String name,char [] value,int start,int length)
//-------------------------------------------------------------------
{
	int d = cur;
	int bl = buffer.length;

	if (entries != 0) {
		if (d >= bl) bl = expand();
		buffer[d++] = '&';
	}
	char [] nc = ewe.sys.Vm.getStringChars(name);
	int nl = nc.length;
	for (int i = 0; i<nl; i++) {
		char c = nc[i];
		if (c == ' ') c = '+';
		else if (c < ' ' || c == '+' || c == '&' || c == '%' || c == '=' || c == '|' || c == '{' || c == '}'){
			while (d+2 >= bl) bl = expand();
			buffer[d++] = '%';
			buffer[d++] = hex.charAt((c >> 4) & 0xf);
			buffer[d++] = hex.charAt(c & 0xf);
			continue;
		}
		if (d >= bl) bl = expand();
		buffer[d++] = c;
	}
	if (d >= bl) bl = expand();
	buffer[d++] = '=';

	int ve = start+length;
	for (int i = start; i<ve; i++) {
		char c = value[i];
		if (c == ' ') c = '+';
		else if (c < ' ' || c == '+' || c == '%' || c == '&' || c == '=' || c == '|' || c == '{' || c == '}'){
			while (d+2 >= bl) bl = expand();
			buffer[d++] = '%';
			buffer[d++] = hex.charAt((c >> 4) & 0xf);
			buffer[d++] = hex.charAt(c & 0xf);
			continue;
		}
		if (d >= bl) bl = expand();
		buffer[d++] = c;
	}
	entries++;
	cur = d;
}

//===================================================================
public void addValue(String name,String value)
//===================================================================
{
	char [] v = value.toCharArray();
	addValue(name,v,0,v.length);
}
//===================================================================
public SubString toSubString(SubString dest)
//===================================================================
{
	if (dest == null) dest = new SubString();
	dest.set(buffer,0,cur);
	return dest;
}
//===================================================================
public String toString() {return new String(buffer,0,cur);}
//===================================================================

//===================================================================
public static Object getObject(String type,String encoded,Class requestor)
//===================================================================
{
	//ewe.sys.Vm.debug(type+" = "+encoded);
	if (type == null) return encoded;
	if (type.length() == 0)
 		if (encoded.length() != 0) return encoded;
		else return null;
	if (type.equals("java/lang/String")) return encoded;
	Reflect r = Reflect.getForName(type,requestor);
	if (r == null) return null;
	else{
		if (r.isArray()) return arrayFromText(encoded,requestor);
		Object vobj = r.newInstance();
		if (vobj != null){
			fromString(vobj,encoded,requestor);
		}else{
			//ewe.sys.Vm.debug("Could not create new instance!");
		}
		return vobj;
	}
}
//===================================================================
public static Object arrayFromText(String txt,Class requestor)
//===================================================================
{
	TextDecoder td = new TextDecoder(txt);
	String sz = td.getValue("<size>");
	if (sz == null) return null;
	int len = ewe.sys.Convert.toInt(sz);
	String type = td.getValue("<type>");
	if (type == null) return null;
	Object array = Reflect.newArrayInstance(type,len);
	char t = type.charAt(0);
	if (t == 'L' || t == '['){
		Object [] dest = (Object [])array;
		int d = 0;
		for (int i = 0; i<td.size(); i++){
			String nm = td.getName(i);
			String v = td.getValue(i);
			if (nm.length() == 0) dest[d++] = getObject(nm,td.getValue(i),requestor);
			else if (nm.charAt(0) == '<') continue;
			else dest[d++] = getObject(nm,td.getValue(i),requestor);
		}
	}else{
		ewe.util.Utils.primitiveArrayFromString(array,td.getValue("<value>"));
	}
	return array;
}

/**
* This returns an encoded string as:<p>
* <className>=<objectValue><p>
* If the object is null then just "=" will be encoded.<p>
* If the class is a string then className will be empty EXCEPT if the String is empty,
* in which case the className will be "java/lang/String";
**/
//===================================================================
public static void saveFullObject(Object obj,TextEncoder te)
//===================================================================
{
	if (obj == null) te.addValue("","");
	else{
		String className = Reflect.getForObject(obj).getClassName();
		String data = toString(obj);
		if (obj instanceof String)
			if (((String)obj).length() != 0)
				className = "";
		te.addValue(className,data);
	}
}

//===================================================================
public static Object getFullObject(String typeAndData,Class requestor)
//===================================================================
{
	//ewe.sys.Vm.debug("typeAndData: "+typeAndData);
	TextDecoder td  = new TextDecoder(typeAndData);
	return getObject(td.getName(0),td.getValue(0),requestor);
}

public static String
//==================================================================
	toString(Object obj)
//==================================================================
{
	if (obj == null) return "";
	else if (Reflect.isArray(obj)){
		TextEncoder te = new TextEncoder();
		te.addValue("<size>",""+Reflect.arrayLength(obj));
		Reflect r = Reflect.getForObject(obj);
		te.addValue("<type>",r.getComponentType());
		int type = r.getComponentType().charAt(0);
		if (type == '[' || type == 'L'){
			Object [] array = (Object [])obj;
			for (int i = 0; i<array.length; i++)
				saveFullObject(array[i],te);
		}else{
			te.addValue("<value>",ewe.util.Utils.primitiveArrayToString(obj));
		}
		return te.toString();
	}else if (obj instanceof String) return (String)obj;
	else if (obj instanceof ewe.data.LiveData){
		ewe.data.LiveData ld = (ewe.data.LiveData)obj;
		return ld.textEncode();
/*
	}else if (isOfType(c,"mJava.PropertyList") && obj != null){
		PropertyList pl = (PropertyList)obj;
		return pl.encodeAsText();
*/
	}else if (obj instanceof Textable) {
		return ((Textable)obj).getText();
	}else if (obj instanceof Vector){
		TextEncoder te = new TextEncoder();
		Vector v = (Vector)obj;
		for (int i = 0; i<v.size(); i++)
			saveFullObject(v.get(i),te);
		return te.toString();
	}else if (obj instanceof Encodable){
		return Utils.textEncode((Encodable)obj);
	}else
		return obj.toString();
}
public static String
//==================================================================
	toString(Field f,Object o)
//==================================================================
{
	Wrapper w = f.getValue(o,f.wrapper);
	if (w.getType() == w.OBJECT || w.getType() == w.ARRAY)
		return toString(w.getObject());
	else
		return w.toString();
}

public static void fromString(Object obj,String val)
{
	if (obj == null) return;
	fromString(obj,val,obj.getClass());
}

public static void
//==================================================================
	fromString(Object obj,String val,Class requestor)
//==================================================================
{
	if (obj == null) return;
	else if (obj instanceof ewe.data.LiveData){
		ewe.data.LiveData ld = (ewe.data.LiveData)obj;
		ld.textDecode(val);
		return;
/*
	}else if (isOfType(c,"mJava.PropertyList")){
		PropertyList pl = (PropertyList)obj;
		pl.decodeAsText(val);
		return;
*/
	}else if (obj instanceof Textable){
		((Textable)obj).setText(val);
		return;
	}else if (obj instanceof Vector){
		Vector v = (Vector)obj;
		v.clear();
		TextDecoder td = new TextDecoder(val);
		for (int i = 0; i<td.size(); i++){
			v.add(getObject(td.getName(i),td.getValue(i),requestor));
		}
	}else if (obj instanceof Stringable){
		((Stringable)obj).fromString(val);
		return;
	}else if (obj instanceof Encodable){
		Utils.textDecode((Encodable)obj,val);
		return;
	}
}
public static String [] textables = {
	"Lewe/data/LiveData;","Lewe/util/Vector;","Lewe/util/Textable;","Lewe/util/Stringable;","Lewe/util/Encodable;"
};

public static void
//==================================================================
	fromString(Field f,Object o,String val)
//==================================================================
{
	if (o == null) return;
	if (f.wrapperType == Wrapper.OBJECT){
		if (f.fullType.equals("Ljava/lang/String;")) f.setValue(o,f.wrapper.setObject(val));
		else for (int i = 0; i<textables.length; i++){
			if (Reflect.isTypeOf(f.fullType,textables[i])){
				Object obj = f.getValue(o,f.wrapper).getObject();
				if (obj == null){
					Reflect r = Reflect.getForName(f.fullType,o.getClass());
					if (r != null){
						obj = r.newInstance();
						if (obj != null) f.setValue(o,f.wrapper.setObject(obj));
					}
				}
				if (obj != null) fromString(obj,val,o.getClass());
				return;
			}
		}
	}else if (f.wrapperType == Wrapper.ARRAY){
		f.setValue(o,f.wrapper.setObject(arrayFromText(val,o.getClass())));
	}else{
		f.makeWrapperCompatible(f.wrapper);
		f.wrapper.fromString(val);
		f.setValue(o,f.wrapper);
	}
}

//===================================================================
public static char [] toText(long value,int numBytes,char [] dest,int offset)
//===================================================================
{
	if (dest == null) {
		dest = new char[numBytes*2];
		offset = 0;
	}
	for (int i = numBytes*2-1; i >= 0; i--){
		int v = (int)(value & 0xf);
		if (v < 10) dest[offset+i] = (char)('0'+v);
		else dest[offset+i] = (char)('A'+v-10);
		value >>= 4;
	}
	return dest;
}
//===================================================================
public static char [] toText(double value, char [] dest,int offset)
//===================================================================
{
	return toText(ewe.sys.Convert.toLongBitwise(value),8,dest,offset);
}
//===================================================================
public static long longFromText(int numBytes,char [] from,int offset)
//===================================================================
{
	if (from == null) return 0;
	long value = 0;
	for (int i = 0; i < numBytes*2; i++){
		char c = from[offset+i];
		int v = c-'0';
		if (c >= 'a' && c <= 'f') v = 10+c-'a';
		else if (c >= 'A' && c <= 'F') v = 10+c-'A';
		value <<= 4;
		value |= (v & 0xf);
	}
	return value;
}

//===================================================================
public static double doubleFromText(char [] from,int offset)
//===================================================================
{
	long got = longFromText(8,from,offset);
	return ewe.sys.Convert.toDoubleBitwise(got);
}
//===================================================================
public static String toText(String typeName,Object value)
//===================================================================
{
	char [] ret = null;
	ewe.sys.Long ln = ewe.sys.Long.l1;
	if (value instanceof ewe.sys.Double)
		ln.set(ewe.sys.Convert.toLongBitwise(((ewe.sys.Double)value).value));
	else
		if (value instanceof ewe.sys.Long) ln = (ewe.sys.Long)value;

	switch(typeName.charAt(0)){
		case 'Z':
		case 'B': ret = toText(ln.value,1,null,0); break;
		case 'C':
		case 'S': ret = toText(ln.value,2,null,0); break;
		case 'I': ret = toText(ln.value,4,null,0); break;
		case 'D':
		case 'F':
		case 'J': ret = toText(ln.value,8,null,0); break;
		case '[':
		case 'L': return toString(value);
	}
	if (ret != null) return new String(ret);
	return null;
}

//===================================================================
public static Wrapper toWrapper(String typeName,String actualClass,String from,Class requestor)
//===================================================================
{
	return toWrapper(typeName,actualClass,ewe.sys.Vm.getStringChars(from),0,from.length(),requestor);
}
//===================================================================
public static Wrapper toWrapper(String typeName,String actualClass,char [] from,int offset,int length,Class requestor)
//===================================================================
{
	Wrapper ret = new Wrapper();
	switch(typeName.charAt(0)){
		case 'Z':	return ret.setBoolean(longFromText(1,from,offset) != 0);
		case 'B': return ret.setByte((byte)longFromText(1,from,offset));
		case 'C': return ret.setChar((char)longFromText(2,from,offset));
		case 'S': return ret.setShort((short)longFromText(2,from,offset));
		case 'I': return ret.setInt((int)longFromText(4,from,offset));
		case 'J': return ret.setLong(longFromText(8,from,offset));
		case 'D':	return ret.setDouble(doubleFromText(from,offset));
		case 'F': return ret.setFloat((float)doubleFromText(from,offset));
		case '[': return ret.setObject(arrayFromText(new String(from,offset,length),requestor));
		case 'L': {
			Reflect r = Reflect.getForName(actualClass,requestor);
			if (r == null) return ret.setObject(null);
			else {
				String enc = new String(from,offset,length);
				if (actualClass.equals("java/lang/String"))
					return ret.setObject(enc);
				Object obj = r.newInstance();
				if (obj != null)
					fromString(obj,enc,requestor);
				return ret.setObject(obj);
			}
		}
	}
	return ret;
}
//===================================================================
public static String toText(Wrapper value)
//===================================================================
{
	TextEncoder te = new TextEncoder();
	if (value.getType() == 'L' || value.getType() == '['){
		Object tr = value.getObject();
		Reflect r = tr == null ? null : Reflect.getForObject(tr);
		if (r == null) te.addValue("","");
		else te.addValue(r.getClassName(),toString(value.getObject()));
	}else{
		switch(value.getType()){
			case 'Z': te.addValue("",new String(toText(value.getBoolean() ? 1 : 0,1,null,0))); break;
			case 'B': te.addValue("",new String(toText(value.getByte(),1,null,0))); break;
			case 'C': te.addValue("",new String(toText(value.getChar(),2,null,0))); break;
			case 'S': te.addValue("",new String(toText(value.getShort(),2,null,0))); break;
			case 'I': te.addValue("",new String(toText(value.getInt(),4,null,0))); break;
			case 'D': te.addValue("",new String(toText(value.getDouble(),null,0))); break;
			case 'F': te.addValue("",new String(toText(value.getFloat(),null,0))); break;
			case 'J': te.addValue("",new String(toText(value.getLong(),8,null,0))); break;
		}
	}
	return te.toString();
}
/*
//===================================================================
public static Object fromText(String typeName,Object value0
//===================================================================
{
	char [] ret = null;
	ewe.sys.Long ln = ewe.sys.Long.l1;
	if (value instanceof ewe.sys.Double)
		ln.set(ewe.sys.Convert.toLongBitwise(((ewe.sys.Double)value).value));
	else
		if (value instanceof ewe.sys.Long) ln = (ewe.sys.Long)value;

	switch(typeName.charAt(0)){
		case 'Z':
		case 'B': ret = toText(ln.value,1,null,0); break;
		case 'C':
		case 'S': ret = toText(ln.value,2,null,0); break;
		case 'I': ret = toText(ln.value,4,null,0); break;
		case 'D':
		case 'F':
		case 'J': ret = toText(ln.value,8,null,0); break;
		case 'L': return toString(value);
	}
}
*/
/*
//===================================================================
public String fromText(String typeName,Object value)
//===================================================================
{
	char [] ret = null;
	ewe.sys.Long ln = ewe.sys.Long.l1;
	if (value instanceof ewe.sys.Double)
		ln.set(ewe.sys.Convert.toLongBitwise(((ewe.sys.Double)value).value));
	else
		if (value instanceof ewe.sys.Long) ln = (ewe.sys.Long)value;

	switch(typeName.charAt(0)){
		case 'Z':
		case 'B': ret = toText(ln.value,1,null,0); break;
		case 'C':
		case 'S': ret = toText(ln.value,2,null,0); break;
		case 'I': ret = toText(ln.value,4,null,0); break;
		case 'D':
		case 'F':
		case 'J': ret = toText(ln.value,8,null,0); break;
	}
	if (ret != null) return new String(ret);
	return null;
}
*/
//##################################################################
}
//##################################################################

