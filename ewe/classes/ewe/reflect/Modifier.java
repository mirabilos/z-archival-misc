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
public class Modifier {
//##################################################################
public static final int PUBLIC           = 0x00000001;
public static final int PRIVATE          = 0x00000002;
public static final int PROTECTED        = 0x00000004;
public static final int STATIC           = 0x00000008;
public static final int FINAL            = 0x00000010;
public static final int SYNCHRONIZED     = 0x00000020;
public static final int VOLATILE         = 0x00000040;
public static final int TRANSIENT        = 0x00000080;
public static final int NATIVE           = 0x00000100;
public static final int INTERFACE        = 0x00000200;
public static final int ABSTRACT         = 0x00000400;
public static final int STRICT           = 0x00000800;

//===================================================================
public static boolean isPublic(int m) {return (m & PUBLIC) != 0;}
public static boolean isPrivate(int m) {return (m & PRIVATE) != 0;}
public static boolean isProtected(int m) {return (m & PROTECTED) != 0;}
public static boolean isStatic(int m) {return (m & STATIC) != 0;}
public static boolean isFinal(int m) {return (m & FINAL) != 0;}
public static boolean isSynchronized(int m) {return (m & SYNCHRONIZED) != 0;}
public static boolean isVolatile(int m) {return (m & VOLATILE) != 0;}
public static boolean isTransient(int m) {return (m & TRANSIENT) != 0;}
public static boolean isNative(int m) {return (m & NATIVE) != 0;}
public static boolean isInterface(int m) {return (m & INTERFACE) != 0;}
public static boolean isAbstract(int m) {return (m & ABSTRACT) != 0;}
public static boolean isStrict(int m) {return (m & STRICT) != 0;}
//===================================================================
public static String toString(int m)
//===================================================================
{
	StringBuffer sb = new StringBuffer();
	int len;
	if ((m & PUBLIC) != 0)	sb.append("public ");
	if ((m & PRIVATE) != 0)	sb.append("private ");
	if ((m & PROTECTED) != 0)	sb.append("protected ");
	if ((m & ABSTRACT) != 0)	sb.append("abstract ");
	if ((m & STATIC) != 0)	sb.append("static ");
	if ((m & FINAL) != 0)		sb.append("final ");
	if ((m & TRANSIENT) != 0)	sb.append("transient ");
	if ((m & VOLATILE) != 0)	sb.append("volatile ");
	if ((m & NATIVE) != 0)	sb.append("native ");
	if ((m & SYNCHRONIZED) != 0)	sb.append("synchronized ");
	if ((m & INTERFACE) != 0)	sb.append("interface ");
	if ((m & STRICT) != 0)	sb.append("strictfp ");
	if ((len = sb.length()) > 0) return sb.toString().substring(0, len-1);
	else return "";
}

//##################################################################
}
//##################################################################

