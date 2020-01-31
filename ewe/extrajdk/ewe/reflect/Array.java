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
public class Array{
//##################################################################

//===================================================================
public static boolean isArray(Object obj)
//===================================================================
{
	if (obj == null) return false;
	return obj.getClass().isArray();
}
//===================================================================
public static int getLength(Object obj)
//===================================================================
{
	if (!isArray(obj)) return -1;
	return java.lang.reflect.Array.getLength(obj);
}
//===================================================================
public static Object newInstance(Class type,int len)
//===================================================================
{
	try{
		return java.lang.reflect.Array.newInstance(type,len);
	}catch(Throwable t){
		return null;
	}
}
/**
 * Create a new instance of a particular class - which cannot be a primitive value, but
 * can be of an array or object type. All active class loaders are searched to resolve the
 * class if necessary.
 * @param className A class or array name.
 * @param len The number of elements.
 * @return An array if it could be created.
 */
//===================================================================
public static Object newInstance(String className,int len)
//===================================================================
{
	try{
		if (className.charAt(0) == '[')
			return Reflect.newArrayInstance(className,len);
		else
			return Reflect.newArrayInstance('L'+className.replace('.','/')+';',len);
	}catch(Throwable t){
	}
	Class c = Reflect.loadClass(className);
	if (c == null) return null;
	return newInstance(c,len);
}

//===================================================================
public static Class getComponentType(Object array)
//===================================================================
{
	if (array == null) return null;
	return array.getClass().getComponentType();
}
//-------------------------------------------------------------------
private static void getSetElement(Object array,int index,Wrapper w,boolean isGet)
//-------------------------------------------------------------------
{
	if (isGet) {
		Object got = java.lang.reflect.Array.get(array,index);
		w.fromJavaWrapper(got,0);
	}else{
		java.lang.reflect.Array.set(array,index,w.toJavaWrapper());
	}
}
//===================================================================
public static void setElement(Object array,int index,Wrapper value)
//===================================================================
{
	getSetElement(array,index,value,false);
}
//===================================================================
public static Wrapper getElement(Object array,int index,Wrapper dest)
//===================================================================
{
	if (dest == null) dest = new Wrapper();
	getSetElement(array,index,dest,true);
	return dest;
}

//##################################################################
}
//##################################################################
