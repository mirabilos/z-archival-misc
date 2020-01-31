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
package java.lang;

/**

Standard Java Class object. Supports all operations that do not use or return Objects
in the java.net or java.io libraries.

**/
import ewe.reflect.Reflect;
//##################################################################
public final class Class extends Object{
//##################################################################
/* 3 Native variables.................*/
Object nativeClass;
String typeName;
Object classLoaderForTarget;
/*....................................*/
Reflect myReflect;
//-------------------------------------------------------------------
static Class forPrimitive(char ch)
//-------------------------------------------------------------------
{
	Class c = new Class();
	c.typeName = ewe.sys.Convert.toString(ch);
	return c;
}
protected Class(){nativeClass = classLoaderForTarget = typeName = null;}
/**
* Return the name of the class in dot notation (e.g. "java.lang.Object")
**/
//===================================================================
public native String getName();
//===================================================================
//===================================================================
public String toString(){return (isInterface() ? "interface " : isPrimitive() ? "" : "class ")+getName();}
//===================================================================
/**
* Under Ewe this will either return a valid ClassLoader or null if the class
* was loaded by the normal bootstrap (VM) loader. Under other Java implementations
* it is allowed to return a value other than null for the bootstrap loader.
**/
//===================================================================
public native ClassLoader getClassLoader();
//===================================================================
/**
This attempts to load a Class given the class name. If the class is not accessable via the
system loader it will throw a ClassNotFoundException.<p>

To obtain a Class for an array using forName you must provide a name in the form: <b>"[Java_Type"</b>
where <b>Java_Type</b> is one of:<p>
'Z' - boolean <br>
'B' - byte <br>
'C' - char <br>
'S' - short <br>
'I' - int <br>
'J' - long <br>
'D' - double <br>
'F' - float <br>
"Lfull_class_name;" - class (e.g.: Ljava.lang.String)<br>
<p>
When specifying an array of Objects using "[Lfull_class_name;" if the class full_class_name could not
be found a ClassNotFoundException will be thrown.
<p>
To get a Class that represents a primitive type call forName() to get a Class representing the array
of the primitive type, and then call getComponentType() on that returned class. For example:<p>
<b>Class intClass = Class.forName("[I").getComponentType();</b>
<p>
**/
//===================================================================
public static native Class forName(String className) throws ClassNotFoundException;
//===================================================================
public boolean equals(Object other)
//===================================================================
{
	if (other instanceof Class) return (((Class)other).nativeClass == nativeClass);
	return super.equals(other);
}
/**
* Return the superclass of the represented class. If the class represented is an
* interface or the Object class, this will return null.
**/
//===================================================================
public native Class getSuperclass();
//===================================================================
/**
* Returns an array of interface implemented by this class. If this class represents
* an interface, it returns an array of interfaces extended by this interface. If the
* class implements or extends no interfaces, it will return an array of length zero.
**/
//===================================================================
public native Class [] getInterfaces();
//===================================================================
/**
* Returns true if this class represents an interface.
**/
//===================================================================
public native boolean isInterface();
//===================================================================
/**
* Returns true if the this class is a superclass or superinterface of the specified other class.
**/
//===================================================================
public boolean isAssignableFrom(Class other)
//===================================================================
{
	return ewe.reflect.Reflect.isTypeOf(other.typeName,typeName);
}
//===================================================================
//public native boolean nativeIsAssignableFrom(Class other);
//===================================================================
//===================================================================
//public native boolean isInstance(Object obj);
//===================================================================
/**
* This is the equivalent of the instanceof operator. Checks if the specified object is a derived
* class of this class, or implements the interface represented by this class.
**/
public boolean isInstance(Object obj)
{
	if (obj == null) return false;
	return ewe.reflect.Reflect.isTypeOf(Reflect.getForObject(obj).getClassName(),typeName);
}
/**
* Checks if this class represents a primitive type.
* @return true if this Class represents a primitive type.
*/
//===================================================================
public boolean isPrimitive()
//===================================================================
{
	if (nativeClass != null) return false;
	try{
		return (typeName.charAt(0) != '[');
	}catch(Exception e){
		return false;
	}
}
/**
* Returns true if the class represents an array.
**/
//===================================================================
public boolean isArray()
//===================================================================
{
	if (nativeClass != null) return false;
	try{
		return (typeName.charAt(0) == '[');
	}catch(Exception e){
		return false;
	}
}
/**
* Return a Class representing the component type IF this class represents an array.
**/
//===================================================================
public Class getComponentType()
//===================================================================
{
	try{
		if (!isArray()) return null;
		if (typeName.charAt(1) == 'L') return forName(typeName.replace('/','.').substring(2,typeName.length()-1));
		else if (typeName.charAt(1) == '[') return forName(typeName.substring(1));
		else {
			Class c = new Class();
			c.nativeClass = null;
			c.typeName = typeName.substring(1,2);
			return c;
		}
	}catch(Exception e){
		return null;
	}

}
//===================================================================
public int hashCode() {return getName().hashCode();}
//===================================================================
/**
* This currently always returns an array of zero length as signers are not currently
* supported.
**/
//===================================================================
public Object [] getSigners() {return new Object[0];}
//===================================================================
public native int getModifiers();
//===================================================================
/**
* This returns an array of Classes representing all the named inner classes declared by this
* class. It returns an empty array if the Class declares no classes.
**/
//===================================================================
public native Class [] getDeclaredClasses();
//===================================================================

//===================================================================
public Object newInstance() throws InstantiationException, IllegalAccessException
//===================================================================
{
	ewe.reflect.Reflect r = new ewe.reflect.Reflect(this);
	ewe.reflect.Constructor c = r.getConstructor("()V",ewe.reflect.Member.PUBLIC);
	if (c == null) throw new IllegalAccessException("No public default constructor available for "+this);
	Object got = c.newInstance(ewe.reflect.Wrapper.noParameter);
	if (got != null) return got;
	Throwable t = c.instantiationError;
	if (t instanceof IllegalAccessException) throw (IllegalAccessException)t;
	if (t instanceof InstantiationException) throw (InstantiationException)t;
	if (t instanceof RuntimeException) throw (RuntimeException)t;
	if (t instanceof Error) throw (Error)t;
	if (t == null) throw new InstantiationException("Unknown error");
	throw new InstantiationException(t.getMessage());
}

//-------------------------------------------------------------------
private java.lang.reflect.Field[] toFields(ewe.reflect.Field[] f)
//-------------------------------------------------------------------
{
	java.lang.reflect.Field[] ret = new java.lang.reflect.Field[f == null ? 0 : f.length];
	for (int i = 0; i<ret.length; i++)
		ret[i] = new java.lang.reflect.Field(f[i]);
	return ret;
}
//-------------------------------------------------------------------
private java.lang.reflect.Field toField(ewe.reflect.Field f,String name)
throws NoSuchFieldException
//-------------------------------------------------------------------
{
	if (f == null) throw new NoSuchFieldException(name);
	return new java.lang.reflect.Field(f);
}
//===================================================================
public java.lang.reflect.Field getField(String name)
	throws NoSuchFieldException, SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toField(myReflect.getField(name,myReflect.PUBLIC),name);
}
//===================================================================
public java.lang.reflect.Field getDeclaredField(String name)
	throws NoSuchFieldException, SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toField(myReflect.getField(name,myReflect.DECLARED),name);
}
//===================================================================
public java.lang.reflect.Field[] getFields()
	throws SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toFields(myReflect.getFields(myReflect.PUBLIC));
}
//===================================================================
public java.lang.reflect.Field[] getDeclaredFields()
	throws SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toFields(myReflect.getFields(myReflect.DECLARED));
}

//-------------------------------------------------------------------
private String parameterListToString(Class[] types)
//-------------------------------------------------------------------
{
	if (types == null || types.length == 0) return "";
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i<types.length; i++)
		sb.append(Reflect.getType(types[i]));
	return sb.toString();
}
//-------------------------------------------------------------------
private java.lang.reflect.Method toMethod(ewe.reflect.Method f,String name)
throws NoSuchMethodException
//-------------------------------------------------------------------
{
	if (f == null) throw new NoSuchMethodException(name);
	return new java.lang.reflect.Method(f);
}
//-------------------------------------------------------------------
private java.lang.reflect.Method[] toMethods(ewe.reflect.Method[] f)
//-------------------------------------------------------------------
{
	java.lang.reflect.Method[] ret = new java.lang.reflect.Method[f == null ? 0 : f.length];
	for (int i = 0; i<ret.length; i++)
		ret[i] = new java.lang.reflect.Method(f[i]);
	return ret;
}

//===================================================================
public java.lang.reflect.Method getMethod(String name, Class[] parameterTypes)
throws NoSuchMethodException, SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toMethod(myReflect.getMethod(name,"("+parameterListToString(parameterTypes)+")",myReflect.PUBLIC),name);
}
//===================================================================
public java.lang.reflect.Method[] getMethods()
throws SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toMethods(myReflect.getMethods(Reflect.PUBLIC));
}
//===================================================================
public java.lang.reflect.Method getDeclaredMethod(String name, Class[] parameterTypes)
throws NoSuchMethodException, SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toMethod(myReflect.getMethod(name,"("+parameterListToString(parameterTypes)+")",myReflect.DECLARED),name);
}
//===================================================================
public java.lang.reflect.Method[] getDeclaredMethods()
throws SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toMethods(myReflect.getMethods(Reflect.DECLARED));
}


//-------------------------------------------------------------------
private java.lang.reflect.Constructor toConstructor(ewe.reflect.Constructor f)
throws NoSuchMethodException
//-------------------------------------------------------------------
{
	if (f == null) throw new NoSuchMethodException();
	return new java.lang.reflect.Constructor(f);
}
//-------------------------------------------------------------------
private java.lang.reflect.Constructor[] toConstructors(ewe.reflect.Constructor[] f)
//-------------------------------------------------------------------
{
	java.lang.reflect.Constructor[] ret = new java.lang.reflect.Constructor[f == null ? 0 : f.length];
	for (int i = 0; i<ret.length; i++)
		ret[i] = new java.lang.reflect.Constructor(f[i]);
	return ret;
}

//===================================================================
public java.lang.reflect.Constructor getConstructor(Class[] parameterTypes)
throws NoSuchMethodException, SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toConstructor(myReflect.getConstructor("("+parameterListToString(parameterTypes)+")",myReflect.PUBLIC));
}
//===================================================================
public java.lang.reflect.Constructor[] getConstructors()
throws SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toConstructors(myReflect.getConstructors(Reflect.PUBLIC));
}
//===================================================================
public java.lang.reflect.Constructor getDeclaredConstructor(Class[] parameterTypes)
throws NoSuchMethodException, SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toConstructor(myReflect.getConstructor("("+parameterListToString(parameterTypes)+")",myReflect.DECLARED));
}
//===================================================================
public java.lang.reflect.Constructor[] getDeclaredConstructors()
throws SecurityException
//===================================================================
{
	if (myReflect == null) myReflect = new Reflect(this);
	return toConstructors(myReflect.getConstructors(Reflect.DECLARED));
}


//##################################################################
}
//##################################################################

