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

//##################################################################
public interface TreeNode{
//##################################################################
/**
* Get the parent of this object.
**/
public TreeNode getParent();
/**
* Get the child at the specified index.
**/
public TreeNode getChild(int index);
/**
* Get the number of children that this object has.
**/
public int getChildCount();
/**
* Get an Iterator for the children.
**/
public ewe.util.Iterator getChildren();
/**
* Return the index of the specified child. This will return -1 if the
* parameter is not a child of this TreeNode.
**/
public int indexOfChild(TreeNode child);
/**
* Returns whether this node is expandable.
**/
public boolean canExpand();
/**
* Tell it to expand (ie gather its children).
**/
public boolean expand();
/**
* Tell it that it can release its children.
**/
public boolean collapse();
/**
* Returns whether this is a node or leaf.
**/
public boolean isLeaf();
//##################################################################
}
//##################################################################


