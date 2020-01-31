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
//##################################################################
public class Field implements Member{
//##################################################################
// This is used by the VM. Do not move.
protected int field;
public int wrapperType;
public String fullType;

protected String fieldName;
public Reflect reflect;
java.lang.reflect.Field theField;
public static Wrapper wrapper = new Wrapper();

//-------------------------------------------------------------------
protected boolean fromField(java.lang.reflect.Field f)
//-------------------------------------------------------------------
{
	try{
		fieldName = f.getName();
		theField = f;
		//if (!Modifier.isPublic(theField.getDeclaringClass().getModifiers())) return false;
		fullType = Reflect.toFullType(theField.getType());
		wrapperType = fullType.charAt(0);
		return true;
	}catch(Throwable t){
		return false;
	}
}
//===================================================================
Field(Reflect from)
//===================================================================
{
	reflect = from;
}
//===================================================================
public String getName() {return fieldName;}
public String toString() {return theField.toString();}
//===================================================================

//===================================================================
public Wrapper getValue(Object from,Wrapper dest)
//===================================================================
{
	if (!Modifier.isPublic(getModifiers())) {
		ewe.sys.Vm.debug(fieldName+" is not public!");
		return null;
	}
	if (dest == null) dest = new Wrapper();
	return nativeGetValue(from,dest);
}
//===================================================================
public void setValue(Object to,Wrapper value)
//===================================================================
{
	if (!Modifier.isPublic(getModifiers()))  throw new RuntimeException("Field is not public: "+fieldName);
	if (value == null) throw new NullPointerException();
	if (!value.isCompatibleWith(wrapperType)) throw new RuntimeException("Bad wrapper type. Should be: "+(char)wrapperType+", is: "+(char)value.getType());
	nativeSetValue(to,value);
}

//===================================================================
public void copyValue(Object from,Object to)
//===================================================================
{
	setValue(to,getValue(from,wrapper));
}
//-------------------------------------------------------------------
private Wrapper nativeGetValue(Object from,Wrapper dest)
//-------------------------------------------------------------------
{
	try{
		Class c = theField.getType();
		if (c.equals(Boolean.TYPE)) dest.setBoolean(theField.getBoolean(from));
		else if (c.equals(Byte.TYPE)) dest.setByte(theField.getByte(from));
		else if (c.equals(Character.TYPE)) dest.setChar(theField.getChar(from));
		else if (c.equals(Short.TYPE)) dest.setShort(theField.getShort(from));
		else if (c.equals(Integer.TYPE)) dest.setInt(theField.getInt(from));
		else if (c.equals(Float.TYPE)) dest.setFloat(theField.getFloat(from));
		else if (c.equals(Double.TYPE)) dest.setDouble(theField.getDouble(from));
		else if (c.equals(Long.TYPE)) dest.setLong(theField.getLong(from));
		else if (c.isArray()) dest.setArray(theField.get(from));
		else dest.setObject(theField.get(from));
		return dest;
	}catch(Exception e){
		e.printStackTrace();
		return new Wrapper();
		//return null;
	}
}
//-------------------------------------------------------------------
private Wrapper nativeSetValue(Object from,Wrapper dest)
//-------------------------------------------------------------------
{
	try{
		Class c = theField.getType();
		if (c.equals(Boolean.TYPE)) theField.setBoolean(from,dest.getBoolean());
		else if (c.equals(Byte.TYPE)) theField.setByte(from,dest.getByte());
		else if (c.equals(Character.TYPE)) theField.setChar(from,dest.getChar());
		else if (c.equals(Short.TYPE)) theField.setShort(from,dest.getShort());
		else if (c.equals(Integer.TYPE)) theField.setInt(from,dest.getInt());
		else if (c.equals(Float.TYPE)) theField.setFloat(from,dest.getFloat());
		else if (c.equals(Double.TYPE)) theField.setDouble(from,dest.getDouble());
		else if (c.equals(Long.TYPE)) theField.setLong(from,dest.getLong());
		else if (c.isArray()) theField.set(from,dest.getArray());
		else theField.set(from,dest.getObject());
		return dest;
	}catch(IllegalArgumentException e){
		throw e;
	}catch(IllegalAccessException e){
		throw new IllegalArgumentException(e.toString());
	}
}
//===================================================================
public void makeWrapperCompatible(Wrapper w)
//===================================================================
{
	w.type = wrapperType;
}
public Class getDeclaringClass() {return theField.getDeclaringClass();}
public int getModifiers() {return theField.getModifiers();}
public String getType() {return fullType;}
/**
 * Return the type of the field as a Class.
 */
//===================================================================
public Class getFieldType() {return Reflect.typeToClass(fullType);}
//===================================================================
//===================================================================
public java.lang.reflect.Field toJavaField()
//===================================================================
{
	return theField;
}

//##################################################################
}
//##################################################################
