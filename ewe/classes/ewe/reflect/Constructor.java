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
public class Constructor extends MethodConstructor{
//##################################################################

/**
* If the method call newInstance() returns null, then this will be set to be
* the exception that caused the newInstance() call to fail.
**/
public Throwable instantiationError; //The most recent instantiation error.

//public Throwable invocationError; //The most recent invocation error.

//===================================================================
Constructor(Reflect from)
//===================================================================
{
	reflect = from;
}


/**
 * Use the Constructor to create a new instance of the Object.
 * Additional verbose
 * @param params  an array of parameters which must be of the correct length.
 * @return The newly created Object. If this is not null then the call was successful and the
	instantiationError variable should be ignored. If it is null, then the instantiationError will
	be the exception that caused the failure.
 */
//===================================================================
public Object newInstance(Wrapper [] params)
//===================================================================
{
	instantiationError = null;
	if (params == null) params = Wrapper.noParameter;
	try{
		Object got = nativeNewInstance(params);
		if (got == null) instantiationError = new InstantiationException(getDeclaringClass().toString());
		return got;
	}catch(Throwable t){
		instantiationError = t;
		return null;
	}
}

//-------------------------------------------------------------------
private native Object nativeNewInstance(Wrapper [] params);
//-------------------------------------------------------------------

//===================================================================
public String getName() {return getDeclaringClass().getName();}
//===================================================================

private java.lang.reflect.Constructor constructor;

//===================================================================
public java.lang.reflect.Constructor toJavaConstructor()
//===================================================================
{
	if (constructor == null) constructor = new java.lang.reflect.Constructor(this);
	return constructor;
}

//##################################################################
}
//##################################################################

