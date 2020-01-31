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
package ewe.datastore;
import ewe.util.*;
import ewe.data.*;
import ewe.ui.*;

//##################################################################
public class DataStoreBrowser extends Editor{
//##################################################################

TreeControl tree;

//===================================================================
public void set(ewe.io.RandomAccessStream stream,boolean showLeavesInTree) throws ewe.io.IOException
//===================================================================
{
	DataStorage ds = new DataStorage(stream);
	set(ds,showLeavesInTree);
}
//===================================================================
public void set(DataStorage ds,boolean showLeavesInTree)
//===================================================================
{
	set(ds.getRoot(),showLeavesInTree);
}
//===================================================================
public void set(DataEntry root,boolean showLeavesInTree)
//===================================================================
{
	addLast(new ScrollBarPanel(tree = new TreeControl()));
	tree.backGround = ewe.fx.Color.White;
	DataEntryNode den = (DataEntryNode)myNode.getNew();
	den.entry = root;
	den.storage = root.storage;
	den.displayLeaves = showLeavesInTree;
	den.made();
	tree.getTreeTableModel().setRootObject(den);
}

DataEntryNode myNode;

//===================================================================
public DataStoreBrowser() {this(new DataEntryNode());}
//===================================================================
public DataStoreBrowser(DataEntryNode node)
//===================================================================
{
	myNode = node;
}
//##################################################################
}
//##################################################################

