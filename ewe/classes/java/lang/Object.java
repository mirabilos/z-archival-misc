/* $MirOS: contrib/hosted/ewe/classes/java/lang/Object.java,v 1.2 2007/08/30 23:27:01 tg Exp $ */

/*********************************************************************************
 *  Ewe Virtual Machine - Version 1.14, January 24, 2002                         *
 *  Copyright (c) 2007 Thorsten “mirabilos” Glaser <tg@mirbsd.de>                *
 *  Copyright (C) 1998, 1999, 2000  Free Software Foundation                     *
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

/**
 * Object is the the base class for all objects.
Under Ewe all methods except the wait() and notify() methods are supported.
 * <p>
 * The number of methods in this class is
 * small since each method added to this class is added to all other classes
 * in the system.
 * <p>
 * As with all classes in the ewe.lang package, you can't reference the
 * Object class using the full specifier of ewe.lang.Object.
 * The ewe.lang package is implicitly imported.
 * Instead, you should simply access the Object class like this:
 * <pre>
 * Object obj = (Object)value;
 * </pre>
 */

public class Object
{
/**
 * Return a String representation of this object.
 * @return a String representing this object.
 */
//===================================================================
public String toString()
//===================================================================
{return getClass().getName()+"@"+hashCode();}
/**
 * Return a Class object that represents the class of this object.
<b>Note:</b> under a Ewe VM Class objects returned by this method are
not static. Therefore when comparing class objects you should use the equals() method
and never the '==' operator.
 * @return A Class object representing the class of this object.
 */
//===================================================================
public native Class getClass();
//===================================================================
/**
 * Returns if this object is considered equal to the other object.
 * @param other Another object to compare to.
 * @return true if this object is considered equal to the other object.
 */
//===================================================================
public boolean equals(Object other){return other == this;}
//===================================================================
/**
* Returns a hashCode for the object. The general contract of hashCode is:
* <nl>
* <li>An object must return the same hash code for its entire existence.
* <li>If two objects are considered equal by the equals() method, they should
* return the same hash code.
* </nl>
* Not all Objects will do this and you should only use hashCode() from Objects which
* declare an overrided version of hashCode(). The only Objects which provide consistent
* and correct hash codes under Ewe are Object, String and Class.
**/
//===================================================================
public int hashCode() {return ewe.sys.Vm.toInt(this);}
//===================================================================
/**
* This method (when overriden), will be called when the VM determines that the Object can be
* garbage collected. Any action can be taken by the finalize() method, including making the Object
* available to other Threads (which would stop the garbage collection of the Object).<p>
* The finalize() method of a Class is only called if it overrides finalize() - the finalize() method
* java.lang.Object is never called by the Ewe VM.<p>
* Note that under a native Ewe VM, the call to finalize() will be synchronized with the Ewe library,
* but under a Java VM, the finalize() method may be called by a separate Thread that is not synchronized
* with the Ewe library. To ensure that the execution of the finalize() method is sychronized with the
* Ewe library your finalize() method should look like this:<p>
* <pre>
* protected void finalize()
* {
*   synchronized(ewe.sys.Vm.getSyncObject()){
*   //
*   // Put your finalize() code here.
*   //
*   }
* }
* </pre>
* Under Ewe the synchronized keyword is silently ignored.
*
**/
//===================================================================
protected void finalize() throws Throwable{}
//===================================================================
/**
 * Create a field for field copy of this Object <b>if</b> this Object
	implements the Cloneable interface.
 * @return A clone of this Object.
 * @exception CloneNotSupportedException if this Object does not implement the Cloneable interface.
 */
//===================================================================
protected Object clone() throws CloneNotSupportedException
//===================================================================
{
	if (!(this instanceof Cloneable)) throw new CloneNotSupportedException(getClass().getName());
	return makeClone();
}

//-------------------------------------------------------------------
private native Object makeClone();
//-------------------------------------------------------------------

//===================================================================
public Object () {}
//===================================================================

}
