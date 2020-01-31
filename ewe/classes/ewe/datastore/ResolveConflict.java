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
import ewe.sys.Time;
import ewe.sys.Handle;
import ewe.ui.*;

//##################################################################
public class ResolveConflict extends ewe.data.LiveObject{
//##################################################################

public boolean copyFromLocalToRemoteDatabase = false;
public boolean copyFromRemoteToLocalDatabase = false;
public boolean resolveConflictLater = false;
public boolean takeThisActionForAllConflicts = false;

//===================================================================
public void fromCommand(int c)
//===================================================================
{
	copyFromLocalToRemoteDatabase = copyFromRemoteToLocalDatabase = resolveConflictLater = false;
	c = c & ~Synchronizer.ALWAYS;
	if (c == Synchronizer.RESOLVE_LATER) resolveConflictLater = true;
	else if (c == Synchronizer.REMOTE_TO_LOCAL) copyFromRemoteToLocalDatabase = true;
	else copyFromLocalToRemoteDatabase = true;
}
//===================================================================
public int toCommand()
//===================================================================
{
	int mask = takeThisActionForAllConflicts ? Synchronizer.ALWAYS : 0;
	if (copyFromLocalToRemoteDatabase) return mask|Synchronizer.LOCAL_TO_REMOTE;
	else if (copyFromRemoteToLocalDatabase) return mask|Synchronizer.REMOTE_TO_LOCAL;
	else return mask|Synchronizer.RESOLVE_LATER;
}
//-------------------------------------------------------------------
Control addCheckbox(Editor ed,CellPanel to,String name,CheckBoxGroup group)
//-------------------------------------------------------------------
{
	mCheckBox cb = new mCheckBox(InputStack.nameToPrompt(name));
	ed.addField(to.addLast(cb),name); if (group != null) cb.setGroup(group);
	return cb;
}
//===================================================================
public void addToPanel(CellPanel cp,Editor ed,int which)
//===================================================================
{
	CellPanel c = new CellPanel(); c.setBorder(c.EDGE_ETCHED,2);
	CheckBoxGroup cbg = new CheckBoxGroup();
	addCheckbox(ed,c,"copyFromLocalToRemoteDatabase",cbg);
	addCheckbox(ed,c,"copyFromRemoteToLocalDatabase",cbg);
	addCheckbox(ed,c,"resolveConflictLater",cbg);
	cp.addLast(c);
	addCheckbox(ed,cp,"takeThisActionForAllConflicts",null);
}
//##################################################################
}
//##################################################################

