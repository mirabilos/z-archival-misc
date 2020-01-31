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
This class provides an easy way to refer to and link to a Class that may
not be present at run time.<p>
You create and use a Type object like so:
<p><pre>
Type tp = new Type("ewe.security.Decryptor");
boolean wasFound = tp.exists();
boolean isAnInstance = tp.isInstance(anObject);
</pre>
**/
//##################################################################
public class Type{
//##################################################################
private Reflect r;
private String className;
/**
Return a new Instance of the class if the class exists.
**/
//===================================================================
public Object newInstance()
//===================================================================
{
	if (r == null) return null;
	return r.newInstance();
}
//===================================================================
public Object newInstance(String constructorSpecs, Object[] parameters)
//===================================================================
{
	if (r == null) return null;
	return r.newInstance(constructorSpecs,Wrapper.toEweWrappers(parameters));
}
//===================================================================
public Object invoke(Object dest, String nameAndSpecs, Object[] parameters)
//===================================================================
{
	if (r == null) return null;
	Method m = r.getMethod(nameAndSpecs,r.PUBLIC);
	if (m == null) return null;
	Wrapper w = m.invoke(dest,Wrapper.toEweWrappers(parameters),null);
	if (w == null) return null;
	return w.toJavaWrapper();
}
/**
Returns the ewe.reflect.Reflect object that represents the class,
if the class was found during the construction of this Type. If the
class was not found during the cunstruction, this will return null.
**/
//===================================================================
public Reflect getReflection()
//===================================================================
{
	return r;
}
/**
* Return the name of the Class this Type represents.
**/
//===================================================================
public String getClassName()
//===================================================================
{
	return className;
}
/**
Create a Type for the specified className - which should be specified in standard "."
notation. If the class is not found the Type will be still be created, but the isInstance()
method will always return false. The exists() method can be used to check if the class
actually was found.
**/
//===================================================================
public Type(String className)
//===================================================================
{
	if (className == null) return;
	r = Reflect.loadForName(className);
	String name = className;
	if (name.charAt(0) == 'L' && name.charAt(name.length()-1) == ';')
		name = name.substring(1,name.length()-1).replace('/','.');
	this.className = name;
}
/**
Return if the class of the Type was actually found.
**/
//===================================================================
public boolean exists()
//===================================================================
{
	return r != null;
}
/**
 * Return if the specified Object is an instance of the Class represented by this Type.
 * @param obj the object to check.
 * @return true if the object is an instance of the Class represented by this Type or false
 * if not, or if obj is null, or if the Class for this type was not found.
 */
//===================================================================
public boolean isInstance(Object obj)
//===================================================================
{
	if (r == null || obj == null) return false;
	return r.isInstance(obj);
}
/*
//=================================================================
public static void main(String[] args)
//=================================================================
{
	ewe.sys.Vm.startEwe(args);
	Type t = new Type("java.lang.String");
	ewe.sys.Vm.debug(t.isInstance("hello") +", "+ t.isInstance(t));
	ewe.sys.mThread.nap(2000);
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

