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
* This is used to invoke methods on objects. IT ONLY WORKS FOR NON-STATIC METHODS.
**/
//##################################################################
public class Method extends MethodConstructor{
//##################################################################
public java.lang.reflect.Method method;
public Throwable invocationError; //The most recent invocation error.
//===================================================================
Method(Reflect from)
//===================================================================
{
	reflect = from;
}

//-------------------------------------------------------------------
protected boolean fromMethod(java.lang.reflect.Method m)
//-------------------------------------------------------------------
{
	try{
		methodName = m.getName();
		this.method = m;
		//if (!Modifier.isPublic(m.getDeclaringClass().getModifiers())) return false;
		Class [] pars = m.getParameterTypes();
		methodSpecs = "(";
		for (int i = 0; i<pars.length; i++)
			methodSpecs += Reflect.toFullType(pars[i]);
		methodSpecs += ")";
		String type = Reflect.toFullType(m.getReturnType());
		methodSpecs += type;
		wrapperType = type.charAt(0);
		modifiers = method.getModifiers();
		declaringClass = method.getDeclaringClass();
		return true;
	}catch(Throwable t){
		return false;
	}
}

//===================================================================
public Wrapper invoke(Object target,Wrapper [] parameters,Wrapper result)
//===================================================================
{
	if (!Modifier.isPublic(getModifiers())) {
		invocationError = new IllegalAccessException("Method: "+getName()+" is not public");
		return null;
	}
	if (result == null) result = new Wrapper();
	result.setType(wrapperType);
	return nativeInvoke(target,parameters,result);
}
//===================================================================
public boolean returnsValue()
//===================================================================
{
	int l = methodSpecs.length();
	return (methodSpecs.charAt(l-1) != 'V');
}

//-------------------------------------------------------------------
Wrapper invokeInThread(Object target,Wrapper [] parameters,Wrapper result)
//-------------------------------------------------------------------
{
	if (result == null) result = new Wrapper();
	result.setType(wrapperType);
	invokeRet = null;
	try{
		Object got = method.invoke(target,Wrapper.toJavaWrappers(parameters));
		if (returnsValue()) result.fromJavaWrapper(got);
		invokeRet = result;
	}catch(java.lang.reflect.InvocationTargetException ite){
		invocationError = ite.getTargetException();
	}catch(Throwable e){
		invocationError = e;
	}
	return invokeRet;
}

Wrapper invokeRet;
//===================================================================
public Wrapper nativeInvoke(final Object target,final Wrapper [] parameters,final Wrapper result)
//===================================================================
{
	invokeInThread(target,parameters,result);
	return invokeRet;
/*
	Thread t = new Thread(){
		public void run(){
			invokeInThread(target,parameters,result);
		}
	};
	//t.run();
	t.start();
	try{t.join();}catch(InterruptedException e){}
	return invokeRet;
	*/
}
public Class [] getExceptionTypes() {return method.getExceptionTypes();}

//===================================================================
public java.lang.reflect.Method toJavaMethod()
//===================================================================
{
	return method;
}
//##################################################################
}
//##################################################################
