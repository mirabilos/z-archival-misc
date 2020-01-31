/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (C) 1999-2002 Michael L Brereton <michael_brereton@ewesoft.com>    *
 *  Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>                         *
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

import ewe.sys.AsyncTask;

/**
* This class is as close to the java.lang.System class as is possible. However no
* references to any classes in "java." packages other than "java.lang" are not possible.
* This means all the methods that deal with IO streams are not available.
* <p>
* Most of these method simply pass the call to corresponding ones in ewe.sys.Vm.
* <p>
* If you are looking for System.out and System.in, use Vm.out and Vm.in instead.
**/
//##################################################################
public final class System{
//##################################################################
/**
* Copy from one array to another.
**/
//===================================================================
public static void arraycopy(Object src,int src_position,Object dest,int dest_position,int length)
//===================================================================
{
	ewe.sys.Vm.copyArray(src,src_position,dest,dest_position,length);
}

static ewe.sys.Time now = new ewe.sys.Time();
/**
* Get the current time in milliseconds.
**/
//===================================================================
public static long currentTimeMillis()
//===================================================================
{
	now.setToCurrentTime();
	return now.getTime();
}
/**
* Tell the VM to exit with the specified exit code.
**/
//===================================================================
public static void exit(int exitCode)
//===================================================================
{
	ewe.sys.Vm.exit(exitCode);
}
/**
 * Tell the VM to do a garbage collection run.
 */
//===================================================================
public static void gc()
//===================================================================
{
	ewe.sys.Vm.gc();
}
/**
 * Get a system property.
 * @param name The name of the property.
 * @return The value of the property or null if it is not set.
 */
//===================================================================
public static String getProperty(String name)
//===================================================================
{
	return ewe.sys.Vm.getProperty(name,null);
}
public static String getenv(String envVariableName)
{
	return ewe.sys.Vm.getProperty(envVariableName,null);
}
/**
 * Get a system property.
 * @param name The name of the property.
 * @param defaultValue the default value to return if the property is not set.
 * @return The value of the property or the defaultValue if it is not set.
 */
//===================================================================
public static String getProperty(String name,String defaultValue)
//===================================================================
{
	return ewe.sys.Vm.getProperty(name,defaultValue);
}
private static SecurityManager sm;
//-------------------------------------------------------------------
private static SecurityManager getSetSecurityManager(SecurityManager manager)
//-------------------------------------------------------------------
{
	if (manager != null) sm = manager;
	return sm;
}
/**
 * Get the current SecurityManager if one is set.
 * @return the current SecurityManager if one is set, or null if not.
 */
//===================================================================
public static SecurityManager getSecurityManager()
//===================================================================
{
	return 	getSetSecurityManager(null);
}
/**
* Set the SecurityManager. Under Ewe version 1.30 the SecurityManager does
* nothing. Future versions will have this security enabled.
* @param sm The SecurityManager.
* @exception SecurityException if a SecurityManager is already set.
*/
//===================================================================
public static void setSecurityManager(SecurityManager sm) throws SecurityException
//===================================================================
{
	if (sm == null) throw new NullPointerException();
	if (getSecurityManager() != null) throw new SecurityException();
	getSetSecurityManager(sm);
}
/**
 * Load a library name, not using the system default library extension.
 * @param libraryName The name of the library.
 * @exception UnsatisfiedLinkError If the library could not be loaded or is invalid.
 * @exception SecurityException If library loading is not allowed.
 */
//===================================================================
public static void loadLibrary(String libraryName)
throws UnsatisfiedLinkError, SecurityException
//===================================================================
{
	ewe.sys.Vm.loadDynamicLibrary(libraryName);
}
/**
 * Load a library name using the absolute path name.
 * @param libraryName The name and path of the library.
 * @exception UnsatisfiedLinkError If the library could not be loaded or is invalid.
 * @exception SecurityException If library loading is not allowed.
 */
//===================================================================
public static void load(String libraryName)
throws UnsatisfiedLinkError, SecurityException
//===================================================================
{
	ewe.sys.Vm.load(libraryName);
}
/**
* Under a Ewe VM this will simply do a gc(). Under a Java VM this will run
* finalizations on pending classes.
**/
//===================================================================
public static void runFinalization()
//===================================================================
{
	gc();
}
/**
* Tells the VM to run finalizers on exit. Under Ewe this does nothing.
**/
//===================================================================
public static void runFinalizationOnExit(boolean on)
//===================================================================
{

}
/**
* Get what the hashcode of this Object would be if the Object.hashCode() method
* was called instead of any overriding hashCode() methods.
**/
//===================================================================
public static int identityHashCode(Object obj)
//===================================================================
{
	return ewe.sys.Vm.identityHashCode(obj);
}
/**
* Set a property value.
* @param key The property name.
* @param value The property value.
* @return the old property value (if any).
*/
//===================================================================
public static String setProperty(String key,String value)
//===================================================================
{
	String old = getProperty(key);
	ewe.sys.Vm.setProperty(key,value);
	return old;
}
/*
//===================================================================
public static String mapLibraryName(String libName)
//===================================================================
{
	return ewe.sys.Vm.mapLibraryName();
}
*/
//##################################################################
}
//##################################################################

