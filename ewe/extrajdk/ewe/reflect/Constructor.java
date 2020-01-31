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
import java.lang.reflect.*;
//##################################################################
public class Constructor extends MethodConstructor {
//##################################################################

protected java.lang.reflect.Constructor constructor;
public Throwable instantiationError; //The most recent instantiation error.
//public Throwable invocationError; //The most recent invocation error.

//===================================================================
public Constructor(Reflect from)
//===================================================================
{
	reflect = from;
}
//-------------------------------------------------------------------
protected boolean fromConstructor(java.lang.reflect.Constructor m)
//-------------------------------------------------------------------
{
	try{
		methodName = m.getName();
		this.constructor = m;
		Class [] pars = m.getParameterTypes();
		methodSpecs = "(";
		for (int i = 0; i<pars.length; i++)
			methodSpecs += Reflect.toFullType(pars[i]);
		methodSpecs += ")";
		String type = "V";
		methodSpecs += type;
		wrapperType = type.charAt(0);
		modifiers = constructor.getModifiers();
		declaringClass = constructor.getDeclaringClass();
		return true;
	}catch(Throwable t){
		return false;
	}
}
//===================================================================
public Object newInstance(Wrapper [] params)
//===================================================================
{
	if (params == null) params = Wrapper.noParameter;
	return nativeNewInstance(params);
}
Object retObj;
//===================================================================
public Object nativeNewInstance(final Wrapper [] params)
//===================================================================
{
		retObj = null;
		try {
			retObj = constructor.newInstance(Wrapper.toJavaWrappers(params));
		}catch(InvocationTargetException te){
			instantiationError = te.getTargetException();
		}catch(Throwable e){
			instantiationError = e;
		}
		return retObj;
	/*
	Thread t = new Thread(){
		public void run(){
			synchronized(ewe.sys.Vm.getSyncObject()){
				retObj = null;
				try {
					retObj = constructor.newInstance(Wrapper.toJavaWrappers(params));
				}catch(InvocationTargetException te){
					instantiationError = te.getTargetException();
				}catch(Exception e){
					instantiationError = e;
				}finally{
					ewe.sys.Vm.getSyncObject().notifyAll();
				}
			}
		}
	};
	t.start();
	try{ewe.sys.Vm.getSyncObject().wait();}catch(InterruptedException e){}
	return retObj;
	*/
}
//===================================================================
public Class [] getExceptionTypes() {return constructor.getExceptionTypes();}
//===================================================================
//===================================================================
public java.lang.reflect.Constructor toJavaConstructor()
//===================================================================
{
	return constructor;
}
//##################################################################
}
//##################################################################
