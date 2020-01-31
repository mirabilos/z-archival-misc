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
* This class is a base class for Method and Constructor. It is not to be used directly.
**/
//##################################################################
public class MethodConstructor implements IMethod{
//##################################################################
//......................................................
// These are used by the VM. Do not move.
//......................................................
protected int _method_; // This holds a WClassMethod * value. Do not access.
protected int _vclass_; // This holds a WClass * value. Do not access.
public int wrapperType; // The return type of the wrapper.
protected String methodSpecs; // The full specs - parameters and return value.
protected int modifiers;
protected Class declaringClass;
public Reflect reflect;
protected String methodName;
//......................................................
//-------------------------------------------------------------------
MethodConstructor(){}
//-------------------------------------------------------------------
/**
* Returns the java encoded type returned by the method/constructor.
**/
//===================================================================
public String getType()
//===================================================================
{
	int idx = methodSpecs.indexOf(')');
	return methodSpecs.substring(idx+1);
}
/**
* Get the return type of the method as a class.
* @return the return type of the method as a class, or null if it is a void method.
*/
//===================================================================
public Class getReturnType()
//===================================================================
{
	String ty = getType();
	if (ty.equals("V")) return Void.TYPE;
	return Reflect.typeToClass(ty);
}
//===================================================================
public int getModifiers() {return modifiers;}
//===================================================================
public Class getDeclaringClass() {return declaringClass;}
//===================================================================
/**
* Returns the exception types the method can throw.
**/
//===================================================================
public native Class [] getExceptionTypes();
//===================================================================
/**
* This returns the parameter specs of the method including the enclosing brackets.
**/
//===================================================================
public String getParameters()
//===================================================================
{
	int idx = methodSpecs.indexOf(')');
	return methodSpecs.substring(0,idx+1);
}
/**
* Get a list of the parameters as Class objects.
**/
//===================================================================
public Class [] getParameterTypes()
//===================================================================
{
	String [] all = Reflect.getParameters(Reflect.getMethodParameterList(getSpecs()));
	Class[] ret = new Class[all.length];
	for (int i = 0; i<ret.length; i++)
		ret[i] = Reflect.typeToClass(all[i]);
	return ret;
}
//===================================================================
public String getName() {return methodName;}
public String getSpecs() {return methodSpecs;}
public String toString() {return getName()+"()";}//getSpecs();}
//===================================================================

//##################################################################
}
//##################################################################

