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
package ewe.data;
import ewe.util.*;

//##################################################################
public class DataUtils{
//##################################################################

//===================================================================
public static TreeNode getChildAt(TreeNode parent,int [] address)
//===================================================================
{
	if (address == null) return null;
	TreeNode n = parent;
	for (int i = 0; i<address.length && n != null; i++)
		n = n.getChild(address[i]);
	return n;
}
//===================================================================
public static int [] addressOfChild(TreeNode parent,TreeNode child)
//===================================================================
{
	TreeNode ch = child;
	int i;
	for (i = 0;child != parent && child != null;i++) child = child.getParent();
	if (child == null) return null; //Child not in parent.
	int [] ret = new int[i];
	child = ch;
	for (int j = 0;child != parent;j++){
		TreeNode p = child.getParent();
		ret[i-j-1] = p.indexOfChild(child);
		child = p;
	}
	return ret;
}
//===================================================================
public static TreeNode getSibling(TreeNode child,int change)
//===================================================================
{
	TreeNode p = child.getParent();
	if (p == null) return null;
	return p.getChild(p.indexOfChild(child)+change);
}
//===================================================================
public static TreeNode getParent(TreeNode child,int levels)
//===================================================================
{
	for (int i = 0; i<levels && child != null; i++) child = child.getParent();
	return child;
}

//##################################################################
}
//##################################################################

