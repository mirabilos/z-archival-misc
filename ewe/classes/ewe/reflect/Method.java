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
* This is used to invoke methods on objects. It works on static and instance methods.
**/
//##################################################################
public class Method extends MethodConstructor {
//##################################################################
public Throwable invocationError; //The most recent invocation error.
private WeakReference lastInvoked;
//===================================================================
Method(Reflect from)
//===================================================================
{
	reflect = from;
}

/**
 * Invoke the method on the specified target object.
 * @param target The object to invoke the method on, or null in the case of static methods.
 * @param parameters an array of parameters which must be of the correct length.
 * @param results an optional Wrapper provided to retrieve the results. If it is null then a
	new one will be created and returned.
 * @return A wrapper containing the returned value. If the method type is void then the value
	stored in the wrapper should be ignored. If the returned value is null then an error occured
	and the invocationError variable will hold the exception that caused it.
 */
//===================================================================
public Wrapper invoke(Object target,Wrapper [] parameters,Wrapper result)
//===================================================================
{
	if (target == null || lastInvoked == null || lastInvoked.get() != target){
		if (target != null){
			if (!Reflect.isTypeOf(target.getClass().getName(),getDeclaringClass().getName()))
				throw new IllegalArgumentException("Object is not of correct class.");
		}else{
			if (!Modifier.isStatic(getModifiers()))
				throw new NullPointerException("Method is not static");
		}
		if (!Modifier.isPublic(getModifiers())) {
			invocationError = new IllegalAccessException("Method: "+getName()+" is not public");
			return null;
		}
		lastInvoked = new WeakReference(target);
	}
	if (result == null) result = new Wrapper();
	result.setType(wrapperType);
	try{
		Wrapper w = nativeInvoke(target,parameters,result);
		return w;
	}catch(Throwable t){
		t.printStackTrace();
		invocationError = t;
		return null;
	}
}
//-------------------------------------------------------------------
private native Wrapper nativeInvoke(Object target,Wrapper [] parameters,Wrapper result);
//-------------------------------------------------------------------

/**
* Returns true if the method is not of type void.
**/
//===================================================================
public boolean returnsValue()
//===================================================================
{
	int l = methodSpecs.length();
	return (methodSpecs.charAt(l-1) != 'V');
}
//===================================================================
public String toString() //{return getName();}
//===================================================================
{
	Class[] all = getParameterTypes();
	String ret = Modifier.toString(getModifiers())+" "+getReturnType().getName()+" "+getDeclaringClass().getName()+"."+getName()+"(";
	for (int i = 0; i<all.length; i++){
		if (i != 0) ret += ",";
		ret += all[i].getName();
	}
	ret += ")";
	return ret;
}

private java.lang.reflect.Method method;

//===================================================================
public java.lang.reflect.Method toJavaMethod()
//===================================================================
{
	if (method == null) method = new java.lang.reflect.Method(this);
	return method;
}
//##################################################################
}
//##################################################################

