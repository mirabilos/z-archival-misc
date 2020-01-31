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
package ewe.ui;
import ewe.fx.*;
import ewe.sys.Vm;
import ewe.data.*;
import ewe.util.*;

//##################################################################
public abstract class TreeModelAdapter extends TreeTableModel{
//##################################################################

//===================================================================
public TreeModelAdapter()
//===================================================================
{
	init((long)-1 << 32,(byte)(IsNode|CanExpand));
}
//-------------------------------------------------------------------
protected Vector objects = new Vector();
//-------------------------------------------------------------------
//-------------------------------------------------------------------
protected int newNodeObject(Object node)
//-------------------------------------------------------------------
{
	objects.add(node);
	return objects.size()-1;
}
//-------------------------------------------------------------------
protected Object getParentObject(int whichLine)
//-------------------------------------------------------------------
{
	long ref = objectAt(whichLine);
	int oi = (int)(ref >> 32);
	if (oi < 0) return rootObject;
	else return objects.get(oi);
}
//-------------------------------------------------------------------
protected int getIndexFor(int whichLine)
//-------------------------------------------------------------------
{
	return (int)objectAt(whichLine) & 0xffffffff;
}

//===================================================================
public Object getDataObjectAt(int whichLine)
//===================================================================
{
	return createObjectFor(getParentObject(whichLine),getIndexFor(whichLine));
}
//===================================================================
public int doExpand(int whichLine)
//===================================================================
{
	if (!canDoExpand(whichLine)) return 0;
	Object newNode = createObjectFor(getParentObject(whichLine),getIndexFor(whichLine));
	if (newNode == null) return 0;
	long parentIndex = (long)newNodeObject(newNode) << 32;
	long [] refs = getChildIndexes(newNode);
	if (refs == null) refs = new long[0];
	byte [] flags = new byte[refs.length];
	for (int i = 0; i<refs.length; i++) {
		refs[i] |= parentIndex;
		flags[i] = CanExpand|IsNode;
	}
	adjustFlags(newNode,refs,flags);
	return expand(whichLine,refs,flags);
}
/**
* Use this to create a new node object for a node which is about to be
* expanded.
**/
//-------------------------------------------------------------------
protected abstract Object createObjectFor(Object parent,int childIndex);
//-------------------------------------------------------------------
/**
* This is used to get an array of indexes for each of the children of the parent
* Object. By default this will call getChildCount() and then create an incremental
* list of indexes from 0 to the count-1.
* <p>
* These indexes are to be used by you to reference that child relative to the parent.
**/
//-------------------------------------------------------------------
protected long [] getChildIndexes(Object parent)
//-------------------------------------------------------------------
{
	int size = getChildCount(parent);
	long [] refs = new long[size];
	for (int i = 0; i<size; i++) refs[i] = i;
	return refs;
}
/**
* If you do not override getChildIndexes() you must implement this.
**/
//-------------------------------------------------------------------
protected int getChildCount(Object parent)
//-------------------------------------------------------------------
{
	return 0;
}
/**
* Use this to change the flags for the child indexes of the specified parent.
* The flags are created with each child being set as CanExpand and IsNode. By default
* this will leave the entries as they are.
**/
//-------------------------------------------------------------------
protected void adjustFlags(Object parent,long [] indexes,byte [] flags){}
//-------------------------------------------------------------------

//===================================================================
public String getDisplayString(int forLine,TreeNode node)
//===================================================================
{
	return getDisplayString(getParentObject(forLine),getIndexFor(forLine));
}
//===================================================================
public IImage getIcon(int forLine,TreeNode node)
//===================================================================
{
	return getIcon(getParentObject(forLine),getIndexFor(forLine));
}
//===================================================================
public byte getFlags(int forLine,TreeNode node)
//===================================================================
{
	return getFlags(getParentObject(forLine),getIndexFor(forLine),(byte)flagsAt(forLine));
}
/**
* Override this to get the display string for the child of the parent.
**/
//-------------------------------------------------------------------
protected String getDisplayString(Object parentNode,int childIndex)
//-------------------------------------------------------------------
{
	if (parentNode == null) return "Tree";
	return parentNode.toString()+"("+childIndex+")";
}
/**
* Override this to get the icon for the child of the parent.
**/
//-------------------------------------------------------------------
protected IImage getIcon(Object parentNode,int childIndex)
//-------------------------------------------------------------------
{
	return null;
}
/**
* This is only called if "dynamicCanExpand" is set true. This will then be called each time
* the node is to be displayed.<p>
* Override this to return the flags (IsNode and/or CanExpand) for the child of the parent.
**/
//-------------------------------------------------------------------
protected byte getFlags(Object parentNode,int childIndex,byte savedFlags)
//-------------------------------------------------------------------
{
	return savedFlags;
}


//##################################################################
}
//##################################################################

