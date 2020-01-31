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
public class Browser {
//##################################################################
/*
//-------------------------------------------------------------------
protected void setupMainWindow()
//-------------------------------------------------------------------
{
	if (programArguments.length == 0)
		windowTitle = "DataStorage Browser";
	else
		windowTitle = new ewe.io.File(programArguments[0]).getFileExt();
	windowFlagsToSet |= FLAG_HAS_TITLE|FLAG_HAS_CLOSE_BUTTON;
	super.setupMainWindow();
}
*/
//===================================================================
public static void main(String args[])
//===================================================================
{
	ewe.sys.Vm.startEwe(args);
	String name = null;
	if (args.length != 0) name = args[0];
	if (name == null){
		ewe.filechooser.FileChooser fc = new ewe.filechooser.FileChooser(ewe.filechooser.FileChooser.OPEN,".");
		fc.title = "Choose Data Storage";
		fc.addMask("*.dat - Database Files");
		fc.addMask(fc.allFilesMask);
		if (fc.execute(null,Gui.FILL_FRAME) == Form.IDCANCEL) ewe.sys.Vm.exit(0);
		name = fc.getChosen();
	}
	if (name != null){
		DataStoreBrowser browser = new DataStoreBrowser();
		browser.setPreferredSize(240,320);
		ewe.io.File f = ewe.sys.Vm.newFileObject().getNew(name);
		Gui.setAppFormTitle(browser,f.getFileExt());
		try{
			browser.title = f.getName();
			browser.set(f.toRandomAccessStream("r"),true);
			browser.execute(null,Gui.FILL_FRAME);
		}catch(ewe.io.IOException e){
			new ReportException(e,null,null,false).execute();
		}
	}
	ewe.sys.Vm.exit(0);
}
//##################################################################
}
//##################################################################

