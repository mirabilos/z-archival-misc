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
public class LiveTreeNode extends LiveObject implements MutableTreeNode{
//##################################################################
/**
* The parent of this Node.
**/
protected TreeNode parent;
/**
* The children of this Node. This will be NULL until children are added.
**/
protected Vector children;

//===================================================================
public TreeNode getChild(int index)
//===================================================================
{
	if (children == null) return null;
	if (index >= children.size() || index < 0) return null;
	return (TreeNode)children.get(index);
}
//===================================================================
public Iterator getChildren()
//===================================================================
{
	if (children == null) return new ObjectIterator(null);
	else return children.iterator();
}
//===================================================================
public int getChildCount()
//===================================================================
{
	if (children == null) return 0;
	return children.size();
}
//===================================================================
public TreeNode getParent() {return parent;}
//===================================================================
public void setParent(TreeNode p) {parent = p;}
//===================================================================
public void removeChild(MutableTreeNode ch)
//===================================================================
{
	if (ch == null) return;
	if (children == null) return;
	int idx = children.find(ch);
	if (idx == -1) return;
	children.del(idx);
	ch.setParent(null);
	if (children.size() == 0) children = null;
}
//===================================================================
public void addChild(MutableTreeNode ch)
//===================================================================
{
	if (children == null) children = new Vector();
	insertChild(ch,children.size());
}
//===================================================================
public void insertChild(MutableTreeNode ch,int index)
//===================================================================
{
	if (ch == null) return;
	if (children == null) children = new Vector();
	if (children.find(ch) != -1) children.remove(ch);
	if (index < 0 || index > children.size()) index = children.size();
	children.insert(index,ch);
	ch.setParent(this);
}
//===================================================================
public int indexOfChild(TreeNode child)
//===================================================================
{
	if (children == null) return -1;
	return children.find(child);
}
//===================================================================
public int [] addressOfChild(TreeNode child)
//===================================================================
{
	return addressOfChild(this,child);
}
//===================================================================
public TreeNode getChildAt(int [] address)
//===================================================================
{
	return getChildAt(this,address);
}
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
public static boolean isNamed(TreeNode node,String name)
//===================================================================
{
	if (node == null) return false;
	String nm = mString.removeTrailingSlash((node instanceof LiveData) ? ((LiveData)node).getName() : node.toString());
	if (nm == null) return false;
	return nm.equalsIgnoreCase(name);
}
//===================================================================
public static TreeNode findNamedChild(TreeNode parent,String name)
//===================================================================
{
	if (parent == null) return null;
	int n = parent.getChildCount();
	for (int i = 0; i<n; i++){
		TreeNode c = parent.getChild(i);
		if (isNamed(c,name)) return c;
	}
	return null;
}
/**
* This tries to find the address of a child given its path from the parent.
**/
//===================================================================
public static int [] addressOfChild(TreeNode parent,String childPath)
//===================================================================
{
	if (childPath == null || parent == null) return null;
	childPath = mString.removeTrailingSlash(childPath.replace('\\','/'));
	String [] all = mString.split(childPath,'/');
	if (all != null) if (all.length == 0) all = null;
	if (all == null) all = new String[]{""};

	TreeNode cur = parent;
	for (int i = 0; i<all.length; i++){
		cur.expand();
		cur = findNamedChild(cur,all[i]);
		if (cur == null) return null;
	}
	return addressOfChild(parent,cur);
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
public boolean canExpand() {return getChildCount() != 0;}
public boolean expand(){return true;}
public boolean isLeaf() {return getChildCount() == 0;}
public boolean collapse() {return true;}
//##################################################################
}
//##################################################################


