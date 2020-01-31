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
* A ClassLoader is used to load and link classes which would not be loaded
* by the system (bootstrap loader).<p>
* The only method you should override is <b>loadClass(String name,boolean resolve)</b> This
* method should do the following:
* <p>
* <nl>
* <li>
* First call Reflect.getForName(String name) to see if it is a system loadable class. If it
* is it should return that value. Otherwise it should catch the exception.
* <li>
* Second see if it has already loaded that class (by checking some kind
* of cache of previously loaded classes).
* <li>
* Then it should attempt to locate the bytes for the class definition. If it could not
* then it should throw a ClassNotFoundException.
* <li>
* If it finds the bytes it should call defineClass(String className,byte [] bytes,int offset,int length).
* This will define the class within the VM.
* <li>
* If the "resolve" parameter is true, it should call resolveClass(Class definedClass) on the Class
* returned by defineClass.
* </nl>
*
* <p>An easier class loader to extend is ewe.util.mClassLoader. This does most of this work already
* and you only need to override findClassBytes().
**/
//##################################################################
public abstract class ClassLoader{
//##################################################################
/** Do not use this - used by the native VM! */
private Object nextLoader;

/**
* This requests a class to be loaded and resolved.
**/
//===================================================================
public final Class loadClass(String name) throws ClassNotFoundException
//===================================================================
{
	return loadClass(name,true);
}
/**
* This should be overriden to actually locate the class bytes and define the class.
**/
//-------------------------------------------------------------------
protected abstract Class loadClass(String name,boolean resolve) throws ClassNotFoundException;
//-------------------------------------------------------------------
/**
* This requests the VM to resolve the class after being defined. It should be called by loadClass() if
* the resolve parameter is true.
**/
//-------------------------------------------------------------------
protected final void resolveClass(Class c){}
//-------------------------------------------------------------------
/**
* This tells the VM to convert a sequence of bytes representing a class definition into a Class Object. It
* should be called by loadClass() once the bytes are located.
**/
//-------------------------------------------------------------------
protected final native Class defineClass(String name,byte [] classBytes,int start,int offset);
//-------------------------------------------------------------------

//-------------------------------------------------------------------
protected ClassLoader()
//-------------------------------------------------------------------
{
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) sm.checkCreateClassLoader();
	nextLoader = this;
	nativeCreate();
}
//-------------------------------------------------------------------
private native void nativeCreate();
//-------------------------------------------------------------------

//##################################################################
}
//##################################################################

