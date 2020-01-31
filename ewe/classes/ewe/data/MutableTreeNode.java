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
public interface MutableTreeNode extends TreeNode{
//##################################################################
/**
* Set the parent of this TreeNode.
**/
public void setParent(TreeNode parent);
/**
* Insert a child at the specified index. If the index is greater than
* the number of children, the child will be appended to the child list.
* If the child is already in the child list it will be removed and then
* re-inserted at the specified index.
**/
public void insertChild(MutableTreeNode child,int index);
/**
* Add a child to this TreeNode at the end of the child list.
* If the child is already in the child list it will be removed and then
* added to the end.
**/
public void addChild(MutableTreeNode child);
/**
* Remove a child from this TreeNode.
**/
public void removeChild(MutableTreeNode child);

//##################################################################
}
//##################################################################


