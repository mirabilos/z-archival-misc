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

//##################################################################
public class Throwable{
//##################################################################
// Don't move these four.
private String message;
private String stackTrace;
private Throwable cause;
private boolean causeSet = false;

//===================================================================
public Throwable(String message) {this.message = message;}
//===================================================================
public Throwable() {this((String)null);}
//===================================================================
public Throwable(Throwable cause) {this((String)null); initCause(cause);}
//===================================================================
public Throwable(String message,Throwable cause) {this(message); initCause(cause);}
//===================================================================

//===================================================================
public final String getMessage() {return message;}
//===================================================================
public String toString()
//===================================================================
{
	ewe.reflect.Reflect r = ewe.reflect.Reflect.getForObject(this);
	if (message != null) return r.getClassName().replace('/','.')+": "+message;
	else return r.getClassName().replace('/','.');
}
//public void printStackTrace(java.io.PrintWriter pw){}
//===================================================================
public void printStackTrace()
//===================================================================
{
	String got = "";
	for (Throwable t = this; t != null; t = t.getCause()){
		if (t != this) got += "\nCaused by: "+t.toString()+"\n\t... more";
		else {
			got += t.toString();
			if (t.stackTrace != null) got += t.stackTrace;
		}
	}
	ewe.sys.Vm.debug(got);
	/*
	try{
		ewe.io.StreamWriter sw = new ewe.io.StreamWriter(new ewe.io.FileOutputStream("/StackTrace.txt",true));
		sw.println(got);
		sw.close();
	}catch(Throwable t){

	}
	*/
}
//===================================================================
public Throwable fillInStackTrace()
//===================================================================
{
	fillInStackTrace(2);
	return this;
}
//-------------------------------------------------------------------
private native void fillInStackTrace(int ignore);
//-------------------------------------------------------------------
/**
 * Get the cause for this Throwable.
 * @return the cause for this Throwable.
 */
//===================================================================
public Throwable getCause()
//===================================================================
{
	return cause;
}
/**
 * Initialize the cause of this Throwable to be the specified Throwable.
 * @param t The cause for this Throwable.
 * @return this Throwable.
 * @exception IllegalStateException if cause was already set.
 * @exception IllegalArgumentException if the parameter is this Throwable.
 */
//===================================================================
public Throwable initCause(Throwable t) throws IllegalStateException, IllegalArgumentException
//===================================================================
{
	if (causeSet) throw new IllegalStateException();
	if (t == this) throw new IllegalArgumentException();
	cause = t;
	return this;
}

//##################################################################
}
//##################################################################

