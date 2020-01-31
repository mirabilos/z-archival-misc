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

//##################################################################
public class DataEntryNode extends StoredObject{
//##################################################################

public boolean displayLeaves = false;

//===================================================================
public DataEntryNode(){}
//===================================================================

//===================================================================
public DataEntryNode(DataEntry entry,boolean displayLeaves)
//===================================================================
{
	this.entry = entry;
	this.displayLeaves = displayLeaves;
}

//===================================================================
public String getName()
//===================================================================
{
	try{
		String nm = entry.getName();
		if (nm == null)
			if (parent == null) nm = "/";
			else nm = "";
		return nm;
	}catch(ewe.io.IOException e){
		return "IO Error!";
	}
}
//===================================================================
public DataEntryNode getNewFor(DataEntry de)
//===================================================================
{
	DataEntryNode den = (DataEntryNode)getNew();
	den.entry = de;
	den.storage = de.storage;
	den.displayLeaves = displayLeaves;
	den.made();
	return den;
}
//===================================================================
public boolean checkChildren(boolean doExpand)
//===================================================================
{
	if (doExpand) children = new Vector();
	IntArray got = entry.getAllChildIds(null);
	if (got.length == 0) return false;
	if (displayLeaves && !doExpand) return true;
	for (int i = 0; i<got.length; i++){
		DataEntry de = entry.get(got.data[i]);
		DataEntryNode den = getNewFor(de);
		if (!den.isLeaf() && !doExpand) return true;
		if (doExpand)
			if (displayLeaves || !den.isLeaf()){
				addChild(den);
			}
	}
	if (!doExpand) return false;
	return true;
}
/*
//===================================================================
public String [] list()
//===================================================================
{
	if (name == null) {
		String [] ret = (String[])new File("").getInfo(File.INFO_ROOT_LIST,null,null,0);
		if (ret == null) ret = new String[]{"/"};
		return ret;
	}
	else return toFile().list(null,File.LIST_DIRECTORIES_ONLY);
}
*/
//===================================================================
public void made(){}
//===================================================================

//===================================================================
public boolean canExpand()
//===================================================================
{
	return checkChildren(false);
}
//===================================================================
public boolean isLeaf() {return !entry.hasChildren();}
//===================================================================
public boolean expand()
//===================================================================
{
	checkChildren(true);
	return true;
}
//##################################################################
}
//##################################################################

