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
package ewe.applet;
import java.util.*;
import java.io.*;
//import java.net.*;
/**
* This is a class loader which does automatic caching
* and resolving. To use it you just need to override the
* getClassBytes() method.
**/
//############################################################
public class mClassLoader extends ClassLoader implements ClassInfoLoader{
//############################################################
protected static Hashtable cache = new Hashtable();
public static mClassLoader defaultLoader;

//==================================================================
// This will only work if it is not an applet.
//==================================================================
static {
	try{
		//defaultLoader = new mClassLoader();
	}catch(Throwable e){
		defaultLoader = null;
	}
}

/**
* This is a set of ClassInfoLoader objects. By default, the getClassBytes() method
* will go through each of these and request them to get the classInfo bytes for
* a particular class name. If they return with available bytes in a ClassInfo structure
* then these bytes are used to define the class.
**/
public Vector classInfoLoaders = new Vector();
/**
* This is the only thing you MAY need to override.
*/
//==================================================================
public boolean getClassBytes(ClassInfo ci)
//==================================================================
{
	//............................................................
	for(Enumeration e = classInfoLoaders.elements();e.hasMoreElements();) {
		ClassInfoLoader cur = (ClassInfoLoader)e.nextElement();
		if (cur.getClassBytes(ci)) return true;
		//ClassInfo nci = cur.getClassInfo(ci.fullName);
		//if (nci == null) continue;
		//if (nci.bytes == null) continue;
		//ci.bytes = nci.bytes;
		//return true;
	}
//............................................................
	return false;
}
//==================================================================
/**
* You can override this for things to do just before loading. It should
* return an object which will be passed to justLoaded. The object is only
* to be used by "justLoaded()" method. If it returns null loading will
* be aborted.
*/
//==================================================================
//protected Object prepareForLoad(ClassInfo ci) {return this;}
//==================================================================
/**
* You can override this for things to do just after loading. The parameter
* prep is the object returned by prepareForLoad().
*/
//==================================================================
//protected void justLoaded(ClassInfo ci,Object prep){}
//==================================================================

/**
*
*/
protected boolean
//------------------------------------------------------------
  tryDefineClass(ClassInfo ci)
//------------------------------------------------------------
{
	try {
		ci.theClass = defineClass(ci.fullName,ci.bytes,0,ci.bytes.length);
	}catch (Throwable t){
		//System.out.println("Failed: "+ci.fullName);
		ci.theClass = null;
		ci.error = t;
	}
	return (ci.theClass != null);
}
//------------------------------------------------------------------

/**
* This does the custom load, without looking for system classes or
* already loaded classes.
*/
public final ClassInfo
//============================================================
  doLoadClass(String name)
//============================================================
{
	//System.out.println("doLoadClass("+name+");");
	ClassInfo ci = new ClassInfo(name,this);
	boolean got = getClassBytes(ci);
	if (!got) return null;
//..................................................................
	try {
		got = tryDefineClass(ci);
	}catch(Throwable t){
		//t.printStackTrace();
		ci.error = t;
		got = false;
	}
	ci.bytes = null;
	if (!got) return null;
	return ci;
}
protected final Class
//------------------------------------------------------------------
	loadClass(String name,boolean resolve)
//------------------------------------------------------------------
{
	ClassInfo ci = loadClassInfo(name,resolve);
	if (ci == null) return null;
	return ci.theClass;
}
//------------------------------------------------------------------

public final ClassInfo
//==================================================================
	loadClassInfo(String name,boolean resolve)
//==================================================================
{
	ClassInfo ci = (ClassInfo)cache.get(name);
	if (ci != null) return ci;
	Class c = null;
//...................................................
	ci = new ClassInfo(name,this);
	try {
		c = findSystemClass(name);
		if (c != null) ci.isSystemClass = true;
	}catch(Exception e){}
	if (c == null) {
		try {
			c = findLoadedClass(name);
		}catch(Exception e){}
	}
	if (c != null) {
		ci.theClass = c;
		return ci;
	}
//...................................................
	ci = doLoadClass(name);
	if (ci == null) return null;
//..................................................................
	if (resolve) resolveClass(ci.theClass);
//..................................................................
	cache.put(name,ci);
	return ci;
}
//==================================================================

//==================================================================
public final ClassInfo getClassInfo(String name) {return loadClassInfo(name,true);}
//==================================================================

//------------------------------------------------------------------

//==================================================================
public static Class getPrimitiveClass(String name)
//==================================================================
{
	if (name.equals("byte")) return Byte.TYPE;
	else if (name.equals("char")) return Character.TYPE;
	else if (name.equals("boolean")) return Boolean.TYPE;
	else if (name.equals("int")) return Integer.TYPE;
	else if (name.equals("short")) return Short.TYPE;
	else if (name.equals("long")) return Long.TYPE;
	else if (name.equals("float")) return Float.TYPE;
	else if (name.equals("double")) return Double.TYPE;
	else if (name.equals("void")) return Void.TYPE;
	else return null;
}
/**
* This calls the loadClass() method to get and resolve the class.
* It returns null if it could not load the class.
*/
public Class
//==================================================================
	getClass(String name)
//==================================================================
{
	Class c = getPrimitiveClass(name);
	if (c != null) return c;
	return loadClass(name,true);
}
/**
*
*
*/
public static Class
//==================================================================
	getForName(String name)
//==================================================================
{
	Class c = getPrimitiveClass(name);
	if (c != null) return c;
	try{
		c = Class.forName(name);
		return c;
	}catch(Exception e){
		//e.printStackTrace();
		return null;
	}
}

/**
* This gets an instance of a class. Returns null if the class could
* not be loaded or if it could not be instantiated.
*/
public Object
//==================================================================
	getObject(String className)
//==================================================================
{
	try {
		Class c = getClass(className);
		return c.newInstance();
	}catch (Exception e){
		//System.out.println(e);
		return null;
	}
}
/**
* This gets the ClassInfo object for a class which was just loaded.
* It may return null if:
*   The Class is a primitive or system class.
*   The Class was already loaded without its info being saved.
*/
public ClassInfo
//============================================================
  getClassInfo(Class cl)
//============================================================
{
//............................................................
	for(Enumeration e = cache.elements();e.hasMoreElements();) {
		ClassInfo cur = (ClassInfo)e.nextElement();
		if (cur.theClass == cl) return cur;
	}
//............................................................
	return null;
}
//==================================================================

//public InputStream getResourceAsStream(String rs)
//{

//}
/*
public static void main(String args[])
{
	ClassLoader cl = new mClassLoader();
	URL url = cl.getSystemResource(args[0]);
	if (url == null) System.out.println("No resource found.");
	else {
		try {
			InputStream is = url.openStream();
			byte [] b = new byte[10];
			int got = is.read(b);
			System.out.println("Got: "+got);
			for (int i = 0; i<got; i++)
				System.out.print("<"+b[i]+">");
		}catch (Exception e) {System.out.println(e);}
	}
}
*/
//############################################################
}
//############################################################
