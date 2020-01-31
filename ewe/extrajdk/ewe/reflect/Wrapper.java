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
package ewe.reflect;

import java.lang.reflect.Field;

//##################################################################
public class Wrapper implements DataConverter,ewe.util.Stringable{
//##################################################################
//These seven are used by the native VM - do not move.
protected int type;
protected int value;
protected float floatValue;
protected Object objValue;
protected int refValue;
protected long longValue;
protected double doubleValue;
public static final int NONE = 0;
public static final int VOID = 'V';
public static final int BYTE = 'B';
public static final int CHAR = 'C';
public static final int SHORT = 'S';
public static final int INT = 'I';
public static final int BOOLEAN = 'Z';
public static final int FLOAT = 'F';
public static final int LONG = 'J';
public static final int DOUBLE = 'D';

public static final int OBJECT = 'L';
public static final int ARRAY = '[';
public static final int VMREFERENCE = '?';

protected static final String stringClass = "Ljava/lang/String;";
protected static final String objectClass = "Ljava/lang/Object;";
//protected static final String doubleClass = "Lewe/sys/Double;";
//protected static final String longClass = "Lewe/sys/Long;";
protected static final String valueClass = "Lewe/sys/Value;";
public static Wrapper [] noParameter = new Wrapper[0];
//===================================================================
Wrapper setType(String typeName)
//===================================================================
{
	if (typeName == null) return this;
	if (typeName.length() == 0) return this;
	return setType(typeName.charAt(0));
}

//-------------------------------------------------------------------
protected Wrapper setType(int type)
//-------------------------------------------------------------------
{
	this.type = type;
	return this;
}
//===================================================================
private Wrapper setInteger(int v,int type)
//===================================================================
{
	value = v;
	return setType(type);
}
//===================================================================
private Wrapper setFloat(float v,int type)
//===================================================================
{
	floatValue = v;
	return setType(type);
}
//===================================================================
private Wrapper setObject(Object obj,int type)
//===================================================================
{
	objValue = obj;
	return setType(type);
}
//===================================================================
public Wrapper setLong(long v)
//===================================================================
{
	longValue = v;
	return setType(LONG);
}
//===================================================================
public Wrapper setDouble(double d)
//===================================================================
{
	doubleValue = d;
	return setType(DOUBLE);
}

public Wrapper setByte(byte v){return setInteger(v,BYTE);}
public Wrapper setChar(char v) {return setInteger(v,CHAR);}
public Wrapper setShort(short v) {return setInteger(v,SHORT);}
public Wrapper setInt(int v){return setInteger(v,INT);}
public Wrapper setBoolean(boolean v) {return setInteger(v ? 1 : 0,BOOLEAN);}
public Wrapper setFloat(float v) {return setFloat(v,FLOAT);}
public Wrapper setObject(Object v) {return setObject(v,OBJECT);}
public Wrapper setArray(Object v) {return setObject(v,ARRAY);}
//public native Wrapper setLong();
//public native Wrapper setDouble();
//===================================================================
public Wrapper getCopy()
//===================================================================
{
	Wrapper wr = new Wrapper();
	wr.type = type;
	wr.value = value;
	wr.objValue = objValue;
	wr.floatValue = floatValue;
	wr.refValue = refValue;
	wr.longValue = longValue;
	wr.doubleValue = doubleValue;
	return wr;
}
//===================================================================
public int getType() {return type;}
//===================================================================
private static String badError = "Bad data type";
private static String typeError = badError+" - should be: ";
public int getInt() {if (type != INT) throw new RuntimeException(typeError+"int"); return value;}
public short getShort() {if (type != SHORT) throw new RuntimeException(typeError+"short"); return (short)value;}
public char getChar() {if (type != CHAR) throw new RuntimeException(typeError+"char"); return (char)value;}
public byte getByte() {if (type != BYTE) throw new RuntimeException(typeError+"byte"); return (byte)value;}
public boolean getBoolean() {if (type != BOOLEAN) throw new RuntimeException(typeError+"boolean"); return value == 0 ? false:true;}
public float getFloat() {if (type != FLOAT) throw new RuntimeException(typeError+"float"); return floatValue;}
public Object getObject() {if (type != OBJECT && type != ARRAY) throw new RuntimeException(typeError+"Object"); return objValue;}
public Object getArray() {if (type != ARRAY && type != OBJECT) throw new RuntimeException(typeError+"Array"); return objValue;}
public long getLong() {if (type != LONG) throw new RuntimeException(typeError+"long"); return longValue;}
public double getDouble() {if (type != DOUBLE) throw new RuntimeException(typeError+"double"); return doubleValue;}
//===================================================================

//===================================================================
public byte toByte()
//===================================================================
{
	if (type == BYTE) return (byte)value;
	throw new RuntimeException(badError);
}
//===================================================================
public short toShort()
//===================================================================
{
	if (type == SHORT || type == BYTE) return (short)value;
	throw new RuntimeException(badError);
}
//===================================================================
public int toInt()
//===================================================================
{
	if (type == INT || type == SHORT || type == BYTE || type == CHAR) return value;
	throw new RuntimeException(badError);
}
//===================================================================
public long toLong()
//===================================================================
{
	if (type == LONG) return longValue;
	if (type == INT || type == SHORT || type == BYTE || type == CHAR) return (long)value;
	throw new RuntimeException(badError);
}
//===================================================================
public char toChar()
//===================================================================
{
	if (type == CHAR) return (char)value;
	throw new RuntimeException(badError);
}
//===================================================================
public float toFloat()
//===================================================================
{
	if (type == FLOAT) return (float)floatValue;
	throw new RuntimeException(badError);
}
//===================================================================
public double toDouble()
//===================================================================
{
	if (type == DOUBLE) return doubleValue;
	if (type == FLOAT) return (double)floatValue;
	throw new RuntimeException(badError);
}
//===================================================================
public boolean toBoolean()
//===================================================================
{
	if (type == BOOLEAN) return value == 0 ? false : true;
	throw new RuntimeException(badError);
}
//===================================================================
public void copyValue(Wrapper from)
//===================================================================
{
	value = from.value;
	objValue = from.objValue;
	floatValue = from.floatValue;
	refValue = from.refValue;
	doubleValue = from.doubleValue;
	longValue = from.longValue;
}
//===================================================================
public void zero()
//===================================================================
{
	value = 0;
	objValue = null;
	floatValue = 0;
	refValue = 0;
	doubleValue = 0;
	longValue = 0;
}
public Wrapper zero(int type)
{
	zero();
	setType(type);
	return this;
}

//===================================================================
public static boolean doConvertData(Wrapper source,String sourceType,Wrapper dest,String destType)
//===================================================================
{
	dest.setType(destType);
	if (sourceType.equals(destType)){
		dest.copyValue(source);
		return true;
	}
	//......................................................
	// Converting from a string into...
	//......................................................
	if (sourceType.equals(Wrapper.stringClass)){
		String str = (String)source.objValue;
		char c = destType.charAt(0);
		if (c != OBJECT && c != ARRAY)
			dest.fromString(str);
		else if (Reflect.isTypeOf(destType,valueClass))
		//else if (destType.equals(doubleClass) || destType.equals(longClass))
			dest.fromString(str);
		else
			return false;
	//......................................................
	// Converting to a string from
	//......................................................
	}else if (destType.equals(Wrapper.stringClass)){
		char c = sourceType.charAt(0);
		if (c != OBJECT && c != ARRAY)
			dest.objValue = source.toString();
		else if (Reflect.isTypeOf(sourceType,valueClass))
		//else if (sourceType.equals(doubleClass) || sourceType.equals(longClass))
			dest.objValue = source.toString();
		else
			return false;
	}
	return true;
}
//===================================================================
public boolean convertData(Wrapper source,String sourceType,Wrapper dest,String destType)
//===================================================================
{
	return doConvertData(source,sourceType,dest,destType);
}

/**
 * Convert this Wrapper to a standard Java wrapper Object (e.g. java.lang.Integer, java.lang.Boolean)
 */
//===================================================================
public Object toJavaWrapper()
//===================================================================
{
	switch(type){
		case INT: return new Integer(value);
		case BYTE: return new Byte((byte)value);
		case CHAR: return new Character((char)value);
		case SHORT: return new Short((short)value);
		case LONG: return new Long((long)longValue);
		case FLOAT: return new Float((float)floatValue);
		case DOUBLE: return new Double((double)doubleValue);
		case BOOLEAN: return value == 0 ? Boolean.FALSE : Boolean.TRUE;
		default:
			return objValue;
	}
}
/**
 * Set the value of this Wrapper to be equal to a standard Java wrapper Object (e.g. java.lang.Integer, java.lang.Boolean)
 */
//===================================================================
public void fromJavaWrapper(Object ret)
//===================================================================
{
	fromJavaWrapper(ret,0);
}
/**
 * Set the value of this Wrapper to be equal to a standard Java wrapper Object (e.g. java.lang.Integer, java.lang.Boolean)
 */
//-------------------------------------------------------------------
void fromJavaWrapper(Object ret,int ty)
//-------------------------------------------------------------------
{
		if (ty == 0){
			if (ret instanceof Integer) ty = INT;
			else if (ret instanceof Byte) ty = BYTE;
			else if (ret instanceof Character) ty = CHAR;
			else if (ret instanceof Short) ty = SHORT;
			else if (ret instanceof Long) ty = LONG;
			else if (ret instanceof Float) ty = FLOAT;
			else if (ret instanceof Double) ty = DOUBLE;
			else if (ret instanceof Boolean) ty = BOOLEAN;
			else if (ret != null && ret.getClass().isArray()) ty = ARRAY;
			else ty = OBJECT;
		}
		type = ty;
		switch(type){
		case INT: value = ((Integer)ret).intValue(); break;
		case BYTE: value = ((Byte)ret).byteValue(); break;
		case CHAR: value = ((Character)ret).charValue(); break;
		case SHORT: value = ((Short)ret).shortValue(); break;
		case LONG: longValue = ((Long)ret).longValue(); break;
		case FLOAT: floatValue = ((Float)ret).floatValue(); break;
		case DOUBLE: doubleValue = ((Double)ret).doubleValue(); break;
		case BOOLEAN: value = ((Boolean)ret).booleanValue() ? 1 : 0; break;
		default:
			objValue = ret;
	}
}
/**
 * Convert an array of Ewe Wrappers to an array of standard Java wrapper Objects.
 * @param wrappers the array of Ewe Wrappers.
 * @return an array of Java wrapper objects.
 */
//===================================================================
public static Object [] toJavaWrappers(Wrapper [] wrappers)
//===================================================================
{
	Object [] j = new Object[wrappers.length];
	for (int i = 0; i<j.length; i++)
		j[i] = wrappers[i].toJavaWrapper();
	return j;
}
/**
 * Convert an array of standard Java wrapper objects to an array of Ewe Wrappers.
 * @param javaWrappers the array of Java wrapper objects.
 * @return an array of Ewe Wrappers.
 */
//===================================================================
public static Wrapper [] toEweWrappers(Object [] javaWrappers)
//===================================================================
{
	if (javaWrappers instanceof Wrapper[]) return (Wrapper[])javaWrappers;
	Wrapper [] w = new Wrapper[javaWrappers.length];
	for (int i = 0; i<w.length; i++){
		w[i] = new Wrapper();
		w[i].fromJavaWrapper(javaWrappers[i]);
	}
	return w;
}

//===================================================================
public String toString()
//===================================================================
{
	switch(type){
		case LONG: return ""+longValue;
		case DOUBLE: return ""+doubleValue;
		case INT: case BYTE: case SHORT: return ""+value;
		case BOOLEAN: return value == 0 ? "false" : "true";
		case CHAR: return ""+(char)value;
		case FLOAT: return ""+floatValue;
		case OBJECT:
		case ARRAY:
			return ewe.util.mString.toString(objValue);
		default: return "";
	}
}
//===================================================================
public void fromString(String what)
//===================================================================
{
	if (what == null) return;
	if (what.length() == 0) return;
	switch(type){
		case DOUBLE: doubleValue = ewe.sys.Convert.toDouble(what); break;
		case LONG: longValue = ewe.sys.Convert.toLong(what); break;
		case INT: case BYTE: case SHORT:  value = ewe.sys.Convert.toInt(what); break;
		case BOOLEAN: value = ewe.sys.Convert.toBoolean(what) ? 1 : 0; break;
		case CHAR: value = ewe.sys.Convert.toChar(what); break;
		case FLOAT: floatValue = ewe.sys.Convert.toFloat(what); break;
		case OBJECT:
			{
			if (objValue instanceof ewe.sys.Value)
				((ewe.sys.Value)objValue).fromString(what);
			}
	}
}
//===================================================================
public boolean isCompatibleWith(int aType)
//===================================================================
{
	if (aType == type) return true;
	if ((aType == OBJECT || aType == ARRAY) && (type == OBJECT || type == ARRAY))
		return true;
	return aType == type;
}
/**
 * Widen a Java wrapper object to a specified primitive type. For example a Short wrapper
 * will be widened to an Integer if the type class represents the primitive int type.
 * @param value The Java wrapper object to widen.
 * @param type the target primitive Java type.
 * @return possibly a new Java wrapper object with the same value.
 * @exception IllegalArgumentException if the value is incompatible with the type.
 */
//===================================================================
public static Object widenJavaWrapper(Object value,Class type) throws IllegalArgumentException
//===================================================================
{
	boolean t = false;
	if (!type.isPrimitive()) return value;
	else if (value == null) t = true;
	else if (type == Boolean.TYPE){
		if (!(value instanceof Boolean)) t = true;
	}else if (type == Character.TYPE){
		if (!(value instanceof Character)) t = true;
	}else if (type == Byte.TYPE){
		if (!(value instanceof Byte)) t = true;
	}else if (type == Short.TYPE){
		if (value instanceof Byte) value = new Short(((Byte)value).shortValue());
		else if (!(value instanceof Short)) t = true;
	}else if (type == Integer.TYPE){
		if (value instanceof Byte) value = new Integer(((Byte)value).intValue());
		else if (value instanceof Short) value = new Integer(((Short)value).intValue());
		else if (value instanceof Character) value = new Integer(((Character)value).charValue());
		else if (!(value instanceof Integer)) t = true;
	}else if (type == Long.TYPE){
		if (value instanceof Byte) value = new Long(((Byte)value).longValue());
		else if (value instanceof Short) value = new Long(((Short)value).longValue());
		else if (value instanceof Character) value = new Long(((Character)value).charValue());
		else if (value instanceof Integer) value = new Long(((Integer)value).longValue());
		else if (!(value instanceof Long)) t = true;
	}else if (type == Float.TYPE){
		if (value instanceof Byte) value = new Float(((Byte)value).floatValue());
		else if (value instanceof Short) value = new Float(((Short)value).floatValue());
		else if (value instanceof Character) value = new Float(((Character)value).charValue());
		else if (value instanceof Integer) value = new Float(((Integer)value).floatValue());
		else if (value instanceof Long) value = new Float(((Long)value).floatValue());
		else if (!(value instanceof Float)) t = true;
	}else if (type == Double.TYPE){
		if (value instanceof Byte) value = new Double(((Byte)value).doubleValue());
		else if (value instanceof Short) value = new Double(((Short)value).doubleValue());
		else if (value instanceof Character) value = new Double(((Character)value).charValue());
		else if (value instanceof Integer) value = new Double(((Integer)value).doubleValue());
		else if (value instanceof Long) value = new Double(((Long)value).doubleValue());
		else if (value instanceof Float) value = new Double(((Float)value).doubleValue());
		else if (!(value instanceof Double)) t = true;
	}
	if (t) throw new IllegalArgumentException();
	return value;
}
/**
Put the value stored in this Wrapper in the specified field in the specified object.
@param f The field to store the data in.
@param dest The destination object.
@return true if successful, false if not successful for any reason.
*/
//===================================================================
public boolean putInField(Field f,Object dest)
//===================================================================
{
	try{
		switch(type){
			case BOOLEAN: f.setBoolean(dest,longValue != 0); return true;
			case BYTE: f.setByte(dest,(byte)longValue); return true;
			case CHAR: f.setChar(dest,(char)longValue); return true;
			case SHORT: f.setShort(dest,(short)longValue); return true;
			case INT: f.setInt(dest,(int)longValue); return true;
			case LONG: f.setLong(dest,longValue); return true;
			case FLOAT: f.setFloat(dest,(float)doubleValue); return true;
			case DOUBLE: f.setDouble(dest,doubleValue); return true;
			case ARRAY:
			case OBJECT: f.set(dest,objValue); return true;
			default: return false;
		}
	}catch(Exception e){
		return false;
	}
}
/**
Get the value stored in the field f in the object source into this Wrapper.
@param f The field to get the data from.
@param source The source object.
@return true if successful, false if not successful for any reason.
*/
//===================================================================
public boolean getFromField(Field f,Object source)
//===================================================================
{
	zero(NONE);
	try{
		switch(Reflect.getWrapperType(f.getType())){
			case BOOLEAN: setBoolean(f.getBoolean(source)); return true;
			case BYTE: setByte(f.getByte(source)); return true;
			case CHAR: setChar(f.getChar(source)); return true;
			case SHORT: setShort(f.getShort(source)); return true;
			case INT: setInt(f.getInt(source)); return true;
			case LONG: setLong(f.getLong(source)); return true;
			case FLOAT: setFloat(f.getFloat(source)); return true;
			case DOUBLE: setDouble(f.getDouble(source)); return true;
			case ARRAY:
			case OBJECT: setObject(f.get(source)); return true;
			default: return false;
		}
	}catch(Exception e){
		return false;
	}
}
//##################################################################
}
//##################################################################
