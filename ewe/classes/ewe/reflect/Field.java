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
/**
* This is used to access static and instance fields in objects.
**/
//##################################################################
public class Field implements Member{
//##################################################################
//..................................................................
// These two are used by the VM. Do not move.
private int _field_; // This holds a WClassField * value - Do not use this field.
public int wrapperType;
public String fullType;
protected int modifiers;
protected Class declaringClass;
public Reflect reflect;
protected String fieldName;
//......................................................
// You may alter from here.
//......................................................
 public static Wrapper wrapper = new Wrapper();
//
private WeakReference lastRead, lastWrote;

//===================================================================
Field(Reflect from)
//===================================================================
{
	reflect = from;
}
//===================================================================
public String getName() {return fieldName;}
//===================================================================
public String toString() //{return getName();}
//===================================================================
{
	return Modifier.toString(getModifiers())+" "+getFieldType().getName()+" "+getDeclaringClass().getName()+"."+getName();
}
/**
 * Return the type of the field as a Java encoded type string.
 */
//===================================================================
public String getType() {return fullType;}
//===================================================================
/**
 * Return the type of the field as a Class.
 */
//===================================================================
public Class getFieldType() {return Reflect.typeToClass(fullType);}
//===================================================================

/**
 * Get the value of the field.
 * @param from The object to retrieve the field value from, or null for static fields.
 * @param dest An optional destination wrapper value. If it is null then a new one will be
	created and returned.
 * @return A wrapper containing the value in the Field. If this returns null then the Field
	was not public.
 */
//===================================================================
public Wrapper getValue(Object from,Wrapper dest)
//===================================================================
{
	if (from == null || lastRead == null || lastRead.get() != from){
		int m = modifiers;
		if (!Modifier.isPublic(m)) return null;
		if (!Modifier.isStatic(m) && from == null) return null;
		if (from != null && !Reflect.isTypeOf(from.getClass().getName(),getDeclaringClass().getName())){
			//ewe.sys.Vm.debug(this+" - "+from.getClass().getName()+" - "+getDeclaringClass().getName());
			throw new IllegalArgumentException("Object "+from.getClass().getName()+" is not of correct class for field: "+this);
		}
		lastRead = new WeakReference(from);
	}
	if (dest == null) dest = new Wrapper();
	dest.type = wrapperType;
	return nativeGetValue(from,dest);
}
/**
 * Set the value of the field. This throws a RuntimeException is the field is not public.
 * @param to The object to set the field value to, or null for static fields.
 * @param value The value to set.
 */
//===================================================================
public void setValue(Object to,Wrapper value)
//===================================================================
{
	if (to == null || lastWrote == null || lastWrote.get() != to){
		int m = modifiers;
		if (to != null && !Reflect.isTypeOf(to.getClass().getName(),getDeclaringClass().getName()))
			throw new IllegalArgumentException("Object is not of correct class.");
		if (!Modifier.isPublic(m))  throw new RuntimeException("Field is not public: "+fieldName);
		if (!Modifier.isStatic(m) && to == null) throw new NullPointerException();
		lastWrote = new WeakReference(to);
	}
	if (value == null) throw new NullPointerException();
	if (!value.isCompatibleWith(wrapperType)) throw new RuntimeException("Bad wrapper type. Should be: "+(char)wrapperType+", is: "+(char)value.getType());
	if (wrapperType == '[' || wrapperType == 'L'){
		Object toStore = value.getObject();
		if (toStore != null){
			Class c = toStore.getClass();
			String vt = Reflect.getType(c);
			if (!Reflect.isTypeOf(vt,fullType))
				throw new IllegalArgumentException(vt+" is not of type: "+fullType);
		}
	}
	nativeSetValue(to,value);
}
//===================================================================
public void copyValue(Object from,Object to)
//===================================================================
{
	setValue(to,getValue(from,wrapper));
}

//===================================================================
private native Wrapper nativeGetValue(Object from,Wrapper dest);
private native Wrapper nativeSetValue(Object to,Wrapper value);
//===================================================================

//===================================================================
public void makeWrapperCompatible(Wrapper w)
//===================================================================
{
	w.type = wrapperType;
}

//===================================================================
public int getModifiers() {return modifiers;}
//===================================================================

//===================================================================
public Class getDeclaringClass() {return declaringClass;}
//===================================================================

//-------------------------------------------------------------------
private java.lang.reflect.Field field;
//-------------------------------------------------------------------

//===================================================================
public java.lang.reflect.Field toJavaField()
//===================================================================
{
	if (field == null) field = new java.lang.reflect.Field(this);
	return field;
}
//##################################################################
}
//##################################################################

