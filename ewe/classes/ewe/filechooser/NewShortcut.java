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
package ewe.filechooser;
import ewe.ui.*;
import ewe.fx.*;
import ewe.util.*;
import ewe.io.File;
import ewe.sys.Vm;
import ewe.sys.Time;
import ewe.sys.Locale;
import ewe.data.*;

//##################################################################
public class NewShortcut extends ewe.data.LiveObject{
//##################################################################

public String shortcutName = "Shortcut.lnk";
public String target = "";
public String arguments = "";

public void addToPanel(CellPanel cp,Editor ed,int which)
{
	InputStack is = new InputStack();
	is.doubleLined = true;
	mFileInput mf;
	is.add(ed.addField(new mInput(),"shortcutName"),"Name:");
	is.add(ed.addField(mf = new mFileInput(),"target"),"Target:");
	is.add(ed.addField(new mInput(),"arguments"),"Program Arguments:");
	mf.title = "Select Shortcut Target";
	mf.masks.add("*.exe;*.ewe - Exe/Ewe Files.");
	mf.masks.add("*.class - Java Class Files.");
	mf.masks.add(FileChooser.allFilesMask);
	cp.addLast(is);
	ed.title = "New Shortcut";
	ed.doButtons(ed.OKB|ed.CANCELB);
}

//===================================================================
public void fieldChanged(String fieldName,Editor ed)
//===================================================================
{
	if (fieldName.equals("target")){
		if (target.endsWith(".class")){
			MessageBox mb = new MessageBox("Run with Ewe?","Do you want to use the Ewe VM\nto run that class file?",MessageBox.MBYESNO);
			mb.windowFlagsToClear |= Window.FLAG_HAS_CLOSE_BUTTON;
			if (mb.execute() == mb.IDNO) return;
			String path = target.substring(0,target.length()-6);
			StringBuffer programDir = new StringBuffer();
			String cls = FileChooser.askClassName(target,programDir,null);
			if (cls == null) return;
			target = ewe.sys.Vm.getPathToEweVM();
			arguments = "/d \""+programDir.toString()+"\" "+cls;
			ed.toControls("arguments,target");
		}
	}
}
//##################################################################
}
//##################################################################

