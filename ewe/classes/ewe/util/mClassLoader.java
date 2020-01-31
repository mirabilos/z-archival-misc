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
import ewe.data.PropertyList;
import ewe.security.*;

//##################################################################
public abstract class mClassLoader extends ClassLoader{
//##################################################################

private static WeakSet classLoaders;
private static Object[] empty = new Object[0];

/**
* This passes authority from this ClassLoader to another. This call
* can only be made by system classes or by classes loaded by this ClassLoader,
* or by this ClassLoader itself.
**/
//===================================================================
public final void authorize(mClassLoader other) throws SecurityException
//===================================================================
{
	SecurityManager sm = System.getSecurityManager();
	if (sm instanceof mSecurityManager){
		mSecurityManager ms = ((mSecurityManager)sm);
		ms.checkPassAuthority(this,other);
		PropertyList mine = null;
		try{
			mine = ms.getAuthorizers(this,false);
		}catch(SecurityException e){
		// Nothing to authorize.
			return;
		}
		PropertyList pl = ms.getAuthorizers(other,true);
		pl.addAll(mine);
		return;
	}
	throw new SecurityException();
}
/**
* Add a ClassLoader to the WeakSet of application classLoaders. You do not need
* to do this if you are creating a ClassLoader that inherits from mClassLoader
* as they automatically add to the WeakSet upon creation.
**/
//===================================================================
public static void addClassLoader(ClassLoader loader)
//===================================================================
{
	if (loader == null) return;
	if (classLoaders == null) classLoaders = new WeakSet();
	classLoaders.add(loader);
}
//===================================================================
public static void removeClassLoader(ClassLoader loader)
//===================================================================
{
	if (classLoaders == null) return;
	classLoaders.remove(loader);
	if (classLoaders.isEmpty()) classLoaders = null;
}
/**
 * Get the current set of application ClassLoaders into a Vector, or simply count the number
 * of active ClassLoaders.
 * @param dest A Vector to hold the class loaders. If this is not null it will be cleared before
 * the classes are placed in it. If it is null then this just counts the number of active ClassLoaders.
 * @return The number of active class loaders.
 */
//===================================================================
public static int getClassLoaders(Vector dest)
//===================================================================
{
	if (dest == null) dest.clear();
	if (classLoaders == null) return 0;
 	if (classLoaders.isEmpty()) {
		classLoaders = null;
		return 0;
	}
	Object[] all = classLoaders.getRefs();
	int num = 0;
	for (int i = 0; i<all.length; i++){
		if (all[i] == null) continue;
		num++;
		if (dest != null) dest.add(all[i]);
	}
	return num;
}

//===================================================================
public mClassLoader()
//===================================================================
{
	addClassLoader(this);
}
/**
* This method will check for system classes and cached classes before calling
* findClassBytes().
**/
//-------------------------------------------------------------------
protected Class loadClass(String className,boolean resolve) throws ClassNotFoundException
//-------------------------------------------------------------------
{
	ewe.reflect.Reflect r = ewe.reflect.Reflect.getForName(className);
	if (r != null) return r.getReflectedClass();
	Class c = findCachedClass(className);
	if (c == null){
		ByteArray data = findClassBytes(className);
		c = data == null ? null : defineClass(className,data.data,0,data.length);
	}else{
		//ewe.sys.Vm.debug("Found in cache: "+className);
	}
	if (c == null) throw new ClassNotFoundException(className);
	if (resolve) resolveClass(c);
	cacheClass(className,c);
	return c;
}
/**
* A cache of already loaded classes.
**/
//-------------------------------------------------------------------
protected Hashtable loaded = new Hashtable();
//-------------------------------------------------------------------
/**
* Override this to provide class caching. By default a simple hashtable is used.
**/
//-------------------------------------------------------------------
protected Class findCachedClass(String className) {return (Class)loaded.get(className);}
//-------------------------------------------------------------------
/**
* Override this to provide class caching. By default a simple hashtable is used.
**/
//-------------------------------------------------------------------
protected void cacheClass(String className,Class aClass){loaded.put(className,aClass);}
//-------------------------------------------------------------------
/**
* You can override this method OR you can override getInputStreamFor(String className) - which is called
* by this method. It should attempt to locate the class
* bytes and put it in a returned ByteArray object. If it could not it should return null
* or throw a RuntimeException.
**/
//-------------------------------------------------------------------
protected ByteArray findClassBytes(String className)
//-------------------------------------------------------------------
{
	ewe.io.Stream is = getInputStreamFor(className);
	if (is == null) return null;
	ewe.io.MemoryFile mf = ewe.io.MemoryFile.createFrom(is,null);
	is.close();
	if (mf == null) return null;
	return mf.data;
}
/**
* This is called by findClassBytes by default and you can override this if necessary instead of overriding
* findClassBytes(). By default it will convert all '.' to '/' and then append '.class' to the end of it.
* It will then call openResource() with the converted class name. Therefore, overriding openResource() will
* take care of loading classes AND class dependant resources.
**/
//-------------------------------------------------------------------
protected ewe.io.Stream getInputStreamFor(String className)
//-------------------------------------------------------------------
{
	className = className.replace('.','/')+".class";
	return openResource(className);
}
/**
* This is used to get a resource which may be dependant on the how the class
* was loaded.
**/
//===================================================================
public ewe.io.Stream openResource(String resourceName){return null;}
//===================================================================

/**
* This is a utility method used to load a Class by another Class which itself may have
* been loaded via a ClassLoader. It first tries to load the class via the default
* system class loader using Class.forName(). If this fails it checks the ClassLoader
* for the requestor class (if the requestor is not null) and requests that ClassLoader
* to load the class. If that fails as well it will return null.
**/
//===================================================================
public static Class getClass(String name,Class requestor)
//===================================================================
{
	ewe.reflect.Reflect r = ewe.reflect.Reflect.getForName(name);
	if (r != null) return r.getReflectedClass();
	if (requestor == null) return null;
	ClassLoader cl = requestor.getClassLoader();
	if (cl == null) return null;
	try{
		return cl.loadClass(name);
	}catch(Exception ex){
		return null;
	}
}
/**
* This tries to get a new instance of a class. First it calls mClassLoader.getClass()
* to get a Class object for the className. If that is successful it attempts to create
* a new instance of the class by calling the default constructor. If this is is successful
* it will return the newly created object.
**/
//===================================================================
public static Object newInstance(String className,Class requestor)
//===================================================================
{
	Class c = getClass(className,requestor);
	if (c == null) return null;
	ewe.reflect.Reflect r = new ewe.reflect.Reflect(c);
	return r.newInstance();
}
/**
* This tries to open a resource via the class loader of the requestor. If this fails it will
* then try to open the resource normally. This actually calls ewe.sys.Vm.openResource().
**/
//===================================================================
public static ewe.io.Stream openResource(String resourceName,Class requestor)
//===================================================================
{
	return ewe.sys.Vm.openResource(requestor,resourceName);
}
//##################################################################
}
//##################################################################


