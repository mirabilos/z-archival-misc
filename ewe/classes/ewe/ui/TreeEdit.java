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
import ewe.reflect.*;
import ewe.util.*;
import ewe.data.*;
import ewe.fx.*;

//##################################################################
public class TreeEdit extends Editor{
//##################################################################

//===================================================================
public static ewe.fx.IImage defaultIcon;
//===================================================================

protected TreeNode root;

static
{
	defaultIcon = new ewe.fx.mImage("ewe/editsmall.bmp",ewe.fx.Color.White);
}
/**
* The tree being used.
**/
public TreeControl tree;

//===================================================================
public TreeEdit()
//===================================================================
{
	this(null,null);
}

//===================================================================
public TreeEdit(TreeControl treeControl,Object objectOrClassOrReflectToEdit)
//===================================================================
{
	super(objectOrClassOrReflectToEdit);
	if (treeControl == null) treeControl = new TreeControl();
	tree = treeControl;
	addLast(new ScrollBarPanel(tree));
}
/**
 * Set the root tree node of the editor. If both parameters specified are null then the root
	of the tree will be hidden.
 * @param name The name for the root. This can be null.
 * @param icon The icon for the root. This can be null.
 * @return A TreeNode representing the root object.
 */
//===================================================================
public TreeNode setRoot(String name,IImage icon)
//===================================================================
{
	treeNode tn = new treeNode(name,icon);
	root = tn;
	if (name == null && icon == null) tree.getTreeTableModel().hideRoot = true;
	return tn;
}
/**
 * Get the root TreeNode for the Tree.
 * @return The root TreeNode
 */
//===================================================================
public TreeNode getRoot() {return root;}
//===================================================================


/**
 * Add a new control in a new TreeNode to the root tree node.
 * @param name The display name of the node.
 * @param icon The display icon of the node.
 * @param c The control to be added.
 * @param fieldName The name of the field for the control.
 * @return The new TreeNode which contains the control.
 */
//===================================================================
public TreeNode add(String name,IImage icon,Control c,String fieldName)
//===================================================================
{
	return add(getRoot(),name,icon,c,fieldName);
}
/**
 * Add a new control in a new TreeNode to the specified tree node.
 * @param node The node to add the control to.
 * @param name The display name of the node.
 * @param icon The display icon of the node.
 * @param c The control to be added.
 * @param fieldName The name of the field for the control.
 * @return The new TreeNode which contains the control.
 */
//===================================================================
public TreeNode add(TreeNode node,String name,IImage icon,Control c,String fieldName)
//===================================================================
{
	treeNode tn = new treeNode(name,icon);
	add(node,tn);
	if (fieldName != null) addField(c,fieldName);
	tn.addChild(new TreeControl.ControlTreeNode(c,null));
	return tn;
}
/**
 * Add an empty new TreeNode to the specified tree node.
 * @param node The node to add the new node to.
 * @param name The display name of the node.
 * @param icon The display icon of the node.
 * @return The new TreeNode created.
 */
//===================================================================
public TreeNode add(TreeNode node,String name,IImage icon)
//===================================================================
{
	treeNode tn = new treeNode(name,icon);
	add(node,tn);
	//tree.getTreeTableModel().inserted(node,tn,false);
	return tn;
}
//-------------------------------------------------------------------
protected void add(TreeNode node,TreeNode child)
//-------------------------------------------------------------------
{
	if (node instanceof MutableTreeNode && child instanceof MutableTreeNode){
		((MutableTreeNode)node).addChild((MutableTreeNode)child);
	}
}

//===================================================================
public void make(boolean reMake)
//===================================================================
{
	tree.getTreeTableModel().hasControls = true;
	tree.getTreeTableModel().setRootObject(root);
	super.make(reMake);
}
	//##################################################################
	class treeNode extends ewe.data.LiveTreeNode{
	//##################################################################
	public String name;
	public ewe.fx.IImage icon;

	treeNode(String nm,ewe.fx.IImage ic) {name = nm; icon = ic;}

	public String getName() {return name;}
	public ewe.fx.IImage getIcon() {return icon == null ? defaultIcon : icon;}

	//##################################################################
	}
	//##################################################################
/*
public static void main(String args[])
{
	ewe.sys.Vm.startEwe(args);
	samples.data.PersonInfo pi;
	final TreeEdit ed = new TreeEdit(null,pi = new samples.data.PersonInfo());
	ed.resizable = true;
	ed.modify(ed.DrawFlat,0);
	ed.setRoot(null,null);
	//ed.setRoot("A Person Info",null);
	ed.add("Personal Info",null,pi.getEditor(0),"this");
	ed.add("More Info",null,pi.getEditor(2),"this");
	ed.addLast(new mButton("Homer"){
		public void doAction(int how){
			samples.data.PersonInfo pi = new samples.data.PersonInfo();
			pi.set(pi.homer,null);
			ed.setObject(pi);
		}
	}).setCell(HSTRETCH);
	ed.execute();
	ewe.sys.Vm.exit(0);
}
*/
//##################################################################
}
//##################################################################

